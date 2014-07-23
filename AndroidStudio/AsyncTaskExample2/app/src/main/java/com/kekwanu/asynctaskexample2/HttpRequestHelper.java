package com.kekwanu.asynctaskexample2;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by onwuneme on 6/13/14.
 */
public class HttpRequestHelper {
    private static final String TAG = HttpRequestHelper.class.getCanonicalName();

    public HttpRequestHelper(){
        Log.i(TAG, "HttpRequestHelper - constructor");
    }

    public HttpResponse makeRequest(String uri, String json) {
        Log.i(TAG, "makeRequest");

        try { //HttpGet t = new HttpGet(uri);
            HttpPost httpPost = new HttpPost(uri);

            if (!json.equals("")) {
                httpPost.setEntity(new StringEntity(json));
            }
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            //return new DefaultHttpClient().execute(httpPost);

            return sslClient(new DefaultHttpClient()).execute(httpPost);
        }
        catch (UnsupportedEncodingException e) {
            Log.i(TAG, "makeRequest - UnsupportedEncodingException: " + e.getMessage());

            e.printStackTrace();
        }
        catch (ClientProtocolException e) {
            Log.i(TAG, "makeRequest - ClientProtocolException: " + e.getMessage());

            e.printStackTrace();
        }
        catch (IOException e) {
            Log.i(TAG, "makeRequest - IOexception: " + e.getMessage());

            e.printStackTrace();
        }

        return null;
    }

    private HttpClient sslClient(HttpClient client) {
        Log.i(TAG, "sslClient");

        try {
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = client.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, client.getParams());
        }
        catch (Exception ex) {
            return null;
        }
    }
}
