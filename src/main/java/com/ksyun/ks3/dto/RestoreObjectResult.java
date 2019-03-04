package com.ksyun.ks3.dto;

import com.ksyun.ks3.service.common.StorageClass;

public class RestoreObjectResult extends Ks3Result{
	
	public enum RestoreCycle {
		RESTORE,	//解冻
		RESTORING,	//解冻中
		RESTORED;	//已解冻
	}
	
	private StorageClass cls;
	
	private RestoreCycle type;

	public StorageClass getCls() {
		return cls;
	}

	public void setCls(StorageClass cls) {
		this.cls = cls;
	}

	public RestoreCycle getType() {
		return type;
	}

	public void setType(RestoreCycle type) {
		this.type = type;
	}
}
