package com.dbschools.quickquiz.msg;

import java.io.Serializable;

import com.dbschools.quickquiz.Quiz;

public class QuizReadyMsg implements Serializable {
    private final Quiz quiz;

    public QuizReadyMsg(Quiz quiz) {
        super();
        this.quiz = quiz;
    }

    public Quiz getQuiz() {
        return quiz;
    }
}
