package cn.zero.spider.crawler.entity.chapter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 章节
 * <p>
 * Created by yuyuhang on 2018/1/7.
 */
@Entity

@Table(name = "chapter_list")
public class ChapterList implements Serializable {

    @Id
    private Long bookId; // 主键ID

    @OneToMany(fetch= FetchType.EAGER,cascade={CascadeType.ALL},orphanRemoval = true )
    @JoinColumn(name = "bookId")
    private List<Chapter>  chapters= new ArrayList<>();

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public List<Chapter> getChapterList() {
        return chapters;
    }

    public void setChapterList(List<Chapter> chapterList) {
        this.chapters = chapterList;
    }


}
