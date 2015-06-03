package com.ksyun.ks3.service;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class NoKeyPutTest extends Ks3ClientTest{
	String bucket = "eflakee";
	@Test
	public void testPut(){
		client1.putObject(bucket,"123",new ByteArrayInputStream("123.jpg".getBytes()),null);
	}
	public void testPut1(){
		
	}
}
