package ru.job4j.grabber;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Модель Post
 * id идентификатор вакансии (берется из нашей базы данных)
 * name название
 * text описание вакансии
 * link ссылка на описание вакансии
 * created дата создания вакансии
 */
public class Post {
    private int id;
    private String name;
    private String text;
    private String link;
    private LocalDateTime created;

    public Post(String name, String text, String link, LocalDateTime created) {
        this.name = name;
        this.text = text;
        this.link = link;
        this.created = created;
    }

    public Post(int id, String name, String text, String link, LocalDateTime created) {
        this.id = id;
        this.name = name;
        this.text = text;
        this.link = link;
        this.created = created;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return id == post.id && Objects.equals(link, post.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, link);
    }

    @Override
    public String toString() {
        return "Post{"
                + "id=" + id
                + ", title='" + name + '\''
                + ", description='" + text + '\''
                + ", link='" + link + '\''
                + ", created=" + created
                + '}';
    }
}
