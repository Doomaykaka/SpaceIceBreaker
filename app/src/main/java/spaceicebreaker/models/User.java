package spaceicebreaker.models;

import java.util.Date;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "users")
public class User {
    @Id
    @TableGenerator(
            name = "user_gen",
            table = "user_gen",
            pkColumnName = "gen_name",
            valueColumnName = "gen_val",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "user_gen")
    private Long id;

    @NotNull @Column(nullable = false)
    private String name;

    @NotNull @Column(nullable = false)
    private Date creationDate;

    @NotNull @Column(nullable = false)
    private long level;

    @NotNull @Column(nullable = false)
    private long experience;

    @NotNull @Column(nullable = false)
    private long experienceToNextLevel;

    @NotNull @OneToOne
    private Statistic statistic;

    public User() {}

    public User(
            String name,
            Date creationDate,
            long level,
            long experience,
            long experienceToNextLevel,
            Statistic statistic) {
        final Long DEFAULT_ID = null;

        this.id = DEFAULT_ID;
        this.name = name;
        this.creationDate = creationDate;
        this.level = level;
        this.experience = experience;
        this.experienceToNextLevel = experienceToNextLevel;
        this.statistic = statistic;
    }

    public long getExperience() {
        return experience;
    }

    public void setExperience(long experience) {
        this.experience = experience;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }

    public long getExperienceToNextLevel() {
        return experienceToNextLevel;
    }

    public void setExperienceToNextLevel(long experienceToNextLevel) {
        this.experienceToNextLevel = experienceToNextLevel;
    }

    public Statistic getStatistic() {
        return statistic;
    }

    public void setStatistic(Statistic statistic) {
        this.statistic = statistic;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return level == user.level
                && experience == user.experience
                && experienceToNextLevel == user.experienceToNextLevel
                && Objects.equals(id, user.id)
                && Objects.equals(name, user.name)
                && Objects.equals(creationDate, user.creationDate)
                && Objects.equals(statistic, user.statistic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, creationDate, level, experience, experienceToNextLevel, statistic);
    }
}
