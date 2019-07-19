package com.echo.echosql.router.transqueue;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.BlockingQueue;

public class CmdComsumer {

    private BlockingQueue<ByteBuf> cmdQueue;

    public CmdComsumer(BlockingQueue<ByteBuf> cmdQueue) {
        this.cmdQueue = cmdQueue;
    }

    public ByteBuf poll() throws InterruptedException {
        //System.out.println("命令结果消费");
        //return cmdQueue.take();
        return null;
    }

    /**
     * 返回的是队列的长度
     * @return
     */
    public long getQueueLength()
    {
        return cmdQueue.size();
    }
}
