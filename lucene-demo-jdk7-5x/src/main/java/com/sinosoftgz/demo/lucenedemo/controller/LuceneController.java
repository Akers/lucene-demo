package com.sinosoftgz.demo.lucenedemo.controller;

import com.sinosoftgz.demo.lucenedemo.service.SmsDocIndexGenerator;
import com.sinosoftgz.demo.lucenedemo.service.SmsDocSearcher;
import com.sinosoftgz.demo.lucenedemo.service.lucene.LuceneSearchUtils;
import com.sinosoftgz.demo.lucenedemo.vo.SearchQueryVo;
import com.sinosoftgz.demo.lucenedemo.vo.SearchResultVo;
import com.sinosoftgz.demo.lucenedemo.vo.SmsDoc;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Liang Wenxu
 * @since 2018/8/17
 */
@Controller
public class LuceneController {
    @Autowired
    SmsDocIndexGenerator smsDocIndexGenerator;

    @Autowired
    SmsDocSearcher searcher;

    // 动态添加文档测试
    @RequestMapping(value = "doc", method = RequestMethod.POST)
    @ResponseBody
    public Object add(SmsDoc smsDoc) {
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(32));
        List<SmsDoc> smsDocs = new ArrayList<>();
        smsDocs.add(smsDoc);
        smsDocIndexGenerator.updateIndexes(smsDocs);

        return smsDoc;
    }

    // 动态删除文档测试
    @RequestMapping(value = "doc/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public Object add(@PathVariable("id") String id) {
        SmsDoc smsDoc = new SmsDoc();
        smsDoc.setId(id);
        smsDoc.setDeleteFlag(true);
        List<SmsDoc> docs = new ArrayList<>();
        docs.add(smsDoc);
        smsDocIndexGenerator.updateIndexes(docs);
        return "deleted " + id;
    }


    @RequestMapping("/init")
    @ResponseBody
    public Object init() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SmsDoc> docs = new ArrayList<>();


        SmsDoc smsDoc = new SmsDoc();
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(10));
        smsDoc.setMobile("13800138000");
        smsDoc.setContent("短信验证码 318652，请在十分钟内完成验证。");
        smsDoc.setSendDate(sdf.parse("2018-01-01 01:00:00"));
        docs.add(smsDoc);

        smsDoc = new SmsDoc();
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(10));
        smsDoc.setMobile("13760000011");
        smsDoc.setContent("测试短信内容1111111111111111111111111111111。");
        smsDoc.setSendDate(sdf.parse("2016-03-01 01:00:00"));
        docs.add(smsDoc);

        smsDoc = new SmsDoc();
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(10));
        smsDoc.setMobile("13924023111");
        smsDoc.setContent("测试短信内容222222222222222222222222222222222222222222222");
        smsDoc.setSendDate(sdf.parse("2016-06-29 01:00:00"));
        docs.add(smsDoc);

        smsDoc = new SmsDoc();
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(10));
        smsDoc.setMobile("18966561222");
        smsDoc.setContent("尊贵的客户王五您好！");
        smsDoc.setSendDate(sdf.parse("2017-03-12 01:00:00"));
        docs.add(smsDoc);

        smsDoc = new SmsDoc();
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(10));
        smsDoc.setMobile("18966561232");
        smsDoc.setContent("尊贵的客户王五您好！您的短信验证码 88488，请在十分钟内完成验证。");
        smsDoc.setSendDate(sdf.parse("2016-08-18 01:00:00"));
        docs.add(smsDoc);

        smsDoc = new SmsDoc();
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(10));
        smsDoc.setMobile("18966561242");
        smsDoc.setContent("短信验证码 318652，请在十分钟内完成验证。");
        smsDoc.setSendDate(sdf.parse("2016-08-02 01:00:00"));
        docs.add(smsDoc);

        smsDoc = new SmsDoc();
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(10));
        smsDoc.setMobile("18966569822");
        smsDoc.setContent("RDAAxxxxxxxxxx    张三，您好，案件后续理赔进度查询、定损报价等理赔问题，请拨打理赔服务专线：0762-3395518，服务时间：8:00-20:00");
        smsDoc.setSendDate(sdf.parse("2012-02-14 01:00:00"));
        docs.add(smsDoc);

        smsDoc = new SmsDoc();
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(10));
        smsDoc.setMobile("13800138000");
        smsDoc.setContent("短信验证码 318653，请在十分钟内完成验证。");
        smsDoc.setSendDate(sdf.parse("2018-01-01 01:00:00"));
        docs.add(smsDoc);

        smsDoc = new SmsDoc();
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(10));
        smsDoc.setMobile("13800138000");
        smsDoc.setContent("短信验证码 418552，请在十分钟内完成验证。");
        smsDoc.setSendDate(sdf.parse("2018-01-01 01:00:00"));
        docs.add(smsDoc);

        smsDoc = new SmsDoc();
        smsDoc.setId(RandomStringUtils.randomAlphanumeric(10));
        smsDoc.setMobile("13800138000");
        smsDoc.setContent("短信验证码 311552，请在十分钟内完成验证。");
        smsDoc.setSendDate(sdf.parse("2018-01-01 01:00:00"));
        docs.add(smsDoc);

        // 初始化索引/全量生成索引
        smsDocIndexGenerator.initIndexes(docs);


        return docs;
    }

    @RequestMapping("")
    public String index() {
        return "index";
    }

    @RequestMapping("searcher")
    @ResponseBody
    public Map searcher(SearchQueryVo query) {
        Map json = new HashMap<>();
        SearchResultVo r = null;
        try {
            json.put("success", true);
            json.put("data", searcher.search(query));
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            json.put("success", false);
            json.put("message", e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            json.put("success", false);
            json.put("message", e.getLocalizedMessage());
            e.printStackTrace();
        } catch (ParseException e) {
            json.put("success", false);
            json.put("message", e.getLocalizedMessage());
            e.printStackTrace();
        }
        return json;
    }

}
