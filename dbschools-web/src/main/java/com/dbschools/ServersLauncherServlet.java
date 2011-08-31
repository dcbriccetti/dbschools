package com.dbschools;

import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class ServersLauncherServlet extends HttpServlet {
    private static final long serialVersionUID = 3993499220948844738L;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            RmiRegistryCreator.create();
            BeanFactory context = new ClassPathXmlApplicationContext(
                    new String[] {"spring-setprops.xml", "spring-config.xml"});
            context.getBean("musicServer");
            // TODO launch a JGroups GossipServer?
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

}
