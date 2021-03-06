package com.zjtelcom.cpct.dto.grouping;

import java.io.Serializable;
import java.util.Map;

public class TrialResponse implements Serializable {
    private String resultCode;
    private String resultMsg;
    private Map<String,Object>  hitsList;
    private Long total;

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public Map<String, Object> getHitsList() {
        return hitsList;
    }

    public void setHitsList(Map<String, Object> hitsList) {
        this.hitsList = hitsList;
    }
}
