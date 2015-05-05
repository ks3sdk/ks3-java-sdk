package com.ksyun.ks3.service.request;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import com.ksyun.ks3.utils.StringUtils;

import java.security.Permission;

/**
 * 获取bucket acl的请求
 * @author LIJUNWEI
 *
 */
public class GetBucketACLRequest extends Ks3WebServiceRequest{

	private String bucket;

    @Override
	public void validateParams() throws IllegalArgumentException {
        if(StringUtils.isBlank(this.bucket))
            throw notNull("bucketname");
    }

    public GetBucketACLRequest(String bucketName) {
        this.bucket = bucketName;
    }

	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.GET);
		request.getQueryParams().put("acl","");
		request.setBucket(bucket);
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
}
