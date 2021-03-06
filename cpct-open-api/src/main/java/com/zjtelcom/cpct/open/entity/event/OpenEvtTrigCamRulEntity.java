package com.zjtelcom.cpct.open.entity.event;

import java.util.Date;

public class OpenEvtTrigCamRulEntity {

    private String actType;
    private Long evtTrigCamRulId;
    private Long eventId;
    private String eventNbr;
    private Long mktCampaignId;
    private String evtRulName;
    private String evtRulDesc;
    private String evtRulHandleClass;
    private String evtRulExpression;
    private String statusCd;
    private Date statusDate;
    private Long createStaff;
    private Date createDate;
    private Long updateStaff;
    private Date updateDate;
    private String remark;

    public String getActType() {
        return actType;
    }

    public void setActType(String actType) {
        this.actType = actType;
    }

    public Long getEvtTrigCamRulId() {
        return evtTrigCamRulId;
    }

    public void setEvtTrigCamRulId(Long evtTrigCamRulId) {
        this.evtTrigCamRulId = evtTrigCamRulId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventNbr() {
        return eventNbr;
    }

    public void setEventNbr(String eventNbr) {
        this.eventNbr = eventNbr;
    }

    public Long getMktCampaignId() {
        return mktCampaignId;
    }

    public void setMktCampaignId(Long mktCampaignId) {
        this.mktCampaignId = mktCampaignId;
    }

    public String getEvtRulName() {
        return evtRulName;
    }

    public void setEvtRulName(String evtRulName) {
        this.evtRulName = evtRulName;
    }

    public String getEvtRulDesc() {
        return evtRulDesc;
    }

    public void setEvtRulDesc(String evtRulDesc) {
        this.evtRulDesc = evtRulDesc;
    }

    public String getEvtRulHandleClass() {
        return evtRulHandleClass;
    }

    public void setEvtRulHandleClass(String evtRulHandleClass) {
        this.evtRulHandleClass = evtRulHandleClass;
    }

    public String getEvtRulExpression() {
        return evtRulExpression;
    }

    public void setEvtRulExpression(String evtRulExpression) {
        this.evtRulExpression = evtRulExpression;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }

    public Long getCreateStaff() {
        return createStaff;
    }

    public void setCreateStaff(Long createStaff) {
        this.createStaff = createStaff;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Long getUpdateStaff() {
        return updateStaff;
    }

    public void setUpdateStaff(Long updateStaff) {
        this.updateStaff = updateStaff;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
