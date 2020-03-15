package cn.zero.spider.repository;

import cn.zero.spider.pojo.User;
import cn.zero.spider.pojo.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserBookRepository extends JpaRepository<UserBook,Long> {

    List<UserBook> findUserBookByBookId(Long bookId);
    @Transactional
    void deleteUserBooksByUserId(Long userId);
    @Transactional
    void deleteAllByBookId(Long userId);

}

