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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contenId; // 主键ID

    public String title;

    public String lastUpdateTime;

    public String link;

    // @Column(columnDefinition = "BLOB NOT NULL")
    @Lob
    @Column(columnDefinition = "text")
    public String content;

}
