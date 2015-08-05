package com.ksyun.ks3.service.request;

import java.util.ArrayList;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notCorrect;

import java.util.List;
import java.util.Map.Entry;

import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.AccessControlList;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.Grant;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.Permission;
import com.ksyun.ks3.dto.SSECustomerKey;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Mimetypes;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.DateUtils;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.utils.DateUtils.DATETIME_PROTOCOL;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 上午10:37:41
 * 
 * @description 初始化分块上传
 **/
public class InitiateMultipartUploadRequest extends Ks3WebServiceRequest {
	private String bucket;
	private String key;
	/**
	 * 设置object的元数据
	 */
	private ObjectMetadata objectMeta = new ObjectMetadata();
	/**
	 * 设置object的acl
	 */
	private AccessControlList acl = new AccessControlList();
	/**
	 * 使用一种快捷的方式设置acl
	 */
	private CannedAccessControlList cannedAcl;
	private String redirectLocation;
	/**
	 * 使用用户指定的key进行服务端加密
	 */
	private SSECustomerKey sseCustomerKey;

	public InitiateMultipartUploadRequest(String bucketname, String objectkey) {
		this.bucket = bucketname;
		this.key = objectkey;
	}
	public InitiateMultipartUploadRequest(String bucketname,String objectkey,ObjectMetadata metadata){
		this.bucket = bucketname;
		this.key = objectkey;
		this.objectMeta = metadata;
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if (StringUtils.validateBucketName(this.bucket) == null)
			throw notCorrect("bucketname", this.bucket, "详见API文档");
		if (StringUtils.isBlank(this.key))
			throw notNull("objectkey");
		if (this.redirectLocation != null) {
			if (!this.redirectLocation.startsWith("/")
					&& !this.redirectLocation.startsWith("http://")
					&& !this.redirectLocation.startsWith("https://"))
				throw notCorrect("redirectLocation", this.redirectLocation,
						"以 / http:// 或 https://开头");
		}
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
	public ObjectMetadata getObjectMeta() {
		return objectMeta;
	}

	public void setObjectMeta(ObjectMetadata objectMeta) {
		this.objectMeta = objectMeta;
	}

	public AccessControlList getAcl() {
		return acl;
	}

	public void setAcl(AccessControlList acl) {
		this.acl = acl;
	}

	public CannedAccessControlList getCannedAcl() {
		return cannedAcl;
	}

	public void setCannedAcl(CannedAccessControlList cannedAcl) {
		this.cannedAcl = cannedAcl;
	}

	public String getRedirectLocation() {
		return redirectLocation;
	}

	public void setRedirectLocation(String redirectLocation) {
		this.redirectLocation = redirectLocation;
	}
	public SSECustomerKey getSseCustomerKey() {
		return sseCustomerKey;
	}
	public void setSseCustomerKey(SSECustomerKey sseCustomerKey) {
		this.sseCustomerKey = sseCustomerKey;
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.POST);
		request.setBucket(bucket);
		request.setKey(key);
		request.addHeader(HttpHeaders.ContentType,"binary/octet-stream");
		request.getQueryParams().put("uploads", null);
		if (this.objectMeta == null)
			this.objectMeta = new ObjectMetadata();
		// 根据object key匹配content-type
		if (StringUtils.isBlank(objectMeta.getContentType()))
			objectMeta.setContentType(Mimetypes.getInstance().getMimetype(
					this.key));
		// 添加meta data
		request.getHeaders().putAll(HttpUtils.convertMeta2Headers(objectMeta));
		//添加服务端加密相关
		request.getHeaders().putAll(HttpUtils.convertSSECustomerKey2Headers(sseCustomerKey));
		if (this.cannedAcl != null) {
			request.addHeader(HttpHeaders.CannedAcl.toString(),
					cannedAcl.toString());
		}
		if (this.acl != null) {
			request.getHeaders().putAll(HttpUtils.convertAcl2Headers(acl));
		}
		if (this.redirectLocation != null) {
			request.addHeader(HttpHeaders.XKssWebsiteRedirectLocation,
					this.redirectLocation);
		}
		
		//这个请求是不需要content-length的
		request.getHeaders().remove(HttpHeaders.ContentLength);
	}

}
