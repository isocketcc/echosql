package com.echo.echosql.common.fronted.factory;

import com.echo.echosql.common.codec.MySqlPacketHeadDecoder;
import com.echo.echosql.common.fronted.handler.FrontendAuthHandler;
import com.echo.echosql.common.fronted.pool.BackendClient;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("frontendHandlerFactory")
public class FrontendHandlerFactory extends ChannelInitializer<SocketChannel> {
//    @Autowired
//    private MySqlPacketHeadDecoder mySqlPacketHeadDecoder;

    @Autowired
    private FrontendAuthHandler frontendAuthHandler;

    private GenericObjectPool<BackendClient> backendClientPool;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        frontendAuthHandler.setBackendClientPool(this.backendClientPool);

        System.out.println("frondtend initChannel");

        ch.pipeline().addLast(new MySqlPacketHeadDecoder());

        ch.pipeline().addLast(frontendAuthHandler);
    }

    public void setBackendClientPool(GenericObjectPool<BackendClient> backendClientPool) {
        this.backendClientPool = backendClientPool;
    }
}
