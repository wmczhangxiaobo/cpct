/*
package com.zjtelcom.cpct.service.impl.campaign;

//import com.ctzj.service.outbound.QrCodeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.zjtelcom.cpct.bean.ResponseVO;
import com.zjtelcom.cpct.common.Page;
import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.campaign.MktCamChlConfAttrMapper;
import com.zjtelcom.cpct.dao.campaign.MktCamResourceQRCodeMapper;
import com.zjtelcom.cpct.domain.campaign.PostBackgroundDO;
import com.zjtelcom.cpct.domain.channel.MktCamResource;
import com.zjtelcom.cpct.enums.ConfAttrEnum;
import com.zjtelcom.cpct.service.campaign.MktCamResourceQRCodeService;
import com.zjtelcom.cpct.util.*;
import javafx.beans.binding.ObjectBinding;
import net.sf.ehcache.transaction.xa.EhcacheXAException;
import org.bouncycastle.asn1.cms.PasswordRecipientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class MktCamResourceQRCodeImpl implements MktCamResourceQRCodeService {
*/
/*
    @Autowired(required = false)
    private QrCodeService qrCodeService;
*//*

    @Autowired
    private MktCamResourceQRCodeMapper mktCamResourceQRCodeMapper;
    @Autowired
    private MktCamChlConfAttrMapper mktCamChlConfAttrMapper;
    @Autowired
    private   ResponseVO responseVO;

    @Value("${ftp.address}")
    private String ftpAddress;
    @Value("${ftp.prodaddress}")
    private String prodaddress;
    @Value("${ftp.port}")
    private int ftpPort;
    @Value("${ftp.name}")
    private String ftpName;
    @Value("${ftp.password}")
    private String ftpPassword;
    @Value("${ftp.mscname}")
    private String mscname;
    @Value("${ftp.mscpassword}")
    private String mscpassword;

    @Value("${ftp.qrCodePath}")
    private String qrCodePath;
    @Value("${ftp.postUrlPath}")
    private String postUrlPath;
    @Value("${ftp.postBackgroundPath}")
    private String postBackgroundPath;

    private static final Logger logger = LoggerFactory.getLogger(MktCamResourceQRCodeImpl.class);

    private String getQRUrlByMktResourceId(String requestId,String h5Url,String mktResourceId){
        Map<String,Object> paraMap = new HashMap<>();
        Map<String,Object> urlParams = new HashMap<>();
        urlParams.put("wd",mktResourceId);
        paraMap.put("requestId",requestId);
        paraMap.put("url",h5Url);
        paraMap.put("urlParams",urlParams);
        logger.info("获取二维码url  paramMap:" + paraMap);
        Map<String,Object> qrResultMap = null; //qrCodeService.generate(paraMap);
        logger.info("获取二维码url  qrResultMap:" + qrResultMap);
        String code =(String)qrResultMap.get("code");
        String qrUrl = "";
        String digitalCouponsId =(String) paraMap.get("digitalCouponsId");
        if(code.equals("0")){
            qrUrl = (String)qrResultMap.get("qrCode");
        }
        return qrUrl;
    }
    //提供海报元素查询
    @Override
    public Map<String, Object> generatePoster(Map<String,Object> params) throws Exception {
        String requestId = DateUtil.Date2String(new Date());
        String h5Url = "https://www.baidu.com/s";
        Integer mktCamResourceId =(Integer)params.get("mktCamResourceId");
        Map<String,Object> resultMap = new HashMap<>();
        MktCamResource mktCamResource = mktCamResourceQRCodeMapper.selectRecordByRuleId(mktCamResourceId.longValue());
        //获取电子券id
        Long resourceId = mktCamResource.getResourceId();
        //查看二维码是否存在
        String qrUrl = mktCamResource.getQcCodeUrl();
        String qrUrlToUse = "";
        if( qrUrl != null && !qrUrl.equals("")){
            //直接返回QRurl
           qrUrlToUse = qrUrl;

        }else {
            qrUrl = this.getQRUrlByMktResourceId(requestId,h5Url,mktCamResourceId.toString());
            String pathName = "/app/cpcp_cxzx/qrcode"; //保存地址，本地暂存与ftp地址一致
            qrUrlToUse = base64SaveToFtp(qrUrl,qrCodePath);
            logger.info("二维码url保存地保存电子券综合表中址" + qrUrlToUse);
            //
            mktCamResourceQRCodeMapper.updateQRUrlbyMktResourceId(qrUrlToUse,mktCamResourceId.longValue());
        }
        resultMap.put("resourceId",resourceId);
        resultMap.put("qrUrlToUse",qrUrl);
        resultMap.put("mktCamResourceId",mktCamResourceId.longValue());
        return  resultMap;
    }

//保存海报url到电子券综合表
    @Override
    public Map<String,Object> savePostUrl(Map<String, Object> params) throws Exception {
        Map<String,Object> resultMap = new HashMap<>();
        Integer mktCamResourceId =(Integer) params.get("mktCamResourceId");
        String postUrl =(String) params.get("postUrl");
        String pathName = "/app/cpcp_cxzx/post_url"; //保存地址，本地暂存与ftp地址一致
        String postUrlPathToUse = base64SaveToFtp(postUrl,postUrlPath);
        mktCamResourceQRCodeMapper.updatePostUrlbyMktResourceId(postUrlPathToUse,mktCamResourceId.longValue());
        resultMap.put("postUrlPath",postUrlPathToUse);
        return resultMap;
    }


    */
/*海报背景图上传*//*

    @Override
    public Map<String, Object> savePostBackgroundUrl(Map<String, Object> params) throws Exception {
        Map<String,Object> resultMap = new HashMap<>();
        Integer mktCamResourceId =(Integer) params.get("mktCamResourceId");
        String postBackgroundUrl =(String) params.get("postBackgroundUrl");
        String pathName = "/app/cpcp_cxzx/post_background"; //保存地址，本地暂存与ftp地址一致
        String postBackgroundUrlPath = base64SaveToFtp(postBackgroundUrl,postBackgroundPath);
        resultMap.put("postBackgroundUrlPath",postBackgroundUrlPath);
        return resultMap;
    }

    */
/*海报背景图分页*//*

    @Override
    public Map<String, Object> getPostgroundPathPage(Map<String,Object> params) throws SftpException {
        Map<String,Object> resultMap = new HashMap<>();
        Integer pageNum = (Integer) params.get("pageNum");
        Integer pageSize = (Integer) params.get("pageSize");
        String orderBy = "desc";
        String ftpAddress = "134.108.3.130";
        int ftpPort = 22;
        String ftpName= "ftp";
        String ftpPassword = "V1p9*2_9%3#";
        String pathName = "/app/cpcp_cxzx/post_background"; //保存地址，本地暂存与ftp地址一致
        SftpUtils sftpUtils = new SftpUtils();
        final ChannelSftp sftp = sftpUtils.connect(ftpAddress, ftpPort, ftpName, ftpPassword);
        List<String> files = sftpUtils.listFiles(postBackgroundPath,sftp);
        List<String> postList = new ArrayList<>();
        String tempFilePath = "/app/cpcp_cxzx/post_background/";//保存到本地地址
        File pathDir = new File(tempFilePath);
        List<String> fileList = new ArrayList<>();
        if(!pathDir.exists()){
            boolean isExist = pathDir.mkdir();
        }
        for(String fileName: files){
            if(fileName.equals(".") || fileName.equals("..")){
                continue;
            }
            String path = sftpUtils.cd(postBackgroundPath, sftp);
            boolean result = sftpUtils.download(sftp, path + "/", fileName, tempFilePath);
            String base64Jpg = FileUtil.convertFileToBase64(tempFilePath + fileName );
          */
/*  PostBackgroundDO postBackgroundDO = new PostBackgroundDO();
            postBackgroundDO.setPostUrl(base64Jpg);*//*

            postList.add(base64Jpg);
            fileList.add(fileName);
        }
        logger.info("postList" + postList);
//        PageHelper.startPage(pageNum,pageSize,orderBy);
        resultMap.put("pageInfo",PageUtil.startPage(postList,pageNum,pageSize));
        resultMap.put("files",PageUtil.startPage(fileList,pageNum,pageSize));
        return resultMap;
    }
//海报获取
    @Override
    public Map<String, Object> getPostUrlByRuleId(Map<String, Object> params) throws SftpException {
        Map<String,Object> resultMap = new HashMap<>();
        String tempFilePath = postUrlPath;//本地暂存地址
        Integer ruleId = (Integer)params.get("ruleId");
        String postPath = mktCamResourceQRCodeMapper.getPostPathByRuleId(ruleId.longValue());
        SftpUtils sftpUtils = new SftpUtils();
        final ChannelSftp sftp = sftpUtils.connect(ftpAddress, ftpPort, ftpName, ftpPassword);
        String path = sftpUtils.cd(postUrlPath, sftp);
        int lastIndex = postPath.lastIndexOf("/");
        String fileName = postPath.substring(lastIndex + 1,postPath.length());
        logger.info("根据规则id获取海报 postPath ： " +postPath);
        logger.info("根据规则id获取海报 path " +path);
        logger.info("根据规则id获取海报 fileName " +fileName);
        boolean result = sftpUtils.download(sftp, path + "/",fileName,tempFilePath);
        String base64Jpg = FileUtil.convertFileToBase64(tempFilePath + fileName);
        resultMap.put("data",base64Jpg);
        return resultMap;
    }

    // 将base64的字符流转为图片保存到指定ftp路径
    public String  base64SaveToFtp(String base64Url,String pathName) throws Exception {
        Date date2 = new Date();
        String strDateFormat = "yyyyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strDateFormat);
        String nowDate = simpleDateFormat.format(date2);
        String fileName =nowDate + Math.round(Math.random() * 1000) + ".jpg"; //二维码图片名
        FileUtil.GenerateImage(base64Url,pathName,fileName); //保存为图片到本地
      */
/*  String ftpAddress = "134.108.3.130";
        int ftpPort = 22;
        String ftpName= "ftp";
        String ftpPassword = "V1p9*2_9%3#";*//*

        SftpUtils sftpUtils = new SftpUtils();
        final ChannelSftp sftp = sftpUtils.connect(ftpAddress, ftpPort, ftpName, ftpPassword);
        File pathDir = new File(pathName);
        if(!pathDir.exists()){
            boolean isExist = pathDir.mkdir();
        }
        sftpUtils.changeDir(pathName, sftp);
        FileInputStream fio = new FileInputStream(new File(pathName + File.separator + fileName));
        sftpUtils.uploadFile(pathName,fileName,fio,sftp);//上传到ftp
        sftp.quit();

        return  pathName + "/"+  fileName;
    }


}
*/
