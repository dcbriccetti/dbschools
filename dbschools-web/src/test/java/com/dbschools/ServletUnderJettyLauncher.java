package com.dbschools;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class ServletUnderJettyLauncher {
    public static void main(String... a) {
        Server server = new Server(18080);
        Context root = new Context(server, "/dbschools", Context.SESSIONS);
        root.setResourceBase("/Volumes/Grape/workspace/dbschools-web/src/main/webapp");
        final ServletHolder servletHolder = new ServletHolder(new ServersLauncherServlet());
        root.addServlet(servletHolder, "/servlet/main/*");
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
}
