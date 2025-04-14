package com.graduation.peelcs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件配置类
 */
@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private Integer port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.protocol:smtp}")
    private String protocol;

    @Value("${spring.mail.default-encoding:UTF-8}")
    private String defaultEncoding;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private String starttlsEnable;
    
    @Value("${spring.mail.properties.mail.smtp.timeout:5000}")
    private String timeout;
    
    @Value("${spring.mail.properties.mail.smtp.socketFactory.class:#{null}}")
    private String socketFactoryClass;
    
    @Value("${spring.mail.properties.mail.smtp.socketFactory.port:#{null}}")
    private String socketFactoryPort;

    /**
     * 配置邮件发送器
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setDefaultEncoding(defaultEncoding);
        mailSender.setProtocol(protocol);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.timeout", timeout);
        
        // 如果使用的是SSL/TLS连接
        if ("smtps".equals(protocol) || socketFactoryClass != null) {
            if (socketFactoryClass != null) {
                props.put("mail.smtp.socketFactory.class", socketFactoryClass);
            } else {
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            
            if (socketFactoryPort != null) {
                props.put("mail.smtp.socketFactory.port", socketFactoryPort);
            } else {
                props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            }
        }

        return mailSender;
    }
} 