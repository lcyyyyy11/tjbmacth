package com.zzs.TJBmatch.handlers.userAndSystem;


import com.zzs.TJBmatch.utility.GridObjUtil;
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
public class LoginRecordsHandler {
    private DatabaseClient dbClient;
    public LoginRecordsHandler(@Qualifier("DBClient") DatabaseClient client){
        this.dbClient = client;
    }

    public Mono<ServerResponse> gridSearch(ServerRequest request){
        Map<String, String> sFullParams = request.queryParams().toSingleValueMap();
        String sqlText = "select rtrim(loginid) as loginid,DATE_FORMAT(logintime,'%Y-%m-%d %H:%i:%s') AS logintime," +
                "rtrim(loginip) as loginip,rtrim(browsertype) as browsertype,rtrim(loginremark) as loginremark," +
                "rtrim(screenresolution) as screenresolution,loginname as username " +
                "from sysloginlog  " +
                "WHERE DATEDIFF(:startDate,logintime) <= 0 AND DATEDIFF(logintime,:endDate) <= 0 " +
                "ORDER BY sysloginlog.logintime DESC";
        return GridObjUtil.queryResult(dbClient,request,"登录日志","查询",sqlText,new HashMap<String,Object>(sFullParams),"0")
                .flatMap(result-> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }
}

