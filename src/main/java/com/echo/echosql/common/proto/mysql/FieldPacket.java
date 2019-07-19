package com.echo.echosql.common.proto.mysql;

import com.echo.echosql.common.proto.utils.BufferUtil;
import io.netty.buffer.ByteBuf;

/**
 * MySql服务器===>ECHOSQL
 * lenenc_str     catalog
 * lenenc_str     schema
 * lenenc_str     table
 * lenenc_str     org_table
 * lenenc_str     name
 * lenenc_str     org_name
 * lenenc_int     length of fixed-length fields [0c]
 * 2              character set
 * 4              column length
 * 1              type
 * 2              flags
 * 1              decimals
 * 2              filler [00] [00]
 *   if command was COM_FIELD_LIST {
 * lenenc_int     length of default-values
 * string[$len]   default values
 *   }
 *
 * @see <a https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-Protocol::ColumnDefinition>mysql</a>
 */
public class FieldPacket extends MySqlPacket {

    private static final byte[] FILTER = {0x00,0x00};

    private byte[] catalog = "def".getBytes();   //通常是ASILL的字符串

    private byte[] schema;  //数据库的名称

    private byte[] aliasTable;  //别名

    private byte[] orgTable;   //物理表名

    private byte[] aliasColumnName;  //列别名

    private byte[] orgColumnName;  //物理列名

    private long flags1 = (byte) 0x0C;     //标识接下来的数据的长度

    private int characterSet;   //编码设置

    private long columnLength;    //列的长度

    private byte columnType;    //列的类型

    private int flags2;   //标记

    private byte decimals;  //小数

    private byte[] filter = FILTER;   //填充值

    private byte[] defaults; //默认值

    /**
     * 读取数据报文信息
     * @param bin
     */
    public void read(BinaryPacket bin)
    {
        this.packetLength = bin.packetLength;
        this.packetId = bin.packetId;
//        System.out.println("长度："+this.packetLength);
//        System.out.println("ID："+this.packetId);
//        for(int i =0;i< bin.data.length;i++)
//        {
//            System.out.print(" "+bin.data[i]);
//        }
        MySqlMessage msg = new MySqlMessage(bin.data);
        this.catalog = msg.readBytesWithVarLength();
        this.schema = msg.readBytesWithVarLength();
        this.aliasTable = msg.readBytesWithVarLength();
        this.orgTable = msg.readBytesWithVarLength();
        this.aliasColumnName = msg.readBytesWithVarLength();
        this.orgColumnName=  msg.readBytesWithVarLength();
        //System.out.print("当前获取的列的名称:"+new String(this.orgColumnName));
        msg.skip(1);
        this.characterSet = msg.readUB2();
        this.columnLength = msg.readUB4();
        //System.out.print("获取的列的长度:"+this.columnLength);
        this.columnType = msg.readUB();
        this.flags2 = msg.readUB2();
        this.decimals = msg.readUB();
        msg.skip(2);   //跳过填充值
        if(msg.hasRemaining())
        {
            this.defaults = msg.readBytesWithEnd();
        }
    }
    /**
     * 构造数据包
     * @param bf
     */
    public void write(ByteBuf bf)
    {
        byte nullVar = 0;
        BufferUtil.writeUB3(bf,calcPacketSize());
        BufferUtil.writeUB(bf,packetId);
        BufferUtil.writeWithVarLength(bf,catalog,nullVar);
        BufferUtil.writeWithVarLength(bf,schema);
        BufferUtil.writeWithVarLength(bf,aliasTable,nullVar);
        BufferUtil.writeWithVarLength(bf,orgTable,nullVar);
        BufferUtil.writeWithVarLength(bf,aliasColumnName,nullVar);
        BufferUtil.writeWithVarLength(bf,orgColumnName,nullVar);
        BufferUtil.writeLength(bf,flags1);       //标识位
        BufferUtil.writeUB2(bf,characterSet);
        BufferUtil.writeUB4(bf,columnLength);
        BufferUtil.writeUB(bf,columnType);
        BufferUtil.writeUB2(bf,flags2);
        BufferUtil.writeUB(bf,decimals);
        BufferUtil.writeBytes(bf,FILTER);
        if(defaults != null)
        {
            BufferUtil.writeWithVarLength(bf,defaults);
        }
    }
    @Override
    protected String getPacketInfo() {
        return "MySql FieldPacket";
    }

    @Override
    public int calcPacketSize() {
        int i = 0;
        i += (catalog == null ? 1 : BufferUtil.getLength(catalog));
        i += (schema == null ? 1 : BufferUtil.getLength(schema));
        i += (aliasTable == null ? 1 : BufferUtil.getLength(aliasTable));
        i += (orgTable == null ? 1 : BufferUtil.getLength(orgTable));
        i += (aliasColumnName == null ? 1 : BufferUtil.getLength(aliasColumnName));
        i += (orgColumnName == null ? 1 : BufferUtil.getLength(orgColumnName));
        i += 13;  //1 + 2 + 4 + 1 + 2 + 1 + 2
        if(defaults != null)
        {
            i += BufferUtil.getLength(defaults);
        }
        return i;
    }

    public byte[] getCatalog() {
        return catalog;
    }

    public void setCatalog(byte[] catalog) {
        this.catalog = catalog;
    }

    public byte[] getSchema() {
        return schema;
    }

    public void setSchema(byte[] schema) {
        this.schema = schema;
    }

    public byte[] getAliasTable() {
        return aliasTable;
    }

    public void setAliasTable(byte[] aliasTable) {
        this.aliasTable = aliasTable;
    }

    public byte[] getOrgTable() {
        return orgTable;
    }

    public void setOrgTable(byte[] orgTable) {
        this.orgTable = orgTable;
    }

    public byte[] getAliasColumnName() {
        return aliasColumnName;
    }

    public void setAliasColumnName(byte[] aliasColumnName) {
        this.aliasColumnName = aliasColumnName;
    }

    public byte[] getOrgColumnName() {
        return orgColumnName;
    }

    public void setOrgColumnName(byte[] orgColumnName) {
        this.orgColumnName = orgColumnName;
    }

    public long getFlags1() {
        return flags1;
    }

    public int getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(int characterSet) {
        this.characterSet = characterSet;
    }

    public long getColumnLength() {
        return columnLength;
    }

    public void setColumnLength(long columnLength) {
        this.columnLength = columnLength;
    }

    public byte getColumnType() {
        return columnType;
    }

    public void setColumnType(byte columnType) {
        this.columnType = columnType;
    }

    public byte getDecimals() {
        return decimals;
    }

    public void setDecimals(byte decimals) {
        this.decimals = decimals;
    }

    public byte[] getDefaults() {
        return defaults;
    }

    public void setDefaults(byte[] defaults) {
        this.defaults = defaults;
    }

    public void setFlags1(long flags1) {
        this.flags1 = flags1;
    }

    public int getFlags2() {
        return flags2;
    }

    public void setFlags2(int flags2) {
        this.flags2 = flags2;
    }
}
