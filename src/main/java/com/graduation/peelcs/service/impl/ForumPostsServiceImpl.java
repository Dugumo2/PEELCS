package com.graduation.peelcs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.graduation.peelcs.commen.Constant;
import com.graduation.peelcs.domain.po.ForumPosts;
import com.graduation.peelcs.domain.po.PostComments;
import com.graduation.peelcs.domain.po.Users;
import com.graduation.peelcs.domain.query.CommentQuery;
import com.graduation.peelcs.domain.query.PostQuery;
import com.graduation.peelcs.domain.vo.CommentVO;
import com.graduation.peelcs.domain.vo.PostDetailVO;
import com.graduation.peelcs.domain.vo.PostVO;
import com.graduation.peelcs.mapper.ForumPostsMapper;
import com.graduation.peelcs.mapper.PostCommentsMapper;
import com.graduation.peelcs.mapper.UsersMapper;
import com.graduation.peelcs.service.IForumPostsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graduation.peelcs.service.IPostCommentsService;
import com.graduation.peelcs.service.IUsersService;
import com.graduation.peelcs.utils.redis.IRedisService;
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
 * 论坛帖子表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForumPostsServiceImpl extends ServiceImpl<ForumPostsMapper, ForumPosts> implements IForumPostsService {

    private final IPostCommentsService postCommentsService;
    private final IRedisService redisService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPosts createPost(Long userId, String title, String content, Boolean isAnonymous) {
        // 参数验证
        if (userId == null || !StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 创建帖子
        ForumPosts post = new ForumPosts();
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setIsAnonymous(isAnonymous != null && isAnonymous);
        post.setIsOfficial(false); // 非官方帖子
        post.setStatus("pending"); // 默认状态为待审核
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        
        this.save(post);
        
        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPosts createOfficialPost(Long userId, String title, String content) {
        // 参数验证
        if (userId == null || !StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new IllegalArgumentException("参数不完整");
        }

        
        // 创建官方帖子
        ForumPosts post = new ForumPosts();
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setIsAnonymous(false); // 官方帖子不匿名
        post.setIsOfficial(true); // 官方帖子
        post.setStatus("approved"); // 官方帖子直接通过审核
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        
        this.save(post);
        
        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPosts reviewPost(Long userId, Long postId, String status) {
        // 参数验证
        if (userId == null || postId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 状态只能是approved或rejected
        if (!"approved".equals(status) && !"rejected".equals(status)) {
            throw new IllegalArgumentException("状态值无效");
        }

        
        // 获取帖子
        ForumPosts post = this.getById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        
        // 只有待审核的帖子可以被审核
        if (!"pending".equals(post.getStatus())) {
            throw new IllegalArgumentException("该帖子已审核");
        }
        
        // 更新帖子状态
        post.setStatus(status);
        post.setUpdatedAt(LocalDateTime.now());
        
        this.updateById(post);
        
        // 如果审核通过，给发帖用户增加积分（有每日限制）
        if ("approved".equals(status)) {
            addPostPoints(post.getUserId());
        }
        
        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePost(Long userId, Long postId) {
        // 参数验证
        if (userId == null || postId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 获取帖子
        ForumPosts post = this.getById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }

        // 检查权限，只有管理员或自己发的帖子可以删除
        Users user = Db.lambdaQuery(Users.class).eq(Users::getId, userId).one();
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        if (!"admin".equals(user.getRole()) && !userId.equals(post.getUserId())) {
            throw new IllegalArgumentException("无权限删除该帖子");
        }
        
        // 删除帖子和相关评论
        // 先删除评论
        LambdaQueryWrapper<PostComments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostComments::getPostId, postId);
        postCommentsService.remove(wrapper);
        
        // 再删除帖子
        return this.removeById(postId);
    }

    @Override
    public List<PostVO> getPosts(PostQuery query) {
        if (query == null) {
            query = new PostQuery();
        }
        
        // 构建查询条件
        LambdaQueryWrapper<ForumPosts> wrapper = new LambdaQueryWrapper<>();
        
        // 查询条件：帖子状态
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(ForumPosts::getStatus, query.getStatus());
        } else {
            // 默认只查询已通过审核的帖子
            wrapper.eq(ForumPosts::getStatus, "approved");
        }
        
        // 查询条件：是否官方帖子
        if (query.getIsOfficial() != null) {
            wrapper.eq(ForumPosts::getIsOfficial, query.getIsOfficial());
        }
        
        // 查询条件：用户ID
        if (query.getUserId() != null) {
            wrapper.eq(ForumPosts::getUserId, query.getUserId());
        }
        
        // 查询条件：标题关键字
        if (StringUtils.hasText(query.getTitleKeyword())) {
            wrapper.like(ForumPosts::getTitle, query.getTitleKeyword());
        }
        
        // 构建分页对象
        Long pageNo = query.getPageNo() == null || query.getPageNo() < 1 ? 1L : query.getPageNo();
        Long pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10L : query.getPageSize();
        Page<ForumPosts> page = new Page<>(pageNo, pageSize);
        
        // 添加排序条件
        if (StringUtils.hasText(query.getSortBy())) {
            // 根据字段名进行排序
            if ("createdAt".equals(query.getSortBy())) {
                if (Boolean.TRUE.equals(query.getIsAsc())) {
                    wrapper.orderByAsc(ForumPosts::getCreatedAt);
                } else {
                    wrapper.orderByDesc(ForumPosts::getCreatedAt);
                }
            } else if ("updatedAt".equals(query.getSortBy())) {
                if (Boolean.TRUE.equals(query.getIsAsc())) {
                    wrapper.orderByAsc(ForumPosts::getUpdatedAt);
                } else {
                    wrapper.orderByDesc(ForumPosts::getUpdatedAt);
                }
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(ForumPosts::getCreatedAt);
        }
        
        // 执行分页查询
        IPage<ForumPosts> postsPage = this.page(page, wrapper);
        
        // 转换为VO列表
        return postsPage.getRecords().stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());
    }

    @Override
    public PostDetailVO getPostDetail(Long postId, Long userId, boolean isAdmin) {
        // 参数验证
        if (postId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 获取帖子
        ForumPosts post = this.getById(postId);
        if (post == null) {
            return null; // 帖子不存在
        }
        
        // 权限检查
        // 1. 如果帖子已审核通过，任何人都可以查看
        // 2. 如果帖子未审核通过，只有发布者自己或管理员可以查看
        if (!"approved".equals(post.getStatus())) {
            if (!isAdmin && (userId == null || !userId.equals(post.getUserId()))) {
                return null; // 没有权限查看未审核的帖子
            }
        }
        
        // 转换为详情VO
        PostDetailVO detailVO = convertToPostDetailVO(post);
        
        // 获取顶级评论，使用CommentQuery
        CommentQuery commentQuery = new CommentQuery();
        commentQuery.setPostId(postId);
        commentQuery.setPageNo(1L);
        commentQuery.setPageSize(20L);
        commentQuery.setSortBy("createdAt");
        commentQuery.setIsAsc(false);
        
        List<CommentVO> topComments = postCommentsService.getComments(commentQuery);
        detailVO.setComments(topComments);
        
        return detailVO;
    }
    
    /**
     * 转换为帖子列表VO
     */
    private PostVO convertToPostVO(ForumPosts post) {
        if (post == null) {
            return null;
        }
        
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        
        // 处理内容字段 - 如果超过200个字符则截断
        if (StringUtils.hasText(vo.getContent()) && vo.getContent().length() > 200) {
            vo.setContent(vo.getContent().substring(0, 200) + "...");
        }
        
        // 查询作者信息（非匿名）
        if (!post.getIsAnonymous()) {
            Users user = Db.lambdaQuery(Users.class).eq(Users::getId,post.getUserId()).one();
            if (user != null) {
                vo.setNickname(user.getNickname());
            }
        } else {
            vo.setNickname("匿名用户");
        }
        
        // 查询评论数量
        LambdaQueryWrapper<PostComments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostComments::getPostId, post.getId());
        int commentCount = (int)postCommentsService.count(wrapper);
        
        vo.setCommentCount(commentCount);
        
        return vo;
    }
    
    /**
     * 转换为帖子详情VO
     */
    private PostDetailVO convertToPostDetailVO(ForumPosts post) {
        if (post == null) {
            return null;
        }
        
        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);
        
        // 查询作者信息（非匿名）
        if (!post.getIsAnonymous()) {
            Users user = Db.lambdaQuery(Users.class).eq(Users::getId,post.getUserId()).one();
            if (user != null) {
                vo.setNickname(user.getNickname());
            }
        } else {
            vo.setNickname("匿名用户");
        }
        
        vo.setComments(new ArrayList<>());
        
        return vo;
    }
    
    /**
     * 给用户增加发帖积分（有每日限制）
     *
     * @param userId 用户ID
     */
    private void addPostPoints(Long userId) {
        if (userId == null) {
            return;
        }
        
        // 构造当日发帖积分记录的键
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String pointsKey = Constant.RedisKey.POST_POINTS_KEY_PREFIX + userId + ":" + today;
        
        // 获取当日已获得积分的次数
        Integer count = redisService.getValue(pointsKey);
        count = count == null ? 0 : count;
        
        // 如果未达到限制，增加积分
        if (count < Constant.PointsSettings.DAILY_POINTS_LIMIT) {
            // 增加用户积分
            Users user = Db.lambdaQuery(Users.class).eq(Users::getId,userId).one();
            if (user != null) {
                Db.lambdaUpdate(Users.class)
                        .set(Users::getPoints, user.getPoints() + Constant.PointsSettings.POST_POINTS)
                        .set(Users::getUpdatedAt, LocalDateTime.now())
                        .eq(Users::getId,userId).update();
                
                // 更新计数
                redisService.setValue(pointsKey, count + 1, 24 * 60 * 60 * 1000L); // 设置24小时过期
            }
        }
    }
}
