package com.zzs.TJBmatch.handlers.publichandler;

import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.domain.BasicLogObj;
import com.zzs.TJBmatch.services.ImgSer;
import com.zzs.TJBmatch.services.MSExcelSer;
import com.zzs.TJBmatch.services.Utilities;
import com.zzs.TJBmatch.utility.FormObjUtil;
import com.zzs.TJBmatch.utility.SecurityLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class DownLoadAndUpload implements ApplicationEventPublisherAware {
    private static ApplicationEventPublisher myApplicationEventPublisher;
    private static Logger log = LoggerFactory.getLogger( DownLoadAndUpload.class );

    // Excel导入导出临时文件路径
    Path dpath;
    private String DownLoadFiles  = Paths.get(System.getProperty("user.dir"),"Temp").toString();

    private DatabaseClient dbClient;
    public DownLoadAndUpload(@Qualifier("DBClient") DatabaseClient client){
        this.dbClient = client;
    }

    /**
     * 封装方法：上传文件
     * 文件存放路径：path
     * Function<String,Boolean> function  上传后文件名  您的操作是否成功标识
     *
     * @param request
     * @param function
     * @return
     */
    public <T> Mono<ServerResponse> zupload(ServerRequest request, Path path, Function<String,Mono<T>> function){
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            Mono.error(e);
        }
        return request.multipartData()
                .map(mp -> mp.toSingleValueMap())
                .flatMap(part -> Mono.just((FilePart) part.get("file")))
                .cast(FilePart.class)
                .flatMap(filePart -> {
                    // 1. 保存上传文件；文明名一致则覆盖
                    Path file_path = Paths.get(path.toString(),filePart.filename());
                    String fullFileName = filePart.filename();
                    filePart.transferTo(file_path);

                    // 2. 您的操作返回值
                    Mono<T> apply = function.apply(fullFileName);
                    return apply.flatMap(t -> {
                        if (t instanceof Boolean) {
                            Boolean apply_bool = (Boolean)t;
                            if(!apply_bool){
                                return ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON)
                                        .body(Mono.just(FormObjUtil.errorResult("文件上传","上传","","上传失败")), String.class);
                            }
                            return ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(Mono.just(FormObjUtil.okResult("文件上传","上传","","上传成功")), String.class);
                        }
                        else if(t instanceof String) {
                            String apply_str = (String)t;
                            if(apply_str.equals("0")){
                                return ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON)
                                        .body(Mono.just(FormObjUtil.errorResult("文件上传","上传","","上传失败")), String.class);
                            }
                            else if (apply_str.equals("1")) {
                                return ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON)
                                        .body(Mono.just(FormObjUtil.okResult("文件上传","上传","","上传成功")), String.class);
                            }
                            else {
                                return ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON)
                                        .body(Mono.just(FormObjUtil.errorResult("文件上传","上传","",apply_str)), String.class);
                            }
                        }
                        return ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON)
                                .body(Mono.just(FormObjUtil.errorResult("文件上传","上传","","传入参数类型错误")), String.class);
                    });
                });
    }

//    /**
//     * 封装方法：上传文件
//     * 文件存放路径：path
//     * Function<String,Boolean> function  上传后文件名  您的操作是否成功标识
//     *
//     * @param request
//     * @param function
//     * @return
//     */
//    public Mono<ServerResponse> zupload(ServerRequest request, Path path, Function<String, Mono<Boolean>> function){
//        try {
//            Files.createDirectories(path);
//        } catch (IOException e) {
//            Mono.error(e);
//        }
//        return request.multipartData()
//                .map( MultiValueMap::toSingleValueMap)
//                .flatMap(part -> Mono.just((FilePart) part.get("file")))
//                .cast( FilePart.class)
//                .flatMap(filePart -> {
//                    // 1. 保存上传文件；文件名一致则覆盖
//                    Path file_path = Paths.get(path.toString(),filePart.filename());
//                    String fullFileName = filePart.filename();
//                    filePart.transferTo(file_path);
//
//                    // 2. 您的操作返回值
//                    Mono<Boolean> aBoolean =  function.apply(fullFileName);
//                    return aBoolean.flatMap(x -> {
//                        if(!x){
//                            return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
//                                    .body( Mono.just(FormObjUtil.okResult("文件上传","上传","","上传失败")), String.class);
//                        }
//                        else{
//                            ///3. 填写业务检测 zzs 2019-07-30
//                            try{
//                                SecurityLog.RecordQuerylog( dbClient, request, "上传文件", "", fullFileName).then();
//                            }catch (Exception er){;}
//                            return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
//                                    .body( Mono.just(FormObjUtil.okResult("文件上传","上传","","上传成功")), String.class);
//                        }
//                    });
//                });
//    }


    public Mono<ServerResponse> zupload_RBese64(ServerRequest request, Path path, Function<String, Mono<String>> function){
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            Mono.error(e);
        }
        return request.multipartData()
                .map( MultiValueMap::toSingleValueMap)
                .flatMap(part -> Mono.just((FilePart) part.get("file")))
                .cast( FilePart.class)
                .flatMap(filePart -> {
                    // 1. 保存上传文件；文件名一致则覆盖
                    // LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"."+StringUtils.getFilenameExtension(fullFileName)
                    String fullFileName = filePart.filename();
                    String n_fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"."+ StringUtils.getFilenameExtension(fullFileName);
//                    Path file_path = Paths.get(path.toString(),fullFileName);
                    Path file_path = Paths.get(path.toString(),n_fileName);
                    filePart.transferTo(file_path);

                    // 2. 您的操作返回值
                    Mono<String> aBoolean =  function.apply(n_fileName);
                    return aBoolean.flatMap(x -> {
//                        log.info("文件上传结果:"+x);
                        if(x.isEmpty() || x.equals("NoPicture")){
                            return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
                                    .body( Mono.just(FormObjUtil.errorResult("文件上传","上传","","抱歉，图片上传失败，请稍后再试")), String.class);
                        }
                        else if(x.equals("NotPicture")){
                            return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
                                    .body( Mono.just(FormObjUtil.errorResult("文件上传","上传","","抱歉，系统检测到非法图片，请重新上传!")), String.class);
                        }
                        else if(x.equals("MaxSize") ){
                            return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
                                    .body( Mono.just(FormObjUtil.errorResult("文件上传","上传","","抱歉，上传图片大小超过200k!")), String.class);
                        }
                        else{
                            ///3. 填写业务检测 zzs 2019-07-30
                            try{
                                SecurityLog.RecordQuerylog( dbClient, request, "上传文件", "", n_fileName).then();
                            }catch (Exception er){;}
//                            log.info("Base64:=="+file_path.toString());
                            return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
                                    .body( Mono.just(FormObjUtil.okResult("文件上传","上传","获取Base64", ImgSer.getImgStr(file_path.toString()))), String.class);
                        }
                    });
                });
    }




    /**
     * 封装方法：文件下载
     * 文件存放路径：path
     *
     * @param path
     * @return
     */
    public Mono<ServerResponse> zdownload(Path path){
        String tpath = path.toString();
        String filename = "";
        if(tpath.indexOf("/") > -1) {
            filename = tpath.substring(tpath.lastIndexOf("/") + 1);
        }
        else if(tpath.indexOf("\\") > -1){
            filename = tpath.substring(tpath.lastIndexOf("\\") + 1);
        }
        //1.  解决中文名被替换为下划线
        filename = Utilities.setEncodevalue(filename);

        //2. 以Resource形式返回文件
        return ServerResponse.ok()
                .header( HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=" + filename)
                .body( BodyInserters.fromResource(new FileSystemResource(path.toString())))
                .switchIfEmpty( Mono.empty());
    }

    //零拷贝方法返回文件流
    public Mono<ServerResponse> zdownloadByWriteWith(Path path, String filename) {
        //1.  解决中文名被替换为下划线
        filename = Utilities.setEncodevalue(filename);
        Resource resource = new FileSystemResource(path.toString());
        File file = null;
        try {
            file = resource.getFile();
        } catch (IOException e) {
            log.error("下载文件出错:{}", e.getMessage());
            return ServerResponse.noContent().build();
        }
        File finalFile = file;
        String filenameDown = Utilities.getDecodeValue(filename);
        return ServerResponse.ok()
                .header( HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=" + filenameDown)
                .contentType(MediaType.MULTIPART_MIXED)
                .body((p, a) -> {
                    ZeroCopyHttpOutputMessage resp = (ZeroCopyHttpOutputMessage) p;
                    return resp.writeWith(finalFile, 0, finalFile.length());
                });
    }

    /**
     * 封装方法：文件下载
     * 文件存放路径：path
     *
     * @param path
     * @return
     */
    public Mono<ServerResponse> zdownload(Path path,String filename){
        //1.  解决中文名被替换为下划线
        filename = Utilities.setEncodevalue(filename);

        //2. 以Resource形式返回文件
        return ServerResponse.ok()
                .header( HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=" + filename)
                .body( BodyInserters.fromResource(new FileSystemResource(path.toString())))
                .switchIfEmpty( Mono.empty());
    }

    public Mono<ServerResponse> zdownloadByProgress(Path path,String filename){
        //1.  解决中文名被替换为下划线
        filename = Utilities.setEncodevalue(filename);
        Resource resource = new FileSystemResource(path.toString());
        File file = null;
        try {
            file = resource.getFile();
        } catch (IOException e) {
            log.error("下载文件出错：{}", e.getMessage());
            return ServerResponse.noContent().build();
        }
        File finalFile = file;
        return ServerResponse.ok()
                .header( HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=" + filename)
                .body((p, a) -> {
                    p.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    ZeroCopyHttpOutputMessage resp = (ZeroCopyHttpOutputMessage) p;
                    return resp.writeWith(finalFile, 0, finalFile.length());
                });
    }

    /**
     * 封装方法：文件下载
     * 文件存放路径：path
     *
     * @param path
     * @return
     */
    public Mono<ServerResponse> zdownloadthenrm(Path path){
        String tpath = path.toString();
        String filename = "";
        if(tpath.indexOf("/") > -1) {
            filename = tpath.substring(tpath.lastIndexOf("/") + 1);
        }
        else if(tpath.indexOf("\\") > -1){
            filename = tpath.substring(tpath.lastIndexOf("\\") + 1);
        }
        //1.  解决中文名被替换为下划线
        filename = Utilities.setEncodevalue(filename);

        ByteArrayResource byteArrayResource = readFileByte(path);
        // 2.   删除文件
        deleteFile(path);

        //3.    以Resource形式返回文件
        return ServerResponse.ok()
                .header( HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=" + filename)
                .body( BodyInserters.fromResource(byteArrayResource))
                .switchIfEmpty( Mono.empty());
    }

    /**
     * 封装方法：Excel数据导入数据库
     * Function<String,Boolean>  文件名  导入操作是否成功
     * 临时文件存放路径：Paths.get("downLoadFiles")
     *
     * @param request
     * @param  t
     * @return
     */
    public <T> Mono<ServerResponse> zExcelToSql(ServerRequest request, Function<String, Mono<T>> t){
        dpath = Paths.get(DownLoadFiles);
        Map<String,String> sFullParams = request.queryParams().toSingleValueMap();
        try {
            Files.createDirectories(dpath);
        } catch (IOException e) {
            Mono.error(e);
        }
        return request.multipartData()
                .map( MultiValueMap::toSingleValueMap)
                .flatMap(part -> Mono.just((FilePart) part.get("file")))
                .cast( FilePart.class)
                .flatMap(filePart -> {
                    // 1. 保存导入文件
                    final String extension = getExtension(filePart.filename()).get();
                    final String baseName = getBaseName(filePart.filename()).get();
                    final String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    String FileName = String.format("%s-%s.%s", baseName, format, extension);
                    final Path file_path = Paths.get(dpath.toString(), FileName);
                    filePart.transferTo(file_path);

                    // 2. 解析Excel导入数据库(您的操作)
                    Mono<T> apply = t.apply(FileName);
                   return apply.flatMap(x -> {
//                       log.info("x："+x + "，"+ (x instanceof Boolean));
                       if(x instanceof Boolean){
                           Boolean apply_bool = (Boolean)x;
                           if(!apply_bool){
                               deleteFile(request,Paths.get(dpath.toString()), FileName);
                               return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
                                       .body( Mono.just(FormObjUtil.okResult("Excel导入","导入解析","","导入失败")), String.class);
                           }
                           deleteFile(request,Paths.get(dpath.toString()), FileName);
                           return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
                                   .body( Mono.just(FormObjUtil.okResult("Excel导入","导入","","导入成功")), String.class);
                       }
                       else if(x instanceof String){
                           String apply_str = (String)x;
                           if(apply.equals("0")){
                               deleteFile(request,Paths.get(dpath.toString()), FileName);
                               return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
                                       .body( Mono.just(FormObjUtil.okResult("Excel导入","导入解析","","导入失败")), String.class);
                           }
                           else if (apply.equals("1")) {
                               deleteFile(request,Paths.get(dpath.toString()), FileName);
                               return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
                                       .body( Mono.just(FormObjUtil.okResult("Excel导入","导入","","导入成功")), String.class);
                           }
                           else {
                               deleteFile(request,Paths.get(dpath.toString()), FileName);
                               return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
                                       .body( Mono.just(FormObjUtil.okResult("Excel导入","导入","",apply_str)), String.class);
                           }
                       }
                       else{
                           deleteFile(request,Paths.get(dpath.toString()), FileName);
                           return ServerResponse.ok().contentType( MediaType.APPLICATION_JSON)
                                   .body( Mono.just(FormObjUtil.okResult("Excel导入","导入","","传入参数类型错误")), String.class);
                       }
                    });
                });
    }


    /**
     * 封装方法：数据库数据导出Excel
     * 临时文件存放路径：Paths.get("downLoadFiles")
     *
     * @param sqlText sql
     * @param title 导出文件的名字
     * @return
     */
    public Mono<ServerResponse> zToExcel(String sqlText, String title){
        dpath = Paths.get(DownLoadFiles);
        try {
            Files.createDirectories(dpath);
        } catch (IOException e) {
            Mono.error(e);
        }
        Mono<List<Map<String, Object>>> listMap = ZZSR2DBCService.getListMap( dbClient, sqlText);
        return  sqlListMapToExcel(listMap,title);
    }

    public Mono<ServerResponse> zToExcel_Judge(String sqlText, Map<String,String> sparas, String title,String matchName, String mbtname, String judgename){
        dpath = Paths.get(DownLoadFiles);
        try {
            Files.createDirectories(dpath);
        } catch (IOException e) {
            Mono.error(e);
        }
        Mono<List<Map<String, Object>>> listMap  = ZZSR2DBCService.getListMap( dbClient, sqlText, new HashMap<String,Object>(  sparas ));
        dpath = Paths.get(DownLoadFiles);
        String fileName =  String.format("%s-%s.%s", title, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")), "xls");
        Path path_file = Paths.get(dpath.toString(), fileName);

        return listMap.flatMap( lm ->{
            MSExcelSer.zzsSqlListMapToExcelFile_Judge(path_file.toString(),lm, matchName, mbtname, judgename);
            // 3.2.1  nio -> 文件转为byte[]
            ByteArrayResource byteArrayResource = readFileByte(path_file);
            // 3.2.2 删除文件
            deleteFile(path_file);
            return   ServerResponse.ok()
                    .header( HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=" + Utilities.setEncodevalue(fileName))
                    .contentType( MediaType.MULTIPART_FORM_DATA)
                    .body( BodyInserters.fromResource(byteArrayResource));
        });
    }

    public Mono<ServerResponse> zToExcel(String sqlText, Map<String,String> sparas, String title){
        dpath = Paths.get(DownLoadFiles);
        try {
            Files.createDirectories(dpath);
        } catch (IOException e) {
            Mono.error(e);
        }
        Mono<List<Map<String, Object>>> listMap  = ZZSR2DBCService.getListMap( dbClient, sqlText, new HashMap<String,Object>(  sparas ));
        return  sqlListMapToExcel(listMap,title);
    }

    public Mono<ServerResponse> sqlListMapToExcel(Mono<List<Map<String, Object>>> listmap, String title){
        dpath = Paths.get(DownLoadFiles);
        String fileName =  String.format("%s-%s.%s", title, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")), "xls");
        Path path_file = Paths.get(dpath.toString(), fileName);

        return listmap.flatMap( lm ->{
            MSExcelSer.zzsSqlListMapToExcelFile(path_file.toString(),lm);
            // 3.2.1  nio -> 文件转为byte[]
            ByteArrayResource byteArrayResource = readFileByte(path_file);
            // 3.2.2 删除文件
            deleteFile(path_file);
            return   ServerResponse.ok()
                    .header( HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=" + Utilities.setEncodevalue(fileName))
                    .contentType( MediaType.MULTIPART_FORM_DATA)
                    .body( BodyInserters.fromResource(byteArrayResource));
        });
    }


    public static String getFormatValue(Object value) {
        DecimalFormat df2 = new DecimalFormat("0.00");  //格式化数字
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 格式化日期
        if (value instanceof LocalDateTime)
            value = LocalDateTime.parse(value.toString(), dtf2);
        else if (value instanceof LocalDate)
            value = LocalDateTime.parse(value.toString(), dtf2);
        else if (value instanceof Float)
            value = df2.format(value);
        else if (value instanceof Double)
            value = df2.format(value);
        else
            value = value;

        return value !=null ? value.toString() : "";
    }


    public String getFormatVal(String columnTypeName,Object value){
        DecimalFormat df2 = new DecimalFormat("0.00");  //格式化数字
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 格式化日期
        switch (columnTypeName) {
            case "money":
                value = df2.format(value);
                break;
            case "datetime":
                value = LocalDateTime.parse(value.toString(), dtf2).toString();
                break;
            default:
                break;
        }
        return value!=null?value.toString():"";
    }


    public static int arr_IndexOf(String[] arr,String... key){
        for(int i = 0;i<arr.length;i++){
            for (int k = 0;k<key.length;k++) {
                if (key[k].trim().equals(arr[i].trim())) {
                    return i;
                }
            }
        }
        return -1;
    }


    public ByteArrayResource readFileByte(Path path){
        try {
            byte[] bFile = Files.readAllBytes(path);
            return new ByteArrayResource(bFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public  boolean deleteFile(Path path){
        try {
            return FileSystemUtils.deleteRecursively(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public  Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
    public  Optional<String> getBaseName(String filename) {
        int startIndex = filename.lastIndexOf('\\')>=0? filename.lastIndexOf('\\')+1:(filename.lastIndexOf('/')>=0?
                filename.lastIndexOf('/')+1:0);

        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(startIndex,filename.lastIndexOf(".")));
    }

    public boolean deleteFile(ServerRequest request, Path path, String fileName){
        dpath = Paths.get(DownLoadFiles);
        // 3. 填写业务检测 zzs 2019-07-30
//        try{
//            SecurityLog.RecordQuerylog(dbClient, request, "上传文件", "", fileName);
//        }catch (Exception er){;}
        myApplicationEventPublisher.publishEvent( new BasicLogObj( dbClient,request,"上传文件","",fileName));
        // 4. 导入完成删除文件
        return deleteFile(Paths.get(dpath.toString(), fileName));

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        myApplicationEventPublisher = applicationEventPublisher;
    }
}
