package com.munisso;

import com.munisso.models.Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void xmain(String[] args) throws Exception
    {

        //String dir = System.getProperty("user.dir");

        ObjectMapper mapper= new ObjectMapper();

        File json = new File("./Resources/azure_storage.json");
        Model model = mapper.readValue(json, Model.class);

        HttpEntity entity;
        HttpResponse resp;

        // Sun, 11 Oct 2009 21:49:13 GMT
        final String DATEFORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        final String utcTime = sdf.format(new Date());
        final String secret = "B7wPXLWFU4BP62Z4fKBvQfiIRsMblRkzB49CaBGms8HMwj6X6q5a1CellQeSglRcmdtQz+bgxkC0reNmu9GxPQ==";

        String listUrl = "http://riccardonci.blob.core.windows.net/?comp=list";
        String createUrl = "http://riccardonci.blob.core.windows.net/testapicontainer?restype=container";
        String listBlobsUrl = "http://riccardonci.blob.core.windows.net/testapicontainer?restype=container&comp=list";
        String createBlobUrl = "http://riccardonci.blob.core.windows.net/testapicontainer/myblob";

        //URL url = new URL("http://riccardonci.blob.core.windows.net/riccardocontainer?restype=container");

        final boolean useProxy = false;
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder = builder.disableContentCompression().disableConnectionState();

        if(useProxy)
        {
            HttpHost p = new HttpHost("localhost", 8888, "http");
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(p);
            builder = builder.setRoutePlanner(routePlanner);
        }

        builder.addInterceptorLast((HttpRequestInterceptor) (request, context) -> {
            try {
                SignRequest(request, "riccardonci", secret);
            }
            catch (Exception e){

            }

        });

        HttpClient httpclient = builder.build();


        // LIST CONTAINER
        HttpRequestBase listContainer = new HttpGet(listUrl);
        listContainer.addHeader("x-ms-date", utcTime);
        listContainer.addHeader("x-ms-version", "2015-04-05");
        //SignRequest(listContainer, "riccardonci", secret);

        resp = httpclient.execute(listContainer);
        entity = resp.getEntity();
        EntityUtils.consume(entity);

        // PUT CONTAINER
        HttpRequestBase createContainer = new HttpPut(createUrl);
        createContainer.addHeader("x-ms-date", utcTime);
        createContainer.addHeader("x-ms-version", "2015-04-05");
        //SignRequest(createContainer, "riccardonci", secret);

        //resp = httpclient.execute(createContainer);
        //entity = resp.getEntity();
        // EntityUtils.consume(entity);

        // LIST BLOBS CONTAINER
        HttpRequestBase listBlobs = new HttpGet(listBlobsUrl);
        listBlobs.addHeader("x-ms-date", utcTime);
        listBlobs.addHeader("x-ms-version", "2015-04-05");
        //SignRequest(listBlobs, "riccardonci", secret);

        resp = httpclient.execute(listBlobs);
        entity = resp.getEntity();
        EntityUtils.consume(entity);

        // PUT BLOB CONTAINER
        HttpPut createBlob = new HttpPut(createBlobUrl);
        createBlob.addHeader("x-ms-blob-type", "BlockBlob");
        createBlob.addHeader("x-ms-date", utcTime);
        createBlob.addHeader("x-ms-version", "2015-04-05");


        createBlob.addHeader("x-ms-date", utcTime);

        HttpEntity body =  new StringEntity("test blob", "UTF-8");
        //createBlob.addHeader("Content-Length", Long.toString(body.getContentLength()));

        createBlob.setEntity(body);
        Header[] h = createBlob.getAllHeaders();
        //createBlob.completed();

        //SignRequest(createBlob, "riccardonci", secret);

        resp = httpclient.execute(createBlob);
        entity = resp.getEntity();
        EntityUtils.consume(entity);


        HttpRequestBase httpost = new HttpDelete("http://riccardonci.blob.core.windows.net/test5?restype=container");

        httpost.addHeader("x-ms-date", utcTime);
        httpost.addHeader("x-ms-version", "2015-04-05");

        SignRequest(httpost, "riccardonci", secret);

        resp = httpclient.execute(httpost);

        entity = resp.getEntity();

//        System.out.println("Request Handled?: " + resp.getStatusLine());
//        InputStream in = entity.getContent();


    }


    
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
}
