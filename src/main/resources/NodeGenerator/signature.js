'use strict';
var awsSigner = require('aws4');
var request = require('request');
var config = require('config');
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
    _azureSignRequestSync(requestData, config.accountName, config.sharedKey);
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
}

/*
 private static void SignRequest(HttpRequest request, String accountName, String sharedKey) throws Exception
 {

 StringBuffer buffer = new StringBuffer(request.getRequestLine().getMethod());
 AppendHeaderValue(buffer, request, "Content-Encoding");
 AppendHeaderValue(buffer, request, "Content-Language");
 AppendHeaderValue(buffer, request, "Content-Length");
 AppendHeaderValue(buffer, request, "Content-MD5");
 AppendHeaderValue(buffer, request, "Content-Type");
 AppendHeaderValue(buffer, request, "Date");
 AppendHeaderValue(buffer, request, "If-Modified-Since");
 AppendHeaderValue(buffer, request, "If-Match");
 AppendHeaderValue(buffer, request, "If-None-Match");
 AppendHeaderValue(buffer, request, "If-Unmodified-Since");
 AppendHeaderValue(buffer, request, "Range");
 AppendValue(buffer, BuildCanonicalizedHeaders(request));
 AppendValue(buffer, BuildCanonicalizedResource(request, accountName));

 String hash = HashRequestString(buffer.toString(), sharedKey);

 request.addHeader("Authorization", "SharedKey " + accountName + ":" + hash);
 }

 private static String HashRequestString(String stringToSign, String sharedKey) throws NoSuchAlgorithmException, InvalidKeyException
 {
 final Charset utf8 = Charset.forName("UTF-8");
 final Mac hmacSha256 = Mac.getInstance("HmacSHA256");
 final SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(Base64.getDecoder().decode(sharedKey), "HmacSHA256");
 hmacSha256.init(secret_key);
 final byte[] mac_data = hmacSha256.doFinal(stringToSign.getBytes(utf8));

 return Base64.getEncoder().encodeToString(mac_data);
 }

 private static void AppendValue(StringBuffer buffer, String value)
 {
 buffer.append("\n");
 buffer.append(value);
 }

*/

function _azureAppendHeaderValue(buffer, requestData, headerName) {
    var value = requestData[headerName] || "";
    return _azureAppendValue(buffer, value);
}

function _azureAppendValue(buffer, value) {
    return buffer + "\n" + value;
}

/*

 private static void AppendHeaderValue(StringBuffer buffer, HttpRequest request, String headerName)
 {
 Header header = request.getFirstHeader(headerName);
 String value;
 if(header != null )
 {
 value = header.getValue();
 }
 else
 {
 value = "";
 }
 AppendValue(buffer, value);
 }


*/

function _azureBuildCanonicalizedHeaders(requestData){
    var buffer = "";
}

/*

 private static String BuildCanonicalizedHeaders(HttpRequest request)
 {
 StringBuffer buffer = new StringBuffer();
 Header[] headers = request.getAllHeaders();
 Arrays.sort(headers, (o1, o2) -> o1.getName().compareTo(o2.getName()));

 for (Header item : headers) {
 String lcName = item.getName().toLowerCase();
 if(lcName.startsWith("x-ms-")) {
 if (buffer.length() > 0) {
 buffer.append("\n");
 }

 buffer.append(lcName);
 buffer.append(":");
 buffer.append(item.getValue());
 }
 }

 return buffer.toString();
 }

*/

function _azureBuildCanonicalizedResource(requestData) {
    var buffer = "";
    return buffer;
}

/*
 private static String BuildCanonicalizedResource(HttpRequest request, String accountName) throws Exception
 {
 StringBuffer buffer = new StringBuffer("/");
 buffer.append(accountName);
 String requestUri = request.getRequestLine().getUri();
 URI uri = new URI(requestUri);

 buffer.append(uri.getPath());

 List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
 params.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));

 for (NameValuePair pair : params ) {
 AppendValue(buffer, pair.getName() + ":" + pair.getValue());
 }

 return buffer.toString();
 }
 */