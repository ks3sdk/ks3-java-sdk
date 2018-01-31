package com.ksyun.ks3.service.response;

import com.ksyun.ks3.dto.ListPartsResult;
import com.ksyun.ks3.dto.Owner;
import com.ksyun.ks3.dto.Part;
import com.ksyun.ks3.utils.DateUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * @date 2014年10月23日 下午2:53:03
 * @description
 **/
public class ListPartsResponse extends Ks3WebServiceXmlResponse<ListPartsResult> {

    private Owner owner;
    private Part part;

    public int[] expectedStatus() {
        return new int[]{200};
    }

    @Override
    public void preHandle() {
    }

    @Override
    public void startDocument() throws SAXException {
        result = new ListPartsResult();
    }

    @Override
    public void startEle(String uri, String localName, String qName,
                         Attributes attributes) throws SAXException {
        if ("Initiator".equalsIgnoreCase(qName)) {
            owner = new Owner();
        } else if ("Owner".equalsIgnoreCase(qName)) {
            owner = new Owner();
        } else if ("Part".equalsIgnoreCase(qName)) {
            part = new Part();
        }
    }

    @Override
    public void endEle(String uri, String localName, String qName)
            throws SAXException {
        if ("Initiator".equalsIgnoreCase(qName)) {
            result.setInitiator(owner);
        } else if ("Owner".equalsIgnoreCase(qName)) {
            result.setOwner(owner);
        } else if ("Part".equalsIgnoreCase(qName)) {
            result.getParts().add(part);
        }
    }

    @Override
    public void string(String s) {
        if ("Initiator".equalsIgnoreCase(getTag(1))) {
            if ("ID".equalsIgnoreCase(getTag())) {
                owner.setId(s);
            } else if ("DisplayName".equalsIgnoreCase(getTag())) {
                owner.setDisplayName(s);
            }
        } else if ("Owner".equalsIgnoreCase(getTag(1))) {
            if ("ID".equalsIgnoreCase(getTag())) {
                owner.setId(s);
            } else if ("DisplayName".equalsIgnoreCase(getTag())) {
                owner.setDisplayName(s);
            }
        } else if ("Part".equalsIgnoreCase(getTag(1))) {
            if ("PartNumber".equalsIgnoreCase(getTag())) {
                part.setPartNumber(Integer.parseInt(s));
            } else if ("LastModified".equalsIgnoreCase(getTag())) {
                part.setLastModified(DateUtils.convertStr2Date(s));
            } else if ("ETag".equalsIgnoreCase(getTag())) {
                part.setETag(s);
            } else if ("Size".equalsIgnoreCase(getTag())) {
                part.setSize(Long.parseLong(s));
            }
        } else {
            if ("Bucket".equalsIgnoreCase(getTag())) {
                result.setBucketname(s);
            } else if ("Key".equalsIgnoreCase(getTag())) {
                result.setKey(s);
            } else if ("UploadId".equalsIgnoreCase(getTag())) {
                result.setUploadId(s);
            } else if ("PartNumberMarker".equalsIgnoreCase(getTag())) {
                result.setPartNumberMarker(s);
            } else if ("NextPartNumberMarker".equalsIgnoreCase(getTag())) {
                result.setNextPartNumberMarker(s);
            } else if ("MaxParts".equalsIgnoreCase(getTag())) {
                result.setMaxParts(s);
            } else if ("IsTruncated".equalsIgnoreCase(getTag())) {
                result.setTruncated("true".equalsIgnoreCase(s));
            } else if ("Encoding-Type".equals(getTag())) {
                result.setEncodingType(s);
            } else if ("StorageClass".equals(getTag())) {
                result.setStorageClass(s);
            }
        }
    }
}
