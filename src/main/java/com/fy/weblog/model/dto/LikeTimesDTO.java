package com.fy.weblog.model.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 记录单个业务的点赞数
 */
@Data
@AllArgsConstructor(staticName = "of")//【of创建对象，和new区别：of方法是静态方法 可用于缓存，new方法是实例方法】
public class LikeTimesDTO implements Serializable {
    private Long bizId;
    private Long likeTimes;
}
