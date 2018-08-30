package com.zjtelcom.cpct.elastic.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CampaignHitResponse implements Serializable {
    private Map<String,Object> labelInfo;
    private Map<String,Object> eventInfo;
    private Map<String,Object> campaignInfo;
    private Long total;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Map<String, Object> getLabelInfo() {
        return labelInfo;
    }

    public void setLabelInfo(Map<String, Object> labelInfo) {
        this.labelInfo = labelInfo;
    }

    public Map<String, Object> getEventInfo() {
        return eventInfo;
    }

    public void setEventInfo(Map<String, Object> eventInfo) {
        this.eventInfo = eventInfo;
    }

    public Map<String, Object> getCampaignInfo() {
        return campaignInfo;
    }

    public void setCampaignInfo(Map<String, Object> campaignInfo) {
        this.campaignInfo = campaignInfo;
    }
}