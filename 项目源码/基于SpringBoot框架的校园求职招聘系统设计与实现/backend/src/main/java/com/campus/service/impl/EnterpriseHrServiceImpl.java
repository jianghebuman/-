package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.UserContext;
import com.campus.dto.EnterpriseHrDTO;
import com.campus.entity.EnterpriseHr;
import com.campus.mapper.EnterpriseHrMapper;
import com.campus.service.EnterpriseHrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 企业 HR 账号服务
 */
@Service
public class EnterpriseHrServiceImpl extends ServiceImpl<EnterpriseHrMapper, EnterpriseHr>
        implements EnterpriseHrService {

    private static final String DEFAULT_PASSWORD = "123456";

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public EnterpriseHr getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<EnterpriseHr>().eq(EnterpriseHr::getUsername, username), false);
    }

    @Override
    public PageResult<EnterpriseHr> pageByEnterprise(Long enterpriseId, Integer pageNum, Integer pageSize, String keyword) {
        Page<EnterpriseHr> page = new Page<>(pageNum, pageSize);
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        LambdaQueryWrapper<EnterpriseHr> wrapper = new LambdaQueryWrapper<EnterpriseHr>()
                .eq(EnterpriseHr::getEnterpriseId, enterpriseId)
                .and(hasKeyword, w -> w.like(EnterpriseHr::getUsername, keyword)
                        .or().like(EnterpriseHr::getRealName, keyword)
                        .or().like(EnterpriseHr::getPhone, keyword)
                        .or().like(EnterpriseHr::getEmail, keyword))
                .orderByDesc(EnterpriseHr::getCreateTime);
        this.page(page, wrapper);
        page.getRecords().forEach(hr -> hr.setPassword(null));
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    @Override
    public Long createHr(Long enterpriseId, EnterpriseHrDTO dto) {
        EnterpriseHr hr = new EnterpriseHr();
        hr.setEnterpriseId(enterpriseId);
        hr.setUsername(trim(dto.getUsername()));
        hr.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        hr.setRealName(trim(dto.getRealName()));
        hr.setPhone(trim(dto.getPhone()));
        hr.setEmail(trim(dto.getEmail()));
        hr.setHrRole(normalizeRole(dto.getHrRole()));
        hr.setStatus(1);
        try {
            this.save(hr);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("HR账号已存在");
        }
        return hr.getId();
    }

    @Override
    public void updateHr(Long hrId, EnterpriseHrDTO dto) {
        EnterpriseHr db = requireHr(hrId);
        String newRole = normalizeRole(dto.getHrRole());
        if (!db.getHrRole().equals(newRole)) {
            ensureCanLeaveSupervisor(db);
        }
        EnterpriseHr update = new EnterpriseHr();
        update.setId(hrId);
        update.setUsername(null);
        update.setPassword(null);
        update.setEnterpriseId(null);
        update.setRealName(trim(dto.getRealName()));
        update.setPhone(trim(dto.getPhone()));
        update.setEmail(trim(dto.getEmail()));
        update.setHrRole(newRole);
        this.updateById(update);
    }

    @Override
    public void changeRole(Long hrId, String role) {
        EnterpriseHr db = requireHr(hrId);
        String newRole = normalizeRole(role);
        if (!db.getHrRole().equals(newRole)) {
            ensureCanLeaveSupervisor(db);
        }
        EnterpriseHr update = new EnterpriseHr();
        update.setId(hrId);
        update.setHrRole(newRole);
        this.updateById(update);
    }

    @Override
    public void changeStatus(Long hrId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态非法");
        }
        EnterpriseHr db = requireHr(hrId);
        if (status == 0) {
            ensureCanLeaveSupervisor(db);
        }
        EnterpriseHr update = new EnterpriseHr();
        update.setId(hrId);
        update.setStatus(status);
        this.updateById(update);
    }

    @Override
    public void resetPassword(Long hrId) {
        requireHr(hrId);
        EnterpriseHr update = new EnterpriseHr();
        update.setId(hrId);
        update.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        this.updateById(update);
    }

    @Override
    public boolean isSupervisor() {
        return ROLE_SUPERVISOR.equals(UserContext.getHrRole());
    }

    private EnterpriseHr requireHr(Long hrId) {
        EnterpriseHr hr = this.getById(hrId);
        if (hr == null) {
            throw new BusinessException("HR账号不存在");
        }
        return hr;
    }

    private void ensureCanLeaveSupervisor(EnterpriseHr hr) {
        if (!ROLE_SUPERVISOR.equals(hr.getHrRole()) || !Integer.valueOf(1).equals(hr.getStatus())) {
            return;
        }
        long supervisors = this.count(new LambdaQueryWrapper<EnterpriseHr>()
                .eq(EnterpriseHr::getEnterpriseId, hr.getEnterpriseId())
                .eq(EnterpriseHr::getHrRole, ROLE_SUPERVISOR)
                .eq(EnterpriseHr::getStatus, 1)
                .ne(EnterpriseHr::getId, hr.getId()));
        if (supervisors == 0) {
            throw new BusinessException("每个企业至少需要保留一个启用的主管HR");
        }
    }

    private String normalizeRole(String role) {
        if (ROLE_SUPERVISOR.equals(role)) {
            return ROLE_SUPERVISOR;
        }
        if (role == null || role.trim().isEmpty() || ROLE_STAFF.equals(role)) {
            return ROLE_STAFF;
        }
        throw new BusinessException("HR角色非法");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
