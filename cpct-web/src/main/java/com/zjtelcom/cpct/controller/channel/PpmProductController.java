package com.zjtelcom.cpct.controller.channel;

import com.zjtelcom.cpct.controller.BaseController;
import com.zjtelcom.cpct.domain.channel.PpmProduct;
import com.zjtelcom.cpct.dto.channel.ProductParam;
import com.zjtelcom.cpct.service.channel.ProductService;
import com.zjtelcom.cpct.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;
import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;

@RestController
@RequestMapping("${adminPath}/ppmProduct")
public class PpmProductController extends BaseController  {

    @Autowired
    private ProductService productService;

    /**
     * 获取销售品列表
     */
    @PostMapping("getProductList")
    @CrossOrigin
    public Map<String,Object> getProductList(@RequestBody HashMap<String,String> params){
        Map<String ,Object> result = new HashMap<>();
        Long userId = UserUtil.loginId();
        List<PpmProduct> productList = new ArrayList<>();
        try {
            productList = productService.getProductList(userId,params.get("productName"));
        }catch (Exception e){
            logger.error("[op:PpmProductController] fail to getProductList",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg"," fail to addCamScript");
            return result;
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",productList);
        return result;
    }


    /**
     * 添加规则下的销售品
     * @param
     * @return
     */
    @PostMapping("addProductRule")
    @CrossOrigin
    public Map<String, Object> addProductRule(@RequestBody ProductParam param) {
        Map<String ,Object> result = new HashMap<>();
        Long userId = UserUtil.loginId();
        try {
            result = productService.addProductRule(userId,param.getIdList());
        }catch (Exception e){
            logger.error("[op:PpmProductController] fail to addProductRule",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg"," fail to addProductRule");
            return result;
        }
        return result;
    }

    /**
     * 添加备注
     * @param params
     * @return
     */
    @PostMapping("editProductRule")
    @CrossOrigin
    public Map<String, Object> editProductRule(@RequestBody HashMap<String,Object> params)  {
        Map<String ,Object> result = new HashMap<>();
        Long userId = UserUtil.loginId();
        try {
            String remark = null;
            Long ruleId = Long.valueOf(params.get("ruleId").toString());
            if (params.get("remark")!=null){
                remark = params.get("remark").toString();
            }
            result = productService.editProductRule(userId,ruleId,remark);
        }catch (Exception e){
            logger.error("[op:PpmProductController] fail to getProductList",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg"," fail to editProductRule");
            return result;
        }
        return result;
    }

    /**
     * 获取规则下的销售品列表
     * @return
     */
    @PostMapping("getProductRuleList")
    @CrossOrigin
    public Map<String, Object> getProductRuleList(@RequestBody ProductParam ruleParam) {
        Map<String ,Object> result = new HashMap<>();
        Long userId = UserUtil.loginId();
        try {
            result = productService.getProductRuleList(userId,ruleParam.getIdList());
        }catch (Exception e){
            logger.error("[op:PpmProductController] fail to getProductRuleList",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg"," fail to getProductRuleList");
            return result;
        }
        return result;
    }

    /**
     * 删除规则的销售品
     * @return
     */
    @PostMapping("delProductRule")
    @CrossOrigin
    public Map<String, Object> delProductRule(@RequestBody HashMap<String,Object> params) {
        Map<String, Object> result = new HashMap<>();
        Long userId = UserUtil.loginId();
        try {
            Long ruleId = Long.valueOf(params.get("ruleId").toString());
            result = productService.delProductRule(userId, ruleId);
        } catch (Exception e) {
            logger.error("[op:PpmProductController] fail to delProductRule", e);
            result.put("resultCode", CODE_FAIL);
            result.put("resultMsg", " fail to delProductRule");
            return result;
        }
        return result;
    }

}
