package com.zjtelcom.cpct.service.channel;

import com.zjtelcom.cpct.domain.channel.LabelCatalog;

import java.util.List;
import java.util.Map;

public interface LabelCatalogService  {

    Map<String,Object> batchAdd(List<String> nameList,Long parentId,Long level);

    Map<String,Object> addLabelCatalog( LabelCatalog addVO);

    Map<String,Object> listLabelCatalog(String labelType, String labelStatus);

    Map<String,Object> listLabelByCatalogId(Long catalogId);



}
