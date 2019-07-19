package com.echo.echosql.common.proto.utils;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class BufferUtil {
    /**
     * 写入带个字节
     * @param bf
     * @param b
     */
    public static final void writeByte(ByteBuf bf,byte b)
    {
        bf.writeByte(b);
    }
    /**
     * 向缓冲区中写入单个字节
     * @param buf
     * @param b
     */
    public static final void writeUB(ByteBuf buf,byte b)
    {
        buf.writeByte(b);
    }
    /***
     * 向缓冲区中写入两个字节
     * @param buf
     * @param i
     */
    public static final void writeUB2(ByteBuf buf,int i)
    {
        buf.writeByte((byte)(i & 0xFF));
        buf.writeByte((byte)((i >>> 8) & 0xFF));
    }
    /**
     * 向ByteBuf中写入3个字节的长度数据 注意字节序
     * @param buf
     * @param i
     */
    public static final void writeUB3(ByteBuf buf, int i)
    {
        buf.writeByte((byte)(i & 0xFF));
        buf.writeByte((byte)((i >>> 8) & 0xFF));
        buf.writeByte((byte)((i >>> 16) & 0xFF));
    }

    /***
     * 写入long类型的数据
     * @param buf
     * @param l
     */
    public static final void writeUB4(ByteBuf buf,long l)
    {
        buf.writeByte((byte)(l & 0xFF));
        buf.writeByte((byte)((l >>> 8) & 0xFF));
        buf.writeByte((byte)((l >>> 16) & 0xFF));
        buf.writeByte((byte)((l >>> 24) & 0xFF));
    }

    /***
     * 写入int类型的数据
     * @param buf
     * @param i
     */
    public static final void writeInt(ByteBuf buf,int i)
    {
        buf.writeByte((byte)(i & 0xFF));
        buf.writeByte((byte)((i >>> 8) & 0xFF));
        buf.writeByte((byte)((i >>> 16) & 0xFF));
        buf.writeByte((byte)((i >>> 24) & 0xFF));
    }

    /**
     * 写入长整型数据
     * @param buf
     * @param l
     */
    public static final void writeLong(ByteBuf buf,long l)
    {
        buf.writeByte((byte)(l & 0xFF));
        buf.writeByte((byte)((l >>> 8) & 0xFF));
        buf.writeByte((byte)((l >>> 16) & 0xFF));
        buf.writeByte((byte)((l >>> 24) & 0xFF));
        buf.writeByte((byte)((l >>> 32) & 0xFF));
        buf.writeByte((byte)((l >>> 40) & 0xFF));
        buf.writeByte((byte)((l >>> 48) & 0xFF));
        buf.writeByte((byte)((l >>> 56) & 0xFF));
        buf.writeByte((byte)((l >>> 64) & 0xFF));
    }
    /**
     * 写入边长数据的长度
     * @param buf
     * @param l
     */
    public static final void writeLength(ByteBuf buf,long l)
    {
        if(l < 251)
        {
            buf.writeByte((byte)l);
        }else if(l <  0x10000L)
        {
            buf.writeByte((byte)0xfc);
            writeUB2(buf,(int)l);
        }else if(l < 0x1000000L)
        {
            buf.writeByte((byte)0xfd);
            writeUB3(buf,(int)l);
        }else {
            buf.writeByte((byte)0xfe);
            writeUB4(buf,l);
        }
    }

    /**
     * 缓冲区中写入多个字节
     * @param bs
     */
    public static  final void writeBytes(ByteBuf buf,byte[] bs)
    {
        buf.writeBytes(bs);
    }

    /**
     * string[null] 类型的字符串写入
     * @param buf
     * @param src
     */
    public static final void writeWithNull(ByteBuf buf,byte[] src)
    {
        buf.writeBytes(src);
        buf.writeByte((byte)0);
    }

    /**
     * 写入动态字符串数据
     * @param buf
     * @param src
     */
    public static final void writeWithVarLength(ByteBuf buf,byte[] src)
    {
        int length = src.length;
        //写入字符串长度的标识
        if(length < 251)
        {
            buf.writeByte((byte)length);
        }else if(length < 0x10000L)
        {
            buf.writeByte((byte)252);
            writeUB2(buf,length);
        }else if(length < 0x1000000L)
        {
            buf.writeByte((byte)253);
            writeUB3(buf,length);
        }else
        {
            buf.writeByte((byte)254);
            writeLong(buf,length);
        }
        //写入实际字符串
        buf.writeBytes(src);
    }
    /**
     * 写入动态字符串数据 若字符串为空 写入null
     * @param buf
     * @param src
     * @param nullValue
     */
    public static final void writeWithVarLength(ByteBuf buf,byte[] src,byte nullValue)
    {
        if(src == null)
        {
            buf.writeByte(nullValue);
        }else{
            writeWithVarLength(buf,src);
        }
    }
    /**
     * 返回int<lenenc>字段所占的长度
     * @return
     */
    public static  final int getLength(long length)
    {
        if(length < 251)
        {
            return 1;
        }else if(length < 0x10000L)
        {
            return 3;
        }else if(length < 0x1000000L)
        {
            return 4;
        }else {
            return 9;
        }
    }
    /**
     * 返回string<lenenc>类型字段所占长度
     * @param src
     * @return
     */
    public static final int getLength(byte[] src)
    {
        int length = src.length;
        if(length < 251)
        {
            return 1 + length;  //返回长度表示字节数量 + 字符串字节数
        }else if(length < 0x0000L)
        {
            return 3 + length;
        }else if(length < 0x000000L)
        {
            return 4 + length;
        }else
        {
            return 9 + length;
        }
    }


}
