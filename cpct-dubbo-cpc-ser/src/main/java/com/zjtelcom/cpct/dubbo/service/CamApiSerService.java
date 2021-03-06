package com.zjtelcom.cpct.dubbo.service;

import com.ql.util.express.DefaultContext;

import java.util.List;
import java.util.Map;

public interface CamApiSerService {

    Map<String, Object>  ActivitySerTask(Map<String, String> params, Long activityId, Map<String, String> privateParams, Map<String, String> labelItems, List<Map<String, Object>> evtTriggers, List<Map<String, Object>> strategyMapList, DefaultContext<String, Object> context);

    Map<String, Object>  ActivityXieTongTask(Map<String, String> params, Long activityId, Map<String, String> privateParams, Map<String, String> labelItems, List<Map<String, Object>> evtTriggers, List<Map<String, Object>> strategyMapList, DefaultContext<String, Object> context);

}
