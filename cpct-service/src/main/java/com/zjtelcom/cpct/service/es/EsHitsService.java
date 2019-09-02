package com.zjtelcom.cpct.service.es;

import com.alibaba.fastjson.JSONObject;
import com.zjtelcom.cpct.elastic.model.CampaignHitParam;

import java.util.Map;

public interface EsHitsService {

    void add() throws Exception;

    void save(JSONObject jsonObject, String indexName);

    void save(JSONObject jsonObject, String indexName, String _id);

    Map<String,Object> searchCampaignHitsInfo(CampaignHitParam param);

    Map<String,Object> searchCampaignHitsTotal(CampaignHitParam param);

    Map<String,Object> searchLabelInfoByRuleId(String ruleId, String isi, String hitEntity);
}
