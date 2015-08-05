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
	public int getSocketReceiveBufferSizeHint() {
		return socketReceiveBufferSizeHint;
	}
	public void setSocketReceiveBufferSizeHint(int socketReceiveBufferSizeHint) {
		this.socketReceiveBufferSizeHint = socketReceiveBufferSizeHint;
	}
	public int getSocketTimeOut() {
		return socketTimeOut;
	}
	public void setSocketTimeOut(int socketTimeOut) {
		this.socketTimeOut = socketTimeOut;
	}
	public int getConnectionTimeOut() {
		return connectionTimeOut;
	}
	public void setConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}
	public int getConnectionTTL() {
		return connectionTTL;
	}
	public void setConnectionTTL(int connectionTTL) {
		this.connectionTTL = connectionTTL;
	}
	public int getMaxConnections() {
		return maxConnections;
	}
	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}
	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
	public int getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	public String getProxyUserName() {
		return proxyUserName;
	}
	public void setProxyUserName(String proxyUserName) {
		this.proxyUserName = proxyUserName;
	}
	public String getProxyPassWord() {
		return proxyPassWord;
	}
	public void setProxyPassWord(String proxyPassWord) {
		this.proxyPassWord = proxyPassWord;
	}
	public String getProxyDomain() {
		return proxyDomain;
	}
	public void setProxyDomain(String proxyDomain) {
		this.proxyDomain = proxyDomain;
	}
	public String getProxyWorkStation() {
		return proxyWorkStation;
	}
	public void setProxyWorkStation(String proxyWorkStation) {
		this.proxyWorkStation = proxyWorkStation;
	}
	public boolean isPreemptiveBasicProxyAuth() {
		return isPreemptiveBasicProxyAuth;
	}
	public void setPreemptiveBasicProxyAuth(boolean isPreemptiveBasicProxyAuth) {
		this.isPreemptiveBasicProxyAuth = isPreemptiveBasicProxyAuth;
	}
	public int getMaxRetry() {
		return maxRetry;
	}
	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}
	
}
