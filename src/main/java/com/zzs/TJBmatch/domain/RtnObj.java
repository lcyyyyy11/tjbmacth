package com.zzs.TJBmatch.domain;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RtnObj<T> implements ServerResponse {
    private Integer errcode;
    private String msg;
    private T data;

    public Integer getErrcode() {
        return errcode;
    }

    public void setErrcode(Integer errcode) {
        this.errcode = errcode;
    }

    @Override
    public HttpStatus statusCode(){
        return null;
    }

    @Override
    public int rawStatusCode() {
        return 0;
    }

    @Override
    public HttpHeaders headers() {
        return null;
    }

    @Override
    public MultiValueMap<String, ResponseCookie> cookies() {
        return null;
    }

    @Override
    public Mono<Void> writeTo(ServerWebExchange serverWebExchange, Context context) {
        return null;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
