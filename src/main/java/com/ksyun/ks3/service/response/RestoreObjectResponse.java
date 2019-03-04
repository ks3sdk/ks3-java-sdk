package com.ksyun.ks3.service.response;

import org.apache.http.Header;

import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.RestoreObjectResult;
import com.ksyun.ks3.dto.RestoreObjectResult.RestoreCycle;
import com.ksyun.ks3.service.common.StorageClass;

public class RestoreObjectResponse extends Ks3WebServiceDefaultResponse<RestoreObjectResult> {

	@Override
	public void preHandle() {
		result = new RestoreObjectResult();
		int statusCode = this.getHttpResponse().getStatusLine().getStatusCode();
		if (statusCode == 200 || statusCode == 202) {
			Header[] headers = this.getHttpResponse().getAllHeaders();
			for (int i = 0; i < headers.length; i++) {
				if (headers[i].getName().equalsIgnoreCase(Constants.specHeaderPrefix + "storageclass")) {
					result.setCls(StorageClass.fromValue(headers[i].getValue()));
				}
			}
		}
		switch(statusCode) {
		case 200:
			result.setType(RestoreCycle.RESTORED);
			break;
		case 202:
			result.setType(RestoreCycle.RESTORE);
			break;
		case 409:
			result.setType(RestoreCycle.RESTORING);
			break;
		default:
			break;
		}
	}

	@Override
	public int[] expectedStatus() {
		return new int[] {200, 202, 409};
	}

}
