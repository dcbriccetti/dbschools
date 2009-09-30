package com.dbschools.gui;

import java.awt.Cursor;
import java.rmi.RemoteException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.dbschools.DatabaseAccessException;
import com.dbschools.ServerInfo;

public abstract class ClientApp extends JFrame {

    protected String loggedInUserName;

    public void setLoggedInUserName(String loggedInUserName) {
    	this.loggedInUserName = loggedInUserName;
    }

    protected void addUserNameToFrameTitle() {
    	setTitle(new StringBuilder(loggedInUserName).append(" - ").append(getTitle()).toString());
    }

    protected abstract void initialize() throws RemoteException, DatabaseAccessException;

    protected abstract LoginDialog createLoginDialog(ServerInfo serverInfo, ClientApp app);
     
    public void begin(ServerInfo serverInfo, ClientApp app) throws RemoteException, DatabaseAccessException {
    	try {
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	} catch (Exception e) {
            // Ignore
    	}
    	final LoginDialog ld = createLoginDialog(serverInfo, app);
        ld.setLocationRelativeTo(null);
    	ld.setVisible(true);
    	if (! ld.isLoggedIn()) {
    		System.exit(0);
    	} else {
    		app.doAfterLoginSetup();
    	}
    	ld.dispose();
    }

    private void doAfterLoginSetup() throws RemoteException, DatabaseAccessException {
    	addUserNameToFrameTitle();
    	initialize();
        setLocationRelativeTo(null);
    	setVisible(true);
    }

    public void launch(ClientApp clientApp) throws RemoteException, DatabaseAccessException {
    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	clientApp.setLoggedInUserName(loggedInUserName);
    	clientApp.doAfterLoginSetup();
    	setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Enables or disables all the specified components.
     * @param components
     * @param enabled
     */
    public static void enableAll(JComponent[] components, boolean enabled) {
        for (JComponent c : components) {
            c.setEnabled(enabled);
        }
    }

}
