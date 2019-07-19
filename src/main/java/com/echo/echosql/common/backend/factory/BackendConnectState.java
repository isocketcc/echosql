package com.echo.echosql.common.backend.factory;

/**
 * 后台连接状态
 */
public enum BackendConnectState {
  //初始化是非认证状态
    BACKEND_AUTHED(true),
    BACKEND_NOT_AUTHED(false),
    BACKEND_SEND_AUTH(true);

    private boolean value;

    BackendConnectState(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

}
