package com.fy.weblog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fy.weblog.dto.Result;
import com.fy.weblog.dto.UserDTO;
import com.fy.weblog.entity.Blog;
import com.fy.weblog.service.BlogSerivce;
import com.fy.weblog.service.UserService;
import com.fy.weblog.utils.UserHolder;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private BlogSerivce blogSerivce;
    @Resource
    private UserService userService;

    //发布文章
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        //获取登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        blog.setUserId(user.getId());
        //保存推文
        blogSerivce.save(blog); //IService<Blog>的save方法，实现保存功能
        //返回id
        return Result.ok(blog.getId());
    }

    //查看单个文章详情
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable Long id) {
        return blogSerivce.queryBlogById(id);
    }

    //查询热门文章（多个）
    @GetMapping("/hot")
    public Result queryHotBlogs(@RequestParam(defaultValue = "1") Integer current) {
        //defaultValue = "1"，默认current第一页
        return blogSerivce.queryHotBlog(current);
    }
    
    //点赞
    @PostMapping("/like/{id}")
    public Result likeBlog(@PathVariable Long id) {
        return blogSerivce.likeBlog(id);
    }

    //点赞前5人
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable Long id) {
        return blogSerivce.queryBlogLikes(id);
    }

    //收藏
    @PostMapping("/collect/{id}")
    public Result collectBlog(@PathVariable Long id) {
        return blogSerivce.collectBlog(id);
    }

    //评论
    // @PostMapping("/comment/{id}")
    // public Result commentBlog(@PathVariable Long id) {
    //     return blogSerivce.commentBlog(id);
    // }

    //评论前5人（根据点赞数量）
    // @GetMapping("/comments/{id}")
    // public Result queryBlogComments(@PathVariable Long id) {
    //     return blogSerivce.queryBlogComments(id);
    // }

}
