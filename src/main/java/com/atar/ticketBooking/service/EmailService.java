package com.atar.ticketBooking.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
@AllArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    private static final String EMAIL_ADDR = "ticket.booker.bpmn@gmail.com";

    private static final String SUCCES_TEMPLATE = "reservation-succesful.html";

    public String makeTemplate(String template) {
        var context = new Context();
        return templateEngine.process(template, context);
    }

    public void sendEmail(String to, String subject, String content) {
        try {
            var message = javaMailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(EMAIL_ADDR);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public void sendReservationCodeEmail(String to) {
        var emailContent = makeTemplate(SUCCES_TEMPLATE);
        sendEmail(to, "Potwierdzenie rezerwacji", emailContent);
    }
}
