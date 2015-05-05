package com.ksyun.ks3.utils;

import javax.xml.bind.DatatypeConverter;

/**
 * A Base 64 codec API.
 * 
 * See http://www.ietf.org/rfc/rfc4648.txt
 * 
 * @author Hanson Char
 */
public enum Base64 {
    ;
    private static final Base64Codec codec = new Base64Codec();
    
    /**
     * Returns a base 64 encoded string of the given bytes.
     */
    public static String encodeAsString(byte ... bytes) {
        if (bytes == null)
            return null;
        return DatatypeConverter.printBase64Binary(bytes);
    }
    
    /**
     * Returns a 64 encoded byte array of the given bytes.
     */
    public static byte[] encode(byte[] bytes) { return bytes == null || bytes.length == 0 ? bytes : codec.encode(bytes); }
    
    /** 
     * Decodes the given base 64 encoded string,
     * skipping carriage returns, line feeds and spaces as needed.
     */
    public static byte[] decode(String b64) {
        if (b64 == null)
            return null;
        if (b64.length() == 0)
            return new byte[0];
        byte[] buf = new byte[b64.length()];
        int len = CodecUtils.sanitize(b64, buf);
        return codec.decode(buf, len);
    }
    
    /** 
     * Decodes the given base 64 encoded bytes.
     */
    public static byte[] decode(byte[] b64) { return b64 == null || b64.length == 0 ? b64 :  codec.decode(b64, b64.length); }
}
