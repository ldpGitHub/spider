package cn.zero.spider.crawler.entity.chapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * 章节
 * <p>
 * Created by yuyuhang on 2018/1/7.
 */
@Entity
@Table(name = "chapter")
public class Chapter implements Serializable {
    public Long getChapterId() {
        chapterId = StringUtils.isBlank(link) ? null :  (long)Objects.hash(getTitle(), getBookId());
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    @Id
    private Long chapterId; // 主键ID


    public Integer getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(Integer chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    private Integer chapterIndex; //章节排序

    public String title;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chapter)) return false;
        Chapter chapter = (Chapter) o;
        return Objects.equals(getTitle(), chapter.getTitle()) &&
                Objects.equals(getBookId(), chapter.getBookId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getBookId());
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }


    private Long bookId; // 书籍ID


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String lastUpdateTime;



    public String link;
    @Override
    public String toString() {
        return "Chapter{" +
                "title='" + title + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
