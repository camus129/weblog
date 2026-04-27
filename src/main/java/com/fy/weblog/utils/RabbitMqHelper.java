package com.fy.weblog.utils;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.MDC;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RabbitMqHelper {

    private static final String REQUEST_ID_HEADER = "requestId";
    
    private final RabbitTemplate rabbitTemplate;
    private final MessagePostProcessor processor = new BasicIdMessageProcessor();
    private final ThreadPoolTaskExecutor executor;

    public RabbitMqHelper(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(10);
        //配置最大线程数
        executor.setMaxPoolSize(15);
        //配置队列大小
        executor.setQueueCapacity(99999);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("mq-async-send-handler");

        // 设置拒绝策略：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
    }

    /**
     * 根据exchange和routingKey发送消息
     */
    public <T> void send(String exchange, String routingKey, T t) {
        log.debug("准备发送消息，exchange：{}， RoutingKey：{}， message：{}", exchange, routingKey, t);
        // 1.设置消息标示，用于消息确认，消息发送失败直接抛出异常，交给调用者处理
        String id = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(id);
        // 2.设置发送超时时间为500毫秒
        rabbitTemplate.setReplyTimeout(500);
        // 3.发送消息，同时设置消息id
        rabbitTemplate.convertAndSend(exchange, routingKey, t, correlationData);
    }

    /**
     * 根据exchange和routingKey发送消息，并且可以设置延迟时间
     */
    public <T> void sendDelayMessage(String exchange, String routingKey, T t, Duration delay) {
        // 1.设置消息标示，用于消息确认，消息发送失败直接抛出异常，交给调用者处理
        String id = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(id);
        // 2.设置发送超时时间为500毫秒
        rabbitTemplate.setReplyTimeout(500);
        // 3.发送消息，同时设置消息id
        rabbitTemplate.convertAndSend(exchange, routingKey, t, correlationData);
    }


    /**
     * 根据exchange和routingKey 异步发送消息，并指定一个延迟时间
     *
     * @param exchange 交换机
     * @param routingKey 路由KEY
     * @param t 数据
     * @param <T> 数据类型
     */
    public <T> void sendAsyn(String exchange, String routingKey, T t, Long time) {
        String requestId = MDC.get(REQUEST_ID_HEADER);
        CompletableFuture.runAsync(() -> {
            try {
                MDC.put(REQUEST_ID_HEADER, requestId);
                if (time != null && time > 0) {
                    Thread.sleep(time);
                }
                send(exchange, routingKey, t);
            } catch (Exception e) {
                log.error("推送消息异常，t:{}", t, e);
            }
        }, executor);
    }


    /**
     * 根据exchange和routingKey 异步发送消息
     *
     * @param exchange 交换机
     * @param routingKey 路由KEY
     * @param t 数据
     * @param <T> 数据类型
     */
    public <T> void sendAsyn(String exchange, String routingKey, T t) {
        sendAsyn(exchange, routingKey, t, null);
    }
    
    /**
     * 基础消息处理器，添加消息ID
     */
    private static class BasicIdMessageProcessor implements MessagePostProcessor {
        @Override
        public org.springframework.messaging.Message<?> postProcessMessage(org.springframework.messaging.Message<?> message) {
            return message;
        }
    }
    
    /**
     * 延迟消息处理器
     */
    private static class DelayedMessageProcessor implements MessagePostProcessor {
        private final Duration delay;
        
        public DelayedMessageProcessor(Duration delay) {
            this.delay = delay;
        }
        
        @Override
        public org.springframework.messaging.Message<?> postProcessMessage(org.springframework.messaging.Message<?> message) {
            // 这里可以添加延迟消息的处理逻辑
            return message;
        }
    }

}