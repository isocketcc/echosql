package com.echo.echosql.common.proto.utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

public class ByteUtil {
    /**
     * 读取三个字节的长度  Mysql传输协议是小端字节序 需要进行相应的转换
     * @param data
     * @return int
     */
      public static int readUB3(ByteBuf data)
      {
          int i = data.readByte() & 0xFF;
          i |= (data.readByte() & 0xFF) << 8;
          i |= (data.readByte() & 0xFF) << 16;
          return i;
      }

      @Test
      public void  test()
      {
           ByteBuf buffer = Unpooled.buffer(10);
           byte[] bytes = {0x01,0x02,0x03,0x04,0x05};
           buffer.writeBytes(bytes);
           System.out.println("获取三个字节测试:"+readUB3(buffer));
      }

}
