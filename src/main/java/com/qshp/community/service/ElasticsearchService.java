package com.qshp.community.service;

import com.qshp.community.entity.DiscussPost;

import java.io.IOException;
import java.util.List;

public interface ElasticsearchService {
    void saveDiscussPost(DiscussPost post);
    void deleteDiscussPost(int id);
    List<DiscussPost> searchDiscussPost(String keyword, int current, int limit) throws IOException;
}
