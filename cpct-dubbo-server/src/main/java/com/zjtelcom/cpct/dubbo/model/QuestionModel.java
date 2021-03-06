package com.zjtelcom.cpct.dubbo.model;

import java.io.Serializable;
import java.util.ArrayList;

public class QuestionModel implements Serializable {
    private QuestionVO question;
    private ArrayList<QuestionDetailVO> questionDetailList;

    public QuestionVO getQuestion() {
        return question;
    }

    public void setQuestion(QuestionVO question) {
        this.question = question;
    }

    public ArrayList<QuestionDetailVO> getQuestionDetailList() {
        return questionDetailList;
    }

    public void setQuestionDetailList(ArrayList<QuestionDetailVO> questionDetailList) {
        this.questionDetailList = questionDetailList;
    }
}
