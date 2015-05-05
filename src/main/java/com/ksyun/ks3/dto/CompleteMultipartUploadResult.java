package com.ksyun.ks3.dto;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 下午1:43:32
 * 
 * @description CompleteMutipartUpload 操作返回的结果
 **/
public class CompleteMultipartUploadResult extends SSEResultBase{
	/**
	 * 新建对象的uri
	 */
	private String location;
	/**
	 * 新建object存放的bucket
	 */
	private String bucket;
	/**
	 * 新建object的object key
	 */
	private String key;
	/**
	 * 新建object的etag
	 */
	private String eTag;
	/**
	 * 如果在请求中设置了数据处理任务的话，将会返回任务id,否则为空
	 */
	private String taskid;
	public String toString()
	{
		return StringUtils.object2string(this);
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
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
	public String geteTag() {
		return eTag;
	}
	public void seteTag(String eTag) {
		this.eTag = eTag;
	}
	public String getTaskid() {
		return taskid;
	}
	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}
	
}
