package com.ksyun.ks3.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lijunwei[13810414122@163.com]  
 * 
 * @date 2014年11月16日
 * 
 * @description 列出bucket下分块上传未abort或complete的块
 **/
public class ListMultipartUploadsResult {
	private String bucket;
	private String keyMarker;
	private String uploadIdMarker;
	private String nextKeyMarker;
	private String nextUploadIdMarker;
	private String encodingType;
	private Integer maxUploads;
	/**
	 * 若isTruncated为true,则nextKeyMarker可以作为下次请求的keyMarker
	 * 则nextUploadIdMarker可以作为下次请求的uploadIdMarker
	 */
	private boolean isTruncated;
	private String prefix;
	private String delimiter;
	private List<String> commonPrefixes = new ArrayList<String>();
	private List<MultiPartInfo> uploads = new ArrayList<MultiPartInfo>();

	public String toString() {
		return "ListMultipartUploadsResult[bucket=" + this.bucket
				+ ";keyMarker=" + this.keyMarker + ";uploadIdMarker="
				+ this.uploadIdMarker + ";nextKeyMarker=" + this.nextKeyMarker
				+ ";nextUploadIdMarker=" + this.nextUploadIdMarker
				+ ";encodingType=" + this.encodingType + ";maxUploads="
				+ this.maxUploads + ";isTruncated=" + this.isTruncated
				+ ";prefix=" + this.prefix + ";delimiter=" + this.delimiter
				+ ";commonPrefix=" + this.commonPrefixes + ";uploads="
				+ this.uploads + "]";
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getKeyMarker() {
		return keyMarker;
	}

	public void setKeyMarker(String keyMarker) {
		this.keyMarker = keyMarker;
	}

	public String getUploadIdMarker() {
		return uploadIdMarker;
	}

	public void setUploadIdMarker(String uploadIdMarker) {
		this.uploadIdMarker = uploadIdMarker;
	}

	public String getNextKeyMarker() {
		return nextKeyMarker;
	}

	public void setNextKeyMarker(String nextKeyMarker) {
		this.nextKeyMarker = nextKeyMarker;
	}

	public String getNextUploadIdMarker() {
		return nextUploadIdMarker;
	}

	public void setNextUploadIdMarker(String nextUploadIdMarker) {
		this.nextUploadIdMarker = nextUploadIdMarker;
	}

	public String getEncodingType() {
		return encodingType;
	}

	public void setEncodingType(String encodingType) {
		this.encodingType = encodingType;
	}

	public Integer getMaxUploads() {
		return maxUploads;
	}

	public void setMaxUploads(Integer maxUploads) {
		this.maxUploads = maxUploads;
	}

	public boolean isTruncated() {
		return isTruncated;
	}

	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public List<String> getCommonPrefixes() {
		return commonPrefixes;
	}

	public void setCommonPrefixes(List<String> commonPrefixes) {
		this.commonPrefixes = commonPrefixes;
	}

	public List<MultiPartInfo> getUploads() {
		return uploads;
	}

	public void setUploads(List<MultiPartInfo> uploads) {
		this.uploads = uploads;
	}

}
