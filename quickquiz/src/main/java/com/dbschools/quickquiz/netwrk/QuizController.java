package com.dbschools.quickquiz.netwrk;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.JChannel;

import com.dbschools.quickquiz.Quiz;
import com.dbschools.quickquiz.client.giver.QuizCreationOptions;
import com.dbschools.quickquiz.msg.QuizReadyMsg;

public class QuizController {

    private final static Logger log = Logger.getLogger(QuizController.class);

    public static enum NetworkStackKey {
        UDP_STACK_KEY("udpStack"),
        UDP_GOSSIP_HOST_STACK_KEY("udpGossipHostStack"),
        TUNNEL_STACK_KEY("tunnelStack");
        
        private final String key;

        NetworkStackKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
    
    private static final int QUIZ_CHANNEL_PORT_BASE = 5000;
    private static final int QUIZ_CONTROL_ID = 1;
    private static final int MIN_QUIZ_ID = 2;
    private static final int MAX_QUIZ_ID = 254;

    private static final ResourceBundle networkProps = ResourceBundle.getBundle("network");
    private final JChannel controlChannel;
    private final Map<Address, Quiz> quizzes = new ConcurrentHashMap<Address, Quiz>();
    private final NetworkStackKey stackKey;

    public QuizController(NetworkStackKey stackKey) throws ChannelException {
        this.stackKey = stackKey;
        log.debug("Stack config: " + stackKey.key);

        log.debug("Creating control channel");
        controlChannel = new JChannel(channelProperties(QUIZ_CONTROL_ID));
        controlChannel.setReceiver(new ControlReceiver(quizzes));
        log.debug("Connecting to control channel");
        controlChannel.connect("QuickQuiz control");
        log.debug("Done");
        
        log.debug("Requesting state");
        boolean stateResult = controlChannel.getState(null, 5000);
        log.debug("getState returned " + stateResult);
    }
    
    public Quiz createQuiz(QuizCreationOptions options) throws ChannelNotConnectedException, ChannelClosedException {
        log.debug("Creating quiz");
        Collection<Integer> ids = new HashSet<Integer>();
        for (Quiz quiz : quizzes.values()) {
            ids.add(quiz.getId());
        }
        for (int i = MIN_QUIZ_ID; i <= MAX_QUIZ_ID; ++i) {
            if (! ids.contains(i)) {
                final Quiz quiz = new Quiz(i, options);
                controlChannel.send(null, null, new QuizReadyMsg(quiz));
                log.debug("Quiz created");
                return quiz;
            }
        }
        throw new RuntimeException("All quiz ids are in use");
    }

    public Collection<Quiz> getQuizzes() {
        return quizzes.values();
    }

    public JChannel joinQuizChannel(Quiz quiz) throws ChannelException {
        JChannel chatChannel = new JChannel(channelProperties(quiz.getId()));
        chatChannel.connect(quiz.getName());
        return chatChannel;
    }

    private String channelProperties(final Integer id) {
        final MessageFormat formatter = new MessageFormat(networkProps.getString(stackKey.getKey()));
        final String port = Integer.toString(id + QUIZ_CHANNEL_PORT_BASE);
        return formatter.format(new Object[] {id.toString(), port});
    }

}
