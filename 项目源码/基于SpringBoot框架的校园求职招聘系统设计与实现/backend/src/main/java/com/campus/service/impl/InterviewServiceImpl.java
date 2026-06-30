package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.UserContext;
import com.campus.entity.InterviewFeedback;
import com.campus.entity.InterviewNotice;
import com.campus.entity.JobApply;
import com.campus.entity.JobPost;
import com.campus.mapper.InterviewFeedbackMapper;
import com.campus.mapper.InterviewNoticeMapper;
import com.campus.mapper.JobApplyMapper;
import com.campus.mapper.JobPostMapper;
import com.campus.service.InterviewService;
import com.campus.service.SystemNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 面试服务实现（企业HR端）
 *
 * @author campus
 */
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewNoticeMapper, InterviewNotice>
        implements InterviewService {

    @Autowired
    private JobApplyMapper jobApplyMapper;

    @Autowired
    private SystemNoticeService systemNoticeService;

    @Autowired
    private InterviewFeedbackMapper interviewFeedbackMapper;

    @Autowired
    private JobPostMapper jobPostMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long invite(InterviewNotice notice) {
        Long enterpriseId = UserContext.getEnterpriseId();
        if (notice.getApplyId() == null) {
            throw new BusinessException("缺少投递记录ID");
        }
        if (notice.getInterviewTime() == null) {
            throw new BusinessException("面试时间不能为空");
        }
        JobApply apply = jobApplyMapper.selectById(notice.getApplyId());
        if (!canAccessApply(apply, enterpriseId)) {
            throw new BusinessException("投递记录不存在或无权操作");
        }
        // 写面试通知，强制归属与冗余字段从投递记录回填
        notice.setId(null);
        notice.setEnterpriseId(enterpriseId);
        notice.setHrId(apply.getHrId());
        notice.setStudentId(apply.getStudentId());
        notice.setJobId(apply.getJobId());
        notice.setStudentStatus(0);
        if (notice.getInterviewType() == null) {
            notice.setInterviewType(1);
        }
        this.save(notice);
        // 把对应 job_apply.status 置 2（邀请面试）
        JobApply applyUpdate = new JobApply();
        applyUpdate.setId(apply.getId());
        applyUpdate.setStatus(2);
        jobApplyMapper.updateById(applyUpdate);
        // 给学生发系统通知
        systemNoticeService.send(apply.getStudentId(), "STUDENT", "面试邀请",
                "您收到职位「" + jobTitle(apply.getJobId()) + "」的面试邀请，请及时确认。",
                "INTERVIEW");
        return notice.getId();
    }

    @Override
    public PageResult<InterviewNotice> myInterviewPage(Integer pageNum, Integer pageSize, Integer studentStatus) {
        Long enterpriseId = UserContext.getEnterpriseId();
        Page<InterviewNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InterviewNotice> wrapper = new LambdaQueryWrapper<InterviewNotice>()
                .eq(InterviewNotice::getEnterpriseId, enterpriseId)
                .eq(!UserContext.isSupervisorHr(), InterviewNotice::getHrId, UserContext.getUserId())
                .eq(studentStatus != null, InterviewNotice::getStudentStatus, studentStatus)
                .orderByDesc(InterviewNotice::getCreateTime);
        this.page(page, wrapper);
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    @Override
    public void updateInterview(InterviewNotice notice) {
        Long enterpriseId = UserContext.getEnterpriseId();
        InterviewNotice db = this.getById(notice.getId());
        if (!canAccessNotice(db, enterpriseId)) {
            throw new BusinessException("面试记录不存在或无权操作");
        }
        // 不允许篡改归属与学生确认状态
        notice.setEnterpriseId(enterpriseId);
        notice.setHrId(db.getHrId());
        notice.setStudentId(null);
        notice.setStudentStatus(null);
        this.updateById(notice);
        systemNoticeService.send(db.getStudentId(), "STUDENT", "面试安排已更新",
                "您职位「" + jobTitle(db.getJobId()) + "」的面试安排已更新，请及时查看。",
                "INTERVIEW");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void feedback(Long noticeId, Integer score, String content, Integer isPass, String interviewer) {
        Long enterpriseId = UserContext.getEnterpriseId();
        InterviewNotice db = this.getById(noticeId);
        if (!canAccessNotice(db, enterpriseId)) {
            throw new BusinessException("面试记录不存在或无权操作");
        }
        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setNoticeId(noticeId);
        feedback.setApplyId(db.getApplyId());
        feedback.setEnterpriseId(enterpriseId);
        feedback.setHrId(db.getHrId());
        feedback.setScore(score);
        feedback.setContent(content);
        feedback.setIsPass(isPass);
        feedback.setInterviewer(interviewer);
        interviewFeedbackMapper.insert(feedback);
        systemNoticeService.send(db.getStudentId(), "STUDENT", "面试评价已更新",
                "您职位「" + jobTitle(db.getJobId()) + "」的面试评价已录入，请及时查看后续流程。",
                "INTERVIEW");
    }

    private String jobTitle(Long jobId) {
        JobPost job = jobPostMapper.selectById(jobId);
        return job == null ? "" : job.getTitle();
    }

    private boolean canAccessApply(JobApply apply, Long enterpriseId) {
        if (apply == null || !enterpriseId.equals(apply.getEnterpriseId())) {
            return false;
        }
        return UserContext.isSupervisorHr() || UserContext.getUserId().equals(apply.getHrId());
    }

    private boolean canAccessNotice(InterviewNotice notice, Long enterpriseId) {
        if (notice == null || !enterpriseId.equals(notice.getEnterpriseId())) {
            return false;
        }
        return UserContext.isSupervisorHr() || UserContext.getUserId().equals(notice.getHrId());
    }
}
