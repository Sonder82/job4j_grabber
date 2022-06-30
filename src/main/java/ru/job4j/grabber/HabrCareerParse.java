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
    public List<Post> list(String link) throws IOException {
        List<Post> postList = new ArrayList<>();
        Connection connection = Jsoup.connect(PAGE_LINK);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__inner").first();
            Element linkElement = titleElement.child(0);
            Element dateTimeElement = row.select(".vacancy-card__date").first();
            Element dateElement = dateTimeElement.child(0);
            String linkPage = String.format("%s%s", link, linkElement.attr("href"));
            String date = String.format("%s", dateElement.attr("datetime"));
            String vacancyName = titleElement.text();
            try {
                postList.add(new Post(vacancyName, linkPage, HabrCareerParse.retriveDescription(link),
                        dateTimeParser.parse(date)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return postList;
    }

    private static String retriveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        return document.select(".job_show_description__body").text();
    }

    public static void main(String[] args) throws IOException {
        String url = PAGE_LINK;
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        Parse parse = new HabrCareerParse(dateTimeParser);
        for (int page = 1; page < 6; page++) {
            String linkPage = url + page;
            System.out.println(parse.list(linkPage));
        }

    }
}
