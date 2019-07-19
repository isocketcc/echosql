package com.echo.echosql.router;

import com.echo.echosql.common.proto.mysql.BinaryPacket;
import io.netty.buffer.ByteBuf;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MsgQueue {
    //稍后改为泛型
    public static Queue<ByteBuf> cmdQueue = new ConcurrentLinkedQueue<>();

    public static Queue<ByteBuf> resQueue = new ConcurrentLinkedQueue<>();
}
