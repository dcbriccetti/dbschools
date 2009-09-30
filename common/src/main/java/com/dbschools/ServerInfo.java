package com.dbschools;

public class ServerInfo {
    private final String serverName;
    private final int rmiRegistryPort;
    private final String databaseName;
    private final String userName;
    private final String password;
    
    public ServerInfo(String serverName, int rmiRegistryPort,
            String databaseName, String userName, String password) {
        super();
        this.serverName = serverName;
        this.rmiRegistryPort = rmiRegistryPort;
        this.databaseName = databaseName;
        this.userName = userName;
        this.password = password;
    }
    
    public String getServerName() {
        return serverName;
    }
    public int getRmiRegistryPort() {
        return rmiRegistryPort;
    }
    public String getDatabaseName() {
        return databaseName;
    }
    public String getUserName() {
        return userName;
    }
    public String getPassword() {
        return password;
    }
}