package app.services;

import app.clients.LichessClient;
import app.daos.DailyPuzzleDAO;
import app.dtos.lichess.LichessDailyPuzzleDTO;
import app.dtos.puzzles.DailyPuzzleResponseDTO;
import app.entities.DailyPuzzle;
import app.exceptions.ApiException;
import app.exceptions.DuplicatePuzzleException;
import app.gameengine.gameservices.FenParser;
import app.mappers.DailyPuzzleMapper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DailyPuzzleService {
    private final Clock clock;
    private final DailyPuzzleDAO dailyPuzzleDAO;
    private final LichessClient lichessClient;

    public DailyPuzzleService(Clock clock, DailyPuzzleDAO dailyPuzzleDAO, LichessClient lichessClient){
        this.clock = clock;
        this.dailyPuzzleDAO = dailyPuzzleDAO;
        this.lichessClient = lichessClient;
    }

    public DailyPuzzleResponseDTO getTodaysPuzzle(){
        DailyPuzzle dailyPuzzle = getOrFetchTodaysPuzzle();
        List<String> solution = dailyPuzzle.getSolution();
        int movesToSolve = (solution.size() + 1)/2;
        boolean isWhitesTurn = FenParser.fromFen(dailyPuzzle.getFen()).isWhitesTurn();

        return DailyPuzzleMapper.toDto(dailyPuzzle, isWhitesTurn, movesToSolve);
    }

    private DailyPuzzle getOrFetchTodaysPuzzle(){
        LocalDate today = LocalDate.now(clock);
        Optional<DailyPuzzle> dailyPuzzle = dailyPuzzleDAO.getByDate(today);
        return dailyPuzzle.orElseGet(() -> fetchAndSave(today));
    }

    private DailyPuzzle fetchAndSave(LocalDate today){
        LichessDailyPuzzleDTO dto = lichessClient.getDailyPuzzle();
        if (dto == null || dto.puzzle() == null) {
            throw new ApiException(502, "Lichess returned no puzzle");
        }
        if (dto.puzzle().solution() == null || dto.puzzle().solution().isEmpty()) {
            throw new ApiException(502, "Lichess returned a puzzle without a solution");
        }

        // validate the FEN before saving, so a broken puzzle never reaches the database
        try {
            FenParser.fromFen(dto.puzzle().fen());
        } catch (ApiException e) {
            throw new ApiException(502, "Lichess returned an invalid puzzle FEN: " + e.getMessage());
        }

        DailyPuzzle entity = DailyPuzzleMapper.toEntity(dto, today);

        try{
            return dailyPuzzleDAO.create(entity);
        }catch (DuplicatePuzzleException e){
            return dailyPuzzleDAO.getByDate(today).orElseThrow(() -> new ApiException(500, "Puzzle for " + today + " was a duplicate but could not be read"));
        }
    }
}
