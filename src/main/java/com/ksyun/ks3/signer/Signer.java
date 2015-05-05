package com.ksyun.ks3.signer;

import com.ksyun.ks3.dto.Authorization;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.service.request.Ks3WebServiceRequest;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月6日 上午10:44:23
 * 
 * @description 签名计算器
 **/
public interface Signer {
	public void sign(Authorization auth,Request request);
}
