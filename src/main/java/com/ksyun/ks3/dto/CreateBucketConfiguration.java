package com.ksyun.ks3.dto;

import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.utils.XmlWriter;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月10日 下午1:47:26
 * 
 * @description 
 * <p>public CreateBucketConfiguration({@link REGION} region)</p>
 * <P>bucket存储地点配置，用于{@link com.ksyun.ks3.service.request.CreateBucketRequest}</P>
 **/
public class CreateBucketConfiguration {
	/**
	 * bucket存储地点
	 * @author LIJUNWEI
	 *
	 */
	public static enum REGION {
		HANGZHOU,AMERICA,HONGKONG,BEIJING,SHANGHAI;
		public static REGION load(String s){
			for(REGION region :REGION.values()){
				if(region.toString().equals(s))
					return region;
			}
			throw ClientIllegalArgumentExceptionGenerator.notCorrect("region",s,"AMERICA,HANGZHOU,HONGKONG,BEIJING,SHANGHAI");
		}
	}
	public CreateBucketConfiguration(REGION region){
		this.location = region;
	}
	private REGION location;

	public REGION getLocation() {
		return location;
	}

	public void setLocation(REGION location) {
		this.location = location;
	}

	public String toString() {
		return StringUtils.object2string(this);
	}
}
