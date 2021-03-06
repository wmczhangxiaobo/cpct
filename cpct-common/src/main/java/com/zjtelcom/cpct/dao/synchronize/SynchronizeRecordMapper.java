package com.zjtelcom.cpct.dao.synchronize;

import com.zjtelcom.cpct.dto.synchronize.SynchronizeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: anson
 * @Date: 2018/8/27
 * @Description:同步操作记录
 */
@Mapper
@Repository
public interface SynchronizeRecordMapper {

    int insert(SynchronizeRecord record);

    List<SynchronizeRecord> selectList(SynchronizeRecord record);
}
