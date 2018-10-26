package com.zjtelcom.cpct.controller.system;

import com.alibaba.fastjson.JSON;
import com.zjtelcom.cpct.controller.BaseController;
import com.zjtelcom.cpct.domain.system.SysStaff;
import com.zjtelcom.cpct.dto.system.SysStaffDTO;
import com.zjtelcom.cpct.enums.ErrorCode;
import com.zjtelcom.cpct.service.system.SysStaffService;
import com.zjtelcom.cpct.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.rmi.CORBA.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户模块控制器
 */
@RestController
@RequestMapping("${adminPath}/staff")
public class SysStaffController extends BaseController {

    @Autowired
    private SysStaffService sysStaffService;

    /**
     * 查询员工列表（分页）
     *
     * @return
     */
    @RequestMapping(value = "listStaff", method = RequestMethod.POST)
    @CrossOrigin
    public String listStaff(@RequestBody Map<String,String> params) {
        Map result = new HashMap();

        try {
            result = sysStaffService.listStaff(params);
        } catch (Exception e) {
            logger.error("[op:SysStaffController] fail to eventList Exception: ", e);
            return initFailRespInfo(ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorMsg(), ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorCode());
        }
        return JSON.toJSON(result).toString();
    }

    /**
     * 根据员工id查询员工信息
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "getStaff", method = RequestMethod.POST)
    @CrossOrigin
    public String getStaff(@RequestBody Map<String,String> params) {
        Map result = new HashMap();

        Long staffId = Long.parseLong(params.get("staffId"));

        try {
            result = sysStaffService.getStaff(staffId);
        } catch (Exception e) {
            logger.error("[op:SysStaffController] fail to eventList Exception: ", e);
            return initFailRespInfo(ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorMsg(), ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorCode());
        }
        return JSON.toJSON(result).toString();
    }

    /**
     * 新增员工
     *
     * @param sysStaffDTO
     * @return
     */
    @RequestMapping(value = "saveStaff", method = RequestMethod.POST)
    @CrossOrigin
    public String saveStaff(@RequestBody SysStaffDTO sysStaffDTO) {
        Map result = new HashMap();
        try {
            result = sysStaffService.saveStaff(sysStaffDTO);
        } catch (Exception e) {
            logger.error("[op:SysStaffController] fail to saveStaff Exception: ", e);
            return initFailRespInfo(ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorMsg(), ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorCode());
        }

        return JSON.toJSON(result).toString();
    }

    /**
     * 修改员工
     *
     * @param sysStaff
     * @return
     */
    @RequestMapping(value = "updateStaff", method = RequestMethod.POST)
    @CrossOrigin
    public String updateStaff(@RequestBody SysStaffDTO sysStaff) {
        Map result = new HashMap();
        try {
            result = sysStaffService.updateStaff(sysStaff);
        } catch (Exception e) {
            logger.error("[op:SysStaffController] fail to updateStaff Exception: ", e);
            return initFailRespInfo(ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorMsg(), ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorCode());
        }

        return JSON.toJSON(result).toString();
    }

    /**
     * 修改员工账号状态
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "changeStatus", method = RequestMethod.POST)
    @CrossOrigin
    public String changeStatus(@RequestBody Map<String,String> params) {
        Map result = new HashMap();

        Long staffId = Long.parseLong(params.get("staffId"));
        Long status = Long.parseLong(params.get("status"));

        try {
            result = sysStaffService.changeStatus(staffId, status);
        } catch (Exception e) {
            logger.error("[op:SysStaffController] fail to updateStaff Exception: ", e);
            return initFailRespInfo(ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorMsg(), ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorCode());
        }

        return JSON.toJSON(result).toString();
    }

    /**
     * 修改密码
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "updatePassword", method = RequestMethod.POST)
    @CrossOrigin
    public String updatePassword(@RequestBody Map<String,String> params) {
        Map result = new HashMap();

        Long staffId = Long.parseLong(params.get("staffId"));
        String password = params.get("password");

        try {
            result = sysStaffService.updatePassword(staffId, password);
        } catch (Exception e) {
            logger.error("[op:SysStaffController] fail to updateStaff Exception: ", e);
            return initFailRespInfo(ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorMsg(), ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorCode());
        }

        return JSON.toJSON(result).toString();
    }

    /**
     * 获取当前用户信息
     */
    @RequestMapping(value = "loginInfo", method = RequestMethod.POST)
    @CrossOrigin
    public String loginInfo() {
        Map result = new HashMap();
        Long staffId = UserUtil.loginId();
        try {
            result = sysStaffService.getStaff(staffId);
        }catch (Exception e) {
            logger.error("[op:SysStaffController] fail to loginInfo Exception: ", e);
            return initFailRespInfo(ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorMsg(), ErrorCode.SEARCH_EVENT_LIST_FAILURE.getErrorCode());
        }
        return JSON.toJSON(result).toString();
    }

}
