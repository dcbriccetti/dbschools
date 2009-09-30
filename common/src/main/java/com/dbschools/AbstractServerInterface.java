package com.dbschools;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AbstractServerInterface extends Remote {

    /**
     * Logs in to the server.
     * 
     * @param userName
     * @param password
     * @return session id
     * @throws RemoteException
     * @throws DatabaseAccessException
     * @throws NoSuchUserPasswordException
     */
    int logIn(String userName, String password) 
    throws RemoteException, DatabaseAccessException, NoSuchUserPasswordException;

}
