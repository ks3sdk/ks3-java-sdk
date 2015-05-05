package com.ksyun.ks3.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.ksyun.ks3.utils.StringUtils;

/** 
 * 包含{@link Owner}和{@link AccessControlList}
 * @author LIJUNWEI
 *
 */
public class AccessControlPolicy extends Ks3Result{

	/**
	 * acl
	 */
	private AccessControlList accessControlList = new AccessControlList();
    private Owner owner;

    public Owner getOwner() {
        return owner;
    }
    public void setOwner(Owner owner) {
        this.owner = owner;
    }
	public AccessControlList getAccessControlList() {
		return accessControlList;
	}
	public void setAccessControlList(AccessControlList accessControlList) {
		this.accessControlList = accessControlList;
	}
    public Set<Grant> getGrants()
    {
    	return this.accessControlList.getGrants();
    }
    public void setGrants(HashSet<Grant> grants)
    {
    	this.accessControlList.setGrants(grants);
    }
    public String toString()
    {
    	return StringUtils.object2string(this);
    }
    /**
     * 从AccessControlPolicy中提取CannedAccessControlList
     * @return {@link CannedAccessControlList}
     */
    public CannedAccessControlList getCannedAccessControlList(){
		final Collection<Permission> allUsersPermissions = new LinkedHashSet<Permission>();
		for (final Grant grant : this.getGrants()) {
			if (GranteeUri.AllUsers.equals(grant.getGrantee())) {
				allUsersPermissions.add(grant.getPermission());
			}
		}
		final boolean read = allUsersPermissions.contains(Permission.Read);
		final boolean write = allUsersPermissions.contains(Permission.Write);
		if (read && write) {
			return CannedAccessControlList.PublicReadWrite;
		} else if (read) {
			return CannedAccessControlList.PublicRead;
		} else {
			return CannedAccessControlList.Private;
		}
    }
}
