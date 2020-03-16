package com.zjtelcom.cpct.service.impl.blacklist;

import com.zjtelcom.cpct.dao.blacklist.BlackListMapper;
import com.zjtelcom.cpct.domain.blacklist.BlackListDO;
import com.zjtelcom.cpct.service.blacklist.BlackListService;
import com.zjtelcom.cpct.util.SftpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class BlackListServiceImpl implements BlackListService {

    private final static Logger log = LoggerFactory.getLogger(BlackListServiceImpl.class);
    private final static String splitMark = "\u0007";

    private String ftpAddress = "134.108.0.93";
    private int ftpPort = 22;
    private String ftpName= "ftp";
    private String ftpPassword="V1p9*2_9%3#";
    private String excelIssurepath="/app/ftp/msc/userlist/fees";
    private String uploadExcelPath="/app/ftp/msc/userlist/fees/";



    @Autowired
    private BlackListMapper blackListMapper;

    @Override
    public boolean exportBlackListFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMdd");
        String Date = dateFormat.format(new Date());
        //String headFileName = "BLACK_LIST_HEAD_" + Date + ".dat";     //文件路径+名称+文件类型
        String dataFileName = "BLACK_LIST_" + Date + ".dat";     //文件路径+名称+文件类型
        //创建头文件
        //File headFile = new File(headFileName);
        //创建数据文件
        File dataFile = new File(dataFileName);
        if (!dataFile.exists()) {
            // 如果文件不存在，则创建新的文件
            try {
                dataFile.createNewFile();
                //创建策略定义文件
                // 从数据库查询获取黑名单数据
                List<BlackListDO> allBlackList = blackListMapper.getAllBlackList();
                for (BlackListDO blackListDO : allBlackList) {
                    StringBuilder sline = new StringBuilder();
                    if (blackListDO.getBlackId() != 0) {
                        sline.append(blackListDO.getBlackId());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getAssetPhone() != null) {
                        sline.append(blackListDO.getAssetPhone());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getServiceCate() != null) {
                        sline.append(blackListDO.getServiceCate());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getMaketingCate() != null) {
                        sline.append(blackListDO.getMaketingCate());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getPublicBenefitCate() != null) {
                        sline.append(blackListDO.getPublicBenefitCate());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getChannel() != null) {
                        sline.append(blackListDO.getChannel());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getStaffId() != null) {
                        sline.append(blackListDO.getStaffId());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getCreateStaff() != null) {
                        sline.append(blackListDO.getCreateStaff());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getCreateDate() != null) {
                        sline.append(blackListDO.getCreateDate());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getUpdateStaff() != null) {
                        sline.append(blackListDO.getUpdateStaff());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getUpdateDate() != null) {
                        sline.append(blackListDO.getUpdateDate());
                    }
                    sline.append(splitMark);
                    if (blackListDO.getOperType() != null) {
                        sline.append(blackListDO.getOperType());
                    }
                    sline.append(splitMark);

                    SftpUtils sftpUtils = new SftpUtils();
                    sftpUtils.writeFileContent(dataFile.getName(), sline.toString());
                }
            } catch (Exception e) {
                log.error("黑名单数据文件dataFile失败！Expection = ", e);
            }
        }
        return true;
    }


}
