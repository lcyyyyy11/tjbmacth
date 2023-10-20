package com.zzs.TJBmatch.handlers.publichandler;

import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class GetOptions {

    private DatabaseClient dbCLient;
    public GetOptions(@Qualifier("DBClient") DatabaseClient client){
        this.dbCLient = client;
    }

    public Mono<ServerResponse> doGetOptions(ServerRequest request) {
        return request.session().flatMap((s)->{
            String userrole = s.getAttribute("UserRole");
            String userid = s.getAttribute("UserID");

            String sqlText = "";

            String[] aParaList = new String[3];

            try{
                Object[] snm = request.queryParams().toSingleValueMap().values().toArray();

                for (int i=0;i<snm.length;i++) {
                    aParaList[i] = snm[i].toString();
                }

                if(aParaList[0].contains("&")){
                    aParaList[1] = aParaList[0].substring(aParaList[0].indexOf("=") + 1);
                    aParaList[0] = aParaList[0].substring(0,aParaList[0].indexOf("&"));
                    if (aParaList[1].contains("&")){
                        aParaList[2] = aParaList[1].substring(aParaList[1].indexOf("=") + 1);
                        aParaList[1] = aParaList[1].substring(0,aParaList[1].indexOf("&"));
                    }
                }

                switch (aParaList[0]) {
                    case "UserRole":
                        sqlText = "SELECT roleno as userrole,rolename from sysuserrole";
                        break;
                    case "UserRoleSponsor":
                        sqlText = "SELECT roleno AS userrole,rolename FROM sysuserrole WHERE roleno <= 2";
                        break;
                    //查找所有州市
                    case "CityInfo":
                        sqlText = "SELECT cityinfo.cid AS ccode, RTRIM(cityinfo.cname) AS cname FROM cityinfo";
                        break;
                    //赛事日程当前选择的赛事
                    case "SelectMatchInfo":
                        sqlText = String.format("SELECT matid,RTRIM(mtitle) AS mtitle FROM matchinfo WHERE matid = %s",aParaList[1]);
                        break;
                    //赛事活动
                    case "MatchInfo":
                        sqlText = "SELECT matid,mtitle FROM matchinfo ORDER BY ifactive DESC,endtime DESC";
                        break;
                    // 自己参与的赛事活动
                    case "JoinMatch":
                        sqlText = String.format("SELECT matchinfo.matid, matchinfo.mtitle FROM matchinfo " +
                                "INNER JOIN matchsysperson ON matchinfo.matid = matchsysperson.matid " +
                                "WHERE matchsysperson.pid = '%s' AND matchsysperson.mptype < 10 " +
                                "AND matchsysperson.ifactive = '1'   " +
                                "ORDER BY matchinfo.ifactive DESC,matchinfo.plantime", userid);
                        break;
                    //当前激活赛事
                    case "ActiveMatchInfo":
                        sqlText = "SELECT matid,RTRIM(mtitle) AS mtitle FROM matchinfo WHERE ifactive = '1'";
                        break;
                    // 查询赛事阶段
                    case "Stage":
                        sqlText = "SELECT catno AS stageId, sname FROM sysstage where catno > 0";
                        break;
                    // 查询指定阶段的赛事日程
                    case "MatchSchedule":
                        sqlText = String.format("SELECT DISTINCT funcid, functitle FROM sysfunctions WHERE catno = '%s'", aParaList[1]);
                        break;
                    // 评分环节
                    case "ScoreLink":
                        sqlText = "SELECT scorelink.scorelinkid, scorelink.scorelinkname FROM scorelink";
                        break;
                    //项目大类
                    case "MatchBroadType":
                        sqlText = String.format("SELECT mbtid,mbtname FROM matchbroadtype WHERE matid = %s ORDER BY mbtid",aParaList[1]);
                        break;
                    //查找当前赛事下的所有州市
                    case "MatchCity":
                        sqlText = String.format("SELECT cityinfo.cid AS ccode, RTRIM(cityinfo.cname) AS cname FROM cityinfo " +
                                " INNER JOIN matchcityinfo ON matchcityinfo.cid = cityinfo.cid " +
                                " WHERE matchcityinfo.matid = '%s'", aParaList[1]);
                        break;
                    //根据当前赛事的州市联络员添加报名人员填充所在州市
                    case "MatchCityByLeader":
                        sqlText = String.format("SELECT cityinfo.cid AS ccode, RTRIM(cityinfo.cname) AS cname FROM matchcityinfo " +
                                "INNER JOIN cityinfo ON cityinfo.cid = matchcityinfo.cid " +
                                "WHERE cityinfo.managerid = '%s' AND matchcityinfo.matid = '%s'", userid,aParaList[1]);
                        break;
                    //项目小类
                    case "MatchCriteria":
                        sqlText = String.format("SELECT mcid,mcname FROM matchcriteria WHERE mbtid = %s ORDER BY mcid ",aParaList[1]);
                        break;
                    //赛事下所有项目小类
                    case "AllMatchCriteria":
                        sqlText = String.format("SELECT DISTINCT mcid,mcname FROM matchcriteria INNER JOIN matchbroadtype ON   " +
                                "matchcriteria.mbtid = matchbroadtype.mbtid WHERE matchbroadtype.matid = '%s'  ",aParaList[1]);
                        break;
                    // 自己管理的学段课程
                    case "JoinGroup":
                        sqlText = String.format("SELECT DISTINCT matchbroadtype.mbtid, matchbroadtype.mbtname " +
                                "FROM matchbroadtype INNER JOIN matchprojectworker ON matchbroadtype.mbtid = matchprojectworker.mbtid " +
                                "INNER JOIN matchsysperson ON matchprojectworker.mspid = matchsysperson.mspid " +
                                "AND matchbroadtype.matid = matchsysperson.matid " +
                                "WHERE matchbroadtype.matid = '%s' AND matchsysperson.pid = '%s' ", aParaList[1], userid);
                        break;

                    // 自己管理的答辩组别
                    case "JoinSessionGroup":
                        sqlText = String.format("SELECT DISTINCT matchprojectsession.mpsid,matchprojectsession.mpsname " +
                                " FROM matchprojectsession INNER JOIN matchprojectworker on  matchprojectsession.mpsid = matchprojectworker.mpsid    " +
                                " INNER JOIN matchsysperson ON matchprojectworker.mspid = matchsysperson.mspid    " +
                                " AND matchprojectsession.matid = matchsysperson.matid    " +
                                " WHERE matchprojectsession.matid = '%s' AND matchsysperson.pid = '%s' ", aParaList[1], userid);
                        break;
                    //查询当前赛事下的答辩组
                    case "MatchProjectSession":
                        sqlText = String.format("SELECT mpsid,mpsname FROM matchprojectsession WHERE matid = %s ORDER BY mpsid",aParaList[1]);
                        break;
                    //答辩组
                    case "MatchGroup":
                        sqlText = String.format("SELECT A.mpsid,A.mpsname FROM matchprojectsession AS A WHERE A.mbtid = %s ORDER BY A.mpscode",aParaList[1]);
                        break;
                    //当前答辩组下选手所在州市
                    case "MatchCityByMbtid":
                        sqlText = String.format("SELECT DISTINCT matchcityinfo.cid AS ccode, RTRIM(cityinfo.cname) AS cname   " +
                                "FROM cityinfo INNER JOIN matchcityinfo ON cityinfo.cid = matchcityinfo.cid  " +
                                "INNER JOIN matchprojectperson ON matchcityinfo.cid = matchprojectperson.cid   " +
                                "AND matchcityinfo.matid = matchprojectperson.matid  " +
                                "WHERE matchcityinfo.matid = '%s' AND matchprojectperson.mbtid = '%s'",aParaList[1], aParaList[2]);
                        break;
                    // 打分评审专家
                    case "ScoreJudge":
                        sqlText = String.format("SELECT DISTINCT matchsysperson.pid,  matchsysperson.pname  " +
                                "FROM matchsysperson INNER JOIN matchprojectworker ON matchsysperson.mspid = matchprojectworker.mspid  " +
                                "WHERE matchsysperson.matid = '%s' AND matchprojectworker.mbtid = '%s'  AND matchsysperson.mptype = '7'", aParaList[1], aParaList[2]);
                        break;
                    //专家查询各自负责的打分组别
                    case "ScoreJudgeSession":
                        sqlText = String.format("SELECT DISTINCT matchsysperson.pid,  matchsysperson.pname  " +
                                "FROM matchsysperson INNER JOIN matchprojectworker ON matchsysperson.mspid = matchprojectworker.mspid  " +
                                "WHERE matchsysperson.matid = '%s' AND matchprojectworker.mpsid = '%s'  AND matchsysperson.mptype = '7'", aParaList[1], aParaList[2]);
                        break;

                    //管理员可查询的答辩组别
                    case "ManagerJoinGroup":
                        sqlText = String.format("SELECT DISTINCT matchprojectsession.mpsid,matchprojectsession.mpsname " +
                                "FROM matchprojectsession WHERE matchprojectsession.matid = '%s' ", aParaList[1], userid);
                        break;
                    default:
                        break;
                }
                return ZZSR2DBCService.getSelectLinkStr(dbCLient,sqlText).map(t -> t.replace( ',',':' ))
                        .flatMap( s1 -> ServerResponse.ok().bodyValue(s1) );
            }catch (Exception e) {
                return ServerResponse.badRequest().build();
            }
        });

    }

}
