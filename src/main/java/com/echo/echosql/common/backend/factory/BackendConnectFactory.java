package com.echo.echosql.common.backend.factory;
import com.echo.echosql.common.backend.config.BackendConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

//@Service("backendConnection")
public class BackendConnectFactory extends Thread{
        //方便打印日志的时候 输出日志所属的类
        static final Logger LOGGER = LoggerFactory.getLogger(BackendConnectFactory.class);
        //服务器的端口号
        @Value("${MySqlPort.port}")
        private int port = 3306;
        //数据库的ip
        @Value("${server.ip}")
        private String ip = "127.0.0.1";

        private CountDownLatch latch;

        private HashMap<String,ArrayList<ChannelFuture>> channelMap = new HashMap<>();

        private BlockingQueue<ByteBuf> cmdQueue = null;

        private BlockingQueue<ByteBuf> resQueue = null;

        private  List<ChannelFuture> futureList = new ArrayList<>();

        private BackendHandlerFactory backendHandlerFactory = null;
//        @Autowired
//        private BackendHandlerFactory backendHandlerFactory;

//        private BackendHandlerFactory backendHandlerFactory = new BackendHandlerFactory();

        public BackendConnectFactory()
        {
            backendHandlerFactory = new BackendHandlerFactory();
        }
        @Override
        public  void run() {
            LOGGER.info("Start BackendConnectFactory");
            backendHandlerFactory.setCmdQueue(this.cmdQueue);
            backendHandlerFactory.setResQueue(this.resQueue);
            backendHandlerFactory.setLatch(this.latch);
            startBackend();
        }

        public void startBackend() {
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE,true);
            b.handler(backendHandlerFactory);
            try {
                //synchronized (this) {
                    //开始连接请求
                    for (String str : BackendConfig.getConnectHost()) {
                        System.out.println("begin connect");
                        ChannelFuture f = b.connect(str, BackendConfig.getMysqlPort());
                        if (channelMap.containsKey(str)) {
                            channelMap.get(str).add(f);
                        } else {
                            ArrayList<ChannelFuture> value = new ArrayList();
                            value.add(f);
                            channelMap.put(str, value);
                        }
                        LOGGER.info(BackendConnectFactory.class.getName() + "started and listen on" + f.channel().localAddress());
                        futureList.add(f);
                        f.addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                //写操作完成
                                if(future.isSuccess())
                                {
                                    System.out.println("完成");
                                }else {
                                    future.cause().printStackTrace();;
                                }
                            }
                        });
                    }
                    //latch.countDown(); //一个连接完成 计数器减一
                    for (ChannelFuture future : futureList) {
                        future.channel().closeFuture().sync();
                    }
                //}
                    //打印日志
                } catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    workerGroup.shutdownGracefully();
                }

        }

    public HashMap<String, ArrayList<ChannelFuture>> getChannelMap() {
        return channelMap;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public void setChannelMap(HashMap<String, ArrayList<ChannelFuture>> channelMap) {
        this.channelMap = channelMap;
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
}
