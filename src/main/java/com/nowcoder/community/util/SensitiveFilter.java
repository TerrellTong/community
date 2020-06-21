package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //定义替换符
    private static final String REPLACEMENT = "***";

    //定义根节点
    private TrieNode root = new TrieNode();

    //初始化前就进行树的构造
    @PostConstruct
    public void init(){
        try (
                //getClassLoader()获得的是类加载器的路径，即target/classes下的文件
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ){
                String keyword;
                //每次读取一行，存到keyword
                while ((keyword = reader.readLine())!=null){
                    //将读到的敏感词添加到前缀树中
                    this.addKeyword(keyword);
                }
        }catch (IOException e){
            logger.error("加载敏感词文件失败:"+ e.getMessage());
        }
    }

    //将敏感词添加到前缀树中
    public void addKeyword(String keyword){
        TrieNode tempNode = root;
        for(int i=0;i<keyword.length();i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            //判断subNode是否为空
            if(subNode == null){
                //初始化一个subNode结点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            //指向子节点，进入下一个循环
            tempNode = subNode;

            //设置结束标志符
            if(i == keyword.length()-1)
                tempNode.setKeywordEnd(true);
        }
    }

    /*
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
    * */
    public String filter(String text){
        if(StringUtils.isBlank(text))
            return null;
        //指针1
        TrieNode tempNode = root;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        //为什么要让指针3为指标？因为指针3可以更快的到达尾部，减少循环
        while(position < text.length()){
            char c = text.charAt(position);

            //跳过符号
            //对于 ※赌※博※ 这样的特殊字符进行过滤
            //如果是特殊字符
            if(isSymbol(c)){
                //如果指针1 指向的root结点，则要存入这个特殊字符，并让指针2下走一步
                if(tempNode == root){
                    sb.append(c);
                    begin++;
                }
                //无论符号在中间还是开头，指针3都要向下走一步
                position++;
                continue;
            }

            //获取当前结点中下级结点含有字符变量c的结点
            tempNode = tempNode.getSubNode(c);
            //如果获取不到，则说明以指针2开头的不是关键词
            if(tempNode == null){
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置，且position和begin处于同一位置
                position = ++begin;
                //重新指向根节点
                tempNode = root;
            }else if (tempNode.isKeywordEnd()){
                //发现敏感词，并将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置,此时指针2，从指针3的后面一个开始
                begin = ++position;
                //重新指向根节点
                tempNode = root;
            }else{
                //检查下一个字符，此时正在前缀树进行遍历，移动指针3
                position++;
            }
        }
        //将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    //判断是否是特殊符号
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF 是东亚文字范围
        //isAsciiAlphanumeric 是否是普通字符，是普通字符返回true,特殊字符返回false
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    //构造一个前缀树,是树不是二叉树！！
    private class TrieNode{
        //关键词结束的标志
        private boolean isKeywordEnd = false;

        //存储所有的子节点(key是下级字符，Value是下级结点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        //通过字符获得子结点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
