package com.sinosoftgz.demo.lucenedemo.service;

import com.sinosoftgz.demo.lucenedemo.service.lucene.LuceneIndexUtils;
import com.sinosoftgz.demo.lucenedemo.service.lucene.LuceneSearchUtils;
import com.sinosoftgz.demo.lucenedemo.vo.SmsDoc;
import org.apache.lucene.document.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liang Wenxu
 * @since 2018/8/17
 */
@Component
public class SmsDocIndexGenerator {
    @Autowired
    LuceneIndexUtils luceneIndexUtils;

    @Autowired
    LuceneSearchUtils luceneSearchUtils;

    /**
     * 初始化所有索引
     */
    public void initIndexes(List<SmsDoc> smsDocs) {
        try {
            List<Document> docs = new ArrayList<>();
            for(SmsDoc smsDoc : smsDocs) {
                docs.add(this.generateDocument(smsDoc));
            }

            // 因为是重新生成，所以调用deleteAll删除旧索引
            luceneIndexUtils.removeAll(true);
            // 添加文档并提交
            luceneIndexUtils.addDocs(docs, false, true, true);
            // 重新加载索引
            luceneSearchUtils.reloadIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新索引
     * @param smsDocs
     */
    public void updateIndexes(List<SmsDoc> smsDocs) {
        List<Document> docs = new ArrayList<>();
        for(SmsDoc smsDoc : smsDocs) {
            if(smsDoc.isDeleteFlag()) {
                luceneIndexUtils.removeDoc(this.generateDocument(smsDoc));
            }
            docs.add(this.generateDocument(smsDoc));
        }

        try {
            // 添加文档，备份并提交
            luceneIndexUtils.addDocs(docs, true, true, true);
            // 重新加载索引
            luceneSearchUtils.reloadIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重点：将数据转换成Lucene的文档
     * @param smsDoc
     * @return
     */
    private Document generateDocument(SmsDoc smsDoc) {
        Document doc = new Document();

        // 建立ID索引，此处使用StringField，与TextField区别是StringField只索引不拆词，适合做精确匹配，例如id、code之类的字段
        doc.add(new StringField("id", smsDoc.getId().replaceAll("\\-", ""), Field.Store.NO));
        doc.add(new StoredField("id", smsDoc.getId()));

        if(smsDoc.getContent() != null && smsDoc.getContent().trim().length() > 0) {
            // 建立内容字段，分词并索引，适合做内容全文检索等模糊匹配索引，如果Store选择Store.YES则为存储字段，会被存储到索引文件中在搜索结果中可全部还原，否则将只能搜索不能还原
            Field contentField = new TextField("content", smsDoc.getContent(), Field.Store.YES);
            doc.add(contentField);
        }

        if(smsDoc.getSendDate() != null) {
            // 建立可排序索引，可实现按时间戳进行排序
            NumericDocValuesField sendDateFeild = new NumericDocValuesField("sendDate", smsDoc.getSendDate().getTime());
            doc.add(sendDateFeild);
            // 单独加入一个StoredField，方便还原的时候做转换
            doc.add(new StoredField("sendDateStored", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(smsDoc.getSendDate())));
        }

        // 同理，电话号码等需要精确搜索的字段，使用StringField可避免被分词器拆词
        if(smsDoc.getMobile() != null && smsDoc.getMobile().trim().length() > 0) {
            doc.add(new StringField("mobile", smsDoc.getMobile(), Field.Store.YES));
        }


        return doc;
    }
}
