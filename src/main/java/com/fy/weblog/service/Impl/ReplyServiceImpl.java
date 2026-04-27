package com.fy.weblog.service.Impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.weblog.mapper.ReplyMapper;
import com.fy.weblog.model.dto.PageDTO;
import com.fy.weblog.model.dto.ReplyDTO;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.dto.UserDTO;
import com.fy.weblog.model.entity.Reply;
import com.fy.weblog.model.entity.User;
import com.fy.weblog.model.query.ReplyPageQuery;
import com.fy.weblog.model.vo.ReplyVO;
import com.fy.weblog.service.ReplyService;
import com.fy.weblog.service.UserService;
import com.fy.weblog.utils.UserHolder;
import com.fy.weblog.exception.BusinessException;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;

@Service
public class ReplyServiceImpl extends ServiceImpl<ReplyMapper, Reply> implements ReplyService {
    private static final String DATA_FIELD_NAME_LIKED_TIME = "likedTimes";
    private static final String DATA_FIELD_NAME_CREATE_TIME = "createTime";
    @Autowired
    //private ReplyMapper replyMapper;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 保存回答
    @Override
    public Result<Reply> saveReply(ReplyDTO replyDTO) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.数据转换
        Reply reply = BeanUtil.toBean(replyDTO, Reply.class);
        // 3.补充数据
        reply.setUserId(userId);
        reply.setArticleId(replyDTO.getArticleId());
        reply.setContent(replyDTO.getContent());
        reply.setReplyTimes(0);
        reply.setLikedTimes(0);
        reply.setCreateTime(LocalDateTime.now());
        reply.setUpdateTime(LocalDateTime.now());
        reply.setHidden(false);
        // 4.保存评论
        save(reply);
        return Result.ok(reply);
    }

    @Override
    public PageDTO<ReplyVO> replyPage(ReplyPageQuery query) {
        // 1. 校验参数
        if (query.getArticleId() == null) {
            throw new BusinessException("文章ID不能为空");
        }

        // 2. 确定排序规则
        OrderItem orderItem = query.getSortByLikes() ? 
            OrderItem.desc("liked_times") : OrderItem.desc("create_time");

        // 3. 分页查询数据库 (PO)
        Page<Reply> page = this.lambdaQuery()
                .eq(Reply::getArticleId, query.getArticleId())
                .page(query.toPage(orderItem));

        // 4. 数据转换：PO -> VO
        List<Reply> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        List<ReplyVO> voList = records.stream().map(record -> {
            ReplyVO vo = new ReplyVO();
            BeanUtil.copyProperties(record, vo);
            // 这里可以添加额外逻辑，如查询用户信息、判断当前登录人是否点过赞等
            return vo;
        }).collect(Collectors.toList());

        // 5. 返回统一的分页结果 DTO
        return PageDTO.of(page, voList);
    }

    //根据id查询评论
    @Override
    public ReplyVO queryReplyById(Long id) {
        //1.查询评论
        Reply reply = lambdaQuery()
                .eq(Reply::getId, id)
                .one();
        ReplyVO replyVO = BeanUtil.toBean(reply, ReplyVO.class);//将评论复制到vo中
        
        //2.设置用户信息
        //【userClient.数据集聚合】：批量获取当前页所有相关的用户ID，然后一次性去用户中心查回来
        //UserDTO userDTO = IUserService.queryUserById(replyVO.getUserId());
        //2.1. 批量查询用户信息
        Set<Long> userIds = new HashSet<>();
        userIds.add(reply.getUserId());
        // 如果有被回复者，也把 ID 加进来
        if(replyVO.getTargetUserId()!=null){
            userIds.add(replyVO.getTargetUserId());
        }
        // 直接调用本地 IService 的 listByIds 方法
        List<User> users = userService.listByIds(userIds);

        //2.2.将 User 集合转为 Map，方便后续 VO 填充
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        User user = userMap.get(reply.getUserId());
        replyVO.setUserIcon(user.getIcon());
        replyVO.setUserName(user.getNickName());
        replyVO.setUserId(user.getId());
        return replyVO;
    }

    @Override
    @Transactional // 开启事务，保证回复保存与计数更新原子性
    public Result<Reply> saveSonReply(ReplyDTO replyDTO) {
        // 1. 获取当前登录用户（直接拿对象避免后续重复查询）
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        
        // 2. 缓存查询优化
        String parentKey = "reply:info:" + replyDTO.getParentId();
        Map<Object, Object> parentMap = stringRedisTemplate.opsForHash().entries(parentKey);
        
        // 如果是空标记（防止穿透）
        if ("-1".equals(parentMap.get("id"))) {
            return Result.fail("评论不存在");
        }

        if (parentMap.isEmpty()) {
            Reply parent = getById(replyDTO.getParentId());
            if (parent == null) {
                // 设置空缓存，过期时间设短一点
                stringRedisTemplate.opsForHash().put(parentKey, "id", "-1");
                stringRedisTemplate.expire(parentKey, 5, TimeUnit.MINUTES);
                return Result.fail("父评论不存在或已被删除");
            }
            // 正常回填，建议转为 String
            parentMap = new HashMap<>();
            parentMap.put("id", parent.getId().toString());
            parentMap.put("articleId", parent.getArticleId().toString());
            stringRedisTemplate.opsForHash().putAll(parentKey, parentMap);
            stringRedisTemplate.expire(parentKey, 1, TimeUnit.HOURS); // 必须设过期时间
        }
        
        // 3. 构造并保存回复
        Reply reply = BeanUtil.copyProperties(replyDTO, Reply.class);
        reply.setUserId(userId);
        reply.setReplyTimes(0);
        reply.setLikedTimes(0);
        reply.setHidden(false);
        reply.setCreateTime(LocalDateTime.now());
        reply.setUpdateTime(LocalDateTime.now());
        save(reply);
        
        // 4. 更新父评论计数
        boolean updateCount = lambdaUpdate()
                .eq(Reply::getId, replyDTO.getParentId())
                .setSql("reply_times = reply_times + 1")
                .update();
        
        if (!updateCount) throw new RuntimeException("更新父评论计数失败");

        // 5. 组装返回对象 (VO)
        ReplyVO replyVO = BeanUtil.toBean(reply, ReplyVO.class);
        // 自己人的信息直接从 UserHolder 拿，减少一次 DB 查询
        replyVO.setUserName(user.getNickName());
        replyVO.setUserIcon(user.getIcon());
        
        // 只查询目标用户信息
        if (replyDTO.getUserId() != null) {
            User targetUser = userService.getById(replyDTO.getUserId());
            if (targetUser != null) {
                replyVO.setTargetUserName(targetUser.getNickName());
            }
        }
        
        return Result.ok(reply);
    }
}
