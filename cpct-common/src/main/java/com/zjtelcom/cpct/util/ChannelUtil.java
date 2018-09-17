package com.zjtelcom.cpct.util;

import com.zjtelcom.cpct.dao.channel.InjectionLabelValueMapper;
import com.zjtelcom.cpct.domain.campaign.MktCampaignDO;
import com.zjtelcom.cpct.domain.channel.*;
import com.zjtelcom.cpct.dto.campaign.CampaignVO;
import com.zjtelcom.cpct.dto.channel.*;
import com.zjtelcom.cpct.enums.Operator;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class ChannelUtil  {


    public static String getUUID(){
        UUID uuid=UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr=str.replace("-", "");
        return uuidStr;
    }
    public static CampaignVO map2CampaignVO(MktCampaignDO campaignDO){
        CampaignVO vo = BeanUtil.create(campaignDO,new CampaignVO());
        vo.setCampaignId(campaignDO.getMktCampaignId());
        vo.setCampaignName(campaignDO.getMktCampaignName());
        return vo;
    }

    public static String StringList2String(List<String> stringList){
        if (stringList == null || stringList.isEmpty()){
            return "";
        }
        String[] sts = new String[stringList.size()];
        for (int i = 0;i<sts.length;i++){
            sts[i] = stringList.get(i);
        }
        return StringUtils.join(sts,",");
    }


    public static String idList2String(List<Long> idList){
        if (idList == null || idList.isEmpty()){
            return "";
        }
        Long[] ids = new Long[idList.size()];
        for (int i = 0;i<ids.length;i++){
            ids[i] = idList.get(i);
        }
        return StringUtils.join(ids,",");
    }

    public static String List2String(List<Integer> idList){
        if (idList == null || idList.isEmpty()){
            return "";
        }
        Integer[] ids = new Integer[idList.size()];
        for (int i = 0;i<ids.length;i++){
            ids[i] = idList.get(i);
        }
        return StringUtils.join(ids,",");
    }

    public static ChannelVO map2ChannelVO(Channel channel){
        ChannelVO vo = BeanUtil.create(channel,new ChannelVO());
        return vo;
    }

    public static ScriptVO map2ScriptVO(Script script){
        ScriptVO vo = BeanUtil.create(script,new ScriptVO());
        return vo;
    }

    public static CamScriptVO map2CamScriptVO(CamScript script){
        CamScriptVO vo = BeanUtil.create(script,new CamScriptVO());
        return vo;
    }

    public static LabelVO map2LabelVO(Label label,List<LabelValue> labelValueList){
        LabelVO vo = BeanUtil.create(label,new LabelVO());
        if (label.getOperator()!=null && !label.getOperator().equals("")){
            List<String> opratorList = StringToList(label.getOperator());
            List<OperatorDetail> opStList  = new ArrayList<>();
            for (String operator : opratorList){
                Operator op = Operator.getOperator(Integer.valueOf(operator));
                OperatorDetail detail = new OperatorDetail();
                if (op!=null){
                    detail.setOperName(op.getDescription());
                    detail.setOperValue(op.getValue());
                }
                opStList.add(detail);
            }
            vo.setOperatorList(opStList);
        }
        List<String> valueList = new ArrayList<>();
        if (labelValueList!=null && !labelValueList.isEmpty()){
            valueList = valueList2StList(labelValueList);
        }
        if (label.getScope().equals(0)){
            vo.setScope("自有标签");
        }else {
            vo.setScope("大数据标签");
        }
        vo.setValueList(valueList);
        return vo;
    }

    public static LabelValueVO map2LabelValueVO(LabelValue labelValue){
        LabelValueVO vo = BeanUtil.create(labelValue,new LabelValueVO());
        return vo;
    }

    public static VerbalVO map2VerbalVO(MktVerbal verbal){
        VerbalVO vo = BeanUtil.create(verbal,new VerbalVO());
        return vo;
    }

    public static String getRandomStr(int length) {
        char[] chars = "23456789".toCharArray();
        Random r = new Random(System.currentTimeMillis());
        String string = "";

        for(int i = 0; i < length; ++i) {
            string = string + chars[r.nextInt(8)];
        }

        return string;
    }


    public static List<String> valueList2StList(List<LabelValue> valueList){
        List<String> stringList = new ArrayList<>();
        for (LabelValue labelValue : valueList){
            if (labelValue.getLabelValue()!=null){
                stringList.add(labelValue.getLabelValue());
            }
        }
        return stringList;
    }



    public static List<String> StringToList(String var1) {
        String[] array = var1.split(",");
        List<String> list = new ArrayList<String>();
        for (String str : array)
        {
            list.add(str);
        }
        return list;
    }

    public static Object getCellValue(Cell cell) {
        Object cellValue;
        switch (cell.getCellTypeEnum()){
            case NUMERIC://数字
                cellValue = cell.getNumericCellValue() + "";
                break;
            case STRING: // 字符串
                cellValue = cell.getStringCellValue();
                break;

            case BOOLEAN: // Boolean
                cellValue = cell.getBooleanCellValue() + "";
                break;

            case FORMULA: // 公式
                cellValue = cell.getCellFormula() + "";
                break;

            case BLANK: // 空值
                cellValue = "";
                break;

            case ERROR: // 故障
                cellValue = "非法字符";
                break;

            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }

    public static List<Long> StringToidList(String var1) {
        String[] array = var1.split(",");
        List<Long> list = new ArrayList<Long>();
        for (String str : array)
        {
            list.add(Long.valueOf(str));
        }
        return list;
    }

    public static String getDataType(String dataType){
        //1000	日期型;1100	日期时间型;1200	字符型;1300	浮点型;1400	整数型;1500	布尔型;1600	计算型
        String data = "";
        switch (dataType){
            case "text":
                data = "1200";
                break;
            case "number":
                data = "1400";
                break;
            case "date":
                data = "1100";
                break;
            case "integer":
                data = "1400";
                break;
            case "character":
                data = "1200";
                break;
            case "smallint":
                data = "1600";
                break;
        }
        return data;
    }



}
