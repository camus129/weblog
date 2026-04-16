package com.fy.weblog.service.Impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.weblog.mapper.ArticleMapper;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.dto.UserDTO;
import com.fy.weblog.model.entity.Article;
import com.fy.weblog.model.entity.User;
import com.fy.weblog.service.ArticleService;
import com.fy.weblog.service.UserService;
import com.fy.weblog.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    
    public ArticleServiceImpl(UserService userService, StringRedisTemplate stringRedisTemplate) {
        this.userService = userService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Result saveArticle(Article article) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        article.setCreateUser(user.getId());
        // 保存文章
        save(article);
        // 返回id
        return Result.ok(article.getId());
    }

    @Override
    public Result queryArticleById(Integer id) {
        // 1.查询文章
        Article article = getById(id);
        if (article == null) {
            return Result.fail("文章不存在");
        }
        // 2.补充作者信息
        Long userId = article.getCreateUser();
        User user = userService.getById(userId);
        article.setName(user.getNickName());
        article.setIcon(user.getIcon());
        // 3.返回结果
        return Result.ok(article);
    }

    @Override
    public Result queryHotArticles(Integer current) {
        // 创建查询条件，按点赞数降序排序
        LambdaQueryWrapper<Article> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(Article::getLiked);
        // 分页查询
        Page<Article> page = new Page<>(current, 10);
        page = this.page(page, lqw);
        // 获取当前页的文章列表
        List<Article> articles = page.getRecords();
        // 补充作者信息
        queryArticleUser(articles);

        return Result.ok(articles);
    }
    
    // 补充文章作者信息
    private void queryArticleUser(List<Article> articles) {
        articles.forEach(article -> {
            Long userId = article.getCreateUser();
            User user = userService.getById(userId);
            article.setName(user.getNickName());
            article.setIcon(user.getIcon());
        });
    }

    @Override
    public Result queryArticleLikes(Integer id) {
        // 1.查询top5的点赞用户
        String key = "article:liked:" + id;
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 2.解析出其中的用户id
        List<Long> ids = top5.stream()
            .map(Long::valueOf)
            .collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);

        // 3.根据用户id查询用户
        List<UserDTO> userDTOs = userService.query()
            .in("id", ids).last("ORDER BY FIELD(id," + idStr + ") LIMIT 5").list()
            .stream()
            .map(user -> (UserDTO) BeanUtil.copyProperties(user, UserDTO.class))
            .collect(Collectors.toList());

        return Result.ok(userDTOs);
    }

    @Override
    public Result collectArticle(Integer id) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.ok("未登录，收藏失败");
        }
        // 2.判断当前登录用户是否已收藏
        String key = "article:collect:" + id;
        Double isMember = stringRedisTemplate.opsForZSet()
            .score(key, userId.toString());
        if (isMember == null) {
            // 3.如果未收藏，可以收藏
            // 3.1.数据库收藏数+1
            boolean updateSucess = update().setSql("collected = collected + 1").eq("id", id).update();
            // 3.2.保存用户到redis的set集合
            if (updateSucess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 4.如果已收藏，取消收藏
            // 4.1.数据库收藏-1
            boolean updateSucess = update().setSql("collected = collected - 1").eq("id", id).update();
            // 4.2.把用户从redis的set集合移除
            if (updateSucess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
            return Result.ok("取消收藏成功");
        }

        return Result.ok("收藏成功");
    }
}
