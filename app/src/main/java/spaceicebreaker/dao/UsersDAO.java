package spaceicebreaker.dao;

import spaceicebreaker.dao.utils.EntityDAO;
import spaceicebreaker.models.User;
import spaceicebreaker.utils.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class UsersDAO extends EntityDAO<User> {
    public UsersDAO(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    @Override
    protected CriteriaQuery<User> getSelectQuery(CriteriaBuilder builder, Long id) {
        CriteriaQuery<User> preparedQuery = null;

        Logger.getInstance().info("Get user");

        preparedQuery = builder.createQuery(User.class);
        Root<User> root = preparedQuery.from(User.class);
        preparedQuery = preparedQuery.select(root);
        preparedQuery = preparedQuery.where(builder.equal(root.get("id"), id));

        return preparedQuery;
    }

    @Override
    protected CriteriaQuery<User> getSelectAllQuery(CriteriaBuilder builder) {
        CriteriaQuery<User> preparedQuery = null;

        Logger.getInstance().info("Get all users");

        preparedQuery = builder.createQuery(User.class);
        Root<User> root = preparedQuery.from(User.class);
        preparedQuery = preparedQuery.select(root);

        return preparedQuery;
    }

    @Override
    protected User searchEntity(User entity, EntityManager manager) {
        Logger.getInstance().info("Search user");

        return entity == null || entity.getId() == null ? null : manager.find(User.class, entity.getId());
    }
}
