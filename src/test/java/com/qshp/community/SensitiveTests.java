package com.qshp.community;

import com.qshp.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class SensitiveTests {

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "这里可以赌博,可以嫖娼,可以吸毒,可以开票,哈哈哈!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "这里可以☆赌☆博☆,可以☆嫖☆娼☆,可以☆吸☆毒☆,可以☆开☆票☆,哈哈哈!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }

}
