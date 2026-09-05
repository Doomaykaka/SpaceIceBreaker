package spaceicebreaker.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import spaceicebreaker.dao.StatisticsDAO;
import spaceicebreaker.dao.UsersDAO;
import spaceicebreaker.models.Statistic;
import spaceicebreaker.models.User;
import spaceicebreaker.utils.Constants;
import spaceicebreaker.utils.Logger;
import spaceicebreaker.utils.SupportFunctions;

public class GameOperationsController {
    private UsersDAO usersDAO;
    private StatisticsDAO statisticsDAO;

    public GameOperationsController(UsersDAO usersDAO, StatisticsDAO statisticsDAO) {
        this.usersDAO = usersDAO;
        this.statisticsDAO = statisticsDAO;
    }

    public List<User> getAllUsers() {
        List<User> allUsers = null;

        Logger.getInstance().info("Get all users");

        allUsers = this.usersDAO.getAll();

        return allUsers;
    }

    public void saveUser(User user) {
        Logger.getInstance().info("Save user");

        this.statisticsDAO.create(user.getStatistic());
        this.usersDAO.create(user);
    }

    public void updateUser(User user) {
        Logger.getInstance().info("Update user");

        this.statisticsDAO.update(user.getStatistic());
        this.usersDAO.update(user);
    }

    public User createNewUser(String name) {
        User newUser = SupportFunctions.createEmptyUser(name);

        Logger.getInstance().info("Create user");

        this.statisticsDAO.create(newUser.getStatistic());
        this.usersDAO.create(newUser);

        return newUser;
    }

    public boolean removeUser(Long id) {
        User userToRemove = this.usersDAO.get(id);

        Logger.getInstance().info("Remove user");

        return this.usersDAO.remove(userToRemove);
    }

    public void exportUser(User user, File fileToSave) {
        JSONObject userObj = new JSONObject();

        Logger.getInstance().info("Export user");

        JSONParser parser = new JSONParser();

        try {
            Long id = user.getId();
            String name = user.getName();
            Date creationDate = user.getCreationDate();
            Long level = user.getLevel();
            Long experience = user.getExperience();
            Long experienceToNextLevel = user.getExperienceToNextLevel();
            Statistic statistic = user.getStatistic();

            userObj.put("id", id);
            userObj.put("name", name);
            userObj.put("creationDate", creationDate.toInstant().getEpochSecond());
            userObj.put("level", level);
            userObj.put("experience", experience);
            userObj.put("experienceToNextLevel", experienceToNextLevel);

            String statisticJSON = statisticToJSON(statistic);
            JSONObject statisticObj = (JSONObject) parser.parse(statisticJSON);
            userObj.put("statistic", statisticObj);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        StringWriter stringWriter = new StringWriter();

        try {
            userObj.writeJSONString(stringWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SupportFunctions.writeContentInNewFile(
                fileToSave.getParentFile(), fileToSave.getName(), List.of(stringWriter.toString()));
    }

    private String statisticToJSON(Statistic statistic) {
        JSONObject statisticObj = new JSONObject();

        Logger.getInstance().info("Export statistic");

        Long id = statistic.getId();
        Date lastPlayDate = statistic.getLastPlayDate();
        Long daysInGame = statistic.getDaysInGame();
        Long scoutBestScore = statistic.getScoutBestScore();
        Long tankBestScore = statistic.getTankBestScore();
        Long damageDealerBestScore = statistic.getDamageDealerBestScore();

        statisticObj.put("id", id);
        statisticObj.put("lastPlayDate", lastPlayDate.toInstant().getEpochSecond());
        statisticObj.put("daysInGame", daysInGame);
        statisticObj.put("scoutBestScore", scoutBestScore);
        statisticObj.put("tankBestScore", tankBestScore);
        statisticObj.put("damageDealerBestScore", damageDealerBestScore);

        StringWriter stringWriter = new StringWriter();

        try {
            statisticObj.writeJSONString(stringWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringWriter.toString();
    }

    public User importUser(File fileToLoad) {
        User importedUser = null;

        Logger.getInstance().info("Import user");

        List<String> fileContent = List.of();

        try {
            fileContent = SupportFunctions.readFileContent(new FileReader(fileToLoad));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fileContent.isEmpty()) {
            return importedUser;
        }

        String userJSON = fileContent.getFirst();

        JSONParser parser = new JSONParser();

        try {
            JSONObject userObj = (JSONObject) parser.parse(userJSON);

            String name = (String) userObj.get("name");
            Date creationDate = new Date((Long) userObj.get("creationDate") * Constants.SECONDS_TO_MILLIS_MULTIPLIER);
            Long level = (Long) userObj.get("level");
            Long experience = (Long) userObj.get("experience");
            Long experienceToNextLevel = (Long) userObj.get("experienceToNextLevel");
            Statistic statistic = statisticFromJSON((JSONObject) userObj.get("statistic"));

            importedUser = new User(name, creationDate, level, experience, experienceToNextLevel, statistic);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return importedUser;
    }

    private Statistic statisticFromJSON(JSONObject statisticObj) {
        Statistic statistic = null;

        Logger.getInstance().info("Import statistic");

        Date lastPlayDate = new Date((Long) statisticObj.get("lastPlayDate") * Constants.SECONDS_TO_MILLIS_MULTIPLIER);
        Long daysInGame = (Long) statisticObj.get("daysInGame");
        Long scoutBestScore = (Long) statisticObj.get("scoutBestScore");
        Long tankBestScore = (Long) statisticObj.get("tankBestScore");
        Long damageDealerBestScore = (Long) statisticObj.get("damageDealerBestScore");

        statistic = new Statistic(lastPlayDate, daysInGame, scoutBestScore, tankBestScore, damageDealerBestScore);

        return statistic;
    }
}
