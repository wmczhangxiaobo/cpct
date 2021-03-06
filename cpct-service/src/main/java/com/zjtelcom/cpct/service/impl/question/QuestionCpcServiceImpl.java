package com.zjtelcom.cpct.service.impl.question;

import com.ctzj.smt.bss.sysmgr.model.common.SysmgrResultObject;
import com.ctzj.smt.bss.sysmgr.model.dataobject.SystemPost;
import com.ctzj.smt.bss.sysmgr.model.dto.SystemUserDto;
import com.ctzj.smt.bss.sysmgr.model.query.QrySystemPostReq;
import com.ctzj.smt.bss.sysmgr.privilege.service.dubbo.api.ISystemPostDubboService;
import com.ctzj.smt.bss.sysmgr.privilege.service.dubbo.api.ISystemUserDtoDubboService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjtelcom.cpct.common.Page;
import com.zjtelcom.cpct.constants.CommonConstant;
import com.zjtelcom.cpct.dao.question.MktQstQuestRelMapper;
import com.zjtelcom.cpct.dao.question.MktQuestionDetailMapper;
import com.zjtelcom.cpct.dao.question.MktQuestionMapper;
import com.zjtelcom.cpct.dao.question.MktQuestionnaireMapper;
import com.zjtelcom.cpct.domain.question.Question;
import com.zjtelcom.cpct.domain.question.QuestionDetail;
import com.zjtelcom.cpct.dto.question.*;
import com.zjtelcom.cpct.service.question.QuestionService;
import com.zjtelcom.cpct.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.zjtelcom.cpct.constants.CommonConstant.CODE_FAIL;
import static com.zjtelcom.cpct.constants.CommonConstant.CODE_SUCCESS;

@Service
@Transactional
public class QuestionCpcServiceImpl implements QuestionService {


    @Autowired
    private MktQuestionMapper questionMapper;
    @Autowired
    private MktQuestionDetailMapper questionDetailMapper;
    @Autowired
    private MktQuestionnaireMapper questionnaireMapper;
    @Autowired
    private MktQstQuestRelMapper questRelMapper;

    private String value;
    @Autowired(required = false)
    private ISystemPostDubboService iSystemPostDubboService;

    @Autowired(required = false)
    private ISystemUserDtoDubboService iSystemUserDtoDubboService;


    public String fourthDataSource() {
//        RequestInstRel requestInstRel = requestInstRelMapper.selectByPrimaryKey(1L);
        String postName = "";
        SystemPost systemPost = new SystemPost();
        systemPost.setSysPostCode("cpcp0001");
        QrySystemPostReq qrySystemPostReq = new QrySystemPostReq();
        qrySystemPostReq.setSystemPost(systemPost);
        SysmgrResultObject<com.ctzj.smt.bss.sysmgr.model.common.Page> pageSysmgrResultObject = iSystemPostDubboService.qrySystemPostPage(new com.ctzj.smt.bss.sysmgr.model.common.Page(), qrySystemPostReq);
        if(pageSysmgrResultObject!=null){
            if( pageSysmgrResultObject.getResultObject()!=null){
                List<SystemPost> dataList = (List<SystemPost>) pageSysmgrResultObject.getResultObject().getDataList();
                if(dataList!=null){
                    for (SystemPost post : dataList){
                        if (post.getStatusCd().equals("1000")){
                            postName = post.getSysPostName();
                        }
                        break;
                    }
                }
            }
        }
        return postName;
    }

    public Map<String, Object> userTest() {
        Map<String,Object> resutl = new HashMap<>();
        SysmgrResultObject<SystemUserDto> systemUserDtoSysmgrResultObject = iSystemUserDtoDubboService.qrySystemUserDto(121119809L, new ArrayList<Long>());
        resutl.put("staffId",systemUserDtoSysmgrResultObject);
        SysmgrResultObject<SystemUserDto> sysUser = iSystemUserDtoDubboService.qrySystemUserDto(100010L, new ArrayList<Long>());
        resutl.put("sysUser",sysUser);
        return resutl;
    }

    /**
     * 获取题库问题详情
     * @param questionId
     * @return
     */
    @Override
    public Map<String, Object> getQuestionDetail(Long questionId) {
        Map<String,Object> result = new HashMap<>();
        Question question = questionMapper.selectByPrimaryKey(questionId);
        if (question==null){
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","问题不存在");
            return result;
        }
        QuestionModel vo = getQuestionModel(question);
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",null);
        result.put("data",vo);
        return result;


    }

    private QuestionModel getQuestionModel(Question question) {
        QuestionModel vo = BeanUtil.create(question,new QuestionModel());
        vo.setAnswerTypeName(ChannelUtil.getAnswerType(question));
        vo.setQuestionTypeName(ChannelUtil.getQuestionType(question));
        vo.setCheckList(new ArrayList<String>());
        vo.setQuestionOrder(question.getQuestionId().toString());
        List<QuestionDetail> questionDetailList = questionDetailMapper.findDetailListByQuestionId(question.getQuestionId());
        List<QuestionDetailVO> detailVOS = new ArrayList<>();
        for (QuestionDetail detail : questionDetailList){
            QuestionDetailVO detailVO = BeanUtil.create(detail,new QuestionDetailVO());
            detailVO.setQstDetailOrder(Integer.valueOf(detail.getQstDetailId().toString()));
            detailVOS.add(detailVO);
        }
        vo.setQuestionDetailList(detailVOS);
        return vo;
    }

    /**
     * 获取题库列表
     * @return
     */
    @Override
    public Map<String, Object> listQuestion(Question param, Integer page,Integer pageSize) {
        Map<String,Object> result = new HashMap<>();
        List<QuestionModel> voList = new ArrayList<>();
        PageHelper.startPage(page,pageSize);
        List<Question> questionList = questionMapper.selectByParam(param);
        Page pageInfo = new Page(new PageInfo(questionList));
        for (Question question : questionList){
            if (question!=null){
                QuestionModel vo = getQuestionModel(question);
                voList.add(vo);
            }
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg",null);
        result.put("data",voList);
        result.put("page",pageInfo);
        return result;
    }



    /**
     * 添加问卷问题及答案
     * @param userId
     * @param addVO
     * @return
     */
    @Override
    public Map<String, Object> addQuestion(Long userId, QuestionAddVO addVO) {
        Map<String,Object> result = new HashMap<>();
        Question question = BeanUtil.create(addVO,new Question());
        Map<String,Object> map = createQuestion(userId,question);
        if (!map.get("resultCode").equals(CODE_SUCCESS)){
            return map;
        }
        final Long questionId = Long.valueOf(map.get("questionId").toString());
        if (addVO.getQuestionDetailAddVOList()!=null){
            batchAddQuestionDetail(addVO.getQuestionDetailAddVOList(),questionId);
        }
        result.put("resultCode",CODE_SUCCESS);
        result.put("resultMsg","添加成功");
        result.put("questionId",questionId);


        return result;
    }


    /**
     * 编辑问题
     * @param userId
     * @param editVO
     * @return
     */
    @Override
    public Map<String, Object> modQuestion(Long userId, QuestionEditVO editVO) {
        Map<String,Object> result = new HashMap<>();
        final Question question = questionMapper.selectByPrimaryKey(editVO.getQuestionId());
        if (question==null){
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","问题不存在");
            return result;
        }
        BeanUtil.copy(editVO,question);
        question.setUpdateDate(new Date());
        question.setUpdateStaff(userId);
        questionMapper.updateByPrimaryKey(question);
        //删除问题下面所有选项
        questionDetailMapper.deleteByQuestionId(question.getQuestionId());
        //添加问卷问题选项内容
        batchAddQuestionDetail(editVO.getQuestionDetailAddVOList(),question.getQuestionId());

        result.put("resultCode", CommonConstant.CODE_SUCCESS);
        result.put("resultMsg","编辑成功");


        return result;
    }

    private void batchAddQuestionDetail(List<QuestionDetailAddVO> addVOList,Long questionId){
        for (QuestionDetailAddVO detailAddVO : addVOList){
            QuestionDetail detail = BeanUtil.create(detailAddVO,new QuestionDetail());
            detail.setQuestionId(questionId);
            detail.setCreateDate(DateUtil.getCurrentTime());
            detail.setUpdateDate(DateUtil.getCurrentTime());
            detail.setStatusDate(DateUtil.getCurrentTime());
            detail.setUpdateStaff(UserUtil.loginId());
            detail.setCreateStaff(UserUtil.loginId());
            detail.setStatusCd(CommonConstant.STATUSCD_EFFECTIVE);
            questionDetailMapper.insert(detail);
        }
    }

    @Override
    public Map<String, Object> createQuestion(Long userId, Question addVO) {
        Map<String,Object> result = new HashMap<>();
        Question question = BeanUtil.create(addVO,new Question());
        question.setCreateDate(DateUtil.getCurrentTime());
        question.setUpdateDate(DateUtil.getCurrentTime());
        question.setStatusDate(DateUtil.getCurrentTime());
        question.setUpdateStaff(UserUtil.loginId());
        question.setCreateStaff(UserUtil.loginId());
        question.setStatusCd(CommonConstant.STATUSCD_EFFECTIVE);
        questionMapper.insert(question);
        result.put("resultCode", CommonConstant.CODE_SUCCESS);
        result.put("resultMsg","添加成功");
        result.put("questionId",question.getQuestionId());
        return result;
    }

    /**
     * 删除问题
     * @param userId
     * @param questionId
     * @return
     */
    @Override
    public Map<String, Object> delQuestion(Long userId, Long questionId) {
        Map<String,Object> result = new HashMap<>();
        final Question question = questionMapper.selectByPrimaryKey(questionId);
        if (question==null){
            result.put("resultCode",CODE_FAIL);
            result.put("resultMsg","问题不存在");
            return result;
        }
        questionMapper.deleteByPrimaryKey(questionId);
        questionDetailMapper.deleteByQuestionId(questionId);
        result.put("resultCode", CommonConstant.CODE_SUCCESS);
        result.put("resultMsg","删除成功");



        return result;
    }
//
//
//    //弃用
//    @Override
//    public Map<String, Object> modQuestionDetail(Long userId,  QuestionDetail editVO) {
//        Map<String,Object> result = new HashMap<>();
//        QuestionDetail detail = questionDetailMapper.selectByPrimaryKey(editVO.getQstDetailId());
//        if (detail==null){
//            result.put("resultCode",CODE_FAIL);
//            result.put("resultMsg","选项不存在");
//            return result;
//        }
//        BeanUtil.copy(editVO,detail);
//        detail.setUpdateDate(new Date());
//        detail.setUpdateStaff(userId);
//        questionDetailMapper.updateByPrimaryKey(detail);
//        result.put("resultCode", CommonConstant.CODE_SUCCESS);
//        result.put("resultMsg","编辑成功");
//        return result;
//    }
//
//    //弃用
//    /**
//     *删除选项
//     * @param userId
//     * @param questionDetailId
//     * @return
//     */
//    @Override
//    public Map<String, Object> delQuestionDetail(Long userId, Long questionDetailId) {
//        Map<String,Object> result = new HashMap<>();
//        QuestionDetail detail = questionDetailMapper.selectByPrimaryKey(questionDetailId);
//        if (detail==null){
//            result.put("resultCode",CODE_FAIL);
//            result.put("resultMsg","选项不存在");
//            return result;
//        }
//        questionDetailMapper.deleteByPrimaryKey(questionDetailId);
//        result.put("resultCode", CommonConstant.CODE_SUCCESS);
//        result.put("resultMsg","删除成功");
//        return result;
//    }
}
