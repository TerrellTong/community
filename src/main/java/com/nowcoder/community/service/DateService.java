package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DateService {
    @Autowired
    private RedisTemplate redisTemplate;

    //用于将Date装成字符串
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    //将IP记录到单日UV
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    //统计指定日期范围内的UV
    public long calculateUV(Date start,Date end){
        if(start == null || end == null)
            throw new IllegalArgumentException("参数不能为空");

        //整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        //日历
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            //获取某一天的key
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            //将某一天的key插入到日期集合中
            keyList.add(key);
            //日期加一天
            calendar.add(Calendar.DATE, 1);
        }

        //合并这些数据
        String rediskey = RedisKeyUtil.getUVKey(df.format(start),df.format(end));
        redisTemplate.opsForHyperLogLog().union(rediskey,keyList.toArray());

        //统计合并后的数据总数
        return redisTemplate.opsForHyperLogLog().size(rediskey);
    }

    //将指定用户记录到DAU中
    public void recordDAU(int userId){
        String rediskey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(rediskey,userId,true);
    }

    //统计指定日期范围内的DAU
    public long calculateDAU(Date start,Date end){
        if(start == null || end == null)
            throw new IllegalArgumentException("参数不能为空");

        //整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }

        //进行OR运算
        return (long)redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
