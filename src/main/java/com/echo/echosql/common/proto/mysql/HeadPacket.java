package com.echo.echosql.common.proto.mysql;

import com.echo.echosql.common.proto.utils.BufferUtil;
import io.netty.buffer.ByteBuf;

/**
 * MySql服务器===>EchoSql
 *
 * lenenc_int column_count > 0
 *
 * column_count * Protocol::ColumnDefinition
 *
 * none or many ProtocolBinary::ResultsetRow
 *
 * EOF_Packet
 * @see <a https://dev.mysql.com/doc/internals/en/com-query-response.html#column-definition>mysql</a>
 */
public class HeadPacket extends  MySqlPacket{

    private long columnCount;    //数据的列的数量

    private long extra;          //额外的数据

    /**
     * 解析数据包
     * @param bin
     */
    public void read(BinaryPacket bin)
    {
        this.packetLength = bin.packetLength;
        this.packetId = bin.packetId;
        MySqlMessage msg = new MySqlMessage(bin.data);
        this.columnCount = msg.readVarLength();  //读取长度
        if(msg.hasRemaining())
        {
            this.extra = msg.readVarLength();
        }
    }
    /**
     * 构造数据包
     * @param bf
     */
    public void write(ByteBuf bf)
    {
        BufferUtil.writeUB3(bf,calcPacketSize());
        BufferUtil.writeUB(bf,packetId);
        BufferUtil.writeLength(bf,columnCount);
        if(extra > 0)
        {
            BufferUtil.writeLength(bf,extra);
        }
    }
    @Override
    protected String getPacketInfo() {
        return "MySql HeadPacket";
    }

    @Override
    public int calcPacketSize() {
        int i =  0;
        i += BufferUtil.getLength(this.columnCount);
        if(extra > 0)
        {
            i += BufferUtil.getLength(this.extra);
        }
        return i;
    }

    public long getColumnCount() {
        return columnCount;
    }

    public long getExtra() {
        return extra;
    }

    public void setColumnCount(long columnCount) {
        this.columnCount = columnCount;
    }

    public void setExtra(long extra) {
        this.extra = extra;
    }

}
