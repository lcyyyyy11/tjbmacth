package com.zzs.TJBmatch.handlers.userAndSystem;


import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
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

import java.util.HashMap;
import java.util.Map;

import static com.zzs.TJBmatch.enums.RtnEnum.NO_RIGHT;


@Component
public class CollegeLeaderHandler {
    private DatabaseClient dbCLient;

    public CollegeLeaderHandler(@Qualifier("DBClient") DatabaseClient client) {
        this.dbCLient = client;
    }

    //jqgrid--查找高校信息库
    public Mono<ServerResponse> gridSearch(ServerRequest request){
        String sqlText = "SELECT unitinfo.uid,unitinfo.uname,unitinfo.cid, " +
                "unitinfo.ulocation,unitinfo.upostcode,unitinfo.utype,unitinfo.unote FROM unitinfo  " ;
        Map<String,String> rData = request.queryParams().toSingleValueMap();
        return GridObjUtil.queryResult(dbCLient, request, "中小学信息库", "查询中小学信息", sqlText, new HashMap<>(rData), "")
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    //添加中小学
    public Mono<ServerResponse> gridAdd(ServerRequest request){
        Mono<WebSession> rSession = request.session();
        Mono<Map<String, String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        String sqlText = "INSERT INTO unitinfo( uname, ulocation,  upostcode, cid ,utype, unote) " +
                "VALUES (:uname, :ulocation, :upostcode, :cid , :utype,:unote)";
        return Mono.zip(rSession, rDatas)
                .flatMap(f -> {
                    String userrole = f.getT1().getAttribute("UserRole");
                    if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) > 2)
                        throw new ValidationException(NO_RIGHT, "暂无操作权限！");
                    return FormObjUtil.updateResult(dbCLient,request,"中小学信息库",f.getT2().getOrDefault("pid",""),sqlText,new HashMap<>(f.getT2()),"添加失败");
                }).flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(r));
    }
//
    //编辑中小学
    public Mono<ServerResponse> gridEdit(ServerRequest request){
        Mono<WebSession> rSession = request.session();
        Mono<Map<String, String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        String sqlText = "UPDATE unitinfo SET  uname = :uname, cid = :cid, " +
                "ulocation = :ulocation, utype = :utype , unote =:unote " +
                "WHERE unitinfo.uid = :uid";
        return rSession.zipWith(rDatas)
                .flatMap(f->{
                    String userrole = f.getT1().getAttribute("UserRole");
                    if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) > 2)
                        throw new ValidationException(RtnEnum.NO_RIGHT, "抱歉，您无操作权限，请和主办方联系！");

                    return FormObjUtil.updateResult(dbCLient,request,"中小学信息库","编辑中小学信息",sqlText,new HashMap<String,Object>(f.getT2()),"编辑失败!");
                })
                .flatMap(result-> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }
//
    //删除中小学
public Mono<ServerResponse> gridDelete(ServerRequest request){
    Mono<WebSession> rSession = request.session();
    Mono<Map<String, String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
    String sqlText = "DELETE FROM unitinfo WHERE uid = :uid";
    String selUid = "SELECT CAST(COUNT(mptid) AS CHAR(7)) AS cnt FROM matchprojectperson WHERE uid = :uid";
    Mono<String> selUidMono = rDatas.flatMap(f -> ZZSR2DBCService.getSingleValue(dbCLient, selUid, new HashMap<>(f)));
    return Mono.zip(rSession, rDatas, selUidMono)
            .flatMap(f->{
                String userrole = f.getT1().getAttribute("UserRole");
                if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) > 2)
                    throw new ValidationException(RtnEnum.NO_RIGHT, "抱歉，您无操作权限！");

                if (StringUtils.hasLength(f.getT3()) && !f.getT3().equals("0")) {
                    throw new ValidationException(RtnEnum.GENERAL_ERROR, "当前学校已有参赛选手！不能进行删除！");
                }
                return FormObjUtil.updateResult(dbCLient,request,"中小学信息库","删除中小学",sqlText,new HashMap<String,Object>(f.getT2()),"删除失败!");
            })
            .flatMap(result-> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
}

    //查询当前赛事下是否已存在该人员信息
    public Mono<ServerResponse> selMatchLeaderPid(ServerRequest request){
        return request.formData().map(MultiValueMap::toSingleValueMap)
                .flatMap(s-> {
                    String sqlText = "";
                    if (StringUtils.isEmpty(s.getOrDefault("uid",""))) {
                        sqlText = "SELECT uid FROM unitinfo WHERE upid = :upid AND uid <> :uid ";
                    }
                    else {
                        sqlText = "SELECT uid FROM unitinfo WHERE upid = :upid";
                    }
                    return ZZSR2DBCService.getListMap(dbCLient, sqlText,new HashMap<>(s));
                })
                .flatMap(result -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }

}
