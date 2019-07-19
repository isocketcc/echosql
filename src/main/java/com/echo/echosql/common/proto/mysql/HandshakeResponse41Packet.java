package com.echo.echosql.common.proto.mysql;

import com.echo.echosql.common.proto.utils.BufferUtil;
import com.echo.echosql.common.proto.utils.Capabilities;
import com.echo.echosql.common.proto.utils.SecurityUtil;
import com.echo.echosql.common.proto.utils.SeqIDUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
/**
 * EchoSql===》MySql服务端
 * 4              capability flags, CLIENT_PROTOCOL_41 always set
 * 4              max-packet size
 * 1              character set
 * string[23]     reserved (all [0])
 * string[NUL]    username
 *   if capabilities & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA {
 * lenenc-int     length of auth-response
 * string[n]      auth-response
 *   } else if capabilities & CLIENT_SECURE_CONNECTION {
 * 1              length of auth-response
 * string[n]      auth-response
 *   } else {
 * string[NUL]    auth-response
 *   }
 *   if capabilities & CLIENT_CONNECT_WITH_DB {
 * string[NUL]    database
 *   }
 *   if capabilities & CLIENT_PLUGIN_AUTH {
 * string[NUL]    auth plugin name
 *   }
 *   if capabilities & CLIENT_CONNECT_ATTRS {
 * lenenc-int     length of all key-values
 * lenenc-str     key
 * lenenc-str     value
 *    if-more data in 'length of all key-values', more keys and value pairs
 *   }
 *
 * @see <a https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake>mysql</a>
 */
public class HandshakeResponse41Packet extends MySqlPacket {

    private long capabilityFlags;  //权能标识

    private long maxPacketSize;  //最大包的大小

    private byte characterSet;  //编码设置

    private final static byte[] extra = new byte[23];     //默认的天充值

    private byte[] userName;   //用户名

    private byte authResponseLength;  //认证回应长度

    private long authResponseLength2;//对应变长长度的认证数据的长度

    private byte[] authResponse;  //认证回应

    private  byte[] dataBase;   //数据库

    private byte[] authPluginName;   //支持认证插件

    //属性属性设置
    private long  kayValueLength;

    private byte[] key;

    private byte[] value;

    private String  remoteIpAddress;   //记录远程地址

    static {
        for(int i =0;i < 23;i++)
        {
            extra[i] = FILTER;
        }
    }
    /**
     * 初始化包信息
     * @param hsV10
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public void initPacketInfo(HandShakeV10Packet hsV10) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        //this.capabilityFlags = hsV10.getServerCapability();
        this.capabilityFlags = getCapabilities();
        this.maxPacketSize = 1 << 31 -1;
        this.characterSet = hsV10.getCharacterSet();
        this.userName = "root".getBytes();  //设置用户名

        if(hsV10.getAuthPluginDataPart2() == null)
        {
            this.authResponse = SecurityUtil.scramble411_2("123".getBytes("UTF-8"),hsV10.getAuthPluginDataPart1());
        }else {
            final byte[] auth1 = hsV10.getAuthPluginDataPart1();
            final byte[] auth2 = hsV10.getAuthPluginDataPart2();

            byte[] seed = new byte[auth1.length + auth2.length - 1];
            System.arraycopy(auth1,0,seed,0,auth1.length);
            System.arraycopy(auth2,0,seed,auth1.length,auth2.length - 1);

            byte[] authResponse = SecurityUtil.scramble411_2("123".getBytes("UTF-8"),seed);
            this.authResponseLength = (byte)authResponse.length;
            this.authResponse = authResponse;
        }

        //如果设置的认证模式中带有数据库
        if((capabilityFlags & Capabilities.CLIENT_CONNECT_WITH_DB) != 0)
        {
           this.dataBase = "test".getBytes("UTF-8");
        }
        //认证插件
        if((capabilityFlags & Capabilities.CLIENT_PLUGIN_AUTH) != 0)
        {
            this.authPluginName = hsV10.getAuthPluginName();
        }
    }
    /**
     * 写入构造完成的包
     * @return
     */
    public void write(ByteBuf bf)
    {
        this.packetLength = calcPacketSize();
        //this.packetId = 0;
        //this.packetId = (byte)SeqIDUtil.getSeq(remoteIpAddress); //目前设置为1
        this.packetId = 1;
        //数据包头部写入4字节
        BufferUtil.writeUB3(bf,packetLength);
        BufferUtil.writeUB(bf,packetId);
        //实际数据包
        BufferUtil.writeUB4(bf,capabilityFlags);
        BufferUtil.writeUB4(bf,maxPacketSize);
        BufferUtil.writeUB(bf,characterSet);
        BufferUtil.writeBytes(bf,extra);
        if(userName != null)
        {
            BufferUtil.writeWithNull(bf,userName);
        }else{
            BufferUtil.writeUB(bf,(byte)0);
        }
        if((this.capabilityFlags & Capabilities.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0)
        {
            BufferUtil.writeWithVarLength(bf,authResponse);
            //BufferUtil.writeUB(bf,(byte)0);
        }else if((this.capabilityFlags & Capabilities.CLIENT_SECURE_CONNECTION)!= 0)
        {
            BufferUtil.writeUB(bf,(byte)this.authResponse.length);
            BufferUtil.writeBytes(bf,this.authResponse);
            //BufferUtil.writeByte(bf,(byte)0);
        }else
        {
            BufferUtil.writeWithNull(bf,this.authResponse);
        }
        if((this.capabilityFlags & Capabilities.CLIENT_CONNECT_WITH_DB) != 0)
        {
            BufferUtil.writeWithNull(bf,dataBase);

        }else {
            BufferUtil.writeUB(bf,(byte)0);
        }
        if((this.capabilityFlags & Capabilities.CLIENT_PLUGIN_AUTH) != 0)
        {
            BufferUtil.writeWithNull(bf,authPluginName);
        }else {
            BufferUtil.writeUB(bf,(byte)0);
        }
    }
    /**
     * 读取客服端发送过来认证数据包
     */
    public void read(BinaryPacket bin)
    {
        //读取数据包头
        this.packetLength = bin.packetLength;
        this.packetId = bin.packetId;
        //数据数据包
        MySqlMessage msg = new MySqlMessage(bin.data);
        this.capabilityFlags = msg.readUB4();
        this.maxPacketSize = msg.readUB4();
        this.characterSet = msg.readUB();
        msg.skip(23);
        this.userName = msg.readBytesEndWidthNull();   //用户名的读取
        if((this.capabilityFlags & Capabilities.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0)
        {
            //this.authResponseLength2 = msg.readVarLength();
            this.authResponse = msg.readBytesWithVarLength();
        }else if((this.capabilityFlags & Capabilities.CLIENT_SECURE_CONNECTION) != 0)
        {
            this.authResponseLength = msg.readUB();
            this.authResponse = msg.readBytesWithFixLength(this.authResponseLength);
        }else {
            this.authResponse = msg.readBytesEndWidthNull();
        }
        if((this.capabilityFlags & Capabilities.CLIENT_CONNECT_WITH_DB) != 0)
        {
            this.dataBase = msg.readBytesEndWidthNull();
        }
        if((this.capabilityFlags & Capabilities.CLIENT_PLUGIN_AUTH) != 0)
        {
            this.authPluginName= msg.readBytesEndWidthNull();
        }
        //后期处理属性
    }

    @Override
    protected String getPacketInfo() {
        return "Mysql HandshakeResponse41 Packet ";
    }

    @Override
    public int calcPacketSize() {
        int i = 32;  //4 + 4 + 1 + 23
        i += (this.userName == null) ? 1 : this.userName.length + 1;
 //       i += this.authResponse.length + 1;
        if((this.capabilityFlags & Capabilities.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0)
        {
            i += BufferUtil.getLength(this.authResponse);
            //i += 1;
        }else if((this.capabilityFlags & Capabilities.CLIENT_SECURE_CONNECTION) != 0)
        {
            i += 1 + this.authResponse.length ;
        }else {
            i += this.authResponse.length + 1;
        }

        if((this.capabilityFlags & Capabilities.CLIENT_CONNECT_WITH_DB) != 0)
        {
            i += this.dataBase.length + 1;
        }else
        {
            i += 1;
        }
        if((this.capabilityFlags & Capabilities.CLIENT_PLUGIN_AUTH) != 0)
        {
            i += this.authPluginName.length + 1;
        }else
        {
            i += 1;
        }
        return i;
    }

    /**
     * 获取当前数据包发送的远程地址
     * @param ctx
     * @return
     */
    public String getRemoteAddress(ChannelHandlerContext ctx)
    {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        return inetSocketAddress.getAddress().getHostAddress();
    }
    /**
     * 定义与服务器通信的方式
     * @return
     */
    public static final int getCapabilities()
    {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        flag |= Capabilities.CLIENT_ODBC;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        //flag |= Capabilities.CLIENT_PLUGIN_AUTH;  //为了版本的兼容  取消auth_plugin_auth
        return flag;
    }
    /**
     * 返回包实例
     * @param
     * @return
     */
    public static final HandshakeResponse41Packet newInstance(ChannelHandlerContext ctx)
    {
        HandshakeResponse41Packet handshakeResponse41Packet = new HandshakeResponse41Packet();
        handshakeResponse41Packet.remoteIpAddress =  handshakeResponse41Packet.getRemoteAddress(ctx);
        return handshakeResponse41Packet;
    }

    public long getCapabilityFlags() {
        return capabilityFlags;
    }

    public void setCapabilityFlags(long capabilityFlags) {
        this.capabilityFlags = capabilityFlags;
    }

    public long getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(long maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    public byte getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(byte characterSet) {
        this.characterSet = characterSet;
    }

    public static byte[] getExtra() {
        return extra;
    }

    public byte[] getUserName() {
        return userName;
    }

    public void setUserName(byte[] userName) {
        this.userName = userName;
    }

    public byte getAuthResponseLength() {
        return authResponseLength;
    }

    public void setAuthResponseLength(byte authResponseLength) {
        this.authResponseLength = authResponseLength;
    }

    public byte[] getAuthResponse() {
        return authResponse;
    }

    public void setAuthResponse(byte[] authResponse) {
        this.authResponse = authResponse;
    }

    public byte[] getDataBase() {
        return dataBase;
    }

    public void setDataBase(byte[] dataBase) {
        this.dataBase = dataBase;
    }

    public byte[] getAuthPluginName() {
        return authPluginName;
    }

    public void setAuthPluginName(byte[] authPluginName) {
        this.authPluginName = authPluginName;
    }

    public long getKayValueLength() {
        return kayValueLength;
    }

    public void setKayValueLength(long kayValueLength) {
        this.kayValueLength = kayValueLength;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "HandshakeResponse41Packet{" +
                "capabilityFlags=" + capabilityFlags +
                ", maxPacketSize=" + maxPacketSize +
                ", characterSet=" + characterSet +
                ", userName=" + Arrays.toString(userName) +
                ", authResponseLength=" + authResponseLength +
                ", authResponse=" + Arrays.toString(authResponse) +
                ", dataBase=" + Arrays.toString(dataBase) +
                ", authPluginName=" + Arrays.toString(authPluginName) +
                ", kayValueLength=" + kayValueLength +
                ", key=" + Arrays.toString(key) +
                ", value=" + Arrays.toString(value) +
                '}';
    }
}
