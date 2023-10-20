package com.zzs.TJBmatch.utility;

import com.zzs.TJBmatch.domain.RtnObj;
import org.springframework.stereotype.Service;

@Service
public class RtnObjUtil {
    public static RtnObj success(Object obj){
        RtnObj rtnObj = new RtnObj();
        rtnObj.setErrcode(1);
        rtnObj.setMsg("成功");
        rtnObj.setData(obj);
        return rtnObj;
    }

    public static RtnObj success(){
       return success(null);
    }
    public static RtnObj error(Integer ecode,String msg,String extraMsg){
        RtnObj rtnObj = new RtnObj();
        rtnObj.setErrcode(ecode);
        rtnObj.setMsg(msg);
        rtnObj.setData(msg +" " + extraMsg);
        return rtnObj;
    }
}
