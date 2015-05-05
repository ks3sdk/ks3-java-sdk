package com.ksyun.ks3.service.response;

import com.ksyun.ks3.dto.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 杨春建 on 2014/10/20.
 */
public class PutBucketACLResponse extends Ks3WebServiceDefaultResponse<Ks3Result> {

    public int[] expectedStatus() {
        return new int[]{200};
    }

    @Override
    public void preHandle() {
    	this.result = new Ks3Result();
    }
}
