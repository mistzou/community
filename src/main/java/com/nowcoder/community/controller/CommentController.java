package com.nowcoder.community.controller;

import ch.qos.logback.core.joran.spi.EventPlayer;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.Eventproducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private Eventproducer eventproducer;


    @RequestMapping(path = "/add/{postId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("postId")int id, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(comment.getUserId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",id);
        if(event.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost discussPost = discussPostService.selectDiscussPostById(comment.getEntityId());
            event.setEntityUserId(discussPost.getUserId());
        }else if(event.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment comment1 = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(comment1.getUserId());
        }

        eventproducer.fireEvent(event);


        return "redirect:/discuss/details/"+id;
    }

}
