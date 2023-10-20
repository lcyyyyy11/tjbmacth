package com.zzs.TJBmatch.handlers.userAndSystem;


import com.zzs.TJBmatch.handlers.publichandler.DownLoadAndUpload;
import com.zzs.TJBmatch.utility.GridObjUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class SafeRecordsHandler {
    private DatabaseClient dbClient;
    @Autowired
    private DownLoadAndUpload downLoadAndUpload;
    public SafeRecordsHandler(@Qualifier("DBClient") DatabaseClient client){
        this.dbClient = client;
    }

    public Mono<ServerResponse> gridSearch(ServerRequest request){
        Map<String, String> sFullParams = request.queryParams().toSingleValueMap();
        String sqlText = "select rtrim(personno) as personno,rtrim(personname) as personname,rtrim(opclass) as opclass," +
                "DATE_FORMAT(optime,'%Y-%m-%d %H:%i:%s') as optime,rtrim(optype) as optype," +
                "rtrim(opcontent) as opcontent,rtrim(markid) as markid,rtrim(romoteaddr) as romoteaddr " +
                "from syssecuritylog where (DATEDIFF(:startDate,optime) <= 0 or (:startDate is null)) " +
                "and (DATEDIFF(optime,:endDate) <= 0 or (:endDate is null)) " +
                "ORDER BY syssecuritylog.optime DESC";
        return GridObjUtil.queryResult(dbClient,request,"系统变更日志","查询",sqlText,new HashMap<String,Object>(sFullParams),"0")
         .flatMap(result-> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }

    public Mono<ServerResponse> export(ServerRequest request){
        Map<String,String> sFullParams  = request.queryParams().toSingleValueMap();
        String sqlText = "SELECT rtrim(personno) as '账号',rtrim(personname) as '姓名',rtrim(opclass) as '操作代码'," +
                "rtrim(optype) as '操作类型',rtrim(opcontent) as '操作内容'," +
                "DATE_FORMAT(optime,'%Y-%m-%d %H:%i:%s') as '操作时间',rtrim(markid) as '标记值'," +
                "rtrim(romoteaddr) as '地址' FROM syssecuritylog " +
                "where (DATEDIFF(:startDate,optime) <= 0 or (:startDate is null)) " +
                "and (DATEDIFF(optime,:endDate) <= 0 or (:endDate is null)) " +
                "ORDER BY syssecuritylog.optime DESC";
        return downLoadAndUpload.zToExcel(sqlText,sFullParams,"系统安全记录");
    }
}

