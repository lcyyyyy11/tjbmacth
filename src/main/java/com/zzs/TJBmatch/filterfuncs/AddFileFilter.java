package com.zzs.TJBmatch.filterfuncs;

import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Component
public class AddFileFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public Mono<ServerResponse> filter(ServerRequest serverRequest, HandlerFunction<ServerResponse> handlerFunction) {
        String format = serverRequest.queryParam("format").orElse("");
        if (StringUtils.isEmpty(format)) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取文件格式失败！");
        }
        return serverRequest.multipartData()
                .map(mp -> mp.toSingleValueMap())
                .flatMap(part -> Mono.just((FilePart) part.get("file")))
                .cast(FilePart.class)
                .flatMap(filePart -> {
                    String fileName = filePart.filename();
                    String suffix = "." + StringUtils.getFilenameExtension(fileName);
                    boolean match = Arrays.stream(format.split(","))
                            .map(String::toUpperCase)
                            .anyMatch(x -> x.equals(suffix.toUpperCase()));
                    if (match) {
                        return handlerFunction.handle(serverRequest);
                    }
                    else {
                        throw new ValidationException(RtnEnum.GENERAL_ERROR,"上传文件失败！请检查文件格式！");
                    }
                });
    }
}
