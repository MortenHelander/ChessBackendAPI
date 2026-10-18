package app.websocket;

import app.dtos.games.GameEventDTO;
import io.javalin.websocket.WsContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GameConnectionRegistry {
    private final Map<Integer, Set<WsContext>> connections = new ConcurrentHashMap<>();

    public void join(Integer gameId, WsContext ctx){
        connections.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(ctx);
    }

    public void leave(Integer gameId, WsContext ctx){
        connections.computeIfPresent(gameId, (k, set) -> {
            set.remove(ctx);
            if (set.isEmpty()){
                return null;
            }else{
                return set;
            }
        });
    }

    public void broadcast(Integer gameId, GameEventDTO message){
        for (WsContext wsContext : connections.getOrDefault(gameId, Set.of())) {
            if (wsContext.session.isOpen()){
                try {
                    wsContext.send(message);
                }catch (Exception e){
                    log.error("Could not send to a client in game {}", gameId, e);
                }
            }
        }
    }
}
