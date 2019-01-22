package com.zjtelcom.cpct.domain.channel;

import java.io.Serializable;
import java.util.Date;

/**
 * 需求函
 */
public class RequestInfo implements Serializable {
    private static final long serialVersionUID = -1674418682059359688L;
    /** 需求申请单实例标识ID*/
    private Long requestTemplateInstId;

    /** 需求函编号*/
    private String batchNo;

    /** 申请单名称*/
    private String name;

    /** 申请单描述*/
    private String desc;

    /** 发起部门*/
    private String deptCode;

    /** 联系人*/
    private String contName;

    /** 联系电话*/
    private String contTele;

    /** 创建时间*/
    private Date startDate;

    /** 完成时间*/
    private Date finishDate;

    /** 预计完成时间*/
    private Date expectFinishDate;

    /** 需求类型*/
    private String requestType;

    /** 操作类型*/
    private String actionType;

    /** 流程主键*/
    private String activitiKey;

    /** 需求紧急程度:紧急，一般*/
    private String requestUrgentType;

    /** 提出理由*/
    private String reason;

    /** 流程类型（0：普通流程；1：快速修改）*/
    private String processType;

    /** 流程是否启动（0：未启动；1：启动）*/
    private String isstartup;

    /** 状态*/
    private String statusCd;

    /** 状态时间*/
    private Date statusDate;

    /** 创建人*/
    private Long createStaff;

    /** 创建时间*/
    private Date createDate;

    /** 修改人*/
    private Long updateStaff;

    /** 修改时间*/
    private Date updateDate;

    /** 集团需求单类型*/
    private String attrVal;

    /** 测试类型,选项值：无需测试，功能测试，测试环境业务测试，生产环境业务测试*/
    private String testType;

    /** 业务需求优先级,选项值：一星~七星*/
    private String priority;

    /** 计划发布测试时间*/
    private Date pstDate;

    /** 计划发布生产时间*/
    private Date prodDate;

    /** 概要设计说明*/
    private String outlineDesign;

    /** 测试要点说明*/
    private String testPoints;

    /** 集团发起人*/
    private String jtContName;

    /** 集团发起人联系电话*/
    private String jtContTele;

    /** ITSM拆单需求流水号*/
    private String patchSerial;

    /** 业务类型,取值于ZJ_REQUEST_001*/
    private String businessType;

    /** 报备标识，取值于ZJ_REQUEST_002*/
    private String reportTag;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.REQUEST_TEMPLATE_INST_ID
     *
     * @return the value of request_info.REQUEST_TEMPLATE_INST_ID
     *
     * @mbg.generated
     */
    public Long getRequestTemplateInstId() {
        return requestTemplateInstId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.REQUEST_TEMPLATE_INST_ID
     *
     * @param requestTemplateInstId the value for request_info.REQUEST_TEMPLATE_INST_ID
     *
     * @mbg.generated
     */
    public void setRequestTemplateInstId(Long requestTemplateInstId) {
        this.requestTemplateInstId = requestTemplateInstId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.BATCH_NO
     *
     * @return the value of request_info.BATCH_NO
     *
     * @mbg.generated
     */
    public String getBatchNo() {
        return batchNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.BATCH_NO
     *
     * @param batchNo the value for request_info.BATCH_NO
     *
     * @mbg.generated
     */
    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.NAME
     *
     * @return the value of request_info.NAME
     *
     * @mbg.generated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.NAME
     *
     * @param name the value for request_info.NAME
     *
     * @mbg.generated
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.DESC
     *
     * @return the value of request_info.DESC
     *
     * @mbg.generated
     */
    public String getDesc() {
        return desc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.DESC
     *
     * @param desc the value for request_info.DESC
     *
     * @mbg.generated
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.DEPT_CODE
     *
     * @return the value of request_info.DEPT_CODE
     *
     * @mbg.generated
     */
    public String getDeptCode() {
        return deptCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.DEPT_CODE
     *
     * @param deptCode the value for request_info.DEPT_CODE
     *
     * @mbg.generated
     */
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.CONT_NAME
     *
     * @return the value of request_info.CONT_NAME
     *
     * @mbg.generated
     */
    public String getContName() {
        return contName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.CONT_NAME
     *
     * @param contName the value for request_info.CONT_NAME
     *
     * @mbg.generated
     */
    public void setContName(String contName) {
        this.contName = contName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.CONT_TELE
     *
     * @return the value of request_info.CONT_TELE
     *
     * @mbg.generated
     */
    public String getContTele() {
        return contTele;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.CONT_TELE
     *
     * @param contTele the value for request_info.CONT_TELE
     *
     * @mbg.generated
     */
    public void setContTele(String contTele) {
        this.contTele = contTele;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.START_DATE
     *
     * @return the value of request_info.START_DATE
     *
     * @mbg.generated
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.START_DATE
     *
     * @param startDate the value for request_info.START_DATE
     *
     * @mbg.generated
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.FINISH_DATE
     *
     * @return the value of request_info.FINISH_DATE
     *
     * @mbg.generated
     */
    public Date getFinishDate() {
        return finishDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.FINISH_DATE
     *
     * @param finishDate the value for request_info.FINISH_DATE
     *
     * @mbg.generated
     */
    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.EXPECT_FINISH_DATE
     *
     * @return the value of request_info.EXPECT_FINISH_DATE
     *
     * @mbg.generated
     */
    public Date getExpectFinishDate() {
        return expectFinishDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.EXPECT_FINISH_DATE
     *
     * @param expectFinishDate the value for request_info.EXPECT_FINISH_DATE
     *
     * @mbg.generated
     */
    public void setExpectFinishDate(Date expectFinishDate) {
        this.expectFinishDate = expectFinishDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.REQUEST_TYPE
     *
     * @return the value of request_info.REQUEST_TYPE
     *
     * @mbg.generated
     */
    public String getRequestType() {
        return requestType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.REQUEST_TYPE
     *
     * @param requestType the value for request_info.REQUEST_TYPE
     *
     * @mbg.generated
     */
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.ACTION_TYPE
     *
     * @return the value of request_info.ACTION_TYPE
     *
     * @mbg.generated
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.ACTION_TYPE
     *
     * @param actionType the value for request_info.ACTION_TYPE
     *
     * @mbg.generated
     */
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.ACTIVITI_KEY
     *
     * @return the value of request_info.ACTIVITI_KEY
     *
     * @mbg.generated
     */
    public String getActivitiKey() {
        return activitiKey;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.ACTIVITI_KEY
     *
     * @param activitiKey the value for request_info.ACTIVITI_KEY
     *
     * @mbg.generated
     */
    public void setActivitiKey(String activitiKey) {
        this.activitiKey = activitiKey;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.REQUEST_URGENT_TYPE
     *
     * @return the value of request_info.REQUEST_URGENT_TYPE
     *
     * @mbg.generated
     */
    public String getRequestUrgentType() {
        return requestUrgentType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.REQUEST_URGENT_TYPE
     *
     * @param requestUrgentType the value for request_info.REQUEST_URGENT_TYPE
     *
     * @mbg.generated
     */
    public void setRequestUrgentType(String requestUrgentType) {
        this.requestUrgentType = requestUrgentType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.REASON
     *
     * @return the value of request_info.REASON
     *
     * @mbg.generated
     */
    public String getReason() {
        return reason;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.REASON
     *
     * @param reason the value for request_info.REASON
     *
     * @mbg.generated
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.PROCESS_TYPE
     *
     * @return the value of request_info.PROCESS_TYPE
     *
     * @mbg.generated
     */
    public String getProcessType() {
        return processType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.PROCESS_TYPE
     *
     * @param processType the value for request_info.PROCESS_TYPE
     *
     * @mbg.generated
     */
    public void setProcessType(String processType) {
        this.processType = processType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.ISSTARTUP
     *
     * @return the value of request_info.ISSTARTUP
     *
     * @mbg.generated
     */
    public String getIsstartup() {
        return isstartup;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.ISSTARTUP
     *
     * @param isstartup the value for request_info.ISSTARTUP
     *
     * @mbg.generated
     */
    public void setIsstartup(String isstartup) {
        this.isstartup = isstartup;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.STATUS_CD
     *
     * @return the value of request_info.STATUS_CD
     *
     * @mbg.generated
     */
    public String getStatusCd() {
        return statusCd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.STATUS_CD
     *
     * @param statusCd the value for request_info.STATUS_CD
     *
     * @mbg.generated
     */
    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.STATUS_DATE
     *
     * @return the value of request_info.STATUS_DATE
     *
     * @mbg.generated
     */
    public Date getStatusDate() {
        return statusDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.STATUS_DATE
     *
     * @param statusDate the value for request_info.STATUS_DATE
     *
     * @mbg.generated
     */
    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.CREATE_STAFF
     *
     * @return the value of request_info.CREATE_STAFF
     *
     * @mbg.generated
     */
    public Long getCreateStaff() {
        return createStaff;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.CREATE_STAFF
     *
     * @param createStaff the value for request_info.CREATE_STAFF
     *
     * @mbg.generated
     */
    public void setCreateStaff(Long createStaff) {
        this.createStaff = createStaff;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.CREATE_DATE
     *
     * @return the value of request_info.CREATE_DATE
     *
     * @mbg.generated
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.CREATE_DATE
     *
     * @param createDate the value for request_info.CREATE_DATE
     *
     * @mbg.generated
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.UPDATE_STAFF
     *
     * @return the value of request_info.UPDATE_STAFF
     *
     * @mbg.generated
     */
    public Long getUpdateStaff() {
        return updateStaff;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.UPDATE_STAFF
     *
     * @param updateStaff the value for request_info.UPDATE_STAFF
     *
     * @mbg.generated
     */
    public void setUpdateStaff(Long updateStaff) {
        this.updateStaff = updateStaff;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.UPDATE_DATE
     *
     * @return the value of request_info.UPDATE_DATE
     *
     * @mbg.generated
     */
    public Date getUpdateDate() {
        return updateDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.UPDATE_DATE
     *
     * @param updateDate the value for request_info.UPDATE_DATE
     *
     * @mbg.generated
     */
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.ATTR_VAL
     *
     * @return the value of request_info.ATTR_VAL
     *
     * @mbg.generated
     */
    public String getAttrVal() {
        return attrVal;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.ATTR_VAL
     *
     * @param attrVal the value for request_info.ATTR_VAL
     *
     * @mbg.generated
     */
    public void setAttrVal(String attrVal) {
        this.attrVal = attrVal;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.TEST_TYPE
     *
     * @return the value of request_info.TEST_TYPE
     *
     * @mbg.generated
     */
    public String getTestType() {
        return testType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.TEST_TYPE
     *
     * @param testType the value for request_info.TEST_TYPE
     *
     * @mbg.generated
     */
    public void setTestType(String testType) {
        this.testType = testType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.PRIORITY
     *
     * @return the value of request_info.PRIORITY
     *
     * @mbg.generated
     */
    public String getPriority() {
        return priority;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.PRIORITY
     *
     * @param priority the value for request_info.PRIORITY
     *
     * @mbg.generated
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.PST_DATE
     *
     * @return the value of request_info.PST_DATE
     *
     * @mbg.generated
     */
    public Date getPstDate() {
        return pstDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.PST_DATE
     *
     * @param pstDate the value for request_info.PST_DATE
     *
     * @mbg.generated
     */
    public void setPstDate(Date pstDate) {
        this.pstDate = pstDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.PROD_DATE
     *
     * @return the value of request_info.PROD_DATE
     *
     * @mbg.generated
     */
    public Date getProdDate() {
        return prodDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.PROD_DATE
     *
     * @param prodDate the value for request_info.PROD_DATE
     *
     * @mbg.generated
     */
    public void setProdDate(Date prodDate) {
        this.prodDate = prodDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.OUTLINE_DESIGN
     *
     * @return the value of request_info.OUTLINE_DESIGN
     *
     * @mbg.generated
     */
    public String getOutlineDesign() {
        return outlineDesign;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.OUTLINE_DESIGN
     *
     * @param outlineDesign the value for request_info.OUTLINE_DESIGN
     *
     * @mbg.generated
     */
    public void setOutlineDesign(String outlineDesign) {
        this.outlineDesign = outlineDesign;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.TEST_POINTS
     *
     * @return the value of request_info.TEST_POINTS
     *
     * @mbg.generated
     */
    public String getTestPoints() {
        return testPoints;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.TEST_POINTS
     *
     * @param testPoints the value for request_info.TEST_POINTS
     *
     * @mbg.generated
     */
    public void setTestPoints(String testPoints) {
        this.testPoints = testPoints;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.JT_CONT_NAME
     *
     * @return the value of request_info.JT_CONT_NAME
     *
     * @mbg.generated
     */
    public String getJtContName() {
        return jtContName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.JT_CONT_NAME
     *
     * @param jtContName the value for request_info.JT_CONT_NAME
     *
     * @mbg.generated
     */
    public void setJtContName(String jtContName) {
        this.jtContName = jtContName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.JT_CONT_TELE
     *
     * @return the value of request_info.JT_CONT_TELE
     *
     * @mbg.generated
     */
    public String getJtContTele() {
        return jtContTele;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.JT_CONT_TELE
     *
     * @param jtContTele the value for request_info.JT_CONT_TELE
     *
     * @mbg.generated
     */
    public void setJtContTele(String jtContTele) {
        this.jtContTele = jtContTele;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.PATCH_SERIAL
     *
     * @return the value of request_info.PATCH_SERIAL
     *
     * @mbg.generated
     */
    public String getPatchSerial() {
        return patchSerial;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.PATCH_SERIAL
     *
     * @param patchSerial the value for request_info.PATCH_SERIAL
     *
     * @mbg.generated
     */
    public void setPatchSerial(String patchSerial) {
        this.patchSerial = patchSerial;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.business_type
     *
     * @return the value of request_info.business_type
     *
     * @mbg.generated
     */
    public String getBusinessType() {
        return businessType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.business_type
     *
     * @param businessType the value for request_info.business_type
     *
     * @mbg.generated
     */
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column request_info.report_tag
     *
     * @return the value of request_info.report_tag
     *
     * @mbg.generated
     */
    public String getReportTag() {
        return reportTag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column request_info.report_tag
     *
     * @param reportTag the value for request_info.report_tag
     *
     * @mbg.generated
     */
    public void setReportTag(String reportTag) {
        this.reportTag = reportTag;
    }
}