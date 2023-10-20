package com.zzs.TJBmatch.handlers.userlogin.RSAKeyPairManage;

import java.security.MessageDigest;
import java.util.Random;

public class MD5Ser {
    private static char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    /**
     * @return: salt
     * @Descrption: 自定义简单生成盐，是一个随机生成的长度为16的字符串，每一个字符是随机的十六进制字符
     */
    public static String salt() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < sb.capacity(); i++) {
            sb.append(hex[random.nextInt(16)]);
        }
        return sb.toString();
    }
    /**
     * @return: 十六进制字符串
     * @params: [bytes]
     * @Descrption: 将字节数组转换成十六进制字符串
     */
    private static String byte2HexStr(byte[] bytes) {
        int len = bytes.length;
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < len; i++) {
            byte byte0 = bytes[i];
            result.append(hex[byte0 >>> 4 & 0xf]);
            result.append(hex[byte0 & 0xf]);
        }
        return result.toString();
    }

    /**
     *@return: 提取的salt
     *@params: [hash] 3i byte带盐的hash值,带盐方法与MD5WithSalt中相同
     *@Descrption: 从库中查找到的hash值提取出的salt
     */
    public static String getSaltFromHash(String hash){
        StringBuilder sb=new StringBuilder();
        char [] h=hash.toCharArray();
        for(int i=0;i<hash.length();i+=3){
            sb.append(h[i+1]);
        }
        return sb.toString();
    }

    /**
     *@params: [inputStr] 输入明文
     *@Descrption: 不加盐MD5
     */
    public static String getMD5(String inputStr) {
        try {
            //申明使用MD5算法,更改参数为"SHA"就是SHA算法了
            MessageDigest md = MessageDigest.getInstance("MD5");
            //哈希计算,转换输出
            return byte2HexStr(md.digest(inputStr.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }


    /**
     *@params: [inputStr, type,queriedHash] inputStr是输入的明文；type是处理类型，0表示注册存hash值到库时，1表示登录验证时；queriedHash为数据库中存的hash值
     *@Descrption: MD5加盐，盐的获取分两种情况；输入明文加盐；输出密文带盐(将salt存储到hash值中)
     */
    public static String MD5WithSalt(String inputStr, int type,String queriedHash) {
        try {
            //申明使用MD5算法,更改参数为"SHA"就是SHA算法了
            MessageDigest md = MessageDigest.getInstance("MD5");
            String salt = null;
            //注册存hash值到库时，new salt
            if (type == 0) {
                salt = salt();
            } else if (type == 1) {
                //登录验证时，使用从库中查找到的hash值提取出的salt
                //从库中查找到的hash值
                salt=getSaltFromHash(queriedHash);
            }
            //加盐，输入加盐
            String inputWithSalt = inputStr + salt;
            //哈希计算,转换输出
            String hashResult = byte2HexStr(md.digest(inputWithSalt.getBytes()));
            // 将salt存储到hash值中，用于登陆验证密码时使用相同的盐
            char[] cs = new char[48];
            for (int i = 0; i < 48; i += 3) {
                cs[i] = hashResult.charAt(i / 3 * 2);
                //输出带盐，存储盐到hash值中;每两个hash字符中间插入一个盐字符
                cs[i + 1] = salt.charAt(i / 3);
                cs[i + 2] = hashResult.charAt(i / 3 * 2 + 1);
            }
            hashResult = new String(cs);
            return hashResult;
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static String getHashWithSalt(String pwdHash,String salt){
        char[] cs = new char[48];
        for (int i = 0; i < 48; i += 3) {
            cs[i] = pwdHash.charAt(i / 3 * 2);
            //输出带盐，存储盐到hash值中;每两个hash字符中间插入一个盐字符
            cs[i + 1] = salt.charAt(i / 3);
            cs[i + 2] = pwdHash.charAt(i / 3 * 2 + 1);
        }
        return new String(cs);
    }
}
