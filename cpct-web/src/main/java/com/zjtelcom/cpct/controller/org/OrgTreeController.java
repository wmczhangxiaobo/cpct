package com.zjtelcom.cpct.controller.org;

import com.alibaba.fastjson.JSON;
import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.controller.BaseController;
import com.zjtelcom.cpct.service.org.OrgTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: anson
 * @Date: 2018/9/30
 * @Description:营销组织树
 */
@RestController
@RequestMapping("${adminPath}/orgTree")
public class OrgTreeController extends BaseController {

    @Autowired
    private OrgTreeService orgTreeService;


    /**
     * 测试营销组织树插入
     * @return
     */
    @RequestMapping("fileToSql")
    @CrossOrigin
    public String batchEvent(@RequestBody Map<String, Object> params){
        Map<String, Object> map=new HashMap<>();
        long l = System.currentTimeMillis();
        String name= (String) params.get("name");
        String path="D:\\code\\"+name;
            orgTreeService.getDataByFtp(path);
            map.put("resultCode", CommonConstant.CODE_FAIL);
            map.put("resultMsg", "success");
        System.out.println("插入完毕 耗时:"+(System.currentTimeMillis()-l));
        return  JSON.toJSONString(map);
    }


    /**
     * 通过菜单id查询其子菜单 营销组织树
     * @param params
     * @return
     */
    @RequestMapping("selectOrgTree")
    @CrossOrigin
    public String selectOrg(@RequestBody Map<String, Object> params){
        Map<String, Object> maps = new HashMap<>();
        try{
        maps = orgTreeService.selectBySumAreaId(params);
        } catch (Exception e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            maps.put("resultMsg", e.getMessage());
            logger.error("selectOrg通过菜单id查询其子菜单失败！Exception: ", e.getMessage());
        }
        return JSON.toJSONString(maps);
    }

    /**
     * 通过菜单id查询其子菜单 营销组织树权限控制 c1 c2 c3 c4 c5
     * @param params
     * @return
     */
    @RequestMapping("selectOrgTreeForUser")
    @CrossOrigin
    public String selectOrgTreeForUser(@RequestBody Map<String, Object> params){
        Map<String, Object> maps = new HashMap<>();
        try{
            maps = orgTreeService.selectOrgTreeForUser(params);
        } catch (Exception e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            maps.put("resultMsg", e.getMessage());
            logger.error("selectOrgTreeForUser通过菜单id查询其子菜单失败！Exception: ", e);
        }
        return JSON.toJSONString(maps);
    }



    /**
     *  组织数模糊查询
     */
    @RequestMapping("fuzzyQuery")
    @CrossOrigin
    public String fuzzyQueryOrgTree(@RequestBody Map<String, Object> params){
        Map<String, Object> maps = new HashMap<>();
        try{
            maps = orgTreeService.fuzzyQuery(params);
        } catch (Exception e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            maps.put("resultMsg", e.getMessage());
            logger.error("组织数模糊查询失败！Exception: ", e.getMessage());
        }
        return JSON.toJSONString(maps);
    }

    /**
     * 组织id查询名称信息
     * @param params
     * @return
     */
    @RequestMapping("selectByAreaId")
    @CrossOrigin
    public String selectByAreaId(@RequestBody Map<String, Object> params){
        Map<String, Object> maps = new HashMap<>();
        try{
            maps = orgTreeService.selectByAreaId(params);
        } catch (Exception e) {
            maps.put("resultCode", CommonConstant.CODE_FAIL);
            maps.put("resultMsg", e.getMessage());
            logger.error("名称查询失败！Exception: ", e.getMessage());
        }
        return JSON.toJSONString(maps);
    }


}
