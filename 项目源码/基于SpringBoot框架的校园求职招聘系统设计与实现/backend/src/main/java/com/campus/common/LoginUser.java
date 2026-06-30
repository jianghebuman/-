package com.campus.common;

import lombok.Data;

/**
 * 登录用户信息（存于 token 与上下文）
 *
 * @author campus
 */
@Data
public class LoginUser {

    /** 用户ID */
    private Long userId;

    /** 登录账号 */
    private String username;

    /** 角色：ADMIN/ENTERPRISE/STUDENT */
    private String role;

    /** 企业ID（企业HR登录时为所属公司ID） */
    private Long enterpriseId;

    /** HR角色：SUPERVISOR/STAFF（仅企业HR） */
    private String hrRole;
}
