package cn.zero.spider.controller;

import cn.zero.spider.pojo.ResponseData;
import cn.zero.spider.pojo.User;
import cn.zero.spider.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(path = "/reg")
    public ResponseData<User> reg(String username, String password) {
        User user = userService.saveUser(username, password);
        if (user == null) {
            return new ResponseData<>(false, "用户名已被注册", 200, null);
        }
        return new ResponseData<>(true, "ok", 200, user);
    }

}
