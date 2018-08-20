package com.sinosoftgz.demo.lucenedemo.service.lucene;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.store.FSDirectory;
import org.lionsoul.jcseg.analyzer.v5x.JcsegAnalyzer5X;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.sinosoftgz.demo.lucenedemo.vo.PageRequest;
import com.sinosoftgz.demo.lucenedemo.vo.PageResult;

/**
 * Lucene索引搜索
 */
@Component
@DependsOn(value = "luceneIndexUtils")
public class LuceneSearchUtils {

    private static final Logger logger = LoggerFactory.getLogger(LuceneSearchUtils.class);

    @Value("${lucene.index-path}")
    private String luceneIndexPath;

    protected IndexWriter writer = null;
    @Value("${lucene.bufferSize:256.0}")
    private Double bufferSize = 256.0;

    IndexSearcher searcher;

    Analyzer analyzer;

    boolean inited = false;
    IndexReader reader;

    /**
     * 分页查找
     * @param query 查询条件
     * @param pageable 分页
     * @param sort 排序
     * @return
     * @throws IOException
     */
    public PageResult<ScoreDoc> search(Query query, PageRequest pageable, Sort sort) throws IOException {
    	TopFieldCollector collector = TopFieldCollector.create(sort, pageable.getOffset() + pageable.getPageSize(), false, true, true);;
    	getSearcher().search(query, collector);
    	TopDocs topDocs = collector.topDocs(pageable.getOffset(), pageable.getPageSize());
    	ScoreDoc[] hits = topDocs.scoreDocs;
    	if(hits == null  || hits.length <= 0) {
    		return new PageResult<>(null);
    	}
    	
    	return new PageResult<>(Arrays.asList(hits), pageable, topDocs.totalHits);
    }
    
    /**
     * 获取复杂Query的解析器
     * @param feild 字段名
     * @param queryStr 查询字符串（关键字串）
     * @param operator 关键字组合方式，OR或AND
     * @return
     * @throws ParseException 查询字符串解析报错
     */
    public Query parseComplexQuery(String feild, String queryStr, QueryParser.Operator operator) throws ParseException {
    	ComplexPhraseQueryParser aqp = new ComplexPhraseQueryParser("content", this.analyzer);
    	aqp.setDefaultOperator(operator == null ? QueryParser.Operator.OR : operator);
    	return aqp.parse(queryStr);
    }
    
    /**
     * 从ScoreDoc还原Document
     * @param hits ScoreDoc，查询结果
     * @return
     * @throws IOException
     */
    public Document getDoc(ScoreDoc hits) throws IOException {
    	return getSearcher().doc(hits.doc);
    }
    
    @PostConstruct
    public synchronized void init() throws IOException {
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(luceneIndexPath)));
            searcher = new IndexSearcher(reader);

            analyzer = new JcsegAnalyzer5X(JcsegTaskConfig.COMPLEX_MODE);

            inited = true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            //e.printStackTrace();
        }
    }
    
    
    public IndexSearcher getSearcher() throws IOException {
    	if(!inited) {
    		this.init();
    	}
    	return this.searcher;
    }
    
    /**
     * 重新加载index
     *
     * @throws IOException
     */
    public synchronized void reloadIndex() throws IOException {
        IndexReader newreader = DirectoryReader.open(FSDirectory.open(Paths.get(luceneIndexPath)));

        IndexSearcher indexSearcher = new IndexSearcher(newreader);

        IndexSearcher old_indexSearcher = searcher;
        final IndexReader old_reader = reader;

        reader = newreader;
        searcher = indexSearcher;

        final Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (old_reader != null) {
                        old_reader.close();
                        timer.cancel();// 如果任务已执行完，管理timer
                    }
                } catch (IOException e) {
                    timer.cancel();
                    //e.printStackTrace();
                    logger.error("关闭旧index reader 时出错");
                    logger.error(e.getMessage(), e);

                }
                //System.out.println("Hello !!!");
            }
        };

        long delay = 0;
        long intevalPeriod = 10 * 1000;
        // schedules the task to be run in an interval
        timer.scheduleAtFixedRate(task, delay, intevalPeriod);
    }
}
