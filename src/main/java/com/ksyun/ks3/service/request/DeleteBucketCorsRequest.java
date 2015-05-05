package com.ksyun.ks3.service.request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年12月31日 下午3:36:02
 * 
 * @description 删除bucket的跨域资源共享配置
 **/
public class DeleteBucketCorsRequest extends Ks3WebServiceRequest{
	private String bucket;
	public DeleteBucketCorsRequest(String bucketName){
		this.bucket = bucketName;
	}
	@Override
	public void validateParams() throws IllegalArgumentException {
		if(StringUtils.isBlank(bucket))
			throw notNull("bucketName");
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.DELETE);
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
