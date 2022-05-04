package com.qshp.community.dao;

import com.qshp.community.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Select({
            "select id, user_id, entity_type, entity_id, target_id, content, status, create_time from comment " +
                    "where `status` = 0 and entity_type = #{entityType} and entity_id = #{entityId} " +
                    "order by create_time asc limit #{offset}, #{limit}"
    })
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(id) from comment where status = 0 and entity_type = #{entityType} and entity_id = #{entityId}")
    int selectCountByEntity(@Param("entityType")int entityType, @Param("entityId") int entityId);

    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @Insert({
            "insert into comment(user_id, entity_type, entity_id, target_id, content, status, create_time)",
            "values ( #{userId}, #{entityType}, #{entityId}, #{targetId}, #{content}, #{status}, #{createTime})"
    })
    int insertComment(Comment comment);

    @Select("select * from comment where id = #{id}")
    Comment selectCommentById(int id);
}
