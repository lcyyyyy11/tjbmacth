package com.zzs.TJBmatch.utility;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendSmsUtil {

    private static Logger logger = LoggerFactory.getLogger(SendSmsUtil.class);

    //发送短信验证码
    public static String sendValidCode(String phoneNumber) {
        //判断手机号
        if (!isPhoneNumber(phoneNumber)){
            return "";
        }
        //生成短信验证码
        StringBuilder code = new StringBuilder();
        while (code.length() < 6) {
            Random r = new Random();
            int i = r.nextInt(10);
            if (!code.toString().contains(String.valueOf(i))) {
                code.append(String.valueOf(i));
            }
        }
//        //发送短信验证码
//        String resp = send(phoneNumber, code.toString());
//        if (null == resp) {
//            return "";
//        }
//        return code.toString();
        return "111111";
    }

   //发送消息
    public static String send(String phoneNumber, String code)  {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        String token = MD5Utils.getMD5Content("20030083"+date);
        return wrap("http://202.115.196.193:8088/sicnu_usc/api/send/21c540cde9454bdf949698628615901a","1","1","0",phoneNumber,token,"短信验证码","您的验证码为"+code);
    }

    /**
     * 包装数据发送信息
     * urlSubmit:服务器地址
     * isShortMsg：发送消息方式；1-短信，3-邮件，2-微信
     * isPhone：是否发送手机号码；0-学工号，1-电话号码
     * isSign：是否添加应用签名；0-不添加签名，1-添加签名
     * accepter：电话号码
     * token：令牌
     * title：消息标题
     * content：消息内容
     */
    private static String wrap(String urlSubmit, String isShortMsg, String isPhone, String isSign, String accepter,
                               String token, String title, String content)  {

        Map<String,String> map = new HashMap<>();
        map.put("isShortMsg",isShortMsg);
        map.put("isPhone",isPhone);
        map.put("isSign",isSign);
        map.put("accepter",accepter);
        map.put("token",token);
        map.put("title",title);
        map.put("content",content);
        return post(urlSubmit, map);
    }

    private static String post(String urlStr, Map<String, String> parameterMap){
        HttpURLConnection httpURLConnection = null;
        PrintWriter pw = null;
        BufferedReader br = null;
        StringBuilder sb = null;
        try {
            //获取连接
            URL url = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            //设置连接属性
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("charset","utf-8");

            //发送数据给服务器
            pw = new PrintWriter(new BufferedOutputStream(httpURLConnection.getOutputStream()));
            StringBuffer parameter = new StringBuffer();

            parameter.append("1=1");
            for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                parameter.append("&" + entry.getKey() + "=" + entry.getValue());
            }

            pw.write(parameter.toString());
            pw.flush();

            // 读取数据
            br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            sb = new StringBuilder();
            char[] b = new char[2048];
            int len = 0;
            while ((len = br.read(b)) != -1) {
                sb.append(new String(b, 0, len));
            }
        }catch (Exception e){
            logger.error("发送短信{}出错：{}" , parameterMap.getOrDefault("accepter",""),e.getMessage());
        }finally {
            if (httpURLConnection !=null) httpURLConnection.disconnect();
            try {
                if (pw !=null) pw.close();
                if (br !=null) br.close();
            }catch (Exception e){
                logger.error("发送短信关闭连接出错：{}", e.getMessage());
            }
        }
        if (null != sb && !sb.toString().isEmpty()) {
            return sb.toString();
        }
        return null;
    }

    //判断电话号码
    private static boolean isPhoneNumber(String phoneNumber) {
        String regEx = "^13[0-9]{9}|(15[0-35-9]|18[0123456789]|14[56789]|17[0-9]|16[567]|19[0-9])[0-9]{8}$";
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

}

