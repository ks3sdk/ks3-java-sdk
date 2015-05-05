package com.ksyun.ks3.service.request;

import com.ksyun.ks3.dto.*;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.StringUtils;

/**
 * 修改bucket的acl
 * @author LIJUNWEI
 *
 */
public class PutBucketACLRequest extends Ks3WebServiceRequest{
	private String bucket;
	/**
	 * bucket的acl
	 */
    private AccessControlList accessControlList;
    /**
     * 一种快捷的配置方式
     */
    private CannedAccessControlList cannedAcl;
    
    public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public CannedAccessControlList getCannedAcl() {
        return cannedAcl;
    }

    public void setCannedAcl(CannedAccessControlList cannedAcl) {
        this.cannedAcl = cannedAcl;
    }
    

    public AccessControlList getAccessControlList() {
		return accessControlList;
	}

	public void setAccessControlList(AccessControlList accessControlList) {
		this.accessControlList = accessControlList;
	}

    @Override
	public void validateParams() throws IllegalArgumentException {
    	if(StringUtils.isBlank(this.bucket)){
    		throw notNull("bucketname");
    	}
    	if(this.accessControlList==null&&this.cannedAcl==null)
    		throw notNull("accessControlList","cannedAcl");
    }
    public PutBucketACLRequest(String bucketName)
    {
    	this.bucket = bucketName;
    }
    public PutBucketACLRequest(String bucketName,CannedAccessControlList cannedAcl)
    {
    	this.bucket = bucketName;
    	this.setCannedAcl(cannedAcl);
    }
    public PutBucketACLRequest(String bucketName,AccessControlList accessControlList) {
    	this.bucket = bucketName;
        this.accessControlList = accessControlList;
    }

	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.PUT);
		request.setBucket(bucket);
		request.addQueryParam("acl", "");
        if(getCannedAcl()!=null){
        	request.addHeader(HttpHeaders.CannedAcl,getCannedAcl().toString());
        }

		if(this.accessControlList!=null)
		{
			request.getHeaders().putAll(HttpUtils.convertAcl2Headers(accessControlList));
		}
	}
}
