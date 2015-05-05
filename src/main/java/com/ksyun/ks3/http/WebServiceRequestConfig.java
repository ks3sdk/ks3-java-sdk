package com.ksyun.ks3.http;

import java.util.HashMap;
import java.util.Map;

import com.ksyun.ks3.config.Constants;

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
}
