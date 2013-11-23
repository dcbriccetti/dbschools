package com.dbschools.music.orm;

import org.apache.log4j.BasicConfigurator;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dbschools.music.orm.Musician;


public class MusicianTest {

    private Session session;

    public static void main(String...a) {
        new MusicianTest().canLoadAMusician();
    }
    
    public MusicianTest() {
        BasicConfigurator.configure();
        final BeanFactory context = new ClassPathXmlApplicationContext(
                new String[] {"spring-config.xml"});
        final SessionFactory sessionFactory = (SessionFactory)context.getBean("sessionFactory");
        session = sessionFactory.openSession();
    }
    
    @Test public void canLoadAMusician() {
        Musician musician = (Musician) session.load(Musician.class, 100514);
        
    }

}
