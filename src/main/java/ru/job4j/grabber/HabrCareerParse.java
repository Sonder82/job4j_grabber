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

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        final int PAGE_COUNT = 5;
        List<Post> postList = new ArrayList<>();
        for (int page = 1; page <= PAGE_COUNT; page++) {
            try {
                Connection connection = Jsoup.connect(PAGE_LINK + page);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__inner").first();
                    Element linkElement = titleElement.child(0);
                    Element dateTimeElement = row.select(".vacancy-card__date").first();
                    Element dateElement = dateTimeElement.child(0);

                    try {
                        postList.add(parsePost(titleElement, linkElement, link, dateElement));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return postList;
    }

    private Post parsePost(
            Element titleElement, Element linkElement, String linkDescription, Element dateElement)
            throws IOException {
        String vacancyName = titleElement.text();
        String linkPage = String.format("%s%s", PAGE_LINK, linkElement.attr("href"));
        String date = String.format("%s", dateElement.attr("datetime"));
        return new Post(vacancyName, linkPage, retriveDescription(linkDescription), dateTimeParser.parse(date));
    }

    private static String retriveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        return document.select(".job_show_description__body").text();
    }

    public static void main(String[] args) {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        Parse parse = new HabrCareerParse(dateTimeParser);
            System.out.println(parse.list(PAGE_LINK));
        }
}
