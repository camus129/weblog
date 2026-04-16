package com.fy.weblog.constants;

/**
 * 交换机名称
 */
public interface MqConstants {
    /*支付有关的交换机*/
    String PAY_EXCHANGE = "pay.topic";
    /*交易服务延迟任务交换机*/
    String TRADE_DELAY_EXCHANGE = "trade.delay.topic";

    /*点赞记录有关的交换机*/
    String LIKE_RECORD_EXCHANGE = "like.record.topic";

    /**
     * RoutingKey:告诉交换机应该把消息发送到哪个队列
     */
    interface Keys {
        /*点赞的RoutingKey*/
        String LIKED_TIMES_KEY_TEMPLATE = "{}.times.changed"; 
    }
}
	
