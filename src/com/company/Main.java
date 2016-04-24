package com.company;

import com.company.models.Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception
    {

        ObjectMapper mapper= new ObjectMapper();

        File json = new File("E:\\model.json");
        Model model = mapper.readValue(json, Model.class);


        // Sun, 11 Oct 2009 21:49:13 GMT
        final String DATEFORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        final String utcTime = sdf.format(new Date());


        URL listUrl = new URL("http://riccardonci.blob.core.windows.net/?comp=list");

        URL url = new URL("http://riccardonci.blob.core.windows.net/riccardocontainer?restype=container");

        HttpHost p = new HttpHost("localhost", 8888, "http");
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(p);
        HttpClient httpclient = HttpClientBuilder.create()/*.setRoutePlanner(routePlanner)*/.disableContentCompression().disableConnectionState().build();

        HttpRequestBase httpost = new HttpDelete("http://riccardonci.blob.core.windows.net/test5?restype=container");

        httpost.addHeader("x-ms-date", utcTime);
        httpost.addHeader("x-ms-version", "2015-04-05");

        String secret = "B7wPXLWFU4BP62Z4fKBvQfiIRsMblRkzB49CaBGms8HMwj6X6q5a1CellQeSglRcmdtQz+bgxkC0reNmu9GxPQ==";

        String sign = SignRequest(httpost, "riccardonci", secret);

        HttpResponse resp = httpclient.execute(httpost);

        HttpEntity entity = resp.getEntity();
        System.out.println("Request Handled?: " + resp.getStatusLine());
        InputStream in = entity.getContent();


    }
    
    private static String SignRequest(HttpRequestBase request, String accountName, String sharedKey) throws NoSuchAlgorithmException, InvalidKeyException
    {
        StringBuffer buffer = new StringBuffer(request.getMethod());
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

        return buffer.toString();
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
    
    private static void AppendHeaderValue(StringBuffer buffer, HttpRequestBase request, String headerName)
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
    
    private static String BuildCanonicalizedHeaders(HttpRequestBase request)
    {
        StringBuffer buffer = new StringBuffer();
        Header[] headers = request.getAllHeaders();
        Arrays.sort(headers, new Comparator<Header>() {
            @Override
            public int compare(Header o1, Header o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

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

    private static String BuildCanonicalizedResource(HttpRequestBase request, String accountName)
    {
        StringBuffer buffer = new StringBuffer("/");
        buffer.append(accountName);
        URI requestUri = request.getURI();

        buffer.append(requestUri.getPath());

        List<NameValuePair> params = URLEncodedUtils.parse(requestUri, "UTF-8");
        for (NameValuePair pair : params ) {
            AppendValue(buffer, pair.getName() + ":" + pair.getValue());
        }

        return buffer.toString();
    }
}
