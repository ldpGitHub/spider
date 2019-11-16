package cn.zero.spider.crawler.entity.content;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 章节
 * <p>
 * Created by yuyuhang on 2018/1/7.
 */
@Entity

@Table(name = "content")
public class Content implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contenId; // 主键ID
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String title;


    public String lastUpdateTime;

    public String link;

    //    @Column(columnDefinition = "BLOB NOT NULL")
    @Lob
    @Column(columnDefinition = "text")
    public String content;

    @Override
    public String toString() {
        return "Chapter{" +
                "title='" + title + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
