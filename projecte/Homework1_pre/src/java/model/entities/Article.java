package model.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;

//import jakarta.validation.constraints.NotNull;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ARTICLE")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
   
    @Id
    @SequenceGenerator(name = "Article_Gen", sequenceName = "ARTICLE_GEN", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Article_Gen")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(length = 500)
    private String summary;

    private String imageUrl;

    @Temporal(TemporalType.DATE)
    private Date publicationDate;

    private int viewCount;

    private boolean isPrivate;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Customer author;

    @ManyToMany
    @JoinTable(
        name = "ARTICLE_TOPIC",
        joinColumns = @JoinColumn(name = "ARTICLE_ID"),
        inverseJoinColumns = @JoinColumn(name = "TOPIC_ID")
    )
    private List<Topic> topics;

    // Constructor sin parámetros
    public Article() {}
    
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

    public Customer getAuthor() {
        return author;
    }

    public void setAuthor(Customer author) {
        this.author = author;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }


    // Métodos hashCode, equals y toString

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Article)) {
            return false;
        }
        Article other = (Article) object;
        if ((this.id == null && other.id != null) || 
            (this.id != null && !this.id.equals(other.id))) {
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.entities.Article[ id=" + id + " ]";
    }
}
