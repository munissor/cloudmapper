'use strict';


module.exports = {
    buildSignature: _buildSignatureCallback,
};

function _buildSignatureCallback(type, req, config) {
    if( type === 'aws-v4')
    {
        return _awsV4SignRequest;
    }
    else if (type == 'azure') {
        return _azureSignRequest;
    }

    return null;
}


function  _awsV4SignRequest(clientRequest){
}



function _azureSignRequest(clientRequest){
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