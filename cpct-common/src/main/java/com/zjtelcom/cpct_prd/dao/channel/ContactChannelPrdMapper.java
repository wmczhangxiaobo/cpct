package com.zjtelcom.cpct_prd.dao.channel;

import com.zjtelcom.cpct.domain.channel.Channel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface ContactChannelPrdMapper {
    int deleteByPrimaryKey(Long contactChlId);

    int insert(Channel record);

    Channel selectByPrimaryKey(@Param("contactChlId") Long contactChlId);

    Channel selectChannel4AllChannel(Long contactChlId);

    List<Channel> selectAll(@Param("channelName") String channelName);

    List<Channel> selectByType(@Param("channelType") String channelType);

    List<Channel> findParentList();

    List<Channel> findChildListByParentId(@Param("parentId") Long parentId);

    int updateByPrimaryKey(Channel record);

    List<Channel> findChildList();

    Channel selectByCode(@Param("code") String code);

}