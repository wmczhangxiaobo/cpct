package com.zjtelcom.cpct.service.impl.channel;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjtelcom.cpct.common.Page;
import com.zjtelcom.cpct.dao.channel.*;
import com.zjtelcom.cpct.dao.system.SysParamsMapper;
import com.zjtelcom.cpct.domain.channel.*;
import com.zjtelcom.cpct.domain.system.SysParams;
import com.zjtelcom.cpct.dto.channel.*;
import com.zjtelcom.cpct.enums.StatusCode;
import com.zjtelcom.cpct.request.channel.DisplayAllMessageReq;
import com.zjtelcom.cpct.request.channel.MessageReq;
import com.zjtelcom.cpct.service.BaseService;
import com.zjtelcom.cpct.service.channel.MessageLabelService;
import com.zjtelcom.cpct.util.BeanUtil;
import com.zjtelcom.cpct.util.DateUtil;
import com.zjtelcom.cpct.util.SystemParamsUtil;
import com.zjtelcom.cpct.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;
import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;

/**
 * 信息关联标签实现类
 */
@Service
public class MessageLabelServiceImpl extends BaseService implements MessageLabelService {

    @Autowired
    private MessageLabelMapper messageLabelMapper;
    @Autowired
    private InjectionLabelMapper injectionLabelMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private DisplayColumnMapper displayColumnMapper;
    @Autowired
    private DisplayColumnLabelMapper displayColumnLabelMapper;
    @Autowired
    private SysParamsMapper systemParamMapper;




    /**
     * 查询标签列表
     * @param labelIdList
     * @return
     */
    @Override
    public Map<String, Object> qureyLabelListByIdList(ProductParam labelIdList) {
        Map<String, Object> maps = new HashMap<>();
        List<Label> labelList = new ArrayList<>();
        for (Long id : labelIdList.getIdList()){
            Label label = injectionLabelMapper.selectByPrimaryKey(id);
            if (label==null){
                continue;
            }
            labelList.add(label);
        }
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", labelList);
        return maps;
    }

    /**
     * 获取展示列标签列表
     * @param req
     * @return
     */
    @Override
    public Map<String, Object> queryLabelListByDisplayId(DisplayColumn req) {
        Map<String, Object> maps = new HashMap<>();
        DisplayColumn displayColumn = displayColumnMapper.selectByPrimaryKey(req.getDisplayColumnId());
        List<DisplayColumnLabel> realList = displayColumnLabelMapper.findListByDisplayId(req.getDisplayColumnId());
        List<LabelDTO> labelList = new ArrayList<>();
        List<Long> messageTypes = new ArrayList<>();

        for (DisplayColumnLabel real : realList){
            Label label = injectionLabelMapper.selectByPrimaryKey(real.getInjectionLabelId());
            if (label==null){
                continue;
            }
            LabelDTO labelDTO = new LabelDTO();
            labelDTO.setInjectionLabelId(label.getInjectionLabelId());
            labelDTO.setInjectionLabelName(label.getInjectionLabelName());
            labelDTO.setLabelType(label.getLabelType());
            labelDTO.setMessageType(real.getMessageType());
            labelDTO.setLabelCode(label.getInjectionLabelCode());
            if(real.getLabelDisplayType() != null) {
                labelDTO.setDisplayTypeNum(real.getLabelDisplayType().split(",").length);
            }else {
                labelDTO.setDisplayTypeNum(0);
            }
            labelList.add(labelDTO);
            if (!messageTypes.contains(real.getMessageType())){
                messageTypes.add(real.getMessageType());
            }
        }
        List<MessageLabelInfo> mlInfoList = new ArrayList<>();
        for (int i = 0;i<messageTypes.size();i++){

            Long messageType = messageTypes.get(i);
            Message messages = messageMapper.selectByPrimaryKey(messageType);
            MessageLabelInfo info = BeanUtil.create(messages,new MessageLabelInfo());
            List<LabelDTO> dtoList = new ArrayList<>();
            for (LabelDTO dto : labelList){
                if (messageType.equals(dto.getMessageType())){
                    dtoList.add(dto);
                }
            }
            info.setLabelDTOList(dtoList);
            //判断是否选中
            if (dtoList.isEmpty()){
                info.setChecked("1");//false
            }else {
                info.setChecked("0");//true
            }
            mlInfoList.add(info);
        }
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg",mlInfoList);
        maps.put("labels",labelList);
        maps.put("displayColumn",displayColumn);
        return maps;

    }

    /**
     * 删除展示列
     * @param req
     * @return
     */
    @Override
    public Map<String, Object> delDisplayColumn(DisplayAllMessageReq req) {
        Map<String, Object> maps = new HashMap<>();
        final DisplayColumn displayColumn = displayColumnMapper.selectByPrimaryKey(req.getDisplayColumnId());
        if (displayColumn==null){
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", "展示列不存在");
            return maps;
        }
        displayColumnMapper.deleteByPrimaryKey(displayColumn.getDisplayColumnId());
        displayColumnLabelMapper.deleteByDisplayId(req.getDisplayColumnId());
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);



        return maps;
    }

    /**
     * 删除展示列标签关联
     * @param
     * @return
     */
    @Override
    public Map<String, Object> delColumnLabelRel(final Long displayId, final Long labelId) {
        Map<String, Object> maps = new HashMap<>();
        DisplayColumnLabel displayColumnLabel = displayColumnLabelMapper.findByDisplayIdAndLabelId(displayId,labelId);
        if (displayColumnLabel!=null){
            displayColumnLabelMapper.deleteByPrimaryKey(displayColumnLabel.getDisplayColumnLabelId());
        }
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", "删除成功");



        return maps;
    }

    @Override
    public Map<String, Object> qureyMessageLabelByMessageIdList(MessageReq req) {
        Map<String, Object> maps = new HashMap<>();
       List<MessageLabelDTO> dtoList = new ArrayList<>();
        for (Long messageId : req.getMessageId()){
            List<LabelDTO> labelDTOList = new ArrayList<>();
            MessageLabelDTO messageLabelDTO = new MessageLabelDTO();
            List<MessageLabel> messageLabelList = messageLabelMapper.qureyMessageLabelByMessageId(messageId);
            messageLabelDTO.setMessageId(messageId);
            //标签结果拼装
            queryLabelInfo(labelDTOList, messageLabelList);
            messageLabelDTO.setMessageLabelId(labelDTOList);
            dtoList.add(messageLabelDTO);
        }
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", dtoList);
        return maps;
    }

    private void queryLabelInfo(List<LabelDTO> labelDTOList, List<MessageLabel> messageLabelList) {
        for (MessageLabel messageLabel : messageLabelList) {
            LabelDTO labelDTO = new LabelDTO();
            labelDTO.setInjectionLabelId(messageLabel.getInjectionLabelId());
            Label label = injectionLabelMapper.selectByPrimaryKey(messageLabel.getInjectionLabelId());
            if (label==null){
                continue;
            }
            labelDTO.setInjectionLabelName(label.getInjectionLabelName());
            labelDTOList.add(labelDTO);
        }
    }

    /**
     * 查询出信息关联标签列表
     */
    @Override
    public Map<String, Object> qureyMessageLabel(Message message) {
        Map<String, Object> maps = new HashMap<>();
        MessageLabelDTO messageLabelDTO = new MessageLabelDTO();
        List<LabelDTO> labelDTOList = new ArrayList<>();
        List<MessageLabel> messageLabelList = messageLabelMapper.qureyMessageLabel(message);
        messageLabelDTO.setMessageId(message.getMessageId());
        queryLabelInfo(labelDTOList, messageLabelList);
        messageLabelDTO.setMessageLabelId(labelDTOList);
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);
        maps.put("messageLabelDTO", messageLabelDTO);
        return maps;
    }

    /**
     * 查询出所有信息
     */
    @Override
    public Map<String, Object> queryMessages(String displayColumnType) {
        Map<String, Object> maps = new HashMap<>();
        List<MessageVO> messageVOList = new ArrayList<>();
        List<Message> messageList = messageMapper.selectAll();
        for (Message message : messageList) {
            if (message.getMessageName().equals("固定信息") && displayColumnType!=null && displayColumnType.equals("2000")){
                continue;
            }
            MessageVO messageVO = BeanUtil.create(message,new MessageVO());
            messageVO.setChecked("0");
            Map<String,Object> labelMaps = qureyMessageLabel(message);
            if (labelMaps.get("resultCode").equals(CODE_SUCCESS)){
                MessageLabelDTO messageLabelDTO = (MessageLabelDTO)labelMaps.get("messageLabelDTO");
                List<LabelDTO> list = messageLabelDTO.getMessageLabelId();
                for (LabelDTO labelDTO : list){
                    labelDTO.setMessageType(message.getMessageId());
                }
                messageVO.setLabelDTOList(list);
            }
            messageVOList.add(messageVO);
        }
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);
        maps.put("messageVOList", messageVOList);
        return maps;
    }

    /**
     * 新增标签组
     */
    @Override
    public Map<String, Object> createLabelGroup(final DisplayColumn displayColumn) {
        Map<String, Object> maps = new HashMap<>();
        displayColumn.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
        displayColumn.setCreateStaff(UserUtil.loginId());
        displayColumn.setUpdateStaff(UserUtil.loginId());
        displayColumn.setCreateDate(DateUtil.getCurrentTime());
        displayColumn.setUpdateDate(DateUtil.getCurrentTime());
        displayColumn.setStatusDate(DateUtil.getCurrentTime());
        displayColumnMapper.insert(displayColumn);
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", displayColumn.getDisplayColumnId());


        return maps;
    }

    /**
     * 查询出所有展示列
     */
    @Override
    public Map<String, Object> queryDisplays(String displayName,String displayType) {
        Map<String, Object> maps = new HashMap<>();
        List<DisplayColumnVO> displayColumnVOList = new ArrayList<>();
        List<DisplayColumn> displayColumnList = displayColumnMapper.findDisplayListByParam(displayName,displayType);
        for (DisplayColumn displayColumn : displayColumnList) {
            DisplayColumnVO displayColumnVO = BeanUtil.create(displayColumn,new DisplayColumnVO());
            SysParams sysParams = systemParamMapper.findParamsByValue("DISPLAY_CLOUMN_TYPE",displayColumn.getDisplayColumnType());
            if (sysParams!=null){
                displayColumnVO.setDisplayColumnTypeName(sysParams.getParamName());
            }
            displayColumnVOList.add(displayColumnVO);
        }
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);
        maps.put("displayColumnVOList", displayColumnVOList);
        return maps;
    }

    /**
     * 保存展示列所有信息
     */
    @Override
    public Map<String, Object> createDisplayAllMessage(DisplayAllMessageReq displayAllMessageReq) {
        Map<String, Object> maps = new HashMap<>();
        final DisplayColumn displayColumn = displayColumnMapper.selectByPrimaryKey(displayAllMessageReq.getDisplayColumnId());
        if (displayColumn==null){
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", "展示列不存在");
            return maps;
        }
        List<DisplayLabelInfo> injectionLabelIds = displayAllMessageReq.getInjectionLabelIds();
        List<DisplayLabelInfo> labelInfoList = new ArrayList<>();
        List<Long> oldRels = displayColumnLabelMapper.findOldIdListByDisplayId(displayAllMessageReq.getDisplayColumnId());

        //校验展示列是否与标签已关联，已关联跳过
        for (DisplayLabelInfo info : injectionLabelIds){
            if (oldRels.contains(info.getLabelId())){
                continue;
            }
            labelInfoList.add(info);
        }
        //关联关系添加
        for (DisplayLabelInfo labelInfo : labelInfoList) {
            DisplayColumnLabel displayColumnLabel = new DisplayColumnLabel();
            displayColumnLabel.setDisplayId(displayAllMessageReq.getDisplayColumnId());
            displayColumnLabel.setInjectionLabelId(labelInfo.getLabelId());
            displayColumnLabel.setMessageType(labelInfo.getMessageTypeId());
            displayColumnLabel.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
            displayColumnLabel.setCreateStaff(UserUtil.loginId());
            displayColumnLabel.setUpdateStaff(UserUtil.loginId());
            displayColumnLabel.setCreateDate(DateUtil.getCurrentTime());
            displayColumnLabel.setUpdateDate(DateUtil.getCurrentTime());
            displayColumnLabel.setStatusDate(DateUtil.getCurrentTime());
            displayColumnLabelMapper.insert(displayColumnLabel);
        }
        DisplayColumn dc = new DisplayColumn();
        dc.setDisplayColumnId(displayAllMessageReq.getDisplayColumnId());
        displayColumn.setStatusCd("2000");
        displayColumnMapper.updateByPrimaryKey(displayColumn);


        return  queryLabelListByDisplayId(dc);
    }

    /**
     * 编辑展示列
     */
    @Override
    public Map<String, Object> viewDisplayColumn(DisplayAllMessageReq displayAllMessageReq) {
        Map<String, Object> map = new HashMap<>();
        Page page = displayAllMessageReq.getPage();
        PageHelper.startPage(page.getPage(),page.getPageSize());
        List<Long> injectionLabelIds = new ArrayList<>();
        for (DisplayLabelInfo labelInfo : displayAllMessageReq.getInjectionLabelIds()){
            injectionLabelIds.add(labelInfo.getLabelId());
        }
        List<Label> labelList = injectionLabelMapper.queryLabelsExceptSelected(injectionLabelIds,displayAllMessageReq.getLabelName());
        Page info = new Page(new PageInfo(labelList));
        List<LabelDTO> labelDTOList = new ArrayList<>();
        for (Label label : labelList) {
            LabelDTO labelDTO = new LabelDTO();
            labelDTO.setInjectionLabelId(label.getInjectionLabelId());
            labelDTO.setInjectionLabelName(label.getInjectionLabelName());
            labelDTOList.add(labelDTO);
        }
        map.put("resultCode", CODE_SUCCESS);
        map.put("resultMsg", StringUtils.EMPTY);
        map.put("labelList", labelDTOList);
        map.put("pageInfo",info);
        return map;
    }

    /**
     * 展示列标签展示类型配置
     */
    @Override
    public Map<String, Object> configureLabelDisplayType(Long displayColumnId, Long labelId, List<String> labelDisplayTypeId) {
        Map<String, Object> map = new HashMap<>();
        List<DisplayColumnLabel> displayColumnLabels = displayColumnLabelMapper.findListByDisplayId(displayColumnId);
        if(displayColumnLabels.size() == 0) {
            map.put("resultCode", CODE_FAIL);
            map.put("resultMsg", "展示列标签不存在，请保存后再操作");
            return map;
        }
        DisplayColumnLabel displayColumnLabel = displayColumnLabelMapper.findByDisplayIdAndLabelId(displayColumnId, labelId);
        String labelDisplayType = null;
        if(labelDisplayTypeId.size() > 0) {
            labelDisplayType = labelDisplayTypeId.get(0);
            for(int i=1;i<labelDisplayTypeId.size();i++) {
                labelDisplayType = labelDisplayType + "," + labelDisplayTypeId.get(i);
            }
        }
        displayColumnLabel.setLabelDisplayType(labelDisplayType);
        displayColumnLabel.setUpdateDate(DateUtil.getCurrentTime());
        displayColumnLabelMapper.updateByPrimaryKey(displayColumnLabel);
        map.put("resultCode", CODE_SUCCESS);
        map.put("resultMsg", StringUtils.EMPTY);
        return map;
    }

    /**
     * 查询展示列标签展示类型
     */
    public Map<String, Object> viewLabelDisplayType(Long displayColumnId, Long labelId) {
        Map<String, Object> map = new HashMap<>();
        DisplayColumnLabel displayColumnLabel = displayColumnLabelMapper.findByDisplayIdAndLabelId(displayColumnId, labelId);
        String[] labelDisplayTypeId = {};
        if(displayColumnLabel != null) {
            String labelDisplayType = displayColumnLabel.getLabelDisplayType();
            if (labelDisplayType != null) {
                labelDisplayTypeId = labelDisplayType.split(",");
            }
        }
        map.put("resultCode", CODE_SUCCESS);
        map.put("resultMsg", labelDisplayTypeId);
        return map;
    }

    /**
     * 分页查询出所有展示列
     */
    @Override
    public Map<String, Object> listDisplaysPage(String displayName, String displayType, Integer page, Integer pageSize) {
        Map<String, Object> maps = new HashMap<>();
        List<DisplayColumnVO> displayColumnVOList = new ArrayList<>();
        PageHelper.startPage(page,pageSize);
        List<DisplayColumn> displayColumnList = displayColumnMapper.findDisplayListByParam(displayName,displayType);
        for (DisplayColumn displayColumn : displayColumnList) {
            DisplayColumnVO displayColumnVO = BeanUtil.create(displayColumn,new DisplayColumnVO());
            SysParams sysParams = systemParamMapper.findParamsByValue("DISPLAY_CLOUMN_TYPE",displayColumn.getDisplayColumnType());
            if (sysParams!=null){
                displayColumnVO.setDisplayColumnTypeName(sysParams.getParamName());
            }
            displayColumnVOList.add(displayColumnVO);
        }
        Page pageInfo = new Page(new PageInfo(displayColumnList));
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", StringUtils.EMPTY);
        maps.put("displayColumnVOList", displayColumnVOList);
        maps.put("page",pageInfo);
        return maps;
    }

    /**
     * 新增展示列（新）
     */
    @Override
    public Map<String, Object> createDisplayColumn(DisplayColumnEntity displayColumnEntity) {
        Map<String, Object> maps = new HashMap<>();
        //重名校验
        List<DisplayColumn> displayColumnList = displayColumnMapper.findDisplayListByName(displayColumnEntity.getDisplayColumnName());
        if(displayColumnList != null && displayColumnList.size() > 0) {
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", "展示列名称已存在");
            return maps;
        }
        DisplayColumn displayColumn = BeanUtil.create(displayColumnEntity,new DisplayColumn());
        displayColumn.setStatusCd("2000");
        displayColumn.setCreateStaff(UserUtil.loginId());
        displayColumn.setUpdateStaff(UserUtil.loginId());
        displayColumn.setCreateDate(DateUtil.getCurrentTime());
        displayColumn.setUpdateDate(DateUtil.getCurrentTime());
        displayColumn.setStatusDate(DateUtil.getCurrentTime());
        displayColumnMapper.insert(displayColumn);
        //展示列关联标签
        List<DisplayLabelInfo> injectionLabelIds = displayColumnEntity.getInjectionLabelIds();
        if(injectionLabelIds.size() > 0) {
            for (DisplayLabelInfo labelInfo : injectionLabelIds) {
                DisplayColumnLabel displayColumnLabel = new DisplayColumnLabel();
                displayColumnLabel.setDisplayId(displayColumn.getDisplayColumnId());
                displayColumnLabel.setInjectionLabelId(labelInfo.getLabelId());
                displayColumnLabel.setMessageType(labelInfo.getMessageTypeId());
                displayColumnLabel.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
                displayColumnLabel.setCreateStaff(UserUtil.loginId());
                displayColumnLabel.setUpdateStaff(UserUtil.loginId());
                displayColumnLabel.setCreateDate(DateUtil.getCurrentTime());
                displayColumnLabel.setUpdateDate(DateUtil.getCurrentTime());
                displayColumnLabel.setStatusDate(DateUtil.getCurrentTime());
                displayColumnLabelMapper.insert(displayColumnLabel);
            }
        }
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", "新增成功");
        return maps;
    }

    /**
     * 修改展示列（新）
     */
    @Override
    public Map<String, Object> editDisplayColumn(DisplayColumnEntity displayColumnEntity) {
        Map<String, Object> maps = new HashMap<>();
        DisplayColumn displayColumn = displayColumnMapper.selectByPrimaryKey(displayColumnEntity.getDisplayColumnId());
        if (displayColumn == null){
            maps.put("resultCode", CODE_FAIL);
            maps.put("resultMsg", "展示列不存在");
            return maps;
        }
        //重名校验
        List<DisplayColumn> displayColumnList = displayColumnMapper.findDisplayListByName(displayColumnEntity.getDisplayColumnName());
        if(displayColumnList != null && displayColumnList.size() > 0) {
            for(DisplayColumn display : displayColumnList) {
                if(display.getDisplayColumnId().equals(displayColumnEntity.getDisplayColumnId())) {
                    continue;
                }
                maps.put("resultCode", CODE_FAIL);
                maps.put("resultMsg", "展示列名称已存在");
                return maps;
            }
        }
        BeanUtil.copy(displayColumnEntity,displayColumn);
        displayColumn.setUpdateStaff(UserUtil.loginId());
        displayColumn.setUpdateDate(DateUtil.getCurrentTime());
        displayColumnMapper.updateByPrimaryKey(displayColumn);

        //展示列关联标签
        List<Long> oldIdListByDisplayId = displayColumnLabelMapper.findOldIdListByDisplayId(displayColumn.getDisplayColumnId());
        List<DisplayLabelInfo> injectionLabelIds = displayColumnEntity.getInjectionLabelIds();
        List<Long> longlist = new ArrayList<>();
        for (DisplayLabelInfo labelInfo : injectionLabelIds) {
            longlist.add(labelInfo.getLabelId());
        }

        List<Long> deleteList = new ArrayList<>();
        List<DisplayLabelInfo> addList = new ArrayList<>();
        for (DisplayLabelInfo labelInfo : injectionLabelIds) {
            DisplayColumnLabel byDisplay = displayColumnLabelMapper.findByDisplayIdAndLabelId(displayColumn.getDisplayColumnId(), labelInfo.getLabelId());
            if (byDisplay == null) {
                addList.add(labelInfo);
            }
        }
        for (Long id : oldIdListByDisplayId) {
            if (!longlist.contains(id)) {
                deleteList.add(id);
            }
        }
        if (!deleteList.isEmpty()) {
            displayColumnLabelMapper.deleteByDisplayIdAndLabelList(displayColumn.getDisplayColumnId(), deleteList);
        }
        for (DisplayLabelInfo labelInfo : addList) {
            DisplayColumnLabel displayColumnLabel = new DisplayColumnLabel();
            displayColumnLabel.setDisplayId(displayColumn.getDisplayColumnId());
            displayColumnLabel.setInjectionLabelId(labelInfo.getLabelId());
            displayColumnLabel.setMessageType(labelInfo.getMessageTypeId());
            displayColumnLabel.setStatusCd(StatusCode.STATUS_CODE_EFFECTIVE.getStatusCode());
            displayColumnLabel.setCreateStaff(UserUtil.loginId());
            displayColumnLabel.setUpdateStaff(UserUtil.loginId());
            displayColumnLabel.setCreateDate(DateUtil.getCurrentTime());
            displayColumnLabel.setUpdateDate(DateUtil.getCurrentTime());
            displayColumnLabel.setStatusDate(DateUtil.getCurrentTime());
            displayColumnLabelMapper.insert(displayColumnLabel);
        }
        maps.put("resultCode", CODE_SUCCESS);
        maps.put("resultMsg", "修改成功");
        return maps;
    }
}
