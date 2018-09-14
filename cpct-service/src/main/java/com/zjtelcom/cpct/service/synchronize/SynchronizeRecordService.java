package com.zjtelcom.cpct.service.synchronize;

import com.zjtelcom.cpct.dto.synchronize.SynchronizeRecord;

/**
 * @Auther: anson
 * @Date: 2018/8/27
 * @Description:同步记录
 */
public interface SynchronizeRecordService {

    int insert(SynchronizeRecord record);


    int addRecord(String roleName, String name,Long eventId, Integer type);
}
