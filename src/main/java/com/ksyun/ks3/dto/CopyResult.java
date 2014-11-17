package com.ksyun.ks3.dto;

import java.util.Date;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[13810414122@163.com]  
 * 
 * @date 2014年11月17日 下午1:00:34
 * 
 * @description Copy Object 和 Copy Part的结果
 **/
public class CopyResult {
	private Date lastModified;
	private String ETag;
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public String getETag() {
		return ETag;
	}
	public void setETag(String eTag) {
		ETag = eTag;
	}
	public String toString(){
		return StringUtils.object2string(this);
	}
}
