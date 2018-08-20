package com.sinosoftgz.demo.lucenedemo.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.sinosoftgz.demo.lucenedemo.service.custom.CustomHighlighterUtil;
import com.sinosoftgz.demo.lucenedemo.vo.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocValuesRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sinosoftgz.demo.lucenedemo.service.lucene.LuceneSearchUtils;


/**
 * @author Liang Wenxu
 * @since 2018/8/17
 */
@Component
public class SmsDocSearcher {	
	private static final Logger logger = LoggerFactory.getLogger(LuceneSearchUtils.class);

	
    @Autowired
    LuceneSearchUtils luceneSearchUtils;

    /**
     * 搜索
     * @param queryVo
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public SearchResultVo search(SearchQueryVo queryVo) throws ParseException, IOException, java.text.ParseException {
        Long start = System.currentTimeMillis();
        BooleanQuery.Builder rootBuilder = new BooleanQuery.Builder();

        //TokenStream tokenStream =null;
//        String keyWord=null;
     /*   if (StringUtils.isNotBlank(queryVo.getKeyWord())) {

            queryVo.setKeyWord(keyWord);
//            String keyWord1 = queryVo.getKeyWord().replaceAll("\\*", "");
//            queryVo.setKeyWord(keyWord1);
        }*/

        String regEx = "[`~!@#$%^&*=|{}:;',\\[\\].<>/?~！@#￥%……&*——+|{}；：”“’。，、？\"]";
       
        // 内容查找（分词）
        Query query_content = null;
        if(queryVo.getContentKeyword() != null && queryVo.getContentKeyword().trim().length() > 0) {
            //去除keyWord中的反斜杠,* 避免lucene解析报错
            String originKeyWord = queryVo.getContentKeyword().replaceAll("\\\\", "");
            originKeyWord = originKeyWord.replaceAll("\\*", "");
            originKeyWord = originKeyWord.replaceAll("/", "");
            originKeyWord = originKeyWord.replaceAll(regEx, "");
            logger.info("keyword=" + originKeyWord);
            // 模糊查找，使用OR，如要精确搜索用AND查询关键字完全符合才返回结果
            query_content = luceneSearchUtils.parseComplexQuery("content", originKeyWord, QueryParser.Operator.OR);
            // 内容查询
            rootBuilder.add(query_content, BooleanClause.Occur.MUST);
        }

        // 精确匹配手机号
        if(queryVo.getMobile() != null && queryVo.getMobile().trim().length() > 0) {
            TermQuery termQuery = new TermQuery(new Term("mobile", queryVo.getMobile()));
            rootBuilder.add(termQuery, BooleanClause.Occur.MUST);
        }

        // 范围匹配时间
        if(queryVo.getSendDateBegin() != null || queryVo.getSendDateEnd() != null) {
            Query sendDateQuery = DocValuesRangeQuery.newLongRange("sendDate"
                    , new Long(queryVo.getSendDateBegin() == null ? 0L : queryVo.getSendDateBegin().getTime())
                    , new Long(queryVo.getSendDateEnd() == null ? new Date().getTime() : queryVo.getSendDateEnd().getTime()), true, true);
            rootBuilder.add(sendDateQuery, BooleanClause.Occur.MUST);
        }
        
        // 按照sendDate倒序
        Sort sort = new Sort();
        sort.setSort(new SortedNumericSortField("sendDate", SortField.Type.INT, true));


        Query query = rootBuilder.build();

        PageResult<ScoreDoc> r = luceneSearchUtils.search(query, new PageRequest(queryVo.getStart(), queryVo.getPageSize()), sort);
        
        SearchResultVo searchResultVo = new SearchResultVo();
        searchResultVo.setTotal((int) r.getTotalElements());
        searchResultVo.setStart(r.getNumber());
        searchResultVo.setPageSize(r.getSize());
        logger.info("查询:{},用时:{}", query.toString(), System.currentTimeMillis() - start);
        if (r.getTotalElements() <= 0)
            return searchResultVo;
        // 在搜索结果中还原文档数据
        for (ScoreDoc hit : r) {
            searchResultVo.getResult().add(getSmsDoc(hit));
        }
        
        // 查询结果高亮
        if(query_content != null) {
        	// 短信内容查找的高亮处理
        	// 查询Token，用于处理
            List<String> contentQueryTokens = CustomHighlighterUtil.getQueryTokens(query_content); //获取关键词
            
            for(SmsDoc doc : searchResultVo.getResult()) {
            	doc.setContent(CustomHighlighterUtil.highlighterQueryTokens(contentQueryTokens, "<b class='highlighter'>", "</b>", doc.getContent()));
            }
        }
        
        
        return searchResultVo;
    }

    /**
     * 从搜索结果中还原文档数据
     * @param hit
     * @return
     * @throws IOException
     */
    private SmsDoc getSmsDoc(ScoreDoc hit) throws IOException {
        //System.out.println(searcher.doc(hit.doc));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Document document = luceneSearchUtils.getDoc(hit);
        SmsDoc smsDoc = new SmsDoc();
        smsDoc.setSearchScore(hit.score);
        smsDoc.setContent(document.get("content"));
        smsDoc.setMobile(document.get("mobile"));
        smsDoc.setId(document.get("id"));
        try {
            smsDoc.setSendDate(sdf.parse(document.get("sendDateStored")));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return smsDoc;
    }
}
