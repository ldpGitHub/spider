package cn.zero.spider.service.impl;

import cn.zero.spider.controller.BookController;
import cn.zero.spider.crawler.entity.chapter.Chapter;
import cn.zero.spider.pojo.User;
import cn.zero.spider.repository.UserRepository;
import cn.zero.spider.service.ChapterService;
import cn.zero.spider.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ChapterServiceImpl implements ChapterService {

    @Override
    public void saveAll(List<Chapter> chapters) {

    }

    @Override

    public List<Chapter> findAll(Example<Chapter> chapterExample, Sort chapterSort) {
        return null;
    }

    @Override
    public Optional<Chapter> findById(long id) {
        return Optional.empty();
    }
}
