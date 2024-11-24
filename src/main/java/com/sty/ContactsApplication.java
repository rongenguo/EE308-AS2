package com.sty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * 1.创建HelloWorldMainApplication类,并声明这是一个主程序类也是个SpringBoot应用
 */
@SpringBootApplication(scanBasePackages = {"com.sty"})
public class ContactsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContactsApplication.class, args);
    }
}