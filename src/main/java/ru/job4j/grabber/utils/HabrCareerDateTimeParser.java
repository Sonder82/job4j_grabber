package ru.job4j.grabber.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParser implements DateTimeParser {
    @Override
    public ZonedDateTime parse(String parse) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm z");
        ZonedDateTime dateTime = ZonedDateTime.parse(parse, formatter);
        return dateTime;
    }
}
