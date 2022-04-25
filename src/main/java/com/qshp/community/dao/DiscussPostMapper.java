package com.qshp.community.dao;

import com.qshp.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
