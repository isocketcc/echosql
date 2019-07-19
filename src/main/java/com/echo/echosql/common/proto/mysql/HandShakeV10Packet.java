package com.echo.echosql.common.proto.mysql;

import com.echo.echosql.common.proto.utils.BufferUtil;
import com.echo.echosql.common.proto.utils.Capabilities;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;

/**
 *  Mysql服务端===>EchoSql
 *   握手数据包
 *  1              [0a] protocol version
 * string[NUL]    server version
 * 4              connection id
 * string[8]      auth-plugin-data-part-1
 * 1              [00] filler
 * 2              capability flags (lower 2 bytes)
 *   if more data in the packet:
 * 1              character set
 * 2              status flags
 * 2              capability flags (upper 2 bytes)
 *   if capabilities & CLIENT_PLUGIN_AUTH {
 * 1              length of auth-plugin-data
 *   } else {
 * 1              [00]
 *   }
 * string[10]     reserved (all [00])
 *   if capabilities & CLIENT_SECURE_CONNECTION {
 * string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
 *   if capabilities & CLIENT_PLUGIN_AUTH {
 * string[NUL]    auth-plugin name
 *   }
 *
 * @see <a https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake>mysql</a>
 */
public class HandShakeV10Packet extends MySqlPacket {
    private static final byte[] FILLER_10 ={0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};

    private static final byte  FILLER = 0x00;

    private byte protocolVersin;      //协议版本

    private byte[] serverVersion;    //服务版本

    private long connectionId;       //链接ID

    private byte[] authPluginDataPart1;  //挑战随机数第一部分

    private int serverCapability;    //服务器权能值

    private byte characterSet;      //字节编码

    private int statusFlags;        //状态标记

    private byte[] authPluginDataPart2;  //挑战随机数第二部分

    private byte authPluginDataLength;

    private byte[] authPluginName;      //支持认证插件

    public HandShakeV10Packet() {}

    public void read(BinaryPacket bin)
    {
        this.packetLength = bin.packetLength;
        this.packetId = bin.packetId;
        MySqlMessage msg = new MySqlMessage(bin.data);
        this.protocolVersin = msg.readUB();
        this.serverVersion = msg.readBytesEndWidthNull();
        this.connectionId = msg.readUB4();
        this.authPluginDataPart1 = msg.readBytesEndWidthNull();
        this.serverCapability = msg.readUB2();
        this.characterSet = msg.readUB();
        this.statusFlags = msg.readUB2();
        this.serverCapability = (this.serverCapability | (msg.readUB2() << 16));
        if((serverCapability & Capabilities.CLIENT_PLUGIN_AUTH )!= 0)  {
            this.authPluginDataLength = msg.readUB();
        }
        else {
            msg.skip(1);
        }
        //跳过10个字节
        msg.skip(10);
        if((serverCapability & Capabilities.CLIENT_SECURE_CONNECTION) != 0)
        {
            int length = Math.max(13,this.authPluginDataLength - 8);
            this.authPluginDataPart2 = msg.readUBWithLength(length);
        }
        if((serverCapability & Capabilities.CLIENT_PLUGIN_AUTH) != 0)
        {
            this.authPluginName = msg.readBytesEndWidthNull();
        }
    }
    /**
     * 将数据包通过后端写出
     */
    public void write(ByteBuf bf){
        //写入包头
        BufferUtil.writeUB3(bf,calcPacketSize());
        BufferUtil.writeUB(bf,packetId);
        //写入包体
        BufferUtil.writeUB(bf,this.protocolVersin);
        BufferUtil.writeWithNull(bf,this.serverVersion);
        BufferUtil.writeUB4(bf,this.connectionId);
        BufferUtil.writeBytes(bf,this.authPluginDataPart1);
        BufferUtil.writeUB(bf,FILTER);
        BufferUtil.writeUB2(bf,(this.serverCapability));
        BufferUtil.writeUB(bf,this.characterSet);
        BufferUtil.writeUB2(bf,this.statusFlags);
        BufferUtil.writeUB2(bf,((this.serverCapability >>> 16)& 0xffff));  //服务权能标识 高12字节

        if((serverCapability & Capabilities.CLIENT_PLUGIN_AUTH) != 0)
        {
            BufferUtil.writeUB(bf,this.authPluginDataLength);
        }else {
            BufferUtil.writeUB(bf,(byte)0x00);
        }
        BufferUtil.writeBytes(bf,FILLER_10);
        if((serverCapability & Capabilities.CLIENT_SECURE_CONNECTION)!= 0)
        {
            BufferUtil.writeWithNull(bf,this.authPluginDataPart2);
        }
        if((serverCapability & Capabilities.CLIENT_PLUGIN_AUTH) != 0)
        {
            BufferUtil.writeWithNull(bf,this.authPluginName);
        }
    }

    @Override
    protected String getPacketInfo() {
        return "Mysql HandShakeV10 Packet";
    }

    @Override
    public int calcPacketSize() {
        int i = 1;
        i += this.serverVersion.length;
        i += 5;  //1+4
        i += this.authPluginDataPart1.length;
        i += 19;  //1 + 2 + 1 + 2 + 2 + 1 + 10
        if(((serverCapability & Capabilities.CLIENT_SECURE_CONNECTION)!= 0) && (this.authPluginDataPart2 != null))
        {
            i += this.authPluginDataPart2.length + 1;
        }
        if(((serverCapability & Capabilities.CLIENT_PLUGIN_AUTH) != 0) && this.authPluginName != null)
        {
            i += this.authPluginName.length+1;
        }
        return i;
    }

    /**
     * 返回握手包实例
     * @param
     * @return
     */
    public static final HandShakeV10Packet newInstance()
    {
        HandShakeV10Packet packet = new HandShakeV10Packet();

        return packet;
    }


    public static byte[] getFILLER_10() {
        return FILLER_10;
    }

    public byte getProtocolVersin() {
        return protocolVersin;
    }

    public void setProtocolVersin(byte protocolVersin) {
        this.protocolVersin = protocolVersin;
    }

    public byte[] getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(byte[] serverVersion) {
        this.serverVersion = serverVersion;
    }

    public long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(long connectionId) {
        this.connectionId = connectionId;
    }

    public byte[] getAuthPluginDataPart1() {
        return authPluginDataPart1;
    }

    public void setAuthPluginDataPart1(byte[] authPluginDataPart1) {
        this.authPluginDataPart1 = authPluginDataPart1;
    }

    public int getServerCapability() {
        return serverCapability;
    }

    public void setServerCapability(int serverCapability) {
        this.serverCapability = serverCapability;
    }

    public byte getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(byte characterSet) {
        this.characterSet = characterSet;
    }

    public int getStatusFlags() {
        return statusFlags;
    }

    public void setStatusFlags(int statusFlags) {
        this.statusFlags = statusFlags;
    }

    public byte[] getAuthPluginDataPart2() {
        return authPluginDataPart2;
    }

    public void setAuthPluginDataPart2(byte[] authPluginDataPart2) {
        this.authPluginDataPart2 = authPluginDataPart2;
    }

    public byte getAuthPluginDataLength() {
        return authPluginDataLength;
    }

    public void setAuthPluginDataLength(byte authPluginDataLength) {
        this.authPluginDataLength = authPluginDataLength;
    }

    public byte[] getAuthPluginName() {
        return authPluginName;
    }

    public void setAuthPluginName(byte[] authPluginName) {
        this.authPluginName = authPluginName;
    }


    @Override
    public String toString() {
        return "HandShakeV10Packet{" +
                "protocolVersin=" + protocolVersin +
                ", serverVersion=" + Arrays.toString(serverVersion) +
                ", connectionId=" + connectionId +
                ", authPluginDataPart1=" + Arrays.toString(authPluginDataPart1) +
                ", serverCapability=" + serverCapability +
                ", characterSet=" + characterSet +
                ", statusFlags=" + statusFlags +
                ", authPluginDataPart2=" + Arrays.toString(authPluginDataPart2) +
                ", authPluginDataLength=" + authPluginDataLength +
                ", authPluginName=" + Arrays.toString(authPluginName) +
                '}';
    }
}
