package com.ksyun.ks3.dto;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年5月5日 上午11:10:30
 * 
 * @description 
 **/
public class Ks3Result {
	private String requestId;
	private HttpRequest originRequest;
	private HttpResponse originResponse;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public HttpRequest getOriginRequest() {
		return originRequest;
	}

	public void setOriginRequest(HttpRequest originRequest) {
		this.originRequest = originRequest;
	}

	public HttpResponse getOriginResponse() {
		return originResponse;
	}

	public void setOriginResponse(HttpResponse originResponse) {
		this.originResponse = originResponse;
	}
	
}
