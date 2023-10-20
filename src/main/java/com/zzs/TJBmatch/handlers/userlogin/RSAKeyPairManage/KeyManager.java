package com.zzs.TJBmatch.handlers.userlogin.RSAKeyPairManage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyManager {
    private static Logger log = LoggerFactory.getLogger( KeyManager.class );
    //公钥
    private static String public_key;
    //私钥
    private static String private_key;
    //Token 对时间戳的MD5加密
    private static String token;

    public static String getToken() {
//        log.info("KeyManager get token："+token);
        return token;
    }

    public static void setToken(String token) {
        KeyManager.token = token;
//        log.info("KeyManager set token："+token);
    }

    public static String getPublic_key() {
        return public_key;
    }
    public static void setPublic_key(String public_key) {
        KeyManager.public_key = public_key;
    }
    public static String getPrivate_key() {
        return private_key;
    }
    public static void setPrivate_key(String private_key) {
        KeyManager.private_key = private_key;
    }
    
}
