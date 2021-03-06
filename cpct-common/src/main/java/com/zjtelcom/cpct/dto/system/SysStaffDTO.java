package com.zjtelcom.cpct.dto.system;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysStaffDTO implements Serializable {

    private Long staffId;

    private String staffCode;

    private String staffAccount;

    private String staffName;

    private String password;

    private String confirmPassword;

    private String staffPhone;

    private String staffTelephone;

    private String staffEmail;

    private Long channelId;

    private Long cityId;

    private Long status;

    private Date lastLogin;

    /**
     * 角色id
     */
    private Long roleId;


}
