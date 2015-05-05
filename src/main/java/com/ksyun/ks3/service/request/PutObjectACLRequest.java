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
public class PutObjectACLRequest extends Ks3WebServiceRequest{
	private String bucket;
	private String key;
	/**
	 * bucket的acl
	 */
    private AccessControlList accessControlList;
    /**
     * 对acl的一种快捷配置方式
     */
    private CannedAccessControlList cannedAcl;

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
    	if(StringUtils.isBlank(this.key)){
    		throw notNull("objectkey");
    	}
    	if(this.accessControlList==null&&this.cannedAcl==null)
    		throw notNull("accessControlList","cannedAcl");
    }
    public PutObjectACLRequest(String bucketName,String objectName) {
    	this.bucket = bucketName;
    	this.key = objectName;
    }
    public PutObjectACLRequest(String bucketName,String objectName,AccessControlList accessControlList){
    	this(bucketName,objectName);
        this.setAccessControlList(accessControlList);
    }
    public PutObjectACLRequest(String bucketName,String objectName,CannedAccessControlList cannedAcl){
    	this(bucketName,objectName);
        this.setCannedAcl(cannedAcl);
    }
    public PutObjectACLRequest(String bucketName,String objectName,AccessControlList accessControlList,CannedAccessControlList cannedAcl){
    	this(bucketName,objectName);
        this.setAccessControlList(accessControlList);
        this.setCannedAcl(cannedAcl);
    }

	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.PUT);
		request.setBucket(bucket);
		request.setKey(key);
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
