package com.ksyun.ks3.service.request;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月16日 下午3:07:07
 * 
 * @description 删除bucket的请求
 **/
public class DeleteBucketRequest extends Ks3WebServiceRequest{
	private String bucket;
	public DeleteBucketRequest(String bucketname)
	{
		this.bucket = bucketname;
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if(StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.DELETE);
		request.setBucket(bucket);
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
}
