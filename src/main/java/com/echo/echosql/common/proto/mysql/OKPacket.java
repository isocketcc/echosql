package com.echo.echosql.common.proto.mysql;

import com.echo.echosql.common.proto.utils.BufferUtil;

import java.util.Arrays;

/***
 * 服务器===>EchoSql
 *
 * int<1>	header	[00] or [fe] the OK packet header
 * int<lenenc>	affected_rows	affected rows
 * int<lenenc>	last_insert_id	last insert-id
 * if capabilities & CLIENT_PROTOCOL_41 {
 *   int<2>	status_flags	Status Flags
 *   int<2>	warnings	number of warnings
 * } elseif capabilities & CLIENT_TRANSACTIONS {
 *   int<2>	status_flags	Status Flags
 * }
 * if capabilities & CLIENT_SESSION_TRACK {
 *   string<lenenc>	info	human readable status information
 *   if status_flags & SERVER_SESSION_STATE_CHANGED {
 *     string<lenenc>	session_state_changes	session state info
 *   }
 * } else {
 *   string<EOF>	info	human readable status information
 * }
 * @see <a>https://dev.mysql.com/doc/internals/en/packet-OK_Packet.html</a>
 */
public class OKPacket extends MySqlPacket {
    public static final byte[] OK = {7,0,0,1,0,0,0,2,0,0,0};

    public static final byte[] AUTH_OK = {7,0,0,2,0,0,0,2,0,0,0};

    public static final byte HEADER = 0x00;

    private byte header = HEADER;

    private long affectRows;

    private long lastInsertId;

    private int statusFlags;

    private int warningsCount;

    private byte[] message;

    public void read(BinaryPacket bin)
    {
        this.packetLength = bin.packetLength;
        this.packetId  =bin.packetId;
        MySqlMessage msg = new MySqlMessage(bin.data);
        this.header = msg.readUB();
        this.affectRows = msg.readVarLength();
        this.lastInsertId = msg.readVarLength();
        this.statusFlags = msg.readUB2();
        this.warningsCount = msg.readUB2();
        if(msg.hasRemaining())
        {
            this.message = msg.readBytesWithVarLength();
        }
    }
    @Override
    protected String getPacketInfo() {
        return "Mysql OKPacket";
    }

    @Override
    public int calcPacketSize() {
        int i = 1;
        i += BufferUtil.getLength(affectRows);
        i += BufferUtil.getLength(lastInsertId);
        i += 4;    //status_flags+warning
        if(message != null)
        {
            i += BufferUtil.getLength(message);
        }
        return i;
    }

    /**
     * 返回OK协议包
     * @return
     */
    public static final OKPacket newInstance()
    {
        OKPacket okPacket = new OKPacket();
        return okPacket;
    }

    public byte getHeader() {
        return header;
    }

    public void setHeader(byte header) {
        this.header = header;
    }

    public long getAffectRows() {
        return affectRows;
    }

    public void setAffectRows(long affectRows) {
        this.affectRows = affectRows;
    }

    public long getLastInsertId() {
        return lastInsertId;
    }

    public void setLastInsertId(long lastInsertId) {
        this.lastInsertId = lastInsertId;
    }

    public int getStatusFlags() {
        return statusFlags;
    }

    public void setStatusFlags(int statusFlags) {
        this.statusFlags = statusFlags;
    }

    public int getWarningsCount() {
        return warningsCount;
    }

    public void setWarningsCount(int warningsCount) {
        this.warningsCount = warningsCount;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OKPacket{" +
                "header=" + header +
                ", affectRows=" + affectRows +
                ", lastInsertId=" + lastInsertId +
                ", statusFlags=" + statusFlags +
                ", warningsCount=" + warningsCount +
                ", message=" + Arrays.toString(message) +
                '}';
    }
}
