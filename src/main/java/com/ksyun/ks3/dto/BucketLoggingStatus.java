package com.ksyun.ks3.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月16日
 * 
 * @description 获取bucket的日志配置
 **/
public class BucketLoggingStatus extends Ks3Result{
	/**
	 * 是否开启日志功能
	 */
	private boolean enable = false;
	/**
	 * 存储日志的bucket
	 */
	private String targetBucket;
	/**
	 * 日志文件前缀
	 */
	private String targetPrefix;
	/**
	 * 日志权限信息（暂不支持）
	 */
	private HashSet<Grant> targetGrants = new HashSet<Grant>();

	@Override
	public String toString() {
		return StringUtils.object2string(this);
	}

	/**
	 * 是否开启了日志
	 */
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	/**
	 * 存储日志的bucket
	 */
	public String getTargetBucket() {
		return targetBucket;
	}

	public void setTargetBucket(String targetBucket) {
		this.targetBucket = targetBucket;
	}

	/**
	 * 日志文件的前缀
	 */
	public String getTargetPrefix() {
		return targetPrefix;
	}

	public void setTargetPrefix(String targetPrefix) {
		this.targetPrefix = targetPrefix;
	}

	/**
	 * 日志权限
	 */
	public HashSet<Grant> getTargetGrants() {
		return targetGrants;
	}

	public void setTargetGrants(HashSet<Grant> targetGrants) {
		this.targetGrants = targetGrants;
	}
	public void addGrant(Grant grant){
		this.targetGrants.add(grant);
	}
}
