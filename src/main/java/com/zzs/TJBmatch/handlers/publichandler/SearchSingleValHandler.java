package com.zzs.TJBmatch.handlers.publichandler;


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

@Component
public class SearchSingleValHandler {

    private DatabaseClient dbCLient;
    public SearchSingleValHandler(@Qualifier("DBClient") DatabaseClient client){
        this.dbCLient = client;
    }

    //查询赛事活动的时间范围
    public Mono<ServerResponse> searchMatchTime(ServerRequest request){
        String sql = "SELECT CONCAT_WS(',',DATE_FORMAT(plantime,'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(closetime,'%Y-%m-%d %H:%i:%s')) AS matchinfotime FROM matchinfo WHERE matid = :matid";
        Map<String,String> params = request.queryParams().toSingleValueMap();
        return ZZSR2DBCService.getSingleValue(dbCLient,sql,new HashMap<String,Object>(params))
                .flatMap(result-> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(result));
    }
    public Mono<ServerResponse> selAssignMbtid(ServerRequest request){
        String sql = "SELECT  CAST(IFNULL(COUNT(1),0) AS CHAR(3)) AS count FROM  matchcollegecourse  " +
                "INNER JOIN matchsysperson ON matchcollegecourse.uid = matchsysperson.uid AND matchcollegecourse.matid = matchsysperson.matid  " +
                "WHERE matchcollegecourse.matid = :matid AND matchsysperson.pid = :pid";
        Map<String,String> params = request.queryParams().toSingleValueMap();
        return ZZSR2DBCService.getSingleValue(dbCLient,sql,new HashMap<String,Object>(params))
                .flatMap(result-> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(result));
    }




    //查询赛事活动的比赛时间范围
    public Mono<ServerResponse> searchTimeContest(ServerRequest request){
        String sql = "SELECT CONCAT_WS(',',DATE_FORMAT(starttime,'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(endtime,'%Y-%m-%d %H:%i:%s')) AS matchinfotime FROM matchinfo WHERE matid = :matid";
        Map<String,String> params = request.queryParams().toSingleValueMap();
        return ZZSR2DBCService.getSingleValue(dbCLient,sql,new HashMap<String,Object>(params))
                .flatMap(result-> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(result));
    }

    // 查询打开的界面
    public Mono<ServerResponse> searchPage(ServerRequest request){
        String sqlText = "SELECT funcid, funcsubid, functitle, funcsubtitle, funcpage " +
                " FROM sysfunctions WHERE funcid = :funcid " +
                "ORDER BY funcorder";
        return request.formData()
                .flatMap(f -> ZZSR2DBCService.getListMap(dbCLient, sqlText, new HashMap<>(f)))
                .flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON).bodyValue(r));
    }

    //查询当前赛事下是否已存在该人员信息
    public Mono<ServerResponse> selMatchPid(ServerRequest request){
        String sqlText = "SELECT mspid FROM matchsysperson WHERE matid = :matid AND pid = :pid";
        return request.formData().map(MultiValueMap::toSingleValueMap)
                .flatMap(s-> ZZSR2DBCService.getListMap(dbCLient, sqlText,new HashMap<>(s))
                        .flatMap(result -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result)));
    }

  // 查询选手状态
    public Mono<ServerResponse> selPersonStatusName(ServerRequest request) {
        Mono<Map<String, String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        String sqlText = "SELECT mpstatus from matchprojectperson where mptid = :mptid";
        return rDatas.flatMap(f1 -> {
            String mptid = f1.getOrDefault("mptid", "");
            if (StringUtils.isEmpty(mptid)) {
                throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败，请稍后再试！");
            }
            return ZZSR2DBCService.getSingleValue(dbCLient, sqlText, new HashMap<>(f1));
        })
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(getStatus(res)));
    }

    // 查询选手状态
    public Mono<ServerResponse> selPersonStatus(ServerRequest request) {
        Mono<Map<String, String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        String sqlText = "SELECT mpstatus from matchprojectperson where mptid = :mptid";
        return rDatas.flatMap(f1 -> {
            String mptid = f1.getOrDefault("mptid", "");
            if (StringUtils.isEmpty(mptid)) {
                throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败，请稍后再试！");
            }
            return ZZSR2DBCService.getSingleValue(dbCLient, sqlText, new HashMap<>(f1));
        })
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }




    // 获取教学片段个数
    public Mono<ServerResponse> selTeachNum(ServerRequest request) {
        Map<String, String> dataMap = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT CAST(IFNULL(teachnum,0) AS CHAR(3)) AS teachnum from matchinfo where matid = :matid";
        return ZZSR2DBCService.getSingleValue(dbCLient, sqlText, new HashMap<>(dataMap))
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    // --获取答辩时间
    public Mono<ServerResponse> getSessionGroup(ServerRequest request){
        Map<String, String> map = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT mpsid, CONCAT(mpsstime,'至', mpsetime) AS sessionname FROM matchprojectsession WHERE mbtid = :mbtid";
        return GridObjUtil.queryResult(dbCLient, request, "现场校验","查询答辩组信息",sqlText,new HashMap<>(map), "")
                .flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(r));
    }

    //    // 查询是否有赛事
    public Mono<ServerResponse> getUnitNumber(ServerRequest request) {
        String sqlText = "SELECT CAST(COUNT(1) AS CHAR(20)) AS cnt FROM matchinfo";
        return ZZSR2DBCService.getSingleValue(dbCLient, sqlText, new HashMap<>())
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    // --获取答辩时间
    public Mono<ServerResponse> getJudgeSessionGroup(ServerRequest request){
        Map<String, String> map = request.queryParams().toSingleValueMap();
        return request.session().flatMap(f -> {
            String userid = f.getAttribute("UserID");
            map.put("pid", userid);
            String sqlText = "SELECT matchprojectsession.mpsid, CONCAT(matchprojectsession.mpsstime,'至', matchprojectsession.mpsetime) AS sessionname   " +
                    " FROM matchprojectsession INNER JOIN matchprojectworker ON matchprojectsession.mpsid = matchprojectworker.mpsid " +
                    " INNER JOIN matchsysperson ON matchprojectworker.mspid = matchsysperson.mspid  " +
                    " AND matchprojectsession.matid = matchsysperson.matid  " +
                    " AND matchprojectsession.mpsid = matchprojectworker.mpsid WHERE matchprojectsession.mpsid = :mpsid AND matchsysperson.pid = :pid ";
            return GridObjUtil.queryResult(dbCLient, request, "现场校验","查询答辩组信息",sqlText,new HashMap<>(map), "")
                    .flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(r));
        });

    }



    // 获取当前的答辩时间
    public Mono<ServerResponse> getCurSessionGroup(ServerRequest request){
        Map<String, String> map = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT mpsid, TIMESTAMPDIFF(SECOND,NOW(),mpsstime) AS startSecond, " +
                "TIMESTAMPDIFF(SECOND,NOW(),mpsetime) AS endSecond " +
                "FROM matchprojectsession WHERE mbtid = :mbtid AND matid = :matid";
        return ZZSR2DBCService.getListMap(dbCLient, sqlText, new HashMap<>(map))
                .flatMap(f1 -> {
                    if (null == f1 || f1.size() == 0) { // 直接返回 无需选中任意一个
                        return Mono.just("");
                    }
                    long startSecond = Long.parseLong(f1.get(0).getOrDefault("startSecond", "0").toString());
                    long endSecond = Long.parseLong(f1.get(0).getOrDefault("endSecond", "0").toString());
                    if (startSecond > 0) {
                        return Mono.just(f1.get(0).getOrDefault("mpsid", ""));
                    }
                    else if (endSecond >= 0) {
                        return Mono.just(f1.get(0).getOrDefault("mpsid", ""));
                    }
                    for (int i = 1, len = f1.size(); i < len; i++) {
                        startSecond = Long.parseLong(f1.get(i).getOrDefault("startSecond", "0").toString());
                        endSecond = Long.parseLong(f1.get(i).getOrDefault("endSecond", "0").toString());
                        if (startSecond > 0) {
                            return Mono.just(f1.get(i).getOrDefault("mpsid", ""));
                        }
                        else if (endSecond >= 0) {
                            return Mono.just(f1.get(i).getOrDefault("mpsid", ""));
                        }
                    }
                    return Mono.just("");
                })
                .flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(r));
    }



//
    // --获取当前的答辩时间
    public Mono<ServerResponse> getCurJudgeSessionGroup(ServerRequest request){
        Map<String, String> map = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT matchprojectsession.mpsid, TIMESTAMPDIFF(SECOND,NOW(),matchprojectsession.mpsstime) AS startSecond, " +
                " TIMESTAMPDIFF(SECOND,NOW(),matchprojectsession.mpsetime) AS endSecond   " +
                "FROM matchprojectsession INNER JOIN matchprojectworker ON matchprojectsession.mpsid = matchprojectworker.mpsid   " +
                "INNER JOIN matchsysperson ON matchprojectworker.mspid = matchsysperson.mspid   " +
                "AND matchprojectsession.matid = matchsysperson.matid   " +
//                "AND matchprojectsession.mpsid = matchprojectworker.mpsid   " +
                "WHERE matchprojectsession.mpsid = :mpsid AND matchsysperson.pid = :pid";
        return request.session().flatMap(f -> {
                    String userid = f.getAttribute("UserID");
                    map.put("pid", userid);
            return ZZSR2DBCService.getListMap(dbCLient, sqlText, new HashMap<>(map))
                    .flatMap(f1 -> {
                        if (null == f1 || f1.size() == 0) { // 直接返回 无需选中任意一个
                            return Mono.just("");
                        }
                        long startSecond = Long.parseLong(f1.get(0).getOrDefault("startSecond", "0").toString());
                        long endSecond = Long.parseLong(f1.get(0).getOrDefault("endSecond", "0").toString());
                        if (startSecond > 0) {
                            return Mono.just(f1.get(0).getOrDefault("mpsid", ""));
                        }
                        else if (endSecond >= 0) {
                            return Mono.just(f1.get(0).getOrDefault("mpsid", ""));
                        }
                        for (int i = 1, len = f1.size(); i < len; i++) {
                            startSecond = Long.parseLong(f1.get(i).getOrDefault("startSecond", "0").toString());
                            endSecond = Long.parseLong(f1.get(i).getOrDefault("endSecond", "0").toString());
                            if (startSecond > 0) {
                                return Mono.just(f1.get(i).getOrDefault("mpsid", ""));
                            }
                            else if (endSecond >= 0) {
                                return Mono.just(f1.get(i).getOrDefault("mpsid", ""));
                            }
                        }
                        return Mono.just("");
                    })
                    .flatMap(r -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(r));
        });
    }

//    // =====================查询比赛开始、终止和模块的开始、结束的时间==========================
    public Mono<ServerResponse> getModuleSchedule(ServerRequest request){
        Map<String, String> map = request.queryParams().toSingleValueMap();
        String matid = map.getOrDefault("matid","");
        String funcid = map.getOrDefault("funcid","");
        if (StringUtils.isEmpty(matid) || StringUtils.isEmpty(funcid)) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR, "获取参数失败！");
        }
        String sqlText = "SELECT DATE_FORMAT(matchinfo.plantime, '%Y-%m-%d') AS plantime,   " +
                "DATE_FORMAT(matchinfo.closetime, '%Y-%m-%d') AS closetime,   " +
                "DATE_FORMAT(matchinfo.applystime, '%Y-%m-%d') AS applystime,   " +
                "DATE_FORMAT(matchschedule.starttime, '%Y-%m-%d') AS starttime, " +
                "DATE_FORMAT(matchschedule.endtime, '%Y-%m-%d') AS endtime FROM   " +
                "matchinfo LEFT JOIN   " +
                "matchschedule ON matchinfo.matid = matchschedule.matid  " +
                "AND matchschedule.funcid = :funcid  " +
                "WHERE matchinfo.matid = :matid   ";
        return FormObjUtil.queryResult(dbCLient,request,"模块时间","成功",sqlText, new HashMap<>(map),matid)
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    // 查看是否已进行抽签
    public Mono<ServerResponse> selBallot(ServerRequest request) {
        Map<String, String> dataMap = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT CAST(COUNT(mbid) AS CHAR(5)) AS cnt FROM matchballot WHERE matid = :matid AND islock = '1'";
        return ZZSR2DBCService.getSingleValue(dbCLient, sqlText, new HashMap<>(dataMap))
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    // 查询 预赛还是决赛
    public Mono<ServerResponse> selMatchType(ServerRequest request) {
        Map<String, String> dataMap = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT matchinfo.mtype FROM matchinfo WHERE matid = :matid";
        return ZZSR2DBCService.getSingleValue(dbCLient, sqlText, new HashMap<>(dataMap))
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    public Mono<ServerResponse> beginMark(ServerRequest request){
        Map<String,String> sFullParams = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT COUNT(1) AS isbegin FROM matchplayerscore WHERE matchplayerscore.mbtid = :mbtid";
        return GridObjUtil.queryResult(dbCLient,request,"答辩组抽签","是否开始打分",sqlText,new HashMap<String,Object>(sFullParams),sFullParams.getOrDefault("mproid", ""))
                .flatMap(result-> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }



    // 查询 赛事类型 和 打分方式
    public Mono<ServerResponse> selDetailTotalType(ServerRequest request) {
        Map<String, String> dataMap = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT matchinfo.mtype,matchinfo.detailtotal FROM matchinfo WHERE matid = :matid";
        return ZZSR2DBCService.getListMap(dbCLient, sqlText, new HashMap<>(dataMap))
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

   // 查询单位名称
    public Mono<ServerResponse> getUnitName(ServerRequest request) {
        Map<String, String> dataMap = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT uname FROM unitinfo WHERE uid = :uid";
        return ZZSR2DBCService.getSingleValue(dbCLient, sqlText, new HashMap<>(dataMap))
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }
    //根据身份作证查uid
    public Mono<ServerResponse> getUnitUidByPid(ServerRequest request) {
        Mono<WebSession> rSession = request.session();
        Map<String, String> sFullParams = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT CAST(uid AS CHAR(4)) AS uid FROM matchsysperson WHERE pid = :userno AND matid = :matid";
        return rSession.flatMap(f -> {
                    String userID = f.getAttribute("UserID");
                    sFullParams.put("userno", userID);
                    return ZZSR2DBCService.getSingleValue(dbCLient, sqlText, new HashMap<String, Object>(sFullParams))
                            .flatMap(result -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(result));
        });
        }
    // 可添加的直播赛事类别
    public Mono<ServerResponse> getLiveBroadType(ServerRequest request) {
        Map<String, String> dataMap = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT matchbroadtype.mbtid, matchbroadtype.mbtname  " +
                "FROM matchbroadtype LEFT JOIN matchlivecontrol  " +
                "ON matchbroadtype.matid = matchlivecontrol.matid  " +
                "AND matchbroadtype.mbtid = matchlivecontrol.mbtid " +
                "WHERE matchbroadtype.matid = :matid AND matchlivecontrol.mlcid IS NULL";
        return ZZSR2DBCService.getListMap(dbCLient, sqlText, new HashMap<>(dataMap))
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }



    // 查询参赛组别是否已经发布成绩
    public Mono<ServerResponse> getIsPublish(ServerRequest request) {
        Map<String, String> map = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT ispublish FROM matchscorerank WHERE mbtid = :mbtid AND matid=:matid LIMIT 1";
        return ZZSR2DBCService.getListMap(dbCLient, sqlText, new HashMap<>(map))
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }


    //决赛查询即席演讲时间、模拟授课时间、现场答辩时间
    public Mono<ServerResponse> getSessionTime(ServerRequest request) {
        Map<String, String> dataMap = request.queryParams().toSingleValueMap();
        String matid = dataMap.getOrDefault("matid","");
        String sqlText = "SELECT CAST(matchinfo.coursetime AS CHAR(7)) as coursetime,CAST(matchinfo.replytime AS CHAR(7)) as replytime" +
                "  FROM matchinfo WHERE matid = :matid";
        return FormObjUtil.queryResult(dbCLient,request,"模块时间","成功",sqlText, new HashMap<>(dataMap),matid)
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    //查询当前人员是否有指定模块
    public Mono<ServerResponse> selHasModule(ServerRequest request){
        Mono<WebSession> rSession = request.session();
        Map<String, String> sFullParams = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT matchsysuserrolefunc.funcid FROM matchsysuserrolefunc   " +
                "INNER JOIN  matchsysperson ON matchsysuserrolefunc.mspid = matchsysperson.mspid  " +
                "AND matchsysuserrolefunc.matid = matchsysperson.matid " +
                "WHERE matchsysuserrolefunc.matid = :matid AND matchsysuserrolefunc.funcid = :funcid  " +
                "AND matchsysperson.pid = :userno AND matchsysperson.mptype = '4' ";
        return rSession.flatMap(f -> {
            String userID = f.getAttribute("UserID");
            sFullParams.put("userno", userID);
            return ZZSR2DBCService.getSingleValue(dbCLient,sqlText,new HashMap<String,Object>(sFullParams))
                    .flatMap(result-> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(result));
        });
    }

    private String getStatus(String mpstatus) {
        String result = "";
        // -2-已退回；-1-材料审核未通过；0-未提交；1-信息已提交；2-报名审核通过；3-材料已提交；4-材料审核通过；
        // 5-已签到；6-现场比赛中；7-比赛完成；
        switch (mpstatus) {
            case "-2":
                result = "已退回";
                break;
            case "-1":
                result = "材料审核未通过";
                break;
            case "0":
                result = "未提交";
                break;
            case "1":
                result = "信息已提交";
                break;
            case "2":
                result = "报名审核通过";
                break;
            case "3":
                result = "材料已提交";
                break;
            case "4":
                result = "材料审核通过";
                break;
            case "5":
                result = "已签到";
                break;
            case "6":
                result = "现场比赛中";
                break;
            case "7":
                result = "比赛完成";
                break;

        }
        return result;
    }

}
