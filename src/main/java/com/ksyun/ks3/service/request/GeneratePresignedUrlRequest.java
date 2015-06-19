package com.ksyun.ks3.service.request;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ksyun.ks3.dto.ResponseHeaderOverrides;
import com.ksyun.ks3.dto.SSECustomerKey;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.StringUtils;

public class GeneratePresignedUrlRequest extends Ks3WebServiceRequest{
	/**
	 * 指定该外链的使用的HTTP方法
	 */
	private HttpMethod method = HttpMethod.GET;
	/**
	 * 指定bucket，在key为空的情况下可以为空
	 */
	private String bucket;
	/**
	 * 指定操作的文件key，可以为空
	 */
	private String key;
	/**
	 * 外链过期时间，默认是15分钟之后
	 */
	private Date expiration;
	/**
	 * 指定Content-MD5
	 */
	private String contentMD5;
	/**
	 * 指定Content-Type
	 */
	private String contentType;
	/**
	 * 设置外链的请求参数
	 */
	private Map<String,String> requestParameters = new HashMap<String,String>();
	/**
	 * 设置返回的header
	 */
	private ResponseHeaderOverrides responseHeaders;
	/**
	 * 设置客户提供主密钥的服务端加密
	 */
	private SSECustomerKey sseCustomerKey;
	/**
	 * 设置服务端加密
	 */
	private String sseAlgorithm;
	
	@Override
	public void buildRequest(Request request) {
		request.setMethod(method);
		if(!StringUtils.isBlank(bucket))
			request.setBucket(bucket);
		if(!StringUtils.isBlank(key))
			request.setKey(key);
		if(!StringUtils.isBlank(contentMD5))
			request.addHeader(HttpHeaders.ContentMD5,contentMD5);
		if(!StringUtils.isBlank(contentType))
			request.addHeader(HttpHeaders.ContentType,contentType);
		if(requestParameters != null)
			request.getQueryParams().putAll(requestParameters);
		if(responseHeaders != null)
			request.getQueryParams().putAll(responseHeaders.getOverrides());
		if(sseCustomerKey != null)
			request.getHeaders().putAll(HttpUtils.convertSSECustomerKey2Headers(sseCustomerKey));
		if(!StringUtils.isBlank(sseAlgorithm))
			request.addHeader(HttpHeaders.XKssServerSideEncryption,sseAlgorithm);
		if(this.expiration != null)
			request.setExpires(this.expiration);
		else
			request.setExpires( new Date(System.currentTimeMillis() + 1000 * 60 * 15));
		
	}

	@Override
	public void validateParams() {

	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public String getContentMD5() {
		return contentMD5;
	}

	public void setContentMD5(String contentMD5) {
		this.contentMD5 = contentMD5;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Map<String, String> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(Map<String, String> requestParameters) {
		this.requestParameters = requestParameters;
	}

	public ResponseHeaderOverrides getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(ResponseHeaderOverrides responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	public SSECustomerKey getSseCustomerKey() {
		return sseCustomerKey;
	}

	public void setSseCustomerKey(SSECustomerKey sseCustomerKey) {
		this.sseCustomerKey = sseCustomerKey;
	}

	public String getSseAlgorithm() {
		return sseAlgorithm;
	}

	public void setSseAlgorithm(String sseAlgorithm) {
		this.sseAlgorithm = sseAlgorithm;
	}
	

}
