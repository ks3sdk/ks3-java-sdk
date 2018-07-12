package com.ksyun.ks3.service.request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.StringUtils;

public class DeleteBucketLifecycleRequest extends Ks3WebServiceRequest {
	private String bucketName;

	public DeleteBucketLifecycleRequest(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getBucketName() {
		return this.bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}



	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.DELETE);
		request.setBucket(bucketName);
		request.addQueryParam("lifecycle","");

	}

	@Override
	public void validateParams() {
		if(StringUtils.isBlank(this.bucketName))
			throw notNull("bucketName");

	}
	
}