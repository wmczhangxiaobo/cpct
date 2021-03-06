package com.zjtelcom.cpct.dao.blacklist;

import com.zjtelcom.cpct.domain.blacklist.BlackListDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface BlackListMapper {

    int addBlackList(BlackListDO blackListDO);

    int updateBlackList(BlackListDO blackListDO);

    int deleteBlackListById(List<String> phoneNumsDeleted);

    List<BlackListDO> getBlackListById(List<String> phoneNums);

    List<BlackListDO> getAllBlackList();

    List<BlackListDO> getBlackListLimit(@Param("m") int m, @Param("n") int n);

    int insertBatch(List<BlackListDO> blackListDOS);

    List<String> selectByBlackList(BlackListDO blackListDO);

    int getCountAll();

    List<BlackListDO>  getBlackListPageByKey(@Param("assetPhone") String assetPhone, @Param("serviceCate") String serviceCate, @Param("maketingCate") String maketingCate, @Param("publicBenefitCate") String publicBenefitCate, @Param("channel") String channel, @Param("staffId") String staffId);

    Integer checkPhoneisExist(BlackListDO blackListDO);

    BlackListDO getBlackListByAssetPhone(@Param("assetPhone") String assetPhone);

    List<String> getDistinctPhone(@Param("begin") Integer begin, @Param("end") Integer end);

    List<BlackListDO> getBlackListByPhone(@Param("assetPhone") String assetPhone);

    Integer deleteByBlackId(@Param("blackId") Integer blackId);

    Integer updateByBlackId(BlackListDO blackListDO);
}

