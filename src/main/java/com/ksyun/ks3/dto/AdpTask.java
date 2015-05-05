package com.ksyun.ks3.dto;

import java.util.ArrayList;
import java.util.List;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年2月2日 下午5:29:41
 * 
 * @description 查询数据处理任务返回结果
 **/
public class AdpTask extends Ks3Result{
	/**
	 * taskid
	 */
	private String taskId;
	/**
	 * 	0,"task is create fail"、1,"task is create success"、2,"task is processing"、3,"task is process success"、4,"task is process fail"
	 */
	private String processstatus;
	private String processdesc;
	/**
	 * 0,"task is not notify"、1,"task is notify success"、2,"task is notify fail"
	 */
	private String notifystatus;
	private String notifydesc;
	/**
	 * 每条命令的具体处理结果
	 */
	private List<AdpInfo> adpInfos = new ArrayList<AdpInfo>();
	public String toString()
	{
		return StringUtils.object2string(this);
	}
	public boolean isProcessFinished(){
		return !"2".equals(processstatus);
	}
	public boolean isProcessSuccess(){
		return "3".equals(processstatus);
	}
	public boolean isNotified(){
		return !"0".equals(notifystatus);
	}
	public boolean isNotifiedSuccess(){
		return "1".equals(notifystatus);
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getProcessstatus() {
		return processstatus;
	}
	public void setProcessstatus(String processstatus) {
		this.processstatus = processstatus;
	}
	public String getProcessdesc() {
		return processdesc;
	}
	public void setProcessdesc(String processdesc) {
		this.processdesc = processdesc;
	}
	public String getNotifystatus() {
		return notifystatus;
	}
	public void setNotifystatus(String notifystatus) {
		this.notifystatus = notifystatus;
	}
	public String getNotifydesc() {
		return notifydesc;
	}
	public void setNotifydesc(String notifydesc) {
		this.notifydesc = notifydesc;
	}
	public List<AdpInfo> getAdpInfos() {
		return adpInfos;
	}
	public void setAdpInfos(List<AdpInfo> adpInfos) {
		this.adpInfos = adpInfos;
	}
	
}
