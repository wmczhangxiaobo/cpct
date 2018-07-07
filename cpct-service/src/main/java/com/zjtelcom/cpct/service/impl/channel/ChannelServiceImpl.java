package com.zjtelcom.cpct.service.impl.channel;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjtelcom.cpct.common.Page;
import com.zjtelcom.cpct.dao.channel.ContactChannelMapper;
import com.zjtelcom.cpct.domain.channel.Channel;
import com.zjtelcom.cpct.dto.channel.ChannelDetail;
import com.zjtelcom.cpct.dto.channel.ContactChannelDetail;
import com.zjtelcom.cpct.dto.channel.ChannelVO;
import com.zjtelcom.cpct.enums.ChannelType;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.channel.ChannelService;
import com.zjtelcom.cpct.util.BeanUtil;
import com.zjtelcom.cpct.util.ChannelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;
import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;

@Service
public class ChannelServiceImpl extends BaseService implements ChannelService {

    @Autowired
    private ContactChannelMapper channelMapper;


    @Override
    public Map<String, Object> getChannelTreeForActivity(Long userId) {
        Map<String,Object> result = new HashMap<>();
        Map<String,Object> resultMap = new HashMap<>();

        List<ChannelDetail> initChannelList = new ArrayList<>();//主动
        List<ChannelDetail> passiveChannelList = new ArrayList<>();//被动

        List<Channel> parentList = channelMapper.findParentList();
        for (Channel parent : parentList){
            int initCount = 0;
            int passCount = 0;
            List<Channel> childList = channelMapper.findChildListByParentId(parent.getContactChlId());

            List<ChannelDetail> initChildList = new ArrayList<>();
            List<ChannelDetail> passChildList = new ArrayList<>();

            for (Channel child : childList){
                if (child.getChannelType().equals(ChannelType.INITIATIVE.getValue().toString())){
                    ChannelDetail detail = getDetail(child);
                    detail.setChildrenList(null);
                    initChildList.add(detail);
                    initCount++;
                }else {
                    ChannelDetail detail = getDetail(child);
                    detail.setChildrenList(null);
                    passChildList.add(detail);
                    passCount++;
                }
            }
            //所有主动渠道的渠道信息
            if (initCount >0 ){
                ChannelDetail detail = getDetail(parent);
                detail.setChildrenList(initChildList);
                initChannelList.add(detail);
            }
            if (passCount > 0){
                ChannelDetail detail = getDetail(parent);
                detail.setChildrenList(passChildList);
                passiveChannelList.add(detail);
            }
        }
        resultMap.put("initChannelList",initChannelList);
        resultMap.put("passiveChannelList",passiveChannelList);
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",resultMap);
        return result;

    }

    private ChannelDetail getDetail(Channel channel ){
        ChannelDetail detail = new ChannelDetail();
        detail.setChannelId(channel.getContactChlId());
        detail.setChannelName(channel.getContactChlName());
        return detail;
    }

    /**
     * 添加父级渠道
     * @param userId
     * @param parentAddVO
     * @return
     */
    @Override
    public Map<String, Object> createParentChannel(Long userId, ContactChannelDetail parentAddVO) {
        Map<String,Object> result = new HashMap<>();
        Channel channel = BeanUtil.create(parentAddVO,new Channel());
        channel.setParentId(null);
        channel.setChannelType(null);//主动被动
        channel.setCreateDate(new Date());
        channel.setUpdateDate(new Date());
        channel.setCreateStaff(userId);
        channel.setUpdateStaff(userId);
        channel.setStatusCd("1000");
        channelMapper.insert(channel);
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg","添加成功");
        return result;
    }

    /**
     * 添加子渠道
     * @param userId
     * @param addVO
     * @return
     */
    @Override
    public Map<String,Object> createContactChannel(Long userId, ContactChannelDetail addVO) {
        Map<String,Object> result = new HashMap<>();
        if (addVO.getParentId()==null){
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","请选择父级渠道添加");
            return result;
        }
        Channel parent = channelMapper.selectByPrimaryKey(addVO.getChannelId());
        if (parent==null){
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","父级渠道不存在");
            return result;
        }
        Channel channel = BeanUtil.create(addVO,new Channel());
        channel.setCreateDate(new Date());
        channel.setUpdateDate(new Date());
        channel.setCreateStaff(userId);
        channel.setUpdateStaff(userId);
        channel.setStatusCd("1000");
        channelMapper.insert(channel);
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg","添加成功");
        return result;
    }

    @Override
    public  Map<String,Object> modContactChannel(Long userId, ContactChannelDetail editVO) {
        Map<String,Object> result = new HashMap<>();
        Channel channel = channelMapper.selectByPrimaryKey(editVO.getChannelId());
        if (channel==null){
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","渠道不存在");
            return result;
        }
        BeanUtil.copy(editVO,channel);
        channel.setUpdateDate(new Date());
        channel.setUpdateStaff(userId);
        channelMapper.updateByPrimaryKey(channel);
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg","添加成功");
        return result;
    }

    @Override
    public  Map<String,Object> delContactChannel(Long userId, ContactChannelDetail channelDetail) {
        Map<String,Object> result = new HashMap<>();
        Channel channel = channelMapper.selectByPrimaryKey(channelDetail.getChannelId());
        if (channel==null){
            result.put("resultCode",CODE_FAIL);

            result.put("resultMsg","渠道不存在");
            return result;
        }
        channelMapper.deleteByPrimaryKey(channelDetail.getChannelId());
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg","添加成功");
        return result;
    }

    @Override
    public  Map<String,Object> getChannelList(Long userId,String channelName ,Integer page, Integer pageSize) {
        Map<String,Object> result = new HashMap<>();
        List<ChannelVO> voList = new ArrayList<>();
        PageHelper.startPage(page,pageSize);
        List<Channel> channelList = channelMapper.selectAll(channelName);
        Page pageInfo = new Page(new PageInfo(channelList));
        for (Channel channel : channelList){
            ChannelVO vo = ChannelUtil.map2ChannelVO(channel);
            voList.add(vo);
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",voList);
        result.put("pageInfo",pageInfo);
        return result;
    }

    @Override
    public Map<String, Object> getChannelListByType(Long userId, String channelType) {
        Map<String,Object> result = new HashMap<>();
        List<ChannelVO> voList = new ArrayList<>();
        List<Channel> channelList = channelMapper.selectByType(channelType);
        for (Channel channel : channelList){
            ChannelVO vo = ChannelUtil.map2ChannelVO(channel);
            voList.add(vo);
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",voList);
        return result;
    }

    @Override
    public  Map<String,Object> getChannelDetail(Long userId, Long channelId) {
        Map<String,Object> result = new HashMap<>();
        ChannelVO vo = new ChannelVO();
        try {
            Channel channel = channelMapper.selectByPrimaryKey(channelId);
            vo = ChannelUtil.map2ChannelVO(channel);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("[op:ChannelServiceImpl] fail to listChannel ", e);
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",vo);
        return result;
    }
}
