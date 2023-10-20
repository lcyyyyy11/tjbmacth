package com.zzs.TJBmatch.handlers.userlogin;

import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.exceptions.ValidationException;
import com.zzs.TJBmatch.handlers.userlogin.RSAKeyPairManage.KeyManager;
import com.zzs.TJBmatch.handlers.userlogin.RSAKeyPairManage.MD5Ser;
import com.zzs.TJBmatch.handlers.userlogin.RSAKeyPairManage.RSAUtil;
import com.zzs.TJBmatch.services.Utilities;
import com.zzs.TJBmatch.utility.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.HttpCookie;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.zzs.TJBmatch.enums.RtnEnum.GENERAL_ERROR;

/**
 * zzs 2019.12
 * 模拟验证数据库登录，并设置session值
 *
 * 以下编写方式是典型的合成几个Mono返回内容的合并处理方式
 * 主要通过flatMap的多重嵌套，
 * 可以合成任意多个返回类型为Mono的值的合并！
 * 其中任意一层的提取值在下层中都可以直接使用！！
 *
 * zzs:关于Mono的深入理解：
 * WebFlux提供异步非阻塞调用与执行，自然是线程组合来完成。但了解这些远远不够。
 * 简单讲，WebFlux是流式操作和响应式流的结合。
 * 具体讲，流式操作是惰性执行的，在终止操作发起之前，整个流中（管道中）的链式操作
 * 是不会执行任何功能的。终止操作触发时，所有链式操作串接起来一气呵成，完成操作！
 *
 * 而响应式流是通过订阅方的背压式请求来唤起提供方的生产（功能运转），非则提供方并不执行！
 *
 * 具体到结合两者的WebFlux,这里主要应用Mono,其机制是：
 * 请求返回Mono的真正含义是：系统会将需要执行的一系列动作串联起来，形成执行链，
 * 但并不会真正执行。直至返回给前端的任务感知到本请求真正要返回客户端时，才链式执行。
 * 因为webflux返回前端是异步非阻塞的，采用类似嗅探机制感知是否网络等资源可以处理该请求返回。
 * 这时候才使整个链路的动作动起来。因为之前通过Mono已经将所有需要处理的任务串接好了，所以整个操作
 * 可以管道式的流线执行完成！！
 *
 * 基于这种机制，这种形成的链可以串接，也就是整个请求的执行环节上的不同Mono可以组成一个
 * 相连的长链。这种相联的长链，如果用响应式流理解，则是上个链可以作为一个processor,
 * 可以作为下个链(Mono)的Provider或Pruducer.
 * 程序编写过程中可以将Mono转换成Processor(toProcessor()),然后再由下一个Mono订阅。
 * 但最推荐的方式是采用flatMap完成不同链式的组装和连接。
 * flatMap具有展开功能，可以将Mono展开连接在一起！
 * 如下列示例代码所示！！
 *
 * 注意：简单或耗资源少的操作是不需要包在Mono中的！！
 *
 */

@Component
public class ULoginVerifyHandler {

    private Logger logger = LoggerFactory.getLogger(ULoginVerifyHandler.class);

    @Autowired
    private Environment environment;

    private DatabaseClient dbCLient;
    public ULoginVerifyHandler(@Qualifier("DBClient") DatabaseClient client){
        this.dbCLient = client;
    }
    private final String mainUrl = "window.document.location = '/MainFrm.html'";

    public Mono<ServerResponse> clearStamp(ServerRequest request){
        Mono<WebSession> rSession = request.session();
        return rSession.flatMap(s -> {
            s.setMaxIdleTime(Duration.ofSeconds(1));
            Map<String, HttpCookie> cookieMap = request.cookies().toSingleValueMap();
            if (cookieMap.containsKey("SESSION")) {
            }
            return Mono.just(FormObjUtil.okResult("主页","查询","", ""));
        }).flatMap(r -> ServerResponse.ok().bodyValue(r));
    }


    // 验证登录系统
    public Mono<ServerResponse> loginVerify(ServerRequest request){
        // 主办方使用这个sql查询
        String sqlText = "select '0' as matid, rtrim(sysuserinfo.userno) as userno,rtrim(sysuserinfo.username) as username," +
                "rtrim(sysuserinfo.pwd) as pwd, rtrim(sysuserinfo.pid) as pid, " +
                " sysuserrole.roleno as userrole," +
                "rtrim(sysuserrole.rolename) as rolename from sysuserinfo " +
                "inner join sysuserrole on sysuserinfo.userrole = sysuserrole.roleno " +
                "where rtrim(sysuserinfo.userno) = :user and sysuserinfo.ifactive = '1' " +
                "and sysuserrole.roleno <= 2" ;
        Mono<WebSession> rSession = request.session();
        Mono<Map<String,String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        ConcurrentHashMap<String,String> userMap = new ConcurrentHashMap<>();

        Mono<List<Map<String, Object>>> userList = rSession.zipWith(rDatas)
                .flatMap(f1 -> {
                    f1.getT1().setMaxIdleTime(Duration.ofSeconds(1));//清除session
                    // 1、判断请求是否超时
                    String timestamp = f1.getT2().getOrDefault("TimeStamp", "");
                    String token = f1.getT2().getOrDefault("token","");
                    ValidUtil.judgeIsValid(timestamp ,token);

                    String sUser = f1.getT2().getOrDefault("user", "");
                    String sPwd = f1.getT2().getOrDefault("passWord", "");

                    userMap.put("uuser", sUser);
                    userMap.put("usPwd", sPwd);

                    return ZZSR2DBCService.getListMap(dbCLient, sqlText, new HashMap<>(f1.getT2()));
                });

        Mono<Integer> login = userList.zipWith(rDatas)
                .flatMap(f2 -> {
                    List<Map<String, Object>> userLists = f2.getT1();
                    // 2、用户是否存在
                    if (userLists.size() == 0) { // 比赛用户表
                        String matchUserSql = "SELECT matchsysperson.matid, RTRIM(matchsysperson.pid) AS userno, " +
                                "RTRIM(matchsysperson.pname) AS username,   " +
                                "RTRIM(matchsysperson.pwd) AS pwd,RTRIM(matchsysperson.pid) AS pid,   " +
                                "matchsysperson.mptype AS userrole,matchpersonrole.mprname AS rolename   " +
                                "FROM matchsysperson INNER JOIN matchinfo ON matchinfo.matid = matchsysperson.matid   " +
                                "INNER JOIN matchpersonrole ON matchsysperson.mptype = matchpersonrole.mprid  " +
                                "WHERE (matchsysperson.mptype > 2 AND matchsysperson.mptype < 10)   " +
                                "AND  matchsysperson.pid = :user  " +
                                "AND ((matchinfo.plantime >= NOW()) OR (matchinfo.closetime >= NOW() AND matchinfo.plantime <= NOW()))  " +
                                "ORDER BY matchinfo.ifactive DESC, matchinfo.plantime ";
                        return ZZSR2DBCService.getListMap(dbCLient, matchUserSql, new HashMap<>(f2.getT2()))
                                .flatMap(f3 -> {

                                    ValidUtil.judgeHasUser(f3);
                                    userMap.putAll(createUserList_bak(userMap,f3));
                                    String pidSql ="select pid, IFNULL(onlineMark,0) AS onlineMark, " +
                                            "IFNULL(TIMESTAMPDIFF(SECOND, lastonline, NOW()), 0) AS sec  " +
                                            " from matchsysperson where pid = :db_uid " +
                                            "AND matid = :db_matid ";
                                    return ZZSR2DBCService.getListMap(dbCLient, pidSql, new HashMap<String, Object>(userMap))
                                            .flatMap(f4 -> {
                                                // 4、未登录进行密码验证
//                                                ValidUtil.judgeUserIsOnline(f4);
                                                String onlineMark = f4.get(0).get("onlineMark").toString();
                                                if (onlineMark.equals("1")) {
                                                    long sec = Long.parseLong(f4.get(0).get("sec").toString());
                                                    if (sec > 0 && sec <= 180) {
                                                        throw new ValidationException(GENERAL_ERROR, sec + "");
                                                    }
                                                }
                                                // 前台传过来的加密密码解密
                                                String decryptPwdStr = decrypt(userMap.get("usPwd"));
                                                ValidUtil.judgePwd(decryptPwdStr,"");

                                                // 获取数据库该用户的pwd中隐藏的salt，并将解密后的密码与salt进行MD5计算，得到的值如果和数据库中pwd相同，则用户名和密码正确。
                                                String salt = MD5Ser.getSaltFromHash(userMap.get("db_pwd"));
                                                String pwdSaltResult = MD5Ser.getMD5(decryptPwdStr + salt);
                                                String sqlHashResult = MD5Ser.getHashWithSalt(pwdSaltResult, salt);

                                                ValidUtil.judgePwd(sqlHashResult,userMap.get("db_pwd"));

                                                // 5、设置登录状态
                                                String onlineSql = "update matchsysperson set onlinemark = '1',lastonline = now() " +
                                                        "where rtrim(pid) = :db_uid AND matid = :db_matid";
                                                return ZZSR2DBCService.insertUpdateDelete(dbCLient, onlineSql, new HashMap<String, Object>(userMap));
                                            });
                                });
                    }
                    else {
                        // 系统用户表
                        userMap.putAll(createUserList_bak(userMap,userLists));
                        String usernoSql =  "select userno, IFNULL(onlineMark,0) AS onlineMark,  " +
                                "IFNULL(TIMESTAMPDIFF(SECOND, lastonline, NOW()), 0) AS sec " +
                                "from sysuserinfo where userno = :db_uid ";
                        return ZZSR2DBCService.getListMap(dbCLient, usernoSql, new HashMap<String, Object>(userMap))
                                .flatMap(f4 -> {
                                    if (null == f4 || f4.size() == 0) {
                                        throw new ValidationException(GENERAL_ERROR,"获取登录信息失败！请稍后重试！");
                                    }

                                    String onlineMark = f4.get(0).get("onlineMark").toString();
                                    if (onlineMark.equals("1")) {
                                        long sec = Long.parseLong(f4.get(0).get("sec").toString());
                                        if (sec > 0 && sec <= 180) {
                                            throw new ValidationException(GENERAL_ERROR, sec + "");
                                        }
                                    }
                                    // 4、未登录进行密码验证
//                                    ValidUtil.judgeUserIsOnline(f4);
                                    // 前台传过来的加密密码解密
                                    String decryptPwdStr = decrypt(userMap.get("usPwd"));
                                    ValidUtil.judgePwd(decryptPwdStr,"");

                                    // 获取数据库该用户的pwd中隐藏的salt，并将解密后的密码与salt进行MD5计算，得到的值如果和数据库中pwd相同，则用户名和密码正确。
                                    String salt = MD5Ser.getSaltFromHash(userMap.get("db_pwd"));
                                    String pwdSaltResult = MD5Ser.getMD5(decryptPwdStr + salt);
                                    String sqlHashResult = MD5Ser.getHashWithSalt(pwdSaltResult, salt);
                                    ValidUtil.judgePwd(sqlHashResult,userMap.get("db_pwd"));

                                    // 5、设置登录状态
                                    String onlineSql = "update sysuserinfo set onlinemark = '1',lastonline = now() " +
                                            "where rtrim(userno) = :db_uid";
                                    return ZZSR2DBCService.insertUpdateDelete(dbCLient, onlineSql, new HashMap<String, Object>(userMap));
                                });
                    }
                });


        return login.zipWith(rSession)
                .flatMap(f4 -> {
                    //判断是否登录成功
                    ValidUtil.judgeLoginSuccess(f4.getT1());
                    Map<String, Object> sessionAttributes = addSessionData(f4.getT2(),userMap);

                    String sStamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    sStamp = sStamp + "==" + sStamp.hashCode();
                    sessionAttributes.put("STAMPHASH", sStamp);

                    f4.getT2().setMaxIdleTime(Duration.ofSeconds(86400));
                    return ServerResponse.ok()
                            .header("YMatid", userMap.get("db_matid"))
                            .header("YLoginId", userMap.get("db_uid"))
                            .header("YUserName", Utilities.setEncodevalue(userMap.get("db_user")))
                            .header("YRole", userMap.get("db_role"))
                            .header("YRoleName", Utilities.setEncodevalue(userMap.get("db_rolename")))
                            .header("STAMPHASH", sStamp)
                            .body(BodyInserters.fromValue(RtnObjUtil.success(mainUrl)));
                });
    }

    // 生成验证码
    public Mono<ServerResponse> getValidCode(ServerRequest request) {
        Mono<WebSession> rSession = request.session();
        Mono<Map<String,String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        // 生成四位验证码 数字+英文字符
        String checkCode = GenerateCheckCode(4);

        return rSession.zipWith(rDatas)
                .flatMap(r -> {
                    r.getT1().getAttributes().put("VerifyCode", checkCode);
                    BufferedImage image = drawVerificationCode(checkCode);
                    try {
                        return ServerResponse.ok()
                                .contentType(MediaType.IMAGE_JPEG)
                                .body(BodyInserters.fromResource(new ByteArrayResource(toByteArrayAutoClosable(image, "png"))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return ServerResponse.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .bodyValue("验证码生成失败");
                });
    }

    // 验证验证码
    public Mono<ServerResponse> validCode(ServerRequest request){
        Mono<WebSession> rSession = request.session();

        Mono<Map<String,String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);

        return rSession.zipWith(rDatas)
                .flatMap(r -> {
                    r.getT1().setMaxIdleTime(Duration.ofSeconds(300));

                    String imgCode = r.getT2().getOrDefault("ImgCode","");
                    String Code = (String)r.getT1().getAttributes().getOrDefault("VerifyCode", "");
                    Code = Code.toUpperCase();
                    JSONObject jsonObject = new JSONObject();
                    if (imgCode.isEmpty() || !imgCode.toUpperCase().equals(Code)) {
                        jsonObject.put("valid",false);
                    }else{
                        jsonObject.put("valid",true);
                    }
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(jsonObject.toString()));
                });
    }

    // 获得公钥
    public Mono<ServerResponse> getPublicKey(ServerRequest request) {
        String publickey = KeyManager.getPublic_key();
        JSONObject jsonObject = new JSONObject();

        if (publickey != null && !publickey.isEmpty()) {
            jsonObject.put("PublicKey", publickey);
            long current = System.currentTimeMillis();
            String token = MD5Ser.getMD5(String.valueOf(current)+"ps");
            jsonObject.put("TimeStamp", current);
            jsonObject.put("token", token);
            KeyManager.setToken(token);
            return ServerResponse
                    .ok()
                    .body(BodyInserters.fromValue(jsonObject.toString()));
        } else
            return ServerResponse
                    .ok()
                    .body(BodyInserters.fromValue(RtnObjUtil.error(-1, "失败", "获取公钥失败")));
    }

    // 登录或退出系统
    public Mono<ServerResponse> checkUser(ServerRequest request){
        String qPuser = request.queryParam("puser").orElse("");
        String sUser = request.queryParam("UserID").orElse("");
        String roleno = request.queryParam("UserRole").orElse("");
        String matid = request.queryParam("Matid").orElse("");
        return request.session()
                .flatMap((s) -> {
                    String sqlText = "";
                    if (qPuser.equals("ONLINEMARK")) {
                        if (StringUtils.hasLength(roleno)  && Integer.parseInt(roleno) > 2) {
                            sqlText = String.format("update matchsysperson set lastonline = now(), " +
                                    " onlineMark = '1' " +
                                    "Where pid = '%s' AND matid = '%s'",sUser, matid);
                        }
                        else {
                            sqlText = String.format("update sysuserinfo set lastonline = now(),onlineMark = '1' " +
                                    "Where userno = '%s'",sUser);
                        }

                    } else if (qPuser.equals("GOEXIT")) {
                        if (StringUtils.hasLength(roleno) && Integer.parseInt(roleno) > 2) {
                            sqlText = String.format("update matchsysperson set lastonline = now(),onlineMark = '0' " +
                                    "Where pid = '%s' AND matid = '%s'",sUser, matid);
                        }
                        else {
                            sqlText = String.format("update sysuserinfo set lastonline = now(),onlineMark = '0' " +
                                    "Where userno = '%s'",sUser);
                        }
                        s.setMaxIdleTime(Duration.ofSeconds(1));
                    }
                    return ZZSR2DBCService.insertUpdateDelete(dbCLient, sqlText);
                })
                .flatMap(r -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_PLAIN)
                                .body(BodyInserters.fromValue("验证登录态"))
                );
    }



    private String decrypt(String encryptInfo) {
        try {
            // 将Base64编码后的私钥转换成PrivateKey对象
            PrivateKey privateKey = RSAUtil.string2PrivateKey(KeyManager.getPrivate_key());
            // 加密后的内容Base64解码
            byte[] base642Byte = RSAUtil.base642Byte(encryptInfo);
            // 用私钥解密
            byte[] privateDecrypt = RSAUtil.privateDecrypt(base642Byte, privateKey);
            // 解密后的明文
            String decryptInfo = new String(privateDecrypt);
            return decryptInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String GenerateCheckCode(int length) {
        int number;
        char code;
        String checkCode = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            number = random.nextInt();
            if (number % 2 == 0)
                code = (char) ('0' + (char) number % 10);
            else
                code = (char) ('A' + (char) number % 26);

            checkCode += code;
        }
        return checkCode;
    }

    private byte[] toByteArrayAutoClosable(BufferedImage image, String type) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()){
            ImageIO.write(image, type, out);
            return out.toByteArray();
        }
    }

    private BufferedImage drawVerificationCode(String checkCode){
        Random random = new Random();
        BufferedImage image = new BufferedImage(70, 22, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        //验证码图片的背景颜色
        Color c = new Color(255, 255, 255);
        g.setColor(c);
        g.fillRect(0, 0, 70, 22);  //图片边框
        g.setFont(new Font("黑体", Font.BOLD, 20));
        //写字符
        for (int i = 0; i < checkCode.length(); i++) {
            //nextInt(a,b) -> [a,b)
            g.setColor(new Color(0, 154, 97));
            //drawString传入String,int,int  所以char需要+""后转为字符串
            g.drawString(checkCode.charAt(i) + "", 15 * i + 3, 16);
        }
        //画干扰线
        for (int i = 0; i < 8; i++) {
            g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g.drawLine(random.nextInt(70), random.nextInt(20), random.nextInt(70), random.nextInt(20));
        }
        return image;
    }

    private ConcurrentHashMap<String,String> createUserList(ConcurrentHashMap<String,String> userMap,List<Map<String, Object>> userLists){
        userMap.put("db_matid", String.valueOf(userLists.get(0).getOrDefault("matid", "")));
        userMap.put("db_uid", String.valueOf(userLists.get(0).getOrDefault("userno", "")));
        userMap.put("db_user", String.valueOf(userLists.get(0).getOrDefault("username", "")));
        userMap.put("db_role", String.valueOf(userLists.get(0).getOrDefault("userrole", "")));
        userMap.put("db_rolename", String.valueOf(userLists.get(0).getOrDefault("rolename", "")));
        return userMap;
    }

    private ConcurrentHashMap<String,String> createUserList_bak(ConcurrentHashMap<String,String> userMap,List<Map<String, Object>> userLists){
        userMap.put("db_matid", String.valueOf(userLists.get(0).getOrDefault("matid", "")));
        userMap.put("db_uid", String.valueOf(userLists.get(0).getOrDefault("userno", "")));
        userMap.put("db_user", String.valueOf(userLists.get(0).getOrDefault("username", "")));
        userMap.put("db_role", String.valueOf(userLists.get(0).getOrDefault("userrole", "")));
        userMap.put("db_rolename", String.valueOf(userLists.get(0).getOrDefault("rolename", "")));
        userMap.put("db_pwd", String.valueOf(userLists.get(0).getOrDefault("pwd", "")));
        return userMap;
    }

    private Map<String, Object> addSessionData(WebSession ws, ConcurrentHashMap<String,String> userMap){
        Map<String, Object> sessionAttributes = ws.getAttributes();
        sessionAttributes.put("Matid", userMap.get("db_matid"));
        sessionAttributes.put("UserName", userMap.get("db_user"));
        sessionAttributes.put("UserID", userMap.get("db_uid"));
        sessionAttributes.put("UserRole", userMap.get("db_role"));
        return sessionAttributes;
    }


    /**
     * 包装数据发送信息
     * urlSubmit:服务器地址
     * isShortMsg：发送消息方式；1-短信，3-邮件，2-微信
     * isPhone：是否发送手机号码；0-学工号，1-电话号码
     * isSign：是否添加应用签名；0-不添加签名，1-添加签名
     * accepter：电话号码
     * token：令牌
     * title：消息标题
     * content：消息内容
     */
    private String wrap(String urlSubmit, String isShortMsg, String isPhone, String isSign, String accepter,
                               String token, String title, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append(urlSubmit).append("?");
        sb.append("isShortMsg").append("=").append(isShortMsg).append("&");
        sb.append("isPhone").append("=").append(isPhone).append("&");
        sb.append("isSign").append("=").append(isSign).append("&");
        sb.append("accepter").append("=").append(accepter).append("&");
        sb.append("token").append("=").append(token).append("&");
        sb.append("title").append("=").append(title).append("&");
        sb.append("content").append("=").append(content);
        return sb.toString();
    }

}
