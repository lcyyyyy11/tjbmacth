package com.zzs.TJBmatch.handlers.userAndSystem;


import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
import com.zzs.TJBmatch.handlers.userlogin.RSAKeyPairManage.MD5Ser;
import com.zzs.TJBmatch.utility.FormObjUtil;
import com.zzs.TJBmatch.utility.GridObjUtil;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class UserInfoHandler {
    private DatabaseClient dbClient;
    public UserInfoHandler(@Qualifier("DBClient") DatabaseClient client){
        this.dbClient = client;
    }

    public Mono<ServerResponse> gridSearch(ServerRequest request){
        return request.session().flatMap(s->{
            String sqlText = "select rtrim(sysuserinfo.userno) as userno,rtrim(sysuserinfo.username) as username," +
                    "sysuserinfo.userrole, sysuserrole.rolename,rtrim(sysuserinfo.pid) as pid,rtrim(sysuserinfo.phone) as phone,sysuserinfo.unote," +
                    "DATE_FORMAT(sysuserinfo.lastonline,'%Y-%m-%d %H:%i:%s') as lastonline,sysuserinfo.onlinemark," +
                    "sysuserinfo.ifactive,sysuserinfo.ifactive as activemark,sysuserinfo.pwd from sysuserinfo " +
                    "inner join sysuserrole on sysuserrole.roleno = sysuserinfo.userrole WHERE sysuserrole.roleno <= 2";
           return GridObjUtil.queryResult(dbClient,request,"系统用户","用户信息查询",sqlText,new HashMap<String,Object>(),"0");
        })
          .flatMap(result-> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }

    public Mono<ServerResponse> gridAdd(ServerRequest request){
        Mono<Map<String,String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        Mono<WebSession> rSession = request.session();
        String sqlText = "insert into sysuserinfo(userno,username,userrole,pid,ifactive,unote,phone,pwd) " +
                "values(:userno,:username,:userrole,:pid,:ifactive,:unote,:phone,:pwd)";
        return rSession.zipWith(rDatas)
                .flatMap(f -> {
                    //限定仅级别为1的用户可添加
                    String userrole = f.getT1().getAttribute("UserRole");
                    String pid = f.getT2().get("pid");
                    String phone = f.getT2().get("phone");
                    String concat = pid.substring(pid.length() - 6).concat(phone.substring(phone.length() - 2));
                    String salt = MD5Ser.salt();
                    String md5 = MD5Ser.getMD5(concat + salt);
                    String hashWithSalt = MD5Ser.getHashWithSalt(md5, salt);
                    f.getT2().put("pwd",hashWithSalt);
                    if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) > 2)
                        throw new ValidationException(RtnEnum.NO_RIGHT,"暂无操作权限！");
                    return FormObjUtil.updateResult(dbClient,request,"新加用户","0",sqlText,new HashMap<String, Object>(f.getT2()),"添加用户失败");
                })
                .flatMap(rs-> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(rs));
    }

    public Mono<ServerResponse> gridEdit(ServerRequest request){
        Mono<WebSession> rSession = request.session();
        Mono<Map<String, String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);

        String sqlText = "update sysuserinfo set username = :username, phone = :phone, userrole = :userrole," +
                "ifactive = :ifactive,unote = :unote, pwd = :pwd where userno = :userno";

        return rSession.zipWith(rDatas)
                .flatMap(f->{
                    if (StringUtils.isEmpty(f.getT2().getOrDefault("userno","")))
                        throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败！");
                    //限定仅级别为1的用户可编辑
                    String userrole = f.getT1().getAttribute("UserRole");
                    if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) > 2)
                        throw new ValidationException(RtnEnum.NO_RIGHT,"暂无操作权限！");
                    String pid = f.getT2().get("pid");
                    String phone = f.getT2().get("phone");
                    String concat = pid.substring(pid.length() - 6).concat(phone.substring(phone.length() - 2));
                    String salt = MD5Ser.salt();
                    String md5 = MD5Ser.getMD5(concat + salt);
                    String hashWithSalt = MD5Ser.getHashWithSalt(md5, salt);
                    f.getT2().put("pwd",hashWithSalt);
                    return FormObjUtil.updateResult(dbClient,request,"用户信息编辑",f.getT2().getOrDefault("userno",""),sqlText,new HashMap<String,Object>(f.getT2()),"编辑失败!");
                })
                .flatMap(result-> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }

    public Mono<ServerResponse> gridDelete(ServerRequest request){
        Mono<WebSession> rSession = request.session();
        Mono<Map<String, String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);

        return  rSession.zipWith(rDatas)
                .flatMap(f->{
                    //限定仅级别为1的用户可删除
                    String userrole = f.getT1().getAttribute("UserRole");
                    if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) > 2)
                        throw new ValidationException(RtnEnum.NO_RIGHT,"暂无操作权限！");

                    String usernos = f.getT2().getOrDefault("usernos", "");
                    if (StringUtils.isEmpty(usernos))
                        throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败，请稍后再试！");

                    String s = Arrays.stream(usernos.split(","))
                          .map( UserNo -> "'" +  UserNo+ "'")
                          .collect(Collectors.joining(","));
                    String sqlText = "DELETE FROM sysuserinfo WHERE userno in (" + s +")";
                    return FormObjUtil.updateResult(dbClient,request,"用户信息删除",s,sqlText,new HashMap<String,Object>(f.getT2()),"删除失败！");
              })
              .flatMap(result-> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }

    //查找用户账号
    public Mono<ServerResponse> selUserNo(ServerRequest request){
        Map<String,String> mapParam =  request.queryParams().toSingleValueMap();
        String sqlText = "select userno from sysuserinfo where userno = :userno";
        return ZZSR2DBCService.getSingleValue(dbClient,sqlText,new HashMap<String,Object>( mapParam))
                .flatMap(rs -> ServerResponse.ok()
                .contentType( MediaType.TEXT_PLAIN)
                .bodyValue(rs));
    }

    //查找身份证号
    public Mono<ServerResponse> selPid(ServerRequest request){
              Map<String,String> mapParam =  request.queryParams().toSingleValueMap();
              String sqlText = "select pid from sysuserinfo where pid = :pid";
              return ZZSR2DBCService.getSingleValue(dbClient,sqlText,new HashMap<String,Object>( mapParam))
              .flatMap(rs -> ServerResponse.ok()
                      .contentType( MediaType.TEXT_PLAIN)
                      .bodyValue(rs));
    }

    //批量启用
    public Mono<ServerResponse> setValid(ServerRequest request){
        return common(request,"valid");
    }

    //批量停用
    public Mono<ServerResponse> setInvalid(ServerRequest request){
        return common(request,"invalid");
    }

    //批量离线
    public Mono<ServerResponse> setOffline(ServerRequest request){
        return common(request,"offline");
    }

    //重置密码

    private Mono<ServerResponse> common(ServerRequest request,String flag){
        Mono<WebSession> rSession = request.session();
        Mono<Map<String, String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);

        String mark = "";
        String sqlText = "update sysuserinfo set ";
        switch (flag){
            case "valid":
                mark = "启用";
                sqlText += "ifactive = '1' ";
                break;
            case "invalid":
                mark = "停用";
                sqlText += "ifactive = '0' ";
                break;
            case "offline":
                mark = "离线";
                sqlText += "onlinemark = '0' ";
                break;
        }

        sqlText += " where userno in (";

        String finalMark = mark;
        String finalSqlText = sqlText;
        return  rSession.zipWith(rDatas)
                .flatMap(f->{
                    //限定仅级别为1的用户
                    String userrole = f.getT1().getAttribute("UserRole");
                    if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) > 2)
                        throw new ValidationException(RtnEnum.NO_RIGHT,"暂无操作权限！");

                    String usernos = f.getT2().getOrDefault("usernos", "");
                    if (StringUtils.isEmpty(usernos))
                        throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败，请稍后再试！");

                    usernos = "'" + usernos.replace(",","','") + "'";

                    return FormObjUtil.updateResult(dbClient,request,"批量" + finalMark,userrole, finalSqlText + usernos + ")",new HashMap<String,Object>(f.getT2()), "批量" + finalMark + "失败！");
                })
                .flatMap(result-> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }
}

