package com.fy.weblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fy.weblog.dto.Result;
import com.fy.weblog.entity.Category;

public interface CategoryService extends IService<Category> {

    Result addCategory(Category category);

    Result queryCategoriesByPage(Integer current, Integer size);

    Result queryCategoryList();

    Result updateCategory(Category category);

    Result deleteCategory(Long id);
}
