package com.zzs.TJBmatch.handlers.publichandler;

import com.zzs.TJBmatch.enums.RtnEnum;
import com.zzs.TJBmatch.exceptions.ValidationException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;

/**
 * zzs 2022-05
 */

@Service
public class OnlineVideo {

    private static final String FORMAT = "file:%s";  //资源文件绝对路径地址的设定

    private final ResourceLoader resourceLoader;
    public OnlineVideo(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Mono<Resource> getVideo(String title) {
        if (!new File(title).exists()) {
            throw new ValidationException(RtnEnum.GENERAL_ERROR,"文件不存在！");
        }
        return Mono.fromSupplier(() -> this.resourceLoader.getResource(String.format(FORMAT, title)));
    }

}
