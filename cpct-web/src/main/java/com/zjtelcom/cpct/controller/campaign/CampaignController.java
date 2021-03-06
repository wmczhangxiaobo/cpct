package com.zjtelcom.cpct.controller.campaign;

import com.alibaba.fastjson.JSON;
import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.controller.BaseController;
import com.zjtelcom.cpct.dao.campaign.MktCampaignMapper;
import com.zjtelcom.cpct.dao.channel.*;
import com.zjtelcom.cpct.dao.grouping.TarGrpConditionMapper;
import com.zjtelcom.cpct.domain.campaign.MktCampaignDO;
import com.zjtelcom.cpct.domain.channel.ObjCatItemRel;
import com.zjtelcom.cpct.domain.channel.ObjectLabelRel;
import com.zjtelcom.cpct.domain.channel.TopicLabelValue;
import com.zjtelcom.cpct.domain.strategy.MktStrategyConfRuleDO;
import com.zjtelcom.cpct.dto.campaign.MktCampaignDetailVO;
import com.zjtelcom.cpct.dto.pojo.Result;
import com.zjtelcom.cpct.dto.strategy.MktStrategyConfDetail;
import com.zjtelcom.cpct.dubbo.out.OpenApiScheService;
import com.zjtelcom.cpct.enums.StatusCode;
import com.zjtelcom.cpct.service.MqService;
import com.zjtelcom.cpct.service.campaign.MktCamResourceQRCodeService;
import com.zjtelcom.cpct.service.campaign.MktCampaignApiService;
import com.zjtelcom.cpct.service.campaign.MktCampaignService;
import com.zjtelcom.cpct.service.channel.CatalogService;
import com.zjtelcom.cpct.service.strategy.MktStrategyConfService;
import com.zjtelcom.cpct.service.thread.TarGrpRule;
import com.zjtelcom.cpct.util.MapUtil;
import com.zjtelcom.cpct.util.RedisUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;
import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;


@RestController
@RequestMapping("${adminPath}/campaign")
public class CampaignController extends BaseController {

    @Autowired
    private MktCampaignService mktCampaignService;

    @Autowired
    private MktStrategyConfService mktStrategyConfService;

    @Autowired
    private TarGrpConditionMapper tarGrpConditionMapper;

    @Autowired
    private InjectionLabelMapper injectionLabelMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MktCampaignApiService mktCampaignApiService;

    @Autowired
    private CatalogService catalogService;
    @Autowired(required = false)
    private OpenApiScheService openApiScheService;
    @Autowired
    private MktCampaignApiService apiService;

//    @Autowired
//    private MktCamResourceQRCodeService mktCamResourceQRCodeService;

    /**
     * 需求函类型限制活动类型校验
     * @param param
     * @return
     */
    @PostMapping(value = "/checkCampaignByRequestInfo")
    @CrossOrigin
    public Map<String, Object> checkCampaignByRequestInfo(@RequestBody  Map<String,Object> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            result =  mktCampaignService.checkCampaignByRequestInfo(param);
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "失败");
        }
        return result;
    }

    @RequestMapping(value = "openCampaignScheForDay", method = RequestMethod.POST)
    @CrossOrigin
    public String openCampaignScheForDay() {
        Map result = new HashMap();
        result = openApiScheService.openCampaignScheForMonth();
        return JSON.toJSON(result).toString();
    }


    /**
     * 营销活动目录
     *
     * @return
     */
    @PostMapping("/listCatalogItemTree")
    @CrossOrigin
    public Map<String, Object> listCatalogItemTree(){
        Map<String,Object> result = new HashMap<>();
        try {
            result = catalogService.listCatalogItemTree();
        } catch (Exception e) {
            logger.error("[op:CampaignController] fail to channelEffectDateCheck",e);
        }
        return result;
    }


    @Autowired
    private MktCampaignMapper mktCampaignMapper;
    @Autowired
    private ObjCatItemRelMapper objCatItemRelMapper;
    @Autowired
    private CatalogItemMapper catalogItemMapper;
    @Autowired
    private ObjectLabelRelMapper objectLabelRelMapper;
    @Autowired
    private TopicLabelMapper topicLabelMapper;
    @Autowired
    private LabelValueMapper labelValueMapper;



    @PostMapping("/topicLabel")
    @CrossOrigin
    public Map<String, Object> topicLabel(@RequestBody Map<String,Object> param){
        Map<String,Object> result = new HashMap<>();
        try {
            List<MktCampaignDO> allTheme = mktCampaignMapper.getAllTheme(param.get("theme").toString());
            if (!allTheme.isEmpty()) {
                for (MktCampaignDO mktCampaignDO : allTheme) {
                    List<ObjectLabelRel> objectLabelRels = objectLabelRelMapper.selectByObjId(mktCampaignDO.getMktCampaignId());
                    if (objectLabelRels.isEmpty()){
                        ObjectLabelRel aaa = new ObjectLabelRel();
                        aaa.setObjId(mktCampaignDO.getMktCampaignId());
                        aaa.setLabelId(613861134L);

                        TopicLabelValue id = labelValueMapper.selectByPrimaryKey(Long.valueOf(param.get("id").toString()));
                        if (id!=null){
                            aaa.setLabelValue(id.getLabelValue());
                        }
                        aaa.setLabelId(Long.valueOf(param.get("id").toString()));
                        aaa.setStatusCd("1000");
                        aaa.setObjType("1900");
                        aaa.setObjNbr(mktCampaignDO.getMktActivityNbr());
                        aaa.setCreateDate(new Date());
                        aaa.setStatusDate(new Date());
                        aaa.setUpdateDate(new Date());
                        objectLabelRelMapper.insert(aaa);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("[op:CampaignController] fail to channelEffectDateCheck",e);
        }
        return result;
    }



    @PostMapping("/selectByObjId")
    @CrossOrigin
    public Map<String, Object> selectByObjId(){
        Map<String,Object> result = new HashMap<>();
        try {
            List<MktCampaignDO> allTheme = mktCampaignMapper.getAllTheme("");
            if (!allTheme.isEmpty()) {
                for (MktCampaignDO mktCampaignDO : allTheme) {
                    Long aLong = catalogItemMapper.selectCatalogItemIdByCatalogItemDesc(mktCampaignDO.getTheMe());
                    if (aLong==null)
                    {
                        aLong = 614406331L;
                    }
                    final List<ObjCatItemRel> objCatItemRels = objCatItemRelMapper.selectByObjId(mktCampaignDO.getMktCampaignId());
                    if (objCatItemRels.isEmpty()){
                        ObjCatItemRel objCatItemRel = new ObjCatItemRel();
                        objCatItemRel.setObjId(mktCampaignDO.getMktCampaignId());
                        objCatItemRel.setCatalogItemId(aLong);
                        objCatItemRel.setStatusCd("1000");
                        objCatItemRel.setObjType("6000");
                        objCatItemRel.setObjNbr(mktCampaignDO.getMktActivityNbr());
                        objCatItemRel.setCreateDate(new Date());
                        objCatItemRel.setStatusDate(new Date());
                        objCatItemRel.setUpdateDate(new Date());
                        objCatItemRelMapper.insert(objCatItemRel);
                    }

                }
            }
        } catch (Exception e) {
            logger.error("[op:CampaignController] fail to channelEffectDateCheck",e);
        }
        return result;
    }








    /**
     * 校验协同渠道时间是否在活动时间范围之内
     *
     * @return
     */
    @PostMapping("/dataConfig")
    @CrossOrigin
    public Map<String, Object> dataConfig(@RequestBody Map<String,String> param){
        Map<String,Object> result = new HashMap<>();
        try {
            result = mktCampaignService.dataConfig(param);
        } catch (Exception e) {
            logger.error("[op:CampaignController] fail to channelEffectDateCheck",e);
        }
        return result;
    }



    /**
     * 校验协同渠道时间是否在活动时间范围之内
     *
     * @return
     */
    @PostMapping("/channelEffectDateCheck")
    @CrossOrigin
    public Map<String, Object> channelEffectDateCheck(@RequestBody Map<String,Object> params){
        Map<String,Object> result = new HashMap<>();
        try {
            result = mktCampaignService.channelEffectDateCheck(params);
        } catch (Exception e) {
            logger.error("[op:CampaignController] fail to channelEffectDateCheck",e);
            result.put("resultCode",CODE_SUCCESS);
            result.put("resultMsg","");
            result.put("data","true");
            return result;
        }
        return result;
    }


    /**
     * 同步活动列表--活动延期
     *
     * @return
     */
    @PostMapping("/delayCampaign4Sync")
    @CrossOrigin
    public Map<String, Object> delayCampaign4Sync(@RequestBody Map<String,Object> params){
        Map<String,Object> result = new HashMap<>();
        try {
            Long campaignId = Long.valueOf(params.get("campaignId").toString());
            Date lastTime = new Date(Long.valueOf(params.get("lastTime").toString()));
            result = mktCampaignService.delayCampaign4Sync(campaignId,lastTime);
        } catch (Exception e) {
            logger.error("[op:CampaignController] fail to delayCampaign4Sync",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg"," 延期失败！");
            return result;
        }
        return result;
    }

    /**
     * 同步活动列表--活动审核
     *
     * @return
     */
    @PostMapping("/examineCampaign4Sync")
    @CrossOrigin
    public Map<String, Object> examineCampaign4Sync(@RequestBody Map<String,Object> params){
        Map<String,Object> result = new HashMap<>();
        try {
            result = mktCampaignService.examineCampaign4Sync(Long.valueOf(params.get("campaignId").toString()),params.get("statusCd").toString());
        } catch (Exception e) {
            logger.error("[op:CampaignController] fail to examineCampaign4Sync",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","审核失败！");
            return result;
        }
        return result;
    }

    /**
     * 活动审核--同步列表
     * @return
     */
    @PostMapping("/getCampaignEndTime4Sync")
    @CrossOrigin
    public Map<String, Object> getCampaignEndTime4Sync(@RequestBody Map<String,Long> params){
        Map<String,Object> result = new HashMap<>();
        try {
            result = mktCampaignService.getCampaignEndTime4Sync(params.get("campaignId"));
        } catch (Exception e) {
            logger.error("[op:CampaignController] fail to getCampaignEndTime4Sync",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","获取活动结束时间失败！");
            return result;
        }
        return result;
    }

    /**
     * 同步活动列表(分页)
     *
     * @return
     */
    @PostMapping("/qryMktCampaignList4Sync")
    @CrossOrigin
    public String qryMktCampaignList4Sync(@RequestBody Map<String,Object> params) throws Exception {
        Integer page = MapUtil.getIntNum(params.get("page"));  // 页码
        Integer pageSize = MapUtil.getIntNum(params.get("pageSize")); // 条数
        Map<String, Object> map = mktCampaignService.qryMktCampaignList4Sync(params, page, pageSize);
        return JSON.toJSONString(map);
    }

    /**
     * 延期活动列表（只显示创建人为当前登录人的活动）
     */
    @PostMapping("/queryDelayCampaignList")
    @CrossOrigin
    public String queryDelayCampaignList() {
        Result result = new Result();
        try{
            result = mktCampaignService.queryDelayCampaignList();
        }catch(Exception e){
            result.setResultCode("500");
            result.setResultMessage(e.toString());
            logger.error("[op:CampaignController] fail to queryDelayCampaignList",e);
        }
        return JSON.toJSONString(result);
    }


    /**
     * 查询活动列表(分页，活动总览)
     *
     * @return
     */
    @RequestMapping(value = "/listCampaignPage", method = RequestMethod.POST)
    @CrossOrigin
    public String qryMktCampaignList(@RequestBody Map<String, Object> params) throws Exception {
        logger.info("进入活动总览-qryMktCampaignList");
        Map<String, Object> map = null;
        try {
            map = mktCampaignService.qryMktCampaignListPage(params);
        } catch (Exception e) {
            logger.info("[op:CampaignController] failed to listCampaignPage , Expection = ", e);
            map.put("resultCode", CommonConstant.CODE_FAIL);
            map.put("resultMsg", "查询活动列表失败！");
        }
        return JSON.toJSONString(map);
    }


    /**
     * 查询活动列表(分页，活动总览，不是发布和调整中的活动)
     *
     * @return
     */
    @RequestMapping(value = "/listCampaignPageForNoPublish", method = RequestMethod.POST)
    @CrossOrigin
    public String qryMktCampaignListForNoPublish(@RequestBody Map<String, Object> params) throws Exception {
        Map<String, Object> map = null;
        try {
            map = mktCampaignService.qryMktCampaignListPageForNoPublish(params);
        } catch (Exception e) {
            logger.info("[op:CampaignController] failed to listCampaignPage , Expection = ", e);
            map.put("resultCode", CommonConstant.CODE_FAIL);
            map.put("resultMsg", "查询活动列表失败！");
        }
        return JSON.toJSONString(map);
    }


    /**
     * 查询活动列表(分页，活动总览，发布和调整中的活动)
     *
     * @return
     */
    @RequestMapping(value = "/listCampaignPageForPublish", method = RequestMethod.POST)
    @CrossOrigin
    public String qryMktCampaignListForPublish(@RequestBody Map<String, Object> params) throws Exception {
        Map<String, Object> map = null;
        try {
            map = mktCampaignService.qryMktCampaignListPageForPublish(params);
        } catch (Exception e) {
            logger.info("[op:CampaignController] failed to listCampaignPage , Expection = ", e);
            map.put("resultCode", CommonConstant.CODE_FAIL);
            map.put("resultMsg", "查询活动列表失败！");
        }
        return JSON.toJSONString(map);
    }

    /**
     * 获取活动列表
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getCampaignList", method = RequestMethod.POST)
    @CrossOrigin
    public String getCampaignList(@RequestBody Map<String, Object> params) throws Exception {
        String mktCampaignName = params.get("mktCampaignName").toString();  // 活动名称
        Long eventId = null;
        String mktCampaignType = null;
        if (params.get("eventId") != null) {
            eventId = Long.valueOf(params.get("eventId").toString());
        }
        if (params.get("mktCampaignType")!=null && !params.get("mktCampaignType").equals("")){
            mktCampaignType = params.get("mktCampaignType").toString(); // 活动
        }
        Map<String, Object> map = mktCampaignService.getCampaignList(mktCampaignName, mktCampaignType, eventId);
        return JSON.toJSONString(map);
    }

    @RequestMapping(value = "/getCampaignList4EventScene", method = RequestMethod.POST)
    @CrossOrigin
    public String getCampaignList4EventScene(@RequestBody Map<String, String> params) throws Exception {
        String mktCampaignName = params.get("mktCampaignName");  // 活动名称
        Map<String, Object> map = mktCampaignService.getCampaignList4EventScene(mktCampaignName);
        return JSON.toJSONString(map);
    }

    /**
     * 新增营销活动
     *
     * @param mktCampaignVO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createMktCampaign", method = RequestMethod.POST)
    @CrossOrigin
    public String createMktCampaign(@RequestBody MktCampaignDetailVO mktCampaignVO) throws Exception {
        logger.info("[op:createMktCampaign] mktCampaignVO = " + JSON.toJSONString(mktCampaignVO));
        // 存活动
        Map<String, Object> mktCampaignMap = mktCampaignService.createMktCampaign(mktCampaignVO);
        if (mktCampaignMap.get("mktCampaignId")==null) return JSON.toJSONString(mktCampaignMap);
        Long mktCampaignId = Long.valueOf(mktCampaignMap.get("mktCampaignId").toString());
        if (mktCampaignVO.getMktStrategyConfDetailList().size() > 0) {
            for (MktStrategyConfDetail mktStrategyConfDetail : mktCampaignVO.getMktStrategyConfDetailList()) {
                mktStrategyConfDetail.setMktCampaignId(mktCampaignId);
                mktStrategyConfDetail.setMktCampaignName(mktCampaignVO.getMktCampaignName());
                mktStrategyConfDetail.setMktCampaignType(mktCampaignVO.getMktCampaignType());
                mktStrategyConfService.saveMktStrategyConf(mktStrategyConfDetail);
            }
        }
        // 刷活动报表数据
        mktCampaignService.campaignConfig(Long.valueOf(mktCampaignId));
        return JSON.toJSONString(mktCampaignMap);
    }

    /**
     * 修改营销活动
     *
     * @param mktCampaignVO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/modMktCampaign", method = RequestMethod.POST)
    @CrossOrigin
    public String modMktCampaign(@RequestBody MktCampaignDetailVO mktCampaignVO) throws Exception {
        Map<String, Object> mktCampaignMap = mktCampaignService.modMktCampaign(mktCampaignVO);
        return JSON.toJSONString(mktCampaignMap);
    }


    /**
     * 查询营销活动
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getMktCampaign", method = RequestMethod.POST)
    @CrossOrigin
    public String getMktCampaign(@RequestBody Map<String, String> params) throws Exception {
        Long mktCampaignId = Long.valueOf(params.get("mktCampaignId"));
        Map<String, Object> map = new HashMap<>();
        try {
            map = mktCampaignService.getMktCampaign(mktCampaignId);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("resultCode", CommonConstant.CODE_FAIL);
            map.put("resultMsg", "查询活动失败");
        }
        return JSON.toJSONString(map);
    }


    /**
     * 根据活动Id查询策略和规则
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getAllConfRuleName", method = RequestMethod.POST)
    @CrossOrigin
    public String getAllConfRuleName(@RequestBody Map<String, String> params) throws Exception{
        Long mktCampaignId = Long.valueOf(params.get("mktCampaignId"));
        Map<String, Object> map = null;
        try {
            map = mktCampaignService.getAllConfRuleName(mktCampaignId);
        } catch (Exception e) {
            map.put("resultCode", CommonConstant.CODE_FAIL);
            map.put("resultMsg", "查询活动失败");
        }
        return JSON.toJSONString(map);
    }


    /**
     * 删除营销活动
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/delMktCampaign", method = RequestMethod.POST)
    @CrossOrigin
    public String delMktCampaign(@RequestBody Map<String, String> params) throws Exception {
        Long mktCampaignId = Long.valueOf(params.get("mktCampaignId"));
        Map<String, Object> map = null;
        try {
            map = mktCampaignService.delMktCampaign(mktCampaignId);
        } catch (Exception e) {
            map.put("resultCode", CommonConstant.CODE_FAIL);
            map.put("resultMsg", "删除活动失败");
        }
        return JSON.toJSONString(map);
    }

    /**
     * 更改营销活动转态
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/changeMktCampaignStatus", method = RequestMethod.POST)
    @CrossOrigin
    public String changeMktCampaignStatus(@RequestBody Map<String, String> params) throws Exception {
        Long mktCampaignId = Long.valueOf(params.get("mktCampaignId"));
        String statusCd = params.get("statusCd");
        Map<String, Object> map = mktCampaignService.changeMktCampaignStatus(mktCampaignId, statusCd);
        return JSON.toJSONString(map);
    }


    @RequestMapping(value = "/test", method = RequestMethod.POST)
    @CrossOrigin
    public String test(@RequestBody Map<String, String> params) throws Exception {
        Long tarGrpId = Long.valueOf(params.get("tarGrpId"));
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 1; i < 3; i++) {
            MktStrategyConfRuleDO mktStrategyConfRuleDO = new MktStrategyConfRuleDO();
            mktStrategyConfRuleDO.setMktStrategyConfRuleId(Long.valueOf(i));
            mktStrategyConfRuleDO.setTarGrpId(null);
            // 线程池执行规则存入redis
            executorService.submit(new TarGrpRule(Long.valueOf(i), Long.valueOf(i), mktStrategyConfRuleDO, redisUtils, tarGrpConditionMapper, injectionLabelMapper));
        }
        return null;
    }

    /**
     * 发布并下发活动
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/publishMktCampaign", method = RequestMethod.POST)
    @CrossOrigin
    public String publishMktCampaign(@RequestBody Map<String, String> params) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Long parentMktCampaignId = Long.valueOf(params.get("mktCampaignId"));
        String statusCd = params.get("statusCd");
        try {
            Map<String, Object> mktCampaignMap = mktCampaignService.publishMktCampaign(parentMktCampaignId);
            map = mktCampaignService.changeMktCampaignStatus(parentMktCampaignId, statusCd);
        } catch (Exception e) {
            logger.error("[op:publishMktCampaign] 发布活动mktCampaignId = {}, statusCd ={}失败！Exception =  ", parentMktCampaignId, statusCd, e);
        }
        return JSON.toJSONString(map);
    }

    /**
     * 升级活动
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/upgradeMktCampaign", method = RequestMethod.POST)
    @CrossOrigin
    public String upgradeMktCampaign(@RequestBody Map<String, String> params) throws Exception {
        Long parentMktCampaignId = Long.valueOf(params.get("mktCampaignId"));
        Map<String, Object> mktCampaignMap = mktCampaignService.upgradeMktCampaign(parentMktCampaignId);
        //   mktCampaignService.changeMktCampaignStatus(parentMktCampaignId, StatusCode.STATUS_CODE_ROLL.getStatusCode());
        return JSON.toJSONString(mktCampaignMap);
    }


    /**
     * 获取活动模板
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getMktCampaignTemplate", method = RequestMethod.POST)
    @CrossOrigin
    public String getMktCampaignTemplate(@RequestBody Map<String, String> params) throws Exception {
        Long parentMktCampaignId = Long.valueOf(params.get("mktCampaignId"));
        Map<String, Object> mktCampaignMap = null;
        try {
            mktCampaignMap = mktCampaignService.getMktCampaignTemplate(parentMktCampaignId);
            mktCampaignMap.put("resultCode", CommonConstant.CODE_SUCCESS);
        } catch (Exception e) {
            logger.error("[op:CampaignController] failed to getMktCampaignTemplate by parentMktCampaignId = {}, Exception = ", parentMktCampaignId, e);
            mktCampaignMap.put("resultCode", CommonConstant.CODE_FAIL);
        }
        return JSON.toJSONString(mktCampaignMap);
    }

    /**
     * 获取活动模板
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getMktCampaignTemplate4CamResource", method = RequestMethod.POST)
    @CrossOrigin
    public String getMktCampaignTemplate4CamResource(@RequestBody Map<String, String> params) throws Exception {
        Long parentMktCampaignId = Long.valueOf(params.get("mktCampaignId"));
        Map<String, Object> mktCampaignMap = null;
        try {
            mktCampaignMap = mktCampaignService.getMktCampaignTemplate4CamResource(parentMktCampaignId);
            mktCampaignMap.put("resultCode", CommonConstant.CODE_SUCCESS);
        } catch (Exception e) {
            logger.error("[op:CampaignController] failed to getMktCampaignTemplate by getMktCampaignTemplate4CamResource = {}, Exception = ", parentMktCampaignId, e);
            mktCampaignMap.put("resultCode", CommonConstant.CODE_FAIL);
        }
        return JSON.toJSONString(mktCampaignMap);
    }






    /**
     * 调整活动
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/adjustMktCampaign", method = RequestMethod.POST)
    @CrossOrigin
    public String adjustMktCampaign(@RequestBody Map<String, String> params) throws Exception {
        Long parentMktCampaignId = Long.valueOf(params.get("mktCampaignId"));
        Map<String, Object> mktCampaignMap = new HashMap<>();
        try {
            // 修改源活动状态
            mktCampaignService.changeMktCampaignStatus(parentMktCampaignId, StatusCode.STATUS_CODE_ADJUST.getStatusCode());
            // 复制活动
            mktCampaignMap = mktCampaignService.copyMktCampaign(parentMktCampaignId);
            mktCampaignMap.put("resultCode", CommonConstant.CODE_SUCCESS);
        } catch (Exception e) {
            logger.error("[op:CampaignController] failed to adjustMktCampaign by parentMktCampaignId = {}, Exception = ", parentMktCampaignId, e);
            mktCampaignMap.put("resultCode", CommonConstant.CODE_FAIL);
        }
        return JSON.toJSONString(mktCampaignMap);
    }


    /**
     * 过期活动
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/dueMktCampaign", method = RequestMethod.POST)
    @CrossOrigin
    public String dueMktCampaign() throws Exception {
        Map<String, Object> mktCampaignMap = new HashMap<>();
        try {
            // 过期活动
            mktCampaignMap = mktCampaignService.dueMktCampaign();
        } catch (Exception e) {
            logger.error("[op:CampaignController] failed to dueMktCampaign, Exception = ", e);
        }
        return JSON.toJSONString(mktCampaignMap);
    }


    /**
     * 统计活动报表
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/countMktCampaign", method = RequestMethod.POST)
    @CrossOrigin
    public String countdueMktCampaign(@RequestBody Map<String, Object> params) throws Exception {
        Map<String, Object> mktCampaignMap = new HashMap<>();
        try {
            mktCampaignMap = mktCampaignService.countMktCampaign(params);
        } catch (Exception e) {
            logger.error("[op:CampaignController] failed to dueMktCampaign, Exception = ", e);
        }
        return JSON.toJSONString(mktCampaignMap);
    }

    @PostMapping("salesOffShelf")
    @CrossOrigin
    public Map<String,Object> salesOffShelf(){
        Map<String, Object> stringObjectMap = mktCampaignApiService.salesOffShelf(new HashMap<String, Object>());
        return stringObjectMap;
    }

    /**
     * 集团活动不承接反馈接口
     */
    @RequestMapping(value = "/mktCampaignJtRefuse", method = RequestMethod.POST)
    @CrossOrigin
    public Map<String, Object> mktCampaignJtRefuse(Long mktCampaignId) {
        Map<String, Object> result = new HashMap<>();
        result = mktCampaignService.mktCampaignJtRefuse(mktCampaignId);
        return result;
    }


    @RequestMapping(value = "/searchBatch", method = RequestMethod.POST)
    @CrossOrigin
    public Map<String, Object> searchBatch(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        List<Long> mktCampaignIdList = (List<Long>) params.get("mktCampaignIdList");
        result = mktCampaignService.searchBatch(mktCampaignIdList);
        return result;
    }

    @PostMapping(value = "/acceptGroupCampaign")
    @CrossOrigin
    public Map<String, Object> acceptGroupCampaign(@RequestBody MktCampaignDO mktCampaignDO) {
        Map<String, Object> result = new HashMap<>();
        try {
            mktCampaignService.acceptGroupCampaign(mktCampaignDO);
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg", "成功");
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "失败");
        }
        return result;
    }

    @PostMapping(value = "/notAcceptGroupCampaign")
    @CrossOrigin
    public Map<String, Object> notAcceptGroupCampaign(@RequestBody MktCampaignDO mktCampaignDO) {
        Map<String, Object> result = new HashMap<>();
        try {
            mktCampaignService.notAcceptGroupCampaign(mktCampaignDO);
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg", "成功");
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "失败");
        }
        return result;
    }


    @PostMapping(value = "/getByC3AndAuto")
    @CrossOrigin
    public Map<String, Object> getByC3AndAuto(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            result = mktCampaignService.getByC3AndAuto(params);
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg", "成功");
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "失败");
        }
        return result;
    }


    /*批量活动活动派单过扰，c3级校验是否超过5个*/
    @PostMapping(value = "/isOpenDisturb")
    @CrossOrigin
    public Map<String, Object> isOpenDisturb(@RequestBody MktCampaignDO mktCampaignDO) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean isOverFive = mktCampaignService.isOpenDisturb(mktCampaignDO);
            if(isOverFive == false){
                result.put("info", "当前地市过扰活动达到阈值，禁止开启过扰");
            }
            result.put("isOverFive",isOverFive);
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg", "成功");
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "失败");
        }
        return result;
    }

    @PostMapping(value = "/redisTest")
    @CrossOrigin
    public Map<String, Object> redisTest(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String key = (String) params.get("key");
            Long id = ((Integer) params.get("id")).longValue();
            mktCampaignService.redisTest(key, id);
            result.put("resultCode", CODE_SUCCESS);
            result.put("resultMsg", "成功");
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "失败");
        }
        return result;
    }

    @PostMapping(value = "/updateStaffById")
    @CrossOrigin
    public Map<String, Object> updateStaffById(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            return mktCampaignService.updateStaffById(params);
        }catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    /*
    *生成二维码url，如果存在则直接获取
     */
    @PostMapping(value = "/generatePoster")
    @CrossOrigin
    public Map<String, Object> generatePoster(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            //data = mktCamResourceQRCodeService.generatePoster(params);
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "失败");
            result.put("data",data);
            return  result;
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "成功");
        result.put("data",data);
        return result;
    }

    /*
     *保存海报url到渠道属性表
     */
    @PostMapping(value = "/savePostUrl")
    @CrossOrigin
    public Map<String, Object> savePostToChanlAttr(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            //data = mktCamResourceQRCodeService.savePostUrl(params);
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "保存失败");
            return  result;
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "保存成功");
        result.put("data",data);
        return result;
    }

    /*
     *上传海报背景图
     */
    @PostMapping(value = "/savePostBackgroundUrl")
    @CrossOrigin
    public Map<String, Object> savePostBackgroundUrl(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            //data = mktCamResourceQRCodeService.savePostBackgroundUrl(params);
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "保存失败");
            return  result;
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "保存成功");
        result.put("data",data);
        return result;
    }

    /*
     * 海报背景图分页查询
     */
    @PostMapping(value = "/getPostgroundPathPage")
    @CrossOrigin
    public Map<String, Object> getPostgroundPathPage(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            //data = mktCamResourceQRCodeService.getPostgroundPathPage(params);
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "获取失败");
            return  result;
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "获取成功");
        result.put("data",data);
        return result;
    }

    /*
     * 根据规则id获取海报
     */
    @PostMapping(value = "/getPostUrlByRuleId")
    @CrossOrigin
    public Map<String, Object> getPostUrlByRuleId(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
//            data = mktCamResourceQRCodeService.getPostUrlByRuleId(params);
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "获取失败");
            return  result;
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "获取成功");
        result.put("data",data);
        return result;
    }
    /*
     * 根据需求函类型获取审批人
     */
    @PostMapping(value = "/getStaffByMktRequest")
    @CrossOrigin
    public Map<String, Object> getStaffByMktRequest(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            data = mktCampaignService.getStaffByMktRequest(params);
        }catch (Exception e) {
            e.printStackTrace();
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", "获取失败");
            return  result;
        }
        result.put("resultCode", CODE_SUCCESS);
        result.put("resultMsg", "获取成功");
        result.put("data",data);
        return result;
    }



}
