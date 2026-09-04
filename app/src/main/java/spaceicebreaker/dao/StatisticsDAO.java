package spaceicebreaker.dao;

import spaceicebreaker.dao.utils.EntityDAO;
import spaceicebreaker.models.Statistic;
import spaceicebreaker.utils.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class StatisticsDAO extends EntityDAO<Statistic> {
    public StatisticsDAO(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    @Override
    protected CriteriaQuery<Statistic> getSelectQuery(CriteriaBuilder builder, Long id) {
        CriteriaQuery<Statistic> preparedQuery = null;

        Logger.getInstance().info("Get statistic");

        preparedQuery = builder.createQuery(Statistic.class);
        Root<Statistic> root = preparedQuery.from(Statistic.class);
        preparedQuery = preparedQuery.select(root);
        preparedQuery = preparedQuery.where(builder.equal(root.get("id"), id));

        return preparedQuery;
    }

    @Override
    protected CriteriaQuery<Statistic> getSelectAllQuery(CriteriaBuilder builder) {
        CriteriaQuery<Statistic> preparedQuery = null;

        Logger.getInstance().info("Get all statistics");

        preparedQuery = builder.createQuery(Statistic.class);
        Root<Statistic> root = preparedQuery.from(Statistic.class);
        preparedQuery = preparedQuery.select(root);

        return preparedQuery;
    }

    @Override
    protected Statistic searchEntity(Statistic entity, EntityManager manager) {
        Logger.getInstance().info("Search statistic");

        return entity == null || entity.getId() == null ? null : manager.find(Statistic.class, entity.getId());
    }
}
