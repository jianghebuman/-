package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.common.BusinessException;
import com.campus.common.RequireRole;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.entity.ActivitySign;
import com.campus.entity.CampusTalk;
import com.campus.entity.JobFair;
import com.campus.mapper.ActivitySignMapper;
import com.campus.mapper.CampusTalkMapper;
import com.campus.mapper.JobFairMapper;
import com.campus.entity.SystemNotice;
import com.campus.service.SystemNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/activity")
@RequireRole("STUDENT")
public class ActivitySignController {

    @Autowired
    private ActivitySignMapper activitySignMapper;

    @Autowired
    private CampusTalkMapper campusTalkMapper;

    @Autowired
    private JobFairMapper jobFairMapper;

    @Autowired
    private SystemNoticeService systemNoticeService;

    @PostMapping("/sign")
    public Result<Void> sign(@RequestParam Integer activityType, @RequestParam Long activityId) {
        if (activityType == null || activityId == null || (activityType != 1 && activityType != 2)) {
            throw new BusinessException("报名参数不正确");
        }
        String title;
        String location;
        Date activityTime;
        if (activityType == 1) {
            CampusTalk talk = campusTalkMapper.selectById(activityId);
            if (talk == null || !Integer.valueOf(1).equals(talk.getStatus())) {
                throw new BusinessException("宣讲会不存在或已关闭");
            }
            title = talk.getTitle();
            location = talk.getLocation();
            activityTime = talk.getTalkTime();
        } else {
            JobFair fair = jobFairMapper.selectById(activityId);
            if (fair == null || !Integer.valueOf(1).equals(fair.getStatus())) {
                throw new BusinessException("招聘会不存在或已关闭");
            }
            title = fair.getTitle();
            location = fair.getLocation();
            activityTime = fair.getFairTime();
        }

        Long studentId = UserContext.getUserId();
        Long signed = activitySignMapper.selectCount(new LambdaQueryWrapper<ActivitySign>()
                .eq(ActivitySign::getActivityType, activityType)
                .eq(ActivitySign::getActivityId, activityId)
                .eq(ActivitySign::getStudentId, studentId)
                .ne(ActivitySign::getSignStatus, 3));
        if (signed > 0) {
            sendActivityNoticeIfMissing(studentId, activityType, title, location, activityTime);
            return Result.success("您已报名，请准时参加", null);
        }

        ActivitySign sign = new ActivitySign();
        sign.setActivityType(activityType);
        sign.setActivityId(activityId);
        sign.setStudentId(studentId);
        sign.setSignStatus(1);
        activitySignMapper.insert(sign);

        if (activityType == 1) {
            campusTalkMapper.update(null, Wrappers.<CampusTalk>lambdaUpdate()
                    .eq(CampusTalk::getId, activityId)
                    .setSql("sign_count = IFNULL(sign_count, 0) + 1"));
        } else {
            jobFairMapper.update(null, Wrappers.<JobFair>lambdaUpdate()
                    .eq(JobFair::getId, activityId)
                    .setSql("sign_count = IFNULL(sign_count, 0) + 1"));
        }
        sendActivityNotice(studentId, activityType, title, location, activityTime);
        return Result.success("报名成功，请准时参加", null);
    }

    private void sendActivityNoticeIfMissing(Long studentId, Integer activityType, String title, String location, Date activityTime) {
        String typeName = activityType == 1 ? "宣讲会" : "招聘会";
        String content = activityNoticeContent(title, location, activityTime);
        long count = systemNoticeService.count(new LambdaQueryWrapper<SystemNotice>()
                .eq(SystemNotice::getReceiverId, studentId)
                .eq(SystemNotice::getReceiverType, "STUDENT")
                .eq(SystemNotice::getNoticeType, "ACTIVITY")
                .eq(SystemNotice::getTitle, typeName + "报名成功")
                .eq(SystemNotice::getContent, content));
        if (count == 0) {
            sendActivityNotice(studentId, activityType, title, location, activityTime);
        }
    }

    private void sendActivityNotice(Long studentId, Integer activityType, String title, String location, Date activityTime) {
        String typeName = activityType == 1 ? "宣讲会" : "招聘会";
        systemNoticeService.send(studentId, "STUDENT", typeName + "报名成功",
                activityNoticeContent(title, location, activityTime),
                "ACTIVITY");
    }

    private String activityNoticeContent(String title, String location, Date activityTime) {
        return "您已成功报名「" + title + "」。时间：" + formatTime(activityTime) + "，地点：" + (location == null ? "待定" : location) + "。请准时参加。";
    }

    private String formatTime(Date time) {
        if (time == null) {
            return "待定";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time);
    }
}
