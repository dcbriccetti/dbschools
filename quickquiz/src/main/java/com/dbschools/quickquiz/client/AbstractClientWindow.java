package com.dbschools.quickquiz.client;

import java.awt.HeadlessException;
import java.text.MessageFormat;
import java.util.Date;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.Address;

import com.dbschools.gui.TitleBlinker;
import com.dbschools.quickquiz.MessagesListModel;
import com.dbschools.quickquiz.PointsAwardeeInfo;
import com.dbschools.quickquiz.QuizTaker;
import com.dbschools.quickquiz.msg.ChatMsg;
import com.dbschools.quickquiz.msg.JoiningMsg;
import com.dbschools.quickquiz.msg.PointsAwardedMsg;
import com.dbschools.quickquiz.netwrk.Util;
import com.dbschools.quickquiz.netwrk.AsyncSender;

import javax.swing.*;

/**
 * Provides features for the giver and the taker clients.
 * 
 * @author David C. Briccetti
 */
public abstract class AbstractClientWindow extends JFrame implements ReceivedMessageProcessor {
    private static final long serialVersionUID = 1097765678911663990L;
    private final static Logger log = Logger.getLogger(AbstractClientWindow.class);
    private final String quizName;
    protected JChannel quizChannel;
    private QuizState quizState = new QuizState();
    protected final TakerTableDisplay takerTableDisplay = new TakerTableDisplay(
            this instanceof com.dbschools.quickquiz.client.giver.MainWindow ? 
                TakerTableDisplay.TableStyle.GIVER : TakerTableDisplay.TableStyle.TAKER);
    private final AsyncSender asyncSender = new AsyncSender();
    
    protected AbstractClientWindow(final String quizName) throws HeadlessException {
        super();
        this.quizName = quizName;
    }

    protected String getQuizName() {
        return quizName;
    }

    public JChannel getQuizChannel() {
        return quizChannel;
    }

    public void setQuizChannel(JChannel quizChannel) {
        this.quizChannel = quizChannel;
    }

    public QuizState getQuizState() {
        return quizState;
    }

    protected void setQuizState(QuizState quizState) {
        this.quizState = quizState;
    }

    protected void sendAsync(JChannel channel, Address address, Serializable message) {
        asyncSender.enqueue(channel, address, message);
    }

    protected void processIncomingChatMsg(final ChatMsg chatMsg) {
        /* Display in messages window */
        getMessagesListModel().add(new MessageFormat(Resources.getString("chatMsg")).format(new Object[]{
                chatMsg.getCreationTime(), chatMsg.getSenderTakerName(), chatMsg.getChatMessage()}));

        scrollMessagesToEnd();
        new TitleBlinker(getMessagesPanel()).blink();
    }

    private void scrollMessagesToEnd() {
        final JScrollBar scrollBar = getMessagesScrollBar();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollBar.setValue(scrollBar.getMaximum());
            }
        });
    }

    abstract protected MessagesListModel getMessagesListModel(); 
    abstract protected JScrollBar getMessagesScrollBar(); 
    abstract protected JPanel getChatPanel(); 
    abstract protected JPanel getMessagesPanel(); 

    protected void processJoiningMsg(final Address source, final Object msgObj) {
        quizState.getTakers().put(source, ((JoiningMsg)msgObj).getQuizTaker());
        takerTableDisplay.setTakers(quizState.getTakers().values());
    }

    protected void processPointsAwardedMsg(final PointsAwardedMsg paMsg) {
        for (PointsAwardeeInfo pai : paMsg.getPointsAwardeesInfo()) {
            final int numPoints = pai.getPoints();
            
            /* Display in messages window */
            final QuizTaker awardeeTaker = pai.getTaker();
            final Object[] messageArguments = { new Date(), numPoints, 
                    awardeeTaker.getTakerName(), awardeeTaker.getLastResponse() };
            final MessageFormat formatter = new MessageFormat(Resources.getString(
                    numPoints == 1 ? "pointAwarded" : "pointsAwarded"));
            getMessagesListModel().add(formatter.format(messageArguments));
            quizState.getTakers().put(awardeeTaker.getAddress(), awardeeTaker);
        }
        takerTableDisplay.setTakers(quizState.getTakers().values());
        
        scrollMessagesToEnd();
        new TitleBlinker(getMessagesPanel()).blink();
    }

    public void processViewAccepted(View view) {
        if (Util.removeMapEntriesForLeftSources(view, quizState.getTakers()) > 0) {
            takerTableDisplay.setTakers(quizState.getTakers().values());
        }
    }

    public void processUpdatedTaker(final QuizTaker taker) {
        quizState.getTakers().put(taker.getAddress(), taker);
        takerTableDisplay.setTakers(quizState.getTakers().values());
    }

    /**
	 * Sends a chat message to the server 
     * @param channel
    * @param txtChatLine
    * @param senderName
    */
    public void handleSendingChatMsg(final JChannel channel, final JTextField txtChatLine,
            final String senderName) {
        
        final String text = txtChatLine.getText();
        
        if (text.trim().length() > 0) {
            final ChatMsg chatMsg = new ChatMsg(text, senderName);
            sendAsync(channel, null, chatMsg);
        }
        
        txtChatLine.setText(null);
        txtChatLine.requestFocus();
    }
}
