package com.ksyun.ks3.service.request;

import com.ksyun.ks3.dto.SSECustomerKey;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月20日 下午2:15:03
 * 
 * @description 
 **/
public interface SSECustomerKeyRequest {
	public void setSseCustomerKey(SSECustomerKey sseCustomerKey);
	public SSECustomerKey getSseCustomerKey();
}
