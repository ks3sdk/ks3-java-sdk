package com.ksyun.ks3.service.common;

public enum BucketType {

	/**
     * 默认的KS3存储类型。Standard 提供较高的持久性、可用性和性能对象存储。
     */
    Normal("NORMAL"),

    /**
     * 用于不频繁访问但在需要时也要求快速访问的数据。Standard – IA 提供较高的持久性、吞吐量和较低的延迟
     */
    Archive("ARCHIVE");

    public static BucketType fromValue(String typeString) throws IllegalArgumentException {
        for (BucketType type : BucketType.values()) {
            if (type.toString().equals(typeString)) return type;
        }

        throw new IllegalArgumentException("Cannot create enum from " + typeString + " value!");
    }

    private final String typeId;

    private BucketType(String id) {
        this.typeId = id;
    }

    @Override
    public String toString() {
        return typeId;
    }
}
