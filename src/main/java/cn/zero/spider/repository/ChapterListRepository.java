package cn.zero.spider.repository;

import cn.zero.spider.crawler.entity.chapter.Chapter;
import cn.zero.spider.crawler.entity.chapter.ChapterList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterListRepository extends JpaRepository<ChapterList,Long> {
}
