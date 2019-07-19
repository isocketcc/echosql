package com.echo.echosql.common.proto.utils;

public interface Capabilities {
    /**
     * mysql 权能标识
     * @see https://dev.mysql.com/doc/internals/en/capability-flags.html
     */

    /**Use the improved version of Old Password Authentication. Assumed to be set since 4.1.1. */
    public  static final int  CLIENT_LONG_PASSWORD = 0x00000001;

    /**Send found rows instead of affected rows in EOF_Packet.*/
    public  static final int  CLIENT_FOUND_ROWS = 0x00000002;

    /** Longer flags in Protocol::ColumnDefinition320.
     *
     * Server
     *
     *     Supports longer flags.
     * Client
     *
     *     Expects longer flags.
     * */
    public  static final int  CLIENT_LONG_FLAG = 0x00000004;

    /**Database (schema) name can be specified on connect in Handshake Response Packet.
     *
     * Server
     *
     *     Supports schema-name in Handshake Response Packet.
     * Client
     *
     *     Handshake Response Packet contains a schema-name.
     * */
    public  static final int  CLIENT_CONNECT_WITH_DB = 0x00000008;

    /**
     *Server
     *
     *     Do not permit database.table.column.
     * */
    public  static final int  CLIENT_NO_SCHEMA = 0x00000010;

    /**
     * Compression protocol supported.
     *Server
     *
     *     Supports compression.
     * Client
     *
     *     Switches to Compression compressed protocol after successful authentication.
     */
    public  static final int  CLIENT_COMPRESS = 0x00000020;

    /**
     *  Special handling of ODBC behavior. No special behavior since 3.22.
     */
    public  static final int  CLIENT_ODBC = 0x00000040;

    /**
     *Can use LOAD DATA LOCAL.
     * Server
     *
     *     Enables the LOCAL INFILE request of LOAD DATA|XML.
     * Client
     *
     *     Will handle LOCAL INFILE request.
     */
    public  static final int  CLIENT_LOCAL_FILES = 0x00000080;

    /**
     * Server
     *
     *     Parser can ignore spaces before '('.
     * Client
     *
     *     Let the parser ignore spaces before '('.
     */
    public  static final int  CLIENT_IGNORE_SPACE = 0x00000100;

    /**
     *  this value was CLIENT_CHANGE_USER in 3.22, unused in 4.0
     * Server
     *
     *     Supports the 4.1 protocol.
     * Client
     *
     *     Uses the 4.1 protocol.
     */
    public  static final int  CLIENT_PROTOCOL_41 = 0x00000200;

    /**
     * wait_timeout versus wait_interactive_timeout.
     * Server
     *
     *     Supports interactive and noninteractive clients.
     * Client
     *
     *     Client is interactive.
     */
    public  static final int  CLIENT_INTERACTIVE = 0x00000400;

    /**
     * Server
     *
     *     Supports SSL.
     * Client
     *
     *     Switch to SSL after sending the capability-flags.
     */
    public  static final int  CLIENT_SSL = 0x00000800;

    /**
     * Client
     *
     *     Do not issue SIGPIPE if network failures occur (libmysqlclient only).
     */
    public  static final int  CLIENT_IGNORE_SIGPIPE = 0x00001000;

    /**
     *  This flag is optional in 3.23, but always set by the server since 4.0.
     * Server
     *
     *     Can send status flags in EOF_Packet.
     * Client
     *
     *     Expects status flags in EOF_Packet.
     */
    public  static final int  CLIENT_TRANSACTIONS = 0x00002000;

    /**
     * Unused  Was named CLIENT_PROTOCOL_41 in 4.1.0.
     */
    public  static final int  CLIENT_RESERVED = 0x00004000;

    /**
     * Server
     *
     *     Supports Authentication::Native41.
     * Client
     *
     *     Supports Authentication::Native41.
     */
    public  static final int  CLIENT_SECURE_CONNECTION = 0x00008000;

    /**
     *  Was named CLIENT_MULTI_QUERIES in 4.1.0, renamed later.
     * Server
     *
     *     Can handle multiple statements per COM_QUERY and COM_STMT_PREPARE.
     * Client
     *
     *     May send multiple statements per COM_QUERY and COM_STMT_PREPARE.
     * Requires
     *
     *     CLIENT_PROTOCOL_41
     */
    public  static final int  CLIENT_MULTI_STATEMENTS = 0x00010000;

    /**
     * Server
     *
     *     Can send multiple resultsets for COM_QUERY.
     * Client
     *
     *     Can handle multiple resultsets for COM_QUERY.
     * Requires
     *
     *     CLIENT_PROTOCOL_41
     */
    public  static final int  CLIENT_MULTI_RESULTS = 0x00020000;

    /**
     * Server
     *
     *     Can send multiple resultsets for COM_STMT_EXECUTE.
     * Client
     *
     *     Can handle multiple resultsets for COM_STMT_EXECUTE.
     * Requires
     *
     *     CLIENT_PROTOCOL_41
     */
    public  static final int  CLIENT_PS_MULTI_RESULTS = 0x00040000;

    /**
     * Server
     *
     *     Sends extra data in Initial Handshake Packet and supports the pluggable authentication protocol.
     * Client
     *
     *     Supports authentication plugins.
     * Requires
     *
     *     CLIENT_PROTOCOL_41
     */
    public  static final int  CLIENT_PLUGIN_AUTH = 0x00080000;

    /**
     * Server
     *
     *     Permits connection attributes in Protocol::HandshakeResponse41.
     * Client
     *
     *     Sends connection attributes in Protocol::HandshakeResponse41.
     */
    public  static final int  CLIENT_CONNECT_ATTRS = 0x00100000;

    /**
     *  The flag was introduced in 5.6.6, but had the wrong value.
     * Server
     *
     *     Understands length-encoded integer for auth response data in Protocol::HandshakeResponse41.
     * Client
     *
     *     Length of auth response data in Protocol::HandshakeResponse41 is a length-encoded integer.
     */
    public  static final int  CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 0x00200000;

    /**
     * Server
     *
     *     Announces support for expired password extension.
     * Client
     *
     *     Can handle expired passwords.
     */
    public  static final int  CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS = 0x00400000;


    /**
     * Server
     *
     *     Can set SERVER_SESSION_STATE_CHANGED in the Status Flags and send session-state change data after a OK packet.
     * Client
     *
     *     Expects the server to send sesson-state changes after a OK packet.
     */
    public  static final int  CLIENT_SESSION_TRACK = 0x00800000;

    /**
     * Server
     *
     *     Can send OK after a Text Resultset.
     * Client
     *
     *     Expects an OK (instead of EOF) after the resultset rows of a Text Resultset.
     * Background
     *
     *     To support CLIENT_SESSION_TRACK, additional information must be sent after all successful commands. Although the OK packet is extensible, the EOF packet is not due to the overlap of its bytes with the content of the Text Resultset Row.
     */
    public  static final int  CLIENT_DEPRECATE_EOF = 0x01000000;
}
