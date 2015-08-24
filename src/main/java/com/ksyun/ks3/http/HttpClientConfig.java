package com.ksyun.ks3.http;

/**
 * http client 配置
 * @author lijunwei
 *
 */
public class HttpClientConfig {
	private int socketSendBufferSizeHint = 8192;
	private int socketReceiveBufferSizeHint = 8192;
	/**
	 * socket超时时间，单位毫秒
	 */
	private int socketTimeOut = 50000;
	/**
	 * 连接超时时间，单位毫秒
	 */
	private int connectionTimeOut = 50000;
	private int connectionTTL = -1;
	/**
	 *httpclient 最大连接数
	 */
	private int maxConnections = 50;
	private String proxyHost;
	private int proxyPort;
	private String proxyUserName;
	private String proxyPassWord;
	private String proxyDomain;
	private String proxyWorkStation;
	private boolean isPreemptiveBasicProxyAuth = false;
	/**
	 * httpclient 重试次数
	 */
	private int maxRetry = 2;
	public int getSocketSendBufferSizeHint() {
		return socketSendBufferSizeHint;
	}
	public void setSocketSendBufferSizeHint(int socketSendBufferSizeHint) {
		this.socketSendBufferSizeHint = socketSendBufferSizeHint;
	}
	public HttpClientConfig withSocketSendBufferSizeHint(int socketSendBufferSizeHint) {
		this.socketSendBufferSizeHint = socketSendBufferSizeHint;
		return this;
	}
	public int getSocketReceiveBufferSizeHint() {
		return socketReceiveBufferSizeHint;
	}
	public void setSocketReceiveBufferSizeHint(int socketReceiveBufferSizeHint) {
		this.socketReceiveBufferSizeHint = socketReceiveBufferSizeHint;
	}
	public HttpClientConfig withSocketReceiveBufferSizeHint(int socketReceiveBufferSizeHint){
		this.socketReceiveBufferSizeHint = socketReceiveBufferSizeHint;
		return this;
	}
	public int getSocketTimeOut() {
		return socketTimeOut;
	}
	public void setSocketTimeOut(int socketTimeOut) {
		this.socketTimeOut = socketTimeOut;
	}
	public HttpClientConfig withSocketTimeOut(int socketTimeOut) {
		this.socketTimeOut = socketTimeOut;
		return this;
	}
	public int getConnectionTimeOut() {
		return connectionTimeOut;
	}
	public void setConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}
	public HttpClientConfig withConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
		return this;
	}
	public int getConnectionTTL() {
		return connectionTTL;
	}
	public void setConnectionTTL(int connectionTTL) {
		this.connectionTTL = connectionTTL;
	}
	public HttpClientConfig withConnectionTTL(int connectionTTL) {
		this.connectionTTL = connectionTTL;
		return this;
	}
	public int getMaxConnections() {
		return maxConnections;
	}
	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}
	public HttpClientConfig withMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
		return this;
	}
	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
	public HttpClientConfig withProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
		return this;
	}
	public int getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	public HttpClientConfig withProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
		return this;
	}
	public String getProxyUserName() {
		return proxyUserName;
	}
	public void setProxyUserName(String proxyUserName) {
		this.proxyUserName = proxyUserName;
	}
	public HttpClientConfig withProxyUserName(String proxyUserName) {
		this.proxyUserName = proxyUserName;
		return this;
	}
	public String getProxyPassWord() {
		return proxyPassWord;
	}
	public void setProxyPassWord(String proxyPassWord) {
		this.proxyPassWord = proxyPassWord;
	}
	public HttpClientConfig withProxyPassWord(String proxyPassWord) {
		this.proxyPassWord = proxyPassWord;
		return this;
	}
	public String getProxyDomain() {
		return proxyDomain;
	}
	public void setProxyDomain(String proxyDomain) {
		this.proxyDomain = proxyDomain;
	}
	public HttpClientConfig withProxyDomain(String proxyDomain) {
		this.proxyDomain = proxyDomain;
		return this;
	}
	public String getProxyWorkStation() {
		return proxyWorkStation;
	}
	public void setProxyWorkStation(String proxyWorkStation) {
		this.proxyWorkStation = proxyWorkStation;
	}
	public HttpClientConfig withProxyWorkStation(String proxyWorkStation) {
		this.proxyWorkStation = proxyWorkStation;
		return this;
	}
	public boolean isPreemptiveBasicProxyAuth() {
		return isPreemptiveBasicProxyAuth;
	}
	public void setPreemptiveBasicProxyAuth(boolean isPreemptiveBasicProxyAuth) {
		this.isPreemptiveBasicProxyAuth = isPreemptiveBasicProxyAuth;
	}
	public HttpClientConfig withPreemptiveBasicProxyAuth(boolean isPreemptiveBasicProxyAuth) {
		this.isPreemptiveBasicProxyAuth = isPreemptiveBasicProxyAuth;
		return this;
	}
	public int getMaxRetry() {
		return maxRetry;
	}
	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}
	public HttpClientConfig withMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
		return this;
	}
}
