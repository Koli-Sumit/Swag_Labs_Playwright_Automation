package com.swaglabs.utils;


import com.swaglabs.constants.AppConstants;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Properties;

public class EmailUtil {

    public static void sendEmail() {

        try {

            // Read configuration
            String host = ConfigReader.get(AppConstants.EMAIL_HOST);
            String port = ConfigReader.get(AppConstants.EMAIL_PORT);
            String username = ConfigReader.get(AppConstants.EMAIL_USERNAME);
            String password = ConfigReader.get(AppConstants.EMAIL_PASSWORD);
            String to = ConfigReader.get(AppConstants.EMAIL_TO);
            String subject = ConfigReader.get(AppConstants.EMAIL_SUBJECT);

            // SMTP Properties
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);

            // Authentication
            Session session = Session.getInstance(properties,
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            // Create Message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject(subject);

            // Email Body
            MimeBodyPart bodyPart = new MimeBodyPart();

            String htmlBody = """
                    <html>
                    <head>
                        <style>
                            body {
                                font-family: Arial, Helvetica, sans-serif;
                                font-size: 14px;
                                color: #333333;
                                line-height: 1.6;
                            }
                    
                            .container {
                                max-width: 650px;
                                margin: auto;
                                border: 1px solid #dddddd;
                                border-radius: 8px;
                                overflow: hidden;
                            }
                    
                            .header {
                                background-color: #2563eb;
                                color: white;
                                padding: 18px;
                                text-align: center;
                                font-size: 22px;
                                font-weight: bold;
                            }
                    
                            .content {
                                padding: 25px;
                            }
                    
                            .success {
                                color: #16a34a;
                                font-weight: bold;
                            }
                    
                            .footer {
                                background: #f5f5f5;
                                padding: 15px;
                                text-align: center;
                                font-size: 12px;
                                color: #777777;
                            }
                        </style>
                    </head>
                    
                    <body>
                    
                    <div class="container">
                    
                        <div class="header">
                            Automation Execution Report
                        </div>
                    
                        <div class="content">
                    
                            <p>Hi,</p>
                    
                            <p>
                                Your automation test execution has completed
                                <span class="success">successfully</span>.
                            </p>
                    
                            <p>
                                Please find the attached execution report for detailed test results.
                            </p>
                    
                            <p>
                                Thank you,<br>
                                <strong>Automation Framework</strong>
                            </p>
                    
                        </div>
                    
                        <div class="footer">
                            This is an automated email generated by the Playwright Automation Framework.
                        </div>
                    
                    </div>
                    
                    </body>
                    </html>
                    """;
            bodyPart.setContent(htmlBody, "text/html; charset=UTF-8");

            // Attachment
            String reportPath = "test-output/chaintest/Index.html";

            File reportFile = new File(reportPath);

            if (!reportFile.exists()) {
                throw new RuntimeException(
                        "Report not found : " + reportFile.getAbsolutePath()
                );
            }

            MimeBodyPart attachmentPart = new MimeBodyPart();

            DataSource source = new FileDataSource(reportFile);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(reportFile.getName());

            // Combine Body + Attachment
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            // Send Email
            Transport.send(message);

//            System.out.println("======================================");
//            System.out.println("Email sent successfully.");
//            //System.out.println("Report : " + reportFile.getAbsolutePath());
//            //System.out.println("Recipient : " + to);
//            System.out.println("======================================");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email.", e);
        }
    }

    @Test
    public void testEmail() {
        EmailUtil.sendEmail();
    }
}