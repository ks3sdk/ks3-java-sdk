package com.ksyun.ks3.service.response;

import com.ksyun.ks3.dto.Ks3Result;

/**
 * Created by 杨春建 on 2014/10/20.
 */
public class PutObjectACLResponse extends Ks3WebServiceDefaultResponse<Ks3Result> {

    public int[] expectedStatus() {
        return new int[]{200};
    }

    @Override
    public void preHandle() {
    	this.result = new Ks3Result();
    }
}
