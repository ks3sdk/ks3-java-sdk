package com.ksyun.ks3.utils;

/**
 * Codec SPI
 * 
 * @author Hanson Char
 */
interface Codec {
    public byte[] encode(byte[] src);
    public byte[] decode(byte[] src, final int length); 
}