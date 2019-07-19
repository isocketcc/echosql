package com.echo.echosql.common.proto.mysql;

import com.echo.echosql.common.proto.utils.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;

/**
 * ECHOSQL===》MySql服务器
 *
 * commond
 *
 * args
 *
 * @see <a https://dev.mysql.com/doc/internals/en/text-protocol.html>mysql</a>
 */
public class CMDPacket extends MySqlPacket {
    private byte cmd;  //命令

    private byte[]  args;  //参数

    //private static byte i = 0;
    private String remoteIpAddress;  //连接的远程地址

//    public CMDPacket(byte commend, byte[] args) {
//        this.cmd = cmd;
//        this.args = args;
//    }
    /**
     * 读取函数
     * @param bin
     */
    public void read(BinaryPacket bin)
    {
        //获取包头
        this.packetLength = bin.packetLength;
        this.packetId = bin.packetId;
        //获取包体
        MySqlMessage msg = new MySqlMessage(bin.data);
        this.cmd = msg.readUB();  //获取命令
        this.args = msg.readBytesWithEnd();

    }

    /**
     * 构造命令数据包
     * @param buf
     */
    public void write(ByteBuf buf)
    {
        packetLength = calcPacketSize();
       //System.out.println("发送的数据包的长度是:"+calcPacketSize());
        packetId = 0;  //所有命令包的编号都是从0开始的
        BufferUtil.writeUB3(buf,packetLength);
        BufferUtil.writeUB(buf,packetId);

        BufferUtil.writeUB(buf,this.cmd);
        if(args != null)
        {
            BufferUtil.writeBytes(buf,args);
        }
    }
    @Override
    protected String getPacketInfo() {
        return "MySql CMDPacket";
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
     * 返回包实例
     * @param
     * @return
     */
    public static final CMDPacket newInstance(ChannelHandlerContext ctx, byte commend, byte[] args)
    {
        CMDPacket cmdPacket = new CMDPacket();

        cmdPacket.remoteIpAddress =  cmdPacket.getRemoteAddress(ctx);
        return cmdPacket;
    }

    /**
     * 返回数据包的长度
     * @return
     */
    @Override
    public int calcPacketSize() {
        int i = 1;
        if(args != null)
        {
            i += args.length;
        }
        return i;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public byte[] getArgs() {
        return args;
    }

    public void setArgs(byte[] args) {
        this.args = args;
    }
}
