package com.zjtelcom.cpct.service.grouping;

import com.zjtelcom.cpct.domain.grouping.TarGrpRel;
import com.zjtelcom.cpct.dto.grouping.TarGrp;
import com.zjtelcom.cpct.dto.grouping.TarGrpCondition;
import com.zjtelcom.cpct.dto.grouping.TarGrpDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 目标分群service
 * @Author pengy
 * @Date 2018/6/25 10:34
 */
public interface TarGrpService {

    Map<String, Object> createTarGrpByTemplateId(Long templateId,Long  oldTarGrpId,String needDelete);

    Map<String, Object> createTarGrp(TarGrpDetail tarGrpDetail,boolean isCopy);

    Map<String, Object> saveTagNumFetch(Long mktCamGrpRulId, List<TarGrpCondition> tarGrpConditionDTOList);

    Map<String,Object> delTarGrpCondition(Long conditionId, Long closeRule);

    Map<String,Object> delTarGrpCondition(Long conditionId);

    Map<String,Object> editTarGrpConditionDO(Long conditionId);

    Map<String,Object> updateTarGrpCondition(TarGrpCondition tarGrpCondition);

    Map<String, Object> saveBigDataModel(Long mktCamGrpRulId);

    Map<String,Object> listTarGrpCondition(Long tarGrpId) ;

    Map<String,Object> listBigDataModel(Long mktCamGrpRulId);

    Map<String,Object> modTarGrp(TarGrpDetail tarGrpDetail);

    Map<String,Object> delTarGrp(TarGrpDetail tarGrpDetail);

    Map<String,Object> copyTarGrp(Long tarGrpId,boolean isCopy);

    Map<String,Object> labelListByCampaignId(List<Integer> campaignId);

    Map<String,Object> labelListByEventId(Long eventId);

    Map<String,Object> conditionSwitch(Long conditionId,String type,String value);

    Integer modTarGrpOther(TarGrp tarGrp);

    Map<String, Object> queryTarGrpOther(TarGrp tarGrp);

    Map<String, Object> getNewTarGrpByTemplate(TarGrpRel tarGrpRel);

    Map<String, Object> getTarGrpRel(Long id);

    Map<String, Object> deleteTarGrpRel(Long id);

    Map<String, Object> updateTarGrpName(TarGrp tarGrp);
}
