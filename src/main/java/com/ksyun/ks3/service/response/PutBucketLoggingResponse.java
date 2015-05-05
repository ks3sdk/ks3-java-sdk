package com.ksyun.ks3.service.response;

import com.ksyun.ks3.dto.Ks3Result;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月16日
 * 
 * @description 编辑bucket的日志配置
 **/
public class PutBucketLoggingResponse extends Ks3WebServiceDefaultResponse<Ks3Result> {

	public int[] expectedStatus() {
		return new int[]{200};
	}

	@Override
	public void preHandle() {
		this.result = new Ks3Result();
	}

}
