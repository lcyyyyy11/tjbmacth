package com.zzs.TJBmatch.filterfuncs;

import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.services.Utilities;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.function.BiFunction;

@Component
public class AddLogRecords implements BiFunction<ServerRequest, ServerResponse, ServerResponse> {
    private static Logger log = LoggerFactory.getLogger( AddLogRecords.class );
    private DatabaseClient dbCLient;
    public AddLogRecords(@Qualifier("DBClient") DatabaseClient client){
        this.dbCLient = client;
    }

    @Override
    public ServerResponse apply(ServerRequest request, ServerResponse serverResponse) {
        // 获取登录人员的IP地址等信息
        Optional<InetSocketAddress> remoteAddress = request.remoteAddress();
        String sClientIP = remoteAddress.get().getAddress().getHostAddress();
        UserAgent userAgent = UserAgent.parseUserAgentString(request.headers().header("User-Agent").toString());
        String sBrowser = userAgent.getBrowser()+ " " + userAgent.getBrowserVersion();
        String sOp = userAgent.getOperatingSystem().getName();
        try{
            String rst = "1", sMark = "登录成功";
            if (!serverResponse.headers().containsKey("STAMPHASH")){
                rst = "0";
                sMark = "登录失败";
            }
            String sLogNo = serverResponse.headers().getFirst("YLoginId");
            String sLogName = Utilities.getDecodeValue(serverResponse.headers().getFirst("YUserName"));
//            String sqlText = "insert into sysloginlog (loginid,logintime,loginip,browsertype," +
//                    "loginresult,loginremark,screenresolution) Values " +
//                    "('" + sLogNo + "',now(),'" + sClientIP + "','" + sBrowser + "','" + rst + "','" + sMark + "','" + sOp +"')";
            String sqlText = String.format("insert into sysloginlog (loginid, loginname,logintime,loginip,browsertype,loginresult,loginremark,screenresolution) Values ('%s','%s',now(),'%s','%s','%s','%s','%s')", sLogNo, sLogName, sClientIP, sBrowser, rst, sMark, sOp);
            ZZSR2DBCService.insertUpdateDelete(dbCLient, sqlText).subscribe( );
        }catch (Exception e){;}
        return serverResponse;
    }
}
