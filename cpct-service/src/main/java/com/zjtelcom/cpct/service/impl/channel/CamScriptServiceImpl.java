package com.zjtelcom.cpct.service.impl.channel;

import com.zjtelcom.cpct.bean.RespInfo;
import com.zjtelcom.cpct.dao.channel.MktCamScriptMapper;
import com.zjtelcom.cpct.domain.channel.CamScript;
import com.zjtelcom.cpct.dto.channel.CamScriptAddVO;
import com.zjtelcom.cpct.dto.channel.CamScriptEditVO;
import com.zjtelcom.cpct.dto.channel.CamScriptVO;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.channel.CamScriptService;
import com.zjtelcom.cpct.util.BeanUtil;
import com.zjtelcom.cpct.util.ChannelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;
import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;

@Service
public class CamScriptServiceImpl extends BaseService implements CamScriptService {

    @Autowired
    private MktCamScriptMapper camScriptMapper;



    @Override
    public Map<String,Object> addCamScript(Long userId, CamScriptAddVO addVO) {
        Map<String,Object> result = new HashMap<>();
        CamScript script = BeanUtil.create(addVO,new CamScript());
        script.setCreateDate(new Date());
        script.setUpdateDate(new Date());
        script.setCreateStaff(userId);
        script.setUpdateStaff(userId);
        script.setStatusCd("1000");
        camScriptMapper.insert(script);
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultData","添加成功");
        return result;
    }

    @Override
    public Map<String,Object> editCamScript(Long userId, CamScriptEditVO editVO) {
        Map<String,Object> result = new HashMap<>();
        CamScript script = camScriptMapper.selectByPrimaryKey(editVO.getCamScriptId());
        if (script==null){
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","活动关联脚本信息不存在");
            return result;
        }
        BeanUtil.copy(editVO,script);
        script.setUpdateDate(new Date());
        script.setUpdateStaff(userId);
        camScriptMapper.updateByPrimaryKey(script);
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultData","修改成功");
        return result;
    }

    @Override
    public Map<String,Object> deleteCamScript(Long userId, List<Long> camScriptIdList) {
        Map<String,Object> result = new HashMap<>();
        for (Long id : camScriptIdList){
            CamScript script = camScriptMapper.selectByPrimaryKey(id);
            if (script==null){
                result.put("resultCode",CODE_FAIL);
                result.put("resultMsg","活动关联脚本信息不存在");
                return result;
            }
            camScriptMapper.deleteByPrimaryKey(id);
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultData","删除成功");
        return result;
    }

    @Override
    public Map<String,Object> getCamScriptList(Long userId, Long campaignId, Long evtContactConfId) {
        Map<String,Object> result = new HashMap<>();
        //todo  活动标识确定活动 推送渠道id确定渠道
        List<CamScriptVO> voList = new ArrayList<>();
        List<CamScript> scriptList = new ArrayList<>();
        try {
            scriptList = camScriptMapper.selectAll(campaignId,evtContactConfId);
            for (CamScript script : scriptList){
                CamScriptVO vo = ChannelUtil.map2CamScriptVO(script);
                voList.add(vo);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("[op:ChannelServiceImpl] fail to listChannel ", e);
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultData",voList);
        return result;
    }

    @Override
    public Map<String,Object> getCamScriptVODetail(Long userId, Long camScriptId) {
        return null;
    }
}
