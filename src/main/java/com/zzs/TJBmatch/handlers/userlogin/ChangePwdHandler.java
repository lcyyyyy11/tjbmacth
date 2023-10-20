package com.zzs.TJBmatch.handlers.userlogin;

import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
import com.zzs.TJBmatch.handlers.userlogin.RSAKeyPairManage.MD5Ser;
import com.zzs.TJBmatch.utility.FormObjUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChangePwdHandler {

    private DatabaseClient dbCLient;
    public ChangePwdHandler(@Qualifier("DBClient") DatabaseClient client){
        this.dbCLient = client;
    }

//    public Mono<ServerResponse> ChangePwd(ServerRequest request) {
//        Mono<Map<String,String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
//        Mono<WebSession> rSession = request.session();
//        return rSession.zipWith(rDatas)
//                .flatMap(f -> {
//                    String sqlText = "";
//                    String userrole = f.getT1().getAttribute("UserRole");
//                    String newpwd = f.getT2().get("newpwd");
//                    String salt = MD5Ser.salt();
//                    String md5 = MD5Ser.getMD5(newpwd+salt);
//                    String hashWithSalt = MD5Ser.getHashWithSalt(md5, salt);
//                    f.getT2().put("pwd",hashWithSalt);
//                    if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) < 3)
//                        sqlText = "update sysuserinfo set pwd=:pwd where pid=:pid";
//                    else {
//                        sqlText =" update matchsysperson set pwd=:pwd where pid=:pid";
//                    }
//                    return FormObjUtil.updateResult(dbCLient,request,"修改用户密码","0",sqlText,new HashMap<String, Object>(f.getT2()),"添加用户失败");
//                })
//                .flatMap(rs-> ServerResponse.ok()
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .bodyValue(rs));
//    }

    public Mono<ServerResponse> ChangePwd(ServerRequest request) {
        Mono<Map<String,String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        Mono<WebSession> rSession = request.session();



        return rSession.zipWith(rDatas)
                .flatMap(f -> {
                    String updateSql ;
                    String selPwdSql ;
                    String userrole = f.getT1().getAttribute("UserRole");
                    String userId = f.getT1().getAttribute("UserID");
                    String oldpwd = f.getT2().get("pwd");
                    String newPwd = f.getT2().get("newPwd");
                    String confirmPwd = f.getT2().get("confirmPwd");
                    f.getT2().put("pid",userId);
                    System.out.println(f.getT2().get("pid"));
                    if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) < 3){
                         selPwdSql = "SELECT pwd FROM sysuserinfo WHERE userno = :pid";//查询用户表
                    }else {
                        selPwdSql = "SELECT pwd FROM matchsysperson WHERE pid = :pid";//查询比赛用户表
                    }


                    if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) < 3)
                        updateSql = "update sysuserinfo set pwd=:pwd where userno=:pid";
                    else {
                        updateSql =" update matchsysperson set pwd=:pwd where pid=:pid";
                    }

                    //两次密码是否相同
                    if (!newPwd.equals(confirmPwd)) {
                        throw new ValidationException(RtnEnum.GENERAL_ERROR, "两次密码不一样，请重新输入！");
                    }
                    return ZZSR2DBCService.getJsonArray(dbCLient, selPwdSql, new HashMap<>(f.getT2()))
                            .flatMap(f1 -> {
                                String pwd = f1.getJSONObject(0).get("pwd")+"";

                                // 获取数据库该用户的pwd中隐藏的salt，并将oldPwd密码与salt进行MD5计算，得到的值如果和数据库中pwd相同，则用户名和密码正确。
                                String salt = MD5Ser.getSaltFromHash(pwd);
                                String pwdSaltResult = MD5Ser.getMD5(oldpwd + salt);
                                String sqlHashResult = MD5Ser.getHashWithSalt(pwdSaltResult, salt);

                                if (!sqlHashResult.equals(pwd))
                                    throw new ValidationException(RtnEnum.GENERAL_ERROR, "初始密码错误");

                                //对新密码进行加密
                                String salt2 = MD5Ser.salt();
                                String md5 = MD5Ser.getMD5(newPwd+salt2);
                                String hashWithSalt = MD5Ser.getHashWithSalt(md5, salt2);
                                f.getT2().put("pwd",hashWithSalt);

                                return FormObjUtil.updateResult(dbCLient,request,"修改密码","登录密码修改",updateSql,new HashMap<String, Object>(f.getT2()),"修改密码失败");

                            });
                })
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }
}
