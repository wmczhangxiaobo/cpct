package com.zjtelcom.cpct_prd.dao.question;


import com.zjtelcom.cpct.domain.question.Questionnaire;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MktQuestionnairePrdMapper {
    int deleteByPrimaryKey(Long naireId);

    int insert(Questionnaire record);

    Questionnaire selectByPrimaryKey(Long naireId);

    List<Questionnaire> selectAll();

    List<Questionnaire> findQuestionnaireListByParam(@Param("naireName") String naireName, @Param("naireType") String naireType);

//    Page<Questionnaire> findNairePageByParam(@Param("naireName")String naireName, @Param("naireType")String naireType);


    int updateByPrimaryKey(Questionnaire record);
}