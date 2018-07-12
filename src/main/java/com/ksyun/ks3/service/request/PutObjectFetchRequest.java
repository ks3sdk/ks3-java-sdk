package com.ksyun.ks3.service.request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.ksyun.ks3.utils.Jackson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ksyun.ks3.dto.AccessControlList;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.StringUtils;

public class PutObjectFetchRequest extends Ks3WebServiceRequest{

	private static final Log log = LogFactory.getLog(PutObjectFetchRequest.class);
	/**
	 * 目标bucket
	 */
	private String bucket;
	/**
	 * 目标key
	 */
	private String key;
	/**
	 * 将要上传的object的元数据
	 */
	private ObjectMetadata objectMeta = new ObjectMetadata();
	/**
	 * 设置新的object的acl
	 */
	private CannedAccessControlList cannedAcl;
	/**
	 * 设置新的object的acl
	 */
	private AccessControlList acl = new AccessControlList();
	
	private String sourceUrl;
	
	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}


	private String callbackUrl;

	private boolean force;

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	private String fetchSourceHeader;

	public String getFetchSourceHeader() {
		return fetchSourceHeader;
	}

	public void setFetchSourceHeader(String fetchSourceHeader) {
		this.fetchSourceHeader = fetchSourceHeader;
	}

	/**
	 * 
	 * @param bucketname
	 * @param key
	 * @param sourceUrl
	 *            要上传的文件
	 */
	public PutObjectFetchRequest(String bucketname, String key,String sourceUrl) {
		this.bucket = bucketname;
		this.key = key;
		this.sourceUrl = sourceUrl;
	}

	/**
	 * 
	 * @param bucketname
	 * @param key
	 * @param metadata
	 *            请尽量提供content-length,否则可能会导致jvm内存溢出
	 */
	public PutObjectFetchRequest(String bucketname, String key,ObjectMetadata metadata) {
		if(metadata == null)
			metadata = new ObjectMetadata();
		this.bucket = bucketname;
		this.key = key;
		this.setObjectMeta(metadata);
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if (StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
		if (StringUtils.isBlank(this.key))
			throw notNull("objectkey");
		if(StringUtils.isBlank(this.sourceUrl)){
			throw notNull("sourceUrl");
		}
		
	}
	
	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
	
	public void setMd5Base64(String md5Base64) {
		this.objectMeta.setContentMD5(md5Base64);
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

	public CannedAccessControlList getCannedAcl() {
		return cannedAcl;
	}

	public void setCannedAcl(CannedAccessControlList cannedAcl) {
		this.cannedAcl = cannedAcl;
	}

	public AccessControlList getAcl() {
		return acl;
	}

	public void setAcl(AccessControlList acl) {
		this.acl = acl;
	}


	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.PUT);
		request.setBucket(bucket);
		request.setKey(key);
		request.addQueryParam("fetch", "");
//		request.addHeader(HttpHeaders.ContentType,"application/octet-stream");
		if (this.objectMeta == null)
			this.objectMeta = new ObjectMetadata();
		
		//添加元数据
		request.getHeaders().putAll(HttpUtils.convertMeta2Headers(objectMeta));
		// acl
		if (this.cannedAcl != null) {
			request.addHeader(HttpHeaders.CannedAcl.toString(),
					cannedAcl.toString());
		}
		if (this.acl != null) {
			request.getHeaders().putAll(HttpUtils.convertAcl2Headers(acl));
		}
		try {
			if(this.sourceUrl != null){
				request.getHeaders().put(HttpHeaders.XKssSourceUrl.toString(), URLEncoder.encode(this.sourceUrl,"UTF-8"));
			}
			if(this.callbackUrl != null){
				request.getHeaders().put(HttpHeaders.XKssCallbackUrl.toString(), URLEncoder.encode(this.callbackUrl,"UTF-8"));
			}
			if (this.isForce()) {
				request.getHeaders().put(HttpHeaders.XKssForce.toString(), String.valueOf(this.force));
			}
			if (this.fetchSourceHeader != null) {
				Map<String, String> map = Jackson.fromJsonString(this.fetchSourceHeader, Map.class);
				request.getHeaders().put(HttpHeaders.XKssFetchSourceHeader.toString(), this.fetchSourceHeader);
			}
		} catch (UnsupportedEncodingException e) {
			log.error("put head error:{}",e);
		}
	}

}
