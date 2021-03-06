package com.zjtelcom.cpct.domain.openApi.tarGrp;

import java.util.Date;
import java.util.List;

public class OpenTarGrpEntity {

    private String actType;
    private Long tarGrpId;
    private String tarGrpName;
    private String tarGrpDesc;
    private String tarGrpType;
    private String statusCd;
    private String statusDate;
    private Long createStaff;
    private String createDate;
    private Long updateStaff;
    private String updateDate;
    private List<OpenTarGrpConditionEntity> tarGrpConditions;
    private String remark;
    private String tarGrpConditionExpression;

    public String getTarGrpConditionExpression() {
        return tarGrpConditionExpression;
    }

    public void setTarGrpConditionExpression(String tarGrpConditionExpression) {
        this.tarGrpConditionExpression = tarGrpConditionExpression;
    }

    public String getActType() {
        return actType;
    }

    public void setActType(String actType) {
        this.actType = actType;
    }

    public Long getTarGrpId() {
        return tarGrpId;
    }

    public void setTarGrpId(Long tarGrpId) {
        this.tarGrpId = tarGrpId;
    }

    public String getTarGrpName() {
        return tarGrpName;
    }

    public void setTarGrpName(String tarGrpName) {
        this.tarGrpName = tarGrpName;
    }

    public String getTarGrpDesc() {
        return tarGrpDesc;
    }

    public void setTarGrpDesc(String tarGrpDesc) {
        this.tarGrpDesc = tarGrpDesc;
    }

    public String getTarGrpType() {
        return tarGrpType;
    }

    public void setTarGrpType(String tarGrpType) {
        this.tarGrpType = tarGrpType;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public String getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(String statusDate) {
        this.statusDate = statusDate;
    }

    public Long getCreateStaff() {
        return createStaff;
    }

    public void setCreateStaff(Long createStaff) {
        this.createStaff = createStaff;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public Long getUpdateStaff() {
        return updateStaff;
    }

    public void setUpdateStaff(Long updateStaff) {
        this.updateStaff = updateStaff;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public List<OpenTarGrpConditionEntity> getTarGrpConditions() {
        return tarGrpConditions;
    }

    public void setTarGrpConditions(List<OpenTarGrpConditionEntity> tarGrpConditions) {
        this.tarGrpConditions = tarGrpConditions;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
