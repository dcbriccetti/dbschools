package com.dbschools;

public class ServerInfoArgsAdapter{
    public static ServerInfo adaptFromArgs(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Missing host name");
        }
        if (args.length < 2) {
            throw new IllegalArgumentException("Missing RMI Registry port (try 1099)");
        }
        if (args.length < 3) {
            throw new IllegalArgumentException("Missing database name");
        }
        String user = null;
        String pass = null;
        if (args.length >= 4) {
            user = args[3];
        }
        if (args.length >= 5) {
            pass = args[4];
        }
        return new ServerInfo(args[0], Integer.valueOf(args[1]), args[2], user, pass);
    }
}