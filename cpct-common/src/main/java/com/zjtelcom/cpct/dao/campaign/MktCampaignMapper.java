package com.zjtelcom.cpct.dao.campaign;


import com.zjtelcom.cpct.domain.campaign.MktCamDisplayColumnRel;
import com.zjtelcom.cpct.domain.campaign.MktCampaignCountDO;
import com.zjtelcom.cpct.domain.campaign.MktCampaignDO;
import com.zjtelcom.cpct.dto.campaign.MktCampaign;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @Description:
 * @author: linchao
 * @date: 2018/09/06 16:14
 * @version: V1.0
 */
@Mapper
@Repository
public interface MktCampaignMapper {
    int deleteByPrimaryKey(Long mktCampaignId);

    int insert(MktCampaignDO mktCampaignDO);

    MktCampaignDO selectByPrimaryKey(Long mktCampaignId);

    List<MktCampaignDO> selectBatch(@Param("mktCampaignIds") List<Long> mktCampaignIds);

    List<MktCampaignDO> selectAll();

    int updateByPrimaryKey(MktCampaignDO mktCampaignDO);

    List<MktCampaignCountDO> qryMktCampaignListPage(MktCampaignDO mktCampaignDO);

    List<MktCampaignCountDO> qryMktCampaignList4Moudle(MktCampaignDO mktCampaignDO);

    List<MktCampaignCountDO> qryMktCampaignListPageForNoPublish(MktCampaignDO mktCampaignDO);

    List<MktCampaignCountDO> qryMktCampaignListPageForPublish(MktCampaignDO mktCampaignDO);

    List<MktCampaignCountDO> qryMktCampaignListPage4Sync(@Param("map") Map<String,Object> map);

    void changeMktCampaignStatus(@Param("mktCampaignId")Long mktCampaignId, @Param("statusCd")String statusCd, @Param("updateDate")Date updateDate, @Param("updateStaff")Long updateStaff);

    List<MktCampaignDO> qryMktCampaignListByCondition(MktCampaignDO mktCampaignDO);

    List<MktCampaignDO> qryMktCampaignListByTypeAndStatus(@Param("execType") String execType, @Param("statusCd") String statusCd);

    MktCampaignDO selectByRuleId(@Param("ruleId") Long ruleId);

    List<MktCamDisplayColumnRel> selectAllGroupByCamId();

    List<MktCampaignDO> selectCampaignByInitId(@Param("initId")Long initId);

    MktCampaignDO selectByInitId(@Param("initId")Long initId);

    MktCampaignDO selectByInitId3(@Param("initId")Long initId);

    MktCampaignDO selectPrimaryKeyByInitId(@Param("initId")Long initId, @Param("statusCd")String statusCd);

    MktCampaignDO selectByInitForRollBack(@Param("initId")Long initId);

    int countByStatus(@Param("map")Map<String, Object> map);

    int countBylanIdFour(@Param("map")Map<String, Object> map);

    int countByTrial(@Param("map")Map<String, Object> map);

    List<MktCampaignDO> queryRptBatchOrderForMktCampaign(HashMap<String, Object> paramMap);

    List<MktCampaignDO> getMktCampaignDetails(HashMap<String, Object> hashMap);

    List<MktCampaignDO> getMktCampaignDetailsForDate(@Param("mktCampaignId")List<String> s);

    List<MktCampaignDO> selectAllMktCampaignDetailsByStatus(@Param("status")List<String> status, @Param("staff")Long createStaffId);

    List<Long> getUserListTempMktCamCodeList();

    List<Long> getCreateStaffList();

    void updateByStaffToC4AndC5(@Param("map")Map<String, Object> map);

    void updateMktCampaignByC4OfSysArea(@Param("map")HashMap<String, Object> map);

    List<Long> getByOrgNameC4IsNotNull();

    List<MktCampaignDO> getActivityStatisticsByName(@Param("name")String s);

    List<MktCampaignDO> queryRptBatchOrderForMktCampaignFromDate(HashMap<String, Object> paramMap);

    List<MktCampaignDO> selectCampaignTheme(@Param("directoryId") Long directoryId, @Param("date") String date,@Param("type") String type);

    List<MktCampaignDO> selectCampaignThemeByC3(@Param("directoryId") Long directoryId, @Param("startDate") String startDate,@Param("endDate") String endDate,@Param("type") String type,@Param("lanId") String lanId,@Param("regionFlg")String regionFlg);

    Integer getCountFromActivityTheme(@Param("startDate") String startDate,@Param("type") String type,@Param("endDate")String endDate);

    Integer getCountFromActivityThemeByC3(@Param("startDate") String startDate,@Param("type") String type,@Param("endDate")String endDate,@Param("lanId") String lanId, @Param("regionFlg")String regionFlg);

    List<MktCampaignDO> getMktCampaignFromInitId(@Param("date") String date,@Param("type") String type);

    List<MktCampaignDO> getMktCampaignFromInitIdFromStatus(@Param("startDate") String startDate,@Param("endDate") String endDate,@Param("type") String type,@Param("status") String status);

    List<MktCampaignDO> selectCampaignThemeFromStatus(@Param("value") String value, @Param("startDate") String startDate,@Param("endDate") String endDate,@Param("type") String type,@Param("status") String status);

    List<MktCampaignDO> getQuarterActivities(@Param("value")Long value, @Param("statusCd")String statusCd, @Param("startDate")String startDate,@Param("endDate") String endDate, @Param("type")String type,@Param("lanId") String lanId, @Param("regionFlg")String regionFlg);

    MktCampaignDO selectByInitIdFromOne(@Param("initId")Long initId);



    List<MktCampaignDO> getQuarterActivitiesIsEnd(@Param("value")Long value, @Param("statusCd")String statusCd, @Param("startDate")String startDate,@Param("endDate") String endDate, @Param("type")String type,@Param("lanId") String lanId, @Param("regionFlg")String regionFlg);

    List<MktCampaignCountDO> qryMktCampaignListPage4Count(MktCampaignDO mktCampaignDO);

    int saveMktCamDesc(MktCampaignDO mktCampaignDOS);

    List<String> selectByMktCampaingIDFromTrial(HashMap<String, Object> paramMap);

    String getCloseRuleNameFromMktCamId(Long mktCampaignId);


    List<MktCampaignDO> listByCreateDate(@Param("createDateStart") Date createDateStart,@Param("createDateEnd") Date createDateEnd);


    List<MktCampaignDO> listByCode(@Param("code") String code);

    List<MktCampaignDO> getAllTheme( @Param("theme") String theme);

    List<MktCampaignDO> getCampaignList();

    List<MktCampaignDO> listDisturbedCampaignForlanId(@Param("lanId") Long lanId, @Param("tiggerType") String tiggerType);

    List<MktCampaignDO> getByC3AndAuto(@Param("lanId") Long lanId, @Param("mktCampaignName") String mktCampaignName);

    int updateStaffById(MktCampaignDO mktCampaignDO);
}