package cn.zero.spider.repository;

import cn.zero.spider.crawler.entity.book.SearchBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchResultRepository extends JpaRepository<SearchBook,Long> {
}
