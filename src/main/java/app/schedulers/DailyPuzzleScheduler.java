package app.schedulers;

import app.services.DailyPuzzleService;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DailyPuzzleScheduler {
    private final DailyPuzzleService dailyPuzzleService;
    private final Clock clock;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public DailyPuzzleScheduler(DailyPuzzleService dailyPuzzleService, Clock clock) {
        this.dailyPuzzleService = dailyPuzzleService;
        this.clock = clock;
    }

    public void start(){
        scheduler.execute(this::prefetchPuzzle);
        scheduler.scheduleAtFixedRate(this::prefetchPuzzle, secondsUntilNextRun(), TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    public void stop(){
        scheduler.shutdown();
    }

    void prefetchPuzzle(){
        try{
            dailyPuzzleService.getTodaysPuzzle();
            log.info("Daily puzzle is ready");
        }catch (Exception e){
            log.error("Daily puzzle prefetch failed", e); //logs error, but doesn't throw so schedulers doesn't stop
        }
    }

    long secondsUntilNextRun(){ //no public or private, package private
        ZonedDateTime now = ZonedDateTime.now(clock);
        ZonedDateTime nextRun = now.toLocalDate().atTime(0, 5).atZone(ZoneOffset.UTC);
        if (!nextRun.isAfter(now)){
            nextRun = nextRun.plusDays(1);
        }
        return Duration.between(now, nextRun).getSeconds();
    }
}
