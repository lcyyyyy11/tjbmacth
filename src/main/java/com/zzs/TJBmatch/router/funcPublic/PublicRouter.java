package com.zzs.TJBmatch.router.funcPublic;

import com.zzs.TJBmatch.handlers.publichandler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class PublicRouter {

    @Bean
    RouterFunction<ServerResponse> getOption(GetOptions handler) {
        return  RouterFunctions.route()
                .path("/ps/",b1 -> b1
                        .nest(RequestPredicates.accept(MediaType.TEXT_PLAIN), b2 -> b2
                                .GET("GetOptions" ,handler::doGetOptions))
                )
                .build();
    }


    @Bean
    RouterFunction<ServerResponse> SearchSingleValRouter(SearchSingleValHandler handler) {
        return  RouterFunctions.route()
                .path("/ps/",b1 -> b1
//                        .nest(RequestPredicates.accept(MediaType.TEXT_PLAIN), b2 -> b2
                                .GET("searchMatchTime", handler::searchMatchTime)
                                .GET("selAssignMbtid", handler::selAssignMbtid)
                                .GET("searchTimeContest" ,handler::searchTimeContest)
                                .POST("selMatchPid" ,handler::selMatchPid)
                                .POST("selPersonStatusName",handler::selPersonStatusName)
                                .POST("selPersonStatus",handler::selPersonStatus)
                                .GET("selTeachNum", handler::selTeachNum)

                                .GET("getCurSessionGroup", handler::getCurSessionGroup)
                                .GET("getCurJudgeSessionGroup", handler::getCurJudgeSessionGroup)
                                .GET("selBallot", handler::selBallot)
                                .GET("selMatchType", handler::selMatchType)
                                .GET("beginMark", handler::beginMark)
                                .GET("getUnitName", handler::getUnitName)
                                .GET("getUnitUidByPid", handler::getUnitUidByPid)
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_STREAM_JSON), b2 -> b2
                                .POST("searchPage" ,handler::searchPage)
                                .GET("getSessionGroup", handler::getSessionGroup)
                                .GET("getModuleSchedule", handler::getModuleSchedule)
                                .GET("selDetailTotalType",handler::selDetailTotalType)
                                .GET("getJudgeSessionGroup",handler::getJudgeSessionGroup)
                                .GET("getLiveBroadType", handler::getLiveBroadType)
                                .GET("getUnitNumber", handler::getUnitNumber)
                                .GET("getSessionTime", handler::getSessionTime)
                                .GET("selHasModule", handler::selHasModule)
                                .GET("getIsPublish", handler::getIsPublish)

                        )
                )
                .build();
    }

    // 图片裁剪
    @Bean
    RouterFunction<ServerResponse> PicCropperRouter(PicCropperHandler handler)
    {
        return RouterFunctions.route().path("/ps/PicCropper",
                b1->b1.nest(RequestPredicates.accept(MediaType.APPLICATION_STREAM_JSON),
                        b2->b2
                                .POST("/picSearch",handler::picSearch)
                                .POST("/picSave",handler::picSave)
                )
        )
                .build();
    }
    //pdf预览
    @Bean
    public RouterFunction<ServerResponse> PdfViewRouter(PdfViewHandler handler){
        return RouterFunctions.route().path("/ps/funcPublic/PdfView",
                b1->b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        b2->b2
                                .GET("/previewMatchMaterialPDF",handler::previewMatchMaterialPDF)
                                .GET("/previewUserManualPDF",handler::previewUserManualPDF)
                )
        )
                .build();
    }

    //pdf预览
    @Bean
    public RouterFunction<ServerResponse> CreateWordRouter(CreateWordHandler handler){
        return RouterFunctions.route().path("/ps/funcPublic/CreateWord",
                b1->b1.nest(RequestPredicates.accept(MediaType.APPLICATION_OCTET_STREAM),
                        b2->b2
                                .POST("/createJudgeScoreReport",handler::createJudgeScoreReport)
                )
        )
                .build();
    }




}
