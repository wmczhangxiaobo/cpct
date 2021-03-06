package com.zjtelcom.cpct.service.impl.report;

import com.alibaba.fastjson.JSON;
import com.ctzj.smt.bss.cooperate.service.dubbo.IReportService;
import com.zjtelcom.cpct.dao.campaign.MktCampaignMapper;
import com.zjtelcom.cpct.dao.channel.CatalogItemMapper;
import com.zjtelcom.cpct.dao.channel.ContactChannelMapper;
import com.zjtelcom.cpct.dao.channel.OrganizationMapper;
import com.zjtelcom.cpct.dao.system.SysAreaMapper;
import com.zjtelcom.cpct.dao.system.SysParamsMapper;
import com.zjtelcom.cpct.domain.SysArea;
import com.zjtelcom.cpct.domain.campaign.MktCampaignDO;
import com.zjtelcom.cpct.domain.channel.CatalogItem;
import com.zjtelcom.cpct.domain.channel.Channel;
import com.zjtelcom.cpct.domain.channel.Organization;
import com.zjtelcom.cpct.domain.system.SysParams;
import com.zjtelcom.cpct.enums.AreaCodeEnum;
import com.zjtelcom.cpct.enums.AreaNameEnum;
import com.zjtelcom.cpct.enums.OrgEnum;
import com.zjtelcom.cpct.service.report.XinNewAactivityService;
import com.zjtelcom.cpct.util.AcitvityParams;
import com.zjtelcom.cpct.util.ChannelUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;
import static com.zjtelcom.cpct.service.impl.report.ActivityStatisticsServiceImpl.getPercentFormat;

@Service
@Transactional
public class XinNewAactivityServiceImpl implements XinNewAactivityService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(XinNewAactivityServiceImpl.class);

    @Autowired(required = false)
    private IReportService iReportService;
    @Autowired
    private SysParamsMapper sysParamsMapper;
    @Autowired
    private MktCampaignMapper mktCampaignMapper;
    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private ContactChannelMapper contactChannelMapper;
    @Autowired
    private ContactChannelMapper channelMapper;
    @Autowired
    private SysAreaMapper sysAreaMapper;
    @Autowired
    private CatalogItemMapper catalogItemMapper;

    /**
     * 活动匹配地市
     * @param campaignDO
     * @return
     */
    private String getArea(MktCampaignDO campaignDO){
        String result = "";
        String regionFlg = campaignDO.getRegionFlg();
        if (StringUtils.isNotBlank(regionFlg)) {
            String sysArea = AreaCodeEnum.getSysAreaNameBySysArea(campaignDO.getRegionFlg());
            if (regionFlg.equals("C2")) {
                result = sysArea;
            }
            if (regionFlg.equals("C3")) {
                String name = AreaNameEnum.getNameByLandId(campaignDO.getLanId());
                result = sysArea + "-" + name;
            }
            if (regionFlg.equals("C4")) {
                if (campaignDO.getLanIdFour()== null || campaignDO.getLanIdFour().toString().length()>6){
                    result = sysArea;
                }else {
                    SysArea area = sysAreaMapper.selectByPrimaryKey(Integer.valueOf(campaignDO.getLanIdFour().toString()));
                    String name = area == null ? "" : area.getName();
                    result = sysArea + "-" + name;
                }
            }

            if (regionFlg.equals("C5")) {
                Organization organization = organizationMapper.selectByPrimaryKey(campaignDO.getLanIdFive());
                String name = organization == null ? "" : organization.getOrgName();
                result = sysArea + "-" + name;
            }
            return result;
        }
        return result;
    }




    /**
     *  新活动报表 主题活动
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> activityTheme(Map<String, Object> params) {
        HashMap<String, Object> result = new HashMap<>();
        Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
        if (paramMapToSuccess(result, paramMap)) return result;
        //  List<Map<String, String>> campaignTheme = sysParamsMapper.listParamsByKey("CAMPAIGN_THEME");
        // 查询二级目录
        List<CatalogItem> catalogItemList = catalogItemMapper.selectChild();
        String startDate = params.get("startDate").toString();
        String endDate = params.get("endDate").toString();
        String type = paramMap.get("mktCampaignType").toString();
        String lanId = "";
        String regionFlg = "";
        if (params.get("orglevel1").toString().equals("800000000004")){

        }else if (params.get("orglevel1").toString().equals("1")){
            lanId = "";
            regionFlg = "C2";

        }else {
            lanId = ChannelUtil.getAreaByOrg(params.get("orglevel1").toString());
            regionFlg = "C3";
        }
        //总数
        Integer count = mktCampaignMapper.getCountFromActivityThemeByC3(startDate,type,endDate,lanId,regionFlg);
        log.info("【count】："+JSON.toJSONString(count));
        List<Map<String,Object>> dataMap = new ArrayList<>();
        if (catalogItemList != null && catalogItemList.size() > 0) {
            //主题列表
            for (CatalogItem catalogItem : catalogItemList) {
                Map<String,Object> themeMap = new HashMap<>();
                //每个主题个数
                List<MktCampaignDO> mktCampaignList = mktCampaignMapper.selectCampaignThemeByC3(catalogItem.getCatalogItemId(), startDate,endDate ,type, lanId,regionFlg);
                String substring = null;
                StringBuilder stringBuilder = new StringBuilder();
                if (mktCampaignList!=null && mktCampaignList.size()>0){
                    for (MktCampaignDO mktCampaignDO : mktCampaignList) {
                        stringBuilder.append(mktCampaignDO.getInitId()).append(",");
                    }
                    //多个id  “，”拼接 去除最后的一个 ，
                    substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                }
                if (substring == null){
                    continue;
                }
                paramMap.put("mktCampaignId", substring);
                //维度 按活动
                paramMap.put("rptType", 2);
                paramMap.put("pageSize","5");
                paramMap.put("sortColumn","contactRate");
                //按转换率排序去前五 top5
                log.info("【入参】新活动报表 主题活动 按转换率排序去前五 top5:："+JSON.toJSONString(paramMap));
                Map<String, Object> stringObjectMap = iReportService.queryRptOrder(paramMap);
                log.info("新活动报表 主题活动 按转换率排序去前五 top5:"+JSON.toJSONString(stringObjectMap));
                List<Map<String,Object>> rptList = (List<Map<String,Object>>) stringObjectMap.get("rptOrderList");
                for (Map<String, Object> stringMap : rptList) {
                    for (MktCampaignDO campaignDO : mktCampaignList) {
                        if (campaignDO.getInitId().toString().equals(stringMap.get("mktCampaignId").toString())){
                            stringMap.put("mktCampaignName",campaignDO.getMktCampaignName());
                            log.info("【活动region】:"+JSON.toJSONString(campaignDO));
                            stringMap.put("area", getArea(campaignDO));
                        }
                    }
                    //转换率百分比
                    Object contactRate = stringMap.get("contactRate");
                    log.info("contactRate!@#$%:"+contactRate);
                    if (contactRate!=null && contactRate!=""){
                        if (contactRate.toString().contains("%") && !contactRate.toString().equals("00.00%")){
                            stringMap.put("contactRate",contactRate);
                        }else if (contactRate.toString().equals("00.00%") || contactRate.toString().equals("1")){
                            stringMap.put("contactRate","100%");
                        }else {
                            stringMap.put("contactRate",getPercentFormat(Double.valueOf(contactRate.toString()),2,2));
                        }
                    }else {
                        stringMap.put("contactRate","0%");
                    }
                    Object revenueReduceRate = stringMap.get("revenueReduceRate");
                    if (revenueReduceRate!=null && revenueReduceRate!=""){
                        if (revenueReduceRate.toString().contains("%")){
                            stringMap.put("revenueReduceRate",revenueReduceRate);
                        }else {
                            stringMap.put("revenueReduceRate",getPercentFormat(Double.valueOf(revenueReduceRate.toString()),2,2));
                        }
                    }else {
                        stringMap.put("revenueReduceRate","0%");
                    }
                }

                //按收入提高排序取前五
                paramMap.put("sortColumn","incomeUp");
                log.info("【入参】新活动报表 主题活动 按收入提高排序取前五 top5:："+JSON.toJSONString(paramMap));
                Map<String, Object> stringObjectMap1 = iReportService.queryRptOrder(paramMap);
                log.info("新活动报表 主题活动 按收入提高排序取前五:"+JSON.toJSONString(stringObjectMap1));
                List<Map<String,Object>> rptList2 = (List<Map<String,Object>>) stringObjectMap1.get("rptOrderList");
                for (Map<String, Object> stringMap : rptList2) {
                    for (MktCampaignDO campaignDO : mktCampaignList) {
                        if (campaignDO.getInitId().toString().equals(stringMap.get("mktCampaignId").toString())){
                            stringMap.put("mktCampaignName",campaignDO.getMktCampaignName());
                            stringMap.put("area", getArea(campaignDO));
                        }
                    }
                    //转换率百分比
                    Object contactRate = stringMap.get("contactRate");
                    if (contactRate!=null && contactRate!=""){
                        if (contactRate.toString().contains("%")){
                            stringMap.put("contactRate",contactRate);
                        }else if (contactRate.toString().equals("1")){
                            stringMap.put("contactRate","100%");
                        }else {
                            String string = contactRate.toString();
                            log.info("转换率百分比数值："+string);
                            Double aDouble = Double.valueOf(contactRate.toString());
                            log.info("转换率百分比异常查看："+aDouble);
                            stringMap.put("contactRate",getPercentFormat(Double.valueOf(contactRate.toString()),2,2));
                        }
                    }else {
                        stringMap.put("contactRate","0%");
                    }
                    Object revenueReduceRate = stringMap.get("revenueReduceRate");
                    if (revenueReduceRate!=null && revenueReduceRate!=""){
                        if (revenueReduceRate.toString().contains("%")){
                            stringMap.put("revenueReduceRate",revenueReduceRate);
                        }else {
                            stringMap.put("revenueReduceRate",getPercentFormat(Double.valueOf(revenueReduceRate.toString()),2,2));
                        }
                    }else {
                        stringMap.put("revenueReduceRate","0%");
                    }
                }
                //主题百分比
                double num = (double) mktCampaignList.size() / (double)count;
                //返回拼装
                themeMap.put("name", catalogItem.getCatalogItemName());
                themeMap.put("number",mktCampaignList.size());
                themeMap.put("value",getPercentFormat(Double.valueOf(num), 2, 2));
                themeMap.put("conversionList",rptList);
                themeMap.put("incomeList",rptList2);
                dataMap.add(themeMap);
            }
        }
        result.put("code","0000");
        result.put("message","成功");
        result.put("data",dataMap);
        return result;
    }


//    /**
//     * 客触数
//     * @param params
//     * @return
//     */
//    @Override
//    public Map<String, Object> contactNumber(Map<String, Object> params) {
//        Map<String,Object> result = new HashMap<>();
//        Map<String,Object> dataMap = new HashMap<>();
//
//        Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
//        //统计维度(0:按渠道，1按地市) 不用就不传
//        paramMap.put("rptType","1");
//        //按客触数排序
//        paramMap.put("sortColumn","contactNum");
//        //查询总数 解析
//        log.info("【入参】新活动报表 客触数 ："+JSON.toJSONString(paramMap));
//        Map<String,Object> stringOMap = iReportService.queryRptOrder(paramMap);
//        log.info("【出参】新活动报表 客触数 ："+JSON.toJSONString(stringOMap));
//        List<Map<String,Object>> rptList = (List<Map<String,Object>>) stringOMap.get("rptOrderList");
//        if (rptList.size()!=1 ){
//            result.put("code","0001");
//            result.put("message","报表查询失败");
//            return result;
//        }
//        dataMap.put("contactNum",rptList.get(0).get("contactNum"));
//        dataMap.put("successNum",rptList.get(0).get("orderSuccessNum"));
//        dataMap.put("contactRate",getPercentFormat(Double.valueOf(rptList.get(0).get("contactRate").toString()),2,2));
//        //地市(ALL表示所有,多个用逗号隔开) 添加11个地市的orgid
//        paramMap.put("orglevel2",OrgEnum.getNameByOrgId());
//        //查询地市排名
//        log.info("【入参】新活动报表 客触数  查询地市排名："+JSON.toJSONString(paramMap));
//        Map<String,Object> orgMapRes = iReportService.queryRptOrder(paramMap);
//        log.info("【出参】新活动报表 客触数 查询地市排名:"+JSON.toJSONString(orgMapRes));
//        List<Map<String,Object>> orgList = (List<Map<String,Object>>) orgMapRes.get("rptOrderList");
//        for (Map<String, Object> orgMap : orgList) {
//            orgMap.put("name",OrgEnum.getNameByOrgId(Long.valueOf(orgMap.get("orgId").toString())));
//            if (orgMap.get("contactRate")!=null &&  orgMap.get("contactRate").toString().equals("") && orgMap.get("contactRate")!="null"){
//                orgMap.put("contactRate",getPercentFormat(Double.valueOf(orgMap.get("contactRate").toString()),2,2));
//            }else {
//                orgMap.put("contactRate","0%");
//            }
//            //没一个C2下的C3地市数据
//            paramMap.put("orglevel2",orgMap.get("orgId").toString());
//            paramMap.put("orglevel3","all");
//            //查询地市排名
//            log.info("【入参】新活动报表 客触数  C3查询地市排名："+JSON.toJSONString(paramMap));
//            Map<String,Object> orglevel3List = iReportService.queryRptOrder(paramMap);
//            log.info("【出参】新活动报表 客触数 C3查询地市排名:"+JSON.toJSONString(orglevel3List));
//            List<Map<String,Object>> orgC3List = (List<Map<String,Object>>) orglevel3List.get("rptOrderList");
//            for (Map<String, Object> c3Map : orgC3List) {
//                Organization orgId = organizationMapper.selectByPrimaryKey(Long.valueOf(c3Map.get("orgId").toString()));
//                //C3 地区名称
//                c3Map.put("name",orgId.getOrgName().substring(0,2));
//            }
//            orgMap.put("orgC3List",orgC3List);
//        }
//        //按渠道排序
//        paramMap.put("rptType","0");
//        paramMap.put("sortColumn","channel");
//        //查询渠道排序
//        log.info("【入参】新活动报表 客触数  查询渠道排序："+JSON.toJSONString(paramMap));
//        Map<String, Object> channelMapRes = iReportService.queryRptOrder(paramMap);
//        log.info("【出参】新活动报表 客触数 查询渠道排序:"+JSON.toJSONString(channelMapRes));
//        List<Map<String,Object>> channelList = (List<Map<String,Object>>) channelMapRes.get("rptOrderList");
//        for (Map<String, Object> channelMap : channelList) {
//            Channel channel = channelMapper.selectByCode(channelMap.get("channel").toString());
//            channelMap.put("name",channel==null ? "" : channel.getContactChlName());
//            //revenueReduceNum
//            Object revenueReduceNum = channelMap.get("revenueReduceNum");
//            if (revenueReduceNum==null || ""==revenueReduceNum){
//                channelMap.put("revenueReduceNum",0);
//            }
//            if (channelMap.get("contactRate")!=null && channelMap.get("contactRate").toString().equals("") && channelMap.get("contactRate")!="null"){
//                channelMap.put("contactRate",getPercentFormat(Double.valueOf(channelMap.get("contactRate").toString()),2,2));
//            }else {
//                channelMap.put("contactRate","0%");
//            }
//        }
//        dataMap.put("areaList",orgList);
//        dataMap.put("channelList",channelList);
//        result.put("code","0000");
//        result.put("message","成功");
//        result.put("data",dataMap);
//        return result;
//    }

    /**
     * 11/15 修改后的 客触数
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> contactNumber(Map<String, Object> params) {
        Map<String,Object> result = new HashMap<>();
        Map<String,Object> dataMap = new HashMap<>();

        Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
        if (paramMapToSuccess(result, paramMap)) return result;
        //统计维度(0:按渠道，1按地市) 不用就不传
        paramMap.put("rptType","1");
        //按客触数排序
        paramMap.put("sortColumn","contactNum");
        //查询总数 解析
        log.info("【入参】新活动报表 客触数 ："+JSON.toJSONString(paramMap));
        Map<String,Object> stringOMap = iReportService.queryRptOrder(paramMap);
        log.info("【出参】新活动报表 客触数 ："+JSON.toJSONString(stringOMap));
        List<Map<String,Object>> rptList = (List<Map<String,Object>>) stringOMap.get("rptOrderList");
        if (rptList.size()!=1 ){
            result.put("code","0001");
            result.put("message","报表查询失败");
            return result;
        }
        dataMap.put("contactNum",rptList.get(0).get("contactNum"));
        dataMap.put("orderNum",rptList.get(0).get("orderNum")); //总商机数
        dataMap.put("successNum",rptList.get(0).get("orderSuccessNum"));
        dataMap.put("contactRate",getPercentFormat(Double.valueOf(rptList.get(0).get("contactRate").toString()),2,2));

        //权限控制C2 / C3
        if (params.get("orglevel1").toString().equals("800000000004")) {
            paramMap.put("orglevel2","all");
        }else {
            paramMap.put("orglevel3","all");
        }
        paramMap.put("pageSize","11");
        //查询地市排名
        log.info("【入参】新活动报表 客触数  查询地市排名："+JSON.toJSONString(paramMap));
        Map<String,Object> orgMapRes = iReportService.queryRptOrder(paramMap);
        log.info("【出参】新活动报表 客触数 查询地市排名:"+JSON.toJSONString(orgMapRes));
        List<Map<String,Object>> orgList = (List<Map<String,Object>>) orgMapRes.get("rptOrderList");
        for (Map<String, Object> orgMap : orgList) {
            if (params.get("orglevel1").toString().equals("800000000004")) {
                orgMap.put("name",OrgEnum.getNameByOrgId(Long.valueOf(orgMap.get("orgId").toString())));
            }else {
                SysArea sysArea = sysAreaMapper.getNameByOrgId(orgMap.get("orgId").toString());
                if (sysArea==null){
                    orgMap.put("name","本部");
                }else {
                    orgMap.put("name",sysArea.getName());
                }
            }
            if (orgMap.get("contactRate")!=null &&  !orgMap.get("contactRate").toString().equals("null") && orgMap.get("contactRate")!=""){
                orgMap.put("contactRate",getPercentFormat(Double.valueOf(orgMap.get("contactRate").toString()),2,2));
            }else {
                orgMap.put("contactRate","0%");
            }
        }
        //按渠道排序
        paramMap.put("rptType","0");
        paramMap.put("sortColumn","channel");
        //查询渠道排序
        log.info("【入参】新活动报表 客触数  查询渠道排序："+JSON.toJSONString(paramMap));
        Map<String, Object> channelMapRes = iReportService.queryRptOrder(paramMap);
        log.info("【出参】新活动报表 客触数 查询渠道排序:"+JSON.toJSONString(channelMapRes));
        List<Map<String,Object>> channelList = (List<Map<String,Object>>) channelMapRes.get("rptOrderList");
        for (Map<String, Object> channelMap : channelList) {
            Channel channel = channelMapper.selectByCode(channelMap.get("channel").toString());
            channelMap.put("name",channel==null ? "" : channel.getContactChlName());
            //revenueReduceNum
            Object revenueReduceNum = channelMap.get("revenueReduceNum");
            if (revenueReduceNum==null || ""==revenueReduceNum){
                channelMap.put("revenueReduceNum",0);
            }
            if (channelMap.get("contactRate")!=null && !channelMap.get("contactRate").toString().equals("null") && channelMap.get("contactRate")!=""){
                channelMap.put("contactRate",getPercentFormat(Double.valueOf(channelMap.get("contactRate").toString()),2,2));
            }else {
                channelMap.put("contactRate","0%");
            }
        }
        dataMap.put("areaList",orgList);
        dataMap.put("channelList",channelList);
        result.put("code","0000");
        result.put("message","成功");
        result.put("data",dataMap);
        Map<String, Object> map1 = activityThemeLevel(params);
        if (map1.get("code").equals("0000")){
            dataMap.put("orglevel2",map1.get("orglevel2"));
        }
        return result;
    }

//    /**
//     * 转换率
//     * @param params
//     * @return
//     */
//    @Override
//    public Map<String, Object> orderSuccessRate(Map<String, Object> params) {
//        Map<String,Object> result = new HashMap<>();
//        Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
//        List<Map<String,Object>>  dataList = new ArrayList<>();
//        //统计维度(0:按渠道，1按地市) 不用就不传
//        paramMap.put("rptType","2");
//        paramMap.put("pageSize","5");
//        log.info("【入参】新活动报表 转换率  top5："+JSON.toJSONString(paramMap));
//        Map<String, Object> stringObjectMap = iReportService.queryRptOrder(paramMap);
//        log.info("【出参】新活动报表 转换率  top5："+JSON.toJSONString(stringObjectMap));
//        if (stringObjectMap.get("resultCode").equals("1000")){
//            result.put("code","0001");
//            result.put("message","报表查询失败");
//            return result;
//        }
//        List<Map<String, Object>> data = new ArrayList<>();
//        if (stringObjectMap.get("resultCode") != null && "1".equals(stringObjectMap.get("resultCode").toString())) {
//            Object rptOrderList = stringObjectMap.get("rptOrderList");
//            if (rptOrderList!=null && ""!=rptOrderList){
//                data = (List<Map<String, Object>>) stringObjectMap.get("rptOrderList");
//                for (Map<String, Object> datum : data) {
//                    Map<String,Object> camMap = new HashMap<>();
//                    MktCampaignDO campaignDO = mktCampaignMapper.selectByInitId(Long.valueOf(datum.get("mktCampaignId").toString()));
//                    camMap.put("mktCampaignName",campaignDO==null ? "" : campaignDO.getMktCampaignName());
//                    camMap.put("conversion",getPercentFormat(Double.valueOf(datum.get("contactRate").toString()),2,2));
//                    camMap.put("area",getArea(campaignDO));
//                    log.info("活动报表查询接口:orderSuccessRate"+stringObjectMap);
//                    //按地市
//                    paramMap.put("rptType","1");
//                    paramMap.put("orglevel2","all");
//                    paramMap.put("mktCampaignId",datum.get("mktCampaignId"));
//                    paramMap.put("pageSize","20");
//                    log.info("【入参】新活动报表 转换率  按地市："+JSON.toJSONString(paramMap));
//                    Map<String, Object> stringObjectMap1 = iReportService.queryRptOrder(paramMap);
//                    log.info("【出参】新活动报表 转换率 按地市:"+JSON.toJSONString(stringObjectMap1));
//                    if (stringObjectMap1.get("resultCode").equals("1000")){
//                        continue;
//                    }
//                    List<Map<String,Object>> orgList = (List<Map<String,Object>>) stringObjectMap1.get("rptOrderList");
//                    for (Map<String, Object> orgMap : orgList) {
//                        orgMap.put("name",OrgEnum.getNameByOrgId(Long.valueOf(orgMap.get("orgId").toString())));
//                        if (orgMap.get("contactRate")!=null ||  orgMap.get("contactRate").toString().equals("")){
//                            orgMap.put("contactRate",getPercentFormat(Double.valueOf(orgMap.get("contactRate").toString()),2,2));
//                        }else {
//                            orgMap.put("contactRate","0%");
//                        }
//                    }
//                    //按渠道
//                    paramMap.put("rptType","0");
//                    paramMap.put("channelCode","all");
//                    paramMap.put("mktCampaignId",datum.get("mktCampaignId"));
//                    paramMap.remove("orglevel2");
//                    log.info("【入参】新活动报表 转换率  按渠道："+JSON.toJSONString(paramMap));
//                    Map<String, Object> stringObjectMap2 = iReportService.queryRptOrder(paramMap);
//                    log.info("【出参】新活动报表 转换率 按渠道:"+JSON.toJSONString(stringObjectMap2));
//                    if (stringObjectMap2.get("resultCode").equals("1000")){
//                        continue;
//                    }
//                    List<Map<String,Object>> channelList = (List<Map<String,Object>>) stringObjectMap2.get("rptOrderList");
//                    for (Map<String, Object> channelMap : channelList) {
//                        Channel channel = channelMapper.selectByCode(channelMap.get("channel").toString());
//                        channelMap.put("name",channel==null ? "" : channel.getContactChlName());
//                        if (channelMap.get("contactRate")!=null || channelMap.get("contactRate").toString().equals("")){
//                            channelMap.put("contactRate",getPercentFormat(Double.valueOf(channelMap.get("contactRate").toString()),2,2));
//                        }else {
//                            channelMap.put("contactRate","0%");
//                        }                   }
//                    camMap.put("areaList",orgList);
//                    camMap.put("channelList",channelList);
//                    dataList.add(camMap);
//                }
//            }
//        } else {
//            Object reqId = stringObjectMap.get("reqId");
//            stringObjectMap.put("resultCode", CODE_FAIL);
//            stringObjectMap.put("resultMsg", "查询无结果 queryRptBatchOrder error :" + reqId.toString());
//        }
//        result.put("code","0000");
//        result.put("message","成功");
//        result.put("data",dataList);
//        Map<String, Object> map = activityThemeLevelAndChannel(params);
//        if (map.get("code").equals("0000")){
//            result.put("orglevel2",map.get("orglevel2"));
//            result.put("channel",map.get("channel"));
//        }
//        return result;
//    }


    /**
     * 11/15 修改后的 转换率
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> orderSuccessRate(Map<String, Object> params) {
        Map<String,Object> result = new HashMap<>();
        Map<String,Object> dataMap = new HashMap<>();

        Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
        if (paramMapToSuccess(result, paramMap)) return result;
        //统计维度 按活动 默认转换率排序
        paramMap.put("rptType","2");
        //top5
        paramMap.put("pageSize","5");

        //查询总数 解析
        log.info("【入参】新活动报表 转换率修改后 ："+JSON.toJSONString(paramMap));
        Map<String,Object> stringOMap = iReportService.queryRptOrder(paramMap);
        log.info("【出参】新活动报表 转换率修改后 ："+JSON.toJSONString(stringOMap));
        List<Map<String,Object>> rptList = (List<Map<String,Object>>) stringOMap.get("rptOrderList");
        for (Map<String, Object> datum : rptList) {
            MktCampaignDO campaignDO = mktCampaignMapper.selectByInitIdFromOne(Long.valueOf(datum.get("mktCampaignId").toString()));
            datum.put("mktCampaignName",campaignDO==null ? "" : campaignDO.getMktCampaignName());
            if (datum.get("contactRate").toString().equals("1")) {
                datum.put("conversion","100%");
            }else {
                datum.put("conversion",getPercentFormat(Double.valueOf(datum.get("contactRate").toString()),2,2));
            }
            if (campaignDO == null){
                datum.put("area","");
            }else {
                datum.put("area",getArea(campaignDO));
            }
        }
        //地市(ALL表示所有,多个用逗号隔开) 添加11个地市的orgid
        paramMap.put("rptType","1");
        //权限控制C2 / C3
        if (params.get("orglevel1").toString().equals("800000000004")) {
            paramMap.put("orglevel2","all");
        }else {
            paramMap.put("orglevel3","all");
        }
        paramMap.put("pageSize","20");
        //查询地市排名
        log.info("【入参】新活动报表 转换率修改后  查询地市排名："+JSON.toJSONString(paramMap));
        Map<String,Object> orgMapRes = iReportService.queryRptOrder(paramMap);
        log.info("【出参】新活动报表 转换率修改后 查询地市排名:"+JSON.toJSONString(orgMapRes));
        List<Map<String,Object>> orgList = (List<Map<String,Object>>) orgMapRes.get("rptOrderList");
        for (Map<String, Object> orgMap : orgList) {
            if (params.get("orglevel1").toString().equals("800000000004")) {
                orgMap.put("name",OrgEnum.getNameByOrgId(Long.valueOf(orgMap.get("orgId").toString())));
            }else {
                SysArea sysArea = sysAreaMapper.getNameByOrgId(orgMap.get("orgId").toString());
                if (sysArea==null){
                    orgMap.put("name","本部");
                }else {
                    orgMap.put("name",sysArea.getName());
                }
            }
            if (orgMap.get("contactRate")!=null &&  !orgMap.get("contactRate").toString().equals("null") && orgMap.get("contactRate")!=""){
                orgMap.put("contactRate",getPercentFormat(Double.valueOf(orgMap.get("contactRate").toString()),2,2));
            }else {
                orgMap.put("contactRate","0%");
            }
        }
        //按渠道排序
        paramMap.put("rptType","0");
        paramMap.put("sortColumn","channel");
        //查询渠道排序
        log.info("【入参】新活动报表 客触数  查询渠道排序："+JSON.toJSONString(paramMap));
        Map<String, Object> channelMapRes = iReportService.queryRptOrder(paramMap);
        log.info("【出参】新活动报表 客触数 查询渠道排序:"+JSON.toJSONString(channelMapRes));
        List<Map<String,Object>> channelList = (List<Map<String,Object>>) channelMapRes.get("rptOrderList");
        for (Map<String, Object> channelMap : channelList) {
            Channel channel = channelMapper.selectByCode(channelMap.get("channel").toString());
            channelMap.put("name",channel==null ? "" : channel.getContactChlName());
            //revenueReduceNum
            Object revenueReduceNum = channelMap.get("revenueReduceNum");
            if (revenueReduceNum==null || ""==revenueReduceNum){
                channelMap.put("revenueReduceNum",0);
            }
            if (channelMap.get("contactRate")!=null && !channelMap.get("contactRate").toString().equals("null") && channelMap.get("contactRate")!=""){
                channelMap.put("contactRate",getPercentFormat(Double.valueOf(channelMap.get("contactRate").toString()),2,2));
            }else {
                channelMap.put("contactRate","0%");
            }
        }
        //转换率TOP5
        dataMap.put("contactRateTop5",rptList);
        dataMap.put("areaList",orgList);
        dataMap.put("channelList",channelList);
        result.put("code","0000");
        result.put("message","成功");
        result.put("data",dataMap);
        return result;
    }

    private  List<Map<String,Object>> mapSort (List<Map<String,Object>> list,String sort){
        Collections.sort(list,new Comparator<Map<String,Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                // TODO Auto-generated method stub
                return o1.get(sort).toString().compareTo(o2.get(sort).toString());
            }
        });
        return list;
    }

    public String fun2(double f) {
        String s = String.format("%.2f", f);
        return s;
    }

//    /**
//     * 收入拉动
//     * @param params
//     * @return
//     */
//    @Override
//    public Map<String, Object> incomePull(Map<String, Object> params) {
//        Map<String,Object> result = new HashMap<>();
//        Map<String,Object> dataMap = new HashMap<>();
//        List<Map<String,Object>>  dateMapList = new ArrayList<>();
//        List<Map<String,Object>>  dataList = new ArrayList<>();
//        List<Map<String,Object>>  mapsList = new ArrayList<>();
//        //按地市
//        params.put("rptType","1");
//        Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
//        String date = params.get("endDate").toString();
//        String startDate = params.get("startDate").toString();
//        String type = paramMap.get("mktCampaignType").toString();
//        //总数
//        Integer count = mktCampaignMapper.getCountFromActivityTheme(date,type,startDate);
//        //查询一条数据 返回总数
//
//        log.info("【入参】新活动报表 收入拉动  查询一条数据 ："+JSON.toJSONString(paramMap));
//        Map<String, Object> stringObjectMap = iReportService.queryRptOrder(paramMap);
//        log.info("【出参】新活动报表 收入拉动 查询一条数据 返回总数:"+JSON.toJSONString(stringObjectMap));
//        if (stringObjectMap.get("resultCode").equals("1000")|| ((List<Map<String,Object>>) stringObjectMap.get("rptOrderList")).size()<1){
//            result.put("code","0001");
//            result.put("message","报表查询失败");
//            return result;
//        }
//        List<Map<String,Object>> list = (List<Map<String,Object>>) stringObjectMap.get("rptOrderList");
//        //收入低迁金额
//        dataMap.put("收入低迁金额",list.get(0).get("incomeDown"));
//        //高迁收入
//        dataMap.put("高迁收入",list.get(0).get("incomeUp"));
//        //收入底迁活动数
//        dataMap.put("收入低迁活动数",list.get(0).get("downCount"));
//        //收入高签活跃数
//        dataMap.put("收入高迁活跃数",list.get(0).get("upCount"));
//        double v = Double.valueOf(dataMap.get("高迁收入").toString()).doubleValue() + Double.valueOf(dataMap.get("收入低迁金额").toString()).doubleValue();
//        dataMap.put("总收入",fun2(v)); //总收入
//        dataMap.put("收入平迁金额","0.00"); //收入平迁金额
//        //收入平迁活动数
//        dataMap.put("收入平迁活动数",count - (Integer.valueOf(dataMap.get("收入高迁活跃数").toString()) + Integer.valueOf(dataMap.get("收入低迁活动数").toString())) );
//        //低迁率
//        dataMap.put("低迁率",getPercentFormat(Double.valueOf(dataMap.get("收入低迁活动数").toString()).doubleValue()/(double) count,2,2));
//        //高迁率
//        dataMap.put("高迁率",getPercentFormat(Double.valueOf(dataMap.get("收入高迁活跃数").toString()).doubleValue()/(double)count,2,2));
//        //平迁率
//        dataMap.put("平迁率",getPercentFormat(Double.valueOf(dataMap.get("收入平迁活动数").toString()).doubleValue()/(double)count,2,2));
//        Iterator<String> iterator = dataMap.keySet().iterator();
//        while (iterator.hasNext()){
//            String next = iterator.next();
//            HashMap<String, Object> map = new HashMap<>();
//            Object o = dataMap.get(next);
//            map.put("name",next);
//            map.put("value",o);
//            dateMapList.add(map);
//        }
//        HashMap<String, Object> map2 = new HashMap<>();
//        HashMap<String, Object> map3 = new HashMap<>();
//        HashMap<String, Object> map4 = new HashMap<>();
//        HashMap<String, Object> map5 = new HashMap<>();
//        map2.put("name","收入低迁活动");
//        map2.put("value",dataMap.get("收入低迁活动数"));
//        map2.put("rate",dataMap.get("低迁率"));
//        map2.put("num",dataMap.get("收入低迁金额"));
//
//        map3.put("name","收入高迁活跃");
//        map3.put("value",dataMap.get("收入高迁活跃数"));
//        map3.put("rate",dataMap.get("平迁率"));
//        map3.put("num",dataMap.get("高迁收入"));
//
//        map4.put("name","收入平迁活动");
//        map4.put("value",dataMap.get("收入平迁活动数"));
//        map4.put("rate",dataMap.get("高迁率"));
//        map4.put("num",dataMap.get("收入平迁金额"));
//
//        map5.put("name","总收入");
//        map5.put("num",dataMap.get("总收入"));
//
//        mapsList.add(map2);
//        mapsList.add(map3);
//        mapsList.add(map4);
//        mapsList.add(map5);
//        dataMap.put("dateMapList",mapsList);
//        dataMap.put("dateMapList2",dateMapList);
//        //返回多条 按活动查询
//        paramMap.put("rptType","2");
//        //top5
//        paramMap.put("pageSize","5");
//        //收入提高
//        paramMap.put("sortColumn","incomeUp");
//        //收入拉动top5
//        Map<String, Object> map = iReportService.queryRptOrder(paramMap);
//        log.info("新活动报表 收入拉动 收入拉动top5:"+JSON.toJSONString(map));
//        //每个活动需要按地市和渠道排序 取活动id查询地市信息 和渠道信息
//        List<Map<String, Object>> data = new ArrayList<>();
//        if (map.get("resultCode") != null && "1".equals(map.get("resultCode").toString())) {
//            Object rptOrderList = map.get("rptOrderList");
//            if (rptOrderList!=null && ""!=rptOrderList){
//                data = (List<Map<String, Object>>) map.get("rptOrderList");
//                for (Map<String, Object> datum : data) {
//                    Map<String,Object> camMap = new HashMap<>();
//                    MktCampaignDO campaignDO = mktCampaignMapper.selectByInitId(Long.valueOf(datum.get("mktCampaignId").toString()));
//                    camMap.put("mktCampaignName",campaignDO==null ? "" : campaignDO.getMktCampaignName());
//                    camMap.put("income",datum.get("incomeUp"));
//                    camMap.put("area",campaignDO==null ? "" : getArea(campaignDO));
//                    log.info("活动报表查询接口:orderSuccessRate"+stringObjectMap);
//                    //按地市
//                    paramMap.put("rptType","1");
//                    paramMap.put("orglevel2","all");
//                    paramMap.put("mktCampaignId",datum.get("mktCampaignId"));
//                    paramMap.put("pageSize",20);
//                    Map<String, Object> stringObjectMap1 = iReportService.queryRptOrder(paramMap);
//                    log.info("新活动报表 收入拉动 按地市:"+JSON.toJSONString(stringObjectMap1));
//                    if (stringObjectMap1.get("resultCode").equals("1000")){
//                        continue;
//                    }
//                    List<Map<String,Object>> orgList = (List<Map<String,Object>>) stringObjectMap1.get("rptOrderList");
//                    for (Map<String, Object> orgMap : orgList) {
//                        orgMap.put("name",OrgEnum.getNameByOrgId(Long.valueOf(orgMap.get("orgId").toString())));
//                        Double totalIncome = Double.valueOf(orgMap.get("incomeUp").toString())+ Double.valueOf(orgMap.get("incomeDown").toString());
//                        orgMap.put("totalIncome",totalIncome.toString());
//                    }
//                    Map<String,Object> areaList = new HashMap<>();
//                    areaList.put("incomeSum",mapSort(orgList,"totalIncome"));
//                    areaList.put("lowIncome",mapSort(orgList,"incomeDown"));
//                    camMap.put("areaList",areaList);
//
//                    //按渠道
//                    paramMap.put("rptType","0");
//                    paramMap.put("channelCode","all");
//                    paramMap.put("mktCampaignId",datum.get("mktCampaignId"));
//                    paramMap.remove("orglevel2");
//                    Map<String, Object> stringObjectMap2 = iReportService.queryRptOrder(paramMap);
//                    log.info("新活动报表 收入拉动 按渠道:"+JSON.toJSONString(stringObjectMap2));
//                    if (stringObjectMap2.get("resultCode").equals("1000")){
//                        continue;
//                    }
//                    List<Map<String,Object>> channelList = (List<Map<String,Object>>) stringObjectMap2.get("rptOrderList");
//                    for (Map<String, Object> channelMap : channelList) {
//                        Channel channel = channelMapper.selectByCode(channelMap.get("channel").toString());
//                        channelMap.put("name",channel==null ? "" : channel.getContactChlName());
//                        Double totalIncome = Double.valueOf(channelMap.get("incomeUp").toString())+ Double.valueOf(channelMap.get("incomeDown").toString());
//                        channelMap.put("totalIncome",totalIncome.toString());
//                    }
//
//                    Map<String,Object> channels = new HashMap<>();
//                    channels.put("incomeSum",mapSort(channelList,"totalIncome"));
//                    channels.put("lowIncome",mapSort(channelList,"incomeDown"));
//
//                    camMap.put("channelList",channels);
//                    dataList.add(camMap);
//                }
//            }
//        } else {
//            Object reqId = stringObjectMap.get("reqId");
//            stringObjectMap.put("resultCode", CODE_FAIL);
//            stringObjectMap.put("resultMsg", "查询无结果 queryRptBatchOrder error :" + reqId.toString());
//        }
//        dataMap.put("dataList",dataList);
//        result.put("code","0000");
//        result.put("message","成功");
//        result.put("data",dataMap);
//        Map<String, Object> map1 = activityThemeLevelAndChannel(params);
//        if (map1.get("code").equals("0000")){
//            result.put("orglevel2",map1.get("orglevel2"));
//            result.put("channel",map1.get("channel"));
//        }
//        return result;
//    }


    /**
     * 11/15 修改后的 收入拉动
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> incomePull(Map<String, Object> params) {
        Map<String,Object> result = new HashMap<>();
        Map<String,Object> dataMap = new HashMap<>();
        List<Map<String,Object>>  dataList = new ArrayList<>();
        List<Map<String,Object>>  mapsList = new ArrayList<>();
        //按地市
        params.put("rptType","1");
        Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
        if (paramMapToSuccess(result, paramMap)) return result;
        String date = params.get("endDate").toString();
        String startDate = params.get("startDate").toString();
        String type = paramMap.get("mktCampaignType").toString();
        //总数
        Integer count = mktCampaignMapper.getCountFromActivityTheme(startDate,type,date);
        //查询一条数据 返回总数
        log.info("【入参】新活动报表 收入拉动  查询一条数据 ："+JSON.toJSONString(paramMap));
        Map<String, Object> stringObjectMap = iReportService.queryRptOrder(paramMap);
        log.info("【出参】新活动报表 收入拉动 查询一条数据 返回总数:"+JSON.toJSONString(stringObjectMap));
        if (stringObjectMap.get("resultCode").equals("1000")|| ((List<Map<String,Object>>) stringObjectMap.get("rptOrderList")).size()<1){
            result.put("code","0001");
            result.put("message","报表查询失败");
            return result;
        }
        List<Map<String,Object>> list = (List<Map<String,Object>>) stringObjectMap.get("rptOrderList");
        //收入低迁金额
        dataMap.put("收入低迁金额",list.get(0).get("incomeDown"));
        //高迁收入
        dataMap.put("高迁收入",list.get(0).get("incomeUp"));
        //收入底迁活动数
        dataMap.put("收入低迁活动数",list.get(0).get("downCount"));
        //收入高签活跃数
        dataMap.put("收入高迁活动数",list.get(0).get("upCount"));
        double v = Double.valueOf(dataMap.get("高迁收入").toString()).doubleValue() + Double.valueOf(dataMap.get("收入低迁金额").toString()).doubleValue();
        dataMap.put("总收入",fun2(v)); //总收入
        dataMap.put("收入平迁金额",0); //收入平迁金额
        //收入平迁活动数
        dataMap.put("收入平迁活动数",count - (Integer.valueOf(dataMap.get("收入高迁活动数").toString()) + Integer.valueOf(dataMap.get("收入低迁活动数").toString())) );
        //低迁率
        dataMap.put("低迁率",getPercentFormat(Double.valueOf(dataMap.get("收入低迁活动数").toString()).doubleValue()/(double) count,2,2));
        //高迁率
        dataMap.put("高迁率",getPercentFormat(Double.valueOf(dataMap.get("收入高迁活动数").toString()).doubleValue()/(double)count,2,2));
        //平迁率
        dataMap.put("平迁率",getPercentFormat(Double.valueOf(dataMap.get("收入平迁活动数").toString()).doubleValue()/(double)count,2,2));
//        Iterator<String> iterator = dataMap.keySet().iterator();
//        while (iterator.hasNext()){
//            String next = iterator.next();
//            HashMap<String, Object> map = new HashMap<>();
//            Object o = dataMap.get(next);
//            map.put("name",next);
//            map.put("value",o);
//            dateMapList.add(map);
//        }
        HashMap<String, Object> map2 = new HashMap<>();
        HashMap<String, Object> map3 = new HashMap<>();
        HashMap<String, Object> map4 = new HashMap<>();
        HashMap<String, Object> map5 = new HashMap<>();
        map2.put("name","收入低迁活动");
        map2.put("value",dataMap.get("收入低迁活动数"));
        map2.put("rate",dataMap.get("低迁率"));
        map2.put("num",dataMap.get("收入低迁金额"));

        map3.put("name","收入高迁活动");
        map3.put("value",dataMap.get("收入高迁活动数"));
        map3.put("rate",dataMap.get("高迁率"));
        map3.put("num",dataMap.get("高迁收入"));

        map4.put("name","收入平迁活动");
        map4.put("value",dataMap.get("收入平迁活动数"));
        map4.put("rate",dataMap.get("平迁率"));
        map4.put("num",dataMap.get("收入平迁金额"));

        map5.put("name","总收入");
        map5.put("num",dataMap.get("总收入"));

        mapsList.add(map2);
        mapsList.add(map3);
        mapsList.add(map4);
        mapsList.add(map5);

//        dataMap.put("dateMapList2",dateMapList);
        //返回多条 按活动查询
        paramMap.put("rptType","2");
        //top5
        paramMap.put("pageSize","5");
        //收入提高
        paramMap.put("sortColumn","incomeUp");
        //收入拉动top5
        Map<String, Object> map = iReportService.queryRptOrder(paramMap);
        log.info("新活动报表 收入拉动 收入拉动top5:"+JSON.toJSONString(map));
        List<Map<String,Object>> incomeTop5List = (List<Map<String,Object>>) map.get("rptOrderList");
        for (Map<String, Object> orgMap : incomeTop5List) {
            MktCampaignDO campaignDO = mktCampaignMapper.selectByInitIdFromOne(Long.valueOf(orgMap.get("mktCampaignId").toString()));
            orgMap.put("mktCampaignName",campaignDO==null ? "" : campaignDO.getMktCampaignName());
            orgMap.put("income",orgMap.get("incomeUp"));
            orgMap.put("area",campaignDO==null ? "" : getArea(campaignDO));
//            orgMap.put("name",OrgEnum.getNameByOrgId(Long.valueOf(orgMap.get("orgId").toString())));
            orgMap.put("mktCampaignId",campaignDO.getMktCampaignId());
        }

        //权限控制C2 / C3
        if (params.get("orglevel1").toString().equals("800000000004")) {
            paramMap.put("orglevel2","all");
        }else {
            paramMap.put("orglevel3","all");
        }
        paramMap.put("rptType","1");
        paramMap.put("pageSize",20);
        //按地市查询
        Map<String,Object> camMap = new HashMap<>();
        Map<String, Object> stringObjectMap1 = iReportService.queryRptOrder(paramMap);
        log.info("新活动报表 收入拉动 按地市:"+JSON.toJSONString(stringObjectMap1));
        List<Map<String,Object>> orgList = (List<Map<String,Object>>) stringObjectMap1.get("rptOrderList");
        for (Map<String, Object> orgMap : orgList) {
            if (params.get("orglevel1").toString().equals("800000000004")) {
                orgMap.put("name",OrgEnum.getNameByOrgId(Long.valueOf(orgMap.get("orgId").toString())));
            }else {
                SysArea sysArea = sysAreaMapper.getNameByOrgId(orgMap.get("orgId").toString());
                if (sysArea==null){
                    orgMap.put("name","本部");
                }else {
                    orgMap.put("name",sysArea.getName());
                }
            }
            Double totalIncome = Double.valueOf(orgMap.get("incomeUp").toString())+ Double.valueOf(orgMap.get("incomeDown").toString());
            orgMap.put("totalIncome",totalIncome.toString());
        }
        Map<String,Object> areaList = new HashMap<>();
//        Collections.sort(orgList, new Comparator<Map<String, Object>>() {
//            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
//                Double name1 = Double.valueOf(o1.get("totalIncome").toString()) ;//name1是从你list里面拿出来的一个
//                Double name2 = Double.valueOf(o2.get("totalIncome").toString()) ; //name1是从你list里面拿出来的第二个name
//                return name2.compareTo(name1);
//            }
//        });
//        Collections.sort(orgList, new Comparator<Map<String, Object>>() {
//            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
//                Double name1 = Double.valueOf(o1.get("lowIncome").toString()) ;//name1是从你list里面拿出来的一个
//                Double name2 = Double.valueOf(o2.get("lowIncome").toString()) ; //name1是从你list里面拿出来的第二个name
//                return name2.compareTo(name1);
//            }
//        });
        areaList.put("incomeSum",orgList);
        areaList.put("lowIncome",orgList);
        camMap.put("areaList",areaList);
        dataList.add(camMap);

        //按渠道
        paramMap.put("rptType","0");
        paramMap.put("channelCode","all");
        Map<String, Object> stringObjectMap2 = iReportService.queryRptOrder(paramMap);
        log.info("新活动报表 收入拉动 按渠道:"+JSON.toJSONString(stringObjectMap2));
        List<Map<String,Object>> channelList = (List<Map<String,Object>>) stringObjectMap2.get("rptOrderList");
        for (Map<String, Object> channelMap : channelList) {
            Channel channel = channelMapper.selectByCode(channelMap.get("channel").toString());
            channelMap.put("name",channel==null ? "" : channel.getContactChlName());
            Double totalIncome = Double.valueOf(channelMap.get("incomeUp").toString())+ Double.valueOf(channelMap.get("incomeDown").toString());
            channelMap.put("totalIncome",totalIncome.toString());
        }
        Map<String,Object> channels = new HashMap<>();
//        Collections.sort(channelList, new Comparator<Map<String, Object>>() {
//            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
//                Double name1 = Double.valueOf(o1.get("totalIncome").toString()) ;//name1是从你list里面拿出来的一个
//                Double name2 = Double.valueOf(o2.get("totalIncome").toString()) ; //name1是从你list里面拿出来的第二个name
//                return name2.compareTo(name1);
//            }
//        });
//        Collections.sort(channelList, new Comparator<Map<String, Object>>() {
//            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
//                Double name1 = Double.valueOf(o1.get("lowIncome").toString()) ;//name1是从你list里面拿出来的一个
//                Double name2 = Double.valueOf(o2.get("lowIncome").toString()) ; //name1是从你list里面拿出来的第二个name
//                return new Double(o1.get("lowIncome").toString()).compareTo(name1);
//            }
//        });
        channels.put("incomeSum",channelList);
        channels.put("lowIncome",channelList);
        camMap.put("channelList",channels);
        dataList.add(camMap);

        dataMap.put("dateMapList",mapsList);
        dataMap.put("incomeTop5List",incomeTop5List);
        dataMap.put("dataList",dataList);
        result.put("code","0000");
        result.put("message","成功");
        result.put("data",dataMap);
//        Map<String, Object> map1 = activityThemeLevelAndChannel(params);
//        if (map1.get("code").equals("0000")){
//            result.put("orglevel2",map1.get("orglevel2"));
//            result.put("channel",map1.get("channel"));
//        }
        return result;
    }


    /**
     * 收入底迁率 top5
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> incomeDown(Map<String, Object> params) {
        Map<String,Object> result = new HashMap<>();
        Map<String,Object> dataMap = new HashMap<>();
        params.put("rptType","2");
        Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
        if (paramMapToSuccess(result, paramMap)) return result;
        //收入低迁率 排序
        paramMap.put("sortColumn","revenueReduceRate");
        paramMap.put("pageSize","5");
        log.info("【入参】新活动报表 收入底迁率 top5  ："+JSON.toJSONString(paramMap));
        Map<String, Object> stringObjectMap = iReportService.queryRptOrder(paramMap);
        log.info("【出参】新活动报表 收入底迁率 top5 返回总数:"+JSON.toJSONString(stringObjectMap));
        List<Map<String,Object>> incomeTop5List = (List<Map<String,Object>>) stringObjectMap.get("rptOrderList");
        for (Map<String, Object> orgMap : incomeTop5List) {
            MktCampaignDO campaignDO = mktCampaignMapper.selectByInitIdFromOne(Long.valueOf(orgMap.get("mktCampaignId").toString()));
            orgMap.put("mktCampaignName",campaignDO==null ? "" : campaignDO.getMktCampaignName());
            if (orgMap.get("revenueReduceRate")!=null &&orgMap.get("revenueReduceRate")!=""){
                orgMap.put("revenueReduceRate", getPercentFormat(Double.valueOf(orgMap.get("revenueReduceRate").toString()), 2, 2));
            }else {
                orgMap.put("revenueReduceRate","0%");
            }
        }
        dataMap.put("incomeTop5List",incomeTop5List);
        result.put("code","0000");
        result.put("message","成功");
        result.put("data",dataMap);
        return result;
    }


    /**
     * 活动主题分类和数量  看下面这个
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> activityThemeCount(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            ArrayList<Object> list = new ArrayList<>();
            Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
            if (paramMapToSuccess(resultMap, paramMap)) return resultMap;
            // List<Map<String, String>> campaignTheme = sysParamsMapper.listParamsByKey("CAMPAIGN_THEME");
            // 查询二级目录
            List<CatalogItem> catalogItemList = catalogItemMapper.selectChild();
            String date = params.get("startDate").toString();
            String startDate = params.get("startDate").toString();

            String type = paramMap.get("mktCampaignType").toString();
            //总数
            Integer count = mktCampaignMapper.getCountFromActivityTheme(date,type,startDate);
            if ( catalogItemList!=null && catalogItemList.size()>0){
                for (CatalogItem catalogItem : catalogItemList) {
                    Long catalogItemId = catalogItem.getCatalogItemId();
                    String catalogItemName = catalogItem.getCatalogItemName();
                    //每个主题个数
                    List<MktCampaignDO> mktCampaignList = mktCampaignMapper.selectCampaignTheme(catalogItemId, date, type);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name",catalogItemName);
                    map.put("value",mktCampaignList.size());
                    map.put("type",catalogItemId.toString());
                    list.add(map);
                }
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("name","全部");
            map.put("value",count);
            map.put("type","all");
            list.add(0,map);
            resultMap.put("code","0000");
            resultMap.put("msg","成功");
            resultMap.put("data",list);
        } catch (Exception e) {
            resultMap.put("code","0001");
            e.printStackTrace();
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> activityThemeCountByC3(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            ArrayList<Object> list = new ArrayList<>();
            Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
            if (paramMapToSuccess(resultMap, paramMap)) return resultMap;
            // List<Map<String, String>> campaignTheme = sysParamsMapper.listParamsByKey("CAMPAIGN_THEME");
            // 查询二级目录
            List<CatalogItem> catalogItemList = catalogItemMapper.selectChild();
            String startDate = params.get("startDate").toString();
            String endDate = params.get("endDate").toString();
            String type = paramMap.get("mktCampaignType").toString();
            String lanId = "";
            String regionFlg = "";
//            if (params.get("C3") != null && !params.get("C3").equals("")) {
//                lanId = ChannelUtil.getAreaByOrg(params.get("C3").toString());
//                regionFlg = "C3";
//            }
//            if (params.get("C2") != null && !params.get("C2").equals("")) {
//                lanId = "";
//                regionFlg = "C2";
//            }

            if (params.get("orglevel1")!=null && params.get("orglevel1")!="") {
                if (params.get("orglevel1").toString().equals("800000000004")){

                }else if (params.get("orglevel1").toString().equals("1")){
                    lanId = "";
                    regionFlg = "C2";
                }else {
                    lanId = ChannelUtil.getAreaByOrg(params.get("orglevel1").toString());
                    regionFlg = "C3";
                }
            }

            //总数
            Integer count = mktCampaignMapper.getCountFromActivityThemeByC3(startDate,type,endDate,lanId,regionFlg);
            if (catalogItemList != null && catalogItemList.size() > 0) {
                for (CatalogItem catalogItem : catalogItemList) {
                    Long catalogItemId = catalogItem.getCatalogItemId();
                    String catalogItemName = catalogItem.getCatalogItemName();
                    //每个主题个数
                    List<MktCampaignDO> mktCampaignList = mktCampaignMapper.selectCampaignThemeByC3(catalogItemId, startDate,endDate ,type, lanId,regionFlg);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name",catalogItemName);
                    map.put("value",mktCampaignList.size());
                    map.put("type",catalogItemId.toString());
                    list.add(map);
                }
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("name","全部");
            map.put("value",count);
            list.add(0,map);
            resultMap.put("code","0000");
            resultMap.put("msg","成功");
            resultMap.put("data",list);
        } catch (Exception e) {
            resultMap.put("code","0001");
            e.printStackTrace();
        }
        return resultMap;
    }

    private boolean paramMapToSuccess(Map<String, Object> resultMap, Map<String, Object> paramMap) {
        if (!paramMap.get("resultCode").toString().equals(CODE_SUCCESS)) {
            resultMap.put("code","0001");
            resultMap.put("msg","入参异常");
            return true;
        }
        return false;
    }

    /**
     * 地市查询和渠道查询
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> activityThemeLevelAndChannel(Map<String, Object> params) {
        HashMap<String, Object> resultMap = new HashMap<>();
        ArrayList<HashMap<String, Object>> areaList = new ArrayList<>();
        ArrayList<HashMap<String, Object>> channelLists = new ArrayList<>();
        //地市信息
        if (params.get("orglevel1").toString().equals("800000000004")) {
            List<Organization> organizations = organizationMapper.selectMenuByEleven();
            if (organizations.size()>0 && organizations!=null){
                for (Organization organization : organizations) {
                    HashMap<String, Object> map = new HashMap<>();
                    String nameByOrgId = OrgEnum.getNameByOrgId(organization.getOrgId());
                    Long orgId = organization.getOrgId();
                    map.put("name",nameByOrgId);
                    map.put("type",orgId);
                    areaList.add(map);
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("name","省级");
                map.put("type","1");
                areaList.add(0,map);
                HashMap<String, Object> map2 = new HashMap<>();
                map2.put("name","全省");
                map2.put("type","800000000004");
                areaList.add(0,map2);
                resultMap.put("orglevel2",areaList);
            }else {
                resultMap.put("code","0001");
            }
        }else {
            SysArea sysArea = sysAreaMapper.getNameByOrgId(params.get("orglevel1").toString());
            if (sysArea == null){
                resultMap.put("code","0001");
            }else {
                List<SysArea> sysAreas = sysAreaMapper.selectByParnetArea(sysArea.getAreaId());
                for (SysArea area : sysAreas) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name",area.getName());
                    map.put("type",area.getOrgId());
                    areaList.add(map);
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("name",sysArea.getName());
                map.put("type",params.get("orglevel1"));
                areaList.add(0,map);
                resultMap.put("orglevel2",areaList);
            }
        }
        //渠道信息
        List<Channel> channelList = contactChannelMapper.getNewActivityChannel();
        if (channelList.size()>0 &&channelList!=null){
            for (Channel channel : channelList) {
                HashMap<String, Object> map = new HashMap<>();
                String channelName = channel.getContactChlName();
                String contactChlCode = channel.getContactChlCode();
                map.put("name",channelName);
                map.put("type",contactChlCode);
                map.put("id",channel.getContactChlId());
                channelLists.add(map);
            }
            resultMap.put("code","0000");
            resultMap.put("msg","成功");
            resultMap.put("channel",channelLists);
        }else {
            resultMap.put("code","0001");
        }
        return resultMap;
    }

    /**
     * 地市查询返回防止无数据没有地市显示数据
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> activityThemeLevel(Map<String, Object> params) {
        HashMap<String, Object> resultMap = new HashMap<>();
        ArrayList<HashMap<String, Object>> areaList = new ArrayList<>();
        //地市信息
        if (params.get("orglevel1").toString().equals("800000000004")) {
            List<Organization> organizations = organizationMapper.selectMenuByEleven();
            if (organizations.size()>0 && organizations!=null){
                for (Organization organization : organizations) {
                    HashMap<String, Object> map = new HashMap<>();
                    String nameByOrgId = OrgEnum.getNameByOrgId(organization.getOrgId());
                    Long orgId = organization.getOrgId();
                    map.put("name",nameByOrgId);
                    map.put("type",orgId);
                    areaList.add(map);
                }
                resultMap.put("orglevel2",areaList);
            }else {
                resultMap.put("code","0001");
                resultMap.put("msg","失败");
            }
        }else {
            SysArea sysArea = sysAreaMapper.getNameByOrgId(params.get("orglevel1").toString());
            if (sysArea == null){
                resultMap.put("code","0001");
                resultMap.put("msg","失败");
            }else {
                List<SysArea> sysAreas = sysAreaMapper.selectByParnetArea(sysArea.getAreaId());
                for (SysArea area : sysAreas) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name",area.getName());
                    map.put("type",area.getOrgId());
                    areaList.add(map);
                }
                resultMap.put("orglevel2",areaList);
            }
        }
        resultMap.put("code","0000");
        resultMap.put("msg","成功");
        return resultMap;
    }



    /**
     * 季度营销活动
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> quarterActivities(Map<String, Object> params) {
        HashMap<String, Object> resultMap = new HashMap<>();
        try {
            Map<String, Object> paramMap = AcitvityParams.ActivityParamsByMap(params);
            if (paramMapToSuccess(resultMap, paramMap)) return resultMap;
            //按活动统计
            paramMap.put("rptType","2");
//        //商机成功数 排序 orderSuccessNum
//        paramMap.put("sortColumn","orderSuccessNum");
            StringBuilder stringBuilder = new StringBuilder();
            //活动按主题 选 all或者主题
            Object theMe = params.get("theMe");
            String startDate = params.get("startDate").toString();
            String endDate = params.get("endDate").toString();
            String type = paramMap.get("mktCampaignType").toString();
            Object statusCd = params.get("statusCd");

            String lanId = "";
            String regionFlg = "";
            //权限控制
            // 全省数据
            if (params.get("orglevel1").toString().equals("800000000004")) {
                paramMap.put("orglevel2","all");
            }else if (params.get("orglevel1").toString().equals("1")){
                //省级数据 不是指全省 只是省级数据
                paramMap.put("orglevel2","all");
                regionFlg = "C2";
            }else {
                //11个地市的数据
                lanId = ChannelUtil.getAreaByOrg(params.get("orglevel1").toString());
                regionFlg = "C3";
            }

            String status = null;
            if (statusCd!=null && ""!=statusCd){
                status = statusCd.toString();
            }
            Long value = null;
            if (theMe!=null && ""!=theMe){
                if (!theMe.toString().equals("all")) {
                    value = Long.valueOf(theMe.toString());
                }
            }
//            List<MktCampaignDO> mktCampaignList =null;
//            //主题过滤
//            if (theMe!=null && theMe!=""){
//                if (theMe.toString().equals("all")){
//                    mktCampaignList = mktCampaignMapper.getMktCampaignFromInitIdFromStatus(startDate,endDate,type,status);
//                }else {
//                    //查询主题  theMe = value 参数 对应 表字段theMe 1000 - 1800
//                    mktCampaignList = mktCampaignMapper.selectCampaignThemeFromStatus(theMe.toString(), startDate,endDate, type,status);
//                }
//            }else {
//                mktCampaignList = mktCampaignMapper.getMktCampaignFromInitIdFromStatus(startDate,endDate,type,status);
//            }
//            if (theMe!=null && theMe!="" && theMe.toString().equals("all")) {
//
//            }else {
//                mktCampaignList = mktCampaignMapper.getQuarterActivities(theMe.toString(),status,startDate,endDate,type,lanId,regionFlg);
//            }
            List<MktCampaignDO> mktCampaignList =null;
            if (params.get("isHis")!=null && params.get("isHis")!="" && params.get("isHis").toString().equals("1")) {
                status = "2010";
                mktCampaignList = mktCampaignMapper.getQuarterActivitiesIsEnd(value,status,startDate,endDate,type,lanId,regionFlg);
            }else {
                mktCampaignList = mktCampaignMapper.getQuarterActivities(value,status,startDate,endDate,type,lanId,regionFlg);
            }
            List<Map<String, Object>> data = new ArrayList<>();
            if (mktCampaignList.size() > 0 && mktCampaignList != null) {
                for (MktCampaignDO mktCampaignDO : mktCampaignList) {
                    //活动级别所有的initId 根据活动维度分页查询所有 默认按成功数排序
                    stringBuilder.append(mktCampaignDO.getInitId()).append(",");
                    //多个id  “，”拼接
                    String substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                    paramMap.put("mktCampaignId", substring);
                }
                //按全部或者按主题查询 活动级别 默认排序 清单列表参数 渲染页面
                log.info("季度营销活动入参："+JSON.toJSONString(paramMap));
                Map<String, Object> stringObjectMap = iReportService.queryRptOrder(paramMap);
                log.info("新活动报表 季度营销活动 按活动:"+JSON.toJSONString(stringObjectMap));
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                List<HashMap<String, Object>> list = new ArrayList<>();
                if (stringObjectMap.get("resultCode") != null && "1".equals(stringObjectMap.get("resultCode").toString())) {
                    Object rptOrderList = stringObjectMap.get("rptOrderList");
                    data = (List<Map<String, Object>>) stringObjectMap.get("rptOrderList");
                    for (Map<String, Object> datum : data) {
                        HashMap<String, Object> camMap = new HashMap<>();
                        String mktCampaignId1 = datum.get("mktCampaignId").toString();
                        MktCampaignDO campaignDO = mktCampaignMapper.selectByInitIdFromOne(Long.valueOf(mktCampaignId1));
                        if (campaignDO == null){
                            // 如果有为空 跳过
                            continue;
                        }
                        String statusCd1 = campaignDO.getStatusCd();
                        SysParams paramsByValue = sysParamsMapper.findParamsByValue( "CAM-0001",statusCd1);
                        if (paramsByValue ==  null){
                            continue;
                        }
                        camMap.put("statusCd",paramsByValue.getParamName());
                        camMap.put("mktCampaignName", campaignDO == null ? "" : campaignDO.getMktCampaignName());
                        camMap.put("mktCampaignId", campaignDO.getMktCampaignId());
                        camMap.put("area", getArea(campaignDO));
                        //成功数
                        camMap.put("orderSuccessNum", datum.get("orderSuccessNum"));
                        //开始和结束时间
                        if (campaignDO.getPlanBeginTime()!=null) {
                            camMap.put("beginTime", fmt.format(campaignDO.getPlanBeginTime()));
                        }else {
                            camMap.put("beginTime","无开始时间");
                        }
                        if (campaignDO.getPlanEndTime()!=null) {
                            camMap.put("endTime", fmt.format(campaignDO.getPlanEndTime()));
                        }else {
                            camMap.put("endTime","无结束时间");
                        }
//                        camMap.put("beginTime", fmt.format(campaignDO.getPlanBeginTime()));
//                        camMap.put("endTime", fmt.format(campaignDO.getPlanEndTime()));
                        camMap.put("contactNum",datum.get("contactNum")); //客户接触数
                        camMap.put("orderNum",datum.get("orderNum")); //商机推荐数
                        camMap.put("orderSuccessNum",datum.get("orderSuccessNum")); //商机成功数
                        camMap.put("contactRate",getPercentFormat(Double.valueOf(datum.get("contactRate").toString()),2,2)); //转化率
                        camMap.put("orgChannelRate",getPercentFormat(Double.valueOf(datum.get("orgChannelRate").toString()), 2, 2));//门店有销率
                        if (datum.get("incomeUp")!=null &&datum.get("incomeUp")!=""){
                            camMap.put("incomeUp",datum.get("incomeUp"));//收入提升
                        }else {
                            camMap.put("incomeUp",0);
                        }
//                        camMap.put("incomeUp",datum.get("incomeUp"));//收入提升
                        if (datum.get("incomeDown")!=null &&datum.get("incomeDown")!=""){
                            camMap.put("incomeDown",datum.get("incomeDown"));//收入提升
                        }else {
                            camMap.put("incomeDown",0);
                        }
//                        camMap.put("incomeDown",datum.get("incomeDown")); // 收入低迁
                        camMap.put("upCount",datum.get("upCount"));//收入提升活动数
                        camMap.put("downCount",datum.get("downCount"));//收入低迁活动数
                        //收入拉动
                        camMap.put("income",Double.valueOf(camMap.get("incomeUp").toString())+ Double.valueOf(camMap.get("incomeDown").toString()));
                        //收入低迁率
                        if (datum.get("revenueReduceRate")!=null &&datum.get("revenueReduceRate")!=""){
                            camMap.put("revenueReduceRate",getPercentFormat(Double.valueOf(datum.get("revenueReduceRate").toString()), 2, 2));
                        }else {
                            camMap.put("revenueReduceRate","0%");
                        }
                        //收入低迁数
                        if (datum.get("revenueReduceNum")!=null && datum.get("revenueReduceNum")!=""){
                            camMap.put("revenueReduceNum",datum.get("revenueReduceNum"));
                        }else {
                            camMap.put("revenueReduceNum","0");
                        }


                        list.add(camMap);
                    }
                    resultMap.put("data",list);
                    resultMap.put("msg","成功");
                    resultMap.put("total",stringObjectMap.get("total"));
                    resultMap.put("code","0000");
                }
            }else {
                resultMap.put("code","0001");
                resultMap.put("msg","查无该类活动数据");
            }
        } catch (NumberFormatException e) {
            resultMap.put("code","0001");
            resultMap.put("msg","失败");
            e.printStackTrace();
        }
        return resultMap;
    }
}
