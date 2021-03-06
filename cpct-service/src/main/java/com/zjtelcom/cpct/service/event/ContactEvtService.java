package com.zjtelcom.cpct.service.event;

import com.zjtelcom.cpct.common.Page;
import com.zjtelcom.cpct.dto.event.ContactEvt;
import com.zjtelcom.cpct.request.event.CreateContactEvtJtReq;
import com.zjtelcom.cpct.request.event.CreateContactEvtReq;

import java.util.List;
import java.util.Map;

/**
 * @Description EventService
 * @Author pengy
 * @Date 2018/6/21 9:45
 */

public interface ContactEvtService {

    Map<String,Object> listMktCampaignType(Long userId);

    Map<String,Object> listEventsForCam(ContactEvt contactEvt,Page pageInfo);

    Map<String,Object> listEvents(ContactEvt contactEvt,Page pageInfo);

    Map<String,Object> listEventNoPages(ContactEvt contactEvt);

    Map<String,Object> selectContactEvtByChlCode(Map<String, Object> params);

    Map<String,Object> createContactEvtJt(CreateContactEvtJtReq createContactEvtJtReq) throws Exception;

    Map<String,Object> createContactEvt(CreateContactEvtReq createContactEvtReq) throws Exception;

    Map<String,Object> delEvent(Long contactEvtId);

    Map<String,Object> closeEvent(Long contactEvtId,String statusCd);

    Map<String,Object> editEvent(Long contactEvtId) throws Exception;

    Map<String,Object> modContactEvtJt(CreateContactEvtJtReq createContactEvtJtReq);

    Map<String,Object> modContactEvt(CreateContactEvtReq createContactEvtReq) throws Exception;

    Map<String,Object> evtDetails(ContactEvt contactEvt);

    Map<String,Object> evtDetailsByIdList(List<Integer> idList );

    Map<String,Object> editEventRelConfig(Map<String,Object> param);

    Map<String,Object> getEventRelConfig(Map<String,Object> param);

    Map<String,Object> selectBatchByCode(List<String> contactChlCodeList);

    void xxxxxx1();

    void xxxxxx2();

    void xxxxxx3();
}
