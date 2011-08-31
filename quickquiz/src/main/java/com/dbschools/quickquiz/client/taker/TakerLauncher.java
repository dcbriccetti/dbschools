package com.dbschools.quickquiz.client.taker;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.apache.commons.lang.ObjectUtils;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.apache.log4j.Logger;
import org.jgroups.ChannelException;

import com.dbschools.quickquiz.Quiz;
import com.dbschools.quickquiz.client.CommandLine;
import com.dbschools.quickquiz.client.Resources;
import com.dbschools.quickquiz.netwrk.QuizController;

class TakerLauncher {
    private final static Logger log = Logger.getLogger(TakerLauncher.class);
    
    static void launchTakerWithSelectedQuiz(final org.apache.commons.cli.CommandLine line) throws ChannelException {
        final QuizController quizController = new QuizController(CommandLine.getStack(line));
        final Collection<Quiz> quizzes = quizController.getQuizzes();
        
        if (quizzes.size() == 0) {
            JOptionPane.showMessageDialog(null, Resources.getString("noQuizzesAvail"));
            System.exit(0);
        } else { 
            Quiz selectedQuiz = null;
            String userName = CommandLine.getUserName(line);
            final String quizName = line.hasOption(CommandLine.QUIZ_NAME_CODE) ? 
                    line.getOptionValue(CommandLine.QUIZ_NAME_CODE) : null;
            final String quizPassword = line.hasOption(CommandLine.QUIZ_PASSWORD_CODE) ? 
                    line.getOptionValue(CommandLine.QUIZ_PASSWORD_CODE) : null;
    
            if (isBlank(userName) || isBlank(quizName)) {
                final JoiningDialog jd = showJoinDialog(quizzes);
                userName = jd.getPlayerName();
                selectedQuiz = jd.getSelectedQuiz();
            } else {
                for (Quiz quiz : quizzes) {
                    if (quiz.getName().equals(quizName)) {
                        if (ObjectUtils.equals(quiz.getPassword(), quizPassword)) {
                            selectedQuiz = quiz;
                        }
                        break;
                    }
                }
                if (selectedQuiz == null) {
                    throw new IllegalArgumentException("Invalid quiz name or password");
                }
            }
            final MainWindow mainWindow = new MainWindow(selectedQuiz, userName, quizController);
            mainWindow.initialize();
            mainWindow.setFrameTitle();
            mainWindow.setLocationRelativeTo(null);
            mainWindow.setVisible(true);
        }
    }

    private static JoiningDialog showJoinDialog(final Collection<Quiz> quizzes) {
        final JoiningDialog jd = new JoiningDialog(null, true);
        jd.setQuizzes(new ArrayList<Quiz>(quizzes));
        jd.setLocationRelativeTo(null);
        jd.setVisible(true);
        if (!jd.isJoinRequested()) {
            System.exit(0);
        }
        return jd;
    }

}
