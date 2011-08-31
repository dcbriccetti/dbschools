package com.dbschools.quickquiz.client;

import java.util.Arrays;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.dbschools.quickquiz.netwrk.QuizController;

/**
 * Command line handling.
 * @author David C. Briccetti
 */
public class CommandLine {
    public static final String STACK_CODE = "s";
    public static final String HELP_CODE = "?";
    public static final String USER_NAME_CODE = "n";
    public static final String QUIZ_NAME_CODE = "q";
    public static final String QUIZ_PASSWORD_CODE = "P";

    /**
     * A callback for subclasses to add command line options.
     * 
     * @author David C. Briccetti
     */
    public interface OptionsSetter {

        /**
         * Add the specified options to the command line processing.
         * @param options the options to add
         */
        void addOptions(Options options);
    }

    public static String getUserName(org.apache.commons.cli.CommandLine line) {
        return line.hasOption(CommandLine.USER_NAME_CODE) ? 
                line.getOptionValue(CommandLine.USER_NAME_CODE).replace('_', ' ') : null;
    }

    public static QuizController.NetworkStackKey getStack(org.apache.commons.cli.CommandLine line) {
        return line.hasOption(CommandLine.STACK_CODE) ?
                QuizController.NetworkStackKey.valueOf(line.getOptionValue(CommandLine.STACK_CODE)) : 
                QuizController.NetworkStackKey.UDP_STACK_KEY;
    }

    /**
     * Processes arguments and returns a CommandLine.
     * @param args the program's main arguments
     * @param optionsSetter a callback object for processing additional options
     * @return a CommandLine
     * @throws ParseException
     */
    public static org.apache.commons.cli.CommandLine processCmdLine(String[] args, 
            CommandLine.OptionsSetter optionsSetter) throws ParseException {
        System.out.println(Arrays.asList(args));
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        options.addOption(CommandLine.HELP_CODE, "help", false, "display help" );
        options.addOption(CommandLine.STACK_CODE, "stack", true, 
                "name of stack configuration" );
        options.addOption(CommandLine.USER_NAME_CODE, "username", true, "user name" );
        options.addOption(CommandLine.QUIZ_NAME_CODE, "quizname", true, "quiz name" );
        options.addOption(CommandLine.QUIZ_PASSWORD_CODE, "quizpassword", true, "quiz password" );
    
        if (optionsSetter != null) {
            optionsSetter.addOptions(options);
        }
    
        org.apache.commons.cli.CommandLine line = parser.parse( options, args );
    
        if (line.hasOption(CommandLine.HELP_CODE)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "MainWindow", options );
            return null;
        }
    
        return line;
    }

}
