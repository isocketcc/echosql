package com.echo.echosql.common.backend.handler;
import com.echo.echosql.common.backend.factory.BackendConnectState;
import com.echo.echosql.common.proto.mysql.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ChannelHandler.Sharable
//@Service("backendAuthHandler")
public class BackendAuthHandler extends ChannelInboundHandlerAdapter {
    //定义状态默认是没有认证
    private boolean  state = BackendConnectState.BACKEND_NOT_AUTHED.getValue();
//    @Autowired
//    private BackendCmdHandler backendCmdHandler;
    private int count = 0;
    private static final Logger logger = LoggerFactory.getLogger(BackendAuthHandler.class);

    private Lock lock = new ReentrantLock();

    private BlockingQueue<ByteBuf> cmdQueue = null;

    private BlockingQueue<ByteBuf> resQueue = null;

    private CountDownLatch latch = null;

    //private CmdComsumer CmdComsumer;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }
    @Override
    public void  channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println(" "+ctx.channel().localAddress());
            BinaryPacket bin = (BinaryPacket)msg;
            //System.out.println("接收到的函数");
           // System.out.println(new String(bin.data));
            //认证数据包已经发送或者认证通过
            System.out.println(state);
            if(state){
                //System.out.println("进入if语句");
                //System.out.println(bin.data[0]);
                //取第一个字段判断数据报的类型
                switch (bin.getData()[0])  //返回的是OK数据包
                {
                    case 0x00:
                        authOK(ctx,msg);         //返回的是OK数据包
                        break;
                    case (byte)0xFE:
                        break;
                    case (byte)0xFF:
                        authError(ctx,msg);
                        break;
                    default:
                        //System.out.println("PACKET FORMAT FOR IDENTIFICATION");
                        break;
                }
            }else{
                //System.out.println("进入认证的函数");
                auth(ctx,msg);
            }
    }
    /**
     * 认证处理
     */
    private synchronized void auth(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        BinaryPacket bin = (BinaryPacket)msg;
        //默认的大小是256
        ByteBuf authPacketBuffer = ctx.alloc().buffer();
        //解读服务器的握手包
        HandShakeV10Packet hsV10 = HandShakeV10Packet.newInstance();
        hsV10.read(bin);

        //构造认证包
        HandshakeResponse41Packet hsrV41 = HandshakeResponse41Packet.newInstance(ctx);
        hsrV41.initPacketInfo(hsV10);

        hsrV41.write(authPacketBuffer);
        //标记认证包已经发出

        state = BackendConnectState.BACKEND_SEND_AUTH.getValue();
//        lock.lock();
//        try{
//            int k = authPacketBuffer.readableBytes();
//            authPacketBuffer.markReaderIndex();
//            for(int i = 0; i < k;i++)
//            {
//                System.out.print(" "+authPacketBuffer.readByte());
//            }
//            System.out.println();
//        }finally {
//            lock.unlock();
//        }
//
//        authPacketBuffer.resetReaderIndex();
        //将构造好的数据返回给服务端
        ctx.writeAndFlush(authPacketBuffer);
    }
    /**
     * 成功认证
     * @param ctx
     * @param msg
     */
    private synchronized void authOK(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        BinaryPacket binPacket = (BinaryPacket)msg;
        //认证成功
        state = BackendConnectState.BACKEND_AUTHED.getValue();
        //线程同步减1
        latch.countDown();
        System.out.println(latch.getCount());

        logger.info("BACKEND AUTH OK");
        //OKPacket ok = OKPacket.newInstance();
        //解析消息
        //ok.read(binPacket);
        //ctx.writeAndFlush(cmd());
        //验证成功后替换掉当前认证Channel 替换为命令处理Chanel
        ctx.pipeline().replace(this, "backendCmdHandle", new BackendCmdHandler(resQueue));
        /*
         * 理论上是每个连接创建一个阻塞队列
         * 阻塞队列在netty框架基础上 不在创建多线程
         */
        //ctx.writeAndFlush(CmdComsumer.poll().retain());
    }
    /***
     * 错误认证
     * @param ctx
     * @param msg
     */
    private synchronized void authError(ChannelHandlerContext ctx, Object msg)
    {
        BinaryPacket bin = (BinaryPacket) msg;
        ERRPacket err = new ERRPacket();
        err.read(bin);
        logger.error("AUTH FAILED:"+err.getErrCode()+"--"+new String(err.getErrMessage()));
        state = BackendConnectState.BACKEND_NOT_AUTHED.getValue();
    }

    /**
     * 构造一个命令包
     */
    public static final ByteBuf cmd()
    {
        ByteBuf b = Unpooled.buffer();
        CMDPacket cmdPacket = new CMDPacket();
        cmdPacket.setCmd(MySqlPacket.COM_QUERY);
        cmdPacket.setArgs("select @@version_comment limit 1".getBytes());
        cmdPacket.write(b);
        return b;
    }

    public void setCmdQueue(BlockingQueue<ByteBuf> cmdQueue) {
        this.cmdQueue = cmdQueue;
    }

    public void setResQueue(BlockingQueue<ByteBuf> resQueue) {
        this.resQueue = resQueue;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }
}