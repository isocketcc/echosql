package com.echo.echosql.common.proto.mysql;

public class MySqlMessage {

    public static final long NULL_LENGTH = -1;

    public static final byte[] EMPTY_BYTES = new byte[0];

    private  byte[] innerByteBuf;

    private int pos = 0;

    private  int dataLength;

    public MySqlMessage() {

    }

    public MySqlMessage(byte[] buffer)
    {
        this.innerByteBuf = buffer;
        this.dataLength = buffer.length;
    }

    public void setBufferMessage(byte[] buffer)
    {
        this.innerByteBuf = buffer;
        this.dataLength = buffer.length;
    }
    /**
     * 读取以0x00结尾的字符串
     * @return byte[]
     */
    public byte[] readBytesEndWidthNull()
    {
        final byte[] b = this.innerByteBuf;
        if(pos > dataLength)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        int offset = -1;
        while(b[pos++] != 0)  //没有遇到null结尾 一直循环
        {
            offset++;
        }
        if(offset == 0)
        {
            return EMPTY_BYTES;
        }else
        {
            byte[] temp = new byte[offset+1];
            System.arraycopy(b,pos-2-offset,temp,0,offset+1);
            return temp;
        }
    }
    /**
     * 读取变长字符串
     * @return
     */
    public byte[] readBytesWithVarLength()
    {
        final byte[] b = this.innerByteBuf;
        int length = (int) readVarLength();  //获取长度
        if(length < 0)
        {
            return EMPTY_BYTES;
        }
        byte[] temp = new byte[length];
        System.arraycopy(b,pos,temp,0,temp.length);
        pos += length;
        return temp;
    }

    /**
     * 读取指制定字节
     * @param i
     * @return
     */
    public byte read(int i)
    {
        final byte[] b = this.innerByteBuf;

        return b[i];
    }

    /**
     * 读取从当前位置开始到字符串末尾全部字符串
     * @return
     */
    public byte[] readBytesWithEnd()
    {
        final byte[] b = this.innerByteBuf;

        if(pos >= dataLength)
        {
            return EMPTY_BYTES;
        }
        byte[] temp = new byte[dataLength - pos];
        System.arraycopy(b,pos,temp,0,temp.length);
        pos = dataLength;
        return temp;
    }

    /**
     * 读取固定字节的长度
     * @param length
     * @return
     */
    public byte[] readBytesWithFixLength(int length)
    {
        final byte[] b = this.innerByteBuf;
        byte[] temp = new byte[length];
        System.arraycopy(b,pos,temp,0,length);
        pos += length;
        return temp;
    }
    /**
     * 读取单个字节
     * @return byte
     */
    public byte readUB()
    {
        final byte[] b = this.innerByteBuf;

        return b[pos++];   //读取一个字节 read指针移动
    }

    /**
     * 读取两个字符
     * @return int
     */
    public int readUB2()
    {
        final byte[] b = this.innerByteBuf;
        int i = b[pos++] & 0xFF;
        i |= ((b[pos++] & 0xFF) << 8);
        return i;
    }

    /**
     * 读取三个字节
     * @return
     */
    public int readUB3()
    {
        final byte[] b = this.innerByteBuf;
        int i = (b[pos++] & 0xFF);
        i |= ((b[pos++] & 0xFF ) << 8);
        i |= ((b[pos++] & 0xFF) << 16);
        return i;
    }
    /**
     * 读取四个字节
     * @return long
     */
    public long readUB4()
    {
        final byte[] b = this.innerByteBuf;
        long l = (b[pos++] & 0xFF);
        l |= ((b[pos++] & 0xFF) << 8);
        l |= ((b[pos++] & 0xFF) << 16);
        l |= ((b[pos++] & 0xFF) << 32);
        return l;
    }

    /**
     * 读取一个int型数据
     * @return
     */
    public int readInt()
    {
        final byte[] b = this.innerByteBuf;
        int i = (b[pos++] & 0xFF);
        i |= ((b[pos++] & 0xFF ) << 8);
        i |= ((b[pos++] & 0xFF) << 16);
        i |= ((b[pos++] & 0xFF) << 24);
        return i;
    }
    /**
     * 读取长整型数据
     * @return
     */
    public long readLong()
    {
        final byte[] b = this.innerByteBuf;
        long l = (b[pos++] & 0xFF);
        l |= (long)((b[pos++] & 0xFF) << 8);
        l |= (long)((b[pos++] & 0xFF) << 16);
        l |= (long)((b[pos++] & 0xFF) << 24);
        l |= (long)((b[pos++] & 0xFF) << 32);
        l |= (long)((b[pos++] & 0xFF) << 40);
        l |= (long)((b[pos++] & 0xFF) << 48);
        l |= (long)((b[pos++] & 0xFF) << 56);
        return l;
    }
    /**
     * 跳过length字节数
     * @param length
     */
    public void skip(int length)
    {
        pos += length;
    }

    /**
     * 读取指定长度字节
     * @param length
     * @return
     */
    public byte[] readUBWithLength(int length)
    {
        final byte[] b = this.innerByteBuf;
        byte[] temp = new byte[length];
        System.arraycopy(b,pos,temp,0,length);
        pos += length;
        return temp;
    }
    /**
     * 读取int<lenenc>类型的数据
     * @return
     */
    public long readVarLength()
    {
        final byte[] b = this.innerByteBuf;
        int length = b[pos++] & 0xFF;
        switch (length)
        {
            case 251:
                return NULL_LENGTH;
            case 252:
                return readUB2();
            case 253:
                return readUB3();
            case 254:
                return readLong();
            default:
                return length;
        }
    }

    /**
     * 数据包没有读取完
     * @return
     */
    public boolean hasRemaining()
    {
        return dataLength > pos;
    }

    /**
     * 重置
     */
    public void resetMark()
    {
        pos = 0;
    }

    public byte[] getInnerByteBuf() {
        return innerByteBuf;
    }

    public int getPos() {
        return pos;
    }

    public long getDataLength() {
        return dataLength;
    }
}