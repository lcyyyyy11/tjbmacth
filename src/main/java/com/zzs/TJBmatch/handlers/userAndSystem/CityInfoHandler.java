package com.zzs.TJBmatch.handlers.userAndSystem;


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
public class CityInfoHandler {
    private DatabaseClient dbCLient;

    public CityInfoHandler(@Qualifier("DBClient") DatabaseClient client) {
        this.dbCLient = client;
    }

    //jqgrid--查找市州信息库
    public Mono<ServerResponse> gridSearch(ServerRequest request){
        String sqlText = "SELECT cid , cname , managername , managerphone , managerid ,manageremail , cnote ,managersex,managerbirth  FROM cityinfo" ;

        Map<String,String> rData = request.queryParams().toSingleValueMap();
        return GridObjUtil.queryResult(dbCLient, request, "市州信息库", "查询市州信息", sqlText, new HashMap<>(rData), "")
                .flatMap(res->ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

//
    //编辑市州联络员信息
    public Mono<ServerResponse> gridEdit(ServerRequest request){
        Mono<WebSession> rSession = request.session();
        Mono<Map<String, String>> rDatas = request.formData().map(MultiValueMap::toSingleValueMap);
        String sqlText = "UPDATE cityinfo SET  managername =:managername, managerphone =:managerphone, " +
                "managerid =:managerid, manageremail =:manageremail,cnote =:cnote, managersex = :managersex,managerbirth = :managerbirth   " +
                "WHERE cityinfo.cid = :cid";
        return rSession.zipWith(rDatas)
                .flatMap(f->{
                    String userrole = f.getT1().getAttribute("UserRole");
                    if (!StringUtils.isEmpty(userrole) && Integer.parseInt(userrole) > 2)
                        throw new ValidationException(RtnEnum.NO_RIGHT, "抱歉，您无操作权限，请和主办方联系！");

                    return FormObjUtil.updateResult(dbCLient,request,"市州信息库","编辑市州信息",sqlText,new HashMap<String,Object>(f.getT2()),"编辑失败!");
                })
                .flatMap(result-> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result));
    }
}
