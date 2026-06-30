package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 企业 HR 账号
 */
@Data
@TableName("enterprise_hr")
public class EnterpriseHr implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long enterpriseId;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    /** SUPERVISOR 主管HR；STAFF 普通HR */
    private String hrRole;

    /** 1正常0禁用 */
    private Integer status;

    private Date lastLogin;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Integer deleted;
}
