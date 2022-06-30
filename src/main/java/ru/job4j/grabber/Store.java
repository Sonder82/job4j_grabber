package ru.job4j.grabber;

import java.util.List;

/**
 * Метод save() - сохраняет объявление в базе.
 * Метод getAll() - позволяет извлечь объявления из базы.
 * Метод findById(int id) - позволяет извлечь объявление из базы по id.
 */
public interface Store {
    void save(Post post);

    List<Post> getAll();

    Post findById(int id);
}
