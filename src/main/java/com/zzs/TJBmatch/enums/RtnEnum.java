package com.zzs.TJBmatch.enums;


public enum RtnEnum {

    TIMEOUT(-1,"请求超时或非法请求！"),
    USER_NO_EXIST(101,"用户不存在或已注销"),
    USER_PASSWORD_ERROR(102,"用户名或密码错误"),
    IMGCODE_ERROR(103,"验证码错误"),
    NO_RIGHT(104,"无操作权限"),
    INVALID_DATA_ERROR(105,"数据格式不正确"),

    UNKNOW_ERROR(-1,"未知错误"),
    SUCCESS(0,"成功"),
    GENERAL_ERROR(-2,"错误"),
    SAVE_ERROR(11,"更新失败,请核查数据合法性!"),
    UPLOAD_ERROR(12,"上传文件失败"),
    ///登录部分
    USER_ONLINEE(100,"该用户目前在线，不能重复登录!"),

    ;
    private Integer errcode;
    private String msg;

    RtnEnum(Integer code, String msg){
        this.errcode = code;
        this.msg = msg;
    }
    public Integer getErrcode() {
        return errcode;
    }

    public String getMsg() {
        return msg;
    }
}
