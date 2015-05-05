package com.ksyun.ks3.dto;

import java.util.ArrayList;
import java.util.List;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月10日 下午2:56:45
 * 
 * @description 批量删除结果
 **/
public class DeleteMultipleObjectsResult extends Ks3Result{
	/**
	 * 删除成功的object key
	 */
	private List<String> deleted = new ArrayList<String>();
	/**
	 * 删除失败的object key及错误信息
	 */
	private List<DeleteMultipleObjectsError> errors = new ArrayList<DeleteMultipleObjectsError>();
	public List<String> getDeleted() {
		return deleted;
	}
	public void addDelete(String deleted) {
		this.deleted.add(deleted);
	}
	public List<DeleteMultipleObjectsError> getErrors() {
		return errors;
	}
	public void addError(DeleteMultipleObjectsError error) {
		this.errors.add(error);
	}
	public String toString()
	{
		return StringUtils.object2string(this);
	}
}
