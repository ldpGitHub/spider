package cn.zero.spider.crawler.entity.book;


import cn.zero.spider.crawler.entity.source.Source;

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

@Table(name = "search_book")
public class SearchBook implements Serializable {
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    private boolean isSelected;


    public String getCover() {
        return cover;
    }

    public Long getBookId() {
        bookId = Long.valueOf(this.hashCode());
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    @Id
    private Long bookId; // 书籍Id



    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<SL> getSources() {
        return sources;
    }

    public void setSources(List<SL> sources) {
        this.sources = sources;
    }

    /**
     * 封面图
     */
    public String cover;

    /**
     * 书名
     */
    public String title;

    /**
     * 作者名
     */
    public String author;

    @Override
    public String toString() {
        return "SearchBook{" +
                "cover='" + cover + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", desc='" + desc + '\'' +
                ", sources=" + sources +
                '}';
    }

    /**
     * 描述
     */
//    @Column(name = "bookDesc",columnDefinition = "BLOB NOT NULL")
    @Lob
    @Column(name = "bookDesc",columnDefinition = "text")
    public String desc;

    public String getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapter = lastChapter;
    }

    public String lastChapter;

    /**
     * 源&目录列表链接
     */
    @OneToMany(fetch=FetchType.EAGER,cascade={CascadeType.ALL},orphanRemoval = true )
    @JoinColumn(name = "bookId")
    public List<SL> sources = new ArrayList<>();

    /**
     * 源和对应链接
     */
    @Entity
    public static class SL implements Serializable{
        public SL() {
        }

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long SLId; // 主键ID

        private Long bookId; // 书籍ID

        public Long getBookId() {
            return bookId;
        }

        public void setBookId(Long bookId) {
            this.bookId = bookId;
        }

        public String link;


        @Override
        public String toString() {
            return "SL{" +
                    "link='" + link + '\'' +
                    ", source=" + source +
                    '}';
        }

        public Source source;

        public SL(String link, Source source) {
            this.link = link;
            this.source = source;
        }
    }
}
