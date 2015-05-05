package com.ksyun.ks3.service.request;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月21日 下午2:15:06
 * 
 * @description Head请求bucket,一般用于查看一个bucket是否在全局中已经存在
 **/
public class HeadBucketRequest extends Ks3WebServiceRequest{
	private String bucket;
	public HeadBucketRequest(String bucketname)
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
		request.setMethod(HttpMethod.HEAD);
		request.setBucket(bucket);
	}
	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

}
