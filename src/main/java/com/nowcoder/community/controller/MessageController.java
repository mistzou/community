package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;



    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        page.setPath("/letter/list");
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.findConversation(user.getId(), page.getOffset() ,page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(Message message:conversationList){
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId()?message.getToId():message.getFromId();
                map.put("target",userService.getUserById(targetId));
                conversations.add(map);
            }
        }

        model.addAttribute("conversation",conversations);
        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);


        return "/site/letter";
    }


    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId")String conversationId,Model model,Page page){
        //分页信息
        page.setLimit(5);
        page.setRows(messageService.findLetterCount(conversationId));
        page.setPath("/letter/detail/"+conversationId);
        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId,page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if(letterList != null){
            for(Message message:letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.getUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);

        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids,1);
        }


        return "/site/letter-detail";
    }

    public User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0){
            return userService.getUserById(id1);
        }else{
            return userService.getUserById(id0);
        }
    }

    @RequestMapping(path = "/letter/sendMessage",method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }

        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId()< message.getToId()){
            message.setConversationId(message.getFromId()+"_"+ message.getToId());
        }else{
            message.setConversationId(message.getToId()+"_"+ message.getFromId());
        }

        messageService.insertMessage(message);
        return CommunityUtil.getJSONString(0,"发送成功！");
    }


    @RequestMapping(path = "/letter/delMessage",method = RequestMethod.POST)
    @ResponseBody
    public String getDelMessage(int id){
        List<Integer> list = new ArrayList<>();
        list.add(id);
        messageService.readMessage(list,2);
        return CommunityUtil.getJSONString(0,"删除成功");
    }


    public List<Integer> getLetterIds(List<Message> list){
        List<Integer> ids = new ArrayList<>();
        if (!list.isEmpty()){
            for (Message message:list){
                if(hostHolder.getUser().getId()==message.getToId() && message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String notice(Model model){
        User user = hostHolder.getUser();

        //查询评论类通知
        Message message = messageService.findLastNotice(user.getId(),TOPIC_COMMENT);
        Map<String,Object> messageVo = new HashMap<>();
        getMessageMap(user, message, messageVo, TOPIC_COMMENT);
        model.addAttribute("commentNotice",messageVo);

        //点赞类通知
        message = messageService.findLastNotice(user.getId(),TOPIC_LIKE);
        messageVo = new HashMap<>();
        getMessageMap(user, message, messageVo, TOPIC_LIKE);
        model.addAttribute("likeNotice",messageVo);

        //关注类通知
        message = messageService.findLastNotice(user.getId(),TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        messageVo.put("message",message);
        if (message != null){
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.getUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("count",count);
            messageVo.put("unread",messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW));
        }
        model.addAttribute("followNotice",messageVo);

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }

    private void getMessageMap(User user, Message message, Map<String, Object> messageVo, String topicComment) {
        messageVo.put("message",message);
        if (message != null){
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user",userService.getUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), topicComment);
            messageVo.put("count",count);
            messageVo.put("unread",messageService.findNoticeUnreadCount(user.getId(), topicComment));
        }
    }


    @RequestMapping(path = "/notices/detail/{topic}",method = RequestMethod.GET)
    public String notices(@PathVariable("topic")String topic,Model model,Page page){
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notices/detail/"+topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null){
            for (Message message : noticeList){
                Map<String,Object> map = new HashMap<>();
                //通知
                map.put("notice",message);
                //内容
                String content = HtmlUtils.htmlUnescape(message.getContent());
                Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
                map.put("user",userService.getUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                //通知作者
                map.put("fromUser",userService.getUserById(message.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);

        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids,1);
        }

        return "/site/notice-detail";
    }


}
