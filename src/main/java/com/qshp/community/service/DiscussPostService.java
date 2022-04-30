package com.qshp.community.service;

import com.qshp.community.entity.DiscussPost;

import java.util.List;

public interface DiscussPostService {

    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit);
    int findDiscussPostRows(int userId);
    int addDiscussPost(DiscussPost post);
    DiscussPost findDiscussPostById(int id);
    int updateCommentCount(int id, int commentCount);
}
