package ru.job4j.grabber;

import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private Connection connection;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        try {
            connection = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Метод выполняет вставку объекта Post в SQL таблицу.
     *
     * @param post объект Post
     */
    @Override
    public void save(Post post) {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into post (name, text, link, created) values (?, ?, ?, ?)")) {
            statement.setString(1, post.getName());
            statement.setString(2, post.getText());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод выполняет операцию получения списка всех Post
     *
     * @return список Post
     */
    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("select * from post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(find(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Метод выполняет поиск объекта Post по id.
     *
     * @param id номер id
     * @return объект Post
     */
    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = connection.prepareStatement(
                "select * from post where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    post = find(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    private Post find(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("text"),
                resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime());
    }

    public static void main(String[] args) {
        Properties cfg = new Properties();
        try (InputStream stream = PsqlStore.class.getClassLoader().getResourceAsStream("grabber.properties")) {
            cfg.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        Parse parse = new HabrCareerParse(dateTimeParser);
        List<Post> list = parse.list(PAGE_LINK);
        try (PsqlStore store = new PsqlStore(cfg)) {
            for (Post post : list) {
                store.save(new Post(post.getName(), post.getText(), post.getLink(), post.getCreated()));
            }
            System.out.println(store.findById(628));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
