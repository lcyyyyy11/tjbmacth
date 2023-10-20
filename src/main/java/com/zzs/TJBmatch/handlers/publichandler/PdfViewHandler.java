package com.zzs.TJBmatch.handlers.publichandler;


import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
import com.zzs.TJBmatch.services.Utilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class PdfViewHandler {

    @Value("${TeachSkillFiles.MatchPersonMaterials}")
    private String matchPersonMaterials;

    @Value("${TeachSkillFiles.UserManual}")
    private String userManual;



    // =================预览教学材料PDF===================
    public Mono<ServerResponse> previewMatchMaterialPDF(ServerRequest request){
        Map<String,String> sFullParams = request.queryParams().toSingleValueMap();
        String mptid = sFullParams.getOrDefault("mptid", "");
        if (StringUtils.isEmpty(mptid)) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败，请稍后再试！");
        }
        String matid = sFullParams.getOrDefault("matid", "");
        if (StringUtils.isEmpty(matid)) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败，请稍后再试！");
        }
        String[] location = Utilities.searchLocation(matid);
        if (null == location) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败！");
        }
        String filepath = sFullParams.getOrDefault("filepath", "");
        if (StringUtils.isEmpty(filepath)) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败，请稍后再试！");
        }
        String filename = sFullParams.getOrDefault("filename","");
        if (StringUtils.isEmpty(filename)) {
            filename = filepath;
        }
        Path sPath = Paths.get(matchPersonMaterials, location[0], location[1], location[2], location[3], mptid, filepath);
        return getFileStream(sPath, filename);
    }


    // ====================预览用户使用手册=====================================
    public Mono<ServerResponse> previewUserManualPDF(ServerRequest request){
        return request.session().flatMap(f -> {
            String role = f.getAttribute("UserRole");
            if (StringUtils.isEmpty(role)) {
                throw new ValidationException(RtnEnum.NO_RIGHT,"抱歉，您无操作权限，请和管理员联系！");
            }
            int roleNo = Integer.parseInt(role);
            StringBuilder sb = new StringBuilder();
            if (roleNo <= 4) {
                sb.append("主办方承办方操作指南.pdf");
            }
            else if (roleNo == 6) {
                sb.append("市州联络员操作指南.pdf");
            }
            else if (roleNo == 7) {
                sb.append("评审专家操作指南.pdf");
            }
            if (sb.length() == 0) {
                throw new ValidationException(RtnEnum.NO_RIGHT,"抱歉，您无操作权限，请和管理员联系！");
            }
            Path sPath = Paths.get(userManual, sb.toString());
            return getFileStream(sPath, sb.toString());
        });
    }

    private Mono<ServerResponse> getFileStream(Path sPath, String fname) {
        File file = sPath.toFile();
        String filename = Utilities.setEncodevalue(fname);
        BufferedInputStream fis = null;
        ByteArrayOutputStream toClient = null;
        //以二进制流的形式传回文件
        byte[] bytes = null;
        try {
            if (file.exists()) {
                fis = new BufferedInputStream(new FileInputStream(sPath.toString()));
                toClient = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int i = -1;
                while ((i = fis.read(buffer)) != -1) { //不能一次性读完，大文件会内存溢出（不能直接fis.read(buffer);）
                    toClient.write(buffer, 0, i);
                }
                bytes = toClient.toByteArray();
            }
        } catch (Exception e){
            ;
        } finally {
            try{
                if (null != toClient) {
                    toClient.close();
                }
                if (null != fis) {
                    fis.close();
                }
            } catch (IOException e) {
                ;
            }
        }
        return ServerResponse.ok() //200
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(BodyInserters.fromValue(bytes == null ? "" : bytes));
    }

}
