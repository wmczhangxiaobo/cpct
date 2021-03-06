/**
 * @(#)TarGrpTemplateService.java, 2018/9/6.
 * <p/>
 * Copyright 2018 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.zjtelcom.cpct.service.grouping;

import com.zjtelcom.cpct.dto.grouping.OrgGridRel;
import com.zjtelcom.cpct.dto.grouping.TarGrpTemplateDetail;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: linchao
 * @date: 2018/09/06 16:14
 * @version: V1.0
 */
public interface TarGrpTemplateService {

    Map<String, Object> saveTarGrpTemplate(TarGrpTemplateDetail tarGrpTemplateDetail, HttpServletRequest request, HttpServletResponse response );

    Map<String, Object> getTarGrpTemplate(Long tarGrpTemplateId);

    Map<String, Object> updateTarGrpTemplate(TarGrpTemplateDetail tarGrpTemplateDetail);

    Map<String, Object> deleteTarGrpTemplate(Long tarGrpTemplateId);

    Map<String, Object> deleteTarGrpTemplateCondition(Long conditionId);

    Map<String, Object> listTarGrpTemplateAll();

    Map<String, Object> listTarGrpTemplatePage(String tarGrpTemplateName,String tarGrpType, Integer page, Integer pageSize);

    Map<String, Object> listTarGrpTemplatePageForRel(String tarGrpTemplateName,String tarGrpType, Integer page, Integer pageSize);

    Map<String, Object> getTarGrpTemByOfferId(Long requestId);

    Map<String, Object> importUserList4TarTemp(MultipartFile file,String tempName)throws IOException ;

    Map<String, Object> tarGrpTemplateCountAndIssue(Map<String, Object> params);

    Map<String, Object> tarGrpTemplateScheduledBatchIssue();

    Map<String, Object> tarGrpTemplateCountByExpressions(Map<String, Object> params);

    List<OrgGridRel> fuzzyQueryOrgGrid(String gridName, Integer page, Integer pageSize);

    List<OrgGridRel> queryOrgGridByCode(List<String> codeList);
}