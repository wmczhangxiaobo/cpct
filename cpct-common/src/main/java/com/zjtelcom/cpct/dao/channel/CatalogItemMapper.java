package com.zjtelcom.cpct.dao.channel;



import com.zjtelcom.cpct.domain.channel.CatalogItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CatalogItemMapper {
    int deleteByPrimaryKey(Long catalogItemId);

    int insert(CatalogItem record);

    CatalogItem selectByPrimaryKey(Long catalogItemId);

    List<CatalogItem> selectAll();

    List<CatalogItem> selectByCatalog(@Param("catalogId")Long catalogId);

    List<CatalogItem> selectByParentId(@Param("parentId")Long parentId);

    int updateByPrimaryKey(CatalogItem record);

    CatalogItem selectByCatlogItemCode(String catalogItemCode);


    Long selectCatalogItemIdByCatalogItemDesc(String desc);


}