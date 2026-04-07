package com.fy.weblog.service.Impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.weblog.dto.Result;
import com.fy.weblog.dto.UserDTO;
import com.fy.weblog.entity.Blog;
import com.fy.weblog.entity.User;
import com.fy.weblog.mapper.BlogMapper;
import com.fy.weblog.service.BlogSerivce;
import com.fy.weblog.service.UserService;
import com.fy.weblog.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper,Blog> implements BlogSerivce{
    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    
    public BlogServiceImpl(UserService userService, StringRedisTemplate stringRedisTemplate) {
        this.userService = userService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //根据id查询文章
    @Override
    public Result queryBlogById(Long id) {
        //1.查询blog
        Blog blog = getById(id);
        if(blog == null){
            return Result.fail("文章不存在");
        }
        //2.补充作者信息
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
        //3.返回结果
        return Result.ok(blog);
    }

    @Override
    public Result queryHotBlog(Integer current) {//参数current：当前页
        //创建全站博客，并按点赞数降序排序
        LambdaQueryWrapper<Blog> lqw = new LambdaQueryWrapper<>();//MyBatis-Plus 的查询条件构造器
        lqw.orderByDesc(Blog::getLiked);//按点赞数降序排序
        //分页查询：MyBatis-Plus自动计算，生成分页SQL
        Page<Blog> page = new Page<>(current,10);//分页对象：10，每页显示的文章数量
        page = this.page(page,lqw);//分页查询
        //获取当前页的文章列表
        List<Blog> blogs = page.getRecords();
        //补充作者信息
        queryBlogUser(blogs);

        return Result.ok(blogs);
    }
    //得到每个文章作者信息
    private void queryBlogUser(List<Blog> blogs) {
        blogs.forEach(blog -> { //等价于 for (Blog blog : blogs)
            Long userId = blog.getUserId();
            User user = userService.getById(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
        });
    }

    @Override
    public Result likeBlog(Long id) {//博客的id
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        if(userId == null){
            //用户未登录，无需查询是否点赞
            return Result.ok("未登录，点赞失败");
        }
        //2.判断当前登录用户是否已点赞
        String key = "blog:liked:"+id;
        Double isMember = stringRedisTemplate.opsForZSet()//ZSet有序集合排序
            .score(key, userId.toString());//查询用户id是否在set点赞列表里
        if(isMember == null){
            //3.如果未点赞，可以点赞
            //3.1.数据库点赞数+1
            boolean updateSucess = update().setSql("liked = liked + 1").eq("id",id).update();
            //3.2.保存用户到redis的set集合
            if(updateSucess){   //数据更新成功才能更新redis
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
                //ZSet有序集合；System.currentTimeMillis()当前时间戳
            }
        }else{
            //4.如果已点赞，取消点赞
            //4.1.数据库点赞-1
            boolean updateSucess = update().setSql("liked = liked - 1").eq("id",id).update();
            //4.2.把用户从redis的set集合移除
            if(updateSucess){ 
                stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            }
        }

        return Result.ok("点赞成功");
    }

    @Override
    public Result queryBlogLikes(Long id) {//博客id
        //1.查询top5的点赞用户
        String key = "blog:liked:"+id;
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key,0,4);//range:索引
        if(top5 == null || top5.isEmpty()){//检查结果，没有的话返回空列表
            return Result.ok(Collections.emptyList());//返回的是个什么东西？？？？？
        }
        //2.解析出其中的用户id
        List<Long> ids = top5.stream()//将集合转化为流【？？？】
            .map(Long::valueOf)//每个字符串id转为Long类型
            .collect(Collectors.toList());//处理后的流收集为List
        String idStr = StrUtil.join(",", ids);

        //3.根据用户id查询用户
        List<UserDTO> userDTOs = userService.query()
            .in("id",ids).last("ORDER BY FIELD(id,"+idStr+") LIMIT 5").list()//in查询是倒着的，我们就要last
            .stream()
            .map(user -> (UserDTO)BeanUtil.copyProperties(user,UserDTO.class))//->映射到/变成
            .collect(Collectors.toList());

        return Result.ok(userDTOs);
    
    }





   }
