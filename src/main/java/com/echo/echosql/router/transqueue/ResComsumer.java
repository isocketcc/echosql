package com.echo.echosql.router.transqueue;
import com.echo.echosql.common.proto.mysql.*;
import com.echo.echosql.common.proto.utils.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ResComsumer implements  Runnable{
    private BlockingQueue<ByteBuf> resQueue;

    private MySqlMessage msgBuf = new MySqlMessage();

    private ByteBuf resBuf;

    private byte[] byteBuf;

    private long columnCount = 0;

    private int headSeq = 1;

    private int rowSeq = 3;

    private BinaryPacket bin = new BinaryPacket();

    private ArrayUtil<ByteBuf> packetList = new ArrayUtil<>(50);

    private ChannelHandlerContext ctx;

    private HeadPacket headPacket = new HeadPacket();

    private FieldPacket fieldPacket = new FieldPacket();

    private EOFPacket eofPacket1 = new EOFPacket();

    private RowPacket rowPacket = new RowPacket();

    private EOFPacket eofPacket2 = new EOFPacket();

    public ResComsumer(BlockingQueue<ByteBuf> resQueue) {
        this.resQueue = resQueue;
    }

    @Override
    public void run() {
        while(true)
        {
            try {
                resBuf = resQueue.take();
                System.out.println("测试是否接受到数据");
                byteBuf = new byte[resBuf.readableBytes()];
                resBuf.readBytes(byteBuf);
                msgBuf.setBufferMessage(byteBuf);
                switch (msgBuf.readUB()){
                    case (byte)0xfc:
                        //构造数据包头
                        setHeadPacket(msgBuf);
                        setFieldPacket(msgBuf);
                        setEofPacket1(msgBuf);
                        msgBuf.resetMark();   //复位
                        break;
                    case (byte)0xfe:
                        setEofPacket2(msgBuf);
                        msgBuf.resetMark();   //复位
                        if(MySqlPacket.EOF_STATE.get() %  MySqlPacket.DB_NODE  == 0)
                        {
                            putPacket();
                        }
                        break;
                    case (byte)0xfd:
                        setRowPacket(msgBuf);
                        msgBuf.resetMark();   //复位
                        break;
                    default:
                        System.out.println("unknow Type");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 构造包头
     * @param msg
     */
    public void setHeadPacket(MySqlMessage msg)
    {
        //System.out.println("   header");
        ByteBuf buffer = Unpooled.buffer();
        columnCount = msg.readUB4();
        headPacket.setColumnCount(columnCount);
        headPacket.packetId = (byte) (headSeq++);
        headPacket.write(buffer);
        //packetList.add(buffer);
        packetList.add(headPacket.packetId-1,buffer);
    }

    /**
     * 构造字段数据包
     * @param msg
     */
    public void setFieldPacket(MySqlMessage msg)
    {
        //System.out.println("   filed");
        byte[] schame =  msg.readBytesWithVarLength();  //读取数据库名称
        byte[] aliasTable = msg.readBytesWithVarLength();
        byte[] orgTable = msg.readBytesWithVarLength();    //读取表的名称
        int characterSet = msg.readUB2();
        long columnLength= msg.readUB4();
        byte columnType = msg.readUB();
        int flags2 = msg.readUB2();
        byte decimals = msg.readUB();
        for(int i = 0; i < columnCount;i++)
        {
            ByteBuf buffer = Unpooled.buffer();
            byte[] columnName = msg.readBytesWithVarLength();
            fieldPacket.setSchema(schame);
            fieldPacket.setAliasTable(aliasTable);
            fieldPacket.setOrgTable(orgTable);
            fieldPacket.setAliasColumnName(columnName);
            fieldPacket.setOrgColumnName(columnName);
            fieldPacket.setCharacterSet(characterSet);
            fieldPacket.setColumnLength(columnLength);
            fieldPacket.setColumnType(columnType);
            fieldPacket.setFlags2(flags2);
            fieldPacket.setDecimals(decimals);
            fieldPacket.setDefaults(null);
            fieldPacket.packetId = (byte)(headSeq++);
            fieldPacket.write(buffer);
            //packetList.add(buffer);
            packetList.add(fieldPacket.packetId-1,buffer);
        }
    }

    /**
     * 构造eof包
     * @param msg
     */
    public void setEofPacket1(MySqlMessage msg)
    {
        //System.out.println("   eof1");
        ByteBuf buffer = Unpooled.buffer();
        eofPacket1.packetId = (byte)(headSeq++);
        eofPacket1.setWarningsCount(msg.readUB2());
        eofPacket1.setStatusFlags(msg.readUB2());
        eofPacket1.write(buffer);
        //packetList.add(buffer);
        packetList.add(eofPacket1.packetId-1,buffer);
    }
    /**
     * 构造行数据包
     * @param msg
     */
    public void setRowPacket(MySqlMessage msg)
    {
        //System.out.println("   row");
        ByteBuf buffer = Unpooled.buffer();
        long pos = 0;  //位置
        columnCount = msg.readUB4();
        List<byte[]> rowDataList = new ArrayList<>();
        for(int i  = 0; i < columnCount;i++)
        {
            rowDataList.add(msg.readBytesWithVarLength());
        }
        rowPacket.setColumnCount(columnCount);
        rowPacket.setColumnValues(rowDataList);
        pos = columnCount+(rowSeq++);
        rowPacket.packetId = (byte)(pos);
        rowPacket.write(buffer);
        packetList.add((int)(pos-1),buffer);
    }
    /**
     * 构造eof数据包
     * @param msg
     */
    public void setEofPacket2(MySqlMessage msg)
    {
        // System.out.println("   eof2");
        ByteBuf buffer = Unpooled.buffer();
        eofPacket2.setWarningsCount(msg.readUB2());
        eofPacket2.setStatusFlags(msg.readUB2());
        eofPacket2.packetId = (byte)(columnCount+(rowSeq++));
        eofPacket2.write(buffer);
        packetList.add(eofPacket2.packetId-1,buffer);
    }
    /**
     * 将数据包输出用户
     */
    public void putPacket()
    {
        System.out.println("list链表的大小是:"+packetList.getSize());
        for(int i = 0;i < packetList.getSize();i++)
        {
            //System.out.println(ctx.channel().localAddress());
            ByteBuf b = packetList.get(i);
//            b.markReaderIndex();
//            int k = b.readableBytes();
//            for(int j = 0;j< k;j++)
//            {
//                System.out.print(" "+b.readByte());
//            }
//            b.resetReaderIndex();

             ctx.writeAndFlush(b.retain());
        }
        packetList.clear();
        resQueue.clear();
        this.headSeq = 1;
        this.rowSeq = 3;
    }
    /**
     * 返回的是队列的长度
     * @return
     */
    public long getQueueLength()
    {
        return resQueue.size();
    }


    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}