/*
 * QuickQuiz
 * Copyright (C) 2007 David C. Briccetti
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

package com.dbschools.quickquiz.client.giver;

import java.awt.event.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.*;

import javax.swing.event.ListSelectionEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.Message;

import com.dbschools.gui.*;
import com.dbschools.gui.SwingWorker;
import com.dbschools.gui.CountdownMeter.CountdownFinishListener;
import com.dbschools.quickquiz.MessagesListModel;
import com.dbschools.quickquiz.PointsAwardeeInfo;
import com.dbschools.quickquiz.Quiz;
import com.dbschools.quickquiz.QuizTaker;
import com.dbschools.quickquiz.client.AbstractClientWindow;
import com.dbschools.quickquiz.client.Resources;
import com.dbschools.quickquiz.client.taker.Simulator;
import com.dbschools.quickquiz.msg.*;
import com.dbschools.quickquiz.netwrk.QuizController;

import javax.swing.event.ListSelectionListener;

/**
 * Quiz giver client application.
 * 
 * @author David C. Briccetti
 */
public final class MainWindow extends AbstractClientWindow implements GiverReceivedMessageProcessor {

    private static final long serialVersionUID = 5740827448803658723L;
    private final static Logger logger = Logger.getLogger(MainWindow.class);
    private final AtomicLong numTakersWhenMsgSent = new AtomicLong();
    private final AtomicLong numAnswersReceived = new AtomicLong();
    private final Quiz quiz;
    private Timer questionTimeoutTimer;

    /**
     * Starts the giver. 
     * @param args the command line arguments
     * @throws ParseException when the command line can’t be parsed
     * @throws ChannelException 
     */
    public static void main(final String args[]) throws ParseException, ChannelException {
    	LookAndFeelUtil.setSystemLookAndFeel();
    	MacGuiConfig.config(Resources.getString("macGiverTitle"));
    
        final CommandLine line = com.dbschools.quickquiz.client.CommandLine.processCmdLine(args, null);
    	final QuizCreationOptions options = showCreationDialog(line);
        final MainWindow window = new MainWindow(options, line);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private MainWindow(QuizCreationOptions qcd, CommandLine line) throws ChannelException {
        super(qcd.getName());
        QuizController quizController = new QuizController(com.dbschools.quickquiz.client.CommandLine.getStack(line));
        quiz = quizController.createQuiz(qcd);
        initComponents();
        takerTableDisplay.setFocusable(false);
        pnlPlayers.add(takerTableDisplay);
        customizeDefaultButtonHandling();
        quizChannel = quizController.joinQuizChannel(quiz);
        quizChannel.setReceiver(new Receiver(this));
        addListeners();
        setFrameTitle();
        pack();
    }

    protected MessagesListModel getMessagesListModel() {
        return (MessagesListModel) lstMessages.getModel();
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

    private void addListeners() {
        takerTableDisplay.addTableKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    char keyChar = e.getKeyChar();
                    if (Character.isDigit(keyChar) && e.isControlDown()) {
                        awardPoints(Integer.parseInt(Character.toString(keyChar)));
                    }
                }
            });
        final ListSelectionModel takerTableSelectionModel = takerTableDisplay.getSelectionModel();
        takerTableSelectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                btnAwardPoints.setEnabled(! takerTableSelectionModel.isSelectionEmpty());
            }
        });
        countdownMeter.addCountdownFinishListener(new CountdownFinishListener() {
            public void countdownFinished() {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        btnSendQuestion.setEnabled(true);
                    }});
            }});
    }

    private void processAnswerMessage(Object msgObj, Message msg) {
        final QuizTaker taker = getQuizState().getTakers().get(msg.getSrc());
        final AnswerMsg answerMsg = ((AnswerMsg) msgObj);
        taker.setLastResponse(answerMsg.getAnswer());
        taker.setLastResponseReceivedAt(new Date());
        logger.info("Taker " + taker.getTakerName() + " answered: " + answerMsg.getAnswer());
        if (numAnswersReceived.incrementAndGet() >= numTakersWhenMsgSent.get()) {
            countdownMeter.setActive(false);
            if (questionTimeoutTimer != null && questionTimeoutTimer.isRunning()) {
                questionTimeoutTimer.stop();
                endQuestion();
            }
        }
        sendAsync(quizChannel, null, new TakerUpdateMsg(taker));
    }

    public void processReceivedMsg(final Message msg) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                final Object msgObj = msg.getObject();

                if (msgObj instanceof ChatMsg) {
                    processIncomingChatMsg((ChatMsg) msgObj);

                } else if (msgObj instanceof AnswerMsg) {
                    processAnswerMessage(msgObj, msg);

                } else if (msgObj instanceof JoiningMsg) {
                    processJoiningMsg(msg.getSrc(), msgObj);

                } else if (msgObj instanceof TakerUpdateMsg) {
                    processUpdatedTaker(((TakerUpdateMsg)msgObj).getTaker());

                } else if (msgObj instanceof PointsAwardedMsg) {
                    processPointsAwardedMsg(((PointsAwardedMsg)msgObj));
                }
            }
        });
    }

    private void customizeDefaultButtonHandling() {
        final FocusListener focusListener = new FocusListener() {
            public final void focusGained(final FocusEvent e) {
                final Component comp = e.getComponent();
                final JRootPane pane = getRootPane();
                if (comp == txtChatLine) {
                    pane.setDefaultButton(btnSendChatLine);
                } else if (comp == cbxQuestions || comp == txtTimeLimit) {
                    pane.setDefaultButton(btnSendQuestion);
                } else if (comp == sprPoints) {
                    pane.setDefaultButton(btnAwardPoints);
                }
            }

            public void focusLost(FocusEvent e) {
                // Ignore
            }
        };
        txtChatLine .addFocusListener(focusListener);
        cbxQuestions.addFocusListener(focusListener);
        txtTimeLimit.addFocusListener(focusListener);
        sprPoints   .addFocusListener(focusListener);
    }
    
    private static QuizCreationOptions showCreationDialog(final CommandLine line) {
        final QuizCreationDialog qcd = new QuizCreationDialog(null, true);
    	qcd.setLocationRelativeTo(null);
        customizeDialogFromCommandLine(qcd, line);
    	qcd.setVisible(true);
    	if (!qcd.isStartRequested()) {
    		System.exit(0);
        }
        return qcd;
    }

    private static void customizeDialogFromCommandLine(final QuizCreationDialog qcd, final CommandLine line) {
        final String userName = com.dbschools.quickquiz.client.CommandLine.getUserName(line);
        if (StringUtils.isNotBlank(userName)) {
            qcd.setGiverName(userName);
        }
        if (line.hasOption(com.dbschools.quickquiz.client.CommandLine.QUIZ_NAME_CODE)) {
            qcd.setName(line.getOptionValue(com.dbschools.quickquiz.client.CommandLine.QUIZ_NAME_CODE));
        }
        if (line.hasOption(com.dbschools.quickquiz.client.CommandLine.QUIZ_PASSWORD_CODE)) {
            qcd.setPassword(line.getOptionValue(com.dbschools.quickquiz.client.CommandLine.QUIZ_PASSWORD_CODE));
        }
    }

    /**
     * Awards points to takers
     */
    private void awardPoints(int pointsAwarded) {
        final int[] selectedIndexes = takerTableDisplay.getSelectedRows();
        final Collection<PointsAwardeeInfo> pointsAwardeesInfo = new ArrayList<PointsAwardeeInfo>();

        if (selectedIndexes.length > 0) {
            for (int selectedIndex : selectedIndexes) {
                final QuizTaker taker = (QuizTaker) takerTableDisplay.getTakers().get(selectedIndex);
                taker.setScore(taker.getScore() + pointsAwarded);

                pointsAwardeesInfo.add(new PointsAwardeeInfo(taker, pointsAwarded));
            }
            sendAsync(quizChannel, null, new PointsAwardedMsg(pointsAwardeesInfo));
        }
    }

    private void setFrameTitle() {
        final Object[] messageArguments = {getQuizName()        };
        final MessageFormat formatter = new MessageFormat(
                Resources.getString("serverTitle"));
        setTitle(formatter.format(messageArguments));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlPlayers = new javax.swing.JPanel();
        pnlChat = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtChatLine = new javax.swing.JTextField();
        btnSendChatLine = new javax.swing.JButton();
        chkChatEnabled = new javax.swing.JCheckBox();
        pnlMessages = new javax.swing.JPanel();
        scpMessages = new javax.swing.JScrollPane();
        lstMessages = new javax.swing.JList();
        pnlQA = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        countdownMeter = new com.dbschools.gui.CountdownMeter();
        pnlButtons = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtTimeLimit = new javax.swing.JFormattedTextField();
        btnSendQuestion = new javax.swing.JButton();
        pnlAward = new javax.swing.JPanel();
        btnAwardPoints = new javax.swing.JButton();
        sprPoints = new javax.swing.JSpinner();
        cbxQuestions = new javax.swing.JComboBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mnuReset = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        mnuLoad = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/dbschools/quickquiz/client/giver/Bundle"); // NOI18N
        pnlPlayers.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("takers"))); // NOI18N
        pnlPlayers.setPreferredSize(new java.awt.Dimension(600, 250));
        pnlPlayers.setRequestFocusEnabled(false);
        pnlPlayers.setLayout(new javax.swing.BoxLayout(pnlPlayers, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 2, 2);
        getContentPane().add(pnlPlayers, gridBagConstraints);

        pnlChat.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("chat"))); // NOI18N
        pnlChat.setLayout(new java.awt.GridBagLayout());

        jLabel4.setDisplayedMnemonic('m');
        jLabel4.setLabelFor(txtChatLine);
        jLabel4.setText(bundle.getString("chatMessage")); // NOI18N
        pnlChat.add(jLabel4, new java.awt.GridBagConstraints());

        txtChatLine.setColumns(40);
        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("quickquiz"); // NOI18N
        txtChatLine.setToolTipText(bundle1.getString("tttChatMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        pnlChat.add(txtChatLine, gridBagConstraints);

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

        chkChatEnabled.setSelected(true);
        chkChatEnabled.setText("Enabled");
        chkChatEnabled.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chatEnable(evt);
            }
        });
        pnlChat.add(chkChatEnabled, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 2, 2);
        getContentPane().add(pnlChat, gridBagConstraints);

        pnlMessages.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("messages"))); // NOI18N
        pnlMessages.setLayout(new javax.swing.BoxLayout(pnlMessages, javax.swing.BoxLayout.LINE_AXIS));

        lstMessages.setFont(new java.awt.Font("SansSerif", 0, 10));
        lstMessages.setModel(new MessagesListModel());
        lstMessages.setFocusable(false);
        lstMessages.setRequestFocusEnabled(false);
        scpMessages.setViewportView(lstMessages);

        pnlMessages.add(scpMessages);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 0, 2);
        getContentPane().add(pnlMessages, gridBagConstraints);

        pnlQA.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("qa"))); // NOI18N
        pnlQA.setLayout(new java.awt.GridBagLayout());

        jLabel2.setDisplayedMnemonic('q');
        jLabel2.setLabelFor(cbxQuestions);
        jLabel2.setText(bundle.getString("question")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlQA.add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlQA.add(countdownMeter, gridBagConstraints);

        pnlButtons.setLayout(new javax.swing.BoxLayout(pnlButtons, javax.swing.BoxLayout.LINE_AXIS));

        jLabel3.setDisplayedMnemonic('l');
        jLabel3.setLabelFor(txtTimeLimit);
        jLabel3.setText(bundle.getString("timeLimit")); // NOI18N
        jPanel2.add(jLabel3);

        txtTimeLimit.setColumns(3);
        txtTimeLimit.setToolTipText(bundle1.getString("tttTimeLimit")); // NOI18N
        txtTimeLimit.setValue(new Integer(20));
        jPanel2.add(txtTimeLimit);

        btnSendQuestion.setMnemonic('s');
        btnSendQuestion.setText(bundle.getString("sendQuestion")); // NOI18N
        btnSendQuestion.setToolTipText(bundle1.getString("tttSendQuestion")); // NOI18N
        btnSendQuestion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendQuestionActionPerformed(evt);
            }
        });
        jPanel2.add(btnSendQuestion);

        pnlButtons.add(jPanel2);

        btnAwardPoints.setMnemonic('a');
        btnAwardPoints.setText(bundle.getString("winnerButton")); // NOI18N
        btnAwardPoints.setToolTipText(bundle.getString("tttAssignWinner")); // NOI18N
        btnAwardPoints.setEnabled(false);
        btnAwardPoints.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAwardPointsActionPerformed(evt);
            }
        });
        pnlAward.add(btnAwardPoints);

        sprPoints.setToolTipText(bundle.getString("tttWinnerPoints")); // NOI18N
        sprPoints.setPreferredSize(new java.awt.Dimension(50, 24));
        pnlAward.add(sprPoints);

        pnlButtons.add(pnlAward);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pnlQA.add(pnlButtons, gridBagConstraints);

        cbxQuestions.setEditable(true);
        cbxQuestions.setMaximumRowCount(100);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlQA.add(cbxQuestions, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(pnlQA, gridBagConstraints);

        jMenu1.setText(bundle.getString("quiz")); // NOI18N

        mnuReset.setText(bundle.getString("resetButton")); // NOI18N
        mnuReset.setToolTipText(bundle.getString("tttResetButton")); // NOI18N
        mnuReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuResetActionPerformed(evt);
            }
        });
        jMenu1.add(mnuReset);

        jMenuItem1.setText("Add Simulated Takers");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSimulatedTakers(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText(bundle.getString("questions")); // NOI18N

        mnuLoad.setText(bundle.getString("load")); // NOI18N
        mnuLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLoadActionPerformed(evt);
            }
        });
        jMenu2.add(mnuLoad);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mnuLoadActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLoadActionPerformed
        final QuestionsDialog questionsDialog = new QuestionsDialog(this, true);
        questionsDialog.setLocationRelativeTo(this);
        questionsDialog.setVisible(true);
        String[] predefinedQuestions = questionsDialog.getQuestions();
        if (predefinedQuestions != null) {
            cbxQuestions.removeAllItems();
            for (String predefinedQuestion : predefinedQuestions) {
                cbxQuestions.addItem(predefinedQuestion);
            }
        }
    }//GEN-LAST:event_mnuLoadActionPerformed

    private void mnuResetActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuResetActionPerformed
        try {
            resetTakers(Quiz.RESET_LAST_RESPONSES | Quiz.RESET_SCORES);
        } catch (java.lang.Throwable ivjExc) {
            ErrorHandler.handleException(ivjExc);
        }
    }//GEN-LAST:event_mnuResetActionPerformed

    private void btnSendChatLineActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendChatLineActionPerformed
        handleSendingChatMsg(quizChannel, txtChatLine, quiz.getGiverName());
	}//GEN-LAST:event_btnSendChatLineActionPerformed

    private void btnSendQuestionActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendQuestionActionPerformed
        sendQuestion();
    }//GEN-LAST:event_btnSendQuestionActionPerformed

    private void sendQuestion() {
        final String question = (String) cbxQuestions.getSelectedItem();
        getQuizState().setQuestion(question);
        btnSendQuestion.setEnabled(false);
        final Integer timeLimitFieldValue = getTimeLimitProcessedValue();
        resetTakers(Quiz.RESET_LAST_RESPONSES);
        numTakersWhenMsgSent.set(quizChannel.getView().size() - 1);
        numAnswersReceived.set(0);
        logger.debug("Sending question");
        sendAsync(quizChannel, null, new QuestionMsg(question, timeLimitFieldValue));
        countdownMeter.countDown(timeLimitFieldValue);
        questionTimeoutTimer = new Timer(timeLimitFieldValue * 1000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                endQuestion();
            }
        });
        questionTimeoutTimer.setRepeats(false);
        questionTimeoutTimer.start();
    }

    private void endQuestion() {
        getQuizState().setQuestion(null);
        logger.info("Sending question over");
        sendAsync(quizChannel, null, new QuestionOverMsg());
    }

    /**
     * Gets the time limit value and converts it into an Integer if necessary
     * @return time limit value
     */
    private Integer getTimeLimitProcessedValue() {
        final Integer timeLimitFieldValue;
        final Object val = txtTimeLimit.getValue();
        if (val instanceof Long) {// Why is it sometimes Long?

            timeLimitFieldValue = ((Long) val).intValue();
        } else {
            timeLimitFieldValue = (Integer) val;
        }

        return timeLimitFieldValue;
    }

	private void btnAwardPointsActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAwardPointsActionPerformed
            awardPoints(((SpinnerNumberModel) sprPoints.getModel()).getNumber().intValue());
    }//GEN-LAST:event_btnAwardPointsActionPerformed
    
    /** Exit the Application */
    private void exitForm(final java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        try {
            quizChannel.send(null, null, new QuizOverMsg());
        } catch (java.lang.Throwable ivjExc) {
            ErrorHandler.handleException(ivjExc);
        }
        System.exit(0);
    }//GEN-LAST:event_exitForm

private void addSimulatedTakers(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSimulatedTakers
    final String[] names = {"Dave Smith", "Mary James", "Jose Garcia", "Mercedes Escalantes",
        "Georgia Smithton", "Frederica Tableau"
    };
    new SwingWorker() {

        @Override
        public Object construct() {
            try {
                for (final String name : names) {
                    new Simulator(name);
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ex) {
                        logger.error(ex);
                    }
                }
            } catch (ChannelException ex) {
                logger.error(ex);
            }
            return null;
        }
    }.start();
}//GEN-LAST:event_addSimulatedTakers

    private void chatEnable(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chatEnable
        final boolean enabled = chkChatEnabled.isSelected();
        logger.debug("Changing chat state. Enabled: " + enabled);
        getQuizState().setChatEnabled(enabled);
        sendAsync(quizChannel, null, new ChatEnableMsg(enabled));
    }//GEN-LAST:event_chatEnable
    
    private void resetTakers(final int resetOptions) {
        // Reset takers locally
        Quiz.resetTakers(getQuizState().getTakers().values(), resetOptions);
        takerTableDisplay.setTakers(getQuizState().getTakers().values());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAwardPoints;
    private javax.swing.JButton btnSendChatLine;
    private javax.swing.JButton btnSendQuestion;
    private javax.swing.JComboBox cbxQuestions;
    private javax.swing.JCheckBox chkChatEnabled;
    private com.dbschools.gui.CountdownMeter countdownMeter;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JList lstMessages;
    private javax.swing.JMenuItem mnuLoad;
    private javax.swing.JMenuItem mnuReset;
    private javax.swing.JPanel pnlAward;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlChat;
    private javax.swing.JPanel pnlMessages;
    private javax.swing.JPanel pnlPlayers;
    private javax.swing.JPanel pnlQA;
    private javax.swing.JScrollPane scpMessages;
    private javax.swing.JSpinner sprPoints;
    private javax.swing.JTextField txtChatLine;
    private javax.swing.JFormattedTextField txtTimeLimit;
    // End of variables declaration//GEN-END:variables

}
