package com.echo.echosql.common.fronted.pool;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ConnectPoolFactory implements PooledObjectFactory {

    @Override
    public PooledObject makeObject() throws Exception {
        BackendClient backendClient = new BackendClient();
        return new DefaultPooledObject(backendClient);
    }

    @Override
    public void destroyObject(PooledObject pooledObject) throws Exception {
        BackendClient backendClient = (BackendClient) pooledObject.getObject();
        backendClient = null;  //复合对象的distroy没有这么简单 目前为了测试功能 先这么些
    }

    @Override
    public boolean validateObject(PooledObject pooledObject) {
        return true;
    }

    @Override
    public void activateObject(PooledObject pooledObject) throws Exception {
        //激活对象的时候 重新设置连接的通道上下文
    }

    @Override
    public void passivateObject(PooledObject pooledObject) throws Exception {

    }
}