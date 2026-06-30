package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.EnterpriseHr;
import com.campus.entity.SystemNotice;
import com.campus.mapper.EnterpriseHrMapper;
import com.campus.mapper.SystemNoticeMapper;
import com.campus.service.EnterpriseHrService;
import com.campus.service.SystemNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统消息通知服务实现
 *
 * @author campus
 */
@Service
public class SystemNoticeServiceImpl extends ServiceImpl<SystemNoticeMapper, SystemNotice> implements SystemNoticeService {

    @Autowired
    private EnterpriseHrMapper enterpriseHrMapper;

    @Override
    public void send(Long receiverId, String receiverType, String title, String content, String noticeType) {
        if (receiverId == null) {
            return;
        }
        SystemNotice notice = new SystemNotice();
        notice.setReceiverId(receiverId);
        notice.setReceiverType(receiverType);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setNoticeType(noticeType == null ? "SYSTEM" : noticeType);
        notice.setIsRead(0);
        this.save(notice);
    }

    @Override
    public void sendToEnterpriseSupervisors(Long enterpriseId, String title, String content, String noticeType) {
        if (enterpriseId == null) {
            return;
        }
        List<EnterpriseHr> hrs = enterpriseHrMapper.selectList(new LambdaQueryWrapper<EnterpriseHr>()
                .eq(EnterpriseHr::getEnterpriseId, enterpriseId)
                .eq(EnterpriseHr::getHrRole, EnterpriseHrService.ROLE_SUPERVISOR)
                .eq(EnterpriseHr::getStatus, 1));
        for (EnterpriseHr hr : hrs) {
            send(hr.getId(), "ENTERPRISE", title, content, noticeType);
        }
    }

    @Override
    public long unreadCount(Long receiverId, String receiverType) {
        return this.count(new LambdaQueryWrapper<SystemNotice>()
                .eq(SystemNotice::getReceiverId, receiverId)
                .eq(SystemNotice::getReceiverType, receiverType)
                .eq(SystemNotice::getIsRead, 0));
    }
}
