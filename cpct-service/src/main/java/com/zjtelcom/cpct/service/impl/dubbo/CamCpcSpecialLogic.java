package com.zjtelcom.cpct.service.impl.dubbo;

import com.ccssoft.interfaceplatform.zj.module.service.ISaleService;
import com.zjtelcom.cpct.dao.org.StaffGisRelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

@Service
public class CamCpcSpecialLogic {

    public static final Logger logger = LoggerFactory.getLogger(CamCpcSpecialLogic.class);

    @Autowired(required = false)
    private ISaleService iSaleService;
    @Autowired
    private StaffGisRelMapper staffGisRelMapper;

    // 线上扫码、电话到家事件接入特殊逻辑,判断事件是这两个事件   DefaultContext<String, Object> context
    public Map<String, Object> onlineScanCodeOrCallPhone4Home(Map<String, Object> context, String eventCode,String lanId) {
        Map<String, Object> resultMap = new HashMap<>();
        String tel = "";
        try {
            // c4标识
            String c4 = "";
            if (!"EVT0000000103".equals(eventCode) && context.get("400600000026")!=null) {
                c4 = context.get("400600000026").toString();
            } else if (!"EVT0000000103".equals(eventCode) && context.get("400600000014")!=null) {
                c4 = context.get("400600000014").toString();
            }else if(context.get("c4Name")!=null){
                c4 = context.get("c4Name").toString();
            }else{
                c4 = "无c4信息";
            }
            // 19:配送地址 16：装机详细地址
            String addr = "";
            //19:配送地址  16：装机详细地址
            String type = "";
            if (!"EVT0000000103".equals(eventCode) && context.get("400600000019") != null ) {
                addr = context.get("400600000019").toString();
                type= "1";
            } else if(!"EVT0000000103".equals(eventCode) && context.get("400600000016") != null){
                addr = context.get("400600000016").toString();
                type= "2";
            }else if(context.get("addressDesc") != null){
                addr = context.get("addressDesc").toString();
            }


            String respXml = null;

            try {
                // 本地网标识
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
            if (null == respXml || "".equals(respXml)) {
                resultMap.put("tel", "GIS网格覆盖编码查询失败：地址不精确。");
            }
            logger.info("onlineScanCodeOrCallPhone4Home-->respXml:" + respXml);
            List<Map<String, Object>> maps = parseData(respXml);
            logger.info("onlineScanCodeOrCallPhone4Home-->maps:" + maps);
            Map<String, Object> map = maps.get(0);

            // 获取GIS网格编码
            String wgbm = getValue4CycleMap(map, "Wgbm");
            if (null == wgbm || "".equals(wgbm)) {
                resultMap.put("tel", "地址查询网格编码为空");
            }
            resultMap.put("wgbm", wgbm);
            logger.info("onlineScanCodeOrCallPhone4Home-->wgbm:" + wgbm);

            List<String> list = new ArrayList<>();
            //35：社区经理 36：装维经理 37：装机经理
            if("1".equals(type)){
                String[] positionzj = new String []{"35","36","37"};
                for(String p:positionzj){
                    System.out.println("35：社区经理 36：装维经理 37：装机经理********"+p);
                    list = staffGisRelMapper.selectStaffTelByGisCodeOne(wgbm,p);
                    if(!list.isEmpty()){
                        break;
                    }
                    if ("37".equals(p)){
                        list = staffGisRelMapper.selectStaffTelByGisCode(wgbm);
                    }
                }
            }else if ("2".equals(type)){
                String[] positionzj = new String []{"37","36"};
                for(String p:positionzj){
                    list = staffGisRelMapper.selectStaffTelByGisCodeOne(wgbm,p);
                    if(!list.isEmpty()){
                        break;
                    }
                    if ("36".equals(p)){
                        list = staffGisRelMapper.selectStaffTelByGisCode(wgbm);
                    }
                }
            }else{
                list = staffGisRelMapper.selectStaffTelByGisCode(wgbm);
            }

            if (null == list || list.size() == 0) {
                resultMap.put("tel", "GIS网格编码:" + wgbm + "对应专员编码为空");
            }
            for (String s : list) {
                if (s == null) {
                    continue;
                }
                resultMap.put("tel", s);
                break;
            }
        }catch (Exception e) {
            logger.info("onlineScanCodeOrCallPhone4Home-->error!!!" );
            e.printStackTrace();
        }
        return resultMap;
    }


    public List<Map<String, Object>> parseData(String str) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Document doc = null;
        try {
            // 转为可解析对象
            doc = DocumentHelper.parseText(str);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = new HashMap<String, Object>();
        if (doc == null)
            return list;
        // 获取根节点
        Element rootElement = doc.getRootElement();
        // 转换map
        element2map(rootElement, map);
        list.add(map);
        return list;
    }

    private void element2map(Element elmt, Map<String, Object> map) {
        if (null == elmt) {
            return;
        }
        String name = elmt.getName();
        // 当前元素是最小元素
        if (elmt.isTextOnly()) {
            // 查看map中是否已经有当前节点
            Object f = map.get(name);
            // 用于存放元素属性
            Map<String, Object> m = new HashMap<String, Object>();
            // 遍历元素中的属性
            Iterator ai = elmt.attributeIterator();
            // 用于第一次获取该元素数据
            boolean aiHasNex = false;
            while (ai.hasNext()) {
                aiHasNex = true;
                // 拿到属性值
                Attribute next = (Attribute) ai.next();
                m.put(name + "." + next.getName(), next.getValue());
            }
            // 第一次获取该元素
            if (f == null) {
                // 判断如果有属性
                if (aiHasNex) {
                    // 将属性map存入解析map中
                    m.put(name, elmt.getText());
                    map.put(name, m);
                } else {
                    // 没有属性，直接存入相应的值
                    map.put(name, elmt.getText());
                }
            } else {
                // 解析map中已经有相同的节点
                // 如果当前值是list
                if (f instanceof List<?>) {
                    // list中添加此元素
                    m.put(name, elmt.getText());
                    ((List) f).add(m);
                } else {
                    // 如果不是，说明解析map中只存在一个与此元素名相同的对象
                    // 存放元素
                    List<Object> listSub = new ArrayList<Object>();
                    // 如果解析map中的值为string，说明第一个元素没有属性
                    if (f instanceof String) {
                        // 转换为map对象，
                        Map<String, Object> m1 = new HashMap<String, Object>();
                        m1.put(name, f);
                        // 添加到list中
                        listSub.add(m1);
                    } else {
                        // 否则直接添加值
                        listSub.add(f);
                    }
                    // 将当前的值包含的属性值放入list中
                    m.put(name, elmt.getText());
                    listSub.add(m);
                    // 解析map中存入list
                    map.put(name, listSub);
                }

            }
        } else {
            // 存放子节点元素
            Map<String, Object> mapSub = new HashMap<String, Object>();
            // 遍历当前元素的属性存入子节点map中
            attributeIterator(elmt, mapSub);
            // 获取所有子节点
            List<Element> elements = (List<Element>) elmt.elements();
            // 遍历子节点
            for (Element elmtSub : elements) {
                // 递归调用转换map
                element2map(elmtSub, mapSub);
            }
            // 当前元素没有子节点后 获取当前map中的元素名所对应的值
            Object first = map.get(name);
            if (null == first) {
                // 如果没有将值存入map中
                map.put(name, mapSub);
            } else {
                // 如果有，则为数组对象
                if (first instanceof List<?>) {
                    attributeIterator(elmt, mapSub);
                    ((List) first).add(mapSub);
                } else {
                    List<Object> listSub = new ArrayList<Object>();
                    listSub.add(first);
                    attributeIterator(elmt, mapSub);
                    listSub.add(mapSub);
                    map.put(name, listSub);
                }
            }
        }
    }

    private void attributeIterator(Element elmt, Map<String, Object> map) {
        if (elmt != null) {
            Iterator ai = elmt.attributeIterator();
            while (ai.hasNext()) {
                Attribute next = (Attribute) ai.next();
                map.put(elmt.getName() + "." + next.getName(), next.getValue());
            }
        }
    }

    public String getValue4CycleMap(Map map,String key) {
        String result = null;
        for (Object o : map.keySet()) {
            if (key.equals(o)) {
                return map.get(key).toString();
            } else {
                Object o1 = map.get(o);
                if (o1 instanceof Map ? true : false) {
                    Map subMap = (Map) o1;
                    result = getValue4CycleMap(subMap, key);
                }
            }
        }
        return result;
    }
}
