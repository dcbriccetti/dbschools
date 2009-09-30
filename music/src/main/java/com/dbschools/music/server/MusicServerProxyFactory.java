package com.dbschools.music.server;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

import com.dbschools.music.Constants;

/**
 * A factory for music server proxies.
 * @author David C. Briccetti
 */
public final class MusicServerProxyFactory {

    static Logger logger = Logger.getLogger(MusicServerProxyFactory.class);
    private final String host;
    private final int rmiRegistryPort;

    /**
     * Creates an instance using the specified host name and port
     * @param host the host name of the RMI registry
     * @param rmiRegistryPort the port of the RMI registry
     */
    public MusicServerProxyFactory(final String host, final int rmiRegistryPort) {
        this.host = host;
        this.rmiRegistryPort = rmiRegistryPort;
    }

    /**
     * Creates an instance using the specified host name
     * @param host the host name of the RMI registry
     */
    public MusicServerProxyFactory(final String host) {
        this(host, Registry.REGISTRY_PORT);
    }
    
    /**
     * Gets a proxy to the music server.
     * @return a music server proxy
     * @throws RemoteException
     * @throws NotBoundException
     * @throws AccessException
     */
    public MusicServer getInstance() throws RemoteException, NotBoundException,
            AccessException {
        final Registry rmiRegistry = LocateRegistry.getRegistry(host, rmiRegistryPort);
        
        final MusicServer musicServer = (MusicServer) rmiRegistry.lookup(Constants.RMI_MUSIC_BIND_NAME);
        logger.debug("getInstance found MusicServer: " + musicServer);
        return musicServer;
    }

}
