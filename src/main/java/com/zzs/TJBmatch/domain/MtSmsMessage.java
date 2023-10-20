package com.zzs.TJBmatch.domain;

import java.util.List;
import java.util.Map;

//短信消息体
public class MtSmsMessage {
    //接收方电话
    private List<String> mobiles;
    //短信模板ID
    private String templateId;
    //消息信息
    private Map<String, Object> templateParas;
    private String signature;
    private String messageId;
    private String extCode;
    private List<NamedPatameter> extendInfos;

    public List<String> getMobiles() {
        return mobiles;
    }

    public void setMobiles(List<String> mobiles) {
        this.mobiles = mobiles;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Map<String, Object> getTemplateParas() {
        return templateParas;
    }

    public void setTemplateParas(Map<String, Object> templateParas) {
        this.templateParas = templateParas;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getExtCode() {
        return extCode;
    }

    public void setExtCode(String extCode) {
        this.extCode = extCode;
    }

    public List<NamedPatameter> getExtendInfos() {
        return extendInfos;
    }

    public void setExtendInfos(List<NamedPatameter> extendInfos) {
        this.extendInfos = extendInfos;
    }


}

