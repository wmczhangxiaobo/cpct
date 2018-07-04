package com.zjtelcom.cpct.service.channel;

import com.zjtelcom.cpct.dto.channel.ContactChannelDetail;
import com.zjtelcom.cpct.dto.channel.ChannelEditVO;

import java.util.Map;


/**
 * @Description ChannelService
 * @Author hyf
 * @Date 2018/06/21
 */
public interface ChannelService {

    Map<String,Object> createContactChannel (Long userId, ContactChannelDetail addVO);

    Map<String,Object> modContactChannel (Long userId, ContactChannelDetail editVO);

    Map<String,Object> delContactChannel (Long userId,ContactChannelDetail channelDetail);

    Map<String,Object> getChannelList(Long userId,String channelName,Integer page,Integer pageSize);

    Map<String,Object> getChannelDetail(Long userId,Long channelId);

    Map<String,Object> getChannelListByType(Long userId,String channelType);





}