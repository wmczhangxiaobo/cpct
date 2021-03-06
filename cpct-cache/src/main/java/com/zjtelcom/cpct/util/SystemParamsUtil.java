package com.zjtelcom.cpct.util;

import com.zjtelcom.cpct.dao.system.SysParamsMapper;
import com.zjtelcom.cpct.domain.system.SysParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Auther: anson
 * @Date: 2018/12/20
 * @Description:静态参数工具类-开关配置
 */
@Component
public class SystemParamsUtil {

    private static String IS_OPEN_SYNC="";                 //是否开启同步  0不开启同步  1开启同步(所有模块的同步)   2开启同步(但是事件和活动模块同步功能关闭)

    private static String SYNC_VALUE="IS_OPEN_SYNC";      //数据库中系统参数表同步开关的key


    @Value("${spring.profiles.active}")
    private String springActive;                           //如果值为pst不需要同步修改到es环境的redis

    private static String active;

    //在静态方法中使用配置变量
    @PostConstruct
    public void getApiToken() {
        active = this.springActive;
    }

    public static String getActiveStatus() {
        return active;
    }


    private static final Logger log = Logger.getLogger(SystemParamsUtil.class);

    /**
     * 获取同步开关状态  由于存在分布式部署 所以开关数据只能实时从redis获取会比较好
     * @return
     */
    public synchronized static String getSyncValue(){
        RedisUtils bean = null;
        try {
            bean = SpringUtil.getBean(RedisUtils.class);
            IS_OPEN_SYNC= (String) bean.get(SYNC_VALUE);    //先从redis获取数据
        } catch (Exception e) {
            e.printStackTrace();
            log.info("redis获取IS_OPEN_SYNC值失败");
        }
        if(StringUtils.isBlank(IS_OPEN_SYNC)){
            SysParamsMapper sysParamsMapper=SpringUtil.getBean(SysParamsMapper.class);
            List<SysParams> sysParams = sysParamsMapper.listParamsByKeyForCampaign(SYNC_VALUE);
            if (sysParams.isEmpty()) {
                //如果数据库没有值 则返回默认不开启
                IS_OPEN_SYNC="0";
            }else{
                if ("1".equals(sysParams.get(0).getParamValue())) {
                    IS_OPEN_SYNC = "1";
                }else if("2".equals(sysParams.get(0).getParamValue())){
                    //除了事件和活动  其他的模块都开启同步功能
                    IS_OPEN_SYNC="2";
                }else{
                    //不开启同步
                    IS_OPEN_SYNC="0";
                }
            }
            //重新存入存入redis
            try {
                bean.set(SYNC_VALUE,IS_OPEN_SYNC);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("redis存储IS_OPEN_SYNC值失败");
            }
            return IS_OPEN_SYNC;
        }
        return IS_OPEN_SYNC;
    }



    /**
     * 初始化系统参数值  修改redis里的值
     * 测试环境只用修改测试环境的redis就行
     * 生产环境需要修改es那套redis的值，因为实时和批量模块用的redis配置是es那套redis
     */
    public static void initValue(SysParams sysParams){
        try {
            RedisUtils bean = SpringUtil.getBean(RedisUtils.class);
            bean.set(sysParams.getParamKey(),sysParams.getParamValue());
            if(StringUtils.isNotBlank(sysParams.getParamKey())){
                //如果是yz的同步操作
                if(isYZ(sysParams.getParamKey())&&!"pst".equals(getActiveStatus())){
                    log.info("非pst环境的 "+sysParams.getParamKey()+"值修改为 "+sysParams.getParamValue());
                    RedisUtils_es es = SpringUtil.getBean(RedisUtils_es.class);
                    es.set(sysParams.getParamKey(),sysParams.getParamValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("redis存储"+sysParams.getParamKey()+"值失败");
            IS_OPEN_SYNC="";
        }
    }

    /**
     * 判断是否为实时或者批量因子查询的开关操作,或者试算标签数据类型是否有效
     * @param str
     * @return
     */
    private static boolean isYZ(String str){
        if(str.equals("IS_ES_FLG") ||str.equals("YZS_ES_FLG") ||str.equals("YZ_ES_FLG")||str.equals("TRIAL_LABEL_DATA_TYPE")||str.equals("ISALE_CHECK_FLG")){
              return  true;
        }else{
              return false;
        }
    }

    /**
     * 同步开关的key
     * @return
     */
    public static String getSyncName(){
        return SYNC_VALUE;
    }






}
