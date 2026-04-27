package com.fy.weblog.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.weblog.dto.Result;
import com.fy.weblog.entity.Category;
import com.fy.weblog.mapper.CategoryMapper;
import com.fy.weblog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public Result addCategory(Category category) {
        // 1. 校验分类名称
        if (category.getName() == null || category.getName().isEmpty()) {
            return Result.fail("分类名称不能为空");
        }

        // 2. 【修复点】校验分类别名。如果业务允许为空，可以改成：category.setCategoryAlias("");
        if (category.getCategoryAlias() == null || category.getCategoryAlias().isEmpty()) {
            return Result.fail("分类别名不能为空");
        }

        // 3. 处理父级 ID 默认值
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }

        // 4. 【修复点】处理创建人默认值。防止 create_user 字段也报没有默认值的错
        // 实际开发中应该从请求头或 Token 中获取当前登录用户的 ID
        if (category.getCreateUser() == null) {
            category.setCreateUser(1L);
        }

        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        this.save(category);
        return Result.ok("成功");
    }

    @Override
    public Result queryCategoriesByPage(Integer current, Integer size) {
        Page<Category> page = new Page<>(current, size);
        return Result.ok(this.page(page));
    }

    @Override
    public Result queryCategoryList() {
        List<Category> list = this.list();
        return Result.ok(list);
    }

    @Override
    public Result updateCategory(Category category) {
        if (category.getId() == null) {
            return Result.fail("分类id不能为空");
        }
        if (category.getName() == null || category.getName().isEmpty()) {
            return Result.fail("分类名称不能为空");
        }
        Category updateCategory = new Category();
        updateCategory.setId(category.getId());
        updateCategory.setName(category.getName());
        // 如果更新时允许改别名，也可以把别名 set 进去
        if (category.getCategoryAlias() != null) {
            updateCategory.setCategoryAlias(category.getCategoryAlias());
        }
        updateCategory.setUpdateTime(LocalDateTime.now());
        this.updateById(updateCategory);
        return Result.ok("成功");
    }

    @Override
    public Result deleteCategory(Long id) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        long count = this.count(wrapper);
        if (count > 0) {
            return Result.fail("存在子类，不允许删除");
        }
        this.removeById(id);
        return Result.ok("成功");
    }
}