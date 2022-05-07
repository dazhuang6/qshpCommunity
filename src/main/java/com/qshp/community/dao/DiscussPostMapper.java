package com.qshp.community.dao;

import com.qshp.community.entity.DiscussPost;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    @Select({
            "<script>",
            "select * from discuss_post where status!= 2",
            "<if test='userId!=0'>",
                "and user_id = #{userId}",
            "</if>",
            "order by type desc, create_time desc limit #{offset}, #{limit}",
            "</script>"
    })
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, @Param("offset") int offset, @Param("limit")int limit);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    @Select({
            "<script>",
            "select count(id) from discuss_post where status!=2",
            "<if test='userId!=0'>",
                "and user_id = #{userId}",
            "</if>",
            "</script>"
    })
    int selectDiscussPostRows(@Param("userId") int userId);

    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @Insert({
            "insert into discuss_post(user_id, title, content, type, status, create_time, comment_count, score)",
                    "values(#{userId},#{title},#{content},#{type},#{status},#{createTime},#{commentCount},#{score})"
    })
    int insertDiscussPost(DiscussPost discussPost);

    @Select("Select * from discuss_post where id = #{id}")
    DiscussPost selectDiscussPostById(int id);

    @Update("update discuss_post set comment_count = #{commentCount} where id = #{id}")
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    //用于导入es
    @Select("select * from discuss_post")
    List<DiscussPost> selectDiscussPostsAll();

    @Update("update discuss_post set type = #{type} where id = #{id}")
    int updateType(@Param("id") int id, @Param("type") int type);

    @Update("update discuss_post set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") int id,@Param("status") int status);

}
