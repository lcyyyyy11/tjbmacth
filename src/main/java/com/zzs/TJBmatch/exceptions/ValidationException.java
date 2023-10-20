package com.zzs.TJBmatch.exceptions;

import com.zzs.TJBmatch.enums.RtnEnum;
import org.json.JSONObject;

public class ValidationException extends RuntimeException{

    private Integer errcode;
    private String extraMsg;
    public ValidationException(RtnEnum rtnEnum, String extraMsg){
        super(rtnEnum.getMsg());
        this.errcode = rtnEnum.getErrcode();
        this.extraMsg = extraMsg;
    }

    public Integer getErrcode() {
        return errcode;
    }

    public String getExtraMsg() {
        return extraMsg;
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("opcode","参数校验");
        jsonObject.put("msg", "参数校验未通过: ("+ extraMsg+")");
        jsonObject.put("mark",errcode);
        jsonObject.put("data",extraMsg);
        return jsonObject.toString();
    }
}
