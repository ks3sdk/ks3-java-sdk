package com.ksyun.ks3.dto;

import java.io.Serializable;

import com.ksyun.ks3.utils.StringUtils;


/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月14日 下午6:01:00
 * 
 * @description Ks3用户
 **/
public class Owner{

    private String displayName;
    private String id;

    public Owner() {
    }

    public Owner(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String toString() {
    	 return StringUtils.object2string(this);
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String name) {
        this.displayName = name;
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Owner)) {
            return false;
        }

        Owner otherOwner = (Owner)obj;

        String otherOwnerId = otherOwner.getId();
        String otherOwnerName = otherOwner.getDisplayName();
        String thisOwnerId = this.getId();
        String thisOwnerName = this.getDisplayName();

        if (otherOwnerId == null) otherOwnerId = "";
        if (otherOwnerName == null) otherOwnerName = "";
        if (thisOwnerId == null) thisOwnerId = "";
        if (thisOwnerName == null) thisOwnerName = "";

        return (otherOwnerId.equals(thisOwnerId) &&
                otherOwnerName.equals(thisOwnerName));
    }
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        } else {
            return 0;
        }
    }
}
