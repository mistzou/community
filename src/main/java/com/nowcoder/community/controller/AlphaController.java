package com.nowcoder.community.controller;

import com.nowcoder.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/alpha/")
public class AlphaController {

    @Autowired
    private AlphaDao alphaDao;

    @RequestMapping("hello")
    @ResponseBody
    public String getHello(){
        return "hello spring boot";
    }

    @RequestMapping("data")
    @ResponseBody
    public String getData(){
        return alphaDao.select();
    }

    @RequestMapping(path = "/student",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(
            @RequestParam(name = "name",required = false,defaultValue = "1")String name,
            @RequestParam(name="code",required = false,defaultValue = "1")String code
    ){
        System.out.println(name);
        System.out.println(code);
        return "some student";
    }

    @RequestMapping(path = "/students/{name}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent2(@PathVariable("name") String name){
        System.out.println(name);
        return "some student";
    }



}
