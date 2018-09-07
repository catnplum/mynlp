package com.mayabot.nlp.segment.tokenizer;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.WordTermCollector;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.utils.CharSet;

import java.util.Iterator;

/**
 * 提供各种WordTermCollector的默认实现。
 *
 * @author jimichan
 */
public class WordTermCollectors {

    static final String TABLE =
            "\u2002\u3000\r\u0085\u200A\u2005\u2000\u3000"
                    + "\u2029\u000B\u3000\u2008\u2003\u205F\u3000\u1680"
                    + "\u0009\u0020\u2006\u2001\u202F\u00A0\u000C\u2009"
                    + "\u3000\u2004\u3000\u3000\u2028\n\u2007\u3000";
    static CharSet whitespaceCharset = CharSet.getInstance(TABLE);

    private static final boolean isWhiteSpace(String word) {
        return whitespaceCharset.contains(word.charAt(0));
    }

    /**
     * 最优路径选择器。
     * 如果有subword，双层结构表示。类似于语料库中处理复合词的做法
     * 北京人民大学  =》 北京 [人民 大学]
     */
    public static WordTermCollector bestPath = (wordnet, wordPath, target) -> {
        Iterator<Vertex> vertexIterator = wordPath.iteratorBestPath();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();

            WordTerm term = new WordTerm(vertex.realWord(), vertex.guessNature());
            term.setOffset(vertex.getRowNum());

            //默认把空白的字符去除掉
            if (isWhiteSpace(term.word)) {
                continue;
            }

            if (vertex.subWords != null) {
                term.setSubword(Lists.newArrayListWithCapacity(vertex.subWords.size()));
                for (Vertex subWord : vertex.subWords) {
                    WordTerm sub = new WordTerm(subWord.realWord(), null);
                    sub.setOffset(subWord.getRowNum());
                    term.getSubword().add(sub);
                }
            }


            target.accept(term);
        }
    };


    /**
     * 最优路径选择器，子词平铺
     * 如果有子词，那么平铺，抛弃原有的组合词
     * 北京人民大学  =》 北京 人民 大学
     */
    public static WordTermCollector bestpath_subword_flat = (wordnet, wordPath, target) -> {
        Iterator<Vertex> vertexIterator = wordPath.iteratorBestPath();


        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();

            if (vertex.subWords != null) {
                for (Vertex subWord : vertex.subWords) {
                    WordTerm sub = new WordTerm(subWord.realWord(), null);
                    sub.setOffset(subWord.getRowNum());
                    target.accept(sub);
                }
            } else {
                WordTerm term = new WordTerm(vertex.realWord(), vertex.guessNature());
                //默认把空白的字符去除掉
                if (isWhiteSpace(term.word)) {
                    continue;
                }
                term.setOffset(vertex.getRowNum());

                target.accept(term);
            }

        }
    };


    /**
     * 索引分词。只有求得所有组合的可能性
     */
    public static WordTermCollector indexs_ = (wordnet, wordPath, target) -> {
        Iterator<Vertex> vertexIterator = wordPath.iteratorBestPath();

        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();

            if (vertex.subWords != null) {
                for (Vertex subWord : vertex.subWords) {
                    WordTerm sub = new WordTerm(subWord.realWord(), null);
                    sub.setOffset(subWord.getRowNum());
                    target.accept(sub);
                }
            } else {
                WordTerm term = new WordTerm(vertex.realWord(), vertex.guessNature());
                term.setOffset(vertex.getRowNum());

                //默认把空白的字符去除掉
                if (isWhiteSpace(term.word)) {
                    continue;
                }

                target.accept(term);
            }

        }
    };
}