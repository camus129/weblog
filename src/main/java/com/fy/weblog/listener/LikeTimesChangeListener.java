package com.fy.weblog.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeTimesChangeListener {

    // 文章点赞相关服务（后续实现）
    // private final ArticleService articleService;

    // 注释掉评论点赞相关的业务
    /*
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "qa.liked.times.queue", durable = "true"),
            exchange = @Exchange(name = LIKE_RECORD_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = QA_LIKED_TIMES_KEY
    ))
    public void listenReplyLikedTimesChange(LikedTimesDTO likedTimesDTO) {
        log.debug("监听到回答或评论的点赞数变更的消息: {}，点赞数：{}",
                likedTimesDTO.getBizId(), likedTimesDTO.getLikedTimes());

        InteractionReply r = new InteractionReply();
        r.setId(likedTimesDTO.getBizId());
        r.setLikedTimes(likedTimesDTO.getLikedTimes());
        replyService.updateById(r);
    }
    */

    // 文章点赞相关的监听器（后续实现）
    /*
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "article.liked.times.queue", durable = "true"),
            exchange = @Exchange(name = LIKE_RECORD_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = "article.times.changed"
    ))
    public void listenArticleLikedTimesChange(LikedTimesDTO likedTimesDTO) {
        log.debug("监听到文章的点赞数变更的消息: {}，点赞数：{}",
                likedTimesDTO.getBizId(), likedTimesDTO.getLikedTimes());

        // 更新文章的点赞数
        // articleService.updateLikedTimes(likedTimesDTO.getBizId(), likedTimesDTO.getLikedTimes());
    }
    */
}
