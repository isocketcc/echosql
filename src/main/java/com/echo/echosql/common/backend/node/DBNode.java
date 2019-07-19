package com.echo.echosql.common.backend.node;

/**
 * 数据库节点
 * 目前是能适配的是MYSQL数据库
 */
public class DBNode {
    private static  final int MYSQLPORT = 3306;   //mysql固定的端口号是3306
    private String host;

    private int port = MYSQLPORT;

    private String userName;

    private String userPass;

    public DBNode() {

    }

    public DBNode(String host, int port, String userName, String userPass) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.userPass = userPass;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }
}
