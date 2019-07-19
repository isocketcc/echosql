package com.echo.echosql.common.proto.mysql;
import com.echo.echosql.common.proto.utils.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
/**
 * 定义包的外层结构
 * 将数据包产传给下一个数据包进行处理
 */
public class BinaryPacket extends MySqlPacket {
    public static final byte OK = 0;

    public byte[] data;

    @Override
    public int calcPacketSize() {
        return data == null ? 0 : data.length;
    }

    @Override
    public void write(ByteBuf bf)
    {
        BufferUtil.writeUB3(bf,this.packetLength);
        BufferUtil.writeUB(bf,this.packetId);
        BufferUtil.writeBytes(bf,this.data);
    }

    @Override
    protected String getPacketInfo() {
        return "MySql Binary Packet";
    }
    /**
     * data域分配空间
     * @param length
     */
    public void initData(int length) {
        this.data = new byte[length];
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}