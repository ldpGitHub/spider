package cn.zero.spider.service;

import cn.zero.spider.crawler.entity.chapter.Chapter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface ChapterService {
    void saveAll(List<Chapter> chapters);

    List<Chapter> findAll(Example<Chapter> chapterExample, Sort chapterSort);

    Optional<Chapter> findById(long id);

}
