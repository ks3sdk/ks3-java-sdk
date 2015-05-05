package com.ksyun.ks3.dto;

import java.util.HashMap;
import java.util.Map;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月10日 下午12:00:19
 * 
 * @description HEAD请求一个bucket
 **/
public class HeadBucketResult extends Ks3Result{
	/**
	 * http 状态码
	 */
	private int statueCode;
	/**
	 * 返回的htpp头信息
	 */
	private Map<String,String> headers = new HashMap<String,String>();
	public int getStatueCode() {
		return statueCode;
	}
	public void setStatueCode(int statueCode) {
		this.statueCode = statueCode;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public String toString()
	{
		return StringUtils.object2string(this);
	}
}
