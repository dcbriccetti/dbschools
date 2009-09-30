package com.dbschools.music;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.dbschools.music.server.MusicServer;

public final class StudentSelectorTest {

    private int sessionId;
    private MusicServer musicServer;
    private final static Logger log = Logger.getLogger(StudentSelectorTest.class);
    
    public static void main(String[] args) {
        BasicConfigurator.configure();
        new StudentSelectorTest().run();
    }

    private void run() {
//        try {
//            musicServer = MusicServerProxyFactory.getInstance("localhost");
//            sessionId = musicServer.logIn("admin", "admin");
//            JFrame frame = new JFrame(ClassUtils.getShortClassName(getClass()));
//            final Container cp = frame.getContentPane();
//            cp.setLayout(new BorderLayout());
//            final StudentSelector studentSelector = new StudentSelector();
//            studentSelector.addInstrumentsItemListener(new ItemListener() {
//                public void itemStateChanged(ItemEvent e) {
//                    log.debug("instrument selection: " + e);
//                }});
//            StudentSelectorDataSupplier supplier = new 
//                    StudentSelectorDataSupplier(sessionId, musicServer);
//            studentSelector.setBands(supplier.getGroupItems());
//            studentSelector.setInstruments(ItemDisplayAdapter.getItemList
//                    ((Collection<Instrument>)musicServer.getEntities(sessionId, Instrument.class)));
//            cp.add(studentSelector, BorderLayout.NORTH);
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setLocationRelativeTo(null);
//            frame.pack();
//            frame.setVisible(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
