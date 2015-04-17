package com.ksyun.ks3.service.request;

import org.apache.http.client.methods.HttpRequestBase;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.http.WebServiceRequestConfig;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月17日 下午5:14:27
 * 
 * @description 
 **/
public abstract class Ks3WebServiceRequest {
	private WebServiceRequestConfig config = new WebServiceRequestConfig();
	public abstract void buildHttpRequest(Request request);
	public abstract void validateParams();
	public abstract HttpMethod getHttpMethod();
	public WebServiceRequestConfig getConfig() {
		return config;
	}
	public void setConfig(WebServiceRequestConfig config) {
		this.config = config;
	}
}
