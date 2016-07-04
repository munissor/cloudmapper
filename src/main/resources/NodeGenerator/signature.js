'use strict';
var awsSigner = require('aws4');
var config = require('config');
var url = require('url');

module.exports = {
    buildSignature: _buildSignature
};

function _buildSignature(type, request) {
    if( type === 'aws-v4')
        _awsV4SignRequest(request);
    else if (type == 'azure')
        _azureSignRequest(request);
    else
        throw new Error('Unsupported signature algorithm');
}


function  _awsV4SignRequest(request) {
    // request: { path | body, [host], [method], [headers], [service], [region] }
    // credentials: { accessKeyId, secretAccessKey, [sessionToken] }
    var parsedUrl = url.parse(request.url);
    var idx = parsedUrl.host.indexOf('.');
    var service = parsedUrl.host.substring(0, idx);

    // add other name of services that require signatures in the query rather than headers
    var signQuery = service === 'sqs';

    var r = {
        path: parsedUrl.path,
        body: request.body,
        host: parsedUrl.host,
        method: request.method,
        headers: request.headers,
        service: service,
        signQuery: signQuery
    };

    var credentials = {
        accessKeyId: config.accessKeyID,
        secretAccessKey: config.secretAccessKey
    };


    var signed = awsSigner.sign(r, credentials);
    if(signQuery) {
        request.url =
            parsedUrl.protocol +
            (parsedUrl.slashes ? '//' : '') +
            signed.host +
            signed.path;
    }
}


function _azureSignRequest(request) {
    return request;
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