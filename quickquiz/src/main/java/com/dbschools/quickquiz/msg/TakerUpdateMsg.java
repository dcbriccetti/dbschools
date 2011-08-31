package com.dbschools.quickquiz.msg;

import com.dbschools.quickquiz.QuizTaker;

public class TakerUpdateMsg extends AbstractBroadcastMsg {
    private static final long serialVersionUID = -7228615853589570605L;
    private final QuizTaker taker;

    public TakerUpdateMsg(QuizTaker taker) {
        super();
        this.taker = taker;
    }

    public QuizTaker getTaker() {
        return taker;
    }
}
