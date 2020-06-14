package cn.zero.spider.crawler.entity.content;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 章节
 * <p>
 * Created by yuyuhang on 2018/1/7.
 */
@Entity
@Data
@Table(name = "content")
public class Content implements Serializable {


    @Id
    private Long chapterId; // 主键ID

    public String title;

    public String lastUpdateTime;

    public String link;

    public Content(Long chapterId) {
        this.chapterId = chapterId;
    }

    // @Column(columnDefinition = "BLOB NOT NULL")
    @Lob
    @Column(columnDefinition = "text")
    public String content;


}
