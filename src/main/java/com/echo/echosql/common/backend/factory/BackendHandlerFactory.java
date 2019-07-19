package com.echo.echosql.common.backend.factory;
import com.echo.echosql.common.backend.handler.BackendAuthHandler;
import com.echo.echosql.common.backend.handler.BackendCmdHandler;
import com.echo.echosql.common.codec.MySqlPacketHeadDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

//@Service("backendHandlerFactory")
public class BackendHandlerFactory extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(BackendCmdHandler.class);

    //    @Autowired
//    private MySqlPacketHeadDecoder mySqlPacketHeadDecoder;
//    @Autowired
//    private BackendAuthHandler backendAuthHandler;
    private CountDownLatch latch = null;

    private BlockingQueue<ByteBuf> cmdQueue = null;

    private BlockingQueue<ByteBuf> resQueue = null;

    //private BackendAuthHandler backendAuthHandler =  new BackendAuthHandler();

//    public BackendHandlerFactory(BlockingQueue<ByteBuf> cmdQueue,BlockingQueue<ByteBuf> resQueue) {
//        this.cmdQueue = cmdQueue;
//        this.resQueue = resQueue;
//    }
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        BackendAuthHandler backendAuthHandler =  new BackendAuthHandler();
        ch.pipeline().addLast(new MySqlPacketHeadDecoder());
        System.out.println("initChannel 后端的使用");
        if(this.resQueue != null)
        {
            backendAuthHandler.setResQueue(this.resQueue);
        }else {
            logger.warn(BackendHandlerFactory.class.getName(),"---resQueue not init");
        }
        if(this.cmdQueue != null)
        {
            backendAuthHandler.setCmdQueue(this.cmdQueue);
        }else {
            logger.warn(BackendHandlerFactory.class.getName(),"---cmdQueue not init");
        }
        if(latch != null)
        {
            backendAuthHandler.setLatch(latch);
        }else {
            logger.warn(BackendHandlerFactory.class.getName(),"---latch not init");
        }
        ch.pipeline().addLast(backendAuthHandler);     //后台验证
    }

    public BlockingQueue<ByteBuf> getCmdQueue() {
        return cmdQueue;
    }

    public void setCmdQueue(BlockingQueue<ByteBuf> cmdQueue) {
        this.cmdQueue = cmdQueue;
    }

    public BlockingQueue<ByteBuf> getResQueue() {
        return resQueue;
    }

    public void setResQueue(BlockingQueue<ByteBuf> resQueue) {
        this.resQueue = resQueue;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }
}