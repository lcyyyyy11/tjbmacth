package com.zzs.TJBmatch.filterfuncs;

import com.zzs.TJBmatch.services.BytesSer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * zzs 2019.12
 * 该全局过滤器为webflux的RouterFunction全局过滤器
 * 判断是否已经登录
 * 否则返回登录
 *
 * 注：该WebFilter所有请求都会拦截
 * 所以一般不适合集成在Gateway中
 * 也会影响选择性拦截的应用中
 * 一般一应采用HandlerFilterFunction
 * 本框架提供了实例
 */

@Primary
@Component
public class RouterGlobalFilter implements WebFilter {

    private Logger log = LoggerFactory.getLogger( RouterGlobalFilter.class );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().getHeaders().setContentType( MediaType.APPLICATION_JSON);
        // exchange.getResponse().getHeaders().getAcceptCharset().forEach(s -> System.out.println( "编码："+s));

        //防止XSS攻击
        exchange.getResponse()
                .getHeaders().add("X-XSS-Protection", "1; mode=block");

        String sUrl = exchange.getRequest().getURI().getPath();
        String[] sKeys = {"/LogIn", "/img/", "/js/", "/css/", "/fonts/","/music/","html","/Plugins","/downLoad","/searchSingleVal"};
        if (BytesSer.stringContainsItemFromList(sUrl,sKeys)) {
            return chain.filter(exchange);
        }
        else if (sUrl.contains( "/loginVerify" )) {
            return chain.filter(exchange);
        }
        else if (sUrl.contains( "/checkUserVerify" )) {
            return chain.filter(exchange);
        }
        else
            return exchange
                    .getSession()
                    .flatMap( s -> {
//                        log.info("gate way 登录sessionId："+s.getId());
                        String[] sStamp = null;
//                        log.info("判断是否存在签名session:"+s.getAttributes().containsKey( "STAMPHASH" ));
//                        log.info("签名session:"+s.getAttribute("STAMPHASH"));
                        if (s.getAttributes().containsKey( "STAMPHASH" )) {
                            sStamp = s.getAttributes().get( "STAMPHASH" ).toString().split( "==" );
                            if (sStamp[0].hashCode() == Integer.parseInt( sStamp[1] )) {
                                return chain.filter( exchange );
                            } else{
                                return reLogin(exchange,chain);
                            }
                        } else{
                            return reLogin(exchange,chain);
                        }
                    } );
//        return chain.filter( exchange );
    }
    private Mono<Void> reLogin(ServerWebExchange exchange, WebFilterChain chain){
//        log.info(" ==== 无权限===  ");
        URI newURI = null;
        try {
            // ngnix
//            newURI = new URI( exchange.getRequest().getPath().contextPath() + "/educloudfrm/LogIn.html" );
            //  localhost
            newURI = new URI( exchange.getRequest().getPath().contextPath() + "/LogIn.html");

            // 直接进入统一认证平台
//            Map<String, String> stringStringMap = exchange.getRequest().getQueryParams().toSingleValueMap();
//            stringStringMap.forEach((k,v) -> log.info("k,v"+k,v));
//            String ticket = exchange.getRequest().getQueryParams().toSingleValueMap().getOrDefault("ticket","");
//            log.info("ticket："+ticket);
//            newURI = new URI( "http://121.37.12.111:7777/logback?ticket="+ticket);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode( HttpStatus.MOVED_PERMANENTLY );
        response.getHeaders().setLocation( newURI );
        return Mono.empty();
    }
}
