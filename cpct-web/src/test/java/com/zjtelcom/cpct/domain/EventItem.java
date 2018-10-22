package com.zjtelcom.cpct.domain;

import java.util.Date;

public class EventItem {
    private Long evtItemId;

    private Long eventId;

    private Long evtTypeId;

    private String evtItemName;

    private String evtItemCode;

    private String valueDataType;

    private String evtItemFormat;

    private String isNullable;

    private Integer evtItemLength;

    private Integer standardSort;

    private String statusCd;

    private Long createStaff;

    private Long updateStaff;

    private Date createDate;

    private Date statusDate;

    private Date updateDate;

    private String remark;

    public Long getEvtItemId() {
        return evtItemId;
    }

    public void setEvtItemId(Long evtItemId) {
        this.evtItemId = evtItemId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getEvtTypeId() {
        return evtTypeId;
    }

    public void setEvtTypeId(Long evtTypeId) {
        this.evtTypeId = evtTypeId;
    }

    public String getEvtItemName() {
        return evtItemName;
    }

    public void setEvtItemName(String evtItemName) {
        this.evtItemName = evtItemName;
    }

    public String getEvtItemCode() {
        return evtItemCode;
    }

    public void setEvtItemCode(String evtItemCode) {
        this.evtItemCode = evtItemCode;
    }

    public String getValueDataType() {
        return valueDataType;
    }

    public void setValueDataType(String valueDataType) {
        this.valueDataType = valueDataType;
    }

    public String getEvtItemFormat() {
        return evtItemFormat;
    }

    public void setEvtItemFormat(String evtItemFormat) {
        this.evtItemFormat = evtItemFormat;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(String isNullable) {
        this.isNullable = isNullable;
    }

    public Integer getEvtItemLength() {
        return evtItemLength;
    }

    public void setEvtItemLength(Integer evtItemLength) {
        this.evtItemLength = evtItemLength;
    }

    public Integer getStandardSort() {
        return standardSort;
    }

    public void setStandardSort(Integer standardSort) {
        this.standardSort = standardSort;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public Long getCreateStaff() {
        return createStaff;
    }

    public void setCreateStaff(Long createStaff) {
        this.createStaff = createStaff;
    }

    public Long getUpdateStaff() {
        return updateStaff;
    }

    public void setUpdateStaff(Long updateStaff) {
        this.updateStaff = updateStaff;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
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