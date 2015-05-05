package com.ksyun.ks3.service.request;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月16日 下午6:45:23
 * 
 * @description 获取 bucket的存储地点
 **/
public class GetBucketLocationRequest extends Ks3WebServiceRequest{
	private String bucket;
	public GetBucketLocationRequest(String bucketName){
		this.bucket = bucketName;
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if(StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.GET);
		request.setBucket(bucket);
		request.getQueryParams().put("location", "");
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

}
