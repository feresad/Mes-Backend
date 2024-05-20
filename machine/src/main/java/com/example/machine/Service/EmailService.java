package com.example.machine.Service;
import com.example.machine.entity.Panne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Set;


@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendMachinePanneEmail(String toEmail, String subject,
                                      String machineName,
                                      String formattedDate,
                                      String formattedTime,
                                      Set<Panne> pannes) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("feresadouani10@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("machineName", machineName);
            context.setVariable("formattedDate", formattedDate);
            context.setVariable("formattedTime", formattedTime);
            context.setVariable("pannes", pannes);

            String emailContent = templateEngine.process("machine_panne_email", context);
            helper.setText(emailContent, true);

            mailSender.send(message);
            System.out.println("E-mail envoyé avec succès.");
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
        }
    }
}