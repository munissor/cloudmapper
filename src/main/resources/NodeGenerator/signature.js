'use strict';
var awsSigner = require('aws4');
var request = require('request');
var config = require('config');
var crypto = require('crypto');
var url = require('url');
var jwt = require('jwt-simple');

module.exports = {
    buildSignature: _buildSignature
};

function _buildSignature(type, requestData, callback) {
    if( type === 'aws-v4')
        _awsV4SignRequest(requestData, callback);
    else if( type === 'google-service-token')
        _googleServiceToken(requestData, callback);
    else if (type == 'azure')
        _azureSignRequest(requestData, callback);
    else
        throw new Error('Unsupported signature algorithm');
}

function  _awsV4SignRequest(requestData, callback) {
    // request: { path | body, [host], [method], [headers], [service], [region] }
    // credentials: { accessKeyId, secretAccessKey, [sessionToken] }
    var parsedUrl = url.parse(requestData.url);
    var idx = parsedUrl.host.indexOf('.');
    var service = parsedUrl.host.substring(0, idx);

    // add other name of services that require signatures in the query rather than headers
    var signQuery = service === 'sqs';

    var r = {
        path: parsedUrl.path,
        body: requestData.body,
        host: parsedUrl.host,
        method: requestData.method,
        headers: requestData.headers,
        service: service,
        signQuery: signQuery
    };

    var credentials = {
        accessKeyId: config.accessKeyID,
        secretAccessKey: config.secretAccessKey
    };


    var signed = awsSigner.sign(r, credentials);
    if(signQuery) {
        requestData.url =
            parsedUrl.protocol +
            (parsedUrl.slashes ? '//' : '') +
            signed.host +
            signed.path;
    }

    callback(requestData);
}

var google_token = null;
var google_expire = 0;

function  _googleServiceToken(requestData, callback) {
    var date = new Date().getTime() / 1000;
    if( date > google_expire ){
        var exp = (new Date().getTime() + 60 * 60 * 1000)/1000;

        var payload = {
            "iss":config.account,
            "scope":config.scope,
            "aud":"https://www.googleapis.com/oauth2/v4/token",
            "iat": date,
            "exp": exp
        };

        var token = jwt.encode(payload, config.privateKey, 'RS256');

        var options = {
            method: 'POST',
            url: "https://www.googleapis.com/oauth2/v4/token",
            form: {
                grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
                assertion: token
            },
            headers: []
        };

        request(options, function(error, response, body){
            google_token = JSON.parse(response.body).access_token;
            google_expire = exp - 10; // keep 10 seconds between the token  expires and the last request is made
            requestData.headers['Authorization'] = "Bearer " + google_token;
            callback(requestData);
        });
    }
    else {
        requestData.headers['Authorization'] = "Bearer " + google_token;
        callback(requestData);
    }
}


function _azureSignRequest(requestData, callback) {
    _azureSignRequestSync(requestData, config.AccountName, config.sharedKey);
    callback(requestData);
}


function _azureSignRequestSync(requestData, accountName, sharedKey){
    var buffer = requestData.method;

    buffer = _azureAppendHeaderValue(buffer, requestData, "Content-Encoding");
    buffer = _azureAppendHeaderValue(buffer, requestData, "Content-Language");
    buffer = _azureAppendHeaderValue(buffer, requestData, "Content-Length");
    buffer = _azureAppendHeaderValue(buffer, requestData, "Content-MD5");
    buffer = _azureAppendHeaderValue(buffer, requestData, "Content-Type");
    buffer = _azureAppendHeaderValue(buffer, requestData, "Date");
    buffer = _azureAppendHeaderValue(buffer, requestData, "If-Modified-Since");
    buffer = _azureAppendHeaderValue(buffer, requestData, "If-Match");
    buffer = _azureAppendHeaderValue(buffer, requestData, "If-None-Match");
    buffer = _azureAppendHeaderValue(buffer, requestData, "If-Unmodified-Since");
    buffer = _azureAppendHeaderValue(buffer, requestData, "Range");
    buffer = _azureAppendValue(buffer, _azureBuildCanonicalizedHeaders(requestData));
    buffer = _azureAppendValue(buffer, _azureBuildCanonicalizedResource(requestData, accountName));

    var hash = _azureHashRequestString(buffer, sharedKey);

    requestData.headers["Authorization"] = "SharedKey " + accountName + ":" + hash;
}


function _azureHashRequestString(buffer, sharedKey){
    return crypto.createHmac('SHA256', "sharedKey").update(buffer).digest('base64');
}

function _azureAppendHeaderValue(buffer, requestData, headerName) {
    var value = requestData.headers[headerName] || '';
    return _azureAppendValue(buffer, value);
}

function _azureAppendValue(buffer, value) {
    return buffer + '\n' + value;
}

function _azureBuildCanonicalizedHeaders(requestData){
    var buffer = "";
    var keys = Object.keys(requestData.headers).sort();
    keys.forEach(function(k){
        var lcName = k.toLowerCase();
        if(lcName.indexOf('x-ms-') === 0){
            if(buffer){
                buffer = buffer + '\n';
            }
            buffer = buffer + lcName + ':' + requestData.headers[k];
        }
    });

    return buffer;
}


function _azureBuildCanonicalizedResource(requestData, accountName) {
    var buffer = "/" + accountName;
    var parsedUrl = url.parse(requestData.url, true);
    buffer = buffer + parsedUrl.path;
    var keys = Object.keys(parsedUrl.query).sort();
    keys.forEach(function(k){
        buffer = _azureAppendValue(k + ':' + parsedUrl.query[k]);
    });

    return buffer;
}
