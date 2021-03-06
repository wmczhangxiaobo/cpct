package com.zjtelcom.cpct.service.impl.channel;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjtelcom.cpct.common.Page;
import com.zjtelcom.cpct.dao.channel.CatalogItemMapper;
import com.zjtelcom.cpct.dao.channel.CatalogMapper;
import com.zjtelcom.cpct.dao.channel.ObjCatItemRelMapper;
import com.zjtelcom.cpct.domain.channel.CatalogItem;
import com.zjtelcom.cpct.domain.channel.Offer;
import com.zjtelcom.cpct.domain.channel.PpmProduct;
import com.zjtelcom.cpct.dto.channel.CatalogItemDetail;
import com.zjtelcom.cpct.dto.channel.CatalogItemTree;
import com.zjtelcom.cpct.dto.event.Catalog;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.channel.CatalogService;
import com.zjtelcom.cpct.util.BeanUtil;
import com.zjtelcom.cpct.dao.offer.CatalogItemProdMapper;
import com.zjtelcom.cpct.dao.offer.OfferProdMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;
import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;

@Service
public class CatalogCpcServiceImpl extends BaseService implements CatalogService {
    @Autowired
    private CatalogItemProdMapper catalogItemMapper;
    @Autowired
    private OfferProdMapper offerMapper;
    @Autowired
    private ObjCatItemRelMapper objCatItemRelMapper;
    @Autowired
    private CatalogMapper catalogMapper;
    @Autowired
    private CatalogItemMapper catItemMapper;

    /**
     * 营销活动目录
     * @return
     */
    @Override
    public Map<String, Object> listCatalogItemTree() {
        Map<String,Object> result = new HashMap<>();
        Catalog catalog = catalogMapper.selectByType("1800");
        if (catalog==null){
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","目录查询出错，请联系管理员");
            return result;
        }
        List<CatalogItemDetail> resultList = new ArrayList<>();
        List<CatalogItem> parentList = catItemMapper.selectByParentId(0L);
        for (CatalogItem parent : parentList) {
            CatalogItemDetail detail = BeanUtil.create(parent, new CatalogItemDetail());
            list(detail);
            resultList.add(detail);
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",resultList);
        return result;
    }


    private void  list(CatalogItemDetail cat){
        List<CatalogItemDetail> childList = new ArrayList<>();
        List<CatalogItem> list = catItemMapper.selectByParentId(cat.getCatalogItemId());
        for (CatalogItem cata : list) {
            CatalogItemDetail detail = BeanUtil.create(cata, new CatalogItemDetail());
            List<CatalogItem> xxx = catItemMapper.selectByParentId(detail.getCatalogItemId());
            if (!xxx.isEmpty()){
                list(detail);
            }
            childList.add(detail);
        }
        cat.setChildList(childList);
    }


    /**
     * 通过目录节点id获取销售品信息
     * @param catalogId
     * @return
     */
    @Override
    public Map<String, Object> listOfferByCatalogId(Long catalogId,String productName,Integer page,Integer pageSize) {
        Map<String,Object> result = new HashMap<>();
        List<PpmProduct> offerList = new ArrayList<>();
        PageHelper.startPage(page,pageSize);
//        List<OfferCatalogLocation> catalogLocations = offerCatalogLocationMapper.selectByCatalogItemId(catalogId);
//
//        for (OfferCatalogLocation catalogLocation : catalogLocations){
//            Offer offer = offerMapper.selectByPrimaryKeyAndName(catalogLocation.getOfferId(),productName);
//            if (offer!=null){
//                PpmProduct product = new PpmProduct();
//                product.setProductId(Long.valueOf(offer.getOfferId()));
//                product.setProductName(offer.getOfferName());
//               offerList.add(product);
//            }
//        }
        List<Offer> productList = offerMapper.listByCatalogItemId(catalogId,productName);
        Page pa = new Page(new PageInfo(productList));
        for (Offer offer : productList){
            PpmProduct product = new PpmProduct();
            product.setProductId(Long.valueOf(offer.getOfferId()));
            product.setProductName(offer.getOfferName());
            offerList.add(product);
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",offerList);
        result.put("page",pa);
        return result;
    }

    /**
     * 销售品目录树
     * @return
     */
    @Override
    public Map<String, Object> listProductTree() {
        Map<String,Object> result = new HashMap<>();
        //所有销售品目录
        List<CatalogItem> catalogItems = catalogItemMapper.selectByCatalog(1L);
        List<CatalogItemTree> catalogItemTrees = generateTree(catalogItems);
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",catalogItemTrees);
        return result;
    }


    /**
     * 生成树
     */
    private List<CatalogItemTree> generateTree(List<CatalogItem> catalogItemList) {
        List<CatalogItemTree> dtoList = new ArrayList<>();
        for (CatalogItem catalogItem : catalogItemList) {
            if (catalogItem.getParCatalogItemId() == null) {
                CatalogItemTree catalogItemTree = new CatalogItemTree();
                catalogItemTree.setId(catalogItem.getCatalogItemId());
                catalogItemTree.setName(catalogItem.getCatalogItemName());
                dtoList.add(catalogItemTree);
            }
            // 为一级菜单设置子菜单，getChild是递归调用的
            for (CatalogItemTree catalogItemTree : dtoList) {
                catalogItemTree.setChildren(getChild(catalogItemTree.getId(),catalogItemList));
            }
        }
        return dtoList;
    }

    /**
     * 递归查找子菜单
     */
    private List<CatalogItemTree> getChild(Long id, List<CatalogItem> rootMenu) {
        // 子菜单
        List<CatalogItemTree> childList = new ArrayList<>();
        for (CatalogItem catalogItem : rootMenu) {
            // 遍历所有节点，将父菜单id与传过来的id比较
            if (catalogItem.getParCatalogItemId()!=null && catalogItem.getParCatalogItemId()!=0) {
                if (catalogItem.getParCatalogItemId().equals(id)) {
                    CatalogItemTree catalogItemTree = new CatalogItemTree();
                    catalogItemTree.setId(catalogItem.getCatalogItemId());
                    catalogItemTree.setName(catalogItem.getCatalogItemName());
                    childList.add(catalogItemTree);
                }
            }
        }
        // 把子菜单的子菜单再循环一遍
        for (CatalogItemTree catalogItemTree : childList) {
            List<CatalogItem> list = new ArrayList<>();
            for (CatalogItem catalogItem : rootMenu){
                if (catalogItem.getParCatalogItemId()!=null && catalogItem.getParCatalogItemId().equals(catalogItemTree.getId())){
                    list.add(catalogItem);
                }
            }
//            List<CatalogItem> list = catalogItemMapper.selectByParentId(catalogItemTree.getId());
            if (!list.isEmpty()) {
                // 递归
                catalogItemTree.setChildren(getChild(catalogItemTree.getId(), rootMenu));
            }
        }
        // 递归退出条件
        if (childList.isEmpty()) {
            return null;
        }
        return childList;
    }
}
