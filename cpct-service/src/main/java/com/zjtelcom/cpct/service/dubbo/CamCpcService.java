package com.zjtelcom.cpct.service.dubbo;

import com.ql.util.express.DefaultContext;

import java.util.List;
import java.util.Map;

public interface CamCpcService {

    Map<String, Object>  ActivityCpcTask(Map<String, String> params, Long activityId, Map<String, String> privateParams, Map<String, String> labelItems, List<Map<String, Object>> evtTriggers, List<Map<String, Object>> strategyMapList, DefaultContext<String, Object> context);

}
