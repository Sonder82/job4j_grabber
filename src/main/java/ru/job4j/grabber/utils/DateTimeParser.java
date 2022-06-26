package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public interface DateTimeParser {
    LocalDateTime parse(String parse);
}
