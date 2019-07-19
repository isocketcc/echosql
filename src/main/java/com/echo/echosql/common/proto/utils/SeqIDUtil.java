package com.echo.echosql.common.proto.utils;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SeqIDUtil{

    //一个channel在其生命周期内只会注册一个EventLoop 一个EventLoop在生命周期内只会绑定一个Thread
    public static final byte getSeq() {
        AtomicInteger seq = new AtomicInteger(1);
        int s = seq.getAndAdd(1);
        if (s >= 255) {
            seq.set(1);
        }
        return (byte) s;
    }
}