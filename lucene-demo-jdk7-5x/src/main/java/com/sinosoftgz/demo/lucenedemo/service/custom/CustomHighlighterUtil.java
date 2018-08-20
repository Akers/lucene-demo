package com.sinosoftgz.demo.lucenedemo.service.custom;

import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 自定义高亮工具类
 * @author akers
 *
 */
public class CustomHighlighterUtil {
    static Logger logger = LoggerFactory.getLogger(CustomHighlighterUtil.class);

    /**
     * 高亮渲染文本
     *
     * @param tokens
     * @param preTag
     * @param postTag
     * @param originalText
     * @return
     */
    public static String highlighterQueryTokens(List<String> tokens, String preTag, String postTag, String originalText) {
        originalText=originalText.toLowerCase();
        if (tokens != null && !tokens.isEmpty()) {
            List<String> subTokens = null;
            for (int t = 0; t < tokens.size(); t++) {
                if (originalText.contains(tokens.get(t))) {
                    subTokens = new ArrayList<>(tokens.subList(t, tokens.size()));
                    break;
                }
            }
            if (subTokens == null || subTokens.isEmpty()) {
                return originalText;
            }
            String topToken = subTokens.get(0);
            String[] splitTexts = originalText.split(topToken);
            if (splitTexts.length == 1) {
                if (originalText.startsWith(topToken)) {
                    return new StringBuilder().append(preTag).append(topToken).append(postTag).append(splitTexts[0]).toString();
                } else if (originalText.endsWith(topToken)) {
                    return new StringBuilder().append(splitTexts[0]).append(preTag).append(topToken).append(postTag).toString();
                }
            } else {
                StringBuilder textBuilder = new StringBuilder();
                subTokens.remove(0);
                for (int t = 0; t < splitTexts.length; t++) {
                    textBuilder.append(highlighterQueryTokens(subTokens, preTag, postTag, splitTexts[t]));
                    if (t < splitTexts.length - 1) {
                        textBuilder.append(preTag).append(topToken).append(postTag);
                    }
                }
                return textBuilder.toString();
            }
        }

        return originalText;
    }

    /**
     * 获取关键词
     *
     * @param query
     * @return
     */
    public static List<String> getQueryTokens(Query query) {
        List<String> tokens = getQueryTokens(query, null);
        Collections.sort(tokens, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (o1.length() >= o2.length()) ? -1 : 1;
            }
        });
        return tokens;
    }

    private static List<String> getQueryTokens(Query query, List tokens) {
        tokens = (tokens == null ? new ArrayList<String>() : tokens);
        try {
            if (query instanceof TermQuery) {
                String wordTokenText = ((TermQuery) query).getTerm().text();
                if (tokens.contains(wordTokenText)) {
                    return tokens;
                }
                tokens.add(wordTokenText);
            } else if (query instanceof BoostQuery) {
                return getQueryTokens(((BoostQuery) query).getQuery());
            } else {
                Iterator<BooleanClause> iterator = ((BooleanQuery) query).iterator();
                while (iterator.hasNext()) {
                    BooleanClause next = iterator.next();
                    getQueryTokens(next.getQuery(), tokens);
                }
            }
        } catch (ClassCastException e) {
            logger.error("Query只能为分词器解析返回未经过处理的Query", e);
        }
        return tokens;
    }

}
