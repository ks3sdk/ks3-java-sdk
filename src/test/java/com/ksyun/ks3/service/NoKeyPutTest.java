package com.ksyun.ks3.service;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class NoKeyPutTest extends Ks3ClientTest{
	String bucket = "eflakee";
	String bucketsha1 = "whm";
	@Test
	public void testPut(){
		client1.putObject(bucket,"123",new ByteArrayInputStream("123.jpg".getBytes()),null);
	}
	@Test
	public void testPutSHA1(){
		client1.putObject(bucketsha1,"123",new ByteArrayInputStream("123.jpg".getBytes()),null);
	}
}
