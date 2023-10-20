package com.zzs.TJBmatch.utility;

import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
import com.zzs.TJBmatch.handlers.userlogin.RSAKeyPairManage.KeyManager;
import com.zzs.TJBmatch.handlers.userlogin.RSAKeyPairManage.MD5Ser;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class ValidUtil {
    public static void judgeIsValid(String timestamp, String token) {
        if (!StringUtils.hasLength(timestamp) || !StringUtils.hasLength(KeyManager.getToken())) {
            throw new ValidationException(RtnEnum.TIMEOUT, "请求超时或非法请求！");
        }
        if ((System.currentTimeMillis() - Long.parseLong(timestamp)) / 1000 > 120) { // modify 60 to 120
            throw new ValidationException(RtnEnum.TIMEOUT, "请求超时或非法请求！");
        }
        if (!MD5Ser.getMD5(timestamp + "ps").trim().equals(KeyManager.getToken()))
            throw new ValidationException(RtnEnum.TIMEOUT, "请求超时或非法请求！");
        if (!token.equals(KeyManager.getToken())){
            throw new ValidationException(RtnEnum.TIMEOUT, "请求超时或非法请求！");
        }
    }

    public static void judgeHasUser(List<Map<String, Object>> list) {
        if (list.size() == 0)
            throw new ValidationException(RtnEnum.USER_NO_EXIST, "用户不存在或已注销");
    }

    public static void judgeUserIsOnline(String s) {
        if (!StringUtils.hasLength(s))
            throw new ValidationException(RtnEnum.USER_ONLINEE, "该用户目前在线，不能重复登录!");
    }

    public static void judgePwd(String s1, String s2) {
        if (!StringUtils.hasLength(s1) && s2.equals(""))
            throw new ValidationException(RtnEnum.USER_PASSWORD_ERROR, "用户名或密码错误");
        if (!s2.equals("") && !s1.equals(s2))
            throw new ValidationException(RtnEnum.USER_PASSWORD_ERROR, "用户名或密码错误");
    }

    public static void judgeLoginSuccess(int i) {
        if (i < 1)
            throw new ValidationException(RtnEnum.TIMEOUT, "请求超时或非法请求！");
    }

}
