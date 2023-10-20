package com.zzs.TJBmatch.utility;


import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequestUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    //发送短信验证码
    public static String sendValidCode(Environment environment, String phoneNumber) {
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
        //发送短信验证码
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            String resp = send(phoneNumber, code.toString());
            if (null == resp) {
                return "";
            }
            return code.toString();
        }
        else {
            return "111111";
        }
    }

    //判断电话号码
    private static boolean isPhoneNumber(String phoneNumber) {
        String regEx = "^13[0-9]{9}|(15[0-35-9]|18[0123456789]|14[56789]|17[0-9]|16[567]|19[0-9])[0-9]{8}$";
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public static String send(String phoneNumber, String code){
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
                               String token, String title, String content) {

        Map<String,String> map = new HashMap<>();
        map.put("isShortMsg",isShortMsg);
        map.put("isPhone",isPhone);
        map.put("isSign",isSign);
        map.put("accepter",accepter);
        map.put("token",token);
        map.put("title",title);
        map.put("content",content);
        return getRequest(urlSubmit, map);
    }

    // httpclient 4.4版本之后将这些设置封装到 RequestConfig 对象⾥，其中
    // setConnectTimeout 是设置连接到⽬标 URL 的等待时长，超过这个时间还没连上就抛出连接超时；
    // setConnectionRequestTimeout 是从connect Manager（连接池）获取连接的等待时长，这个版本是共享连接池的；
    // setSocketTimeout 是连接到⽬标URL之后等待返回响应的时长，即超过这个时间就放弃本次调⽤并抛出；
    public static String getRequest(String url, Map<String, String> params) {
        String urlParams = url + "?";
        String res = null;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlParams += (entry.getKey() + "=" + entry.getValue() + "&");
        }
        HttpGet httpGet = null;

        CloseableHttpClient httpClient = null;
        try {
            httpGet = new HttpGet(urlParams);
            httpClient = HttpClientBuilder.create().build();
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(10000)
                    .setSocketTimeout(10000)
                    .setConnectionRequestTimeout(6000)
                    .build();
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = httpClient.execute(httpGet);//由客户端执行(发送)Get请求
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 从响应模型中获取响应实体
                res = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        } catch (Exception e) {
            logger.error("发送短信连接出错：{}", e.getMessage());
        }
        finally {
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error("httpClient关闭连接出错：{}", e.getMessage());
                }
            }
            if (null != httpGet) {
                httpGet = null;
            }
        }

        if (null != res && !res.isEmpty() && res.contains("信息发送成功")) {
            return res;
        }
        return null;
    }
}
