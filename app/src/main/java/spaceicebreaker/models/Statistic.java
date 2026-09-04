package spaceicebreaker.models;

import java.util.Date;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "statistics")
public class Statistic {
    @Id
    @TableGenerator(
            name = "statistic_gen",
            table = "statistic_gen",
            pkColumnName = "gen_name",
            valueColumnName = "gen_val",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "statistic_gen")
    private Long id;

    @NotNull @Column(nullable = false)
    private Date lastPlayDate;

    @NotNull @Column(nullable = false)
    private Long daysInGame;

    @NotNull @Column(nullable = false)
    private Long scoutBestScore;

    @NotNull @Column(nullable = false)
    private Long tankBestScore;

    @NotNull @Column(nullable = false)
    private Long damageDealerBestScore;

    public Statistic() {}

    public Statistic(
            Date lastPlayDate,
            Long daysInGame,
            Long scoutBestScore,
            Long tankBestScore,
            Long damageDealerBestScore) {
        final Long DEFAULT_ID = null;

        this.id = DEFAULT_ID;
        this.lastPlayDate = lastPlayDate;
        this.daysInGame = daysInGame;
        this.scoutBestScore = scoutBestScore;
        this.tankBestScore = tankBestScore;
        this.damageDealerBestScore = damageDealerBestScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastPlayDate() {
        return lastPlayDate;
    }

    public void setLastPlayDate(Date lastPlayDate) {
        this.lastPlayDate = lastPlayDate;
    }

    public Long getDaysInGame() {
        return daysInGame;
    }

    public void setDaysInGame(Long daysInGame) {
        this.daysInGame = daysInGame;
    }

    public Long getScoutBestScore() {
        return scoutBestScore;
    }

    public void setScoutBestScore(Long scoutBestScore) {
        this.scoutBestScore = scoutBestScore;
    }

    public Long getTankBestScore() {
        return tankBestScore;
    }

    public void setTankBestScore(Long tankBestScore) {
        this.tankBestScore = tankBestScore;
    }

    public Long getDamageDealerBestScore() {
        return damageDealerBestScore;
    }

    public void setDamageDealerBestScore(Long damageDealerBestScore) {
        this.damageDealerBestScore = damageDealerBestScore;
    }

    @Override
    public int hashCode() {
        return Objects.hash(damageDealerBestScore, daysInGame, id, lastPlayDate, scoutBestScore, tankBestScore);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Statistic other = (Statistic) obj;
        return Objects.equals(damageDealerBestScore, other.damageDealerBestScore)
                && Objects.equals(daysInGame, other.daysInGame) && Objects.equals(id, other.id)
                && Objects.equals(lastPlayDate, other.lastPlayDate)
                && Objects.equals(scoutBestScore, other.scoutBestScore)
                && Objects.equals(tankBestScore, other.tankBestScore);
    }
}
