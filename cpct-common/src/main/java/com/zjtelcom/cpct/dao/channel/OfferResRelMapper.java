package com.zjtelcom.cpct.dao.channel;



import com.zjtelcom.cpct.domain.channel.OfferResRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface OfferResRelMapper {
    int deleteByPrimaryKey(Long offerResRelId);

    int insert(OfferResRel record);

    OfferResRel selectByPrimaryKey(Long offerResRelId);

    List<OfferResRel> selectAll();

    int updateByPrimaryKey(OfferResRel record);

    List<OfferResRel> selectByOfferIdAndObjType(@Param("offerId") Long offerId,@Param("objType")String objType);
}