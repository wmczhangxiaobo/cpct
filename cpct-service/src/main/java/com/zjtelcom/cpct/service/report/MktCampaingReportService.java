package com.zjtelcom.cpct.service.report;

import java.util.Map;

/**
 * @Description:
 * @author: linchao
 * @date: 2019/11/07 20:36
 * @version: V1.0
 */
public interface MktCampaingReportService {

    Map<String, Object> getHeadInfo(Map<String, Object> params);

    Map<String, Object> getOperationInfo(Map<String, Object> params);

    Map<String, Object> getChannelInfo(Map<String, Object> params);

    Map<String, Object> getTypeInfo(Map<String, Object> params);

    Map<String, Object> getTimeInfo(Map<String, Object> params);

    Map<String, Object> getRegionInfo(Map<String, Object> params);

}