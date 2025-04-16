package com.graduation.peelcs.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 帖子评论表
 * </p>
 *
 * @author feng
 * @since 2025-04-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("post_comments")
public class PostComments implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 顶级评论ID(NULL表示该评论本身为顶级评论)
     */
    private Long rootCommentId;

    /**
     * 目标评论ID(NULL表示该评论本身为顶级评论)
     */
    private Long toCommentId;

    /**
     * 回复目标用户昵称
     */
    private String toUserNickname;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 是否匿名:0否/1是
     */
    private Boolean isAnonymous;

    /**
     * 状态:pending/approved/rejected
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;


}
