package com.ksyun.ks3.service;

import org.apache.http.client.HttpClient;

import com.ksyun.ks3.http.HttpClientConfig;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年5月14日 下午5:00:38
 * 
 * @description 客户端配置
 **/
public class Ks3ClientConfig {
	public static enum PROTOCOL{
		http,https
	}
	/**
	 * 服务地址,参考{@linkplain http://ks3.ksyun.com/doc/api/index.html} </br>
	 * 中国（杭州）:kss.ksyun.com</br>
	 * 中国（杭州）cdn:kssws.ks-cdn.com</br>
	 * 美国（圣克拉拉）:ks3-us-west-1.ksyun.com</br>
	 * 中国（北京）:ks3-cn-beijing.ksyun.com</br>
	 * 中国（香港）:ks3-cn-hk-1.ksyun.com</br>
	 * 中国（上海）:ks3-cn-shanghai.ksyun.com</br>
	 */
	private String endpoint;
	/**
	 * http或者https
	 */
	private PROTOCOL protocol = PROTOCOL.http;
	/**
	 * 是否使用path style access方式访问
	 */
	private boolean pathStyleAccess = true;
	/**
	 * 允许客户端发送匿名请求
	 */
	private boolean allowAnonymous = true;
	/**
	 * 当服务端返回307时是否自动跳转，
	 * 主要发生在用Region A的endpoint请求Region B的endpoint
	 */
	private boolean flowRedirect = true;
	/**
	 * 是否使用绑定的域名作为endpoint
	 */
	private boolean domainMode = false;
	private String signerClass = "com.ksyun.ks3.signer.DefaultSigner";
	private HttpClientConfig httpClientConfig =  new HttpClientConfig();
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public Ks3ClientConfig withEndpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}
	public PROTOCOL getProtocol() {
		return protocol;
	}
	public void setProtocol(PROTOCOL protocol) {
		this.protocol = protocol;
	}
	public Ks3ClientConfig withProtocol(PROTOCOL protocol) {
		this.protocol = protocol;
		return this;
	}
	public boolean isPathStyleAccess() {
		return pathStyleAccess;
	}
	public void setPathStyleAccess(boolean pathStyleAccess) {
		this.pathStyleAccess = pathStyleAccess;
	}
	public String getSignerClass() {
		return signerClass;
	}
	public void setSignerClass(String signerClass) {
		this.signerClass = signerClass;
	}
	public HttpClientConfig getHttpClientConfig() {
		return httpClientConfig;
	}
	public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
		this.httpClientConfig = httpClientConfig;
	}
	public Ks3ClientConfig withHttpClientConfig(HttpClientConfig httpClientConfig) {
		this.httpClientConfig = httpClientConfig;
		return this;
	}
	public boolean isAllowAnonymous() {
		return allowAnonymous;
	}
	public void setAllowAnonymous(boolean allowAnonymous) {
		this.allowAnonymous = allowAnonymous;
	}
	public Ks3ClientConfig withAllowAnonymous(boolean allowAnonymous) {
		this.allowAnonymous = allowAnonymous;
		return this;
	}
	public boolean isFlowRedirect() {
		return flowRedirect;
	}
	public void setFlowRedirect(boolean flowRedirect) {
		this.flowRedirect = flowRedirect;
	}
	public Ks3ClientConfig withFlowRedirect(boolean flowRedirect) {
		this.flowRedirect = flowRedirect;
		return this;
	}
	public boolean isDomainMode() {
		return domainMode;
	}
	public void setDomainMode(boolean domainMode) {
		this.domainMode = domainMode;
	}
	public Ks3ClientConfig withDomainMode(boolean domainMode) {
		this.domainMode = domainMode;
		return this;
	}
}
