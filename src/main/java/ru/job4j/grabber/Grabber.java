package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;

public class Grabber implements Grab {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private final Properties cfg = new Properties();

    public Store store() {
        return new PsqlStore(cfg);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void cfg() throws IOException {
        try (InputStream in = Grabber.class.getClassLoader().getResourceAsStream("grabber.properties")) {
            cfg.load(in);
        }
    }

    /**
     * Данный метод инициализирует работу планировщика.
     *
     * 1. Создаем хранилище JobDataMap. В нем мы будем хранить параметры, которые передадим в Job.
     * Эти параметры возьмем при выполнении планировщика в методе execute.
     *
     * 2. Создадим задачу с помощью JobDetail.
     *
     * 3. Добавим в планировщик расписание(интервал) с которым будет выполняться Job.
     * Для этого воспользуемся SimpleScheduleBuilder
     *
     * 4. Триггер. Задача запускается через Trigger.
     * В нашем случае запуск начинается сразу.
     * <p>
     * 5. Загружаем задачу и триггер в планировщик с помощью .scheduleJob
     *
     * @param parse     парсер объявлений
     * @param store     хранилище объявлений
     * @param scheduler планировщик
     * @throws SchedulerException
     */
    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    /**
     * Метод выполняет работу объекта Job
     * <p>
     * 1. Quartz создает объект Job, каждый раз при выполнении работы.
     * <p>
     * 2. Чтобы получить объекты(это в нашем случае) из context используются методы
     * getJobDetail().getJobDataMap();
     * Полученные объекты являются общими для каждой работы.
     * У нас это хранилище и парсер
     */
    public static class GrabJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            List<Post> list = parse.list(PAGE_LINK);
            for (Post post : list) {
                store.save(post);
            }
        }
    }

    /**
     * Данный метод выводит результат
     * парсера на веб-страницу.
     *
     * 1. Создаем сервер.
     * Вызов конструктора ServerSocket создает серверный сокет, привязанный к указанному порту.
     * 9000 порт. По умолчанию адрес localhost
     *
     * 2. Далее создаем клиентский сокет.
     * Вызов метода accept() заставляет программу ждать подключений по указанному порту.
     * Работа программы продолжится только после подключения клиента.
     * После успешного подключения метод возвращает объект Socket,
     * который используется для взаимодействия с клиентом.
     *
     * 3. Во втором блоке try с помощью объекта Socket программа может получить входной поток
     * и может отправить данные в выходной поток.
     *
     * 4. В ответ мы записываем строчку,
     * где указали, что все прочитали:HTTP/1.1 200 OK\r\n\r\n
     *
     * 5. В программе читается весь входной поток через цикл for.
     */

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(cfg.getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store().getAll()) {
                            out.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (IOException io) {
                io.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws IOException, SchedulerException {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new HabrCareerParse(new HabrCareerDateTimeParser()), store, scheduler);
        grab.web(store);
    }
}
