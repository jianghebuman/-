package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.common.UserContext;
import com.campus.entity.Enterprise;
import com.campus.entity.EnterpriseAudit;
import com.campus.mapper.EnterpriseAuditMapper;
import com.campus.mapper.EnterpriseMapper;
import com.campus.service.EnterpriseAuditService;
import com.campus.service.EnterpriseVerificationService;
import com.campus.service.EnterpriseVerificationService.VerificationResult;
import com.campus.service.SystemNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 企业认证审核服务实现
 *
 * @author campus
 */
@Service
public class EnterpriseAuditServiceImpl extends ServiceImpl<EnterpriseAuditMapper, EnterpriseAudit>
        implements EnterpriseAuditService {

    @Autowired
    private EnterpriseMapper enterpriseMapper;
    @Autowired
    private EnterpriseVerificationService enterpriseVerificationService;
    @Autowired
    private SystemNoticeService systemNoticeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAudit(String licenseNo, String licenseImg, String extraImg) {
        Long enterpriseId = UserContext.getEnterpriseId();
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException("企业信息不存在");
        }
        // 已通过认证不允许重复提交
        if (Integer.valueOf(2).equals(enterprise.getAuditStatus())) {
            throw new BusinessException("企业已通过认证，无需重复提交");
        }
        // 待审核中不允许重复提交
        if (Integer.valueOf(1).equals(enterprise.getAuditStatus())) {
            throw new BusinessException("认证申请审核中，请耐心等待");
        }
        if (licenseNo == null || licenseNo.trim().isEmpty()) {
            throw new BusinessException("营业执照编号不能为空");
        }
        if (licenseImg == null || licenseImg.trim().isEmpty()) {
            throw new BusinessException("营业执照图片不能为空");
        }
        VerificationResult verify = enterpriseVerificationService.verify(enterprise, licenseNo);
        boolean autoPass = Boolean.TRUE.equals(verify.getMatched());

        // 新增一条审核记录；权威核验一致时系统自动通过，仍保留管理员复核痕迹。
        EnterpriseAudit audit = new EnterpriseAudit();
        audit.setEnterpriseId(enterpriseId);
        audit.setLicenseNo(licenseNo);
        audit.setLicenseImg(licenseImg);
        audit.setExtraImg(extraImg);
        audit.setAuditStatus(autoPass ? 2 : 1);
        audit.setAuditRemark(autoPass
                ? "系统自动核验通过：" + verify.getVerifyRemark()
                : verify.getVerifyRemark());
        audit.setAuditorId(autoPass ? 0L : null);
        audit.setAuditTime(autoPass ? new Date() : null);
        audit.setVerifySource(verify.getVerifySource());
        audit.setVerifySourceUrl(verify.getVerifySourceUrl());
        audit.setVerifyTime(verify.getVerifyTime());
        audit.setVerifyCompanyName(verify.getVerifyCompanyName());
        audit.setVerifyCreditCode(verify.getVerifyCreditCode());
        audit.setVerifyStatus(verify.getVerifyStatus());
        audit.setVerifyResult(verify.getVerifyResult());
        audit.setVerifyRemark(verify.getVerifyRemark());
        audit.setVerifySnapshotHash(verify.getVerifySnapshotHash());
        this.save(audit);

        // 同步企业认证状态：自动核验一致则直接通过，否则进入人工审核。
        Enterprise update = new Enterprise();
        update.setId(enterpriseId);
        update.setAuditStatus(autoPass ? 2 : 1);
        enterpriseMapper.updateById(update);
        if (autoPass) {
            systemNoticeService.sendToEnterpriseSupervisors(enterpriseId, "企业认证已通过",
                    "您的企业认证已通过系统自动核验，可正常开展招聘。",
                    "AUDIT");
        }
    }

    @Override
    public EnterpriseAudit getLatest() {
        Long enterpriseId = UserContext.getEnterpriseId();
        return this.getOne(new LambdaQueryWrapper<EnterpriseAudit>()
                .eq(EnterpriseAudit::getEnterpriseId, enterpriseId)
                .orderByDesc(EnterpriseAudit::getCreateTime)
                .last("LIMIT 1"), false);
    }
}
