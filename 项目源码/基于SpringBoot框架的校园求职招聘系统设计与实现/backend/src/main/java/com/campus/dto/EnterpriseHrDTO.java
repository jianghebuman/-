package com.campus.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class EnterpriseHrDTO {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$|^0\\d{2,3}-?\\d{7,8}$", message = "请输入有效的手机号或固定电话")
    private String phone;

    @Email(message = "请输入有效邮箱")
    private String email;

    /** SUPERVISOR/STAFF */
    private String hrRole;
}
