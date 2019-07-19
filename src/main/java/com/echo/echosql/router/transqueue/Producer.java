package com.echo.echosql.router.transqueue;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.BlockingQueue;

public class Producer {

    private BlockingQueue<ByteBuf> cmdQueue;

    public Producer(BlockingQueue<ByteBuf> cmdQueue) {
        this.cmdQueue = cmdQueue;
    }

    public void put(ByteBuf bf)
    {
        try {
            cmdQueue.put(bf);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
