package com.zjtelcom.cpct.service.impl.filter;

import com.ctzj.smt.bss.centralized.web.util.BssSessionHelp;
import com.ctzj.smt.bss.sysmgr.model.dto.SystemUserDto;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjtelcom.cpct.common.Page;
import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.channel.InjectionLabelMapper;
import com.zjtelcom.cpct.dao.channel.MktVerbalConditionMapper;
import com.zjtelcom.cpct.dao.filter.FilterRuleMapper;
import com.zjtelcom.cpct.dao.system.SysParamsMapper;
import com.zjtelcom.cpct.dao.user.UserListMapper;
import com.zjtelcom.cpct.domain.channel.Label;
import com.zjtelcom.cpct.domain.channel.MktVerbalCondition;
import com.zjtelcom.cpct.domain.channel.Offer;
import com.zjtelcom.cpct.domain.system.SysParams;
import com.zjtelcom.cpct.dto.channel.OfferDetail;
import com.zjtelcom.cpct.dto.filter.FilterRule;
import com.zjtelcom.cpct.dto.filter.FilterRuleAddVO;
import com.zjtelcom.cpct.dto.filter.FilterRuleVO;
import com.zjtelcom.cpct.dto.user.UserList;
import com.zjtelcom.cpct.enums.ConditionType;
import com.zjtelcom.cpct.enums.FilterRuleType;
import com.zjtelcom.cpct.request.filter.FilterRuleReq;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.filter.FilterRuleService;
import com.zjtelcom.cpct.util.*;
import com.zjtelcom.cpct.dao.offer.OfferProdMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;

/**
 * @Description 规律规则实现类
 * @Author pengy
 * @Date 2018/6/21 9:46
 */
@Service
@Transactional
public class FilterRuleServiceImpl extends BaseService implements FilterRuleService {

    @Autowired
    private FilterRuleMapper filterRuleMapper;
    @Autowired
    private UserListMapper userListMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SysParamsMapper sysParamsMapper;
    @Autowired
    private OfferProdMapper offerMapper;
    @Autowired
    private MktVerbalConditionMapper verbalConditionMapper;
    @Autowired
    private InjectionLabelMapper labelMapper;




    /**
     * 过滤规则列表（含分页）
     */
    @Override
    public Map<String, Object> qryFilterRule(FilterRuleReq filterRuleReq) {
        Map<String, Object> maps = new HashMap<>();
        Page pageInfo = filterRuleReq.getPageInfo();
        PageHelper.startPage(pageInfo.getPage(), pageInfo.getPageSize());
        List<FilterRule> filterRules = filterRuleMapper.qryFilterRule(filterRuleReq.getFilterRule());
        Page page = new Page(new PageInfo(filterRules));
        List<FilterRuleVO> voList = new ArrayList<>();
        for (FilterRule rule : filterRules){
            FilterRuleVO vo = BeanUtil.create(rule,new FilterRuleVO());
            SysParams sysParams = sysParamsMapper.findParamsByValue("FILTER_RULE_TYPE",rule.getFilterType());
            if (sysParams!=null){
                vo.setFilterTypeName(sysParams.getParamName());
            }
            voList.add(vo);
        }
        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);
        maps.put("filterRules", voList);
        maps.put("pageInfo",page);
        return maps;
    }

    /**
     * 过滤规则列表（不含分页）
     */
    @Override
    public Map<String, Object> qryFilterRules(FilterRuleReq filterRuleReq) {
        Map<String, Object> maps = new HashMap<>();
        List<FilterRule> filterRules = filterRuleMapper.qryFilterRule(filterRuleReq.getFilterRule());
        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);
        maps.put("filterRules", filterRules);
        return maps;
    }

    /**
     * 导入用户名单
     */
    @Transactional(readOnly = false)
    @Override
    public Map<String, Object> importUserList(MultipartFile multipartFile, Long ruleId, String ruleName, String filterType) throws IOException {
        Map<String, Object> maps = new HashMap<>();
        InputStream inputStream = multipartFile.getInputStream();
        XSSFWorkbook wb = new XSSFWorkbook(inputStream);
        Sheet sheet = wb.getSheetAt(0);
        Integer rowNums = sheet.getLastRowNum() + 1;
        List<String> resultList = new ArrayList<>();
        String key = "USER_LIST_"+ChannelUtil.getRandomStr(5);
        for (int i = 1; i < rowNums; i++) {
            Row row = sheet.getRow(i);
            if (row.getLastCellNum()>=2){
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg","请返回检查模板格式");
                return maps;
            }
//            for (int j = 0; j < row.getLastCellNum(); j++) {
            Cell cell = row.getCell(0);
            String cellValue = ChannelUtil.getCellValue(cell).toString();
            if (!cellValue.equals("null")){
                resultList.add(cellValue);
            }
//            }
//            UserList userListT = userListMapper.getUserList(userList);
//            if (userListT != null) {
//                continue;
//            }
//            userList.setRuleId(ruleId);
//            userList.setCreateDate(new Date());
//            userList.setUpdateDate(new Date());
//            userList.setStatusDate(new Date());
//            userList.setRemark("123");
//            userList.setLanId(1L);
//            userList.setUpdateStaff(UserUtil.loginId());
//            userList.setCreateStaff(UserUtil.loginId());
//            userList.setStatusCd(CommonConstant.STATUSCD_EFFECTIVE);
//            userListMapper.insert(userList);
//
//            //新用户存入redis;.
//            redisUtils.hmSet(String.valueOf(userList.getUserId()), "userName", userList.getUserName());
//            redisUtils.hmSet(String.valueOf(userList.getUserId()), "userPhone", userList.getUserPhone());
//            redisUtils.hmSet(String.valueOf(userList.getUserId()), "filterType", userList.getFilterType());
//            redisUtils.hmSet(key, "userList",cellValue );
//            System.out.println(redisUtils.hmGet(key, "userList"));

        }
        FilterRule filterRules = new FilterRule();
        List<FilterRule> filterRuleList = new ArrayList<>();
        if(ruleId == null) {
            //重名校验
            filterRuleList = filterRuleMapper.selectFilterRuleByRuleName(ruleName);
            if(filterRuleList != null && filterRuleList.size() > 0) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "过滤规则名称已存在");
                return maps;
            }
            filterRules.setRuleName(ruleName);
            filterRules.setFilterType(filterType);
            filterRules.setCreateDate(new Date());
            filterRules.setCreateStaff(UserUtil.loginId());
            filterRules.setUpdateDate(new Date());
            filterRules.setUpdateStaff(UserUtil.loginId());
            filterRules.setStatusDate(new Date());
            filterRules.setStatusCd(CommonConstant.STATUSCD_EFFECTIVE);
            filterRules.setUserList(ChannelUtil.StringList2String(resultList));
            filterRuleMapper.insert(filterRules);
        }else {
            filterRules = filterRuleMapper.selectByPrimaryKey(ruleId);
            if (filterRules==null){
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg","过滤规则不存在");
                return maps;
            }
            //重名校验
            filterRuleList = filterRuleMapper.selectFilterRuleByRuleName(ruleName);
            if(filterRuleList != null && filterRuleList.size() > 0) {
                for(FilterRule rule : filterRuleList) {
                    if(rule.getRuleId().equals(ruleId)) {
                        continue;
                    }
                    maps.put("resultCode", CODE_FAIL);
                    maps.put("resultMsg", "过滤规则名称已存在");
                    return maps;
                }
            }
            filterRules.setRuleName(ruleName);
            filterRules.setFilterType(filterType);
            filterRules.setUpdateDate(new Date());
            filterRules.setUpdateStaff(UserUtil.loginId());
            filterRules.setUserList(ChannelUtil.StringList2String(resultList));
            filterRuleMapper.updateByPrimaryKey(filterRules);
            //redisUtils.del("FILTER_RULE_" + ruleId);
        }
        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        maps.put("resultMsg", filterRules.getUserList());
        maps.put("key",key);
        return maps;
    }

    /**
     * 从缓存中获取用户列表
     */
    @Override
    public Map<String, Object> listUserList(UserList userList) throws IOException {
        Map<String, Object> maps = new HashMap<>();
//        List<UserList> userLists = new ArrayList<>();
//        Set<Object> keys = redisUtils.keys("[1-9]*");
//        for (Object str : keys) {
//            UserList userListT = new UserList();
//            userListT.setUserName(String.valueOf(redisUtils.hmGet((String) str, "userName")));
//            userListT.setUserPhone(String.valueOf(redisUtils.hmGet((String) str, "userPhone")));
//            userLists.add(userListT);
//        }
//        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
//        maps.put("resultMsg", StringUtils.EMPTY);
//        maps.put("userLists", userLists);
        return maps;
    }

    /**
     * 删除过滤规则
     */
    @Override
    public Map<String, Object> delFilterRule(final FilterRule filterRule) {
        Map<String, Object> maps = new HashMap<>();
        FilterRule rule = filterRuleMapper.selectByPrimaryKey(filterRule.getRuleId());
        if(rule != null) {
            filterRuleMapper.delFilterRule(filterRule);
            //redisUtils.del("FILTER_RULE_" + filterRule.getRuleId());
            verbalConditionMapper.deleteByPrimaryKey(rule.getConditionId());
        }
        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);


        return maps;
    }

    /**
     * 查询单个过滤规则
     */
    @Override
    public Map<String, Object> getFilterRule(Long ruleId) {
        Map<String, Object> map = new HashMap<>();
        FilterRule filterRuleT = filterRuleMapper.selectByPrimaryKey(ruleId);
        if(null == filterRuleT) {
            map.put("resultCode", CommonConstant.CODE_FAIL);
            map.put("resultMsg", "过滤规则不存在");
            return map;
        }
        FilterRuleVO vo = BeanUtil.create(filterRuleT,new FilterRuleVO());
        if (filterRuleT.getChooseProduct()!=null && !filterRuleT.getChooseProduct().equals("")){
            List<String> codeList = ChannelUtil.StringToList(filterRuleT.getChooseProduct());
            List<OfferDetail> productList = new ArrayList<>();
            for (String code : codeList){
                Offer offer = offerMapper.selectByPrimaryKey(Integer.valueOf(code));
                if (offer!=null){
                    OfferDetail offerDetail = BeanUtil.create(offer,new OfferDetail());
                    productList.add(offerDetail);
                }
            }
            vo.setProductList(productList);
        }
        if (filterRuleT.getConditionId()!=null){
            MktVerbalCondition condition = verbalConditionMapper.selectByPrimaryKey(filterRuleT.getConditionId());
            if (condition!=null){
                Label label = labelMapper.selectByPrimaryKey(Long.valueOf(condition.getLeftParam()));
                if (label!=null){
                    vo.setLabelId(label.getInjectionLabelId());
                    vo.setConditionName(label.getInjectionLabelName());
                    vo.setOperType(condition.getOperType());
                    vo.setRightParam(condition.getRightParam());
                }
            }
        }
        map.put("resultCode", CommonConstant.CODE_SUCCESS);
        map.put("resultMsg", StringUtils.EMPTY);
        map.put("filterRule", vo);
        return map;
    }

    /**
     * 新建过滤规则
     */
    @Override
    public Map<String, Object> createFilterRule(FilterRuleAddVO addVO) {
        Map<String, Object> maps = new HashMap<>();
        //重名校验
        List<FilterRule> filterRuleList = filterRuleMapper.selectFilterRuleByRuleName(addVO.getRuleName());
        if(filterRuleList != null && filterRuleList.size() > 0) {
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", "过滤规则名称已存在");
            return maps;
        }
        final FilterRule filterRule = BeanUtil.create(addVO,new FilterRule());
        filterRule.setCreateDate(DateUtil.getCurrentTime());
        filterRule.setUpdateDate(DateUtil.getCurrentTime());
        filterRule.setStatusDate(DateUtil.getCurrentTime());
        filterRule.setUpdateStaff(UserUtil.loginId());
        filterRule.setCreateStaff(UserUtil.loginId());
        filterRule.setStatusCd(CommonConstant.STATUSCD_EFFECTIVE);
        List<String> codeList = new ArrayList<>();
        if (filterRule.getFilterType().equals(FilterRuleType.DISPATCHING_PERTURBED.getValue().toString())){
            filterRuleMapper.createFilterRule(filterRule);

        }else {
            for (Long offerId : addVO.getChooseProduct()){
                Offer offer = offerMapper.selectByPrimaryKey(Integer.valueOf(offerId.toString()));
                if (offer==null){
                    continue;
                }
                codeList.add(offer.getOfferId().toString());
            }
            filterRule.setChooseProduct(ChannelUtil.StringList2String(codeList));
            //销售品互斥过滤 加labelcode
            if (filterRule.getFilterType().equals("3000")){
                filterRule.setLabelCode("PROM_LIST");
            }
            if (addVO.getFilterType().equals(FilterRuleType.PERTURBED.getValue().toString())){
                MktVerbalCondition condition = BeanUtil.create(addVO.getCondition(),new MktVerbalCondition());
                condition.setVerbalId(0L);
                condition.setConditionType(ConditionType.FILTER_RULE.getValue().toString());
                verbalConditionMapper.insert(condition);
                filterRule.setConditionId(condition.getConditionId());
            }

            filterRuleMapper.createFilterRule(filterRule);
            if (filterRule.getFilterType()!=null && filterRule.getFilterType().equals("2000") ){
//                addVO.setExpression("CR002");
                String expression = CpcUtil.addZeroForNum(String.valueOf(filterRule.getRuleId()), 6);
//                filterRule.setExpression(expression);
                filterRuleMapper.updateExpression2(filterRule.getRuleId().toString(),"CR002"+expression);
            }
        }
        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);
        maps.put("filterRule", filterRule);
        return maps;
    }

    /**
     * 修改过滤规则
     */
    @Override
    public Map<String, Object> modFilterRule(FilterRuleAddVO editVO) {
        Map<String, Object> maps = new HashMap<>();
        final FilterRule filterRule = filterRuleMapper.selectByPrimaryKey(editVO.getRuleId());
        if (filterRule==null){
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", StringUtils.EMPTY);
            return maps;
        }
        //重名校验
        List<FilterRule> filterRuleList = filterRuleMapper.selectFilterRuleByRuleName(editVO.getRuleName());
        if(filterRuleList != null && filterRuleList.size() > 0) {
            for(FilterRule rule : filterRuleList) {
                if(rule.getRuleId().equals(editVO.getRuleId())) {
                    continue;
                }
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "过滤规则名称已存在");
                return maps;
            }
        }
        BeanUtil.copy(editVO,filterRule);
        filterRule.setUpdateDate(DateUtil.getCurrentTime());
        filterRule.setUpdateStaff(UserUtil.loginId());

        List<String> codeList = new ArrayList<>();
        for (Long offerId : editVO.getChooseProduct()){
            Offer offer = offerMapper.selectByPrimaryKey(Integer.valueOf(offerId.toString()));
            if (offer==null){
                continue;
            }
            codeList.add(offer.getOfferId().toString());
        }
        filterRule.setChooseProduct(ChannelUtil.StringList2String(codeList));
        if (filterRule.getFilterType().equals("3000")){
            filterRule.setLabelCode("PROM_LIST");
        }
        if (editVO.getFilterType().equals("6000")){
            if (filterRule.getConditionId()==null){
                MktVerbalCondition condition = BeanUtil.create(editVO.getCondition(),new MktVerbalCondition());
                condition.setVerbalId(0L);
                condition.setConditionType(ConditionType.FILTER_RULE.getValue().toString());
                verbalConditionMapper.insert(condition);
                filterRule.setConditionId(condition.getConditionId());
            }else {
                MktVerbalCondition condition = verbalConditionMapper.selectByPrimaryKey(filterRule.getConditionId());
                if (condition!=null){
                    BeanUtil.copy(editVO.getCondition(),condition);
                    verbalConditionMapper.updateByPrimaryKey(condition);
                }
            }
        }
        filterRuleMapper.updateByPrimaryKey(filterRule);
        redisUtils.del("FILTER_RULE_" + filterRule.getRuleId());
        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);
        maps.put("filterRule", filterRule);


        return maps;
    }


    /**
     * 根据过滤规则id集合查询过滤规则集合
     */
    @Override
    public Map<String, Object> getFilterRule(List<Integer> filterRuleIdList) {
        Map<String, Object> map = new HashMap<>();
        List<FilterRule> filterRuleList = new ArrayList<>();
        for (Integer filterRuleId : filterRuleIdList) {
            FilterRule filterRule = filterRuleMapper.selectByPrimaryKey(filterRuleId.longValue());
            if (filterRule==null){
                continue;
            }
            filterRuleList.add(filterRule);
        }
        map.put("resultCode", CommonConstant.CODE_SUCCESS);
        map.put("resultMsg", StringUtils.EMPTY);
        map.put("filterRuleList", filterRuleList);
        return map;
    }

    /**
     * 导入销售品
     */
    @Override
    public Map<String,Object> importOfferList(MultipartFile multipartFile, Long ruleId, String ruleName, String filterType, String operator, Long[] rightListId)throws IOException {
        Map<String,Object> maps = new HashMap<>();
        if(ruleName == null || operator == null || filterType == null) {
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", "过滤规则信息不完善");
        }
        List<String> resultList = new ArrayList<>();
        InputStream inputStream = multipartFile.getInputStream();
        XSSFWorkbook wb = new XSSFWorkbook(inputStream);
        Sheet sheet = wb.getSheetAt(0);
        for(int i=0;i<rightListId.length;i++) {
            Offer offer = offerMapper.selectByPrimaryKey(Integer.valueOf(rightListId[i].toString()));
            if (offer==null){
                continue;
            }
            resultList.add(offer.getOfferId().toString());
        }
        Integer rowNums = sheet.getLastRowNum() + 1;
        List<String> errorOffer = new ArrayList<>();
        for (int i = 1; i < rowNums; i++) {
            Row row = sheet.getRow(i);
            if (row.getLastCellNum() >= 2) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "请检查Excel文件列数是否有两列及以上");
                return maps;
            }
            Cell cell = row.getCell(0);
            String cellValue = ChannelUtil.getCellValue(cell).toString();
            if (!cellValue.equals("null")){
                List<Offer> offerList = offerMapper.selectByCode(cellValue);
                if(offerList.size() > 0) {
                    for (Offer offerSingle : offerList) {
                        if (offerSingle != null) {
                            if (!resultList.contains(offerSingle.getOfferId().toString())) {
                                resultList.add(offerSingle.getOfferId().toString());
                            }
                        }
                    }
                }else {
                    errorOffer.add(cellValue);
                }
            }
        }
        if(errorOffer.size() > 0) {
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", "失败原因：以下" + errorOffer.size() + "个销售品编码错误！" + "\n" + errorOffer);
            return maps;
        }
        int total = resultList.size();
        if(total > 500) {
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", "销售品数量超过上限500个");
            return maps;
        }
        FilterRule filterRules = new FilterRule();
        List<FilterRule> filterRuleList = new ArrayList<>();
        if(ruleId == null) {
            //重名校验
            filterRuleList = filterRuleMapper.selectFilterRuleByRuleName(ruleName);
            if(filterRuleList != null && filterRuleList.size() > 0) {
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "过滤规则名称已存在");
                return maps;
            }
            filterRules.setRuleName(ruleName);
            filterRules.setFilterType(filterType);
            filterRules.setLabelCode("PROM_LIST");
            filterRules.setChooseProduct(ChannelUtil.StringList2String(resultList));
            filterRules.setOperator(operator);
            filterRules.setCreateDate(new Date());
            filterRules.setCreateStaff(UserUtil.loginId());
            filterRules.setUpdateDate(new Date());
            filterRules.setUpdateStaff(UserUtil.loginId());
            filterRules.setStatusDate(new Date());
            filterRules.setStatusCd(CommonConstant.STATUSCD_EFFECTIVE);
            filterRuleMapper.insert(filterRules);
        }else {
            filterRules = filterRuleMapper.selectByPrimaryKey(ruleId);
            if (filterRules==null){
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg","过滤规则不存在");
                return maps;
            }
            //重名校验
            filterRuleList = filterRuleMapper.selectFilterRuleByRuleName(ruleName);
            if(filterRuleList != null && filterRuleList.size() > 0) {
                for(FilterRule rule : filterRuleList) {
                    if(rule.getRuleId().equals(ruleId)) {
                        continue;
                    }
                    maps.put("resultCode", CODE_FAIL);
                    maps.put("resultMsg", "过滤规则名称已存在");
                    return maps;
                }
            }
            filterRules.setRuleName(ruleName);
            filterRules.setFilterType(filterType);
            filterRules.setChooseProduct(ChannelUtil.StringList2String(resultList));
            filterRules.setOperator(operator);
            filterRules.setUpdateDate(new Date());
            filterRules.setUpdateStaff(UserUtil.loginId());
            filterRuleMapper.updateByPrimaryKey(filterRules);
            redisUtils.del("FILTER_RULE_" + filterRules.getRuleId());
        }
        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        maps.put("resultMsg", "导入成功，文件导入" + sheet.getLastRowNum() + "个，共计" + total + "个");
        return maps;
    }

    /**
     * 过滤规则列表（分页），排除红黑名单过滤类型
     */
    @Override
    public Map<String, Object> qryFilterRuleExcludeType(FilterRuleReq filterRuleReq) {
        Map<String, Object> maps = new HashMap<>();
        Page pageInfo = filterRuleReq.getPageInfo();
        PageHelper.startPage(pageInfo.getPage(), pageInfo.getPageSize());
        List<FilterRule> filterRules = filterRuleMapper.qryFilterRuleExcludeType(filterRuleReq.getFilterRule());
        Page page = new Page(new PageInfo(filterRules));
        List<FilterRuleVO> voList = new ArrayList<>();
        for (FilterRule rule : filterRules){
            FilterRuleVO vo = BeanUtil.create(rule,new FilterRuleVO());
            SysParams sysParams = sysParamsMapper.findParamsByValue("FILTER_RULE_TYPE",rule.getFilterType());
            if (sysParams!=null){
                vo.setFilterTypeName(sysParams.getParamName());
            }
            voList.add(vo);
        }
        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);
        maps.put("filterRules", voList);
        maps.put("pageInfo",page);
        return maps;
    }

    /**
     * 过滤规则列表（分页），创建人权限控制
     */
    @Override
    public Map<String, Object> qryFilterRuleByUser(FilterRuleReq filterRuleReq) {
        Map<String, Object> maps = new HashMap<>();
        SystemUserDto user = BssSessionHelp.getSystemUserDto();
        if(user.getSysUserId() == null) {
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", StringUtils.EMPTY);
            maps.put("filterRules", "");
        }
        filterRuleReq.getFilterRule().setCreateStaff(user.getSysUserId());
        Page pageInfo = filterRuleReq.getPageInfo();
        PageHelper.startPage(pageInfo.getPage(), pageInfo.getPageSize());
        List<FilterRule> filterRules = filterRuleMapper.qryFilterRuleByUserExcludeType(filterRuleReq.getFilterRule());
        Page page = new Page(new PageInfo(filterRules));
        List<FilterRuleVO> voList = new ArrayList<>();
        for (FilterRule rule : filterRules){
            FilterRuleVO vo = BeanUtil.create(rule,new FilterRuleVO());
            SysParams sysParams = sysParamsMapper.findParamsByValue("FILTER_RULE_TYPE",rule.getFilterType());
            if (sysParams!=null){
                vo.setFilterTypeName(sysParams.getParamName());
            }
            voList.add(vo);
        }
        maps.put("resultCode", CommonConstant.CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);
        maps.put("filterRules", voList);
        maps.put("pageInfo",page);
        return maps;
    }

}
