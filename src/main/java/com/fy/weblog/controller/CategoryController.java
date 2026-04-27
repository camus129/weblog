package com.fy.weblog.controller;

import com.fy.weblog.dto.Result;
import com.fy.weblog.entity.Category;
import com.fy.weblog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @PostMapping("/add")
    public Result addCategory(@RequestBody Category category) {
        return categoryService.addCategory(category);
    }

    @GetMapping("/page")
    public Result queryCategoriesByPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "5") Integer size) {
        return categoryService.queryCategoriesByPage(current, size);
    }

    @GetMapping("/list")
    public Result queryCategoryList() {
        return categoryService.queryCategoryList();
    }

    @PostMapping("/update")
    public Result updateCategory(@RequestBody Category category) {
        return categoryService.updateCategory(category);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}
