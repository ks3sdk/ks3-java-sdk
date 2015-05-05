package com.ksyun.ks3.dto;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 上午11:13:10
 * 
 * @description 
 **/
public class PartETag extends SSEResultBase{
	private int partNumber;
	private String eTag;
	
	public int getPartNumber() {
		return partNumber;
	}
	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}
	public String geteTag() {
		return eTag;
	}
	public void seteTag(String eTag) {
		this.eTag = eTag;
	}
	public PartETag(){}
	public PartETag(int partNumber,String eTag){
		this.partNumber = partNumber;
		this.eTag = eTag;
	}
	public String toString()
	{
		return StringUtils.object2string(this);
	}
	public boolean equals(Object obj)
	{
		if(obj instanceof PartETag)
		{
			if(this.partNumber == ((PartETag) obj).partNumber&&this.eTag.equals(((PartETag) obj).geteTag()))
			{
				return true;
			}
		}
		return false;
	}
	@Override
	public int hashCode()
	{
		return this.eTag.hashCode()+this.partNumber;
	}
}
