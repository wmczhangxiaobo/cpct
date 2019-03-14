package com.zjtelcom.cpct.service.impl.channel;

import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.channel.EventRelMapper;
import com.zjtelcom.cpct.dao.event.ContactEvtMapper;
import com.zjtelcom.cpct.domain.channel.EventRel;
import com.zjtelcom.cpct.dto.event.ContactEvt;
import com.zjtelcom.cpct.service.channel.EventRelService;
import com.zjtelcom.cpct.util.BeanUtil;
import com.zjtelcom.cpct.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;

@Service
@Transactional
public class EventRelServiceImpl implements EventRelService {

    @Autowired
    private EventRelMapper eventRelMapper;
    @Autowired
    private ContactEvtMapper contactEvtMapper;

    @Override
    public Map<String,Object> getEventNoRelation(Long userId, ContactEvt contactEvt) {
        Map<String,Object> resultMap = new HashMap<>();
        List<ContactEvt> eventList = new ArrayList<>();
        List<Long> delEventRelList = new ArrayList<>();
        List<ContactEvt> contactEvtList = contactEvtMapper.query();
        List<EventRel> eventRelList = eventRelMapper.selectByZEvtId(contactEvt.getContactEvtId());

        for(int i = 0;i<eventRelList.size();i++) {
            delEventRelList.add(eventRelList.get(i).getaEvtId());
        }
        delEventRelList.add(contactEvt.getContactEvtId());
        for(int i = 0;i<contactEvtList.size();i++) {
            if(!delEventRelList.contains(contactEvtList.get(i).getContactEvtId())) {
                eventList.add(contactEvtList.get(i));
            }
        }
        resultMap.put("resultCode", CODE_SUCCESS);
        resultMap.put("resultMsg", eventList);
        return resultMap;
    }

    @Override
    public Map<String,Object> creatEventRelation(Long userId, EventRel addVO) {
        Map<String,Object> resultMap = new HashMap<>();
        EventRel eventRel = BeanUtil.create(addVO, new EventRel());
        eventRel.setSort(1L);
        eventRel.setCreateDate(DateUtil.getCurrentTime());
        eventRel.setUpdateDate(DateUtil.getCurrentTime());
        eventRel.setStatusDate(DateUtil.getCurrentTime());
        eventRel.setUpdateStaff(userId);
        eventRel.setCreateStaff(userId);
        eventRel.setStatusCd(CommonConstant.STATUSCD_EFFECTIVE);
        eventRelMapper.insert(eventRel);
        resultMap.put("resultCode", CODE_SUCCESS);
        resultMap.put("resultMsg", "添加成功");
        return resultMap;
    }

}