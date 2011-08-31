/*
 * QuickQuiz
 * Copyright (C) 2005–2009 David C. Briccetti
 * www.davebsoft.com
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.dbschools.quickquiz.client.taker;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;

import javax.swing.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.ChannelException;
import org.jgroups.Message;

import com.dbschools.gui.ErrorHandler;
import com.dbschools.gui.LookAndFeelUtil;
import com.dbschools.gui.MacGuiConfig;
import com.dbschools.gui.TitleBlinker;
import com.dbschools.quickquiz.MessagesListModel;
import com.dbschools.quickquiz.Quiz;
import com.dbschools.quickquiz.QuizTaker;
import com.dbschools.quickquiz.client.AbstractClientWindow;
import com.dbschools.quickquiz.client.Resources;
import com.dbschools.quickquiz.client.QuizState;
import com.dbschools.quickquiz.msg.*;
import com.dbschools.quickquiz.netwrk.QuizController;

/**
 * Quiz taker client application.
 * 
 * @author David C. Briccetti
 */
public final class MainWindow extends AbstractClientWindow 
        implements FocusListener, TakerReceivedMessageProcessor {
    
    private static final long serialVersionUID = -1305289771253713033L;
    private final static Logger log = Logger.getLogger(MainWindow.class);
    private MessagesListModel messagesModel;
    private final String takerName;
    private final Quiz quiz;
    private final QuizController quizController;
    
    public static void main(final String args[]) throws ParseException, ChannelException {
        MacGuiConfig.config(Resources.getString("macGiverTitle"));
        LookAndFeelUtil.setSystemLookAndFeel();
        final CommandLine line = com.dbschools.quickquiz.client.CommandLine.processCmdLine(args, null);
        TakerLauncher.launchTakerWithSelectedQuiz(line);
    }

    MainWindow(Quiz selectedQuiz, String takerName, QuizController quizController) {
        super(selectedQuiz.getName());
        this.quiz = selectedQuiz;
        this.takerName = takerName;
        this.quizController = quizController;
        initComponents();
        pnlPlayers.add(takerTableDisplay);
    }

    final void initialize() throws ChannelException {
        txtChatLine.addFocusListener(this);
        txtAnswer.addFocusListener(this);

        quizChannel = quizController.joinQuizChannel(quiz);
        quizChannel.setReceiver(new Receiver(this));
        quizChannel.send(null, null, new JoiningMsg(new QuizTaker(this.takerName, 
                quizChannel.getLocalAddress())));
        quizChannel.getState(null, 5000);

        messagesModel = (MessagesListModel) lstMessages.getModel();
    }

    @Override public void processReceivedMsg(final Message msg) {
        final Object msgObj = msg.getObject();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (msgObj instanceof ChatMsg) {
                    processIncomingChatMsg((ChatMsg) msgObj);
                } else if (msgObj instanceof QuizOverMsg) {
                    processQuizOverMsg();
                } else if (msgObj instanceof JoiningMsg) {
                    processJoiningMsg(msg.getSrc(), msgObj);
                } else if (msgObj instanceof QuestionMsg) {
                    processQuestionMsg((QuestionMsg) msgObj);
                } else if (msgObj instanceof QuestionOverMsg) {
                    processQuestionOver();
                } else if (msgObj instanceof TakerUpdateMsg) {
                    processUpdatedTaker(((TakerUpdateMsg)msgObj).getTaker());
                } else if (msgObj instanceof PointsAwardedMsg) {
                    processPointsAwardedMsg(((PointsAwardedMsg)msgObj));
                } else if (msgObj instanceof ChatEnableMsg) {
                    processChatEnableMsg(msgObj);
                }
            }});
    }

    protected MessagesListModel getMessagesListModel() {
        return messagesModel;
    }

    protected JScrollBar getMessagesScrollBar() {
        return scpMessages.getVerticalScrollBar();
    }

    protected JPanel getChatPanel() {
        return pnlChat;
    }

    protected JPanel getMessagesPanel() {
        return pnlMessages;
    }

    private void processQuestionMsg(QuestionMsg questionMsg) {
        final String question = questionMsg.getQuestion();
        final Integer timeLimit = questionMsg.getTimeLimitSeconds();
        lblQuestion.setText(question);

        messagesModel.add(new MessageFormat(Resources.getString("questionReceived")).format(
                new Object[]{ questionMsg.getCreationTime(), timeLimit, question}));

        /* Configure input controls */
        txtAnswer.requestFocus();
        btnSubmit.setEnabled(true);
        btnSendChatLine.setEnabled(false);

        /* Start countdown meter */
        if (timeLimit != null && timeLimit > 0) {
            countdownMeter.countDown(timeLimit);
        }

        /* Blink the QA panel title */
        new TitleBlinker(pnlQA).blink();
    }

    private void processQuestionOver() {
        lblQuestion.setText(Resources.getString("waitingForNextQuestion"));
        btnSubmit.setEnabled(false);
        restoreChat();
    }

    private void processQuizOverMsg() {
        final MessageFormat formatter = 
                new MessageFormat(Resources.getString("quizOver"));
        JOptionPane.showMessageDialog(MainWindow.this, 
                formatter.format(new Object[] { getQuizName() }));
        deregister();
        System.exit(0);
    }

    private void processChatEnableMsg(Object msgObj) {
        final boolean enabled = ((ChatEnableMsg) msgObj).getChatEnabled();
        configureChatSendButton(enabled);
        getQuizState().setChatEnabled(enabled);
    }

    private void configureChatSendButton(boolean enabled) {
        btnSendChatLine.setEnabled(enabled);
        btnSendChatLine.setText(enabled ? "Send" : "Disabled");
    }

    public void processNewQuizState(QuizState quizState) {
        setQuizState(quizState);
        configureChatSendButton(quizState.isChatEnabled());
        takerTableDisplay.setTakers(quizState.getTakers().values());
    }

    private final void btnSendChatLineActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendChatLineActionPerformed
		handleSendingChatMsg(quizChannel, txtChatLine, takerName);
    }//GEN-LAST:event_btnSendChatLineActionPerformed

    private final void btnSubmitActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        String answer = txtAnswer.getText();
        if (answer.trim().length() <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter a response.");
            return;
        }
        countdownMeter.setActive(false);
        processQuestionOver();
        txtAnswer.setText(null);
        if (txtChatLine.getText().length() > 0) {
            txtChatLine.requestFocus();
        } else {
            txtAnswer.requestFocus();
        }

        final Address giver = quizChannel.getView().getMembers().firstElement();
        sendAsync(quizChannel, giver, new AnswerMsg(txtAnswer.getText()));
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void restoreChat() {
        btnSendChatLine.setEnabled(getQuizState().isChatEnabled());
    }

    /** Exit the Application */
    private final void exitForm(final WindowEvent evt) {//GEN-FIRST:event_exitForm
        try {
            deregister();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    final void setFrameTitle() {
        final Object[] messageArguments = { takerName, getQuizName(), "?" };
		final MessageFormat formatter = new MessageFormat(
                Resources.getString("clientTitle"));
        setTitle(formatter.format(messageArguments));
    }    
    
    private void deregister()  {
        try {
            log.debug("Disconnecting");
            quizChannel.disconnect();
            log.debug("Closing");
            quizChannel.close();
            log.debug("Done");
		} catch (Exception e) {
			ErrorHandler.handleException(e);
        }
    }
    
	/**
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public final void focusGained(final FocusEvent e) {
		if (e.getID() == FocusEvent.FOCUS_GAINED && e.getComponent() == txtChatLine) {
			getRootPane().setDefaultButton(btnSendChatLine);
        }
        
		else if (e.getID() == FocusEvent.FOCUS_GAINED && e.getComponent() == txtAnswer) {
			getRootPane().setDefaultButton(btnSubmit);
        }
	}

	/**
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public final void focusLost(@SuppressWarnings("unused") final FocusEvent e) {
        // Ignore
	}
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel4 = new javax.swing.JLabel();
        pnlQA = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtAnswer = new javax.swing.JTextField();
        btnSubmit = new javax.swing.JButton();
        lblQuestion = new javax.swing.JLabel();
        countdownMeter = new com.dbschools.gui.CountdownMeter();
        pnlChat = new javax.swing.JPanel();
        txtChatLine = new javax.swing.JTextField();
        btnSendChatLine = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        pnlSideBySide = new javax.swing.JPanel();
        pnlPlayers = new javax.swing.JPanel();
        pnlMessages = new javax.swing.JPanel();
        scpMessages = new javax.swing.JScrollPane();
        lstMessages = new javax.swing.JList();

        jLabel4.setText("jLabel4");

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/dbschools/quickquiz/client/taker/Bundle"); // NOI18N
        pnlQA.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("qa"))); // NOI18N
        pnlQA.setLayout(new java.awt.GridBagLayout());

        jLabel2.setDisplayedMnemonic('a');
        jLabel2.setLabelFor(txtAnswer);
        jLabel2.setText(bundle.getString("a")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlQA.add(jLabel2, gridBagConstraints);

        txtAnswer.setColumns(30);
        txtAnswer.setFont(new java.awt.Font("Serif", 0, 14));
        txtAnswer.setToolTipText(bundle.getString("tttAnswer")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 3.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlQA.add(txtAnswer, gridBagConstraints);

        btnSubmit.setMnemonic('s');
        btnSubmit.setText(bundle.getString("submitAnswer")); // NOI18N
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlQA.add(btnSubmit, gridBagConstraints);

        lblQuestion.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblQuestion.setText(bundle.getString("waitingForNextQuestion")); // NOI18N
        lblQuestion.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        lblQuestion.setPreferredSize(new java.awt.Dimension(179, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 2, 2);
        pnlQA.add(lblQuestion, gridBagConstraints);

        countdownMeter.setToolTipText("Displays the amount of time left for answering the question");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlQA.add(countdownMeter, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(pnlQA, gridBagConstraints);

        pnlChat.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("chat"))); // NOI18N
        pnlChat.setLayout(new java.awt.GridBagLayout());

        txtChatLine.setColumns(40);
        txtChatLine.setToolTipText(bundle.getString("tttChatMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        pnlChat.add(txtChatLine, gridBagConstraints);

        btnSendChatLine.setMnemonic('d');
        btnSendChatLine.setText(bundle.getString("sendChatLine")); // NOI18N
        btnSendChatLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendChatLineActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        pnlChat.add(btnSendChatLine, gridBagConstraints);

        jLabel1.setDisplayedMnemonic('m');
        jLabel1.setLabelFor(txtChatLine);
        jLabel1.setText(bundle.getString("chatMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        pnlChat.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 2, 2);
        getContentPane().add(pnlChat, gridBagConstraints);

        pnlSideBySide.setLayout(new java.awt.GridBagLayout());

        pnlPlayers.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("takers"))); // NOI18N
        pnlPlayers.setMaximumSize(new java.awt.Dimension(250, 99999));
        pnlPlayers.setMinimumSize(new java.awt.Dimension(100, 50));
        pnlPlayers.setPreferredSize(new java.awt.Dimension(250, 400));
        pnlPlayers.setLayout(new javax.swing.BoxLayout(pnlPlayers, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 2, 2);
        pnlSideBySide.add(pnlPlayers, gridBagConstraints);

        pnlMessages.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("messages"))); // NOI18N
        pnlMessages.setLayout(new javax.swing.BoxLayout(pnlMessages, javax.swing.BoxLayout.LINE_AXIS));

        lstMessages.setFont(new java.awt.Font("SansSerif", 0, 10));
        lstMessages.setModel(new MessagesListModel());
        scpMessages.setViewportView(lstMessages);

        pnlMessages.add(scpMessages);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 0, 2);
        pnlSideBySide.add(pnlMessages, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(pnlSideBySide, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSendChatLine;
    private javax.swing.JButton btnSubmit;
    private com.dbschools.gui.CountdownMeter countdownMeter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblQuestion;
    private javax.swing.JList lstMessages;
    private javax.swing.JPanel pnlChat;
    private javax.swing.JPanel pnlMessages;
    private javax.swing.JPanel pnlPlayers;
    private javax.swing.JPanel pnlQA;
    private javax.swing.JPanel pnlSideBySide;
    private javax.swing.JScrollPane scpMessages;
    private javax.swing.JTextField txtAnswer;
    private javax.swing.JTextField txtChatLine;
    // End of variables declaration//GEN-END:variables

}
