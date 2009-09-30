package com.dbschools.music.server;

import java.rmi.RemoteException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dbschools.RmiRegistryCreator;

public class MusicServerRunner {

    /**
     * Launch the server, in standalone test mode.
     * @param args
     */
    public static void main(String[] args) {
        try {
            RmiRegistryCreator.create();
            launchFromSpring();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void launchFromSpring() throws BeansException {
        BeanFactory context = new ClassPathXmlApplicationContext(new String[]{"spring-setprops.xml", "spring-config.xml"});
        context.getBean("musicServer");
    }
    
}
