package com.echo.echosql.common.proto.mysql;

import com.echo.echosql.common.proto.utils.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 服务端===》EchoSql
 * int<1>	header	[ff] header of the ERR packet
 * int<2>	error_code	error-code
 * if capabilities & CLIENT_PROTOCOL_41 {
 *   string[1]	sql_state_marker	# marker of the SQL State
 *   string[5]	sql_state	SQL State
 * }
 * string<EOF>	error_message	human readable error message
 *
 * @see <a https://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html>mysql</a>
 */
public class ERRPacket extends MySqlPacket {

    private byte header = (byte)0xFF;  //包头

    private int errCode;  //错误码

    private byte sqlStateMarker = (byte)'#';  //sql状态标记

    private byte[] sqlState = "YH000".getBytes();   //sql状态

    private byte[] errMessage;  //错误消息

    /**
     * 读取消息
     * @param bin
     */
    public void read(BinaryPacket bin)
    {
        this.packetLength = bin.packetLength;
        this.packetId  =bin.packetId;
        MySqlMessage msg = new MySqlMessage(bin.data);
        this.header = msg.readUB();
        this.errCode = msg.readUB2();
        if(msg.hasRemaining() && (msg.read(msg.getPos()) == '#'))
        {
            msg.skip(1);
            this.sqlState = msg.readUBWithLength(5);
        }
        this.errMessage = msg.readBytesWithEnd();

    }

    /**
     * 写入数据包
     */
    public  void write(ByteBuf bf)
    {
        BufferUtil.writeUB3(bf,this.calcPacketSize());
        BufferUtil.writeUB(bf,this.packetId);
        BufferUtil.writeUB(bf,this.header);
        BufferUtil.writeUB2(bf,this.errCode);
        BufferUtil.writeUB(bf,this.sqlStateMarker);
        BufferUtil.writeBytes(bf,this.sqlState);
        if(errMessage != null)
        {
            BufferUtil.writeBytes(bf,errMessage);
        }
    }
    @Override
    protected String getPacketInfo() {
        return "MySQL ERRPacket";
    }

    @Override
    public int calcPacketSize() {
        int i = 9; //1 + 2 + 1 + 5
        if(errMessage != null)
        {
            i += errMessage.length;
        }
        return i;
    }

    public byte getHeader() {
        return header;
    }

    public void setHeader(byte header) {
        this.header = header;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public byte getSqlStateMarker() {
        return sqlStateMarker;
    }

    public void setSqlStateMarker(byte sqlStateMarker) {
        this.sqlStateMarker = sqlStateMarker;
    }

    public byte[] getSqlState() {
        return sqlState;
    }

    public void setSqlState(byte[] sqlState) {
        this.sqlState = sqlState;
    }

    public byte[] getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(byte[] errMessage) {
        this.errMessage = errMessage;
    }
}
