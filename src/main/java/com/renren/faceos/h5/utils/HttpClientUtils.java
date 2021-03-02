package com.renren.faceos.h5.utils;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @ClassName: HttpsUtils
 * @Description: TODO(https post忽略证书请求)
 */
public class HttpClientUtils {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static SSLConnectionSocketFactory sslsf = null;
    private static PoolingHttpClientConnectionManager cm = null;
    private static SSLContextBuilder builder = null;
    protected static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    static {
        try {
            builder = new SSLContextBuilder();
            // 全部信任 不做身份鉴定
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            });
            sslsf = new SSLConnectionSocketFactory(builder.build(),
                    new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(HTTP, new PlainConnectionSocketFactory()).register(HTTPS, sslsf).build();
            cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(200);// max connection
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 功能描述：发送一个http post请求，并尝试将响应转换为JSON
     *
     * @param url
     * @param data
     * @return java.lang.String
     * @author pengjiaxin@faceos.com
     * @date 2019/6/25
     */
    public static String httpPost(String url, String data) throws Exception {
        logger.debug(url);
        logger.debug(data);
        String jsonResult = null;
        //发送get请求
        HttpClient client = new SSLClient();
        HttpPost request = new HttpPost(url);
        if (data != null) {
            StringEntity stringEntity = new StringEntity(data, "UTF-8");
            stringEntity.setContentEncoding("UTF-8");
            request.setHeader("Content-type", "application/json");
            request.setEntity(stringEntity);
        }
        HttpResponse response = client.execute(request);
        jsonResult = EntityUtils.toString(response.getEntity());
        logger.debug(jsonResult);
        return jsonResult;
    }

    public static CloseableHttpClient getHttpClient() throws Exception {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(cm)
                .setConnectionManagerShared(true).build();
        return httpClient;
    }


}


/**
 * 用于进行Https请求的HttpClient
 *
 * @author Devin <xxx>
 * @ClassName: SSLClient
 * @Description: TODO
 * @date 2017年2月7日 下午1:42:07
 */
class SSLClient extends DefaultHttpClient {
    public SSLClient() throws Exception {
        super();
        SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        ctx.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        ClientConnectionManager ccm = this.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", 443, ssf));
    }
}