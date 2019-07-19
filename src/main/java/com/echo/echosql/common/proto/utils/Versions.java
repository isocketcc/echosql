package com.echo.echosql.common.proto.utils;

/**
 *  版本信息
 */
public interface Versions {
    /**
     * 使用的协议的版本
     */
    byte PROTOCOL_VERSION = 10;

    byte[] SERVER_VERSION = "1.1.0".getBytes();
}
