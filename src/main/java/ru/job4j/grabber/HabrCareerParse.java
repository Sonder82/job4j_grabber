package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {

    private  static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private static String retriveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        return document.select(".job_show_description__body").text();
    }

    public static void main(String[] args) throws IOException {
        for (int page = 1; page < 2; page++) {
            Connection connection = Jsoup.connect(PAGE_LINK + page);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateTimeElement = row.select(".vacancy-card__date").first();
                Element dateElement = dateTimeElement.child(0);
                String date = String.format("%s", dateElement.attr("datetime"));
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", date, vacancyName, link);
            });
        }
    }
}
