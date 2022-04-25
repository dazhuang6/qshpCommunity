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
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests {
    @Resource
    DiscussPostMapper discussPostMapper;

    @Test
    void contextLoads() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for(DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

}
