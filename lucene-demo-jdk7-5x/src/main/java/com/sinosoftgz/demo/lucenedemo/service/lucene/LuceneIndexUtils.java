package com.sinosoftgz.demo.lucenedemo.service.lucene;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.lionsoul.jcseg.analyzer.v5x.JcsegAnalyzer5X;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * @author Liang Wenxu
 * @since 2018/8/17
 */
@Component
public class LuceneIndexUtils {
    private Logger log = LoggerFactory.getLogger(LuceneIndexUtils.class);

    /**
     * 索引文档的字段名{@value}: id
     */
    public static final String DOC_FEILD_NAME_ID = "id";

    @Value("${lucene.index-path}")
    private String luceneIndexPath;

    @Value("${lucene.backup-path}")
    private String backupIndexDir;

    protected IndexWriter writer = null;
    @Value("${lucene.bufferSize:256.0}")
    private Double bufferSize = 256.0;


	public void init() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(luceneIndexPath));

        Directory dirSearch = FSDirectory.open(Paths.get(luceneIndexPath));
        // 指定Jcseg中文分词，复杂分词模式
        JcsegAnalyzer5X a = new JcsegAnalyzer5X(JcsegTaskConfig.COMPLEX_MODE);
        Analyzer analyzer = new JcsegAnalyzer5X(JcsegTaskConfig.COMPLEX_MODE);
        
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        iwc.setIndexDeletionPolicy(new SnapshotDeletionPolicy(iwc.getIndexDeletionPolicy()));

        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        // 当内存中的暂存数据达到一定量时，将数据flush到磁盘，理论上Buffer越大磁盘IO次数越少，此时Searcher无感知
        iwc.setRAMBufferSizeMB(bufferSize);

        writer = new IndexWriter(dir, iwc);
    }

    public IndexWriter getWriter() {
        if (writer == null) {
            try {
                init();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return writer;
    }

    Object lockObj = new Object();

    /**
     * 添加文档
     * @param doc
     * @return
     * @throws IOException
     */
    private boolean addDoc(Document doc) throws IOException {
        // 更新文档，需指定一个Term（Lucene里的分词，最小查询单元）作为文档的唯一索引，一般可以用业务数据id来做
        IndexableField idField = doc.getField(DOC_FEILD_NAME_ID);
        if(idField == null) {
            doc.add(new StringField(DOC_FEILD_NAME_ID, doc.hashCode()+"", Field.Store.YES));
        }
        getIdxWriter().updateDocument(new Term("id", idField.stringValue().replaceAll("\\-", "")), doc);
        return true;
    }

    /**
     * 计算数值属性的boost分值
     * 计算公式为：
     * 当count = 0 时，boost = boost * countBoost
     * 当count > 0 时，boost = boost * countBoost^(count / 50)
     * @param count 数值
     * @param countBoost 数值对应的boost评分系数
     * @param boost 基准分值
     * @return
     */
    private float getBoost(Integer count, float countBoost, float boost) {
        float f;
        if (Integer.valueOf(count / 50) == 0) {
            f = countBoost;
        } else {
            f = Double.valueOf(Math.pow(countBoost, Integer.valueOf(count / 50))).floatValue();
        }
        if (f > 0) {
            boost = boost * f;
        }
        return boost;
    }

    /**
     * 批量添加文档，每批commit
     * @param docList 列表中的每个Document，必须包含有名为id的Feild用做索引的唯一标识
     * @throws IOException
     */
    public void addDocs(List<Document> docList) throws IOException {
    	addDocs(docList, false, false, false);
    }

    public void addDocs(List<Document> docList, Boolean backup, Boolean commitAndFlush) throws IOException {
    	addDocs(docList, backup, commitAndFlush ? true : false, commitAndFlush ? true : false);
    }

    /**
     * 添加文档
     * @param docList 文档列表
     * @param backup 是否备份
     * @param commit 是否提交（提交之后，Searcher在reloadIndex后即可生效）
     * @param flush 是否序列化（flush之后，Searcher无感知，直至commit后生效）
     * @throws IOException
     */
    public void addDocs(List<Document> docList, Boolean backup, Boolean commit, Boolean flush) throws IOException {
    	if(backup) {
    		backupIndex();
    	}
    	for (Document doc : docList) {
            addDoc(doc);
        }
        // 调试用，查询IndexWriter中的文档数量
        IndexWriter iw = getIdxWriter();
        log.info("文档已添加：[numDocs: {}, numRamDocs: {}, memUsed: {}KB]", iw.numDocs(), iw.numRamDocs(), iw.ramBytesUsed() / 1024);
        if(flush) {
        	iw.flush();
        	log.info("文档已flush：[numDocs: {}, numRamDocs: {}, memUsed: {}KB]", iw.numDocs(), iw.numRamDocs(), iw.ramBytesUsed() / 1024);
        }
        if(commit) {
        	iw.flush();
        	log.info("文档已commit：[numDocs: {}, numRamDocs: {}, memUsed: {}KB]", iw.numDocs(), iw.numRamDocs(), iw.ramBytesUsed() / 1024);
        }
    }

    /**
     * 删除文档
     * @param doc
     */
    public void removeDoc(Document doc) {
        try {
            IndexableField idField = doc.getField(DOC_FEILD_NAME_ID);
            getIdxWriter().deleteDocuments(new Term(DOC_FEILD_NAME_ID, idField.stringValue().replaceAll("\\-", "")));
            log.debug("delete doc :{}", idField.stringValue());
        } catch (IOException e) {
            log.error("删除文档失败", e);
        }
    }
    
    /**
     * 删除所有文档
     * @throws IOException 
     */
    public void removeAll(Boolean backup) throws IOException {
    	if(backup) {
    		backupIndex();
    	}
    	getWriter().deleteAll();
    }

    /**
     * 备份索引
     * @throws IOException
     */
    public void backupIndex() throws IOException {
        IndexWriterConfig config = (IndexWriterConfig) getIdxWriter().getConfig();
        SnapshotDeletionPolicy snapshotDeletionPolicy = (SnapshotDeletionPolicy) config.getIndexDeletionPolicy();
        IndexCommit snapshot = null;
        try {
            snapshot = snapshotDeletionPolicy.snapshot();
        } catch (Exception e) {
            log.warn("No index commit to snapshot backup skiped");
            return;
        }

        if(snapshot != null && backupIndexDir != null && backupIndexDir.trim().length() > 0) {
            log.info("Backuping snapshot: {}", snapshot.getSegmentsFileName());
            //设置索引提交点，默认是null，会打开最后一次提交的索引点
            config.setIndexCommit(snapshot);
            Collection<String> fileNames = snapshot.getFileNames();
            File[] dest = new File(backupIndexDir).listFiles();
            String sourceFileName;
            String destFileName;
            if (dest != null && dest.length > 0) {
                //先删除备份文件中的在此次快照中已经不存在的文件
                for (File file : dest) {
                    boolean flag = true;
                    //包括文件扩展名
                    destFileName = file.getName();
                    for (String fileName : fileNames) {
                        sourceFileName = fileName;
                        if (sourceFileName.equals(destFileName)) {
                            flag = false;
                            break;//跳出内层for循环
                        }
                    }
                    if (flag) {
                        file.delete();//删除
                    }
                }
                //然后开始备份快照中新生成的文件
                for (String fileName : fileNames) {
                    boolean flag = true;
                    sourceFileName = fileName;
                    for (File file : dest) {
                        destFileName = file.getName();
                        //备份中已经存在无需复制，因为Lucene索引是一次写入的，所以只要文件名相同不要要hash检查就可以认为它们的数据是一样的
                        if (destFileName.equals(sourceFileName)) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        File from = new File(luceneIndexPath + File.separator + sourceFileName);//源文件
                        File to = new File(backupIndexDir + File.separator + sourceFileName);//目的文件
                        FileUtils.copyFile(from, to);
                    }
                }
            } else {
                //备份不存在，直接创建
                for (String fileName : fileNames) {
                    File from = new File(luceneIndexPath + File.separator + fileName);//源文件
                    File to = new File(backupIndexDir + File.separator + fileName);//目的文件
                    FileUtils.copyFile(from, to);
                }
            }
            snapshotDeletionPolicy.release(snapshot);
            //删除已经不再被引用的索引提交记录
            getIdxWriter().deleteUnusedFiles();
            log.info("Backuped snapshot: {}", snapshot.getSegmentsFileName());
        }
    }

    public  void maybeMerge() throws IOException {
        getIdxWriter().maybeMerge();
    }

    public  void commit() throws IOException {
        getIdxWriter().commit();
    }

    public  void flush() {
        try {
            getIdxWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IndexWriter getIdxWriter() throws IOException {
        if (writer == null) {
            synchronized (lockObj) {
                if (writer == null) {
                    init();
                }
            }
        }
        return writer;
    }

	public String getLuceneIndexPath() {
		return luceneIndexPath;
	}

	public void setLuceneIndexPath(String luceneIndexPath) {
		this.luceneIndexPath = luceneIndexPath;
	}

	public String getBackupIndexDir() {
		return backupIndexDir;
	}

	public void setBackupIndexDir(String backupIndexDir) {
		this.backupIndexDir = backupIndexDir;
	}

	public Double getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(Double bufferSize) {
		this.bufferSize = bufferSize;
	}
}
