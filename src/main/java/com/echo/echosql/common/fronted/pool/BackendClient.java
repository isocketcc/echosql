package com.echo.echosql.common.fronted.pool;

import com.echo.echosql.common.backend.factory.BackendConnectFactory;
import com.echo.echosql.common.proto.mysql.MySqlPacket;
import com.echo.echosql.router.transqueue.ResComsumer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

//实现后端和前端一起启动
public class BackendClient {
    public  static int count = 0;

    private ChannelHandlerContext ctx = null;//前端地址

    private CountDownLatch latch = null;  //线程同步 标记

    private BlockingQueue<ByteBuf> cmdQueue = null; //队列的容量可以配置

    private BlockingQueue<ByteBuf> resQueue = null;  //接受结果队列

    private BackendConnectFactory backendConnectFactory = null;

    private ResComsumer resComsumer = null;

    Thread resThread = null;
    public BackendClient() {
        latch = new CountDownLatch(MySqlPacket.DB_NODE);
        cmdQueue = new ArrayBlockingQueue<>(128);
        resQueue = new ArrayBlockingQueue<>(128);
        resComsumer = new ResComsumer(resQueue);
        backendConnectFactory = new BackendConnectFactory();
        backendConnectFactory.setResQueue(resQueue);
        backendConnectFactory.setCmdQueue(cmdQueue);
        backendConnectFactory.setLatch(latch);
        //backendConnectFactory = new BackendConnectFactory();
        /*backendConnectFactory.setResQueue(resQueue);
        backendConnectFactory.setCmdQueue(cmdQueue);
        backendConnectFactory.setLatch(latch);*/
        this.resThread = new Thread(resComsumer);   //对象对线程进行管理

    }

    /**
     * 启动后端连接
     */
    public void startBackendClient()
    {
        if(!this.backendConnectFactory.isAlive())
        {
            this.backendConnectFactory.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String toString() {
        return Integer.toString(this.hashCode());
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ResComsumer getResComsumer() {
        return resComsumer;
    }

    public Thread getResThread() {
        return resThread;
    }

    public BlockingQueue<ByteBuf> getCmdQueue() {
        return cmdQueue;
    }

    public BlockingQueue<ByteBuf> getResQueue() {
        return resQueue;
    }

    public BackendConnectFactory getBackendConnectFactory() {
        return backendConnectFactory;
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
