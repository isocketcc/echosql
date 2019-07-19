package com.echo.echosql.common.proto.utils;
import com.echo.echosql.common.backend.factory.HandlePacketsState;
import com.echo.echosql.common.proto.mysql.*;
import com.echo.echosql.router.transqueue.Producer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * 查询结果处理工具类
 */
public class QueryPacketUtil {
    //定义当前数据包的处理状态
    private  int responseType = HandlePacketsState.STATE_NONE.getValue();

    private  HandlePacketsState handlePacketsState;

    private HeadPacket headPacket;

    private  FieldPacket fieldPacket;

    private EOFPacket eofPacket1;

    private RowPacket rowPacket;

    private EOFPacket eofPacket2;

    private ByteBufUtil byteBufUtil;

    private List<byte[]> resultSet;  //数据结果中的行的信息

    private List<byte[]> resultSetHeader;  //结果的头部

    private List<byte[]> fieldSet;   //数据结果中的列的信息

    private List<ByteBuf> rowSet;  //数据包中行的数据

    private Producer resProducer;

    private int k;

    private Lock lock = new ReentrantLock(); //锁对象

    public  QueryPacketUtil(Producer resProducer) {
        headPacket = new HeadPacket();
        fieldPacket = new FieldPacket();
        eofPacket1 = new EOFPacket();
        rowPacket = new RowPacket();
        eofPacket2 = new EOFPacket();
        resultSet = new ArrayList<>();
        fieldSet = new ArrayList<>();
        rowSet = new ArrayList<>();
        byteBufUtil = new ByteBufUtil();
        this.resProducer = resProducer;
    }
    /**
     * 获取结果
     */
    public  List<byte[]> getResultRows(BinaryPacket bin)
    {
        if(responseType == 0 )   //当前解析状态为零
        {
            //System.out.println("head");
            headPacket.read(bin);
            responseType =  HandlePacketsState.STATE_COLUMN.getValue();
        } else if((responseType == 1) && (bin.data[0] != (byte)0xFE) )  //包头解析完毕 现在解析列数据
        {
            //System.out.println("filed");
            fieldPacket.read(bin);
            fieldSet.add(fieldPacket.getAliasColumnName());
        }else if((responseType == 1) && (bin.data[0] == (byte)0xFE))
        {
            //System.out.println("eof1");
            eofPacket1.read(bin);
            if(MySqlPacket.HEAD_STATE.getAndAdd(1) == 0)
            {
                ByteBuf bf = Unpooled.buffer();
                byteBufUtil.writeByte(bf,(byte) 0xfc);
                byteBufUtil.writeUB4(bf,headPacket.getColumnCount());
                byteBufUtil.writeWithVarLength(bf,fieldPacket.getSchema());
                byteBufUtil.writeWithVarLength(bf,fieldPacket.getAliasTable());
                byteBufUtil.writeWithVarLength(bf,fieldPacket.getOrgTable());
                byteBufUtil.writeUB2(bf,fieldPacket.getCharacterSet());
                byteBufUtil.writeUB4(bf,fieldPacket.getColumnLength());
                byteBufUtil.writeUB(bf,fieldPacket.getColumnType());
                byteBufUtil.writeUB2(bf,fieldPacket.getFlags2());
                byteBufUtil.writeUB(bf,fieldPacket.getDecimals());
                for(byte[] src : fieldSet)
                {
                    byteBufUtil.writeWithVarLength(bf,src);
                }
                byteBufUtil.writeUB2(bf, eofPacket1.getWarningsCount());
                byteBufUtil.writeUB2(bf, eofPacket1.getStatusFlags());
                resProducer.put(bf);
            }
            fieldSet.clear();   //清空list保证的单次数据不重复
            responseType = HandlePacketsState.STATE_ROW.getValue();
        }else if(responseType == 4 && (bin.data[0] != (byte)0xFE) )
        {
            //System.out.println("row");
            ByteBuf bf = Unpooled.buffer();
            long columnCount = headPacket.getColumnCount();
            rowPacket.setColumnCount(columnCount);
            rowPacket.read(bin);
            resultSet = rowPacket.getColumnValues();
            byteBufUtil.writeByte(bf,(byte) 0xfd);
            byteBufUtil.writeUB4(bf,columnCount);
            for(byte[] src : resultSet)
            {
                byteBufUtil.writeWithVarLength(bf,src);
            }
            resProducer.put(bf);
            resultSet.clear();
        }else if(responseType == 4 && (bin.data[0] == (byte)0xFE))
        {
            //System.out.println("eof2");
            eofPacket2.read(bin);
            if (MySqlPacket.EOF_STATE.addAndGet(1) %  MySqlPacket.DB_NODE  == 0)
            {
                ByteBuf bf = Unpooled.buffer();
                BufferUtil.writeUB(bf, eofPacket2.getHeader());
                BufferUtil.writeUB2(bf, eofPacket2.getWarningsCount());
                BufferUtil.writeUB2(bf, eofPacket2.getStatusFlags());
                resProducer.put(bf);
                MySqlPacket.HEAD_STATE.set(0);
//                MySqlPacket.EOF_STATE = 0;
//                MySqlPacket.HEAD_STATE =0;
//                MySqlPacket.FIELD_STATE = 0;
            }
            responseType = HandlePacketsState.STATE_NONE.getValue();  //将数据包的解析状态还原为初值 以便下次使用
        }
       if(resultSet != null)
        {
            return resultSet;
        }
        return null;
    }
    //返回数据结果
    public  int getResponseType() {
        return responseType;
    }
    //返回列信息集合
    public List<byte[]> getFieldSet() {
        return fieldSet;
    }
}
