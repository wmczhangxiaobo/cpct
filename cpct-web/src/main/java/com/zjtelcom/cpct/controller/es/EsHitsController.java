package com.zjtelcom.cpct.controller.es;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zjtelcom.cpct.elastic.model.CampaignHitParam;
import com.zjtelcom.cpct.elastic.util.ElasticsearchUtil;
import com.zjtelcom.cpct.elastic.util.EsPage;

import com.zjtelcom.cpct.service.es.EsHitsService;
import com.zjtelcom.cpct.service.impl.es.EsHitsServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/7/9.
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/esHits")
public class EsHitsController {
    private Logger logger = LoggerFactory.getLogger(EsHitsController.class);
    /**
     * 测试索引
     */
    private String indexName="test_index2";

    /**
     * 类型
     */
    private String esType="external";

    @Autowired(required = false)
    private EsHitsService esService;



    /**
     *规则弹窗---活动命中查询
     * @param param
     * @return
     */
    @RequestMapping("/searchLabelInfoByRuleId")
    @CrossOrigin
    public Map<String, Object> searchLabelInfoByRuleId(@RequestBody HashMap<String,Object> param) {
        return esService.searchLabelInfoByRuleId(param.get("ruleId").toString(),param.get("isi").toString(),param.get("hitEntity").toString());
    }


    /**
     *活动命中数量查询
     * @param param
     * @return
     */
    @RequestMapping("/searchCampaignHitsTotal")
    @CrossOrigin
    public Map<String, Object> searchCampaignHitsTotal(@RequestBody CampaignHitParam param) {
        return esService.searchCampaignHitsTotal(param);
    }


    /**
     * 活动命中查询
     * @param param
     * @return
     */
    @RequestMapping("/searchCampaignHitsInfo")
    @CrossOrigin
    public Map<String, Object> searchCampaignHitsInfo(@RequestBody CampaignHitParam param) {
        return esService.searchCampaignHitsInfo(param);
    }


    /**
     * http://127.0.0.1:8080/es/createIndex
     * 创建索引
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/createIndex")
    @CrossOrigin
    public String createIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        if(!ElasticsearchUtil.isIndexExist("test_hyf")) {
//            ElasticsearchUtil.createIndex("test_hyf");
//        }
//        else{
//            return "索引已经存在";
//        }
        esService.add();
        return "索引创建成功";
    }



    @RequestMapping("/updateMapping")
    @CrossOrigin
    public String createIndexMapping(String indexName) {
       ElasticsearchUtil.updateMapping(indexName);
        return "索引mapping更新成功";
    }



    /**
     * 获取数据
     * http://127.0.0.1:8080/es/getData?id=2018-04-25%2016:33:44
     * @param id
     * @return
     */
    @RequestMapping("/getData")
    @CrossOrigin
    public String getData(String id){
        if(StringUtils.isNotBlank(id)) {
            Map<String, Object> map= ElasticsearchUtil.searchDataById(indexName,esType,id,null);
            return JSONObject.toJSONString(map);
        }
        else{
            return "id为空";
        }
    }

    /**
     * 查询数据
     * 模糊查询
     * @return
     */
    @RequestMapping("/queryMatchData")
    @CrossOrigin
    public String queryMatchData() {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolean matchPhrase = false;
        if (matchPhrase == Boolean.TRUE) {
            boolQuery.must(QueryBuilders.matchPhraseQuery("name", "修"));
        } else {
            boolQuery.must(QueryBuilders.matchQuery("name", "修"));
        }
        List<Map<String, Object>> list = ElasticsearchUtil.searchListData(indexName, esType, boolQuery, 10, null, null, null);
        return JSONObject.toJSONString(list);
    }

    /**
     * 通配符查询数据
     * 通配符查询 ?用来匹配1个任意字符，*用来匹配零个或者多个字符
     * @return
     */
    @RequestMapping("/queryWildcardData")
    @CrossOrigin
    public String queryWildcardData() {
        QueryBuilder queryBuilder = QueryBuilders.wildcardQuery("name.keyword", "j-*466");
        List<Map<String, Object>> list = ElasticsearchUtil.searchListData(indexName, esType, queryBuilder, 10, null, null, null);
        return JSONObject.toJSONString(list);
    }

    /**
     * 正则查询
     * @return
     */
    @RequestMapping("/queryRegexpData")
    @CrossOrigin
    public String queryRegexpData() {
        QueryBuilder queryBuilder = QueryBuilders.regexpQuery("name.keyword", "j--[0-9]{1,11}");
        List<Map<String, Object>> list = ElasticsearchUtil.searchListData(indexName, esType, queryBuilder, 10, null, null, null);
        return JSONObject.toJSONString(list);
    }

    /**
     * 查询数字范围数据
     * @return
     */
    @RequestMapping("/queryIntRangeData")
    @CrossOrigin
    public String queryIntRangeData() {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.rangeQuery("age").from(21)
                .to(25));
        List<Map<String, Object>> list = ElasticsearchUtil.searchListData(indexName, esType, boolQuery, 10, null, null, null);
        return JSONObject.toJSONString(list);
    }

    /**
     * 查询日期范围数据
     * @return
     */
    @RequestMapping("/queryDateRangeData")
    @CrossOrigin
    public String queryDateRangeData() {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.rangeQuery("date").from("2018-04-25T08:33:44.840Z")
                .to("2018-04-25T10:03:08.081Z"));
        List<Map<String, Object>> list = ElasticsearchUtil.searchListData(indexName, esType, boolQuery, 10, null, null, null);
        return JSONObject.toJSONString(list);
    }

    /**
     * 查询分页
     * @param startPage   第几条记录开始
     *                    从0开始
     *                    第1页 ：http://127.0.0.1:8080/es/queryPage?startPage=0&pageSize=2
     *                    第2页 ：http://127.0.0.1:8080/es/queryPage?startPage=2&pageSize=2
     * @param pageSize    每页大小
     * @return
     */
    @RequestMapping("/queryPage")
    @CrossOrigin
    public String queryPage(String startPage,String pageSize){
        if(StringUtils.isNotBlank(startPage)&&StringUtils.isNotBlank(pageSize)) {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.rangeQuery("date").from("2018-04-25T08:33:44.840Z")
                    .to("2018-04-25T10:03:08.081Z"));
            EsPage list = ElasticsearchUtil.searchDataPage(indexName, esType, Integer.parseInt(startPage), Integer.parseInt(pageSize), boolQuery, null, null, null);
            return JSONObject.toJSONString(list);
        }
        else{
            return  "startPage或者pageSize缺失";
        }
    }

    //客户级查询接口控制层订阅 加一个前端页面展示
    @PostMapping("/getCustomer")
    @CrossOrigin
    public Map<String,Object> getCustomer(@RequestBody Map<String, String> params){
        Map<String, Object> map = new HashMap<>();
        try {
            map = esService.getCustomer(params);
        } catch (Exception e) {
            logger.error("getCustomer! Exception: ", JSONArray.toJSON(params), e);
        }
        return map;
    }

    //资产查询接口
    @PostMapping("/getCustomerByCustId")
    @CrossOrigin
    public Map<String,Object> getCustomerByCustId(@RequestBody Map<String, String> params){
        Map<String, Object> map = new HashMap<>();
        try {
            map = esService.getCustomerByCustId(params);
        } catch (Exception e) {
            logger.error("getCustomerByCustId! Exception: ", JSONArray.toJSON(params), e);
        }
        return map;
    }
}
