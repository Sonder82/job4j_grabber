package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class HabrCareerParse implements Parse {

    private final DateTimeParser dateTimeParser;

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    public static final int PAGE_COUNT = 5;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    /**
     * Метод загружает список всех постов
     * @param link ссылка
     * @return список всех постов
     */
    @Override
    public List<Post> list(String link) {
        List<Post> postList = new ArrayList<>();
        for (int page = 1; page <= PAGE_COUNT; page++) {
            try {
                Connection connection = Jsoup.connect(link + page);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    try {
                        postList.add(parsePost(row));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
        }
        return postList;
    }

    /**
     * Метод принимает на вход Element страницы для дальнейшего парсинга.
     * @param element element страницы
     * @return объект класса Post
     * @throws IOException исключение
     */
    private Post parsePost(Element element) throws IOException {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element dateTimeElement = element.select(".vacancy-card__date").first();
        Element dateElement = dateTimeElement.child(0);
        String date = dateElement.attr("datetime");
        String vacancyName = titleElement.text();
        String linkPage = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        return new Post(vacancyName, linkPage, retriveDescription(linkPage), dateTimeParser.parse(date));
    }

    /**
     * Метод принимает на вход ссылку описания вакансии
     * @param link ссылка описания вакансии
     * @return строку с описанием вакансии
     * @throws IOException исключение
     */
    private static String retriveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        return document.select(".style-ugc").text();
    }

    public static void main(String[] args) {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        Parse parse = new HabrCareerParse(dateTimeParser);
            System.out.println(parse.list(PAGE_LINK));
        }
}
