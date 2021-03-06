package com.zjtelcom.cpct.dao.question;


import com.zjtelcom.cpct.domain.question.Questionnaire;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface MktQuestionnaireMapper {
    int deleteByPrimaryKey(Long naireId);

    int insert(Questionnaire record);

    Questionnaire selectByPrimaryKey(Long naireId);

    List<Questionnaire> selectAll();

    List<Questionnaire> findQuestionnaireListByParam(@Param("map")Map<String,Object> params);

//    Page<Questionnaire> findNairePageByParam(@Param("naireName")String naireName, @Param("naireType")String naireType);


    int updateByPrimaryKey(Questionnaire record);

    List<Questionnaire> queryList(Questionnaire questionnaire);
}