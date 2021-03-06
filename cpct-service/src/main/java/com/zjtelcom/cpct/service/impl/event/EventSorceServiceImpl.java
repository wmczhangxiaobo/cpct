/**
 * @(#)EventSorceServiceImpl.java, 2018/8/20.
 * <p/>
 * Copyright 2018 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.zjtelcom.cpct.service.impl.event;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjtelcom.cpct.common.Page;
import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.event.EventSorceMapper;
import com.zjtelcom.cpct.dao.system.SysParamsMapper;
import com.zjtelcom.cpct.domain.event.EventSorceDO;
import com.zjtelcom.cpct.domain.system.SysParams;
import com.zjtelcom.cpct.dto.event.EventSorce;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.event.EventSorceService;
import com.zjtelcom.cpct.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Description:
 * author: linchao
 * date: 2018/08/20 16:04
 * version: V1.0
 */
@Service
@Transactional
public class EventSorceServiceImpl extends BaseService implements EventSorceService {



    @Autowired
    private EventSorceMapper eventSorceMapper;

    @Autowired
    private SysParamsMapper sysParamsMapper;


    /**
     * 新增事件源
     *
     * @param eventSorce
     * @return
     */
    @Override
    public Map<String, Object> saveEventSorce(EventSorce eventSorce) {
        Map<String, Object> eventSorceMap = new HashMap<>();
        final EventSorceDO eventSorceDO = BeanUtil.create(eventSorce, new EventSorceDO());
        try {
            eventSorceDO.setEvtSrcCode("ERC"+DateUtil.date2String(new Date())+ChannelUtil.getRandomStr(4));
            eventSorceDO.setCreateStaff(UserUtil.loginId());
            eventSorceDO.setCreateDate(new Date());
            eventSorceDO.setUpdateStaff(UserUtil.loginId());
            eventSorceDO.setUpdateDate(new Date());
            eventSorceMapper.insert(eventSorceDO);
            eventSorceMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            eventSorceMap.put("resultMsg", "新增事件源成功！");
            eventSorceMap.put("evtSrcId", eventSorceDO.getEvtSrcId());
        } catch (Exception e) {
            eventSorceMap.put("resultCode", CommonConstant.CODE_FAIL);
            eventSorceMap.put("resultMsg", "新增事件源失败！");
            logger.error("[op:EventSorceServiceImpl] 新增事件源eventSorce = {}失败！Exception: ", JSON.toJSON(eventSorce), e);
        }
        return eventSorceMap;
    }

    /**
     * 查询事件源
     *
     * @param evtSrcId
     * @return
     */
    @Override
    public Map<String, Object> getEventSorce(Long evtSrcId) {
        Map<String, Object> eventSorceMap = new HashMap<>();
        try {

            EventSorceDO eventSorceDO = eventSorceMapper.selectByPrimaryKey(evtSrcId);
            EventSorce eventSorce = BeanUtil.create(eventSorceDO, new EventSorce());
            if (eventSorceDO.getRegionId()!=null){
                SysParams sysParams = sysParamsMapper.findParamsByValue("LOC-0001",eventSorceDO.getRegionId().toString());
                eventSorce.setRegionName(sysParams.getParamName());
            }
            eventSorceMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            eventSorceMap.put("resultMsg", "查询事件源成功！");
            eventSorceMap.put("eventSorce", eventSorce);
        } catch (Exception e) {
            eventSorceMap.put("resultCode", CommonConstant.CODE_FAIL);
            eventSorceMap.put("resultMsg", "查询事件源失败！");
            logger.error("[op:EventSorceServiceImpl] 通过evtSrcId = {}查询事件源eventSorce = {}失败！Exception: ", evtSrcId, e);
        }
        return eventSorceMap;
    }

    /**
     * 更新事件源
     *
     * @param eventSorce
     * @return
     */
    @Override
    public Map<String, Object> updateEventSorce(EventSorce eventSorce) {
        Map<String, Object> eventSorceMap = new HashMap<>();
        final EventSorceDO eventSorceDO = BeanUtil.create(eventSorce, new EventSorceDO());
        try {
            eventSorceDO.setUpdateStaff(UserUtil.loginId());
            eventSorceDO.setUpdateDate(new Date());
            eventSorceMapper.updateByPrimaryKey(eventSorceDO);
            eventSorceMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            eventSorceMap.put("resultMsg", "更新事件源成功！");
            eventSorceMap.put("evtSrcId", eventSorceDO.getEvtSrcId());
        } catch (Exception e) {
            eventSorceMap.put("resultCode", CommonConstant.CODE_FAIL);
            eventSorceMap.put("resultMsg", "更新事件源失败！");
            logger.error("[op:EventSorceServiceImpl] 更新事件源eventSorce = {}失败！Exception: ", JSON.toJSON(eventSorce), e);
        }
        return eventSorceMap;
    }

    /**
     * 删除事件源
     *
     * @param evtSrcId
     * @return
     */
    @Override
    public Map<String, Object> deleteEventSorce(final Long evtSrcId) {
        Map<String, Object> eventSorceMap = new HashMap<>();
        try {
            eventSorceMapper.deleteByPrimaryKey(evtSrcId);
            eventSorceMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            eventSorceMap.put("resultMsg", "删除事件源成功！");
            eventSorceMap.put("evtSrcId", evtSrcId);
        } catch (Exception e) {
            eventSorceMap.put("resultCode", CommonConstant.CODE_FAIL);
            eventSorceMap.put("resultMsg", "查询事件源失败！");
            logger.error("[op:EventSorceServiceImpl] 通过evtSrcId = {} 删除事件源失败！Exception: ", evtSrcId, e);
        }

        return eventSorceMap;
    }

    /**
     * 分页查询事件源列表
     *
     * @param evtSrcCode
     * @param evtSrcName
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Map<String, Object> listEventSorcePage(String evtSrcCode, String evtSrcName, Integer page, Integer pageSize) {
        Map<String, Object> eventSorceMap = new HashMap<>();
        try {
            EventSorceDO eventSorceDOReq = new EventSorceDO();
            eventSorceDOReq.setEvtSrcCode(evtSrcCode);
            eventSorceDOReq.setEvtSrcName(evtSrcName);
            PageHelper.startPage(page, pageSize);
            List<EventSorceDO> eventSorceDOList = eventSorceMapper.selectForPage(eventSorceDOReq);
            Page pageInfo = new Page(new PageInfo(eventSorceDOList));
            List<EventSorce> eventSorcesList = new ArrayList<>();
            for (EventSorceDO eventSorceDO : eventSorceDOList) {
                EventSorce eventSorce = BeanUtil.create(eventSorceDO, new EventSorce());
                if (eventSorceDO.getRegionId()!=null){
                    SysParams sysParams = sysParamsMapper.findParamsByValue("LOC-0001",eventSorceDO.getRegionId().toString());
                    eventSorce.setRegionName(sysParams.getParamName());
                }
                eventSorcesList.add(eventSorce);
            }
            eventSorceMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            eventSorceMap.put("resultMsg", "分页查询事件源列表成功！");
            eventSorceMap.put("eventSorcesList", eventSorcesList);
            eventSorceMap.put("page",pageInfo);
        } catch (Exception e) {
            eventSorceMap.put("resultCode", CommonConstant.CODE_FAIL);
            eventSorceMap.put("resultMsg", "分页查询事件源列表失败！");
            logger.error("[op:EventSorceServiceImpl] 通过evtSrcCode = {}, evtSrcName = {}, page = {}, pageSize = {} 分页查询事件源列表失败！Exception: ", evtSrcCode, evtSrcName, page, pageSize, e);
        }
        return eventSorceMap;
    }

    /**
     * 查询所有事件源列表
     *
     * @return
     */
    @Override
    public Map<String, Object> listEventSorceAll() {


        Map<String, Object> eventSorceMap = new HashMap<>();
        try {
            List<EventSorceDO> eventSorceDOList = eventSorceMapper.selectAll();
            List<EventSorce> eventSorcesList = new ArrayList<>();
            for (EventSorceDO eventSorceDO : eventSorceDOList) {
                EventSorce eventSorce = BeanUtil.create(eventSorceDO, new EventSorce());
                eventSorcesList.add(eventSorce);
            }
            eventSorceMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            eventSorceMap.put("resultMsg", "查询事件源列表成功！");
            eventSorceMap.put("eventSorcesList", eventSorcesList);
        } catch (Exception e) {
            eventSorceMap.put("resultCode", CommonConstant.CODE_FAIL);
            eventSorceMap.put("resultMsg", "查询事件源列表失败！");
            logger.error("[op:EventSorceServiceImpl] 查询事件源列表失败！Exception: " , e);
        }
        return eventSorceMap;
    }
}