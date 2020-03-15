package cn.zero.spider.service.impl;

import cn.zero.spider.pojo.User;
import cn.zero.spider.repository.UserRepository;
import cn.zero.spider.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserDetailsService, UserService {

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }


    @Override
    public User saveUser(String username, String password) {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            User newUser = new User(username, encoder.encode(password));
            return userRepository.saveAndFlush(newUser);
        }
        return null;
    }

    @Override
    public User saveUserDirect(String username, String mobileToken,String registrationId) {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setMobileToken(mobileToken);
            newUser.setRegistrationId(registrationId);
            return userRepository.saveAndFlush(newUser);
        }else {
            user.setRegistrationId(registrationId);
            user.setMobileToken(mobileToken);
            return userRepository.saveAndFlush(user);
        }
    }

}
