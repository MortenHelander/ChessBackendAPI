package app.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "chess_com_stats")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
public class ChessComUser {

    @Id
    @Column(name = "user_id")
    private Integer id;
}
