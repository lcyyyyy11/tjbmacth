package com.zzs.TJBmatch.utility;

import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.services.ImgSer;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class GridObjUtil {
    //private static Logger log = LoggerFactory.getLogger(GridObjUtil.class);
    public static Mono<JSONArray> getJSONArray(DatabaseClient dbClient, String sql, Map<String,Object> paraMap){
        return ZZSR2DBCService.getJsonArray( dbClient,sql,paraMap );
    }

    public static Mono<String> queryResult(DatabaseClient dbClient, ServerRequest request, String opcode, String info, String sqlText, Map<String,Object> sparas, String sMark){

        return getJSONArray(dbClient,sqlText,sparas)
                .flatMap( jsonArr ->{
                    JSONObject jsonObject =  new JSONObject();
                    jsonObject.put( "opcode",opcode );
                    jsonObject.put( "infos",info );
                    jsonObject.put( "rows",jsonArr );
//                    return SecurityLog.RecordQuerylog(dbClient,request,opcode,Arrays.stream( Utilities.findSqlTables(sqlText))
//                            .collect(Collectors.joining(",")),sMark)
//                            .flatMap( s -> Mono.just(  jsonObject.toString()) );
                    return Mono.just(jsonObject.toString());
                } );
    }

//    public static String queryImgResult(DatabaseClient dbClient, ServerRequest request, String opcode, String info, String sqlText, Map<String,String> sparas, String photoName, String path) {
//        JSONArray jsonArr = null;
//        if (sqlText != null && !sqlText.trim().equals(""))
//            jsonArr = ZZSR2DBCService.getJsonArray(dbClient,sqlText, new HashMap<String, Object>(sparas) ).block();
//        JSONObject object = new JSONObject();
//        if(jsonArr != null && jsonArr.length()>0){
//            for (int i = 0; i< jsonArr.length(); i++){
//                JSONObject jsonObject = jsonArr.getJSONObject(i);
//                String photoPath = jsonObject.has(photoName) ? jsonObject.get(photoName).toString() : "";
//                if(StringUtils.isEmpty(photoPath) || photoPath.equals("null")){
//                    jsonObject.put("ImgBase", "");
//                }else {
//                    String Path = path + File.separator  + "thumbnail" + File.separator + photoPath;
//                    String imageStr = ImgSer.getImgStr(Path);
//                    jsonObject.put("ImgBase", imageStr);
//                }
//            }
//        }
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("opcode",opcode);
//        jsonObject.put("infos",info);
//        jsonObject.put("rows",jsonArr);
//        return jsonObject.toString();
//    }

    public static Mono<String> queryImgResult(DatabaseClient dbClient, ServerRequest request, String opcode, String info, String sqlText, Map<String,String> sparas, String photoName, String path) {
        return getJSONArray(dbClient,sqlText,new HashMap<>(sparas))
                .flatMap( jsonArr ->{
                    if(jsonArr != null && jsonArr.length()>0){
                        for (int i = 0; i< jsonArr.length(); i++){
                            JSONObject jsonObject = jsonArr.getJSONObject(i);
                            String photoPath = jsonObject.has(photoName) ? jsonObject.get(photoName).toString() : "";
                            if(StringUtils.isEmpty(photoPath) || photoPath.equals("null")){
                                jsonObject.put("ImgBase", "");
                            }else {
                                String Path = path + File.separator  + "thumbnail" + File.separator + photoPath;
                                String imageStr = ImgSer.getImgStr(Path);
                                jsonObject.put("ImgBase", imageStr);
                            }
                        }
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("opcode",opcode);
                    jsonObject.put("infos",info);
                    jsonObject.put("rows",jsonArr);
                    return Mono.just(jsonObject.toString()) ;
                });
    }

    public static Mono<String> queryOImgResult(DatabaseClient dbClient, ServerRequest request, String opcode, String info, String sqlText, Map<String,String> sparas, String photoName, String path) {
        return getJSONArray(dbClient,sqlText,new HashMap<>(sparas))
                .flatMap( jsonArr ->{
                    if(jsonArr != null && jsonArr.length()>0){
                        for (int i = 0; i< jsonArr.length(); i++){
                            JSONObject jsonObject = jsonArr.getJSONObject(i);
                            String photoPath = jsonObject.has(photoName) ? jsonObject.get(photoName).toString() : "";
                            if(StringUtils.isEmpty(photoPath) || photoPath.equals("null")){
                                jsonObject.put("ImgBase", "");
                            }else {
                                String Path = path + File.separator + photoPath;
                                String imageStr = ImgSer.getImgStr(Path);
                                jsonObject.put("ImgBase", imageStr);
                            }
                        }
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("opcode",opcode);
                    jsonObject.put("infos",info);
                    jsonObject.put("rows",jsonArr);
                    return Mono.just(jsonObject.toString()) ;
                });
    }


}
