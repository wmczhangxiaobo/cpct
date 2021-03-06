package com.zjtelcom.cpct.dao.channel;

import com.zjtelcom.cpct.domain.channel.DisplayColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 展示列持久层
 *
 * @author pengyu
 */
@Mapper
@Repository
public interface DisplayColumnMapper {
    int deleteByPrimaryKey(Long displayColumnId);

    int insert(DisplayColumn record);

    DisplayColumn selectByPrimaryKey(Long displayColumnId);

    List<DisplayColumn> selectAll();

    List<DisplayColumn> findDisplayListByParam(@Param("displayName") String displayName,@Param("displayType") String displayType);

    int updateByPrimaryKey(DisplayColumn record);

    List<DisplayColumn> findDisplayListByName(@Param("displayName") String displayName);
}