package com.zenkit.util;

import io.vertx.core.Vertx;
import io.vertx.ext.mail.*;

public class EmailUtil {
    private final MailClient mailClient;

    public EmailUtil(Vertx vertx) {
        MailConfig config = new MailConfig()
                .setHostname("smtp.gmail.com")
                .setPort(587)
                .setStarttls(StartTLSOptions.REQUIRED)
                .setUsername("rithikm2609@gmail.com")
                .setPassword("vizb yxou xert nywm");  // Use App Password

        this.mailClient = MailClient.createShared(vertx, config);
    }

    public void sendResetCode(String to, String code) {
        MailMessage message = new MailMessage()
                .setFrom("Zenkit <rithikm2609@gmail.com>")
                .setTo(to)
                .setSubject("Zenkit Password Reset Code")
                .setText("Your password reset code is: " + code);

        mailClient.sendMail(message, res -> {
            if (res.succeeded()) {
                System.out.println("Reset code sent to: " + to);
            } else {
                res.cause().printStackTrace();
            }
        });
    }
}
