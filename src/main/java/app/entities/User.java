package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL)
    private UserStats userStats;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Player> players = new HashSet<>();

    public User(String firstName, String lastName, String email, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public void addUserStats(UserStats userStats){
        this.userStats = userStats;
        if (userStats != null){
            userStats.setUser(this);
        }
    }

    public void addPlayer(Player player){
        this.players.add(player);
        if (player != null){
            player.setUser(this);
        }
    }
}
