package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    private static Properties properties = new Properties();

    public static void main(String[] args) {
        readProperties();
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(properties.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            System.out.println("Rabbit runs here...");
        }
    }

    public static void readProperties() {
        try (InputStream inputStream = AlertRabbit.class.getClassLoader().getResourceAsStream(
                "rabbit.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
