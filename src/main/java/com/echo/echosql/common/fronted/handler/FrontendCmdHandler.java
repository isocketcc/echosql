package com.echo.echosql.common.fronted.handler;

import com.echo.echosql.common.fronted.pool.BackendClient;
import com.echo.echosql.common.proto.mysql.*;
import com.echo.echosql.common.proto.utils.ErrorCode;
import com.echo.echosql.router.transqueue.Producer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

//@Service("frontendCmdHandler")
public class FrontendCmdHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(FrontendCmdHandler.class);

    private GenericObjectPool<BackendClient> backendClientPool = null;

    private BackendClient backendClient = null;
//
//    private BlockingQueue<ByteBuf> cmdQueue = null;// = new ArrayBlockingQueue<>(1024); //队列的容量可以配置
//
//    private BlockingQueue<ByteBuf> resQueue = null;
    //map集合
    private HashMap<String, ArrayList<ChannelFuture>> channelMap = null;

    //定义命令的生产者 保证每一个连接都有独立的生产者队列
    private Producer cmdProducer = null;
    //结果消费者
    //private ResComsumer resComsumer = null;

    //private BackendConnectFactory backendConnection = null;

    private ChannelHandlerContext authCtx = null;

    /**
     * 设置命令通道的相关信息
     * @param
     */
    public void  setFrontendCmdHandler() {

        this.cmdProducer = new Producer(this.backendClient.getCmdQueue());

        //this.resComsumer = new ResComsumer(this.backendConnection.getResQueue());

        this.channelMap = backendClient.getBackendConnectFactory().getChannelMap();

        //this.authCtx = this.backendConnection.getCtx();
        asynWaitForData(backendClient.getCtx());
    }
    //注册消费事件
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BinaryPacket bin = (BinaryPacket)msg;

        logger.info("Received Actions:----"+new String(bin.getData()));

        //this.backendClient.setCtx(ctx);

        //向客服端返回一个Ok包 表示 表示已经准备好接受命令
        byte cmdType = bin.getData()[0];  //获取数据包类型
        CMDPacket cmd = new CMDPacket();

        //判断命令包的类型 进行相应的处理
        switch (cmdType)
        {
            case MySqlPacket.COM_QUERY:
                asnyWaitForCmd(ctx,bin,cmd);
                break;
            case MySqlPacket.COM_INIT_DB:
                break;
            case MySqlPacket.COM_PING:
                break;
            case MySqlPacket.COM_SLEEP:
                break;
            case MySqlPacket.COM_QUIT:
                backendClientPool.returnObject(backendClient);
                //backendPool.returnObject();
                break;
            case MySqlPacket.COM_FIELD_LIST:
                break;
            case MySqlPacket.COM_CREATE_DB:
                break;
            case MySqlPacket.COM_REFRESH:
                break;
            case MySqlPacket.COM_DROP_DB:
                break;
            case MySqlPacket.COM_SHUTDOWN:
                break;
            case MySqlPacket.COM_STATISTICS:
                break;
            case MySqlPacket.COM_PROCESS_INFO:
                break;
            case MySqlPacket.COM_CONNECT:
                break;
            case MySqlPacket.COM_PROCESS_KILL:
                break;
            case MySqlPacket.COM_DEBUG:
                break;
            case MySqlPacket.COM_TIME:
                break;
            case MySqlPacket.COM_DELAYED_INSERT:
                break;
            case MySqlPacket.COM_CHANGE_USER:
                break;
            case MySqlPacket.COM_RESET_CONNECTION:
                break;
            case MySqlPacket.COM_DAEMON:
                break;
            default:   //没有匹配到的命令设置为错误包
                ERRPacket err = new ERRPacket();
                err.packetId = 1;  //目前写死
                err.setErrCode(ErrorCode.ER_UNKNOWN_COM_ERROR);
                err.setErrMessage("UNKNOWN CMD".getBytes());
                ByteBuf bfERR = ctx.alloc().buffer();
                err.write(bfERR);
                ctx.writeAndFlush(bfERR);
                break;
        }
    }

    /**
     * 前端获取命令
     * @param ctx
     * @param bin
     * @param cmd
     */
    public void asnyWaitForCmd(ChannelHandlerContext ctx,BinaryPacket bin,CMDPacket cmd)
    {
        for(int i = 0;i < channelMap.get("127.0.0.1").size();i++) {
            ByteBuf bf = ctx.alloc().buffer();
            cmd.read(bin);
            cmd.write(bf);
            channelMap.get("127.0.0.1").get(i).channel().writeAndFlush(bf.retain());
            //System.out.println("后端的链接的地址:"+channelMap.get("127.0.0.1").get(i).channel().localAddress());
            bf.release();  //释放内存
        }
    }
    /**
     * 前端认证成功后 分析sql语句
     * 找到对应的数据库进行后台认证
     *
     */
    public void asynWaitForData(ChannelHandlerContext ctx)
    {
        this.backendClient.getResComsumer().setCtx(ctx);
        if(!this.backendClient.getResThread().isAlive())
        {
            this.backendClient.getResThread().start();
        }
    }
    //通道出现异常
    //通道使用完毕 需要关闭队列 disruptor.shutdown();

    public HashMap<String,ArrayList<ChannelFuture>> getChannelMap() {
        return channelMap;
    }

//    public BlockingQueue<ByteBuf> getCmdQueue() {
//        return cmdQueue;
//    }
//
//    public void setCmdQueue(BlockingQueue<ByteBuf> cmdQueue) {
//        this.cmdQueue = cmdQueue;
//    }
//
//    public BlockingQueue<ByteBuf> getResQueue() {
//        return resQueue;
//    }
//
//    public void setResQueue(BlockingQueue<ByteBuf> resQueue) {
//        this.resQueue = resQueue;
//    }

    public Producer getCmdProducer() {
        return cmdProducer;
    }

    public void setCmdProducer(Producer cmdProducer) {
        this.cmdProducer = cmdProducer;
    }

//    public ResComsumer getResComsumer() {
//        return resComsumer;
//    }
//
//    public void setResComsumer(ResComsumer resComsumer) {
//        this.resComsumer = resComsumer;
//    }

    public ChannelHandlerContext getAuthCtx() {
        return authCtx;
    }

    public void setAuthCtx(ChannelHandlerContext authCtx) {
        this.authCtx = authCtx;
    }

    public void setChannelMap(HashMap<String, ArrayList<ChannelFuture>> channelMap) {
        this.channelMap = channelMap;
    }

    public BackendClient getBackendClient() {
        return backendClient;
    }

    public void setBackendClient(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    public void setBackendClientPool(GenericObjectPool<BackendClient> backendClientPool) {
        this.backendClientPool = backendClientPool;
    }
}