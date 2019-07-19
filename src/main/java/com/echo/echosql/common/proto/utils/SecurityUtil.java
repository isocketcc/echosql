package com.echo.echosql.common.proto.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *  加密算法 工具类
 *   mysql4.1.x之前的版本的采用的323加密方式
 *   mysql4.1.x之后的版本采用的是411的加密方式
 */
public class SecurityUtil {
    /**
     * mysql4.1.x之后的加密方式
     * @param pass
     * @param seed
     * @return pass3
     * @throws NoSuchAlgorithmException
     */
    public static final byte[] scramble411(byte[] pass,byte[] seed) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] pass1 = md.digest(pass);  //第一次加密计算
        md.reset();                         //对摘要进行重置
        byte[] pass2 = md.digest(pass1);
        md.reset();
        md.update(seed);
        byte[] pass3 = md.digest(pass2);
        for(int i = 0; i < pass3.length; i++)
        {
            pass3[i] = (byte) (pass3[i] ^ pass1[i]);  //异或运算
        }
        return pass3;
    }

    /**
     * mysql4.1.x之前的加密算法
     * @param pass
     * @param seed
     * @return
     */
    public static final String scramble323(String pass,String seed)
    {
        if((pass == null) || (pass.length() == 0))
        {
            return pass;
        }
        byte b;
        double d;
        long[] pw = hash(seed);
        long[] msg = hash(pass);
        long max = 0x3FFFFFFFL;
        long seed1 = (pw[0] ^ msg[0]) % max;
        long seed2 = (pw[1] ^ msg[1]) % max;
        char[] chars = new char[seed.length()];
        for(int i = 0;i < seed.length(); i++)
        {
            seed1 =((seed1 * 3) + seed2) % max;
            seed2 = (seed1 + seed2 + 33) % max;
            d = (double) seed1 / (double)max;
            b = (byte) Math.floor((d*31)+64);
            chars[i] = (char)b;
        }
        seed1 = ((seed1 * 3)+seed2) % max;
        d = (double) seed1 / (double)max;
        b = (byte)Math.floor(d * 31);
        for(int i = 0;i < seed.length();i++)
        {
            chars[i] ^= (char)b;
        }
        return new String(chars);
    }

    /**
     *
     * @param pass
     * @param seed
     * @return toBeXord
     * @throws NoSuchAlgorithmException
     */
    public static byte[] scramble411_2(byte[] pass,byte[]  seed) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] passwordHashStage1 = md.digest(pass);
        md.reset();

        byte[] passwordHashStage2 = md.digest(passwordHashStage1);
        md.reset();;

        byte[] seedAsBytes = seed;
        md.update(seedAsBytes);
        md.update(passwordHashStage2);

        byte[] toBeXord = md.digest();

        int numToXor = toBeXord.length;

        for(int i = 0; i < numToXor; i++)
        {
            toBeXord[i] = (byte)(toBeXord[i] ^ passwordHashStage1[i]);
        }
        return toBeXord;
    }

    /**
     * hash值
     * @param src
     * @return result
     */
    private static long[] hash(String src)
    {
        long nr = 1345345333L;
        long add = 7;
        long nr2 = 0x12345671L;
        long tmp;
        for (int i = 0; i < src.length(); ++i)
        {
            switch (src.charAt(i))
            {
                case ' ':
                case '\t':
                    continue;
                default:
                    tmp = (0xFF & src.charAt(i));
                    nr ^= ((((nr & 63) + add) * tmp)+(nr << 8));
                    nr2 += ((nr2 << 8) ^ nr);
                    add += tmp;
            }
        }
        long[] result = new long[2];
        result[0] = nr & 0x7FFFFFFFL;
        result[1] = nr2 & 0x7FFFFFFFL;
        return result;
    }

}
