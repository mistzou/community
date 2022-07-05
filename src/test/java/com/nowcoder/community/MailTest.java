package com.nowcoder.community;

import com.nowcoder.community.utils.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextSend(){
        mailClient.sendMail("1464962591@qq.com","text","Hello My Baby!!!");
    }

    @Test
    public void testHtmlSend(){
        Context context = new Context();
        context.setVariable("username","邹利勤");
        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);
        mailClient.sendMail("1464962591@qq.com","HTML",content);
    }

}
