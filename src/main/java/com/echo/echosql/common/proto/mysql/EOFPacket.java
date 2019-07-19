package com.echo.echosql.common.proto.mysql;

import com.echo.echosql.common.proto.utils.BufferUtil;
import io.netty.buffer.ByteBuf;

/**
 * 服务端===》echosql
 * int<1>	header	[fe] EOF header
 * if capabilities & CLIENT_PROTOCOL_41 {
 *   int<2>	warnings	number of warnings
 *   int<2>	status_flags	Status Flags
 * }
 * @see <a https://dev.mysql.com/doc/internals/en/packet-EOF_Packet.html>mysql</a>
 */
public class EOFPacket extends MySqlPacket {

    public static final byte FIELD_COUNT = (byte) 0xFE;

    private byte header = FIELD_COUNT;    //头

    private int warningsCount;  //警告数

    private int statusFlags = 2;    //状态标记

    /**
     * 读取数据包的信息
     * @param bin
     */
    public void read(BinaryPacket bin)
    {
        this.packetLength = bin.packetLength;
        this.packetId  =bin.packetId;
        MySqlMessage msg = new MySqlMessage(bin.data);
        this.header = msg.readUB();
        this.warningsCount = msg.readUB2();
        this.statusFlags = msg.readUB2();
    }

    /**
     * 写入数据包
     * @param bf
     */
    public void write(ByteBuf bf)
    {
        BufferUtil.writeUB3(bf,calcPacketSize());
        BufferUtil.writeUB(bf,packetId);
        BufferUtil.writeUB(bf,header);
        BufferUtil.writeUB2(bf,warningsCount);
        BufferUtil.writeUB2(bf,statusFlags);
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL EOFPacket ";
    }

    @Override
    public int calcPacketSize() {
        int i = 1;
        i += 2;
        i += 2;
        return i;
    }

    public static byte getFieldCount() {
        return FIELD_COUNT;
    }

    public byte getHeader() {
        return header;
    }

    public void setHeader(byte header) {
        this.header = header;
    }

    public int getWarningsCount() {
        return warningsCount;
    }

    public void setWarningsCount(int warningsCount) {
        this.warningsCount = warningsCount;
    }

    public int getStatusFlags() {
        return statusFlags;
    }

    public void setStatusFlags(int statusFlags) {
        this.statusFlags = statusFlags;
    }
}
