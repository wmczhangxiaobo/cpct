package com.zjtelcom.cpct.service.impl.channel;

import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.channel.InjectionLabelCatalogMapper;
import com.zjtelcom.cpct.dao.channel.InjectionLabelMapper;
import com.zjtelcom.cpct.dao.channel.InjectionLabelValueMapper;
import com.zjtelcom.cpct.domain.channel.Label;
import com.zjtelcom.cpct.domain.channel.LabelCatalog;
import com.zjtelcom.cpct.domain.channel.LabelValue;
import com.zjtelcom.cpct.dto.channel.*;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.channel.LabelCatalogService;
import com.zjtelcom.cpct.util.BeanUtil;
import com.zjtelcom.cpct.util.ChannelUtil;
import com.zjtelcom.cpct.util.DateUtil;
import com.zjtelcom.cpct.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;

@Service
public class LabelCatalogServiceImpl extends BaseService implements LabelCatalogService {
    @Autowired
    private InjectionLabelMapper labelMapper;
    @Autowired
    private InjectionLabelCatalogMapper labelCatalogMapper;
    @Autowired
    private InjectionLabelValueMapper labelValueMapper;


    @Override
    public Map<String, Object> batchAdd(List<String> nameList, Long parentId, Long level) {
        Map<String,Object> result = new HashMap<>();
        for (String st : nameList){
            LabelCatalog addvo = new LabelCatalog();
            addvo.setLevelId(level);
//            addvo.setParentId(parentId);
            addvo.setCatalogName(st);
            addLabelCatalog(addvo);
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg","添加成功");
        return result;
    }

    /**
     * 添加目录节点
     * @param addVO
     * @return
     */
    @Override
    public Map<String, Object> addLabelCatalog(LabelCatalog addVO) {
        Map<String,Object> result = new HashMap<>();
        LabelCatalog catalog = BeanUtil.create(addVO,new LabelCatalog());
        catalog.setCreateDate(DateUtil.getCurrentTime());
        catalog.setUpdateDate(DateUtil.getCurrentTime());
        catalog.setStatusDate(DateUtil.getCurrentTime());
        catalog.setUpdateStaff(UserUtil.loginId());
        catalog.setCreateStaff(UserUtil.loginId());
        catalog.setStatusCd(CommonConstant.STATUSCD_EFFECTIVE);
        labelCatalogMapper.insert(catalog);
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg","添加成功");
        return result;
    }


    /**
     * 通过目录节点获取标签列表
     * @param catalogId
     * @return
     */
    @Override
    public Map<String, Object> listLabelByCatalogId(Long catalogId) {
        Map<String,Object> result = new HashMap<>();
        List<Label> labelList = labelMapper.findLabelListByCatalogId(catalogId);
        List<LabelDTO> voList = new ArrayList<>();
        for (Label label : labelList){
            LabelDTO vo = BeanUtil.create(label,new LabelDTO());
            voList.add(vo);
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",voList);
        return result;
    }

    /**
     * 标签树
     * @return
     */
    @Override
    public Map<String, Object> listLabelCatalog(String rightOperand, String newOrOld) {
//        List<Label> resuList = new ArrayList<>();
//        List<Label> labels = labelMapper.selectAllByCondition();
//        List<LabelValue> vas = labelValueMapper.selectAll();
//        List<Long> idlist = new ArrayList<>();
//        for (LabelValue value : vas){
//            idlist.add(value.getInjectionLabelId());
//        }
//        for (Label label : labels){
//            if (!idlist.contains(label.getInjectionLabelId())){
//                label.setConditionType("4");
//                label.setOperator("2000,3000,1000,4000,6000,5000,7000,7200");
//                labelMapper.updateByPrimaryKey(label);
//            }
//        }

        if ("".equals(rightOperand)){
            rightOperand = "0";
        }
        if ("".equals(newOrOld)){
            newOrOld = "0";
        }
        Map<String,Object> result = new HashMap<>();
        List<CatalogTreeParent> resultTree = new ArrayList<>();
        List<CatalogTreeParentVO> resultTreeVO = new ArrayList<>();
//        List<LabelCatalog> parentList = new ArrayList<>();

//        List<LabelCatalog> labelCatalogList = labelCatalogMapper.findByParentId(String.valueOf(0));
//        for(LabelCatalog labelCatalog : labelCatalogList) {
//            if(labelCatalog.getRemark().equals(newOrOld)) {
//                parentList.add(labelCatalog);
//            }
//        }
        List<LabelCatalog> parentList = labelCatalogMapper.findByParentId(String.valueOf(0));
        List<Label> allLabels = labelMapper.selectByScope(Long.valueOf(1),rightOperand.equals("1") ? null : rightOperand);
        List<LabelCatalog> allCatalogs = labelCatalogMapper.selectAll(newOrOld);
        List<LabelValue> valueList = labelValueMapper.selectAll();
        if(newOrOld.equals("0")) {
            for (LabelCatalog parent : parentList) {
                CatalogTreeParent parentTree = new CatalogTreeParent();
                parentTree.setInjectionLabelId(parent.getCatalogId());
                parentTree.setInjectionLabelName(parent.getCatalogName());

                List<LabelCatalogTree> onceTreeList = new ArrayList<>();
                List<LabelCatalog> firstList = labelCatalogMapper.findByParentId(parent.getCatalogCode());
                for (LabelCatalog first : firstList) {
                    if(!first.getRemark().equals(newOrOld)) {
                        continue;
                    }
                    LabelCatalogTree firstTree = new LabelCatalogTree();
                    firstTree.setInjectionLabelId(first.getCatalogId());
                    firstTree.setInjectionLabelName(first.getCatalogName());

                    List<CatalogTreeTwo> twiceTreeList = new ArrayList<>();
                    List<LabelCatalog> twiceList = getCatalogListByParentId(allCatalogs, first.getCatalogCode());
                    for (LabelCatalog twice : twiceList) {
                        CatalogTreeTwo twiceTree = new CatalogTreeTwo();
                        twiceTree.setInjectionLabelId(twice.getCatalogId());
                        twiceTree.setInjectionLabelName(twice.getCatalogName());

                        List<CatalogTreeThree> thirdTreeList = new ArrayList<>();
                        List<LabelCatalog> thirdList = getCatalogListByParentId(allCatalogs, twice.getCatalogCode());
                        for (LabelCatalog third : thirdList) {
                            CatalogTreeThree thirdTree = new CatalogTreeThree();
                            thirdTree.setInjectionLabelId(third.getCatalogId());
                            thirdTree.setInjectionLabelName(third.getCatalogName());

                            List<LabelVO> labelVOList = new ArrayList<>();
                            for (Label label : allLabels) {
                                if (label.getCatalogId() == null || !label.getCatalogId().equals(third.getCatalogCode())) {
                                    continue;
                                }
                                List<LabelValue> values = new ArrayList<>();
                                for (LabelValue value : valueList) {
                                    if (value.getInjectionLabelId() != null && value.getInjectionLabelId().equals(label.getInjectionLabelId())) {
                                        values.add(value);
                                    }
                                }
                                LabelVO vo = ChannelUtil.map2LabelVO(label, values);
                                labelVOList.add(vo);
                            }
                            thirdTree.setChildren(labelVOList);
                            thirdTreeList.add(thirdTree);
                        }
                        twiceTree.setChildren(thirdTreeList);
                        twiceTreeList.add(twiceTree);
                    }
                    firstTree.setChildren(twiceTreeList);
                    onceTreeList.add(firstTree);
                }
                parentTree.setChildren(onceTreeList);
                resultTree.add(parentTree);
            }
        }else if(newOrOld.equals("1")) {
            for (LabelCatalog parent : parentList) {
                CatalogTreeParentVO parentTree = new CatalogTreeParentVO();
                parentTree.setInjectionLabelId(parent.getCatalogId());
                parentTree.setInjectionLabelName(parent.getCatalogName());

                List<CatalogTreeFirstVO> onceTreeList = new ArrayList<>();
                List<LabelCatalog> firstList = labelCatalogMapper.findByParentId(parent.getCatalogCode());
                for (LabelCatalog first : firstList) {
                    if(!first.getRemark().equals(newOrOld)) {
                        continue;
                    }
                    CatalogTreeFirstVO firstTree = new CatalogTreeFirstVO();
                    firstTree.setInjectionLabelId(first.getCatalogId());
                    firstTree.setInjectionLabelName(first.getCatalogName());

                    List<CatalogTreeTwoVO> twiceTreeList = new ArrayList<>();
                    List<LabelCatalog> twiceList = getCatalogListByParentId(allCatalogs, first.getCatalogCode());
                    for (LabelCatalog twice : twiceList) {
                        CatalogTreeTwoVO twiceTree = new CatalogTreeTwoVO();
                        twiceTree.setInjectionLabelId(twice.getCatalogId());
                        twiceTree.setInjectionLabelName(twice.getCatalogName());

                        List<LabelVO> labelVOList = new ArrayList<>();
                        for (Label label : allLabels) {
                            if (label.getCatalogId() == null || !label.getOriginalLabLevel1Code().equals(twice.getCatalogCode())) {
                                continue;
                            }
                            List<LabelValue> values = new ArrayList<>();
                            for (LabelValue value : valueList) {
                                if (value.getInjectionLabelId() != null && value.getInjectionLabelId().equals(label.getInjectionLabelId())) {
                                    values.add(value);
                                }
                            }
                            LabelVO vo = ChannelUtil.map2LabelVO(label, values);
                            labelVOList.add(vo);
                        }
                        twiceTree.setChildren(labelVOList);
                        twiceTreeList.add(twiceTree);
                    }
                    firstTree.setChildren(twiceTreeList);
                    onceTreeList.add(firstTree);
                }
                parentTree.setChildren(onceTreeList);
                resultTreeVO.add(parentTree);
            }
        }
        //自有标签
        List<Label> sysLabel = labelMapper.selectByScope(0L,null);
        if(newOrOld.equals("0")) {
            List<LabelCatalogTree> sysTreeList = new ArrayList<>();
            CatalogTreeParent treeParent = new CatalogTreeParent();
            for (Label la : sysLabel) {
                List<LabelValue> values = new ArrayList<>();
                for (LabelValue value : valueList) {
                    if (value.getInjectionLabelId() != null && value.getInjectionLabelId().equals(la.getInjectionLabelId())) {
                        values.add(value);
                    }
                }
                LabelVO vo = ChannelUtil.map2LabelVO(la, values);
                LabelCatalogTree tree = BeanUtil.create(vo, new LabelCatalogTree());
                sysTreeList.add(tree);
            }
            treeParent.setInjectionLabelId(-1L);
            treeParent.setInjectionLabelName("自有标签");
            treeParent.setChildren(sysTreeList);
            resultTree.add(treeParent);
        }else if(newOrOld.equals("1")) {
            List<CatalogTreeFirstVO> sysTreeList = new ArrayList<>();
            CatalogTreeParentVO treeParent = new CatalogTreeParentVO();
            for (Label la : sysLabel) {
                List<LabelValue> values = new ArrayList<>();
                for (LabelValue value : valueList) {
                    if (value.getInjectionLabelId() != null && value.getInjectionLabelId().equals(la.getInjectionLabelId())) {
                        values.add(value);
                    }
                }
                LabelVO vo = ChannelUtil.map2LabelVO(la, values);
                CatalogTreeFirstVO tree = BeanUtil.create(vo, new CatalogTreeFirstVO());
                sysTreeList.add(tree);
            }
            treeParent.setInjectionLabelId(-1L);
            treeParent.setInjectionLabelName("自有标签");
            treeParent.setChildren(sysTreeList);
            resultTreeVO.add(treeParent);
        }
        result.put("resultCode",CODE_SUCCESS);
        if(newOrOld.equals("0")) {
            result.put("resultMsg", resultTree);
        }else if(newOrOld.equals("1")) {
            result.put("resultMsg", resultTreeVO);
        }
        return result;
    }

    private List<LabelCatalog> getCatalogListByParentId(List<LabelCatalog> allList,String catalogCode){
        List<LabelCatalog> resultList = new ArrayList<>();
        for (LabelCatalog catalog : allList){
            if (!catalog.getParentId().equals(catalogCode)){
                continue;
            }
            resultList.add(catalog);
        }
        return resultList;
    }
}
