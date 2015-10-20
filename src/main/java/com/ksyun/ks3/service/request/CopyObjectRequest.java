package com.ksyun.ks3.service.request;

import com.ksyun.ks3.dto.AccessControlList;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.SSECustomerKey;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.StringUtils;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;


/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月17日 上午11:19:28
 * 
 * @description Copy Object
 * <p>将指定的object复制到目标地点，将复制源object的元数据、acl等信息</p>
 * <p>public CopyObjectRequest(String destinationBucket,String destinationObject,String sourceBucket,String sourceKey)</p>
 **/
public class CopyObjectRequest extends Ks3WebServiceRequest{
	/**
	 * 目标bucket
	 */
	private String destinationBucket;
	/**
	 * 目标key
	 */
	private String destinationKey;
	/**
	 * 源bucket
	 */
	private String sourceBucket;
	/**
	 * 源object
	 */
	private String sourceKey;
	
	/**
	 * 通过CannedAccessControlList设置新的object的acl
	 */
	private CannedAccessControlList cannedAcl;
	/**
	 * 设置新的object的acl
	 */
	private AccessControlList accessControlList;
	/**
	 * 设置新的object的元数据
	 */
    private ObjectMetadata newObjectMetadata;
    /**
     * 如果copy的源object使用客户提供的key加密，则需要提供
     */
    private SSECustomerKey sourceSSECustomerKey;
    /**
     * 指定目标object的加密
     */
    private SSECustomerKey destinationSSECustomerKey;
	/**
	 * 
	 * @param destinationBucket 目标bucket
	 * @param destinationObject 目标object
	 * @param sourceBucket 源bucket
	 * @param sourceKey 源object
	 */
	public CopyObjectRequest(String destinationBucket,String destinationObject,String sourceBucket,String sourceKey){
		this.destinationBucket = destinationBucket;
		this.destinationKey = destinationObject;
		this.setSourceBucket(sourceBucket);
		this.setSourceKey(sourceKey);
	}
	/**
	 * 
	 * @param destinationBucket 目标bucket
	 * @param destinationObject 目标object
	 * @param sourceBucket 源bucket
	 * @param sourceKey 源bucket
	 * @param cannedAcl 设置新object的acl
	 */
	public CopyObjectRequest(String destinationBucket,String destinationObject,String sourceBucket,String sourceKey,CannedAccessControlList cannedAcl){
		this(destinationBucket,destinationObject,sourceBucket,sourceKey);
		this.setCannedAcl(cannedAcl);
	}
	/**
	 * 
	 * @param destinationBucket 目标bucket
	 * @param destinationObject 目标object
	 * @param sourceBucket 源bucket
	 * @param sourceKey 源bucket
	 * @param accessControlList 设置新object的acl
	 */
	public CopyObjectRequest(String destinationBucket,String destinationObject,String sourceBucket,String sourceKey,AccessControlList accessControlList){
		this(destinationBucket,destinationObject,sourceBucket,sourceKey);
		this.setAccessControlList(accessControlList);
	}
	

	public String getDestinationBucket() {
		return destinationBucket;
	}
	public void setDestinationBucket(String destinationBucket) {
		this.destinationBucket = destinationBucket;
	}
	public String getDestinationKey() {
		return destinationKey;
	}
	public void setDestinationKey(String destinationKey) {
		this.destinationKey = destinationKey;
	}
	public String getSourceBucket() {
		return sourceBucket;
	}

	public void setSourceBucket(String sourceBucket) {
		this.sourceBucket = sourceBucket;
	}

	public String getSourceKey() {
		return sourceKey;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
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
	public ObjectMetadata getNewObjectMetadata() {
		return newObjectMetadata;
	}
	public void setNewObjectMetadata(ObjectMetadata newObjectMetadata) {
		this.newObjectMetadata = newObjectMetadata;
	}
	public SSECustomerKey getSourceSSECustomerKey() {
		return sourceSSECustomerKey;
	}
	public void setSourceSSECustomerKey(SSECustomerKey sourceSSECustomerKey) {
		this.sourceSSECustomerKey = sourceSSECustomerKey;
	}
	public SSECustomerKey getDestinationSSECustomerKey() {
		return destinationSSECustomerKey;
	}
	public void setDestinationSSECustomerKey(SSECustomerKey destinationSSECustomerKey) {
		this.destinationSSECustomerKey = destinationSSECustomerKey;
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.PUT);
		request.setBucket(this.destinationBucket);
		request.setKey(this.destinationKey);
		request.addHeader(HttpHeaders.XKssCopySource,("/"+this.getSourceBucket()+"/"+HttpUtils.urlEncode(this.getSourceKey(),true)).replace("//", "/%2F"));
        if(getCannedAcl()!=null){
        	request.addHeader(HttpHeaders.CannedAcl,getCannedAcl().toString());
        }
        //添加元数据
        request.getHeaders().putAll(HttpUtils.convertMeta2Headers(this.newObjectMetadata));
      	//添加服务端加密相关
        request.getHeaders().putAll(HttpUtils.convertSSECustomerKey2Headers(this.destinationSSECustomerKey));
        request.getHeaders().putAll(HttpUtils.convertCopySourceSSECustomerKey2Headers(this.sourceSSECustomerKey));
        if(this.accessControlList!=null)
        {
        	request.getHeaders().putAll(HttpUtils.convertAcl2Headers(accessControlList));
        }
	}
	@Override
	public void validateParams() {
		if(StringUtils.isBlank(sourceBucket))
			throw notNull("sourceBucket");
		if(StringUtils.isBlank(sourceKey))
			throw notNull("sourceKey");
		if(StringUtils.isBlank(this.destinationBucket))
			throw notNull("destinationBucket");
		if(StringUtils.isBlank(this.destinationKey))
			throw notNull("destinationKey");
	}

}
