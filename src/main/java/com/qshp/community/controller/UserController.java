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
            model.addAttribute("error", "????????????????????????!");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "????????????????????????!");
            return "/site/setting";
        }

        // ?????????????????????
        fileName = CommunityUtil.generateUUID() + suffix;
        // ???????????????????????????
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // ????????????
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????: " + e.getMessage());
            throw new RuntimeException("??????????????????,?????????????????????!", e);
        }

        // ????????????????????????????????????(web????????????)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    //index?????????headUrl?????????????????????/head/xxx
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // ?????????????????????
        fileName = uploadPath + "/" + fileName;
        // ????????????
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // ????????????????????????????????????????????????????????????????????????png??????
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
            logger.error("??????????????????: " + e.getMessage());
        }
    }

    @LoginRequired
    @PostMapping("/upPassword")
    public String upPassword(String oldPwd, String newPwd, String newPwd2, Model model){
        User user = hostHolder.getUser();
        if (!newPwd.equals(newPwd2)){
            model.addAttribute("newPwdMsg", "???????????????");
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

    // ????????????
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????!");
        }

        // ??????
        model.addAttribute("user", user);
        // ????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // ????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // ????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // ???????????????
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
        // ????????????
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
        // ????????????
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
