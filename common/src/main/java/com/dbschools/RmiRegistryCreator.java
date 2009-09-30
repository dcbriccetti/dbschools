package com.dbschools;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

/**
 * RMI Registry Creator. Creates an application-specific RMI registry.
 * @author David C. Briccetti
 */
public class RmiRegistryCreator {
    private final static Logger log = Logger
            .getLogger(RmiRegistryCreator.class);
    
    private RmiRegistryCreator() {
        // Don't instantiate
    }

    /**
     * Creates an RMI Registry using the default port.
     * @throws RemoteException
     */
    public static void create() throws RemoteException {
        create(Registry.REGISTRY_PORT);
    }
    
    /**
     * Creates an RMI Registry using the specified port.
     * @param rmiRegistryPort the port to use
     * @throws RemoteException
     */
    public static void create(Integer rmiRegistryPort) throws RemoteException {
        Remote registry = LocateRegistry.createRegistry(rmiRegistryPort);
        log.debug("Created " + registry);
    }

    public static int getDefaultPortIfNull(Integer rmiRegistryPort) {
        return rmiRegistryPort == null ? 
                Registry.REGISTRY_PORT : rmiRegistryPort;
    }

}
