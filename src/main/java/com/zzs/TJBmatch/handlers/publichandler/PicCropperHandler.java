package com.zzs.TJBmatch.handlers.publichandler;


import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
import com.zzs.TJBmatch.services.ImgSer;
import com.zzs.TJBmatch.services.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

@Component
public class PicCropperHandler {

    private Logger logger = LoggerFactory.getLogger(PicCropperHandler.class);

    @Value("${TeachSkillFiles.MatchPic}")
    private String matchPicDir;

    @Value("${TeachSkillFiles.BannerPic}")
    private String bannerPic;

    @Value("${TeachSkillFiles.ActivityPic}")
    private String activityPic;

    @Value("${TeachSkillFiles.MatchPersonPic}")
    private String matchPersonPic;

    @Value("${TeachSkillFiles.MatchLiveQrCodes}")
    private String matchLiveQrCodes;

    public Mono<ServerResponse> picSearch(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(map -> {
            Map<String, String> stringMap = map.toSingleValueMap();
            String sDocCode = stringMap.getOrDefault("sDocCode","");  // 模块标识
            String sPicTitle = stringMap.getOrDefault("sPicTitle",""); // 文件名
            String matid = stringMap.getOrDefault("matid",""); // 赛事活动主键

            if (StringUtils.isEmpty(sDocCode) || StringUtils.isEmpty(sPicTitle))
                throw new ValidationException(RtnEnum.GENERAL_ERROR,"参数获取失败，请稍后再试！");

            if (!sDocCode.equals("MatchInfo") && StringUtils.isEmpty(matid))
                throw new ValidationException(RtnEnum.GENERAL_ERROR,"参数获取失败，请稍后再试！");

            String pathfile = getDirectory(matid,sDocCode);

            if(pathfile.equals( "-1"))
                throw new ValidationException(RtnEnum.GENERAL_ERROR,"参数获取失败，请稍后再试！");
            pathfile = pathfile + File.separator + sPicTitle.trim();


            String base64Str = "";
            byte[] imgbyte = null;
            try {
                Path path = Paths.get(pathfile);
                imgbyte = Files.readAllBytes(path);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            base64Str = Base64.getEncoder().encodeToString(imgbyte);
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                    .body(BodyInserters.fromValue(base64Str));
        });
    }

    public Mono<ServerResponse> picSave(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(map -> {
            Map<String, String> stringMap = map.toSingleValueMap();
            String sDocCode = stringMap.getOrDefault("sDocCode","");  // 模块标识
            String sPicTitle = stringMap.getOrDefault("sPicTitle",""); // 文件名
            String matid = stringMap.getOrDefault("matid",""); // 赛事活动
            String base64Str = stringMap.get("base64Str"); // 新的base64
            if (StringUtils.isEmpty(sDocCode) || StringUtils.isEmpty(sPicTitle) || StringUtils.isEmpty(base64Str))
                throw new ValidationException(RtnEnum.GENERAL_ERROR,"参数获取失败，请稍后再试！");

            if (!sDocCode.equals("MatchInfo") && StringUtils.isEmpty(matid))
                throw new ValidationException(RtnEnum.GENERAL_ERROR,"参数获取失败，请稍后再试！");

            String pathfile = getDirectory(matid,sDocCode);

            String thumfile = pathfile + File.separator + "thumbnail" + File.separator + sPicTitle.trim();
            if(pathfile.equals( "-1"))
                throw new ValidationException(RtnEnum.GENERAL_ERROR,"参数获取失败，请稍后再试！");

            pathfile = pathfile + File.separator + sPicTitle.trim();
            boolean s = ImgSer.SaveImgByBase64(base64Str, pathfile);
            if(sDocCode.equals("skill") || sDocCode.equals("Carousel") || sDocCode.equals("style")|| sDocCode.equals("MatchPerson")){
                createThumnailImg(pathfile, thumfile);
            }
            else {
                try {
                    ImgSer.createThumnailFIle(pathfile, thumfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String res = "抱歉，保存失败，请稍后再试。";
            if(s){
                res = "保存成功";
            }
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                    .body(BodyInserters.fromValue( res));
        });
    }

    private String getDirectory(String matid,String moudle){
        Path path = null;
        String[] location = Utilities.searchLocation(matid);
        if (null == location) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取参数失败！");
        }
        switch (moudle){
            case "MatchInfo":
                path = Paths.get(matchPicDir);
                break;
            case "Carousel":
                path = Paths.get(bannerPic,location[0], location[1], location[2], location[3]);
                break;
            case "style":
                path = Paths.get(activityPic,location[0], location[1], location[2], location[3]);
                break;
            case "MatchPerson":
                path = Paths.get(matchPersonPic,location[0], location[1], location[2], location[3]);
                break;
            case "LiveQrCode":
                path = Paths.get(matchLiveQrCodes,location[0], location[1], location[2], location[3]);
                break;
            default:
                path = Paths.get("-1");
                break;

        }
        return path.toString();
    }

    private void createThumnailImg(String imgFile,String outFile){
        File img = new File(imgFile);
//        if (img.length() < 200*1024) return;
        Image image= null;
        try {
            image = javax.imageio.ImageIO.read(img);
            //按照指定的条件创建缩略图
            //width of the created image :创建图像的宽度(iwpx1、ihpx1)
            //the height to which to scale the image : 缩放图像的高度(iwpx2、ihpx2)
            //iwpx1 <= iwpx2 否则就会出现黑影
            ImgSer.createThumnailFIle(imgFile,outFile,image.getWidth(null),image.getHeight(null),image.getWidth(null),image.getHeight(null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
