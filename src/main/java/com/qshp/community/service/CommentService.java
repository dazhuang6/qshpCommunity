package com.qshp.community.service;

import com.qshp.community.entity.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit);
    int findCommentCount(int entityType, int entityId);
    int addComment(Comment comment);
    Comment findCommentById(int id);
}
