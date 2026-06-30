package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.RequireRole;
import com.campus.common.Result;
import com.campus.entity.ForumComment;
import com.campus.entity.ForumPost;
import com.campus.service.SystemNoticeService;
import com.campus.service.ForumPostService;
import com.campus.service.impl.ForumCommentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员-论坛社区管理
 *
 * @author campus
 */
@RestController
@RequestMapping("/admin/forum")
@RequireRole("ADMIN")
public class ForumAdminController {

    @Autowired
    private ForumPostService forumPostService;
    @Autowired
    private ForumCommentServiceImpl forumCommentService;
    @Autowired
    private SystemNoticeService systemNoticeService;

    @GetMapping("/posts")
    public Result<PageResult<ForumPost>> postPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestParam(required = false) Integer auditStatus,
                                                  @RequestParam(required = false) String title) {
        Page<ForumPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<ForumPost>()
                .eq(auditStatus != null, ForumPost::getAuditStatus, auditStatus)
                .like(title != null && !title.isEmpty(), ForumPost::getTitle, title)
                .orderByDesc(ForumPost::getCreateTime);
        forumPostService.page(page, wrapper);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 审核帖子：status=1通过 2驳回 */
    @PutMapping("/posts/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam Integer status) {
        ForumPost db = forumPostService.getById(id);
        if (db == null) {
            return Result.error("帖子不存在");
        }
        ForumPost post = new ForumPost();
        post.setId(id);
        post.setAuditStatus(status);
        forumPostService.updateById(post);
        if (!status.equals(db.getAuditStatus())) {
            systemNoticeService.send(db.getStudentId(), "STUDENT",
                    status == 1 ? "帖子审核通过" : "帖子审核驳回",
                    "您的帖子【" + db.getTitle() + "】" + (status == 1 ? "已审核通过。" : "未通过审核。"),
                    "FORUM");
        }
        return Result.success(status == 1 ? "已通过" : "已驳回", null);
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> delPost(@PathVariable Long id) {
        ForumPost db = forumPostService.getById(id);
        forumPostService.removeById(id);
        if (db != null) {
            systemNoticeService.send(db.getStudentId(), "STUDENT", "帖子已删除",
                    "您的帖子【" + db.getTitle() + "】已被管理员删除。",
                    "FORUM");
        }
        return Result.success("删除成功", null);
    }

    @GetMapping("/posts/{postId}/comments")
    public Result<List<ForumComment>> comments(@PathVariable Long postId) {
        return Result.success(forumCommentService.list(
                new LambdaQueryWrapper<ForumComment>()
                        .eq(ForumComment::getPostId, postId)
                        .orderByDesc(ForumComment::getCreateTime)));
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> delComment(@PathVariable Long id) {
        ForumComment db = forumCommentService.getById(id);
        forumCommentService.removeById(id);
        if (db != null) {
            systemNoticeService.send(db.getStudentId(), "STUDENT", "评论已删除",
                    "您在社区发布的评论已被管理员删除。",
                    "FORUM");
        }
        return Result.success("删除成功", null);
    }
}
