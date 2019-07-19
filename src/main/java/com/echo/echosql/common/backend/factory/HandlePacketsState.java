package com.echo.echosql.common.backend.factory;

public enum HandlePacketsState {

    STATE_NONE(0),      //解析未开始

    STATE_COLUMN(1),    //列信息解析中

    STATE_COLUMN_END(2),//列信息解析完成

    STATE_ROW(4),   //行数据解析中

    STATE_END(8);        //行数据包解析完成

    //STATE_END(9);     //整个过程结束

    private int value;

    HandlePacketsState(int value)
    {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
