package com.trecapps.admin;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
        "com.trecapps.admin.*",                     // Scan this app
        //"com.trecapps.base.InfoResource.models",    // usable models
        "com.trecapps.auth.*",                      // Authentication library
        "com.trecapps.pictures.*"  // picture management
})
public class Driver {
    public static void main(String[] args)
    {
        ApplicationInsights.attach();
        SpringApplication.run(Driver.class, args);
    }
}
