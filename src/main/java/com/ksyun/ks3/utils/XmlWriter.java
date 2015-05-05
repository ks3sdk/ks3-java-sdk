package com.ksyun.ks3.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.ksyun.ks3.config.Constants;


/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 *
 * @date 2014年10月15日 下午4:53:56
 *
 * @description
 **/
public class XmlWriter {
    private List<String> tag = new ArrayList<String>();
    private StringBuffer buffer = new StringBuffer();

    public XmlWriter start(String nodeName) {
        buffer.append("<" + nodeName + ">");
        this.tag.add(nodeName);
        return this;
    }

    public XmlWriter start(String nodeName, String param, String value) {
        buffer.append("<" + nodeName + " " + param + "=\"" + value + "\">");
        this.tag.add(nodeName);
        return this;
    }
    public XmlWriter start(String nodeName, String[] params, String[] values) {
    	if(params.length!=values.length)
    		throw new IllegalArgumentException("params.length should be equals with values.length");
    	
        buffer.append("<" + nodeName+" ");
        for(int i =0;i<params.length;i++){
        	buffer.append(params[i]+"=\""+values[i]+"\" ");
        }
        buffer.append(">");
        this.tag.add(nodeName);
        return this;
    }

    public XmlWriter startWithNs(String nodeName) {
        return start(nodeName, "xmlns", Constants.KS3_XML_NAMESPACE);
    }
    public XmlWriter end() {
        buffer.append("</" + tag.get(tag.size() - 1) + ">");
        tag.remove(tag.size() - 1);
        return this;
    }

    public XmlWriter value(String value) {
    	appendEscapedString(value,buffer);
        return this;
    }
    public XmlWriter value(int value) {
    	appendEscapedString(String.valueOf(value),buffer);
        return this;
    }

    @Override
    public String toString() {
    	String xml = buffer.toString();
    	LogFactory.getLog(this.getClass()).debug("xml to send is "+xml);
        return xml;
    }
    
    private void appendEscapedString(String s, StringBuffer builder) {
        if (s == null)
            s = "";
        int pos;
        int start = 0;
        int len = s.length();
        for (pos = 0; pos < len; pos++) {
            char ch = s.charAt(pos);
            String escape;
            switch (ch) {
            case '\t':
                escape = "&#9;";
                break;
            case '\n':
                escape = "&#10;";
                break;
            case '\r':
                escape = "&#13;";
                break;
            case '&':
                escape = "&amp;";
                break;
            case '"':
                escape = "&quot;";
                break;
            case '<':
                escape = "&lt;";
                break;
            case '>':
                escape = "&gt;";
                break;
            default:
                escape = null;
                break;
            }

            // If we found an escape character, write all the characters up to that
            // character, then write the escaped char and get back to scanning
            if (escape != null) {
                if (start < pos)
                    builder.append(s, start, pos);
                buffer.append(escape);
                start = pos + 1;
            }
        }

        // Write anything that's left
        if (start < pos) buffer.append(s, start, pos);
    }

}