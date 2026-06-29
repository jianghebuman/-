package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 企业认证审核
 *
 * @author campus
 */
@Data
@TableName("enterprise_audit")
public class EnterpriseAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long enterpriseId;

    private String licenseNo;

    private String licenseImg;

    private String extraImg;

    /** 审核状态：1待审核2通过3驳回 */
    private Integer auditStatus;

    private String auditRemark;

    private Long auditorId;

    private Date auditTime;

    private String verifySource;

    private String verifySourceUrl;

    private Date verifyTime;

    private String verifyCompanyName;

    private String verifyCreditCode;

    private String verifyStatus;

    /** 权威核验结果：0未核验1一致2不一致3未接入或异常 */
    private Integer verifyResult;

    private String verifyRemark;

    private String verifySnapshotHash;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Integer deleted;
}
