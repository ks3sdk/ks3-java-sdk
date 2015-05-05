package com.ksyun.ks3.service.request;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import com.ksyun.ks3.utils.StringUtils;

/**
 * 获取object的acl
 * @author LIJUNWEI
 *
 */
public class GetObjectACLRequest extends Ks3WebServiceRequest{

	private String bucket;
	private String key;

    @Override
    public void validateParams() throws IllegalArgumentException {
        if(StringUtils.isBlank(this.bucket))
            throw notNull("bucketname");
    }

    public GetObjectACLRequest(String bucketName,String objectName) {
        this.bucket = bucketName;
        this.key = objectName;
    }

	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.GET);
		request.setBucket(bucket);
		request.setKey(key);
		request.getQueryParams().put("acl","");
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
