package com.zzs.TJBmatch.router.userAndSystem;

import com.zzs.TJBmatch.handlers.userAndSystem.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class UserAndSystemRouter {

    //系统用户信息
    @Bean
    public RouterFunction<ServerResponse> UserInfo(UserInfoHandler handler){
        return RouterFunctions.route().path("/ps/userAndSystem/userInfo",
                b1->b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        b2->b2
                                .GET("/gridSearch",handler::gridSearch)
                                .POST("/gridAdd",handler::gridAdd)
                                .POST("/gridEdit",handler::gridEdit)
                                .POST("/gridDelete",handler::gridDelete)
                                .GET("/selUserNo",handler::selUserNo)
                                .GET("/selPid",handler::selPid)
                                .POST("/setValid",handler::setValid)
                                .POST("/setInvalid",handler::setInvalid)
                                .POST("/setOffline",handler::setOffline)
//                                .POST("/resetPwd",handler::resetPwd)
                )
        )
                .build();
    }

    // 角色权限
    @Bean
    public RouterFunction<ServerResponse> MUserRoleSet(UserRoleSetHandler handler) {
        return RouterFunctions.route().path("/ps/userAndSystem/UserRoleSet",
                b1 -> b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        b2 -> b2
                                // 第一面板
                                .GET("/gridSearch",handler::gridSearch)
                                .POST("/gridEdit",handler::gridEdit)
                                .POST("/gridAdd",handler::gridAdd)
                                .POST("/gridDelete",handler::gridDelete)
                                .POST("/selRole",handler::selRole)
                                // 第二面板
                                .GET("/grid2Search",handler::grid2Search)
                                .POST("/roleFuncEdit",handler::roleFuncEdit)
                )
        )
                .build();
    }

    //登录日志
    @Bean
    public RouterFunction<ServerResponse> LoginRecords(LoginRecordsHandler handler){
        return RouterFunctions.route().path("/ps/userAndSystem/LoginRecords",
                b1->b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        b2->b2
                                .GET("/gridSearch",handler::gridSearch)
                )
        )
                .build();
    }

    //变更日志
    @Bean
    public RouterFunction<ServerResponse> SafeRecords(SafeRecordsHandler handler){
        return RouterFunctions.route().path("/ps/userAndSystem/SafeRecords",
                b1->b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        b2->b2
                                .GET("/gridSearch",handler::gridSearch)
                                .POST("/Export",handler::export)
                )
        )
                .build();
    }

    //市州信息库
    @Bean
    public RouterFunction<ServerResponse> CityInfoRouter(CityInfoHandler handler){
        return RouterFunctions.route().path("/ps/userAndSystem/CityInfo",
                        b1->b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                b2->b2
                                        .GET("/gridSearch",handler::gridSearch)
                                        .POST("/gridEdit",handler::gridEdit)
                        )
                )
                .build();
    }


    //中小学信息库
    @Bean
    public RouterFunction<ServerResponse> CollegeLeaderRouter(CollegeLeaderHandler handler){
        return RouterFunctions.route().path("/ps/userAndSystem/CollegeLeader",
                        b1->b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                b2->b2
                                        .GET("/gridSearch",handler::gridSearch)
                                        .POST("/gridAdd",handler::gridAdd)
                                        .POST("/gridEdit",handler::gridEdit)
                                        .POST("/gridDelete",handler::gridDelete)
                        ).nest(RequestPredicates.accept(MediaType.TEXT_PLAIN), b2 -> b2
                                .POST("/selMatchLeaderPid",handler::selMatchLeaderPid)
                        )
                )
                .build();
    }


}
