package com.fy.weblog.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fy.weblog.dto.Result;
import com.fy.weblog.entity.Blog;

@Service
public interface BlogSerivce extends IService<Blog>{

    Result queryBlogById(Long id);

    Result queryHotBlog(Integer current);

    // Result likeBlog(Long id);

    Result queryBlogLikes(Long id);

    Result collectBlog(Long id);
    
}
