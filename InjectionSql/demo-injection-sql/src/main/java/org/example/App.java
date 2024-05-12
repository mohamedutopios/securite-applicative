package org.example;


import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class App
{
    public static void main(String[] args) {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8081);

        try {
            tomcat.addWebapp("", new File("src/main/webapp").getAbsolutePath());
            tomcat.start();
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }
}
