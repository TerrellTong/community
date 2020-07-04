package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
//<处理的实体类是谁，主键的类型是什么>
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {

}
