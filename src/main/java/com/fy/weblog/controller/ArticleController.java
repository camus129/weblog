package com.fy.weblog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.entity.Article;
import com.fy.weblog.service.ArticleService;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/article")
public class ArticleController {
    @Resource
    private ArticleService articleService;

    // 发布文章
    @PostMapping
    public Result saveArticle(@RequestBody Article article) {
        return articleService.saveArticle(article);
    }

    // 查看单个文章详情
    @GetMapping("/{id}")
    public Result queryArticleById(@PathVariable Integer id) {
        return articleService.queryArticleById(id);
    }

    // 查询热门文章（多个）
    @GetMapping("/hot")
    public Result queryHotArticles(@RequestParam(defaultValue = "1") Integer current) {
        return articleService.queryHotArticles(current);
    }
    
    // 点赞前5人
    @GetMapping("/likes/{id}")
    public Result queryArticleLikes(@PathVariable Integer id) {
        return articleService.queryArticleLikes(id);
    }

    // 收藏
    @PostMapping("/collect/{id}")
    public Result collectArticle(@PathVariable Integer id) {
        return articleService.collectArticle(id);
    }

}
