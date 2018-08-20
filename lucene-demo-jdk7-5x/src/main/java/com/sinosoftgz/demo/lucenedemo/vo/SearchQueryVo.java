package com.sinosoftgz.demo.lucenedemo.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 索引查询对象
 */
public class SearchQueryVo implements Serializable {
    /**
     * 页数
     */
    private Integer pageSize;

    /**
     * 页码
     */
    private Integer start;

    /**
     * z总数
     */
    private Integer total;

    /**
     * 发送时间开始
     */
    private Date sendDateBegin;
    /**
     * 发送时间结束
     */
    private Date sendDateEnd;
    /**
     * 发送内容关键字
     */
    private String contentKeyword;
    /**
     * 手机号
     */
    private String mobile;

    public Date getSendDateBegin() {
        return sendDateBegin;
    }

    public void setSendDateBegin(Date sendDateBegin) {
        this.sendDateBegin = sendDateBegin;
    }

    public Date getSendDateEnd() {
        return sendDateEnd;
    }

    public void setSendDateEnd(Date sendDateEnd) {
        this.sendDateEnd = sendDateEnd;
    }

    public String getContentKeyword() {
        return contentKeyword;
    }

    public void setContentKeyword(String contentKeyword) {
        this.contentKeyword = contentKeyword;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
