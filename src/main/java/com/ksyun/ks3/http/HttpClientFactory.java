
package com.ksyun.ks3.http;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.client.config.RequestConfig;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月14日 下午8:05:52
 * 
 * @description
 **/
public class HttpClientFactory {

	private final Log log = LogFactory.getLog(this.getClass());
	public HttpClient createHttpClient(HttpClientConfig config) {
		
		/*初始化配置*/
		SocketConfig soConfig = SocketConfig.custom()
				//.setSoTimeout(config.getInt(ClientConfig.SOCKET_TIMEOUT))
				.setTcpNoDelay(true)
				.build();
		int socketSendBufferSizeHint = config.getSocketSendBufferSizeHint();
		int socketReceiveBufferSizeHint = config.getSocketReceiveBufferSizeHint();
		int buffersize = 0;
		if (socketSendBufferSizeHint > 0 || socketReceiveBufferSizeHint > 0) {
			buffersize = Math.max(socketSendBufferSizeHint, socketReceiveBufferSizeHint);
		}
		ConnectionConfig coConfig = ConnectionConfig.custom()
				.setBufferSize(buffersize)
				.build();
		RequestConfig reConfig = RequestConfig.custom()
				.setConnectTimeout(config.getConnectionTimeOut())
				.setSocketTimeout(config.getSocketTimeOut())
				.setStaleConnectionCheckEnabled(true)
				.build();		

		PlainConnectionSocketFactory sf = PlainConnectionSocketFactory.getSocketFactory();
		SSLContext sslContext;
		SSLConnectionSocketFactory sslsf = null;
		try {
			sslContext = SSLContexts.custom().build();
			String version0 = System.getProperty("java.specification.version");
			float version = 0f;
			try{
				version = Float.valueOf(version0);
			}catch(Exception e){}
			if(version >= 1.7f){
				log.debug("java version "+version+", use STRICT_HOSTNAME_VERIFIER");
				sslsf = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER);
			}
			else{
				log.debug("java version "+version+", use BROWSER_COMPATIBLE_HOSTNAME_VERIFIER");
				sslsf = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			}
		} catch (Exception e) {
			throw new Ks3ClientException("Unable to access default SSL context",e);
		}

		Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory> create().register("http", sf).register("https", sslsf).build();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(r
				,null,null,null,config.getConnectionTTL(), TimeUnit.MILLISECONDS);
		connectionManager.setMaxTotal(config.getMaxConnections());
		connectionManager.setDefaultMaxPerRoute(connectionManager.getMaxTotal());
		connectionManager.setDefaultConnectionConfig(coConfig);
		connectionManager.setDefaultSocketConfig(soConfig);
		
		/* Set proxy if configured */
		String proxyHost = config.getProxyHost();
		int proxyPort = config.getProxyPort();
		
		
		CloseableHttpClient httpClient = null;
		if (proxyHost != null && proxyPort > 0) {

			HttpHost proxyHttpHost = new HttpHost(proxyHost, proxyPort);
			String proxyUsername = config.getProxyUserName();
			String proxyPassword = config.getProxyPassWord();
			String proxyDomain = config.getProxyDomain();
			String proxyWorkstation = config.getProxyWorkStation();
			
			BasicCredentialsProvider creprovide = null;
			if (proxyUsername != null && proxyPassword != null) {
				creprovide = new BasicCredentialsProvider();
				creprovide.setCredentials(new AuthScope(proxyHost, proxyPort),new NTCredentials(proxyUsername, proxyPassword, proxyWorkstation, proxyDomain));
			}
			HttpRequestInterceptor interceptor = null;
			if (config.isPreemptiveBasicProxyAuth()){
				interceptor = new PreemptiveProxyAuth(proxyHttpHost);
			}
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHttpHost);
			
			httpClient = HttpClients.custom().setConnectionManager(connectionManager).setRedirectStrategy(new NeverFollowRedirectStrategy())
					.setRetryHandler(new DefaultHttpRequestRetryHandler(config.getMaxRetry(), false))
					.setDefaultRequestConfig(reConfig)
					.setRoutePlanner(routePlanner)
					.setDefaultCredentialsProvider(creprovide)
					.addInterceptorFirst(interceptor)
					.build();
			

		} else {
			httpClient = HttpClients.custom().setConnectionManager(connectionManager).setRedirectStrategy(new NeverFollowRedirectStrategy())
					.setRetryHandler(new DefaultHttpRequestRetryHandler(config.getMaxRetry(), false))
					.setDefaultRequestConfig(reConfig)
					.build();
		}

		return httpClient;
	}

	private static final class NeverFollowRedirectStrategy  extends DefaultRedirectStrategy {

		  @Override
	        public boolean isRedirected(HttpRequest request, HttpResponse response,
	                HttpContext context) throws ProtocolException {
	            return false;
	        }

	        @Override
	        public HttpUriRequest getRedirect(HttpRequest request,
	                HttpResponse response, HttpContext context)
	                throws ProtocolException {
	            return null;
	        }
	}

	/**
	 * HttpRequestInterceptor implementation to set up pre-emptive
	 * authentication against a defined basic proxy server.
	 */
	private static class PreemptiveProxyAuth implements HttpRequestInterceptor {

		private final HttpHost proxyHost;


		public PreemptiveProxyAuth ( HttpHost proxyHost ) {

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
				authCache = (AuthCache) context.getAttribute(ClientContext.AUTH_CACHE);
				authCache.put(this.proxyHost, basicScheme);
			}
		}
	}
}
