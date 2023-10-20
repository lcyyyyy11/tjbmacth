package com.zzs.TJBmatch.handlers.exceptionHandler;

import com.zzs.TJBmatch.exceptions.ValidationException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * zzs 2020-12
 */
@Component
@Order(-2)
public class ExceptionHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable ex) {

        ServerHttpResponse response = serverWebExchange.getResponse();

        //一般响应头设置400，但这里可以设置为200。为客户端正常反应执行
        response.setStatusCode( HttpStatus.OK);

        //设置返回类型
        response.getHeaders().setContentType( MediaType.APPLICATION_JSON);
       //设置异常信息
        String errMsg = toStr(ex);

        DataBuffer db = response.bufferFactory().wrap( errMsg.getBytes(StandardCharsets.UTF_8) );

        return response.writeWith( Mono.just( db ));
    }

    private String toStr(Throwable ex) {
       //自定义异常
        if (ex instanceof ValidationException){
            ValidationException e = (ValidationException)ex;
            return ex.toString();
        }
        //其它异常
        else{
            ex.printStackTrace();
            return ex.toString();
        }
    }
}
