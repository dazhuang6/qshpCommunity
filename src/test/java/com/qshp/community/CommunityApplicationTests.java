package com.qshp.community;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommunityApplicationTests {

    @Test
    void contextLoads() {
        String a = new String("abc");
        String b = "abc";
        String c = "abc";
        System.out.println(a==b);
        System.out.println(b==c);
    }

}
