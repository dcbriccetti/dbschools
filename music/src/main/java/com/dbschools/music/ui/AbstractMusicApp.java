package com.dbschools.music.ui;

import com.dbschools.music.dao.RemoteDao;
import com.dbschools.music.dao.RemoteDaoImpl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.WindowConstants;

import com.dbschools.DatabaseAccessException;
import com.dbschools.NoSuchUserPasswordException;
import com.dbschools.ServerInfo;
import com.dbschools.ServerInfoArgsAdapter;
import com.dbschools.gui.ClientApp;
import com.dbschools.gui.LoginDialog;
import com.dbschools.music.server.MusicServerProxyFactory;

public abstract class AbstractMusicApp extends ClientApp {

    protected static void beginApp(ClientApp app, String[] args) {
        try {
            app.begin(ServerInfoArgsAdapter.adaptFromArgs(args), app);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        }
    }

    private RemoteDaoImpl remoteDao;

    public AbstractMusicApp() {
        setTitle(getApplicationTitle());
    }

    @Override
    protected LoginDialog createLoginDialog(final ServerInfo serverInfo, ClientApp app) {
        final LoginDialog loginDialog = new LoginDialog(app, true) {
            private static final long serialVersionUID = -8224108688163054607L;

            @Override
            protected void tryLogin() throws InterruptedException, InvocationTargetException, RemoteException, NotBoundException, AccessException, DatabaseAccessException, NoSuchUserPasswordException {
                String userNameText = getUserName().trim();
                final String passwordText = getPassword().trim();
                rejectBadUserPass(userNameText, passwordText);
                remoteDao = new RemoteDaoImpl(new MusicServerProxyFactory(
                                        serverInfo.getServerName(), serverInfo.getRmiRegistryPort()));
                remoteDao.logIn(serverInfo.getDatabaseName(), userNameText, passwordText);
                setLoggedInUserName(userNameText);
                setLoggedIn(true);
                setStatusMsg("");
            }};
        loginDialog.setUserName(serverInfo.getUserName());
        loginDialog.setPassword(serverInfo.getPassword());
        return loginDialog;
    }

    @Override
    protected void initialize() throws RemoteException, DatabaseAccessException {
        getContentPane().add(createApplicationComponent(), BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (remoteDao != null) {
                    try {
                        remoteDao.logOut();
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            });
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
    }

    protected abstract String getApplicationTitle();
    
    protected abstract Component createApplicationComponent();

    public RemoteDao getRemoteDao() {
        return remoteDao;
    }
    
}
