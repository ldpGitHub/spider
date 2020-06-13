package cn.zero.spider.crawler.entity.book;


import cn.zero.spider.crawler.entity.source.Source;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 搜索结果（搜索时，书名和作者名一样时，认为是同一本书，合并起来，同时增加一个源）
 * <p>
 * Created by yuyuhang on 2018/1/7.
 */
@Entity
@Data
@Table(name = "search_book")
public class SearchBook implements Serializable {

    /**
     * 书籍Id
     */
    @Id
    private Long bookId;

    private boolean isSelected;

    /**
     * 封面图
     */
    private String cover;

    /**
     * 书名
     */
    private String title;

    /**
     * 作者名
     */
    private String author;

    /**
     * 描述
     */
//    @Column(name = "bookDesc",columnDefinition = "BLOB NOT NULL")
    @Lob
    @Column(name = "bookDesc", columnDefinition = "text")
    private String desc;
    private String lastChapter;
    public long updateTime;

    /**
     * 源&目录列表链接
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = "bookId")
    private List<SL> sources = new ArrayList<>();

  /*  public Long getBookId() {
        bookId = Long.valueOf(this.hashCode());
        return bookId;
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchBook)) return false;
        SearchBook that = (SearchBook) o;
        return getTitle().equals(that.getTitle()) &&
                getAuthor().equals(that.getAuthor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getAuthor());
    }

    /**
     * 源和对应链接
     */
    @Data
    @Entity

    public static class SL implements Serializable {

        private static final long serialVersionUID = -8319559717065460819L;

        /**
         * 主键ID
         */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long SLId;

        /**
         * 书籍ID
         */
        private Long bookId;

        public String link;

        public Source source;

        public SL() {
        }
        public SL(String link, Source source) {
            this.link = link;
            this.source = source;
        }
    }
}
