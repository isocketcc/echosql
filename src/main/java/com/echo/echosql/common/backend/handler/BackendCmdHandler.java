package com.echo.echosql.common.backend.handler;

import com.echo.echosql.common.proto.mysql.BinaryPacket;
import com.echo.echosql.common.proto.mysql.ERRPacket;
import com.echo.echosql.common.proto.mysql.MySqlPacket;
import com.echo.echosql.common.proto.mysql.OKPacket;
import com.echo.echosql.common.proto.utils.QueryPacketUtil;
import com.echo.echosql.router.transqueue.Producer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 处理请求命令
 */
//@Service("backendCmdHandler")
public class BackendCmdHandler extends ChannelInboundHandlerAdapter{
    private static final Logger logger = LoggerFactory.getLogger(BackendCmdHandler.class);

    int k = 0;

    private BlockingQueue<ByteBuf> resQueue;

    private Producer resProducer = null;

    private QueryPacketUtil queryPacketUtil = null;

    private Lock lock = new ReentrantLock();  //锁对象

    List<byte[]> resultSet = null;

    List<ByteBuf> resultRow = null;

    List<ByteBuf> resultFiled = null;

    public BackendCmdHandler(BlockingQueue<ByteBuf> resQueue) {

        //this.cmdComsumer = cmdComsumer;  //此处的消费者 就是认证阶段的消费者
        this.resQueue = resQueue;
        resProducer = new Producer(resQueue); //创建生产者
        queryPacketUtil = new QueryPacketUtil(this.resProducer);
        resultSet = new ArrayList<>();
        resultFiled = new ArrayList<>();
        resultRow = new ArrayList<>();
    }

    @Override
    public void  channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BinaryPacket bin = (BinaryPacket)msg;
        //System.out.println(new String(bin.data));

        if(MySqlPacket.QUERY_TYPE_SELECT == 1 && (bin.getData()[0] != 0x00) && (bin.getData()[0] != (byte)0xFF))  //如果前端发送的是数据查询命令
        {
            resultSet = queryPacketUtil.getResultRows(bin);

            if(queryPacketUtil.getResponseType() == 0)
            {
                //当前数据解析完毕 双重加锁才能保证其安全
//               for(int i = 0;i < resultSet.size();i++)
//               {
//                   System.out.println(new String(resultSet.get(i)));
//               }
                resultSet.clear();
                //System.out.println();
                waitForCMDData(ctx);
            }
        }else{
            switch (bin.getData()[0])  //返回的是OK数据包
            {
                case 0x00:
                    OKPacket ok = new OKPacket();
                    ok.read(bin);    //读取ok信息
                    logger.info("BackendCmdHandler COMMAND EXECUTION OK:----"+"Affected_rows:----"+ok.getAffectRows());      //返回的是OK数据包
                    waitForCMDData(ctx);
                    break;
                case (byte)0xFE:
                    logger.info("BackendCmdHandler COMMAND EXECUTION END:");
                    waitForCMDData(ctx);
                    break;
                case (byte)0xFF:
                    ERRPacket err = new ERRPacket();
                    err.read(bin);   //读取错误信息
                    logger.info("BackendCmdHandler COMMAND EXECUTION FAILED:----"+"ErrCode:----"+err.getErrCode()+"ErrMessage:----"+new String(err.getErrMessage()));
                    waitForCMDData(ctx);
                    break;
                default:
                    logger.info("PACKET FORMAT FOR IDENTIFICATION");
                    waitForCMDData(ctx);
                    break;
            }
        }
    }
    /**
     * 数据读取完成之后的回掉函数
     * @param ctx
     * @throws Exception
     */
    public void waitForCMDData(ChannelHandlerContext ctx) throws Exception {
        //ctx.writeAndFlush());
        //System.out.println("等待接收命令");
    }
//    /**
//     * 异常处理
//     * @param ctx
//     * @param cause
//     * @throws Exception
//     */
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
//            throws Exception {
//        ctx.channel().close();
//        logger.warn(ctx.channel().localAddress().toString()+"  Disconnect!");
//    }
}
