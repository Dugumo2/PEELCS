package com.graduation.peelcs.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.graduation.peelcs.commen.Result;
import com.graduation.peelcs.domain.dto.CommentDTO;
import com.graduation.peelcs.domain.dto.PostDTO;
import com.graduation.peelcs.domain.po.ForumPosts;
import com.graduation.peelcs.domain.po.PostComments;
import com.graduation.peelcs.domain.po.Users;
import com.graduation.peelcs.domain.query.CommentQuery;
import com.graduation.peelcs.domain.query.PostQuery;
import com.graduation.peelcs.domain.vo.CommentVO;
import com.graduation.peelcs.domain.vo.PostDetailVO;
import com.graduation.peelcs.domain.vo.PostVO;
import com.graduation.peelcs.service.IForumPostsService;
import com.graduation.peelcs.service.IPostCommentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 帖子与评论 前端控制器
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final IForumPostsService forumPostsService;
    private final IPostCommentsService postCommentsService;
    
    /**
     * 发布帖子
     */
    @PostMapping
    public Result<ForumPosts> createPost(@RequestBody PostDTO postDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        ForumPosts post = forumPostsService.createPost(
                userId,
                postDTO.getTitle(),
                postDTO.getContent(),
                postDTO.getIsAnonymous());
        return Result.success(post);
    }
    
    /**
     * 发布官方帖子（需要管理员权限）
     */
    @PostMapping("/official")
    @SaCheckRole("admin")
    public Result<ForumPosts> createOfficialPost(@RequestBody PostDTO postDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        ForumPosts post = forumPostsService.createOfficialPost(
                userId,
                postDTO.getTitle(),
                postDTO.getContent());
        return Result.success(post);
    }
    
    /**
     * 审核帖子（需要管理员权限）
     */
    @PutMapping("/review/{id}")
    @SaCheckRole("admin")
    public Result<ForumPosts> reviewPost(
            @PathVariable Long id,
            @RequestParam String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        ForumPosts post = forumPostsService.reviewPost(userId, id, status);
        return Result.success(post);
    }
    
    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = forumPostsService.deletePost(userId, id);
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
    
    /**
     * 获取帖子列表
     */
    @PostMapping("/list")
    public Result<List<PostVO>> getPosts(@RequestBody PostQuery query) {
        // 安全检查：普通用户只能看到审核通过的帖子
        Long userId = StpUtil.getLoginIdAsLong();
        Users user = null;

            user = Db.lambdaQuery(Users.class).eq(Users::getId, userId).one();

        
        // 如果不是管理员，强制设置status为approved
        if (user == null || !"admin".equals(user.getRole())) {
            if (query == null) {
                query = new PostQuery();
            }
            query.setStatus("approved");
        }
        
        List<PostVO> posts = forumPostsService.getPosts(query);
        return Result.success(posts);
    }
    
    /**
     * 获取待审核的帖子列表（管理员接口）
     */
    @PostMapping("/pending")
    @SaCheckRole("admin")
    public Result<List<PostVO>> getPendingPosts(@RequestBody PostQuery query) {
        query.setStatus("pending");
        List<PostVO> posts = forumPostsService.getPosts(query);
        return Result.success(posts);
    }
    
    /**
     * 获取帖子详情
     */
    @GetMapping("/{id}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long id) {
        // 获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 获取用户信息，判断是否为管理员
        Users user = null;
        try {
            user = Db.lambdaQuery(Users.class).eq(Users::getId, userId).one();
        } catch (Exception e) {
            // 如果查询失败，当作普通用户处理
        }
        
        boolean isAdmin = user != null && "admin".equals(user.getRole());
        
        // 查询帖子详情，传入当前用户ID，让服务层判断权限
        PostDetailVO post = forumPostsService.getPostDetail(id, userId, isAdmin);
        
        if (post != null) {
            return Result.success(post);
        } else {
            return Result.error("帖子不存在或未通过审核");
        }
    }
    
    /**
     * 获取我的帖子
     */
    @PostMapping("/my")
    public Result<List<PostVO>> getMyPosts(@RequestBody PostQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        if (query == null) {
            query = new PostQuery();
        }
        
        // 设置用户ID，只查询当前用户的帖子
        query.setUserId(userId);
        
        // 用户查看自己的帖子时无需状态过滤，可以看到所有自己发布的帖子包括待审核的
        
        List<PostVO> posts = forumPostsService.getPosts(query);
        return Result.success(posts);
    }
    
    /**
     * 发表评论（顶级评论）
     */
    @PostMapping("create/comments")
    public Result<PostComments> createComment(@RequestBody CommentDTO commentDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查帖子ID是否存在
        if (commentDTO.getPostId() == null) {
            return Result.error("缺少帖子ID");
        }
        
        PostComments comment = postCommentsService.createComment(
                userId,
                commentDTO.getPostId(),
                commentDTO.getContent(),
                commentDTO.getIsAnonymous());
        return Result.success(comment);
    }
    
    /**
     * 发表评论回复（子评论）
     */
    @PostMapping("/comments/{commentId}/reply")
    public Result<PostComments> replyComment(
            @PathVariable Long commentId,
            @RequestBody CommentDTO commentDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        PostComments reply = postCommentsService.replyComment(
                userId,
                commentId,
                commentDTO.getContent(),
                commentDTO.getIsAnonymous());
        return Result.success(reply);
    }
    
    /**
     * 获取帖子的评论列表（顶级评论）
     */
    @PostMapping("/comments")
    public Result<List<CommentVO>> getPostComments(@RequestBody CommentQuery query) {
        // 确保只查询顶级评论
        query.setRootCommentId(null);
        
        // 检查帖子ID是否存在
        if (query.getPostId() == null) {
            return Result.error("缺少帖子ID");
        }
        
        List<CommentVO> comments = postCommentsService.getComments(query);
        return Result.success(comments);
    }
    
    /**
     * 获取顶级评论的回复列表（子评论）
     */
    @PostMapping("/comments/replies")
    public Result<List<CommentVO>> getCommentReplies(@RequestBody CommentQuery query) {
        // 检查根评论ID是否存在
        if (query.getRootCommentId() == null) {
            return Result.error("缺少根评论ID");
        }
        
        List<CommentVO> replies = postCommentsService.getComments(query);
        return Result.success(replies);
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = postCommentsService.deleteComment(userId, id);
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}
