package com.ksyun.ks3.service.request;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.StringUtils;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月16日 下午3:45:23
 * 
 * @description 删除单个object的请求
 **/
public class DeleteObjectRequest extends Ks3WebServiceRequest{
	private String bucket;
	/**
	 * 要删除的文件名
	 */
	public String key;
	public DeleteObjectRequest(String bucketname,String key)
	{
		this.bucket = bucketname;
		this.key = key;
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
		request.setMethod(HttpMethod.DELETE);
		request.setBucket(bucket);
		request.setKey(key);
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
}
