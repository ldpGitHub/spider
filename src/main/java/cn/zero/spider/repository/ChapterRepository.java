package cn.zero.spider.repository;

import cn.zero.spider.crawler.entity.chapter.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<Chapter,Long> {
}
