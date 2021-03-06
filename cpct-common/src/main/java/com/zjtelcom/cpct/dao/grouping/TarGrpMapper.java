package com.zjtelcom.cpct.dao.grouping;

import com.zjtelcom.cpct.domain.grouping.TarGrpTemplateDO;
import com.zjtelcom.cpct.dto.grouping.TarGrp;
import com.zjtelcom.cpct.dto.grouping.TarGrpDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Mapper
@Repository
public interface TarGrpMapper {
    int deleteByPrimaryKey(Long tarGrpId);

    int insert(TarGrpDetail record);

    TarGrp selectByPrimaryKey(Long tarGrpId);

    List<TarGrpDetail> selectAll();

    int updateByPrimaryKey(TarGrpDetail record);

    int createTarGrp(TarGrp tarGrp);

    int modTarGrp(TarGrp record);

    int delTarGrp(TarGrp tarGrp);

    List<TarGrp> selectByName(@Param("tarGrpTemplateName") String tarGrpTemplateName,@Param("tarGrpType")String tarGrpType,@Param("remark")String remark);

    List<TarGrp> selectByCondition(@Param("tarGrpTemplateName") String tarGrpTemplateName,@Param("tarGrpType")String tarGrpType,@Param("remark")String remark, @Param("lanId") Long lanId);

    List<TarGrp> queryList(TarGrp tarGrp);

    int modTarGrpOther(TarGrp tarGrp);

}