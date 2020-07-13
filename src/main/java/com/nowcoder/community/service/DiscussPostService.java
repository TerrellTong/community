package com.nowcoder.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Resource
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //Caffeine核心接口：Cache，子接口LoadingCache(同步缓存)，AsyncLoadingCache(异步缓存)

    //贴子列表缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //帖子总数缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        //初始化帖子列表缓存,key:offset + : + limit
        postListCache = Caffeine.newBuilder()
                        .maximumSize(maxSize)
                        .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                        .build(
                                //new 接口的作用：当从缓冲取数据时，Caffeine会从缓存看一下有没有数据
                                //如果有，则直接返回，如果没有，该怎么查数据然后存到缓存里
                                //key是由调用缓存的get方法传过来的
                                new CacheLoader<String, List<DiscussPost>>() {
                            @Nullable
                            @Override
                            public List<DiscussPost> load(@NonNull String key) throws Exception {
                                if(key == null || key.length() == 0)
                                    throw new IllegalArgumentException("参数错误!");

                                String[] params = key.split(":");
                                if(params == null || params.length != 2)
                                    throw new IllegalArgumentException("参数错误!");

                                int offset = Integer.valueOf(params[0]);
                                int limit = Integer.valueOf(params[1]);

                                //可以继续加二级缓存
                                //....

                                logger.debug("load post list from DB.");
                                return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                            }
                        });
        //初始化贴子总数缓存
        postRowsCache = Caffeine.newBuilder()
                        .maximumSize(maxSize)
                        .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                        .build(new CacheLoader<Integer, Integer>() {
                            @Nullable
                            @Override
                            public Integer load(@NonNull Integer key) throws Exception {
                                logger.debug("load post rows from DB.");
                                return discussPostMapper.selectDiscussPostsRows(key);
                            }
                        });
    }

    public List<DiscussPost> findDiscussPost(int userId,int offset,int limit,int orderMode){
        //直接从缓存中获取结果
        if(userId == 0 && orderMode == 1)
            return postListCache.get(offset + ":" + limit);

        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    public int findDiscussPostRows(int userId){
        if(userId == 0)
            return postRowsCache.get(userId);

        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostsRows(userId);
    }

    //根据用户id插入DiscussPost
    public int addDiscussPost(DiscussPost post){
        if(post == null)
            throw new IllegalArgumentException("参数不能为空!");

        //转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    //查询用户
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    //更新贴子type
    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }

    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id,status);
    }

    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id,score);
    }
}
