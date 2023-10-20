package com.zzs.TJBmatch.handlers.userAndSystem;


import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
import com.zzs.TJBmatch.utility.FormObjUtil;
import com.zzs.TJBmatch.utility.GridObjUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserRoleSetHandler {

    private DatabaseClient dbClient;

    public UserRoleSetHandler(@Qualifier("DBClient") DatabaseClient client) {
        this.dbClient = client;
    }

    public Mono<ServerResponse> gridSearch(ServerRequest request) {
        String sqlText = "SELECT sysuserrole.roleno,sysuserrole.rolename,sysuserrole.rolemark," +
                "CASE WHEN A.num IS NULL THEN 0 ELSE A.num  END AS num FROM sysuserrole " +
                "LEFT JOIN (SELECT COUNT(*) AS num,userrole FROM sysuserinfo GROUP BY userrole)A " +
                "ON sysuserrole.roleno = A.userrole";

        return GridObjUtil.queryResult(dbClient, request, "用户与系统", "角色查询", sqlText, new HashMap<String, Object>(), "0")
                .flatMap(result -> ServerResponse.ok().
                        contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }

    //第一面板=====角色管理
    public Mono<ServerResponse> gridAdd(ServerRequest request) {
        Mono<WebSession> rSession = request.session();
        Mono<Map<String,String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        String sqlText = "INSERT INTO sysuserrole (roleno,rolename,rolemark) " +
                "VALUES (:roleno,:rolename,:rolemark)";
        return rSession.zipWith(rDatas)
                .flatMap(f -> {
                    String role = f.getT1().getAttribute("UserRole");
                    if(StringUtils.isEmpty(role) ||  Integer.parseInt(role) > 2)
                        throw new ValidationException(RtnEnum.NO_RIGHT,"抱歉，您无权限添加角色！");

                    return FormObjUtil.updateResult(dbClient,request,"添加角色",f.getT2().get("roleno"),sqlText,new HashMap<String,Object>( f.getT2()),"添加失败!");
                })
                .flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(r));
    }

    //第一面板=====角色管理
    public Mono<ServerResponse> gridEdit(ServerRequest request) {
        Mono<WebSession> rSession = request.session();
        Mono<Map<String,String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        String sqlText = "UPDATE sysuserrole SET rolename = :rolename,rolemark = :rolemark WHERE roleno = :roleno";
        return rSession.zipWith(rDatas)
                .flatMap(f -> {
                    String role = f.getT1().getAttribute("UserRole");
                    if(StringUtils.isEmpty(role) ||  Integer.parseInt(role) > 2)
                        throw new ValidationException(RtnEnum.NO_RIGHT,"抱歉，您无权限编辑角色！");

                    return FormObjUtil.updateResult(dbClient,request,"编辑角色",f.getT2().get("roleno"),sqlText,new HashMap<String,Object>( f.getT2()),"编辑失败!");
                })
                .flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(r));
    }

    //第一面板=====角色管理
    public Mono<ServerResponse> gridDelete(ServerRequest request) {
        Mono<WebSession> rSession = request.session();
        Mono<Map<String,String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        String sqlSel = "SELECT CAST(COUNT(*) AS CHAR(3)) AS num FROM sysuserinfo WHERE userrole = :roleno";
        String selMatchSql = "SELECT CAST(COUNT(*) AS CHAR(10)) AS num FROM matchsysperson WHERE mptype =  :roleno";

        String sqlText = "delete from sysuserrole WHERE roleno = :roleno;" +
                "delete from sysuserrolefunc where roleno = :roleno";
        Mono<String> sqlSelMono = rDatas.flatMap(f -> ZZSR2DBCService.getSingleValue(dbClient, sqlSel, new HashMap<>(f)));
        Mono<String> selMatchMono = rDatas.flatMap(f -> ZZSR2DBCService.getSingleValue(dbClient, selMatchSql, new HashMap<>(f)));
        return Mono.zip(rSession, rDatas, sqlSelMono, selMatchMono)
                .flatMap(f -> {
                    String role = f.getT1().getAttribute("UserRole");
                    if (StringUtils.isEmpty(role) ||  Integer.parseInt(role) > 2)
                        throw new ValidationException(RtnEnum.NO_RIGHT, "抱歉，您无权限删除角色！");
                    if ((!f.getT3().isEmpty() && !f.getT3().equals("0")) || !f.getT4().isEmpty() && !f.getT4().equals("0"))
                        throw new ValidationException(RtnEnum.GENERAL_ERROR, "抱歉，当前角色下已有用户，不能删除！");
                    return FormObjUtil.updateResult(dbClient,request,"删除角色",f.getT2().get("roleno"),sqlText,new HashMap<String,Object>(f.getT2()),"删除失败!");
                })
                .flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(r));
    }

    //查找角色编码
    public Mono<ServerResponse> selRole(ServerRequest request) {
        Map<String,String> mapParam =  request.queryParams().toSingleValueMap();
        String sqlText = "select roleno from sysuserrole where roleno = :roleno";

        String oprType = mapParam.getOrDefault("oprType","");
        if (!StringUtils.isEmpty(oprType) && oprType.equals("edit"))
            return Mono.just("")
                    .flatMap( rs -> ServerResponse.ok().contentType( MediaType.TEXT_PLAIN).bodyValue(rs));
        else
            return ZZSR2DBCService.getSingleValue(dbClient,sqlText,new HashMap<String,Object>( mapParam))
                    .flatMap( rs -> ServerResponse.ok().contentType( MediaType.TEXT_PLAIN).bodyValue(rs));
    }



    //====================================第二面板==================================
    public Mono<ServerResponse> grid2Search(ServerRequest request){
        Map<String,String> rData = request.queryParams().toSingleValueMap();
        // 验证传入的参数
        String roleno = rData.getOrDefault("roleno","");
        if(StringUtils.isEmpty(roleno))
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"参数错误!");
        String sqlText = "SELECT DISTINCT sysstage.catno,sysstage.sname AS servicenote,sysfunctions.funcid, " +
                "sysfunctions.functitle,sysuserrolefunc.fid   " +
                "FROM sysfunctions    " +
                "INNER JOIN sysstage ON sysfunctions.catno = sysstage.catno   " +
                "LEFT JOIN sysuserrolefunc ON sysuserrolefunc.roleno = :roleno " +
                " AND sysfunctions.catno = sysuserrolefunc.catno   " +
                "AND sysfunctions.funcid = sysuserrolefunc.funcid    " +
                "WHERE sysstage.catno > 0  " +
                "ORDER BY sysstage.catno ASC,sysfunctions.funcid ASC  ";
        return  GridObjUtil.queryResult(dbClient, request, "系统设置", "角色功能列表", sqlText, new HashMap<String, Object>(rData), "0")
                .flatMap(result -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }

    public Mono<ServerResponse> roleFuncEdit(ServerRequest request) {
        Mono<WebSession> rSession = request.session();
        Mono<Map<String,String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        List<String> lsqlTxt = new ArrayList<String>();
        lsqlTxt.add("Delete From sysuserrolefunc Where roleno = :roleno");
        return rSession.zipWith(rDatas)
                .flatMap(f -> {
                    String role = f.getT1().getAttribute("UserRole");
                    if(StringUtils.isEmpty(role) ||  Integer.parseInt(role) > 2)
                        throw new ValidationException(RtnEnum.NO_RIGHT,"抱歉，您无权限操作！");

                    String jsonString = f.getT2().getOrDefault("myJson","");
                    String roleno = f.getT2().getOrDefault("roleno","");
                    return ZZSR2DBCService.insertUpdateDeleteForBatch(dbClient,strToList(lsqlTxt,jsonString,roleno).toArray(new String[0]), new HashMap<String, Object>(f.getT2()));
                })
                .flatMap(t->{
                    if(t < 1)
                        throw new ValidationException(RtnEnum.UNKNOW_ERROR,"保存失败！");
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(FormObjUtil.okResult("用户与系统", "用户角色保存", "0", "保存成功")));
                });
    }


    private List<String> strToList(List<String> lsqlTxt, String jsonString,String sRoleid){
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json_data = jsonArray.getJSONObject(i);
            String fid = json_data.getString("funcid");
            String catno = json_data.getString("catno");
            lsqlTxt.add("Insert Into sysuserrolefunc(roleno,funcid,catno) " +
                    "Values ('" + sRoleid + "','" + fid + "','"+catno+"')");
        }
        return lsqlTxt;
    }


}
