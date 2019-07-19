package com.echo.echosql.common.fronted.handler;
import com.echo.echosql.common.fronted.pool.BackendClient;
import com.echo.echosql.common.proto.mysql.*;
import com.echo.echosql.common.proto.utils.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.security.NoSuchAlgorithmException;

@ChannelHandler.Sharable
@Service("frontendAuthHandler")
public class FrontendAuthHandler extends ChannelInboundHandlerAdapter {

    static final Logger LOGGER = LoggerFactory.getLogger(FrontendAuthHandler.class);

    private HandShakeV10Packet handShakeV10Packet = null;

    private GenericObjectPool<BackendClient> backendClientPool  = null;
    private BackendClient backendClient = null;
    private byte[] seed = {0};
    /**
     *  发送握手包
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //生成认证数据
        byte[] rand1 = RandomUtil.randomBytes(8);
        byte[] rand2 = RandomUtil.randomBytes(12);
        byte[] seed = new byte[rand1.length + rand2.length];
        this.seed = seed;
        System.arraycopy(rand1,0,seed,0,rand1.length);
        System.arraycopy(rand2,0,seed,rand1.length,rand2.length);

        //构造握手数据包
        HandShakeV10Packet hsV10 = HandShakeV10Packet.newInstance();
        //hsV10.packetLength = hsV10.calcPacketSize();
        hsV10.packetId = 0;
        hsV10.setProtocolVersin(Versions.PROTOCOL_VERSION);
        hsV10.setServerVersion(Versions.SERVER_VERSION);
        hsV10.setConnectionId(1000);  //目前写死
        hsV10.setAuthPluginDataPart1(rand1);
        hsV10.setServerCapability(getServerCapabilities());
        hsV10.setCharacterSet((byte)(CharsetUtil.getIndex("utf8") & 0xff));
        hsV10.setStatusFlags(2);
        hsV10.setAuthPluginDataLength((byte)(seed.length+1));
        hsV10.setAuthPluginDataPart2(rand2);
        hsV10.setAuthPluginName("mysql_native_password".getBytes());
        //发送方握手数据包
        ByteBuf bf = ctx.alloc().buffer();

        hsV10.write(bf);

        ctx.writeAndFlush(bf);  //返回的是握手认证包
    }
    //对前端的连接验证
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收客服端的回显数据包
        BinaryPacket bin = (BinaryPacket)msg;

        HandshakeResponse41Packet hsRes41 = HandshakeResponse41Packet.newInstance(ctx);
        hsRes41.read(bin);
        //判断用户名和密码是否合法
        if(!checkPassword(hsRes41.getAuthResponse(),hsRes41.getUserName()))
        {
            failure(ctx,ErrorCode.ER_ACCESS_DENIED_ERROR,"Access denied for user '"+hsRes41.getUserName().toString()+"'");
            return;
        }
        success(ctx);
    }

    /**
     * 密码校验
     * @param password
     * @param user
     * @return
     */
    public boolean checkPassword(byte[] password,byte[] user)
    {
        String username = "root";
        String pass = "123";

        if(pass == null || pass.length() == 0)
        {
            if(password == null || password.length == 0)
            {
                return true;
            }else
            {
                return false;
            }
        }
        if(password == null || password.length == 0)
        {
            return false;
        }
        byte[]  encryptPass = null;
        try {
            encryptPass = SecurityUtil.scramble411_2(pass.getBytes(),seed);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn(e.toString());
            return false;
        }
        if(encryptPass != null && (encryptPass.length == password.length))
        {
            int i = encryptPass.length;
            while(i-- != 0)
            {
                if(encryptPass[i] != password[i])
                {
                    return false;
                }
            }
        }else
        {
            return false;
        }
        return true;
    }
    /**
     * 用户验证成功 发送Ok数据包
     * 同时将管道改为处理客服端命令的管道
     * @param ctx
     */
    protected  void success(final ChannelHandlerContext ctx)
    {
        //从连接池中获取一个对象
        try {
            backendClient = backendClientPool.borrowObject();
            backendClient.startBackendClient();
            backendClient.setCtx(ctx);
            //backendConnection.getBackendConnectFactory().start();
            //backendClient.getLatch().wait();
            //System.out.println(backendConnection.toString());
            //backendPool.returnObject(backendConnection);
            FrontendCmdHandler frontendCmdHandler = new FrontendCmdHandler();
            frontendCmdHandler.setBackendClientPool(backendClientPool);   //将连接池放入到后端中去
            frontendCmdHandler.setBackendClient(backendClient);
            // frontendCmdHandler.setCmdQueue(backendConnection.getCmdQueue());
            // frontendCmdHandler.setResQueue(backendConnection.getResQueue());
            // frontendCmdHandler.setAuthCtx(ctx);
            //frontendCmdHandler.setBackendConnection(backendConnection.getBackendConnectFactory());
            frontendCmdHandler.setFrontendCmdHandler();
            ByteBuf authOk = ctx.alloc().buffer().writeBytes(OKPacket.AUTH_OK);
            ctx.writeAndFlush(authOk);
            //将当前的通道替换成命令通道
            ctx.pipeline().replace(this,"frontendCmdHandler",frontendCmdHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 检验失败处理函数
     * @param errno
     * @param info
     */
    protected void failure(final ChannelHandlerContext ctx,int errno,String info)
    {
        LOGGER.error(info);
        //返回错误编码包
        ERRPacket authErr = new ERRPacket();
        authErr.packetId = 2;
        authErr.setErrCode(errno);
        authErr.setErrMessage(info.getBytes());

        ByteBuf bf = ctx.alloc().buffer();
        authErr.write(bf);

        //返回客服端链接的错误码
        ctx.writeAndFlush(bf);
    }


    protected int getServerCapabilities() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        flag |= Capabilities.CLIENT_ODBC;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        flag |= Capabilities.CLIENT_PLUGIN_AUTH;
        return flag;
    }

    public void setBackendClientPool(GenericObjectPool<BackendClient> backendClientPool) {
        this.backendClientPool = backendClientPool;
    }
}
