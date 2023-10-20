package com.zzs.TJBmatch.handlers.userlogin;

import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
import com.zzs.TJBmatch.handlers.publichandler.DownLoadAndUpload;
import com.zzs.TJBmatch.services.Utilities;
import com.zzs.TJBmatch.utility.FormObjUtil;
import com.zzs.TJBmatch.utility.GridObjUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MainFrmHandler {

    @Value("${TeachSkillFiles.UserManual}")
    private String userManual;

    @Autowired
    private DownLoadAndUpload downLoadAndUpload;

    private DatabaseClient dbCLient;
    public MainFrmHandler(@Qualifier("DBClient") DatabaseClient client){
        this.dbCLient = client;
    }

    public Mono<ServerResponse> getRights(ServerRequest request) {
        Map<String, String> map = request.queryParams().toSingleValueMap();
        String matid = map.getOrDefault("matid","");
        if (StringUtils.isEmpty(matid)) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"参数错误!");
        }

        return request.session().flatMap(f -> {
            String userRole = f.getAttribute("UserRole");
            if (null == userRole || StringUtils.isEmpty(userRole)) {
                throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败！");
            }
            String sqlText = "";
            if (Integer.parseInt(userRole) == 4) {
                sqlText = "SELECT DISTINCT sysfunctions.funcid,  sysfunctions.functitle,    " +
                        "CASE WHEN sysuserrolefunc.fid IS NULL THEN '0' ELSE '1' END AS mark,   " +
                        "CASE WHEN matchsysperson.mspid IS NULL THEN '0' ELSE '1' END AS wmark,    " +
                        "DATE_FORMAT(matchschedule.starttime, '%Y年%m月%d日') AS starttime,    " +
                        "DATE_FORMAT(matchschedule.endtime, '%Y年%m月%d日') AS endtime,  " +
                        " sysfunctions.fname  " +
                        "FROM sysfunctions LEFT JOIN sysuserrolefunc    " +
                        "ON sysfunctions.funcid = sysuserrolefunc.funcid    " +
                        "AND sysfunctions.catno = sysuserrolefunc.catno " +
                        "AND sysuserrolefunc.roleno = :roleno      " +
                        "LEFT JOIN matchschedule ON sysfunctions.funcid = matchschedule.funcid    " +
                        "AND matchschedule.matid = :matid   " +
                        "LEFT JOIN matchsysuserrolefunc ON sysfunctions.funcid = matchsysuserrolefunc.funcid " +
                        "AND sysfunctions.catno = matchsysuserrolefunc.catno  " +
                        "AND matchsysuserrolefunc.matid = :matid " +
                        "LEFT JOIN matchsysperson ON matchsysuserrolefunc.matid = matchsysperson.matid " +
                        "AND matchsysuserrolefunc.mspid = matchsysperson.mspid  " +
                        "AND matchsysperson.pid = :userno " +
                        "WHERE sysfunctions.catno > 0    " +
                        "ORDER BY sysfunctions.funcid";
                map.put("userno", f.getAttribute("UserID"));
            }
            else {
                sqlText = "SELECT DISTINCT sysfunctions.funcid,  sysfunctions.functitle,    " +
                        "CASE WHEN sysuserrolefunc.fid IS NULL THEN '0' ELSE '1' END AS mark, '1' as wmark,  " +
                        "DATE_FORMAT(matchschedule.starttime, '%Y年%m月%d日') AS starttime,    " +
                        "DATE_FORMAT(matchschedule.endtime, '%Y年%m月%d日') AS endtime," +
                        "sysfunctions.fname  " +
                        "FROM sysfunctions LEFT JOIN sysuserrolefunc   " +
                        "ON sysfunctions.funcid = sysuserrolefunc.funcid   " +
                        "AND sysfunctions.catno = sysuserrolefunc.catno   " +
                        "AND sysuserrolefunc.roleno = :roleno   " +
                        "LEFT JOIN matchschedule ON sysfunctions.funcid = matchschedule.funcid   " +
                        "AND matchschedule.matid = :matid   " +
                        "WHERE sysfunctions.catno > 0   " +
                        "ORDER BY sysfunctions.funcid";
            }
            map.put("roleno", userRole);
            return GridObjUtil.queryResult(dbCLient, request, "主界面", "查询权限信息", sqlText, new HashMap<>(map), map.getOrDefault("matid",""))
               .flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(r));
        });
    }

    public Mono<ServerResponse> getRole(ServerRequest request) {
        Map<String, String> map = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT matchsysperson.matid, RTRIM(matchsysperson.pid) AS userno, " +
                "RTRIM(matchsysperson.pname) AS username,   " +
                "RTRIM(matchsysperson.pwd) AS pwd, RTRIM(matchsysperson.pid) AS pid,   " +
                "matchsysperson.mptype AS userrole,matchpersonrole.mprname AS rolename   " +
                "FROM matchsysperson INNER JOIN matchinfo ON matchinfo.matid = matchsysperson.matid   " +
                "INNER JOIN matchpersonrole ON matchsysperson.mptype = matchpersonrole.mprid  " +
                "WHERE (matchsysperson.mptype > 2 AND matchsysperson.mptype < 10)   " +
                "AND  matchsysperson.pid = :user  " +
                "AND matchinfo.matid = :matid";
        ConcurrentHashMap<String, String> userMap = new ConcurrentHashMap<>();
        Mono<Integer> integerMono = request.session().flatMap(t -> {
            map.put("user", t.getAttribute("UserID"));
            return ZZSR2DBCService.getListMap(dbCLient, sqlText, new HashMap<>(map))
                    .flatMap(f1 -> {
                        if (f1.size() == 0)
                            throw new ValidationException(RtnEnum.USER_NO_EXIST,"获取比赛信息失败！请稍后重试！");
                        userMap.putAll(createUserList(userMap, f1));
                        // 5、设置登录状态
                        String updateSql = "update matchsysperson set onlinemark = '1',lastonline = now() " +
                                "where rtrim(pid) = :db_uid AND matid = :db_matid";
                        return ZZSR2DBCService.insertUpdateDelete(dbCLient, updateSql, new HashMap<>(userMap));
                    });
        });
        return integerMono.zipWith(request.session())
                .flatMap(f -> {
                    if (f.getT1() > 0) {
                        Map<String, Object> sessionAttributes = addSessionData(f.getT2(),userMap);
                        String sStamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        sStamp = sStamp + "==" + sStamp.hashCode();
                        sessionAttributes.put("STAMPHASH", sStamp);
                        f.getT2().setMaxIdleTime(Duration.ofSeconds(86400));
                        return ServerResponse.ok()
                                .header("YMatid", userMap.get("db_matid"))
                                .header("YLoginId", userMap.get("db_uid"))
                                .header("YUserName", Utilities.setEncodevalue(userMap.get("db_user")))
                                .header("YRole", userMap.get("db_role"))
                                .header("YRoleName", Utilities.setEncodevalue(userMap.get("db_rolename")))
                                .header("STAMPHASH", sStamp)
                                .body(BodyInserters.fromValue(FormObjUtil.okResult("主界面","查询",map.getOrDefault("matid",""),"")));
                    }
                    else {
                        throw new ValidationException(RtnEnum.USER_NO_EXIST,"获取比赛信息失败！请稍后重试！");
                    }
                });
    }

    // ====================下载用户使用手册=====================================
    public Mono<ServerResponse> download(ServerRequest request){
        return request.session().flatMap(f -> {
            String role = f.getAttribute("UserRole");
            if (StringUtils.isEmpty(role)) {
                throw new ValidationException(RtnEnum.NO_RIGHT,"抱歉，您无操作权限，请和管理员联系！");
            }
            int roleNo = Integer.parseInt(role);
            StringBuilder sb = new StringBuilder();
            if (roleNo <= 4) {
                sb.append("主办方承办方操作指南.pdf");
            }
            else if (roleNo == 6) {
                sb.append("市州联络员操作指南.pdf");
            }
            else if (roleNo == 7) {
                sb.append("评审专家操作指南.pdf");
            }
            if (sb.length() == 0) {
                throw new ValidationException(RtnEnum.NO_RIGHT,"抱歉，您无操作权限，请和管理员联系！");
            }
            Path sPath = Paths.get(userManual, sb.toString());
            return request.session()
                    .flatMap( s -> downLoadAndUpload.zdownload(Paths.get(String.valueOf(sPath)),"操作指南.pdf"));
        });
    }


    private String buildHtml(JSONArray jsonArray){
        String sHtml = "";
        String sTitle = "开始";
        final String[] sIcon = { "&#xe623;", "&#xe64e;", "&#xe6b5;", "&#xe621;","&#xe641;","&#xe611;", "&#xe606;", "&#xe621;", "&#xe619;", "&#xe604;", "&#xe7a6;", "&#xe630;", "&#xe600;", "&#xe603;", "&#xe6ce;", "&#xe659;", "&#xe69f;", "&#xe7a6;", "&#xe600;" };

        int j = 0;

        for (int i = 0; i < jsonArray.length(); i ++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String functitle = jsonObject.getString("functitle");
            String funcsubtitle = jsonObject.getString("funcsubtitle");
            String funcpage = jsonObject.has("funcpage") ? jsonObject.getString("funcpage") : "";

            if (!sTitle.equals(functitle)){
                sHtml += "</div>";
                sHtml += "</div></div>";
                sHtml += "<div class='panel panel-default dropdown'>";
                sHtml += "<div class='panel-heading nopadding' role='tab' id='heading" + j + "'>" +
                        "<a class='collapsed mBtn' role='button' data-toggle='collapse' data-parent='#menu' href='#collapse" + j + "' aria-expanded='false' aria-controls='collapse" + j + "'>" +
                        "<i class='icon iconfont' title='" + functitle + " '>" + sIcon[j] + "</i>" +
                        "<span class='mTitle'>" + functitle + " </span></a></div>";
                sHtml += "<div id='collapse" + j + "' class='panel-collapse collapse' role='tabpanel' aria-labelledby='heading" + j + "'>" +
                        "<div class='panel-body nopadding'><ul class='nopadding'>";
                sTitle = functitle;
                j++;
            }
            sHtml += "<li><a href='#' id = 'menu" + i + "' class='mBtn1' onclick=\"fillpage(event," + i + ",'" + funcpage + "','')\" >" +
                    "<span>" + funcsubtitle + "</span></a></li>";
        }
        sHtml += "</div>";
        sHtml += "</div>";
        return  sHtml;
    }

    private ConcurrentHashMap<String,String> createUserList(ConcurrentHashMap<String,String> userMap, List<Map<String, Object>> userLists){
        userMap.put("db_matid", String.valueOf(userLists.get(0).getOrDefault("matid", "")));
        userMap.put("db_uid", String.valueOf(userLists.get(0).getOrDefault("userno", "")));
        userMap.put("db_user", String.valueOf(userLists.get(0).getOrDefault("username", "")));
        userMap.put("db_role", String.valueOf(userLists.get(0).getOrDefault("userrole", "")));
        userMap.put("db_rolename", String.valueOf(userLists.get(0).getOrDefault("rolename", "")));
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

}
