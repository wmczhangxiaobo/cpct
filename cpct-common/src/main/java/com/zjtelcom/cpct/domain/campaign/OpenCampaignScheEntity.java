package com.zjtelcom.cpct.domain.campaign;


import com.zjtelcom.cpct.domain.channel.ObjCatItemRel;
import com.zjtelcom.cpct.domain.channel.ObjectLabelRel;
import com.zjtelcom.cpct.dto.campaign.MktCamChlConf;
import com.zjtelcom.cpct.dto.campaign.MktCamEvtRel;

import java.util.Date;
import java.util.List;

public class OpenCampaignScheEntity {

    private String actType;
    private Long mktCampaignId;
    private String tiggerType;
    private String mktCampaignName;
    private Date planBeginTime;
    private Date planEndTime;
    private Date beginTime;
    private Date endTime;
    private String mktCampaignType;
    private String mktActivityNbr;
    private String mktActivityTarget;
    private String mktCampaignDesc;
    private String execType;
    private String execInvl;
    private Long execNum;
    private String manageType;
    private String extMktCampaignId;
    private String statusCd;
    private Date statusDate;
    private Long createStaff;
    private Date createDate;
    private Long updateStaff;
    private Date updateDate;
    private String remark;
    private Long regionId;
    private String regionNbr;
    private String srcType;
    private String srcId;
    private String srcNbr;
    private String serviceType;
    private String lifeStage;
    private Long serviceCancleFlag;
    private Long lanId;
    //营服活动分群规则
    private List<MktCamGrpRul> mktCamGrpRuls;//
    //营服活动推荐条目
    private List<MktCamItem> mktCamItems;//
    //营服活动渠道推送配置
    private List<MktCamChlConf> mktCamChlConfs;//
    //营服活动关联事件
    private List<MktCamEvtRel> mktCamEvtRels;//
    //对象区域关系
    private List<ObjRegionRelEntity> objRegionRels;//
    //对象目录节点关系
    private List<ObjCatItemRel> objCatItemRels;//
    //对象关联标签
    private List<ObjectLabelRel> objectLabelRels;//

    public String getActType() {
        return actType;
    }

    public void setActType(String actType) {
        this.actType = actType;
    }

    public Long getMktCampaignId() {
        return mktCampaignId;
    }

    public void setMktCampaignId(Long mktCampaignId) {
        this.mktCampaignId = mktCampaignId;
    }

    public String getTiggerType() {
        return tiggerType;
    }

    public void setTiggerType(String tiggerType) {
        this.tiggerType = tiggerType;
    }

    public String getMktCampaignName() {
        return mktCampaignName;
    }

    public void setMktCampaignName(String mktCampaignName) {
        this.mktCampaignName = mktCampaignName;
    }

    public Date getPlanBeginTime() {
        return planBeginTime;
    }

    public void setPlanBeginTime(Date planBeginTime) {
        this.planBeginTime = planBeginTime;
    }

    public Date getPlanEndTime() {
        return planEndTime;
    }

    public void setPlanEndTime(Date planEndTime) {
        this.planEndTime = planEndTime;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getMktCampaignType() {
        return mktCampaignType;
    }

    public void setMktCampaignType(String mktCampaignType) {
        this.mktCampaignType = mktCampaignType;
    }

    public String getMktActivityNbr() {
        return mktActivityNbr;
    }

    public void setMktActivityNbr(String mktActivityNbr) {
        this.mktActivityNbr = mktActivityNbr;
    }

    public String getMktActivityTarget() {
        return mktActivityTarget;
    }

    public void setMktActivityTarget(String mktActivityTarget) {
        this.mktActivityTarget = mktActivityTarget;
    }

    public String getMktCampaignDesc() {
        return mktCampaignDesc;
    }

    public void setMktCampaignDesc(String mktCampaignDesc) {
        this.mktCampaignDesc = mktCampaignDesc;
    }

    public String getExecType() {
        return execType;
    }

    public void setExecType(String execType) {
        this.execType = execType;
    }

    public String getExecInvl() {
        return execInvl;
    }

    public void setExecInvl(String execInvl) {
        this.execInvl = execInvl;
    }

    public Long getExecNum() {
        return execNum;
    }

    public void setExecNum(Long execNum) {
        this.execNum = execNum;
    }

    public String getManageType() {
        return manageType;
    }

    public void setManageType(String manageType) {
        this.manageType = manageType;
    }

    public String getExtMktCampaignId() {
        return extMktCampaignId;
    }

    public void setExtMktCampaignId(String extMktCampaignId) {
        this.extMktCampaignId = extMktCampaignId;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }

    public Long getCreateStaff() {
        return createStaff;
    }

    public void setCreateStaff(Long createStaff) {
        this.createStaff = createStaff;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Long getUpdateStaff() {
        return updateStaff;
    }

    public void setUpdateStaff(Long updateStaff) {
        this.updateStaff = updateStaff;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getRegionNbr() {
        return regionNbr;
    }

    public void setRegionNbr(String regionNbr) {
        this.regionNbr = regionNbr;
    }

    public String getSrcType() {
        return srcType;
    }

    public void setSrcType(String srcType) {
        this.srcType = srcType;
    }

    public String getSrcId() {
        return srcId;
    }

    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    public String getSrcNbr() {
        return srcNbr;
    }

    public void setSrcNbr(String srcNbr) {
        this.srcNbr = srcNbr;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(String lifeStage) {
        this.lifeStage = lifeStage;
    }

    public Long getServiceCancleFlag() {
        return serviceCancleFlag;
    }

    public void setServiceCancleFlag(Long serviceCancleFlag) {
        this.serviceCancleFlag = serviceCancleFlag;
    }

    public Long getLanId() {
        return lanId;
    }

    public void setLanId(Long lanId) {
        this.lanId = lanId;
    }

    public List<MktCamGrpRul> getMktCamGrpRuls() {
        return mktCamGrpRuls;
    }

    public void setMktCamGrpRuls(List<MktCamGrpRul> mktCamGrpRuls) {
        this.mktCamGrpRuls = mktCamGrpRuls;
    }

    public List<MktCamItem> getMktCamItems() {
        return mktCamItems;
    }

    public void setMktCamItems(List<MktCamItem> mktCamItems) {
        this.mktCamItems = mktCamItems;
    }

    public List<MktCamChlConf> getMktCamChlConfs() {
        return mktCamChlConfs;
    }

    public void setMktCamChlConfs(List<MktCamChlConf> mktCamChlConfs) {
        this.mktCamChlConfs = mktCamChlConfs;
    }

    public List<MktCamEvtRel> getMktCamEvtRels() {
        return mktCamEvtRels;
    }

    public void setMktCamEvtRels(List<MktCamEvtRel> mktCamEvtRels) {
        this.mktCamEvtRels = mktCamEvtRels;
    }

    public List<ObjRegionRelEntity> getObjRegionRels() {
        return objRegionRels;
    }

    public void setObjRegionRels(List<ObjRegionRelEntity> objRegionRels) {
        this.objRegionRels = objRegionRels;
    }

    public List<ObjCatItemRel> getObjCatItemRels() {
        return objCatItemRels;
    }

    public void setObjCatItemRels(List<ObjCatItemRel> objCatItemRels) {
        this.objCatItemRels = objCatItemRels;
    }

    public List<ObjectLabelRel> getObjectLabelRels() {
        return objectLabelRels;
    }

    public void setObjectLabelRels(List<ObjectLabelRel> objectLabelRels) {
        this.objectLabelRels = objectLabelRels;
    }
}
