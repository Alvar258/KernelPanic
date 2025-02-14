package model.entities;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD) // Esto evita que tengas que anotar cada getter
public class ArticleDTO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String imageUrl;
    private Date publicationDate;
    private int viewCount;
    private boolean isPrivate;
    private String authorName;
    private List<String> topics;

    // Constructor
    public ArticleDTO(Long id, String title, String content, String summary, String imageUrl,
                     Date publicationDate, int viewCount, boolean isPrivate, String authorName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.publicationDate = publicationDate;
        this.viewCount = viewCount;
        this.isPrivate = isPrivate;
        this.authorName = authorName;
    }
    // Constructor sin parámetros
    public ArticleDTO() {}
    
    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public boolean isIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }
}
