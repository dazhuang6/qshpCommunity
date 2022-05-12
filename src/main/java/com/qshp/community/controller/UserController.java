package com.qshp.community.controller;

import com.qshp.community.annotation.LoginRequired;
import com.qshp.community.entity.Comment;
import com.qshp.community.entity.DiscussPost;
import com.qshp.community.entity.Page;
import com.qshp.community.entity.User;
import com.qshp.community.service.*;
import com.qshp.community.util.CommunityConstant;
import com.qshp.community.util.CommunityUtil;
import com.qshp.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    LikeService likeService;

    @Resource
    FollowService followService;

    @Resource
    DiscussPostService discussPostService;

    @Resource
    CommentService commentService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    //index调用的headUrl里的图片路径为/head/xxx
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 响应图片，告诉浏览器我服务器响应的数据类型为图片png类型
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    @LoginRequired
    @PostMapping("/upPassword")
    public String upPassword(String oldPwd, String newPwd, String newPwd2, Model model){
        User user = hostHolder.getUser();
        if (!newPwd.equals(newPwd2)){
            model.addAttribute("newPwdMsg", "密码不一致");
            return "/site/setting";
        }

        Map<String, Object> map = userService.updatePassword(user.getId(), oldPwd, newPwd);
        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPwdMsg", map.get("oldPwdMsg"));
            model.addAttribute("newPwdMsg", map.get("newPwdMsg"));
            return "/site/setting";
        }
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    @LoginRequired
    @RequestMapping(path = "/discussPost", method = RequestMethod.GET)
    public String myDiscussPost(Page page, Model model){
        User user = hostHolder.getUser();
        int count = discussPostService.findDiscussPostRows(user.getId());
        // 分页信息
        page.setLimit(5);
        page.setPath("/user/discussPost");
        page.setRows(count);

        List<DiscussPost> list = discussPostService.findDiscussPosts(user.getId(), page.getOffset(), page.getLimit(), 0);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("user", user);
        model.addAttribute("count", count);
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", 0);
        return "/site/my-post";
    }

    @LoginRequired
    @RequestMapping(path = "/comment", method = RequestMethod.GET)
    public String myComment(Page page, Model model){
        User user = hostHolder.getUser();
        int count = commentService.findMyCommentCount(user.getId());
        // 分页信息
        page.setLimit(5);
        page.setPath("/user/comment");
        page.setRows(count);

        List<Comment> list = commentService.findCommentsByUserId(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> comments = new ArrayList<>();
        if (list != null) {
            for (Comment comment : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);

                Comment comment1 = new Comment();
                DiscussPost post = new DiscussPost();
                if (comment.getEntityType()==ENTITY_TYPE_POST){
                    post = discussPostService.findDiscussPostById(comment.getEntityId());
                }else if (comment.getEntityType()==ENTITY_TYPE_COMMENT){
                    comment1 = commentService.findCommentById(comment.getEntityId());
                }
                map.put("post", post);
                map.put("comment1", comment1);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                map.put("likeCount", likeCount);

                comments.add(map);
            }
        }
        model.addAttribute("user", user);
        model.addAttribute("count", count);
        model.addAttribute("comments", comments);
        return "/site/my-reply";
    }

}
