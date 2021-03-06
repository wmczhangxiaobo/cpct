/**
 * @(#)MktStrategyConfServiceImpl.java, 2018/6/25.
 * <p/>
 * Copyright 2018 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.zjtelcom.cpct.service.impl.strategy;

import com.alibaba.fastjson.JSON;
import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.campaign.*;
import com.zjtelcom.cpct.dao.channel.MktCamResourceMapper;
import com.zjtelcom.cpct.dao.channel.MktProductAttrMapper;
import com.zjtelcom.cpct.dao.channel.OrganizationMapper;
import com.zjtelcom.cpct.dao.grouping.TarGrpConditionMapper;
import com.zjtelcom.cpct.dao.grouping.TarGrpMapper;
import com.zjtelcom.cpct.dao.grouping.TrialOperationMapper;
import com.zjtelcom.cpct.dao.strategy.MktCamStrategyRelMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfRuleMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfRuleRelMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyMapper;
import com.zjtelcom.cpct.domain.User;
import com.zjtelcom.cpct.domain.campaign.*;
import com.zjtelcom.cpct.domain.channel.CamScript;
import com.zjtelcom.cpct.domain.channel.Channel;
import com.zjtelcom.cpct.domain.channel.MktCamResource;
import com.zjtelcom.cpct.domain.channel.Organization;
import com.zjtelcom.cpct.domain.grouping.TrialOperation;
import com.zjtelcom.cpct.domain.org.OrgTreeDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfRuleDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfRuleRelDO;
import com.zjtelcom.cpct.dto.campaign.MktCamChlConfDetail;
import com.zjtelcom.cpct.dto.campaign.MktCamChlResult;
import com.zjtelcom.cpct.dto.channel.CamScriptAddVO;
import com.zjtelcom.cpct.dto.channel.MktCamResourceVO;
import com.zjtelcom.cpct.dto.channel.MktResource4RuleVO;
import com.zjtelcom.cpct.dto.grouping.TarGrp;
import com.zjtelcom.cpct.dto.grouping.TarGrpCondition;
import com.zjtelcom.cpct.dto.grouping.TarGrpDetail;
import com.zjtelcom.cpct.dto.strategy.MktStrategy;
import com.zjtelcom.cpct.dto.strategy.MktStrategyConfRule;
import com.zjtelcom.cpct.enums.*;
import com.zjtelcom.cpct.pojo.MktCamStrategyRel;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.campaign.MktCamChlConfService;
import com.zjtelcom.cpct.service.campaign.MktCamChlResultService;
import com.zjtelcom.cpct.service.channel.CamScriptService;
import com.zjtelcom.cpct.service.channel.ProductService;
import com.zjtelcom.cpct.service.grouping.TarGrpService;
import com.zjtelcom.cpct.service.strategy.MktStrategyConfRuleService;
import com.zjtelcom.cpct.util.*;
import javafx.application.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Description:
 * author: linchao
 * date: 2018/06/25 17:23
 * version: V1.0
 */
@Transactional
@Service
public class MktStrategyConfRuleServiceImpl extends BaseService implements MktStrategyConfRuleService {

    @Autowired
    private MktStrategyConfRuleMapper mktStrategyConfRuleMapper;

    @Autowired
    private MktStrategyConfRuleRelMapper mktStrategyConfRuleRelMapper;

    @Autowired
    private MktCpcAlgorithmsRulMapper mktCpcAlgorithmsRulMapper; //cpc算法规则表

    @Autowired
    private MktCamChlConfMapper mktCamChlConfMapper;

    @Autowired
    private MktCamChlResultService mktCamChlResultService;

    @Autowired
    private MktCamChlConfService mktCamChlConfService;

    @Autowired
    private TarGrpService tarGrpService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MktCamResultRelMapper mktCamResultRelMapper;

    @Autowired
    private CamScriptService camScriptService;

    @Autowired
    private MktCamRecomCalcRelMapper mktCamRecomCalcRelMapper;  //cpc算法与活动关联表

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private TarGrpMapper tarGrpMapper;
    @Autowired
    private TarGrpConditionMapper tarGrpConditionMapper;

    @Autowired
    private MktCamItemMapper mktCamItemMapper;

    @Autowired
    private MktCamChlResultConfRelMapper mktCamChlResultConfRelMapper;

    @Autowired
    private MktStrategyMapper mktStrategyMapper;

    @Autowired
    private MktCamStrategyRelMapper mktCamStrategyRelMapper;

    @Autowired
    private MktCamGrpRulMapper mktCamGrpRulMapper;

    @Autowired
    private TrialOperationMapper trialOperationMapper;

    @Autowired
    private MktCampaignMapper campaignMapper;
    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private MktCamChlConfAttrMapper mktCamChlConfAttrMapper;
    @Autowired
    private MktCamResourceMapper mktCamResourceMapper;
    @Autowired
    private MktProductAttrMapper productAttrMapper;

    /**
     * 添加策略规则
     *
     * @param mktStrategyConfRule
     * @return
     */
    @Override
    public Map<String, Object> saveMktStrategyConfRule(MktStrategyConfRule mktStrategyConfRule) {
        Map<String, Object> mktStrategyConfRuleMap = new HashMap<>();
        MktStrategyConfRuleDO mktStrategyConfRuleDO = new MktStrategyConfRuleDO();
        try {
            CopyPropertiesUtil.copyBean2Bean(mktStrategyConfRuleDO, mktStrategyConfRule);
            String productIds = "";
            String evtContactConfIds = "";
            if (mktStrategyConfRule.getProductIdlist() != null) {
                for (int i = 0; i < mktStrategyConfRule.getProductIdlist().size(); i++) {
                    if (i == 0) {
                        productIds += mktStrategyConfRule.getProductIdlist().get(i);
                    } else {
                        productIds += "/" + mktStrategyConfRule.getProductIdlist().get(i);
                    }
                    // 给mkt_cam_item表添加活动id
                    MktCamItem mktCamItem = mktCamItemMapper.selectByPrimaryKey(mktStrategyConfRule.getProductIdlist().get(i));
                    mktCamItem.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                    mktCamItemMapper.updateByPrimaryKey(mktCamItem);
                }
                mktStrategyConfRuleDO.setProductId(productIds);
            }
            String confs = "";
            if (mktStrategyConfRule.getMktCamChlConfDetailList() != null) {
                for (int i = 0; i < mktStrategyConfRule.getMktCamChlConfDetailList().size(); i++) {
                    Long evtContactConfId =  mktStrategyConfRule.getMktCamChlConfDetailList().get(i).getEvtContactConfId();
                    if (i == 0) {
                        evtContactConfIds += evtContactConfId;
                        confs += evtContactConfId;
                    } else {
                        evtContactConfIds += "/" + evtContactConfId;
                        confs += "||" + evtContactConfId;
                    }

                    //TODO 添加推送渠道添加活动标识
                    MktCamChlConfDO mktCamChlConfDO = mktCamChlConfMapper.selectByPrimaryKey(evtContactConfId);
                    mktCamChlConfDO.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                    mktCamChlConfMapper.updateByPrimaryKey(mktCamChlConfDO);
                    //redisUtils.del("CHL_CONF_DETAIL_" + evtContactConfId);

                    // 保存话术
                    CamScriptAddVO camScriptAddVO = new CamScriptAddVO();
                    camScriptAddVO.setEvtContactConfId(mktStrategyConfRule.getMktCamChlConfDetailList().get(i).getEvtContactConfId());
                    camScriptAddVO.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                    camScriptAddVO.setScriptDesc(mktStrategyConfRule.getMktCamChlConfDetailList().get(i).getScriptDesc());
                    camScriptService.addCamScript(UserUtil.loginId(), camScriptAddVO);
                }
                mktStrategyConfRuleDO.setEvtContactConfId(evtContactConfIds);
            }

            // 新增二次协同结果 并且 将结果id的存到规则中
            String resultConfs = "";
            if (mktStrategyConfRule.getMktCamChlResultList() != null) {
                String mktCamChlResultIds = "";
                for (int i = 0; i < mktStrategyConfRule.getMktCamChlResultList().size(); i++) {
                    mktStrategyConfRule.getMktCamChlResultList().get(i).setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                    Map<String, Object> mktCamChlResultMap = mktCamChlResultService.saveMktCamChlResult(mktStrategyConfRule.getMktCamChlResultList().get(i));
                    MktCamChlResultDO mktCamChlResultDO = (MktCamChlResultDO) mktCamChlResultMap.get("mktCamChlResultDO");
                    Long mktCamChlResultId = mktCamChlResultDO.getMktCamChlResultId();
                    if (i == 0) {
                        mktCamChlResultIds += mktCamChlResultId;
                    } else {
                        mktCamChlResultIds += "/" + mktCamChlResultId;
                    }
                    // 判断类型是否为工单类型 , 保存二次营销结果和活动的关联
                    if ("1".equals(mktCamChlResultDO.getResultType())) {
                        MktCamResultRelDO mktCamResultRelDO = new MktCamResultRelDO();
                        mktCamResultRelDO.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                        mktCamResultRelDO.setMktResultId(mktCamChlResultDO.getMktCamChlResultId());
                        mktCamResultRelDO.setStatus(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode()); //1000-有效
                        mktCamResultRelDO.setCreateDate(new Date());
                        mktCamResultRelDO.setCreateStaff(UserUtil.loginId());
                        mktCamResultRelDO.setUpdateDate(new Date());
                        mktCamResultRelDO.setUpdateStaff(UserUtil.loginId());
                        mktCamResultRelMapper.insert(mktCamResultRelDO);
                    }

                    List<MktCamChlResultConfRelDO> confRelDOList = mktCamChlResultConfRelMapper.selectByMktCamChlResultId(mktCamChlResultId);
                    for (MktCamChlResultConfRelDO mktCamChlResultConfRelDO : confRelDOList) {
                        if("".equals(resultConfs)){
                            resultConfs += mktCamChlResultConfRelDO.getEvtContactConfId();
                        } else{
                            resultConfs += "||" + mktCamChlResultConfRelDO.getEvtContactConfId();
                        }
                    }
                }
                mktStrategyConfRuleDO.setMktCamChlResultId(mktCamChlResultIds);
            }
            mktStrategyConfRuleDO.setMktStrategyConfRuleId(null);
            mktStrategyConfRuleDO.setCreateStaff(UserUtil.loginId());
            mktStrategyConfRuleDO.setCreateDate(new Date());
            mktStrategyConfRuleDO.setUpdateStaff(UserUtil.loginId());
            mktStrategyConfRuleDO.setUpdateDate(new Date());
            mktStrategyConfRuleMapper.insert(mktStrategyConfRuleDO);
            mktStrategyConfRuleDO.setInitId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
            mktStrategyConfRuleMapper.updateByPrimaryKey(mktStrategyConfRuleDO);

            //添加mkt_cam_grp_rul表
            MktCamGrpRul mktCamGrpRul = new MktCamGrpRul();
            if(mktStrategyConfRule.getTarGrpId()!=null){
                mktCamGrpRul.setTarGrpId(mktStrategyConfRule.getTarGrpId());
                mktCamGrpRul.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                //添加所属地市
                if(UserUtil.getUser()!=null){
                    mktCamGrpRul.setLanId(UserUtil.getUser().getLanId());
                } else{
                    mktCamGrpRul.setLanId((AreaCodeEnum.ZHEJIAGN.getRegionId()));
                }
                mktCamGrpRul.setMktStrategyConfId(mktStrategyConfRule.getStrategyConfId());
                mktCamGrpRul.setMktStrategyConfRuleId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
                mktCamGrpRul.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                mktCamGrpRul.setStatusDate(new Date());
                mktCamGrpRul.setCreateDate(new Date());
                mktCamGrpRul.setCreateStaff(UserUtil.loginId());
                mktCamGrpRul.setUpdateDate(new Date());
                mktCamGrpRul.setUpdateStaff(UserUtil.loginId());
                mktCamGrpRulMapper.insert(mktCamGrpRul);

                // 更新客户分群名字为规则名称
                TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(mktStrategyConfRule.getTarGrpId());
                if (tarGrp!=null){
                    tarGrp.setTarGrpName(mktStrategyConfRule.getMktStrategyConfRuleName());
                    tarGrp.setTarGrpDesc(mktStrategyConfRule.getMktStrategyConfRuleName());
                    tarGrpMapper.modTarGrp(tarGrp);
                }
            }

            //如果是增存量联动并且是导入的用户分群模板 创建试算记录
            TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(mktStrategyConfRuleDO.getTarGrpId());
            if (tarGrp!=null && tarGrp.getTarGrpType().equals(TarTempType.PROM_IMPORT_TEMPLETE.getValue())){
                String batchNumSt = DateUtil.date2St4Trial(new Date()) + ChannelUtil.getRandomStr(4);
                TrialOperation trialOp = new TrialOperation();
                //当清单导入时 strategyId name 存储规则信息
                trialOp.setCampaignId(mktStrategyConfRule.getMktCampaignId());
                trialOp.setCampaignName(mktStrategyConfRule.getMktCampaignName());
                trialOp.setStrategyId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
                trialOp.setStrategyName(mktStrategyConfRuleDO.getMktStrategyConfRuleName());
                trialOp.setBatchNum(Long.valueOf(batchNumSt));
                trialOp.setStatusCd(TrialStatus.PPM_IMPORT_GOING.getValue());
                trialOp.setStatusDate(new Date());
                trialOp.setCreateStaff(TrialCreateType.IMPORT_USER_LIST.getValue());
                trialOperationMapper.insert(trialOp);
            }


            // 配置推送渠道关系mkt_strategy表 和 mkt_cam_strategy_rel
            // 获取与结果关联的推送渠道信息
            String ruleExpression = "("+ confs + ")&&(" + resultConfs + ")";
            MktStrategy mktStrategy = new MktStrategy();
            mktStrategy.setStrategyId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
            // 活动分类为服务活动时,为关怀策略 ; 活动分类为营销活动，维系活动等其它活动时,为销售策略
            if(StatusCode.SERVICE_CAMPAIGN.getStatusCode().equals( mktStrategyConfRule.getMktCampaignType())){
                mktStrategy.setStrategyType(StatusCode.CARE_STRATEGY.getStatusCode());
            } else{
                mktStrategy.setStrategyType(StatusCode.SALES_STRATEGY.getStatusCode());
            }
            mktStrategy.setStrategyId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
            mktStrategy.setStrategyName(mktStrategyConfRule.getMktStrategyConfRuleName());
            mktStrategy.setStrategyDesc(mktStrategyConfRule.getMktStrategyConfRuleName());
            mktStrategy.setRuleExpression(ruleExpression);
            mktStrategy.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
            mktStrategy.setStatusDate(new Date());
            mktStrategy.setCreateStaff(UserUtil.loginId());
            mktStrategy.setCreateDate(new Date());
            mktStrategy.setUpdateStaff(UserUtil.loginId());
            mktStrategy.setUpdateDate(new Date());


            mktStrategyMapper.insert(mktStrategy);
            MktCamStrategyRel mktCamStrategyRel = new MktCamStrategyRel();
            mktCamStrategyRel.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
            mktCamStrategyRel.setMktStrategyConfId(mktStrategyConfRule.getStrategyConfId());
            mktCamStrategyRel.setMktStrategyConfRuleId(mktStrategyConfRule.getMktStrategyConfRuleId());
            mktCamStrategyRel.setStrategyId(mktStrategy.getStrategyId());
            mktCamStrategyRel.setCreateStaff(UserUtil.loginId());
            mktCamStrategyRel.setCreateDate(new Date());
            mktCamStrategyRel.setUpdateStaff(UserUtil.loginId());
            mktCamStrategyRel.setUpdateDate(new Date());
            mktCamStrategyRel.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
            mktCamStrategyRel.setStatusDate(new Date());
            mktCamStrategyRelMapper.insert(mktCamStrategyRel);

            //添加cpc算法规则
            MktCpcAlgorithmsRulDO mktCpcAlgorithmsRulDO = new MktCpcAlgorithmsRulDO();
            mktCpcAlgorithmsRulDO.setAlgorithmsRulId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
            mktCpcAlgorithmsRulDO.setAlgorithmsRulName(mktStrategyConfRuleDO.getMktStrategyConfRuleName());
            mktCpcAlgorithmsRulDO.setRuleExpression(
                    "{" + mktStrategyConfRule.getTarGrpId() +"}通过{" + mktStrategyConfRule.getStrategyConfId() +"}推送{" + mktStrategyConfRule.getProductIdlist() + "}");
            mktCpcAlgorithmsRulDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
            mktCpcAlgorithmsRulDO.setStatusDate(new Date());
            mktCpcAlgorithmsRulDO.setCreateDate(new Date());
            mktCpcAlgorithmsRulDO.setCreateStaff(UserUtil.loginId());
            mktCpcAlgorithmsRulDO.setUpdateStaff(UserUtil.loginId());
            mktCpcAlgorithmsRulDO.setUpdateDate(new Date());
            mktCpcAlgorithmsRulMapper.insert(mktCpcAlgorithmsRulDO);
            //cpc算法规则关联表
            MktCamRecomCalcRelDO mktCamRecomCalcRelDO = new MktCamRecomCalcRelDO();
            mktCamRecomCalcRelDO.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
            mktCamRecomCalcRelDO.setAlgorithmsRulId(mktCpcAlgorithmsRulDO.getAlgorithmsRulId());
            mktCamRecomCalcRelDO.setMktStrategyConfId(mktStrategyConfRule.getStrategyConfId());
            mktCamRecomCalcRelDO.setMktStrategyConfRuleId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
            mktCamRecomCalcRelDO.setAlgoId(1L);
            mktCamRecomCalcRelDO.setStatusDate(new Date());
            mktCamRecomCalcRelDO.setCreateDate(new Date());
            mktCamRecomCalcRelDO.setCreateStaff(UserUtil.loginId());
            mktCamRecomCalcRelDO.setUpdateStaff(UserUtil.loginId());
            mktCamRecomCalcRelDO.setUpdateDate(new Date());
            mktCamRecomCalcRelDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
            mktCamRecomCalcRelMapper.insert(mktCamRecomCalcRelDO);

            if (mktStrategyConfRule.getOrganizationList()!=null && !mktStrategyConfRule.getOrganizationList().isEmpty()){
                redisUtils.set("ORG_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId().toString(),mktStrategyConfRule.getOrganizationList());
                redisUtils.set("ORG_CHECK_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId().toString(),"false");
                area2RedisThread(mktStrategyConfRuleDO.getMktStrategyConfRuleId(),mktStrategyConfRule.getOrganizationList());
            }

            List<MktCamResource> camResource = mktCamResourceMapper.selectByCampaignId(mktStrategyConfRule.getMktCampaignId(), FrameFlgEnum.YES.getValue(),null);

            MktCampaignDO mktCampaignDO = campaignMapper.selectByPrimaryKey(mktStrategyConfRule.getMktCampaignId());
            if (!camResource.isEmpty()
                    && (StatusCode.AUTONOMICK_CAMPAIGN.getStatusCode().equals(mktCampaignDO.getMktCampaignCategory())||
                    StatusCode.CONTINUE_CAMPAIGN.getStatusCode().equals(mktCampaignDO.getMktCampaignCategory())) ){
                //规则级别创建电子券实例信息
                MktCamResource mktCamResource = mktStrategyConfRule.getMktCamResource();
                MktResource4RuleVO mktResource4RuleVO = BeanUtil.create(mktCamResource, new MktResource4RuleVO());
                MktCamResource resource = BeanUtil.create(camResource.get(0), new MktCamResource());
                BeanUtil.copy(mktResource4RuleVO, resource);
                resource.setCamResourceName(resource.getCamResourceName() + "_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId());
                resource.setMktCamResourceId(null);
                resource.setFrameFlg(FrameFlgEnum.NO.getValue()); // 是否电子券框架类型, yes-是，no-不是
                resource.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                resource.setRuleId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
                resource.setCreateStaff(UserUtil.loginId());
                resource.setCreateDate(new Date());
                resource.setUpdateStaff(UserUtil.loginId());
                resource.setUpdateDate(new Date());
                resource.setStatusCd("1000");
                resource.setDealShops(ChannelUtil.list2String(mktCamResource.getDealShopList(),"/"));
                List<Long> camItemIdList = mktStrategyConfRule.getProductIdlist();
                List<MktCamItem> camItemList = mktCamItemMapper.selectByBatch(camItemIdList);
                ChannelUtil.addItem2CamResource(resource,camItemList);
                mktCamResourceMapper.insert(resource);
            }

            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.SAVE_MKT_RULE_STR_CONF_RULE_SUCCESS.getErrorMsg());
            mktStrategyConfRuleMap.put("mktStrategyConfRuleDO", mktStrategyConfRuleDO);
        } catch (Exception e) {
            logger.error("[op:MktStrategyConfRuleServiceImpl] failed to save MktStrategyConfRule = {}, Exception:", JSON.toJSON(mktStrategyConfRule), e);
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_FAIL);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.SAVE_MKT_RULE_STR_CONF_RULE_FAILURE.getErrorMsg());
            mktStrategyConfRuleMap.put("mktStrategyConfRuleDO", mktStrategyConfRuleDO);
        }
        return mktStrategyConfRuleMap;
    }


    /**
     * 修改策略规则
     *
     * @param mktStrategyConfRule
     * @return
     */
    @Override
    public Map<String, Object> updateMktStrategyConfRule(MktStrategyConfRule mktStrategyConfRule) {
        // redisUtils.del("MKT_RULE_" + mktStrategyConfRule.getMktStrategyConfRuleId());
        Map<String, Object> mktStrategyConfRuleMap = new HashMap<>();
        String mktCamChlResultIds = "";
        String productIds = "";
        String evtContactConfIds = "";
        try {
            if(mktStrategyConfRule.getTarGrpId()!=null){
                MktCamGrpRul camGrpRul = mktCamGrpRulMapper.selectByTarGrpId(mktStrategyConfRule.getTarGrpId());
                if (camGrpRul == null) {
                    //添加mkt_cam_grp_rul表
                    MktCamGrpRul mktCamGrpRul = new MktCamGrpRul();
                    mktCamGrpRul.setTarGrpId(mktStrategyConfRule.getTarGrpId());
                    mktCamGrpRul.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                    if (UserUtil.getUser() != null) {
                        // 获取当前用户
                        mktCamGrpRul.setLanId(UserUtil.getUser().getLanId());
                    } else{
                        mktCamGrpRul.setLanId((AreaCodeEnum.ZHEJIAGN.getRegionId()));
                    }
                    mktCamGrpRul.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                    mktCamGrpRul.setStatusDate(new Date());
                    mktCamGrpRul.setCreateDate(new Date());
                    mktCamGrpRul.setCreateStaff(UserUtil.loginId());
                    mktCamGrpRul.setUpdateDate(new Date());
                    mktCamGrpRul.setUpdateStaff(UserUtil.loginId());
                    mktCamGrpRulMapper.insert(mktCamGrpRul);
                }
                // 更新客户分群名字为规则名称
                TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(mktStrategyConfRule.getTarGrpId());
                if (tarGrp!=null){
                    tarGrp.setTarGrpName(mktStrategyConfRule.getMktStrategyConfRuleName());
                    tarGrp.setTarGrpDesc(mktStrategyConfRule.getMktStrategyConfRuleName());
                    tarGrpMapper.modTarGrp(tarGrp);
                }
            }
            MktStrategyConfRuleDO mktStrategyConfRuleDO = new MktStrategyConfRuleDO();
            CopyPropertiesUtil.copyBean2Bean(mktStrategyConfRuleDO, mktStrategyConfRule);
            if (mktStrategyConfRule.getProductIdlist() != null) {
                for (int i = 0; i < mktStrategyConfRule.getProductIdlist().size(); i++) {
                    if (i == 0) {
                        productIds += mktStrategyConfRule.getProductIdlist().get(i);
                    } else {
                        productIds += "/" + mktStrategyConfRule.getProductIdlist().get(i);
                    }
                    // 给mkt_cam_item表添加活动id
                    MktCamItem mktCamItem = mktCamItemMapper.selectByPrimaryKey(mktStrategyConfRule.getProductIdlist().get(i));
                    mktCamItem.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                    mktCamItemMapper.updateByPrimaryKey(mktCamItem);
                }
                mktStrategyConfRuleDO.setProductId(productIds);
            }
            if (mktStrategyConfRule.getMktCamChlConfDetailList() != null) {
                for (int i = 0; i < mktStrategyConfRule.getMktCamChlConfDetailList().size(); i++) {
                    Long evtContactConfId = mktStrategyConfRule.getMktCamChlConfDetailList().get(i).getEvtContactConfId();
                    MktCamChlConfDO mktCamChlConfDO = mktCamChlConfMapper.selectByPrimaryKey(evtContactConfId);
                    mktCamChlConfDO.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                    mktCamChlConfMapper.updateByPrimaryKey(mktCamChlConfDO);
                    //redisUtils.del("CHL_CONF_DETAIL_" + evtContactConfId);
                    CamScriptAddVO camScriptAddVO = new CamScriptAddVO();
                    camScriptAddVO.setEvtContactConfId(evtContactConfId);
                    camScriptAddVO.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                    camScriptAddVO.setScriptDesc(mktStrategyConfRule.getMktCamChlConfDetailList().get(i).getScriptDesc());
                    camScriptService.addCamScript(UserUtil.loginId(), camScriptAddVO);
                    if (i == 0) {
                        evtContactConfIds += evtContactConfId;
                    } else {
                        evtContactConfIds += "/" + evtContactConfId;
                    }
                }
                mktStrategyConfRuleDO.setEvtContactConfId(evtContactConfIds);
            }
            if (mktStrategyConfRule.getMktCamChlResultList() != null) {
                for (int i = 0; i < mktStrategyConfRule.getMktCamChlResultList().size(); i++) {
                    Long mktCamChlResultId = 0L;
                    MktCamChlResult mktCamChlResult = mktStrategyConfRule.getMktCamChlResultList().get(i);
                    mktCamChlResult.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                    if (mktCamChlResult.getMktCamChlResultId() != null && mktCamChlResult.getMktCamChlResultId() != 0) {
                        // 修改已有的结果信息
                        mktCamChlResultService.updateMktCamChlResult(mktCamChlResult);
                        mktCamChlResultId = mktCamChlResult.getMktCamChlResultId();
                    } else {
                        // 新增结果信息
                        Map<String, Object> mktCamChlResultMap = mktCamChlResultService.saveMktCamChlResult(mktCamChlResult);
                        MktCamChlResultDO mktCamChlResultDO = (MktCamChlResultDO) mktCamChlResultMap.get("mktCamChlResultDO");
                        mktCamChlResultId = mktCamChlResultDO.getMktCamChlResultId();
                    }
                    if (i == 0) {
                        mktCamChlResultIds += mktCamChlResultId;
                    } else {
                        mktCamChlResultIds += "/" + mktCamChlResultId;
                    }
                }
                mktStrategyConfRuleDO.setMktCamChlResultId(mktCamChlResultIds);
            }

            mktStrategyConfRuleDO.setUpdateStaff(UserUtil.loginId());
            mktStrategyConfRuleDO.setUpdateDate(new Date());
            mktStrategyConfRuleMapper.updateByPrimaryKey(mktStrategyConfRuleDO);
            //redisUtils.del("MKT_RULE_" + mktStrategyConfRuleDO.getMktStrategyConfRuleId());
            //营销组织树
            if (mktStrategyConfRule.getOrganizationList()!=null && !mktStrategyConfRule.getOrganizationList().isEmpty()){
                if (redisUtils.get("ORG_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId().toString())==null
                        || !ChannelUtil.equalsList((List<Long>)redisUtils.get("ORG_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId().toString()),mktStrategyConfRule.getOrganizationList())){
                    redisUtils.set("ORG_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId().toString(),mktStrategyConfRule.getOrganizationList());
                    redisUtils.set("ORG_CHECK_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId().toString(),"false");
                    area2RedisThread(mktStrategyConfRuleDO.getMktStrategyConfRuleId(),mktStrategyConfRule.getOrganizationList());
                }
            }else {
                if (redisUtils.get("ORG_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId().toString())!=null){
                    redisUtils.remove("ORG_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId().toString());
                    redisUtils.remove("ORG_CHECK_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId().toString());
                    redisUtils.remove("AREA_RULE_ISSURE_"+mktStrategyConfRuleDO.getMktStrategyConfRuleId().toString());
                }
            }
            String confs = "";
            if (mktStrategyConfRule.getMktCamChlConfDetailList() != null) {
                for (int i = 0; i < mktStrategyConfRule.getMktCamChlConfDetailList().size(); i++) {
                    Long evtContactConfId =  mktStrategyConfRule.getMktCamChlConfDetailList().get(i).getEvtContactConfId();
                    if (i == 0) {
                        evtContactConfIds += evtContactConfId;
                        confs += evtContactConfId;
                    } else {
                        evtContactConfIds += "/" + evtContactConfId;
                        confs += "||" + evtContactConfId;
                    }
                }
            }

            // 新增二次协同结果 并且 将结果id的存到规则中
            String resultConfs = "";
            if (mktStrategyConfRule.getMktCamChlResultList() != null) {
                for (int i = 0; i < mktStrategyConfRule.getMktCamChlResultList().size(); i++) {
                    MktCamChlResult mktCamChlResult = mktStrategyConfRule.getMktCamChlResultList().get(i);
                    List<MktCamChlConfDetail> mktCamChlConfDetailList = mktCamChlResult.getMktCamChlConfDetailList();
                    for (MktCamChlConfDetail mktCamChlConfDetail : mktCamChlConfDetailList) {
                        if("".equals(resultConfs)){
                            resultConfs += mktCamChlConfDetail.getEvtContactConfId();
                        } else{
                            resultConfs += "||" + mktCamChlConfDetail.getEvtContactConfId();
                        }
                    }
                }
                mktStrategyConfRuleDO.setMktCamChlResultId(mktCamChlResultIds);
            }
            String ruleExpression = "("+ confs + ")&&(" + resultConfs + ")";

            MktStrategy mktStrategy = mktStrategyMapper.selectByPrimaryKey(mktStrategyConfRule.getMktStrategyConfRuleId());

            if (mktStrategy != null) {
                // 活动分类为服务活动时,为关怀策略 ; 活动分类为营销活动，维系活动等其它活动时,为销售策略
                if(StatusCode.SERVICE_CAMPAIGN.getStatusCode().equals( mktStrategyConfRule.getMktCampaignType())){
                    mktStrategy.setStrategyType(StatusCode.CARE_STRATEGY.getStatusCode());
                } else{
                    mktStrategy.setStrategyType(StatusCode.SALES_STRATEGY.getStatusCode());
                }
                mktStrategy.setStrategyName(mktStrategyConfRule.getMktStrategyConfRuleName());
                mktStrategy.setStrategyDesc(mktStrategyConfRule.getMktStrategyConfRuleName());
                mktStrategy.setRuleExpression(ruleExpression);
                mktStrategy.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                mktStrategy.setStatusDate(new Date());
                mktStrategy.setUpdateStaff(UserUtil.loginId());
                mktStrategy.setUpdateDate(new Date());
                mktStrategyMapper.updateByPrimaryKey(mktStrategy);
            } else {
                mktStrategy = new MktStrategy();
                mktStrategy.setStrategyId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
                // 活动分类为服务活动时,为关怀策略 ; 活动分类为营销活动，维系活动等其它活动时,为销售策略
                if(StatusCode.SERVICE_CAMPAIGN.getStatusCode().equals( mktStrategyConfRule.getMktCampaignType())){
                    mktStrategy.setStrategyType(StatusCode.CARE_STRATEGY.getStatusCode());
                } else{
                    mktStrategy.setStrategyType(StatusCode.SALES_STRATEGY.getStatusCode());
                }
                mktStrategy.setStrategyName(mktStrategyConfRule.getMktStrategyConfRuleName());
                mktStrategy.setStrategyDesc(mktStrategyConfRule.getMktStrategyConfRuleName());
                mktStrategy.setRuleExpression(ruleExpression);
                mktStrategy.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                mktStrategy.setStatusDate(new Date());
                mktStrategy.setCreateStaff(UserUtil.loginId());
                mktStrategy.setCreateDate(new Date());
                mktStrategy.setUpdateStaff(UserUtil.loginId());
                mktStrategy.setUpdateDate(new Date());
                mktStrategyMapper.insert(mktStrategy);
            }


            //添加cpc算法规则
            MktCpcAlgorithmsRulDO mktCpcAlgorithmsRulDO = mktCpcAlgorithmsRulMapper.selectByPrimaryKey(mktStrategyConfRule.getMktStrategyConfRuleId());
            if(mktCpcAlgorithmsRulDO!=null){
                mktCpcAlgorithmsRulDO.setAlgorithmsRulName(mktStrategyConfRule.getMktStrategyConfRuleName());
                mktCpcAlgorithmsRulDO.setRuleExpression(
                        "{" + mktStrategyConfRule.getTarGrpId() +"}通过{" + mktStrategyConfRule.getStrategyConfId() +"}推送{" + mktStrategyConfRule.getProductIdlist() + "}");
                mktCpcAlgorithmsRulDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                mktCpcAlgorithmsRulDO.setStatusDate(new Date());
                mktCpcAlgorithmsRulDO.setUpdateStaff(UserUtil.loginId());
                mktCpcAlgorithmsRulDO.setUpdateDate(new Date());
                mktCpcAlgorithmsRulMapper.updateByPrimaryKey(mktCpcAlgorithmsRulDO);
            }else {
                //添加cpc算法规则
                mktCpcAlgorithmsRulDO = new MktCpcAlgorithmsRulDO();
                mktCpcAlgorithmsRulDO.setAlgorithmsRulId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
                mktCpcAlgorithmsRulDO.setAlgorithmsRulName(mktStrategyConfRuleDO.getMktStrategyConfRuleName());
                mktCpcAlgorithmsRulDO.setRuleExpression(
                        "{" + mktStrategyConfRule.getTarGrpId() +"}通过{" + mktStrategyConfRule.getStrategyConfId() +"}推送{" + mktStrategyConfRule.getProductIdlist() + "}");
                mktCpcAlgorithmsRulDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                mktCpcAlgorithmsRulDO.setStatusDate(new Date());
                mktCpcAlgorithmsRulDO.setCreateDate(new Date());
                mktCpcAlgorithmsRulDO.setCreateStaff(UserUtil.loginId());
                mktCpcAlgorithmsRulDO.setUpdateStaff(UserUtil.loginId());
                mktCpcAlgorithmsRulDO.setUpdateDate(new Date());
                mktCpcAlgorithmsRulMapper.insert(mktCpcAlgorithmsRulDO);
            }

            //cpc算法规则关联表
            MktCamRecomCalcRelDO mktCamRecomCalcRelDO = mktCamRecomCalcRelMapper.selectByRuleId(mktStrategyConfRule.getMktStrategyConfRuleId());
            if(mktCamRecomCalcRelDO == null){
                mktCamRecomCalcRelDO = new MktCamRecomCalcRelDO();
                mktCamRecomCalcRelDO.setMktCampaignId(mktStrategyConfRule.getMktCampaignId());
                mktCamRecomCalcRelDO.setAlgorithmsRulId(mktCpcAlgorithmsRulDO.getAlgorithmsRulId());
                mktCamRecomCalcRelDO.setMktStrategyConfId(mktStrategyConfRule.getStrategyConfId());
                mktCamRecomCalcRelDO.setMktStrategyConfRuleId(mktStrategyConfRule.getMktStrategyConfRuleId());
                mktCamRecomCalcRelDO.setAlgoId(1L);
                mktCamRecomCalcRelDO.setStatusDate(new Date());
                mktCamRecomCalcRelDO.setCreateDate(new Date());
                mktCamRecomCalcRelDO.setCreateStaff(UserUtil.loginId());
                mktCamRecomCalcRelDO.setUpdateStaff(UserUtil.loginId());
                mktCamRecomCalcRelDO.setUpdateDate(new Date());
                mktCamRecomCalcRelDO.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                mktCamRecomCalcRelMapper.insert(mktCamRecomCalcRelDO);
            }

            //如果是增存量联动并且是导入的用户分群模板 创建试算记录
            TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(mktStrategyConfRuleDO.getTarGrpId());
            if (tarGrp!=null && tarGrp.getTarGrpType().equals(TarTempType.PROM_IMPORT_TEMPLETE.getValue())){
                String batchNumSt = DateUtil.date2St4Trial(new Date()) + ChannelUtil.getRandomStr(4);
                TrialOperation trialOp = new TrialOperation();
                //当清单导入时 strategyId name 存储规则信息
                trialOp.setCampaignId(mktStrategyConfRule.getMktCampaignId());
                trialOp.setCampaignName(mktStrategyConfRule.getMktCampaignName());
                trialOp.setStrategyId(mktStrategyConfRuleDO.getMktStrategyConfRuleId());
                trialOp.setStrategyName(mktStrategyConfRuleDO.getMktStrategyConfRuleName());
                trialOp.setBatchNum(Long.valueOf(batchNumSt));
                trialOp.setStatusCd(TrialStatus.PPM_IMPORT_GOING.getValue());
                trialOp.setStatusDate(new Date());
                trialOp.setCreateStaff(TrialCreateType.IMPORT_USER_LIST.getValue());
                trialOperationMapper.insert(trialOp);
            }

            //修改电子券的信息
            MktCamResource mktCamResource = mktStrategyConfRule.getMktCamResource();
            if (mktCamResource != null) {
                MktCamResource camResource = mktCamResourceMapper.selectByPrimaryKey(mktCamResource.getMktCamResourceId());
                MktResource4RuleVO mktResource4RuleVO = BeanUtil.create(mktCamResource, new MktResource4RuleVO());
                if (camResource!=null){
                    BeanUtil.copy(mktResource4RuleVO,camResource);
                }
                List<Long> camItemIdList = mktStrategyConfRule.getProductIdlist();
                List<MktCamItem> camItemList = mktCamItemMapper.selectByBatch(camItemIdList);
                ChannelUtil.addItem2CamResource(camResource,camItemList);
                camResource.setUpdateStaff(UserUtil.loginId());
                camResource.setUpdateDate(new Date());
                mktCamResourceMapper.updateByPrimaryKey(camResource);
            }
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.UPDATE_MKT_RULE_STR_CONF_RULE_SUCCESS.getErrorMsg());
            mktStrategyConfRuleMap.put("mktCamChlResultIds", mktCamChlResultIds);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[op:MktStrategyConfRuleServiceImpl] failed to update MktStrategyConfRule = {}, mktCamChlResultIds = {}", JSON.toJSON(mktStrategyConfRule), mktCamChlResultIds, e);
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_FAIL);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.UPDATE_MKT_RULE_STR_CONF_RULE_FAILURE.getErrorMsg());
            mktStrategyConfRuleMap.put("mktCamChlResultIds", mktCamChlResultIds);
        }
        return mktStrategyConfRuleMap;
    }


    @Override
    public Map<String, Object> test(Long ruleId, List<Long> orgList) {
        Map<String,Object> result = new HashMap<>();
        area2RedisThread(ruleId,orgList);
        result.put("res",redisUtils.hgetAllRedisList("AREA_RULE_"+ruleId));
        return result;
    }

    //添加营销组织树集合
    private void area2RedisThread(Long ruleId,List<Long> orgIdList) {
        //过滤 选择level 大于3 的
        List<Long> orgs = organizationMapper.selectByIdList(orgIdList);
        if (orgs == null || orgs.isEmpty()){
            redisUtils.set("ORG_CHECK_"+ruleId.toString(),"true");
        }else {
            //添加所有选择的节点信息到缓存
            redisUtils.set("ORG_ID_"+ruleId.toString(),orgs);
            redisUtils.remove("AREA_RULE_ISSURE_"+ruleId);
            new Thread(){
                public void run(){
                    areaList2Redis(ruleId,orgs);
                }
            }.start();
        }
    }

    public void areaList2Redis(Long ruleId,List<Long> areaIdList){
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(areaIdList.size());
        List<Organization> sysAreaList = new ArrayList<>();
        List<Future<Map<String,Object>>> futureList = new ArrayList<>();
        try {
            for (Long id : areaIdList){
                Future<Map<String,Object>> future = fixedThreadPool.submit(new areaTask(ruleId,id,sysAreaList));
                futureList.add(future);
            }
            for (Future<Map<String,Object>> future : futureList){
                future.get();
            }
            redisUtils.set("ORG_CHECK_"+ruleId.toString(),"true");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class areaTask implements Callable<Map<String,Object>>{
        private Long ruleId;
        private Long id;
        private  List<Organization> sysAreaList;
        public areaTask(Long ruleId,Long id, List<Organization> sysAreaList){
            this.ruleId = ruleId;
            this.id = id;
            this.sysAreaList = sysAreaList;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String,Object> result = new HashMap<>();
            logger.info("查询开始："+id);
            Organization org = organizationMapper.selectByPrimaryKey(id);
            if (org!=null){
                List<String> resultList = new ArrayList<>();
                resultList.add(org.getOrgId4a().toString());
                if (false){
                    areaC4List(id,resultList,sysAreaList);
                }else {
                    areaList(id,resultList,sysAreaList);
                }
                logger.info("父级id :"+id,resultList);
                redisUtils.hset("AREA_RULE_ISSURE_"+ruleId,id.toString(),resultList);
            }
            return result;
        }
    }

    public List<String> areaList(Long parentId,List<String> resultList,List<Organization> areas){
        List<Organization> sysAreaList = organizationMapper.selectByParentId(parentId);
        if (sysAreaList.isEmpty()){
            return resultList;
        }
        for (Organization area : sysAreaList){
            resultList.add(area.getOrgId4a().toString());
            areas.add(area);
            areaList(area.getOrgId(),resultList,areas);
        }
        return resultList;
    }

    public List<String> areaC4List(Long parentId,List<String> resultList,List<Organization> areas){
        List<Organization> sysAreaList = organizationMapper.selectByParentId(parentId);
        if (sysAreaList.isEmpty()){
            return resultList;
        }
        for (Organization area : sysAreaList){
            new Thread(){
                public void run(){
                    resultList.add(area.getOrgId4a().toString());
                    areas.add(area);
                    areaList(area.getOrgId(),resultList,areas);
                }
            }.start();
        }
        return resultList;
    }




    /**-
     * 查询策略规则
     *
     * @param mktStrategyConfRuleId
     * @return
     */
    @Override
    public Map<String, Object> getMktStrategyConfRule(Long mktStrategyConfRuleId) {
        Map<String, Object> mktStrategyConfRuleMap = null;
        MktStrategyConfRuleDO mktStrategyConfRuleDO = new MktStrategyConfRuleDO();
        MktStrategyConfRule mktStrategyConfRule = new MktStrategyConfRule();
        try {
            mktStrategyConfRuleMap = new HashMap<>();
            mktStrategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(mktStrategyConfRuleId);
            CopyPropertiesUtil.copyBean2Bean(mktStrategyConfRule, mktStrategyConfRuleDO);
            if (mktStrategyConfRuleDO.getProductId() != null) {
                String[] productIds = mktStrategyConfRuleDO.getProductId().split("/");
                List<Long> productIdList = new ArrayList<>();
                for (int i = 0; i < productIds.length; i++) {
                    if (productIds[i] != "" && !"".equals(productIds[i]) && !productIds[i].equals(null)) {
                        productIdList.add(Long.valueOf(productIds[i]));
                    }
                }
                mktStrategyConfRule.setProductIdlist(productIdList);
            }

            if (mktStrategyConfRuleDO.getEvtContactConfId() != null) {
                String[] evtContactConfIds = mktStrategyConfRuleDO.getEvtContactConfId().split("/");
                List<MktCamChlConfDetail> mktCamChlConfDetailList = new ArrayList<>();
                for (int i = 0; i < evtContactConfIds.length; i++) {
                    if (evtContactConfIds[i] != "" && !"".equals(evtContactConfIds[i]) && !evtContactConfIds[i].equals(null)) {
                        MktCamChlConfDetail mktCamChlConfDetail = new MktCamChlConfDetail();
                        mktCamChlConfDetail.setEvtContactConfId(Long.valueOf(evtContactConfIds[i]));
                        String evtContactConfName = mktCamChlConfMapper.selectforName(Long.valueOf(evtContactConfIds[i]));
                        mktCamChlConfDetail.setEvtContactConfName(evtContactConfName);
                        mktCamChlConfDetailList.add(mktCamChlConfDetail);
                    }
                }
                mktStrategyConfRule.setMktCamChlConfDetailList(mktCamChlConfDetailList);
            }

            if (mktStrategyConfRuleDO.getMktCamChlResultId() != null) {
                String[] mktCamChlResultIds = mktStrategyConfRuleDO.getMktCamChlResultId().split("/");
                List<MktCamChlResult> mktCamChlResultList = new ArrayList<>();
                if (mktCamChlResultIds != null && !"".equals(mktCamChlResultIds[0])) {
                    for (String mktCamChlResultId : mktCamChlResultIds) {
                        Map<String, Object> mktCamChlResultMap = mktCamChlResultService.getMktCamChlResult(Long.valueOf(mktCamChlResultId));
                        mktCamChlResultList.add((MktCamChlResult) mktCamChlResultMap.get("mktCamChlResult"));
                    }
                }
                mktStrategyConfRule.setMktCamChlResultList(mktCamChlResultList);
            }

            if (redisUtils.get("ORG_"+mktStrategyConfRule.getMktStrategyConfRuleId())!=null){
                mktStrategyConfRule.setOrganizationList((List<Long> )redisUtils.get("ORG_"+mktStrategyConfRule.getMktStrategyConfRuleId()));
            }
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.GET_MKT_RULE_STR_CONF_RULE_SUCCESS.getErrorMsg());
            mktStrategyConfRuleMap.put("mktStrategyConfRule", mktStrategyConfRule);
        } catch (Exception e) {
            logger.error("[op:MktStrategyConfRuleServiceImpl] failed to get MktStrategyConfRule = {}", JSON.toJSON(mktStrategyConfRuleDO), e);
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_FAIL);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.GET_MKT_RULE_STR_CONF_RULE_FAILURE.getErrorMsg());
            mktStrategyConfRuleMap.put("mktStrategyConfRule", mktStrategyConfRule);
        }
        return mktStrategyConfRuleMap;
    }

    /**
     * 查询策略规则列表
     *
     * @return
     */
    @Override
    public Map<String, Object> listAllMktStrategyConfRule() {
        Map<String, Object> mktStrategyConfRuleMap = new HashMap<>();
        List<MktStrategyConfRuleDO> mktStrategyConfRuleDOList = new ArrayList<>();
        try {
            mktStrategyConfRuleDOList = mktStrategyConfRuleMapper.selectAll();
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.GET_MKT_RULE_STR_CONF_RULE_SUCCESS.getErrorMsg());
            mktStrategyConfRuleMap.put("mktStrategyConfRuleDOList", mktStrategyConfRuleDOList);
        } catch (Exception e) {
            logger.error("[op:MktStrategyConfRuleServiceImpl] failed to get the List of mktStrategyConfRuleDOList = {}, Exception", JSON.toJSON(mktStrategyConfRuleDOList), e);
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_FAIL);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.GET_MKT_RULE_STR_CONF_RULE_FAILURE.getErrorMsg());
            mktStrategyConfRuleMap.put("mktStrategyConfRuleDOList", mktStrategyConfRuleDOList);
        }
        return mktStrategyConfRuleMap;
    }

    /**
     * 通过策略Id 查询对应的规则列表（id+名称）
     *
     * @return
     */
    @Override
    public Map<String, Object> listAllMktStrategyConfRuleForName(Long mktStrategyConfId) {
        Map<String, Object> mktStrategyConfRuleMap = new HashMap<>();
        List<MktStrategyConfRule> mktStrategyConfRuleList = new ArrayList<>();
        try {
            // 通过关系表查询策略下对应的规则Id
            List<MktStrategyConfRuleRelDO> mktStrategyConfRuleRelDOList = mktStrategyConfRuleRelMapper.selectByMktStrategyConfId(mktStrategyConfId);
            for (MktStrategyConfRuleRelDO mktStrategyConfRuleRelDO : mktStrategyConfRuleRelDOList) {
                MktStrategyConfRule mktStrategyConfRule = new MktStrategyConfRule();
                String mktStrategyConfRuleName = mktStrategyConfRuleMapper.selectMktStrategyConfRuleName(mktStrategyConfRuleRelDO.getMktStrategyConfRuleId());
                mktStrategyConfRule.setMktStrategyConfRuleId(mktStrategyConfRuleRelDO.getMktStrategyConfRuleId());
                mktStrategyConfRule.setMktStrategyConfRuleName(mktStrategyConfRuleName);
                mktStrategyConfRuleList.add(mktStrategyConfRule);
            }
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.GET_MKT_RULE_STR_CONF_RULE_SUCCESS.getErrorMsg());
            mktStrategyConfRuleMap.put("mktStrategyConfRuleList", mktStrategyConfRuleList);
        } catch (Exception e) {
            logger.error("[op:MktStrategyConfRuleServiceImpl] failed to get the List of mktStrategyConfRuleList = {} , Exception = ", JSON.toJSON(mktStrategyConfRuleList), e);
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_FAIL);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.GET_MKT_RULE_STR_CONF_RULE_FAILURE.getErrorMsg());
            mktStrategyConfRuleMap.put("mktStrategyConfRuleList", mktStrategyConfRuleList);
        }
        return mktStrategyConfRuleMap;
    }

    /**
     * 删除策略规则
     *
     * @param mktStrategyConfRuleId
     * @return
     */
    @Override
    public Map<String, Object> deleteMktStrategyConfRule(Long mktStrategyConfRuleId) {
        Map<String, Object> mktStrategyConfRuleMap = new HashMap<>();
        try {
            //删除规则跟策略的关联
            mktStrategyConfRuleRelMapper.deleteByMktStrategyConfRulId(mktStrategyConfRuleId);
            //redisUtils.del("MKT_RULE_" + mktStrategyConfRuleId);
            //删除活动与客户分群的关系
            MktStrategyConfRuleDO strategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(mktStrategyConfRuleId);
            if (strategyConfRuleDO!=null){
                List<String> confList = ChannelUtil.StringToList4LabelValue(strategyConfRuleDO.getEvtContactConfId());
                for (String confId : confList){
                    mktCamChlConfAttrMapper.deleteByEvtContactConfId(Long.valueOf(confId));
                    //redisUtils.del("CHL_CONF_DETAIL_" + Long.valueOf(confId));
                }
            }
            if(strategyConfRuleDO!=null && strategyConfRuleDO.getTarGrpId()!=null){
                mktCamGrpRulMapper.deleteByTarGrpId(strategyConfRuleDO.getTarGrpId());
            }
            //删除规则
            mktStrategyConfRuleMapper.deleteByPrimaryKey(mktStrategyConfRuleId);
            //redisUtils.del("MKT_RULE_" + mktStrategyConfRuleId);
            // 删除规则下的渠道关系
            mktStrategyMapper.deleteByPrimaryKey(mktStrategyConfRuleId);
            mktCamStrategyRelMapper.deleteByStrategyId(mktStrategyConfRuleId);

            // 删除cpc算法
            mktCpcAlgorithmsRulMapper.deleteByPrimaryKey(mktStrategyConfRuleId);

            //
            mktCamRecomCalcRelMapper.deleteByRuleId(mktStrategyConfRuleId);



            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.GET_MKT_RULE_STR_CONF_RULE_SUCCESS.getErrorMsg());
        } catch (Exception e) {
            logger.error("[op:MktStrategyConfRuleServiceImpl] failed to delete the mktStrategyConfRuleDO by mktStrategyConfRuleId = {},  Exception=", mktStrategyConfRuleId, e);
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_FAIL);
            mktStrategyConfRuleMap.put("resultMsg", ErrorCode.DELETE_MKT_RULE_STR_CONF_RULE_FAILURE.getErrorMsg());
        }
        return mktStrategyConfRuleMap;
    }


    /**
     * 通过父规则Id复制策略规则
     *
     * @param parentMktStrategyConfRuleId
     * @param isPublish                   是否为发布操作
     * @return
     */
    @Override
    public Map<String, Object> copyMktStrategyConfRule(Long parentMktStrategyConfRuleId, Long childMktCampaignId, Boolean isPublish) throws Exception {
        Map<String, Object> mktStrategyConfRuleMap = new HashMap<>();
        try {
            MktStrategyConfRuleDO mktStrategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(parentMktStrategyConfRuleId);
            MktStrategyConfRuleDO chiledMktStrategyConfRuleDO = new MktStrategyConfRuleDO();
            /**
             * 客户分群配置
             */
            //判断是否为发布操作
            Map<String, Object> tarGrpMap = tarGrpService.copyTarGrp(mktStrategyConfRuleDO.getTarGrpId(), isPublish);
            TarGrp tarGrp = (TarGrp) tarGrpMap.get("tarGrp");
            /**
             * 销售品配置
             */
            String childProductIds = "";
            if (mktStrategyConfRuleDO.getProductId() != null) {
                String[] productIds = mktStrategyConfRuleDO.getProductId().split("/");
                if (productIds != null && !"".equals(productIds[0])) {
                    for (int i = 0; i <productIds.length ; i++) {
                        MktCamItem mktCamItem = mktCamItemMapper.selectByPrimaryKey(Long.valueOf(productIds[i]));
                        if (mktCamItem != null) {
                            List<MktCamItem> mktCamItemList = mktCamItemMapper.selectByCampaignIdAndItemIdAndType(mktCamItem.getItemId(), childMktCampaignId, mktCamItem.getItemType());
                            if (mktCamItemList != null && !mktCamItemList.isEmpty()) {
                                if (i == 0){
                                    childProductIds += mktCamItemList.get(0).getMktCamItemId();
                                } else {
                                    childProductIds += "/" + mktCamItemList.get(0).getMktCamItemId();
                                }
                            }
                        }
                    }
                }
            }


            /**
             * 协同渠道配置
             */
            String childEvtContactConfIds = "";
            if (mktStrategyConfRuleDO.getEvtContactConfId() != null) {
                String[] evtContactConfIds = mktStrategyConfRuleDO.getEvtContactConfId().split("/");
                if (evtContactConfIds != null && !"".equals(evtContactConfIds[0])) {
                    for (int i = 0; i < evtContactConfIds.length; i++) {
                        if (evtContactConfIds[i] != "" && !"".equals(evtContactConfIds[i])) {
                            Map<String, Object> mktCamChlConfDOMap = mktCamChlConfService.copyMktCamChlConf(Long.valueOf(evtContactConfIds[i]));
                            MktCamChlConfDetail mktCamChlConfDetail = (MktCamChlConfDetail) mktCamChlConfDOMap.get("mktCamChlConfDetail");
                            if (i == 0) {
                                childEvtContactConfIds += mktCamChlConfDetail.getEvtContactConfId();
                            } else {
                                childEvtContactConfIds += "/" + mktCamChlConfDetail.getEvtContactConfId();
                            }
                        }
                    }
                }
            }

            /**
             * 二次协同结果
             */
            String[] mktCamChlResultIds = mktStrategyConfRuleDO.getMktCamChlResultId().split("/");
            String childMktCamChlResultIds = "";
            if (mktCamChlResultIds != null && !"".equals(mktCamChlResultIds[0])) {
                for (int i = 0; i < mktCamChlResultIds.length; i++) {
                    Map<String, Object> mktCamChlResultDOMap = mktCamChlResultService.copyMktCamChlResult(Long.valueOf(mktCamChlResultIds[i]));
                    MktCamChlResult mktCamChlResult = (MktCamChlResult) mktCamChlResultDOMap.get("mktCamChlResult");
                    if (i == 0) {
                        childMktCamChlResultIds += mktCamChlResult.getMktCamChlResultId();
                    } else {
                        childMktCamChlResultIds += "/" + mktCamChlResult.getMktCamChlResultId();
                    }
                }
            }
            chiledMktStrategyConfRuleDO.setMktStrategyConfRuleName(mktStrategyConfRuleDO.getMktStrategyConfRuleName());
            if (tarGrp != null) {
                chiledMktStrategyConfRuleDO.setTarGrpId(tarGrp.getTarGrpId());
            }

            chiledMktStrategyConfRuleDO.setProductId(childProductIds);
            chiledMktStrategyConfRuleDO.setEvtContactConfId(childEvtContactConfIds);
            chiledMktStrategyConfRuleDO.setMktCamChlResultId(childMktCamChlResultIds);
            chiledMktStrategyConfRuleDO.setCreateDate(new Date());
            chiledMktStrategyConfRuleDO.setCreateStaff(UserUtil.loginId());
            chiledMktStrategyConfRuleDO.setUpdateDate(new Date());
            chiledMktStrategyConfRuleDO.setUpdateStaff(UserUtil.loginId());
            mktStrategyConfRuleMapper.insert(chiledMktStrategyConfRuleDO);
            Long chiledMktStrategyConfRuleId = chiledMktStrategyConfRuleDO.getMktStrategyConfRuleId();

            // 电子券
            MktCamResource mktCamResourceParent = mktCamResourceMapper.selectByRuleId(parentMktStrategyConfRuleId, FrameFlgEnum.NO.getValue());
            MktCamResource mktCamResource = BeanUtil.create(mktCamResourceParent, new MktCamResource());
            mktCamResource.setRuleId(chiledMktStrategyConfRuleId);
            mktCamResource.setCreateDate(new Date());
            mktCamResource.setFrameFlg(FrameFlgEnum.NO.getValue());
            mktCamResourceMapper.insert(mktCamResource);

            // initId
            chiledMktStrategyConfRuleDO.setInitId(chiledMktStrategyConfRuleId);
            mktStrategyConfRuleMapper.updateByPrimaryKey(chiledMktStrategyConfRuleDO);
            mktStrategyConfRuleMap.put("mktStrategyConfRuleId", chiledMktStrategyConfRuleDO.getMktStrategyConfRuleId());
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktStrategyConfRuleMap.put("resultMsg", "复制成功！");
        } catch (Exception e) {
            logger.error("[op:MktStrategyConfRuleServiceImpl] failed to copyMktStrategyConfRule by parentMktStrategyConfRuleId = {},  Exception=", parentMktStrategyConfRuleId, e);
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_FAIL);
            mktStrategyConfRuleMap.put("resultMsg", "复制失败！");
        }
        return mktStrategyConfRuleMap;
    }


    /**
     * 通过规则内容复制策略规则
     *
     * @param
     * @return
     */
    @Override
    public Map<String, Object> copyMktStrategyConfRule(List<MktStrategyConfRule> mktStrategyConfRuleList) throws Exception {
        Map<String, Object> ruleListMap = new HashMap<>();
        //初始化结果集
        Future<Map<String, Object>> ruleFuture = null;
        List<Future<Map<String, Object>>> threadList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        logger.info("MktStrategyConfRule-->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>开始：" + simpleDateFormat.format(new Date()));

        for (MktStrategyConfRule mktStrategyConfRule : mktStrategyConfRuleList) {
            //初始化线程池
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ExecutorService executorService = Executors.newCachedThreadPool();
            ruleFuture = executorService.submit(new CopyMktStrategyConfRuleTask(mktStrategyConfRule));
            threadList.add(ruleFuture);
        }

        List<MktStrategyConfRule> ruleNewList = new ArrayList<>();
        for (Future<Map<String, Object>> ruleFutureNew : threadList) {
            if (ruleFutureNew != null && ruleFutureNew.get() != null) {
                MktStrategyConfRule ruleNew = (MktStrategyConfRule) ruleFutureNew.get().get("mktStrategyConfRule");
                ruleNewList.add(ruleNew);
            }
            ruleListMap.put("mktStrategyConfRuleList", ruleNewList);
        }

        return ruleListMap;
    }


    class CopyTarGrpTask implements Callable<Map<String, Object>> {
        private Long tarGrpId;

        public CopyTarGrpTask(Long tarGrpId) {
            this.tarGrpId = tarGrpId;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> tarGrpMap = tarGrpService.copyTarGrp(tarGrpId, false);
            TarGrp tarGrp = (TarGrp) tarGrpMap.get("tarGrp");
            if (tarGrp != null) {
                tarGrpId = tarGrp.getTarGrpId();
            }
            return tarGrpMap;
        }
    }

    class CopyProductRuleTask implements Callable<Map<String, Object>> {
        private List<Long> productIdlist;

        public CopyProductRuleTask(List<Long> productIdlist) {
            this.productIdlist = productIdlist;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> productRuleMap = productService.copyProductRule(UserUtil.loginId(), productIdlist);
            return productRuleMap;
        }
    }

    class CopyMktCamChlConfTask implements Callable<Map<String, Object>> {
        private List<MktCamChlConfDetail> mktCamChlConfDetailList;

        public CopyMktCamChlConfTask(List<MktCamChlConfDetail> mktCamChlConfDetailList) {
            this.mktCamChlConfDetailList = mktCamChlConfDetailList;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> mktCamChlConfDetailMap = new HashMap<>();
            List<MktCamChlConfDetail> mktCamChlConfDetailNewList = new ArrayList<>();
            if (mktCamChlConfDetailList != null && mktCamChlConfDetailList.size() > 0) {
                for (MktCamChlConfDetail mktCamChlConf : mktCamChlConfDetailList) {
                    Map<String, Object> mktCamChlConfMap = mktCamChlConfService.copyMktCamChlConfFormRedis(mktCamChlConf.getEvtContactConfId(), mktCamChlConf.getScriptDesc());
                    MktCamChlConfDetail mktCamChlConfDetail = (MktCamChlConfDetail) mktCamChlConfMap.get("mktCamChlConfDetail");
                    CamScript camScript = new CamScript();
                    camScript.setScriptDesc(mktCamChlConf.getScriptDesc());
                    if (mktCamChlConfDetail != null) {
                        mktCamChlConfDetail.setScriptDesc(mktCamChlConf.getScriptDesc());
                    }
                    //   redisUtils.set("MktCamChlConfDetail_" + mktCamChlConfDetail.getEvtContactConfId(), mktCamChlConfDetail);
                    //   mktCamChlConf camChlConf = BeanUtil.create(mktCamChlConfDO, new mktCamChlConf());
                    mktCamChlConfDetailNewList.add(mktCamChlConfDetail);
                }
                mktCamChlConfDetailMap.put("mktCamChlConfDetailList", mktCamChlConfDetailNewList);
            }
            return mktCamChlConfDetailMap;
        }
    }


    class CopyMktCamChlResultTask implements Callable<Map<String, Object>> {
        private List<MktCamChlResult> mktCamChlResultList;

        public CopyMktCamChlResultTask(List<MktCamChlResult> mktCamChlResultList) {
            this.mktCamChlResultList = mktCamChlResultList;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> mktCamChlConfDetailMap = new HashMap<>();
            List<MktCamChlResult> mktCamChlResultNewList = new ArrayList<>();
            if (mktCamChlResultList != null && mktCamChlResultList.size() > 0) {
                for (MktCamChlResult mktCamChlResult : mktCamChlResultList) {
                    Map<String, Object> mktCamChlResultMap = mktCamChlResultService.copyMktCamChlResultFromRedis(mktCamChlResult);
                    MktCamChlResult childMktCamChlResult = (MktCamChlResult) mktCamChlResultMap.get("mktCamChlResult");
                    childMktCamChlResult.setMktCamChlResultId(null);
                    mktCamChlResultNewList.add(childMktCamChlResult);
                }
                mktCamChlConfDetailMap.put("mktCamChlResultList", mktCamChlResultNewList);
            }
            return mktCamChlConfDetailMap;
        }
    }

    /**
     * 更新规则下销售品
     *
     * @param productIdList
     * @param ruleId
     * @return
     */
    @Override
    public Map<String, Object> updateProductIds(List<Long> productIdList, Long ruleId) {
        Map<String, Object> mktStrategyConfRuleMap = new HashMap<>();
        MktStrategyConfRuleDO mktStrategyConfRuleDO = new MktStrategyConfRuleDO();
        String productIds = "";
        for (int i = 0; i < productIdList.size(); i++) {
            if (i == 0) {
                productIds += productIdList.get(i);
            } else {
                productIds += "/" + productIdList.get(i);
            }
        }
        mktStrategyConfRuleDO.setMktStrategyConfRuleId(ruleId);
        mktStrategyConfRuleDO.setProductId(productIds);
        mktStrategyConfRuleDO.setUpdateDate(new Date());
        mktStrategyConfRuleDO.setUpdateStaff(UserUtil.loginId());
        mktStrategyConfRuleMapper.updateByPrimaryKey(mktStrategyConfRuleDO);
        //redisUtils.del("MKT_RULE_" + ruleId);
        mktStrategyConfRuleMap.put("ruleId", ruleId);
        return mktStrategyConfRuleMap;
    }


    class CopyMktStrategyConfRuleTask implements Callable<Map<String, Object>> {
        private MktStrategyConfRule parentMktStrategyConfRule;

        public CopyMktStrategyConfRuleTask(MktStrategyConfRule mktStrategyConfRule) {
            this.parentMktStrategyConfRule = mktStrategyConfRule;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            logger.info("MktStrategyConfRule-->>>开始：" + simpleDateFormat.format(new Date()));
            Map<String, Object> mktStrategyConfRuleMap = new HashMap<>();
            MktStrategyConfRule mktStrategyConfRule = new MktStrategyConfRule();
            /**
             * 客户分群配置
             */
            logger.info("-------------->>>>>>>>>>>>>>>>>>>>>>>>>复制规则开始：" + simpleDateFormat.format(new Date()));

            //初始化结果集
            List<Future<Map<String, Object>>> threadList = new ArrayList<>();
            //初始化线程池
            ExecutorService executorService = Executors.newCachedThreadPool();
            Future<Map<String, Object>> tarGrpFuture = null;
            if (parentMktStrategyConfRule.getTarGrpId() != null) {
                tarGrpFuture = executorService.submit(new CopyTarGrpTask(parentMktStrategyConfRule.getTarGrpId()));
                threadList.add(tarGrpFuture);
            }

            /**
             * 销售品配置
             */
   /*         Future<Map<String, Object>> productFuture = null;
            if (parentMktStrategyConfRule.getProductIdlist() != null && parentMktStrategyConfRule.getProductIdlist().size() > 0) {
                productFuture = executorService.submit(new CopyProductRuleTask(parentMktStrategyConfRule.getProductIdlist()));
                threadList.add(productFuture);
            }*/


            /**
             * 协同渠道配置
             */
            Future<Map<String, Object>> mktCamChlConfFuture = null;
            if (parentMktStrategyConfRule.getMktCamChlConfDetailList() != null && parentMktStrategyConfRule.getMktCamChlConfDetailList().size() > 0) {
                mktCamChlConfFuture = executorService.submit(new CopyMktCamChlConfTask(parentMktStrategyConfRule.getMktCamChlConfDetailList()));
                threadList.add(mktCamChlConfFuture);
            }

            /**
             * 二次协同结果
             */
            Future<Map<String, Object>> mktCamChlResultFuture = null;
            if (parentMktStrategyConfRule.getMktCamChlResultList() != null && parentMktStrategyConfRule.getMktCamChlResultList().size() > 0) {
                mktCamChlResultFuture = executorService.submit(new CopyMktCamChlResultTask(parentMktStrategyConfRule.getMktCamChlResultList()));
                threadList.add(mktCamChlResultFuture);
            }
            mktStrategyConfRule.setMktStrategyConfRuleName(parentMktStrategyConfRule.getMktStrategyConfRuleName());

            // 从结果集中获取对应结果数据
            if (tarGrpFuture != null) {
                TarGrp tarGrp = (TarGrp) tarGrpFuture.get().get("tarGrp");
                mktStrategyConfRule.setTarGrpId(tarGrp.getTarGrpId());
            }

         /*   if (productFuture != null) {
                List<Long> ruleIdList = (List<Long>) productFuture.get().get("ruleIdList");
                mktStrategyConfRule.setProductIdlist(ruleIdList);
            }*/

            mktStrategyConfRule.setProductIdlist(parentMktStrategyConfRule.getProductIdlist());

            if (mktCamChlConfFuture != null) {
                List<MktCamChlConfDetail> mktCamChlConfDetailList = (List<MktCamChlConfDetail>) mktCamChlConfFuture.get().get("mktCamChlConfDetailList");
                mktStrategyConfRule.setMktCamChlConfDetailList(mktCamChlConfDetailList);
            }

            if (mktCamChlResultFuture != null) {
                List<MktCamChlResult> mktCamChlResultList = (List<MktCamChlResult>) mktCamChlResultFuture.get().get("mktCamChlResultList");
                mktStrategyConfRule.setMktCamChlResultList(mktCamChlResultList);
            }
            mktStrategyConfRule.setMktCamResource(parentMktStrategyConfRule.getMktCamResource());


            executorService.shutdown();
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktStrategyConfRuleMap.put("mktStrategyConfRule", mktStrategyConfRule);
            logger.info("-------------->>>>>>>>>>>>>>>>>>>>>>>>>复制规则结束：" + simpleDateFormat.format(new Date()));
            return mktStrategyConfRuleMap;
        }
    }


    /**
     * 批量保存客户分群
     *
     * @param ruleIdList
     * @param tarGrpNewId
     * @return
     */
    @Override
    public Map<String, Object> insertTarGrpBatch(List<Integer> ruleIdList, Long tarGrpNewId) {
        Map<String, Object> tarGrpMap = new HashMap<>();
        // 查询新的客户分群
        try {
            TarGrpDetail tarGrpDetail = (TarGrpDetail) redisUtils.get("TAR_GRP_" + tarGrpNewId);
            if (tarGrpDetail == null) {
                TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(tarGrpNewId);
                tarGrpDetail = BeanUtil.create(tarGrp, new TarGrpDetail());
                List<TarGrpCondition> conditionList = tarGrpConditionMapper.listTarGrpCondition(tarGrpNewId);
                tarGrpDetail.setTarGrpConditions(conditionList);
                redisUtils.set("TAR_GRP_" + tarGrp.getTarGrpId(), tarGrpDetail);
            }
            //初始化结果集
            List<Future<Map<String, Object>>> threadList = new ArrayList<>();
            //初始化线程池
            for (Integer ruleId : ruleIdList) {
                Future<Map<String, Object>> tarGrpFuture = null;
                ExecutorService executorService = Executors.newCachedThreadPool();
                tarGrpFuture = executorService.submit(new insertTarGrpBatchTask(ruleId.longValue(), tarGrpDetail));
                threadList.add(tarGrpFuture);
            }

            List<TarGrpDetail> tarGrpDetailList = new ArrayList<>();
            for (Future<Map<String, Object>> tarGrpFutureNew : threadList) {
                if (tarGrpFutureNew != null && tarGrpFutureNew.get() != null) {
                    TarGrpDetail grpDetail = (TarGrpDetail) tarGrpFutureNew.get().get("tarGrpDetail");
                    tarGrpDetailList.add(grpDetail);
                }
                tarGrpMap.put("tarGrpDetailList", tarGrpDetailList);
                tarGrpMap.put("resultCode", CommonConstant.CODE_SUCCESS);
                tarGrpMap.put("resultMsg", "批量插入成功！");
            }
        } catch (Exception e) {
            logger.error("[op:deleteTarGrpBatch],批量插入客户分群失败! Exception = ", e);
            tarGrpMap.put("resultCode", CommonConstant.CODE_FAIL);
            tarGrpMap.put("resultMsg", "批量插入失败！");
        }
        return tarGrpMap;
    }


    class insertTarGrpBatchTask implements Callable<Map<String, Object>> {
        private Long ruleId;
        private TarGrpDetail tarGrpDetail;

        public insertTarGrpBatchTask(Long ruleId, TarGrpDetail tarGrpDetail) {
            this.ruleId = ruleId;
            this.tarGrpDetail = tarGrpDetail;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> map = new HashMap<>();
            MktStrategyConfRuleDO mktStrategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(ruleId);

            if (mktStrategyConfRuleDO.getTarGrpId() != null) {
                TarGrpDetail detail = (TarGrpDetail) redisUtils.get("TAR_GRP_" + mktStrategyConfRuleDO.getTarGrpId());
                List<TarGrpCondition> tarGrpConditionList = new ArrayList<>();
                if (detail == null) {
                    TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(mktStrategyConfRuleDO.getTarGrpId());
                    if (tarGrp != null) {
                        List<TarGrpCondition> conditionList = tarGrpConditionMapper.listTarGrpCondition(mktStrategyConfRuleDO.getTarGrpId());
                        detail = BeanUtil.create(tarGrp, new TarGrpDetail());
                        detail.setTarGrpConditions(conditionList);
                        redisUtils.set("TAR_GRP_" + tarGrp.getTarGrpId(), detail);
                    } else {
                        Map<String, Object> tarGrpMap = tarGrpService.createTarGrp(tarGrpDetail, false);
                        TarGrp tarGrpNew = (TarGrp) tarGrpMap.get("tarGrp");
                        MktStrategyConfRuleDO ruleNew = mktStrategyConfRuleMapper.selectByPrimaryKey(ruleId);
                        ruleNew.setTarGrpId(tarGrpNew.getTarGrpId());
                        ruleNew.setUpdateDate(new Date());
                        ruleNew.setUpdateStaff(UserUtil.loginId());
                        mktStrategyConfRuleMapper.updateByPrimaryKey(ruleNew);
                        // redisUtils.del("MKT_RULE_" + ruleId);
                    }
                }
                List<TarGrpCondition> moreList = new ArrayList<>();
                for (TarGrpCondition tarGrpCondition : detail.getTarGrpConditions()) {
                    for (TarGrpCondition tarGrpConditionNew : tarGrpDetail.getTarGrpConditions()) {
                        tarGrpConditionNew.setConditionId(null);
                        if (tarGrpCondition.getLeftParam().equals(tarGrpConditionNew.getLeftParam())) {
                            moreList.add(tarGrpCondition);
                            continue;
                        }
                    }
                }
                detail.getTarGrpConditions().removeAll(moreList);
                List<TarGrpCondition> newList = new ArrayList<>();
                newList.addAll(detail.getTarGrpConditions());
                newList.addAll(tarGrpDetail.getTarGrpConditions());
                detail.setTarGrpConditions(newList);
                map = tarGrpService.modTarGrp(detail);
            } else {
                tarGrpService.createTarGrp(tarGrpDetail, false);
                Map<String, Object> tarGrpMap = tarGrpService.createTarGrp(tarGrpDetail, false);
                TarGrp tarGrpNew = (TarGrp) tarGrpMap.get("tarGrp");
                MktStrategyConfRuleDO ruleNew = mktStrategyConfRuleMapper.selectByPrimaryKey(ruleId);
                ruleNew.setTarGrpId(tarGrpNew.getTarGrpId());
                ruleNew.setUpdateDate(new Date());
                ruleNew.setUpdateStaff(UserUtil.loginId());
                mktStrategyConfRuleMapper.updateByPrimaryKey(ruleNew);
                //redisUtils.del("MKT_RULE_" + ruleId);
            }
            return map;
        }
    }


    /**
     * 批量修改客户分群
     *
     * @param ruleIdList
     * @param tarGrpNewId
     * @return
     */
    @Override
    public Map<String, Object> updateTarGrpBatch(List<Integer> ruleIdList, Long tarGrpNewId) {
        Map<String, Object> tarGrpMap = new HashMap<>();
        // 查询新的客户分群
        try {
            TarGrpDetail tarGrpDetail = (TarGrpDetail) redisUtils.get("TAR_GRP_" + tarGrpNewId);
            if (tarGrpDetail == null) {
                TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(tarGrpNewId);
                tarGrpDetail = BeanUtil.create(tarGrp, new TarGrpDetail());
                List<TarGrpCondition> conditionList = tarGrpConditionMapper.listTarGrpCondition(tarGrpNewId);
                tarGrpDetail.setTarGrpConditions(conditionList);
                redisUtils.set("TAR_GRP_" + tarGrp.getTarGrpId(), tarGrpDetail);
            }
            //初始化结果集
            List<Future<Map<String, Object>>> threadList = new ArrayList<>();
            //初始化线程池
            for (Integer ruleId : ruleIdList) {
                Future<Map<String, Object>> tarGrpFuture = null;
                ExecutorService executorService = Executors.newCachedThreadPool();
                tarGrpFuture = executorService.submit(new updateTarGrpBatchTask(ruleId.longValue(), tarGrpDetail));
                threadList.add(tarGrpFuture);
            }

            List<TarGrpDetail> tarGrpDetailList = new ArrayList<>();
            for (Future<Map<String, Object>> tarGrpFutureNew : threadList) {
                if (tarGrpFutureNew != null && tarGrpFutureNew.get() != null) {
                    TarGrpDetail grpDetail = (TarGrpDetail) tarGrpFutureNew.get().get("tarGrpDetail");
                    tarGrpDetailList.add(grpDetail);
                }
                tarGrpMap.put("tarGrpDetailList", tarGrpDetailList);
                tarGrpMap.put("resultCode", CommonConstant.CODE_SUCCESS);
                tarGrpMap.put("resultMsg", "批量修改成功！");
            }
        } catch (Exception e) {
            logger.error("[op:deleteTarGrpBatch],批量修改客户分群失败! Exception = ", e);
            tarGrpMap.put("resultCode", CommonConstant.CODE_FAIL);
            tarGrpMap.put("resultMsg", "批量修改失败！");
        }
        return tarGrpMap;
    }


    class updateTarGrpBatchTask implements Callable<Map<String, Object>> {
        private Long ruleId;
        private TarGrpDetail tarGrpDetail;

        public updateTarGrpBatchTask(Long ruleId, TarGrpDetail tarGrpDetail) {
            this.ruleId = ruleId;
            this.tarGrpDetail = tarGrpDetail;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> map = new HashMap<>();
            MktStrategyConfRuleDO mktStrategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(ruleId);
            if (mktStrategyConfRuleDO.getTarGrpId() != null) {
                // 获取原来的额客户分群信息
                TarGrpDetail detail = (TarGrpDetail) redisUtils.get("TAR_GRP_" + mktStrategyConfRuleDO.getTarGrpId());
                if (detail == null) {
                    TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(mktStrategyConfRuleDO.getTarGrpId());
                    detail = BeanUtil.create(tarGrp, new TarGrpDetail());
                    List<TarGrpCondition> conditionList = tarGrpConditionMapper.listTarGrpCondition(mktStrategyConfRuleDO.getTarGrpId());
                    detail.setTarGrpConditions(conditionList);
                    redisUtils.set("TAR_GRP_" + tarGrp.getTarGrpId(), tarGrpDetail);
                }
                if (detail != null) {
                    List<TarGrpCondition> moreList = new ArrayList<>();
                    // 遍历对比新的客户分群条件和原客户分群条件的不同
                    for (int i = 0; i < detail.getTarGrpConditions().size(); i++) {
                        TarGrpCondition tarGrpCondition = detail.getTarGrpConditions().get(i);
                        for (int j = 0; j < tarGrpDetail.getTarGrpConditions().size(); j++) {
                            TarGrpCondition tarGrpConditionNew = tarGrpDetail.getTarGrpConditions().get(j);
                            if (tarGrpCondition.getLeftParam().equals(tarGrpConditionNew.getLeftParam())) {
                                tarGrpCondition.setRightParam(tarGrpConditionNew.getRightParam());
                                tarGrpCondition.setOperType(tarGrpConditionNew.getOperType());
                                moreList.add(tarGrpCondition);
                                continue;
                            } else if (!tarGrpCondition.getLeftParam().equals(tarGrpConditionNew.getLeftParam()) && j == tarGrpDetail.getTarGrpConditions().size() - 1) {
                                moreList.add(tarGrpCondition);
                            }
                        }
                    }
                    detail.setTarGrpConditions(moreList);
                    logger.info("detail = " + JSON.toJSONString(detail));
                    map = tarGrpService.modTarGrp(detail);
                    // tarGrpConditionMapper.deleteBatch(delId);
                }
            }
            return map;
        }
    }


    /**
     * 批量删除
     *
     * @param ruleIdList
     * @param tarGrpNewId
     * @return
     */
    @Override
    public Map<String, Object> deleteTarGrpBatch(List<Integer> ruleIdList, Long tarGrpNewId) {
        Map<String, Object> tarGrpMap = new HashMap<>();
        // 查询新的客户分群
        try {
            TarGrpDetail tarGrpDetail = (TarGrpDetail) redisUtils.get("TAR_GRP_" + tarGrpNewId);
            if (tarGrpDetail == null) {
                TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(tarGrpNewId);
                tarGrpDetail = BeanUtil.create(tarGrp, new TarGrpDetail());
                List<TarGrpCondition> conditionList = tarGrpConditionMapper.listTarGrpCondition(tarGrpNewId);
                tarGrpDetail.setTarGrpConditions(conditionList);
                redisUtils.set("TAR_GRP_" + tarGrp.getTarGrpId(), tarGrpDetail);
            }
            //初始化结果集
            List<Future<Map<String, Object>>> threadList = new ArrayList<>();
            //初始化线程池
            Future<Map<String, Object>> tarGrpFuture = null;
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (Integer ruleId : ruleIdList) {
                tarGrpFuture = executorService.submit(new deleteTarGrpBatchTask(ruleId.longValue(), tarGrpDetail));
                threadList.add(tarGrpFuture);
            }

            List<TarGrpDetail> tarGrpDetailList = new ArrayList<>();
            for (Future<Map<String, Object>> tarGrpFutureNew : threadList) {
                if (tarGrpFutureNew != null && tarGrpFutureNew.get() != null) {
                    TarGrpDetail grpDetail = (TarGrpDetail) tarGrpFutureNew.get().get("tarGrpDetail");
                    tarGrpDetailList.add(grpDetail);
                }
                tarGrpMap.put("tarGrpDetailList", tarGrpDetailList);
                tarGrpMap.put("resultCode", CommonConstant.CODE_SUCCESS);
                tarGrpMap.put("resultMsg", "批量删除成功！");
            }
        } catch (Exception e) {
            logger.error("[op:deleteTarGrpBatch],批量删除客户分群失败! Exception = ", e);
            tarGrpMap.put("resultCode", CommonConstant.CODE_FAIL);
            tarGrpMap.put("resultMsg", "批量删除失败！");
        }
        return tarGrpMap;
    }


    class deleteTarGrpBatchTask implements Callable<Map<String, Object>> {
        private Long ruleId;
        private TarGrpDetail tarGrpDetail;

        public deleteTarGrpBatchTask(Long ruleId, TarGrpDetail tarGrpDetail) {
            this.ruleId = ruleId;
            this.tarGrpDetail = tarGrpDetail;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> map = new HashMap<>();
            MktStrategyConfRuleDO mktStrategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(ruleId);
            if (mktStrategyConfRuleDO.getTarGrpId() != null) {
                TarGrpDetail detail = (TarGrpDetail) redisUtils.get("TAR_GRP_" + mktStrategyConfRuleDO.getTarGrpId());
                if (detail == null) {
                    TarGrp tarGrp = tarGrpMapper.selectByPrimaryKey(mktStrategyConfRuleDO.getTarGrpId());
                    detail = BeanUtil.create(tarGrp, new TarGrpDetail());
                    List<TarGrpCondition> conditionList = tarGrpConditionMapper.listTarGrpCondition(mktStrategyConfRuleDO.getTarGrpId());
                    detail.setTarGrpConditions(conditionList);
                    redisUtils.set("TAR_GRP_" + tarGrp.getTarGrpId(), detail);
                }
                List<TarGrpCondition> tarGrpConditionList = new ArrayList<>();
                if (detail != null) {
                    List<TarGrpCondition> moreList = new ArrayList<>();
                    for (TarGrpCondition tarGrpCondition : detail.getTarGrpConditions()) {
                        for (TarGrpCondition tarGrpConditionNew : tarGrpDetail.getTarGrpConditions()) {
                            if (tarGrpCondition.getLeftParam().equals(tarGrpConditionNew.getLeftParam())) {
                                tarGrpService.delTarGrpCondition(tarGrpCondition.getConditionId());
                            }
                        }
                    }
                }
            }
            return map;
        }
    }


    /**
     * 批量保存推荐条目
     *
     * @param ruleIdList
     * @param camitemIdList
     * @return
     */
    @Override
    public Map<String, Object> insertCamItemBatch(List<Integer> ruleIdList, List<Integer> camitemIdList) {
        List<Long> ruleIdLongList = listInteger2Long(ruleIdList);
        List<Long> camitemIdLongList = listInteger2Long(camitemIdList);
        Map tarGrpMap = new HashMap();
        //初始化结果集
        List<Future<Map<String, Object>>> threadList = new ArrayList<>();
        //初始化线程池
        Future<Map<String, Object>> camItemFuture = null;
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Long ruleId : ruleIdLongList) {
            camItemFuture = executorService.submit(new insertCamItemBatch(ruleId, camitemIdLongList));
            threadList.add(camItemFuture);
        }
        List<Long> ruleIdListNew = new ArrayList<>();
        try {
            for (Future<Map<String, Object>> camItemFutureNew : threadList) {
                if (camItemFutureNew != null && camItemFutureNew.get() != null) {
                    Long ruleId = (Long) camItemFutureNew.get().get("ruleId");
                    ruleIdLongList.add(ruleId);
                }
                tarGrpMap.put("ruleIdList", ruleIdListNew);
                tarGrpMap.put("resultCode", CommonConstant.CODE_SUCCESS);
                tarGrpMap.put("resultMsg", "批量插入销售品成功!");
            }
        } catch (Exception e) {
            tarGrpMap.put("resultCode", CommonConstant.CODE_FAIL);
            tarGrpMap.put("resultMsg", "批量插入销售品失败!");
            logger.error("[op:insertCamItemBatch],批量插入销售品失败! Exception = ", e);
        }
        return tarGrpMap;
    }


    class insertCamItemBatch implements Callable<Map<String, Object>> {
        private Long ruleId;
        private List<Long> camitemIdList;

        public insertCamItemBatch(Long ruleId, List<Long> camitemIdList) {
            this.ruleId = ruleId;
            this.camitemIdList = camitemIdList;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map map = new HashMap();
            MktStrategyConfRuleDO mktStrategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(ruleId);
            String productIdsNew = mktStrategyConfRuleDO.getProductId();
            for (int i = 0; i < camitemIdList.size(); i++) {
                if (i == 0 && (productIdsNew == null || "".equals(productIdsNew))) {
                    productIdsNew += camitemIdList.get(i);
                } else {
                    productIdsNew += "/" + camitemIdList.get(i);
                }
            }
            logger.info("productIdsNew = " + productIdsNew);
            mktStrategyConfRuleDO.setProductId(productIdsNew);
            mktStrategyConfRuleDO.setUpdateStaff(UserUtil.loginId());
            mktStrategyConfRuleDO.setUpdateDate(new Date());
            mktStrategyConfRuleMapper.updateByPrimaryKey(mktStrategyConfRuleDO);
            // redisUtils.del("MKT_RULE_" + ruleId);
            map.put("ruleId", mktStrategyConfRuleDO.getMktStrategyConfRuleId());
            return map;
        }
    }


    /**
     * 批量更新推荐条目
     *
     * @param ruleIdList
     * @param camitemIdList
     * @return
     */
    @Override
    public Map<String, Object> updateCamItemBatch(List<Integer> ruleIdList, List<Integer> camitemIdList) {
        List<Long> ruleIdLongList = listInteger2Long(ruleIdList);
        List<Long> camitemIdLongList = listInteger2Long(camitemIdList);
        Map tarGrpMap = new HashMap();
        //初始化结果集
        List<Future<Map<String, Object>>> threadList = new ArrayList<>();
        //初始化线程池
        Future<Map<String, Object>> camItemFuture = null;
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Long ruleId : ruleIdLongList) {
            camItemFuture = executorService.submit(new updateCamItemBatchTask(ruleId, camitemIdLongList));
            threadList.add(camItemFuture);
        }
        List<Long> ruleIdListNew = new ArrayList<>();
        try {
            for (Future<Map<String, Object>> camItemFutureNew : threadList) {
                if (camItemFutureNew != null && camItemFutureNew.get() != null) {
                    Long ruleId = (Long) camItemFutureNew.get().get("ruleId");
                    ruleIdLongList.add(ruleId);
                }
                tarGrpMap.put("ruleIdList", ruleIdListNew);
                tarGrpMap.put("resultCode", CommonConstant.CODE_SUCCESS);
                tarGrpMap.put("resultMsg", "批量修改销售品成功!");
            }
        } catch (Exception e) {
            tarGrpMap.put("resultCode", CommonConstant.CODE_FAIL);
            tarGrpMap.put("resultMsg", "批量修改销售品失败!");
            logger.error("[op:insertCamItemBatch],批量插入销售品失败! Exception = ", e);
        }
        return tarGrpMap;
    }


    class updateCamItemBatchTask implements Callable<Map<String, Object>> {
        private Long ruleId;
        private List<Long> camitemIdList;

        public updateCamItemBatchTask(Long ruleId, List<Long> camitemIdList) {
            this.ruleId = ruleId;
            this.camitemIdList = camitemIdList;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map map = new HashMap();
            MktStrategyConfRuleDO mktStrategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(ruleId);
            String productIds = mktStrategyConfRuleDO.getProductId();
            List<Long> productIdList = new ArrayList<>();
            List<MktCamItem> mktCamItemList = new ArrayList<>();
            if (productIds != null && !"".equals(productIds)) {
                String[] productIdArrary = productIds.split("/");
                for (String productId : productIdArrary) {
                    productIdList.add(Long.valueOf(productId));
                }
                // 获取原有的推送条目
                if (productIdList!=null && !productIdList.isEmpty()){
                    mktCamItemList = mktCamItemMapper.selectByBatch(productIdList);
                }
            }
            // 获取最新的推荐条目
            List<MktCamItem> mktCamItemListNew = new ArrayList<>();
            if (camitemIdList!=null && !camitemIdList.isEmpty()){
                mktCamItemListNew = mktCamItemMapper.selectByBatch(camitemIdList);
            }
            // 更新后的集合
            List<MktCamItem> updateList = new ArrayList<>();
            List<Long> delList = new ArrayList<>();
            for (int i = 0; i < mktCamItemList.size(); i++) {
                for (int j = 0; j < mktCamItemListNew.size(); j++) {
                    if (!mktCamItemList.get(i).getItemId().equals(mktCamItemListNew.get(j).getItemId()) && j == mktCamItemListNew.size() - 1) {
                        updateList.add(mktCamItemList.get(i));
                    } else if (mktCamItemList.get(i).getItemId().equals(mktCamItemListNew.get(j).getItemId())) {
                        updateList.add(mktCamItemListNew.get(j));
                        continue;
                    }
                }
            }
            String productIdsNew = "";
            for (int i = 0; i < updateList.size(); i++) {
                if (i == 0) {
                    productIdsNew += updateList.get(i).getMktCamItemId();
                } else {
                    productIdsNew += "/" + updateList.get(i).getMktCamItemId();
                }
            }
            mktStrategyConfRuleDO.setProductId(productIdsNew);
            mktStrategyConfRuleDO.setUpdateStaff(UserUtil.loginId());
            mktStrategyConfRuleDO.setUpdateDate(new Date());
            mktStrategyConfRuleMapper.updateByPrimaryKey(mktStrategyConfRuleDO);
            //redisUtils.del("MKT_RULE_" + ruleId);
            map.put("ruleId", mktStrategyConfRuleDO.getMktStrategyConfRuleId());
            return map;
        }

    }


    /**
     * 批量删除推荐条目
     *
     * @param ruleIdList
     * @param camitemIdList
     * @return
     */
    @Override
    public Map<String, Object> deleteCamItemBatch(List<Integer> ruleIdList, List<Integer> camitemIdList) {
        List<Long> ruleIdLongList = listInteger2Long(ruleIdList);
        List<Long> camitemIdLongList = listInteger2Long(camitemIdList);
        Map tarGrpMap = new HashMap();
        //初始化结果集
        List<Future<Map<String, Object>>> threadList = new ArrayList<>();
        //初始化线程池
        Future<Map<String, Object>> camItemFuture = null;
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Long ruleId : ruleIdLongList) {
            camItemFuture = executorService.submit(new deleteCamItemBatchTask(ruleId, camitemIdLongList));
            threadList.add(camItemFuture);
        }
        List<Long> ruleIdListNew = new ArrayList<>();
        try {
            for (Future<Map<String, Object>> camItemFutureNew : threadList) {
                if (camItemFutureNew != null && camItemFutureNew.get() != null) {
                    Long ruleId = (Long) camItemFutureNew.get().get("ruleId");
                    ruleIdLongList.add(ruleId);
                }
                tarGrpMap.put("ruleIdList", ruleIdListNew);
                tarGrpMap.put("resultCode", CommonConstant.CODE_SUCCESS);
                tarGrpMap.put("resultMsg", "批量修改销售品成功!");
            }
        } catch (Exception e) {
            tarGrpMap.put("resultCode", CommonConstant.CODE_FAIL);
            tarGrpMap.put("resultMsg", "批量修改销售品失败!");
            logger.error("[op:insertCamItemBatch],批量插入销售品失败! Exception = ", e);
        }
        return tarGrpMap;
    }


    class deleteCamItemBatchTask implements Callable<Map<String, Object>> {
        private Long ruleId;
        private List<Long> camitemIdList;

        public deleteCamItemBatchTask(Long ruleId, List<Long> camitemIdList) {
            this.ruleId = ruleId;
            this.camitemIdList = camitemIdList;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map map = new HashMap();
            MktStrategyConfRuleDO mktStrategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(ruleId);
            String productIds = mktStrategyConfRuleDO.getProductId();
            List<Long> productIdList = new ArrayList<>();
            List<MktCamItem> mktCamItemList = new ArrayList<>();
            if (productIds != null && !"".equals(productIds)) {
                String[] productIdArrary = productIds.split("/");
                for (String productId : productIdArrary) {
                    // mktCamItemMapper.selectByPrimaryKey(Long.valueOf(productId))
                    productIdList.add(Long.valueOf(productId));
                }
                // 获取原有的推送条目
                if (productIdList!=null && !productIdList.isEmpty()){
                    mktCamItemList = mktCamItemMapper.selectByBatch(productIdList);
                }
            }
            // 获取最新的推荐条目
            List<MktCamItem> mktCamItemListNew = new ArrayList<MktCamItem>();
            if (camitemIdList!=null && !camitemIdList.isEmpty()){
                mktCamItemListNew  = mktCamItemMapper.selectByBatch(camitemIdList);
            }
            List<MktCamItem> delIdList = new ArrayList<>();
            for (int i = 0; i < mktCamItemList.size(); i++) {
                for (int j = 0; j < mktCamItemListNew.size(); j++) {
                    if (mktCamItemList.get(i).getItemId().equals(mktCamItemListNew.get(j).getItemId())) {
                        delIdList.add(mktCamItemList.get(i));
                        continue;
                    }
                }
            }
            mktCamItemList.removeAll(delIdList);
            String productIdsNew = "";
            for (int i = 0; i < mktCamItemList.size(); i++) {
                if (i == 0) {
                    productIdsNew += mktCamItemList.get(i).getMktCamItemId();
                } else {
                    productIdsNew += "/" + mktCamItemList.get(i).getMktCamItemId();
                }
            }
            mktStrategyConfRuleDO.setProductId(productIdsNew);
            mktStrategyConfRuleDO.setUpdateStaff(UserUtil.loginId());
            mktStrategyConfRuleDO.setUpdateDate(new Date());
            mktStrategyConfRuleMapper.updateByPrimaryKey(mktStrategyConfRuleDO);
            //redisUtils.del("MKT_RULE_" + ruleId);
            map.put("ruleId", mktStrategyConfRuleDO.getMktStrategyConfRuleId());
            return map;
        }
    }

    private List<Long> listInteger2Long(List<Integer> integerList) {
        List<Long> longList = new ArrayList<>();
        for (Integer integer : integerList) {
            longList.add(integer.longValue());
        }
        return longList;
    }


    @Override
    public Map<String, Object> getRuleTemplate(Long preStrategyConfId){
        Map<String, Object> ruleMap = new HashMap();
        List<MktStrategyConfRule> mktStrategyConfRuleList = new ArrayList<>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            List<MktStrategyConfRuleDO> mktStrategyConfRuleDOList = mktStrategyConfRuleMapper.selectByMktStrategyConfId(preStrategyConfId);
            List<Future<Map<String, Object>>> threadList = new ArrayList<>();
            Future<Map<String, Object>> ruleFuture = null;
            for (MktStrategyConfRuleDO mktStrategyConfRuleDO : mktStrategyConfRuleDOList) {
                ruleFuture = executorService.submit(new getRuleTemplateTask(mktStrategyConfRuleDO.getMktStrategyConfRuleId()));
                threadList.add(ruleFuture);
            }
            for (Future<Map<String, Object>> ruleFutureNew : threadList) {
                MktStrategyConfRule mktStrategyConfRule = (MktStrategyConfRule) ruleFutureNew.get().get("mktStrategyConfRule");
                mktStrategyConfRule.setMktStrategyConfRuleId(null);
                mktStrategyConfRuleList.add(mktStrategyConfRule);
            }
            ruleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            ruleMap.put("mktStrategyConfRuleList", mktStrategyConfRuleList);
        } catch (Exception e) {
            executorService.shutdown();
            logger.error("[op:MktStrategyConfRuleServiceImpl] failed to getRuleTemplate by preStrategyConfId = {}, Exception = ", preStrategyConfId, e);
            ruleMap.put("resultCode", CommonConstant.CODE_FAIL);
        } finally {
            executorService.shutdown();
        }
        return ruleMap;
    }

    class getRuleTemplateTask implements Callable<Map<String, Object>>{

        private Long preRuleId;

        public getRuleTemplateTask(Long preRuleId) {
            this.preRuleId = preRuleId;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> mktStrategyConfRuleMap = new HashMap<>();
            try {
                MktStrategyConfRuleDO mktStrategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(preRuleId);
                MktStrategyConfRule mktStrategyConfRule = BeanUtil.create(mktStrategyConfRuleDO, new MktStrategyConfRule());
                /**
                 * 客户分群配置
                 */
                Map<String, Object> tarGrpMap = new HashMap<>();
                tarGrpMap = tarGrpService.copyTarGrp(mktStrategyConfRuleDO.getTarGrpId(), false);
                if (tarGrpMap != null) {
                    TarGrp tarGrp = (TarGrp) tarGrpMap.get("tarGrp");
                    if (tarGrp != null) {
                        mktStrategyConfRule.setTarGrpId(tarGrp.getTarGrpId());
                    }
                }

                /**
                 * 协同渠道配置
                 */
                List<MktCamChlConfDetail> mktCamChlConfDetailList = new ArrayList<>();
                if (mktStrategyConfRuleDO.getEvtContactConfId() != null) {
                    String[] evtContactConfIds = mktStrategyConfRuleDO.getEvtContactConfId().split("/");
                    if (evtContactConfIds != null && !"".equals(evtContactConfIds[0])) {
                        for (int i = 0; i < evtContactConfIds.length; i++) {
                            if (evtContactConfIds[i] != "" && !"".equals(evtContactConfIds[i])) {
                                Map<String, Object> mktCamChlConfDOMap = mktCamChlConfService.copyMktCamChlConf(Long.valueOf(evtContactConfIds[i]));
                                MktCamChlConfDetail mktCamChlConfDetail = (MktCamChlConfDetail) mktCamChlConfDOMap.get("mktCamChlConfDetail");
                                mktCamChlConfDetailList.add(mktCamChlConfDetail);
                            }
                        }
                    }
                }
                mktStrategyConfRule.setMktCamChlConfDetailList(mktCamChlConfDetailList);

                /**
                 * 二次协同结果
                 */
                List<MktCamChlResult> mktCamChlResultList = new ArrayList<>();
                if ( mktStrategyConfRuleDO.getMktCamChlResultId()!=null && !"".equals( mktStrategyConfRuleDO.getMktCamChlResultId())){
                    String[] mktCamChlResultIds = mktStrategyConfRuleDO.getMktCamChlResultId().split("/");
                    if (mktCamChlResultIds != null && !"".equals(mktCamChlResultIds[0])) {
                        for (int i = 0; i < mktCamChlResultIds.length; i++) {
                            Map<String, Object> mktCamChlResultDOMap = mktCamChlResultService.copyMktCamChlResult(Long.valueOf(mktCamChlResultIds[i]));
                            MktCamChlResult mktCamChlResult = (MktCamChlResult) mktCamChlResultDOMap.get("mktCamChlResult");
                            mktCamChlResultList.add(mktCamChlResult);
                        }
                        mktStrategyConfRule.setMktCamChlResultList(mktCamChlResultList);
                    }
                }
                mktStrategyConfRuleMap.put("mktStrategyConfRule", mktStrategyConfRule);
                mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            } catch (Exception e) {
                logger.error("[op:MktStrategyConfRuleServiceImpl] failed to get RuleTemplateTask by preRuleId = {},  Exception=", preRuleId, e);
                mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_FAIL);
            }
            return mktStrategyConfRuleMap;
        }
    }



    /**
     * 调整活动-复制策略规则
     *
     * @param parentMktStrategyConfRuleId
     * @return
     */
    @Override
    public Map<String, Object> copyMktStrategyConfRuleForAdjust(Long parentMktStrategyConfRuleId, Long parentMktCampaignId, Long newMktCampaignId, Map<Long, Long> itemMap) throws Exception {
        Map<String, Object> mktStrategyConfRuleMap = new HashMap<>();
        try {
            MktStrategyConfRuleDO mktStrategyConfRuleDO = mktStrategyConfRuleMapper.selectByPrimaryKey(parentMktStrategyConfRuleId);
            MktStrategyConfRuleDO chiledMktStrategyConfRuleDO = new MktStrategyConfRuleDO();
            /**
             * 客户分群配置
             */
            //判断是否为发布操作
            Map<String, Object> tarGrpMap = tarGrpService.copyTarGrp(mktStrategyConfRuleDO.getTarGrpId(), false);
            TarGrp tarGrp = (TarGrp) tarGrpMap.get("tarGrp");
            /**
             * 销售品配置
             */
            String childProductIds = "";
            if (mktStrategyConfRuleDO.getProductId() != null) {
                String[] productIds = mktStrategyConfRuleDO.getProductId().split("/");
                if (productIds != null && !"".equals(productIds[0])) {
                    for (int i = 0; i <productIds.length ; i++) {
                        MktCamItem mktCamItem = mktCamItemMapper.selectByPrimaryKey(Long.valueOf(productIds[i]));
                        if (mktCamItem != null) {
                            List<MktCamItem> mktCamItemList = mktCamItemMapper.selectByCampaignIdAndItemIdAndType(mktCamItem.getItemId(), parentMktCampaignId, mktCamItem.getItemType());
                            if (mktCamItemList != null && !mktCamItemList.isEmpty() && itemMap != null) {
                                if (i == 0){
                                    childProductIds += itemMap.get(mktCamItemList.get(0).getMktCamItemId());
                                } else {
                                    childProductIds += "/" + itemMap.get(mktCamItemList.get(0).getMktCamItemId());;
                                }
                            }
                        }
                    }
                }
            }


            /**
             * 协同渠道配置
             */
            String childEvtContactConfIds = "";
            if (mktStrategyConfRuleDO.getEvtContactConfId() != null) {
                String[] evtContactConfIds = mktStrategyConfRuleDO.getEvtContactConfId().split("/");
                if (evtContactConfIds != null && !"".equals(evtContactConfIds[0])) {
                    for (int i = 0; i < evtContactConfIds.length; i++) {
                        if (evtContactConfIds[i] != "" && !"".equals(evtContactConfIds[i])) {
                            Map<String, Object> mktCamChlConfDOMap = mktCamChlConfService.copyMktCamChlConfForAdjust(Long.valueOf(evtContactConfIds[i]), newMktCampaignId);
                            MktCamChlConfDetail mktCamChlConfDetail = (MktCamChlConfDetail) mktCamChlConfDOMap.get("mktCamChlConfDetail");
                            if (i == 0) {
                                childEvtContactConfIds += mktCamChlConfDetail.getEvtContactConfId();
                            } else {
                                childEvtContactConfIds += "/" + mktCamChlConfDetail.getEvtContactConfId();
                            }
                        }
                    }
                }
            }

            /**
             * 二次协同结果
             */
            if ( mktStrategyConfRuleDO.getMktCamChlResultId()!=null &&  !mktStrategyConfRuleDO.getMktCamChlResultId().equals("")){
                String[] mktCamChlResultIds = mktStrategyConfRuleDO.getMktCamChlResultId().split("/");
                String childMktCamChlResultIds = "";
                if (mktCamChlResultIds != null && !"".equals(mktCamChlResultIds[0])) {
                    for (int i = 0; i < mktCamChlResultIds.length; i++) {
                        Map<String, Object> mktCamChlResultDOMap = mktCamChlResultService.copyMktCamChlResultForAdjust(Long.valueOf(mktCamChlResultIds[i]), newMktCampaignId);
                        MktCamChlResult mktCamChlResult = (MktCamChlResult) mktCamChlResultDOMap.get("mktCamChlResult");
                        if (i == 0) {
                            childMktCamChlResultIds += mktCamChlResult.getMktCamChlResultId();
                        } else {
                            childMktCamChlResultIds += "/" + mktCamChlResult.getMktCamChlResultId();
                        }
                    }
                }
                chiledMktStrategyConfRuleDO.setMktCamChlResultId(childMktCamChlResultIds);
            }
            chiledMktStrategyConfRuleDO.setInitId(mktStrategyConfRuleDO.getInitId());
            chiledMktStrategyConfRuleDO.setMktStrategyConfRuleName(mktStrategyConfRuleDO.getMktStrategyConfRuleName());
            if (tarGrp != null) {
                chiledMktStrategyConfRuleDO.setTarGrpId(tarGrp.getTarGrpId());
            }
            chiledMktStrategyConfRuleDO.setProductId(childProductIds);
            chiledMktStrategyConfRuleDO.setEvtContactConfId(childEvtContactConfIds);
            chiledMktStrategyConfRuleDO.setCreateDate(new Date());
            chiledMktStrategyConfRuleDO.setCreateStaff(UserUtil.loginId());
            chiledMktStrategyConfRuleDO.setUpdateDate(new Date());
            chiledMktStrategyConfRuleDO.setUpdateStaff(UserUtil.loginId());
            mktStrategyConfRuleMapper.insert(chiledMktStrategyConfRuleDO);
            mktStrategyConfRuleMap.put("mktStrategyConfRuleId", chiledMktStrategyConfRuleDO.getMktStrategyConfRuleId());
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_SUCCESS);
            mktStrategyConfRuleMap.put("resultMsg", "复制成功！");

            if (mktStrategyConfRuleDO.getProductId() != null) {
                String[] productIds = mktStrategyConfRuleDO.getProductId().split("/");
                if (productIds != null && !"".equals(productIds[0])) {
                    for (int i = 0; i <productIds.length ; i++) {
                        MktCamItem mktCamItem = mktCamItemMapper.selectByPrimaryKey(Long.valueOf(productIds[i]));
                        if (mktCamItem != null && CamItemType.DEPEND_PRODUCT.getValue().equals(mktCamItem.getItemType())) {
                            List<MktCamItem> mktCamItemList = mktCamItemMapper.selectByCampaignIdAndItemIdAndType(mktCamItem.getItemId(), parentMktCampaignId, mktCamItem.getItemType());
                            if (mktCamItemList != null && !mktCamItemList.isEmpty() && itemMap != null) {
                                Long aLong = itemMap.get(mktCamItemList.get(0).getMktCamItemId());
                                productService.copyMktProductAttr4Rule(mktCamItem.getMktCamItemId(),aLong
                                        ,mktStrategyConfRuleDO.getMktStrategyConfRuleId(),chiledMktStrategyConfRuleDO.getMktStrategyConfRuleId());
                            }
                        }
                    }
                }
            }
            //电子券复制
            productService.copyMktCamResource4Rule(newMktCampaignId,mktStrategyConfRuleDO.getMktStrategyConfRuleId(),chiledMktStrategyConfRuleDO.getMktStrategyConfRuleId());
        } catch (Exception e) {
            logger.error("[op:MktStrategyConfRuleServiceImpl] failed to copyMktStrategyConfRule by parentMktStrategyConfRuleId = {},  Exception=", parentMktStrategyConfRuleId, e);
            mktStrategyConfRuleMap.put("resultCode", CommonConstant.CODE_FAIL);
            mktStrategyConfRuleMap.put("resultMsg", "复制失败！");
        }
        return mktStrategyConfRuleMap;
    }

}