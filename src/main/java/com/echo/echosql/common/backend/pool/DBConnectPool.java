package com.echo.echosql.common.backend.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;

/**
 * 后台数据库连接池
 */
public class DBConnectPool {

    static final Logger LOGGER = LoggerFactory.getLogger(DBConnectPool.class);

    public ChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap = null;

    private BlockingQueue<ByteBuf> cmdQueue;

    private BlockingQueue<ByteBuf> resQueue;

    protected ChannelHandlerContext frontCtx;

    public DBConnectPool(BlockingQueue<ByteBuf> cmdQueue, BlockingQueue<ByteBuf> resQueue,ChannelHandlerContext frontCtx) {
        this.frontCtx = frontCtx;
        this.cmdQueue = cmdQueue;
        this.resQueue = resQueue;
    }

    public void init()
    {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.TCP_NODELAY,true);
        b.option(ChannelOption.SO_KEEPALIVE,true);

//        poolMap = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
//            @Override
//            protected FixedChannelPool newPool(InetSocketAddress key) {
//                return new FixedChannelPool(b.remoteAddress(key), new BackendHandlerFactory(cmdQueue,resQueue),4); //单个host连接池大小
//            }
//        };
    }
}
