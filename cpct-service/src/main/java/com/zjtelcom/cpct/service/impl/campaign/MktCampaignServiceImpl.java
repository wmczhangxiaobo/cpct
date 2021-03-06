package com.zjtelcom.cpct.service.impl.campaign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ctzj.smt.bss.centralized.web.util.BssSessionHelp;
import com.ctzj.smt.bss.cooperate.service.dubbo.ICpcAPIService;
import com.ctzj.smt.bss.sysmgr.model.common.SysmgrResultObject;
import com.ctzj.smt.bss.sysmgr.model.dataobject.SystemPost;
import com.ctzj.smt.bss.sysmgr.model.dto.SystemPostDto;
import com.ctzj.smt.bss.sysmgr.model.dto.SystemUserDto;
import com.ctzj.smt.bss.sysmgr.model.query.QrySystemPostReq;
import com.ctzj.smt.bss.sysmgr.privilege.service.dubbo.api.ISystemPostDubboService;
import com.ctzj.smt.bss.sysmgr.privilege.service.dubbo.api.ISystemUserDtoDubboService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjtelcom.cpct.common.Page;
import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.campaign.*;
import com.zjtelcom.cpct.dao.channel.*;
import com.zjtelcom.cpct.dao.event.ContactEvtMapper;
import com.zjtelcom.cpct.dao.filter.FilterRuleMapper;
import com.zjtelcom.cpct.dao.filter.MktStrategyCloseRuleRelMapper;
import com.zjtelcom.cpct.dao.grouping.TarGrpConditionMapper;
import com.zjtelcom.cpct.dao.grouping.TarGrpMapper;
import com.zjtelcom.cpct.dao.grouping.TrialOperationMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfRuleMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyFilterRuleRelMapper;
import com.zjtelcom.cpct.dao.system.SysAreaMapper;
import com.zjtelcom.cpct.dao.system.SysParamsMapper;
import com.zjtelcom.cpct.domain.SysArea;
import com.zjtelcom.cpct.domain.campaign.*;
import com.zjtelcom.cpct.domain.channel.*;
import com.zjtelcom.cpct.domain.grouping.TrialOperation;
import com.zjtelcom.cpct.domain.strategy.MktStrategyCloseRuleRelDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfRuleDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyFilterRuleRelDO;
import com.zjtelcom.cpct.domain.system.SysParams;
import com.zjtelcom.cpct.dto.campaign.*;
import com.zjtelcom.cpct.dto.channel.LabelDTO;
import com.zjtelcom.cpct.dto.event.ContactEvt;
import com.zjtelcom.cpct.dto.event.EventDTO;
import com.zjtelcom.cpct.dto.filter.FilterRule;
import com.zjtelcom.cpct.dto.grouping.TarGrp;
import com.zjtelcom.cpct.dto.grouping.TarGrpCondition;
import com.zjtelcom.cpct.dto.pojo.Result;
import com.zjtelcom.cpct.dto.strategy.MktStrategyConf;
import com.zjtelcom.cpct.dto.strategy.MktStrategyConfDetail;
import com.zjtelcom.cpct.enums.*;
import com.zjtelcom.cpct.open.service.completeMktCampaign.OpenCompleteMktCampaignService;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.campaign.MktCamDisplayColumnRelService;
import com.zjtelcom.cpct.service.campaign.MktCampaignService;
import com.zjtelcom.cpct.service.campaign.MktDttsLogService;
import com.zjtelcom.cpct.service.campaign.MktOperatorLogService;
import com.zjtelcom.cpct.service.channel.ProductService;
import com.zjtelcom.cpct.service.channel.SearchLabelService;
import com.zjtelcom.cpct.service.cpct.ProjectManageService;
import com.zjtelcom.cpct.service.dubbo.UCCPService;
import com.zjtelcom.cpct.service.event.EventRedisService;
import com.zjtelcom.cpct.service.grouping.TrialProdService;
import com.zjtelcom.cpct.service.report.ActivityStatisticsService;
import com.zjtelcom.cpct.service.strategy.MktStrategyConfService;
import com.zjtelcom.cpct.service.system.SysAreaService;
import com.zjtelcom.cpct.util.*;
import com.zjtelcom.cpct_offer.dao.inst.RequestInfoMapper;
import com.zjtelcom.cpct_offer.dao.inst.RequestInstRelMapper;
import com.zjtelcom.cpct.dao.offer.OfferProdMapper;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import static com.zjtelcom.cpct.constants.CommonConstant.*;
import static com.zjtelcom.cpct.enums.DateUnit.MONTH;
import static com.zjtelcom.cpct.enums.DateUnit.YEAR;
import static com.zjtelcom.cpct.enums.StatusCode.CAM_RESOURCE;
import static com.zjtelcom.cpct.enums.StatusCode.STATUS_CODE_PUBLISHED;
import static com.zjtelcom.cpct.enums.StatusCode.STATUS_CODE_ROLL;
import static com.zjtelcom.cpct.util.DateUtil.*;

/**
 * Description:
 * author: linchao
 * date: 2018/06/22 22:22
 * version: V1.0
 */
@Service
@Transactional
public class MktCampaignServiceImpl extends BaseService implements MktCampaignService {


//    @Override
//    public Map<String, Object> checkCampaignByRequestInfo(Map<String, Object> params) {
//        Map<String,Object> result = new HashMap<>();
//        Long requestInfoId = MapUtil.getLongNum(params.get("requestInfoId"));
//        RequestInfo requestInfo = requestInfoMapper.selectByPrimaryKey(requestInfoId);
//        if (requestInfo==null){
//            result.put("resultCode", CODE_SUCCESS);
//            result.put("resultMsg", "未查询到有效的需求函信息");
//            result.put("flg","false");
//            return result;
//        }
//        String level = UserUtil.getSysUserLevel();
////        String level = MapUtil.getString(params.get("level"));
//        String key = requestInfo.getActivitiKey();
//        Map<String,Object> data = new HashMap<>();
//        String campaignType = "";
//        String chufaType = "";
//        String periodType = "";
//        Long campaignId = MapUtil.getLongNum(params.get("campaignId"));
//        if (!"0".equals(campaignId.toString())){
//            MktCampaignDO campaignDO = mktCampaignMapper.selectByPrimaryKey(campaignId);
//            if (campaignDO!=null ){
//                if (campaignDO.getSrcId()!=null){
//                    periodType = campaignDO.getMktCampaignCategory();
//                }
//                addParam(requestInfo, key,campaignType,chufaType,periodType,data);
//                if (campaignType.equals("1000") && !campaignDO.getMktCampaignType().equals("1000")){
//                    result.put("resultCode", CODE_SUCCESS);
//                    result.put("resultMsg","需求函类型与活动类型不匹配，请重新选择。（营销活动）");
//                    result.put("flg","false");
//                    return result;
//                }
//                if (campaignType.equals("5000") && campaignDO.getMktCampaignType().equals("1000")){
//                    result.put("resultCode", CODE_SUCCESS);
//                    result.put("resultMsg","需求函类型与活动类型不匹配，请重新选择。（服务类活动）");
//                    result.put("flg","false");
//                    return result;
//                }
//                if (chufaType.equals("1000") && !campaignDO.getTiggerType().equals("1000")){
//                    result.put("resultCode", CODE_SUCCESS);
//                    result.put("resultMsg","需求函类型与活动类型不匹配，请重新选择。（批量活动）");
//                    result.put("flg","false");
//                    return result;
//                }
//                if (chufaType.equals("2000") && !campaignDO.getTiggerType().equals("2000")){
//                    result.put("resultCode", CODE_SUCCESS);
//                    result.put("resultMsg","需求函类型与活动类型不匹配，请重新选择。（实时活动）");
//                    result.put("flg","false");
//                    return result;
//                }
//                if (periodType.equals("6100") && !campaignDO.getMktCampaignCategory().equals("6100")){
//                    result.put("resultCode", CODE_SUCCESS);
//                    result.put("resultMsg","需求函类型与活动类型不匹配，请重新选择。（自主活动）");
//                    result.put("flg","false");
//                    return result;
//                }
//                if (periodType.equals("6300") && !campaignDO.getMktCampaignCategory().equals("6300")){
//                    result.put("resultCode", CODE_SUCCESS);
//                    result.put("resultMsg","需求函类型与活动类型不匹配，请重新选择。（框架活动）");
//                    result.put("flg","false");
//                    return result;
//                }
//                if (periodType.equals("6300") || periodType.equals("6100") ){
//                    periodType = "";
//                }
//            }
//        }
//        if ("C1".equals(level) || "C2".equals(level)){
//            addParam(requestInfo, key,campaignType,chufaType,periodType,data);
//            result.put("resultCode", CODE_SUCCESS);
//            result.put("resultMsg", "");
//            result.put("flg","true");
//            result.put("data",data);
//            return result;
//        }else if (!"mkt_free_city_process".equals(key) && !"mkt_free_province_process".equals(key)){
//            result.put("resultCode", CODE_SUCCESS);
//            result.put("resultMsg", "地市工号只能创建自主营销活动，请重新选择需求函类型");
//            result.put("flg","false");
//            return result;
//        }
//        if ("C3".equals(level)){
//            addParam(requestInfo, key,campaignType,chufaType,periodType,data);
//            result.put("resultCode", CODE_SUCCESS);
//            result.put("resultMsg", "");
//            result.put("flg","true");
//            result.put("data",data);
//            return result;
//        }
//        if ("C4".equals(level) ){
//            if (!requestInfo.getBusinessType().equals("1000")){
//                result.put("resultCode", CODE_SUCCESS);
//                result.put("resultMsg", "地市工号只能创建批量活动，请重新选择需求函类型");
//                result.put("flg","false");
//                return result;
//            }
//            addParam(requestInfo, key,campaignType,chufaType,periodType,data);
//            result.put("resultCode", CODE_SUCCESS);
//            result.put("resultMsg", "");
//            result.put("flg","true");
//            result.put("data",data);
//            return result;
//        }
//        if ("C5".equals(level) ){
//            result.put("resultCode", CODE_SUCCESS);
//            result.put("resultMsg", "无法创建活动");
//            result.put("flg","false");
//            return result;
//        }
//        return result;
//    }

    @Override
    public Map<String, Object> checkCampaignByRequestInfo(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Long campaignId = MapUtil.getLongNum(params.get("campaignId"));
        String level = UserUtil.getSysUserLevel();
        Map<String, Object> data = new HashMap<>();
        if ("C1".equals(level) || "C2".equals(level)) {
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg", "");
            result.put("flg", "true");
            return result;
        }
        if ("C5".equals(level)) {
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg", "无法修改活动");
            result.put("flg", "false");
            return result;
        }
        if (!"0".equals(campaignId.toString())) {
            MktCampaignDO campaignDO = mktCampaignMapper.selectByPrimaryKey(campaignId);
            if (campaignDO != null) {
                if ("C3".equals(level)) {
                    if (!"1000".equals(campaignDO.getMktCampaignType()) || !StatusCode.AUTONOMICK_CAMPAIGN.getStatusCode().equals(campaignDO.getMktCampaignCategory())) {
                        result.put("resultCode", CODE_SUCCESS);
                        result.put("resultMsg", "您没有权限调整该活动，请重新选择");
                        result.put("flg", "false");
                        return result;
                    }

                }
                if ("C4".equals(level)) {
                    if (!"1000".equals(campaignDO.getMktCampaignType()) || !"1000".equals(campaignDO.getTiggerType()) || !"1000".equals(campaignDO.getExecType())) {
                        result.put("resultCode", CODE_SUCCESS);
                        result.put("resultMsg", "您没有权限调整该活动，请重新选择");
                        result.put("flg", "false");
                        return result;
                    }
                }
            }
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "");
        result.put("flg", "true");
        return result;
    }

    private void addParam(RequestInfo requestInfo, String key, String campaignType, String chufaType, String periodType, Map<String, Object> data) {
        switch (key) {
            case "mkt_province_ser_process"://服务（随销）活动
                campaignType = "5000";
                periodType = periodType.equals("") ? "6300" : periodType;
                break;
            case "mkt_free_city_process"://地市自主活动
                campaignType = "1000";
                periodType = periodType.equals("") ? "6300" : periodType;
                break;
            case "mkt_free_province_process"://省自主活动
                campaignType = "1000";
                periodType = periodType.equals("") ? "6300" : periodType;
                break;
            case "mkt_force_province"://框架活动
                campaignType = "1000";
                periodType = periodType.equals("") ? "6100" : periodType;
                break;
            case "mkt_force_province_city"://框架活动子活动
                campaignType = "1000";
                periodType = "6300";
                break;
        }
        if (requestInfo.getBusinessType().equals("1000")) {//2000 ： 实时    1000：批量
            chufaType = "1000";
        } else {
            chufaType = "2000";
        }
        data.put("campaignType", campaignType);
        data.put("chufaType", chufaType);
        data.put("periodType", periodType);
    }

    // 集团活动承接接口
    @Override
    public void acceptGroupCampaign(MktCampaignDO mktCampaignDO) {
        SystemUserDto user = UserUtil.getUser();
        if (user != null) {
            mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCampaignDO.getMktCampaignId());
            mktCampaignDO.setSrcId("1");
            mktCampaignMapper.updateByPrimaryKey(mktCampaignDO);
            // 调用创建需求函接口
            logger.info("【添加需求函信息】" + mktCampaignDO.getMktCampaignName());

            generateRequest(mktCampaignDO, user);
            logger.info("【需求函信息添加成功】" + mktCampaignDO.getMktCampaignName());
            try {
                // 更新complete表状态
                logger.info("【反馈】" + mktCampaignDO.getMktCampaignName());
                Map<String, Object> map = openCompleteMktCampaignService.completeMktCampaign(mktCampaignDO.getInitId(), "1100", "10");
                logger.info("【反馈结束】" + JSON.toJSONString(map));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 集团活动不承接接口
    @Override
    public void notAcceptGroupCampaign(MktCampaignDO mktCampaignDO) {
        mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCampaignDO.getMktCampaignId());
        mktCampaignDO.setStatusCd(STATUS_CODE_ROLL.getStatusCode());
        mktCampaignDO.setSrcId("2");
        mktCampaignMapper.updateByPrimaryKey(mktCampaignDO);
        try {
            logger.info("【反馈】" + mktCampaignDO.getMktCampaignName());
            Map<String, Object> map = openCompleteMktCampaignService.completeMktCampaign(mktCampaignDO.getInitId(), "1100", "11");
            logger.info("【反馈结束】" + JSON.toJSONString(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 营销活动
     */
    @Autowired
    private MktCampaignMapper mktCampaignMapper;
    @Autowired
    private CatalogItemMapper catalogItemMapper;

    /**
     * 活动关联
     */
    @Autowired
    private MktCampaignRelMapper mktCampaignRelMapper;

    /**
     * 活动与事件关联
     */
    @Autowired
    private MktCamEvtRelMapper mktCamEvtRelMapper;
    /**
     * 系统参数
     */
    @Autowired
    private SysParamsMapper sysParamsMapper;
    /**
     * 事件
     */
    @Autowired
    private ContactEvtMapper contactEvtMapper;

    @Autowired
    private MktCamChlConfAttrMapper confAttrMapper;
    /**
     * 策略配置和活动关联
     */
    @Autowired
    private MktCamStrategyConfRelMapper mktCamStrategyConfRelMapper;
    /**
     * 策略配置基本信息
     */
    @Autowired
    private MktStrategyConfMapper mktStrategyConfMapper;
    /**
     * 策略配置service
     */
    @Autowired
    private MktStrategyConfService mktStrategyConfService;
    /**
     * 下发城市与活动关联
     */
    @Autowired
    private MktCamCityRelMapper mktCamCityRelMapper;

    /**
     * 下发地市
     */
    @Autowired
    private SysAreaMapper sysAreaMapper;

    @Autowired
    private MktStrategyConfRuleMapper ruleMapper;

    @Autowired
    private OfferProdMapper offerMapper;
    @Autowired
    private FilterRuleMapper filterRuleMapper;

    /**
     * redis
     */
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private RedisUtils_es redisUtils_es;

    @Autowired
    private TarGrpConditionMapper tarGrpConditionMapper;

    @Autowired
    private MktCamDirectoryMapper mktCamDirectoryMapper;

    @Autowired
    private MktCamResultRelMapper mktCamResultRelMapper;

    @Autowired
    private MktStrategyConfRuleMapper mktStrategyConfRuleMapper;

    @Autowired
    private ObjMktCampaignRelMapper objMktCampaignRelMapper;
    //需求涵id 跟活动关联关系
    @Autowired(required = false)
    private RequestInstRelMapper requestInstRelMapper;

    @Autowired
    private MktCamItemMapper mktCamItemMapper;

    @Autowired
    private MktVerbalMapper verbalMapper;

    @Autowired
    private MktStrategyCloseRuleRelMapper mktStrategyCloseRuleRelMapper;


    /**
     * 过滤规则与策略关联 Mapper
     */
    @Autowired
    private MktStrategyFilterRuleRelMapper mktStrategyFilterRuleRelMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private MktOperatorLogService mktOperatorLogService;

    @Autowired
    private TarGrpMapper tarGrpMapper;

    @Autowired(required = false)
    private ISystemUserDtoDubboService iSystemUserDtoDubboService;


    @Autowired(required = false)
    private ISystemPostDubboService iSystemPostDubboService;

    private final static String createChannel = "cpcpcj0001";

    @Autowired
    private RequestInfoMapper requestInfoMapper;

    @Autowired
    private ContactChannelMapper channelMapper;
    @Autowired
    private MktCamChlConfMapper mktCamChlConfMapper;
    /**
     * 展示列
     */
    @Autowired
    private DisplayColumnLabelMapper displayColumnLabelMapper;

    /**
     * 活动与展示列关联
     */
    @Autowired
    private MktCamDisplayColumnRelMapper mktCamDisplayColumnRelMapper;

    @Autowired
    private MktCamDisplayColumnRelService mktCamDisplayColumnRelService;


    @Autowired
    private MktCamChlConfAttrMapper mktCamChlConfAttrMapper;
    @Autowired
    private SearchLabelService searchLabelService;
    @Autowired
    private TrialProdService trialProdService;
    @Autowired
    private SysAreaService sysAreaService;
    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired(required = false)
    private UCCPService uccpService;

    @Autowired
    private MktCampaignCompleteMapper mktCampaignCompleteMapper;
    @Autowired(required = false)
    private OpenCompleteMktCampaignService openCompleteMktCampaignService;
    @Autowired
    private ActivityStatisticsService activityStatisticsService;
    @Autowired
    private TrialOperationMapper trialOperationMapper;
    @Autowired
    private MktDttsLogService mktDttsLogService;
    @Autowired
    private ProjectManageService projectManageService;
    @Autowired
    private EventRedisService eventRedisService;
    @Autowired(required = false)
    private ICpcAPIService iCpcAPIService;
    @Autowired
    private ObjectLabelRelMapper objectLabelRelMapper;
    @Autowired
    private TopicLabelMapper topicLabelMapper;
    @Autowired
    private InjectionLabelMapper labelMapper;
    @Autowired
    private LabelValueMapper labelValueMapper;
    @Autowired
    private ObjCatItemRelMapper objCatItemRelMapper;

    @Autowired
    private MktCamResourceMapper mktCamResourceMapper;

    //指定下发地市人员的数据集合
    private final static String CITY_PUBLISH = "CITY_PUBLISH";
    // 集团活动指定承接人
    private final static String GROUP_CAMPAIGN_RECIPIENT = "GROUP_CAMPAIGN_RECIPIENT";
    @Autowired
    private MktRequestMapper mktRequestMapper;

    /**
     * 校验协同渠道时间是否在活动时间范围之内
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> channelEffectDateCheck(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Date camStart = new Date((Long) params.get("camStart"));
        Date camEnd = new Date((Long) params.get("camEnd"));
        if (params.get("campaignId") == null || params.get("campaignId").toString().equals("")) {
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg", "请先保存活动");
            result.put("data", "true");
            return result;
        }
        String campaignId = String.valueOf((Integer) params.get("campaignId"));
        System.out.println("活动开始时间：" + DateUtil.Date2String(camStart));
        System.out.println("活动失效时间：" + DateUtil.Date2String(camEnd));
        System.out.println("活动id" + campaignId);
        List<MktCamChlConfAttrDO> startDoList = mktCamChlConfAttrMapper.selectAttrStartDateByCampaignId(Long.valueOf(campaignId));
        for (MktCamChlConfAttrDO attrDO : startDoList) {
            if (attrDO.getAttrValue() == null || attrDO.getAttrValue().equals("")) {
                continue;
            }
            if (new Date(Long.valueOf(attrDO.getAttrValue())).before(camStart)) {
                System.out.println("协同渠道开始时间：" + DateUtil.Date2String(new Date(Long.valueOf(attrDO.getAttrValue()))));
                String ruleName = "";
                List<MktStrategyConfRuleDO> ruleDOList = mktStrategyConfRuleMapper.selectByCampaignId(Long.valueOf(campaignId));
                for (MktStrategyConfRuleDO ruleDO : ruleDOList) {
                    List<Long> evtConfList = ChannelUtil.StringToIdList(ruleDO.getEvtContactConfId());
                    if (evtConfList.contains(attrDO.getEvtContactConfId())) {
                        ruleName = ruleDO.getMktStrategyConfRuleName();
                        break;
                    }
                }
                if (!"".equals(ruleName)) {
                    result.put("resultCode", CODE_SUCCESS);
                    result.put("resultMsg", "协同渠道开始时间不符合规范，请检查规则：[" + ruleName + "]");
                    result.put("data", "true");
                    return result;
                }
            }
        }

        List<MktCamChlConfAttrDO> endDoList = mktCamChlConfAttrMapper.selectAttrEndDateByCampaignId(Long.valueOf(campaignId));
        for (MktCamChlConfAttrDO attrDO : endDoList) {
            if (attrDO.getAttrValue() == null) {
                continue;
            }
            if (new Date(Long.valueOf(attrDO.getAttrValue())).after(camEnd)) {
                System.out.println("协同渠道结束时间：" + DateUtil.Date2String(new Date(Long.valueOf(attrDO.getAttrValue()))));
                String ruleName = "";
                List<MktStrategyConfRuleDO> ruleDOList = mktStrategyConfRuleMapper.selectByCampaignId(Long.valueOf(campaignId));
                for (MktStrategyConfRuleDO ruleDO : ruleDOList) {
                    List<Long> evtConfList = ChannelUtil.StringToIdList(ruleDO.getEvtContactConfId());
                    if (evtConfList.contains(attrDO.getEvtContactConfId())) {
                        ruleName = ruleDO.getMktStrategyConfRuleName();
                        break;
                    }
                }
                if (!"".equals(ruleName)) {
                    result.put("resultCode", CODE_SUCCESS);
                    result.put("resultMsg", "协同渠道开始时间不符合规范，请检查规则：[" + ruleName + "]");
                    result.put("data", "true");
                    return result;
                }
            }
        }

        //集团活动环节信息更新反馈
        MktCampaignDO campaign = mktCampaignMapper.selectByPrimaryKey(Long.valueOf(campaignId));
        MktCampaignComplete mktCampaignComplete = mktCampaignCompleteMapper.selectByCampaignIdAndTacheCdAndTacheValueCd(campaign.getInitId(), "1200", "10");
        if (mktCampaignComplete != null) {
//            mktCampaignComplete.setEndTime(new Date());
//            mktCampaignComplete.setTacheValueCd("11");
//            mktCampaignComplete.setStatusCd("1200");
//            mktCampaignComplete.setUpdateStaff(UserUtil.loginId());
//            mktCampaignComplete.setUpdateDate(new Date());
//            mktCampaignCompleteMapper.update(mktCampaignComplete);
//            MktCampaignComplete campaignComplete = mktCampaignCompleteMapper.selectByCampaignIdAndTacheCd(campaign.getInitId(), "1300");
//            if(campaignComplete == null) {
//                MktCampaignComplete mktCamComplete = new MktCampaignComplete();
//                mktCamComplete.setMktCampaignId(mktCampaignComplete.getMktCampaignId());
//                mktCamComplete.setMktActivityNbr(mktCampaignComplete.getMktActivityNbr());
//                mktCamComplete.setOrderId(mktCampaignComplete.getOrderId());
//                mktCamComplete.setOrderName(mktCampaignComplete.getOrderName());
//                mktCamComplete.setTacheCd("1300");
//                mktCamComplete.setTacheValueCd("10");
//                mktCamComplete.setBeginTime(new Date());
//                mktCamComplete.setEndTime(new Date());
//                mktCamComplete.setSort(Long.valueOf("3"));
//                mktCamComplete.setStatusCd("1100");
//                mktCamComplete.setStatusDate(new Date());
//                mktCamComplete.setCreateStaff(campaign.getCreateStaff());
//                mktCamComplete.setCreateDate(new Date());
//                mktCampaignCompleteMapper.insert(mktCamComplete);
            try {
                openCompleteMktCampaignService.completeMktCampaign(campaign.getInitId(), "1200", "11");
                openCompleteMktCampaignService.completeMktCampaign(campaign.getInitId(), "1300", "10");
            } catch (Exception e) {
                e.printStackTrace();
            }
//            }

        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "校验通过");
        result.put("data", "false");
        return result;
    }

    @Override
    public Map<String, Object> searchByCampaignId(Long campaignId) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> strategyMap = new HashMap<>();
        List<MktStrategyConfDO> strategyConfList = mktStrategyConfMapper.selectByCampaignId(campaignId);
        for (MktStrategyConfDO strategyConfDO : strategyConfList) {
            strategyMap.put("strategyId", strategyConfDO.getMktStrategyConfId());
            strategyMap.put("strategyName", strategyConfDO.getMktStrategyConfName());
            Map<String, Object> ruleMap = new HashMap<>();
            List<MktStrategyConfRuleDO> ruleDOList = mktStrategyConfRuleMapper.selectByMktStrategyConfId(strategyConfDO.getMktStrategyConfId());
            for (MktStrategyConfRuleDO ruleDO : ruleDOList) {
                ruleMap.put("ruleId", ruleDO.getMktStrategyConfRuleId());
                ruleMap.put("ruleName", ruleDO.getMktStrategyConfRuleName());
                TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(ruleDO.getTarGrpId());
                Map<String, Object> tarMap = new HashMap<>();
                if (tarGrp != null) {
                    tarMap.put("tarGrpId", tarGrp.getTarGrpId());
                    ruleMap.put("tarGrp", tarMap);
                }
            }
            strategyMap.put("rule", ruleMap);
        }
        result.put("campaignId", campaignId);
        result.put("strategy", strategyMap);
        return result;
    }

    /**
     * 添加活动基本信息 并建立关系
     *
     * @param mktCampaignVO
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> createMktCampaign(MktCampaignDetailVO mktCampaignVO) throws Exception {
        Map<String, Object> maps = new HashMap<>();
        try {
            MktCampaignDO mktCampaignDO = BeanUtil.create(mktCampaignVO, new MktCampaignDO());
            // 创建活动基本信息
            mktCampaignDO.setCreateDate(new Date());
            mktCampaignDO.setCreateStaff(UserUtil.loginId());
            mktCampaignDO.setUpdateDate(new Date());
            mktCampaignDO.setUpdateStaff(UserUtil.loginId());
            mktCampaignDO.setStatusDate(new Date());
            mktCampaignDO.setMktCampaignNameEdit(mktCampaignDO.getMktCampaignName());
            //添加所属地市
            if (UserUtil.getUser() != null) {
                // 获取当前用户
                mktCampaignDO.setRegionId(UserUtil.getUser().getLanId());
                // 获取当前用户的岗位编码包含“cpcpch”
                SystemUserDto userDetail = UserUtil.getRoleCode();
                for (SystemPostDto role : userDetail.getSystemPostDtoList()) {
                    // 判断是否为超级管理员
                    if (role.getSysPostCode().contains(PostEnum.ADMIN.getPostCode())) {
                        mktCampaignDO.setCreateChannel(role.getSysPostCode());
                        break;
                    } else if (role.getSysPostCode().contains("cpcpch")) {
                        mktCampaignDO.setCreateChannel(role.getSysPostCode());
                        continue;
                    }
                }
            } else {
                mktCampaignDO.setRegionId(AreaCodeEnum.ZHEJIAGN.getRegionId());
                mktCampaignDO.setCreateChannel(PostEnum.ADMIN.getPostCode());
            }

//            // 判断是否有创建人信息和岗位信息
//            if (mktCampaignDO.getCreateChannel() == null && mktCampaignDO.getCreateStaff() == 1) {
//                maps.put("resultCode", CommonConstant.CODE_FAIL);
//                maps.put("resultMsg", "创建人信息和岗位信息都为空，请核实工号已选中的岗位权限");
//                logger.info("创建人信息和岗位信息都为空，请核实工号已选中的岗位权限" + JSON.toJSONString(mktCampaignDO));
//                return maps;
//            }
//            if (mktCampaignDO.getCreateChannel() == null) {
//                maps.put("resultCode", CommonConstant.CODE_FAIL);
//                maps.put("resultMsg", "岗位信息都为空，请核实工号已选中的岗位权限");
//                logger.info("岗位信息都为空，请核实工号已选中的岗位权限" + JSON.toJSONString(mktCampaignDO));
//                return maps;
//            }
//            if (mktCampaignDO.getCreateStaff() == 1) {
//                maps.put("resultCode", CommonConstant.CODE_FAIL);
//                maps.put("resultMsg", "创建人信息为空，请核实工号已选中的岗位权限");
//                logger.info("创建人信息为空，请核实工号已选中的岗位权限" + JSON.toJSONString(mktCampaignDO));
//                return maps;
//            }
//            if (mktCampaignDO.getUpdateStaff() == 1) {
//                maps.put("resultCode", CommonConstant.CODE_FAIL);
//                maps.put("resultMsg", "更新人信息为空，请核实工号已选中的岗位权限");
//                logger.info("更新人信息为空，请核实工号已选中的岗位权限" + JSON.toJSONString(mktCampaignDO));
//                return maps;
//            }

            mktCampaignDO.setServiceType(StatusCode.CUST_TYPE.getStatusCode()); // 1000 - 客账户类
            mktCampaignDO.setLanId(AreaCodeEnum.getLandIdByRegionId(mktCampaignDO.getRegionId()));
            Map<String, Object> landFourAndFiveMap = getLandFourAndFive();
            if (landFourAndFiveMap != null) {
                mktCampaignDO.setLanIdFour((Long) landFourAndFiveMap.get("C4"));
                mktCampaignDO.setLanIdFive((Long) landFourAndFiveMap.get("C5"));
            }
            logger.info("c4: ===" + (Long) landFourAndFiveMap.get("C4"));
            logger.info("C5: ===" + (Long) landFourAndFiveMap.get("C5"));


//          保存活动活动名称默认拼上地市信息
            String c3Name = "";
            String c4Name = "";
            if (mktCampaignDO.getLanIdFour() != null) {
                SysArea sysArea = sysAreaMapper.selectByPrimaryKey(mktCampaignDO.getLanIdFour().intValue());
                if (sysArea != null) {
                    c4Name = sysArea.getName();
                } else {
                    Organization organization = organizationMapper.selectByPrimaryKey(mktCampaignDO.getLanIdFour());
                    if (organization != null) {
                        c4Name = organization.getOrgName();
                    }
                }
            }
            if (mktCampaignDO.getLanId() != null) {
                SysArea sysArea = sysAreaMapper.selectByPrimaryKey(mktCampaignDO.getLanId().intValue());
                if (sysArea != null) {
                    c3Name = sysArea.getName();
                }
            }
            String campaignName = mktCampaignDO.getMktCampaignName();

            String creatChannel = mktCampaignDO.getCreateChannel() == null ? "" : mktCampaignDO.getCreateChannel();
            String sysPostCode = "";
            if (creatChannel.equals(AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysPostCode())) {
                sysPostCode = AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysArea();
            } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.SHENGJI.getSysPostCode())) {
                sysPostCode = AreaCodeEnum.sysAreaCode.SHENGJI.getSysArea();
            } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.FENGONGSI.getSysPostCode())) {
                sysPostCode = AreaCodeEnum.sysAreaCode.FENGONGSI.getSysArea();
            } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.FENGJU.getSysPostCode())) {
                sysPostCode = AreaCodeEnum.sysAreaCode.FENGJU.getSysArea();
            } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.ZHIJU.getSysPostCode())) {
                sysPostCode = AreaCodeEnum.sysAreaCode.ZHIJU.getSysArea();
            } else {
                sysPostCode = AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysArea();
            }
            mktCampaignDO.setRegionFlg(sysPostCode);


            //获取活动目录二级
            CatalogItem catalog;
            String catalogName = "";
            if (mktCampaignDO.getDirectoryId() != null) {
                catalog = catalogItemMapper.selectByPrimaryKey(mktCampaignDO.getDirectoryId());
                if (catalog != null) {
                    catalogName = catalog.getCatalogItemName();
                }
            }
            logger.info("活动目录二级" + catalogName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String datestr = simpleDateFormat.format(mktCampaignDO.getCreateDate()).replace("-", "");
            ;


            mktCampaignMapper.insert(mktCampaignDO);
            Long mktCampaignId = mktCampaignDO.getMktCampaignId();
            // 活动编码
            mktCampaignDO.setMktActivityNbr("MKT" + String.format("%06d", mktCampaignId));
            String newCampaignName = mktCampaignDO.getMktCampaignNameEdit().replace("_", "-");
            if ("C1".equals(mktCampaignDO.getRegionFlg()) || "C2".equals(mktCampaignDO.getRegionFlg())) {
                mktCampaignDO.setMktCampaignName("【省】" + "_" + catalogName + "_" + newCampaignName + "_" + datestr);
            } else if ("C3".equals(mktCampaignDO.getRegionFlg())) {
                mktCampaignDO.setMktCampaignName("【市】" + c3Name + "_" + catalogName + "_" + newCampaignName + "_" + datestr);
            } else if ("C4".equals(mktCampaignDO.getRegionFlg())) {
                mktCampaignDO.setMktCampaignName("【区】" + c3Name + c4Name + "_" + catalogName + "_" + newCampaignName + "_" + datestr);
            }

            mktCampaignDO.setInitId(mktCampaignId);
            mktCampaignMapper.updateByPrimaryKey(mktCampaignDO);
            //创建主题关系
            topicLabelRel(mktCampaignId, mktCampaignDO);
            if (mktCampaignDO.getDirectoryId() != null) {
                CatalogItem catalogItem = catalogItemMapper.selectByPrimaryKey(mktCampaignDO.getDirectoryId());
                if (catalogItem != null) {
                    ObjCatItemRel objCatItemRel = new ObjCatItemRel();
                    objCatItemRel.setObjId(mktCampaignDO.getMktCampaignId());
                    objCatItemRel.setCatalogItemId(catalogItem.getCatalogItemId());
                    objCatItemRel.setStatusCd("1000");
                    objCatItemRel.setObjType("6000");
                    objCatItemRel.setObjNbr(mktCampaignDO.getMktActivityNbr());
                    objCatItemRel.setCreateDate(new Date());
                    objCatItemRel.setStatusDate(new Date());
                    objCatItemRel.setUpdateDate(new Date());
                    objCatItemRelMapper.insert(objCatItemRel);
                }
            }
            // 记录活动操作
            mktOperatorLogService.addMktOperatorLog(mktCampaignDO.getMktCampaignName(), mktCampaignId, mktCampaignDO.getMktActivityNbr(), null, mktCampaignDO.getStatusCd(), UserUtil.loginId(), OperatorLogEnum.ADD.getOperatorValue());

            // 创建二次营销活动
            if (mktCampaignVO.getPreMktCampaignId() != null && (mktCampaignVO.getPreMktCampaignId() != 0)) {
                // 创建两个活动为接续关系
                MktCampaignRelDO mktCampaignRelDO = new MktCampaignRelDO();
                mktCampaignRelDO.setaMktCampaignId(mktCampaignVO.getPreMktCampaignId());
                mktCampaignRelDO.setzMktCampaignId(mktCampaignId);
                mktCampaignRelDO.setRelType(StatusCode.SERIAL_RELATION.getStatusCode());  // 2000 -- 接续关系
                mktCampaignRelDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode()); // 1000 -- 有效
                mktCampaignRelDO.setApplyRegionId(mktCampaignDO.getRegionId());
                mktCampaignRelDO.setStatusDate(new Date());
                mktCampaignRelDO.setExpDate(mktCampaignVO.getPlanBeginTime());
                mktCampaignRelDO.setEffDate(mktCampaignVO.getPlanEndTime());
                mktCampaignRelDO.setCreateDate(new Date());
                mktCampaignRelDO.setCreateStaff(UserUtil.loginId());
                mktCampaignRelDO.setUpdateDate(new Date());
                mktCampaignRelDO.setUpdateStaff(UserUtil.loginId());
                mktCampaignRelMapper.insert(mktCampaignRelDO);
            }


            //创建活动与城市之间的关系
            for (CityProperty cityProperty : mktCampaignVO.getApplyRegionIdList()) {
                MktCamCityRelDO mktCamCityRelDO = new MktCamCityRelDO();
                mktCamCityRelDO.setCityId(cityProperty.getCityPropertyId());
                mktCamCityRelDO.setMktCampaignId(mktCampaignId);
                mktCamCityRelDO.setCreateDate(new Date());
                mktCamCityRelDO.setCreateStaff(UserUtil.loginId());
                mktCamCityRelDO.setUpdateDate(new Date());
                mktCamCityRelDO.setUpdateStaff(UserUtil.loginId());
                mktCamCityRelMapper.insert(mktCamCityRelDO);
            }

            //创建活动与事件的关联
            for (EventDTO eventDTO : mktCampaignVO.getEventDTOS()) {
                MktCamEvtRelDO mktCamEvtRelDO = new MktCamEvtRelDO();
                mktCamEvtRelDO.setMktCampaignId(mktCampaignId);
                mktCamEvtRelDO.setEventId(eventDTO.getEventId());
                mktCamEvtRelDO.setCampaignSeq(0); // 默认等级为 0
                mktCamEvtRelDO.setLevelConfig(0); // 默认为资产级 0
                mktCamEvtRelDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                mktCamEvtRelDO.setCreateStaff(UserUtil.loginId());
                mktCamEvtRelDO.setCreateDate(new Date());
                mktCamEvtRelDO.setUpdateStaff(UserUtil.loginId());
                mktCamEvtRelDO.setUpdateDate(new Date());
                mktCamEvtRelMapper.insert(mktCamEvtRelDO);
                // redisUtils.del("CAM_IDS_EVT_REL_" + eventDTO.getEventId());
            }


            //更新推荐条目
            if (mktCampaignVO.getMktCamItemIdList() != null && !mktCampaignVO.getMktCamItemIdList().isEmpty()) {
                List<MktCamItem> mktCamItemList = mktCamItemMapper.selectByBatch(mktCampaignVO.getMktCamItemIdList());
                for (MktCamItem mktCamItem : mktCamItemList) {
                    mktCamItem.setMktCampaignId(mktCampaignId);
                    mktCamItemMapper.updateByPrimaryKey(mktCamItem);
                }
            }
//            //更新推荐条目
//            if (mktCampaignVO.getMktCamItemIdList() != null && !mktCampaignVO.getMktCamItemIdList().isEmpty()) {
//                List<MktCamItem> mktCamItemList = mktCamItemMapper.selectByBatch(mktCampaignVO.getMktCamItemIdList());
//                if (mktCampaignVO.getMktCamResource() != null && mktCampaignVO.getMktCamResource().getParentId() != null) {
//                    Map<String, Object> stringObjectMap = productService.copyItemByCampaign(mktCampaignVO.getMktCamResource().getParentId(), mktCampaignId);
//                    if (CODE_SUCCESS.equals(stringObjectMap.get("resultCode"))){
//                        mktCamItemList = (List<Long>)stringObjectMap.get("resultMsg");
//                    }
//                }
//                for (MktCamItem mktCamItem : mktCamItemList) {
//                    mktCamItem.setMktCampaignId(mktCampaignId);
//                    mktCamItemMapper.updateByPrimaryKey(mktCamItem);
//                }
//
//            }

            //需求涵id不为空添加与活动的关系
            if (mktCampaignVO.getRequestId() != null) {
                RequestInstRel requestInstRel = new RequestInstRel();
                requestInstRel.setRequestInfoId(mktCampaignVO.getRequestId());
                requestInstRel.setRequestObjId(mktCampaignId);
                requestInstRel.setStatusCd(STATUSCD_EFFECTIVE);
                requestInstRel.setStatusDate(new Date());
                requestInstRel.setUpdateDate(new Date());
                requestInstRel.setCreateStaff(UserUtil.loginId());
                requestInstRel.setCreateDate(new Date());
                requestInstRel.setRequestObjType("mkt");
                requestInstRelMapper.insert(requestInstRel);
                List<RequestInstRel> requestInstRels = requestInstRelMapper.selectByRequestId(mktCampaignVO.getRequestId(), "offer");
                for (RequestInstRel request : requestInstRels) {
                    Long offerId = request.getRequestObjId();
                    Offer offer = offerMapper.selectByPrimaryKey(Integer.valueOf(offerId.toString()));
                    if (offer == null) {
                        continue;
                    }
                    ObjMktCampaignRel objMktCam = new ObjMktCampaignRel();
                    objMktCam.setRelType("1000");
                    objMktCam.setObjType("1000");
                    objMktCam.setObjId(offerId);
                    objMktCam.setMktCampaignId(mktCampaignId);
                    objMktCam.setStatusCd(STATUSCD_EFFECTIVE);
                    objMktCam.setStatusDate(new Date());
                    objMktCam.setUpdateDate(new Date());
                    objMktCam.setCreateStaff(UserUtil.loginId());
                    objMktCam.setCreateDate(new Date());
                    objMktCampaignRelMapper.insert(objMktCam);
                }
            }
            //保存活动与过滤规则关系
            if (mktCampaignVO.getFilterRuleIdList() != null && mktCampaignVO.getFilterRuleIdList().size() > 0) {
                for (Long closeRuleId : mktCampaignVO.getFilterRuleIdList()) {
                    MktStrategyFilterRuleRelDO mktStrategyFilterRuleRelDO = new MktStrategyFilterRuleRelDO();
                    mktStrategyFilterRuleRelDO.setRuleId(closeRuleId);
                    mktStrategyFilterRuleRelDO.setStrategyId(mktCampaignId); //表结构不能变更, 逻辑变更, 策略字段放活动Id
                    mktStrategyFilterRuleRelDO.setCreateStaff(UserUtil.loginId());
                    mktStrategyFilterRuleRelDO.setCreateDate(new Date());
                    mktStrategyFilterRuleRelDO.setUpdateStaff(UserUtil.loginId());
                    mktStrategyFilterRuleRelDO.setUpdateDate(new Date());
                    mktStrategyFilterRuleRelMapper.insert(mktStrategyFilterRuleRelDO);
                }
            }

            //保存活动与关单规则关系
            if (mktCampaignVO.getCloseRuleIdList() != null && mktCampaignVO.getCloseRuleIdList().size() > 0) {
                for (Long closeRuleId : mktCampaignVO.getCloseRuleIdList()) {
                    MktStrategyCloseRuleRelDO mktStrategyCloseRuleRelDO = new MktStrategyCloseRuleRelDO();
                    mktStrategyCloseRuleRelDO.setRuleId(closeRuleId);
                    mktStrategyCloseRuleRelDO.setStrategyId(mktCampaignId); //表结构不能变更, 逻辑变更, 策略字段放活动Id
                    mktStrategyCloseRuleRelDO.setCreateStaff(UserUtil.loginId());
                    mktStrategyCloseRuleRelDO.setCreateDate(new Date());
                    mktStrategyCloseRuleRelDO.setUpdateStaff(UserUtil.loginId());
                    mktStrategyCloseRuleRelDO.setUpdateDate(new Date());
                    mktStrategyCloseRuleRelMapper.insert(mktStrategyCloseRuleRelDO);
                }
            }

            //试运算展示列实例化
            if (mktCampaignVO.getCalcDisplay() != null) {
                List<DisplayColumnLabel> displayColumnLabelList = displayColumnLabelMapper.findListByDisplayId(mktCampaignVO.getCalcDisplay());
                for (DisplayColumnLabel displayColumnLabel : displayColumnLabelList) {
                    MktCamDisplayColumnRel mktCamDisplayColumnRel = new MktCamDisplayColumnRel();
                    mktCamDisplayColumnRel.setMktCampaignId(mktCampaignId);
                    mktCamDisplayColumnRel.setInjectionLabelId(displayColumnLabel.getInjectionLabelId());
                    mktCamDisplayColumnRel.setDisplayId(mktCampaignVO.getCalcDisplay());
                    mktCamDisplayColumnRel.setDisplayColumnType("1000");
                    mktCamDisplayColumnRel.setLabelDisplayType(displayColumnLabel.getLabelDisplayType());
                    mktCamDisplayColumnRel.setRemark(displayColumnLabel.getMessageType().toString());
                    mktCamDisplayColumnRel.setStatusCd(STATUSCD_EFFECTIVE);
                    mktCamDisplayColumnRel.setStatusDate(new Date());
                    mktCamDisplayColumnRel.setCreateStaff(UserUtil.loginId());
                    mktCamDisplayColumnRel.setCreateDate(new Date());
                    mktCamDisplayColumnRel.setUpdateStaff(UserUtil.loginId());
                    mktCamDisplayColumnRel.setUpdateDate(new Date());
                    mktCamDisplayColumnRelMapper.insert(mktCamDisplayColumnRel);
                }
            }
            //创建主题关系
            ObjLabelRelCreate(mktCampaignDO);

            //活动级别创建电子券模板信息
            if (mktCampaignVO.getMktCamResource()!=null){
                MktCamResource mktCamResource = mktCampaignVO.getMktCamResource();
                if (mktCamResource.getParentId()!=null){
                    List<MktCamResource> mktCamResources = mktCamResourceMapper.selectByCampaignId(mktCamResource.getParentId(), FrameFlgEnum.YES.getValue(), null);
                    if (!mktCamResources.isEmpty()){
                        mktCamResource = mktCamResources.get(0);
                        mktCamResource.setMktCamResourceId(null);
                    }
                }
                List<Long> camItemIdList = mktCampaignVO.getMktCamItemIdList();
                List<MktCamItem> camItemList = mktCamItemMapper.selectByBatch(camItemIdList);
                ChannelUtil.addItem2CamResource(mktCamResource,camItemList);
                mktCamResource.setFrameFlg(FrameFlgEnum.YES.getValue()); // 是否电子券框架类型, yes-是，no-不是
                mktCamResource.setMktCampaignId(mktCampaignId);
                mktCamResource.setCreateStaff(UserUtil.loginId());
                mktCamResource.setCreateDate(new Date());
                mktCamResource.setUpdateStaff(UserUtil.loginId());
                mktCamResource.setUpdateDate(new Date());
                mktCamResource.setStatusCd("1000");
                mktCamResourceMapper.insert(mktCamResource);
            }
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            if (StatusCode.STATUS_CODE_DRAFT.getStatusCode().equals(mktCampaignVO.getStatusCd())) {
                maps.put("resultMsg", ErrorCode.SAVE_MKT_CAMPAIGN_SUCCESS.getErrorMsg());
            } else if (StatusCode.STATUS_CODE_CHECKING.getStatusCode().equals(mktCampaignVO.getStatusCd())) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_CHECKING_SUCCESS.getErrorMsg());
            }

            maps.put("mktCampaignId", mktCampaignId);
        } catch (Exception e) {
            logger.error("Expersion = ", e);
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            if (StatusCode.STATUS_CODE_DRAFT.getStatusCode().equals(mktCampaignVO.getStatusCd())) {
                maps.put("resultMsg", ErrorCode.SAVE_MKT_CAMPAIGN_FAILURE.getErrorMsg());
            } else if (StatusCode.STATUS_CODE_CHECKING.getStatusCode().equals(mktCampaignVO.getStatusCd())) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_CHECKING_FAILURE.getErrorMsg());
            }
        }
        return maps;
    }

    //创建活动主题关系
    private void ObjLabelRelCreate(MktCampaignDO mktCampaignDO) {
        try {
            if (StringUtils.isNotBlank(mktCampaignDO.getTheMe())) {
                List<TopicLabel> list = topicLabelMapper.selectByCampaignType();
                if (!list.isEmpty()) {
                    TopicLabel label = list.get(0);
                    if (label != null) {
                        objectLabelRelMapper.deleteByObjId(mktCampaignDO.getMktCampaignId());
                        TopicLabelValue labelValue = labelValueMapper.selectByValue(mktCampaignDO.getTheMe());
                        ObjectLabelRel objectLabelRel = new ObjectLabelRel();
                        objectLabelRel.setCreateDate(new Date());
                        objectLabelRel.setCreateStaff(mktCampaignDO.getCreateStaff());
                        objectLabelRel.setObjId(mktCampaignDO.getMktCampaignId());
                        objectLabelRel.setLabelId(label.getLabelId());
                        objectLabelRel.setLabelValue(labelValue.getLabelValue());
                        objectLabelRel.setLabelValueId(labelValue.getLabelValueId());
                        objectLabelRel.setObjType("1900");
                        objectLabelRel.setStatusCd("1000");
                        objectLabelRel.setUpdateDate(new Date());
                        objectLabelRelMapper.insert(objectLabelRel);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("活动主题关系创建失败");
        }
    }

    /**
     * 修改活动基本信息 并重新建立关系
     *
     * @param mktCampaignVO
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> modMktCampaign(MktCampaignDetailVO mktCampaignVO) throws Exception {
        Map<String, Object> maps = new HashMap<>();
        try {
            MktCampaignDO mktCampaignDO = new MktCampaignDO();
            CopyPropertiesUtil.copyBean2Bean(mktCampaignDO, mktCampaignVO);
            mktCampaignDO.setMktCampaignNameEdit(mktCampaignDO.getMktCampaignName());
            /* 更新活动名称*/
            //获取活动目录二级
            CatalogItem catalog;
            String catalogName = "";

            if (mktCampaignDO.getDirectoryId() != null) {
                catalog = catalogItemMapper.selectByPrimaryKey(mktCampaignDO.getDirectoryId());
                if (catalog != null) {
                    catalogName = catalog.getCatalogItemName();
                }
            }
//          获取创建时间
            logger.info("活动名称测试 : " + mktCampaignDO.getMktCampaignId().toString());
            MktCampaignDO mktCampaignDO1 = mktCampaignMapper.selectByPrimaryKey(mktCampaignDO.getMktCampaignId());

            String[] campaignNameArray = mktCampaignDO1.getMktCampaignName().split("_");

            logger.info("初始名称" + campaignNameArray.toString());
            //会走老活动
            if (campaignNameArray.length < 4) {
                logger.info("名称走老活动" + mktCampaignDO1.getMktCampaignName());
//             保存活动活动名称默认拼上地市信息
                String c3Name = "";
                String c4Name = "";
                if (mktCampaignDO1.getLanIdFour() != null) {
                    SysArea sysArea = sysAreaMapper.selectByPrimaryKey(mktCampaignDO1.getLanIdFour().intValue());
                    if (sysArea != null) {
                        c4Name = sysArea.getName();
                    } else {
                        Organization organization = organizationMapper.selectByPrimaryKey(mktCampaignDO1.getLanIdFour());
                        if (organization != null) {
                            c4Name = organization.getOrgName();
                        }
                    }
                }
                if (mktCampaignDO1.getLanId() != null) {
                    SysArea sysArea = sysAreaMapper.selectByPrimaryKey(mktCampaignDO1.getLanId().intValue());
                    if (sysArea != null) {
                        c3Name = sysArea.getName();
                    }
                }
                logger.info("活动目录二级" + catalogName);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String datestr = simpleDateFormat.format(mktCampaignDO1.getCreateDate()).replace("-", "");
                //是否框架活动
                Boolean isFrame = false;
                List<Long> sonIdList = mktCampaignRelMapper.selectZid();
                for (Long sonId : sonIdList) {
                    if (mktCampaignDO1.getInitId().longValue() == sonId.longValue()) {
                        isFrame = true;
                    }
                }
                logger.info("是否框架活动：" + isFrame);
                String campaignName;
                String regionName = "";
                logger.info("EditName" + mktCampaignDO.getMktCampaignNameEdit());
                //如果是框架活动
                String sourceName = mktCampaignDO1.getMktCampaignName();
                if (isFrame) {
                    logger.info("流程3`");
                    logger.info("用老名字，是否框架活动：" + isFrame);

                    if (sourceName.contains("市")) {
                        int index = sourceName.lastIndexOf("市");
                        regionName = sourceName.substring(index - 2, index + 1);
                        campaignName = sourceName.substring(0, index - 3);

                    } else {
                        logger.info("流程4`");
                        campaignName = sourceName;
                    }
                } else {
                    logger.info("流程5`");
                    campaignName = sourceName;
                }

                if (mktCampaignDO.getMktCampaignNameEdit() != null && !mktCampaignDO.getMktCampaignNameEdit().equals("")) {
                    logger.info("流程6`");
                    campaignName = mktCampaignDO.getMktCampaignNameEdit();
                }
                String campaignNameLast = campaignName.replace("_", "-");
               /* if(mktCampaignDO.getMktCampaignNameEdit() != "" || mktCampaignDO.getMktCampaignNameEdit() != null){
                    //如果用户输入新名字
                    logger.info("流程1`");
                   campaignName = mktCampaignDO.getMktCampaignNameEdit();
                    if(isFrame){
                        logger.info("流程3`");
                        logger.info("用老名字，是否框架活动：" + isFrame);
                        String sourceName = mktCampaignDO1.getMktCampaignName();
                        if (sourceName.contains("市")) {
                            int index = sourceName.lastIndexOf("市");
                            regionName = sourceName.substring(index - 2, index + 1);
                        }
                    }
                }else{
                    logger.info("流程2`");
                    //否则用老名字
                    if(isFrame){
                        logger.info("流程3`");
                        logger.info("用老名字，是否框架活动：" + isFrame);
                        String sourceName = mktCampaignDO1.getMktCampaignName();
                        if(sourceName.contains("市")){
                            int index = sourceName.lastIndexOf("市");
                            regionName = sourceName.substring(index-2, index + 1);
                            campaignName = sourceName.substring(0,index-3);

                        }else {
                            logger.info("流程4`");
                            campaignName = sourceName;
                        }
                    }else {
                        logger.info("流程5`");
                        campaignName = mktCampaignDO1.getMktCampaignName();
                    }
                }*/
                String mktActivityNbr = mktCampaignDO1.getMktActivityNbr();

                if ("C1".equals(mktCampaignDO1.getRegionFlg()) || "C2".equals(mktCampaignDO1.getRegionFlg())) {
                    mktCampaignDO.setMktCampaignName("【省】" + "_" + catalogName + "_" + campaignNameLast + "_" + datestr);
                } else if ("C3".equals(mktCampaignDO1.getRegionFlg())) {
                    mktCampaignDO.setMktCampaignName("【市】" + c3Name + "_" + catalogName + "_" + campaignNameLast + "_" + datestr);
                } else if ("C4".equals(mktCampaignDO1.getRegionFlg())) {
                    mktCampaignDO.setMktCampaignName("【区】" + c3Name + c4Name + "_" + catalogName + "_" + campaignNameLast + "_" + datestr);
                }

                if (isFrame && regionName != "") {
                    mktCampaignDO.setMktCampaignName(mktCampaignDO.getMktCampaignName() + "_" + regionName);
                    logger.info("框架活动，拼接框架活动子活动地市信息: " + regionName);
                }
                logger.info("拼接后活动名称" + mktCampaignDO.getMktCampaignName());
            } else {
                logger.info("名称走新活动，原先名称" + mktCampaignDO1.getMktCampaignName());
                campaignNameArray[1] = catalogName;
                campaignNameArray[2] = mktCampaignDO.getMktCampaignNameEdit().replace("_", "-");
                String newCampaignName = StringUtils.join(campaignNameArray, "_");
                logger.info("名称走新活动，修改后名称" + newCampaignName);
                mktCampaignDO.setMktCampaignName(newCampaignName);
            }

            logger.info("活动目录二级" + catalogName);
            logger.info("活动名称测试2 " + mktCampaignDO.getMktCampaignName());
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            String datestr = simpleDateFormat.format(mktCampaignDO1.getCreateDate()).replace("-","");
//
//            String mktActivityNbr = mktCampaignDO1.getMktActivityNbr();
//            mktCampaignDO.setRegionFlg(mktCampaignDO1.getRegionFlg());
//            logger.info("活动名称测试,regionFLG" + mktCampaignDO1.getRegionFlg());
//            if ("C1".equals(mktCampaignDO.getRegionFlg()) || "C2".equals(mktCampaignDO.getRegionFlg())){
//                mktCampaignDO.setMktCampaignName("【省】" +"-" +  catalogName +"-" +  mktCampaignDO.getMktCampaignNameEdit() +"-" +  mktActivityNbr + "-" + datestr);
//            }else if ("C3".equals(mktCampaignDO.getRegionFlg())){
//                mktCampaignDO.setMktCampaignName("【市】" +"-" +  catalogName +"-" + mktCampaignDO.getMktCampaignNameEdit()  +"-" +  mktActivityNbr + "-" + datestr);
//            }else if ("C4".equals(mktCampaignDO.getRegionFlg())){
//                mktCampaignDO.setMktCampaignName("【县】" +"-" +  catalogName +"-" + mktCampaignDO.getMktCampaignNameEdit()  +"-" +  mktActivityNbr + "-" + datestr);
//            }

            // 更新活动基本信息
            mktCampaignDO.setUpdateStaff(UserUtil.loginId());
//            if (mktCampaignDO.getUpdateStaff() == 1) {
//                maps.put("resultCode", CommonConstant.CODE_FAIL);
//                maps.put("resultMsg", "更新人信息为空，请核实工号已选中的岗位权限");
//                logger.info("更新人信息为空，请核实工号已选中的岗位权限" + JSON.toJSONString(mktCampaignDO));
//                return maps;
//            }
            mktCampaignDO.setUpdateDate(new Date());
            mktCampaignMapper.updateByPrimaryKey(mktCampaignDO);
            //创建主题关系
            ObjLabelRelCreate(mktCampaignDO);
            if (mktCampaignDO.getDirectoryId() != null) {
                CatalogItem catalogItem = catalogItemMapper.selectByPrimaryKey(mktCampaignDO.getDirectoryId());
                if (catalogItem != null) {
                    objCatItemRelMapper.deleteByCampaignId(mktCampaignDO.getMktCampaignId());
                    ObjCatItemRel objCatItemRel = new ObjCatItemRel();
                    objCatItemRel.setObjId(mktCampaignDO.getMktCampaignId());
                    objCatItemRel.setCatalogItemId(catalogItem.getCatalogItemId());
                    objCatItemRel.setStatusCd("1000");
                    objCatItemRel.setObjType("6000");
                    objCatItemRel.setObjNbr(mktCampaignDO.getMktActivityNbr());
                    objCatItemRel.setCreateDate(new Date());
                    objCatItemRel.setStatusDate(new Date());
                    objCatItemRel.setUpdateDate(new Date());
                    objCatItemRelMapper.insert(objCatItemRel);
                }
            }
            Long mktCampaignId = mktCampaignDO.getMktCampaignId();
            MktCampaignDO campaign = mktCampaignMapper.selectByPrimaryKey(mktCampaignId);
            // 记录活动操作
            mktOperatorLogService.addMktOperatorLog(mktCampaignDO.getMktCampaignName(), mktCampaignId, mktCampaignDO.getMktActivityNbr(), campaign.getStatusCd(), mktCampaignDO.getStatusCd(), UserUtil.loginId(), OperatorLogEnum.UPDATE.getOperatorValue());
            // redisUtils.del("MKT_CAMPAIGN_" + mktCampaignId);
            //删除原来的活动与城市之间的关系
            mktCamCityRelMapper.deleteByMktCampaignId(mktCampaignId);
            //创建活动与城市之间的关系
            for (CityProperty cityProperty : mktCampaignVO.getApplyRegionIdList()) {
                MktCamCityRelDO mktCamCityRelDO = new MktCamCityRelDO();
                mktCamCityRelDO.setCityId(cityProperty.getCityPropertyId());
                mktCamCityRelDO.setMktCampaignId(mktCampaignId);
                mktCamCityRelDO.setCreateDate(new Date());
                mktCamCityRelDO.setCreateStaff(UserUtil.loginId());
                mktCamCityRelDO.setUpdateDate(new Date());
                mktCamCityRelDO.setUpdateStaff(UserUtil.loginId());
                mktCamCityRelMapper.insert(mktCamCityRelDO);
            }

            // 遍历所有策略集合
            for (MktStrategyConfDetail mktStrategyConfDetail : mktCampaignVO.getMktStrategyConfDetailList()) {
                mktStrategyConfDetail.setMktCampaignId(mktCampaignVO.getMktCampaignId());
                mktStrategyConfDetail.setMktCampaignName(mktCampaignVO.getMktCampaignName());
                mktStrategyConfDetail.setMktCampaignType(mktCampaignVO.getMktCampaignType());
                if (mktStrategyConfDetail.getMktStrategyConfId() != null) {
                    mktStrategyConfService.updateMktStrategyConf(mktStrategyConfDetail);
                } else {
                    mktStrategyConfService.saveMktStrategyConf(mktStrategyConfDetail);
                    //redisUtils.del("MKT_CAM_STRATEGY_" + mktCampaignId);
                }
            }
            //更新推荐条目
            if (mktCampaignVO.getMktCamItemIdList() != null && !mktCampaignVO.getMktCamItemIdList().isEmpty()) {
                List<MktCamItem> mktCamItemList = mktCamItemMapper.selectByBatch(mktCampaignVO.getMktCamItemIdList());
                for (MktCamItem mktCamItem : mktCamItemList) {
                    mktCamItem.setMktCampaignId(mktCampaignId);
                    mktCamItemMapper.updateByPrimaryKey(mktCamItem);
                }
            }

            List<MktCamEvtRelDO> mktCamEvtRelDOList = mktCamEvtRelMapper.selectByMktCampaignId(mktCampaignId);

            if (mktCampaignVO.getEventDTOS() != null) {
                List<EventDTO> comList = new ArrayList<>();
                List<MktCamEvtRelDO> delList = new ArrayList<>();
                for (int i = 0; i < mktCamEvtRelDOList.size(); i++) {
                    for (int j = 0; j < mktCampaignVO.getEventDTOS().size(); j++) {
                        if (mktCampaignVO.getEventDTOS().get(j).getEventId().equals(mktCamEvtRelDOList.get(i).getEventId())) {
                            comList.add(mktCampaignVO.getEventDTOS().get(j));
                            break;
                        }
                        if (j == mktCampaignVO.getEventDTOS().size() - 1) {
                            delList.add(mktCamEvtRelDOList.get(i));
                        }
                    }
                }

                // 删除去掉的关系
                if (mktCampaignVO.getEventDTOS().size() > 0) {
                    for (MktCamEvtRelDO mktCamEvtRelDO : delList) {
                        mktCamEvtRelMapper.deleteByPrimaryKey(mktCamEvtRelDO.getMktCampEvtRelId());
                        //redisUtils.del("CAM_EVT_REL_" + mktCamEvtRelDO.getEventId());
                        //redisUtils.del("CAM_IDS_EVT_REL_" + mktCamEvtRelDO.getEventId());

                    }
                } else {
                    for (MktCamEvtRelDO mktCamEvtRelDO : mktCamEvtRelDOList) {
                        mktCamEvtRelMapper.deleteByPrimaryKey(mktCamEvtRelDO.getMktCampEvtRelId());
                        //redisUtils.del("CAM_EVT_REL_" + mktCamEvtRelDO.getEventId());
                        //redisUtils.del("CAM_IDS_EVT_REL_" + mktCamEvtRelDO.getEventId());
                    }
                }
                if (mktCampaignVO.getEventDTOS() != null) {
                    mktCampaignVO.getEventDTOS().removeAll(comList);
                    //创建活动与事件的关联
                    for (EventDTO eventDTO : mktCampaignVO.getEventDTOS()) {
                        MktCamEvtRelDO mktCamEvtRelDO = new MktCamEvtRelDO();
                        mktCamEvtRelDO.setMktCampaignId(mktCampaignId);
                        mktCamEvtRelDO.setEventId(eventDTO.getEventId());
                        mktCamEvtRelDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                        mktCamEvtRelDO.setCampaignSeq(0); // 默认等级为 0
                        mktCamEvtRelDO.setLevelConfig(0); // 默认为资产级 0
                        mktCamEvtRelDO.setCreateStaff(UserUtil.loginId());
                        mktCamEvtRelDO.setCreateDate(new Date());
                        mktCamEvtRelDO.setUpdateStaff(UserUtil.loginId());
                        mktCamEvtRelDO.setUpdateDate(new Date());
                        mktCamEvtRelMapper.insert(mktCamEvtRelDO);
                    }
                }
            }

            //重建活动与过滤规则关系
            mktStrategyFilterRuleRelMapper.deleteByStrategyId(mktCampaignId);
            // 删除事件接入过滤规则缓存
            //redisUtils.del("FILTER_RULE_LIST_" + mktCampaignId);
            //redisUtils.del("FILTER_RULE_STR_" + mktCampaignId);
            if (mktCampaignVO.getFilterRuleIdList() != null && mktCampaignVO.getFilterRuleIdList().size() > 0) {
                for (Long FilterRuleId : mktCampaignVO.getFilterRuleIdList()) {
                    MktStrategyFilterRuleRelDO mktStrategyFilterRuleRelDO = new MktStrategyFilterRuleRelDO();
                    mktStrategyFilterRuleRelDO.setRuleId(FilterRuleId);
                    mktStrategyFilterRuleRelDO.setStrategyId(mktCampaignId);
                    mktStrategyFilterRuleRelDO.setCreateStaff(UserUtil.loginId());
                    mktStrategyFilterRuleRelDO.setCreateDate(new Date());
                    mktStrategyFilterRuleRelDO.setUpdateStaff(UserUtil.loginId());
                    mktStrategyFilterRuleRelDO.setUpdateDate(new Date());
                    mktStrategyFilterRuleRelMapper.insert(mktStrategyFilterRuleRelDO);
                }
            }

            //重建活动与关单规则关系
            mktStrategyCloseRuleRelMapper.deleteByStrategyId(mktCampaignId);
            if (mktCampaignVO.getCloseRuleIdList() != null && mktCampaignVO.getCloseRuleIdList().size() > 0) {
                for (Long closeRuleId : mktCampaignVO.getCloseRuleIdList()) {
                    MktStrategyCloseRuleRelDO mktStrategyCloseRuleRelDO = new MktStrategyCloseRuleRelDO();
                    mktStrategyCloseRuleRelDO.setRuleId(closeRuleId);
                    mktStrategyCloseRuleRelDO.setStrategyId(mktCampaignId);
                    mktStrategyCloseRuleRelDO.setCreateStaff(UserUtil.loginId());
                    mktStrategyCloseRuleRelDO.setCreateDate(new Date());
                    mktStrategyCloseRuleRelDO.setUpdateStaff(UserUtil.loginId());
                    mktStrategyCloseRuleRelDO.setUpdateDate(new Date());
                    mktStrategyCloseRuleRelMapper.insert(mktStrategyCloseRuleRelDO);
                }
            }

            //重建展示列实例
            mktCamDisplayColumnRelMapper.deleteByMktCampaignId(mktCampaignId);
            if (mktCampaignVO.getCalcDisplay() != null) {
                List<DisplayColumnLabel> displayColumnLabelList = displayColumnLabelMapper.findListByDisplayId(mktCampaignVO.getCalcDisplay());
                for (DisplayColumnLabel displayColumnLabel : displayColumnLabelList) {
                    MktCamDisplayColumnRel mktCamDisplayColumnRel = new MktCamDisplayColumnRel();
                    mktCamDisplayColumnRel.setMktCampaignId(mktCampaignId);
                    mktCamDisplayColumnRel.setInjectionLabelId(displayColumnLabel.getInjectionLabelId());
                    mktCamDisplayColumnRel.setDisplayId(mktCampaignVO.getCalcDisplay());
                    mktCamDisplayColumnRel.setDisplayColumnType("1000");
                    mktCamDisplayColumnRel.setLabelDisplayType(displayColumnLabel.getLabelDisplayType());
                    mktCamDisplayColumnRel.setRemark(displayColumnLabel.getMessageType().toString());
                    mktCamDisplayColumnRel.setStatusCd(STATUSCD_EFFECTIVE);
                    mktCamDisplayColumnRel.setStatusDate(new Date());
                    mktCamDisplayColumnRel.setCreateStaff(UserUtil.loginId());
                    mktCamDisplayColumnRel.setCreateDate(new Date());
                    mktCamDisplayColumnRel.setUpdateStaff(UserUtil.loginId());
                    mktCamDisplayColumnRel.setUpdateDate(new Date());
                    mktCamDisplayColumnRelMapper.insert(mktCamDisplayColumnRel);
                }
            }

            //集团活动环节信息更新反馈
            MktCampaignComplete mktCampaignComplete = mktCampaignCompleteMapper.selectByCampaignIdAndTacheCd(campaign.getInitId(), "1100");
            if (mktCampaignComplete != null && !mktCampaignComplete.getTacheValueCd().equals("11")) {
                try {
                    openCompleteMktCampaignService.completeMktCampaign(campaign.getInitId(), "1200", "10");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //修改电子券的信息
            MktCamResource mktCamResource = mktCampaignVO.getMktCamResource();
            if (mktCamResource != null) {
                List<Long> camItemIdList = mktCampaignVO.getMktCamItemIdList();
                List<MktCamItem> camItemList = mktCamItemMapper.selectByBatch(camItemIdList);
                ChannelUtil.addItem2CamResource(mktCamResource,camItemList);
                mktCamResource.setUpdateStaff(UserUtil.loginId());
                mktCamResource.setUpdateDate(new Date());
                mktCamResourceMapper.updateByPrimaryKey(mktCamResource);
            }

            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            if (StatusCode.STATUS_CODE_DRAFT.getStatusCode().equals(mktCampaignVO.getStatusCd())) {
                maps.put("resultMsg", ErrorCode.UPDATE_MKT_CAMPAIGN_SUCCESS.getErrorMsg());
            } else if (StatusCode.STATUS_CODE_CHECKING.getStatusCode().equals(mktCampaignVO.getStatusCd())) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_CHECKING_SUCCESS.getErrorMsg());
            }
            maps.put("mktCampaignId", mktCampaignId);
        } catch (Exception e) {
            logger.error("Expersion = ", e);
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            if (StatusCode.STATUS_CODE_DRAFT.getStatusCode().equals(mktCampaignVO.getStatusCd())) {
                maps.put("resultMsg", ErrorCode.UPDATE_MKT_CAMPAIGN_FAILURE.getErrorMsg());
            } else if (StatusCode.STATUS_CODE_CHECKING.getStatusCode().equals(mktCampaignVO.getStatusCd())) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_CHECKING_FAILURE.getErrorMsg());
            }
        }
        return maps;
    }


    /**
     * 获取活动基本信息
     *
     * @param mktCampaignId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getMktCampaign(Long mktCampaignId) throws Exception {
        // 获取关系
        List<MktCampaignRelDO> mktCampaignRelDOList = mktCampaignRelMapper.selectByAmktCampaignId(mktCampaignId, StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
        List<CityProperty> applyRegionIds = new ArrayList<>();
        // 获取活动基本信息
        MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCampaignId);

        MktCampaignDetailVO mktCampaignVO = new MktCampaignDetailVO();
        CopyPropertiesUtil.copyBean2Bean(mktCampaignVO, mktCampaignDO);
        // 获取下发城市集合
        List<MktCamCityRelDO> mktCamCityRelDOList = mktCamCityRelMapper.selectByMktCampaignId(mktCampaignVO.getMktCampaignId());
        for (MktCamCityRelDO mktCamCityRelDO : mktCamCityRelDOList) {
            SysArea sysArea = sysAreaMapper.selectByPrimaryKey(mktCamCityRelDO.getCityId().intValue());
            CityProperty cityProperty = new CityProperty();
            cityProperty.setCityPropertyId(sysArea.getAreaId().longValue());
            cityProperty.setCityPropertyName(sysArea.getName());
            applyRegionIds.add(cityProperty);
        }
        mktCampaignVO.setApplyRegionIdList(applyRegionIds);

        // c4,c5
        if ((AreaCodeEnum.sysAreaCode.FENGJU.getSysPostCode().equals(mktCampaignDO.getCreateChannel()) || AreaCodeEnum.sysAreaCode.ZHIJU.getSysPostCode().equals(mktCampaignDO.getCreateChannel())) && mktCampaignDO.getLanIdFour() != null) {
            SysArea sysArea = sysAreaMapper.selectByPrimaryKey(mktCampaignDO.getLanIdFour().intValue());
            //    Organization organization = organizationMapper.selectByPrimaryKey(mktCampaignDO.getLanIdFour());
            if (sysArea != null) {
                mktCampaignVO.setLanIdFourName(sysArea.getName());
            }
        }
        if (AreaCodeEnum.sysAreaCode.ZHIJU.getSysPostCode().equals(mktCampaignDO.getCreateChannel()) && mktCampaignDO.getLanIdFive() != null) {
            Organization organization = organizationMapper.selectByPrimaryKey(mktCampaignDO.getLanIdFive());
            if (organization != null) {
                mktCampaignVO.setLanIdFiveName(organization.getOrgName());
            }
        }

        //查询父活动信息
        List<MktCampaignRelDO> mktCampaignRelDOS = mktCampaignRelMapper.selectByZmktCampaignId(mktCampaignId, "1000");
        if (mktCampaignRelDOS != null && mktCampaignRelDOS.size() > 0) {
            MktCampaignDO mktCampaignDOPre = mktCampaignMapper.selectByPrimaryKey(Long.valueOf(mktCampaignRelDOS.get(0).getaMktCampaignId()));
            mktCampaignVO.setPreMktCampaignId(mktCampaignDOPre.getMktCampaignId());
            String msgByCode = StatusCode.getMsgByCode(mktCampaignDOPre.getMktCampaignCategory());
            mktCampaignVO.setPreMktCampaignType(msgByCode == null ? "" : msgByCode);
        }
        CatalogItem catalogItem = catalogItemMapper.selectByPrimaryKey(mktCampaignDO.getDirectoryId());
        if (catalogItem != null) {
            mktCampaignVO.setDirectoryName(catalogItem.getCatalogItemName());
        } else {
            MktCamDirectoryDO mktCamDirectoryDO = mktCamDirectoryMapper.selectByPrimaryKey(mktCampaignDO.getDirectoryId());
            if (mktCamDirectoryDO != null) {
                mktCampaignVO.setDirectoryName(mktCamDirectoryDO.getMktCamDirectoryName());
            }
        }
        // 获取所有的sysParam
        Map<String, String> paramMap = new HashMap<>();
        List<SysParams> sysParamList = sysParamsMapper.selectAll("", "");
        for (SysParams sysParams : sysParamList) {
            paramMap.put(sysParams.getParamKey() + sysParams.getParamValue(), sysParams.getParamName());
        }
        mktCampaignVO.setTiggerTypeValue(paramMap.
                get(ParamKeyEnum.TIGGER_TYPE.getParamKey() + mktCampaignDO.getTiggerType()));
        mktCampaignVO.setMktCampaignCategoryValue(paramMap.
                get(ParamKeyEnum.MKT_CAMPAIGN_CATEGORY.getParamKey() + mktCampaignDO.getMktCampaignCategory()));
        mktCampaignVO.setMktCampaignTypeValue(paramMap.
                get(ParamKeyEnum.MKT_CAMPAIGN_TYPE.getParamKey() + mktCampaignDO.getMktCampaignType()));
        mktCampaignVO.setStatusCdValue(paramMap.
                get(ParamKeyEnum.STATUS_CD.getParamKey() + mktCampaignDO.getStatusCd()));
        mktCampaignVO.setExecTypeValue(paramMap.
                get(ParamKeyEnum.EXEC_TYPE.getParamKey() + mktCampaignDO.getExecType()));


        // 获取活动关联的事件
        List<MktCamEvtRelDO> mktCamEvtRelDOList = mktCamEvtRelMapper.selectByMktCampaignId(mktCampaignDO.getMktCampaignId());
        if (mktCamEvtRelDOList != null) {
            List<EventDTO> eventDTOList = new ArrayList<>();
            for (MktCamEvtRelDO mktCamEvtRelDO : mktCamEvtRelDOList) {
                Long eventId = mktCamEvtRelDO.getEventId();
                ContactEvt contactEvt = contactEvtMapper.getEventById(eventId);
                if (contactEvt != null) {
                    EventDTO eventDTO = new EventDTO();
                    eventDTO.setEventId(eventId);
                    eventDTO.setEventName(contactEvt.getContactEvtName());
                    eventDTOList.add(eventDTO);
                }
            }
            mktCampaignVO.setEventDTOS(eventDTOList);
        }

        // 获取过滤规则集合
        List<Long> filterRuleIdList = mktStrategyFilterRuleRelMapper.selectByStrategyId(mktCampaignId);
        mktCampaignVO.setFilterRuleIdList(filterRuleIdList);

        // 获取关单规则集合
        List<Long> closeRuleIdList = mktStrategyCloseRuleRelMapper.selectByStrategyId(mktCampaignId);
        mktCampaignVO.setCloseRuleIdList(closeRuleIdList);

        // 获取活动关联策略集合
        List<MktStrategyConfDetail> mktStrategyConfDetailList = new ArrayList<>();
        List<MktCamStrategyConfRelDO> mktCamStrategyConfRelDOList = mktCamStrategyConfRelMapper.selectByMktCampaignId(mktCampaignId);
        for (MktCamStrategyConfRelDO mktCamStrategyConfRelDO : mktCamStrategyConfRelDOList) {
            MktStrategyConfDO mktStrategyConfDO = mktStrategyConfMapper.selectByPrimaryKey(mktCamStrategyConfRelDO.getStrategyConfId());
            MktStrategyConf mktStrategyConf = new MktStrategyConf();

            Map<String, Object> mktStrategyConfMap = mktStrategyConfService.getMktStrategyConf(mktCamStrategyConfRelDO.getStrategyConfId());
            MktStrategyConfDetail mktStrategyConfDetail = (MktStrategyConfDetail) mktStrategyConfMap.get("mktStrategyConfDetail");
            mktStrategyConfDetailList.add(mktStrategyConfDetail);
        }
        mktCampaignVO.setMktStrategyConfDetailList(mktStrategyConfDetailList);
        List<MktCamResource> mktCamResourceList = mktCamResourceMapper.selectByCampaignId(mktCampaignId, FrameFlgEnum.YES.getValue(), null);
        if (mktCamResourceList != null && mktCamResourceList.size() > 0) {
            mktCampaignVO.setMktCamResource(mktCamResourceList.get(0));
        }
        Map<String, Object> maps = new HashMap<>();
        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        maps.put("resultMsg", "查询活动成功");
        maps.put("mktCampaignVO", mktCampaignVO);
        return maps;
    }


    /**
     * 根据活动Id查询所有的策略和规则名称集合
     *
     * @param mktCampaignId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getAllConfRuleName(Long mktCampaignId) throws Exception {
        Map<String, Object> maps = null;
        try {
            maps = new HashMap<>();
            List<MktStrategyConfDO> mktStrategyConfDOS = mktStrategyConfMapper.selectByCampaignId(mktCampaignId);
            List<Map<String, Object>> strConfList = new ArrayList<>();
            for (MktStrategyConfDO mktStrategyConfDO : mktStrategyConfDOS) {
                List<MktStrategyConfRuleDO> mktStrategyConfRuleDOList = mktStrategyConfRuleMapper.selectByMktStrategyConfId(mktStrategyConfDO.getMktStrategyConfId());
                Map<String, Object> strMap = new HashMap<>();
                List<Map<String, Object>> ruleMapList = new ArrayList<>();
                for (MktStrategyConfRuleDO mktStrategyConfRuleDO : mktStrategyConfRuleDOList) {
                    Map<String, Object> ruleMap = new HashMap<>();
                    ruleMap.put("ruleId", mktStrategyConfRuleDO.getMktStrategyConfRuleId());
                    ruleMap.put("ruleName", mktStrategyConfRuleDO.getMktStrategyConfRuleName());
                    ruleMapList.add(ruleMap);
                }
                List<Long> checkboxGroup = new ArrayList<>();
                strMap.put("strConfId", mktStrategyConfDO.getMktStrategyConfId());
                strMap.put("strCofName", mktStrategyConfDO.getMktStrategyConfName());
                strMap.put("ruleMapList", ruleMapList);
                strMap.put("checkboxGroup", checkboxGroup);// 该字段为空集合，前端需要
                strConfList.add(strMap);
            }

            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("resultMsg", "查询成功！");
            maps.put("strConfList", strConfList);
        } catch (Exception e) {
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("strConfList", "查询失败！");
        }
        return maps;
    }

    /**
     * 删除活动基本信息 并删除建立关系
     *
     * @param mktCampaignId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> delMktCampaign(Long mktCampaignId) throws Exception {
        Map<String, Object> maps = new HashMap<>();
        // 删除活动基本信息
        try {

            MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCampaignId);

            // 查询initId为mktCampaignId且状态为调整中
            MktCampaignDO mktCampaignDOAdjust = mktCampaignMapper.selectPrimaryKeyByInitId(mktCampaignDO.getInitId(), StatusCode.STATUS_CODE_ADJUST.getStatusCode());
            // 回滚父活动
            if (mktCampaignDOAdjust != null) {
                changeMktCampaignStatus(mktCampaignDOAdjust.getMktCampaignId(), STATUS_CODE_PUBLISHED.getStatusCode());
            }

            // 记录活动操作
            mktOperatorLogService.addMktOperatorLog(mktCampaignDO.getMktCampaignName(), mktCampaignId, mktCampaignDO.getMktActivityNbr(), mktCampaignDO.getStatusCd(), OperatorLogEnum.DELETE.getOperatorValue(), UserUtil.loginId(), OperatorLogEnum.DELETE.getOperatorValue());

            mktCampaignMapper.deleteByPrimaryKey(mktCampaignId);
            List<MktCamStrategyConfRelDO> mktCamStrategyConfRelDOList = mktCamStrategyConfRelMapper.selectByMktCampaignId(mktCampaignId);
            for (MktCamStrategyConfRelDO mktCamStrategyConfRelDO : mktCamStrategyConfRelDOList) {
                mktStrategyConfService.deleteMktStrategyConf(mktCamStrategyConfRelDO.getStrategyConfId());
            }

            // 删除活动策略关系
            mktCamStrategyConfRelMapper.deleteByMktCampaignId(mktCampaignId);

            // 删除事件接入缓存
            List<MktCamEvtRelDO> mktCamEvtRelDOS = mktCamEvtRelMapper.selectByMktCampaignId(mktCampaignId);
            for (MktCamEvtRelDO mktCamEvtRelDO : mktCamEvtRelDOS) {
                //redisUtils.del("CAM_EVT_REL_" + mktCamEvtRelDO.getEventId());
                //redisUtils.del("CAM_IDS_EVT_REL_" + mktCamEvtRelDO.getEventId());
            }
            // 删除活动与事件的关系
            mktCamEvtRelMapper.deleteByMktCampaignId(mktCampaignId);

            // 删除活动与规则集合
            mktStrategyFilterRuleRelMapper.deleteByStrategyId(mktCampaignId);
            // 删除活动与关单规则集合
            mktStrategyCloseRuleRelMapper.deleteByStrategyId(mktCampaignId);
            // 删除活动与试运算展示列关系
            mktCamDisplayColumnRelMapper.deleteByMktCampaignId(mktCampaignId);

            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("resultMsg", "删除成功！");
            maps.put("mktCampaignId", mktCampaignId);
        } catch (Exception e) {
            logger.error("[op:delMktCampaign] 删除活动mktCampaignId = {}失败！Exception = ", mktCampaignId, e);
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            maps.put("resultMsg", "删除失败！");
            maps.put("mktCampaignId", mktCampaignId);
        }
        return maps;
    }

    @Override
    public Map<String, Object> getCampaignList4EventScene(String mktCampaignName) {
        Map<String, Object> maps = new HashMap<>();
        try {
            MktCampaignDO MktCampaignPar = new MktCampaignDO();
            MktCampaignPar.setMktCampaignName(mktCampaignName);
            List<MktCampaignCountDO> mktCampaignDOList = mktCampaignMapper.qryMktCampaignListPage(MktCampaignPar);
            List<CampaignVO> voList = new ArrayList<>();
            for (MktCampaignDO campaignDO : mktCampaignDOList) {
                CampaignVO vo = ChannelUtil.map2CampaignVO(campaignDO);
                voList.add(vo);
            }
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("resultMsg", voList);
        } catch (Exception e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            maps.put("resultCode", "查询失败！");
        }
        return maps;
    }

    @Override
    public Map<String, Object> getCampaignList(String mktCampaignName, String mktCampaignType, Long eventId) {
        Map<String, Object> maps = new HashMap<>();
        try {
            MktCampaignDO MktCampaignPar = new MktCampaignDO();
            MktCampaignPar.setMktCampaignName(mktCampaignName);
            MktCampaignPar.setMktCampaignType(mktCampaignType);
            MktCampaignPar.setStatusCd("(2002, 2008)");
            List<Long> relationCamList = new ArrayList<>();
            if (eventId != null) {
                List<MktCamEvtRel> camEvtRelList = mktCamEvtRelMapper.qryBycontactEvtId(eventId);
                for (MktCamEvtRel rel : camEvtRelList) {
                    relationCamList.add(rel.getMktCampaignId());
                }
            }
            PageHelper.startPage(1, 50);
            List<MktCampaignCountDO> mktCampaignDOList = mktCampaignMapper.qryMktCampaignListPage(MktCampaignPar);
            Page pageInfo = new Page(new PageInfo(mktCampaignDOList));
            List<CampaignVO> voList = new ArrayList<>();
            for (MktCampaignDO campaignDO : mktCampaignDOList) {
                if (relationCamList.contains(campaignDO.getMktCampaignId())) {
                    continue;
                }
                CampaignVO vo = ChannelUtil.map2CampaignVO(campaignDO);
                voList.add(vo);
            }
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("resultMsg", voList);
        } catch (Exception e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
        }
        return maps;
    }

    /**
     * 活动同步列表
     *
     * @param params
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Map<String, Object> qryMktCampaignList4Sync(Map<String, Object> params, Integer page, Integer pageSize) {
        Map<String, Object> maps = new HashMap<>();
        try {
            PageHelper.startPage(page, pageSize);

            List<MktCampaignCountDO> mktCampaignDOList = mktCampaignMapper.qryMktCampaignListPage4Sync(params);

            // 获取所有的sysParam
            Map<String, String> paramMap = new HashMap<>();
            List<SysParams> sysParamList = sysParamsMapper.selectAll("", "");
            for (SysParams sysParams : sysParamList) {
                paramMap.put(sysParams.getParamKey() + sysParams.getParamValue(), sysParams.getParamName());
            }

            List<MktCampaignDetailVO> mktCampaignVOList = new ArrayList<>();
            for (MktCampaignCountDO mktCampaignCountDO : mktCampaignDOList) {
                MktCampaignDetailVO mktCampaignVO = new MktCampaignDetailVO();
                try {
                    mktCampaignVO.setMktCampaignId(mktCampaignCountDO.getMktCampaignId());
                    mktCampaignVO.setMktCampaignName(mktCampaignCountDO.getMktCampaignName());
                    mktCampaignVO.setMktActivityNbr(mktCampaignCountDO.getMktActivityNbr());
                    mktCampaignVO.setPlanEndTime(mktCampaignCountDO.getPlanEndTime());
                    mktCampaignVO.setPlanBeginTime(mktCampaignCountDO.getPlanBeginTime());
                    mktCampaignVO.setCreateChannel(mktCampaignCountDO.getCreateChannel());
                    mktCampaignVO.setCreateDate(mktCampaignCountDO.getCreateDate());
                    mktCampaignVO.setUpdateDate(mktCampaignCountDO.getUpdateDate());
                    if (mktCampaignCountDO.getStatusCd().equals(STATUS_CODE_PUBLISHED.getStatusCode()) || mktCampaignCountDO.getStatusCd().equals(StatusCode.STATUS_CODE_PASS.getStatusCode()) || mktCampaignCountDO.getStatusCd().equals(StatusCode.STATUS_CODE_ROLL.getStatusCode()) || mktCampaignCountDO.getStatusCd().equals(StatusCode.STATUS_CODE_ADJUST.getStatusCode()) || mktCampaignCountDO.getStatusCd().equals(StatusCode.STATUS_CODE_STOP.getStatusCode()) || mktCampaignCountDO.getStatusCd().equals(StatusCode.STATUS_CODE_PRE_PAUSE.getStatusCode())) {
                        mktCampaignVO.setStatusExamine(StatusCode.STATUS_CODE_PASS.getStatusMsg());
                    } else if (mktCampaignCountDO.getStatusCd().equals(StatusCode.STATUS_CODE_UNPASS.getStatusCode())) {
                        mktCampaignVO.setStatusExamine(StatusCode.STATUS_CODE_UNPASS.getStatusMsg());
                    } else {
                        mktCampaignVO.setStatusExamine(StatusCode.STATUS_CODE_CHECKING.getStatusMsg());
                    }
                } catch (Exception e) {
                    logger.error("Excetion:", e);
                }
                mktCampaignVO.setMktCampaignCategoryValue(paramMap.
                        get(ParamKeyEnum.MKT_CAMPAIGN_CATEGORY.getParamKey() + mktCampaignCountDO.getMktCampaignCategory()));
                mktCampaignVO.setMktCampaignTypeValue(paramMap.
                        get(ParamKeyEnum.MKT_CAMPAIGN_TYPE.getParamKey() + mktCampaignCountDO.getMktCampaignType()));
                mktCampaignVO.setStatusCdValue(paramMap.
                        get(ParamKeyEnum.STATUS_CD.getParamKey() + mktCampaignCountDO.getStatusCd()));
                Boolean isRelation = false;
                //判断该活动是否有有效的父/子活动
                if (mktCampaignCountDO.getRelCount() != 0) {
                    isRelation = true;
                }
                mktCampaignVO.setRelation(isRelation);
                mktCampaignVOList.add(mktCampaignVO);
            }
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("resultMsg", StringUtils.EMPTY);
            maps.put("mktCampaigns", mktCampaignVOList);
            maps.put("pageInfo", new Page(new PageInfo(mktCampaignDOList)));
        } catch (Exception e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
        }
        return maps;
    }


    /**
     * 活动审核--同步列表
     *
     * @param campaignId
     * @return
     */
    @Override
    public Map<String, Object> examineCampaign4Sync(Long campaignId, String statusCd) {
        Map<String, Object> maps = new HashMap<>();
        try {
            MktCampaignDO campaignDO = mktCampaignMapper.selectByPrimaryKey(campaignId);
            if (campaignDO == null) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "活动不存在");
                return maps;
            }
            if (campaignDO.getStatusCd().equals(STATUS_CODE_PUBLISHED.getStatusCode()) || campaignDO.getStatusCd().equals(StatusCode.STATUS_CODE_PASS.getStatusCode())) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "非待审核活动");
                return maps;
            }
            campaignDO.setStatusCd(statusCd);
            campaignDO.setUpdateDate(new Date());
            mktCampaignMapper.updateByPrimaryKey(campaignDO);
            maps.put("resultCode", CODE_SUCCESS);
            if (statusCd.equals(StatusCode.STATUS_CODE_PASS.getStatusCode())) {
                maps.put("resultMsg", "已通过");
            } else {
                maps.put("resultMsg", "已拒绝");
            }
            //集团活动环节信息更新反馈
            MktCampaignComplete mktCampaignComplete = mktCampaignCompleteMapper.selectByCampaignIdAndTacheCd(campaignDO.getInitId(), "1300");
            if (mktCampaignComplete != null) {
//                mktCampaignComplete.setEndTime(new Date());
//                mktCampaignComplete.setTacheValueCd("11");
//                mktCampaignComplete.setStatusCd("1200");
//                mktCampaignComplete.setUpdateStaff(UserUtil.loginId());
//                mktCampaignComplete.setUpdateDate(new Date());
//                mktCampaignCompleteMapper.update(mktCampaignComplete);
//                MktCampaignComplete campaignComplete = mktCampaignCompleteMapper.selectByCampaignIdAndTacheCd(campaignDO.getInitId(), "1400");
//                if(campaignComplete == null) {
//                    MktCampaignComplete mktCamComplete = new MktCampaignComplete();
//                    mktCamComplete.setMktCampaignId(mktCampaignComplete.getMktCampaignId());
//                    mktCamComplete.setMktActivityNbr(mktCampaignComplete.getMktActivityNbr());
//                    mktCamComplete.setOrderId(mktCampaignComplete.getOrderId());
//                    mktCamComplete.setOrderName(mktCampaignComplete.getOrderName());
//                    mktCamComplete.setTacheCd("1400");
//                    mktCamComplete.setBeginTime(new Date());
//                    mktCamComplete.setEndTime(new Date());
//                    mktCamComplete.setSort(Long.valueOf("4"));
//                    mktCamComplete.setStatusCd("1100");
//                    mktCamComplete.setStatusDate(new Date());
//                    mktCamComplete.setCreateStaff(campaignDO.getCreateStaff());
//                    mktCamComplete.setCreateDate(new Date());
//                    mktCampaignCompleteMapper.insert(mktCamComplete);
                try {
                    openCompleteMktCampaignService.completeMktCampaign(campaignDO.getInitId(), "1200", "11");
                    openCompleteMktCampaignService.completeMktCampaign(campaignDO.getInitId(), "1300", "10");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            }
        } catch (Exception e) {
            maps.put("resultCode", CODE_FAIL);
        }
        return maps;
    }


    /**
     * 活动当前结束时间
     *
     * @param campaignId
     * @return
     */
    @Override
    public Map<String, Object> getCampaignEndTime4Sync(Long campaignId) {
        Map<String, Object> maps = null;
        try {
            maps = new HashMap<>();
            MktCampaignDO campaignDO = mktCampaignMapper.selectByPrimaryKey(campaignId);
            if (campaignDO == null) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "活动不存在");
                return maps;
            }
            maps.put("resultCode", CODE_SUCCESS);
            maps.put("resultMsg", campaignDO.getPlanEndTime());
        } catch (Exception e) {
            maps.put("resultCode", CODE_FAIL);
        }
        return maps;
    }

    /**
     * 活动延期--同步列表
     *
     * @param campaignId
     * @param lastTime
     * @return
     */
    @Override
    public Map<String, Object> delayCampaign4Sync(Long campaignId, Date lastTime) {


        Map<String, Object> maps = new HashMap<>();
        try {
            MktCampaignDO campaignDO = mktCampaignMapper.selectByPrimaryKey(campaignId);
            if (!(STATUS_CODE_PUBLISHED.getStatusCode().equals(campaignDO.getStatusCd()) || StatusCode.STATUS_CODE_PRE_PAUSE.getStatusCode().equals(campaignDO.getStatusCd()))) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "活动状态不为发布或者过期");
                return maps;
            }

            if (campaignDO == null) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "活动不存在");
                return maps;
            }
            if (lastTime.before(campaignDO.getPlanEndTime())) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "时间只能后延");
                return maps;
            }
            // 一次性活动最大延期3个月
            if ("1000".equals(campaignDO.getExecType()) && !"5000".equals(campaignDO.getMktCampaignType()) && lastTime.after(DateUtil.addDate(campaignDO.getPlanEndTime(), 3, MONTH))) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "一次性活动最大延期3个月");
                return maps;
            }
            // 周期性活动最大延期1年
            if ("2000".equals(campaignDO.getExecType()) && !"5000".equals(campaignDO.getMktCampaignType()) && lastTime.after(DateUtil.addDate(campaignDO.getPlanEndTime(), 1, YEAR))) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "周期性活动最大延期1年");
                return maps;
            }
            // 服务活动最大延期5年
            if (("5000".equals(campaignDO.getMktCampaignType()) || "6000".equals(campaignDO.getMktCampaignType())) && lastTime.after(DateUtil.addDate(campaignDO.getPlanEndTime(), 5, YEAR))) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "服务活动最大延期5年");
                return maps;
            }
            // 策略生失效时间延期
            List<MktStrategyConfDO> strategyConfList = mktStrategyConfMapper.selectByCampaignId(campaignId);
            for (MktStrategyConfDO strategy : strategyConfList) {
                strategy.setEndTime(lastTime);
                mktStrategyConfMapper.updateByPrimaryKey(strategy);
                //redisUtils.del("MKT_STRATEGY_" + strategy.getMktStrategyConfId());
            }

            // 渠道生失效时间延期
            List<MktCamChlConfAttrDO> mktCamChlConfAttrDOList = mktCamChlConfAttrMapper.selectAttrEndDateByCampaignId(campaignId);
            for (MktCamChlConfAttrDO mktCamChlConfAttrDO : mktCamChlConfAttrDOList) {
                mktCamChlConfAttrDO.setAttrValue(String.valueOf(lastTime.getTime()));
                //redisUtils.del("CHL_CONF_DETAIL_" + mktCamChlConfAttrDO.getEvtContactConfId());
            }
            mktCamChlConfAttrMapper.updateByPrimaryKeyBatch(mktCamChlConfAttrDOList);

            campaignDO.setPlanEndTime(lastTime);
            campaignDO.setStatusCd(StatusCode.STATUS_CODE_PUBLISHED.getStatusCode());
            mktCampaignMapper.updateByPrimaryKey(campaignDO);
            //redisUtils.del("MKT_CAMPAIGN_" + campaignId);

            maps.put("resultCode", CODE_SUCCESS);
            maps.put("resultMsg", "延期成功");
            try {
                eventRedisService.deleteByCampaign(campaignId);
                logger.info("【活动缓存清理成功】：" + campaignDO.getMktCampaignName());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("【活动缓存清理失败】：" + campaignDO.getMktCampaignName());
            }
        } catch (Exception e) {
            logger.error("Excepiton = " + e);
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", "延期失败！");
        }
        return maps;
    }

    private String getUserLevl() {
        SystemUserDto user = UserUtil.getUser();
        String sysPostCode = null;
        ArrayList<String> arrayList = new ArrayList<>();
        List<SystemPostDto> systemPostDtoList = user.getSystemPostDtoList();
        //岗位信息查看最大权限作为岗位信息
        if (systemPostDtoList.size() > 0 && systemPostDtoList != null) {
            for (SystemPostDto systemPostDto : systemPostDtoList) {
                arrayList.add(systemPostDto.getSysPostCode());
            }
        }
        if (arrayList.contains(AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysPostCode())) {
            sysPostCode = AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysArea();
        } else if (arrayList.contains(AreaCodeEnum.sysAreaCode.SHENGJI.getSysPostCode())) {
            sysPostCode = AreaCodeEnum.sysAreaCode.SHENGJI.getSysArea();
        } else if (arrayList.contains(AreaCodeEnum.sysAreaCode.FENGONGSI.getSysPostCode())) {
            sysPostCode = AreaCodeEnum.sysAreaCode.FENGONGSI.getSysArea();
        } else if (arrayList.contains(AreaCodeEnum.sysAreaCode.FENGJU.getSysPostCode())) {
            sysPostCode = AreaCodeEnum.sysAreaCode.FENGJU.getSysArea();
        } else if (arrayList.contains(AreaCodeEnum.sysAreaCode.ZHIJU.getSysPostCode())) {
            sysPostCode = AreaCodeEnum.sysAreaCode.ZHIJU.getSysArea();
        } else {
            sysPostCode = AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysArea();
        }
        return sysPostCode;
    }

    /**
     * 查询活动列表（分页） -- 活动模板
     */
    @Override
    public Map<String, Object> qryMktCampaignListPage(Map<String, Object> params) {
        Map<String, Object> maps = new HashMap<>();
        //需求函类型控制列表查询
//        Long requestInfoId = MapUtil.getLongNum(params.get("requestInfoId"));
//        if (!"0".equals(requestInfoId.toString())){
//            Map<String, Object> map = checkCampaignByRequestInfo(params);
//            if (map.get("resultCode").equals(CODE_SUCCESS)){
//                Map<String,Object> data = (Map<String, Object>) map.get("data");
//                params.put("tiggerType",data.get("chufaType"));
//                params.put("mktCampaignCategory",data.get("periodType"));
//                if (data.get("campaignType")!=null && data.get("campaignType").toString().equals("5000")){
//                    data.put("campaignType","5000,6000,7000");
//                }
//                params.put("mktCampaignType",data.get("campaignType"));
//            }
//        }
        try {
            MktCampaignDO mktCampaignDO = new MktCampaignDO();
            mktCampaignDO.setMktCampaignName(params.get("mktCampaignName").toString());  // 活动名称
            mktCampaignDO.setStatusCd("(2002, 2010)");                 // 活动状态发布
            mktCampaignDO.setTiggerType(params.get("tiggerType").toString());             // 活动触发类型 - 实时，批量
            mktCampaignDO.setMktCampaignCategory(StatusCode.AUTONOMICK_CAMPAIGN.getStatusCode());// 活动分类 - 框架，强制，自主
            if (params.get("mktCampaignType") != null && !params.get("mktCampaignType").toString().equals("")) {
                mktCampaignDO.setMktCampaignType("(" + params.get("mktCampaignType").toString() + ")");   // 活动类别 - 服务，营销，服务+营销
            }
            if (params.get("createStaff") != null && !"".equals(params.get("createStaff").toString())) {
                mktCampaignDO.setCreateStaff(Long.valueOf(params.get("createStaff").toString()));  // 创建人
            }
            String userLevl = getUserLevl();
            if (!"C1".equals(userLevl) && !"C2".equals(userLevl)) {
                mktCampaignDO.setMktCampaignType("(1000)");
            }
            if ("C1".equals(userLevl) || "C2".equals(userLevl)) {
                mktCampaignDO.setLanId(1L);
            }
            if ("C3".equals(userLevl)) {
                Long regionId = UserUtil.getUser().getLanId();
                Long lanId = AreaCodeEnum.getLandIdByRegionId(regionId);
                mktCampaignDO.setLanId(lanId);
            }
            if ("C4".equals(userLevl)) {
                mktCampaignDO.setTiggerType("1000");
                String c4CodeName = (String) params.get("c4CodeName");
                if (c4CodeName != null && !"".equals(c4CodeName)) {
                    mktCampaignDO.setLanIdFour(Long.valueOf(c4CodeName));
                }
            }
            List<Integer> landIdList = (List) params.get("landIds");
            if (landIdList.size() > 0 && !"".equals(landIdList.get(0))) {
                Long landId = Long.valueOf(landIdList.get(landIdList.size() - 1));
                mktCampaignDO.setLanId(landId);        // 所属地市
            }
            mktCampaignDO.setCreateChannel(params.get("createChannel").toString());       // 创建渠道
            PageHelper.startPage(Integer.parseInt(params.get("page").toString()), Integer.parseInt(params.get("pageSize").toString())); // 分页
            List<MktCampaignCountDO> mktCampaignDOList = mktCampaignMapper.qryMktCampaignList4Moudle(mktCampaignDO);

            // 获取所有的sysParam
            Map<String, String> paramMap = new HashMap<>();
            List<SysParams> sysParamList = sysParamsMapper.selectAll("", "");
            for (SysParams sysParams : sysParamList) {
                paramMap.put(sysParams.getParamKey() + sysParams.getParamValue(), sysParams.getParamName());
            }

            List<MktCampaignDetailVO> mktCampaignVOList = new ArrayList<>();
            for (MktCampaignCountDO mktCampaignCountDO : mktCampaignDOList) {
                MktCampaignDetailVO mktCampaignVO = new MktCampaignDetailVO();
                try {
                    mktCampaignVO.setMktCampaignId(mktCampaignCountDO.getMktCampaignId());
                    mktCampaignVO.setMktCampaignName(mktCampaignCountDO.getMktCampaignName());
                    mktCampaignVO.setMktActivityNbr(mktCampaignCountDO.getMktActivityNbr());
                    mktCampaignVO.setPlanBeginTime(mktCampaignCountDO.getPlanBeginTime());
                    mktCampaignVO.setPlanEndTime(mktCampaignCountDO.getPlanEndTime());

                    String postName = "";
                    try {
                        SystemPost systemPost = new SystemPost();
                        systemPost.setSysPostCode(mktCampaignCountDO.getCreateChannel());
                        logger.info("[op:qryMktCampaignListPage] SysPostCode = " + mktCampaignCountDO.getCreateChannel());
                        QrySystemPostReq qrySystemPostReq = new QrySystemPostReq();
                        qrySystemPostReq.setSystemPost(systemPost);
                        long before1 = System.currentTimeMillis();
                        SysmgrResultObject<com.ctzj.smt.bss.sysmgr.model.common.Page> pageSysmgrResultObject = iSystemPostDubboService.qrySystemPostPage(new com.ctzj.smt.bss.sysmgr.model.common.Page(), qrySystemPostReq);
                        logger.info("iSystemPostDubboService.qrySystemPostPage 消耗时间：" + (System.currentTimeMillis() - before1) + " ms");
                        if (pageSysmgrResultObject != null) {
                            if (pageSysmgrResultObject.getResultObject() != null) {
                                List<SystemPost> dataList = (List<SystemPost>) pageSysmgrResultObject.getResultObject().getDataList();
                                if (dataList != null) {
                                    for (SystemPost post : dataList) {
                                        if (post.getStatusCd().equals("1000")) {
                                            postName = post.getSysPostName();
                                            logger.info("--->>> 岗位信息：" + postName);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("iSystemPostDubboService.qrySystemPostPage接口错误！Exception = ", e);
                    }
                    mktCampaignVO.setCreateChannelName(postName);
                    mktCampaignVO.setCreateDate(mktCampaignCountDO.getCreateDate());
                    mktCampaignVO.setPreMktCampaignId(mktCampaignCountDO.getPreMktCampaignId());
                    MktCampaignDO mktCampaignDOPre = mktCampaignMapper.selectByPrimaryKey(mktCampaignCountDO.getPreMktCampaignId());
                    if (mktCampaignDOPre != null) {
                        mktCampaignVO.setPreMktCampaignId(mktCampaignDOPre.getMktCampaignId());
                        String msgByCode = StatusCode.getMsgByCode(mktCampaignDOPre.getMktCampaignCategory());
                        mktCampaignVO.setPreMktCampaignType(msgByCode == null ? "" : msgByCode);
                    }

                    // 获取创建人信息
                    long before2 = System.currentTimeMillis();
                    SysmgrResultObject<SystemUserDto> systemUserDtoSysmgrResultObject = iSystemUserDtoDubboService.qrySystemUserDto(mktCampaignCountDO.getCreateStaff(), new ArrayList<Long>());
                    logger.info(" iSystemUserDtoDubboService.qrySystemUserDto 消耗时间：" + (System.currentTimeMillis() - before2) + "ms");
                    if (systemUserDtoSysmgrResultObject != null) {
                        if (systemUserDtoSysmgrResultObject.getResultObject() != null) {
                            mktCampaignVO.setCreateStaffName(systemUserDtoSysmgrResultObject.getResultObject().getStaffName());
                            logger.info("--->>> 创建人信息：" + systemUserDtoSysmgrResultObject.getResultObject().getStaffName());
                        }
                    }

                } catch (Exception e) {
                    logger.error("Excetion:", e);
                }
                mktCampaignVO.setMktCampaignCategoryValue(paramMap.
                        get(ParamKeyEnum.MKT_CAMPAIGN_CATEGORY.getParamKey() + mktCampaignCountDO.getMktCampaignCategory()));
                mktCampaignVO.setMktCampaignTypeValue(paramMap.
                        get(ParamKeyEnum.MKT_CAMPAIGN_TYPE.getParamKey() + mktCampaignCountDO.getMktCampaignType()));
                mktCampaignVO.setStatusCdValue(paramMap.
                        get(ParamKeyEnum.STATUS_CD.getParamKey() + mktCampaignCountDO.getStatusCd()));

                Boolean isRelation = false;
                //判断该活动是否有有效的父/子活动
                if (mktCampaignCountDO.getRelCount() != 0) {
                    isRelation = true;
                }

                mktCampaignVO.setRelation(isRelation);


                if (mktCampaignCountDO.getLanId() != null) {
                    SysArea sysArea = (SysArea) redisUtils.get("CITY_" + mktCampaignCountDO.getLanId().toString());
                    if (sysArea != null) {
                        mktCampaignVO.setLandName(sysArea.getName());
                    }

                }
                mktCampaignVOList.add(mktCampaignVO);
            }
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("resultMsg", "查询活动列表成功！");
            maps.put("mktCampaigns", mktCampaignVOList);
            maps.put("pageInfo", new Page(new PageInfo(mktCampaignDOList)));
        } catch (NumberFormatException e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            maps.put("resultMsg", "查询活动列表失败！");
        }
        return maps;
    }


    /**
     * 查询活动列表（分页） -- 活动总览(没有发布状态的活动)
     */
    @Override
    public Map<String, Object> qryMktCampaignListPageForNoPublish(Map<String, Object> params) {
        Map<String, Object> maps = new HashMap<>();
        try {
            MktCampaignDO mktCampaignDO = new MktCampaignDO();
            mktCampaignDO.setMktCampaignName(params.get("mktCampaignName").toString());  // 活动名称
            mktCampaignDO.setStatusCd(params.get("statusCd").toString());                 // 活动状态
            mktCampaignDO.setTiggerType(params.get("tiggerType").toString());             // 活动触发类型 - 实时，批量
            mktCampaignDO.setMktCampaignCategory(params.get("mktCampaignCategory").toString());  // 活动分类 - 框架，强制，自主
            mktCampaignDO.setMktCampaignType(params.get("mktCampaignType").toString());   // 活动类别 - 服务，营销，服务+营销
            mktCampaignDO.setMktActivityNbr(params.get("mktActivityNbr").toString());   // 活动编码
            if (params.get("createStaff").toString() != null && !"".equals(params.get("createStaff").toString())) {
                mktCampaignDO.setCreateStaff(Long.valueOf(params.get("createStaff").toString()));  // 创建人
            }

            List<Integer> landIdList = (List) params.get("landIds");
            if (landIdList.size() > 0 && !"".equals(landIdList.get(0))) {
                Long landId = Long.valueOf(landIdList.get(landIdList.size() - 1));
                mktCampaignDO.setLanId(landId);        // 所属地市
            }
            mktCampaignDO.setCreateChannel(params.get("createChannel").toString());       // 创建渠道
            PageHelper.startPage(Integer.parseInt(params.get("page").toString()), Integer.parseInt(params.get("pageSize").toString())); // 分页
            List<MktCampaignCountDO> mktCampaignDOList = mktCampaignMapper.qryMktCampaignListPageForNoPublish(mktCampaignDO);

            // 获取所有的sysParam
            Map<String, String> paramMap = new HashMap<>();
            List<SysParams> sysParamList = sysParamsMapper.selectAll("", "");
            for (SysParams sysParams : sysParamList) {
                paramMap.put(sysParams.getParamKey() + sysParams.getParamValue(), sysParams.getParamName());
            }

            List<MktCampaignDetailVO> mktCampaignVOList = new ArrayList<>();
            for (MktCampaignCountDO mktCampaignCountDO : mktCampaignDOList) {
                MktCampaignDetailVO mktCampaignVO = new MktCampaignDetailVO();
                try {
                    mktCampaignVO.setMktCampaignId(mktCampaignCountDO.getMktCampaignId());
                    mktCampaignVO.setMktCampaignName(mktCampaignCountDO.getMktCampaignName());
                    mktCampaignVO.setMktActivityNbr(mktCampaignCountDO.getMktActivityNbr());
                    mktCampaignVO.setPlanBeginTime(mktCampaignCountDO.getPlanBeginTime());
                    mktCampaignVO.setPlanEndTime(mktCampaignCountDO.getPlanEndTime());

                    String postName = "";
                    try {
                        SystemPost systemPost = new SystemPost();
                        systemPost.setSysPostCode(mktCampaignCountDO.getCreateChannel());
                        logger.info("[op:qryMktCampaignListPage] SysPostCode = " + mktCampaignCountDO.getCreateChannel());
                        QrySystemPostReq qrySystemPostReq = new QrySystemPostReq();
                        qrySystemPostReq.setSystemPost(systemPost);
                        long before1 = System.currentTimeMillis();
                        SysmgrResultObject<com.ctzj.smt.bss.sysmgr.model.common.Page> pageSysmgrResultObject = iSystemPostDubboService.qrySystemPostPage(new com.ctzj.smt.bss.sysmgr.model.common.Page(), qrySystemPostReq);
                        logger.info("iSystemPostDubboService.qrySystemPostPage 消耗时间：" + (System.currentTimeMillis() - before1) + " ms");
                        if (pageSysmgrResultObject != null) {
                            if (pageSysmgrResultObject.getResultObject() != null) {
                                List<SystemPost> dataList = (List<SystemPost>) pageSysmgrResultObject.getResultObject().getDataList();
                                if (dataList != null) {
                                    for (SystemPost post : dataList) {
                                        if (post.getStatusCd().equals("1000")) {
                                            postName = post.getSysPostName();
                                            logger.info("--->>> 岗位信息：" + postName);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("iSystemPostDubboService.qrySystemPostPage接口错误！Exception = ", e);
                    }
                    mktCampaignVO.setCreateChannelName(postName);
                    mktCampaignVO.setCreateDate(mktCampaignCountDO.getCreateDate());
                    mktCampaignVO.setPreMktCampaignId(mktCampaignCountDO.getPreMktCampaignId());
                    MktCampaignDO mktCampaignDOPre = mktCampaignMapper.selectByPrimaryKey(mktCampaignCountDO.getPreMktCampaignId());
                    if (mktCampaignDOPre != null) {
                        String msgByCode = StatusCode.getMsgByCode(mktCampaignDOPre.getMktCampaignCategory());
                        mktCampaignVO.setPreMktCampaignType(msgByCode == null ? "" : msgByCode);
                    }
                    // 集团活动补丁逻辑（现去除）
                    /*List<MktCampaignComplete> mktCampaignCompletes = mktCampaignCompleteMapper.selectByCampaignId(mktCampaignCountDO.getInitId());
                    if (mktCampaignCompletes!=null && !mktCampaignCompletes.isEmpty() ){
                        if (mktCampaignCountDO.getSrcId()==null || "".equals(mktCampaignCountDO.getSrcId())){
                            mktCampaignVO.setSrcId("0");
                        }else {
                            mktCampaignVO.setSrcId(mktCampaignCountDO.getSrcId());
                        }
                    }*/
                    if (mktCampaignCountDO.getSrcId() != null && !mktCampaignCountDO.getSrcId().isEmpty()) {
                        mktCampaignVO.setSrcId(mktCampaignCountDO.getSrcId());
                    }
                    // c4,c5
                    if ((AreaCodeEnum.sysAreaCode.FENGJU.getSysPostCode().equals(mktCampaignCountDO.getCreateChannel()) || AreaCodeEnum.sysAreaCode.ZHIJU.getSysPostCode().equals(mktCampaignCountDO.getCreateChannel())) && mktCampaignCountDO.getLanIdFour() != null) {
                        SysArea sysArea = sysAreaMapper.selectByPrimaryKey(mktCampaignCountDO.getLanIdFour().intValue());
                        //    Organization organization = organizationMapper.selectByPrimaryKey(mktCampaignDO.getLanIdFour());
                        if (sysArea != null) {
                            mktCampaignVO.setLanIdFourName(sysArea.getName());
                        }
                    }
                    if (AreaCodeEnum.sysAreaCode.ZHIJU.getSysPostCode().equals(mktCampaignCountDO.getCreateChannel()) && mktCampaignCountDO.getLanIdFive() != null) {
                        Organization organization = organizationMapper.selectByPrimaryKey(mktCampaignCountDO.getLanIdFive());
                        if (organization != null) {
                            mktCampaignVO.setLanIdFiveName(organization.getOrgName());
                        }
                    }

                    // 获取创建人信息
                    long before2 = System.currentTimeMillis();
                    SysmgrResultObject<SystemUserDto> systemUserDtoSysmgrResultObject = iSystemUserDtoDubboService.qrySystemUserDto(mktCampaignCountDO.getCreateStaff(), new ArrayList<Long>());
                    logger.info(" iSystemUserDtoDubboService.qrySystemUserDto 消耗时间：" + (System.currentTimeMillis() - before2) + "ms");
                    if (systemUserDtoSysmgrResultObject != null) {
                        if (systemUserDtoSysmgrResultObject.getResultObject() != null) {
                            mktCampaignVO.setCreateStaffName(systemUserDtoSysmgrResultObject.getResultObject().getStaffName());
                            logger.info("--->>> 创建人信息：" + systemUserDtoSysmgrResultObject.getResultObject().getStaffName());
                        }
                    }

                } catch (Exception e) {
                    logger.error("Excetion:", e);
                }
                mktCampaignVO.setMktCampaignCategoryValue(paramMap.
                        get(ParamKeyEnum.MKT_CAMPAIGN_CATEGORY.getParamKey() + mktCampaignCountDO.getMktCampaignCategory()));
                mktCampaignVO.setMktCampaignTypeValue(paramMap.
                        get(ParamKeyEnum.MKT_CAMPAIGN_TYPE.getParamKey() + mktCampaignCountDO.getMktCampaignType()));
                mktCampaignVO.setStatusCdValue(paramMap.
                        get(ParamKeyEnum.STATUS_CD.getParamKey() + mktCampaignCountDO.getStatusCd()));
                mktCampaignVO.setStatusCd(mktCampaignCountDO.getStatusCd());
                Boolean isRelation = false;
                //判断该活动是否有有效的父/子活动
                if (mktCampaignCountDO.getRelCount() != 0) {
                    isRelation = true;
                }

                mktCampaignVO.setRelation(isRelation);


                if (mktCampaignCountDO.getLanId() != null) {
                    SysArea sysArea = (SysArea) redisUtils.get("CITY_" + mktCampaignCountDO.getLanId().toString());
                    if (sysArea != null) {
                        mktCampaignVO.setLandName(sysArea.getName());
                    }

                }
                mktCampaignVOList.add(mktCampaignVO);
            }
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("resultMsg", "查询活动列表成功！");
            maps.put("mktCampaigns", mktCampaignVOList);
            maps.put("pageInfo", new Page(new PageInfo(mktCampaignDOList)));
        } catch (NumberFormatException e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            maps.put("resultMsg", "查询活动列表失败！");
        }
        return maps;
    }


    /**
     * 查询活动列表（分页） -- 活动总览(发布或者调整中状态的活动)
     */
    @Override
    public Map<String, Object> qryMktCampaignListPageForPublish(Map<String, Object> params) {
        Map<String, Object> maps = new HashMap<>();
        try {
            MktCampaignDO mktCampaignDO = new MktCampaignDO();
            mktCampaignDO.setMktCampaignName(params.get("mktCampaignName").toString());  // 活动名称
            mktCampaignDO.setStatusCd(params.get("statusCd").toString());                 // 活动状态
            mktCampaignDO.setTiggerType(params.get("tiggerType").toString());             // 活动触发类型 - 实时，批量
            mktCampaignDO.setMktCampaignCategory(params.get("mktCampaignCategory").toString());  // 活动分类 - 框架，强制，自主
            mktCampaignDO.setMktCampaignType(params.get("mktCampaignType").toString());   // 活动类别 - 服务，营销，服务+营销
            mktCampaignDO.setMktActivityNbr(params.get("mktActivityNbr").toString());   // 活动编码
            if (params.get("createStaff").toString() != null && !"".equals(params.get("createStaff").toString())) {
                mktCampaignDO.setCreateStaff(Long.valueOf(params.get("createStaff").toString()));  // 创建人
            }

            List<Integer> landIdList = (List) params.get("landIds");
            if (landIdList.size() > 0 && !"".equals(landIdList.get(0))) {
                Long landId = Long.valueOf(landIdList.get(landIdList.size() - 1));
                mktCampaignDO.setLanId(landId);        // 所属地市
            }
            mktCampaignDO.setCreateChannel(params.get("createChannel").toString());       // 创建渠道
            PageHelper.startPage(Integer.parseInt(params.get("page").toString()), Integer.parseInt(params.get("pageSize").toString())); // 分页
            List<MktCampaignCountDO> mktCampaignDOList = mktCampaignMapper.qryMktCampaignListPageForPublish(mktCampaignDO);

            // 获取所有的sysParam
            Map<String, String> paramMap = new HashMap<>();
            List<SysParams> sysParamList = sysParamsMapper.selectAll("", "");
            for (SysParams sysParams : sysParamList) {
                paramMap.put(sysParams.getParamKey() + sysParams.getParamValue(), sysParams.getParamName());
            }

            List<MktCampaignDetailVO> mktCampaignVOList = new ArrayList<>();
            for (MktCampaignCountDO mktCampaignCountDO : mktCampaignDOList) {
                MktCampaignDetailVO mktCampaignVO = new MktCampaignDetailVO();
                try {
                    mktCampaignVO.setMktCampaignId(mktCampaignCountDO.getMktCampaignId());
                    mktCampaignVO.setMktCampaignName(mktCampaignCountDO.getMktCampaignName());
                    mktCampaignVO.setMktActivityNbr(mktCampaignCountDO.getMktActivityNbr());
                    mktCampaignVO.setPlanBeginTime(mktCampaignCountDO.getPlanBeginTime());
                    mktCampaignVO.setPlanEndTime(mktCampaignCountDO.getPlanEndTime());
                    mktCampaignVO.setCreateStaff(mktCampaignCountDO.getCreateStaff());

                    String postName = "";
                    try {
                        SystemPost systemPost = new SystemPost();
                        systemPost.setSysPostCode(mktCampaignCountDO.getCreateChannel());
                        logger.info("[op:qryMktCampaignListPage] SysPostCode = " + mktCampaignCountDO.getCreateChannel());
                        QrySystemPostReq qrySystemPostReq = new QrySystemPostReq();
                        qrySystemPostReq.setSystemPost(systemPost);
                        long before1 = System.currentTimeMillis();
                        SysmgrResultObject<com.ctzj.smt.bss.sysmgr.model.common.Page> pageSysmgrResultObject = iSystemPostDubboService.qrySystemPostPage(new com.ctzj.smt.bss.sysmgr.model.common.Page(), qrySystemPostReq);
                        logger.info("iSystemPostDubboService.qrySystemPostPage 消耗时间：" + (System.currentTimeMillis() - before1) + " ms");
                        if (pageSysmgrResultObject != null) {
                            if (pageSysmgrResultObject.getResultObject() != null) {
                                List<SystemPost> dataList = (List<SystemPost>) pageSysmgrResultObject.getResultObject().getDataList();
                                if (dataList != null) {
                                    for (SystemPost post : dataList) {
                                        if (post.getStatusCd().equals("1000")) {
                                            postName = post.getSysPostName();
                                            logger.info("--->>> 岗位信息：" + postName);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("iSystemPostDubboService.qrySystemPostPage接口错误！Exception = ", e);
                    }
                    mktCampaignVO.setCreateChannelName(postName);
                    mktCampaignVO.setCreateDate(mktCampaignCountDO.getCreateDate());
                    mktCampaignVO.setPreMktCampaignId(mktCampaignCountDO.getPreMktCampaignId());
                    MktCampaignDO mktCampaignDOPre = mktCampaignMapper.selectByPrimaryKey(mktCampaignCountDO.getPreMktCampaignId());
                    if (mktCampaignDOPre != null) {
                        mktCampaignVO.setPreMktCampaignId(mktCampaignDOPre.getMktCampaignId());
                        String msgByCode = StatusCode.getMsgByCode(mktCampaignDOPre.getMktCampaignCategory());
                        mktCampaignVO.setPreMktCampaignType(msgByCode == null ? "" : msgByCode);
                    }
                    mktCampaignVO.setSrcId(mktCampaignCountDO.getSrcId());
                    // c4,c5
                    if ((AreaCodeEnum.sysAreaCode.FENGJU.getSysPostCode().equals(mktCampaignCountDO.getCreateChannel()) || AreaCodeEnum.sysAreaCode.ZHIJU.getSysPostCode().equals(mktCampaignCountDO.getCreateChannel())) && mktCampaignCountDO.getLanIdFour() != null) {
                        SysArea sysArea = sysAreaMapper.selectByPrimaryKey(mktCampaignCountDO.getLanIdFour().intValue());
                        //    Organization organization = organizationMapper.selectByPrimaryKey(mktCampaignDO.getLanIdFour());
                        if (sysArea != null) {
                            mktCampaignVO.setLanIdFourName(sysArea.getName());
                        }
                    }
                    if (AreaCodeEnum.sysAreaCode.ZHIJU.getSysPostCode().equals(mktCampaignCountDO.getCreateChannel()) && mktCampaignCountDO.getLanIdFive() != null) {
                        Organization organization = organizationMapper.selectByPrimaryKey(mktCampaignCountDO.getLanIdFive());
                        if (organization != null) {
                            mktCampaignVO.setLanIdFiveName(organization.getOrgName());
                        }
                    }
                    // 获取创建人信息
                    long before2 = System.currentTimeMillis();
                    SysmgrResultObject<SystemUserDto> systemUserDtoSysmgrResultObject = iSystemUserDtoDubboService.qrySystemUserDto(mktCampaignCountDO.getCreateStaff(), new ArrayList<Long>());
                    logger.info(" iSystemUserDtoDubboService.qrySystemUserDto 消耗时间：" + (System.currentTimeMillis() - before2) + "ms");
                    if (systemUserDtoSysmgrResultObject != null) {
                        if (systemUserDtoSysmgrResultObject.getResultObject() != null) {
                            mktCampaignVO.setCreateStaffName(systemUserDtoSysmgrResultObject.getResultObject().getStaffName());
                            logger.info("--->>> 创建人信息：" + systemUserDtoSysmgrResultObject.getResultObject().getStaffName());
                        }
                    }

                } catch (Exception e) {
                    logger.error("Excetion:", e);
                }
                mktCampaignVO.setMktCampaignCategoryValue(paramMap.
                        get(ParamKeyEnum.MKT_CAMPAIGN_CATEGORY.getParamKey() + mktCampaignCountDO.getMktCampaignCategory()));
                mktCampaignVO.setMktCampaignTypeValue(paramMap.
                        get(ParamKeyEnum.MKT_CAMPAIGN_TYPE.getParamKey() + mktCampaignCountDO.getMktCampaignType()));
                mktCampaignVO.setStatusCdValue(paramMap.
                        get(ParamKeyEnum.STATUS_CD.getParamKey() + mktCampaignCountDO.getStatusCd()));

                Boolean isRelation = false;
                //判断该活动是否有有效的父/子活动
                if (mktCampaignCountDO.getRelCount() != 0) {
                    isRelation = true;
                }

                mktCampaignVO.setRelation(isRelation);


                if (mktCampaignCountDO.getLanId() != null) {
                    SysArea sysArea = (SysArea) redisUtils.get("CITY_" + mktCampaignCountDO.getLanId().toString());
                    if (sysArea != null) {
                        mktCampaignVO.setLandName(sysArea.getName());
                    }

                }
                mktCampaignVOList.add(mktCampaignVO);
            }
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("resultMsg", "查询活动列表成功！");
            maps.put("mktCampaigns", mktCampaignVOList);
            maps.put("pageInfo", new Page(new PageInfo(mktCampaignDOList)));
        } catch (NumberFormatException e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            maps.put("resultMsg", "查询活动列表失败！");
        }
        return maps;
    }


    /**
     * 修改活动状态
     *
     * @param mktCampaignId
     * @param statusCd
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> changeMktCampaignStatus(final Long mktCampaignId, String statusCd) throws Exception {
        Map<String, Object> maps = new HashMap<>();
        try {
            MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCampaignId);
            String oldStatusCd = mktCampaignDO.getStatusCd();
            Long initId = mktCampaignDO.getInitId();
            // 记录活动操作
            mktOperatorLogService.addMktOperatorLog(mktCampaignDO.getMktCampaignName(), mktCampaignId, mktCampaignDO.getMktActivityNbr(), mktCampaignDO.getStatusCd(), statusCd, UserUtil.loginId(), statusCd);
            mktCampaignMapper.changeMktCampaignStatus(mktCampaignId, statusCd, new Date(), UserUtil.loginId());
            // 判断是否是发布活动, 是该状态生效
            if (STATUS_CODE_PUBLISHED.getStatusCode().equals(statusCd) || StatusCode.STATUS_CODE_ROLL.getStatusCode().equals(statusCd) || StatusCode.STATUS_CODE_PRE_PAUSE.getStatusCode().equals(statusCd)) {
                List<MktCamResultRelDO> mktCamResultRelDOS = mktCamResultRelMapper.selectResultByMktCampaignId(mktCampaignId);
                for (MktCamResultRelDO mktCamResultRelDO : mktCamResultRelDOS) {
                    mktCamResultRelDO.setStatus(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                    if (StatusCode.STATUS_CODE_STOP.getStatusCode().equals(statusCd)) {
                        mktCamResultRelMapper.updateByPrimaryKey(mktCamResultRelDO);
                    }
                }
                // 删除事件接入缓存
                List<MktCamEvtRelDO> mktCamEvtRelDOS = mktCamEvtRelMapper.selectByMktCampaignId(mktCampaignId);
                for (MktCamEvtRelDO mktCamEvtRelDO : mktCamEvtRelDOS) {
                    redisUtils.del("CAM_EVT_REL_" + mktCamEvtRelDO.getEventId());
                    redisUtils.del("CAM_IDS_EVT_REL_" + mktCamEvtRelDO.getEventId());
                }
                // 发布调整的活动，下线源活动
                if (STATUS_CODE_PUBLISHED.getStatusCode().equals(statusCd) && !mktCampaignId.equals(initId)) {
                    // 查询initId为mktCampaignId且状态为调整中
                    MktCampaignDO mktCampaignDOAdjust = mktCampaignMapper.selectPrimaryKeyByInitId(initId, StatusCode.STATUS_CODE_ADJUST.getStatusCode());
                    if (mktCampaignDOAdjust != null) {
                        changeMktCampaignStatus(mktCampaignDOAdjust.getMktCampaignId(), StatusCode.STATUS_CODE_ROLL.getStatusCode());
                    }
                }
                if (StatusCode.STATUS_CODE_PRE_PAUSE.getStatusCode().equals(statusCd)) {
                    // 过期活动
                    updateProjectStateTime(mktCampaignDO.getInitId());
                    mktCamEvtRelMapper.deleteByMktCampaignId(mktCampaignDO.getMktCampaignId());
                }
                if (StatusCode.STATUS_CODE_ROLL.getStatusCode().equals(statusCd)) {
                    // 删除下线活动与事件的关系
                    mktCamEvtRelMapper.deleteByMktCampaignId(mktCampaignId);
                    //派单活动状态修改接口
                    try {
                        List<TrialOperation> trialOperations = trialOperationMapper.listOperationByCamIdAndStatusCd(mktCampaignId, TrialStatus.CHANNEL_PUBLISH_SUCCESS.getValue());
                        if (trialOperations != null && trialOperations.size() > 0) {
                            updateProjectStateTime(mktCampaignDO.getInitId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    campaignRedisChane(mktCampaignId);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("[op:MktCampaignServiceImpl] 缓存添加失败 by mktCampaignId = {}, Expection = ", mktCampaignId, e);
                }
                syncCamData2Synergy(mktCampaignDO);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            eventRedisService.deleteByCampaign(mktCampaignId);
                            logger.info("【活动缓存清理成功】：" + mktCampaignDO.getMktCampaignName());
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("【活动缓存清理失败】：" + mktCampaignDO.getMktCampaignName());
                        }
                    }
                }.start();

            } else if (StatusCode.STATUS_CODE_STOP.getStatusCode().equals(statusCd)) {
                // 活动下线清缓存
                //redisUtils.del("MKT_CAMPAIGN_" + mktCampaignId);
                if (StatusCode.STATUS_CODE_STOP.getStatusCode().equals(statusCd)) {
                    List<MktCamResultRelDO> mktCamResultRelDOS = mktCamResultRelMapper.selectResultByMktCampaignId(mktCampaignId);
                    for (MktCamResultRelDO mktCamResultRelDO : mktCamResultRelDOS) {
                        mktCamResultRelDO.setStatus(StatusCode.STATUS_CODE_NOTACTIVE.getStatusCode());
                        mktCamResultRelMapper.updateByPrimaryKey(mktCamResultRelDO);
                    }
                }
            } else if (StatusCode.STATUS_CODE_ROLL_BACK.getStatusCode().equals(statusCd)) {
                // 查询initId为mktCampaignId且状态为调整待发布
                MktCampaignDO mktCampaignDONew = mktCampaignMapper.selectByInitForRollBack(mktCampaignDO.getInitId());
                // 删除“调整待发布”活动,并保证不能删除源活动
                if (mktCampaignDONew != null && mktCampaignDONew.getMktCampaignId() != mktCampaignDONew.getInitId()) {
                    delMktCampaign(mktCampaignDONew.getMktCampaignId());
                }
                // 回滚活动, 改变原来状态为发布
                changeMktCampaignStatus(mktCampaignId, STATUS_CODE_PUBLISHED.getStatusCode());
            }

            if (STATUS_CODE_PUBLISHED.getStatusCode().equals(statusCd)) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_PUBLISHED_SUCCESS.getErrorMsg());
            } else if (StatusCode.STATUS_CODE_CHECKING.getStatusCode().equals(statusCd)) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_CHECKING_SUCCESS.getErrorMsg());
            } else if (StatusCode.STATUS_CODE_STOP.getStatusCode().equals(statusCd)) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_STOP_SUCCESS.getErrorMsg());
            } else if (StatusCode.STATUS_CODE_ROLL.getStatusCode().equals(statusCd)) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_ROLL_SUCCESS.getErrorMsg());
            }
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[op:changeMktCampaignStatus] 修改活动状态statusCd = {}失败,Exception = ", statusCd, e);
            if (STATUS_CODE_PUBLISHED.getStatusCode().equals(statusCd)) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_PUBLISHED_FAILURE.getErrorMsg());
            } else if (StatusCode.STATUS_CODE_CHECKING.getStatusCode().equals(statusCd)) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_CHECKING_FAILURE.getErrorMsg());
            } else if (StatusCode.STATUS_CODE_STOP.getStatusCode().equals(statusCd)) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_STOP_FAILURE.getErrorMsg());
            } else if (StatusCode.STATUS_CODE_ROLL.getStatusCode().equals(statusCd)) {
                maps.put("resultMsg", ErrorCode.STATUS_CODE_ROLL_FAILURE.getErrorMsg());
            }
            maps.put("resultCode", CommonConstant.CODE_FAIL);
        }

        return maps;
    }

    // 同步活动信息到营服协同
    public void syncCamData2Synergy(MktCampaignDO mktCampaignDO) {
        try {
            Map<String, Object> stringObjectMap = JSON.parseObject(JSON.toJSONString(mktCampaignDO, SerializerFeature.WriteMapNullValue), new TypeReference<Map<String, Object>>() {
            });
            stringObjectMap.put("planBeginTime", date2StringDate(mktCampaignDO.getPlanBeginTime()));
            stringObjectMap.put("planEndTime", date2StringDate(mktCampaignDO.getPlanEndTime()));
            stringObjectMap.put("beginTime", date2StringDate(mktCampaignDO.getBeginTime()));
            stringObjectMap.put("endTime", date2StringDate(mktCampaignDO.getEndTime()));
            stringObjectMap.put("createDate", date2StringDate(mktCampaignDO.getCreateDate()));
            stringObjectMap.put("statusDate", date2StringDate(mktCampaignDO.getStatusDate()));
            stringObjectMap.put("updateDate", date2StringDate(mktCampaignDO.getUpdateDate()));
            stringObjectMap.put("manageType", mktCampaignDO.getMktCampaignCategory());
            Map resultMap = iCpcAPIService.mktCampaignSync(stringObjectMap);
            logger.info(JSON.toJSONString(stringObjectMap));
            logger.info("resultCode:" + resultMap.get("resultCode") + ",resultMsg:" + resultMap.get("resultMsg") + ",reqId:" + resultMap.get("reqId"));
        } catch (Exception e) {
            logger.error("[op:MktCampaignServiceImpl] 发布活动营服调用失败。", e);
        }
    }

    /**
     * 缓存处理
     *
     * @param mktCampaignId
     */
    private void campaignRedisChane(Long mktCampaignId) {
        List<MktCamEvtRelDO> eventRelList = mktCamEvtRelMapper.selectByMktCampaignId(mktCampaignId);
        for (MktCamEvtRelDO eventDo : eventRelList) {
            Map<String, String> mktAllLabels = searchLabelService.labelListByEventId(eventDo.getEventId());  //查询事件下使用的所有标签
            if (null != mktAllLabels) {
                redisUtils.set("EVT_ALL_LABEL_" + eventDo.getEventId(), mktAllLabels);
            }
        }
        List<LabelDTO> labelDTOList = mktCamDisplayColumnRelMapper.selectLabelDisplayListByCamId(mktCampaignId);
        redisUtils.set("CAM_LABEL_DTO_LIST" + mktCampaignId, labelDTOList);
        //过滤规则标签
        List<FilterRule> filterRules = filterRuleMapper.selectFilterRuleList(Long.valueOf(mktCampaignId.toString()));
        redisUtils.set("CAM_FILTER_LIST_" + mktCampaignId, filterRules);
        //规则级的标签
        List<MktStrategyConfRuleDO> ruleList = ruleMapper.selectByCampaignId(Long.valueOf(mktCampaignId.toString()));
        redisUtils.set("CAM_RULE_LIST_" + mktCampaignId, ruleList);
        //话术标签
        for (MktStrategyConfRuleDO ruleDO : ruleList) {
            if (ruleDO.getEvtContactConfId() != null && !ruleDO.getEvtContactConfId().equals("")) {
                String[] confList = ruleDO.getEvtContactConfId().split("/");
                //推荐指引标签
                for (String confId : confList) {
                    List<MktVerbal> verbalList = verbalMapper.findVerbalListByConfId(Long.valueOf(confId));
                    redisUtils.set("CAM_VERBAL_LIST_" + confId, verbalList);
                    List<MktCamChlConfAttrDO> confAttrDOList = confAttrMapper.selectByEvtContactConfId(Long.valueOf(confId));
                    redisUtils.set("CAM_CONF_ATTR_LIST_" + confId, confAttrDOList);
                }
            }
            List<TarGrpCondition> conditionList = tarGrpConditionMapper.listTarGrpCondition(ruleDO.getTarGrpId());
            redisUtils.set("CAM_TAR_CONDITION_LIST_" + ruleDO.getTarGrpId(), conditionList);
        }
    }


    private void conditionFilter(MktStrategyConfRuleDO ruleDO) {

        if (ruleDO != null && ruleDO.getEvtContactConfId() != null && !"".equals(ruleDO.getEvtContactConfId())) {
            String[] evtContactConfIds = ruleDO.getEvtContactConfId().split("/");
            boolean flg = false;
            for (String evtContactConfId : evtContactConfIds) {
                List<MktCamChlConfDO> mktCamChlConfDOS = mktCamChlConfMapper.listByMessageChannel(Long.valueOf(evtContactConfId));
                if (!mktCamChlConfDOS.isEmpty()) {
                    flg = true;
                    break;
                }
            }
            if (flg) {
                Label label = labelMapper.selectByLabelCode("LAST30DAYS_SMS_MARKET_CNT");
                if (label != null) {
                    List<TarGrpCondition> conditions = tarGrpConditionMapper.listTarGrpCondition(ruleDO.getTarGrpId());
                    boolean conditionFlg = true;
                    for (TarGrpCondition condition : conditions) {
                        if (condition.getLeftParam().equals(label.getInjectionLabelId().toString())) {
                            conditionFlg = false;
                            break;
                        }
                    }
                    if (conditionFlg) {
                        TarGrpCondition tarGrpCondition = new TarGrpCondition();
                        tarGrpCondition.setLeftParam(label.getInjectionLabelId().toString());
                        tarGrpCondition.setOperType(Operator.LESS_THAN_EQUAL.getValue().toString());
                        tarGrpCondition.setRightParam("3");
                        tarGrpCondition.setRootFlag(0L);
                        tarGrpCondition.setRemark(tarGrpCondition.getRemark() == null ? "2000" : tarGrpCondition.getRemark());
                        tarGrpCondition.setConditionText(tarGrpCondition.getConditionText() == null ? "2000" : tarGrpCondition.getConditionText());
                        tarGrpCondition.setLeftParamType(LeftParamType.LABEL.getErrorCode());//左参为注智标签
                        tarGrpCondition.setRightParamType(RightParamType.FIX_VALUE.getErrorCode());//右参为固定值
                        tarGrpCondition.setTarGrpId(ruleDO.getTarGrpId());
                        tarGrpCondition.setCreateDate(DateUtil.getCurrentTime());
                        tarGrpCondition.setUpdateDate(DateUtil.getCurrentTime());
                        tarGrpCondition.setStatusDate(DateUtil.getCurrentTime());
                        tarGrpCondition.setCreateStaff(UserUtil.loginId());
                        tarGrpCondition.setStatusCd(CommonConstant.STATUSCD_EFFECTIVE);
                        tarGrpConditionMapper.insert(tarGrpCondition);
                    }
                }
            }
        }
    }


    /**
     * 发布并下发活动
     *
     * @param mktCampaignId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> publishMktCampaign(final Long mktCampaignId) throws Exception {
        Map<String, Object> mktCampaignMap = new HashMap<>();
        try {
            // 获取当前活动信息
            MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCampaignId);
            if (mktCampaignDO != null && mktCampaignDO.getStatusCd().equals(STATUS_CODE_PUBLISHED.getStatusCode())) {
                mktCampaignMap.put("resultCode", CommonConstant.CODE_FAIL);
                mktCampaignMap.put("resultMsg", "已发布活动请勿重复发布！");
                return mktCampaignMap;
            }
            // 获取当前活动标识
            Long parentMktCampaignId = mktCampaignDO.getMktCampaignId();
            // 获取当前活动名称
            String parentMktCampaignName = mktCampaignDO.getMktCampaignName();
            // 获取活动下策略的集合
            List<MktCamStrategyConfRelDO> mktCamStrategyConfRelDOList = mktCamStrategyConfRelMapper.selectByMktCampaignId(parentMktCampaignId);
            if ("1000".equals(mktCampaignDO.getMktCampaignType())) {
                for (MktCamStrategyConfRelDO mktCamStrategyConfRelDO : mktCamStrategyConfRelDOList) {
                    List<MktStrategyConfRuleDO> ruleDOList = mktStrategyConfRuleMapper.selectByMktStrategyConfId(mktCamStrategyConfRelDO.getStrategyConfId());
                    for (MktStrategyConfRuleDO ruleDO : ruleDOList) {
                        conditionFilter(ruleDO);
                    }
                }
            }
            // 获取生失效时间
            Date effDate = mktCampaignDO.getPlanBeginTime();
            Date expDate = mktCampaignDO.getPlanEndTime();
            // 刷活动数据
            campaignConfig(parentMktCampaignId);
            // 获取活动与事件的关系
            List<MktCamEvtRelDO> MktCamEvtRelDOList = mktCamEvtRelMapper.selectByMktCampaignId(parentMktCampaignId);
            // 获取当前活动的下发城市集合
            List<MktCamCityRelDO> mktCamCityRelDOList = mktCamCityRelMapper.selectByMktCampaignId(parentMktCampaignId);
            List<Long> childMktCampaignIdList = new ArrayList<>();
            // 遍历活动下发城市集合
            for (MktCamCityRelDO mktCamCityRelDO : mktCamCityRelDOList) {
                // 为下发城市生成新的活动
                if (!mktCamCityRelDOList.isEmpty()) {
                    mktCampaignDO.setMktCampaignId(null);
                    // 活动名称加上地市
                    String areaName = AreaNameEnum.getNameByLandId(mktCamCityRelDO.getCityId());
                    mktCampaignDO.setMktCampaignName(parentMktCampaignName + "_" + areaName);
                    mktCampaignDO.setMktCampaignCategory(StatusCode.AUTONOMICK_CAMPAIGN.getStatusCode()); // 子活动默认为自主活动
                    mktCampaignDO.setLanId(mktCamCityRelDO.getCityId()); // 本地网标识
                    mktCampaignDO.setRegionId(AreaCodeEnum.getRegionIdByLandId(mktCamCityRelDO.getCityId()));
                    mktCampaignDO.setCreateDate(new Date());
                    mktCampaignDO.setUpdateDate(new Date());
                    mktCampaignDO.setUpdateStaff(UserUtil.loginId());
                    mktCampaignDO.setStatusCd(StatusCode.STATUS_CODE_DRAFT.getStatusCode());
                    mktCampaignDO.setCreateChannel(createChannel);
                    mktCampaignMapper.insert(mktCampaignDO);
                    // 获取新的活动的Id
                    Long childMktCampaignId = mktCampaignDO.getMktCampaignId();
                    // 活动编码
                    mktCampaignDO.setMktActivityNbr("MKT" + String.format("%06d", childMktCampaignId));
                    // initId
                    mktCampaignDO.setInitId(childMktCampaignId);
                    mktCampaignMapper.updateByPrimaryKey(mktCampaignDO);
                    //创建主题关系
                    topicLabelRel(mktCampaignId, mktCampaignDO);
                    childMktCampaignIdList.add(childMktCampaignId);
                    // 与父活动进行关联
                    MktCampaignRelDO mktCampaignRelDO = new MktCampaignRelDO();
                    mktCampaignRelDO.setaMktCampaignId(parentMktCampaignId);
                    mktCampaignRelDO.setzMktCampaignId(childMktCampaignId);
                    mktCampaignRelDO.setApplyRegionId(mktCamCityRelDO.getCityId());
                    mktCampaignRelDO.setEffDate(effDate);
                    mktCampaignRelDO.setExpDate(expDate);
                    mktCampaignRelDO.setRelType(StatusCode.PARENT_CHILD_RELATION.getStatusCode());   //  1000-父子关系
                    mktCampaignRelDO.setCreateDate(new Date());
                    mktCampaignRelDO.setCreateStaff(UserUtil.loginId());
                    mktCampaignRelDO.setUpdateDate(new Date());
                    mktCampaignRelDO.setCreateStaff(UserUtil.loginId());
                    mktCampaignRelDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());  // 1000-有效
                    mktCampaignRelDO.setStatusDate(new Date());
                    mktCampaignRelMapper.insert(mktCampaignRelDO);

                    //事件与新活动建立关联
                    for (MktCamEvtRelDO mktCamEvtRelDO : MktCamEvtRelDOList) {
                        MktCamEvtRelDO childMktCamEvtRelDO = new MktCamEvtRelDO();

                        childMktCamEvtRelDO.setMktCampaignId(childMktCampaignId);
                        childMktCamEvtRelDO.setEventId(mktCamEvtRelDO.getEventId());
                        childMktCamEvtRelDO.setCampaignSeq(mktCamEvtRelDO.getCampaignSeq());
                        childMktCamEvtRelDO.setLevelConfig(mktCamEvtRelDO.getLevelConfig());
                        childMktCamEvtRelDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                        childMktCamEvtRelDO.setStatusDate(new Date());
                        childMktCamEvtRelDO.setCreateDate(new Date());
                        childMktCamEvtRelDO.setCreateStaff(UserUtil.loginId());
                        childMktCamEvtRelDO.setUpdateDate(new Date());
                        childMktCamEvtRelDO.setCreateStaff(UserUtil.loginId());
                        mktCamEvtRelMapper.insert(childMktCamEvtRelDO);
                    }

                    // 推荐条目下发
                    productService.copyItemByCampaignPublish(parentMktCampaignId, childMktCampaignId, mktCampaignDO.getMktCampaignCategory());

                    // 试运算展示列实例化
                    mktCamDisplayColumnRelService.copyDisplayLabelByCamId(parentMktCampaignId, childMktCampaignId);

                    // 遍历活动下策略的集合
                    for (MktCamStrategyConfRelDO mktCamStrategyConfRelDO : mktCamStrategyConfRelDOList) {
                        Map<String, Object> mktStrategyConfMap = mktStrategyConfService.copyMktStrategyConf(mktCamStrategyConfRelDO.getStrategyConfId(), parentMktCampaignId, childMktCampaignId, true, mktCampaignDO.getLanId());
                        Long childMktStrategyConfId = (Long) mktStrategyConfMap.get("childMktStrategyConfId");
                        // 建立活动和策略的关系
                        MktCamStrategyConfRelDO chaildMktCamStrategyConfRelDO = new MktCamStrategyConfRelDO();
                        chaildMktCamStrategyConfRelDO.setMktCampaignId(childMktCampaignId);
                        chaildMktCamStrategyConfRelDO.setStrategyConfId(childMktStrategyConfId);
                        chaildMktCamStrategyConfRelDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode()); // 1000-有效
                        chaildMktCamStrategyConfRelDO.setStatusDate(new Date());
                        chaildMktCamStrategyConfRelDO.setCreateDate(new Date());
                        chaildMktCamStrategyConfRelDO.setCreateStaff(UserUtil.loginId());
                        chaildMktCamStrategyConfRelDO.setUpdateDate(new Date());
                        chaildMktCamStrategyConfRelDO.setUpdateStaff(UserUtil.loginId());
                        mktCamStrategyConfRelMapper.insert(chaildMktCamStrategyConfRelDO);
                    }

                    // 活动下过滤规则
                    List<MktStrategyFilterRuleRelDO> mktStrategyFilterRuleRelDOS = mktStrategyFilterRuleRelMapper.selectRuleByStrategyId(mktCampaignId);
                    for (MktStrategyFilterRuleRelDO mktStrategyFilterRuleRelDO : mktStrategyFilterRuleRelDOS) {
                        mktStrategyFilterRuleRelDO.setMktStrategyFilterRuleRelId(null);
                        mktStrategyFilterRuleRelDO.setStrategyId(childMktCampaignId);
                        mktStrategyFilterRuleRelMapper.insert(mktStrategyFilterRuleRelDO);
                    }

                    // 活动下关单规则
                    List<MktStrategyCloseRuleRelDO> mktStrategyCloseRuleRelDOS = mktStrategyCloseRuleRelMapper.selectRuleByStrategyId(mktCampaignId);
                    for (MktStrategyCloseRuleRelDO mktStrategyCloseRuleRelDO : mktStrategyCloseRuleRelDOS) {
                        mktStrategyCloseRuleRelDO.setMktStrategyFilterRuleRelId(null);
                        mktStrategyCloseRuleRelDO.setStrategyId(childMktCampaignId);
                        mktStrategyCloseRuleRelMapper.insert(mktStrategyCloseRuleRelDO);
                    }
                    //如果是框架活动 生成子活动后  生成对应的子需求函 下发给指定岗位的指定人员
                    generateRequest(mktCampaignDO, mktCamCityRelDO.getCityId());
                }
            }
            if (CAM_RESOURCE.getStatusCode().equals(mktCampaignDO.getMktCampaignType())){
                logger.info("【电子券活动发布调用资源中心开始】：" + JSON.toJSONString(mktCampaignDO));
                productService.mktCamResourceService(mktCampaignDO.getMktCampaignId());
            }
            mktCampaignMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktCampaignMap.put("resultMsg", "发布活动成功！");
            mktCampaignMap.put("childMktCampaignIdList", childMktCampaignIdList);
            //集团活动环节信息更新反馈
            MktCampaignComplete mktCampaignComplete = mktCampaignCompleteMapper.selectByCampaignIdAndTacheCdAndTacheValueCd(mktCampaignId, "1300", "10");
            if (mktCampaignComplete != null) {
                try {
                    openCompleteMktCampaignService.completeMktCampaign(mktCampaignDO.getInitId(), "1300", "11");
                    openCompleteMktCampaignService.completeMktCampaign(mktCampaignDO.getInitId(), "1400", "10");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            logger.error("[op:MktCampaignServiceImpl] failed to publishMktCampaign by mktCampaignId = {}, Exception = ", mktCampaignId, e);
            mktCampaignMap.put("resultCode", CommonConstant.CODE_FAIL);
            mktCampaignMap.put("resultMsg", "发布活动失败！");
        }
        return mktCampaignMap;
    }

    private void topicLabelRel(Long mktCampaignId, MktCampaignDO mktCampaignDO) {
        //创建主题关系
        if (StringUtils.isNotBlank(mktCampaignDO.getTheMe())) {
            TopicLabel label = topicLabelMapper.selectByLabelCode(mktCampaignDO.getTheMe());
            if (label != null) {
                ObjectLabelRel objectLabelRel = new ObjectLabelRel();
                objectLabelRel.setCreateDate(new Date());
                objectLabelRel.setCreateStaff(mktCampaignDO.getCreateStaff());
                objectLabelRel.setObjId(mktCampaignId);
                objectLabelRel.setLabelId(label.getLabelId());
                objectLabelRel.setObjType("1900");
                objectLabelRel.setStatusCd("1000");
                objectLabelRel.setUpdateDate(new Date());
                objectLabelRelMapper.insert(objectLabelRel);
            }
        }
    }

    private void UserListTemp(Long mktCampaignId, MktCampaignDO mktCampaignDO) {
        List<String> mktCamCodeList = (List<String>) redisUtils.get("MKT_CAM_API_CODE_KEY");
        if (mktCamCodeList == null) {
            List<SysParams> sysParamsList = sysParamsMapper.listParamsByKeyForCampaign("MKT_CAM_API_CODE");
            mktCamCodeList = new ArrayList<String>();
            for (SysParams sysParams : sysParamsList) {
                mktCamCodeList.add(sysParams.getParamValue());
            }
            redisUtils.set("MKT_CAM_API_CODE_KEY", mktCamCodeList);
        }
        if (mktCamCodeList.contains(mktCampaignDO.getInitId().toString())) {
            new Thread() {
                public void run() {
                    logger.info("清单活动发布全量算清单：" + mktCampaignId + " INIT_ID:" + mktCampaignDO.getInitId());
                    Map<String, Object> params = new HashMap<>();
                    List<Integer> arrayList = new ArrayList<>();
                    arrayList.add(Integer.valueOf(mktCampaignId.toString()));
                    params.put("userListCam", "USER_LIST_CAM");
                    params.put("idList", arrayList);
                    trialProdService.campaignIndexTask(params);
                }
            }.start();
        }
    }

    //xyl 活动发布 一些活动直接做全量试算
    private void UserListTemp(Long mktCampaignId, Long initId) {
        List<Long> mktCamCodeList = mktCampaignMapper.getUserListTempMktCamCodeList();
        if (mktCamCodeList.contains(initId.toString())) {
            new Thread() {
                public void run() {
                    logger.info("清单活动发布全量算清单：" + mktCampaignId + " INIT_ID:" + initId);
                    Map<String, Object> params = new HashMap<>();
                    List<Integer> arrayList = new ArrayList<>();
                    arrayList.add(Integer.valueOf(mktCampaignId.toString()));
                    params.put("userListCam", "BIG_DATA_TEMP");
                    params.put("idList", arrayList);
                    trialProdService.campaignIndexTask(params);
                }
            }.start(); //BIG_DATA_TEMP
        }

    }

    /**
     * 根据地市生成子需求函，子活动和子需求函的关联，和指定的承接人员
     *
     * @param mktCampaignDO 新生成的子活动
     * @param lanId         地市id
     */
    public void generateRequest(MktCampaignDO mktCampaignDO, Long lanId) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setRequestType("mkt");
        //需求函批次号按规律递增1
        requestInfo.setBatchNo(getBatchNo(requestInfoMapper.selectMaxBatchNo()));
        requestInfo.setName(mktCampaignDO.getMktCampaignName());
        requestInfo.setDesc(mktCampaignDO.getMktCampaignName());
        requestInfo.setReason(mktCampaignDO.getMktCampaignName());
        requestInfo.setStartDate(new Date());
        requestInfo.setExpectFinishDate(mktCampaignDO.getPlanEndTime());
        requestInfo.setStatusCd("1000");
        requestInfo.setStatusDate(new Date());
        requestInfo.setCreateDate(new Date());
        requestInfo.setUpdateDate(new Date());
        requestInfo.setActionType("add");
        requestInfo.setActivitiKey("mkt_force_province_city");  //需求函活动类型
        requestInfo.setRequestUrgentType("一般");
        requestInfo.setProcessType("0");
        requestInfo.setReportTag("0");
        //得到指定下发的人员信息集合
        List<SysParams> sysParams = sysParamsMapper.listParamsByKeyForCampaign(CITY_PUBLISH);
        if (!sysParams.isEmpty()) {
            SysParams s = sysParams.get(0);
            String paramValue = s.getParamValue();
            if (StringUtils.isNotBlank(paramValue)) {
                JSONArray jsonArray = JSONArray.parseArray(paramValue);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject o = JSONObject.parseObject(JSON.toJSONString(jsonArray.get(i)));
                    String lan = o.getString("lanId");
                    if (lanId - Long.valueOf(lan) == 0) {
                        requestInfo.setContName(o.getString("name"));
                        requestInfo.setDeptCode(o.getString("department"));
                        requestInfo.setCreateStaff(o.getLong("employeeId"));   //创建人,目前指定到承接人的工号
                        mktCampaignDO.setCreateStaff(o.getLong("systemUserId"));
                        mktCampaignMapper.updateByPrimaryKey(mktCampaignDO);
                        break;
                    }
                }
            }
        }
        requestInfoMapper.insert(requestInfo);
        //开始增加子活动和需求函的关系
        RequestInstRel rel = new RequestInstRel();
        rel.setRequestObjId(mktCampaignDO.getMktCampaignId());
        rel.setRequestInfoId(requestInfo.getRequestTemplateInstId());
        rel.setRequestObjType("mkt");
        rel.setStatusDate(new Date());
        rel.setUpdateDate(new Date());
        rel.setCreateDate(new Date());
        rel.setStatusCd(STATUSCD_EFFECTIVE);
        requestInstRelMapper.insertInfo(rel);

    }

    //集团活动创建需求函
    public void generateRequest(MktCampaignDO mktCampaignDO, SystemUserDto user) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setRequestType("mkt");
        //需求函批次号按规律递增1
        requestInfo.setBatchNo(getJITUANBatchNo(requestInfoMapper.selectMaxBatchNo()));
        requestInfo.setName(mktCampaignDO.getMktCampaignName());
        requestInfo.setDesc(mktCampaignDO.getMktCampaignName());
        requestInfo.setReason(mktCampaignDO.getMktCampaignName());
        requestInfo.setStartDate(new Date());
        requestInfo.setExpectFinishDate(mktCampaignDO.getPlanEndTime());
        requestInfo.setStatusCd("1000");
        requestInfo.setStatusDate(new Date());
        requestInfo.setCreateDate(new Date());
        requestInfo.setUpdateDate(new Date());
        requestInfo.setActionType("add");
        requestInfo.setActivitiKey("mkt_group_process");  //需求函活动类型
        requestInfo.setRequestUrgentType("一般");
        requestInfo.setProcessType("0");
        requestInfo.setReportTag("0");
        //得到指定下发的人员信息集合
        List<SysParams> sysParams = sysParamsMapper.listParamsByKeyForCampaign(GROUP_CAMPAIGN_RECIPIENT);
        if (!sysParams.isEmpty()) {
            SysParams s = sysParams.get(0);
            String paramValue = s.getParamValue();
            if (StringUtils.isNotBlank(paramValue)) {
                JSONArray jsonArray = JSONArray.parseArray(paramValue);
                //for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject o = JSONObject.parseObject(JSON.toJSONString(jsonArray.get(0)));
                //String lan = o.getString("lanId");
                //if (lanId - Long.valueOf(lan) == 0) {

                requestInfo.setContName(user.getStaffName() == null ? "" : user.getStaffName());
                requestInfo.setDeptCode(user.getOrgName());
                requestInfo.setCreateStaff(user.getStaffId());   //创建人,目前指定到承接人的工号
                mktCampaignDO.setCreateStaff(user.getSysUserId());

//                requestInfo.setContName(o.getString("name"));
//                requestInfo.setDeptCode(o.getString("department"));
//                requestInfo.setCreateStaff(o.getLong("employeeId"));   //创建人,目前指定到承接人的工号
//                mktCampaignDO.setCreateStaff(o.getLong("systemUserId"));
                mktCampaignMapper.updateByPrimaryKey(mktCampaignDO);
                //break;
                //}
                //}
            }
        }
        requestInfoMapper.insert(requestInfo);
        //开始增加子活动和需求函的关系
        RequestInstRel rel = new RequestInstRel();
        rel.setRequestObjId(mktCampaignDO.getMktCampaignId());
        rel.setRequestInfoId(requestInfo.getRequestTemplateInstId());
        rel.setRequestObjType("mkt");
        rel.setStatusDate(new Date());
        rel.setUpdateDate(new Date());
        rel.setCreateDate(new Date());
        rel.setStatusCd(STATUSCD_EFFECTIVE);
        requestInstRelMapper.insertInfo(rel);
    }

    /**
     * 得到最新的批次编号
     * 浙电产品套餐需求浙【2019】1002116号
     *
     * @param batchNo
     * @return
     */
    public String getBatchNo(String batchNo) {
        String substring = "浙电营销活动需求【" + DateUtil.getCurrentYear().toString() + "】";
        Long num = requestInfoMapper.selectBatchNoNum();
        String path = substring + num.toString() + "号";
        return path;
    }

    /**
     * 得到最新的批次编号
     * 浙电产品套餐需求浙【2019】1002116号
     *
     * @param batchNo
     * @return
     */
    public String getJITUANBatchNo(String batchNo) {
        String substring = "集团营销活动需求【" + DateUtil.getCurrentYear().toString() + "】";
        Long num = requestInfoMapper.selectBatchNoNum();
        String path = substring + num.toString() + "号";
        return path;
    }


    /**
     * 升级活动
     *
     * @param mktCampaignId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> upgradeMktCampaign(Long mktCampaignId) throws Exception {
        Map<String, Object> upgradeMap = new HashMap<>();
        try {
            MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCampaignId);
            // 获取原活动Id
            Long parentsMktCampaignId = mktCampaignDO.getMktCampaignId();
            mktCampaignDO.setMktCampaignId(null);
            // 升级后为 服务+营销活动
            mktCampaignDO.setMktCampaignType(StatusCode.SERVICE_SALES_CAMPAIGN.getStatusCode()); //6000 - 升级关系
            mktCampaignDO.setStatusCd(StatusCode.STATUS_CODE_DRAFT.getStatusCode());
            mktCampaignDO.setStatusDate(new Date());
            mktCampaignDO.setCreateDate(new Date());
            mktCampaignDO.setCreateStaff(UserUtil.loginId());
            mktCampaignDO.setUpdateDate(new Date());
            mktCampaignDO.setUpdateStaff(UserUtil.loginId());
            mktCampaignMapper.insert(mktCampaignDO);
            Long childMktCampaignId = mktCampaignDO.getMktCampaignId();

            MktCampaignRelDO mktCampaignRelDO = new MktCampaignRelDO();
            // 设置2个活动的关系为升级关系
            mktCampaignRelDO.setRelType(StatusCode.UPDATE_RELATION.getStatusCode()); //3000 - 升级关系
            mktCampaignRelDO.setaMktCampaignId(parentsMktCampaignId);
            mktCampaignRelDO.setzMktCampaignId(childMktCampaignId);
            mktCampaignRelDO.setApplyRegionId(mktCampaignDO.getLanId());
            mktCampaignRelDO.setEffDate(mktCampaignDO.getPlanBeginTime());
            mktCampaignRelDO.setExpDate(mktCampaignDO.getPlanEndTime());
            mktCampaignRelDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
            mktCampaignRelDO.setStatusDate(new Date());
            mktCampaignRelDO.setCreateDate(new Date());
            mktCampaignRelDO.setCreateStaff(UserUtil.loginId());
            mktCampaignRelDO.setUpdateDate(new Date());
            mktCampaignRelDO.setUpdateStaff(UserUtil.loginId());
            mktCampaignRelMapper.insert(mktCampaignRelDO);

            // 获取事件
            List<MktCamEvtRelDO> mktCamEvtRelDOList = mktCamEvtRelMapper.selectByMktCampaignId(parentsMktCampaignId);
            for (MktCamEvtRelDO mktCamEvtRelDO : mktCamEvtRelDOList) {
                mktCamEvtRelDO.setMktCampEvtRelId(null);
                mktCamEvtRelDO.setMktCampaignId(childMktCampaignId);
                mktCamEvtRelMapper.insert(mktCamEvtRelDO);
            }

            // 下发地市
            List<MktCamCityRelDO> mktCamCityRelDOList = mktCamCityRelMapper.selectByMktCampaignId(parentsMktCampaignId);
            for (MktCamCityRelDO mktCamCityRelDO : mktCamCityRelDOList) {
                mktCamCityRelDO.setMktCamCityRelId(null);
                mktCamCityRelDO.setMktCampaignId(childMktCampaignId);
                mktCamCityRelMapper.insert(mktCamCityRelDO);
            }
            List<MktCamStrategyConfRelDO> mktCamStrategyConfRelDOList = mktCamStrategyConfRelMapper.selectByMktCampaignId(parentsMktCampaignId);
            for (MktCamStrategyConfRelDO mktCamStrategyConfRelDO : mktCamStrategyConfRelDOList) {
                Map<String, Object> map = mktStrategyConfService.copyMktStrategyConf(mktCamStrategyConfRelDO.getStrategyConfId(), parentsMktCampaignId, childMktCampaignId, false, mktCampaignDO.getLanId());
                Long childMktStrategyConfId = (Long) map.get("childMktStrategyConfId");
                MktCamStrategyConfRelDO childtCamStrRelDO = new MktCamStrategyConfRelDO();
                childtCamStrRelDO.setMktCampaignId(childMktCampaignId);
                childtCamStrRelDO.setStrategyConfId(childMktStrategyConfId);
                childtCamStrRelDO.setCreateDate(new Date());
                childtCamStrRelDO.setCreateStaff(UserUtil.loginId());
                childtCamStrRelDO.setUpdateDate(new Date());
                childtCamStrRelDO.setUpdateStaff(UserUtil.loginId());
                mktCamStrategyConfRelMapper.insert(childtCamStrRelDO);
            }
            upgradeMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            upgradeMap.put("resultMsg", "升级活动成功！");
            upgradeMap.put("childMktCampaignId", childMktCampaignId);
        } catch (Exception e) {
            upgradeMap.put("resultCode", CommonConstant.CODE_FAIL);
            upgradeMap.put("resultMsg", "升级活动失败！");
        }
        return upgradeMap;
    }


    /**
     * 选择模板(电子券)
     *
     * @param preMktCampaignId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getMktCampaignTemplate4CamResource(Long preMktCampaignId) throws Exception {
        Map<String, Object> maps = new HashMap<>();
        try {
            // 获取关系
            List<MktCampaignRelDO> mktCampaignRelDOList = mktCampaignRelMapper.selectByAmktCampaignId(preMktCampaignId, StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
            List<CityProperty> applyRegionIds = new ArrayList<>();
            // 获取活动基本信息
            MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(preMktCampaignId);

            MktCampaignDetailVO mktCampaignVO = new MktCampaignDetailVO();
            CopyPropertiesUtil.copyBean2Bean(mktCampaignVO, mktCampaignDO);
            // 获取下发城市集合
            List<MktCamCityRelDO> mktCamCityRelDOList = mktCamCityRelMapper.selectByMktCampaignId(mktCampaignVO.getMktCampaignId());
            for (MktCamCityRelDO mktCamCityRelDO : mktCamCityRelDOList) {
                SysArea sysArea = sysAreaMapper.selectByPrimaryKey(mktCamCityRelDO.getCityId().intValue());
                CityProperty cityProperty = new CityProperty();
                cityProperty.setCityPropertyId(sysArea.getAreaId().longValue());
                cityProperty.setCityPropertyName(sysArea.getName());
                applyRegionIds.add(cityProperty);
            }
            mktCampaignVO.setApplyRegionIdList(applyRegionIds);

            CatalogItem catalogItem = catalogItemMapper.selectByPrimaryKey(mktCampaignDO.getDirectoryId());
            if (catalogItem != null) {
                mktCampaignVO.setDirectoryName(catalogItem.getCatalogItemName());
            } else {
                MktCamDirectoryDO mktCamDirectoryDO = mktCamDirectoryMapper.selectByPrimaryKey(mktCampaignDO.getDirectoryId());
                if (mktCamDirectoryDO != null) {
                    mktCampaignVO.setDirectoryName(mktCamDirectoryDO.getMktCamDirectoryName());
                }
            }
            // 获取所有的sysParam
            Map<String, String> paramMap = new HashMap<>();
            List<SysParams> sysParamList = sysParamsMapper.selectAll("", "");
            for (SysParams sysParams : sysParamList) {
                paramMap.put(sysParams.getParamKey() + sysParams.getParamValue(), sysParams.getParamName());
            }
            mktCampaignVO.setTiggerTypeValue(paramMap.
                    get(ParamKeyEnum.TIGGER_TYPE.getParamKey() + mktCampaignDO.getTiggerType()));
            mktCampaignVO.setMktCampaignCategoryValue(paramMap.
                    get(ParamKeyEnum.MKT_CAMPAIGN_CATEGORY.getParamKey() + mktCampaignDO.getMktCampaignCategory()));
            mktCampaignVO.setMktCampaignTypeValue(paramMap.
                    get(ParamKeyEnum.MKT_CAMPAIGN_TYPE.getParamKey() + mktCampaignDO.getMktCampaignType()));
            mktCampaignVO.setStatusCdValue(paramMap.
                    get(ParamKeyEnum.STATUS_CD.getParamKey() + mktCampaignDO.getStatusCd()));
            mktCampaignVO.setExecTypeValue(paramMap.
                    get(ParamKeyEnum.EXEC_TYPE.getParamKey() + mktCampaignDO.getExecType()));

            // 获取活动关联的事件
            List<MktCamEvtRelDO> mktCamEvtRelDOList = mktCamEvtRelMapper.selectByMktCampaignId(mktCampaignDO.getMktCampaignId());
            if (mktCamEvtRelDOList != null) {
                List<EventDTO> eventDTOList = new ArrayList<>();
                for (MktCamEvtRelDO mktCamEvtRelDO : mktCamEvtRelDOList) {
                    Long eventId = mktCamEvtRelDO.getEventId();
                    ContactEvt contactEvt = contactEvtMapper.getEventById(eventId);
                    if (contactEvt != null) {
                        EventDTO eventDTO = new EventDTO();
                        eventDTO.setEventId(eventId);
                        eventDTO.setEventName(contactEvt.getContactEvtName());
                        eventDTOList.add(eventDTO);
                    }
                }
                mktCampaignVO.setEventDTOS(eventDTOList);
            }

            List<Long> camItemIdList = mktCamItemMapper.selectCamItemIdByCampaignId(preMktCampaignId);

            Map<String, Object> stringObjectMap = productService.copyProductRule(UserUtil.loginId(), camItemIdList);
            List<Long> ruleIdList = (List<Long>) stringObjectMap.get("ruleIdList");
            mktCampaignVO.setMktCamItemIdList(ruleIdList);

            // 获取过滤规则集合
            List<Long> filterRuleIdList = mktStrategyFilterRuleRelMapper.selectByStrategyId(preMktCampaignId);
            mktCampaignVO.setFilterRuleIdList(filterRuleIdList);

            // 获取关单规则集合
            List<Long> closeRuleIdList = mktStrategyCloseRuleRelMapper.selectByStrategyId(preMktCampaignId);
            mktCampaignVO.setCloseRuleIdList(closeRuleIdList);

            Map<String, Object> strategyTemplateMap = mktStrategyConfService.getStrategyTemplate(preMktCampaignId);
            List<MktStrategyConfDetail> mktStrategyConfDetailList = (List<MktStrategyConfDetail>) strategyTemplateMap.get("mktStrategyConfDetailList");
            mktCampaignVO.setMktStrategyConfDetailList(mktStrategyConfDetailList);

            List<MktCamResource> mktCamResourceList = mktCamResourceMapper.selectByCampaignId(preMktCampaignId, FrameFlgEnum.YES.getValue(), null);
            if (mktCamResourceList != null && mktCamResourceList.size() > 0) {
                mktCampaignVO.setMktCamResource(mktCamResourceList.get(0));
            }
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("mktCampaignVO", mktCampaignVO);
        } catch (Exception e) {
            logger.error("[op:MktCampaignServiceImpl] failed to getMktCampaignTemplate by preMktCampaignId = {}, Exception = ", preMktCampaignId, e);
            maps.put("resultCode", CommonConstant.CODE_FAIL);
        }
        return maps;
    }


    /**
     * 选择模板
     *
     * @param preMktCampaignId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getMktCampaignTemplate(Long preMktCampaignId) throws Exception {
        Map<String, Object> maps = new HashMap<>();
        try {
            // 获取关系
            List<MktCampaignRelDO> mktCampaignRelDOList = mktCampaignRelMapper.selectByAmktCampaignId(preMktCampaignId, StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
            List<CityProperty> applyRegionIds = new ArrayList<>();
            // 获取活动基本信息
            MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(preMktCampaignId);

            MktCampaignDetailVO mktCampaignVO = new MktCampaignDetailVO();
            CopyPropertiesUtil.copyBean2Bean(mktCampaignVO, mktCampaignDO);
            // 获取下发城市集合
            List<MktCamCityRelDO> mktCamCityRelDOList = mktCamCityRelMapper.selectByMktCampaignId(mktCampaignVO.getMktCampaignId());
            for (MktCamCityRelDO mktCamCityRelDO : mktCamCityRelDOList) {
                SysArea sysArea = sysAreaMapper.selectByPrimaryKey(mktCamCityRelDO.getCityId().intValue());
                CityProperty cityProperty = new CityProperty();
                cityProperty.setCityPropertyId(sysArea.getAreaId().longValue());
                cityProperty.setCityPropertyName(sysArea.getName());
                applyRegionIds.add(cityProperty);
            }
            mktCampaignVO.setApplyRegionIdList(applyRegionIds);

            CatalogItem catalogItem = catalogItemMapper.selectByPrimaryKey(mktCampaignDO.getDirectoryId());
            if (catalogItem != null) {
                mktCampaignVO.setDirectoryName(catalogItem.getCatalogItemName());
            } else {
                MktCamDirectoryDO mktCamDirectoryDO = mktCamDirectoryMapper.selectByPrimaryKey(mktCampaignDO.getDirectoryId());
                if (mktCamDirectoryDO != null) {
                    mktCampaignVO.setDirectoryName(mktCamDirectoryDO.getMktCamDirectoryName());
                }
            }
            // 获取所有的sysParam
            Map<String, String> paramMap = new HashMap<>();
            List<SysParams> sysParamList = sysParamsMapper.selectAll("", "");
            for (SysParams sysParams : sysParamList) {
                paramMap.put(sysParams.getParamKey() + sysParams.getParamValue(), sysParams.getParamName());
            }
            mktCampaignVO.setTiggerTypeValue(paramMap.
                    get(ParamKeyEnum.TIGGER_TYPE.getParamKey() + mktCampaignDO.getTiggerType()));
            mktCampaignVO.setMktCampaignCategoryValue(paramMap.
                    get(ParamKeyEnum.MKT_CAMPAIGN_CATEGORY.getParamKey() + mktCampaignDO.getMktCampaignCategory()));
            mktCampaignVO.setMktCampaignTypeValue(paramMap.
                    get(ParamKeyEnum.MKT_CAMPAIGN_TYPE.getParamKey() + mktCampaignDO.getMktCampaignType()));
            mktCampaignVO.setStatusCdValue(paramMap.
                    get(ParamKeyEnum.STATUS_CD.getParamKey() + mktCampaignDO.getStatusCd()));
            mktCampaignVO.setExecTypeValue(paramMap.
                    get(ParamKeyEnum.EXEC_TYPE.getParamKey() + mktCampaignDO.getExecType()));

            // 获取活动关联的事件
            List<MktCamEvtRelDO> mktCamEvtRelDOList = mktCamEvtRelMapper.selectByMktCampaignId(mktCampaignDO.getMktCampaignId());
            if (mktCamEvtRelDOList != null) {
                List<EventDTO> eventDTOList = new ArrayList<>();
                for (MktCamEvtRelDO mktCamEvtRelDO : mktCamEvtRelDOList) {
                    Long eventId = mktCamEvtRelDO.getEventId();
                    ContactEvt contactEvt = contactEvtMapper.getEventById(eventId);
                    if (contactEvt != null) {
                        EventDTO eventDTO = new EventDTO();
                        eventDTO.setEventId(eventId);
                        eventDTO.setEventName(contactEvt.getContactEvtName());
                        eventDTOList.add(eventDTO);
                    }
                }
                mktCampaignVO.setEventDTOS(eventDTOList);
            }

            List<Long> camItemIdList = mktCamItemMapper.selectCamItemIdByCampaignId(preMktCampaignId);

            Map<String, Object> stringObjectMap = productService.copyProductRule(UserUtil.loginId(), camItemIdList);
            List<Long> ruleIdList = (List<Long>) stringObjectMap.get("ruleIdList");
            mktCampaignVO.setMktCamItemIdList(ruleIdList);

            // 获取过滤规则集合
            List<Long> filterRuleIdList = mktStrategyFilterRuleRelMapper.selectByStrategyId(preMktCampaignId);
            mktCampaignVO.setFilterRuleIdList(filterRuleIdList);

            // 获取关单规则集合
            List<Long> closeRuleIdList = mktStrategyCloseRuleRelMapper.selectByStrategyId(preMktCampaignId);
            mktCampaignVO.setCloseRuleIdList(closeRuleIdList);

            Map<String, Object> strategyTemplateMap = mktStrategyConfService.getStrategyTemplate(preMktCampaignId);
            List<MktStrategyConfDetail> mktStrategyConfDetailList = (List<MktStrategyConfDetail>) strategyTemplateMap.get("mktStrategyConfDetailList");

            mktCampaignVO.setMktStrategyConfDetailList(mktStrategyConfDetailList);

            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("mktCampaignVO", mktCampaignVO);
        } catch (Exception e) {
            logger.error("[op:MktCampaignServiceImpl] failed to getMktCampaignTemplate by preMktCampaignId = {}, Exception = ", preMktCampaignId, e);
            maps.put("resultCode", CommonConstant.CODE_FAIL);
        }
        return maps;
    }


    /**
     * 调整活动（复制活动）
     *
     * @param parentMktCampaignId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> copyMktCampaign(Long parentMktCampaignId) {
        Map<String, Object> maps = new HashMap<>();
        try {
            // 获取活动基本信息
            MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(parentMktCampaignId);
            // 将新活动数据入库
            mktCampaignDO.setMktCampaignId(null);
            mktCampaignDO.setInitId(mktCampaignDO.getInitId());
            mktCampaignDO.setUpdateStaff(UserUtil.loginId());
            mktCampaignDO.setUpdateDate(new Date());
            mktCampaignDO.setStatusCd(StatusCode.STATUS_CODE_PRE_PUBLISHED.getStatusCode());
            mktCampaignDO.setStatusDate(new Date());
            mktCampaignMapper.insert(mktCampaignDO);
            Long newMktCampaignId = mktCampaignDO.getMktCampaignId();

            // 获取下发城市集合
            List<MktCamCityRelDO> mktCamCityRelDOList = mktCamCityRelMapper.selectByMktCampaignId(parentMktCampaignId);
            for (MktCamCityRelDO mktCamCityRelDO : mktCamCityRelDOList) {
                mktCamCityRelDO.setMktCamCityRelId(null);
                mktCamCityRelDO.setMktCampaignId(newMktCampaignId);
                mktCamCityRelDO.setCreateStaff(UserUtil.loginId());
                mktCamCityRelDO.setCreateDate(new Date());
                mktCamCityRelDO.setUpdateStaff(UserUtil.loginId());
                mktCamCityRelDO.setUpdateDate(new Date());
            }
            if (mktCamCityRelDOList != null && !mktCamCityRelDOList.isEmpty()) {
                mktCamCityRelMapper.insertBatch(mktCamCityRelDOList);
            }


            // 获取活动关联的事件
            List<MktCamEvtRelDO> mktCamEvtRelDOList = mktCamEvtRelMapper.selectByMktCampaignId(parentMktCampaignId);
            if (mktCamEvtRelDOList != null) {
                for (MktCamEvtRelDO mktCamEvtRelDO : mktCamEvtRelDOList) {
                    mktCamEvtRelDO.setMktCampEvtRelId(null);
                    mktCamEvtRelDO.setMktCampaignId(newMktCampaignId);
                    mktCamEvtRelDO.setCreateStaff(UserUtil.loginId());
                    mktCamEvtRelDO.setCreateDate(new Date());
                    mktCamEvtRelDO.setUpdateStaff(UserUtil.loginId());
                    mktCamEvtRelDO.setUpdateDate(new Date());
                }
                if (mktCamEvtRelDOList != null && !mktCamEvtRelDOList.isEmpty()) {
                    mktCamEvtRelMapper.insertBatch(mktCamEvtRelDOList);
                }
            }

            // 推荐条目复制
            Map<String, Object> itemResult = productService.copyItemByCampaign(parentMktCampaignId, newMktCampaignId);
            Map<Long, Long> itemMap = (Map<Long, Long>) itemResult.get("itemMap");

            // 试运算展示列实例化
            mktCamDisplayColumnRelService.copyDisplayLabelByCamId(parentMktCampaignId, newMktCampaignId);

            // 获取过滤规则集合
            List<Long> filterRuleIdList = mktStrategyFilterRuleRelMapper.selectByStrategyId(parentMktCampaignId);
            List<MktStrategyFilterRuleRelDO> mktStrategyFilterRuleRelDOList = new ArrayList<>();
            for (Long filterRuleId : filterRuleIdList) {
                MktStrategyFilterRuleRelDO mktStrategyFilterRuleRelDO = new MktStrategyFilterRuleRelDO();
                mktStrategyFilterRuleRelDO.setStrategyId(newMktCampaignId);
                mktStrategyFilterRuleRelDO.setRuleId(filterRuleId);
                mktStrategyFilterRuleRelDO.setCreateStaff(UserUtil.loginId());
                mktStrategyFilterRuleRelDO.setCreateDate(new Date());
                mktStrategyFilterRuleRelDO.setUpdateStaff(UserUtil.loginId());
                mktStrategyFilterRuleRelDO.setUpdateDate(new Date());
                mktStrategyFilterRuleRelDOList.add(mktStrategyFilterRuleRelDO);
            }
            if (mktStrategyFilterRuleRelDOList != null && !mktStrategyFilterRuleRelDOList.isEmpty()) {
                mktStrategyFilterRuleRelMapper.insertBatch(mktStrategyFilterRuleRelDOList);
            }
            // 获取关单规则集合
            List<Long> closeRuleIdList = mktStrategyCloseRuleRelMapper.selectByStrategyId(parentMktCampaignId);
            List<MktStrategyCloseRuleRelDO> mktStrategyCloseRuleRelDOList = new ArrayList<>();
            for (Long closeRuleId : closeRuleIdList) {
                MktStrategyCloseRuleRelDO mktStrategyCloseRuleRelDO = new MktStrategyCloseRuleRelDO();
                mktStrategyCloseRuleRelDO.setStrategyId(newMktCampaignId);
                mktStrategyCloseRuleRelDO.setRuleId(closeRuleId);
                mktStrategyCloseRuleRelDO.setCreateStaff(UserUtil.loginId());
                mktStrategyCloseRuleRelDO.setCreateDate(new Date());
                mktStrategyCloseRuleRelDO.setUpdateStaff(UserUtil.loginId());
                mktStrategyCloseRuleRelDO.setUpdateDate(new Date());
                mktStrategyCloseRuleRelDOList.add(mktStrategyCloseRuleRelDO);
            }
            if (mktStrategyCloseRuleRelDOList != null && !mktStrategyCloseRuleRelDOList.isEmpty()) {
                mktStrategyCloseRuleRelMapper.insertBatch(mktStrategyCloseRuleRelDOList);
            }
            // 遍历活动下策略的集合
            List<MktCamStrategyConfRelDO> mktCamStrategyConfRelDONewList = new ArrayList<>();
            List<MktCamStrategyConfRelDO> mktCamStrategyConfRelDOList = mktCamStrategyConfRelMapper.selectByMktCampaignId(parentMktCampaignId);
            for (MktCamStrategyConfRelDO mktCamStrategyConfRelDO : mktCamStrategyConfRelDOList) {
                Map<String, Object> mktStrategyConfMap = mktStrategyConfService.copyMktStrategyConfForAdjust(mktCamStrategyConfRelDO.getStrategyConfId(), parentMktCampaignId, newMktCampaignId, mktCampaignDO.getLanId(), itemMap);
                Long childMktStrategyConfId = (Long) mktStrategyConfMap.get("childMktStrategyConfId");
                MktCamStrategyConfRelDO mktCamStrRelDONew = new MktCamStrategyConfRelDO();
                mktCamStrRelDONew.setMktCampaignId(newMktCampaignId);
                mktCamStrRelDONew.setStrategyConfId(childMktStrategyConfId);
                mktCamStrRelDONew.setCreateStaff(UserUtil.loginId());
                mktCamStrRelDONew.setCreateDate(new Date());
                mktCamStrRelDONew.setUpdateStaff(UserUtil.loginId());
                mktCamStrRelDONew.setUpdateDate(new Date());
                mktCamStrategyConfRelDONewList.add(mktCamStrRelDONew);
            }
            if (mktCamStrategyConfRelDONewList != null && !mktCamStrategyConfRelDONewList.isEmpty()) {
                mktCamStrategyConfRelMapper.insertBatch(mktCamStrategyConfRelDONewList);
            }
            //复制电子券
            productService.copyMktCamResource4Cam(parentMktCampaignId,newMktCampaignId);
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("mktCampaignId", newMktCampaignId);
        } catch (Exception e) {
            logger.error("[op:MktCampaignServiceImpl] failed to copyMktCampaign by parentMktCampaignId = {}, Exception = ", parentMktCampaignId, e);
            maps.put("resultCode", CommonConstant.CODE_FAIL);
        }
        return maps;
    }



    /**
     * 定时过期活动
     *
     * @return
     */
    @Override
    public Map<String, Object> dueMktCampaign() {
        Date startDate = new Date();
        // 3月不活跃活动过期
        //activityStatisticsService.MoreThan3MonthsOffline();
        Map<String, Object> result = new HashMap<>();
        // 查出所有已经发布的活动
        try {
            MktCampaignDO parma = new MktCampaignDO();
            parma.setStatusCd("(2002, 2006, 2008)");
            List<MktCampaignDO> mktCampaignDOList = mktCampaignMapper.qryMktCampaignListByCondition(parma);
            Date now = new Date();
            for (MktCampaignDO mktCampaignDO : mktCampaignDOList) {
                // 在失效时间之后置为过期
                if (mktCampaignDO.getPlanEndTime() != null && now.after(mktCampaignDO.getPlanEndTime())) {
                    mktCampaignDO.setStatusCd(StatusCode.STATUS_CODE_PRE_PAUSE.getStatusCode());
                    mktCampaignDO.setStatusDate(now);
                    mktCampaignDO.setUpdateDate(now);
                    mktCampaignMapper.updateByPrimaryKey(mktCampaignDO);
                    mktCamEvtRelMapper.deleteByMktCampaignId(mktCampaignDO.getMktCampaignId());
                    updateProjectStateTime(mktCampaignDO.getInitId());
                }
            }
            result.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktDttsLogService.saveMktDttsLog("5000", "成功", startDate, new Date(), "成功", null);
        } catch (Exception e) {
            result.put("resultCode", CommonConstant.CODE_FAIL);
            result.put("resultMsg", e);
            mktDttsLogService.saveMktDttsLog("5000", "失败", startDate, new Date(), "失败", e.toString());
        }
        return result;
    }


    /**
     * 统计活动
     *
     * @return
     */
    @Override
    public Map<String, Object> countMktCampaign(Map<String, Object> params) {

        /* 条件入参处理 */
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        Date startDate = null;
        Date endDate = null;
        if (params.get("month") != null && !"".equals(params.get("month"))) {
            String month = params.get("month").toString();
            String[] timeArr = month.split("-");
            startDate = string2DateTime4Day(getFisrtDayOfMonth(Integer.valueOf(timeArr[0]), Integer.valueOf(timeArr[1])));
            endDate = string2DateTime4Day(getLastDayOfMonth(Integer.valueOf(timeArr[0]), Integer.valueOf(timeArr[1])));
            paramMap.put("startTime", startDate);
            paramMap.put("endTime", endDate);
        }
        if (params.get("startTime") != null && !"".equals(params.get("startTime"))) {
            String startTime = params.get("startTime").toString();
            String[] timeArr = startTime.split("-");
            startDate = string2DateTime4Day(getFisrtDayOfMonth(Integer.valueOf(timeArr[0]), Integer.valueOf(timeArr[1])));
            paramMap.put("startTime", startDate);
        }
        if (params.get("endTime") != null && !"".equals(params.get("endTime"))) {
            String endTime = params.get("endTime").toString();
            String[] timeArr = endTime.split("-");
            endDate = string2DateTime4Day(getLastDayOfMonth(Integer.valueOf(timeArr[0]), Integer.valueOf(timeArr[1])));
            paramMap.put("endTime", endDate);
        }


        String lanId = params.get("lanId").toString();
        paramMap.put("lanId", lanId);
        try {
            //3个统计
            List<Map> topMapList = new ArrayList<>();
            Map<String, Object> draftMap = new HashMap<>();
            //统计草稿状态活动
            paramMap.put("statusCd", "2001");
            int draftCount = mktCampaignMapper.countByStatus(paramMap);
            draftMap.put("count", draftCount);
            draftMap.put("name", "草稿");
            topMapList.add(draftMap);
            //统计已发布,暂停,状态活动
            paramMap.put("statusCd", "(2002, 2006, 2008)");
            int publishedCount = mktCampaignMapper.countByStatus(paramMap);
            Map<String, Object> publishedMap = new HashMap<>();
            publishedMap.put("count", publishedCount);
            publishedMap.put("name", "已发布的活动");
            topMapList.add(publishedMap);
            //统计下线状态活动
            Map<String, Object> rollMap = new HashMap<>();
            paramMap.put("statusCd", "2007");
            int rollCount = mktCampaignMapper.countByStatus(paramMap);
            rollMap.put("count", rollCount);
            rollMap.put("name", "下线活动");
            topMapList.add(rollMap);
            resultMap.put("topMapList", topMapList);

            //将状态-发布中，已暂停，已下线，调整中
            paramMap.put("statusCd", "(2002, 2006, 2007, 2008)");


            // 饼图
            List<Map> secondMapList = new ArrayList<>();
            // 表格
            List<Map> tableMapList = new ArrayList<>();
            // 柱状图
            List<Map> cityList = new ArrayList<>();
            // 营销活动
            paramMap.put("mktCampaignType", "(1000, 2000, 3000, 4000,7000)");
            int marketingCount = mktCampaignMapper.countByStatus(paramMap);
            Map<String, Object> marketingCountMap = new HashMap<>();
            marketingCountMap.put("count", marketingCount);
            marketingCountMap.put("name", "营销活动");
            secondMapList.add(marketingCountMap);
            paramMap.put("lanId", lanId);


            // 查询所有地市
            List<SysArea> sysAreaList = new ArrayList<>();
            if ("".equals(lanId)) {
                Map<String, Object> sysAreaMap = sysAreaService.listCityAndParentByParentId(AreaCodeEnum.ZHEJIAGN.getLanId().intValue());
                SysArea sysAreaPare = (SysArea) sysAreaMap.get("sysAreaList");
                sysAreaList = sysAreaPare.getChildAreaList();
                sysAreaList.add(sysAreaPare);
            }

            typeCount(paramMap, tableMapList, cityList, sysAreaList);

            // 服务活动
            if (startDate != null) {
                paramMap.put("startTime", startDate);
            }
            if (endDate != null) {
                paramMap.put("endDate", endDate);
            }
            paramMap.put("mktCampaignType", "(5000, 6000)");
            paramMap.put("lanId", lanId);
            int serviceCount = mktCampaignMapper.countByStatus(paramMap);
            Map<String, Object> serviceCountMap = new HashMap<>();
            serviceCountMap.put("count", serviceCount);
            serviceCountMap.put("name", "服务活动");
            secondMapList.add(serviceCountMap);
            paramMap.put("lanId", lanId);
            typeCount(paramMap, tableMapList, cityList, sysAreaList);

            // 全部活动
            if (startDate != null) {
                paramMap.put("startTime", startDate);
            }
            if (endDate != null) {
                paramMap.put("endDate", endDate);
            }
            paramMap.put("mktCampaignType", "");
            paramMap.put("lanId", lanId);
            typeCount(paramMap, tableMapList, cityList, sysAreaList);

            resultMap.put("secondMapList", secondMapList);
            resultMap.put("tableMapList", tableMapList);
            resultMap.put("cityList", cityList);
            resultMap.put("resultCode", CommonConstant.CODE_SUCCESS);

        } catch (Exception e) {
            logger.error("失败！Exception = ", e);
            resultMap.put("resultCode", CommonConstant.CODE_FAIL);
            resultMap.put("resultMsg", e);
        }
        return resultMap;
    }


    @Override
    public Result queryDelayCampaignList() {
        Result result = new Result();
        try {
            System.out.println("queryDelayCampaignList111");
            Long loginId = UserUtil.loginId();
            System.out.println("queryDelayCampaignList222");
            List<String> list = new ArrayList<>();
            list.add(STATUS_CODE_PUBLISHED.getStatusCode());
            List<MktCampaignDO> mktCampaignDOS = mktCampaignMapper.selectAllMktCampaignDetailsByStatus(list, loginId);
            System.out.println("queryDelayCampaignList333");
            if (mktCampaignDOS != null && !mktCampaignDOS.isEmpty()) {
                Iterator<MktCampaignDO> iterator = mktCampaignDOS.iterator();
                while (iterator.hasNext()) {
                    MktCampaignDO campaignDO = iterator.next();
                    Date planEndTime = campaignDO.getPlanEndTime();
                    if (planEndTime == null || planEndTime.before(new Date()) || DateUtil.daysBetween(new Date(), planEndTime) > 7) {
                        iterator.remove();
                    }
                }
                System.out.println("queryDelayCampaignList444");
                // 为方便前端显示，后端转化状态为字符串（前端偷懒= =）
                for (MktCampaignDO mktCampaignDO : mktCampaignDOS) {
                    if (STATUS_CODE_PUBLISHED.getStatusCode().equals(mktCampaignDO.getStatusCd())) {
                        mktCampaignDO.setStatusCd(STATUS_CODE_PUBLISHED.getStatusMsg());
                    }
                }
            }
            System.out.println("queryDelayCampaignList555");
            result.setResultCode("200");
            result.setResultMessage("查询成功");
            result.setResultObject(mktCampaignDOS);
            return result;
        } catch (Exception e) {
            System.out.println("queryDelayCampaignList666");
            e.printStackTrace();
            result.setResultCode("500");
            result.setResultMessage(e.toString());
        }
        return result;
    }

    /**
     * 活动延期短信通知
     *
     * @return
     */
    @Override
    public void campaignDelayNotice() {
        Date startDate = new Date();
        ArrayList<String> list = new ArrayList<>();
        list.add(STATUS_CODE_PUBLISHED.getStatusCode());
        List<MktCampaignDO> mktCampaignDOS = mktCampaignMapper.selectAllMktCampaignDetailsByStatus(list, null);
        int i = 0;
        List<Map> sendFailList = new ArrayList();
        for (MktCampaignDO mktCampaignDO : mktCampaignDOS) {
            try {
                if (mktCampaignDO.getPlanEndTime() != null && mktCampaignDO.getPlanEndTime().after(new Date()) && DateUtil.daysBetween(new Date(), mktCampaignDO.getPlanEndTime()) == 7) {
                    Long staff = mktCampaignDO.getCreateStaff();
                    SysmgrResultObject<SystemUserDto> systemUserDtoSysmgrResultObject = iSystemUserDtoDubboService.qrySystemUserDto(staff, new ArrayList<Long>());
                    if (systemUserDtoSysmgrResultObject != null && systemUserDtoSysmgrResultObject.getResultObject() != null) {
                        String sysUserCode = systemUserDtoSysmgrResultObject.getResultObject().getSysUserCode();
                        Long lanId = mktCampaignDO.getLanId();
                        // TODO  调用发送短信接口
                        String sendContent = "您好，您创建的活动（" + mktCampaignDO.getMktCampaignName() + "）即将到期，且活动不可再延期，到期后即失效。如需继续活动内容，请新建活动！";
                        logger.info(sendContent);
                        if (lanId != null && lanId != 1) {
                            String resultMsg = uccpService.sendShortMessage(sysUserCode, sendContent, lanId.toString(), DttsMsgEnum.DELAY_NOTICE_MESSAGE.getId());
                            if (!resultMsg.isEmpty()) {
                                Map map = new HashMap();
                                map.put("campaignId", mktCampaignDO.getMktCampaignId());
                                map.put("resuleMsg", resultMsg);
                                sendFailList.add(map);
                            }
                            i++;
                        }
                    }
                }
            } catch (Exception e) {
                Map map = new HashMap();
                map.put("campaignId", mktCampaignDO.getMktCampaignId());
                map.put("resuleMsg", e.toString().substring(500));
                sendFailList.add(map);
                e.printStackTrace();
            }
        }
        logger.info("共发送数量=>" + i + ",发送失败活动：" + JSON.toJSONString(sendFailList));
        mktDttsLogService.saveMktDttsLog("6000", "成功", startDate, new Date(), "成功", JSON.toJSONString(sendFailList));
    }


    // 表格中的类型统计
    private Map<String, Object> typeCount(Map<String, Object> paramMap, List<Map> tableMapList, List<Map> cityList, List<SysArea> sysAreaList) throws Exception {
        DecimalFormat df = new DecimalFormat("0.00");
        // 随销活动（实时营销活动）
        paramMap.put("tiggerType", StatusCode.REAL_TIME_CAMPAIGN.getStatusCode());
        int realTimeCount = mktCampaignMapper.countByStatus(paramMap);
        Map<String, Object> realTimeMap = new HashMap<>();
        realTimeMap.put("count", realTimeCount);
        realTimeMap.put("name", "随销活动");

        // 派单活动（批量营销活动）
        paramMap.put("tiggerType", StatusCode.BATCH_CAMPAIGN.getStatusCode());
        int batchCount = mktCampaignMapper.countByStatus(paramMap);
        Map<String, Object> batchMap = new HashMap<>();
        batchMap.put("count", batchCount);
        batchMap.put("name", "派单活动");

        Map<String, Object> trilParamMap = new HashMap<>();
        trilParamMap.putAll(paramMap);
        List<Map<String, Object>> trilMapList = new ArrayList<>();
        int labelListTotal = 0;
        // 标签取数
        trilParamMap.put("trilType", "2000"); // 试算类型
        int labelCount = mktCampaignMapper.countByTrial(trilParamMap);
        Map<String, Object> trilMap = new HashMap<>();
        trilMap.put("name", "标签取数");
        trilMap.put("count", labelCount);
        labelListTotal += labelCount;
        // 清单导入
        trilParamMap.put("trilType", "1000");// 试算类型
        int listCount = mktCampaignMapper.countByTrial(trilParamMap);
        Map<String, Object> listMap = new HashMap<>();
        listMap.put("name", "清单导入");
        listMap.put("count", listCount);
        labelListTotal += listCount;
        if (labelListTotal != 0) {
            trilMap.put("percent", df.format(labelCount * 100.0 / labelListTotal) + "%");
            listMap.put("percent", df.format(listCount * 100.0 / labelListTotal) + "%");
        } else {
            trilMap.put("percent", "0.00%");
            listMap.put("percent", "0.00%");
        }
        trilMapList.add(trilMap);
        trilMapList.add(listMap);
        batchMap.put("labelList", trilMapList);


        // 混合活动（混合营销活动）
        paramMap.put("tiggerType", StatusCode.MIXTURE_CAMPAIGN.getStatusCode());
        int mixtureCount = mktCampaignMapper.countByStatus(paramMap);
        Map<String, Object> mixtureMap = new HashMap<>();
        mixtureMap.put("count", mixtureCount);
        mixtureMap.put("name", "混合活动");

        int tableTotal = realTimeCount + batchCount + mixtureCount;
        Map<String, Object> totalMap = new HashMap<>();
        totalMap.put("count", tableTotal);
        totalMap.put("name", "总量");
        if (tableTotal != 0) {
            totalMap.put("percent", df.format(tableTotal * 100.0 / tableTotal) + "%");
            realTimeMap.put("percent", df.format(realTimeCount * 100.0 / tableTotal) + "%");
            batchMap.put("percent", df.format(batchCount * 100.0 / tableTotal) + "%");
            mixtureMap.put("percent", df.format(mixtureCount * 100.0 / tableTotal) + " %");
        } else {
            realTimeMap.put("percent", "0.00%");
            batchMap.put("percent", "0.00%");
            mixtureMap.put("percent", "0.00%");
        }
        List<Map<String, Object>> tableDateList = new ArrayList<>();
        tableDateList.add(totalMap);
        tableDateList.add(realTimeMap);
        tableDateList.add(batchMap);
        tableDateList.add(mixtureMap);


        paramMap.put("tiggerType", "");
        String lanId = paramMap.get("lanId").toString();
        List<Future<Map<String, Object>>> futureList = new ArrayList<>();
        Map<String, Object> cityCountMap = new HashMap<>();
        // 省查所有地市
        if ("".equals(lanId)) {
            // 起线程去统计11个地市的数据 + 省级
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (SysArea sysArea : sysAreaList) {
                Future<Map<String, Object>> futureMap = executorService.submit(new cityCountTask(paramMap, sysArea.getAreaId(), sysArea.getName()));
                futureList.add(futureMap);
            }
            executorService.shutdown();
        } else {
            cityCountMap = cityLineCount(paramMap, Integer.valueOf(lanId), AreaNameEnum.getNameByLandId(Long.valueOf(lanId)));
        }

        List<Map<String, Object>> cityDataList = new ArrayList<>();
        List<Map<String, Object>> cityMapList = new ArrayList<>();

        int total = 0;
        if (futureList.size() > 0) {
            for (Future<Map<String, Object>> future : futureList) {
                if (future.get() != null && !future.get().isEmpty()) {
                    cityMapList.add(future.get());
                    total += (int) future.get().get("count");
                }
            }

            // 统计百分比
            if (total != 0) {
                for (Map<String, Object> cityMap : cityMapList) {
                    cityMap.put("percent", df.format((int) cityMap.get("count") * 100.0 / total) + "%");
                }
            }
            //排序
          /*  Collections.sort(cityMapList, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Integer count1 = (Integer) o1.get("count");
                    Integer count2 = (Integer) o2.get("count");
                    return count2.compareTo(count1);
                }
            });*/
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.ZHEJIAGN.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.HAGNZHOU.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.NINGBO.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.WENZHOU.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.TAIZHOU.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.SHAOXING.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.JINHUA.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.JIAXING.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.HUZHOU.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.QUZHOU.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.LISHUI.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }
            for (Map<String, Object> cityMap : cityMapList) {
                Long areaId = Long.valueOf((Integer) cityMap.get("lanId"));
                if (AreaCodeEnum.ZHOUSHAN.getLanId().equals(areaId)) {
                    cityDataList.add(cityMap);
                    cityMapList.remove(cityMap);
                    break;
                }
            }

        } else {
            cityDataList.add(cityCountMap);
        }


        Map<String, Object> cityDataMap = new HashMap<>();
        Map<String, Object> tableDataMap = new HashMap<>();
        if ("(1000, 2000, 3000, 4000)".equals(paramMap.get("mktCampaignType").toString())) {
            cityDataMap.put("name", "营销活动");
            cityDataMap.put("cityDataList", cityDataList);
            tableDataMap.put("name", "营销活动");
            tableDataMap.put("tableDateList", tableDateList);
        } else if ("(5000, 6000)".equals(paramMap.get("mktCampaignType").toString())) {
            cityDataMap.put("name", "服务活动");
            cityDataMap.put("cityDataList", cityDataList);
            tableDataMap.put("name", "服务活动");
            tableDataMap.put("tableDateList", tableDateList);
        } else {
            cityDataMap.put("name", "全部活动");
            cityDataMap.put("cityDataList", cityDataList);
            tableDataMap.put("name", "全部活动");
            tableDataMap.put("tableDateList", tableDateList);
        }
        tableMapList.add(tableDataMap);
        cityList.add(cityDataMap);
        return cityDataMap;
    }


    // 柱状图统计
    class cityCountTask implements Callable<Map<String, Object>> {
        private Map<String, Object> paramMap;
        private Integer areaId;
        private String name;


        public cityCountTask(Map<String, Object> paramMap, Integer areaId, String name) {
            this.paramMap = paramMap;
            this.areaId = areaId;
            this.name = name;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> cityParamMap = new ConcurrentHashMap<>();
            cityParamMap.putAll(paramMap);
            cityParamMap.put("lanId", areaId);
            int cityCount = mktCampaignMapper.countByStatus(cityParamMap);
            Map<String, Object> cityCountMap = new ConcurrentHashMap<>();
            cityCountMap.put("count", cityCount);
            cityCountMap.put("lanId", areaId);
            cityCountMap.put("name", name);
            Date startDate = (Date) paramMap.get("startDate");
            Date endDate = (Date) paramMap.get("endDate");

            // c4级别
            if (areaId != null && areaId != 1) {
                List<Map<String, Object>> cityC4MapList = new ArrayList<>();
                Map<String, Object> sysC4AreaMap = sysAreaService.listCityByParentId(areaId);
                List<SysArea> sysAreaC4List = (List<SysArea>) sysC4AreaMap.get("sysAreaList");
                List<Future<Map<String, Object>>> futureList = new ArrayList<>();
                ExecutorService executorService = Executors.newCachedThreadPool();
                for (SysArea sysArea : sysAreaC4List) {
                    Future<Map<String, Object>> futureMap = executorService.submit(new CountC4Task(paramMap, startDate, endDate, areaId.longValue(), sysArea.getAreaId().longValue(), sysArea.getName()));
                    futureList.add(futureMap);
                }
                executorService.shutdown();

                for (Future<Map<String, Object>> future : futureList) {
                    if (future != null && future.get() != null) {
                        cityC4MapList.add(future.get());
                    }
                }
                cityCountMap.put("cityFourList", cityC4MapList);
            }
            return cityCountMap;
        }
    }

    /**
     * 近6个月的折线图
     *
     * @param paramMap
     * @param areaId
     * @param name
     * @return
     * @throws Exception
     */
    private Map<String, Object> cityLineCount(Map<String, Object> paramMap, Integer areaId, String name) throws Exception {
        Map<String, Object> lineResultMap = new HashMap<>();
        Map<Integer, Date> dateMap = new HashedMap();
        dateMap.put(6, string2DateTime4Day(getCurMFirstDay()));// 获取本月的第一天
        // 获取6个月的
        for (int i = 6; i > 0; i--) {
            dateMap.put(i - 1, string2DateTime4Day(getFisrtDayOfMonth(dateMap.get(i))));
        }
        try {
            ExecutorService cityExecutorService = Executors.newCachedThreadPool();
            List<Future<Map<String, Object>>> futureList = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                Future<Map<String, Object>> futureMap = cityExecutorService.submit(new cityMonthTask(paramMap, areaId, dateMap.get(i), dateMap.get(i + 1)));
                futureList.add(futureMap);
            }
            List<Map<String, Object>> lineMapList = new ArrayList<>();
            for (Future<Map<String, Object>> future : futureList) {
                lineMapList.add(future.get());
            }
            lineResultMap.put("areaId", areaId);
            lineResultMap.put("name", name);
            lineResultMap.put("data", lineMapList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lineResultMap;
    }

    class cityMonthTask implements Callable<Map<String, Object>> {
        private Map<String, Object> paramMap;
        private Integer areaId;
        private Date startDate;
        private Date endDate;

        public cityMonthTask(Map<String, Object> paramMap, Integer areaId, Date startDate, Date endDate) {
            this.paramMap = paramMap;
            this.areaId = areaId;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> cityMonthMap = new HashMap<>();
            cityMonthMap.putAll(paramMap);
            cityMonthMap.put("startTime", startDate);
            cityMonthMap.put("endTime", endDate);
            int cityCount = mktCampaignMapper.countByStatus(cityMonthMap);
            Calendar calendar = Calendar.getInstance();
            //设置日期
            calendar.setTime((Date) cityMonthMap.get("startTime"));
            //获取年份
            int year = calendar.get(Calendar.YEAR);
            //获取上个月份
            int month = calendar.get(Calendar.MONTH) + 1;
            Map<String, Object> lineMap = new HashMap<>();
            lineMap.put("count", cityCount);
            lineMap.put("month", year + "年" + month + "月");


            // c4级别
            if (areaId != null && areaId != 1) {
                List<Map<String, Object>> cityC4MapList = new ArrayList<>();
                Map<String, Object> sysC4AreaMap = sysAreaService.listCityByParentId(areaId);
                List<SysArea> sysAreaC4List = (List<SysArea>) sysC4AreaMap.get("sysAreaList");
                List<Future<Map<String, Object>>> futureC4List = new ArrayList<>();
                ExecutorService executorService = Executors.newCachedThreadPool();
                for (SysArea sysArea : sysAreaC4List) {
                    Future<Map<String, Object>> futureMap = executorService.submit(new CountC4Task(paramMap, startDate, endDate, areaId.longValue(), sysArea.getAreaId().longValue(), sysArea.getName()));
                    futureC4List.add(futureMap);
                }
                executorService.shutdown();

                for (Future<Map<String, Object>> futureC4 : futureC4List) {
                    if (futureC4 != null && futureC4.get() != null) {
                        cityC4MapList.add(futureC4.get());
                    }
                }
                lineMap.put("cityFourList", cityC4MapList);
            }


            return lineMap;
        }
    }

    /**
     * 获取C4数据
     *
     * @return
     */
    class CountC4Task implements Callable<Map<String, Object>> {

        private Map<String, Object> paramMap;
        private Date startDate;
        private Date endDate;
        private Long LanId;
        private Long landIdFour;
        private String landIdFourName;

        public CountC4Task(Map<String, Object> paramMap, Date startDate, Date endDate, Long lanId, Long landIdFour, String landIdFourName) {
            this.paramMap = paramMap;
            this.startDate = startDate;
            this.endDate = endDate;
            LanId = lanId;
            this.landIdFour = landIdFour;
            this.landIdFourName = landIdFourName;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> c4ParamMap = new HashMap<>();
            c4ParamMap.putAll(paramMap);
            c4ParamMap.put("lanId", LanId);
            c4ParamMap.put("lanIdFour", landIdFour);
            c4ParamMap.put("startTime", startDate);
            c4ParamMap.put("endTime", endDate);
            int c4Count = mktCampaignMapper.countBylanIdFour(c4ParamMap);

            Map<String, Object> c4ResultMap = new HashMap<>();
            c4ResultMap.put("count", c4Count);
            c4ResultMap.put("lanId", landIdFour);
            c4ResultMap.put("name", landIdFourName);
            return c4ResultMap;
        }
    }


    // 获取c4，c5的数据
    private Map<String, Object> getLandFourAndFive() {
        Map<String, Object> resutlMap = new HashMap<>();
//        SystemUserDto user = BssSessionHelp.getSystemUserDto();
//        Long staffId = user.getStaffId();、
        Long staffId = 1L;
        logger.info("staffId ============" + staffId);
        Long orgId = null;
        List<Map<String, Object>> staffOrgId = organizationMapper.getStaffOrgId(staffId);
        if (!staffOrgId.isEmpty() && staffOrgId.size() > 0) {
            for (Map<String, Object> map : staffOrgId) {
                Object orgDivision = map.get("orgDivision");
                Object orgId1 = map.get("orgId");
                if (orgDivision != null) {
                    if (orgDivision.toString().equals("30")) {
                        orgId = Long.valueOf(orgId1.toString());
                        break;
                    } else if (orgDivision.toString().equals("20")) {
                        orgId = Long.valueOf(orgId1.toString());
                        break;
                    } else if (orgDivision.toString().equals("10")) {
                        orgId = Long.valueOf(orgId1.toString());
                        break;
                    }
                }
            }
        }

        if (orgId != null) {
            Organization organization = organizationMapper.selectByPrimaryKey(orgId);
            if (organization != null) {
                if (organization.getOrgNameC4() != null) {
                    Organization organizationC4 = organizationMapper.selectByPrimaryKey(Long.valueOf(organization.getOrgNameC4()));
                    Long regionIdC4 = organizationC4.getRegionId();
                    if (regionIdC4 != null) {
                        SysArea byCityFour = sysAreaMapper.getByCityFour(regionIdC4.toString());
                        if (byCityFour != null && byCityFour.getAreaId() != null) {
                            resutlMap.put("C4", Long.valueOf(byCityFour.getAreaId()));
                        }
                        if (StringUtils.isNotBlank(organization.getOrgNameC5())) {
                            resutlMap.put("C5", Long.valueOf(organization.getOrgNameC5()));
                        }
                    }
                }
            }
        }
//        resutlMap.put("C4", Long.valueOf("57001"));
//        resutlMap.put("C5", Long.valueOf("800000002004"));
        return resutlMap;
    }

    //集团活动不承接反馈接口
    @Override
    public Map<String, Object> mktCampaignJtRefuse(Long mktCampaignId) {
        Map<String, Object> resultMap = new HashMap<>();
        MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCampaignId);
        List<MktCampaignComplete> mktCampaignCompleteList = mktCampaignCompleteMapper.selectByCampaignId(mktCampaignId);
        if (mktCampaignCompleteList.isEmpty()) {
            resultMap.put("resultCode", CODE_FAIL);
            resultMap.put("resultMsg", "此活动不是集团活动");
            return resultMap;
        }
        for (MktCampaignComplete mktCampaignComplete : mktCampaignCompleteList) {
            if (mktCampaignComplete.getTacheCd().equals("1100") && mktCampaignComplete.getTacheValueCd().equals("10")) {
                mktCampaignComplete.setTacheValueCd("11");
                mktCampaignComplete.setStatusCd("1200");
                mktCampaignComplete.setEndTime(new Date());
                mktCampaignComplete.setUpdateDate(new Date());
                mktCampaignComplete.setUpdateStaff(UserUtil.loginId());
                mktCampaignCompleteMapper.update(mktCampaignComplete);
                try {
                    openCompleteMktCampaignService.completeMktCampaign(mktCampaignDO.getInitId(), "1100");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        resultMap.put("resultCode", CODE_SUCCESS);
        resultMap.put("resultMsg", "反馈完成");
        return resultMap;
    }


    /**
     * 通过活动编码集合批量查询
     *
     * @param mktCampaignIdList
     * @return
     */
    @Override
    public Map<String, Object> searchBatch(List<Long> mktCampaignIdList) {
        Map<String, Object> resultMap = new HashMap<>();
        List<MktCampaignDO> mktCampaignList = mktCampaignMapper.selectBatch(mktCampaignIdList);
        resultMap.put("mktCampaignList", mktCampaignList);
        resultMap.put("resultCode", CODE_SUCCESS);
        resultMap.put("resultMsg", "查询成功");
        return resultMap;
    }


    @Autowired
    private MktCamChlConfMapper camChlConfMapper;

    /**
     * 刷活动数据
     *
     * @return
     */
    @Override
    public Map<String, Object> dataConfig(Map<String, String> map) {
        List<MktCampaignDO> campaignDOList = mktCampaignMapper.selectAll();
        System.out.println("【campaignDOList】:" + campaignDOList.size());
        if (map.get("all") != null) {
            for (MktCampaignDO cam : campaignDOList) {
                List<MktCamChlConfDO> list = camChlConfMapper.selectByCampaignId(cam.getMktCampaignId());
                if (list.size() > 1) {
                    cam.setOneChannelFlg("false");
                } else {
                    cam.setOneChannelFlg("true");
                }
                String creatChannel = cam.getCreateChannel() == null ? "" : cam.getCreateChannel();
                String sysPostCode = "";
                if (creatChannel.equals(AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysPostCode())) {
                    sysPostCode = "C2";
                } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.SHENGJI.getSysPostCode())) {
                    sysPostCode = AreaCodeEnum.sysAreaCode.SHENGJI.getSysArea();
                } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.FENGONGSI.getSysPostCode())) {
                    sysPostCode = AreaCodeEnum.sysAreaCode.FENGONGSI.getSysArea();
                } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.FENGJU.getSysPostCode())) {
                    sysPostCode = AreaCodeEnum.sysAreaCode.FENGJU.getSysArea();
                } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.ZHIJU.getSysPostCode())) {
                    sysPostCode = AreaCodeEnum.sysAreaCode.ZHIJU.getSysArea();
                } else {
                    sysPostCode = AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysArea();
                }
                cam.setRegionFlg(sysPostCode);
                mktCampaignMapper.updateByPrimaryKey(cam);
            }
        }

        List<String> list = ChannelUtil.StringToList(map.get("String"));
        for (String id : list) {
            MktCampaignDO campaignDO = mktCampaignMapper.selectByPrimaryKey(Long.valueOf(id));
            if (campaignDO != null) {
                campaignDO.setTheMe(map.get("theme"));
                mktCampaignMapper.updateByPrimaryKey(campaignDO);
            }
        }
        Map<String, Object> RES = new HashMap<>();
        RES.put("success", "success");
        return RES;
    }


    /**
     * 刷活动数据
     *
     * @return
     */
    @Override
    public Map<String, Object> campaignConfig(Long mktCampaignId) {
        MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCampaignId);
        List<MktCamChlConfDO> list = camChlConfMapper.selectByCampaignId(mktCampaignId);
        if (list.size() > 1) {
            mktCampaignDO.setOneChannelFlg("false");
        } else {
            mktCampaignDO.setOneChannelFlg("true");
        }
        String creatChannel = mktCampaignDO.getCreateChannel() == null ? "" : mktCampaignDO.getCreateChannel();
        String sysPostCode = "";
        if (creatChannel.equals(AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysPostCode())) {
            sysPostCode = AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysArea();
        } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.SHENGJI.getSysPostCode())) {
            sysPostCode = AreaCodeEnum.sysAreaCode.SHENGJI.getSysArea();
        } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.FENGONGSI.getSysPostCode())) {
            sysPostCode = AreaCodeEnum.sysAreaCode.FENGONGSI.getSysArea();
        } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.FENGJU.getSysPostCode())) {
            sysPostCode = AreaCodeEnum.sysAreaCode.FENGJU.getSysArea();
        } else if (creatChannel.equals(AreaCodeEnum.sysAreaCode.ZHIJU.getSysPostCode())) {
            sysPostCode = AreaCodeEnum.sysAreaCode.ZHIJU.getSysArea();
        } else {
            sysPostCode = AreaCodeEnum.sysAreaCode.CHAOGUAN.getSysArea();
        }
        mktCampaignDO.setRegionFlg(sysPostCode);
        mktCampaignMapper.updateByPrimaryKey(mktCampaignDO);

        Map<String, Object> RES = new HashMap<>();
        RES.put("success", "success");
        return RES;
    }

    /**
     * 活动描述为空补全活动名称为活动描述 防止老活动试算或清单异常
     *
     * @return
     */
    @Override
    public Map<String, Object> saveMktCamDesc() {
        List<MktCampaignDO> mktCampaignDOS = mktCampaignMapper.selectAll();
        for (int i = 0; i < mktCampaignDOS.size(); i++) {
            if (mktCampaignDOS.get(i).getMktCampaignDesc().isEmpty()) {
                mktCampaignMapper.saveMktCamDesc(mktCampaignDOS.get(i));
            }
        }
        return null;
    }

    /**
     * 提供接口入参C3 返回配置了自动派单（auotTrail=1）得已发布活动（2002,2008）
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> getByC3AndAuto(Map<String, Object> params) {
        Long c3 = null;
        if (params.get("c3") != null && !"".equals(params.get("c3"))) {
            c3 = Long.valueOf(params.get("c3").toString());
        }
        String mktCampaignName = (String) params.get("mktCampaignName");
        Integer page = (Integer) params.get("page");
        Integer pageSize = (Integer) params.get("pageSize");
        List<MktCampaignDO> mktCampaignList = null;
        Map<String, Object> resultMap = new HashMap<>();
        try {
            PageHelper.startPage(page, pageSize);
            mktCampaignList = mktCampaignMapper.getByC3AndAuto(c3, mktCampaignName);
            resultMap.put("mktCampaignList", mktCampaignList);
            resultMap.put("pageInfo", new Page(new PageInfo(mktCampaignList)));
            resultMap.put("resultCode", CODE_SUCCESS);
            resultMap.put("resultMsg", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("getByC3AndAuto -- >" + e);
            resultMap.put("mktCampaignList", mktCampaignList);
            resultMap.put("resultCode", CODE_FAIL);
            resultMap.put("resultMsg", "查询失败");
        }
        return resultMap;
    }

    @Override
    public boolean isOpenDisturb(MktCampaignDO mktCampaignDO) {
        String triggerType = "1000";//批量活动
        logger.info(mktCampaignDO.getLanId().toString());
        Long lanId = AreaCodeEnum.getLandIdByRegionId(mktCampaignDO.getLanId());
        List<MktCampaignDO> mktCampaignDOList = mktCampaignMapper.listDisturbedCampaignForlanId(lanId, triggerType);
        if (mktCampaignDOList.size() > 5) {
            return false;
        } else {
            return true;
        }
    }

    private Map<String, Object> updateProjectStateTime(Long initId) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 根据initId查询所有的活动
            List<MktCampaignDO> mktCampaignDOList = mktCampaignMapper.selectCampaignByInitId(initId);
            for (MktCampaignDO mktCampaignDO : mktCampaignDOList) {
                List<TrialOperation> trialOperationList = trialOperationMapper.listOperationByCamIdAndStatusCd2(mktCampaignDO.getMktCampaignId(), "(7300, 8100)");
                for (TrialOperation trialOperation : trialOperationList) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("id", trialOperation.getId() == null ? 0 : trialOperation.getId().intValue());  // 试运算Id
                    params.put("effectDate", DateUtil.date2StringDate(new Date()));  // 生效时间
                    params.put("invalidDate", DateUtil.date2StringDate(new Date())); // 失效时间
                    projectManageService.updateProjectStateTime(params);
                }
                projectManageService.updateProjectPcState(mktCampaignDO.getMktCampaignId());
            }
            resultMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("resultCode", CommonConstant.CODE_FAIL);
            return resultMap;
        }
    }

    @Override
    public void redisTest(String key, Long id) {
        //MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(9L);
        //redisUtils.set("test", mktCampaignDO);
        // logger.info("存入缓存");
        Map<String, Object> redis = eventRedisService.getRedis(key, id);
        System.out.println("result ->" + JSON.toJSONString(redis));
    }

    @Override
    public Map<String, Object> updateStaffById(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 修改活动信息
            MktCampaignDO mktCampaignDO = new MktCampaignDO();
            Long mktCampaignId = Long.valueOf(params.get("mktCampaignId").toString());
            mktCampaignDO.setMktCampaignId(mktCampaignId);
            mktCampaignDO.setCreateStaff(Long.valueOf(params.get("sysUserId").toString()));
            mktCampaignDO.setUpdateDate(new Date());
            String contName = (String) params.get("name");
            String tel = (String) params.get("tel");
            String department = (String) params.get("department");
            Long staffId = Long.valueOf(params.get("staffId").toString());
            logger.info("mktCampaignDO --->>>" + JSON.toJSONString(mktCampaignDO));
            mktCampaignMapper.updateStaffById(mktCampaignDO);
            // 修改需求函
            List<RequestInstRel> requestInstRelList = requestInstRelMapper.selectByCampaignId(mktCampaignId, "mkt");
            for (RequestInstRel requestInstRel : requestInstRelList) {
                RequestInfo requestInfo = requestInfoMapper.selectByPrimaryKey(requestInstRel.getRequestInfoId());
                requestInfo.setContName(contName);
                requestInfo.setContTele(tel);
                requestInfo.setDeptCode(department);
                requestInfo.setCreateStaff(staffId);
                requestInfo.setUpdateDate(new Date());
                logger.info("requestInfo --->>>" + JSON.toJSONString(requestInfo));
                requestInfoMapper.updateByPrimaryKey(requestInfo);
            }
            resultMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            resultMap.put("resultMsg", "成功");
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("resultCode", CommonConstant.CODE_FAIL);
            resultMap.put("resultMsg", "失败");
            return resultMap;
        }
    }

    @Override
    public Map<String, Object> getStaffByMktRequest(Map<String, Object> paramMap) {

        Map<String, Object> resultMap = new HashMap<>();
        String requestType = (String) paramMap.get("requestType");
        String nodeId = (String) paramMap.get("nodeId");
        Integer mktCamId = (Integer) paramMap.get("mktCamId");
        Map<String, Object> dataMap = new HashMap<>();
        logger.info("需求函类型获取审批员工：" + requestType);
        logger.info("需求函类型获取审批员工：" + nodeId);
        logger.info("需求函类型获取审批员工：" + mktCamId);
        try {

            MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCamId.longValue());
            logger.info(mktCampaignDO.getDirectoryId() + "需求函类型获取审批员工");
            logger.info(mktCampaignDO.getLanId() + "需求函类型获取审批员工");
            //12是外场营销目录
            if (mktCampaignDO.getDirectoryId() == 12 && mktCampaignDO.getLanId() == 571) {
                MktRequestDO mktRequestDO = mktRequestMapper.getRequestInfoByMktId(requestType, nodeId, mktCamId.longValue());
                logger.info("需求函类型获取审批员工：" + mktRequestDO);
                dataMap.put("requestId", mktRequestDO.getRequestId());
                dataMap.put("requestType", mktRequestDO.getRequestType());
                dataMap.put("nodeId", mktRequestDO.getNodeId());
                dataMap.put("catelogId", mktRequestDO.getCatelogId());
                dataMap.put("lanId", mktRequestDO.getLanId());
                String staffjson = mktRequestDO.getStaff();
                JSONArray objects = JSONObject.parseArray(staffjson);
                List<StaffDO> staffList = new ArrayList();
                for (int i = 0; i < objects.size(); i++) {
                    //通过数组下标取到object，使用强转转为JSONObject，之后进行操作
                    JSONObject object = (JSONObject) objects.get(i);
                    String name = object.getString("name");
                    String staffId = object.getString("staffid");
                    logger.info(name + "需求函类型获取审批员工" + staffId);
                    StaffDO staffDO = new StaffDO();
                    staffDO.setName(name);
                    staffDO.setStaffid(staffId);
                    staffList.add(staffDO);
                }
                dataMap.put("staff", staffList);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("resultCode", CODE_FAIL);
            resultMap.put("resultMessage", "消息返回异常");
            return resultMap;
        }
        resultMap.put("resultCode", CODE_SUCCESS);
        resultMap.put("resultMessage", "消息返回成功");
        resultMap.put("data", dataMap);
        return resultMap;
    }


}