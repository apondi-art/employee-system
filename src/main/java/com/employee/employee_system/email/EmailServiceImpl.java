package com.employee.employee_system.email;

import com.employee.employee_system.Dto.EmployeeResponseDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Async
    @Override
    public void sendWelcomeEmail(EmployeeResponseDto employee) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFrom());
            helper.setTo(employee.getEmail());
            helper.setSubject("Welcome to the team, " + employee.getFirstName() + "!");
            helper.setText(buildWelcomeHtml(employee), true);

            mailSender.send(message);
            log.info("Welcome email sent to {}", employee.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", employee.getEmail(), e.getMessage());
        }
    }

    private String buildWelcomeHtml(EmployeeResponseDto e) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                  <h2>Welcome to the team, %s!</h2>
                  <p>Your employee profile has been created. Here are your details:</p>
                  <table style="border-collapse: collapse;">
                    <tr><td style="padding: 4px 12px 4px 0;"><strong>Full Name</strong></td>
                        <td>%s %s</td></tr>
                    <tr><td style="padding: 4px 12px 4px 0;"><strong>Email</strong></td>
                        <td>%s</td></tr>
                    <tr><td style="padding: 4px 12px 4px 0;"><strong>Department</strong></td>
                        <td>%s</td></tr>
                    <tr><td style="padding: 4px 12px 4px 0;"><strong>Joining Date</strong></td>
                        <td>%s</td></tr>
                  </table>
                  <br/>
                  <p>Best regards,<br/><strong>HR Team</strong></p>
                </body>
                </html>
                """.formatted(
                        e.getFirstName(),
                        e.getFirstName(), e.getLastName(),
                        e.getEmail(),
                        e.getDepartment(),
                        e.getDateOfJoining()
                );
    }
}
