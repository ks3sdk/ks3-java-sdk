package com.ksyun.ks3.service.response;

import com.ksyun.ks3.dto.PutAdpResult;
import com.ksyun.ks3.http.HttpHeaders;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年2月2日 下午5:04:42
 * 
 * @description 添加处理结果持久化任务结果
 **/
public class PutAdpResponse extends Ks3WebServiceDefaultResponse<PutAdpResult>{

	public int[] expectedStatus() {
		return new int[]{200};
	}

	@Override
	public void preHandle() {
		this.result = new PutAdpResult();
		result.setTaskId(super.getHeader(HttpHeaders.TaskId.toString()));
	}

}
