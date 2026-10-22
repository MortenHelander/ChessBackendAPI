package app.testutils;

import app.clients.LichessClient;
import app.dtos.lichess.LichessDailyPuzzleDTO;
import app.exceptions.ApiException;
import lombok.Setter;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeLichessClient extends LichessClient {
        private final AtomicInteger calls = new AtomicInteger();
        @Setter
        private LichessDailyPuzzleDTO puzzleToReturn;
        @Setter
        private ApiException exceptionToThrow;

        public FakeLichessClient(LichessDailyPuzzleDTO puzzleToReturn){
            this.puzzleToReturn = puzzleToReturn;
        }

        public Integer getCalls(){
            return calls.get();
        }

        public void reset(){
            this.calls.set(0);
            this.exceptionToThrow = null;
        }

        @Override
        public LichessDailyPuzzleDTO getDailyPuzzle(){
            calls.incrementAndGet();
            if (exceptionToThrow != null){
                throw exceptionToThrow;
            }
            return puzzleToReturn;
        }
}
