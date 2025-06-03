package br.com.knowledgeislands.service;

import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.knowledgeislands.model.entity.AttemptSendEmail;
import br.com.knowledgeislands.model.entity.Contributor;
import br.com.knowledgeislands.model.entity.SharedLinkCommit;
import br.com.knowledgeislands.model.enums.DevSurveyEmailType;
import br.com.knowledgeislands.repository.AttemptSendEmailRepository;
import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private AttemptSendEmailRepository attemptSendEmailRepository;
	@Value("${configuration.app.send.email.devs}")
	private boolean sendEmail;
	@Value("${spring.mail.username}")
	private String email;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendSingleEmail(SharedLinkCommit sharedLinkCommit) {
		Contributor contributor = sharedLinkCommit.getAuthor();
		String link = createFileLink(sharedLinkCommit);
		String text = getTextEmailSurveyGenAIRawText(contributor.getName(), link);
		String to = cleanEmail(contributor.getEmail());
		AttemptSendEmail attemptSendEmail = null;
		try {
			attemptSendEmail = new AttemptSendEmail(new Date(), contributor, DevSurveyEmailType.RAW, text);
			sendEmail(to, KnowledgeIslandsUtils.SUBJECT_EMAIL_SURVEY_GENAI, text);
			log.info("Sending email to: "+to);
			attemptSendEmail.setSuccess(true);
		} catch (Exception e) {
			attemptSendEmail = new AttemptSendEmail(new Date(), contributor, DevSurveyEmailType.RAW);
			attemptSendEmail.setError(e.getMessage());
			attemptSendEmail.setSuccess(false);
			log.error("Erro on sending email:"+e.getMessage());
		} finally {
			attemptSendEmailRepository.save(attemptSendEmail);
		}
	}

	private String cleanEmail(String email) {
		return email
				.replaceAll("^[“”\"']+|[“”\"']+$", "") 
				.replaceAll("\\p{Cntrl}", "")         
				.replaceAll("\\s+", "")               
				.trim();                           
	}

	private String atualizarHashNaUrl(String urlOriginal, String novoHash) {
		if (urlOriginal == null || novoHash == null) {
			throw new IllegalArgumentException("URL ou hash não podem ser nulos.");
		}
		return urlOriginal.replaceFirst("(?<=/blob/)[^/]+", novoHash);
	}

	private String createFileLink(SharedLinkCommit sharedLinkCommit) {
		return atualizarHashNaUrl(sharedLinkCommit.getFileRepositorySharedLinkCommit().getFile().getHtmlUrl(), 
				sharedLinkCommit.getCommitFileAddedLink().getCommit().getSha());
	}

	public void sendEmail(String to, String subject, String body) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		message.setFrom(new InternetAddress(email));
		message.setRecipients(MimeMessage.RecipientType.TO, to);
		message.setSubject(subject);
		message.setContent(body, "text/html; charset=utf-8");
		//if(false) {
		mailSender.send(message);
		//}
	}

	public String getTextEmailSurveyGenAIRawText(String devName, String fileLink) {
		return """
				<p>Dear %s,</p>

				<p>My name is Otávio Cury, and I am a software engineering researcher at the Federal University of Piauí, Brazil. I invite you to participate in a short academic survey (approx. 3 minutes) about how generative AI tools (like ChatGPT or GitHub Copilot) impact developers’ code understanding.</p>

				<p>Our research team is investigating the impact of AI-generated code on source code comprehension and its implications for developer expertise metrics. Your participation will help us better understand these challenges in software development.</p>

				<p><b>Your participation is voluntary and anonymous.</b> All collected data will be used strictly for academic purposes. As a thank-you, participants will receive early access to the final paper and research findings.</p>

				<hr>
				<h3>Survey Questions</h3>

				<p><b>Question 1:</b> What is your experience level in software development?</p>
				<ol>
				    <li>Junior (up to 2 years)</li>
				    <li>Mid-level (between 2 and 5 years)</li>
				    <li>Senior (over 5 years)</li>
				</ol>

				<p><b>Question 2:</b> How often do you use generative AI tools (e.g., ChatGPT, Copilot, Gemini) to generate code?</p>
				<ol>
				    <li>Never</li>
				    <li>Rarely (less than once a week)</li>
				    <li>Occasionally (once or twice a week)</li>
				    <li>Frequently (almost every day)</li>
				    <li>Always (every day)</li>
				</ol>

				<p><b>Question 3:</b> Do you believe using generative AI for code generation affects your understanding of the integrated code?</p>
				<ol>
				    <li>Positively – It helps me understand the code better</li>
				    <li>No significant impact – My understanding is unaffected</li>
				    <li>Mixed – It depends on the context or scenario</li>
				    <li>Negatively – It diminishes my understanding or learning of the code</li>
				    <li>Unsure</li>
				</ol>

				<p><b>Question 4:</b> How do you typically integrate code generated by generative AI into your projects?</p>
				<ol>
				    <li>I integrate the code with minimal scrutiny, ensuring it works without a detailed review</li>
				    <li>I integrate the code with a general understanding of its functionality (e.g., inputs and outputs)</li>
				    <li>I integrate the code after a detailed review, understanding its implementation thoroughly</li>
				    <li>I use the code as inspiration or a reference but rarely incorporate it directly</li>
				    <li>Other: [Please specify]</li>
				</ol>

				<p><b>Question 5:</b> Have you encountered difficulties maintaining AI-generated code (e.g., ChatGPT, Copilot) previously integrated into your project, such as modifying, extending, or debugging it?</p>
				<ol>
				    <li>Yes, frequently</li>
				    <li>Yes, occasionally</li>
				    <li>No, but I rarely integrate AI-generated code</li>
				    <li>No, never</li>
				</ol>

				<p><b>Question 6:</b> Based on your past contributions, how confident do you feel about maintaining the following file: <a href="%s">%s</a></p>
				<p>(Maintenance may include debugging, extending, or refactoring the code.)</p>
				<ol>
				    <li>I have no familiarity with this file and wouldn’t be able to work on it</li>
				    <li>I remember contributing to it, but I would need to re-familiarize myself before making changes</li>
				    <li>I understand the general purpose and logic, and could make small changes</li>
				    <li>I’m confident I can maintain this file effectively</li>
				    <li>I’m the go-to expert on this file and know it in depth</li>
				</ol>

				<p>Feel free to leave any additional comments on the topic.</p>

				<p>Thank you for your time and contribution to this research.</p>

				<p>Best regards,<br>
				<b>Otávio Cury</b><br>
				Software Engineering Researcher<br>
				<a href="https://scholar.google.com.br/citations?user=bTz_EPcAAAAJ&hl=pt-BR&authuser=1" target="_blank">My Google Scholar</a><br>
				Federal University of Piauí, Brazil</p>
				""".formatted(devName, fileLink, fileLink);
	}

	public String getTextEmailSurveyGenAIGoogleForm(String devName) {
		return """
				Dear %s,

				<p>My name is Otávio Cury, and I am a software engineering researcher at the Federal University of Piauí, Brazil. You can find more details about my work on my <a href="https://scholar.google.com.br/citations?user=bTz_EPcAAAAJ">Google Scholar</a>.</p>

				<p>Our research team is investigating the impact of generative AI on developers' understanding of the source code they integrate into their projects. This survey aims to explore how developers incorporate AI-generated code and whether these tools influence their comprehension of the integrated code.</p>

				<p><b>We assure you that all collected data will be anonymized in future research publications and used exclusively for academic purposes.</b></p>

				<p>Here is the link to the Google form with the survey: https://forms.gle/CtL6XzdXs2D6PXnFA</p>

				<p>We appreciate your time and insights, which will contribute significantly to this research.</p>

				Best regards,<br>
				Otávio Cury<br>
				Software Engineering Researcher<br>
				Federal University of Piauí, Brazil
				""".formatted(devName);
	}

}
