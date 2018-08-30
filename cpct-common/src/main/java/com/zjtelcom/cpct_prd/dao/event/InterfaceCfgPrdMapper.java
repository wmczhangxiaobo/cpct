package com.zjtelcom.cpct_prd.dao.event;



import com.zjtelcom.cpct.domain.event.InterfaceCfg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InterfaceCfgPrdMapper {
    int deleteByPrimaryKey(Long interfaceCfgId);

    int insert(InterfaceCfg record);

    InterfaceCfg selectByPrimaryKey(Long interfaceCfgId);

    List<InterfaceCfg> selectAll();

    List<InterfaceCfg> findInterfaceCfgListByParam(@Param("evtSrcId") Long evtSrcId, @Param("interfaceName") String interfaceName, @Param("interfaceType") String interfaceType);

    int updateByPrimaryKey(InterfaceCfg record);

}