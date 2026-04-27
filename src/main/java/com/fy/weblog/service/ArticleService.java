package com.fy.weblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.entity.Article;

public interface ArticleService extends IService<Article> {

    Result saveArticle(Article article);
    
    Result queryArticleById(Integer id);

    Result queryHotArticles(Integer current);

    Result queryArticleLikes(Integer id);

    Result collectArticle(Integer id);
}
