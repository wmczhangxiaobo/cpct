package com.zjtelcom.cpct.enums;

/**
 * @Description StatusCode
 * @Author pengy
 * @Date 2018/6/20 17:55
 */
public enum StatusCode {

    STATUS_CODE_EFFECTIVE("有效", "1000"),
    STATUS_CODE_FAILURE("无效", "1100"),
    STATUS_CODE_NOTACTIVE("未生效","1200"),
    STATUS_CODE_ARCHIVED("已归档","1300"),
    STATUS_CODE_WILLEFFECTIVE("将生效","1001"),
    STATUS_CODE_WAIT_RESTORED("待恢复","00006"),
    STATUS_CODE_WILLEXPIRE("将失效","1101"),
    STATUS_CODE_TOBEINVALIDATED("待失效","1102"),
    STATUS_CODE_UNDO("撤消","1301"),


    STATUS_CODE_DRAFT("草稿", "2001"),
    STATUS_CODE_PUBLISHED("已发布", "2002"),
    STATUS_CODE_PASS("已通过", "2003"),
    STATUS_CODE_CHECKING("审核中", "2004"),
    STATUS_CODE_UNPASS("未通过", "2005"),
    STATUS_CODE_STOP("已暂停", "2006"),
    STATUS_CODE_ROLL("已下线", "2007"),
    STATUS_CODE_ADJUST("调整中", "2008"),
    STATUS_CODE_PRE_PUBLISHED("调整待发布", "2009"),
    STATUS_CODE_PRE_PAUSE("过期", "2010"),
    STATUS_CODE_ROLL_BACK ("回滚", "2011"),


    /*活动关系*/
    PARENT_CHILD_RELATION("父子关系", "1000"),
    SERIAL_RELATION("连续关系", "2000"),
    UPDATE_RELATION("升级关系", "3000"),

    /*营销维挽策略类型*/
    SALES_STRATEGY("销售策略", "1000"),
    CARE_STRATEGY("关怀策略", "2000"),

    /*营销活动分类*/
    MARKETING_CAMPAIGN("营销活动", "1000"),
    MAINTAIN_CAMPAIGN("维系活动", "2000"),
    RETAIN_CAMPAIGN("挽留活动", "3000"),
    VALUE_ADDED_SERVICE_CAMPAIGN("增值业务活动", "4000"),
    SERVICE_CAMPAIGN("服务活动", "5000"),
    SERVICE_SALES_CAMPAIGN("服务随销活动", "6000"),
    XIETONG_SCENE("协同场景", "7000"),
    CAM_RESOURCE("电子券活动", "8000"),

    /*活动周期性*/
    ONE_TIME("一次性", "1000"),
    PERIODICITY("周期性", "2000"),

    /*营销活动触发类型*/
    BATCH_CAMPAIGN("批量营销活动", "1000"),
    REAL_TIME_CAMPAIGN("实时营销活动", "2000"),
    MIXTURE_CAMPAIGN("混合营销活动", "3000"),

    /*活动类型*/
    FRAMEWORK_CAMPAIGN("框架活动", "6100"),
    ENFORCEMENT_CAMPAIGN("强制活动", "6200"),
    AUTONOMICK_CAMPAIGN("自主活动", "6300"),
    CONTINUE_CAMPAIGN("承接活动", "6400"),
    JITUAN_FRAMEWORK_CAMPAIGN("集团框架活动", "5100"),
    JITUAN_ENFORCEMENT_CAMPAIGN("集团强制活动", "5200"),

    /*服务类型*/
    CUST_TYPE("客账户类", "1000"),
    PRODUCT_TYPE("产品类", "1000"),
    PROM_TYPE("销售品类", "1000"),

    /*标签类型*/
    CCUST_LEVEL("客户级","1000"),
    ASSET_LEVEL("用户级","2000"),
    PRODUCT_LEVEL("销售品级","3000"),
    ZONE_LEVEL("区域级","4000"),

    //关单销售品类型
    OPEN_CLOSE("拆机关单","1000"),
    RECEIVE_CLOSE("受理关单","2000"),
    ARREARS_CLOSE("欠费关单","3000");



    private String statusMsg;
    private String statusCode;

    private StatusCode(final String statusMsg, final String statusCode) {
        this.statusMsg = statusMsg;
        this.statusCode = statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public static String getMsgByCode(String code){
        for (StatusCode statusCode : StatusCode.values()) {
            if(code.equals(statusCode.statusCode)){
                return statusCode.statusMsg;
            }
        }
        return null;
    }
}
