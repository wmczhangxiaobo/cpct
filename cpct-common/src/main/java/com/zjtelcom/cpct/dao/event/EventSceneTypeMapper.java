package com.zjtelcom.cpct.dao.event;

import com.zjtelcom.cpct.domain.event.EventSceneTypeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface EventSceneTypeMapper {
    int deleteByPrimaryKey(Long evtSceneTypeId);

    int insert(EventSceneTypeDO record);

    EventSceneTypeDO selectByPrimaryKey(Long evtSceneTypeId);

    List<EventSceneTypeDO> selectAll();

    int updateByPrimaryKey(EventSceneTypeDO record);

    List<EventSceneTypeDO> listEventSceneTypes(@Param("evtSceneTypeId") Long evtSceneTypeId,@Param("parEvtSceneTypeId") Long parEvtSceneTypeId);

    int saveEventSceneType(EventSceneTypeDO eventTypeDO);

}