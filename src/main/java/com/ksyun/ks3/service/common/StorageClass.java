package com.ksyun.ks3.service.common;

/**
 * @author qichao[qichao@kingsoft.com]  
 * @date 2016年3月1日 下午5:30:30
 * @description 存储类型枚举
 **/
public enum StorageClass {

    /**
     * 默认的KS3存储类型。Standard 提供较高的持久性、可用性和性能对象存储。
     */
    Standard("STANDARD"),

    /**
     * 用于不频繁访问但在需要时也要求快速访问的数据。Standard – IA 提供较高的持久性、吞吐量和较低的延迟
     */
    StandardInfrequentAccess("STANDARD_IA");

    public static StorageClass fromValue(String s3StorageClassString) throws IllegalArgumentException {
        for (StorageClass storageClass : StorageClass.values()) {
            if (storageClass.toString().equals(s3StorageClassString)) return storageClass;
        }

        throw new IllegalArgumentException("Cannot create enum from " + s3StorageClassString + " value!");
    }

    private final String storageClassId;

    private StorageClass(String id) {
        this.storageClassId = id;
    }

    @Override
    public String toString() {
        return storageClassId;
    }
}
