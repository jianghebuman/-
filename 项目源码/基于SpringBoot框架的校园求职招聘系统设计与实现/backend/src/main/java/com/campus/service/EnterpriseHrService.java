package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.common.PageResult;
import com.campus.dto.EnterpriseHrDTO;
import com.campus.entity.EnterpriseHr;

public interface EnterpriseHrService extends IService<EnterpriseHr> {

    String ROLE_SUPERVISOR = "SUPERVISOR";
    String ROLE_STAFF = "STAFF";

    EnterpriseHr getByUsername(String username);

    PageResult<EnterpriseHr> pageByEnterprise(Long enterpriseId, Integer pageNum, Integer pageSize, String keyword);

    Long createHr(Long enterpriseId, EnterpriseHrDTO dto);

    void updateHr(Long hrId, EnterpriseHrDTO dto);

    void changeRole(Long hrId, String role);

    void changeStatus(Long hrId, Integer status);

    void resetPassword(Long hrId);

    boolean isSupervisor();
}
