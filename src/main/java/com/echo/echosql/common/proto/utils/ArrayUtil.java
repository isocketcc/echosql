package com.echo.echosql.common.proto.utils;

/**
 * 动态数组能够实现动态扩容
 * 能够实现在任何位置上插入
 * @param <E>
 */
public class ArrayUtil<E> {
    private E[] data;
    private int size;   //长度

    public ArrayUtil(int capacity){
        data = (E[])new Object[capacity];
        size = 0;
    }
    public ArrayUtil()
    {
        this(50);
    }

    /**
     * 返回数组中元素的个数
     * @return int
     */
    public int getSize()
    {
        return size;
    }

    /**
     * 返回数组实际的长度
     * @return int
     */
    public int getLength()
    {
        return data.length;
    }

    public int getCapacity()
    {
        return data.length;  //返回数组的长度
    }

    public boolean isEmpty()
    {
        return size == 0;
    }

    //向所有的元素的最后一个元素添加
    public void addLast(E e)
    {
        add(size,e);
    }
    //向第一个元素添加
    public void addList(E e)
    {
        add(0,e);
    }

    //向数组指定的位置添加元素
    public void add(int index,E e)
    {
        //System.out.println("index:"+index);
        //符合条件扩容
        if(size == data.length) {
            resize(2 * data.length);
        }
        if(index >= data.length)
        {
            resize(2 * data.length);
        }
        if(index < 0)
        {
            //System.out.println("index:"+index);
            throw new IllegalArgumentException("add failed");
        }
        data[index] = e;
        size++;
    }

    /**
     * 插入数据
     * @param e
     */
    public void add(E e)
    {
        //符合条件扩容
        if(size == data.length) {
            resize(2 * data.length);
        }
        data[size] = e;
        size++;
    }
    //扩容函数
    private void resize(int newCapacity)
    {
        E[] newdata = (E[])new Object[newCapacity];
        for(int i = 0;i < data.length;i++)
        {
            newdata[i]  = data[i];
        }
        for(int i = data.length; i< newCapacity;i++)
        {
            newdata[i] = null;
        }
        data = newdata;
    }

    //获取index索引位置
    public E get(int index)
    {
        if(index < 0)
        {
            throw new IllegalArgumentException("get failed");
        }
        return data[index];
    }

    /**
     * 清空容器
     */
    public void clear()
    {
        size = 0;
    }
}