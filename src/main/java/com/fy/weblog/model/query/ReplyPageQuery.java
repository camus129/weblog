package com.fy.weblog.model.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.weblog.model.entity.Reply;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class ReplyPageQuery {
    private Long articleId;
    private Integer pageNo = 1; // 查询的页码
    private Integer pageSize = 10; // 每页显示的记录数
    private Boolean sortByLikes = false; // 是否按点赞数排序

    // 核心：转换成 MyBatis Plus 的 Page 对象
    public <T> Page<T> toPage(OrderItem... items) {
        Page<T> page = new Page<>(pageNo, pageSize);
        if (items != null && items.length > 0) {
            for (OrderItem item : items) {
                if (item != null) page.addOrder(item);
            }
        } else {
            // 默认排序：按创建时间倒序
            page.addOrder(OrderItem.desc("create_time"));
        }
        return page;
    }
}