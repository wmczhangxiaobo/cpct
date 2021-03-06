package com.zjtelcom.cpct.dubbo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ctzj.biz.asset.model.dto.AssetDto;
import com.ctzj.biz.asset.model.dto.AssetPromDto;
import com.ctzj.bss.customer.data.carrier.outbound.api.CtgCacheAssetService;
import com.ctzj.bss.customer.data.carrier.outbound.model.ResponseResult;
import com.ctzj.smt.bss.cache.service.api.CacheEntityApi.ICacheIdMappingEntityQryService;
import com.ctzj.smt.bss.cache.service.api.CacheEntityApi.ICacheProdEntityQryService;
import com.ctzj.smt.bss.cache.service.api.CacheEntityApi.ICacheRelEntityQryService;
import com.ctzj.smt.bss.cache.service.api.CacheIndexApi.ICacheOfferRelIndexQryService;
import com.ctzj.smt.bss.cache.service.api.CacheIndexApi.ICacheProdIndexQryService;
import com.ctzj.smt.bss.cache.service.api.model.CacheResultObject;
import com.ctzj.smt.bss.cooperate.service.dubbo.IContactTaskReceiptService;
import com.ctzj.smt.bss.customer.model.dataobject.OfferProdInstRel;
import com.ctzj.smt.bss.customer.model.dataobject.ProdInst;
import com.ctzj.smt.bss.customer.model.dataobject.RowIdMapping;
import com.google.common.collect.Sets;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.Operator;
import com.ql.util.express.rule.RuleResult;
import com.telin.dubbo.service.QueryBindByAccCardService;
import com.zjpii.biz.serv.YzServ;
import com.zjpii.biz.service.uam.SyncService;
import com.zjtelcom.cpct.config.IndexList;
import com.zjtelcom.cpct.dao.blacklist.BlackListMapper;
import com.zjtelcom.cpct.dao.campaign.*;
import com.zjtelcom.cpct.dao.channel.*;
import com.zjtelcom.cpct.dao.event.*;
import com.zjtelcom.cpct.dao.filter.FilterRuleMapper;
import com.zjtelcom.cpct.dao.grouping.OrgGridRelMapper;
import com.zjtelcom.cpct.dao.org.StaffGisRelMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfRuleMapper;
import com.zjtelcom.cpct.dao.strategy.MktStrategyConfRuleRelMapper;
import com.zjtelcom.cpct.dao.system.SysParamsMapper;
import com.zjtelcom.cpct.domain.blacklist.BlackListDO;
import com.zjtelcom.cpct.domain.campaign.*;
import com.zjtelcom.cpct.domain.channel.*;
import com.zjtelcom.cpct.domain.event.CommonRegion;
import com.zjtelcom.cpct.domain.event.OfferExpenseDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfRuleDO;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfRuleRelDO;
import com.zjtelcom.cpct.domain.system.SysParams;
import com.zjtelcom.cpct.dto.campaign.MktCamEvtRel;
import com.zjtelcom.cpct.dto.event.ContactEvt;
import com.zjtelcom.cpct.dto.event.ContactEvtMatchRul;
import com.zjtelcom.cpct.dto.event.EventMatchRulCondition;
import com.zjtelcom.cpct.dto.filter.FilterRule;
import com.zjtelcom.cpct.dubbo.service.CamApiSerService;
import com.zjtelcom.cpct.dubbo.service.CamApiService;
import com.zjtelcom.cpct.dubbo.service.EventApiService;
import com.zjtelcom.cpct.enums.AreaCodeEnum;
import com.zjtelcom.cpct.enums.AreaNameEnum;
import com.zjtelcom.cpct.enums.ConfAttrEnum;
import com.zjtelcom.cpct.enums.StatusCode;
import com.zjtelcom.cpct.service.channel.ChannelService;
import com.zjtelcom.cpct.service.channel.SearchLabelService;
import com.zjtelcom.cpct.service.es.EsHitsService;
import com.zjtelcom.cpct.service.event.EventRedisService;
import com.zjtelcom.cpct.service.impl.dubbo.CamCpcSpecialLogic;
import com.zjtelcom.cpct.util.ChannelUtil;
import com.zjtelcom.cpct.util.DateUtil;
import com.zjtelcom.cpct.util.MapUtil;
import com.zjtelcom.cpct.util.RedisUtils;
import com.zjtelcom.es.es.service.EsService;
import com.zjtelcom.es.es.service.EsServiceInfo;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Calendar.MONTH;

@Service
public class EventApiServiceImpl implements EventApiService {

    @Value("${thread.maxPoolSize}")
    private int maxPoolSize;
    @Value("${table.infallible}")
    private String defaultInfallibleTable;
    private static final Logger log = LoggerFactory.getLogger(EventApiServiceImpl.class);

    @Autowired
    private ContactEvtMapper contactEvtMapper; //事件总表

    @Autowired
    private MktCamEvtRelMapper mktCamEvtRelMapper; //事件与活动关联表

    @Autowired
    private MktCampaignMapper mktCampaignMapper; //活动基本信息

    @Autowired
    private MktStrategyConfMapper mktStrategyConfMapper; //策略基本信息

    @Autowired
    private MktStrategyConfRuleMapper mktStrategyConfRuleMapper;//策略规则

    @Autowired
    private FilterRuleMapper filterRuleMapper; //过滤规则

    @Autowired
    private MktCamChlConfAttrMapper mktCamChlConfAttrMapper; //协同渠道配置基本信息

    @Autowired
    private MktCamChlConfMapper mktCamChlConfMapper; //协同渠道配置的渠道

    @Autowired
    private MktCamScriptMapper mktCamScriptMapper; //营销脚本

    @Autowired
    private MktVerbalMapper mktVerbalMapper; //话术

    @Autowired
    private InjectionLabelMapper injectionLabelMapper; //标签因子

    @Autowired
    private EsHitsService esHitService;  //es存储

    @Autowired
    private RedisUtils redisUtils;  // redis方法

    @Autowired
    private ContactEvtItemMapper contactEvtItemMapper;  // 事件采集项

    @Autowired(required = false)
    private IContactTaskReceiptService iContactTaskReceiptService; //协同中心dubbo

    @Autowired(required = false)
    private YzServ yzServ; //因子实时查询dubbo服务

    @Autowired(required = false)
    private ContactChannelMapper contactChannelMapper; //渠道信息

    @Autowired(required = false)
    private MktCamChlResultMapper mktCamChlResultMapper;

    @Autowired(required = false)
    private MktCamChlResultConfRelMapper mktCamChlResultConfRelMapper;

    @Autowired(required = false)
    private MktStrategyConfRuleRelMapper mktStrategyConfRuleRelMapper;

    @Autowired
    private ContactEvtMatchRulMapper contactEvtMatchRulMapper; //事件规则

    @Autowired
    private EventMatchRulConditionMapper eventMatchRulConditionMapper;  //事件规则条件

    @Autowired(required = false)
    private SysParamsMapper sysParamsMapper;  //查询系统参数

    @Autowired(required = false)
    private SearchLabelService searchLabelService;  //查询活动下使用的所有标签

    @Autowired(required = false)
    private CamApiService camApiService; // 活动任务

    @Autowired(required = false)
    private CamApiSerService camApiSerService; // 服务活动任务

    @Autowired(required = false)
    private EsService esService;

    @Autowired(required = false)
    private CtgCacheAssetService ctgCacheAssetService;// 销售品过滤方法

    @Autowired(required = false)
    private QueryBindByAccCardService queryBindByAccCardService; // 通过号码查询绑定状态

    @Autowired(required = false)
    private ICacheProdIndexQryService iCacheProdIndexQryService;

    @Autowired(required = false)
    private ICacheProdEntityQryService iCacheProdEntityQryService;

    @Autowired(required = false)
    private ICacheOfferRelIndexQryService iCacheOfferRelIndexQryService;

    @Autowired(required = false)
    private ICacheRelEntityQryService iCacheRelEntityQryService;

    @Autowired(required = false)
    private ICacheIdMappingEntityQryService iCacheIdMappingEntityQryService;

    @Autowired
    private EventRedisService eventRedisService;

    @Autowired
    private BlackListMapper blackListMapper;

    @Autowired(required = false)
    private EsServiceInfo esServiceInfo;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private CamCpcSpecialLogic camCpcSpecialLogic;

    @Autowired
    private OrgGridRelMapper orgGridRelMapper;
    @Autowired
    private CommonRegionMapper commonRegionMapper;

    @Autowired
    private ChannelService channelService;

    private final static String USED_FLOW = "used_flow";

    private final static String TOTAL_FLOW = "total_flow";

    @Override
    public Map<String, Object> CalculateCPC(Map<String, Object> map) {

        log.info("异步事件接入:" + map.get("reqId"));

        //初始化返回结果
        Map<String, Object> result = new HashMap();
        //构造后面线程要用的参数
        Map<String, String> params = new HashMap<>();

        //初始化es log
        JSONObject esJson = new JSONObject();

        //事件传入参数 开始--------------------
        //获取事件code（必填）
        String eventCode = (String) map.get("eventCode");
        //获取渠道编码（必填）
        String channelCode = (String) map.get("channelCode");
        //本地网
        String lanId = (String) map.get("lanId");
        //业务号码（必填）（资产号码）
        String accNbr = (String) map.get("accNbr");
        //集成编号（必填）（资产集成编码）
        String integrationId = (String) map.get("integrationId");
        //客户编码（必填）
        String custId = (String) map.get("custId");
        //销售员编码（必填）
        String reqId = (String) map.get("reqId");
        //采集时间(yyyy-mm-dd hh24:mm:ss)
        String evtCollectTime = (String) map.get("evtCollectTime");

        //自定义参数集合json字符串
        String evtContent = (String) map.get("evtContent");
        //事件传入参数 结束--------------------

        //构造下级线程使用参数
        params.put("eventCode", eventCode); //事件编码
        params.put("channelCode", channelCode); //渠道编码
        params.put("lanId", lanId); //本地网
        params.put("custId", custId); //客户编码
        params.put("reqId", reqId); //流水号
        params.put("evtCollectTime", evtCollectTime); //事件触发时间
        params.put("evtContent", evtContent); //事件采集项
        params.put("accNbr", accNbr); //资产号码
        params.put("integrationId", integrationId); //资产集成编码

        esJson.put("reqId", reqId);
        esJson.put("eventCode", eventCode);
        esJson.put("integrationId", integrationId);
        esJson.put("accNbr", accNbr);
        esJson.put("custId", custId);
        esJson.put("evtCollectTime", evtCollectTime);
        esJson.put("hit", false);
        esJson.put("success", false);
        esJson.put("msg", "事件接入，开始异步流程");
        esHitService.save(esJson, IndexList.EVENT_MODULE, reqId);

        //异步
        AsyncCPC asyncCPC = new AsyncCPC(params);
        asyncCPC.start();

        result.put("reqId", reqId);
        result.put("resultCode", "1");
        result.put("resultMsg", "success");

        return result;
    }


    /**
     * 异步调用协同中心回调接口
     */
    private class AsyncCPC extends Thread {

        private Map<String, String> params;

        public AsyncCPC(Map<String, String> params) {
            this.params = params;
        }

        public void run() {
            //这里为线程的操作
            //就可以使用注入之后Bean了
            async();

        }

        public Map async() {

            //初始化返回结果
            Map<String, Object> result = new HashMap();
            result = new EventTask().cpc(params);
            //调用协同中心回调接口
            Map<String, Object> back = iContactTaskReceiptService.contactTaskReceipt(result);
            if (back != null) {
                if ("1".equals(back.get("resultCode"))) {
                    log.info("异步事件回调成功" + params.get("reqId"));
                    return null;
                }
            }
            log.info("异步事件回调失败" + params.get("reqId"));
            return result;
        }
    }

    /**
     * 同步计算
     *
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> CalculateCPCSync(Map<String, Object> map) {

        log.info("同步事件接入:" + map.get("reqId"));

        //初始化返回结果
        Map<String, Object> result = new HashMap();

        Map<String, String> params = new HashMap<>();

        //初始化es log
        JSONObject esJson = new JSONObject();

        //事件传入参数 开始--------------------
        //获取事件code（必填）
        String eventCode = (String) map.get("eventCode");
        //获取渠道编码（必填）
        String channelCode = (String) map.get("channelCode");
        //本地网
        String lanId = (String) map.get("lanId");
        //业务号码（必填）（资产号码）
        String accNbr = (String) map.get("accNbr");
        //集成编号（必填）（资产集成编码）
        String integrationId = (String) map.get("integrationId");
        //客户编码（必填）
        String custId = (String) map.get("custId");
        //流水号（必填）
        String reqId = (String) map.get("reqId");
        //采集时间(yyyy-mm-dd hh24:mm:ss)
        String evtCollectTime = (String) map.get("evtCollectTime");
        //自定义参数集合json字符串
        String evtContent = (String) map.get("evtContent");
        //事件传入参数 结束--------------------

        //构造下级线程使用参数
        params.put("eventCode", eventCode); //事件编码
        params.put("channelCode", channelCode); //渠道编码
        params.put("lanId", lanId); //本地网
        params.put("custId", custId); //客户编码
        params.put("reqId", reqId); //流水号
        params.put("evtCollectTime", evtCollectTime); //事件触发时间
        params.put("evtContent", evtContent); //事件采集项
        params.put("accNbr", accNbr); //资产号码
        params.put("integrationId", integrationId); //资产集成编码
        esJson.put("reqId", reqId);
        esJson.put("eventCode", eventCode);
        esJson.put("integrationId", integrationId);
        esJson.put("accNbr", accNbr);
        esJson.put("custId", custId);
        esJson.put("evtCollectTime", evtCollectTime);
        esJson.put("hit", false);
        esJson.put("success", false);
        esJson.put("msg", "事件接入，开始异步流程");
        esHitService.save(esJson, IndexList.EVENT_MODULE, reqId);

        //调用计算方法
        try {
            result = new EventTask().cpc(params);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("同步事件返回失败:" + map.get("reqId"), e.getMessage());
        }


        log.info("同步事件返回成功:" + map.get("reqId"));

        return result;

    }

    /**
     * 事件验证模块公共方法
     */
    class EventTask {

        public Map<String, Object> cpc(Map<String, String> map) {
            //记录开始时间
            long begin = System.currentTimeMillis();
            log.info("事件计算流程开始:" + map.get("eventCode") + "***" + map.get("reqId"));

            //初始化返回结果中的工单信息
            List<Map<String, Object>> activityList = new ArrayList<>();

            //初始化返回结果
            Map<String, Object> result = new HashMap();

            //构造返回结果

            String custId = map.get("custId");
            result.put("reqId", map.get("reqId"));
            result.put("custId", custId);
            result.put("taskList", activityList);
            Map<String, Object> evtContent = (Map<String, Object>) JSON.parse(map.get("evtContent"));
            List<Map<String, Object>> triggersList = new ArrayList<>();
            if (evtContent != null && !evtContent.isEmpty()) {
                for (Map.Entry entry : evtContent.entrySet()) {
                    Map<String, Object> trigger = new HashMap<>();
                    trigger.put("key", entry.getKey());
                    trigger.put("value", entry.getValue());
                    triggersList.add(trigger);
                }
                result.put("triggers", triggersList);
            }

            //初始化es log
            JSONObject esJson = new JSONObject();
            esJson.put("reqId", map.get("reqId"));

            //初始化入参出参的es log
            JSONObject paramsJson = new JSONObject();
            paramsJson.put("reqId", map.get("reqId"));
            paramsJson.put("intoParams", map);  //保存入参

            //es log
            esJson.put("reqId", map.get("reqId"));
            esJson.put("eventCode", map.get("eventCode"));
            esJson.put("integrationId", map.get("integrationId"));
            esJson.put("accNbr", map.get("accNbr"));
            esJson.put("custId", map.get("custId"));
            esJson.put("channel", map.get("channelCode"));
            esJson.put("lanId", map.get("lanId"));
            //如果没有接入时间  自己补上
            try {
                if (map.get("evtCollectTime") == null || "".equals(map.get("evtCollectTime"))) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    esJson.put("evtCollectTime", simpleDateFormat.format(new Date()));
                } else {
                    esJson.put("evtCollectTime", map.get("evtCollectTime"));
                }
            } catch (Exception e) {
                log.error("事件接入时间入参异常:" + map.get("reqId"));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                esJson.put("evtCollectTime", simpleDateFormat.format(new Date()));
            }


            try {
                // List<String> list = contactEvtMapper.selectChannelListByEvtCode(map.get("eventCode"));
                List<String> list = new ArrayList<>();
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("eventCode", map.get("eventCode"));
                Map<String, Object> channelCodeRedis = eventRedisService.getRedis("CHANNEL_CODE_LIST_", paramMap);
                if (channelCodeRedis != null) {
                    list = (List<String>) channelCodeRedis.get("CHANNEL_CODE_LIST_" + map.get("eventCode"));
                }

                if (list.isEmpty() || !list.contains(map.get("channelCode"))) {
                    esJson.put("hit", false);
                    esJson.put("success", true);
                    esJson.put("msg", "接入渠道不符");
                    esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));
                    log.info("接入渠道不符:" + map.get("reqId"));
                    result.put("CPCResultCode", "1000");
                    result.put("CPCResultMsg", "接入渠道不符");
                    return result;
                }
            } catch (Exception e) {
                esJson.put("hit", false);
                esJson.put("success", true);
                esJson.put("msg", "接入渠道查询异常");
                esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));
                e.printStackTrace();
                return result;
            }

            try {
                //事件验证开始↓↓↓↓↓↓↓↓↓↓↓↓↓l
                //解析事件采集项
                JSONObject evtParams = JSONObject.parseObject(map.get("evtContent"));
                //获取C4的数据用于过滤
                String c4 = null;
                if (evtParams != null) {
                    c4 = (String) evtParams.get("C4");
                }

                //根据事件code查询事件信息
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("eventCode", map.get("eventCode"));
                Map<String, Object> eventRedis = eventRedisService.getRedis("EVENT_", paramMap);
                ContactEvt event = new ContactEvt();
                if (eventRedis != null) {
                    event = (ContactEvt) eventRedis.get("EVENT_" + map.get("eventCode"));
                }
                if (event == null) {
                    esJson.put("hit", false);
                    esJson.put("success", true);
                    esJson.put("msg", "未找到相关事件");
                    esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));

                    log.error("未找到相关事件:" + map.get("reqId"));

                    result.put("CPCResultCode", "1000");
                    result.put("CPCResultMsg", "未找到相关事件");
                    return result;
                }
                //获取事件id
                Long eventId = event.getContactEvtId();

                esJson.put("eventId", eventId);

                //验证事件状态
                if (!"1000".equals(event.getStatusCd())) {
                    esJson.put("hit", false);
                    esJson.put("success", true);
                    esJson.put("msg", "事件已关闭");
                    esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));

                    log.info("事件已关闭:" + map.get("reqId"));

                    result.put("CPCResultCode", "1000");
                    result.put("CPCResultMsg", "事件已关闭");
                    return result;
                }

                //验证事件采集项
                List<EventItem> contactEvtItems = new ArrayList<>();
                Map<String, Object> evtItemsRedis = eventRedisService.getRedis("EVENT_ITEM_", eventId);
                if (evtItemsRedis != null) {
                    contactEvtItems = (List<EventItem>) evtItemsRedis.get("EVENT_ITEM_" + eventId);
                }

                List<Map<String, Object>> evtTriggers = new ArrayList<>();
                Map<String, Object> trigger;
                //事件采集项标签集合(事件采集项标签优先规则)
                Map<String, String> labelItems = new HashMap<>();

                StringBuilder stringBuilder = new StringBuilder();
                for (EventItem contactEvtItem : contactEvtItems) {
                    if (evtParams != null && evtParams.containsKey(contactEvtItem.getEvtItemCode())) {
                        //筛选出作为标签使用的事件采集项
                        if ("0".equals(contactEvtItem.getIsLabel())) {
                            labelItems.put(contactEvtItem.getEvtItemCode(), evtParams.getString(contactEvtItem.getEvtItemCode()));
                        }

                        trigger = new HashMap<>();
                        trigger.put("key", contactEvtItem.getEvtItemCode());
                        trigger.put("value", evtParams.get(contactEvtItem.getEvtItemCode()));
                        evtTriggers.add(trigger);
                    } else {
                        //记录缺少的事件采集项
                        stringBuilder.append(contactEvtItem.getEvtItemCode()).append("、");
                    }
                }

                if (stringBuilder.length() > 0) {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }

                //事件采集项返回参数
                if (stringBuilder.length() > 0) {

                    //保存es log
                    long cost = System.currentTimeMillis() - begin;
                    esJson.put("timeCost", cost);
                    esJson.put("hit", false);
                    esJson.put("success", true);
                    esJson.put("msg", "事件采集项验证失败，缺少：" + stringBuilder.toString());
                    esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));

                    log.error(map.get("reqId") + "事件采集项验证失败:" + map.get("reqId"));

                    result.put("CPCResultCode", "1000");
                    result.put("CPCResultMsg", "事件采集项验证失败，缺少：" + stringBuilder.toString());
                    return result;
                }

                //!!!验证事件规则命中
                Map<String, Object> stringObjectMap = matchRulCondition(eventId, labelItems, map);
                if (!stringObjectMap.get("code").equals("success")) {

                    log.error("事件规则未命中:" + map.get("reqId"));

                    //判断不符合条件 直接返回不命中
                    result.put("CPCResultMsg", stringObjectMap.get("result"));
                    result.put("CPCResultCode", "1000");
                    esJson.put("hit", false);
                    esJson.put("success", true);
                    esJson.put("msg", stringObjectMap.get("result"));
                    esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));
                    return result;
                }

                //添加内置标签
                setInlayLabel(labelItems);

                //判断是否有流量事件,EVTS000001001,EVTS000001002
                // CPCP_USED_FLOW为已使用流量， CPCP_LEFT_FLOW为剩余流量, CPCP_NEED_FLOW 需要流量
                String eventCode = (String) map.get("eventCode");
                if ("EVTS000001001".equals(eventCode) || "EVTS000001002".equals(eventCode) && evtParams != null && evtParams.get("CPCP_USED_FLOW") != null && evtParams.get("CPCP_LEFT_FLOW") != null) {
                    String cpcpNeedFlow = getCpcpNeedFlow((String) evtParams.get("CPCP_USED_FLOW"), (String) evtParams.get("CPCP_LEFT_FLOW"));
                    labelItems.put("CPCP_NEED_FLOW", cpcpNeedFlow);
                }
                // 计费短信合并功能 CPCP_JIFEI_CONTENT
                if (evtParams != null) {
                    cpcpJifeiContent(labelItems, evtParams);
                }
                //获取事件推荐活动数
                int recCampaignAmount;
                String recCampaignAmountStr = event.getRecCampaignAmount();
                if (recCampaignAmountStr == null || "".equals(recCampaignAmountStr)) {
                    recCampaignAmount = 0;
                } else {
                    recCampaignAmount = Integer.parseInt(recCampaignAmountStr);
                }
                List<Map<String, Object>> resultByEvent = null;


                try {
                    //事件下所有活动的规则预校验，返回初步可命中活动
                    resultByEvent = getResultByEvent(eventId, map.get("eventCode"), map.get("lanId"), map.get("channelCode"), map.get("reqId"), map.get("accNbr"), c4, map.get("custId"));
                } catch (Exception e) {
                    esJson.put("hit", false);
                    esJson.put("success", true);
                    esJson.put("msg", "预校验返回异常");
                    esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));
                    e.printStackTrace();
                }

                // 固定必中规则提取
                // List<Map<String, Object>> resultByEvent2 = getBitslapByEvent(eventId, resultByEvent);

                if (resultByEvent == null || resultByEvent.size() <= 0) {
                    log.info("预校验为空");
                    long cost = System.currentTimeMillis() - begin;
                    esJson.put("timeCost", cost);
                    esJson.put("hit", false);
                    esJson.put("success", true);
                    esJson.put("msg", "活动均未命中");
                    esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));

                    //事件采集项没有客户编码
                    result.put("CPCResultCode", "1000");
                    result.put("CPCResultMsg", "succes 预校验为空");
                    result.put("taskList", activityList);

                    paramsJson.put("backParams", result);
                    esHitService.save(paramsJson, IndexList.PARAMS_MODULE, map.get("reqId"));

                    log.info("事件计算流程结束:" + map.get("eventCode") + "***" + map.get("reqId") + "（" + (System.currentTimeMillis() - begin) + "）");

                    return result;
                }

                //查询事件下使用的所有标签
                DefaultContext<String, Object> context = new DefaultContext<String, Object>();
                Map<String, String> mktAllLabels = new HashMap<>();
                Map<String, Object> eventLabelRedis = eventRedisService.getRedis("EVT_ALL_LABEL_", eventId);
                if (eventLabelRedis != null) {
                    mktAllLabels = (Map<String, String>) eventLabelRedis.get("EVT_ALL_LABEL_" + eventId);
                    if (mktAllLabels == null) {
                        esJson.put("hit", false);
                        esJson.put("msg", "获取事件下所有标签异常");
                        esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));
                        return Collections.EMPTY_MAP;
                    }
                }
                // 过滤事件采集项中的标签
                Map<String, String> mktAllLabel = filterLabel(eventCode,labelItems, mktAllLabels);

                //初始化结果集
                List<Future<Map<String, Object>>> threadList = new ArrayList<>();
                ExecutorService executorService = Executors.newCachedThreadPool();

                /***********************清单方案流程***************************/
                // 启动线程走清单流程
                List<String> mktCampaginIdList = new ArrayList<>();
                List<String> initIdList = new ArrayList<>();
                for (Map<String, Object> activeMap : resultByEvent) {
                    if (activeMap.get("mktCampaignCustMap") != null && !((Map<String, Object>) activeMap.get("mktCampaignCustMap")).isEmpty()) {
                        mktCampaginIdList.add(((Map<String, Object>) activeMap.get("mktCampaignCustMap")).get("mktCampaginId").toString());
                        initIdList.add(((Map<String, Object>) activeMap.get("mktCampaignCustMap")).get("initId").toString());
                    }
                }
                if (mktCampaginIdList != null && mktCampaginIdList.size() > 0) {
                    Future<Map<String, Object>> f = executorService.submit(new getCustListTask(mktCampaginIdList, initIdList, eventId, map.get("lanId"), custId, map, evtTriggers));
                    threadList.add(f);
                }
                /***********************************************************/


                //判断有没有客户级活动
                Boolean hasAsset = false;  //是否都是资产级活动
                Boolean hasCust = false;  //是否有客户级
                Boolean hasPackage = false;  //是否有套餐
                for (Map<String, Object> activeMap : resultByEvent) {
                    if (((Map) activeMap.get("mktCampaignMap")) != null && !((Map) activeMap.get("mktCampaignMap")).isEmpty()) {
                        if ((Integer) ((Map<String, Object>) activeMap.get("mktCampaignMap")).get("levelConfig") == 1) {  // 客户级
                            hasCust = true;
                        } else if ((Integer) ((Map<String, Object>) activeMap.get("mktCampaignMap")).get("levelConfig") == 2) { // 套餐级
                            hasPackage = true;
                        } else if ((Integer) ((Map<String, Object>) activeMap.get("mktCampaignMap")).get("levelConfig") == 0) { // 资产级
                            hasAsset = true;
                        }
                    }
                }

                // 资产级
                List<DefaultContext<String, Object>> resultMapList = new ArrayList<>();
                // 客户级
                List<DefaultContext<String, Object>> custResultMapList = new ArrayList<>();
                // 套餐级
                List<DefaultContext<String, Object>> packResultMapList = new ArrayList<>();
                List<Map<String, Object>> accNbrMapList = new ArrayList<>();
                JSONArray accArray = new JSONArray();

                if (hasAsset) {
                    //资产级
                    Map<String, String> privateParams = new HashMap<>();
                    privateParams.put("isCust", "1"); //资产级
                    privateParams.put("accNbr", map.get("accNbr"));
                    privateParams.put("integrationId", map.get("integrationId"));// 资产集成编码
                    privateParams.put("custId", map.get("custId"));

                    DefaultContext<String, Object> reultMap = new DefaultContext<>();
                    Map<String, Object> assetLabelMap = getAssetAndPromLabel(mktAllLabel, map, privateParams, context, esJson, labelItems);
                    if (assetLabelMap != null) {
                        reultMap.putAll(assetLabelMap);
                    }
                    resultMapList.add(reultMap);
                    //资产级查询客户id替换
                    custId = reultMap.get("CCUST_ID")==null ? custId : reultMap.get("CCUST_ID").toString();
                }
                // 是客户级的
                if (hasCust) {
                    if (custId == null || "".equals(custId)) {
                        //保存es log
                        long cost = System.currentTimeMillis() - begin;
                        esJson.put("timeCost", cost);
                        esJson.put("hit", false);
                        esJson.put("success", true);
                        esJson.put("msg", "客户级活动，事件采集项未包含客户编码");
                        esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));
                        log.error("采集项未包含客户编码:" + map.get("reqId"));
                    }
                    ExecutorService executor = Executors.newCachedThreadPool();
                    JSONObject param = new JSONObject();
                    //查询标识
                    param.put("c3", map.get("lanId"));
                    param.put("queryId", map.get("custId"));
                    param.put("queryNum", "");
                    param.put("queryFields", "");
                    param.put("type", "4");
                    param.put("centerType", "00");
                    try {
                        Map<String, Object> dubboResult = yzServ.queryYz(JSON.toJSONString(param));
                        if ("0".equals(dubboResult.get("result_code").toString())) {
                            accArray = new JSONArray((List<Object>) dubboResult.get("msgbody"));
                            log.info("【客户级查询资产列表返回】："+JSON.toJSONString(accArray));
                        }

                        // 查询客户级的标签
                        Map<String, String> privateParams = new HashMap<>();
                        privateParams.put("isCust", "1");
                        privateParams.put("accNbr", map.get("accNbr"));
                        privateParams.put("integrationId", map.get("integrationId"));
                        privateParams.put("custId", map.get("custId"));

                        // 客户级
                        List<Future<DefaultContext<String, Object>>> futureList = new ArrayList<>();
                        //多线程获取资产级标签，并加上客户级标签
                        for (Object o : accArray) {
                            Future<DefaultContext<String, Object>> future = executor.submit(new getListMapLabelTask(o, mktAllLabel, map, context, esJson, labelItems));
                            futureList.add(future);
                        }
                        for (Future<DefaultContext<String, Object>> future : futureList) {
                            if (future.get() != null && !future.get().isEmpty()) {
                                DefaultContext<String, Object> reultMap = future.get();
                                custResultMapList.add(reultMap);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Exception = " + e);
                    } finally {
                        executor.shutdown();
                    }
                }
                if (hasPackage) {
                    accNbrMapList = getAccNbrList(map.get("accNbr"));
                    ExecutorService executor = Executors.newCachedThreadPool();
                    // 查询标签
                    try {
                        // 客户级
                        List<Future<DefaultContext<String, Object>>> futureList = new ArrayList<>();
                        //多线程获取资产级标签，并加上客户级标签
                        for (Object o : accNbrMapList) {
                            Future<DefaultContext<String, Object>> future = executor.submit(new getListMapLabelTask(o, mktAllLabel, map, context, esJson, labelItems));
                            futureList.add(future);
                        }
                        for (Future<DefaultContext<String, Object>> future : futureList) {
                            if (future.get() != null && !future.get().isEmpty()) {
                                DefaultContext<String, Object> reultMap = future.get();
                                packResultMapList.add(reultMap);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        executor.shutdown();
                    }
                }


                /*
                 * 满意度调查事件（受理）
                 * 1、先根据资产查大数据标签判断是否绑定公众号，绑定则推送微厅，推送账号为业务号码
                 * 2、如果资产未绑定公众号，则判断联系号码是否绑定公众号，推送微厅，推送账号为联系号码
                 * 3、如果上面2者都未绑定微厅，判断采集项：资产类型是否为移动电话，是则推送短厅，推送账号为业务号码
                 * 4、如果采集项：资产类型不是移动电话，则判断联系电话是否为本网移动电话，是则推送短厅，推送账号为联系电话
                 * 5、都不满足暂不推
                 *
                 * */
                if ("EVTS000001120".equals(eventCode)) {
                    DefaultContext<String, Object> reultMap = resultMapList.get(0);
                    // 判断是否添加是否为微厅的标签
                    Map<String, Object> followFlgRedis = eventRedisService.getRedis("FOLLOW_FLG");
                    String followFlgType = (String) followFlgRedis.get("FOLLOW_FLG");
                    String isBind = (String) reultMap.get(followFlgType);
                    // 联系号码-事件采集项
                    String contactNumberStr = (String) evtParams.get("CPCP_ORDER_PHONE");
                    String contactNumber = "";
                    log.info("contactNumberStr_1120 --->>>" + contactNumberStr);
                    if (contactNumberStr != null) {
                        String[] split = contactNumberStr.split(",");
                        contactNumber = split[0];
                        log.info("contactNumber_1120 --->>>" + contactNumber);
                    }
                    // 1为绑定公众号，0 为不绑定公众号
                    log.info("111---isBind --->" + isBind);
                    if ("1".equals(isBind)) {
                        reultMap.put("CPCP_PUSH_CHANNEL", "1"); // 1-微厅, 2-短厅, 3-IVR
                        reultMap.put("CPCP_ACCS_NBR", map.get("accNbr"));
                    } else {
                        // 若资产号码为绑定微厅，查看联系号码是否绑定微厅
                        Map<String, Object> telMap = new HashMap<>();
                        telMap.put("tel", contactNumber);
                        log.info("esServiceInfo.getAssetByTelFourYN入参 = " + JSON.toJSONString(telMap));
                        Map<String, Object> assetByTelFourYN = esServiceInfo.getAssetByTelFourYN(telMap);
                        log.info("结果assetByTelFourYN = " + JSON.toJSONString(assetByTelFourYN));
                        if (assetByTelFourYN != null && "200".equals(assetByTelFourYN.get("resultCode")) && "true".equals(assetByTelFourYN.get("msg"))) {
                            reultMap.put("CPCP_PUSH_CHANNEL", "1"); // 1-微厅, 2-短厅, 3-IVR
                            reultMap.put("CPCP_ACCS_NBR", contactNumber);
                        } else {
                            // 判断采集项，资产类型是否为移动电话，是则推送短厅，推送账号为业务号码
                            if ("PHY-MAN-0022".equals(evtParams.get("CPCP_PRODUCT_TYPE"))) {
                                reultMap.put("CPCP_PUSH_CHANNEL", "2"); // 1-微厅, 2-短厅, 3-IVR
                                reultMap.put("CPCP_ACCS_NBR", map.get("accNbr"));
                            } else {
                                // 资产类型不是移动电话，则判断联系电话是否为本网移动电话，是则推送短厅，推送账号为联系电话
                                boolean isMobile = false;
                                if (contactNumber != null) {
                                    // 判断是否为手机号码
                                    isMobile = isMobile(contactNumber);
                                }
                                log.info("222---isMobile --->" + isMobile);
                                if (isMobile) {
                                    CacheResultObject<Set<String>> prodInstIdResult = iCacheProdIndexQryService.qryProdInstIndex3(contactNumber, "100000");
                                    log.info("333---prodInstIdResult --->" + JSON.toJSONString(prodInstIdResult));
                                    if (prodInstIdResult != null && prodInstIdResult.getResultObject() != null && prodInstIdResult.getResultObject().size() > 0) {
                                        Set<String> prodInstIds = prodInstIdResult.getResultObject();
                                        log.info("444---prodInstIds --->" + JSON.toJSONString(prodInstIds));
                                        for (String prodInstId : prodInstIds) {
                                            CacheResultObject<ProdInst> prodInstCacheEntity = iCacheProdEntityQryService.getProdInstCacheEntity(prodInstId);
                                            log.info("555---prodInstCacheEntity --->" + JSON.toJSONString(prodInstCacheEntity));
                                            if (prodInstCacheEntity != null && prodInstCacheEntity.getResultObject() != null) {
                                                ProdInst prodInst = prodInstCacheEntity.getResultObject();
                                                log.info("666---prodInst --->" + JSON.toJSONString(prodInst));
                                                if (prodInst != null && prodInst.getProdId() == 1429695L) {
                                                    log.info("777---" + isMobile + " 为C网用户");
                                                    reultMap.put("CPCP_PUSH_CHANNEL", "2"); // 1-微厅, 2-短厅, 3-IVR
                                                    reultMap.put("CPCP_ACCS_NBR", contactNumber);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    log.info("555---reultMap --->" + reultMap);
                    resultMapList.clear();
                    resultMapList.add(reultMap);
                }


                /*
                 * 满意度调查事件（装维）
                 * EVTS000001147	9666政企测评
                 * EVTS000001148	家庭内网检测测评
                 * 1、先判断测评业务号码是否绑定“浙江电信”微信公众号，绑定则推送微厅，推送账号为业务号码
                 * 2、非绑定用户再根据联系电话判断是否有绑定，有则推联系号码，
                 * 3、没有则通过联系电话推送到IVR
                 *
                 * */
                if ("EVTS000001121".equals(eventCode) || "EVTS000001147".equals(eventCode) || "EVTS000001148".equals(eventCode)) {
                    DefaultContext<String, Object> reultMap = resultMapList.get(0);
                    // 判断是否添加是否为微厅的标签
                    Map<String, Object> followFlgRedis = eventRedisService.getRedis("FOLLOW_FLG");
                    String followFlgType = (String) followFlgRedis.get("FOLLOW_FLG");
                    String isBind = (String) reultMap.get(followFlgType);
                    // 联系号码-事件采集项
                    String contactNumberStr = (String) evtParams.get("CPCP_ORDER_PHONE");
                    String contactNumber = "";
                    log.info("contactNumberStr_1121 --->>>" + contactNumberStr);
                    if (contactNumberStr != null) {
                        String[] split = contactNumberStr.split(",");
                        contactNumber = split[0];
                        log.info("contactNumber_1121 --->>>" + contactNumber);
                    }
                    // 1为绑定公众号，0 为不绑定公众号
                    log.info("111---isBind --->" + isBind);
                    if ("1".equals(isBind)) {
                        reultMap.put("CPCP_PUSH_CHANNEL", "1"); // 1-微厅, 2-短厅, 3-IVR
                        reultMap.put("CPCP_ACCS_NBR", map.get("accNbr"));
                    } else {
                        // 若资产号码为绑定微厅，查看联系号码是否绑定微厅
                        Map<String, Object> telMap = new HashMap<>();
                        telMap.put("tel", contactNumber);
                        log.info("esServiceInfo.getAssetByTelFourYN入参 = " + JSON.toJSONString(telMap));
                        Map<String, Object> assetByTelFourYN = esServiceInfo.getAssetByTelFourYN(telMap);
                        log.info("结果assetByTelFourYN = " + JSON.toJSONString(assetByTelFourYN));
                        if (assetByTelFourYN != null && "200".equals(assetByTelFourYN.get("resultCode")) && "true".equals(assetByTelFourYN.get("msg"))) {
                            reultMap.put("CPCP_PUSH_CHANNEL", "1"); // 1-微厅, 2-短厅, 3-IVR
                            reultMap.put("CPCP_ACCS_NBR", contactNumber);
                        } else{
                            reultMap.put("CPCP_PUSH_CHANNEL", "3"); // 1-微厅, 2-短厅, 3-IVR
                            reultMap.put("CPCP_ACCS_NBR", contactNumber);
                        }
                    }
                    log.info("222---reultMap --->" + reultMap);
                    resultMapList.clear();
                    resultMapList.add(reultMap);
                }
                /**
                 * 电渠线上测评, 判断是否有业务号码，如果有则推送业务号码，如果没有，则推送联系号码
                 */
                if ("EVTS000001146".equals(eventCode)) {
                    DefaultContext<String, Object> reultMap = resultMapList.get(0);
                    // 判断是否添加是否为微厅的标签
                    Map<String, Object> followFlgRedis = eventRedisService.getRedis("FOLLOW_FLG");
                    String followFlgType = (String) followFlgRedis.get("FOLLOW_FLG");
                    String isBind = (String) reultMap.get(followFlgType);
                    // 联系号码-事件采集项
                    String contactNumberStr = (String) evtParams.get("CPCP_ORDER_PHONE");
                    String contactNumber = "";
                    log.info("contactNumberStr_1121 --->>>" + contactNumberStr);
                    if (contactNumberStr != null) {
                        String[] split = contactNumberStr.split(",");
                        contactNumber = split[0];
                        log.info("contactNumber_1121 --->>>" + contactNumber);
                    }
                    // 1为绑定公众号，0 为不绑定公众号
                    log.info("111---isBind --->" + isBind);
                    if ("1".equals(isBind)) {
                        reultMap.put("CPCP_PUSH_CHANNEL", "1"); // 1-微厅
                        reultMap.put("CPCP_ACCS_NBR", map.get("accNbr"));
                    } else {
                        // 若资产号码为绑定微厅，查看联系号码是否绑定微厅
                        Map<String, Object> telMap = new HashMap<>();
                        telMap.put("tel", contactNumber);
                        log.info("esServiceInfo.getAssetByTelFourYN入参 = " + JSON.toJSONString(telMap));
                        Map<String, Object> assetByTelFourYN = esServiceInfo.getAssetByTelFourYN(telMap);
                        log.info("结果assetByTelFourYN = " + JSON.toJSONString(assetByTelFourYN));
                        if (assetByTelFourYN != null && "200".equals(assetByTelFourYN.get("resultCode")) && "true".equals(assetByTelFourYN.get("msg"))) {
                            reultMap.put("CPCP_PUSH_CHANNEL", "1"); // 1-微厅
                            reultMap.put("CPCP_ACCS_NBR", contactNumber);
                        }
                    }
                    log.info("222---reultMap --->" + reultMap);
                    resultMapList.clear();
                    resultMapList.add(reultMap);
                }

                /**
                 * 特殊事件处理
                 * 101：扫码下单  102：电话到家  105：政企商城  107：9666协同单
                 */
                specailEventDeal(map, evtContent, eventCode, resultMapList);

                List<String>  regionList = new ArrayList<>();
                Map<String, Object> mktCamCodeListRedis = eventRedisService.getRedis("REGION_LIST_EVENT");
                if (mktCamCodeListRedis != null) {
                    regionList = (List<String>) mktCamCodeListRedis.get("REGION_LIST_EVENT");
                }
                //103事件改造
                if ("EVT0000000103".equals(eventCode)) {
                    if (regionList.contains(map.get("lanId"))){
                        String c4Name = "";
                        String addressDesc = "";
                        CacheResultObject<ProdInst> prodInstCacheEntity = getProdInstCacheEntity(map.get("accNbr"));
                        if (prodInstCacheEntity != null) {
                            addressDesc = prodInstCacheEntity.getResultObject().getAddressDesc();
                            //获取c4
                            Long commonRegionId = prodInstCacheEntity.getResultObject().getRegionId();
                            CommonRegion commonRegion = commonRegionMapper.selectByPrimaryKey(commonRegionId);
                            if (commonRegion != null) {
                                Long c4RegionId = commonRegion.getC4RegionId();
                                if (c4RegionId != null) {
                                    commonRegion = commonRegionMapper.selectByPrimaryKey(c4RegionId);
                                    if (commonRegion != null) {
                                        c4Name = commonRegion.getRegionName();
                                    }
                                } else {
                                    c4Name = commonRegion.getRegionName();
                                }
                                //如果字段为空，那么这个区域本身就是C4，如果不为空则取该字段值的区域名称为C4。
                            }
                        }
                        evtContent.put("addressDesc",addressDesc);
                        evtContent.put("c4Name",c4Name);
                        evtContent.put("400600000016",addressDesc);
                        evtContent.put("400600000014",c4Name);
                        log.info("addressDesc" +addressDesc);
                        log.info("c4Name" + c4Name);

                        Map<String, Object> onlineMap = camCpcSpecialLogic.onlineScanCodeOrCallPhone4Home(evtContent, eventCode, map.get("lanId"));
                        log.info("onlineMap" + onlineMap);

                        log.info("onlineScanCodeOrCallPhone4Home -->>>onlineMap: " + JSON.toJSONString(onlineMap));
                        DefaultContext<String, Object> reultMap = resultMapList.get(0);
                        evtContent.put("400600000040", "");
                        evtContent.put("400600000041", "");
                        if (onlineMap.get("wgbm")!=null){
                            String wgbm = (String) onlineMap.get("wgbm");
                            Map<String, Object> c3AndC4Map = orgGridRelMapper.getC3AndC4(wgbm);
                            if (c3AndC4Map!=null && c3AndC4Map.get("c3")!=null && c3AndC4Map.get("c4")!=null){
                                String c3Str = (String) c3AndC4Map.get("c3");
                                String c4Str = (String) c3AndC4Map.get("c4");
                                evtContent.put("400600000040", c3Str);
                                evtContent.put("400600000041", c4Str);
                            }
                        }
                        reultMap.put("CPCP_ACCS_NBR", onlineMap.get("tel"));
                        resultMapList.clear();
                        resultMapList.add(reultMap);
                    }else {
                        boolean isCommLvl5 = false;
                        boolean isCommLvl4 = false;
                        // 从4A组织ID
                        DefaultContext<String, Object> reultMap = resultMapList.get(0);
                        String commLvl5Id = (String) reultMap.get("COMM_LVL5_ID");
                        String commLvl4Id = (String) reultMap.get("COMM_LVL4_ID");
                        String commLvl3Id = (String) reultMap.get("COMM_LVL3_ID");
                        evtContent.put("400600000040", "");
                        if (commLvl3Id != null) {
                            Long lvl3OrgId = organizationMapper.getByOrgid4a(Long.valueOf(commLvl3Id));
                            if (lvl3OrgId != null) {
                                evtContent.put("400600000040", lvl3OrgId.toString());
                            }
                        }
                        evtContent.put("400600000041", "");
                        if (commLvl4Id != null) {
                            Long lvl4OrgId = organizationMapper.getByOrgid4a(Long.valueOf(commLvl4Id));
                            if (lvl4OrgId != null) {
                                evtContent.put("400600000041", lvl4OrgId.toString());
                            }
                        }
                        log.info("4-获取到COMM_LVL5_ID标签的值为：" + commLvl5Id);
                        if (commLvl5Id != null) {
                            isCommLvl5 = isCommLvl(isCommLvl5, reultMap, commLvl5Id);
                        }
                        //
                        if (!isCommLvl5) {
                            // 从3A组织ID
                            log.info("3-获取到COMM_LVL4_ID标签的值为：" + commLvl4Id);
                            if (commLvl4Id != null) {
                                isCommLvl4 = isCommLvl(isCommLvl4, reultMap, commLvl4Id);
                            }
                        }
                        //如果lv4\lv5都没有 取lv4下面所有的lv5 遍历取经理编码
                        if (!isCommLvl4 && !isCommLvl5) {
                            Long orgId = organizationMapper.getByOrgid4a(Long.valueOf(commLvl4Id));
                            if (orgId != null) {
                                List<Organization> orgNameC5List = organizationMapper.getByOrgNameC5(orgId.toString());
                                for (Organization c5 : orgNameC5List) {
                                    isCommLvl4 = isCommLvl4(isCommLvl4, reultMap, c5.getOrgId());
                                }
                            }
                        }
                        if (!isCommLvl4 && !isCommLvl5) {
                            reultMap.put("CPCP_ACCS_NBR", "lv5:"+commLvl5Id+ " lv4:"+commLvl4Id);
                        }
                        log.info("reultMap的值为：" + JSON.toJSONString(reultMap));
                    }
                }

                //套餐生效套餐变更事件，畅享套餐变更生效事件
                List<String>  offerEventList = new ArrayList<>();
                Map<String, Object> offerEventRedis = eventRedisService.getRedis("OFFER_EVENT_LIST");
                if (offerEventRedis != null) {
                    offerEventList = (List<String>) offerEventRedis.get("OFFER_EVENT_LIST");
                }
                if (offerEventList.contains(eventCode)){
//                if("EVTS000001138".equals(eventCode) || "EVTS000001139".equals(eventCode) || "EVTS000001140".equals(eventCode) || "EVTS000001142".equals(eventCode) || "EVTS000001143".equals(eventCode)){
                    log.info("接入事件： "+ eventCode);
                    DefaultContext<String, Object> reultMap = resultMapList.get(0);
                    String offerNbr = (String)evtContent.get("CPCP_PROM_DIR_NBR");
                    log.info("销售品编码"+ offerNbr);
                    List<OfferExpenseDO> offerExpenseDO = commonRegionMapper.getExpenseByOfferNbr(offerNbr);
                    String offerName = ""; //套餐名称
                    Long amount = 0L; //套餐费用
                    Long CPCP_TOTAL_FLOW = 0L; //套内流量
                    Long CPCP_TOTAL_TIME = 0L; //总时长（分钟）
                    for(OfferExpenseDO offerExpe: offerExpenseDO){
                        if(offerExpe.getTemplateInstName().contains("语音")){
                            CPCP_TOTAL_TIME = offerExpe.getParamValue();
                        }
                        if(offerExpe.getTemplateInstName().contains("流量")){
                            CPCP_TOTAL_FLOW = offerExpe.getParamValue();
                        }
                        offerName = offerExpe.getTemplateInstName();
                        amount = offerExpe.getAmount();
                    }
                    //套餐生效套餐变更事件，畅享套餐变更生效事件
                    if (offerExpenseDO.isEmpty()){
                        List<OfferExpenseDO> offerInfoList = commonRegionMapper.getExpenseByOfferInfo(offerNbr);
                        if (!offerInfoList.isEmpty()){
                            offerName = offerInfoList.get(0).getTemplateInstName();
                            amount = offerInfoList.get(0).getAmount();
                        }
                    }
                    reultMap.put("CPCP_VIR_SUB_NAME", offerName);
                    reultMap.put("CPCP_EXPENSES", amount.toString());
                    reultMap.put("CPCP_TOTAL_FLOW", CPCP_TOTAL_FLOW.toString());
                    reultMap.put("CPCP_TOTAL_TIME", CPCP_TOTAL_TIME.toString());
                    resultMapList.clear();
                    resultMapList.add(reultMap);
                    log.info("resultMapList" + resultMapList);

                }

                List<String>  offerEventList2 = new ArrayList<>();
                Map<String, Object> oel2 = eventRedisService.getRedis("OFFER_EVENT_LIST_TWO");
                if (oel2 != null) {
                    offerEventList2 = (List<String>) oel2.get("OFFER_EVENT_LIST_TWO");
                }
                if (offerEventList2.contains(eventCode)){
                    log.info("接入事件： "+ eventCode);
                    DefaultContext<String, Object> reultMap = resultMapList.get(0);
                    String offerNbr = (String)evtContent.get("CPCP_PROM_DIR_NBR");
                    log.info("销售品编码"+ offerNbr);
                    String offerName = ""; //套餐名称
                    Long amount = 0L; //套餐费用
                    List<OfferExpenseDO> offerInfoList = commonRegionMapper.getExpenseByOfferInfo(offerNbr);
                    if (!offerInfoList.isEmpty()) {
                        offerName = offerInfoList.get(0).getTemplateInstName();
                        amount = offerInfoList.get(0).getAmount();
                    }
                    reultMap.put("CPCP_VIR_SUB_NAME", offerName);
                    reultMap.put("CPCP_EXPENSES", amount.toString());
                    resultMapList.clear();
                    resultMapList.add(reultMap);
                    log.info("resultMapList" + resultMapList);
                }

                //5G套餐办理变更事件
                if("EVTS000001141".equals(eventCode)){
                    DefaultContext<String, Object> reultMap = resultMapList.get(0);
                    String offerNbr = (String)evtContent.get("CPCP_PROM_DIR_NBR");
                    log.info("接入事件： "+ eventCode);
                    List<OfferExpenseDO> offerExpenseDO = commonRegionMapper.getExpenseByOfferNbr(offerNbr);
                    log.info("销售品编码"+ offerNbr);
                    String offerName = ""; //套餐名称
                    Long amount = 0L; //套餐费用
                    Long CPCP_TOTAL_FLOW = 0L; //套内流量
                    Long CPCP_TOTAL_TIME = 0L; //总时长（分钟）
                    for(OfferExpenseDO offerExpe: offerExpenseDO){
                        if(offerExpe.getTemplateInstName().contains("语音")){
                            CPCP_TOTAL_TIME = offerExpe.getParamValue();
                        }
                        if(offerExpe.getTemplateInstName().contains("流量")){
                            CPCP_TOTAL_FLOW = offerExpe.getParamValue();
                        }
                        offerName = offerExpe.getTemplateInstName();
                        amount = offerExpe.getAmount();
                    }
                    reultMap.put("CPCP_VIR_SUB_NAME", offerName);
                    reultMap.put("CPCP_EXPENSES", amount.toString());
                    reultMap.put("CPCP_TOTAL_FLOW", CPCP_TOTAL_FLOW.toString());
                    reultMap.put("CPCP_TOTAL_TIME", CPCP_TOTAL_TIME.toString());
                    String phone = map.get("accNbr");
                    String areaId = AreaCodeEnum.getAreaNameByLanId(Long.parseLong(map.get("lanId")));
                    Map<String,Object> paswMap = channelService.getUamServicePswd(phone,areaId,custId);
                    String pasw =(String)paswMap.get("password");
                    reultMap.put("CPCP_SERVICE_PSWD", pasw);
                    resultMapList.clear();
                    resultMapList.add(reultMap);

                    log.info("resultMapList" + resultMapList);

                }


                //重新赋值一遍
                map.put("evtContent",JSON.toJSONString(evtContent));

                //遍历活动
                for (Map<String, Object> resultMap : resultByEvent) {
                    //提交线程
                    Map<String, Object> activeMap = (Map<String, Object>) resultMap.get("mktCampaignMap");
                    if (activeMap != null && !activeMap.isEmpty()) {
                        if ((Integer) activeMap.get("levelConfig") == 1) { //判断是客户级
                            //客户级
                            for (DefaultContext<String, Object> o : custResultMapList) {
                                //客户级下，循环资产级
                                Map<String, String> privateParams = new HashMap<>();
                                privateParams.put("isCust", "0"); //是客户级
                                privateParams.put("accNbr", o.get("accNbr").toString());
                                privateParams.put("integrationId", o.get("integrationId").toString());
                                privateParams.put("custId", map.get("custId"));
                                //活动优先级为空的时候默认0
                                privateParams.put("orderPriority", activeMap.get("campaignSeq") == null ? "0" : activeMap.get("campaignSeq").toString());

                                Long mktCampaginId = (Long) activeMap.get("mktCampaginId");
                                String type = (String) activeMap.get("type");
                                List<Map<String, Object>> strategyMapList = (List<Map<String, Object>>) activeMap.get("strategyMapList");
                                Future<Map<String, Object>> f = executorService.submit(new ActivityTask(map, mktCampaginId, type, privateParams, labelItems, evtTriggers, strategyMapList, o));
                                //将线程处理结果添加到结果集
                                threadList.add(f);
                            }

                        } else if ((Integer) activeMap.get("levelConfig") == 2) { // 判断是套餐级别
                            //套餐级
                            for (DefaultContext<String, Object> o : packResultMapList) {
                                for (Map<String, Object> accNbrMap : accNbrMapList) {
                                    if (o.get("integrationId").toString().equals(accNbrMap.get("ASSET_INTEG_ID"))) {
                                        //客户级下，循环资产级
                                        Map<String, String> privateParams = new HashMap<>();
                                        privateParams.put("isCust", "0"); //是客户级
                                        privateParams.put("accNbr", o.get("accNbr").toString());
                                        privateParams.put("integrationId", o.get("integrationId").toString());
                                        privateParams.put("custId", map.get("custId"));
                                        //活动优先级为空的时候默认0
                                        privateParams.put("orderPriority", activeMap.get("campaignSeq") == null ? "0" : activeMap.get("campaignSeq").toString());
                                        //线程参数
                                        Long mktCampaginId = (Long) activeMap.get("mktCampaginId");
                                        String type = (String) activeMap.get("type");
                                        List<Map<String, Object>> strategyMapList = (List<Map<String, Object>>) activeMap.get("strategyMapList");
                                        //线程参数
                                        Future<Map<String, Object>> f = executorService.submit(new ActivityTask(map, mktCampaginId, type, privateParams, labelItems, evtTriggers, strategyMapList, o));
                                        //将线程处理结果添加到结果集
                                        threadList.add(f);
                                    }
                                }
                            }
                        } else {
                            //资产级
                            for (DefaultContext<String, Object> o : resultMapList) {
                                String assetId = o.get("integrationId")==null ? "" : o.get("integrationId").toString() ;
                                // 判断资产编码是否与接入的一致
                                if (assetId.equals(map.get("integrationId")==null? "" : map.get("integrationId"))) {
                                    Map<String, String> privateParams = new HashMap<>();
                                    privateParams.put("isCust", "1"); //是否是客户级
                                    privateParams.put("accNbr", map.get("accNbr"));
                                    privateParams.put("integrationId", map.get("integrationId"));
                                    privateParams.put("custId", map.get("custId"));
                                    privateParams.put("orderPriority", activeMap.get("campaignSeq") == null ? "0" : activeMap.get("campaignSeq").toString());
                                    //线程参数
                                    Long mktCampaginId = (Long) activeMap.get("mktCampaginId");
                                    String type = (String) activeMap.get("type");
                                    List<Map<String, Object>> strategyMapList = (List<Map<String, Object>>) activeMap.get("strategyMapList");
                                    //线程参数
                                    //资产级
                                    Future<Map<String, Object>> f = executorService.submit(new ActivityTask(map, mktCampaginId, type, privateParams, labelItems, evtTriggers, strategyMapList, resultMapList.get(0)));
                                    //将线程处理结果添加到结果集
                                    threadList.add(f);
                                }
                            }
                        }
                    }
                }

                //获取结果
                try {
                    Map<String, Object> nonPassedMsg = new HashMap<>();
                    for (Future<Map<String, Object>> future : threadList) {
                        /*if (future.get() != null && !future.get().isEmpty()) {
                            activityList.addAll((List<Map<String, Object>>) (future.get().get("ruleList")));
                        }*/
                        Boolean flag = true;
                        if (future.get() != null && !future.get().isEmpty()) {
                            Map<String, Object> map1 = future.get();
                            for (String s : map1.keySet()) {
                                if (s.contains("cam_")) {
                                    flag = false;
                                    nonPassedMsg.put(s, map1.get(s));
                                }
                            }
                            if (map1.get("nonPassedMsg") != null) {
                                Object nonPassedMsg1 = map1.get("nonPassedMsg");
                                nonPassedMsg.putAll((Map<String, Object>) nonPassedMsg1);
                                // flag = false;
                            }
                            if (flag) {
                                // 命中活动
                                if (future.get() != null && future.get().get("ruleList") != null) {
                                    activityList.addAll((List<Map<String, Object>>) (future.get().get("ruleList")));
                                }
                            }
                        }
                        /*else {
                            // 翼支付未命中原因
                            nonPassedMsg.putAll(future.get());
                        }*/
                    }
                    result.put("nonPassedMsg", JSON.toJSONString(nonPassedMsg));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //发生异常关闭线程池
                    executorService.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    //发生异常关闭线程池
                    executorService.shutdown();
                    return Collections.EMPTY_MAP;
                } finally {
                    //关闭线程池
                    executorService.shutdown();
                }


                // 判断事件推荐活动数，按照优先级排序
                if (activityList.size() > 0 && recCampaignAmount > 0 && recCampaignAmount < activityList.size()) {
                    Collections.sort(activityList, new Comparator<Map<String, Object>>() {
                        public int compare(Map o1, Map o2) {
                            if (!o1.containsKey("orderPriority")) {
                                o1.put("orderPriority", "0");
                            }
                            if (!o2.containsKey("orderPriority")) {
                                o2.put("orderPriority", "0");
                            }
                            return Integer.parseInt((String) o2.get("orderPriority")) - Integer.parseInt((String) o1.get("orderPriority"));
                        }
                    });

                    String orderPriorityLast = (String) activityList.get(recCampaignAmount - 1).get("orderPriority");

                    for (int i = recCampaignAmount; i < activityList.size(); i++) {
                        if (!orderPriorityLast.equals(activityList.get(i).get("orderPriority"))) {
                            //es log
                            esJson.put("msg", "推荐数：" + i + ",命中数：" + activityList.size());
                            //事件推荐活动数
                            activityList = activityList.subList(0, i);
                        }
                    }
                }

                //返回结果
                result.put("taskList", activityList); //协同回调结果

                if (activityList.size() > 0) {
                    //构造返回参数
                    result.put("CPCResultCode", "1");
                    result.put("CPCResultMsg", "success");

                    StringBuilder actStr = new StringBuilder();
                    for (Map<String, Object> actMap : activityList) {
                        actStr.append(actMap.get("activityId")).append(",");
                    }
                    esJson.put("hit", true);
                    esJson.put("hitDetail", actStr.toString());

                } else {
                    result.put("CPCResultCode", "1000");
                    result.put("CPCResultMsg", "success");

                    esJson.put("hit", false);

                }
                result.put("reqId", map.get("reqId"));
                result.put("custId", custId);

                paramsJson.put("backParams", result);
                esHitService.save(paramsJson, IndexList.PARAMS_MODULE, map.get("reqId"));

                //es log
                long cost = System.currentTimeMillis() - begin;
                esJson.put("timeCost", cost);
                esJson.put("success", true);
                esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));

            } catch (Exception e) {
                log.info("策略中心计算异常");
                log.error("Exception = ", e);
                esJson.put("errorMsg", e.getMessage());
                esHitService.save(paramsJson, IndexList.PARAMS_MODULE, map.get("reqId"));

                long cost = System.currentTimeMillis() - begin;
                esJson.put("timeCost", cost);
                esJson.put("hit", false);
                esJson.put("success", true);
                esJson.put("msg", "策略中心计算异常" + e.getMessage() + e.toString());
                esHitService.save(esJson, IndexList.EVENT_MODULE, map.get("reqId"));

                //构造返回参数
                result.put("CPCResultCode", "1000");
                result.put("CPCResultMsg", "策略中心计算异常" + e.getMessage() + e.toString());
                result.put("reqId", map.get("reqId"));
                result.put("custId", custId);
                return result;
            }
            log.info("事件计算流程结束:" + map.get("eventCode") + "***" + map.get("reqId") + "（" + (System.currentTimeMillis() - begin) + "）");
            return result;
        }

        /**
         * 101：扫码下单  102：电话到家  105：政企商城  107：9666协同单
         */
        private void specailEventDeal(Map<String, String> map, Map<String, Object> evtContent, String eventCode, List<DefaultContext<String, Object>> resultMapList) {
            if ("EVT0000000101".equals(eventCode) || "EVT0000000102".equals(eventCode)
                    || "EVT0000000105".equals(eventCode) || "EVT0000000107".equals(eventCode)
                    || "EVT0000000108".equals(eventCode)||"EVT0000000109".equals(eventCode) ) {
                DefaultContext<String, Object> reultMap = resultMapList.get(0);
                evtContent.put("400600000040", "");
                evtContent.put("400600000041", "");
                String lv3 = "";
                String lv4 = "";
                String staffCode = "";
                if ("EVT0000000107".equals(eventCode)){
                    String commLvl4Id = (String) reultMap.get("COMM_LVL4_ID");
                    String commLvl3Id = (String) reultMap.get("COMM_LVL3_ID");
                    if (commLvl3Id != null) {
                        Long lvl3OrgId = organizationMapper.getByOrgid4a(Long.valueOf(commLvl3Id));
                        if (lvl3OrgId != null) {
                            lv3 = lvl3OrgId.toString();
                            evtContent.put("400600000040", lvl3OrgId.toString());
                        }
                    }
                    if (commLvl4Id != null) {
                        Long lvl4OrgId = organizationMapper.getByOrgid4a(Long.valueOf(commLvl4Id));
                        if (lvl4OrgId != null) {
                            lv4 = lvl4OrgId.toString();
                            evtContent.put("400600000041", lvl4OrgId.toString());
                        }
                    }
                    if (!lv4.equals("")){
                        String orgPath = orgGridRelMapper.getOrgPathByOrgId(lv4);
                        if (orgPath!=null && !"".equals(orgPath)){
                            List<String> list = orgGridRelMapper.getStaffByOrgPath(orgPath);
                            if (!list.isEmpty()){
                                staffCode = list.get(0);
                            }
                        }
                    }
                    if ("".equals(staffCode) && !lv3.equals("")){
                        String orgPath = orgGridRelMapper.getOrgPathByOrgId(lv3);
                        if (orgPath!=null && !"".equals(orgPath)){
                            List<String> list = orgGridRelMapper.getStaffByOrgPath(orgPath);
                            if (!list.isEmpty()){
                                staffCode = list.get(0);
                            }
                        }
                    }
                    reultMap.put("CPCP_ACCS_NBR", staffCode);
                }
                if ("".equals(staffCode)){
                    Map<String, Object> onlineMap = camCpcSpecialLogic.onlineScanCodeOrCallPhone4Home(evtContent, eventCode, map.get("lanId"));
                    log.info("onlineScanCodeOrCallPhone4Home -->>>onlineMap: " + JSON.toJSONString(onlineMap));
                    if (onlineMap.get("wgbm")!=null){
                        String wgbm = (String) onlineMap.get("wgbm");
                        Map<String, Object> c3AndC4Map = orgGridRelMapper.getC3AndC4(wgbm);
                        if (c3AndC4Map!=null && c3AndC4Map.get("c3")!=null && c3AndC4Map.get("c4")!=null){
                            String c3Str = (String) c3AndC4Map.get("c3");
                            String c4Str = (String) c3AndC4Map.get("c4");
                            evtContent.put("400600000040", c3Str);
                            evtContent.put("400600000041", c4Str);
                        }
                    }
                    Object flg = evtContent.get("400600000052");
                    if ("EVT0000000105".equals(eventCode)|| "EVT0000000108".equals(eventCode)||"EVT0000000109".equals(eventCode)|| (flg!=null && "1".equals(flg.toString()))){
                        if (onlineMap.get("wgbm")==null){
                            staffCode = "GIS网格编码查询为空";
                        }else {
                            String wgbm = (String) onlineMap.get("wgbm");
                            log.info("【商企专员查询】------》wgbm："+wgbm);
                            Map<String, Object> mapRes = orgGridRelMapper.getC3AndC4(wgbm);
                            if (mapRes!=null && mapRes.get("c4")!=null){
                                log.info("【商企专员查询】------》C5 mapRes："+JSON.toJSONString(mapRes));
                                String orgPath = orgGridRelMapper.getOrgPathByOrgId(mapRes.get("c4").toString());
                                if (orgPath!=null && !"".equals(orgPath)){
                                    List<String> list = orgGridRelMapper.getStaffByOrgPath(orgPath);
                                    if (!list.isEmpty()){
                                        staffCode = list.get(0);
                                    }
                                }
                            }
                            if ("".equals(staffCode)&& mapRes.get("c3")!=null){
                                log.info("【商企专员查询】------》C4 mapRes："+JSON.toJSONString(mapRes));
                                String orgPath = orgGridRelMapper.getOrgPathByOrgId(mapRes.get("c3").toString());
                                if (orgPath!=null && !"".equals(orgPath)){
                                    List<String> list = orgGridRelMapper.getStaffByOrgPath(orgPath);
                                    if (!list.isEmpty()){
                                        staffCode = list.get(0);
                                    }else {
                                        staffCode = "未查询到有效的接单人信息";
                                    }
                                }else {
                                    staffCode = "未查询到有效的接单人信息";
                                }
                            }
                        }
                        log.info("【商企专员查询】------》staffCode："+staffCode);
                        reultMap.put("CPCP_PUSH_NBR", staffCode);
                    }else {
                        reultMap.put("CPCP_ACCS_NBR", onlineMap.get("tel").toString());
                    }
                }
                resultMapList.clear();
                resultMapList.add(reultMap);
            }
        }
    }

    private boolean isCommLvl(boolean isCommLvl4, DefaultContext<String, Object> reultMap, String commLvl4Id) {
        Long orgId = organizationMapper.getByOrgid4a(Long.valueOf(commLvl4Id));
        if (orgId != null) {
            isCommLvl4 = isCommLvl4(isCommLvl4, reultMap, orgId);
        }
        return isCommLvl4;
    }

    private boolean isCommLvl4(boolean isCommLvl4, DefaultContext<String, Object> reultMap, Long orgId2) {
        List<Map<String, Object>> staffIdAndTypeMapList = organizationMapper.getStaffIdAndType(orgId2);
        if (staffIdAndTypeMapList != null) {
            for (Map<String, Object> staffIdAndTypeMap : staffIdAndTypeMapList) {
                if (staffIdAndTypeMap.get("staffId") != null) {
                    Long staffId = (Long) staffIdAndTypeMap.get("staffId");
                    int count = organizationMapper.getCount(staffId);
                    if (count > 0) {
                        if (staffIdAndTypeMap.get("staffCode") != null) {
                            reultMap.put("CPCP_ACCS_NBR", staffIdAndTypeMap.get("staffCode"));
                            isCommLvl4 = true;
                            break;
                        } else {
                        }
                    }
                }
            }
        }
        return isCommLvl4;
    }


    /**
     * 过滤事件采集项中的标签
     *
     * @param labelItems   事件采集项标签集合
     * @param mktAllLabels 事件下所有的标签
     * @return
     */
    private Map<String, String> filterLabel(String  eventCode, Map<String, String> labelItems, Map<String, String> mktAllLabels) {
        Map<String, String> mktAllLabel = new HashMap<>();
        Iterator<Map.Entry<String, String>> iterator = labelItems.entrySet().iterator();
        List<String> assetLabelList = new ArrayList<>();
        List<String> promLabelList = new ArrayList<>();
        List<String> custLabelList = new ArrayList<>();
        if (mktAllLabels.get("assetLabels") != null && !"".equals(mktAllLabels.get("assetLabels"))) {
            assetLabelList = ChannelUtil.StringToList(mktAllLabels.get("assetLabels"));
            // ASSI_PROM_INTEG_ID标签
            assetLabelList.add("ASSI_PROM_INTEG_ID");
        }
        if (mktAllLabels.get("promLabels") != null && !"".equals(mktAllLabels.get("promLabels"))) {
            promLabelList = ChannelUtil.StringToList(mktAllLabels.get("promLabels"));
        }
        if (mktAllLabels.get("custLabels") != null && !"".equals(mktAllLabels.get("custLabels"))) {
            custLabelList = ChannelUtil.StringToList(mktAllLabels.get("custLabels"));
        }

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (assetLabelList.contains(entry.getKey())) {
                assetLabelList.remove(entry.getKey());
            } else if (promLabelList.contains(entry.getKey())) {
                promLabelList.remove(entry.getKey());
            } else if (custLabelList.contains(entry.getKey())) {
                custLabelList.remove(entry.getKey());
            }
        }

        List<String> labelList = new ArrayList<>();
        labelList.addAll(assetLabelList);
        labelList.addAll(promLabelList);
        labelList.addAll(custLabelList);
        // 添加落地网格AREA_ID标签
        labelList.add("AREA_ID");
        if (("EVT0000000103".equals(eventCode) ||"EVT0000000107".equals(eventCode))
                && assetLabelList.isEmpty()) {
            assetLabelList.add("COMM_LVL4_ID");
            assetLabelList.add("COMM_LVL5_ID");
            assetLabelList.add("COMM_LVL3_ID");
        }
        labelList.add("COMM_LVL4_ID");
        labelList.add("COMM_LVL5_ID");
        labelList.add("COMM_LVL3_ID");


        // 判断是否添加是否为微厅的标签
        Map<String, Object> followFlgRedis = eventRedisService.getRedis("FOLLOW_FLG");
        String followFlgType = (String) followFlgRedis.get("FOLLOW_FLG");
        labelList.add(followFlgType);

        if (assetLabelList.size() > 0) {
            mktAllLabel.put("assetLabels", ChannelUtil.StringList2String(labelList));
        }
        return mktAllLabel;
    }

    /*private List<Map<String,Object>> getBitslapByEvent(Long eventId, List<Map<String,Object>> resultByEvent) {
        // 遍历出被过滤掉的活动
        List<Map<String, Object>> mktCampaginIdList = mktCamEvtRelMapper.listActivityByEventId(eventId);
        for (Map<String, Object> passMap : resultByEvent) {
            Map<String,Object> map = passMap.get("mktCampaignMap") == null ? new HashMap<>() : (Map<String, Object>)passMap.get("mktCampaignMap");
            if (map != null && map.get("mktCampaginId") != null && map.get("mktCampaginId") != "") {
                String passCamId = map.get("mktCampaginId").toString();
                for (Map<String, Object> countMap : mktCampaginIdList) {
                    String countCamId = countMap.get("mktCampaginId").toString();
                    if (passCamId.equals(countCamId)) {
                        mktCampaginIdList.remove(countMap);
                        break;
                    }
                }
            }
        }
        // 遍历活动找出配有必中标签的活动
        boolean flag = true;
        if (!mktCampaginIdList.isEmpty()) {
            ListIterator it = mktCampaginIdList.listIterator();
            while (it.hasNext()) {
                flag = true;
                Map<String, Object> map = (Map<String, Object>) it.next();
                String mktCampaginId = map.get("mktCampaginId").toString();
                List<Map<String, String>> mapList = tarGrpConditionMapper.selectAllLabelByCamId(Long.valueOf(mktCampaginId));
                if (!mapList.isEmpty()) {
                    for (Map<String, String> ruleMap : mapList) {
                        String labelCode = ruleMap.get("labelCode");
                        String ruleId = ruleMap.get("ruleId");
                        if (labelCode.equals(defaultInfallibleTable)) {
                            flag = false;
                            map.put("camPass", false);
                            map.put("willBeInRuleId", ruleId);
                        }
                    }
                }
                if (flag) {
                    mktCampaginIdList.remove(map);
                }
            }
        }
        return mktCampaginIdList;
    }*/


    /**
     * 活动级别验证
     */
    class ActivityTask implements Callable<Map<String, Object>> {
        private Long activityId;
        private String type;
        private String reqId;
        private Map<String, String> params;
        private Map<String, String> privateParams;
        private Map<String, String> labelItems;
        private List<Map<String, Object>> evtTriggers;
        private List<Map<String, Object>> strategyMapList;
        private DefaultContext<String, Object> context;

        ActivityTask(Map<String, String> params, Long activityId, String type, Map<String, String> privateParams, Map<String, String> labelItems, List<Map<String, Object>> evtTriggers, List<Map<String, Object>> strategyMapList, DefaultContext<String, Object> context) {
            this.activityId = activityId;
            this.type = type;
            this.params = params;
            this.privateParams = privateParams;
            this.labelItems = labelItems;
            this.evtTriggers = evtTriggers;
            this.strategyMapList = strategyMapList;
            this.reqId = params.get("reqId");
            this.context = context;
        }

        @Override
        public Map<String, Object> call() {
            Map<String, Object> activityTaskResultMap = new HashMap<>();
//            try {
//                boolean isBlack = checkBlackList(privateParams.get("accNbr"), type);
//                if (isBlack) {
//                    activityTaskResultMap.put("cam_" + activityId,"【" + privateParams.get("accNbr") + "】该资产已被黑名单过滤");
//                    return activityTaskResultMap;
//                }
//            } catch (Exception e) {
//                log.error("【全局黑名单校验失败】");
//                e.printStackTrace();
//            }
            if (StatusCode.SERVICE_CAMPAIGN.getStatusCode().equals(type) || StatusCode.SERVICE_SALES_CAMPAIGN.getStatusCode().equals(type)) {
                log.info("服务活动进入camApiSerService.ActivitySerTask");
                activityTaskResultMap = camApiSerService.ActivitySerTask(params, activityId, privateParams, labelItems, evtTriggers, strategyMapList, context);
            } else if (StatusCode.MARKETING_CAMPAIGN.getStatusCode().equals(type)) {
                log.info("进入camApiService.ActivityTask");
                activityTaskResultMap = camApiService.ActivityTask(params, activityId, privateParams, labelItems, evtTriggers, strategyMapList, context);
            } else if (StatusCode.XIETONG_SCENE.getStatusCode().equals(type)) {
                log.info("进入协同场景服务camApiSerService.ActivityXieTongTask");
                activityTaskResultMap = camApiSerService.ActivityXieTongTask(params, activityId, privateParams, labelItems, evtTriggers, strategyMapList, context);
            }
            return activityTaskResultMap;
        }
    }


    private JSONObject getLabelByDubbo(JSONObject param) {
        //查询标签实例数据
        Map<String, Object> dubboResult = yzServ.queryYz(JSON.toJSONString(param));
        if ("0".equals(dubboResult.get("result_code").toString())) {
            JSONObject body = new JSONObject((HashMap) dubboResult.get("msgbody"));
            //解析返回结果
            return body;
        } else {
//            System.out.println("查询标签失败:" + httpResult.getString("result_msg"));
            return new JSONObject();
        }
    }


    private boolean compareHourAndMinute(FilterRule filterRule) {
        Boolean result = false;
        Calendar cal = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.setTime(filterRule.getDayStart());
        start.set(cal.get(Calendar.YEAR), cal.get(MONTH), cal.get(Calendar.DAY_OF_MONTH));
        Calendar end = Calendar.getInstance();
        end.setTime(filterRule.getDayEnd());
        end.set(cal.get(Calendar.YEAR), cal.get(MONTH), cal.get(Calendar.DAY_OF_MONTH));

        if ((cal.getTimeInMillis() - start.getTimeInMillis()) > 0 && (cal.getTimeInMillis() - end.getTimeInMillis()) < 0) {
            result = true;
        }

        return result;
    }

    private List<String> subScript(String str) {
        List<String> result = new ArrayList<>();
        Pattern p = Pattern.compile("(?<=\\$\\{)([^$]+)(?=\\}\\$)");
        Matcher m = p.matcher(str);
        while (m.find()) {
            result.add(m.group(1));
        }
        return result;
    }

    private Map<String, Object> getLabelValue(JSONObject param) {
        //更换为dubbo因子查询-----------------------------------------------------
        Map<String, Object> dubboResult = yzServ.queryYz(JSON.toJSONString(param));
        return dubboResult;
    }


    /**
     * 规则引擎过滤：字符串转数字 （用于大小区间比较）
     * <p>
     * 参数：待比较字段
     */
    class StringToNumOperator extends Operator {
        public StringToNumOperator(String name) {
            this.name = name;
        }

        public Object executeInner(Object[] list) throws Exception {
            String str = (String) list[0];
            if (NumberUtils.isNumber(str)) {
                return NumberUtils.toDouble(str);
            } else {
                return str;
            }
        }
    }

    private CacheResultObject<ProdInst> getProdInstCacheEntity(String accNbr) {
        CacheResultObject<ProdInst> prodInstCacheEntity = null;
        CacheResultObject<Set<String>> prodInstIdsObject = iCacheProdIndexQryService.qryProdInstIndex2(accNbr);
        log.info("22222------prodInstIdsObject --->" + JSON.toJSONString(prodInstIdsObject));
        if (prodInstIdsObject != null && prodInstIdsObject.getResultObject() != null) {
            Long mainOfferInstId = null;
            Set<String> prodInstIds = prodInstIdsObject.getResultObject();
            for (String prodInstId : prodInstIds) {
                CacheResultObject<ProdInst> entity = iCacheProdEntityQryService.getProdInstCacheEntity(prodInstId);
                log.info("555---prodInstCacheEntity --->" + JSON.toJSONString(entity));
                if (entity != null && entity.getResultObject() != null) {
                    ProdInst prodInst = entity.getResultObject();
                    log.info("666---prodInst --->" + JSON.toJSONString(prodInst));
                    //1429768   WIRED_NBR("INT-MAN-0010"),//宽带
                    //1429838    ITV_NBR("OTH-MAN-0034"),//itv
                    if (prodInst != null && (prodInst.getProdId() == 1429768L || prodInst.getProdId() == 1429838L)) {
                        prodInstCacheEntity = entity;
                    }
                }
            }
        }
        return  prodInstCacheEntity;
    }

    public static String cpcLabel(Label label, String type, String rightParam) {
        StringBuilder express = new StringBuilder();
        express.append("if(");

        if ("7100".equals(type)) {
            express.append("!");
        }
        express.append("(");
        express.append(assLabel(label, type, rightParam));
        express.append(") {return true}");

        return express.toString();
    }


    public static String assLabel(Label label, String type, String rightParam) {
        StringBuilder express = new StringBuilder();
        switch (type) {
            case "1000":
                express.append("toNum(").append(label.getInjectionLabelCode()).append("))");
                express.append(" > ");
                express.append(rightParam);
                break;
            case "2000":
                express.append("toNum(").append(label.getInjectionLabelCode()).append("))");
                express.append(" < ");
                express.append(rightParam);
                break;
            case "3000":
                express.append(label.getInjectionLabelCode()).append(")");
                express.append(" == ");
                if (NumberUtils.isNumber(rightParam)) {
                    express.append(rightParam);
                } else {
                    express.append("\"").append(rightParam).append("\"");
                }
                break;
            case "4000":
                express.append(label.getInjectionLabelCode()).append(")");
                express.append(" != ");
                if (NumberUtils.isNumber(rightParam)) {
                    express.append(rightParam);
                } else {
                    express.append("\"").append(rightParam).append("\"");
                }
                break;
            case "5000":
                express.append("toNum(").append(label.getInjectionLabelCode()).append("))");
                express.append(" >= ");
                express.append(rightParam);
                break;
            case "6000":
                express.append("toNum(").append(label.getInjectionLabelCode()).append("))");
                express.append(" <= ");
                express.append(rightParam);
                break;
            case "7100":
            case "7000":
                express.append(label.getInjectionLabelCode()).append(")");
                express.append(" in ");
                String[] strArray = rightParam.split(",");
                express.append("(");
                for (int j = 0; j < strArray.length; j++) {
                    express.append("\"").append(strArray[j]).append("\"");
                    if (j != strArray.length - 1) {
                        express.append(",");
                    }
                }
                express.append(")");
                break;
            case "7200":
                express.append("toNum(").append(label.getInjectionLabelCode()).append("))");
                String[] strArray2 = rightParam.split(",");
                express.append(" >= ").append(strArray2[0]);
                express.append(" && ").append("(toNum(");
                express.append(label.getInjectionLabelCode()).append("))");
                express.append(" <= ").append(strArray2[1]);

        }
        return express.toString();
    }


    /**
     * 判断事件规则条件是否满足  返回map的code值为success则满足条件 否则返回result错误信息
     *
     * @param eventId    事件id
     * @param labelItems 需要作为标签值的标签  即不需要去查询ES的标签
     * @param map        事件接入的信息
     * @return
     */
    public Map<String, Object> matchRulCondition(Long eventId, Map<String, String> labelItems, Map<String, String> map) {
//        log.info("开始验证事件规则条件");
        Map<String, Object> result = new HashMap<>();
        result.put("code", "success");
        //查询事件规则
        ContactEvtMatchRul evtMatchRul = new ContactEvtMatchRul();
        evtMatchRul.setContactEvtId(eventId);
        List<ContactEvtMatchRul> contactEvtMatchRuls = contactEvtMatchRulMapper.listEventMatchRuls(evtMatchRul);
        //事件规则为空不用判断,直接返回
        if (contactEvtMatchRuls.isEmpty()) {
//            log.info("事件规则为空直接返回");
            return result;
        }
        //查询事件规则条件
        List<EventMatchRulCondition> eventMatchRulConditions = new ArrayList<>();
        for (ContactEvtMatchRul c : contactEvtMatchRuls) {
            List<EventMatchRulCondition> list = eventMatchRulConditionMapper.listEventMatchRulCondition(c.getEvtMatchRulId());
            eventMatchRulConditions.addAll(list);
        }
        if (eventMatchRulConditions.isEmpty()) {
            return result;
        }
        //查询事件规则条件对应的标签
        List<Label> labelList = new ArrayList<>();
        for (EventMatchRulCondition condition : eventMatchRulConditions) {
            //!!先查redis 没有再查数据库
            Label label = (Label) redisUtils.get("MATCH_RUL_CONDITION" + condition.getLeftParam());
            if (label == null) {
                label = injectionLabelMapper.selectByPrimaryKey(Long.valueOf(condition.getLeftParam()));
                redisUtils.set("MATCH_RUL_CONDITION_" + condition.getLeftParam(), label);
            }
            labelList.add(label);
        }
        //判断那些标签需要查询ES
        List<Label> selectByEs = new ArrayList<>();   //需要查询ES获取标签值的标签集合
        List<Label> myLabelList = new ArrayList<>();  //使用事件接入的数据作为标签值的标签集合
        for (Label label : labelList) {
            if (labelItems.containsKey(label.getInjectionLabelCode())) {
                myLabelList.add(label);
            } else {
                selectByEs.add(label);
            }
        }

        //判断不需要查ES的标签是否符合规则引擎计算结果
        JSONObject eventItem = JSONObject.parseObject(map.get("evtContent"));
        for (Label label : myLabelList) {
            //添加到上下文
            DefaultContext<String, Object> context = new DefaultContext<String, Object>();
            context.put(label.getInjectionLabelCode(), eventItem.get(label.getInjectionLabelCode()));
            //事件命中规则信息
            EventMatchRulCondition condition = null;
            for (EventMatchRulCondition c : eventMatchRulConditions) {
                //得到标签对应的事件规则
                if (Long.valueOf(c.getLeftParam()).equals(label.getInjectionLabelId())) {
                    condition = c;
                    break;
                }
            }
            Map<String, String> stringStringMap = decideExpress(condition, label, context);
            //拼接规则引擎判断  有一个不满足则不满足
            if (!stringStringMap.get("code").equals("success")) {
                //判断返回为false直接结束命中
                result.put("result", stringStringMap.get("result"));
                result.put("code", "failed");
                return result;
            }
        }

        //有需要查询ES的标签
        if (!selectByEs.isEmpty()) {
            Map<String, Object> stringObjectMap = selectLabelByEs(selectByEs, map);
            if (stringObjectMap.get("result").equals("success")) {
                //查询成功
                JSONObject body = (JSONObject) stringObjectMap.get("body");
                //拼接规则引擎
                for (Label label : selectByEs) {
                    DefaultContext<String, Object> context = new DefaultContext<String, Object>();
                    //拼装参数
                    for (Map.Entry<String, Object> entry : body.entrySet()) {
                        if (entry.getKey().equals(label.getInjectionLabelCode())) {
                            context.put(entry.getKey(), entry.getValue());
                            log.info("规则计算标签：" + entry.getKey() + "  对应值：" + entry.getValue());
                            break;
                        }
                    }

                    //事件命中规则信息
                    EventMatchRulCondition condition = null;
                    for (EventMatchRulCondition c : eventMatchRulConditions) {
                        //得到标签对应的事件规则
                        if (Long.valueOf(c.getLeftParam()) == label.getInjectionLabelId()) {
                            condition = c;
                            break;
                        }
                    }

                    Map<String, String> stringStringMap = decideExpress(condition, label, context);
                    //拼接规则引擎判断  有一个不满足则不满足
                    if (!stringStringMap.get("code").equals("success")) {
                        //判断返回为false直接结束命中
                        result.put("result", stringStringMap.get("result"));
                        result.put("code", "failed");
                        return result;
                    }
                }
            } else {
                result.put("result", stringObjectMap.get("result"));
                result.put("code", "failed");
            }
        }
        return result;
    }


    /**
     * 查询标签因子实例数据
     *
     * @param selectByEs 需要ES查询的标签
     * @param map        事件接入的信息
     * @return
     */
    public Map<String, Object> selectLabelByEs(List<Label> selectByEs, Map<String, String> map) {
        Map<String, Object> dubboLabel = new HashMap<>();
        dubboLabel.put("result", "success");
        JSONObject param = new JSONObject();
        //查询标识
        param.put("queryNum", map.get("accNbr"));
        param.put("c3", map.get("lanId"));
        param.put("queryId", map.get("integrationId"));
        param.put("type", "1");
        StringBuilder queryFields = new StringBuilder();
        for (Label label : selectByEs) {
            queryFields.append(label.getInjectionLabelCode()).append(",");
        }
        if (queryFields.length() > 0) {
            queryFields.deleteCharAt(queryFields.length() - 1);
        }
        param.put("queryFields", queryFields.toString());
        param.put("centerType", "00");
        //查询标签实例数据
        log.info("事件规则请求数据：" + JSON.toJSONString(param));
        Map<String, Object> dubboResult = yzServ.queryYz(JSON.toJSONString(param));
        log.info("事件规则请求ES返回：" + dubboResult.toString());
        if ("0".equals(dubboResult.get("result_code").toString())) {
            //查询成功
            JSONObject body = new JSONObject((HashMap) dubboResult.get("msgbody"));
            dubboLabel.put("body", body);
        } else {
            //查询失败
            dubboLabel.put("result", "查询标签实例失败");
        }
        return dubboLabel;
    }


    /**
     * @param condition 事件规则条件
     * @param label     标签
     * @param context   规则需要比较的上下文内容
     */
    public Map<String, String> decideExpress(EventMatchRulCondition condition, Label label, DefaultContext<String, Object> context) {
        String type = condition.getOperType();
        Map<String, String> message = new HashMap<>();
        message.put("code", "success");
        ExpressRunner runner = new ExpressRunner();
        runner.addFunction("toNum", new StringToNumOperator("toNum"));

        try {
            String str = cpcLabel(label, type, condition.getRightParam());
            log.info("事件规则表达式" + str);
            RuleResult result = runner.executeRule(str, context, true, true);
            if (null == result.getResult()) {
                //计算为false
                message.put("code", "failed");
                message.put("result", "事件规则条件" + label.getInjectionLabelCode() + "的标签值" + context.get(label.getInjectionLabelCode()) + "不满足条件" + str.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }


    /**
     * 事件下活动校验
     *
     * @param eventId
     * @param lanId
     * @param channel
     * @return
     */
    private List<Map<String, Object>> getResultByEvent(Long eventId, String eventCode, String lanId, String channel, String reqId, String accNbr, String c4, String custId) {
        Map<String, Object> redis = eventRedisService.getRedis("CAM_IDS_EVT_REL_", eventId);
        List<Map<String, Object>> mktCampaginIdList = new ArrayList<>();
        if (redis != null) {
            List<Map<String, Object>> mktCampaginIds = (List<Map<String, Object>>) redis.get("CAM_IDS_EVT_REL_" + eventId);
            log.info("查询的结果mktCampaginIds：" + JSON.toJSONString(mktCampaginIds));
            //根据事件code查询事件信息
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("eventCode", eventCode);
            Map<String, Object> eventRedis = eventRedisService.getRedis("EVENT_", paramMap);
            ContactEvt event = new ContactEvt();
            if (eventRedis != null) {
                event = (ContactEvt) eventRedis.get("EVENT_" + eventCode);
            }
            // 根据优先级排序
            Collections.sort(mktCampaginIds, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Long count1 = (Long) o1.get("campaignSeq");
                    Long count2 = (Long) o2.get("campaignSeq");
                    return count2.compareTo(count1);
                }
            });
            // 获取事件中配置的前TopCampaignNum个活动
            if (event == null || (event != null && (event.getTopCampaignNum() == null || event.getTopCampaignNum() == 0))) {
                mktCampaginIdList.addAll(mktCampaginIds);
            } else {
                for (int i = 0; i < mktCampaginIds.size() && i < event.getTopCampaignNum(); i++) {
                    mktCampaginIdList.add(mktCampaginIds.get(i));
                }
            }
            log.info("排序筛选后mktCampaginIdList：" + JSON.toJSONString(mktCampaginIdList));
        }
        // 初始化线程
        ExecutorService fixThreadPool = Executors.newFixedThreadPool(maxPoolSize);
        List<Future<Map<String, Object>>> futureList = new ArrayList<>();
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        try {
            for (Map<String, Object> act : mktCampaginIdList) {
                act.put("eventCode", eventCode);
                Future<Map<String, Object>> future = fixThreadPool.submit(new ListResultByEventTask(lanId, channel, reqId, accNbr, act, c4, custId));
                futureList.add(future);
            }
            if (futureList != null && futureList.size() > 0) {
                for (Future<Map<String, Object>> future : futureList) {
                    try {
                        Map<String, Object> resultMap = future.get();
                        if (resultMap != null && !resultMap.isEmpty()) {
                            resultMapList.add(resultMap);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            log.error("[op:getResultByEvent] failed to getResultByEvent by eventId = {}, lanId = {}, channel = {}, Expection = ", eventId, lanId, channel, e);
        } finally {
            fixThreadPool.shutdown();
        }
        return resultMapList;
    }

    /**
     * 多线程活动校验
     */
    class ListResultByEventTask implements Callable<Map<String, Object>> {
        private String lanId;
        private String channel;
        private String reqId;
        private String accNbr;
        private Map<String, Object> act;
        private String c4;
        private String custId;

        public ListResultByEventTask(String lanId, String channel, String reqId, String accNbr, Map<String, Object> act, String c4, String custId) {
            this.lanId = lanId;
            this.channel = channel;
            this.reqId = reqId;
            this.accNbr = accNbr;
            this.act = act;
            this.c4 = c4;
            this.custId = custId;
        }

        @Override
        public Map<String, Object> call() throws Exception {

            Map<String, Object> resultMap = new ConcurrentHashMap<>();

            Map<String, Object> mktCampaignMap = new ConcurrentHashMap<>();

            Map<String, Object> mktCampaignCustMap = new ConcurrentHashMap<>();
            log.info("活动预校验开始************");

            try {
                Long mktCampaginId = (Long) act.get("mktCampaginId");
                //初始化es log
                JSONObject esJson = new JSONObject();
                esJson.put("reqId", reqId);
                esJson.put("activityId", mktCampaginId);
                esJson.put("activityName", act.get("mktCampaginName"));
                esJson.put("activityCode", act.get("mktCampaginNbr"));
                esJson.put("hitEntity", accNbr); //命中对象
                esJson.put("eventCode", act.get("eventCode"));


                if ("QD000015".equals(channel)) {
                    log.info("活动预校验：channel-----QD000015");
                    List<String> strategyTypeList = new ArrayList<>();
                    strategyTypeList.add("1000");
                    strategyTypeList.add("2000");
                    strategyTypeList.add("5000");

                    boolean iSRed = false;
                    //验证过滤规则时间,默认只查询5000类型的时间段过滤
                    Map<String, Object> params = new HashMap<>();
                    params.put("strategyTypeList", strategyTypeList);
                    List<FilterRule> filterRuleList = new ArrayList<>();
                    Map<String, Object> filterRuleRedis = eventRedisService.getRedis("FILTER_RULE_STR_", mktCampaginId, params);
                    if (filterRuleRedis != null) {
                        filterRuleList = (List<FilterRule>) filterRuleRedis.get("FILTER_RULE_STR_" + mktCampaginId);
                    }

                    for (FilterRule filterRule : filterRuleList) {
                        if ("1000".equals(filterRule.getFilterType()) || "2000".equals(filterRule.getFilterType())) {
                            iSRed = true;
                            break;
                        }
                    }
                    JSONArray accArrayF = new JSONArray();
                    if (iSRed) {
                        JSONObject param = new JSONObject();
                        //查询标识
                        param.put("c3", lanId);
                        param.put("queryId", custId);
                        param.put("queryNum", "");
                        param.put("queryFields", "");
                        param.put("type", "4");
                        param.put("centerType", "00");
                        Map<String, Object> dubboResult_F = new HashMap<>();
                        try {
                            dubboResult_F = yzServ.queryYz(JSON.toJSONString(param));
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("queryYz查询失败：" + e, JSON.toJSONString(dubboResult_F));
                        }
                        try {
                            if ("0".equals(dubboResult_F.get("result_code").toString())) {
                                accArrayF = new JSONArray((List<Object>) dubboResult_F.get("msgbody"));
                            }
                        } catch (Exception e) {
                            log.info("dubboResult_F.get(\"result_code\").toString()异常了~~~");
                            e.printStackTrace();
                        }
                    }

                    try {
                        for (FilterRule filterRule : filterRuleList) {
                            if ("1000".equals(filterRule.getFilterType()) || "2000".equals(filterRule.getFilterType())) {
                                //获取名单
                                String userList = filterRule.getUserList();
                                if (userList != null && !"".equals(userList)) {
                                    for (Object ob : accArrayF) {
                                        int index = userList.indexOf(((Map) ob).get("ACC_NBR").toString());
                                        if (index >= 0) {
                                            esJson.put("hit", false);
                                            esJson.put("msg", "红黑名单过滤规则验证被拦截");
                                            esHitService.save(esJson, IndexList.ACTIVITY_MODULE);
                                            return Collections.EMPTY_MAP;
                                        }
                                    }
                                }
                            } else if ("5000".equals(filterRule.getFilterType())) {
                                //时间段的格式
                                if (compareHourAndMinute(filterRule)) {
                                    log.info("过滤时间段验证被拦截");
                                    esJson.put("hit", false);
                                    esJson.put("msg", "过滤时间段验证被拦截");
                                    esHitService.save(esJson, IndexList.ACTIVITY_MODULE);
                                    return Collections.EMPTY_MAP;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.info("filterRuleList 预校验过滤出错！");
                        e.printStackTrace();
                    }
                } else {
                    try {
                        List<String> strategyTypeList = new ArrayList<>();
                        strategyTypeList.add("1000");
                        strategyTypeList.add("2000");
                        strategyTypeList.add("5000");
                        //验证过滤规则时间,默认只查询5000类型的时间段过滤
                        Map<String, Object> params = new HashMap<>();
                        params.put("strategyTypeList", strategyTypeList);
                        List<FilterRule> filterRuleList = new ArrayList<>();
                        Map<String, Object> filterRuleRedis = eventRedisService.getRedis("FILTER_RULE_STR_", mktCampaginId, params);
                        if (filterRuleRedis != null) {
                            filterRuleList = (List<FilterRule>) filterRuleRedis.get("FILTER_RULE_STR_" + mktCampaginId);
                        }
                        //List<FilterRule> filterRuleList = filterRuleMapper.selectFilterRuleListByStrategyId(mktCampaginId, strategyTypeList);
                        for (FilterRule filterRule : filterRuleList) {
                            if ("1000".equals(filterRule.getFilterType()) || "2000".equals(filterRule.getFilterType())) {
                                //获取名单
                                String userList = filterRule.getUserList();
                                if (userList != null && !"".equals(userList)) {
                                    int index = userList.indexOf(accNbr);
                                    if (index >= 0) {
                                        esJson.put("hit", false);
                                        esJson.put("msg", "红黑名单过滤规则验证被拦截");
                                        esHitService.save(esJson, IndexList.ACTIVITY_MODULE);
                                        return Collections.EMPTY_MAP;
                                    }
                                }
                            } else if ("5000".equals(filterRule.getFilterType())) {
                                //时间段的格式
                                if (compareHourAndMinute(filterRule)) {
                                    log.info("过滤时间段验证被拦截");
                                    esJson.put("hit", false);
                                    esJson.put("msg", "过滤时间段验证被拦截");
                                    esHitService.save(esJson, IndexList.ACTIVITY_MODULE);
                                    return Collections.EMPTY_MAP;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.info("filterRuleList else ！预校验过滤出错");
                        log.info("filterRuleList else ！预校验过滤出错");
                        e.printStackTrace();
                    }
                }


                //查询活动信息
                Map<String, Object> mktCampaignRedis = eventRedisService.getRedis("MKT_CAMPAIGN_", mktCampaginId);
                MktCampaignDO mktCampaign = new MktCampaignDO();
                if (mktCampaignRedis != null) {
                    try {
                        mktCampaign = (MktCampaignDO) mktCampaignRedis.get("MKT_CAMPAIGN_" + mktCampaginId);
                    } catch (Exception e) {
                        redisUtils.del("MKT_CAMPAIGN_"+mktCampaginId);
                        e.printStackTrace();
                    }
//                    if ("3000".equals(mktCampaign.getMktCampaignCategory())){
//                        redisUtils.del("MKT_CAMPAIGN_"+mktCampaginId);
//                    }
                }

                Date now = null;
                try {
                    now = new Date();
                    //验证活动生效时间
                    Date beginTime = mktCampaign.getPlanBeginTime();
                    Date endTime = mktCampaign.getPlanEndTime();
                    if (now.before(beginTime) || now.after(endTime)) {
                        //当前时间不在活动生效时间内
                        esJson.put("hit", false);
                        esJson.put("msg", "当前时间不在活动生效时间内");
                        log.info("当前时间不在活动生效时间内");
                        esHitService.save(esJson, IndexList.ACTIVITY_MODULE);
                        return Collections.EMPTY_MAP;
                    }
                } catch (Exception e) {
                    log.info("验证活动生效时间失败");
                    e.printStackTrace();
                }


                // 判断活动状态


                if (!(StatusCode.STATUS_CODE_PUBLISHED.getStatusCode().equals(mktCampaign.getStatusCd())
                        || StatusCode.STATUS_CODE_ADJUST.getStatusCode().equals(mktCampaign.getStatusCd()))) {
                    esJson.put("hit", false);
                    esJson.put("msg", "活动状态未发布");
//                log.info("活动状态未发布");
                    esHitService.save(esJson, IndexList.ACTIVITY_MODULE);
                    return Collections.EMPTY_MAP;
                }


                // 判断触发活动类型
                if (!StatusCode.REAL_TIME_CAMPAIGN.getStatusCode().equals(mktCampaign.getTiggerType())
                        && !StatusCode.MIXTURE_CAMPAIGN.getStatusCode().equals(mktCampaign.getTiggerType())) {
                    esJson.put("hit", false);
                    esJson.put("msg", "活动触发类型不符");
                    log.info("活动触发类型不符");
                    esHitService.save(esJson, IndexList.ACTIVITY_MODULE);
                    return Collections.EMPTY_MAP;
                }

                // 判断活动类型
                if (!StatusCode.AUTONOMICK_CAMPAIGN.getStatusCode().equals(mktCampaign.getMktCampaignCategory())
                        && !"3000".equals(mktCampaign.getMktCampaignCategory())) {
                    esJson.put("hit", false);
                    esJson.put("msg", "活动类型不符");
                    log.info("活动类型不符");
                    esHitService.save(esJson, IndexList.ACTIVITY_MODULE);
                    return Collections.EMPTY_MAP;
                }

                // 查询策略信息
                Map<String, Object> MktStrategyConfRedis = eventRedisService.getRedis("MKT_CAM_STRATEGY_", mktCampaginId);
                List<MktStrategyConfDO> mktStrategyConfDOS = new ArrayList<>();
                if (MktStrategyConfRedis != null) {
                    mktStrategyConfDOS = (List<MktStrategyConfDO>) MktStrategyConfRedis.get("MKT_CAM_STRATEGY_" + mktCampaginId);
                }

                if (mktStrategyConfDOS == null) {
                    esJson.put("hit", false);
                    esJson.put("msg", "策略查询失败");
                    log.info("策略查询失败");
                    esHitService.save(esJson, IndexList.ACTIVITY_MODULE);
                    return Collections.EMPTY_MAP;
                }
                List<Map<String, Object>> strategyMapList = new ArrayList<>();
                for (MktStrategyConfDO mktStrategyConf : mktStrategyConfDOS) {

                    //初始化es log
                    JSONObject esJsonStrategy = new JSONObject();
                    esJsonStrategy.put("reqId", reqId);
                    esJsonStrategy.put("activityId", mktCampaginId);
                    esJsonStrategy.put("hitEntity", accNbr); //命中对象
                    esJsonStrategy.put("strategyConfId", mktStrategyConf.getMktStrategyConfId());
                    esJsonStrategy.put("strategyConfName", mktStrategyConf.getMktStrategyConfName());

                    Map<String, Object> strategyMap = new ConcurrentHashMap<>();
                    //验证策略生效时间
                    if (!(now.after(mktStrategyConf.getBeginTime()) && now.before(mktStrategyConf.getEndTime()))) {
                        //若当前时间在策略生效时间外
//                    log.info("当前时间不在策略生效时间内");

                        esJson.put("hit", false);
                        esJson.put("msg", "策略未命中");
                        esHitService.save(esJson, IndexList.ACTIVITY_MODULE, reqId + mktCampaginId + accNbr);

                        esJsonStrategy.put("hit", false);
                        esJsonStrategy.put("msg", "当前时间不在策略生效时间内");
                        esHitService.save(esJsonStrategy, IndexList.STRATEGY_MODULE);
                        continue;
                    }
                    //适用地市校验
                    if (mktStrategyConf.getAreaId() != null && !"".equals(mktStrategyConf.getAreaId())) {
                        String[] strArrayCity = mktStrategyConf.getAreaId().split("/");
                        boolean areaCheck = true;
                        for (String str : strArrayCity) {
                            if (c4 != null && c4.equals(str)) {
                                areaCheck = false;
                                break;
                            } else if (lanId != null) {
                                if (lanId.equals(str)) {
                                    areaCheck = false;
                                    break;
                                }
                            } else {
                                //适用地市获取异常 lanId
//                            log.info("适用地市获取异常");

                                esJson.put("hit", false);
                                esJson.put("msg", "策略未命中");
                                esHitService.save(esJson, IndexList.ACTIVITY_MODULE, reqId + mktCampaginId + accNbr);

                                strategyMap.put("msg", "适用地市获取异常");
                                esJsonStrategy.put("hit", "false");
                                esJsonStrategy.put("msg", "适用地市获取异常");
                                esHitService.save(esJsonStrategy, IndexList.STRATEGY_MODULE);
                            }
                        }
                        if (areaCheck) {

                            esJson.put("hit", false);
                            esJson.put("msg", "策略未命中");
                            esHitService.save(esJson, IndexList.ACTIVITY_MODULE, reqId + mktCampaginId + accNbr);

                            strategyMap.put("msg", "适用地市不符");
                            esJsonStrategy.put("hit", "false");
                            esJsonStrategy.put("msg", "适用地市不符");
                            esHitService.save(esJsonStrategy, IndexList.STRATEGY_MODULE);
                            continue;
                        }
                    } else {
                        //适用地市数据异常
//                    log.info("适用地市数据异常");

                        esJson.put("hit", false);
                        esJson.put("msg", "策略未命中");
                        esHitService.save(esJson, IndexList.ACTIVITY_MODULE, reqId + mktCampaginId + accNbr);

                        strategyMap.put("msg", "适用地市数据异常");
                        esJsonStrategy.put("hit", "false");
                        esJsonStrategy.put("msg", "适用地市数据异常");
                        esHitService.save(esJsonStrategy, IndexList.STRATEGY_MODULE);
                        continue;
                    }
                    //判断适用渠道
                 /*   if (mktStrategyConf.getChannelsId() != null && !"".equals(mktStrategyConf.getChannelsId())) {
                        List<String> channelCodeList = new ArrayList<>();
                        Map<String, Object> params = new HashMap<>();
                        params.put("channelsId", mktStrategyConf.getChannelsId());
                        Map<String, Object> channelCodeRedis = eventRedisService.getRedis("CHANNEL_CODE_LIST_", mktStrategyConf.getMktStrategyConfId(), params);
                        if(channelCodeRedis!=null){
                            channelCodeList = (List<String>) channelCodeRedis.get("CHANNEL_CODE_LIST_" + mktStrategyConf.getMktStrategyConfId());
                        }
                        boolean channelCheck = true;
                        try {
                            for (String channelCode : channelCodeList) {
                                if (channel != null) {
                                    if (channel.equals(channelCode)) {
                                        channelCheck = false;
                                        break;
                                    }
                                } else {
                                    //适用地市获取异常 lanId
    //                            log.info("适用渠道获取异常");

                                    esJson.put("hit", false);
                                    esJson.put("msg", "策略未命中");
                                    esHitService.save(esJson, IndexList.ACTIVITY_MODULE, reqId + mktCampaginId + accNbr);

                                    strategyMap.put("msg", "适用渠道获取异常");
                                    esJsonStrategy.put("hit", "false");
                                    esJsonStrategy.put("msg", "适用渠道获取异常");
                                    esHitService.save(esJsonStrategy, IndexList.STRATEGY_MODULE);
                                }
                            }
                        } catch (Exception e) {
                            log.error("预校验渠道获取异常");
                            e.printStackTrace();
                        }
                        if (channelCheck) {
//                        log.info("适用渠道不符");

                            esJson.put("hit", false);
                            esJson.put("msg", "策略未命中");
                            esHitService.save(esJson, IndexList.ACTIVITY_MODULE, reqId + mktCampaginId + accNbr);

                            strategyMap.put("msg", "适用渠道不符");
                            esJsonStrategy.put("hit", "false");
                            esJsonStrategy.put("msg", "适用渠道不符");
                            esHitService.save(esJsonStrategy, IndexList.STRATEGY_MODULE);
                            continue;
                        }
                    } else {
                        //适用地市数据异常
                        log.info("适用渠道数据异常");

                        esJson.put("hit", false);
                        esJson.put("msg", "策略未命中");
                        esHitService.save(esJson, IndexList.ACTIVITY_MODULE, reqId + mktCampaginId + accNbr);

                        strategyMap.put("msg", "适用渠道数据异常");
                        esJsonStrategy.put("hit", "false");
                        esJsonStrategy.put("msg", "适用渠道数据异常");
                        esHitService.save(esJsonStrategy, IndexList.STRATEGY_MODULE);
                        continue;
                    }*/
                    // 获取规则
                    List<Map<String, Object>> ruleMapList = new ArrayList<>();
                    List<MktStrategyConfRuleDO> mktStrategyConfRuleList = new ArrayList<>();
                    Map<String, Object> mktRuleListRedis = eventRedisService.getRedis("RULE_LIST_", mktStrategyConf.getMktStrategyConfId());
                    if (mktRuleListRedis != null) {
                        mktStrategyConfRuleList = (List<MktStrategyConfRuleDO>) mktRuleListRedis.get("RULE_LIST_" + mktStrategyConf.getMktStrategyConfId());
                    }

                    try {
                        for (MktStrategyConfRuleDO mktStrategyConfRuleDO : mktStrategyConfRuleList) {
                            Map<String, Object> ruleMap = new HashMap<>();
                            String evtContactConfIds = mktStrategyConfRuleDO.getEvtContactConfId();
                            //                    if (evtContactConfMapList != null && evtContactConfMapList.size() > 0) {
                            ruleMap.put("ruleId", mktStrategyConfRuleDO.getMktStrategyConfRuleId());
                            ruleMap.put("ruleName", mktStrategyConfRuleDO.getMktStrategyConfRuleName());
                            ruleMap.put("tarGrpId", mktStrategyConfRuleDO.getTarGrpId());
                            ruleMap.put("productId", mktStrategyConfRuleDO.getProductId());
                            ruleMap.put("evtContactConfId", mktStrategyConfRuleDO.getEvtContactConfId());
                            //                        ruleMap.put("evtContactConfMapList", evtContactConfMapList);
                            ruleMapList.add(ruleMap);
                            //                    }
                        }
                    } catch (Exception e) {
                        log.error("预校验获取规则查询失败");
                        e.printStackTrace();
                    }
                    if (ruleMapList != null && ruleMapList.size() > 0) {
                        strategyMap.put("strategyConfId", mktStrategyConf.getMktStrategyConfId());
                        strategyMap.put("strategyConfName", mktStrategyConf.getMktStrategyConfName());
                        strategyMap.put("ruleMapList", ruleMapList);
                        strategyMapList.add(strategyMap);
                    }
                }

                List<String> mktCamCodeList = new ArrayList<>();
                Map<String, Object> mktCamCodeListRedis = eventRedisService.getRedis("MKT_CAM_API_CODE_KEY");
                if (mktCamCodeListRedis != null) {
                    mktCamCodeList = (List<String>) mktCamCodeListRedis.get("MKT_CAM_API_CODE_KEY");
                }

                if (strategyMapList != null && strategyMapList.size() > 0) {
                    // 判断initId是否在清单列表里面
                    if (mktCamCodeList.contains(mktCampaign.getInitId().toString())) {
                        // if (mktCamCodeList.contains(mktCampaign.getMktCampaignId().toString())) {
                        mktCampaignCustMap.put("initId", mktCampaign.getInitId());
                        mktCampaignCustMap.put("mktCampaginId", mktCampaginId);
                        mktCampaignCustMap.put("levelConfig", act.get("levelConfig"));
                        mktCampaignCustMap.put("campaignSeq", act.get("campaignSeq"));
                        mktCampaignCustMap.put("strategyMapList", strategyMapList);
                    } else {
                        mktCampaignMap.put("mktCampaginId", mktCampaginId);
                        mktCampaignMap.put("levelConfig", act.get("levelConfig"));
                        mktCampaignMap.put("campaignSeq", act.get("campaignSeq"));
                        mktCampaignMap.put("strategyMapList", strategyMapList);
                        mktCampaignMap.put("type", mktCampaign.getMktCampaignType().toString()); // 活动类型 5000
                    }
                }
                // 实时
                resultMap.put("mktCampaignMap", mktCampaignMap);
                // 清单
                resultMap.put("mktCampaignCustMap", mktCampaignCustMap);

            } catch (Exception e) {
                log.info("预校验出错", e.getCause());
                log.info("预校验出错", e.toString());
                log.info("预校验出错", e.getMessage());
                e.printStackTrace();
            }
            return resultMap;
        }
    }

    /**
     * 添加内置的标签并赋值
     *
     * @param map
     */
    private void setInlayLabel(Map<String, String> map) {
        Calendar calendar = Calendar.getInstance();
        map.put("CPCP_IN_EVENT_YEAR", String.valueOf(calendar.get(Calendar.YEAR)));
        map.put("CPCP_IN_EVENT_MONTH", String.valueOf(calendar.get(Calendar.MONTH) + 1));
        map.put("CPCP_IN_EVENT_DAY", String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
    }


    // 处理资产级标签和销售品级标签
    private DefaultContext<String, Object> getAssetAndPromLabel(Map<String, String> mktAllLabel, Map<String, String> params, Map<String, String> privateParams, DefaultContext<String, Object> context, JSONObject esJson, Map<String, String> labelItems) {
        //资产级标签
        DefaultContext<String, Object> contextNew = new DefaultContext<String, Object>();
        if (mktAllLabel.get("assetLabels") != null && !"".equals(mktAllLabel.get("assetLabels"))) {
            String assetLabels = mktAllLabel.get("assetLabels");
            if (!assetLabels.contains("CCUST_ID")){
                assetLabels += ",CCUST_ID";
            }
            JSONObject assParam = new JSONObject();
            assParam.put("queryNum", privateParams.get("accNbr"));
            assParam.put("c3", params.get("lanId"));
            assParam.put("queryId", privateParams.get("integrationId"));
            assParam.put("type", "1");
            assParam.put("queryFields", assetLabels);
            assParam.put("centerType", "00");

            //因子查询-----------------------------------------------------
            Map<String, Object> dubboResult = yzServ.queryYz(JSON.toJSONString(assParam));
            if ("0".equals(dubboResult.get("result_code").toString())) {
                JSONObject body = new JSONObject((HashMap) dubboResult.get("msgbody"));
                //ES log 标签实例
                //拼接规则引擎上下文
                for (Map.Entry<String, Object> entry : body.entrySet()) {
                    //添加到上下文
                    contextNew.put(entry.getKey(), entry.getValue());
                }
            }
        }
        contextNew.putAll(labelItems);   //添加事件采集项中作为标签使用的实例
        contextNew.putAll(context);      // 客户级标签
        contextNew.put("integrationId", privateParams.get("integrationId"));
        contextNew.put("accNbr", privateParams.get("accNbr"));
        return contextNew;
    }


    class getListMapLabelTask implements Callable<DefaultContext<String, Object>> {

        private Object o;
        private Map<String, String> mktAllLabel;
        private Map<String, String> map;
        private DefaultContext<String, Object> context;
        private JSONObject esJson;
        Map<String, String> labelItems;

        public getListMapLabelTask(Object o, Map<String, String> mktAllLabel, Map<String, String> map, DefaultContext<String, Object> context, JSONObject esJson, Map<String, String> labelItems) {
            this.o = o;
            this.mktAllLabel = mktAllLabel;
            this.map = map;
            this.context = context;
            this.esJson = esJson;
            this.labelItems = labelItems;
        }

        @Override
        public DefaultContext<String, Object> call() throws Exception {

            DefaultContext<String, Object> resultMap = new DefaultContext<>();
            Map<String, String> privateParams = new ConcurrentHashMap<>();
            privateParams.put("isCust", "0"); //是客户级
            privateParams.put("accNbr", ((Map) o).get("ACC_NBR").toString());
            privateParams.put("integrationId", ((Map) o).get("ASSET_INTEG_ID").toString());
            privateParams.put("custId", map.get("custId"));

            Map<String, Object> assetAndPromLabel = getAssetAndPromLabel(mktAllLabel, map, privateParams, context, esJson, labelItems);
            if (assetAndPromLabel != null) {
                resultMap.putAll(assetAndPromLabel);
            }
            return resultMap;
        }
    }


    /**
     * 获取客户清单 Task
     */
    class getCustListTask implements Callable<Map<String, Object>> {

        private List<String> campaignList;
        private List<String> initIdList;
        private Long eventId;
        private String landId;
        private String custId;
        private Map<String, String> map;
        private List<Map<String, Object>> evtTriggers;

        public getCustListTask(List<String> campaignList, List<String> initIdList, Long eventId, String landId, String custId, Map<String, String> map, List<Map<String, Object>> evtTriggers) {
            this.campaignList = campaignList;
            this.initIdList = initIdList;
            this.eventId = eventId;
            this.landId = landId;
            this.custId = custId;
            this.map = map;
            this.evtTriggers = evtTriggers;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            Map<String, Object> resultMap = new HashMap<>();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Map<String, Object> resultByEventRedis = eventRedisService.getRedis("CAM_EVT_REL_", eventId);
                List<MktCamEvtRel> resultByEvent = new ArrayList<>();
                if (resultByEventRedis != null) {
                    resultByEvent = (List<MktCamEvtRel>) resultByEventRedis.get("CAM_EVT_REL_" + eventId);
                }

//                List<MktCamEvtRel> resultByEvent = (List<MktCamEvtRel> ) redisUtils.get("CAM_EVT_REL_" + eventId);
//                if (resultByEvent == null) {
//                    resultByEvent = mktCamEvtRelMapper.qryBycontactEvtId(eventId);
//                    redisUtils.set("CAM_EVT_REL_" + eventId, resultByEvent);
//                }
                //判断有没有客户级活动
                Boolean hasCust = false;  //是否有客户级
                Boolean hasProm = false;  //是否有套餐级
                for (MktCamEvtRel mktCamEvtRel : resultByEvent) {
                    if (campaignList.contains(mktCamEvtRel.getMktCampaignId().toString()) && !hasCust && mktCamEvtRel.getLevelConfig() == 1) {
                        hasCust = true;
                    } else if (campaignList.contains(mktCamEvtRel.getMktCampaignId().toString()) && !hasProm && mktCamEvtRel.getLevelConfig() == 2) {
                        hasProm = true;
                    }
                }
                List<String> custRuleIdList = new ArrayList<>();
                List<String> assetRuleIdList = new ArrayList<>();
                List<String> packageRuleIdList = new ArrayList<>();
                List<Map<String, Object>> mapList = mktCamEvtRelMapper.selectRuleIdsByEventId(eventId);
                for (Map<String, Object> ruleMap : mapList) {
                    if (campaignList.contains(ruleMap.get("campaignId").toString())) {
                        if ((Integer) ruleMap.get("levelConfig") == 1) {  // 客户级
                            custRuleIdList.add(ruleMap.get("ruleId").toString());
                        } else if ((Integer) ruleMap.get("levelConfig") == 2) { // 套餐级
                            packageRuleIdList.add(ruleMap.get("ruleId").toString());
                        } else {
                            assetRuleIdList.add(ruleMap.get("ruleId").toString());  // 资产级
                        }
                    }
                }

                JSONObject param = new JSONObject();
                //查询标识
                param.put("c3", landId);
                param.put("queryId", custId);
                param.put("queryNum", "");
                param.put("queryFields", "");
                param.put("type", "4");
                param.put("centerType", "00");

                Map<String, Object> custParamMap = new HashMap<>();
                Map<String, Object> assetParamMap = new HashMap<>();
                Map<String, Object> promParamMap = new HashMap<>();
                JSONArray accArray = new JSONArray();
                List<String> custIdList = new ArrayList<>();
                List<String> assetIdList = new ArrayList<>();
                List<String> promIdList = new ArrayList<>();
                if (custId != null && !"".equals(custId) && hasCust) {
                    Map<String, Object> dubboResult = yzServ.queryYz(JSON.toJSONString(param));
                    if ("0".equals(dubboResult.get("result_code").toString())) {
                        accArray = new JSONArray((List<Object>) dubboResult.get("msgbody"));
                        for (Object o : accArray) {
                            custIdList.add(((Map) o).get("ASSET_INTEG_ID").toString());
                        }
                    }
                } else {
                    assetIdList.add(map.get("integrationId"));
                }
                if (hasProm) {
                    List<Map<String, Object>> accNbrMapList = getAccNbrList(map.get("accNbr"));
                    for (Map<String, Object> accNbrMap : accNbrMapList) {
                        String assetIntegId = (String) accNbrMap.get("ASSET_INTEG_ID");
                        promIdList.add(assetIntegId);
                    }
                }
                custParamMap.put("assetList", custIdList);
                custParamMap.put("ruleList", custRuleIdList);

                assetParamMap.put("assetList", assetIdList);
                assetParamMap.put("ruleList", assetRuleIdList);

                promParamMap.put("assetList", promIdList);
                promParamMap.put("ruleList", packageRuleIdList);

                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("prom", promParamMap);
                paramMap.put("cust", custParamMap);
                paramMap.put("asset", assetParamMap);
                paramMap.put("campaignList", initIdList);

                // 获取本地网的中文名
                String landName = AreaNameEnum.getNameByLandId(Long.valueOf(landId));

                Map<String, Object> paramResultMap = esService.queryCustomer4Event(paramMap);

                List<Map<String, Object>> resultList = new ArrayList<>();
                // 解析
                if ("200".equals(paramResultMap.get("resultCode"))) {
                    List<Map<String, Object>> resultMapList = (List<Map<String, Object>>) paramResultMap.get("data");
                    if (resultMapList != null && resultMapList.size() > 0) {
                        for (Map<String, Object> resultMap1 : resultMapList) {
                            Map<String, Object> result = new HashMap();
                            List<Map<String, Object>> taskChlList = (List<Map<String, Object>>) ((Map) resultMap1.get("CPC_VALUE")).get("taskChlList");
                            int count = 0;  // 统计符合渠道的个数
                            List<Map<String, Object>> taskChlListNew = new ArrayList<>();
                            for (Map<String, Object> taskChlMap : taskChlList) {
                                if (map.get("channelCode").equals(taskChlMap.get("channelId"))) {
                                    count++;
                                    taskChlListNew.add(taskChlMap);
                                }
                            }
                            if (count > 0) {
                                ((Map) resultMap1.get("CPC_VALUE")).put("taskChlList", taskChlListNew);
                            }

                            // 销售品过滤
                            String custProdFilter = null;
                            Map<String, Object> custProdFilterRedis = eventRedisService.getRedis("CUST_PROD_FILTER");
                            if (custProdFilterRedis != null) {
                                custProdFilter = (String) custProdFilterRedis.get("CUST_PROD_FILTER");
                            }

//                            String custProdFilter = (String) redisUtils.get("CUST_PROD_FILTER");
//                            //String custProdFilter = null;
//                            if (custProdFilter == null) {
//                                List<SysParams> sysParamsList = sysParamsMapper.listParamsByKeyForCampaign("CUST_PROD_FILTER");
//                                if (sysParamsList != null && sysParamsList.size() > 0) {
//                                    custProdFilter = sysParamsList.get(0).getParamValue();
//                                    redisUtils.set("CUST_PROD_FILTER", custProdFilter);
//                                }
//                            }


                            if (custProdFilter != null && "2".equals(custProdFilter)) {
                                List<Map<String, Object>> taskMapNewList = new ArrayList<>();

                                Map<String, Object> taskMap = (Map<String, Object>) resultMap1.get("CPC_VALUE");

                                List<Map<String, Object>> taskChlNewList = (List<Map<String, Object>>) ((Map) resultMap1.get("CPC_VALUE")).get("taskChlList");

                                String integrationId = (String) taskMap.get("integrationId");

                                // 判断该活动是否配置了销售品过滤
                                Integer mktCampaignId = (Integer) taskMap.get("activityId");

                                Map<String, Object> filterRuleListRedis = eventRedisService.getRedis("FILTER_RULE_LIST_", mktCampaignId.longValue());
                                List<FilterRule> filterRuleList = new ArrayList<>();
                                if (filterRuleListRedis != null) {
                                    filterRuleList = (List<FilterRule>) filterRuleListRedis.get("FILTER_RULE_LIST_" + mktCampaignId);
                                }
//                                List<FilterRule> filterRuleList = (List<FilterRule>) redisUtils.get("FILTER_RULE_LIST_" + mktCampaignId);
//                                if (filterRuleList == null) {
//                                    filterRuleList = filterRuleMapper.selectFilterRuleList(Long.valueOf(mktCampaignId));
//                                    redisUtils.set("FILTER_RULE_LIST_" + mktCampaignId, filterRuleList);
//                                }

                                boolean prodConfig = false;
                                boolean pordFilter = true; // true:未包含要过滤的销售品， false：包含要过滤的销售品
                                for (FilterRule filterRule : filterRuleList) {
                                    if ("3000".equals(filterRule.getFilterType()) || "3000" == filterRule.getFilterType()) {
                                        prodConfig = true;
                                    }
                                    if (prodConfig) {
                                        ResponseResult<AssetDto> assetDtoResponseResult = ctgCacheAssetService.queryCachedAssetDetailByIntegId(integrationId, landName);
                                        AssetDto assetDto = assetDtoResponseResult.getData();
                                        List<String> prodStrList = new ArrayList<>();
                                        if (assetDto != null) {
                                            List<AssetPromDto> assetPromDtoList = assetDto.getAssetPromDtoList();
                                            for (AssetPromDto assetPromDto : assetPromDtoList) {
                                                prodStrList.add(assetPromDto.getSelectablePromNum());
                                            }
                                        }
                                        // 获取过滤规则中的销售品集合
                                        List<String> codeList = ChannelUtil.StringToList(filterRule.getChooseProduct());
                                        //存在于校验
                                        if ("1000".equals(filterRule.getOperator())) {  //  1000 - 存在
                                            int sum = 0;
                                            for (String productCode : codeList) {
                                                // 包含销售品过滤
                                                if (prodStrList.contains(productCode)) {
                                                    sum++;
                                                    break;
                                                }
                                            }
                                            // 若有销售品存在跳过，若没有，直接过滤
                                            if (sum > 0) {
                                                continue;
                                            } else {
                                                pordFilter = false;
                                            }
                                        } else if ("2000".equals(filterRule.getOperator())) {      //  2000 - 不存在
                                            int cou = 0;
                                            for (String productCode : codeList) {
                                                // 不包含销售品过滤
                                                if (prodStrList.contains(productCode)) {
                                                    cou++;
                                                    break;
                                                }
                                            }
                                            // 若有销售品存在，这直接过滤
                                            if (cou > 0) {
                                                pordFilter = false;
                                                break;
                                            }
                                        }
                                    }
                                    prodConfig = false;
                                }
                                if (pordFilter) {
                                    taskMapNewList.addAll(taskChlNewList);

                                }
                                ((Map) resultMap1.get("CPC_VALUE")).put("taskChlList", taskMapNewList);
                            }

                            List<Map> taskChlCountList = (List<Map>) ((Map) resultMap1.get("CPC_VALUE")).get("taskChlList");
                            // 清单方案放入采集项
                            Map<String, Object> evtContent = (Map<String, Object>) JSON.parse(map.get("evtContent"));
                            for (Map<String, Object> taskChlCountMap : taskChlCountList) {
                                // taskChlCountMap.put("triggers", JSONArray.parse(JSONArray.toJSON(map.get("evtContent")).toString()));
                                List<Map<String, Object>> triggersList = new ArrayList<>();
                                if (evtContent != null) {
                                    for (Map.Entry entry : evtContent.entrySet()) {
                                        Map<String, Object> trigger = new HashMap<>();
                                        trigger.put("key", entry.getKey());
                                        trigger.put("value", entry.getValue());
                                        triggersList.add(trigger);
                                    }
                                    taskChlCountMap.put("triggers", triggersList);
                                }
                            }

                            if (taskChlCountList != null && taskChlCountList.size() > 0) {
                                result.putAll((Map) resultMap1.get("CPC_VALUE"));
                                result.put("orderISI", map.get("reqId"));
                                result.put("skipCheck", "0");
                                result.put("orderPriority", "0");
                                Long activityId = Long.valueOf(resultMap1.get("ACTIVITY_ID").toString());

                                //查询活动信息
                                Map<String, Object> mktCampaignRedis = eventRedisService.getRedis("MKT_CAMPAIGN_", activityId);
                                MktCampaignDO mktCampaignDO = new MktCampaignDO();
                                if (mktCampaignRedis != null) {
                                    mktCampaignDO = (MktCampaignDO) mktCampaignRedis.get("MKT_CAMPAIGN_" + activityId);
                                }
//                                MktCampaignDO mktCampaignDO = (MktCampaignDO) redisUtils.get("MKT_CAMPAIGN_" + activityId);
//                                if (mktCampaignDO == null) {
//                                    mktCampaignDO = mktCampaignMapper.selectByPrimaryKey(activityId);
//                                    redisUtils.set("MKT_CAMPAIGN_" + activityId, mktCampaignDO);
//                                }

                                if (mktCampaignDO != null) {
                                    if ("1000".equals(mktCampaignDO.getMktCampaignType())) {
                                        result.put("activityType", "0"); //营销
                                    } else if ("5000".equals(mktCampaignDO.getMktCampaignType())) {
                                        result.put("activityType", "1"); //服务
                                    } else if ("6000".equals(mktCampaignDO.getMktCampaignType())) {
                                        result.put("activityType", "2"); //随销
                                    } else if ("7000".equals(mktCampaignDO.getMktCampaignType())) {
                                        result.put("activityType", "3"); //协同场景
                                    } else {
                                        result.put("activityType", "0"); //活动类型 默认营销
                                    }

                                    result.put("activityStartTime", simpleDateFormat.format(mktCampaignDO.getPlanBeginTime()));
                                    result.put("activityEndTime", simpleDateFormat.format(mktCampaignDO.getPlanEndTime()));
                                } else {
                                    result.put("activityType", "");
                                    result.put("activityStartTime", "");
                                    result.put("activityEndTime", "");
                                }
                                resultList.add(result);
                            }
                        }
                    }
                }
                resultMap.put("ruleList", resultList);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Exception = " + e);
            }
            return resultMap;
        }
    }


    // 计算剩余需要的流量
    private String getCpcpNeedFlow(String cpcpUsedFlow, String cpcpLeftFlow) {
        // 获取当天
        int currentDay = DateUtil.getCurrentDay();
        // 当前月的天数
        int lastDay = DateUtil.getLastDayForCurrentMonth();
        // 已用的流量数
        double cpcpUsedFlowDouble = Double.valueOf(cpcpUsedFlow.replace("GB", ""));
        // 剩余的流量数
        double cpcpLeftFlowDouble = Double.valueOf(cpcpLeftFlow.replace("GB", ""));
        // 这个月到currentDay过的日均用量
        double userAvg = cpcpUsedFlowDouble / currentDay;
        // 需要流量
        double needFlow = 0;
        if (userAvg * (lastDay - currentDay) > cpcpLeftFlowDouble) {
            needFlow = (userAvg * (lastDay - currentDay) - cpcpLeftFlowDouble) * 1024;
        }
        DecimalFormat df = new DecimalFormat("#.##");
        return String.valueOf(df.format(needFlow));
    }

    /**
     * 正则判断是否为手机号码
     *
     * @param phone
     * @return
     */
    private static boolean isMobile(String phone) {
        Pattern p = null;
        Matcher m = null;
        boolean isMatch = false;
        //制定验证条件
        String regex1 = "^[1][3,4,5,7,8][0-9]{9}$";
        String regex2 = "^((13[0-9])|(14[579])|(15([0-3,5-9]))|(16[6])|(17[0135678])|(18[0-9]|19[89]))\\d{8}$";

        p = Pattern.compile(regex2);
        m = p.matcher(phone);
        isMatch = m.matches();
        return isMatch;
    }


    //套餐级业务号码查询资产列表
//    private List<Map<String, Object>> getAccNbrList(String accNbr) {
//        List<String> accNbrList = new ArrayList<>();
//        List<Map<String, Object>> accNbrMapList = new ArrayList<>();
//        // 根据accNum查询prodInstId
//        try {
//            log.info("11111------prodInstIdsObject --->" + accNbr);
//            CacheResultObject<Set<String>> prodInstIdsObject1 = iCacheProdIndexQryService.qryProdInstIndex3(accNbr,"100000");
//            CacheResultObject<Set<String>> prodInstIdsObject2 = iCacheProdIndexQryService.qryProdInstIndex3(accNbr,"120000");
//
//            Set<String> prodInstIds = Sets.newHashSet();
//            if(prodInstIdsObject1!=null&&prodInstIdsObject1.getResultObject()!=null){
////                log.info("100000------qryProdInstIndex3 --->" + JSON.toJSONString(prodInstIdsObject1));
//                prodInstIds.addAll(prodInstIdsObject1.getResultObject());
//            }
//            if(prodInstIdsObject2!=null&&prodInstIdsObject2.getResultObject()!=null){
////                log.info("120000------qryProdInstIndex3 --->" + JSON.toJSONString(prodInstIdsObject2));
//                prodInstIds.addAll(prodInstIdsObject2.getResultObject());
//            }
//            log.info("产品实例id列表-----prodInstIds：" + JSON.toJSONString(prodInstIds));
//            if (prodInstIds.size()>0) {
//                Long mainOfferInstId = null;
//                for (String prodInstId : prodInstIds) {
//                    // 查询产品实例实体缓存 取主产品（1000）的一个
//                    CacheResultObject<ProdInst> prodInstCacheEntity = iCacheProdEntityQryService.getProdInstCacheEntity(prodInstId);
//                    if (prodInstCacheEntity != null && prodInstCacheEntity.getResultObject() != null && "1000".equals(prodInstCacheEntity.getResultObject().getProdUseType())) {
//                        log.info("主产品实例对象-----prodInstCacheEntity：" + JSON.toJSONString(prodInstCacheEntity));
//                        mainOfferInstId = prodInstCacheEntity.getResultObject().getMainOfferInstId();
//                        break;
//                    }
//                }
//                // 根据offerInstId和statusCd查询prodInstEntity
//                if (mainOfferInstId != null) {
//                    CacheResultObject<Set<String>> setCacheResultObject1 = iCacheProdIndexQryService.qryProdInstIndex5(mainOfferInstId.toString(), "100000");
//                    CacheResultObject<Set<String>> setCacheResultObject2 = iCacheProdIndexQryService.qryProdInstIndex5(mainOfferInstId.toString(), "120000");
//                    Set<String> prodInstIdsNew = Sets.newHashSet();
//                    if(setCacheResultObject1!=null&&setCacheResultObject1.getResultObject()!=null){
//                        prodInstIdsNew.addAll(setCacheResultObject1.getResultObject());
//                    }
//                    if(setCacheResultObject2!=null&&setCacheResultObject2.getResultObject()!=null){
//                        prodInstIdsNew.addAll(setCacheResultObject2.getResultObject());
//                    }
//                    log.info("【根据offerInstId和statusCd查询prodInstIds】-----prodInstIdsNew：" + JSON.toJSONString(prodInstIdsNew));
//                    if(prodInstIdsNew.size()>0){
//                        prodInstIdsNew.forEach(prodInstIdNew->{
//                            CacheResultObject<ProdInst> prodInstCacheEntityNew = iCacheProdEntityQryService.getProdInstCacheEntity(prodInstIdNew);
//                            if (prodInstCacheEntityNew != null && prodInstCacheEntityNew.getResultObject() != null) {
//                                final CacheResultObject<RowIdMapping> prodInstIdMappingCacheEntity = iCacheIdMappingEntityQryService.getProdInstIdMappingCacheEntity(prodInstIdNew.toString());
////                                log.info("【根据prodInstIdsNew查询ACC_NBR】-----prodInstIdsNew：" + JSON.toJSONString(prodInstCacheEntityNew));
//                                if (prodInstIdMappingCacheEntity != null && prodInstIdMappingCacheEntity.getResultObject() != null) {
////                                    log.info("【根据prodInstIdsNew查询ASSET_INTEG_ID】-----prodInstIdMappingCacheEntity：" + JSON.toJSONString(prodInstIdMappingCacheEntity));
//                                    String accNum = prodInstCacheEntityNew.getResultObject().getAccNum();
//                                    if (!"661".equals(accNum) && !"662".equals(accNum) && !"663".equals(accNum) && !"664".equals(accNum) && !"665".equals(accNum)){
//                                        Map<String, Object> accNbrMap = new HashMap<>(10);
//                                        accNbrMap.put("ACC_NBR", accNum);
//                                        accNbrMap.put("ASSET_INTEG_ID", prodInstIdMappingCacheEntity.getResultObject().getCrmRowId());
//                                        accNbrMapList.add(accNbrMap);
//
//                                    }
//                                }
//                            }
//                        });
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.info("------套餐级资产查询失败--->" + JSON.toJSONString(accNbrMapList));
//        }
//        log.info("10101010------accNbrMapList --->" + JSON.toJSONString(accNbrMapList));
//        return accNbrMapList;
//    }


    private List<Map<String, Object>> getAccNbrList(String accNbr) {
        List<String> accNbrList = new ArrayList<>();
        List<Map<String, Object>> accNbrMapList = new ArrayList<>();
        // 根据accNum查询prodInstId
        //log.info("11111------prodInstIdsObject --->" + accNbr);
        CacheResultObject<Set<String>> prodInstIdsObject1 = iCacheProdIndexQryService.qryProdInstIndex3(accNbr,"100000");
        CacheResultObject<Set<String>> prodInstIdsObject2 = iCacheProdIndexQryService.qryProdInstIndex3(accNbr,"120000");
        Set<String> prodInstIds = Sets.newHashSet();
        if(prodInstIdsObject1!=null&&prodInstIdsObject1.getResultObject()!=null){
            prodInstIds.addAll(prodInstIdsObject1.getResultObject());
        }
        if(prodInstIdsObject2!=null&&prodInstIdsObject2.getResultObject()!=null){
            prodInstIds.addAll(prodInstIdsObject2.getResultObject());
        }
        //log.info("22222------prodInstIdsObject --->" + JSON.toJSONString(prodInstIdsObject));
        if (prodInstIds.size()>0) {
            Long mainOfferInstId = null;
            for (String prodInstId : prodInstIds) {
                // 查询产品实例实体缓存 取主产品（1000）的一个
                CacheResultObject<ProdInst> prodInstCacheEntity = iCacheProdEntityQryService.getProdInstCacheEntity(prodInstId);
                //        log.info("333333------prodInstCacheEntity --->" + JSON.toJSONString(prodInstCacheEntity));
                if (prodInstCacheEntity != null && prodInstCacheEntity.getResultObject() != null && "1000".equals(prodInstCacheEntity.getResultObject().getProdUseType())) {
                    mainOfferInstId = prodInstCacheEntity.getResultObject().getMainOfferInstId();
                    if(mainOfferInstId!=null){
                        break;
                    }
                }
            }
            // 根据offerInstId和statusCd查询offerProdInstRelId
            if (mainOfferInstId != null) {
                CacheResultObject<Set<String>> setCacheResultObject = iCacheOfferRelIndexQryService.qryOfferProdInstRelIndex1(mainOfferInstId.toString(), "1000");
                if (setCacheResultObject != null && setCacheResultObject.getResultObject() != null && setCacheResultObject.getResultObject().size() > 0) {
                    Set<String> offerProdInstRelIds = setCacheResultObject.getResultObject();
                    for (String offerProdInstRelId : offerProdInstRelIds) {
                        CacheResultObject<OfferProdInstRel> offerProdInstRelCacheEntity = iCacheRelEntityQryService.getOfferProdInstRelCacheEntity(offerProdInstRelId);
                        if (offerProdInstRelCacheEntity != null && offerProdInstRelCacheEntity.getResultObject() != null) {
                            Long prodInstIdNew = offerProdInstRelCacheEntity.getResultObject().getProdInstId();
                            CacheResultObject<ProdInst> prodInstCacheEntityNew = iCacheProdEntityQryService.getProdInstCacheEntity(prodInstIdNew.toString());
                            if (prodInstCacheEntityNew != null && prodInstCacheEntityNew.getResultObject() != null) {
                                final CacheResultObject<RowIdMapping> prodInstIdMappingCacheEntity = iCacheIdMappingEntityQryService.getProdInstIdMappingCacheEntity(prodInstIdNew.toString());
                                String accNum = prodInstCacheEntityNew.getResultObject().getAccNum();
                                if (!"661".equals(accNum) && !"662".equals(accNum) && !"663".equals(accNum) && !"664".equals(accNum) && !"665".equals(accNum)){
                                    Map<String, Object> accNbrMap = new HashMap<>(10);
                                    accNbrMap.put("ACC_NBR", accNum);
                                    accNbrMap.put("ASSET_INTEG_ID", prodInstIdMappingCacheEntity.getResultObject().getCrmRowId());
                                    accNbrMapList.add(accNbrMap);

                                }
                            }
                        }
                    }
                }
            }
        }
        log.info("10101010------accNbrMapList --->" + JSON.toJSONString(accNbrMapList));
        return accNbrMapList;
    }


    /**
     * 计费短信合并功能 CPCP_JIFEI_CONTENT
     *
     * @param labelItems
     */
    private void cpcpJifeiContent(Map<String, String> labelItems, JSONObject evtParams) {
        StringBuilder content = new StringBuilder();
        String usedFlow = "";
        String totalFlow = "";

        usedFlow = (String) redisUtils.get(USED_FLOW);
        if (usedFlow == null || "".equals(usedFlow)) {
            List<SysParams> usedFlowList = sysParamsMapper.listParamsByKeyForCampaign(USED_FLOW);
            if (usedFlowList != null && usedFlowList.size() > 0) {
                SysParams usedFlowParams = usedFlowList.get(0);
                if (usedFlowParams != null) {
                    usedFlow = usedFlowParams.getParamValue();
                    redisUtils.set(USED_FLOW, usedFlow);
                }
            }
        }

        totalFlow = (String) redisUtils.get(TOTAL_FLOW);
        if (totalFlow == null || "".equals(totalFlow)) {
            List<SysParams> totalFlowList = sysParamsMapper.listParamsByKeyForCampaign(TOTAL_FLOW);
            if (totalFlowList != null && totalFlowList.size() > 0) {
                SysParams totalFlowParams = totalFlowList.get(0);
                if (totalFlowParams != null) {
                    totalFlow = totalFlowParams.getParamValue();
                    redisUtils.set(TOTAL_FLOW, totalFlow);
                }
            }
        }

        if (evtParams.get("CPCP_JIFEI_CONTENT") != null) {
            List<Map<String, String>> contentMapList = (List<Map<String, String>>) evtParams.get("CPCP_JIFEI_CONTENT");
            for (int i = 0; i < contentMapList.size(); i++) {
                if (i > 0) {
                    content.append("，");
                }
                content.append(contentMapList.get(i).get("message_name").toString());
                content.append(usedFlow + contentMapList.get(i).get(USED_FLOW) + "，");
                content.append(totalFlow + contentMapList.get(i).get(TOTAL_FLOW));
            }
        }
        labelItems.put("CPCP_JIFEI_MESSAGE", content.toString());
    }

    /**
     * 判断资产号码是否在黑名单中
     *
     * @param accNbr 资产号码
     * @param type   活动类型
     * @return
     */
    private boolean checkBlackList(String accNbr, String type) {
        Object black_list = redisUtils.hget("BLACK_LIST", accNbr);
        if (black_list!=null){
            BlackListDO blackListDO = (BlackListDO) black_list;
            if (StatusCode.MARKETING_CAMPAIGN.getStatusCode().equals(type) && "1".equals(blackListDO.getMaketingCate())){
                log.info(accNbr + "该资产号码在营销类黑名单中，被过滤了");
                return true;
            }else if (!StatusCode.MARKETING_CAMPAIGN.getStatusCode().equals(type) && "1".equals(blackListDO.getServiceCate())){
                log.info(accNbr + "该资产号码在服务类黑名单中，被过滤了");
                return true;
            }
        }
        return false;
    }

}