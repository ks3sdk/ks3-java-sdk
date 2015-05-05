package com.ksyun.ks3.dto;
/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年5月5日 上午11:19:40
 * 
 * @description 添加异步数据处理任务返回的结果
 **/
public class PutAdpResult extends Ks3Result{
	/**
	 * 任务ID，可以用于查询任务。
	 */
	private String taskId;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
}
