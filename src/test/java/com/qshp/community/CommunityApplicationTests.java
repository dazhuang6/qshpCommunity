package com.qshp.community;

import com.qshp.community.dao.DiscussPostMapper;
import com.qshp.community.dao.UserMapper;
import com.qshp.community.entity.DiscussPost;
import com.qshp.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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
