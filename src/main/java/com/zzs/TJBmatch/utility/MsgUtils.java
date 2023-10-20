package com.zzs.TJBmatch.utility;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsgUtils {

    //判断电话号码
    public static boolean isPhoneNumber(String phoneNumber) {
        String regEx = "^13[0-9]{9}|(15[0-35-9]|18[0123456789]|14[56789]|17[0-9]|16[567]|19[0-9])[0-9]{8}$";
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    //生成6位短信验证码
    public static String generateValidCode(String phoneNumber) {
        //判断手机号
        if (!isPhoneNumber(phoneNumber)){
            return "";
        }
        //生成短信验证码
        StringBuilder code = new StringBuilder();
        while (code.length() < 6) {
            Random r = new Random();
            int i = r.nextInt(10);
            if (!code.toString().contains(String.valueOf(i))) {
                code.append(String.valueOf(i));
            }
        }
        return code.toString();
    }

}
