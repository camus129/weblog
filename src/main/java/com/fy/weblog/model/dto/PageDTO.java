package com.fy.weblog.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class PageDTO<T> {
    private Long total;
    private Long pages;
    private List<T> list;

    /**
     * 核心转换方法：将 MP 的 Page 对象和已转换好的 VO 列表封装
     */
    public static <T, R> PageDTO<T> of(Page<R> page, List<T> list) {
        PageDTO<T> dto = new PageDTO<>();
        dto.setTotal(page.getTotal());
        dto.setPages(page.getPages());
        // 如果 list 是 null，返回空集合
        dto.setList(list == null ? Collections.emptyList() : list);
        return dto;
    }

    /**
     * 快捷方法：直接从 MP 的 Page 对象提取数据（适用于 PO 和 VO 是同一个类的情况）
     */
    public static <T> PageDTO<T> of(Page<T> page) {
        return of(page, page.getRecords());
    }

    /**
     * 解决你之前报错的方法：返回空分页结果
     */
    public static <T, R> PageDTO<T> empty(Page<R> page) {
        PageDTO<T> dto = new PageDTO<>();
        dto.setTotal(page.getTotal());
        dto.setPages(page.getPages());
        dto.setList(Collections.emptyList());
        return dto;
    }
}