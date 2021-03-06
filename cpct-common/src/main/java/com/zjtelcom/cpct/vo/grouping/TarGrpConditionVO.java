package com.zjtelcom.cpct.vo.grouping;


import com.zjtelcom.cpct.dto.channel.LabelValueVO;
import com.zjtelcom.cpct.dto.channel.OperatorDetail;
import com.zjtelcom.cpct.dto.grouping.SysAreaVO;
import com.zjtelcom.cpct.dto.grouping.TarGrpCondition;

import java.util.List;

/**
 * @Description 目标分群条件 VO
 * @Author pengy
 * @Date 2018/6/29 16:36
 */
public class TarGrpConditionVO extends TarGrpCondition {

    private String leftParamName;//左参名字
    private String operTypeName;//运算类型,1000> 2000< 3000==  4000!=   5000>=  6000<=  7000in   8000&   9000||   7100	not in
    private Long fitDomainId;//领域对象id
    private String fitDomainName;//领域对象名字
    private String promListName;
    private String labelCode;

    private String rightParamName;

    private String conditionType;

    private List<LabelValueVO> valueList;

    private List<OperatorDetail> operatorList;//运算符

    private List<SysAreaVO> sysAreaList;

    private String labelDataType;


    public String getRightParamName() {
        return rightParamName;
    }

    public void setRightParamName(String rightParamName) {
        this.rightParamName = rightParamName;
    }

    public String getLabelDataType() {
        return labelDataType;
    }

    public void setLabelDataType(String labelDataType) {
        this.labelDataType = labelDataType;
    }

    public String getLabelCode() {
        return labelCode;
    }

    public void setLabelCode(String labelCode) {
        this.labelCode = labelCode;
    }

    public String getPromListName() {
        return promListName;
    }

    public void setPromListName(String promListName) {
        this.promListName = promListName;
    }

    public List<SysAreaVO> getSysAreaList() {
        return sysAreaList;
    }

    public void setSysAreaList(List<SysAreaVO> sysAreaList) {
        this.sysAreaList = sysAreaList;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public List<LabelValueVO> getValueList() {
        return valueList;
    }

    public void setValueList(List<LabelValueVO> valueList) {
        this.valueList = valueList;
    }

    public List<OperatorDetail> getOperatorList() {
        return operatorList;
    }

    public void setOperatorList(List<OperatorDetail> operatorList) {
        this.operatorList = operatorList;
    }

    public String getLeftParamName() {
        return leftParamName;
    }

    public void setLeftParamName(String leftParamName) {
        this.leftParamName = leftParamName;
    }

    public String getOperTypeName() {
        return operTypeName;
    }

    public void setOperTypeName(String operTypeName) {
        this.operTypeName = operTypeName;
    }

    public Long getFitDomainId() {
        return fitDomainId;
    }

    public void setFitDomainId(Long fitDomainId) {
        this.fitDomainId = fitDomainId;
    }

    public String getFitDomainName() {
        return fitDomainName;
    }

    public void setFitDomainName(String fitDomainName) {
        this.fitDomainName = fitDomainName;
    }
}
