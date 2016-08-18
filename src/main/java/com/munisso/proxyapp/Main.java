//package com.munisso.proxyapp;
//
//import org.apache.http.*;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.*;
//import org.apache.http.client.utils.URLEncodedUtils;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
//
//import org.apache.http.util.EntityUtils;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.net.*;
//import java.nio.charset.Charset;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class Main {
//
//    public static void main(String[] args) throws Exception
//    {
//
//        //String dir = System.getProperty("user.dir");
//
//
////
////        HttpEntity entity;
////        HttpResponse resp;
////
////        // Sun, 11 Oct 2009 21:49:13 GMT
////        final String DATEFORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
////
////        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
////        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
////        final String utcTime = sdf.format(new Date());
////
////
////        String listUrl = "http://localhost:8080/?comp=list";
////        String createUrl = "http://localhost:8080/testapicontainer3?restype=container";
////        String listBlobsUrl = "http://localhost:8080/testapicontainer?restype=container&comp=list";
////        String createBlobUrl = "http://localhost:8080/testapicontainer/myblob";
////
////        //URL url = new URL("http://riccardonci.blob.core.windows.net/riccardocontainer?restype=container");
////
////        final boolean useProxy = false;
////        HttpClientBuilder builder = HttpClientBuilder.create();
////        builder = builder.disableContentCompression().disableConnectionState();
////
////        if(useProxy)
////        {
////            HttpHost p = new HttpHost("localhost", 8888, "http");
////            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(p);
////            builder = builder.setRoutePlanner(routePlanner);
////        }
////
////        builder.addInterceptorLast((HttpRequestInterceptor) (request, context) -> {
////            try {
////                SignRequest(request, "riccardonci", secret);
////            }
////            catch (Exception e){
////
////            }
////
////        });
////
////        HttpClient httpclient = builder.build();
////
////
////        // LIST CONTAINER
////        HttpRequestBase listContainer = new HttpGet(listUrl);
////        listContainer.addHeader("x-ms-date", utcTime);
////        listContainer.addHeader("x-ms-version", "2015-04-05");
////
////        resp = httpclient.execute(listContainer);
////        entity = resp.getEntity();
////        EntityUtils.consume(entity);
//
//        // PUT CONTAINER
//        //HttpRequestBase createContainer = new HttpPut(createUrl);
//        //createContainer.addHeader("x-ms-date", utcTime);
//        //createContainer.addHeader("x-ms-version", "2015-04-05");
//        //SignRequest(createContainer, "riccardonci", secret);
//
//        //resp = httpclient.execute(createContainer);
//        //entity = resp.getEntity();
//        //EntityUtils.consume(entity);
//
//        // LIST BLOBS CONTAINER
//        //HttpRequestBase listBlobs = new HttpGet(listBlobsUrl);
//        //listBlobs.addHeader("x-ms-date", utcTime);
//        //listBlobs.addHeader("x-ms-version", "2015-04-05");
//        //SignRequest(listBlobs, "riccardonci", secret);
//
//        //resp = httpclient.execute(listBlobs);
//        //entity = resp.getEntity();
//        //EntityUtils.consume(entity);
//
//        // PUT BLOB CONTAINER
//        //HttpPut createBlob = new HttpPut(createBlobUrl);
//        //createBlob.addHeader("x-ms-blob-type", "BlockBlob");
//        //createBlob.addHeader("x-ms-date", utcTime);
//        //createBlob.addHeader("x-ms-version", "2015-04-05");
//
//
//        //createBlob.addHeader("x-ms-date", utcTime);
//
//        //HttpEntity body =  new StringEntity("test blob", "UTF-8");
//        //createBlob.addHeader("Content-Length", Long.toString(body.getContentLength()));
//
//        //createBlob.setEntity(body);
//        //Header[] h = createBlob.getAllHeaders();
//        //createBlob.completed();
//
//        //SignRequest(createBlob, "riccardonci", secret);
//
//        //resp = httpclient.execute(createBlob);
//        //entity = resp.getEntity();
//        //EntityUtils.consume(entity);
//
//
//        //HttpRequestBase httpost = new HttpDelete("http://riccardonci.blob.core.windows.net/test5?restype=container");
//
//        //httpost.addHeader("x-ms-date", utcTime);
//        //httpost.addHeader("x-ms-version", "2015-04-05");
//
//        //SignRequest(httpost, "riccardonci", secret);
//
//        //resp = httpclient.execute(httpost);
//
//        //entity = resp.getEntity();
//
////        System.out.println("Request Handled?: " + resp.getStatusLine());
////        InputStream in = entity.getContent();
//
//
//    }
//
//
//
//
//}
