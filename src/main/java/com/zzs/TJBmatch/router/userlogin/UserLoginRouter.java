package com.zzs.TJBmatch.router.userlogin;

import com.zzs.TJBmatch.filterfuncs.AddLogRecords;
import com.zzs.TJBmatch.handlers.userlogin.ChangePwdHandler;
import com.zzs.TJBmatch.handlers.userlogin.MainFrmHandler;
import com.zzs.TJBmatch.handlers.userlogin.ULoginVerifyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class UserLoginRouter {

    @Autowired
    private AddLogRecords addLogRecords;

    // ============================  登录 ========================================

    @Bean
    public RouterFunction<ServerResponse> ULogin(ULoginVerifyHandler handler) {
        return RouterFunctions.route().path("/ps/user/loginVerify",
                b1 -> b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        b2 -> b2
                                .POST("/login", handler::loginVerify)
                ))
                .after(addLogRecords)
                .build();
    }

    // ============================  登录 ========================================

    @Bean
    public RouterFunction<ServerResponse> ULoginVerify(ULoginVerifyHandler handler) {
        return RouterFunctions.route().path("/ps/user/loginVerify",
                b1 -> b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        b2 -> b2
                                .GET("/getValidCode",handler::getValidCode)
                                .POST("/validCode",handler::validCode)
                                .POST("/public_key",handler::getPublicKey)
//                                .GET("/checkUser", handler::checkUser)
                ))
                .path("/ps/user/checkUserVerify",
                        b1 -> b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                b2 -> b2
                                        .GET("/checkUser", handler::checkUser)
                        ))
                .path("/ps/user",
                        b1 -> b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                b2 -> b2
                                        .GET("/clearStamp", handler::clearStamp)
                        ))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> MainFrmRouter(MainFrmHandler handler) {
        return RouterFunctions.route().path("/ps/MainFrm",
                b1 -> b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        b2 -> b2
                                .GET("/getRights",handler::getRights)
                                .GET("/getRole", handler::getRole)
                                .POST("/download",handler::download)
                ))
                .build();
    }

    //修改密码
    @Bean
    public RouterFunction<ServerResponse> ChangePwdRouter(ChangePwdHandler handler) {
        return RouterFunctions.route().path("/ps/ChangePwd",
                        b1 -> b1.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                b2 -> b2
//                                        .GET("/getRole", handler::getRole)
                                        .POST("/ChangePwd",handler::ChangePwd)
                        ))
                .build();
    }

}
