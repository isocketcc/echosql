package com.echo.echosql.common.codec;

import com.echo.echosql.common.proto.mysql.BinaryPacket;
import com.echo.echosql.common.proto.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("mySqlPacketHeadDecoder")
public class MySqlPacketHeadDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(MySqlPacketHeadDecoder.class);

    private static int packetHeaderSize = 4;  //Mysql数据包的报文头固定占4个字节

    private static int maxPacketSize =  16 * 1024 * 1024; //Mysql数据包的大小 不得超过16M

    /**
     * 去掉Mysql数据包的头部
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 4 bytes：3byte Length + 1 byte PacketdId
        if(internalBuffer().readableBytes() < packetHeaderSize)
        {
            logger.error("The size of packet received from server less then packetHeaderSize");
             return ;
        }
        //记录当前刻度区域中的索引
        in.markReaderIndex();
        //读取数据包的长度
        int packetLength = ByteUtil.readUB3(in);

        //Mysql单个数据包的最大长度是16M
        if(packetLength > maxPacketSize)
        {
            throw new IllegalArgumentException("Packet size over the limit"+maxPacketSize);
        }
        //读取数据包Id
        byte packetId = in.readByte();
        //半包回溯
        if(in.readableBytes() < packetLength)
        {
            in.resetReaderIndex();    //读标记复位
            logger.error("Packet received from the server is not a complete packet");
            return;
        }
        //构造新的数据报文 传递给下一个通道
        BinaryPacket packet = new BinaryPacket();
        packet.packetLength = packetLength;
        packet.packetId = packetId;
        packet.initData(packetLength);      //为了数据包的data域申请空间
        in.readBytes(packet.data);

        if(packet.data == null || packet.calcPacketSize() == 0)
        {
            logger.error("get data errormessage,packetLength ="+packet.packetLength);
        }
        out.add(packet);
    }
}