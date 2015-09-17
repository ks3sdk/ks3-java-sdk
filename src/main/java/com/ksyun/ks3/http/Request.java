package com.ksyun.ks3.http;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ksyun.ks3.service.Ks3ClientConfig;
import com.ksyun.ks3.service.Ks3ClientConfig.PROTOCOL;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月17日 下午5:30:22
 * 
 * @description
 **/
public class Request {
	private HttpMethod method;
	private Date expires;
	private String endpoint;
	private String bucket;
	private String key;
	private Map<String, String> queryParams = new HashMap<String, String>();
	private Map<String, String> headers = new HashMap<String, String>();
	private InputStream content;

	public String toUrl(Ks3ClientConfig ks3config) {
		String url = "";
		String bucket = this.getBucket();
		String key = this.getKey();
		String endpoint = this.getEndpoint();
		String encodedParams = HttpUtils.encodeParams(this.getQueryParams());
		key = HttpUtils.urlEncode(key, true);

		boolean pathStyle = ks3config.isPathStyleAccess();
		boolean domainMode = ks3config.isDomainMode();

		PROTOCOL spePro = ks3config.getProtocol();
		if (spePro == null)
			spePro = PROTOCOL.http;

		if (!domainMode) {
			if (!pathStyle) {
				url = new StringBuffer()
						.append(StringUtils.isBlank(bucket) ? "" : bucket + ".")
						.append(endpoint)
						.append(StringUtils.isBlank(key) ? "" : "/" + key)
						.toString();
			} else {
				url = new StringBuffer()
						.append(endpoint)
						.append(StringUtils.isBlank(bucket) ? "" : "/" + bucket)
						.append(StringUtils.isBlank(key) ? "" : "/" + key)
						.toString();
			}
		} else {
			url = new StringBuffer().append(endpoint)
					.append(StringUtils.isBlank(key) ? "" : "/" + key)
					.toString();
		}
		url = url.replace("//", "/%2F");
		url = spePro.toString() + "://" + url;
		if (!StringUtils.isBlank(encodedParams))
			url += "?" + encodedParams;
		return url;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
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

	public Map<String, String> getQueryParams() {
		return queryParams;
	}

	public void setQueryParams(Map<String, String> queryParams) {
		this.queryParams = queryParams;
	}

	public void addQueryParam(String key, String value) {
		this.queryParams.put(key, value);
	}

	public void addQueryParamIfNotNull(String key, String value) {
		if (!StringUtils.isBlank(value))
			this.addQueryParam(key, value);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void addHeader(String key, String value) {
		this.headers.put(key, value);
	}

	public void addHeader(HttpHeaders key, String value) {
		this.addHeader(key.toString(), value);
	}

	public void addHeaderIfNotContains(String key, String value) {
		if (!this.headers.containsKey(key))
			this.addHeader(key, value);
	}

	public InputStream getContent() {
		return content;
	}

	public void setContent(InputStream content) {
		this.content = content;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public boolean isPresign() {
		return this.expires != null;
	}

}
