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
public class AddFileNameFormatFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest serverRequest, HandlerFunction<ServerResponse> handlerFunction) {
        String format = serverRequest.queryParam("format").orElse("");
        if (StringUtils.isEmpty(format)) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取文件格式失败！");
        }
        String uname = serverRequest.queryParam("uname").orElse("");
        if (StringUtils.isEmpty(uname)) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取选手学校名称失败！");
        }
        String mpname = serverRequest.queryParam("mpname").orElse("");
        if (StringUtils.isEmpty(mpname)) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"获取选手姓名失败！");
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
                        if (fileName.contains(uname)) {
                            throw new ValidationException(RtnEnum.GENERAL_ERROR,"上传文件失败！请检查文件名是否包含选手学校名称！");
                        }
                        if (fileName.contains(mpname)) {
                            throw new ValidationException(RtnEnum.GENERAL_ERROR,"上传文件失败！请检查文件名是否包含选手姓名！");
                        }
                        return handlerFunction.handle(serverRequest);
                    }
                    else {
                        throw new ValidationException(RtnEnum.GENERAL_ERROR,"上传文件失败！请检查文件格式！");
                    }
                });
    }


}
