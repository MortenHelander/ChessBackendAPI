package app.mappers;

import app.dtos.users.UserStatsDTO;
import app.entities.UserStats;

public class UserStatsMapper {

    public static UserStats fromDTO(UserStatsDTO userStatsDTO){
        return new UserStats(userStatsDTO.id(), userStatsDTO.gamesPlayed(), userStatsDTO.wins(), userStatsDTO.losses(), userStatsDTO.draws(), userStatsDTO.mmr());
    }

    public static UserStatsDTO fromEntity(UserStats userStats){
        return new UserStatsDTO(userStats.getId(), userStats.getGamesPlayed(), userStats.getWins(), userStats.getLosses(), userStats.getDraws(), userStats.getMmr());
    }
}
