package com.qshp.community.dao;

import com.qshp.community.entity.Message;
import lombok.experimental.Accessors;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
    @Select({
            "select id, from_id, to_id, conversation_id, content, status, create_time from message",
            "where id in (select max(id) from message where status != 2 and from_id != 1 and (from_id = #{userId} or to_id = #{userId}) group by conversation_id)",
            "order by id desc limit #{offset}, #{limit}"
    })
    List<Message> selectConversations(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    // 查询当前用户的会话数量.
    @Select({
            "select count(m.maxid) from",
            "(select max(id) as maxid from message where status != 2 and from_id != 1 and (from_id = #{userId} or to_id = #{userId}) group by conversation_id) as m"
    })
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表.
    @Select({
            "select id, from_id, to_id, conversation_id, content, status, create_time from message where status != 2",
            "and from_id != 1 and conversation_id = #{conversationId} order by id desc limit #{offset}, #{limit}"
    })
    List<Message> selectLetters(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    // 查询某个会话所包含的私信数量.
    @Select("select count(id) from message where status != 2 and from_id != 1 and conversation_id = #{conversationId}")
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    @Select({
            "<script>",
            "select count(id) from message where status=0 and from_id != 1 and to_id = #{userId}",
            "<if test='conversationId!=null'>",
            "and conversation_id = #{conversationId}",
            "</if>",
            "</script>"
    })
    int selectLetterUnreadCount(@Param("userId") int userId, @Param("conversationId") String conversationId);

    // 新增消息
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @Insert({
            "insert into message(from_id, to_id, conversation_id, content, status, create_time)",
            "values (#{fromId}, #{toId}, #{conversationId}, #{content}, #{status}, #{createTime})"
    })
    int insertMessage(Message message);

    // 修改消息的状态
    @Update({
            "<script>",
            "update message set status = #{status} where id in",
            "<foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);

    // 查询某个主题下最新的通知
    @Select("select * from message where id in (select max(id) from message where status !=2 and from_id = 1 and to_id = #{userId} and conversation_id = #{topic})")
    Message selectLatestNotice(@Param("userId") int userId, @Param("topic") String topic);

    // 查询某个主题所包含的通知数量
    @Select("select count(id) from message where status !=2 and from_id = 1 and to_id = #{userId} and conversation_id = #{topic}")
    int selectNoticeCount(@Param("userId") int userId, @Param("topic") String topic);

    // 查询未读的通知的数量
    @Select({
            "<script>",
            "select count(id) from message where status=0",
            "and from_id = 1 and to_id = #{userId}",
            "<if test='topic!=null'>",
            "and conversation_id = #{topic}",
            "</if>",
            "</script>"
    })
    int selectNoticeUnreadCount(@Param("userId") int userId, @Param("topic") String topic);

    // 查询某个主题所包含的通知列表
    @Select({
            "select * from message where status!=2 and from_id = 1 and to_id = #{userId}",
            "and conversation_id = #{topic} order by create_time desc limit #{offset}, #{limit}"
    })
    List<Message> selectNotices(@Param("userId") int userId,@Param("topic") String topic,@Param("offset") int offset,@Param("limit") int limit);
}
