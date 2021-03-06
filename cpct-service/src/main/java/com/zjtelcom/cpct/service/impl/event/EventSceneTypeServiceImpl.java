package com.zjtelcom.cpct.service.impl.event;

import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.event.EventSceneTypeMapper;
import com.zjtelcom.cpct.domain.event.EventSceneTypeDO;
import com.zjtelcom.cpct.dto.event.EventSceneTypeDTO;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.event.EventSceneTypeService;
import com.zjtelcom.cpct.util.CopyPropertiesUtil;
import com.zjtelcom.cpct.util.DateUtil;
import com.zjtelcom.cpct.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;
import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;

/**
 * @Description EventTypeServiceImpl
 * @Author pengy
 * @Date 2018/6/21 9:46
 */
@Service
@Transactional
public class EventSceneTypeServiceImpl extends BaseService implements EventSceneTypeService {

    public static final Long EVT_TYPE_ID_NULL = null;
    public static final Long PAR_EVT_TYPE_ID_NULL = null;
    public static final Long PAR_EVT_TYPE_ID_ZERO = 0L;
    public static final int LIST_SIZE_ZERO = 0;

    @Autowired
    private EventSceneTypeMapper eventSceneTypeMapper;

    /**
     * 查询事件目录
     */
    @Override
    public List<EventSceneTypeDTO> listEventSceneTypes() {
        List<EventSceneTypeDO> eventLists = new ArrayList<>();
        List<EventSceneTypeDTO> eventTypeDTOS = new ArrayList<>();
        try {
            //查询出父级菜单
            eventLists = eventSceneTypeMapper.listEventSceneTypes(EVT_TYPE_ID_NULL, PAR_EVT_TYPE_ID_ZERO);
            eventTypeDTOS = generateTree(eventLists);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[op:EventTypeServiceImpl] fail to listEventSceneTypes ", e);
        }
        return eventTypeDTOS;
    }

    /**
     * 新增事件目录保存
     */
    @Override
    public void saveEventSceneType(EventSceneTypeDO eventTypeDO) {
        try {
            eventTypeDO.setCreateDate(DateUtil.getCurrentTime());
            eventTypeDO.setStatusDate(DateUtil.getCurrentTime());
            eventTypeDO.setUpdateStaff(UserUtil.loginId());
            eventTypeDO.setCreateStaff(UserUtil.loginId());
            eventTypeDO.setUpdateDate(DateUtil.getCurrentTime());
            eventTypeDO.setStatusCd(CommonConstant.STATUSCD_EFFECTIVE);
            eventSceneTypeMapper.saveEventSceneType(eventTypeDO);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[op:EventTypeServiceImpl] fail to saveEventSceneType ", e);
        }
    }

    /**
     * 编辑事件目录
     */
    @Override
    public EventSceneTypeDTO getEventSceneTypeDTOById(Long evtTypeId) {
        EventSceneTypeDTO eventTypeDTO = new EventSceneTypeDTO();
        try {
            EventSceneTypeDO eventTypeDO =  eventSceneTypeMapper.selectByPrimaryKey(evtTypeId);
            CopyPropertiesUtil.copyBean2Bean(eventTypeDTO,eventTypeDO);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[op:EventTypeServiceImpl] fail to getEventSceneTypeDTOById ", e);
        }
        return eventTypeDTO;
    }

    /**
     * 编辑事件目录保存
     */
    @Override
    public Map<String, Object> updateEventSceneType(EventSceneTypeDO eventTypeDO) {
        Map<String, Object> result = new HashMap<>();
        try {
            EventSceneTypeDO evt = eventSceneTypeMapper.selectByPrimaryKey(eventTypeDO.getEvtSceneTypeId());
            if (evt==null){
                result.put("resultCode",CODE_FAIL);
                result.put("resultMsg","事件目录不存在");
                return result;
            }
            if (!eventTypeDO.getEvtSceneTypeName().equals(evt.getEvtSceneTypeName())){
                evt.setEvtSceneTypeName(eventTypeDO.getEvtSceneTypeName());
            }
            evt.setUpdateDate(new Date());
            evt.setUpdateStaff(UserUtil.loginId());
            eventSceneTypeMapper.updateByPrimaryKey(evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[op:EventTypeServiceImpl] fail to updateEventSceneType ", e);
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "编辑成功");
        return result;
    }

    /**
     * 删除事件目录
     */
    @Override
    public Map<String, Object> delEventSceneType(Long evtTypeId) {
        Map<String, Object> result = new HashMap<>();
        try {
            EventSceneTypeDO evt = eventSceneTypeMapper.selectByPrimaryKey(evtTypeId);
            if (evt==null){
                result.put("resultCode",CODE_FAIL);
                result.put("resultMsg","事件目录不存在");
                return result;
            }
            eventSceneTypeMapper.deleteByPrimaryKey(evtTypeId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[op:EventTypeServiceImpl] fail to delEventSceneType ", e);
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "删除成功");
        return result;

    }

    /**
     * generate Tree
     */
    private List<EventSceneTypeDTO> generateTree(List<EventSceneTypeDO> eventLists) {
        List<EventSceneTypeDTO> dtoList = new ArrayList<>();
        for (EventSceneTypeDO eventTypeDO : eventLists) {
            if (eventTypeDO.getParEvtSceneTypeId() == PAR_EVT_TYPE_ID_ZERO) {
                EventSceneTypeDTO eventTypeDTO = new EventSceneTypeDTO();
                eventTypeDTO.setEvtSceneTypeId(eventTypeDO.getEvtSceneTypeId());
                eventTypeDTO.setParEvtSceneTypeId(eventTypeDO.getParEvtSceneTypeId());
                eventTypeDTO.setEvtSceneTypeName(eventTypeDO.getEvtSceneTypeName());
                dtoList.add(eventTypeDTO);
            }
            // 为一级菜单设置子菜单，getChild是递归调用的
            for (EventSceneTypeDTO eventTypeDTO : dtoList) {
                eventTypeDTO.setEventSceneTypeDTOList(getChild(eventTypeDTO.getEvtSceneTypeId(), eventLists));
            }
        }
        return dtoList;
    }

    /**
     * 递归查找子菜单
     */
    private List<EventSceneTypeDTO> getChild(Long id, List<EventSceneTypeDO> rootMenu) {
        // 子菜单
        List<EventSceneTypeDTO> childList = new ArrayList<>();
        for (EventSceneTypeDO eventTypeDO : rootMenu) {
            // 遍历所有节点，将父菜单id与传过来的id比较
            if (eventTypeDO.getParEvtSceneTypeId() != PAR_EVT_TYPE_ID_ZERO) {
                if (eventTypeDO.getParEvtSceneTypeId() == id) {
                    EventSceneTypeDTO eventTypeDTO = new EventSceneTypeDTO();
                    eventTypeDTO.setEvtSceneTypeId(eventTypeDO.getEvtSceneTypeId());
                    eventTypeDTO.setEvtSceneTypeName(eventTypeDO.getEvtSceneTypeName());
                    eventTypeDTO.setParEvtSceneTypeId(eventTypeDO.getParEvtSceneTypeId());
                    childList.add(eventTypeDTO);
                }
            }
        }
        // 把子菜单的子菜单再循环一遍
        for (EventSceneTypeDTO eventTypeDTO : childList) {
            List<EventSceneTypeDO> list = eventSceneTypeMapper.listEventSceneTypes(EVT_TYPE_ID_NULL, eventTypeDTO.getParEvtSceneTypeId());
            if (list.size() != LIST_SIZE_ZERO) {
                // 递归
                eventTypeDTO.setEventSceneTypeDTOList(getChild(eventTypeDTO.getEvtSceneTypeId(), rootMenu));
            }
        } // 递归退出条件
        if (childList.size() == LIST_SIZE_ZERO) {
            return null;
        }
        return childList;
    }

}
