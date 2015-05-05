package com.ksyun.ks3.dto;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月22日 下午1:54:37
 * 
 * @description PUT Object返回的结果
 **/
public class PutObjectResult extends SSEResultBase{
	private String eTag;
	/**
	 * 如果在请求中设置了数据处理任务的话，将会返回任务id,否则为空
	 */
	private String taskid;
	public String toString()
	{
		return StringUtils.object2string(this);
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
