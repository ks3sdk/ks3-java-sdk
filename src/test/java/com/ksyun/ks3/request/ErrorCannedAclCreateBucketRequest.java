package com.ksyun.ks3.request;

import java.io.ByteArrayInputStream;

import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.service.request.CreateBucketRequest;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月11日 下午1:49:48
 * 
 * @description 
 **/
public class ErrorCannedAclCreateBucketRequest extends CreateBucketRequest{

	private String acl;
	public ErrorCannedAclCreateBucketRequest(String bucketName,String acl) {
		super(bucketName);
		this.acl = acl;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void buildRequest(Request req){
		super.buildRequest(req);
		req.addHeader(HttpHeaders.CannedAcl, acl);
	}
}
