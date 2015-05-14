package com.ksyun.ks3.http;

import java.util.HashMap;
import java.util.Map;

import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.Authorization;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月17日 下午5:43:06
 * 
 * @description 
 **/
public class WebServiceRequestConfig {
	private String userAgent = Constants.KS3_SDK_USER_AGENT;
	private Map<String,String> extendHeaders = new HashMap<String,String>();
	private String endpoint = null;

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public Map<String,String> getExtendHeaders() {
		return extendHeaders;
	}

	public void setExtendHeaders(Map<String,String> extendHeaders) {
		this.extendHeaders = extendHeaders;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
}
