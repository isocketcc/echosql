package com.echo.echosql.common.fronted.factory;

import com.echo.echosql.common.fronted.pool.BackendClient;
import com.echo.echosql.common.fronted.pool.ConnectPoolFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.InetSocketAddress;

@Service("frontedConnectFactory")
public class FrontendConnectFactory extends  Thread{
    //方便打印日志的时候 输出日志所属的类
    static final Logger LOGGER = LoggerFactory.getLogger(FrontendConnectFactory.class);
    //服务器的端口号
    @Value("${server.port}")
    private int port;

    private static EventLoopGroup boss = new NioEventLoopGroup();
    private static EventLoopGroup work = new NioEventLoopGroup();
    @Autowired
    private FrontendHandlerFactory frontendHandlerFactory;
    //启动引导服务器
    private static ServerBootstrap b = new ServerBootstrap();

    ConnectPoolFactory connectPoolFactory = null;
    //资源池配置
    GenericObjectPoolConfig poolConfig = null;

    GenericObjectPool<BackendClient> backendClientPool = null;
    @Override
    public void run(){
        LOGGER.info("start EchoSqlServer");
        startBackendPool();
        startFrontendProxy();
    }
    /**
     * netty启动器
     */
    public void startFrontendProxy() {
        b.group(boss,work); //boss负责与客服端的tcp请求链接 worker负责与客服端的读写操作
        //设置Nio类型的channel
        b.channel(NioServerSocketChannel.class);
        //设置是监听的端口号
        b.localAddress(new InetSocketAddress(port));
        //设置通道初始化
        frontendHandlerFactory.setBackendClientPool(backendClientPool);

        b.childHandler(frontendHandlerFactory);
        try {
            //配置完成 开始绑定server
            //调用阻塞方法sync同步方法阻塞一直到绑定成功
            ChannelFuture f = b.bind().sync();
            //打印日志消息
            LOGGER.info(FrontendConnectFactory.class.getName() + "started and listen on" + f.channel().localAddress());
            //阻塞监听服务器关闭事件
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //关闭EventLoopGroup释放掉所有的资源
            work.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    public void startBackendPool()
    {
        poolConfig = new GenericObjectPoolConfig();
        //资源池配置
        poolConfig.setMinIdle(10);
        poolConfig.setMaxTotal(200);
        connectPoolFactory = new ConnectPoolFactory();
        backendClientPool = new GenericObjectPool<BackendClient>(connectPoolFactory,poolConfig);
    }
}