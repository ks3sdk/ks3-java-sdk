package com.ksyun.ks3.service.request;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.StringUtils;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;


/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年12月31日 下午1:46:05
 * 
 * @description 获取bucket的跨域资源共享配置
 **/
public class GetBucketCorsRequest extends Ks3WebServiceRequest{
	private String bucket;
	public GetBucketCorsRequest(String bucketName){
		this.bucket = bucketName;
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if(StringUtils.isBlank(this.bucket))
			throw notNull("bucketName");
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.GET);
		request.setBucket(bucket);
		request.getQueryParams().put("cors","");
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

}
