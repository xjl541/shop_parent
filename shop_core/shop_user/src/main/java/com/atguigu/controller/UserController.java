package com.atguigu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("login.html")
    public String login(HttpServletRequest request){
        String originalUrl = request.getParameter("originalUrl");
        request.setAttribute("originalUrl",originalUrl);
        return "login";
    }
}
