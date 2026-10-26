package app.schedulers;

import app.dtos.puzzles.DailyPuzzleResponseDTO;
import app.exceptions.ApiException;
import app.services.DailyPuzzleService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DailyPuzzleSchedulerTest {

    Logger logger;
    ListAppender<ILoggingEvent> logs;

    DailyPuzzleScheduler schedulerAt(String isoInstant) {
        //iso instant represents a single point of time in utc using the: ISO-8601 formatted like '2011-12-03T10:15:30Z' from Oracle documentation
        //the schedulers is set to get the new puzzle 5 minutes after midnight UTC time
        Clock clock = Clock.fixed(Instant.parse(isoInstant), ZoneOffset.UTC);
        return new DailyPuzzleScheduler(null, clock);
    }

    @BeforeEach
    void beforeEach(){
        logger = (Logger) LoggerFactory.getLogger(DailyPuzzleScheduler.class);
        logs = new ListAppender<>();
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void afterEach(){
        logger.detachAppender(logs);
    }

    @Test
    void secondsUntilNextRun_lateEvening_isUntilFivePastMidnight(){
        long seconds = schedulerAt("2026-10-22T23:00:00Z").secondsUntilNextRun(); //23:00:00 1 hour before midnight
        assertThat(seconds, is(3900L)); //65 minutes
    }

    @Test
    void prefetchPuzzle_serviceThrows_logsErrorAndDoesNotThrow(){
        DailyPuzzleService failingService = new DailyPuzzleService(Clock.systemUTC(), null, null, null, null) {
            //overrid the current method for test purposes, simulating a failed api call
            @Override
            public DailyPuzzleResponseDTO getTodaysPuzzle() {
                throw new ApiException(502, "Could not reach Lichess");
            }
        };

        DailyPuzzleScheduler scheduler = new DailyPuzzleScheduler(failingService, Clock.systemUTC());

        assertDoesNotThrow(() -> scheduler.prefetchPuzzle());

        ILoggingEvent event = logs.list.getFirst();
        assertThat(event.getMessage(), is("Daily puzzle prefetch failed"));
        assertThat(event.getLevel(), is(Level.ERROR));
    }

    @Test
    void prefetchPuzzle_serviceWorks_callsServiceOnce_andLogsInfo(){
        AtomicInteger calls = new AtomicInteger();
        DailyPuzzleService countingService = new DailyPuzzleService(Clock.systemUTC(), null, null, null, null) {
            //overrides the real method for test purposes, simulating a successful fetch
            @Override
            public DailyPuzzleResponseDTO getTodaysPuzzle() {
                calls.incrementAndGet();
                return null;
            }
        };
        DailyPuzzleScheduler scheduler = new DailyPuzzleScheduler(countingService, Clock.systemUTC());

        assertDoesNotThrow(() -> scheduler.prefetchPuzzle());

        assertThat(calls.get(), is(1));

        ILoggingEvent event = logs.list.getFirst();
        assertThat(event.getMessage(), is("Daily puzzle is ready"));
        assertThat(event.getLevel(), is(Level.INFO));
    }
}
