package com.zjtelcom.cpct.dao.channel;


import com.zjtelcom.cpct.domain.channel.Offer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Mapper
@Repository
public interface OfferMapper {
    int deleteByPrimaryKey(Integer offerId);

    int insert(Offer record);

    Offer selectByPrimaryKey(Integer offerId);

    List<Offer> selectAll();

    Offer selectByCamItemId(Long camItemId);

    List<Offer> findByName(@Param("offerName")String name);

    List<Offer> selectByCode(@Param("offerCode")String code);

    Offer selectByPrimaryKeyAndName(@Param("offerId") Integer offerId,@Param("productName")String productName);

    List<Offer> listByCatalogItemId(@Param("catalogId")Long catalogId);

    List<String> listByOfferIdList(@Param("list")List<Long> offerList);

    int updateByPrimaryKey(Offer record);

    List<Offer> selectOfferByOver(@Param("preDay")Date preDay, @Param("date")Date date);
}