package com.echo.echosql.common.proto.utils;

import com.echo.echosql.common.backend.factory.HandlePacketsState;
import com.echo.echosql.common.proto.mysql.*;

import java.util.*;

/**
 * 查询结果处理工具类
 */
public class QueryPacketUtil01 {
    //定义当前数据包的处理状态
    private static int responseType = HandlePacketsState.STATE_NONE.getValue();

    private static HeadPacket headPacket = new HeadPacket();

    private static FieldPacket fieldPacket = new FieldPacket();

    private static EOFPacket eofPacket1 = new EOFPacket();

    private static RowPacket rowPacket = new RowPacket();

    private static EOFPacket eofPacket2 = new EOFPacket();

    private static List<byte[]> resultSet;  //接受结果

    /**
     * 获取结果
     */
    public  static List<byte[]> getResultRows(BinaryPacket bin)
    {
        if(responseType == 0 )   //当前解析状态为零
        {
            headPacket.read(bin);
            responseType =  HandlePacketsState.STATE_COLUMN.getValue();
        } else if((responseType == 1) && (bin.data[0] != (byte)0xFE) )  //包头解析完毕 现在解析列数据
        {
            fieldPacket.read(bin);
        } else if((responseType == 1) && (bin.data[0] == (byte)0xFE))
        {
            eofPacket1.read(bin);
            responseType = HandlePacketsState.STATE_ROW.getValue();
        }else if(responseType == 4 && (bin.data[0] != (byte)0xFE) )
        {
            long columnCount = headPacket.getColumnCount();
            rowPacket.setColumnCount(columnCount);
            rowPacket.read(bin);
            resultSet = rowPacket.getColumnValues();
        }else if(responseType == 4 && (bin.data[0] == (byte)0xFE))
        {
            eofPacket2.read(bin);
            responseType = HandlePacketsState.STATE_NONE.getValue();  //将数据包的解析状态还原为初值 以便下次使用
        }

       if(resultSet != null)
        {
            return resultSet;
        }
        return null;
    }

    public static int getResponseType() {
        return responseType;
    }

}
