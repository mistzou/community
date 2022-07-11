package com.nowcoder.community;

import com.nowcoder.community.utils.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;
    /**
     * 赌博
     * 嫖娼
     * 吸毒
     * 斗殴
     */

    @Test
    public void test(){
        String text = "这里有人@赌@博，有人@嫖@娼，有人@吸@毒，有人@斗@殴,fabc";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }




}
