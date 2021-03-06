package com.zjtelcom.cpct.dao.grouping;

import com.zjtelcom.cpct.domain.grouping.TarGrpConditionDO;
import com.zjtelcom.cpct.dto.grouping.TarGrpCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface TarGrpConditionMapper {

    int deleteByPrimaryKey(Long conditionId);

    int insert(TarGrpCondition record);

    TarGrpCondition selectByPrimaryKey(Long conditionId);

    List<TarGrpConditionDO> selectAll();

    int updateByPrimaryKey(TarGrpCondition record);

    TarGrpCondition getTarGrpCondition(Long conditionId);

    List<TarGrpCondition> listTarGrpCondition(@Param("tarGrpId") Long tarGrpId);

    List<TarGrpConditionDO> selectByActivityId(@Param("activityId") Long activityId);

    int modTarGrpCondition(TarGrpCondition record);

    int delTarGrpCondition(TarGrpCondition tarGrpCondition);

    int insertByBatch(@Param("list") List<TarGrpCondition> record);

    List<TarGrpCondition> findListBylabelId(@Param("labelId")Long labelId);

    int deleteBatch(List<Long> conditionIds);

    int deleteByTarGrpTemplateId(@Param("tarGrpTemplateId") Long tarGrpTemplateId);

    List<Map<String,String>> selectAllLabelByTarId(@Param("tarId")Long tarGrpTemplateId);

    List<Map<String,String>> selectAllLabelByCamId(@Param("mktCampaginId")Long mktCampaginId);

    List<TarGrpCondition> selectAllByUpdateStaff(@Param("updateStaff")String updateStaff);

}