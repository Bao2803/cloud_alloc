package westwood222.cloud_alloc.service.noti;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import westwood222.cloud_alloc.model.Job;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private static final String jobNotiTemplateName = "jobNotiTemplate";
    private static final String jobNotiSubject = "A Job finished on Cloud Alloc";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void notifyOwnerAboutJob(
            String to,
            List<Job> jobs
    ) {
        Context context = new Context();
        context.setVariable("jobs", jobs);
        String text = templateEngine.process(jobNotiTemplateName, context);
        try {
            sendHTMLEmail(to, jobNotiSubject, text);
        } catch (MessagingException e) {
            throw new RuntimeException("Cannot delivered email", e);
        }
    }

    private void sendHTMLEmail(
            String to,
            String subject,
            String htmlText
    ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
        messageHelper.setText(htmlText, true);
        messageHelper.setSubject(subject);
        messageHelper.setTo(to);
        mailSender.send(message);
    }
}
