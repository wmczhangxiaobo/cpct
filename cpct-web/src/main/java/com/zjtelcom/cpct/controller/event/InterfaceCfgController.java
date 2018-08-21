package com.zjtelcom.cpct.controller.event;

import com.zjtelcom.cpct.controller.BaseController;
import com.zjtelcom.cpct.domain.event.InterfaceCfg;
import com.zjtelcom.cpct.service.event.InterfaceCfgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;


@RestController
@RequestMapping("${adminPath}/interfaceCfg")
public class InterfaceCfgController extends BaseController {
    @Autowired
    private InterfaceCfgService interfaceCfgService;

    @PostMapping("createInterfaceCfg")
    @CrossOrigin
    public Map<String, Object> createInterfaceCfg(@RequestBody InterfaceCfg interfaceCfg) {
        Map<String,Object> result = new HashMap<>();
        try {
            result = interfaceCfgService.createInterfaceCfg(interfaceCfg);
        } catch (Exception e) {
            logger.error("[op:ScriptController] fail to createInterfaceCfg",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg"," fail to createInterfaceCfg");
            return result;
        }
        return result;
    }

    @PostMapping("modInterfaceCfg")
    @CrossOrigin
    public Map<String, Object> modInterfaceCfg(@RequestBody InterfaceCfg interfaceCfg) {
        Map<String,Object> result = new HashMap<>();
        try {
            result = interfaceCfgService.modInterfaceCfg(interfaceCfg);
        } catch (Exception e) {
            logger.error("[op:ScriptController] fail to modInterfaceCfg",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg"," fail to modInterfaceCfg");
            return result;
        }
        return result;
    }

    @PostMapping("delInterfaceCfg")
    @CrossOrigin
    public Map<String, Object> delInterfaceCfg(@RequestBody InterfaceCfg interfaceCfg) {
        Map<String,Object> result = new HashMap<>();
        try {
            result = interfaceCfgService.delInterfaceCfg(interfaceCfg);
        } catch (Exception e) {
            logger.error("[op:ScriptController] fail to delInterfaceCfg",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg"," fail to delInterfaceCfg");
            return result;
        }
        return result;
    }

    @PostMapping("listInterfaceCfg")
    @CrossOrigin
    public Map<String, Object> listInterfaceCfg(@RequestBody InterfaceCfg interfaceCfg) {
        Map<String,Object> result = new HashMap<>();
        try {
            result = interfaceCfgService.listInterfaceCfg(interfaceCfg);
        } catch (Exception e) {
            logger.error("[op:ScriptController] fail to listInterfaceCfg",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg"," fail to listInterfaceCfg");
            return result;
        }
        return result;
    }

    @PostMapping("getInterfaceCfgDetail")
    @CrossOrigin
    public Map<String, Object> getInterfaceCfgDetail(@RequestBody InterfaceCfg interfaceCfg) {
        Map<String,Object> result = new HashMap<>();
        try {
            result = interfaceCfgService.getInterfaceCfgDetail(interfaceCfg);
        } catch (Exception e) {
            logger.error("[op:ScriptController] fail to getInterfaceCfgDetail",e);
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg"," fail to getInterfaceCfgDetail");
            return result;
        }
        return result;
    }



}
