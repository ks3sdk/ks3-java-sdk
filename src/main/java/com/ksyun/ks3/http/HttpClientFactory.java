package com.ksyun.ks3.http;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.ChallengeState;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.exception.Ks3ClientException;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月14日 下午8:05:52
 * 
 * @description 
 **/
public class HttpClientFactory {
	public HttpClient createHttpClient() {
    	ClientConfig config = ClientConfig.getConfig();
        /* Set HTTP client parameters */
        HttpParams httpClientParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpClientParams, config.getInt(ClientConfig.CONNECTION_TIMEOUT));
        HttpConnectionParams.setSoTimeout(httpClientParams, config.getInt(ClientConfig.SOCKET_TIMEOUT));
        HttpConnectionParams.setStaleCheckingEnabled(httpClientParams, true);
        HttpConnectionParams.setTcpNoDelay(httpClientParams, true);

        int socketSendBufferSizeHint = config.getInt(ClientConfig.SOCKET_SEND_BUFFER_SIZE_HINT);
        int socketReceiveBufferSizeHint = config.getInt(ClientConfig.SOCKET_RECEIVE_BUFFER_SIZE_HINT);
        if (socketSendBufferSizeHint > 0 || socketReceiveBufferSizeHint > 0) {
            HttpConnectionParams.setSocketBufferSize(httpClientParams,
                    Math.max(socketSendBufferSizeHint, socketReceiveBufferSizeHint));
        }

        PoolingClientConnectionManager connectionManager = ConnectionManagerFactory
                .createPoolingClientConnManager(httpClientParams);
        DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, httpClientParams);
        httpClient.setRedirectStrategy(new LocationHeaderNotRequiredRedirectStrategy());
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(config.getInt(ClientConfig.MAX_RETRY), false));
        
        try {
            Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
            SSLSocketFactory sf = new SSLSocketFactory(
                    SSLContext.getDefault(),
                    SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            Scheme https = new Scheme("https", 443, sf);
            SchemeRegistry sr = connectionManager.getSchemeRegistry();
            sr.register(http);
            sr.register(https);
        } catch (NoSuchAlgorithmException e) {
            throw new Ks3ClientException("Unable to access default SSL context", e);
        }

        /* Set proxy if configured */
        String proxyHost = config.getStr(ClientConfig.PROXY_HOST);
        int proxyPort = config.getInt(ClientConfig.PROXY_PORT);
        if (proxyHost != null && proxyPort > 0) {
        	
            HttpHost proxyHttpHost = new HttpHost(proxyHost, proxyPort);
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost);

            String proxyUsername    = config.getStr(ClientConfig.PROXY_USER_NAME);
            String proxyPassword    = config.getStr(ClientConfig.PROXY_PASSWORD);
            String proxyDomain      = config.getStr(ClientConfig.PROXY_DAMAIN);
            String proxyWorkstation = config.getStr(ClientConfig.PROXY_WORKSTATION);

            if (proxyUsername != null && proxyPassword != null) {
                httpClient.getCredentialsProvider().setCredentials(
                        new AuthScope(proxyHost, proxyPort),
                        new NTCredentials(proxyUsername, proxyPassword, proxyWorkstation, proxyDomain));
            }

            // Add a request interceptor that sets up proxy authentication pre-emptively if configured
            if (config.getBoolean(ClientConfig.IS_PREEMPTIVE_BASIC_PROXY_AUTH)){
                httpClient.addRequestInterceptor(new PreemptiveProxyAuth(proxyHttpHost), 0);
            }
        }
        httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        return httpClient;
    }

    /**
     * Customization of the default redirect strategy provided by HttpClient to be a little
     * less strict about the Location header to account for S3 not sending the Location
     * header with 301 responses.
     */
    private static final class LocationHeaderNotRequiredRedirectStrategy
            extends DefaultRedirectStrategy {

        @Override
        public boolean isRedirected(HttpRequest request,
                HttpResponse response, HttpContext context) throws ProtocolException {
            int statusCode = response.getStatusLine().getStatusCode();
            Header locationHeader = response.getFirstHeader("location");

            // Instead of throwing a ProtocolException in this case, just
            // return false to indicate that this is not redirected
            if (locationHeader == null &&
                statusCode == HttpStatus.SC_MOVED_PERMANENTLY) return false;

            return super.isRedirected(request, response, context);
        }
    }

    /**
     * HttpRequestInterceptor implementation to set up pre-emptive
     * authentication against a defined basic proxy server.
     */
    private static class PreemptiveProxyAuth implements HttpRequestInterceptor {
        private final HttpHost proxyHost;

        public PreemptiveProxyAuth(HttpHost proxyHost) {
            this.proxyHost = proxyHost;
        }

        public void process(HttpRequest request, HttpContext context) {
            AuthCache authCache;
            // Set up the a Basic Auth scheme scoped for the proxy - we don't
            // want to do this for non-proxy authentication.
            BasicScheme basicScheme = new BasicScheme(ChallengeState.PROXY);

            if (context.getAttribute(ClientContext.AUTH_CACHE) == null) {
                authCache = new BasicAuthCache();
                authCache.put(this.proxyHost, basicScheme);
                context.setAttribute(ClientContext.AUTH_CACHE, authCache);
            } else {
                authCache =
                    (AuthCache) context.getAttribute(ClientContext.AUTH_CACHE);
                authCache.put(this.proxyHost, basicScheme);
            }
        }
    }
}
