package com.sinosoftgz.demo.lucenedemo.vo;


import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 短信文档
 */
public class SmsDoc implements Serializable {

    /**
     * 业务数据主键id
     */
    private String id;

    /**
     * 短信内容
     */
    private String content;

    /**
     * 删除标记，默认不删除
     */
    private boolean deleteFlag = false;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 发送短信时间
     */
    private Date sendDate;

    /**
     * 用户代码
     */
    private String userCode;

    /**
     * 机构代码
     */
    private String comCode;

    /**
     * 保存的扩展字段
     */
    private Map<String, String> extSaveProperties = new HashMap();

    /**
     * 索引扩展字段
     */
    private Map<String, String> extIndexProperties = new HashMap();

    /**
     * 搜索打分
     */
    private float searchScore;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getComCode() {
        return comCode;
    }

    public void setComCode(String comCode) {
        this.comCode = comCode;
    }

    public Map<String, String> getExtSaveProperties() {
        return extSaveProperties;
    }

    public void setExtSaveProperties(Map<String, String> extSaveProperties) {
        this.extSaveProperties = extSaveProperties;
    }

    public Map<String, String> getExtIndexProperties() {
        return extIndexProperties;
    }

    public void setExtIndexProperties(Map<String, String> extIndexProperties) {
        this.extIndexProperties = extIndexProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmsDoc)) return false;
        SmsDoc smsDoc = (SmsDoc) o;
        return deleteFlag == smsDoc.deleteFlag &&
                Objects.equals(id, smsDoc.id) &&
                Objects.equals(content, smsDoc.content) &&
                Objects.equals(mobile, smsDoc.mobile) &&
                Objects.equals(sendDate, smsDoc.sendDate) &&
                Objects.equals(userCode, smsDoc.userCode) &&
                Objects.equals(comCode, smsDoc.comCode) &&
                Objects.equals(extSaveProperties, smsDoc.extSaveProperties) &&
                Objects.equals(extIndexProperties, smsDoc.extIndexProperties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, content, deleteFlag, mobile, sendDate, userCode, comCode, extSaveProperties, extIndexProperties);
    }

    public float getSearchScore() {
        return searchScore;
    }

    public void setSearchScore(float searchScore) {
        this.searchScore = searchScore;
    }

    @Override
    public String toString() {
        return "SmsDoc{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", deleteFlag=" + deleteFlag +
                ", mobile='" + mobile + '\'' +
                ", sendDate=" + sendDate +
                ", userCode='" + userCode + '\'' +
                ", comCode='" + comCode + '\'' +
                ", extSaveProperties=" + extSaveProperties +
                ", extIndexProperties=" + extIndexProperties +
                ", searchScore=" + searchScore +
                '}';
    }
}
