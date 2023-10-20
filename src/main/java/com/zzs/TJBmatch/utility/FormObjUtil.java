package com.zzs.TJBmatch.utility;

import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.domain.UpdateLogObj;
import com.zzs.TJBmatch.domain.UpdateLogObjBatch;
import com.zzs.TJBmatch.services.Utilities;
import io.r2dbc.spi.ConnectionFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormObjUtil implements ApplicationEventPublisherAware {

    private static ApplicationEventPublisher myApplicationEventPublisher;
    private static Logger log = LoggerFactory.getLogger( FormObjUtil.class);

    public static Mono<String> queryResult(DatabaseClient dbClient, ServerRequest request, String opcode, String msg, String sqlText, Map<String,Object> sparas, String sMark){
        return getQueryData(dbClient,sqlText,sparas)
                .flatMap( data ->{
                    JSONObject jsonObject =  new JSONObject();
                    jsonObject.put( "opcode",opcode );
                    jsonObject.put( "msg",msg );
                    jsonObject.put( "data",data );

//                    return SecurityLog.RecordQuerylog(dbClient,request,opcode,Arrays.stream(Utilities.findSqlTables(sqlText)).collect(Collectors.joining(",")),sMark)
//                            .flatMap( rs -> Mono.just( jsonObject.toString(  )));
                    return Mono.just(jsonObject.toString());
                } );

    }

    public static Mono<JSONObject> getQueryData(DatabaseClient dbClient, String sql, Map<String,Object> paraMap){
        if (sql != null && !sql.trim().equals(""))
            return ZZSR2DBCService.getJSONObjMono(dbClient,sql,paraMap);
        return null;
    }

    public static Mono<Integer> getUpdateData(DatabaseClient dbClient, String sql, Map<String,Object> paraMap){
        if (sql != null && !sql.trim().equals("")) {
            return ZZSR2DBCService.insertUpdateDelete( dbClient, sql, paraMap ).switchIfEmpty( Mono.just( 0 ) );
        }
        return Mono.just( 0 );
    }

    public static Mono<Void> getUpdateDataTrans(ConnectionFactory conn, String[] sqlArr, Map<String,Object> paraMap){
        if (sqlArr.length>0 )
            return  ZZSR2DBCService.insertUpdateDeleteForTrans(conn,sqlArr,paraMap);
        return null;
    }

    public static Mono<String> updateResult(DatabaseClient dbClient, ServerRequest request, String opcode, String sMark, String sqlText, Map<String,Object> sparas, String errMsg){
        String sqlType = Utilities.getSqlType(sqlText);
        return getUpdateData(dbClient,sqlText,sparas)
                .flatMap( mi ->{
                    log.info("影响行数："+mi);
                    JSONObject jsonObject = new JSONObject();
                    if (mi >0){
                        jsonObject.put( "msg", "成功" );
                        jsonObject.put("data", "记录" + sqlType + "成功"  );
                        myApplicationEventPublisher.publishEvent( new UpdateLogObj(dbClient,request,opcode,sqlType,Arrays.stream(Utilities.findSqlTables(sqlText))
                                .collect(Collectors.joining(",")),sMark,sparas));
                        return Mono.just(jsonObject.toString());
                    }
                    else
                    {
                        jsonObject.put("msg", "未改动" );
                        jsonObject.put("data", "记录" + sqlType + "不成功，记录未作改动"+"("+errMsg+")"  );
                        return Mono.just(jsonObject.toString() );
                    }
                } );
    }

//    public static Mono<String> updateTransResult(DatabaseClient dbClient, ServerRequest request, String opcode, String sMark, String[] sqlArr, Map<String,Object> sparas, String errorMsg){
//        String sqlType = Utilities.getSqlType(sqlArr[0]);
//        return SecurityLog.RecordSecuritylog(dbClient,request,opcode,sqlType,Arrays.stream(Utilities.findSqlTables(Arrays.toString(sqlArr)))
//                .collect(Collectors.joining(",")),sMark,sparas)
//                .flatMap( si ->{
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.put( "opcode",opcode  );
//                    jsonObject.put( "msg", "成功" );
//                    jsonObject.put( "data", "记录事务操作" + sqlType + "成功" );
//                    return Mono.just( jsonObject.toString() );
//                } );
//    }

    public static Mono<Integer> updateResultBatch(DatabaseClient dbClient, ServerRequest request, String sMark, String[] sqlArr, Map<String,Object> sparas){
//        String sqlType = Utilities.getSqlType(sqlArr[0]);
//        String type = StringUtils.isEmpty(sqlType) ? Utilities.getSqlType(sqlArr[0]) : sqlType;
        return getUpdateDataBatch(dbClient,sqlArr,sparas)
                .flatMap( mi ->{
//                    log.info("影响行数："+mi);
//                    JSONObject jsonObject = new JSONObject();
                    if (mi >0){
//                        jsonObject.put( "msg", "成功" );
//                        jsonObject.put("data", "记录" + sqlType + "成功"  );
//                        myApplicationEventPublisher.publishEvent( new UpdateLogObj(dbClient,request,"批量操作","批量",Arrays.stream(Utilities.findSqlTables( Arrays.stream( sqlArr ).collect( Collectors.joining( ";" ) )))
//                                .collect(Collectors.joining(",")),sMark,sparas));
                        myApplicationEventPublisher.publishEvent( new UpdateLogObjBatch(dbClient,request,"批量操作",sqlArr,sMark,sparas));
                        return Mono.just(mi);
                    }
                    else
                    {
//                        jsonObject.put( "msg", "未改动" );
//                        jsonObject.put("data", "记录" + sqlType + "不成功，记录未作改动"+"("+errMsg+")"  );
                        return Mono.just(0);
                    }
                } );
    }

    public static Mono<Integer> updateResultBatch(DatabaseClient dbClient, ServerRequest request, String sMark, String[] sqlArr){
        if (sqlArr != null && sqlArr.length != 0) {
            return ZZSR2DBCService.insertUpdateDeleteForBatch( dbClient, sqlArr).switchIfEmpty( Mono.just( 0 ) );
        }
        return Mono.just( 0 );
    }

    private static Mono<Integer> getUpdateDataBatch(DatabaseClient dbClient, String[] sqlArr, Map<String, Object> sparas) {
        if (sqlArr != null && sqlArr.length != 0) {
            return ZZSR2DBCService.insertUpdateDeleteForBatch( dbClient, sqlArr, sparas ).switchIfEmpty( Mono.just( 0 ) );
        }
        return Mono.just( 0 );
    }

    public static String okResult(String opcode,String sqlType, String sMark,String data){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("opcode",opcode);
        jsonObject.put("msg", "成功");
        jsonObject.put("mark",sMark);
        jsonObject.put("data",data);
        return jsonObject.toString();
    }

    public static String errorResult(String opcode,String sqlType, String sMark,String data){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("opcode",opcode);
        jsonObject.put("msg", "失败");
        jsonObject.put("mark",sMark);
        jsonObject.put("data",data);
        return jsonObject.toString();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        myApplicationEventPublisher = applicationEventPublisher;
    }
}
