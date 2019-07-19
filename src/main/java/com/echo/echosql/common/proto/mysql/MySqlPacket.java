package com.echo.echosql.common.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定义包的基本状态和信息
 */
public abstract class MySqlPacket implements Serializable {

    public static final byte FILTER = 0; //天充值 0x00

    public static final int HEAD_LENGTH = 4; //mysql协议头固定长度为4个字节


    /**
     * 客服端命令请求类型
     *
     *  In the command phase, the client sends a command packet with the sequence-id [00]:
     *
     *  @see https://dev.mysql.com/doc/internals/en/text-protocol.html
     */

    /** internal server command **/
    public static final byte COM_SLEEP = 0;

    /**
     * tells the server that the client wants to close the connection
     *
     * response: either a connection close or a OK_Packet
     */
    public static final byte COM_QUIT = 1;

    /**
     * change the default schema of the connection
     *
     * Returns
     *
     *     OK_Packet or ERR_Packet
     */
    public static final byte COM_INIT_DB = 2;

    /**
     *  A COM_QUERY is used to send the server a text-based query that is executed immediately.
     *
     * The server replies to a COM_QUERY packet with a COM_QUERY Response.
     *
     * The length of the query-string is a taken from the packet length - 1.
     *
     * Returns
     *
     *     COM_QUERY_Response
     * */
    public static final byte COM_QUERY = 3;

    /**
     * get the column definitions of a table
     *
     * As of MySQL 5.7.11, COM_FIELD_LIST is deprecated and will be removed in a future version of MySQL. Instead, use mysql_query() to execute a SHOW COLUMNS statement.
     *
     * Returns
     *
     *     COM_FIELD_LIST response
     */
    public static final byte COM_FIELD_LIST = 4;

    /**
     *  create a schema
     *
     *  Returns
     *
     *     OK_Packet or ERR_Packet
     */
    public static final byte COM_CREATE_DB = 5;

    /**
     *  drop a schema
     *
     *  Returns
     *
     *     OK_Packet or ERR_Packet
     */
    public static final byte COM_DROP_DB = 6;

    /**
     * Call REFRESH or FLUSH statements
     *
     * As of MySQL 5.7.11, COM_REFRESH is deprecated and will be removed in a future version of MySQL. Instead, use mysql_query() to execute a FLUSH statement.
     *
     * Returns
     *
     * OK_Packet or ERR_Packet
     */
    public static final byte COM_REFRESH = 7;

    /**
     * shut down the server
     *
     * COM_SHUTDOWN is deprecated as of MySQL 5.7.9 and removed in MySQL 8.0. Instead, use mysql_query() to execute a SHUTDOWN statement.
     */
    public static final byte COM_SHUTDOWN = 8;

    /**
     * get a list of active threads
     *
     * Returns
     *
     *     string.EOF
     */
    public static final byte COM_STATISTICS = 9;

    /**
     * get a list of active threads
     *
     * Returns
     *
     *     a ProtocolText::Resultset or ERR_Packet
     *
     * As of MySQL 5.7.11, COM_PROCESS_INFO is deprecated and will be removed in a future version of MySQL. Instead, use mysql_query() to execute a SHOW PROCESSLIST statement.
     */
    public static final byte COM_PROCESS_INFO = 10;

    /**
     * an internal command in the server
     *
     * Returns
     *
     *     ERR_Packet
     */
    public static final byte COM_CONNECT = 11;

    /**
     * ask the server to terminate a connection
     *
     * Returns
     *
     *     OK_Packet or ERR_Packet
     * As of MySQL 5.7.11, COM_PROCESS_KILL is deprecated and will be removed in a future version of MySQL. Instead, use mysql_query() to execute a KILL statement.
     */
    public static final byte COM_PROCESS_KILL = 12;

    /**
     * dump debug info to stdout
     *
     * Returns
     *
     *     EOF_Packet or ERR_Packet on error
     */
    public static final byte COM_DEBUG = 13;

    /**
     * check if the server is alive
     *
     * Returns
     *
     *     OK_Packet
     */
    public static final byte COM_PING = 14;
    /**
     * an internal command in the server
     *
     * Returns
     *
     *     ERR_Packet
     */
    public static final byte COM_TIME = 15;

    /**
     * an internal command in the server
     *
     * Returns
     *
     * ERR_Packet
     */
    public static final byte COM_DELAYED_INSERT = 16;

    /**
     * change the user of the current connection
     *
     * Returns
     *
     *     Authentication Method Switch Request Packet or ERR_Packet
     */
    public static final byte COM_CHANGE_USER = 17;

    /**
     *request a binlog-stream from the server
     *
     */
    public static final byte COM_BINLOG_DUMP = 18;

    /**
     * dump a table
     *
     * eturns
     *
     *     a table dump or ERR_Packet
     */
    public static final byte COM_TABLE_DUMP = 19;

    /**
     * a internal command in the server
     *
     * Returns
     *
     *     ERR_Packet
     */
    public static final byte COM_CONNECT_OUT = 20;

    /**
     * register a slave at the master
     *
     * Returns
     *
     *     OK_Packet or ERR_Packet
     */
    public static final byte COM_REGISTER_SLAVE = 21;

    /**
     * create a prepared statement
     *
     * Return
     *
     *     COM_STMT_PREPARE_OK on success, ERR_Packet otherwise
     */
    public static final byte COM_STMT_PREPARE = 22;

    /**
     * It sends the values for the placeholders of the prepared statement (if it contained any) in Binary Protocol Value form
     *
     * returns
     *  a COM_STMT_EXECUTE Response.
     */
    public static final byte COM_STMT_EXECUTE = 23;

    /**
     * sends the data for a column. Repeating to send it, appends the data to the parameter.
     *
     *  No response is sent back to the client.
     *
     *   COM_STMT_SEND_LONG_DATA has to be sent before COM_STMT_EXECUTE.
     */
    public static final byte COM_STMT_SEND_LONG_DATA = 24;

    /**
     * deallocates a prepared statement
     *
     *  No response is sent back to the client.
     */
    public static final byte COM_STMT_CLOSE = 25;

    /**
     *  resets the data of a prepared statement which was accumulated with COM_STMT_SEND_LONG_DATA commands and closes the cursor if it was opened with COM_STMT_EXECUTE
     *  The server will send a OK_Packet if the statement could be reset, a ERR_Packet if not.
     */
    public static final byte COM_STMT_RESET = 26;

    /**
     *  Enables capabilities for the current connection to be enabled and disabled:
     *
     *   On success it returns a EOF_Packet otherwise a ERR_Packet.
     */
    public static final byte COM_SET_OPTION = 27;

    /**
     *  Fetch rows from a existing resultset after a COM_STMT_EXECUTE.
     *
     *  Returns
     *
     *     a COM_STMT_FETCH response
     */
    public static final byte COM_STMT_FETCH = 28;

    /**
     * an internal command in the server
     *
     * Returns
     *
     *     ERR_Packet
     */
    public static final byte  COM_DAEMON = 29;

    /**
     *  request the Binlog Network Stream based on a GTID
     *
     *  Returns
     *
     *     a Binlog Network Stream, a ERR_Packet or if BINLOG_DUMP_NON_BLOCK is set a EOF_Packet
     */
    public static final byte COM_BINLOG_DUMP_GTID = 30;

    /**
     * Resets the session state; more lightweight than COM_CHANGE_USER because it does not close and reopen the connection, and does not re-authenticate
     *
     * Returns
     *
     *         a ERR_Packet
     *
     *         a OK_Packet
     */
    public static final byte COM_RESET_CONNECTION = 31;

    /**
     *  查询操作类型
     */
    public static final int QUERY_TYPE_SELECT = 1;
    /**
     * 字段操作状态
     * 0 表示未处理
     * 1 表示已处理
     */
    public static int FIELD_STATE = 0;

    public static AtomicInteger HEAD_STATE = new AtomicInteger(0);

    public static AtomicInteger EOF_STATE = new AtomicInteger(0);

    public static int DB_NODE = 5;

    /***
     *MySql 响应包 类型
     *  For most commands the client sends to the server, the server returns one of these packets in response:
     *
     *     Section 14.1.3.1, “OK_Packet”
     *
     *     Section 14.1.3.2, “ERR_Packet”
     *
     *     Section 14.1.3.3, “EOF_Packet”
     *
     *     An OK packet is sent from the server to the client to signal successful completion of a command. As of MySQL 5.7.5, OK packes are also used to indicate EOF, and EOF packets are deprecated.
     *
     *     These rules distinguish whether the packet represents OK or EOF:
     *
     *     OK: header = 0 and length of packet > 7
     *
     *     EOF: header = 0xfe and length of packet < 9
     */
    public  static final int RESPONSE_OK_PACKAGE = 1; //OK 或者 EOF 响应包

    public  static final int RESPONSE_ERR_PACKAGE = 4; //ERROR响应包


    /**
     * 服务器的状态 status flags
     *
     * @see https://dev.mysql.com/doc/internals/en/status-flags.html
     */
    /**
     *  	a transaction is active
     */
    public static final int SERVER_STATUS_IN_TRANS = 0x0001;

    /**
     * auto-commit is enabled
     */
    public static final int SERVER_STATUS_AUTOCOMMIT = 0x0002;

    public static final int SERVER_MORE_RESULTS_EXISTS = 0x0008;

    public static final int  SERVER_STATUS_NO_GOOD_INDEX_USED = 0x0010;

    public static final int SERVER_STATUS_NO_INDEX_USED = 0x0020;

    /**
     * Used by Binary Protocol Resultset to signal that COM_STMT_FETCH must be used to fetch the row-data.
     */
    public static final int SERVER_STATUS_CURSOR_EXISTS = 0x0040;

    public static final int SERVER_STATUS_LAST_ROW_SENT = 0x0080;

    public static final int SERVER_STATUS_DB_DROPPED = 0x0100;

    public static final int SERVER_STATUS_NO_BACKSLASH_ESCAPES = 0x0200;

    public static final int SERVER_STATUS_METADATA_CHANGED = 0x0400;

    public static final int SERVER_QUERY_WAS_SLOW = 0x0800;

    public static final int SERVER_PS_OUT_PARAMS = 0x1000;

    /**
     * in a read-only transaction
     */
    public static final int SERVER_STATUS_IN_TRANS_READONLY = 0x2000;

    /**
     * connection state information has changed
     */
    public static final int SERVER_SESSION_STATE_CHANGED = 0x4000;

    //数据包的长度
    public  int packetLength;

    public  byte packetId;
    /**
     *  取得数据包的信息
     */
    protected  abstract  String getPacketInfo();

    /**
     * 获取数据包的大小 不包括数据包的头
     * @return
     */
    public abstract int calcPacketSize();

    /**
     * 将数据包通过后端写出
     */
    public void write(ByteBuf bf)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "MySqlPacket{}";
    }
}
