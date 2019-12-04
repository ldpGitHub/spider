package cn.zero.spider.crawler.entity.chapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * 章节
 * <p>
 * Created by yuyuhang on 2018/1/7.
 */
@Data
@Entity
@Table(name = "chapter")
public class Chapter implements Serializable {

    /**
     * 主键ID
     */
    @Id
    private Long chapterId;

    /**
     * 章节排序
     */
    private Integer chapterIndex;

    public String title;

    /**
     * 书籍ID
     */
    private Long bookId;

    public String lastUpdateTime;

    public String link;

    public Long getChapterId() {
        chapterId = StringUtils.isBlank(link) ? null : (long) Objects.hash(getTitle(), getBookId());
        return chapterId;
    }

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

}
