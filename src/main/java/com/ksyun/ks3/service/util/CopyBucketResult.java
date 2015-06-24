package com.ksyun.ks3.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CopyBucketResult {
	private List<String> success = new ArrayList<String>();
	private Map<String,Exception> error = new HashMap<String,Exception>();
	public List<String> getSuccess() {
		return success;
	}
	public void setSuccess(List<String> success) {
		this.success = success;
	}
	public Map<String,Exception> getError() {
		return error;
	}
	public void setError(Map<String,Exception> error) {
		this.error = error;
	}
}
