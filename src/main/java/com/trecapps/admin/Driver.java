package com.trecapps.admin;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
        "com.trecapps.admin.*",                     // Scan this app
        //"com.trecapps.base.InfoResource.models",    // usable models
        "com.trecapps.auth.*",                      // Authentication library
        //"com.trecapps.pictures.*"  // picture management
})
public class Driver {
}
