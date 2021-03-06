package com.zjtelcom.cpct.service.impl.grouping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ccssoft.interfaceplatform.zj.module.service.ISaleService;
import com.ctzj.smt.bss.sysmgr.model.common.SysmgrResultObject;
import com.ctzj.smt.bss.sysmgr.model.dto.SystemUserDto;
import com.ctzj.smt.bss.sysmgr.privilege.service.dubbo.api.ISystemUserDtoDubboService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import com.zjtelcom.cpct.common.Page;
import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.campaign.*;
import com.zjtelcom.cpct.dao.channel.*;
import com.zjtelcom.cpct.dao.filter.CloseRuleMapper;
import com.zjtelcom.cpct.dao.filter.FilterRuleMapper;
import com.zjtelcom.cpct.dao.filter.MktStrategyCloseRuleRelMapper;
import com.zjtelcom.cpct.dao.grouping.ServicePackageMapper;
import com.zjtelcom.cpct.dao.grouping.TarGrpConditionMapper;
import com.zjtelcom.cpct.dao.grouping.TrialOperationMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfRuleMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfRuleRelMapper;
import com.zjtelcom.cpct.dao.system.SysAreaMapper;
import com.zjtelcom.cpct.dao.system.SysParamsMapper;
import com.zjtelcom.cpct.domain.SysArea;
import com.zjtelcom.cpct.domain.campaign.*;
import com.zjtelcom.cpct.domain.channel.*;
import com.zjtelcom.cpct.domain.grouping.GroupingVO;
import com.zjtelcom.cpct.domain.grouping.ServicePackage;
import com.zjtelcom.cpct.domain.grouping.TrialOperation;
import com.zjtelcom.cpct.domain.strategy.MktStrategyCloseRuleRelDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfRuleDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfRuleRelDO;
import com.zjtelcom.cpct.domain.system.SysParams;
import com.zjtelcom.cpct.dto.campaign.MktCamChlConfAttr;
import com.zjtelcom.cpct.dto.campaign.MktCamChlConfDetail;
import com.zjtelcom.cpct.dto.campaign.MktCamChlResult;
import com.zjtelcom.cpct.dto.channel.LabelDTO;
import com.zjtelcom.cpct.dto.channel.TransDetailDataVO;
import com.zjtelcom.cpct.dto.channel.VerbalVO;
import com.zjtelcom.cpct.dto.filter.CloseRule;
import com.zjtelcom.cpct.dto.filter.FilterRule;
import com.zjtelcom.cpct.dto.grouping.*;
import com.zjtelcom.cpct.dto.strategy.MktStrategyConfRule;
import com.zjtelcom.cpct.enums.*;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.MqService;
import com.zjtelcom.cpct.service.campaign.MktCamChlConfService;
import com.zjtelcom.cpct.service.campaign.MktDttsLogService;
import com.zjtelcom.cpct.service.channel.ProductService;
import com.zjtelcom.cpct.service.grouping.TrialOperationService;
import com.zjtelcom.cpct.service.impl.dubbo.CamCpcSpecialLogic;
import com.zjtelcom.cpct.service.org.OrgTreeService;
import com.zjtelcom.cpct.service.strategy.MktStrategyConfRuleService;
import com.zjtelcom.cpct.service.thread.MyThread;
import com.zjtelcom.cpct.util.*;
import com.zjtelcom.cpct.dao.offer.MktResourceProdMapper;
import com.zjtelcom.cpct.dao.offer.OfferProdMapper;
import com.zjtelcom.es.es.entity.*;
import com.zjtelcom.es.es.entity.model.LabelResultES;
import com.zjtelcom.es.es.entity.model.TrialOperationParamES;
import com.zjtelcom.es.es.entity.model.TrialResponseES;
import com.zjtelcom.es.es.service.EsService;
import com.zjtelcom.es.es.service.EsServiceInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;
import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;
import static com.zjtelcom.cpct.enums.ConfAttrEnum.*;
import static com.zjtelcom.cpct.enums.ConfAttrEnum.ISEE_LABEL_AREA_CUSTOMER;

@Service
public class TrialOperationServiceImpl extends BaseService implements TrialOperationService {

    @Autowired
    private TarGrpConditionMapper tarGrpConditionMapper;
    @Autowired
    private InjectionLabelMapper injectionLabelMapper;
    @Autowired
    private TrialOperationMapper trialOperationMapper;
    @Autowired
    private MktCampaignMapper campaignMapper;
    @Autowired
    private MktStrategyConfMapper strategyMapper;
    @Autowired
    private MktStrategyConfRuleRelMapper strategyConfRuleRelMapper;
    @Autowired(required = false)
    private RestTemplate restTemplate;
    @Autowired
    private MktStrategyConfRuleRelMapper ruleRelMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private MktStrategyConfRuleMapper ruleMapper;
    @Autowired
    private InjectionLabelMapper labelMapper;
    @Autowired
    private OfferProdMapper offerMapper;
    @Autowired
    private MktCamChlConfMapper chlConfMapper;
    @Autowired
    private MktCamScriptMapper scriptMapper;
    @Autowired
    private SysParamsMapper sysParamsMapper;
    @Autowired(required = false)
    private EsService esService;
    @Autowired
    private MktResourceProdMapper resourceMapper;
    @Autowired
    private MktStrategyConfMapper strategyConfMapper;
    @Autowired
    private MktCamCustMapper camCustMapper;
    @Autowired
    private FilterRuleMapper filterRuleMapper;
    @Autowired
    private RedisUtils_es redisUtils_es;
    @Autowired
    private MktCamChlConfMapper mktCamChlConfMapper;
    @Autowired
    private MktCamChlConfAttrMapper mktCamChlConfAttrMapper;
    @Autowired
    private MqService mqService;
    @Autowired
    private MktCamDisplayColumnRelMapper mktCamDisplayColumnRelMapper;
    @Autowired
    private ServicePackageMapper servicePackageMapper;
    @Autowired
    private CamCpcSpecialLogic camCpcSpecialLogic;
    @Value("${ctg.cpctTopic}")
    private String importTopic;

    @Autowired
    private MktDttsLogService mktDttsLogService;


//    private String ftpAddress = "134.108.3.130";
    private String ftpAddress = "134.108.0.93";
    private int ftpPort = 22;
    private String ftpName= "ftp";
    private String ftpPassword="V1p9*2_9%3#";
    private String excelIssurepath="/app/ftp/msc/userlist/fees";
    private String uploadExcelPath="/app/ftp/msc/userlist/fees/";

    private String downloadFilePath = "/app";

    /**
     * 销售品service
     */
    @Autowired
    private ProductService productService;
    /**
     * 规则Service
     */
    @Autowired
    private MktStrategyConfRuleService mktStrategyConfRuleService;
    /**
     * 推送渠道service
     */
    @Autowired
    private MktCamChlConfService mktCamChlConfService;

    @Autowired
    private MktCamItemMapper itemMapper;
    @Autowired(required = false)
    private ISystemUserDtoDubboService iSystemUserDtoDubboService;
    @Autowired
    private MktStrategyCloseRuleRelMapper strategyCloseRuleRelMapper;
    @Autowired
    private CloseRuleMapper closeRuleMapper;
    @Autowired(required = false)
    private EsServiceInfo esServiceInfo;
    @Autowired
    private OrgTreeService orgTreeService;
    @Autowired
    private ContactChannelMapper channelMapper;
    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private SysAreaMapper sysAreaMapper;
    @Autowired(required = false)
    private ISaleService iSaleService;
    //抽样展示全量试算记录
    @Override
    public Map<String, Object> showCalculationLog(Long id) {
        TrialOperation trialOperation = trialOperationMapper.selectByPrimaryKey(id);
        return esServiceInfo.showCalculationLog(trialOperation.getBatchNum().toString());

    }

    //xyl 补全mkt_campaign c4 c5 地市编码
    @Override
    public Map<String, Object> insertMktCampaignByC4AndC5() {
        List<Long> staffList = campaignMapper.getCreateStaffList();
        Long orgId = null;
        for (Long aLong : staffList) {
            SysmgrResultObject<SystemUserDto> systemUserDtoSysmgrResultObject = iSystemUserDtoDubboService.qrySystemUserDto(aLong, new ArrayList<Long>());
            if (systemUserDtoSysmgrResultObject != null) {
                if (systemUserDtoSysmgrResultObject.getResultObject()==null || systemUserDtoSysmgrResultObject.getResultObject().getStaffId()==null){
                    continue;
                }
                Long staffId = systemUserDtoSysmgrResultObject.getResultObject().getStaffId();
                List<Map<String, Object>> staffOrgId = organizationMapper.getStaffOrgId(staffId);
                if (!staffOrgId.isEmpty() && staffOrgId.size() > 0){
                    for (Map<String, Object> map : staffOrgId) {
                        Object orgDivision = map.get("orgDivision");
                        Object orgId1 = map.get("orgId");
                        if (orgDivision!=null){
                            if (orgDivision.toString().equals("30")) {
                                orgId = Long.valueOf(orgId1.toString());
                                break;
                            }else if (orgDivision.toString().equals("20")){
                                orgId = Long.valueOf(orgId1.toString());
                                break;
                            }else if (orgDivision.toString().equals("10")){
                                orgId = Long.valueOf(orgId1.toString());
                                break;
                            }
                        }
                    }
                }
                if (orgId==null) return null;
                Organization organization = organizationMapper.selectByPrimaryKey(orgId);
                if (organization!=null){
                    String orgNameC4 = organization.getOrgNameC4();
                    String orgNameC5 = organization.getOrgNameC5();
                    if (StringUtils.isNotBlank(orgNameC4) || StringUtils.isNotBlank(orgNameC5)){
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("orgNameC4",orgNameC4);
                        map.put("orgNameC5",orgNameC5);
                        map.put("aLong",aLong);
                        campaignMapper.updateByStaffToC4AndC5(map);
                    }
                }
            }
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("code","200");
        result.put("msg","补全mkt_campaign c4 c5 地市编码");
        return result;
    }

    @Override
    public Map<String, Object> insertMktCampaignByC4OfSysArea() {
        List<Long> lists =  campaignMapper.getByOrgNameC4IsNotNull();
        for (Long orgId : lists) {
            Organization organization = organizationMapper.selectByPrimaryKey(orgId);
            if (organization!=null){
                String orgNameC4 = organization.getOrgNameC4();
                Organization organizationC4 = organizationMapper.selectByPrimaryKey(Long.valueOf(orgNameC4));
                if (organizationC4!=null){
                    Long regionId = organizationC4.getRegionId();
                    if (regionId!=null){
                        SysArea sysArea =sysAreaMapper.getByCityFour(regionId.toString());
                        if (sysArea!=null && sysArea.getCityFour()!=null){
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("orgId",orgId);
                            map.put("areaId",sysArea.getAreaId());
                            campaignMapper.updateMktCampaignByC4OfSysArea(map);
                        }
                    }
                }
            }
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("code","200");
        result.put("msg","修改补全mkt_campaign c4 地市编码");
        return result;
    }


    private String getCreater(Long createStaff){
        String codeNumber = null;
        try {
            // 获取创建人信息
            SysmgrResultObject<SystemUserDto> systemUserDtoSysmgrResultObject = iSystemUserDtoDubboService.qrySystemUserDto(createStaff, new ArrayList<Long>());
            if (systemUserDtoSysmgrResultObject != null) {
                if (systemUserDtoSysmgrResultObject.getResultObject() != null) {
                    codeNumber = systemUserDtoSysmgrResultObject.getResultObject().getStaffCode();
                    codeNumber = codeNumber + "&&" + systemUserDtoSysmgrResultObject.getResultObject().getSysUserCode();
                    codeNumber = codeNumber + "&&" + systemUserDtoSysmgrResultObject.getResultObject().getStaffName();
                    // codeNumber = systemUserDtoSysmgrResultObject.getResultObject().getSysUserCode();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("创建人查询失败："+createStaff);
            codeNumber = null;
        }
        return codeNumber;
    }


    /**
     * 查询试算记录日志
     * @param batchId
     * @return
     */
    @Override
    public Map<String, Object> trialLog(Long batchId) {
        Map<String, Object> result = new HashMap<>();
        TrialOperation operation = trialOperationMapper.selectByPrimaryKey(batchId);
        if (operation == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "试运算记录不存在");
            return result;
        }
        TrialOperationVOES request = new TrialOperationVOES();
        request.setBatchNum(operation.getBatchNum());
        TrialResponseES responseES = new TrialResponseES();
        try {
            responseES = esService.trialLog(request);
        }catch (Exception e){
            logger.error("日志查询失败");
            e.printStackTrace();
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","日志查询失败");
            return result;
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",responseES.getHitsList());
        return result;
    }

    /**
     * 下发文件到生产sftp
     * @param batchId
     * @return
     */
    @Override
    public Map<String, Object> uploadFile(Long batchId, Long type) {
        Map<String, Object> result = new HashMap<>();
        TrialOperation operation = trialOperationMapper.selectByPrimaryKey(batchId);
        if (type==null){
            type = 0L;
        }
        if(type.toString().equals("1") && redisUtils_es.get( "SPECIFIEDNUM_" + operation.getBatchNum()) == null){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "没有选择合并数目，无法进行预下发，请直接全量下发");
            return result;
        }
        if (operation == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "试运算记录不存在");
            return result;
        }
        if (!TrialStatus.ALL_SAMPEL_SUCCESS.getValue().equals(operation.getStatusCd()) &&
                !TrialStatus.IMPORT_SUCCESS.getValue().equals(operation.getStatusCd())&&
                !TrialStatus.SPECIAL_PUBLISH_SUCCESS.getValue().equals(operation.getStatusCd())){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "不满足下发条件，无法操作");
            return result;
        }
        operation.setStatusCd(TrialStatus.UPLOAD_GOING.getValue());
        trialOperationMapper.updateByPrimaryKey(operation);
        TrialOperationVOES request = new TrialOperationVOES();
        request.setCampaignId(operation.getCampaignId());
        request.setStrategyId(operation.getStrategyId());
        request.setBatchNum(operation.getBatchNum());
        //区分预下发Y与全量下发
        redisUtils_es.set("TYPE_" + operation.getBatchNum().toString(),type);
        if(type.toString().equals("1")){
            redisUtils.set("SPECIAL_NUM_" + operation.getBatchNum().toString(),"1000");
        }else {
            redisUtils.set("SPECIAL_NUM_" + operation.getBatchNum().toString(),"2000");
        }
        new Thread(){
            public void  run(){
                try {
                    //todo 新的dubbo接口
                    TrialResponseES responseES = esService.uploadFile2Prod(request);
                }catch (Exception e){
                    e.printStackTrace();
                    logger.info("下发文件失败");
                }
            }
        }.start();
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "文件已下发，请稍后查看下发结果");
        return result;
    }

    /**
     * 标签查询非空总数
     * @param labelCodes
     * @return
     */
    @Override
    public Map<String, Object> searchCountByLabelList(String labelCodes) {
        Map<String, Object> result = new HashMap<>();
        String[] codeList = labelCodes.split(",");
        TrialResponseES response = new TrialResponseES();
        TrialOperationVOES request = new TrialOperationVOES();
        request.setFieldList(codeList);
        try {
//            response = restTemplate.postForObject("localhost:8090/es/searchCountByLabelList", request, TrialResponseES.class);
            if (!response.getResultCode().equals("200")){
                result.put("resultCode",CODE_FAIL);
                result.put("resultMsg","查询失败");
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","查询失败");
            return result;
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg","查询成功");
        result.put("total",response.getHitsList());
        return result;
    }

    @Override
    public Map<String, Object> conditionCheck(Map<String, Object> params) {
        Map<String,Object> result = new HashMap<>();

        List<TarGrpCondition> conditions = new ArrayList<>();
        if (params.get("conditionList")==null){
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg",0);
            return result;
        }
        Long ruleId = MapUtil.getLongNum(params.get("ruleId"));
        String orgCheck = redisUtils.get("ORG_CHECK_"+ruleId.toString())==null ? null :redisUtils.get("ORG_CHECK_"+ruleId.toString()).toString();
        if (orgCheck!=null && orgCheck.equals("false")){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "营销组织树配置正在努力加载请稍后再试");
            return result;
        }
        List<Map<String,Object>> conditionMap = (List<Map<String,Object>>)params.get("conditionList");
        for (Map<String,Object> map : conditionMap){
            TarGrpCondition condition = ChannelUtil.mapToEntity(map,TarGrpCondition.class);
            condition.setOperType(map.get("operType").toString());
            condition.setLeftParam(map.get("leftParam").toString());
            condition.setRightParam(map.get("rightParam").toString());
            condition.setUpdateStaff(Long.valueOf(map.get("updateStaff").toString()));
            conditions.add(condition);
        }
        String strategyArea = MapUtil.getString(params.get("strategyArea"));
        if (conditions.isEmpty() ){
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg",0);
            return result;
        }
        TrialOperationVO request = new TrialOperationVO();
        //生成批次号
        String batchNumSt = DateUtil.date2St4Trial(new Date()) + ChannelUtil.getRandomStr(4);
        request.setBatchNum(Long.valueOf(batchNumSt));
        request.setFieldList(new String[new ArrayList<String>().size()]);
        request.setSample(true);
        if (!strategyArea.equals("")){
            //添加策略适用地市
            Long strategyId = Long.valueOf(new Date().getTime()+ChannelUtil.getRandomStr(2));
            redisUtils.set("STRATEGY_CONF_AREA_"+strategyId,strategyArea);
            request.setStrategyId(strategyId);
        }
        TrialOperationVOES requests = BeanUtil.create(request, new TrialOperationVOES());
        ArrayList<TrialOperationParamES> paramList = new ArrayList<>();
        TrialOperationParamES param = getTrialOperationParamES(request, Long.valueOf(batchNumSt),  Long.valueOf(new Date().getTime()+ChannelUtil.getRandomStr(2)) + 1, true, conditions);
        param.setRuleId(ruleId);
        paramList.add(param);
        requests.setParamList(paramList);
        TrialResponseES response = new TrialResponseES();
        try {
            //todo
            System.out.println(JSON.toJSONString(requests));
            response = esService.searchBatchInfo(requests);
            if (response.getResultCode().equals(CODE_FAIL)){
                result.put("resultCode", CODE_SUCCESS);
                result.put("resultMsg", 0);
                return result;
            }
            //todo 返回信息结果封装
        } catch (Exception e) {
            e.printStackTrace();
            // 抽样试算失败
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg", 0);
            return result;
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", response.getTotal());
        return result;
    }



    /**
     * 抽样业务校验
     * @param operationVO
     * @return
     */
    @Override
    public Map<String, Object> businessCheck(TrialOperationVO operationVO) {
        Map<String, Object> result = new HashMap<>();
        MktCampaignDO campaign = campaignMapper.selectByPrimaryKey(operationVO.getCampaignId());
        if (campaign == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "活动信息有误");
            return result;
        }
        MktStrategyConfDO strategy = strategyMapper.selectByPrimaryKey(operationVO.getStrategyId());
        if (strategy==null){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "策略信息有误");
            return result;
        }
        //生成批次号
        String batchNumSt = DateUtil.date2St4Trial(new Date()) + ChannelUtil.getRandomStr(4);
        //添加策略适用地市
        redisUtils.set("STRATEGY_CONF_AREA_"+operationVO.getStrategyId(),strategy.getAreaId());


        // 通过活动id获取关联的标签字段数组
        List<Map<String,Object>> labelList =displayLabel(campaign);

        redisUtils.set("LABEL_DETAIL_"+batchNumSt,labelList);

        String[] fieldList = getStrings(campaign,strategy);

        TrialOperationVO request = BeanUtil.create(operationVO,new TrialOperationVO());
        //抽样业务校验
        request.setSample(false);
        TrialOperationVOES requests = BeanUtil.create(request,new TrialOperationVOES());

        ArrayList<TrialOperationParamES> paramList = new ArrayList<>();
        List<MktStrategyConfRuleRelDO> ruleRelList = ruleRelMapper.selectByMktStrategyConfId(operationVO.getStrategyId());
        for (MktStrategyConfRuleRelDO ruleRelDO : ruleRelList) {
            TrialOperationParamES param = getTrialOperationParamES(operationVO,Long.valueOf(batchNumSt), ruleRelDO.getMktStrategyConfRuleId(),true,null);
            paramList.add(param);
          Map<String,Object> stringObjectMap =  getProductAndChannelByRuleId(ruleRelDO.getMktStrategyConfRuleId());
          List<String> stringList = (List<String>) stringObjectMap.get("scriptLabel");
          fieldList = ChannelUtil.arrayInput(fieldList,stringList);
        }
        requests.setFieldList(fieldList);
        requests.setParamList(paramList);
        requests.setBatchNum(Long.valueOf(batchNumSt));
        TrialResponseES response = new TrialResponseES();

        try {
            //todo
            System.out.println(JSON.toJSONString(requests));
             response = esService.searchBatchInfo(requests);
//            response = restTemplate.postForObject("http://localhost:8080/es/searchBatchInfo", requests, TrialResponseES.class);

            if (response.getResultCode().equals(CODE_FAIL)){
                result.put("resultCode", CODE_FAIL);
                result.put("resultMsg", "抽样校验失败");
                return result;
            }
            //todo 返回信息结果封装
        } catch (Exception e) {
            e.printStackTrace();
            // 抽样试算失败
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "抽样校验失败");
            return result;
        }
        if (!response.getResultCode().equals(CODE_SUCCESS)){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "抽样校验失败");
            return result;
        }

        List<Map<String,Object>> customers = new ArrayList<>();
        //抽样数据结果拼装
        for (String ruleIdSt : response.getHitsList().keySet()){
            Long ruleId = Long.valueOf(ruleIdSt);
            List<Map<String,Object>> customerList = (List<Map<String,Object>>) response.getHitsList().get(ruleIdSt);
            Map<String,Object> map = getProductAndChannelByRuleId(ruleId);
            for (Map<String,Object> customer : customerList) {
                String channelScript = map.get("channelTemp")==null? "" : map.get("channelTemp").toString();
                List<String> camList = subScript(channelScript);
                if (!camList.isEmpty()){
                    for (String code : camList){
                        channelScript = channelScript.replace("${" + code + "}$", customer.get(code)==null ? "" : customer.get(code).toString());
                    }
                    map.put("channel", channelScript);
                }
                //todo 目前只查杭州数据 后续加映射关系
                if(customer.get("LATN_NAME")==null){
                    customer.put("LATN_NAME","杭州");
                }
                customer.putAll(map);
                customers.add(customer);
            }
        }
        result.put("data",customers);
        // 抽样试算成功
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", null);
        return result;
    }




    public Map<String,Object> getProductAndChannelByRuleId(Long ruleId){
        Map<String,Object> result = new HashMap<>();
        List<String> scriptList = new ArrayList<>();
        //添加规则下的销售品
        MktStrategyConfRuleDO rule = ruleMapper.selectByPrimaryKey(ruleId);
        if (rule.getProductId()!=null && !rule.getProductId().equals("")){
            List<Long> itemIdList = ChannelUtil.StringToIdList(rule.getProductId());
            List<Long> productList = new ArrayList<>();
            List<String> resourceList = new ArrayList<>();
            for (Long itemId : itemIdList){
                MktCamItem item = itemMapper.selectByPrimaryKey(itemId);
                if (item==null){
                    continue;
                }
                if (item.getItemType().equals("1000")){
                    productList.add(itemId);
                }else if (item.getItemType().equals("3000")){
                    MktResource resource = resourceMapper.selectByPrimaryKey(item.getItemId());
                    if (resource!=null){
                        resourceList.add(resource.getMktResName());
                    }
                }
            }
            List<String> itemList = offerMapper.listByOfferIdList(itemIdList);
            result.put("product",ChannelUtil.StringList2String(itemList));
            result.put("resource",ChannelUtil.StringList2String(resourceList));
        }
        StringBuffer st = new StringBuffer();
        if (rule.getEvtContactConfId()!=null && !rule.getEvtContactConfId().equals("")){
            List<Long> channelIdList = ChannelUtil.StringToIdList(rule.getEvtContactConfId());
            List<MktCamChlConfDO> chlConfList = chlConfMapper.listByIdList(channelIdList);
            for (MktCamChlConfDO chlConf : chlConfList){
                CamScript script = scriptMapper.selectByConfId(chlConf.getEvtContactConfId());
                if (script.getScriptDesc()!=null){
                    List<String> camList = subScript(script.getScriptDesc());
                    scriptList.addAll(camList);
                }
                st.append(chlConf.getEvtContactConfName()).append("(")
                        .append(script.getScriptDesc()==null? "" : script.getScriptDesc())
                        .append(")；");
            }
            result.put("channel",st.toString());
            result.put("channelTemp",st.toString());
        }
        result.put("scriptLabel",scriptList);
        //todo 渠道及推荐指引
        return result;
    }

    //截取脚本内的标签
    private List<String> subScript(String str) {
        List<String> result = new ArrayList<>();
//        Pattern p = Pattern.compile("\\$");
        Pattern p = Pattern.compile("(?<=\\$\\{)([^$]+)(?=\\}\\$)");
        Matcher m = p.matcher(str);
//        List<Integer> list = new ArrayList<>();

        while (m.find()) {
//            list.add(m.start());
            result.add(m.group(1));
        }

//        for (int i = 0; i < list.size(); ) {
//            result.add(str.substring(list.get(i) + 1, list.get(++i)));
//            i++;
//        }
        return result;
    }


    /**
     * ppm-文件下发
     * @param batchId
     * @return
     */
    @Override
    public Map<String, Object> importFromCust4Ppm(Long batchId) {
        Map<String, Object> result = new HashMap<>();
        TrialOperation trialOperation = trialOperationMapper.selectByPrimaryKey(batchId);
        if (trialOperation == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "试运算记录不存在");
            return result;
        }
        if (!trialOperation.getStatusCd().equals(TrialStatus.PPM_IMPORT_GOING.getValue())){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "无法导入");
            return result;
        }
        TrialOperationVO operation = BeanUtil.create(trialOperation,new TrialOperationVO());
        MktStrategyConfRuleDO confRule = ruleMapper.selectByPrimaryKey(operation.getStrategyId());
        if (confRule==null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "未找到有效的活动策略或规则");
            return result;
        }
        Long tarGrpTempId = null;
        Long ruleId = operation.getStrategyId();
        String batchNumSt = operation.getBatchNum().toString();
        List<Map<String,Object>> customerList = new ArrayList<>();
        List<Map<String,Object>> labelList = new ArrayList<>();
        List<Map<String, String>> labelMapList = tarGrpConditionMapper.selectAllLabelByTarId(confRule.getTarGrpId());
        if (!labelMapList.isEmpty()){
            tarGrpTempId = Long.valueOf(labelMapList.get(0).get("rightParam"));
            List<MktCamCust> camCustList = camCustMapper.selectByTarGrpTempId(tarGrpTempId);
            if (camCustList!=null && !camCustList.isEmpty()){
               JSONArray list = (JSONArray) JSON.parse(camCustList.get(0).getRemark());
               if (list!=null){
                   for (Object map : list){
                       labelList.add((Map<String, Object>) map);
                   }
               }
                for (MktCamCust camCust : camCustList){
                    Map<String,Object> jsonObject = (Map<String,Object>)JSON.parse(camCust.getAttrValue());
                    customerList.add(jsonObject);
                }
            }
        }
        return importUserList(result, operation, ruleId, batchNumSt, customerList, labelList);
    }


    private Map<String,Object> userList2Redis(int avg,int redisI,int fild,String batchNumSt,Long ruleId,List<Map<String,Object>> customerList){
        Map<String,Object> result = new HashMap<>();
        if (customerList.isEmpty()){
            result.put("redisI",redisI);
            result.put("listNum",fild);
            return result;
        }
        List<List<Map<String,Object>>> smallCustomers = ChannelUtil.averageAssign(customerList,avg);
        int listNum = fild;
        //按规则存储客户信息
        int number = listNum;
        for (int j = listNum,i = 0; j < avg + number && i < smallCustomers.size(); j++,i++) {
            redisUtils_es.hset("ISSURE_" + batchNumSt + "_" + ruleId + "_KEY_NUM_" + redisI, j + "", smallCustomers.get(i));
            System.out.println("*************插入得key："+"ISSURE_" + batchNumSt + "_" + ruleId + "_KEY_NUM_" + redisI);
            listNum++;
        }
        result.put("redisI",redisI+1);
        result.put("listNum",listNum);
        return result;
    }

    //下发文件
    private Map<String, Object> importUserList(Map<String, Object> result, TrialOperationVO operation, Long ruleId, String batchNumSt, List<Map<String, Object>> customerList, List<Map<String, Object>> labelList) {
        final TrialOperationVOES request = getTrialOperationVOES(operation, ruleId, batchNumSt, labelList);
        /*System.out.println(JSON.toJSONString(request));*/
        new Thread(){
            public void run(){
                try {
                    /*TrialResponseES responseES = esService.issueByFile(request);*/
                    List<FilterRule> productFilter = new ArrayList<>();
                    List<String> typeList = new ArrayList<>();
                    typeList.add("3000");
                    List<FilterRule> filterRuleList = filterRuleMapper.selectFilterRuleListByStrategyId(operation.getCampaignId(), typeList);
                    if (filterRuleList != null && !filterRuleList.isEmpty()) {
                        productFilter = filterRuleList;
                    }
                    TrialOperation trialOperation = BeanUtil.create(operation, new TrialOperation());
                    importListMQ2EsService(request, customerList, productFilter, batchNumSt, ruleId.toString(), trialOperation);
                }catch (Exception e){
                    e.printStackTrace();
                    logger.info("导入清单下发失败");
                }
            }
        }.start();
        result.put("resultCode", CommonConstant.CODE_SUCCESS);
        result.put("resultMsg", "导入成功,请稍后查看结果");
        return result;
    }

    private TrialOperationVOES getTrialOperationVOES(TrialOperationVO operation, Long ruleId, String batchNumSt, List<Map<String, Object>> labelList) {
        redisUtils_es.set("LABEL_DETAIL_" + batchNumSt, labelList);
        MktCampaignDO campaignDO = campaignMapper.selectByPrimaryKey(operation.getCampaignId());
        /*MktStrategyConfDO strategyConfDO = strategyConfMapper.selectByPrimaryKey(operation.getStrategyId());*/
        final TrialOperationVOES request = BeanUtil.create(operation, new TrialOperationVOES());
        request.setBatchNum(Long.valueOf(batchNumSt));
        request.setCampaignType(campaignDO.getMktCampaignType());
        request.setLanId(campaignDO.getLanId());
        request.setCampaignName(campaignDO.getMktCampaignName());
        request.setCamLevel(campaignDO.getCamLevel());
        request.setStrategyName(operation.getStrategyName());
        // 获取创建人员code
        request.setStaffCode(getCreater(campaignDO.getCreateStaff()) == null ? "null" : getCreater(campaignDO.getCreateStaff()));

        //获取销售品及规则列表
        TrialOperationParamES param = getTrialOperationParamES(operation, Long.valueOf(batchNumSt), ruleId, false, null);
        ArrayList<TrialOperationParamES> paramESList = new ArrayList<>();
        paramESList.add(param);
        request.setParamList(paramESList);
        return request;
    }

    private List<Map<String,Object>> displayLabel(MktCampaignDO campaign) {
        List<Map<String,Object>> labelList = new ArrayList<>();
        List<LabelDTO> labelDTOList = mktCamDisplayColumnRelMapper.selectLabelDisplayListByCamId(campaign.getMktCampaignId());
        if (labelDTOList==null){
            labelDTOList = new ArrayList<>();
        }
        String[] displays = new String[labelDTOList.size()];
        for (int i = 0 ; i< labelDTOList.size();i++){
            displays[i] = labelDTOList.get(i).getLabelCode();
            Map<String,Object> label = new HashMap<>();
            label.put("code",labelDTOList.get(i).getLabelCode());
            label.put("name",labelDTOList.get(i).getInjectionLabelName());
            label.put("labelType",labelDTOList.get(i).getLabelType());
            label.put("displayType",labelDTOList.get(i).getLabelDisplayType());
            label.put("labelDataType",labelDTOList.get(i).getLabelDataType());
            labelList.add(label);
        }
        return labelList;
    }

    private void  addLog2Es(String batchNum,String remark){
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("batchNum",batchNum);
            param.put("data",remark);
            esService.addLogByBatchNum(param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * 导入试运算清单
     */
    @Transactional(readOnly = false)
    @Override
    public Map<String, Object> importUserList(MultipartFile multipartFile, TrialOperationVO operation, Long ruleId,boolean isBusinessMkt) throws IOException {
        // 接单人标签索引
        int index = 0;
        Map<String, Object> result = new HashMap<>();
        String batchNumSt = DateUtil.date2St4Trial(new Date()) + ChannelUtil.getRandomStr(4);
        XlsxProcessAbstract xlsxProcess = new XlsxProcessAbstract();
        //InputStream inputStream = multipartFile.getInputStream();

        MktCampaignDO campaign = campaignMapper.selectByPrimaryKey(operation.getCampaignId());
        MktStrategyConfDO strategy = strategyMapper.selectByPrimaryKey(operation.getStrategyId());
        MktStrategyConfRuleDO confRule = ruleMapper.selectByPrimaryKey(ruleId);
        if (campaign == null || strategy == null || confRule == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "未找到有效的活动策略或规则");
            return result;
        }
        if (confRule.getEvtContactConfId()==null || confRule.getEvtContactConfId().equals("")){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "未查找到有效的渠道配置");
            return result;
        }
        campaign.setBatchType("1000");
        campaignMapper.updateByPrimaryKey(campaign);
        TrialOperation op = null;
        try {
            //添加红黑名单列表
            blackList2Redis(campaign);
            List<String> labelNameList = new ArrayList<>();
            List<String> labelEngNameList = new ArrayList<>();
            TransDetailDataVO dataVO;
            List<Map<String, Object>> labelList = new ArrayList<>();
            dataVO = xlsxProcess.processAllSheet(multipartFile);
            if (dataVO.contentList.size() - 3 < 0) {
                result.put("resultCode", CommonConstant.CODE_SUCCESS);
                result.put("resultMsg", "导入文件无数据！");
                return result;
            }
            String[] nameList = dataVO.getContentList().get(0).split("\\|@\\|");
            String[] codeList = dataVO.getContentList().get(1).split("\\|@\\|");

            logger.info("codeList: " + codeList.length);
            for (String s: codeList){
                System.out.println(s);//日志
            }
            logger.info("nameList: " + nameList.length);

            if (nameList.length != codeList.length) {
                result.put("resultCode", CODE_FAIL);
                result.put("resultMsg", "标签中文名个数与英文个数不匹配请重新检查文件");
                return result;
            }
            for (int i = 0; i < nameList.length; i++) {
                if (labelNameList.contains(nameList[i])) {
                    result.put("resultCode", CODE_FAIL);
                    result.put("resultMsg", "标签中文名称不能重复:" + "\"" + nameList[i] + "\"");
                    return result;
                }
                if (labelEngNameList.contains(codeList[i])) {
                    result.put("resultCode", CODE_FAIL);
                    result.put("resultMsg", "标签英文名称不能重复:" + "\"" + codeList[i] + "\"");
                    return result;
                }
                if (nameList[i].equals("接单人号码") || codeList[i].equals("SALE_EMP_NBR")) {
                    if(!(nameList[i].equals("接单人号码") && codeList[i].equals("SALE_EMP_NBR"))){
                        result.put("resultCode", CODE_FAIL);
                        result.put("resultMsg", "接单人号码标签名称错误");
                        return result;
                    }
                   index = i;
                }
                Map<String, Object> label = new HashMap<>();
                label.put("code", codeList[i]);
                label.put("name", nameList[i]);
                labelList.add(label);
                labelNameList.add(nameList[i]);
                labelEngNameList.add(codeList[i]);
            }
            List<String> fields = new ArrayList<>();


            List<Map<String, Object>> displayList = displayLabel(campaign);
            for (Map<String, Object> display : displayList) {
                String code = display.get("code") == null ? null : display.get("code").toString();
                String name = display.get("name") == null ? null : display.get("name").toString();
                if (code != null && !labelEngNameList.contains(code) && name != null && !labelNameList.contains(name)) {
                    Map<String, Object> label = new HashMap<>();
                    label.put("code", code);
                    label.put("name", name);
                    label.put("displayType", display.get("displayType"));
                    labelList.add(label);
                    fields.add(code);
                    labelEngNameList.add(code);
                    labelNameList.add(name);
                }
            }
            //查询活动下面所有渠道属性id是21和22的value
            List<String> attrValue = mktCamChlConfAttrMapper.selectAttrLabelValueByCampaignId(campaign.getMktCampaignId());
            if (attrValue != null && attrValue.size() > 0) {
                for (String attr : attrValue){
                    if (!fields.contains(attr)) {
                        fields.add(attr);
                    }
                }
            }
            // 服务包添加查询字段
            List<String> labels = mktCamChlConfAttrMapper.selectAttrLabelRemarkByCampaignId(campaign.getMktCampaignId());
            if(labels != null && labels.size() > 0){
                for (String s : labels) {
                    if (s != null && !labelEngNameList.contains(s)) {
                        Label label1= injectionLabelMapper.selectByLabelCode(s);
                        Map<String, Object> label = new HashMap<>();
                        label.put("code", s);
                        label.put("name", label1 == null? "":label1.getInjectionLabelName());
                        labelList.add(label);
                        fields.add(s);
                        labelEngNameList.add(s);
                    }
                }
            }
            if (!fields.isEmpty()) {
                redisUtils_es.set("DISPLAY_LABEL_" + campaign.getMktCampaignId(), fields);
            }
            List<Long> attrList = mktCamChlConfAttrMapper.selectByCampaignId(campaign.getMktCampaignId());
            if (attrList.contains(ISEE_CUSTOMER.getArrId()) || attrList.contains(ISEE_LABEL_CUSTOMER.getArrId()) || attrList.contains(SERVICE_PACKAGE.getArrId())){
                if (!labelEngNameList.contains("SALE_EMP_NBR")){
                    Map<String,Object> label = new HashMap<>();
                    label.put("code","SALE_EMP_NBR");
                    label.put("name","接单人号码");
                    label.put("labelDataType","1200");
                    labelList.add(label);
                }
            }
            if (attrList.contains(ISEE_AREA.getArrId()) || attrList.contains(ISEE_LABEL_AREA.getArrId())
                    || attrList.contains(ISEE_LABEL_AREA_CUSTOMER.getArrId())  ){
                if (!labelEngNameList.contains("AREA")){
                    Map<String,Object> label = new HashMap<>();
                    label.put("code","AREA");
                    label.put("name","派单区域");
                    label.put("labelDataType","1200");
                    labelList.add(label);
                }
            }
            redisUtils.set("LABEL_DETAIL_"+batchNumSt,labelList);

            if (labelList.size() > 87) {
                result.put("resultCode", CODE_FAIL);
                result.put("resultMsg", "扩展字段不能超过87个");
                return result;
            }
            List<MktStrategyCloseRuleRelDO> closeRuleRelDOS = strategyCloseRuleRelMapper.selectRuleByStrategyId(campaign.getMktCampaignId());
            //todo 关单规则配置信息
            if (closeRuleRelDOS!=null && !closeRuleRelDOS.isEmpty()){
                List<Map<String,Object>> closeRule = new ArrayList<>();
                for (MktStrategyCloseRuleRelDO ruleRelDO : closeRuleRelDOS){
                    CloseRule closeR = closeRuleMapper.selectByPrimaryKey(ruleRelDO.getRuleId());
                    if (closeR!=null){
                        Map<String,Object> ruleMap = new HashMap<>();
                        ruleMap.put("closeName",closeR.getCloseName());
                        ruleMap.put("closeCode",closeR.getCloseCode());
                        ruleMap.put("closeNbr",closeR.getExpression());
                        ruleMap.put("closeType",closeR.getCloseType());
                        closeRule.add(ruleMap);
                    }
                }
                redisUtils_es.set("CLOSE_RULE_"+campaign.getMktCampaignId(),closeRule);
            }
            TrialOperation trialOp = BeanUtil.create(operation, new TrialOperation());
            trialOp.setCampaignName(campaign.getMktCampaignName());
            //当清单导入时 strategyId name 存储规则信息
            trialOp.setStrategyId(confRule.getMktStrategyConfRuleId());
            trialOp.setStrategyName(confRule.getMktStrategyConfRuleName());
            trialOp.setBatchNum(Long.valueOf(batchNumSt));
            trialOp.setStatusCd(TrialStatus.IMPORT_GOING.getValue());
            trialOp.setStatusDate(new Date());
            trialOp.setCreateStaff(TrialCreateType.IMPORT_USER_LIST.getValue());
            trialOperationMapper.insert(trialOp);
            Long insertId = trialOp.getId();
            op = trialOp;
            int size = dataVO.contentList.size() - 3;
            /*Long landId = orgTreeService.getLandIdBySession();*/
            new MyThread(index) {
                public void run() {
                    try {
                        /*MktCamChlConfDO mktCamChlConfDO = mktCamChlConfMapper.selectByPrimaryKey(Long.valueOf(confRule.getEvtContactConfId()));
                        Channel channel = channelMapper.selectByPrimaryKey(mktCamChlConfDO.getContactChlId());
                        if ("QD00014".equals(channel.getContactChlCode())) {
                            if (!(Arrays.asList(nameList).containsAll(labelNameList) && Arrays.asList(codeList).contains(labelEngNameList))) {
                                addLog2Es(batchNumSt, "导入清单缺少渠道必填列");
                                TrialOperation record = new TrialOperation();
                                record.setId(Long.valueOf(insertId));
                                record.setStatusCd(TrialStatus.IMPORT_FAIL.getValue());
                                record.setRemark("清单导入数据错误");
                                int i = trialOperationMapper.updateByPrimaryKey(record);
                                throw new RuntimeException("导入清单缺少渠道必填列");
                            }
                        }*/
                        List<FilterRule> productFilter = new ArrayList<>();
                        final TrialOperationVOES request = getTrialOperationVOES(operation, ruleId, batchNumSt, labelList);
                        List<Map<String, Object>> customerList = new ArrayList<>();
                        //红黑名单过滤
                        List<String> typeList = new ArrayList<>();
                        typeList.add("3000");
                        List<FilterRule> filterRuleList = filterRuleMapper.selectFilterRuleListByStrategyId(campaign.getMktCampaignId(), typeList);
                        if (filterRuleList != null && !filterRuleList.isEmpty()) {
                            productFilter = filterRuleList;
                        }
                        // 查看当前规则协同渠道是否为沙盘，是否配置接单人派单
                        boolean flag = false;
                        String evtContactConfId = confRule.getEvtContactConfId();
                        Map<String, Object> mktCamChlConf = mktCamChlConfService.getMktCamChlConf(Long.valueOf(confRule.getEvtContactConfId()));
                        MktCamChlConfDetail mktCamChlConfDetail = (MktCamChlConfDetail) mktCamChlConf.get("mktCamChlConfDetail");
                        List<MktCamChlConfAttr> mktCamChlConfAttrList = mktCamChlConfDetail.getMktCamChlConfAttrList();
                        for (MktCamChlConfAttr attr : mktCamChlConfAttrList) {
                            if (attr.getAttrId().equals(ISEE_CUSTOMER.getArrId()) && (attr.getAttrValue() == null || attr.getAttrValue().equals(""))) {
                                flag = true;
                            }
                        }
                        boolean flag2 = false;
                        if (Arrays.asList(nameList).contains("接单人号码") && Arrays.asList(codeList).contains("SALE_EMP_NBR")) {
                            flag2 = true;
                        }
                        logger.info("dataVO.contentList.size()" + dataVO.contentList.size());
                        for (int j = 3; j < dataVO.contentList.size(); j++) {
                            List<String> data = Arrays.asList(dataVO.contentList.get(j).split("\\|@\\|"));
                            for(String s : data){
                                System.out.println("s" + s);
                            }
                            Object[] objects = data.toArray();
                            if (flag) {
                                if (flag2 && (this.getIndex() >= data.size() ? true:(data.get(this.getIndex()) == null || data.get(this.getIndex()).equals("")))) {
                                    // 记录日志，退出线程
                                    addLog2Es(batchNumSt, "导入清单存在接单人无数据");
                                    TrialOperation record = new TrialOperation();
                                    record.setId(Long.valueOf(insertId));
                                    record.setStatusCd(TrialStatus.IMPORT_FAIL.getValue());
                                    record.setRemark("清单导入数据错误");
                                    int i = trialOperationMapper.updateByPrimaryKey(record);
                                    throw new RuntimeException("导入清单第" + (j + 1) + "行接单人无数据");
                                }
                                if (!flag2) {
                                    addLog2Es(batchNumSt, "导入清单缺少接单人必填列");
                                    TrialOperation record = new TrialOperation();
                                    record.setId(Long.valueOf(insertId));
                                    record.setStatusCd(TrialStatus.IMPORT_FAIL.getValue());
                                    record.setRemark("清单导入数据错误");
                                    int i = trialOperationMapper.updateByPrimaryKey(record);
                                    throw new RuntimeException("导入清单缺少渠道必填列");
                                }
                            }
                            Map<String, Object> customers = new HashMap<>();
                            boolean check = true;
                            for (int x = 0; x < codeList.length; x++) {
                                logger.info("codeList[x]: " + codeList[x]);
                                if (codeList[x] == null) {
                                    break;
                                }
                                String value = "";
                                if (x >= data.size()) {
                                    value = "null";
                                } else {
                                    value = data.get(x);
                                }
                                if (value.contains("\r") || value.contains("\n")) {
                                    // 过滤换行符
                                    value = value.replace("\r", "").replace("\n", "");
                                }
                                /*if (codeList[x].equals("LATN_ID") && !value.equals(landId == null?"":landId)) {
                                    logger.info("导入清单工号地区不符=>landId:" + landId);
                                    addLog2Es(batchNumSt, "导入清单工号地区不符");
                                    TrialOperation record = new TrialOperation();
                                    record.setId(Long.valueOf(insertId));
                                    record.setStatusCd(TrialStatus.IMPORT_FAIL.getValue());
                                    record.setRemark("导入清单工号地区不符");
                                    int i = trialOperationMapper.updateByPrimaryKey(record);
                                    throw new RuntimeException("导入清单工号地区不符");
                                }*/
                                if (codeList[x].equals("CCUST_NAME") && (value.contains("null") || value.equals(""))) {
                                    check = false;
                                    break;
                                }
                                if (codeList[x].equals("CCUST_ID") && (value.contains("null") || value.equals(""))) {
                                    check = false;
                                    break;
                                }
                                if (codeList[x].equals("ASSET_INTEG_ID") && (value.contains("null") || value.equals(""))) {
                                    check = false;
                                    break;
                                }
                                if (codeList[x].equals("ASSET_NUMBER") && (value.contains("null") || value.equals(""))) {
                                    check = false;
                                    break;
                                }
                                if (codeList[x].equals("LATN_ID") && (value.contains("null") || value.equals(""))) {
                                    check = false;
                                    break;
                                }

                                customers.put(codeList[x], value);
                            }
                            if (!check || customers.isEmpty()) {
                                continue;
                            }
                            customerList.add(customers);
                        }

                        //蓝海流程
                        if(isBusinessMkt == true){
                            logger.info("customerList数量：" +customerList.size());
                            for (Map<String, Object> customMap : customerList) {
                                try {
                                    String staffid = "null";
                                    String orgName = "null";
                                    String c4 =(String)customMap.get("CPCP_COMPANY_CITY") + "市";
                                    Long lanId = AreaNameEnum.getLanIdByName(c4);
                                    String addr = (String)customMap.get("CPCP_COMPANY_ADDRESS");
                                    logger.info("c4: " + c4);
                                    logger.info("lanId: " + lanId);
                                    logger.info("addr: " + addr);
                                    String wgbm = getWgbmByLanId(lanId.toString(),c4,addr);//地址获取网格编码
                                    logger.info("wgbm: " + wgbm);
                                    if(wgbm == null || wgbm.equals("")){
                                        customMap.put("SALE_EMP_NBR ",staffid);
                                        customMap.put("CLUSTER_NAME",orgName);
                                        continue;
                                    }
                                    String orgpath = trialOperationMapper.selectOrgpathPathByWgbm(wgbm); //网格编码获取组织路径
                                    logger.info("orgpath: " + orgpath);
                                    if (orgpath!=null){
                                        String [] orgpathToUse = orgpath.split("-");
                                        if (orgpathToUse.length>2){
                                            Organization organization = organizationMapper.selectByPrimaryKey(Long.valueOf(orgpathToUse[orgpathToUse.length-1]));//取最后一个组织
                                            orgName = organization.getOrgName();
                                            staffid = trialOperationMapper.selectStaffByOrgpath(orgpathToUse);//获取审核人
                                            logger.info("orgName: " + orgName + "staffid: " + staffid);
                                        }
                                    }
                                    customMap.put("SALE_EMP_NBR",staffid);
                                    customMap.put("CLUSTER_NAME",orgName);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    logger.info("客户查询失败" + JSON.toJSONString(customMap));
                                }
                            }
                        }

                        if (customerList.size() > 0) {
                            importListMQ2EsService(request, customerList, productFilter, batchNumSt, ruleId.toString(), trialOp);
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (op != null) {
                op.setStatusCd(TrialStatus.IMPORT_FAIL.getValue());
                trialOperationMapper.updateByPrimaryKey(op);
            }
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "导入失败");
            return result;
        }
        result.put("resultCode", CommonConstant.CODE_SUCCESS);
        result.put("resultMsg", "导入成功,请稍后查看结果");
        return result;
    }
private String getWgbmByLanId(String lanId,String c4,String addr){
    // 本地网标识
    String respXml = "";
    try {
        String resCoverId = iSaleService.queryCoverIdByAddr(lanId, c4, addr);
        logger.info("onlineScanCodeOrCallPhone4Home-->resCoverId:" + resCoverId);
        String resCoverIdXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<WebService FuncName=\"QueryResCoverInfoService\" City=\"WT\">\n" +
                "\t<Root>\n" +
                "\t\t<AreaBm>" + lanId + "</AreaBm>\n" +
                "\t\t<Method>QueryResCoverInfo</Method>\n" +
                "\t\t<Query>\n" +
                "\t\t\t<ResCoverId>" + resCoverId + "</ResCoverId>\n" +
                "\t\t</Query>\n" +
                "\t</Root>\n" +
                "</WebService>";
        respXml = iSaleService.queryResCoverInfoService(resCoverIdXml);
    } catch (Exception e) {
        e.printStackTrace();
    }
    logger.info("onlineScanCodeOrCallPhone4Home-->respXml:" + respXml);
    List<Map<String, Object>> maps = camCpcSpecialLogic.parseData(respXml);
    logger.info("onlineScanCodeOrCallPhone4Home-->maps:" + maps);
    Map<String, Object> map = maps.get(0);
    // 获取GIS网格编码
    String wgbm = camCpcSpecialLogic.getValue4CycleMap(map, "Wgbm");
    return wgbm;
}
    @Transactional(readOnly = false)
    public Map<String, Object> importExcelUserList(MultipartFile multipartFile) throws IOException {
        // 接单人标签索引
        int index = 0;
        Map<String, Object> result = new HashMap<>();
        String batchNumSt = DateUtil.date2St4Trial(new Date()) + ChannelUtil.getRandomStr(4);
        XlsxProcessAbstract xlsxProcess = new XlsxProcessAbstract();

        String fileName = multipartFile.getName();
        String[] split = fileName.split("_");
        List<Map<String, String>> list = sysParamsMapper.listParamsByKey(split[0]);  // 静态参数表EVT001
        if (list == null || list.isEmpty())
            return null;
        Map<String, String> stringStringMap = list.get(0);
        String value = stringStringMap.get("value");
        List<MktStrategyConfDO> mktStrategyConfDOS = strategyMapper.selectByCampaignId(Long.valueOf(value));
        if (mktStrategyConfDOS == null)
            return null;
        Long mktStrategyConfId = mktStrategyConfDOS.get(0).getMktStrategyConfId();
        List<MktStrategyConfRuleRelDO> mktStrategyConfRuleRelDOS = ruleRelMapper.selectByMktStrategyConfId(mktStrategyConfId);
        if (mktStrategyConfRuleRelDOS == null)
            return null;
        Long mktStrategyConfRuleId = mktStrategyConfRuleRelDOS.get(0).getMktStrategyConfRuleId();

        Long camId = Long.valueOf(value);
        Long strId = Long.valueOf(mktStrategyConfId);
        Long ruleId = Long.valueOf(mktStrategyConfRuleId);
        TrialOperationVO operation = new TrialOperationVO();
        MktCampaignDO campaign = campaignMapper.selectByPrimaryKey(camId);
        MktStrategyConfDO strategy = strategyMapper.selectByPrimaryKey(strId);
        MktStrategyConfRuleDO confRule = ruleMapper.selectByPrimaryKey(ruleId);
        operation.setCampaignId(camId);
        operation.setStrategyId(strId);
        if (campaign == null || strategy == null || confRule == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "未找到有效的活动策略或规则");
            return result;
        }
        TrialOperation op = null;
        try {
            //添加红黑名单列表
            blackList2Redis(campaign);
            List<String> labelNameList = new ArrayList<>();
            List<String> labelEngNameList = new ArrayList<>();
            TransDetailDataVO dataVO;
            List<Map<String, Object>> labelList = new ArrayList<>();
            dataVO = xlsxProcess.processAllSheet(multipartFile);
            String[] nameList = dataVO.getContentList().get(0).split("\\|@\\|");
            String[] codeList = dataVO.getContentList().get(1).split("\\|@\\|");
            if (nameList.length != codeList.length) {
                result.put("resultCode", CODE_FAIL);
                result.put("resultMsg", "标签中文名个数与英文个数不匹配请重新检查文件");
                return result;
            }
            for (int i = 0; i < nameList.length; i++) {
                if (labelNameList.contains(nameList[i])) {
                    result.put("resultCode", CODE_FAIL);
                    result.put("resultMsg", "标签中文名称不能重复:" + "\"" + nameList[i] + "\"");
                    return result;
                }
                if (labelEngNameList.contains(codeList[i])) {
                    result.put("resultCode", CODE_FAIL);
                    result.put("resultMsg", "标签英文名称不能重复:" + "\"" + codeList[i] + "\"");
                    return result;
                }
                if (nameList[i].equals("接单人号码") || codeList[i].equals("SALE_EMP_NBR")) {
                    if(!(nameList[i].equals("接单人号码") && codeList[i].equals("SALE_EMP_NBR"))){
                        result.put("resultCode", CODE_FAIL);
                        result.put("resultMsg", "接单人号码标签名称错误");
                        return result;
                    }
                    index = i;
                }
                Map<String, Object> label = new HashMap<>();
                label.put("code", codeList[i]);
                label.put("name", nameList[i]);
                labelList.add(label);
                labelNameList.add(nameList[i]);
                labelEngNameList.add(codeList[i]);
            }
            List<String> fields = new ArrayList<>();

            List<Map<String, Object>> displayList = displayLabel(campaign);
            for (Map<String, Object> display : displayList) {
                String code = display.get("code") == null ? null : display.get("code").toString();
                String name = display.get("name") == null ? null : display.get("name").toString();
                if (code != null && !labelEngNameList.contains(code) && name != null && !labelNameList.contains(name)) {
                    Map<String, Object> label = new HashMap<>();
                    label.put("code", code);
                    label.put("name", name);
                    label.put("displayType", display.get("displayType"));
                    labelList.add(label);
                    fields.add(code);
                    labelEngNameList.add(code);
                    labelNameList.add(name);
                }
            }
            //查询活动下面所有渠道属性id是21和22的value
            List<String> attrValue = mktCamChlConfAttrMapper.selectAttrLabelValueByCampaignId(campaign.getMktCampaignId());
            if (attrValue != null && attrValue.size() > 0) {
                for (String attr : attrValue){
                    if (!fields.contains(attr)) {
                        fields.add(attr);
                    }
                }
            }
            // 服务包添加查询字段
            List<String> labels = mktCamChlConfAttrMapper.selectAttrLabelRemarkByCampaignId(campaign.getMktCampaignId());
            if(labels != null && labels.size() > 0){
                for (String s : labels) {
                    if (s != null && !labelEngNameList.contains(s)) {
                        Label label1= injectionLabelMapper.selectByLabelCode(s);
                        Map<String, Object> label = new HashMap<>();
                        label.put("code", s);
                        label.put("name", label1 == null? "":label1.getInjectionLabelName());
                        labelList.add(label);
                        fields.add(s);
                        labelEngNameList.add(s);
                    }
                }
            }
            if (!fields.isEmpty()) {
                redisUtils_es.set("DISPLAY_LABEL_" + campaign.getMktCampaignId(), fields);
            }
            List<Long> attrList = mktCamChlConfAttrMapper.selectByCampaignId(campaign.getMktCampaignId());
            if (attrList.contains(ISEE_CUSTOMER.getArrId()) || attrList.contains(ISEE_LABEL_CUSTOMER.getArrId()) || attrList.contains(SERVICE_PACKAGE.getArrId())){
                if (!labelEngNameList.contains("SALE_EMP_NBR")){
                    Map<String,Object> label = new HashMap<>();
                    label.put("code","SALE_EMP_NBR");
                    label.put("name","接单人号码");
                    label.put("labelDataType","1200");
                    labelList.add(label);
                }
            }
            if (attrList.contains(ISEE_AREA.getArrId()) || attrList.contains(ISEE_LABEL_AREA.getArrId()) ){
                if (!labelEngNameList.contains("AREA")){
                    Map<String,Object> label = new HashMap<>();
                    label.put("code","AREA");
                    label.put("name","派单区域");
                    label.put("labelDataType","1200");
                    labelList.add(label);
                }
            }
            redisUtils.set("LABEL_DETAIL_"+batchNumSt,labelList);

            if (labelList.size() > 87) {
                result.put("resultCode", CODE_FAIL);
                result.put("resultMsg", "扩展字段不能超过87个");
                return result;
            }
            List<MktStrategyCloseRuleRelDO> closeRuleRelDOS = strategyCloseRuleRelMapper.selectRuleByStrategyId(campaign.getMktCampaignId());
            //todo 关单规则配置信息
            if (closeRuleRelDOS!=null && !closeRuleRelDOS.isEmpty()){
                List<Map<String,Object>> closeRule = new ArrayList<>();
                for (MktStrategyCloseRuleRelDO ruleRelDO : closeRuleRelDOS){
                    CloseRule closeR = closeRuleMapper.selectByPrimaryKey(ruleRelDO.getRuleId());
                    if (closeR!=null){
                        Map<String,Object> ruleMap = new HashMap<>();
                        ruleMap.put("closeName",closeR.getCloseName());
                        ruleMap.put("closeCode",closeR.getCloseCode());
                        ruleMap.put("closeNbr",closeR.getExpression());
                        ruleMap.put("closeType",closeR.getCloseType());
                        closeRule.add(ruleMap);
                    }
                }
                redisUtils_es.set("CLOSE_RULE_"+campaign.getMktCampaignId(),closeRule);
            }
            TrialOperation trialOp = BeanUtil.create(operation, new TrialOperation());
            trialOp.setCampaignName(campaign.getMktCampaignName());
            //当清单导入时 strategyId name 存储规则信息
            trialOp.setStrategyId(confRule.getMktStrategyConfRuleId());
            trialOp.setStrategyName(confRule.getMktStrategyConfRuleName());
            trialOp.setBatchNum(Long.valueOf(batchNumSt));
            trialOp.setStatusCd(TrialStatus.IMPORT_GOING.getValue());
            trialOp.setStatusDate(new Date());
            trialOp.setCreateStaff(TrialCreateType.ADVANCE_USER_LIST.getValue());
            trialOperationMapper.insert(trialOp);
            Long insertId = trialOp.getId();
            op = trialOp;
            int size = dataVO.contentList.size() - 3;
            /*Long landId = orgTreeService.getLandIdBySession();*/
            new MyThread(index) {
                public void run() {
                    try {
                        List<FilterRule> productFilter = new ArrayList<>();
                        final TrialOperationVOES request = getTrialOperationVOES(operation, ruleId, batchNumSt, labelList);
                        List<Map<String, Object>> customerList = new ArrayList<>();
                        //红黑名单过滤
                        List<String> typeList = new ArrayList<>();
                        typeList.add("3000");
                        List<FilterRule> filterRuleList = filterRuleMapper.selectFilterRuleListByStrategyId(campaign.getMktCampaignId(), typeList);
                        if (filterRuleList != null && !filterRuleList.isEmpty()) {
                            productFilter = filterRuleList;
                        }
                        // 查看当前规则协同渠道是否为沙盘，是否配置接单人派单
                        boolean flag = false;
                        String evtContactConfId = confRule.getEvtContactConfId();
                        Map<String, Object> mktCamChlConf = mktCamChlConfService.getMktCamChlConf(Long.valueOf(confRule.getEvtContactConfId()));
                        MktCamChlConfDetail mktCamChlConfDetail = (MktCamChlConfDetail) mktCamChlConf.get("mktCamChlConfDetail");
                        List<MktCamChlConfAttr> mktCamChlConfAttrList = mktCamChlConfDetail.getMktCamChlConfAttrList();
                        for (MktCamChlConfAttr attr : mktCamChlConfAttrList) {
                            if (attr.getAttrId().equals(ISEE_CUSTOMER.getArrId()) && (attr.getAttrValue() == null || attr.getAttrValue().equals(""))) {
                                flag = true;
                            }
                        }
                        boolean flag2 = false;
                        if (Arrays.asList(nameList).contains("接单人号码") && Arrays.asList(codeList).contains("SALE_EMP_NBR")) {
                            flag2 = true;
                        }
                        for (int j = 3; j < dataVO.contentList.size(); j++) {
                            List<String> data = Arrays.asList(dataVO.contentList.get(j).split("\\|@\\|"));
                            Object[] objects = data.toArray();
                            if (flag) {
                                if (flag2 && (this.getIndex() >= data.size() ? true:(data.get(this.getIndex()) == null || data.get(this.getIndex()).equals("")))) {
                                    // 记录日志，退出线程
                                    addLog2Es(batchNumSt, "导入清单存在接单人无数据");
                                    TrialOperation record = new TrialOperation();
                                    record.setId(Long.valueOf(insertId));
                                    record.setStatusCd(TrialStatus.IMPORT_FAIL.getValue());
                                    record.setRemark("清单导入数据错误");
                                    int i = trialOperationMapper.updateByPrimaryKey(record);
                                    throw new RuntimeException("导入清单第" + (j + 1) + "行接单人无数据");
                                }
                                if (!flag2) {
                                    addLog2Es(batchNumSt, "导入清单缺少接单人必填列");
                                    TrialOperation record = new TrialOperation();
                                    record.setId(Long.valueOf(insertId));
                                    record.setStatusCd(TrialStatus.IMPORT_FAIL.getValue());
                                    record.setRemark("清单导入数据错误");
                                    int i = trialOperationMapper.updateByPrimaryKey(record);
                                    throw new RuntimeException("导入清单缺少渠道必填列");
                                }
                            }
                            Map<String, Object> customers = new HashMap<>();
                            boolean check = true;
                            for (int x = 0; x < codeList.length; x++) {
                                if (codeList[x] == null) {
                                    break;
                                }
                                String value = "";
                                if (x >= data.size()) {
                                    value = "null";
                                } else {
                                    value = data.get(x);
                                }
                                if (value.contains("\r") || value.contains("\n")) {
                                    // 过滤换行符
                                    value = value.replace("\r", "").replace("\n", "");
                                }
                                if (codeList[x].equals("ASSET_NUMBER") && (value.contains("null") || value.equals(""))) {
                                    check = false;
                                    break;
                                }
                                if (codeList[x].equals("LATN_ID") && (value.contains("null") || value.equals(""))) {
                                    check = false;
                                    break;
                                }
                                customers.put(codeList[x], value);
                            }
                            if (!check || customers.isEmpty()) {
                                continue;
                            }
                            customerList.add(customers);
                        }
                        importListMQ2EsService(request, customerList, productFilter, batchNumSt, ruleId.toString(), trialOp);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (op != null) {
                op.setStatusCd(TrialStatus.IMPORT_FAIL.getValue());
                trialOperationMapper.updateByPrimaryKey(op);
            }
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "导入失败");
            return result;
        }
        result.put("resultCode", CommonConstant.CODE_SUCCESS);
        result.put("resultMsg", "导入成功,请稍后查看结果");
        redisUtils_es.set("YOUR_SO_BEAUTIFUL_" + batchNumSt, batchNumSt);
        return result;
    }

    public void importListMQ2EsService(TrialOperationVOES request, List<Map<String, Object>> customerList, List<FilterRule> productFilter, String batchNumSt, String ruleId, TrialOperation operation){
        logger.info("导入试运算清单importUserList->customerList的数量：" + customerList.size());
        Long mqSum = 0L;
        int x = customerList.size() / 1000;
        if (customerList.size() % 1000 > 0) {
            x++;
        }
        for (int i = 1; i <= x; i++) {
            List<Map<String, Object>> newSublist = new ArrayList();
            if (i == x ) {
                newSublist = customerList.subList(0, customerList.size());
            } else {
                newSublist = customerList.subList(0, 1000);
            }
            // 向MQ中扔入request和customersList
            HashMap msgBody = new HashMap();
            msgBody.put("request", request);
            msgBody.put("customerList", newSublist);
            msgBody.put("productFilterList", productFilter);
            try {
                // 判断是否发送成功
                if (!mqService.msg2Producer(msgBody,importTopic, batchNumSt, ruleId).equals("SEND_OK")) {
                    // 发送失败自动重发2次，如果还是失败，记录
                    logger.error("CTGMQ消息生产失败,batchNumSt:" + batchNumSt, msgBody);
                }
                mqSum++;
                msgBody = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            newSublist.clear();
        }
        redisUtils_es.set("MQ_SUM_" + batchNumSt, mqSum);
    }

    private void blackList2Redis(MktCampaignDO campaign) {
        List<String> typeList = new ArrayList<>();
        typeList.add("1000");
        typeList.add("2000");
        List<FilterRule> filterRuleList = filterRuleMapper.selectFilterRuleListByStrategyId(campaign.getMktCampaignId(),typeList);
        if (filterRuleList!=null && !filterRuleList.isEmpty()){
            List<String> userList = new ArrayList<>();
            for (FilterRule filterRule : filterRuleList){
                if (filterRule.getUserList()!=null && !"".equals(filterRule.getUserList())){
                    String[] users = filterRule.getUserList().split(",");
                    userList.addAll(Arrays.asList(users));
                }
            }
            int num = userList.size()/100 + 1;
            List<List<String>> list = ChannelUtil.averageAssign(userList,num);
            for (int i = 0; i < num; i++) {
                redisUtils.hset("AREA_RULE_BLACK_LIST_"+campaign.getMktCampaignId(),i+"",list.get(i));
            }
        }
    }


    /**
     * 策略试运算区域统计查询
     * @param batchId
     * @return
     */
    @Override
    public Map<String, Object> searchCountAllByArea(Long batchId) {
        Map<String, Object> result = new HashMap<>();
        TrialOperation operation = trialOperationMapper.selectByPrimaryKey(batchId);
        if (operation == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "统计查询记录出错啦！");
            return result;
        }
        Map<String,Object> resultMap =  (Map<String, Object>) redisUtils.get("HITS_COUNT_INFO_"+operation.getBatchNum());
        if (resultMap==null || resultMap.get("countMap")==null){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "统计查询记录出错啦！");
            return result;
        }
        Map<String,Object> countMap = (Map<String, Object>) resultMap.get("countMap");
        List<GroupingVO> groupingVOS = new ArrayList<>();
        Iterator iterator = countMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,Object> entry = (Map.Entry<String, Object>) iterator.next();
            GroupingVO vo = new GroupingVO();
            vo.setName(entry.getKey());
            vo.setValue(entry.getValue().toString());
            groupingVOS.add(vo);
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", groupingVOS);
        return result;
    }

    /**
     * 策略试运算统计查询
     * @param batchId
     *
     * @return
     */
    @Override
    public Map<String, Object> searchCountInfo(Long batchId) {
        Map<String, Object> result = new HashMap<>();
        TrialOperation operation = trialOperationMapper.selectByPrimaryKey(batchId);
        if (operation == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "统计查询记录出错啦！");
            return result;
        }
        Map<String,Object> resultMap =  (Map<String, Object>) redisUtils.get("HITS_COUNT_INFO_"+operation.getBatchNum());
        if (resultMap.get("countMap")!=null){
            resultMap.remove("countMap");
        }
        Map<String,Object> map = new HashMap<>();
        List<String> ruleList = new ArrayList<>();
        List<GroupingVO> groupingVOS = new ArrayList<>();

        Iterator iterator = resultMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,Object> entry = (Map.Entry<String,Object>)iterator.next();
            ruleList.add(entry.getKey());
            Map<String,Object> valueMap = (Map<String, Object>) entry.getValue();
            List<String> valueList = new ArrayList<>();
            Iterator iter = valueMap.entrySet().iterator();
            while (iter.hasNext()){
                Map.Entry<String,Object> valueEntry = (Map.Entry<String, Object>) iter.next();
                valueList.add(valueEntry.getValue().toString());
            }
            GroupingVO vo = new GroupingVO();
            vo.setName(entry.getKey());
            vo.setValueList(valueList);
            groupingVOS.add(vo);
        }
        Collections.reverse(ruleList);
        Collections.reverse(groupingVOS);
        map.put("rules",ruleList);
        map.put("values",groupingVOS);
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", map);
        return result;
    }



    /**
     * 新增策略试运算记录
     *
     * @param operationVO
     * @return
     */
    @Override
    public Map<String, Object> createTrialOperation(TrialOperationVO operationVO) {

        String[] statusCd  = new String[1];
        statusCd[0] = TrialStatus.SAMPEL_GOING.getValue();
        Date createTime = new Date(new Date().getTime() - 600000);
        List<TrialOperation> operationCheck = trialOperationMapper.listOperationByCreateTime(null,createTime,statusCd);
        if (operationCheck!=null && !operationCheck.isEmpty()){
            for (TrialOperation operation : operationCheck){
                operation.setStatusCd(TrialStatus.SAMPEL_FAIL.getValue());
                trialOperationMapper.updateByPrimaryKey(operation);
            }
        }
        Map<String, Object> result = new HashMap<>();
        //生成批次号
        String batchNumSt = DateUtil.date2St4Trial(new Date()) + ChannelUtil.getRandomStr(4);
        MktCampaignDO campaign = campaignMapper.selectByPrimaryKey(operationVO.getCampaignId());
        MktStrategyConfDO strategy = strategyMapper.selectByPrimaryKey(operationVO.getStrategyId());
        if (campaign == null || strategy == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "活动策略信息有误");
            return result;
        }
        List<MktStrategyConfRuleDO> ruleList = ruleMapper.selectByMktStrategyConfId(operationVO.getStrategyId());
        if (ruleList==null || ruleList.isEmpty()){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "未找到有效的规则信息");
            return result;
        }
        if (strategy.getAreaId()==null || "".equals(strategy.getAreaId())){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "请配置策略适用地市");
            return result;
        }

        for (MktStrategyConfRuleDO rule : ruleList){
            String orgCheck = redisUtils.get("ORG_CHECK_"+rule.getMktStrategyConfRuleId().toString())==null ? null :redisUtils.get("ORG_CHECK_"+rule.getMktStrategyConfRuleId().toString()).toString();
            if (orgCheck!=null && orgCheck.equals("false")){
                result.put("resultCode", CODE_FAIL);
                result.put("resultMsg", "规则："+rule.getMktStrategyConfRuleName()+"营销组织树配置正在努力加载请稍后再试");
                return result;
            }
        }
        TrialOperation trialOp = BeanUtil.create(operationVO, new TrialOperation());
        trialOp.setCampaignName(campaign.getMktCampaignName());
        trialOp.setStrategyName(strategy.getMktStrategyConfName());
        trialOp.setBatchNum(Long.valueOf(batchNumSt));
        trialOp.setStatusCd(TrialStatus.SAMPEL_GOING.getValue());
        trialOp.setStatusDate(new Date());
        trialOp.setUpdateDate(new Date());
        trialOp.setCreateStaff(TrialCreateType.TRIAL_OPERATION.getValue());
        trialOperationMapper.insert(trialOp);

        operationVO.setTrialId(trialOp.getId());
        operationVO.setCampaignName(campaign.getMktCampaignName());
        operationVO.setStrategyName(strategy.getMktStrategyConfName());
        List<TrialOperation> operationList = trialOperationMapper.findOperationListByStrategyId(operationVO.getStrategyId(),TrialCreateType.TRIAL_OPERATION.getValue());
        // 调用es的抽样接口
        final TrialOperationVO vo = operationVO;
        new Thread() {
            public void run() {
                Map<String,Object> resultMap = sampleFromES(vo);
            }
        }.start();

        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", operationList);
        return result;
    }


    /**
     * es抽样接口
     *
     * @param operationVO
     * @return
     */
    public Map<String, Object> sampleFromES(TrialOperationVO operationVO) {
        Map<String, Object> result = new HashMap<>();
        TrialOperation trialOperation = trialOperationMapper.selectByPrimaryKey(operationVO.getTrialId());
        MktCampaignDO campaign = campaignMapper.selectByPrimaryKey(operationVO.getCampaignId());
        MktStrategyConfDO strategy = strategyMapper.selectByPrimaryKey(operationVO.getStrategyId());
        //添加策略适用地市
        redisUtils.set("STRATEGY_CONF_AREA_"+operationVO.getStrategyId(),strategy.getAreaId());
        //添加红黑名单列表
        blackList2Redis(campaign);


        // 通过活动id获取关联的标签字段数组

        List<Map<String,Object>> labelList = displayLabel(campaign);

        redisUtils.set("LABEL_DETAIL_"+trialOperation.getBatchNum(),labelList);

        String[] fieldList = getStrings(campaign,strategy);


        TrialOperationVO request = BeanUtil.create(operationVO,new TrialOperationVO());
        request.setBatchNum(trialOperation.getBatchNum());
        request.setFieldList(fieldList);
        //策略试运算
        request.setSample(true);
        TrialOperationVOES requests = BeanUtil.create(request,new TrialOperationVOES());
        //todo 待测试
        ArrayList<TrialOperationParamES> paramList = new ArrayList<>();
        List<MktStrategyConfRuleRelDO> ruleRelList = ruleRelMapper.selectByMktStrategyConfId(operationVO.getStrategyId());
        for (MktStrategyConfRuleRelDO ruleRelDO : ruleRelList) {
            TrialOperationParamES param = getTrialOperationParamES(operationVO, trialOperation.getBatchNum(), ruleRelDO.getMktStrategyConfRuleId(),true,null);
            List<LabelResultES> labelResultList = param.getLabelResultList();
            paramList.add(param);
        }
        requests.setParamList(paramList);
        TrialResponseES response = new TrialResponseES();
        TrialResponseES countResponse = new TrialResponseES();

        try {
            //todo
            System.out.println(JSON.toJSONString(requests));
            response = esService.searchBatchInfo(requests);
//            response = restTemplate.postForObject("http://localhost:8080/es/searchBatchInfo", requests, TrialResponseES.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 抽样试算成功
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", null);
        return result;
    }

    private String[] getStrings(MktCampaignDO campaign,MktStrategyConfDO strategy) {
        // 通过活动id获取关联的标签字段数组
        List<LabelDTO> labelDTOList = mktCamDisplayColumnRelMapper.selectLabelDisplayListByCamId(campaign.getMktCampaignId());
        if (labelDTOList==null){
            labelDTOList = new ArrayList<>();
        }
        List<String> codeList = new ArrayList<>();
        for (LabelDTO labelDTO : labelDTOList) {
            codeList.add(labelDTO.getLabelCode());
        }
        List<String> ruleCodeList = (List<String>) redisUtils.hgetAllRedisList("LABEL_CODE_"+strategy.getMktStrategyConfId());
        logger.info("*********** 试算获取全部标签条件编码 ："+JSON.toJSONString(ruleCodeList));
        //添加固定查询标签
        if (!codeList.contains("ACCS_NBR")){
            codeList.add("ACCS_NBR");
        }if (!codeList.contains("LATN_NAME")){
            codeList.add("LATN_NAME");
        }if (!codeList.contains("CCUST_NAME")){
            codeList.add("CCUST_NAME");
        } if (!codeList.contains("CCUST_ID")){
            codeList.add("CCUST_ID");
        } if (!codeList.contains("CCUST_TEL")){
            codeList.add("CCUST_TEL");
        } if (!codeList.contains("LATN_ID")){
            codeList.add("LATN_ID");
        }if (!codeList.contains("CCUST_ROW_ID")){
            codeList.add("CCUST_ROW_ID");
        }if (!codeList.contains("ASSET_NUMBER")){
            codeList.add("ASSET_NUMBER");
        }if (!codeList.contains("COMM_LVL3_ID")){
            codeList.add("COMM_LVL3_ID");
        }if (!codeList.contains("COMM_LVL4_ID")){
            codeList.add("COMM_LVL4_ID");
        }
        //策略下所有分群条件加入
        if (ruleCodeList!=null){
            for (String labelCode : ruleCodeList){
                if (codeList.contains(labelCode)){
                    continue;
                }
                codeList.add(labelCode);
            }
        }
        String[] fieldList = new String[codeList.size()];
        for (int i = 0; i < codeList.size(); i++) {
            fieldList[i] = codeList.get(i);
        }
        return fieldList;
    }


    /**
     * redis查询抽样试算结果清单
     *
     * @param operationId
     * @return
     */
    @Override
    public Map<String, Object> findBatchHitsList(Long operationId) {
        Map<String, Object> result = new HashMap<>();

        TrialOperation operation = trialOperationMapper.selectByPrimaryKey(operationId);
        if (operation == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "试运算记录不存在");
            return result;
        }
        TrialResponseES response = new TrialResponseES();
//        TrialResponse response = new TrialResponse();
        try {
            Map<String, Long> param = new HashMap<>();
            param.put("batchId", operation.getBatchNum());
            response = esService.findBatchHitsList(operation.getBatchNum().toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("试算清单记录查询失败{}",operation.getBatchNum());
        }

        TrialOperationListVO vo = new TrialOperationListVO();
        List<String> labelCodeList = new ArrayList<>();
        List<Map<String, Object>> userList = new ArrayList<>();

        Map<String, Object> hitsList = (Map<String, Object>) response.getHitsList();
        if (hitsList == null) {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "未命中任何客户");
            return result;
        }

        List<Map<String,Object>> mapList = (List<Map<String,Object>>) hitsList.get("result");

        for (Map<String,Object> hitMap : mapList){
            Map<String, Object> searchMap = (Map<String, Object>) hitMap.get("searchHitMap");
            Map<String, Object> map = new HashMap<>();

            TrialOperationParamES ruleInfoMap = new TrialOperationParamES();
            if (hitMap.get("ruleInfo") != null) {
                ruleInfoMap = (TrialOperationParamES) hitMap.get("ruleInfo");
            }

            for (String set : searchMap.keySet()) {
                if (labelCodeList.size() < searchMap.keySet().size()) {
                    labelCodeList.add(set);
                }
                map.put(set, searchMap.get(set));
                // 数据脱敏(客户证件号)，查询全局开关（0：关；1：开）
                String dataDesFilter = (String) redisUtils.get("DATA_DESENSITIZATION_FILTER");
                if (dataDesFilter == null) {
                    List<SysParams> sysParamsList = sysParamsMapper.listParamsByKeyForCampaign("DATA_DESENSITIZATION_FILTER");
                    if (sysParamsList != null && sysParamsList.size() > 0) {
                        dataDesFilter = sysParamsList.get(0).getParamValue();
                        redisUtils.set("DATA_DESENSITIZATION_FILTER", dataDesFilter);
                    }
                }
                // 数据脱敏(客户证件号)，脱敏操作
                if (set.equals("ID_NBR") && ( null == dataDesFilter || dataDesFilter.equals("1"))) {
                    String value = dataDesensitization((String) searchMap.get(set));
                    map.put(set, value);
                } else {
                    map.put(set, searchMap.get(set));
                }
            }
            map.put("campaignId", operation.getCampaignId());
            map.put("campaignName", operation.getCampaignName());
            map.put("strategyId", operation.getStrategyId());
            map.put("strategyName", operation.getStrategyName());
            map.put("ruleId", ruleInfoMap.getRuleId());
            map.put("ruleName", ruleInfoMap.getRuleName());
            //todo 工单号
            map.put("orderId", "49736605");
            userList.add(map);

        }
        if (labelCodeList.size() > 0) {
            List<SimpleInfo> titleList = labelMapper.listLabelByCodeList(labelCodeList);
            vo.setTitleList(titleList);
        }
        vo.setHitsList(userList);
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", vo);
        return result;
    }

    private String dataDesensitization(String sfz) {
        String sfzyc = "";
        if (!StringUtils.isNotBlank(sfz)) {
            if (sfz.length() == 18 || sfz.length() == 15) {
                sfzyc = (sfz.substring(0, 6) + "********" + sfz.substring(sfz.length() - 4, sfz.length()));
            } else if (sfz.length() == 10) {
                sfzyc = (sfz.substring(0, 7) + "***");
            } else if (sfz.length() > 18) {
                sfzyc = (sfz.substring(0, 6) + "********" + sfz.substring(sfz.length() - 4, sfz.length()));
            } else if (sfz.length() - 2 >= 4) {
                sfzyc = (sfz.substring(0, 2) + "******" + sfz.substring(sfz.length() - 2, sfz.length()));
            } else if (sfz.length() - 2 >= 1) {
                sfzyc = (sfz.substring(0, 1) + "*****" + sfz.substring(sfz.length() - 1, sfz.length()));
            }
        }
        return sfzyc;
    }

    /**
     * 下发策略试运算结果
     *
     * @param trialOperation
     * @return
     */
    @Override
    public Map<String, Object> issueTrialResult(TrialOperation trialOperation) {

        Map<String, Object> result = new HashMap<>();
        TrialOperation operation = trialOperationMapper.selectByPrimaryKey(trialOperation.getId());

        if (operation == null){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "试运算记录不存在");
            return result;
        }
//        将预下发指定条目数specifiedNum存到redis
        Long batchNum = operation.getBatchNum();
        String specifiedNum = trialOperation.getSpecifiedNum();
        if (!specifiedNum.equals("")){
            redisUtils_es.set( "SPECIFIEDNUM_" + batchNum, specifiedNum);
        }

        //分批下发数量存到redis  SPECIFIED_File_NUM
        String specifiedFileNum = trialOperation.getSpecifiedFileNum();
        if (!specifiedFileNum.equals("") && !specifiedFileNum.equals("all")){
            redisUtils_es.set( "SPECIFIED_File_NUM_" + batchNum, specifiedFileNum);
        }

        BeanUtil.copy(operation,trialOperation);
        // 通过活动id获取关联的标签字段数组
        MktCampaignDO campaignDO = campaignMapper.selectByPrimaryKey(trialOperation.getCampaignId());
        if (campaignDO==null){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "活动不存在");
            return result;
        }
        if(!StatusCode.STATUS_CODE_PUBLISHED.getStatusCode().equals(campaignDO.getStatusCd())){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "发布活动后才能全量试算");
            return result;
        }
        MktStrategyConfDO strategyConfDO = strategyConfMapper.selectByPrimaryKey(operation.getStrategyId());
        if (strategyConfDO==null){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "策略不存在");
            return result;
        }
        if (!operation.getStatusCd().equals(TrialStatus.SAMPEL_SUCCESS.getValue())){
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "抽样试算失败，无法全量试算");
            return result;
        }
        campaignDO.setBatchType("2000");
        campaignMapper.updateByPrimaryKey(campaignDO);
        List<MktStrategyCloseRuleRelDO> closeRuleRelDOS = strategyCloseRuleRelMapper.selectRuleByStrategyId(campaignDO.getMktCampaignId());
        //todo 关单规则配置信息
        if (closeRuleRelDOS!=null && !closeRuleRelDOS.isEmpty()){
            List<Map<String,Object>> closeRule = new ArrayList<>();
            for (MktStrategyCloseRuleRelDO ruleRelDO : closeRuleRelDOS){
                CloseRule closeR = closeRuleMapper.selectByPrimaryKey(ruleRelDO.getRuleId());
                if (closeR!=null){
                    Map<String,Object> ruleMap = new HashMap<>();
                    ruleMap.put("closeName",closeR.getCloseName());
                    ruleMap.put("closeCode",closeR.getCloseCode());
                    ruleMap.put("closeNbr",closeR.getExpression());
                    ruleMap.put("closeType",closeR.getCloseType());
                    closeRule.add(ruleMap);
                }
            }
            redisUtils_es.set("CLOSE_RULE_"+campaignDO.getMktCampaignId(),closeRule);
        }
        //查询活动下面所有渠道属性id是21和22的value
        List<String> attrValue = mktCamChlConfAttrMapper.selectAttrLabelValueByCampaignId(trialOperation.getCampaignId());
        //添加策略适用地市
        redisUtils.set("STRATEGY_CONF_AREA_"+operation.getStrategyId(),strategyConfDO.getAreaId());
        // 通过活动id获取关联的标签字段数组
        List<LabelDTO> labelDTOList = mktCamDisplayColumnRelMapper.selectLabelDisplayListByCamId(campaignDO.getMktCampaignId());
        if (labelDTOList==null){
            labelDTOList = new ArrayList<>();
        }
        String[] fieldList = new String[labelDTOList.size()+attrValue.size()];

        List<Map<String,Object>> labelList = new ArrayList<>();
        for (int i = 0 ; i< labelDTOList.size();i++){
            fieldList[i] = labelDTOList.get(i).getLabelCode();
            Map<String,Object> label = new HashMap<>();
            label.put("code",labelDTOList.get(i).getLabelCode());
            label.put("name",labelDTOList.get(i).getInjectionLabelName());
            label.put("labelType",labelDTOList.get(i).getLabelType());
            label.put("displayType",labelDTOList.get(i).getLabelDisplayType());
            label.put("labelDataType",labelDTOList.get(i).getLabelDataType());
            labelList.add(label);
        }
        try {
            //指定渠道添加默认展示列
            logger.info("指定渠道添加默认展示列1");
            List<MktCamChlConfDO> mktCamChlConfDOList = mktCamChlConfMapper.selectByCampaignId(trialOperation.getCampaignId());
            logger.info("指定渠道添加默认展示列2 mktCamChlConfDOList" + mktCamChlConfDOList);
            Iterator itrator = mktCamChlConfDOList.iterator();
            while (itrator.hasNext()){
                MktCamChlConfDO mktCamChlConfDO = (MktCamChlConfDO)itrator.next();
                Long contactChlId = mktCamChlConfDO.getContactChlId();
                String contactChlCode =  channelMapper.selectByPrimaryKey(contactChlId).getContactChlCode();
                SysParams sysParams = sysParamsMapper.selectByParamKey("defaultDisplayLabelOnSpecifiedChannel");
                List<Map<String,Object>> displayLabelList = (List<Map<String,Object>>)JSON.parse(sysParams.getParamValue());
                Iterator displayIteraor = displayLabelList.iterator();
                while (displayIteraor.hasNext()){
                    Map<String,Object> displayLabel = (Map<String,Object>)displayIteraor.next();
                    logger.info("默认展示列contactChlCode" + contactChlCode);
                    logger.info("默认展示列displayLabel 3" + displayLabel);
                    logger.info("默认展示列displayLabel.get(\"contactChlCode\").toString()" + displayLabel.get("contactChlCode").toString());
                    if(contactChlCode.equals(displayLabel.get("contactChlCode").toString())){
                        logger.info("默认展示列displayLabel4:" + displayLabel);
                        for(Map<String,Object> map : labelList){
                            if(!map.get("code").toString().equals(displayLabel.get("code").toString())){
                                labelList.add(displayLabel);
                            }
                        }

                    }
                }
            }
            logger.info("展示列labelList:" + labelList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = labelDTOList.size(); i< labelDTOList.size()+attrValue.size();i++){
            fieldList[i] = attrValue.get(i-labelDTOList.size());
        }
        List<Long> attrList = mktCamChlConfAttrMapper.selectByCampaignId(trialOperation.getCampaignId());
        if (attrList.contains(ISEE_CUSTOMER.getArrId()) || attrList.contains(ISEE_LABEL_CUSTOMER.getArrId()) || attrList.contains(SERVICE_PACKAGE.getArrId())){
            Map<String,Object> label = new HashMap<>();
            label.put("code","SALE_EMP_NBR");
            label.put("name","接单人号码");
            label.put("labelDataType","1200");
            labelList.add(label);
        }
        if (attrList.contains(ISEE_AREA.getArrId()) || attrList.contains(ISEE_LABEL_AREA.getArrId())
                || attrList.contains(ISEE_LABEL_AREA_CUSTOMER.getArrId())){
            Map<String,Object> label = new HashMap<>();
            label.put("code","AREA");
            label.put("name","派单区域");
            label.put("labelDataType","1200");
            labelList.add(label);
        }

        redisUtils.set("LABEL_DETAIL_"+trialOperation.getBatchNum(),labelList);
        List<Map<String, Object>> iSaleDisplay = new ArrayList<>();
        iSaleDisplay = (List<Map<String, Object>>) redisUtils.get("EVT_ISALE_LABEL_" + campaignDO.getIsaleDisplay());
        if (iSaleDisplay == null) {
            iSaleDisplay = injectionLabelMapper.listLabelByDisplayId(campaignDO.getIsaleDisplay());
            redisUtils.set("EVT_ISALE_LABEL_" + campaignDO.getIsaleDisplay(), iSaleDisplay);
        }
        redisUtils.set("ISALE_LABEL_"+trialOperation.getBatchNum(),iSaleDisplay);

        TrialOperationVO request = BeanUtil.create(trialOperation,new TrialOperationVO());
        request.setFieldList(fieldList);
        request.setCampaignType(campaignDO.getMktCampaignType());
        request.setLanId(campaignDO.getLanId());
        request.setCamLevel(campaignDO.getCamLevel());
        // 获取创建人员code
        request.setStaffCode(getCreater(campaignDO.getCreateStaff())==null ? "null" : getCreater(campaignDO.getCreateStaff()));

         TrialOperationVOES requests = BeanUtil.create(request,new TrialOperationVOES());
        //todo 待测试
        ArrayList<TrialOperationParamES> paramList = new ArrayList<>();
        List<MktStrategyConfRuleRelDO> ruleRelList = ruleRelMapper.selectByMktStrategyConfId(request.getStrategyId());
        for (MktStrategyConfRuleRelDO ruleRelDO : ruleRelList) {
            TrialOperationParamES param = getTrialOperationParamES(request, trialOperation.getBatchNum(), ruleRelDO.getMktStrategyConfRuleId(),false,null);
            paramList.add(param);
        }
        requests.setParamList(paramList);
        final  TrialOperationVOES  issureRequest = requests;
        System.out.println(JSON.toJSONString(requests));
        try {
            List<String> startList = mktCamChlConfAttrMapper.selectAttrTimeInfoByCampaignId(campaignDO.getMktCampaignId(),
                    ConfAttrEnum.START_DATE.getArrId() );
            List<String> endList = mktCamChlConfAttrMapper.selectAttrTimeInfoByCampaignId(campaignDO.getMktCampaignId(),
                    ConfAttrEnum.END_DATE.getArrId() );
            operation.setStartTime(new Date(Long.valueOf(startList.get(0))));
            operation.setEndTime(new Date(Long.valueOf(endList.get(0))));
            operation.setStatusCd(TrialStatus.ALL_SAMPEL_GOING.getValue());
            trialOperationMapper.updateByPrimaryKey(operation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            new Thread(){
                public void run(){
                   esService.strategyIssure(issureRequest);
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "全量试算中，请稍后刷新页面查看结果");
            return result;
        }
        //更新试算记录状态和时间
        trialOperation.setStatusDate(new Date());
        trialOperation.setStatusCd(TrialStatus.ALL_SAMPEL_GOING.getValue());
        trialOperationMapper.updateByPrimaryKey(trialOperation);
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "全量试算中，请稍后刷新页面查看结果");
        return result;
    }


    //弃用
/*
    private JSONObject searchInfoFromEs(List<TrialOperationParam> operationVOList, String[] fieldList) throws Exception {
        HttpClient httpClient = HttpClients.createDefault();

        String url = "https://localhost/es/searchBatchInfo";

        Map<String, String> paramHeader = new HashMap<>();
        paramHeader.put("Accept", "application/xml");
        Map<String, String> paramBody = new HashMap<>();
        paramBody.put("operationVOList", operationVOList.toString());
        paramBody.put("fieldList", fieldList.toString());

        String result = HTTPSClientUtil.doPost(httpClient, url, paramHeader, paramBody);
        //String result = HTTPSClientUtil.doGet(httpsClient, url, null, null);
        System.out.println(result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        return jsonObject;
    }

*/

    /**
     * 刷新列表(策略试运算)
     * @param strategyId
     * @return
     */
    @Override
    public Map<String, Object> getTrialListByStrategyId(Long strategyId) {
        Map<String, Object> result = new HashMap<>();
        List<String> strategyIdList = strategyMapper.selectByIdForInitId(strategyId);
        if (strategyIdList!=null){
            List<TrialOperation> trialOperations = trialOperationMapper.findOperationListByStrategyIdLsit(strategyIdList);
            trialOperations.forEach(trialOperation -> {
                if (trialOperation.getStatusCd().equals(TrialStatus.ISEE_PUBLISH_SUCCESS.getValue())
                        || trialOperation.getStatusCd().equals(TrialStatus.CHANNEL_PUBLISH_SUCCESS.getValue())){
                    Object o = redisUtils.get("SPECIAL_NUM_" + trialOperation.getBatchNum());
                    if ( o != null && "1000".equals(o.toString())){
                        trialOperation.setStatusCd(TrialStatus.SPECIAL_PUBLISH_SUCCESS.getValue());
                        trialOperationMapper.updateByPrimaryKey(trialOperation);
                    }
                }
            });
            PageHelper.startPage(1,10);
            trialOperations = trialOperationMapper.findOperationListByStrategyIdLsit(strategyIdList);
            Page pageInfo = new Page(new PageInfo(trialOperations));
            List<TrialOperationDetail> operationDetailList = supplementOperation(trialOperations);
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg", operationDetailList);
            result.put("pageInfo",pageInfo);
        }else {
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "strategyIdList isEmpty");
        }
        return result;
    }

    /**
     * 刷新列表(清单导入)
     * @param ruleId
     * @return
     */
    @Override
    public Map<String, Object> getTrialListByRuleId(Long ruleId) {
        Map<String, Object> result = new HashMap<>();
        List<String> strategyIdList = ruleMapper.selectByIdForInitId(ruleId);
        List<TrialOperation> trialOperations = trialOperationMapper.findOperationListByStrategyIdLsit(strategyIdList);
        List<TrialOperationDetail> operationDetailList = supplementOperation(trialOperations);
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", operationDetailList);
        return result;
    }

    //试算记录补充信息
    private List<TrialOperationDetail> supplementOperation(List<TrialOperation> trialOperations) {
        List<TrialOperationDetail> operationDetailList = new ArrayList<>();
        for (TrialOperation trialOperation : trialOperations) {
            TrialOperationDetail detail = BeanUtil.create(trialOperation, new TrialOperationDetail());
            detail.setBatchNumSt(trialOperation.getBatchNum().toString());
            if (trialOperation.getUpdateDate() != null && !trialOperation.getStatusCd().equals(TrialStatus.SAMPEL_GOING.getValue())) {
                Long cost = (trialOperation.getUpdateDate().getTime() - trialOperation.getCreateDate().getTime());
                cost = cost<0L ? 0L : cost;
                if (cost>1000){
                    detail.setCost(cost/1000 + "s");
                }else {
                    detail.setCost(cost+"ms");
                }
            }
            //如果是预下发并且状态是全量试算成功给前端一个标记
            Object o = redisUtils_es.get("SPECIFIEDNUM_" + trialOperation.getBatchNum());
            if (trialOperation.getStatusCd().equals("5000")){
                if ( o != null && !"".equals(o.toString())){
                    detail.setFlg("true");
                }
            }
            operationDetailList.add(detail);
            //如果是分批下发并且状态是全量试算成功给前端一个标记
            Object p = redisUtils_es.get("SPECIFIED_File_NUM_" + trialOperation.getBatchNum());
            if (!trialOperation.getStatusCd().equals(TrialStatus.SAMPEL_GOING.getValue())
                    && !trialOperation.getStatusCd().equals(TrialStatus.SAMPEL_SUCCESS.getValue())
                    && !trialOperation.getStatusCd().equals(TrialStatus.SAMPEL_FAIL.getValue())
                    && !trialOperation.getStatusCd().equals(TrialStatus.ALL_SAMPEL_GOING.getValue())
                    && !trialOperation.getStatusCd().equals(TrialStatus.ALL_SAMPEL_FAIL.getValue())){
                if ( p != null && !"".equals(p.toString())){
                    detail.setBatchFlg("true");
                }
            }
        }

        return operationDetailList;
    }

    /**
     * 下发参数
     * @param operationVO
     * @param batchNum
     * @param ruleId
     * @param isSample
     * @return
     */
    @Override
    public TrialOperationParamES getTrialOperationParamES(TrialOperationVO operationVO, Long batchNum, Long ruleId, boolean isSample,List<TarGrpCondition> conditions) {
        TrialOperationParamES param = new TrialOperationParamES();
        param.setRuleId(ruleId);
        MktStrategyConfRuleDO confRule = ruleMapper.selectByPrimaryKey(ruleId);
        if (confRule != null) {
            param.setRuleName(confRule.getMktStrategyConfRuleName());
            param.setTarGrpId(confRule.getTarGrpId());
        }
        if (!isSample){
            // 获取规则信息
            Map<String, Object> mktStrategyConfRuleMap = mktStrategyConfRuleService.getMktStrategyConfRule(ruleId);
            MktStrategyConfRule mktStrategyConfRule = (MktStrategyConfRule) mktStrategyConfRuleMap.get("mktStrategyConfRule");

            // 获取销售品集合
            Map<String, Object> productRuleListMap = productService.getProductRuleList(UserUtil.loginId(), mktStrategyConfRule.getProductIdlist());
            List<MktProductRule> mktProductRuleList = (List<MktProductRule>) productRuleListMap.get("resultMsg");
            ArrayList<MktProductRuleES> mktProductRuleEsList = new ArrayList<>();
            for (MktProductRule rule : mktProductRuleList){
                MktProductRuleES es = BeanUtil.create(rule,new MktProductRuleES());
                es.setPriority(es.getPriority()==null ? 0L : es.getPriority());
                mktProductRuleEsList.add(es);
            }
            param.setMktProductRuleList(mktProductRuleEsList);

            // 获取推送渠道
            List<MktCamChlConfDetail> mktCamChlConfDetailList = new ArrayList<>();
            ArrayList<MktCamChlConfDetailES> mktCamChlConfDetaiEslList = new ArrayList<>();
            List<MktCamChlConfDetail> mktCamChlConfList = mktStrategyConfRule.getMktCamChlConfDetailList();
            if(mktStrategyConfRule.getMktCamChlResultList()!=null){
                for (MktCamChlResult mktCamChlResult:mktStrategyConfRule.getMktCamChlResultList()) {
                    if(mktCamChlResult.getMktCamChlConfDetailList()!=null){
                        for (MktCamChlConfDetail mktCamChlConfDetail : mktCamChlResult.getMktCamChlConfDetailList()) {
                            mktCamChlConfList.add(mktCamChlConfDetail);
                            // 保存服务包标签到渠道属性备注中
                            List<MktCamChlConfAttr> mktCamChlConfAttrList = mktCamChlConfDetail.getMktCamChlConfAttrList();
                            for (MktCamChlConfAttr attr : mktCamChlConfAttrList) {
                                if (SERVICE_PACKAGE.getArrId().equals(attr.getAttrId())) {
                                    String attrValue = attr.getAttrValue();
                                    ServicePackage servicePackage = servicePackageMapper.selectByPrimaryKey(Long.valueOf(attrValue));
                                    MktCamChlConfAttrDO mktCamChlConfAttrDO = BeanUtil.create(attr, new MktCamChlConfAttrDO());
                                    mktCamChlConfAttrDO.setRemark(servicePackage.getLabel());
                                    mktCamChlConfAttrMapper.updateByPrimaryKey(mktCamChlConfAttrDO);
                                    //redisUtils.del("CHL_CONF_DETAIL_" + mktCamChlConfAttrDO.getEvtContactConfId());
                                }
                            }
                        }
                    }
                }
            }
            if (mktCamChlConfList != null) {
                for (MktCamChlConfDetail mktCamChlConf : mktCamChlConfList) {
                    Map<String, Object> mktCamChlConfDetailMap = mktCamChlConfService.getMktCamChlConf(mktCamChlConf.getEvtContactConfId());
                    MktCamChlConfDetail mktCamChlConfDetail = (MktCamChlConfDetail) mktCamChlConfDetailMap.get("mktCamChlConfDetail");
                    MktCamChlConfDetailES es = BeanUtil.create(mktCamChlConfDetail,new MktCamChlConfDetailES());
                    if (mktCamChlConfDetail.getCamScript()!=null){
                        CamScriptES camScriptES = BeanUtil.create(mktCamChlConfDetail.getCamScript(),new CamScriptES());
                        if (camScriptES.getScriptDesc()!=null && !camScriptES.getScriptDesc().equals("")){
                            if (camScriptES.getScriptDesc().contains("\n")){
                                camScriptES.setScriptDesc(camScriptES.getScriptDesc().replace("\n",""));
                            }
                            if (camScriptES.getScriptDesc().contains("\r")){
                                camScriptES.setScriptDesc(camScriptES.getScriptDesc().replace("\r",""));
                            }
                        }
                        es.setCamScript(camScriptES);
                    }
                    ArrayList<MktCamChlConfAttrES> attrs = new ArrayList<>();
                    ArrayList<VerbalVOES> verbalES = new ArrayList<>();
                    if (mktCamChlConfDetail.getMktCamChlConfAttrList()!=null){
                        for (MktCamChlConfAttr attr : mktCamChlConfDetail.getMktCamChlConfAttrList()){
                            MktCamChlConfAttrES attrES = BeanUtil.create(attr,new MktCamChlConfAttrES());
                            attrs.add(attrES);
                        }
                    }
                    if (mktCamChlConfDetail.getVerbalVOList()!=null){
                        for (VerbalVO verbalVO : mktCamChlConfDetail.getVerbalVOList()){
                            VerbalVOES verbalVOES = BeanUtil.create(verbalVO,new VerbalVOES());
                            verbalES.add(verbalVOES);
                        }
                    }
                    es.setVerbalVOList(verbalES);
                    es.setMktCamChlConfAttrList(attrs);
                    mktCamChlConfDetaiEslList.add(es);
                }
            }
            param.setMktCamChlConfDetailList(mktCamChlConfDetaiEslList);
        }
        // 设置批次号
        param.setBatchNum(batchNum);
        //redis取规则
        String rule = "";
        List<LabelResult> labelResultList = new ArrayList<>();
        ArrayList<LabelResultES> labelResultES = new ArrayList<>();

        //获取规则
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            MktStrategyConfRuleDO ruleDO = ruleMapper.selectByPrimaryKey(ruleId);
            if (ruleDO!=null || isSample){
                Future<Map<String, Object>> future = executorService.submit(new TarGrpRuleTask(operationVO.getCampaignId(),operationVO.getStrategyId(), ruleDO, redisUtils,isSample,conditions));
                rule = future.get().get("express").toString();
                labelResultList = ( List<LabelResult>)future.get().get("labelResultList");
            }
            // 关闭线程池
            if (!executorService.isShutdown()) {
                executorService.shutdown();
            }
        }catch (Exception e){
            e.printStackTrace();
            // 关闭线程池
            if (!executorService.isShutdown()) {
                executorService.shutdown();
            }
        }
        System.out.println("*************************" + rule);
        param.setRule(rule);
        for (LabelResult labelResult : labelResultList){
            LabelResultES labelEs = BeanUtil.create(labelResult,new LabelResultES());
            labelResultES.add(labelEs);
        }
        param.setLabelResultList(labelResultES);
        return param;
    }

    class TarGrpRuleTask implements Callable<Map<String,Object>>{
        private Long mktCampaignId;

        private Long mktStrategyConfId;

        private MktStrategyConfRuleDO mktStrategyConfRuleDO;

        private RedisUtils redisUtils;
        private boolean isSample;
        private List<TarGrpCondition> conditions;



        public TarGrpRuleTask(Long mktCampaignId, Long mktStrategyConfId, MktStrategyConfRuleDO mktStrategyConfRuleDO, RedisUtils redisUtils
                ,boolean isSample,List<TarGrpCondition> conditions) {
            this.mktCampaignId = mktCampaignId;
            this.mktStrategyConfId = mktStrategyConfId;
            this.mktStrategyConfRuleDO = mktStrategyConfRuleDO;
            this.redisUtils = redisUtils;
            this.isSample = isSample;
            this.conditions = conditions;
        }

        @Override
        public Map<String, Object> call() {
            Map<String, Object> result = new HashMap<>();
            List<TarGrpCondition> tarGrpConditionDOs =  new ArrayList<>();
            //查询分群规则list
            Long tarGrpId = 0L;
            if (conditions==null){
                tarGrpId = mktStrategyConfRuleDO.getTarGrpId();
                tarGrpConditionDOs = tarGrpConditionMapper.listTarGrpCondition(tarGrpId);
            }else{
                tarGrpConditionDOs = conditions;
                tarGrpId = -1L;
            }
            //过滤规则
            String prodFilter = "0";
            List<Map<String, String>> sysFilList = sysParamsMapper.listParamsByKey("CUST_PRODUCT_FILTER");
            if (sysFilList != null && !sysFilList.isEmpty()) {
                prodFilter = sysFilList.get(0).get("value");
            }
            if ("1".equals(prodFilter)){
                targrpCondition(mktCampaignId,tarGrpConditionDOs);
            }
            System.out.println(JSON.toJSONString(tarGrpConditionDOs));
            List<LabelResult> labelResultList = new ArrayList<>();
            List<String> codeList = new ArrayList<>();

            StringBuilder express = new StringBuilder();
            if (tarGrpId != null && tarGrpId != 0) {
                //将规则拼装为表达式
                if (tarGrpConditionDOs != null && tarGrpConditionDOs.size() > 0) {
                    express.append("if(");
                    //遍历所有规则
                    for (int i = 0; i < tarGrpConditionDOs.size(); i++) {
                        LabelResult labelResult = new LabelResult();
                        String type = tarGrpConditionDOs.get(i).getOperType();
                        Label label = injectionLabelMapper.selectByPrimaryKey(Long.parseLong(tarGrpConditionDOs.get(i).getLeftParam()));
                        if (label==null){
                            continue;
                        }
                        labelResult.setLabelCode(label.getInjectionLabelCode());
                        labelResult.setLabelName(label.getInjectionLabelName());
                        labelResult.setRightOperand(label.getLabelType());
                        labelResult.setRightParam(tarGrpConditionDOs.get(i).getRightParam());
                        labelResult.setClassName(label.getClassName());
                        labelResult.setOperType(type);
                        labelResult.setLabelDataType(label.getLabelDataType()==null ? "1100" : label.getLabelDataType());
                        labelResultList.add(labelResult);
                        codeList.add(label.getInjectionLabelCode());
                        express.append("(");
                        express.append(label.getInjectionLabelCode());
                        if ("1000".equals(type)) {
                            express.append(">");
                        } else if ("2000".equals(type)) {
                            express.append("<");
                        } else if ("3000".equals(type)) {
                            express.append("==");
                        } else if ("4000".equals(type)) {
                            express.append("!=");
                        } else if ("5000".equals(type)) {
                            express.append(">=");
                        } else if ("6000".equals(type)) {
                            express.append("<=");
                        } else if ("7000".equals(type)) {
                            express.append("in");
                        }else if ("7200".equals(type)) {
                            express.append("@@@@");//区间于
                        } else if ("7100".equals(type)) {
                            express.append("notIn");
                        }

                        if (label.getLabelDataType().equals("1100") && "200".equals(String.valueOf(tarGrpConditionDOs.get(i).getUpdateStaff()))){
                            String date = tarGrpConditionDOs.get(i).getRightParam();
                            if (Operator.BETWEEN.getValue().toString().equals(tarGrpConditionDOs.get(i).getOperType())) {
                                String[] conditions = tarGrpConditionDOs.get(i).getRightParam().split(",");
                                date = DateUtil.getPreDay(Integer.valueOf(conditions[0])) + "," + DateUtil.getPreDay(Integer.valueOf(conditions[1]));
                            } else {
                                date = DateUtil.getPreDay(Integer.valueOf(tarGrpConditionDOs.get(i).getRightParam()));
                            }
                            express.append(date);
                        }else if (label.getInjectionLabelCode().equals("PROM_LIST")){
                            FilterRule filterRule = filterRuleMapper.selectByPrimaryKey(Long.valueOf(tarGrpConditionDOs.get(i).getRightParam()));
                            List<String> stringList = new ArrayList<>();
                            if (filterRule!=null && filterRule.getChooseProduct()!=null){
                                List<String> list = ChannelUtil.StringToList(filterRule.getChooseProduct());
                                for (String id : list){
                                    Offer offer = offerMapper.selectByPrimaryKey(Integer.valueOf(id));
                                    if (offer!=null){
                                        stringList.add(offer.getOfferNbr());
                                    }
                                }
                                express.append(ChannelUtil.list2String(stringList,","));
                            }

                        }else {
                            express.append(tarGrpConditionDOs.get(i).getRightParam());
                        }
                        express.append(")");
                        if (i + 1 != tarGrpConditionDOs.size()) {
                            express.append("&&");
                        }
                    }
                    express.append(") {return true} else {return false}");
                }else {
                    express.append("");
                }
                System.out.println( ">>>>>>>>express->>>>:" + JSON.toJSONString(express));

                //标签条件编码集合 试算展示用
                redisUtils.hset("LABEL_CODE_"+mktStrategyConfId,tarGrpId+"",codeList);
                System.out.println("TAR_GRP_ID>>>>>>>>>>" + tarGrpId + ">>>>>>>>codeList->>>>:" + JSON.toJSONString(codeList));
            }
            result.put("express",express.toString());
            result.put("labelResultList",labelResultList);
            return result;
        }

    }

    private void  targrpCondition(Long campaignId, List<TarGrpCondition> tarGrpConditionDOs){
        List<String> typeList = new ArrayList<>();
        typeList.add("3000");
        List<FilterRule> filterRuleList = filterRuleMapper.selectFilterRuleListByStrategyId(campaignId,typeList);
        List<String> stringList = new ArrayList<>();
        Label label = labelMapper.selectByLabelCode("PROM_LIST");
        for (FilterRule filterRule : filterRuleList){
            if (filterRule!=null && filterRule.getChooseProduct()!=null){
                TarGrpCondition condition = new TarGrpCondition();
                condition.setLeftParam(label.getInjectionLabelId().toString());
//                List<String> list = ChannelUtil.StringToList(filterRule.getChooseProduct());
//                for (String id : list){
//                    Offer offer = offerMapper.selectByPrimaryKey(Integer.valueOf(id));
//                    if (offer!=null){
//                        stringList.add(offer.getOfferNbr());
//                    }
//                }
//                String rightParam =  ChannelUtil.list2String(stringList,",");
                condition.setRightParam(filterRule.getRuleId().toString());
                condition.setOperType(filterRule.getOperator().equals("1000")? "7000" : "7100");
                tarGrpConditionDOs.add(condition);
            }
        }
    }

    @Override
    public Map<String, Object> importUserListByExcel() throws IOException {
        logger.info("定时任务importUserListByExcel启动");
        HashMap<String, Object> map = new HashMap<>();
        MktDttsLog mktDttsLog =new MktDttsLog();
        Date beginTime = new Date();
        SftpUtils sftpUtils = new SftpUtils();
        final ChannelSftp sftp = sftpUtils.connect(ftpAddress, ftpPort, ftpName, ftpPassword);
        logger.info("sftp已获得连接");
        String path = sftpUtils.cd(uploadExcelPath, sftp);
        String tempFilePath = downloadFilePath + "/";
        List<String> files = new ArrayList<>();
        List<String> excelFiles = new ArrayList<>();
        try {
            List<String> allFile = sftpUtils.listFiles(path, sftp);
            for (String fileName : allFile) {
                if (".".equals(fileName) || "..".equals(fileName)) {
                    continue;
                }
//                if (fileName.contains("xlsx")){
//                    files.add(fileName);
//                }
                if (fileName.contains("EVT")){
                    logger.info("excel批量清单文件目录夹名称："+fileName);
                    files.add(fileName);
                }
            }
            logger.info("批量excel已选择文件夹数量 "+ files.size());
        } catch (Exception e) {
            logger.error("批量excel文件读取失败", e);
        }
        //修改遍历文件夹下的文件
        for (String file : files) {
            try {
                List<String> allExcelFile = sftpUtils.listFiles(uploadExcelPath+file, sftp);
                for (String fileName : allExcelFile) {
                    if (".".equals(fileName) || "..".equals(fileName)) {
                        continue;
                    }
                    if (fileName.contains("xlsx")){
                        excelFiles.add(fileName);
                    }
                }
            } catch (SftpException e) {
                e.printStackTrace();
            }
        }
        logger.info("批量excel已选择文件数量 "+ excelFiles.size());
        List<String> uploadList = new ArrayList<>();
        try {
            for (String fileName : excelFiles) {
                // 下载文件到本地
                File file = new File(fileName);
                if (!file.exists()) {
                    logger.info("开始下载文件--->>>" + fileName + " --->>> 时间：" + DateUtil.formatDate(new Date()));
                    String pathxx = sftpUtils.cd(uploadExcelPath+"/"+fileName.split("_")[0], sftp);
                    boolean download = sftpUtils.download(sftp, pathxx+"/", fileName, tempFilePath);
                    if (!download) {
                        logger.info("文件下载失败","文件名：" + fileName);
                    }
                    logger.info("结束下载文件--->>>" + fileName + " --->>> 时间：" + DateUtil.formatDate(new Date()));
                    //                sftpUtils.delete(path + "/", fileName, sftp);
                    //                logger.info("删除校验文件--->>>" + fileName + " --->>> 时间：" + DateUtil.formatDate(new Date()));
                    uploadList.add(fileName);
                }
            }
        } catch (Exception e) {
            logger.info("批量excel下载文件失败");
            e.printStackTrace();
        }
//        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
//        String format = fmt.format(new Date());
        //判断文件是否存在 不存在则创建新文件夹
//        File newFile = new File("/app/ftp/msc/userlist/feesExcelPreserve");
//        if(!newFile .exists()) {
//            newFile.setWritable(true, false);
//            newFile.mkdirs();
//            System.out.println("文件不存在创建文件夹名称"+newFile);
//        }
        sftpUtils.cd("/app/ftp/msc/userlist/feesExcelPreserve/", sftp);
        try {
            for (String s : uploadList) {
                logger.info("准备上传文件名称:" + s);
                File file = new File(tempFilePath+s);
//                File file = new File("D:/idea/cpc/CPCT/app/"+s);
                System.out.println("文件是否存在："+file.exists());
                if (file.exists()) {
                    boolean uploadResult = sftpUtils.uploadFile(uploadExcelPath, s, new FileInputStream(file), sftp);
                    if (uploadResult) {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                                ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
                        importExcelUserList(multipartFile);
                        logger.info("解析成功，开始删除本地文件！");
                        boolean b1 = delFile(tempFilePath+s);
                        if (b1) {
                            logger.info("删除本地文件成功！");
                        }
                    }
                }
            }
            quitSftp(sftp);
            map.put("resultCode","200");
            map.put("resultMsg","成功");
            //定時任務
            mktDttsLogService.saveMktDttsLog("8000","成功",beginTime,new Date(),"成功",null);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("resultCode","500");
            map.put("resultMsg","失败");
            //定時任務
            mktDttsLogService.saveMktDttsLog("8000","失败",beginTime,new Date(),"失败",e.toString());
        }
        return map;
    }

    public static boolean delFile(String path) {
        Boolean bool = false;
        File file = new File(path);
        try {
            if (file.exists()) {
                file.delete();
                bool = true;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return bool;
    }

    private boolean quitSftp(ChannelSftp sftp) throws JSchException {
        if (sftp.isConnected()) {
            sftp.disconnect();
        }
        if (sftp.getSession().isConnected()) {
            sftp.getSession().disconnect();
        }
        sftp.quit();
        return true;
    }

    @Override
    public TrialOperation selectByBatchNum(String batchNum) {
        return trialOperationMapper.selectByBatchNum(batchNum);
    }
}
