package com.zjtelcom.cpct.dto.filter;

import com.zjtelcom.cpct.BaseEntity;
import java.io.Serializable;
import java.util.Date;

/**
 * @Description 规律规则实体类
 * @Author pengy
 * @Date 2018/6/26 15:32
 */
public class FilterRule extends BaseEntity implements Serializable{

    private static final long serialVersionUID = -1612387382337537350L;
    private Long ruleId;//规则标识
    private String ruleName;//规则名称
    private String filterType;//过滤类型
    private String executionChannel;//执行渠道
    private String channelContacts;//渠道执行次数
    private Date effectiveDate;//生效时间
    private Date failureDate;//失效时间

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getExecutionChannel() {
        return executionChannel;
    }

    public void setExecutionChannel(String executionChannel) {
        this.executionChannel = executionChannel;
    }

    public String getChannelContacts() {
        return channelContacts;
    }

    public void setChannelContacts(String channelContacts) {
        this.channelContacts = channelContacts;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getFailureDate() {
        return failureDate;
    }

    public void setFailureDate(Date failureDate) {
        this.failureDate = failureDate;
    }

}