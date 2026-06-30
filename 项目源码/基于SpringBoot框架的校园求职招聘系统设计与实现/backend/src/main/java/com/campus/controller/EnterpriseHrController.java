package com.campus.controller;

import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.RequireRole;
import com.campus.common.Result;
import com.campus.common.UserContext;
import com.campus.dto.EnterpriseHrDTO;
import com.campus.entity.EnterpriseHr;
import com.campus.service.EnterpriseHrService;
import com.campus.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 企业端 HR 管理（主管HR可用）
 */
@RestController
@RequestMapping("/enterprise/hr")
@RequireRole("ENTERPRISE")
public class EnterpriseHrController {

    @Autowired
    private EnterpriseHrService enterpriseHrService;
    @Autowired
    private OperationLogService operationLogService;

    @GetMapping
    public Result<PageResult<EnterpriseHr>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) String keyword) {
        requireSupervisor();
        return Result.success(enterpriseHrService.pageByEnterprise(UserContext.getEnterpriseId(), pageNum, pageSize, keyword));
    }

    @PostMapping
    public Result<Long> add(@Valid @RequestBody EnterpriseHrDTO dto) {
        requireSupervisor();
        Long id = enterpriseHrService.createHr(UserContext.getEnterpriseId(), dto);
        operationLogService.record("OPERATION", "HR管理", "新增HR：" + dto.getUsername(), 1);
        return Result.success("新增成功，初始密码123456", id);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EnterpriseHrDTO dto) {
        requireSupervisor();
        requireSameEnterprise(id);
        enterpriseHrService.updateHr(id, dto);
        operationLogService.record("OPERATION", "HR管理", "修改HR：" + id, 1);
        return Result.success("修改成功", null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam Integer status) {
        requireSupervisor();
        requireSameEnterprise(id);
        enterpriseHrService.changeStatus(id, status);
        return Result.success(status == 1 ? "已启用" : "已禁用", null);
    }

    @PutMapping("/{id}/reset")
    public Result<Void> reset(@PathVariable Long id) {
        requireSupervisor();
        requireSameEnterprise(id);
        enterpriseHrService.resetPassword(id);
        return Result.success("密码已重置为123456", null);
    }

    private void requireSupervisor() {
        if (!UserContext.isSupervisorHr()) {
            throw new BusinessException("仅主管HR可管理HR账号");
        }
    }

    private void requireSameEnterprise(Long id) {
        EnterpriseHr hr = enterpriseHrService.getById(id);
        if (hr == null || !UserContext.getEnterpriseId().equals(hr.getEnterpriseId())) {
            throw new BusinessException("HR账号不存在或无权操作");
        }
    }
}
