package com.ksyun.ks3.dto;

import java.util.ArrayList;
import java.util.List;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月16日
 * 
 * @description 列出bucket下分块上传未abort或complete的块
 **/
public class ListMultipartUploadsResult extends Ks3Result{
	private String bucket;
	/**
	 * <p>keyMarker为空，uploadIdMarker不为空<P>
	 * <p>无意义</p>
	 * <p>keyMarker不为空，uploadIdMarker不为空<P>
	 * <p>列出分块上传object key为keyMarker，且upload id 大于uploadIdMarker的结果</p>
	 * <p>keyMarker不为空，uploadIdMarker为空<P>
	 * <p>列出分块上传object key大于keyMarker的结果</p>
	 */
	private String keyMarker;
	private String uploadIdMarker;
	/**
	 * 作为下次操作的keyMarker
	 */
	private String nextKeyMarker;
	/**
	 * 作为下次操作的nextUploadIdMarker
	 */
	private String nextUploadIdMarker;
	/**
	 * ks3返回的xml中对object key的编码方式
	 */
	private String encodingType;
	/**
	 * 1-1000
	 */
	private Integer maxUploads;
	/**
	 * 若isTruncated为true,则nextKeyMarker可以作为下次请求的keyMarker
	 * 则nextUploadIdMarker可以作为下次请求的uploadIdMarker
	 */
	private boolean isTruncated;
	private String prefix;
	private String delimiter;
	/**
	 * 表示列表中的文件夹下有分块上传
	 * <p>由prefix和delimiter确定，以prefix开头的object key,在prefix之后第一次出现delimiter的位置之前（包含delimiter）的子字符串将存在于commonPrefixes中</p>
	 * <p>比如有一下两个object key</p>
	 * <p>aaaa/bbb/ddd.txt</p>
	 * <p>aaaa/ccc/eee.txt</p>
	 * <p>ssss/eee/fff.txt</p>
	 * <p>prefix为空 delimiter为/ 则commonPrefix 为 aaaa/和ssss/</p>
	 * <p>prefix为aaaa/  delimiter为/ 则commonPrefix 为 aaaa/bbb/和aaaa/ccc/</p>
	 * <p>prefix为ssss/  delimiter为/ 则commonPrefix 为 aaaa/eee/</p>
	 * <p>delimiter为空时commonPrefixes一定为空</p>
	 */
	private List<String> commonPrefixes = new ArrayList<String>();
	private List<MultiPartUploadInfo> uploads = new ArrayList<MultiPartUploadInfo>();

	public String toString() {
		return StringUtils.object2string(this);
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

	public List<MultiPartUploadInfo> getUploads() {
		return uploads;
	}

	public void setUploads(List<MultiPartUploadInfo> uploads) {
		this.uploads = uploads;
	}

}
