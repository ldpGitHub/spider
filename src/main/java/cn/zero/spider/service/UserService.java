package cn.zero.spider.service;

import cn.zero.spider.pojo.User;

public interface UserService {

    User getUserByUsername(String username);

    User saveUser(String username, String password);

    User saveUserDirect(String username, String mobileToken,String registrationId);


}
