package org.tkit.onecx.human.task.domain.daos;

import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Predicate;

import org.tkit.onecx.human.task.domain.criteria.TaskSearchCriteria;
import org.tkit.onecx.human.task.domain.models.Task;
import org.tkit.onecx.human.task.domain.models.Task_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
public class TaskDAO extends AbstractDAO<Task> {

    @Override
    public Task findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Task.class);
            var root = cq.from(Task.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public PageResult<Task> findTasksByCriteria(TaskSearchCriteria criteria) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Task.class);
            var root = cq.from(Task.class);

            List<Predicate> predicates = new ArrayList<>();
            addSearchStringPredicate(predicates, cb, root.get(Task_.TITLE), criteria.getTitle());
            addSearchStringPredicate(predicates, cb, root.get(Task_.PROVIDER_TASK_ID), criteria.getProviderTaskId());

            if (criteria.getStatuses() != null) {
                predicates.add(root.get(Task_.STATUS).in(criteria.getStatuses()));
            }

            addSearchStringPredicate(predicates, cb, root.get(Task_.PROVIDER_TYPE), criteria.getProviderType());

            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));
            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_TASK_BY_CRITERIA, ex);
        }
    }

    public enum ErrorKeys {
        FIND_ENTITY_BY_ID_FAILED,
        ERROR_FIND_TASK_BY_CRITERIA
    }
}
