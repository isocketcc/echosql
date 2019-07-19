package com.echo.echosql.common.backend.config;

import com.echo.echosql.common.backend.node.DBNode;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * 后台连接配置
 */
public class BackendConfig {
    private static final int BackendInitialSize = 10;  //后台初始化链接

    private static final int BackendMaxSize = 20;  //控制后台最多连接 的数据库的数量

    private static final int BackendInitialWaitTime = 60;  //初始化的等待链接的数量

    private static final List<String> ConnectIPHosts = new ArrayList<>(BackendMaxSize); //后台连接主机

    private static final List<InetSocketAddress> ConnectAddrNodes = new ArrayList<>(BackendMaxSize);  //后台连接主机

    private static final List<DBNode> ConnectDBNodes = new ArrayList<>(BackendMaxSize);  //后台数据库连接数

    private static final int MysqlPort = 3306;

    private static final HashMap<String,DBNode>[] DBNodesMap = new HashMap[BackendMaxSize];  //存储后台连接的节点

    static {
        for(int i = 0; i < BackendMaxSize;i++)
        {
            DBNodesMap[i] = new HashMap<String,DBNode>();
        }

        ConnectIPHosts.add("127.0.0.1");
        ConnectIPHosts.add("127.0.0.1");
        ConnectIPHosts.add("127.0.0.1");
        ConnectIPHosts.add("127.0.0.1");
        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
//        ConnectIPHosts.add("127.0.0.1");
        //ConnectAddrNodes.add(new InetSocketAddress("127.0.0.1",3306));
        //ConnectDBNodes.add(new DBNode("127.0.0.1",3306,"root","123"));
        //ConnectDBNodes.add(new DBNode("127.0.0.1",3306,"root","123"));
    }
    private static String DEFAULT_CHARSET = "utf8";

    public static int getBackendInitialSize() {
        return BackendInitialSize;
    }

    public static int getBackendMaxSize() {
        return BackendMaxSize;
    }

    public static int getBackendInitialWaitTime() {
        return BackendInitialWaitTime;
    }

    public static List<String> getConnectHost() {
        return ConnectIPHosts;
    }

    public static List<InetSocketAddress> getConnectNode() {
        return ConnectAddrNodes;
    }

    public static int getMysqlPort() {
        return MysqlPort;
    }

    public static HashMap<String, DBNode>[] getDBNodesMap() {
        return DBNodesMap;
    }

    public static String getDefaultCharset() {
        return DEFAULT_CHARSET;
    }

    public static void setDefaultCharset(String defaultCharset) {
        DEFAULT_CHARSET = defaultCharset;
    }

    public static List<DBNode> getConnectDBNodes() {
        return ConnectDBNodes;
    }
}
