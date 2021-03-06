package com.zjtelcom.cpct.dto.event;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 事件源接口配置详细信息
 * @Author pengy
 * @Date 2018/6/26 14:23
 */
@Data
public class InterfaceCfgDetail implements Serializable {

    private List<InterfaceCfgParam> interfaceCfgParams;

}
