package com.zjtelcom.cpct.dubbo.service;

import java.util.Map;

public interface TrialRedisService {

    Map<String,Object> searchFromRedis(String key);

    Map<String,Object> updateOperationStatus(Long batchNum,String status);

}
