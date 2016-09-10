package db;

import entity.Todoist_karma;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class KarmaDb {

    @PersistenceContext(unitName = "myEverfolioPU")
    private EntityManager em;

    public Todoist_karma findObj(String id) {
        return (Todoist_karma) em.find(Todoist_karma.class, id);
    }
}
