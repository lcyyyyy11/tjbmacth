package com.zzs.TJBmatch.handlers.publichandler;

import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
import com.zzs.TJBmatch.services.MSWordSer;
import com.zzs.TJBmatch.services.Utilities;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class CreateWordHandler {

    // Excel导入导出临时文件路径
    final Path dpath = Paths.get(System.getProperty("user.dir"),"Temp");

    private DatabaseClient dbCLient;
    public CreateWordHandler(@Qualifier("DBClient") DatabaseClient client){
        this.dbCLient = client;
    }

    // =================专家评分表=================
    public Mono<ServerResponse> createJudgeScoreReport(ServerRequest request){
        Map<String, String> sFullParams = request.queryParams().toSingleValueMap();
        String mpsname = sFullParams.getOrDefault("mpsname","");
        String judgename = sFullParams.getOrDefault("judgename","");
        String matchName = sFullParams.getOrDefault("matchName","");

        String sqlText = "SELECT matchballot.xsbh, " +
                " IFNULL(ROUND(A.mplayscore,2),'') AS pdfscore, " +
                " IFNULL(ROUND(B.mplayscore,2),'') AS coursescore,  " +
                " ROUND(IFNULL(F.score,0),2) AS score ,matchteachmaterial.mname  " +
                " FROM matchballot  " +
                " LEFT JOIN (     " +
                " SELECT matchplayerscore.mplayid, matchplayerscore.mptid, matchplayerscore.matid, matchplayerscore.mbtid, matchplayerscore.mpsid,   " +
                " matchplayerscore.mplayscore, matchplayerscore.scorelinkid,matchplayerscore.judsubmit   " +
                " FROM matchplayerscore WHERE matchplayerscore.matid = :matid AND  matchplayerscore.mpsid = :mpsid     " +
                " AND matchplayerscore.judge = :judgeno AND matchplayerscore.scorelinkid = 'A'    " +
                " ) AS A ON matchballot.mptid = A.mptid AND matchballot.mbtid = A.mbtid   " +
                " AND matchballot.mpsid = A.mpsid   AND matchballot.matid = A.matid   " +
                " LEFT JOIN (   " +
                " SELECT matchplayerscore.mplayid, matchplayerscore.mptid, matchplayerscore.matid, matchplayerscore.mbtid, matchplayerscore.mpsid, " +
                " matchplayerscore.mplayscore, matchplayerscore.scorelinkid,matchplayerscore.judsubmit    " +
                " FROM matchplayerscore WHERE matchplayerscore.matid = :matid AND  matchplayerscore.mpsid = :mpsid    " +
                " AND matchplayerscore.judge = :judgeno AND matchplayerscore.scorelinkid = 'B'   " +
                " ) AS B ON matchballot.mptid = B.mptid AND matchballot.mbtid = B.mbtid    " +
                " AND matchballot.mpsid = B.mpsid  AND matchballot.matid = B.matid   " +
                " LEFT JOIN (    " +
                " SELECT T.matid, T.mptid, T.mbtid,T.mpsid, SUM(T.mplayscore * U.ratio) AS score FROM (   " +
                " SELECT matchplayerscore.mptid, matchplayerscore.matid, matchplayerscore.mbtid,matchplayerscore.mpsid, " +
                " matchplayerscore.mplayscore, matchplayerscore.scorelinkid  " +
                " FROM matchplayerscore WHERE matchplayerscore.matid = :matid    " +
                " AND matchplayerscore.judge = :judgeno  " +
                " GROUP BY matchplayerscore.mptid, matchplayerscore.matid, matchplayerscore.mbtid, matchplayerscore.mpsid, matchplayerscore.mplayscore, matchplayerscore.scorelinkid  " +
                " ) AS T INNER JOIN (  " +
                " SELECT matchbroadtype.matid, matchbroadtype.mbtid,  " +
                " IFNULL(SUM(matchcriteriaaspect.maxscore) / 100, 0) AS ratio, matchsubcriteria.scorelinkid  " +
                " FROM matchbroadtype   " +
                " INNER JOIN matchsubcriteria ON matchbroadtype.mbtid = matchsubcriteria.mbtid  " +
                " INNER JOIN matchcriteriaaspect ON matchcriteriaaspect.mbtid = matchsubcriteria.mbtid  " +
                " AND matchcriteriaaspect.mscid = matchsubcriteria.mscid    " +
                " WHERE matchbroadtype.matid = :matid  " +
                " GROUP BY matchcriteriaaspect.mbtid, matchsubcriteria.scorelinkid  " +
                " ) AS U ON T.matid = U.matid AND T.mbtid = U.mbtid AND T.scorelinkid = U.scorelinkid   " +
                " GROUP BY T.mptid, T.mbtid, T.matid ,T.mpsid   " +
                " ) " +
                " AS F ON matchballot.mptid = F.mptid AND matchballot.mbtid = F.mbtid   " +
                " AND matchballot.mpsid = F.mpsid  AND matchballot.matid = F.matid  " +
                " LEFT JOIN matchteachmaterial ON matchballot.mptid = matchteachmaterial.mptid  " +
                " AND matchballot.matid = matchteachmaterial.matid  " +
                " AND matchteachmaterial.choosed = '1' " +
                " INNER JOIN matchprojectperson ON matchprojectperson.mptid =  matchballot.mptid  " +
                " AND matchprojectperson.matid =  matchballot.matid  " +
                " AND matchprojectperson.mbtid =  matchballot.mbtid  " +
                " WHERE matchballot.matid = :matid AND matchballot.mpsid = :mpsid  AND  " +
                " matchprojectperson.mpstatus >= 5  " +
                " ORDER BY matchballot.xsbh  ;";

        return ZZSR2DBCService.getListMap(dbCLient, sqlText, new HashMap<>(sFullParams))
                .flatMap(f1 -> {
                    MSWordSer wordSer = new MSWordSer();
                    XWPFDocument document = wordSer.createXWPFDocument();
                    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
                    String fileName = judgename+"-评分表-"+ time +".docx";
                    String savePath = Paths.get(dpath.toString(),time, fileName).toString();
                    wordSer.createTitle1(document,matchName+ "专家评分表");
                    wordSer.createParagraph(document,"");
                    wordSer.createParagraph(document,"答辩组别："+mpsname + "              " + "专家姓名："+judgename, ParagraphAlignment.CENTER);
                    wordSer.createParagraph(document,"");
                    int len = f1.size();
                        XWPFTable tableNoTitle = wordSer.createTableNoTitle(document, len + 1, 5);
                        List<List<Object>> tableDatas = new ArrayList<>();
                        List<Object> list = new ArrayList<>();
                        list.add("编号");
                        list.add("题目名称");
                        list.add("教学设计");
                        list.add("教学展示");
                        list.add("总分");
                        tableDatas.add(list);
                        for (int i = 0; i < len; i++) {
                            list = new ArrayList<>();
                            list.add(f1.get(i).get("xsbh"));
                            list.add(f1.get(i).get("mname"));
                            list.add(f1.get(i).get("pdfscore"));
                            list.add(f1.get(i).get("coursescore"));
                            list.add(f1.get(i).get("score"));
                            tableDatas.add(list);
                        }
                        wordSer.fillTableData(tableNoTitle,tableDatas);
                    wordSer.createParagraph(document,"");
                    wordSer.createParagraph(document,"专家签名：", ParagraphAlignment.LEFT, 5870);

                    wordSer.createParagraph(document,"年   月   日", ParagraphAlignment.LEFT, 5670);

                    try {
                        wordSer.save(document,savePath);
                    } catch (IOException e) {
                        throw new ValidationException(RtnEnum.GENERAL_ERROR,"文件生成失败！");
                    }
                    return createFile(fileName,time);
                });
    }



    private  Mono<ServerResponse> createFile(String fileName,String dir) {

        Path dirPath = Paths.get(dpath.toString(), dir);
        Path downloadPath = Paths.get(dpath.toString(), dir, fileName);


        String tpath = downloadPath.toString();
        String filename = "";
        if (tpath.contains("/")) {
            filename = tpath.substring(tpath.lastIndexOf("/") + 1);
        } else if (tpath.contains("\\")) {
            filename = tpath.substring(tpath.lastIndexOf("\\") + 1);
        }
        //1.  解决中文名被替换为下划线
        filename = Utilities.setEncodevalue(filename);

        BufferedInputStream fis = null;
        ByteArrayOutputStream toClient = null;
        File file = new File(downloadPath.toString());
        byte[] bytes = null;
        try {
            if (file.exists()) {
                fis = new BufferedInputStream(new FileInputStream(downloadPath.toString()));
                toClient = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int i = -1;
                while ((i = fis.read(buffer)) != -1) { //不能一次性读完，大文件会内存溢出（不能直接fis.read(buffer);）
                    toClient.write(buffer, 0, i);

                }
                bytes = toClient.toByteArray();
            }
        } catch (Exception ex) {
            ;
        } finally {
            try {
                if (null != fis) {
                    fis.close();
                }
            } catch (IOException ex) {
                ;
            }
            try {
                if (null != toClient) {
                    toClient.flush();
                    toClient.close();
                }
            } catch (IOException ex) {
                ;
            }
            try {
                FileSystemUtils.deleteRecursively(dirPath);
            } catch (IOException e) {
                ;
            }
            if (null == bytes) {
                return ServerResponse.ok() //200
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(BodyInserters.fromValue(""));
            }
            return ServerResponse.ok() //200
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(BodyInserters.fromValue(bytes));
        }
    }




}
