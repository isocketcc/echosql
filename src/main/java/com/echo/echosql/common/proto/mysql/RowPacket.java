package com.echo.echosql.common.proto.mysql;

import com.echo.echosql.common.proto.utils.BufferUtil;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * MySql服务器===>ECHOSQL
 * A row with the data for each column.
 *
 * NULL is sent as 0xfb
 *
 * everything else is converted into a string and is sent as Protocol::LengthEncodedString
 *
 *@see <a https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-ProtocolText::ResultsetRow>mysql</>
 */
public class RowPacket extends MySqlPacket {

    private static final byte NULL_MARK = (byte)251;

    private long columnCount;

    private List<byte[]> columnValues;

    public RowPacket() {
        columnValues = new ArrayList<>();
    }

    /**
     * 读取数据
     */
    public void read(BinaryPacket bin)
    {
        this.packetLength = bin.packetLength;
        this.packetId = bin.packetId;
        MySqlMessage msg = new MySqlMessage(bin.data);
        for(int i = 0; i< columnCount; i++)
        {
            columnValues.add(msg.readBytesWithVarLength());
        }
    }

    /**
     * 构造数据
     * @param bf
     */
    public void write(ByteBuf bf)
    {
        BufferUtil.writeUB3(bf,calcPacketSize());
        BufferUtil.writeUB(bf,packetId);
        for(int i = 0; i < columnCount;i++)
        {
            byte[] cv = columnValues.get(i);
            if(cv == null || cv.length == 0)
            {
                BufferUtil.writeUB(bf,RowPacket.NULL_MARK);
            }else
            {
                BufferUtil.writeLength(bf,cv.length);
                BufferUtil.writeBytes(bf,cv);
            }
        }
    }

    @Override
    protected String getPacketInfo() {
        return "MySql RowPackt";
    }

    @Override
    public int calcPacketSize() {
        int i = 0;
        for(int j = 0;j < columnCount;j++)
        {
            byte[] v = columnValues.get(j);
            i += (v == null || v.length == 0) ? 1 : BufferUtil.getLength(v);
        }
        return i;
    }

    public long getColumnCount() {
        return columnCount;
    }

    public List<byte[]> getColumnValues() {
        return columnValues;
    }

    public void setColumnCount(long columnCount) {
        this.columnCount = columnCount;
    }

    public void setColumnValues(List<byte[]> columnValues) {
        this.columnValues = columnValues;
    }
}
