package com.ksyun.ks3.service.request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年2月2日 下午5:08:36
 * 
 * @description 查询数据处理任务的状态
 **/
public class GetAdpRequest extends Ks3WebServiceRequest {

	/**
	 * 由putpfop，postobject，putobject，complete_mutipart_upload返回的taskid
	 */
	private String taskid;
	public GetAdpRequest(String taskid){
		this.taskid = taskid;
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if(StringUtils.isBlank(this.taskid))
			throw notNull("taskid");
	}
	public String getTaskid() {
		return taskid;
	}
	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.GET);
		request.getQueryParams().put("queryadp", "");
		request.setKey(taskid);
	}

}
