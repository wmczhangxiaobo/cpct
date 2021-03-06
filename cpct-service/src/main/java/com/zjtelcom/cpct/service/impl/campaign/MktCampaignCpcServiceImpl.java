package com.zjtelcom.cpct.service.impl.campaign;

import com.alibaba.fastjson.JSON;
import com.ctzj.smt.bss.sysmgr.model.common.SysmgrResultObject;
import com.ctzj.smt.bss.sysmgr.model.dto.SystemUserDto;
import com.ctzj.smt.bss.sysmgr.privilege.service.dubbo.api.ISystemUserDtoDubboService;
import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.campaign.*;
import com.zjtelcom.cpct.dao.channel.ContactChannelMapper;
import com.zjtelcom.cpct.dao.channel.InjectionLabelMapper;
import com.zjtelcom.cpct.dao.channel.MktVerbalConditionMapper;
import com.zjtelcom.cpct.dao.channel.OfferMapper;
import com.zjtelcom.cpct.dao.filter.FilterRuleMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfRuleMapper;
import com.zjtelcom.cpct.dao.system.SysParamsMapper;
import com.zjtelcom.cpct.domain.Rule;
import com.zjtelcom.cpct.domain.RuleDetail;
import com.zjtelcom.cpct.domain.campaign.*;
import com.zjtelcom.cpct.domain.channel.*;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfRuleDO;
import com.zjtelcom.cpct.domain.system.SysParams;
import com.zjtelcom.cpct.dto.campaign.MktCamChlConf;
import com.zjtelcom.cpct.dto.campaign.MktCamChlConfDetail;
import com.zjtelcom.cpct.dto.campaign.MktCamChlResult;
import com.zjtelcom.cpct.dto.filter.FilterRuleModel;
import com.zjtelcom.cpct.dto.strategy.MktStrategyConfRuleRel;
import com.zjtelcom.cpct.enums.DttsMsgEnum;
import com.zjtelcom.cpct.enums.ParamKeyEnum;
import com.zjtelcom.cpct.service.MktCampaignResp;
import com.zjtelcom.cpct.service.MktStrConfRuleResp;
import com.zjtelcom.cpct.service.MktStrategyConfResp;
import com.zjtelcom.cpct.service.campaign.MktCampaignApiService;
import com.zjtelcom.cpct.service.campaign.MktDttsLogService;
import com.zjtelcom.cpct.service.dubbo.UCCPService;
import com.zjtelcom.cpct.util.BeanUtil;
import com.zjtelcom.cpct.util.CopyPropertiesUtil;
import com.zjtelcom.cpct.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;

/**
 * @Description:
 * @author: linchao
 * @date: 2018/07/17 11:11
 * @version: V1.0
 */
@Service
@Transactional
public class MktCampaignCpcServiceImpl implements MktCampaignApiService {

    private static final Logger logger = LoggerFactory.getLogger(MktCampaignCpcServiceImpl.class);

    /**
     * 营销活动
     */
    @Autowired
    private MktCampaignMapper mktCampaignMapper;
    /**
     * 系统参数
     */
    @Autowired
    private SysParamsMapper sysParamsMapper;
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

    @Autowired
    private ContactChannelMapper contactChannelMapper;
    /**
     * 策略配置规则Mapper
     */
    @Autowired
    private MktStrategyConfRuleMapper mktStrategyConfRuleMapper;
    /**
     * 首次协同
     */
    @Autowired
    private MktCamChlConfMapper mktCamChlConfMapper;

    @Autowired
    private FilterRuleMapper filterRuleMapper;

    @Autowired
    private MktCamChlResultMapper mktCamChlResultMapper;

    @Autowired
    private MktCamChlResultConfRelMapper mktCamChlResultConfRelMapper;

    @Autowired
    private MktVerbalConditionMapper mktVerbalConditionMapper;

    @Autowired
    private InjectionLabelMapper injectionLabelMapper;

    @Autowired
    private OfferMapper offerMapper;

    @Autowired(required = false)
    private ISystemUserDtoDubboService iSystemUserDtoDubboService;

    @Autowired(required = false)
    private UCCPService uccpService;

    @Autowired
    private MktDttsLogService mktDttsLogService;
    @Autowired
    private MktCamItemMapper mktCamItemMapper;

    @Override
    public Map<String, Object> qryMktCampaignDetail(Long mktCampaignId) throws Exception {
        // 获取活动基本信息
        MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCampaignId);
        Map<String, Object> maps = new HashMap<>();
        try {
            MktCampaignResp mktCampaignResp = BeanUtil.create(mktCampaignDO, new MktCampaignResp());
            // 获取所有的sysParam
            Map<String, String> paramMap = new HashMap<>();
            List<SysParams> sysParamList = sysParamsMapper.selectAll("", "");
            for (SysParams sysParams : sysParamList) {
                paramMap.put(sysParams.getParamKey() + sysParams.getParamValue(), sysParams.getParamName());
            }
            mktCampaignResp.setTiggerTypeValue(paramMap.
                    get(ParamKeyEnum.TIGGER_TYPE.getParamKey() + mktCampaignDO.getTiggerType()));
            mktCampaignResp.setMktCampaignCategoryValue(paramMap.
                    get(ParamKeyEnum.MKT_CAMPAIGN_CATEGORY.getParamKey() + mktCampaignDO.getMktCampaignCategory()));
            mktCampaignResp.setMktCampaignTypeValue(paramMap.
                    get(ParamKeyEnum.MKT_CAMPAIGN_TYPE.getParamKey() + mktCampaignDO.getMktCampaignType()));
            mktCampaignResp.setStatusCdValue(paramMap.
                    get(ParamKeyEnum.STATUS_CD.getParamKey() + mktCampaignDO.getStatusCd()));

            // 获取活动关联策略集合
            List<MktStrategyConfResp> mktStrategyConfRespList = new ArrayList<>();
            List<MktCamStrategyConfRelDO> mktCamStrategyConfRelDOList = mktCamStrategyConfRelMapper.selectByMktCampaignId(mktCampaignId);
            for (MktCamStrategyConfRelDO mktCamStrategyConfRelDO : mktCamStrategyConfRelDOList) {
                MktStrategyConfResp mktStrategyConfResp = getMktStrategyConf(mktCamStrategyConfRelDO.getStrategyConfId());
                mktStrategyConfRespList.add(mktStrategyConfResp);
            }
            mktCampaignResp.setMktStrategyConfRespList(mktStrategyConfRespList);
            maps.put("resultCode", CommonConstant.CODE_SUCCESS);
            maps.put("resultMsg", "success");
            maps.put("mktCampaignResp", mktCampaignResp);
        } catch (Exception e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            maps.put("resultMsg", "failed");
        }
        return maps;
    }

    @Override
    public Map<String, Object> salesOffShelf(Map<String, Object> map) {
        HashMap<String,Object> resuleMap = new HashMap<>();
        Date date = new Date();
        MktDttsLog mktDttsLog =new MktDttsLog();
        String preDay = DateUtil.getPreDay(1);
        Date preDate = DateUtil.string2DateTime4Day(preDay);
        String dateFormatStr = DateUtil.getDateFormatStr(date);
        List<Offer> offerList = offerMapper.selectOfferByOver(preDate,date);
        if (!offerList.isEmpty()){
            List<MktCamItem> mktCamItems = mktCamItemMapper.getMktCampaignById(offerList);
            if (!mktCamItems.isEmpty()){
                for (MktCamItem mktCamItem : mktCamItems) {
                    MktCampaignDO mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(mktCamItem.getMktCampaignId());
                    if (mktCampaignDO==null) continue;
                    if (mktCampaignDO.getCreateStaff()!=null){
                        try {
                            if (mktCampaignDO.getCreateStaff()!=null){
                                SysmgrResultObject<SystemUserDto> systemUserDtoSysmgrResultObject = iSystemUserDtoDubboService.qrySystemUserDto(mktCampaignDO.getCreateStaff(), new ArrayList<Long>());
                                if (systemUserDtoSysmgrResultObject != null && systemUserDtoSysmgrResultObject.getResultObject() != null) {
                                    String sysUserCode = systemUserDtoSysmgrResultObject.getResultObject().getSysUserCode();
                                    System.out.println("11111+sysUserCode:"+sysUserCode+"123444324 lanId:"+mktCampaignDO.getLanId());
//                                    String sendContent = "您好，您的销售品（" + mktCamItem.getOfferName() + "）马上将要到期，如要延期请登录延期页面进行延期。";
                                    String sendContent = "您好，活动编码："+mktCampaignDO.getMktActivityNbr()+" 活动名称："+mktCampaignDO.getMktCampaignName()+" 中配置的推荐条目:"+ mktCamItem.getOfferName()+"将于"+dateFormatStr+"下架，为保证活动营销正常，请您尽快修改活动配置。";
                                    try {
                                        uccpService.sendShortMessage(sysUserCode,sendContent,mktCampaignDO.getLanId().toString(),DttsMsgEnum.CAMPAIGN.getId());
                                        mktDttsLogService.saveMktDttsLog("9000","成功",date,new Date(),"成功",null);
                                        resuleMap.put("code","200");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        resuleMap.put("code","500");
                                        mktDttsLogService.saveMktDttsLog("9000","成功",date,new Date(),"失败",e.toString());
                                    }
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            resuleMap.put("code","500");
                            mktDttsLogService.saveMktDttsLog("9000","失败",date,new Date(),"失败",e.toString());
                        }
                    }
                }

            }
        }
        return resuleMap;
    }

    /**
     * 查询配置配置信息
     *
     * @param mktStrategyConfId
     * @return
     */

    public MktStrategyConfResp getMktStrategyConf(Long mktStrategyConfId) throws Exception {
        MktStrategyConfResp mktStrategyConfResp = new MktStrategyConfResp();
        //查出获取所有的城市信息, 设成全局Map
/*
        Map<Integer, String> cityMap = new HashMap<>();
        List<SysArea> sysAreaAllList = sysAreaMapper.selectAll();
        for (SysArea sysArea : sysAreaAllList) {
            cityMap.put(sysArea.getAreaId(), sysArea.getName());
        }
*/

        //更具Id查询策略配置信息
        MktStrategyConfDO mktStrategyConfDO = mktStrategyConfMapper.selectByPrimaryKey(mktStrategyConfId);
        CopyPropertiesUtil.copyBean2Bean(mktStrategyConfResp, mktStrategyConfDO);
        // 适用城市
/*
        List<Integer> areaIdList = new ArrayList<>();
        List<SysArea> sysAreaList = new ArrayList<>();
        String[] areaIds = mktStrategyConfDO.getAreaId().split("/");
        if (areaIds != null && !"".equals(areaIds[0])) {
            for (String areaId : areaIds) {
                areaIdList.add(Integer.valueOf(areaId));
                SysArea sysArea = (SysArea) redisUtils.get("CITY_" + areaId);
                sysAreaList.add(sysArea);
            }
            mktStrategyConfResp.setAreaList(sysAreaList);
        }
*/

        // 策略适用渠道
/*
        List<channel> channelsList = new ArrayList<>();
        String[] channelIds = mktStrategyConfDO.getChannelsId().split("/");
        for (String channelId : channelIds) {
            channel channel = contactChannelMapper.selectByPrimaryKey(Long.valueOf(channelId));
            channelsList.add(channel);
        }
        mktStrategyConfResp.setChannelsList(channelsList);

*/

        // 获取过滤规则集合
        List<FilterRuleModel> filterRuleModels = filterRuleMapper.selectFilterRuleByStrategyId(mktStrategyConfId);
        mktStrategyConfResp.setFilterRuleModelList(filterRuleModels);
        /*        List<Long> filterRuleIdList = mktStrategyFilterRuleRelMapper.selectByStrategyId(mktStrategyConfId);
        List<FilterRuleModel> filterRuleModelList = new ArrayList<>();
        for (Long filterRuleId : filterRuleIdList) {
            = new FilterRuleModel();
            FilterRule filterRule = filterRuleMapper.selectByPrimaryKey(filterRuleId);
            if ("2000".equals(filterRule.getFilterType())){
                MktVerbalCondition mktVerbalCondition = mktVerbalConditionMapper.selectByPrimaryKey(filterRule.getConditionId());
                FilterRuleModel filterRuleModel = BeanUtil.create(filterRule, new FilterRuleModel());

                // 查询标签名称

                filterRuleModel.setOperType(mktVerbalCondition.getOperType());
                filterRuleModel.setLabelCode(mktVerbalCondition.get);
  *//*              filterRuleModel
                filterRuleModel
                        filterRuleModel
                filterRuleModel*//*
            }
            //filterRuleList.add(filterRule);
        }*/



        //查询与策略匹配的所有规则
        List<MktStrConfRuleResp> mktStrConfRuleRespList = new ArrayList<>();
        List<MktStrategyConfRuleDO> mktStrategyConfRuleDOList = mktStrategyConfRuleMapper.selectByMktStrategyConfId(mktStrategyConfId);
        List<MktStrategyConfRuleRel> mktStrategyConfRuleRelList = new ArrayList<>();
        for (MktStrategyConfRuleDO mktStrategyConfRuleDO : mktStrategyConfRuleDOList) {
            MktStrConfRuleResp mktStrConfRuleResp = BeanUtil.create(mktStrategyConfRuleDO, new MktStrConfRuleResp());

/*
            // 目标分群
            List<TarGrpConditionVO> tarGrpConditionVOList = listTarGrpCondition(mktStrategyConfRuleDO.getTarGrpId());
            mktStrConfRuleResp.setTarGrpConditionList(tarGrpConditionVOList);

            // 销售品
            if (mktStrategyConfRuleDO.getProductId() != null) {
                String[] productIds = mktStrategyConfRuleDO.getProductId().split("/");
                List<Long> productIdList = new ArrayList<>();
                List<Offer> offerList = new ArrayList<>();
                for (int i = 0; i < productIds.length; i++) {
                    if (productIds[i] != "" && !"".equals(productIds[i])) {
                        productIdList.add(Long.valueOf(productIds[i]));
                        MktCamItem mktCamItem = mktCamItemMapper.selectByPrimaryKey(Long.valueOf(productIds[i]));
                        Offer offer = offerMapper.selectByPrimaryKey(mktCamItem.getItemId().intValue());
                        offerList.add(offer);
                    }
                }
                mktStrConfRuleResp.setOfferList(offerList);
            }
*/

            if (mktStrategyConfRuleDO.getEvtContactConfId() != null) {
                String[] evtContactConfIds = mktStrategyConfRuleDO.getEvtContactConfId().split("/");
                List<MktCamChlConfDetail> mktCamChlConfDetailList = new ArrayList<>();
                List<MktCamChlConf> mktCamChlConfList = new ArrayList<>();
                for (int i = 0; i < evtContactConfIds.length; i++) {
                    if (evtContactConfIds[i] != "" && !"".equals(evtContactConfIds[i])) {
                        MktCamChlConfDO mktCamChlConfDO = mktCamChlConfMapper.selectByPrimaryKey(Long.valueOf(evtContactConfIds[i]));
                        MktCamChlConf mktCamChlConf = BeanUtil.create(mktCamChlConfDO, new MktCamChlConf());
                        mktCamChlConfList.add(mktCamChlConf);
                        MktCamChlConfDetail mktCamChlConfDetail = getMktCamChlConf(Long.valueOf(evtContactConfIds[i]));
                        // 获取触点渠道编码
                        Channel channel = contactChannelMapper.selectByPrimaryKey(mktCamChlConfDetail.getContactChlId());
                        if(channel!=null){
                            mktCamChlConfDetail.setContactChlCode(channel.getContactChlCode());
                        }
                        mktCamChlConfDetailList.add(mktCamChlConfDetail);
                    }
                }
                mktStrConfRuleResp.setMktCamChlConfDetailList(mktCamChlConfDetailList);
            }

            if (mktStrategyConfRuleDO.getMktCamChlResultId() != null) {
                String[] mktCamChlResultIds = mktStrategyConfRuleDO.getMktCamChlResultId().split("/");
                List<MktCamChlResult> mktCamChlResultList = new ArrayList<>();
                for (int i = 0; i < mktCamChlResultIds.length; i++) {
                    if (mktCamChlResultIds[i] != null && !"".equals(mktCamChlResultIds[i])) {
                        MktCamChlResultDO mktCamChlResultDO = mktCamChlResultMapper.selectByPrimaryKey(Long.valueOf(mktCamChlResultIds[i]));
                        MktCamChlResult mktCamChlResult = BeanUtil.create(mktCamChlResultDO, new MktCamChlResult());
                        List<MktCamChlResultConfRelDO> mktCamChlResultConfRelDOList = mktCamChlResultConfRelMapper.selectByMktCamChlResultId(mktCamChlResultDO.getMktCamChlResultId());
                        List<MktCamChlConfDetail> mktCamChlConfDetailList = new ArrayList<>();
                        for (MktCamChlResultConfRelDO mktCamChlResultConfRelDO : mktCamChlResultConfRelDOList) {
                            MktCamChlConfDetail mktCamChlConfDetail = getMktCamChlConf(mktCamChlResultConfRelDO.getEvtContactConfId());
                            mktCamChlConfDetailList.add(mktCamChlConfDetail);
                        }
                        mktCamChlResult.setMktCamChlConfDetailList(mktCamChlConfDetailList);
                        mktCamChlResultList.add(mktCamChlResult);
                    }
                }
                mktStrConfRuleResp.setMktCamChlResultList(mktCamChlResultList);
            }
            mktStrConfRuleRespList.add(mktStrConfRuleResp);
        }
        mktStrategyConfResp.setMktStrConfRuleRespList(mktStrConfRuleRespList);
        return mktStrategyConfResp;
    }


    public MktCamChlConfDetail getMktCamChlConf(Long evtContactConfId) {
        MktCamChlConfDO mktCamChlConfDO = mktCamChlConfMapper.selectByPrimaryKey(evtContactConfId);
//        List<MktCamChlConfAttrDO> mktCamChlConfAttrDOList = mktCamChlConfAttrMapper.selectByEvtContactConfId(evtContactConfId);
        MktCamChlConfDetail mktCamChlConfDetail = BeanUtil.create(mktCamChlConfDO, new MktCamChlConfDetail());
/*
        List<MktCamChlConfAttr> mktCamChlConfAttrList = new ArrayList<>();
        for (MktCamChlConfAttrDO mktCamChlConfAttrDO : mktCamChlConfAttrDOList) {
            MktCamChlConfAttr mktCamChlConfAttr = BeanUtil.create(mktCamChlConfAttrDO, new MktCamChlConfAttr());
            if (mktCamChlConfAttr.getAttrId().equals(ConfAttrEnum.RULE.getArrId())) {
                //通过EvtContactConfId获取规则放入属性中
                String rule = ruleSelect(mktCamChlConfAttr.getEvtContactConfId());
                mktCamChlConfAttr.setAttrValue(rule);
            }
            mktCamChlConfAttrList.add(mktCamChlConfAttr);
        }
        // 查询痛痒点话术列表
        Map<String, Object> verbalListMap = getVerbalListByConfId(UserUtil.loginId(), evtContactConfId);
        List<VerbalVO> verbalVOList = (List<VerbalVO>) verbalListMap.get("resultMsg");
        mktCamChlConfDetail.setVerbalVOList(verbalVOList);

        // 查询脚本
        CamScript camScript = camScriptMapper.selectByConfId(evtContactConfId);
        mktCamChlConfDetail.setCamScript(camScript);
        mktCamChlConfDetail.setMktCamChlConfAttrList(mktCamChlConfAttrList);
*/

        return mktCamChlConfDetail;
    }

    /**
     * 查询协同子策略规则并拼接格式
     *
     * @param evtContactConfId
     * @return
     */

    public String ruleSelect(Long evtContactConfId) {
        //唯一ID
        //查询出所有规则
        List<MktVerbalCondition> mktVerbalConditions = mktVerbalConditionMapper.findConditionListByVerbalId(evtContactConfId);

        List<MktVerbalCondition> labels = new ArrayList<>(); //标签因子
        List<MktVerbalCondition> expressions = new ArrayList<>(); //表达式

        //分类
        for (MktVerbalCondition mktVerbalCondition : mktVerbalConditions) {
            if ("1000".equals(mktVerbalCondition.getLeftParamType())) {
                labels.add(mktVerbalCondition);
            } else if ("2000".equals(mktVerbalCondition.getLeftParamType())) {
                expressions.add(mktVerbalCondition);
            }
        }
        Rule rule = parseRules(labels, expressions, 0);
        return JSON.toJSONString(rule);
    }

    /**
     * 递归查询规则
     *
     * @param labels
     * @param expressions
     * @param index
     * @return
     */

    public Rule parseRules(List<MktVerbalCondition> labels, List<MktVerbalCondition> expressions, int index) {
        Rule rule = new Rule();
        List<RuleDetail> ruleDetails = new ArrayList<>();
        RuleDetail ruleDetail;

        //遍历所有表达式
        if (expressions.size() > 0) {
            rule.setType(expressions.get(index).getOperType());
            for (int i = index; i < expressions.size(); i++) {
                //判断类型  如果不相同就进入下一级
                if (rule.getType().equals(expressions.get(i).getOperType())) {
                    for (MktVerbalCondition condition : labels) {
                        if (expressions.get(i).getLeftParam().equals(condition.getConditionId().toString()) || expressions.get(i).getRightParam().equals(condition.getConditionId().toString())) {
                            ruleDetail = new RuleDetail();
                            ruleDetail.setId(Integer.parseInt(condition.getLeftParam()));
                            //查询获取标签因子名称
                            Label label = injectionLabelMapper.selectByPrimaryKey(Long.parseLong(condition.getLeftParam()));
                            if (label != null) {
                                ruleDetail.setName(label.getInjectionLabelName());
                            } else {
                                ruleDetail.setName("");
                            }
                            ruleDetail.setContent(condition.getRightParam());
                            ruleDetail.setOperType(condition.getOperType());
                            ruleDetails.add(ruleDetail);
                        }
                    }
                } else {
                    rule.setRuleChildren(parseRules(labels, expressions, i));
                    break;
                }
            }
        }

        //判断是否是一个标签的情况
        if (labels.size() == 1) {
            rule.setType("1000");
            ruleDetail = new RuleDetail();
            ruleDetail.setId(Integer.parseInt(labels.get(0).getLeftParam()));
            //查询获取标签因子名称
            Label label = injectionLabelMapper.selectByPrimaryKey(Long.parseLong(labels.get(0).getLeftParam()));
            if (label != null) {
                ruleDetail.setName(label.getInjectionLabelName());
            } else {
                ruleDetail.setName("");
            }
            ruleDetail.setContent(labels.get(0).getRightParam());
            ruleDetail.setOperType(labels.get(0).getOperType());
            ruleDetails.add(ruleDetail);
        }

        if (ruleDetails.size() == 0) {
            return null;
        }

        rule.setListData(ruleDetails);
        return rule;
    }


    /**
     * 查询营销活动列表(分页)
     *
     * @param
     * @return
     */
/*    @Override
    public Map<String, Object> qryMktCampaignList(QryMktCampaignListReq qryMktCampaignListReq) throws Exception {
        Map<String, Object> qryMktCampaignListMap = new HashMap<>();
        // 获取分页信息，默认第1页，10条数据
        Integer page = 1;
        Integer pageSize = 10;
        if (qryMktCampaignListReq.getPageInfo() != null) {
            if (qryMktCampaignListReq.getPageInfo().getPage() != null && qryMktCampaignListReq.getPageInfo().getPage() != 0) {
                page = qryMktCampaignListReq.getPageInfo().getPage();
            }
            if (qryMktCampaignListReq.getPageInfo().getPageSize() != null && qryMktCampaignListReq.getPageInfo().getPageSize() != 0) {
                page = qryMktCampaignListReq.getPageInfo().getPageSize();
            }
        }

        MktCampaignDO mktCampaignDOReq = new MktCampaignDO();
        try {
            CopyPropertiesUtil.copyBean2Bean(mktCampaignDOReq, qryMktCampaignListReq);
        } catch (Exception e) {
            logger.error("[op:MktCampaignServiceImpl] falied to get mktCampaignDO = {}", JSON.toJSON(mktCampaignDOReq), e);
        }

        PageHelper.startPage(page, pageSize);
        List<MktCampaignDO> mktCampaignDOList = mktCampaignMapper.qryMktCampaignListByCondition(mktCampaignDOReq);
        Page pageInfo = new Page(new PageInfo(mktCampaignDOList));
        List<MktCampaignPO> mktCampaignList = new ArrayList<>();
        try {
            for (MktCampaignDO mktCampaignDOResp : mktCampaignDOList) {
                MktCampaignPO mktCampaign = new MktCampaignPO();
                CopyPropertiesUtil.copyBean2Bean(mktCampaign, mktCampaignDOResp);
                mktCampaignList.add(mktCampaign);
            }
        } catch (Exception e) {
            logger.error("[op:MktCampaignServiceImpl] falied to get mktCampaignList = {}", JSON.toJSON(mktCampaignList), e);
        }
        qryMktCampaignListMap.put("resultCode", CommonConstant.CODE_SUCCESS);
        qryMktCampaignListMap.put("resultMsg", "查询活动列表成功！");
        qryMktCampaignListMap.put("mktCampaigns", mktCampaignList);
        qryMktCampaignListMap.put("pageInfo", pageInfo);
        return qryMktCampaignListMap;
    }*/



}