package com.zjtelcom.cpct.domain.system;

import com.zjtelcom.cpct.BaseEntity;

public class SysStaffRole extends BaseEntity {

    private Long staffRoleId;

    private Long staffId;

    private Long roleId;

    public Long getStaffRoleId() {
        return staffRoleId;
    }

    public void setStaffRoleId(Long staffRoleId) {
        this.staffRoleId = staffRoleId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}