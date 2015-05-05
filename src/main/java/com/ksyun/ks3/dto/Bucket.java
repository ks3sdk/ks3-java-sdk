package com.ksyun.ks3.dto;

import java.util.Date;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月14日 下午6:00:34
 * 
 * @description 存储空间
 **/
public class Bucket extends Ks3Result{
	private static final long serialVersionUID = -8646831898339939580L;

	/**
	 * 存储空间名称，全局唯一
	 */
    private String name = null;
    /**
     * bucket拥有者
     */
    private Owner owner = null;
    /**
     * bucket创建时间
     */
    private Date creationDate = null;
    public Bucket() {}
    public Bucket(String name) {
        this.name = name;
    }
    public String toString() {
    	return StringUtils.object2string(this);
    }
    public int hashCode(){
    	return this.name.hashCode();
    }
    @Override
    public boolean equals(Object obj){
    	if(obj instanceof Bucket)
    	    return this.name.equals(((Bucket)obj).getName());
    	else
    		return false;
    }
    public Owner getOwner() {
        return owner;
    }
    public void setOwner(Owner owner) {
        this.owner = owner;
    }
    public Date getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
