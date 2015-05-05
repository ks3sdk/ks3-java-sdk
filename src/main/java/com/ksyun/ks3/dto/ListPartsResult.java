package com.ksyun.ks3.dto;

import java.util.ArrayList;
import java.util.List;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 下午2:30:45
 * 
 * @description 列出一个分块上传操作中，已上传的块
 **/
public class ListPartsResult extends Ks3Result{
	private String bucketname;
	private String key;
	/**
	 * 由init multipart upload生成的uploadid
	 */
	private String uploadId;
	/**
	 * partnumber游标
	 */
	private String partNumberMarker;
	/**
	 * 可以作为下一次list parts操作时的partNumberMarker参数
	 */
	private String nextPartNumberMarker;
	/**
	 * 返回结果的最大数 1-1000
	 */
	private String maxParts;
	/**
	 * 若为false,表示此次已将所有结果全部返回;反之表示只返回部分结果，应该使用nextPartNumberMarker作为partNumberMarker再次请求
	 */
	private boolean isTruncated;
	/**
	 * ks3返回的xml对oject key的编码方式
	 */
	private String encodingType;
	/**
	 * init multipart upload的用户
	 */
	private Owner initiator = new Owner();
	/**
	 * 这个object的所有者
	 */
	private Owner owner = new Owner();
	private List<Part> parts = new ArrayList<Part>();

	@Override
	public String toString() {
		return StringUtils.object2string(this);
	}

	public String getBucketname() {
		return bucketname;
	}

	public void setBucketname(String bucketname) {
		this.bucketname = bucketname;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(String uploadId) {
		this.uploadId = uploadId;
	}

	public String getPartNumberMarker() {
		return partNumberMarker;
	}

	public void setPartNumberMarker(String partNumberMarker) {
		this.partNumberMarker = partNumberMarker;
	}

	public String getNextPartNumberMarker() {
		return nextPartNumberMarker;
	}

	public void setNextPartNumberMarker(String nextPartNumberMarker) {
		this.nextPartNumberMarker = nextPartNumberMarker;
	}

	public String getMaxParts() {
		return maxParts;
	}

	public void setMaxParts(String maxParts) {
		this.maxParts = maxParts;
	}

	public boolean isTruncated() {
		return isTruncated;
	}

	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	public Owner getInitiator() {
		return initiator;
	}

	public void setInitiator(Owner initiator) {
		this.initiator = initiator;
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	public List<Part> getParts() {
		return parts;
	}

	public void setParts(List<Part> parts) {
		this.parts = parts;
	}

	public String getEncodingType() {
		return encodingType;
	}

	public void setEncodingType(String encodingType) {
		this.encodingType = encodingType;
	}

}
