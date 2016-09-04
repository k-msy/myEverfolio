package db;

import entity.Todoist_karma;
import entity.Token_todoist;
import entity.Token_withings;
import entity.Token_zaim;
import entity.Users;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class RegisterDb {

    @PersistenceContext(unitName = "myEverfolioPU")
    private EntityManager em;

    public void create(Users users, Token_withings wi, Token_zaim zaim, Token_todoist todoist, Todoist_karma karma) {
        em.persist(users);
        em.persist(wi);
        em.persist(zaim);
        em.persist(todoist);
        em.persist(karma);
    }
}
