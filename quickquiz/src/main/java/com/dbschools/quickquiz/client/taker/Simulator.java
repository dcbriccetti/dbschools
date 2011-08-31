package com.dbschools.quickquiz.client.taker;

import com.dbschools.gui.SwingWorker;
import com.dbschools.quickquiz.Quiz;
import com.dbschools.quickquiz.QuizTaker;
import com.dbschools.quickquiz.msg.AnswerMsg;
import com.dbschools.quickquiz.msg.QuestionMsg;
import com.dbschools.quickquiz.msg.JoiningMsg;
import com.dbschools.quickquiz.netwrk.QuizController;
import java.util.Collection;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.ExtendedReceiverAdapter;
import org.jgroups.JChannel;
import org.jgroups.Message;

/**
 * @author Dave
 */
public class Simulator {
    private final static Logger logger = Logger.getLogger(Simulator.class);
    private final JChannel quizChannel;
    public Simulator(String name) throws ChannelException {
        QuizController quizController = new QuizController(QuizController.NetworkStackKey.UDP_STACK_KEY);
        Collection<Quiz> quizzes = quizController.getQuizzes();
        if (quizzes.size() > 0) {
            Quiz quiz = quizzes.iterator().next();
            quizChannel = quizController.joinQuizChannel(quiz);
            quizChannel.setReceiver(new QuizChannelReceiver());
            quizChannel.send(null, null, new JoiningMsg(new QuizTaker(name, 
                quizChannel.getLocalAddress())));
        } else {
            quizChannel = null;
        }
    }
    private final class QuizChannelReceiver extends ExtendedReceiverAdapter {
        @Override
        public void receive(final Message msg) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final Object msgObj = msg.getObject();
                    if (msgObj instanceof QuestionMsg) {
                        final QuestionMsg questionMsg = (QuestionMsg) msgObj;
                        logger.info("Question received: " + questionMsg);
                        new SwingWorker() {
                           @Override
                            public Object construct() {
                                try {
                                    Thread.sleep((int) (Math.random() * 5000) + 2000);
                                } catch (InterruptedException ex) {
                                    // Ignore
                                }
                                try {
                                    final String[] answers = {
                                        "f + 32 ⨉ 1.8",
                                        "one light year",
                                        "I have to ask my dad",
                                        "I’d use the quadratic formula",
                                        "Einstein?",
                                        "Data from Star Trek",
                                        "Sine or cosine I'm not sur wich",
                                        "The Trouble with Tribbles",
                                        "Put in another toner cartridge",
                                        "Helium is lighter",
                                        "The glass would break",
                                        "I would use GPS and a smartphone"
                                    };
                                    final Address giver = quizChannel.getView().getMembers().firstElement();
                                    quizChannel.send(giver, null, new AnswerMsg(answers[(int)(Math.random() * answers.length)]));
                                    logger.info("Answer sent");
                                } catch (ChannelNotConnectedException ex) {
                                    logger.error(ex);
                                } catch (ChannelClosedException ex) {
                                    logger.error(ex);
                                }
                                return null;
                            }
                        }.start();
                    }
                }});
        }
    
    }
    
}
