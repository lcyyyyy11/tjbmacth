package com.zzs.TJBmatch.utility;

import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.domain.BasicLogObj;
import com.zzs.TJBmatch.domain.UpdateLogObj;
import com.zzs.TJBmatch.domain.UpdateLogObjBatch;
import com.zzs.TJBmatch.services.Utilities;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SecurityLog {

    private static Logger log = LoggerFactory.getLogger( SecurityLog.class);

    public static Mono<Integer> RecordSecuritylog(DatabaseClient dbClient, ServerRequest request, String opcode, String sqltype, String sTable, String sMark, Map<String,Object> sMapParams) {
        String sqlAdd = "insert into syssecuritylog (markid,personno,optime,opclass,optype,opcontent,romoteaddr,personname) " +
                "Values ('%s','%s',now(),'A','%s','【%s】- 表(%s) - 新加记录','%s','%s')";
        String sqlDel = "insert into syssecuritylog (markid,personno,optime,opclass,optype,opcontent,romoteaddr,personname) " +
                "Values ('%s','%s',now(),'D','%s','【%s】- 表(%s) - 删除记录','%s','%s')";
        String sqlBatch = "insert into syssecuritylog (markid,personno,optime,opclass,optype,opcontent,romoteaddr,personname) " +
                "Values ('%s','%s',now(),'B','%s','【%s】- 表(%s) - 批量操作','%s','%s')";
        return request.session()
               .flatMap(x -> {
           String UserNo = x.getAttribute("UserID");
           String UserName = x.getAttribute("UserName");
           try{
               Optional<InetSocketAddress> remoteAddress = request.remoteAddress();
               String sIPAddr = remoteAddress.get().getAddress().getHostAddress();
               String sKeyVal = "", sqlTxt = "";
               sKeyVal = sMark.trim();
               if (sqltype.contains("新加")) {
                   sqlTxt = String.format(sqlAdd, sKeyVal, UserNo, sqltype, opcode, sTable, sIPAddr, UserName);
                   return ZZSR2DBCService.insertUpdateDelete(dbClient, sqlTxt);
               } else if (sqltype.contains("删除")) {
                   sqlTxt = String.format(sqlDel, sKeyVal, UserNo, sqltype, opcode, sTable, sIPAddr, UserName);
                   return ZZSR2DBCService.insertUpdateDelete(dbClient, sqlTxt);
               } else if (sqltype.contains("修改")) {
                   Map<String, Object> dicParam = sMapParams;
                   Set<String> sKeys = dicParam.keySet();
                   if (sKeys.contains("oldlist")) {
                       Map<String, Object> oldlist = Arrays.asList(dicParam.get("oldlist").toString().split("&"))
                               .stream()
                               .map(elem -> elem.split("="))
                               .filter(elem -> elem.length == 2)
                               .collect(Collectors.toMap(e -> e[0], e -> e[1]));
                       dicParam.putAll(oldlist);
                       sKeys = dicParam.keySet();
                   }
                   dicParam.put("UserName",UserName);
                   List<String> sSql = sKeys.stream()
                           .filter(sk -> sk.contains("_old"))
                           .map(sk -> buildSql(sk, dicParam, UserNo, sIPAddr, sqltype, opcode, sTable, sMark))
                           .collect(Collectors.toList());
                   if (sSql.size() != 0 && !sSql.stream().allMatch(t -> t.equals(";")))
                       return ZZSR2DBCService.insertUpdateDeleteForBatch(dbClient, sSql.toArray(new String[0])).switchIfEmpty( Mono.just(0));
               } else if (sqltype.contains("批量")) {
                   sqlTxt = String.format(sqlBatch, sKeyVal, UserNo, sqltype, opcode, sTable, sIPAddr, UserName);
                   return ZZSR2DBCService.insertUpdateDelete(dbClient, sqlTxt);
               } else{
                   return Mono.just(0);
               }
           }catch (Exception e){
               log.info("日志记录异常："+e.getMessage());
               return Mono.just(0);
           }
            return Mono.just(0);
        });
    }

    public static Mono<Integer> RecordQuerylog(DatabaseClient dbClient, ServerRequest request, String opcode, String sTable, String sMark) {
        return request.session().flatMap(x -> {
            try {
                Optional<InetSocketAddress> remoteAddress = request.remoteAddress();
                String sIPAddr = remoteAddress.get().getAddress().getHostAddress();
                String UserNo = x.getAttribute("UserID");
                String sKeyVal = "", sqlTxt = "", sPname = "";
                if (sMark.trim().length() > 29)
                    sKeyVal = sMark.trim().substring(sMark.trim().length() - 28);
                sKeyVal = sMark.trim();
                if (opcode.substring(0, 2) == "导入" || opcode.substring(0, 2) == "上传")
                    sqlTxt = String.format("Insert Into LogQuery (MarkId,personNo,optime,opclass,optype,opcontent,remoteADDR,pname) Values ('%s','%s','%s',getdate(),'F','%s','【%s】- 上传与导入文件操作','%s','%s')", sKeyVal, UserNo, opcode.substring(0, 2), opcode, sIPAddr, sPname);
                else
                    sqlTxt = String.format("Insert Into LogQuery (MarkId,personNo,optime,opclass,optype,opcontent,remoteADDR,pname) Values ('%s','%s','%s',getdate(),'Q','%s','【%s】- 表(%s)-查询记录','%s','%s')", sKeyVal, UserNo, "查询", opcode, sTable, sIPAddr, sPname);
                return ZZSR2DBCService.insertUpdateDelete(dbClient, sqlTxt).switchIfEmpty( Mono.just(0));
            }catch (Exception e){
                return Mono.just( 0 );
            }
        });
    }

    /*
    * 20210601 bql 分数录入安全日志
    * */
    public static Mono<Integer> ScoringSecurityLog(DatabaseClient dbClient, ServerRequest request, String opcode, String sqltype, String sTable, String sMark, Map<String,Object> sMapParams) {
        return request.session()
                .flatMap(x -> {
                    String UserNo = x.getAttribute("UserID");
                    String UserName = x.getAttribute("UserName") + "(" +x.getAttribute("UserID") +")" ;
                    if (UserName.length() > 39){
                        UserName = x.getAttribute("UserID");
                    }
                    try{
                        Optional<InetSocketAddress> remoteAddress = request.remoteAddress();
                        String sIPAddr = remoteAddress.get().getAddress().getHostAddress();
                        UserAgent userAgent = UserAgent.parseUserAgentString(request.headers().header("User-Agent").toString());

                        String oldvalue = sMapParams.getOrDefault("oldvalue","").toString();
                        String mplayid = sMapParams.getOrDefault("mplayid","").toString();
                        String mptid = sMapParams.getOrDefault("mptid","").toString();
                        String matid = sMapParams.getOrDefault("matid","").toString();
                        String mbtid = sMapParams.getOrDefault("mbtid","").toString();
                        String mscid = sMapParams.getOrDefault("mscid","").toString();
                        String scorelinkid = sMapParams.getOrDefault("scorelinkid","").toString();
                        String modifyreason = sMapParams.getOrDefault("modifyreason","").toString();
                        if (StringUtils.isEmpty(mscid)) {
                            mscid = "NULL";
                        }
                        String mcaid = sMapParams.getOrDefault("mcaid","").toString();
                        if (StringUtils.isEmpty(mcaid)) {
                            mcaid = "NULL";
                        }
                        String newvalue = sMapParams.getOrDefault("score","").toString();
                        if (StringUtils.isEmpty(oldvalue)){
                            String opcontent = "【" + opcode + "】- 表(" + sTable + ") - 新加记录";
                            String sql = "insert into matchscoresecurity (mplayid,mptid,matid,mbtid,mscid,mcaid,opname,opid,optime," +
                                    "opcontent,oldvalue,newvalue,loginip,browsertype,scorelinkid,modifyreason) " +
                                    "Values (NULL," + mptid + "," + matid + "," + mbtid +","+
                                    mscid + "," + mcaid + ",'" + UserName + "','" + UserNo + "',now(),'" + opcontent + "'," +
                                    "NULL ," + newvalue + ",'" + sIPAddr + "','" + userAgent + "','"+scorelinkid+"','"+modifyreason+"')";
                            return ZZSR2DBCService.insertUpdateDelete(dbClient, sql).switchIfEmpty(Mono.just(0));
                        } else {
                            String opcontent = "【" + opcode + "】- 表(" + sTable + ") - " + "mplayscore" +
                                    " 值由 " + oldvalue + " 修改为 " + newvalue;
                            String sql = "insert into matchscoresecurity (mplayid,mptid,matid,mbtid,mscid,mcaid,opname,opid,optime," +
                                    "opcontent,oldvalue,newvalue,loginip,browsertype,scorelinkid,modifyreason) " +
                                    "Values (" + mplayid + "," + mptid + "," + matid + "," + mbtid + "," +
                                    mscid + "," + mcaid + ",'" + UserName + "','" + UserNo + "',now(),'" + opcontent + "'," +
                                    oldvalue + "," + newvalue + ",'" + sIPAddr + "','" + userAgent + "','"+scorelinkid+"','"+modifyreason+"')";
                            return ZZSR2DBCService.insertUpdateDelete(dbClient, sql).switchIfEmpty(Mono.just(0));
                        }
                    }catch (Exception e){
                        log.info("日志记录异常："+e.getMessage());
                        return Mono.just(0);
                    }
                });
    }

    public static Mono<Integer> RecordBatchSecuritylog(DatabaseClient dbClient, ServerRequest request, String opcode, String[] sqlArr, String sMark, Map<String,Object> sMapParams) {

        if (sqlArr.length < 1)
            return Mono.just(0);

        return request.session().flatMap(x -> {
            try{
//                String sType = x.getAttribute("UserType");
//                if(!sType.isEmpty() && (sType.equals("G") || sType.equals("S")|| sType.equals("D"))){
                    Optional<InetSocketAddress> remoteAddress = request.remoteAddress();
                    String sIPAddr = remoteAddress.get().getAddress().getHostAddress();
                    String sLogIn = x.getAttribute("UserID");
                    String sName = x.getAttribute("UserName");
//                    String sZoneCode = x.getAttribute("ZoneCode");
                    String sKeyVal = "", sqlTxt = "",sqltype="",sTable="";
                    List<String> runSqls = new ArrayList<>(  );
                    sKeyVal = sMark.trim();

                    for (int i = 0; i <sqlArr.length ; i++) {
                        sqltype = Utilities.getSqlType(sqlArr[i]);
                        sTable = Arrays.stream(Utilities.findSqlTables( sqlArr[i]))
                                .collect(Collectors.joining(","));

                        if (sqltype.contains("新加"))
//                            runSqls.add( String.format("Insert Into SecurityLog (MarkId,PersonNo,optime,opclass,optype,opcontent,romoteADDR,pname) Values ('%s','%s',getdate(),'A','%s','【%s】- 表(%s)-新加记录','%s','%s','%s')", sKeyVal, sLogIn, sqltype, opcode, sTable, sIPAddr, sName));
                            runSqls.add( String.format("insert into syssecuritylog (markid,personno,optime,opclass,optype,opcontent,romoteaddr,personname) " +
                                    "Values ('%s','%s',now(),'A','%s','【%s】- 表(%s) - 新加记录','%s','%s')", sKeyVal, sLogIn, sqltype, opcode, sTable, sIPAddr, sName));
                        else if (sqltype.contains("删除"))
//                            runSqls.add( String.format("Insert Into SecurityLog (MarkId,PersonNo,optime,opclass,optype,opcontent,romoteADDR,pname) Values ('%s','%s',getdate(),'D','%s','【%s】- 表(%s)-删除记录','%s','%s','%s')", sKeyVal, sLogIn, sqltype, opcode, sTable, sIPAddr, sName));
                            runSqls.add( String.format("insert into syssecuritylog (markid,personno,optime,opclass,optype,opcontent,romoteaddr,personname) " +
                                    "Values ('%s','%s',now(),'D','%s','【%s】- 表(%s) - 删除记录','%s','%s')", sKeyVal, sLogIn, sqltype, opcode, sTable, sIPAddr, sName));
                        else if (sqltype.contains("修改")) {
                            Map<String, Object> dicParam = sMapParams;
                            Set<String> sKeys = dicParam.keySet();
                            if (sKeys.contains("oldlist")) {
                                Map<String, Object> oldlist = Arrays.asList(dicParam.get("oldlist").toString().split("&"))
                                        .stream()
                                        .map(elem -> elem.split("="))
                                        .filter(elem -> elem.length == 2)
                                        .collect(Collectors.toMap(e -> e[0], e -> e[1]));
                                dicParam.putAll(oldlist);
                                sKeys = dicParam.keySet();
                            }
                            dicParam.put("UserName",sName);
                            String currTable = sTable;
                            List<String> sSql = sKeys.stream()
                                    .filter(sk -> sk.contains("_old"))
                                    //buildSql(sk, dicParam, UserNo, sIPAddr, sqltype, opcode, sTable, sMark))
                                    .map(sk -> buildSql(sk, dicParam, sLogIn, sIPAddr, "修改", opcode, currTable, sMark))
                                    .collect(Collectors.toList());
                            if (sSql.size() != 0)
                                runSqls.addAll( sSql );
                            else
                                runSqls.add(String.format("insert into syssecuritylog (markid,personno,optime,opclass,optype,opcontent,romoteaddr,personname) " +
                                        "Values ('%s','%s',now(),'M','%s','【%s】- 表(%s) - 更新记录','%s','%s')", sKeyVal, sLogIn, sqltype, opcode, sTable, sIPAddr, sName));

//                            runSqls.forEach(System.out::println);
//
                        }
                    }
                    if (runSqls.size() != 0 && !runSqls.stream().allMatch(t -> t.equals(";")))
                        return ZZSR2DBCService.insertUpdateDeleteForBatch(dbClient, runSqls.toArray(new String[0])).switchIfEmpty( Mono.just(0));
//                }else{
//                    return Mono.just(0);
//                }
            }catch (Exception e){
                log.info("日志记录异常："+e.getMessage());
                return Mono.just(0);
            }
            return Mono.just(0);
        });
    }

    private static String buildSql(String sKey, Map<String, Object> paraMap,String sLogIn,String sIPAddr,String sType,String opCode,String sTables,String sMark){
        String sPname="";
        try {
            sPname = paraMap.get("UserName").toString();
        }catch (Exception e){;}

        String sOldVal = paraMap.get(sKey).toString().trim().substring(0, paraMap.get(sKey).toString().trim().indexOf("-zzs-")).trim();
        String sNewKey = sKey.trim().substring(0, sKey.trim().length() - 4).trim();
        String sNewVal = paraMap.get(sNewKey) != null ? paraMap.get(sNewKey).toString().trim() : null;

        if ((null != sOldVal && null != sNewVal) && !sOldVal.equals(sNewVal) ){
            String sContent = "【" + opCode + "】- 表(" + sTables + ") - " + sKey.substring(0,sKey.indexOf("_old")) +
                    "-值由-" + sOldVal + "修改为-" + sNewVal;
            if (sContent.length() > 295) {
                sContent = sContent.substring(0,295);
            }
            return "insert into syssecuritylog (markid,personno,optime,opclass,optype,opcontent," +
                    "romoteaddr,personname) Values ('" + sMark + "','" + sLogIn + "',now(),'M','" +
                    sType + "','" + sContent + "','"  + sIPAddr + "','" + sPname + "')";
        }

        return "";
    }

    @EventListener
    public void accessUpdateLog(UpdateLogObj logObj) {
        String opcode = logObj.getOpcode();
        if (opcode.contains("评分确认与锁定--分数录入"))
            ScoringSecurityLog(logObj.getDbClient(),logObj.getRequest(),logObj.getOpcode(),logObj.getSqltype(),logObj.getsTables()
                    ,logObj.getsMark(),logObj.getsMapParams())
                    .subscribe();
        else
            RecordSecuritylog(logObj.getDbClient(),logObj.getRequest(),logObj.getOpcode(),logObj.getSqltype(),logObj.getsTables()
                    ,logObj.getsMark(),logObj.getsMapParams())
                    .subscribe();
    }

    @EventListener
    public void accessBasicLog(BasicLogObj logObj) {
        RecordQuerylog(logObj.getDbClient(),logObj.getRequest(),logObj.getOpcode(),logObj.getsTables()
                ,logObj.getsMark())
                .subscribe(System.out::println);
    }

    @EventListener
    public void accessBatchLog(UpdateLogObjBatch logObj) {
        RecordBatchSecuritylog(logObj.getDbClient(),logObj.getRequest(),logObj.getOpcode(),logObj.getSqls(),logObj.getsMark(),logObj.getsMapParams())
                .subscribe();
    }

}
