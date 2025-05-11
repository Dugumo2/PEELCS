package com.graduation.peelcs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.graduation.peelcs.commen.Constant;
import com.graduation.peelcs.domain.po.ForumPosts;
import com.graduation.peelcs.domain.po.PostComments;
import com.graduation.peelcs.domain.po.Users;
import com.graduation.peelcs.domain.query.CommentQuery;
import com.graduation.peelcs.domain.vo.CommentVO;
import com.graduation.peelcs.mapper.PostCommentsMapper;
import com.graduation.peelcs.service.IPostCommentsService;
import com.graduation.peelcs.utils.redis.IRedisService;
import com.graduation.peelcs.utils.sensitivewords.ContentFilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 帖子评论表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostCommentsServiceImpl extends ServiceImpl<PostCommentsMapper, PostComments> implements IPostCommentsService {

    private final IRedisService redisService;
    private final ContentFilterService contentFilterService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(Long userId, Long postId, String content, Boolean isAnonymous) {
        // 参数验证
        if (userId == null || postId == null || !StringUtils.hasText(content)) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 过滤敏感词，自动替换为星号
        String filteredContent = contentFilterService.filterContent(content);
        
        // 检查帖子是否存在且已通过审核
        ForumPosts post = Db.lambdaQuery(ForumPosts.class).eq(ForumPosts::getId,postId).one();
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        
        if (!"approved".equals(post.getStatus())) {
            throw new IllegalArgumentException("帖子未通过审核");
        }
        
        // 创建评论（顶级评论）
        PostComments comment = new PostComments();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setRootCommentId(null); // 顶级评论
        comment.setToCommentId(null); // 顶级评论
        comment.setToUserNickname(null); // 顶级评论
        comment.setContent(filteredContent); // 使用过滤后的内容
        comment.setIsAnonymous(isAnonymous != null && isAnonymous);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        
        this.save(comment);

        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment,vo);
        String nickname = Db.lambdaQuery(Users.class).eq(Users::getId, userId).one().getNickname();
        vo.setNickname(nickname);
        
        // 增加用户积分（有每日限制）
        addCommentPoints(userId);
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostComments replyComment(Long userId, Long commentId, String content, Boolean isAnonymous) {
        // 参数验证
        if (userId == null || commentId == null || !StringUtils.hasText(content)) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 过滤敏感词，自动替换为星号
        String filteredContent = contentFilterService.filterContent(content);
        
        // 检查目标评论是否存在
        PostComments targetComment = this.getById(commentId);
        if (targetComment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        
        // 查找目标评论的用户
        Users targetUser = Db.lambdaQuery(Users.class).eq(Users::getId,targetComment.getUserId()).one();
        String targetNickname = targetUser != null ? targetUser.getNickname() : "未知用户";
        if (targetComment.getIsAnonymous()) {
            targetNickname = "匿名用户";
        }
        
        // 创建回复（子评论）
        PostComments reply = new PostComments();
        reply.setPostId(targetComment.getPostId());
        reply.setUserId(userId);
        
        // 设置回复关系
        if (targetComment.getRootCommentId() == null) {
            // 如果目标评论是顶级评论，则此回复的rootCommentId为目标评论ID
            reply.setRootCommentId(targetComment.getId());
        } else {
            // 如果目标评论是子评论，则此回复的rootCommentId与目标评论相同
            reply.setRootCommentId(targetComment.getRootCommentId());
        }
        
        reply.setToCommentId(targetComment.getId());
        reply.setToUserNickname(targetNickname);
        reply.setContent(filteredContent); // 使用过滤后的内容
        reply.setIsAnonymous(isAnonymous != null && isAnonymous);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());
        
        this.save(reply);
        
        // 增加用户积分（有每日限制）
        addCommentPoints(userId);
        
        return reply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long userId, Long commentId) {
        // 参数验证
        if (userId == null || commentId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 获取评论
        PostComments comment = this.getById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        
        // 检查权限，只有管理员或自己发的评论可以删除
        Users user = Db.lambdaQuery(Users.class).eq(Users::getId,userId).one();
        
        if (!"admin".equals(user.getRole()) && !userId.equals(comment.getUserId())) {
            throw new IllegalArgumentException("无权限删除该评论");
        }
        
        // 如果是顶级评论，需要同时删除其下的所有子评论
        if (comment.getRootCommentId() == null) {
            // 删除子评论
            LambdaQueryWrapper<PostComments> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PostComments::getRootCommentId, commentId);
            this.remove(wrapper);
        }
        
        // 删除当前评论
        return this.removeById(commentId);
    }

    @Override
    public List<CommentVO> getPostComments(Long postId, Integer page, Integer size) {
        // 参数验证
        if (postId == null) {
            return new ArrayList<>();
        }
        
        // 默认值处理
        page = page == null || page < 1 ? 1 : page;
        size = size == null || size < 1 ? 20 : size;
        
        // 查询顶级评论
        LambdaQueryWrapper<PostComments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostComments::getPostId, postId)
               .isNull(PostComments::getRootCommentId) // 顶级评论
               .orderByDesc(PostComments::getCreatedAt);
        
        // 分页查询
        Page<PostComments> commentPage = new Page<>(page, size);
        IPage<PostComments> commentsPage = this.page(commentPage, wrapper);
        
        // 转换为VO并查询每个顶级评论的部分回复
        List<CommentVO> commentVOs = commentsPage.getRecords().stream()
                .map(comment -> {
                    CommentVO vo = convertToCommentVO(comment);
                    
                    // 查询每个顶级评论的前3条回复
                    List<CommentVO> replies = getCommentReplies(comment.getId(), 1, 3);
                    vo.setReplies(replies);
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        return commentVOs;
    }

    @Override
    public List<CommentVO> getCommentReplies(Long commentId, Integer page, Integer size) {
        // 参数验证
        if (commentId == null) {
            return new ArrayList<>();
        }
        
        // 默认值处理
        page = page == null || page < 1 ? 1 : page;
        size = size == null || size < 1 ? 10 : size;
        
        // 查询子评论
        LambdaQueryWrapper<PostComments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostComments::getRootCommentId, commentId) // 指定顶级评论的子评论
               .orderByAsc(PostComments::getCreatedAt); // 按时间正序排列
        
        // 分页查询
        Page<PostComments> replyPage = new Page<>(page, size);
        IPage<PostComments> repliesPage = this.page(replyPage, wrapper);
        
        // 转换为VO
        return repliesPage.getRecords().stream()
                .map(this::convertToCommentVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CommentVO> getComments(CommentQuery query) {
        if (query == null) {
            return new ArrayList<>();
        }
        
        // 构建查询条件
        LambdaQueryWrapper<PostComments> wrapper = new LambdaQueryWrapper<>();
        
        // 根据帖子ID查询
        if (query.getPostId() != null) {
            wrapper.eq(PostComments::getPostId, query.getPostId());
        }

        // 根据根评论ID查询（子评论）
        if (query.getRootCommentId() != null) {
            wrapper.eq(PostComments::getRootCommentId, query.getRootCommentId());
        } else {
            // 如果没有指定根评论ID，则查询顶级评论
            wrapper.isNull(PostComments::getRootCommentId);
        }

        // 根据用户ID查询
        if (query.getUserId() != null) {
            wrapper.eq(PostComments::getUserId, query.getUserId());
        }

        // 分页设置
        Long pageNo = query.getPageNo() == null || query.getPageNo() < 1 ? 1L : query.getPageNo();
        Long pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20L : query.getPageSize();

        // 排序设置
        if (StringUtils.hasText(query.getSortBy())) {
            if ("createdAt".equals(query.getSortBy())) {
                if (Boolean.TRUE.equals(query.getIsAsc())) {
                    wrapper.orderByAsc(PostComments::getCreatedAt);
                } else {
                    wrapper.orderByDesc(PostComments::getCreatedAt);
                }
            }
        } else {
            // 默认按创建时间降序
            wrapper.orderByDesc(PostComments::getCreatedAt);
        }

        // 分页查询
        Page<PostComments> commentPage = new Page<>(pageNo, pageSize);
        IPage<PostComments> commentsPage = this.page(commentPage, wrapper);

        // 转换为VO，并处理顶级评论的回复
        List<CommentVO> commentVOs = commentsPage.getRecords().stream()
                .map(comment -> {
                    CommentVO vo = convertToCommentVO(comment);

                    // 如果是顶级评论，查询其回复
                    if (comment.getRootCommentId() == null) {
                        // 默认查询3条回复
                        List<CommentVO> replies = getCommentReplies(comment.getId(), 1, 3);
                        vo.setReplies(replies);
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        return commentVOs;
    }
    
    /**
     * 转换为评论VO
     */
    private CommentVO convertToCommentVO(PostComments comment) {
        if (comment == null) {
            return null;
        }
        
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        
        // 查询作者信息（非匿名）
        if (!comment.getIsAnonymous()) {
            Users user = Db.lambdaQuery(Users.class).eq(Users::getId,comment.getUserId()).one();
            if (user != null) {
                vo.setNickname(user.getNickname());
            }
        } else {
            vo.setNickname("匿名用户");
        }
        
        vo.setReplies(new ArrayList<>());
        
        return vo;
    }
    
    /**
     * 给用户增加评论积分（有每日限制）
     *
     * @param userId 用户ID
     */
    private void addCommentPoints(Long userId) {
        if (userId == null) {
            return;
        }
        
        // 构造当日评论积分记录的键
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String pointsKey = Constant.RedisKey.COMMENT_POINTS_KEY_PREFIX + userId + ":" + today;
        
        // 获取当日已获得积分的次数
        Integer count = redisService.getValue(pointsKey);
        count = count == null ? 0 : count;
        
        // 如果未达到限制，增加积分
        if (count < Constant.PointsSettings.DAILY_POINTS_LIMIT) {
            // 增加用户积分
            Users user = Db.lambdaQuery(Users.class).eq(Users::getId,userId).one();
            if (user != null) {
                user.setPoints(user.getPoints() + Constant.PointsSettings.COMMENT_POINTS);
                user.setUpdatedAt(LocalDateTime.now());
                Db.lambdaUpdate(Users.class)
                   .eq(Users::getId, userId)
                   .set(Users::getPoints, user.getPoints())
                   .set(Users::getUpdatedAt, user.getUpdatedAt())
                   .update();
                
                // 更新计数
                redisService.setValue(pointsKey, count + 1, 24 * 60 * 60 * 1000L); // 设置24小时过期
            }
        }
    }
}
