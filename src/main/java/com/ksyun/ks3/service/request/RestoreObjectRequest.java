package com.ksyun.ks3.service.request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.StringUtils;

public class RestoreObjectRequest extends Ks3WebServiceRequest {
	
	private String bucket;
	
	private String key;

	public RestoreObjectRequest(String bucketName, String objectKey) {
		this.bucket = bucketName;
		this.key = objectKey;
	}
	
	@Override
	public void validateParams() throws IllegalArgumentException {
		if(StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
		if(StringUtils.isBlank(this.key))
			throw notNull("objectkey");
	}

	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.POST);
		request.setBucket(bucket);
		request.setKey(key);
		request.addQueryParam("restore", "");
	}
}
