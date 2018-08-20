package com.sinosoftgz.demo.lucenedemo.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoqian on 2016/11/7.
 */
public class SearchResultVo implements Serializable {
    /**
     * 索引结果集
     */
    private  List<SmsDoc> result = new ArrayList<SmsDoc>();

    /**
     * 页数
     */
    private Integer pageSize;
    /**
     *页码
     */
    private Integer start;
    /**
     *总数
     */
    private Integer total;
    /**
     * 尾页
     */
    private Integer end;

    /**
     * 是否有下页
     * @return true/false
     */
    public boolean next(){
        if(total> (start*pageSize +pageSize)){
            return true;
        }
        return false;
    }

    /**
     * 默认显示页码
     */
    static final Integer showPagesCount = 7;

    /**
     * 计算显示页码
     * @return 页面集合
     */
    public List<Integer> showPages(){
     //   total=91;
        List<Integer> l = new ArrayList();
        if(total==null || total<=0){
            l.add(0);
            return l;
        }

        int t = showPagesCount/2;
        int start = this.start - t;
        for (int i = 0; i < showPagesCount; i++) {
            l.add(start+i);
        }
        for (int i = l.size()-1; i >=0 ; i--) {
            if(l.get(i)<0){
                l.remove(i);
            }
            if(l.get(i)> (total/pageSize)){
                l.remove(i);
            }
        }
        if(l.size()>0 && l.size()<showPagesCount  &&l.get(l.size()-1)< (total/pageSize)){
            int startt = l.get(l.size()-1);
            for (int i = 0; i <=showPagesCount-l.size(); i++) {
                l.add(startt+1+i);
            }
//            for (int i = 1; i <showPagesCount-l.size(); i++) { //zhangqingan 2016/12/28 原方法多插一个空页号
//                l.add(startt+i);
//            }
        }

        int returnSize=l.size();

        List<Integer> returnList=new ArrayList<>();

        for(int i=0 ;i<returnSize;i++){
            if(l.get(i)>=0 && l.get(i)+1<=(new BigDecimal(total).divide(new BigDecimal(pageSize),0,BigDecimal.ROUND_UP).intValue())){
                returnList.add(l.get(i));
            }

        }


        return returnList;
    }

    /**
     * 显示更多
     * @return 数量
     */
    public Integer hasMorePage(){
        int t = start + showPagesCount/2;
        if(t<total/pageSize){
            return t;
        }
        return 0;
    }

    /**
     *
     */
    private Map resultMap = new HashMap();

    public Map getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map resultMap) {
        this.resultMap = resultMap;
    }

    public List<SmsDoc> getResult() {
        return result;
    }

    public void setResult(List<SmsDoc> result) {
        this.result = result;
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

    public Integer getEnd() {
        end = this.total / this.pageSize;
        int last = this.total % this.pageSize;
        if(last > 0) {
            end ++;
        }
        return end;
    }

}
