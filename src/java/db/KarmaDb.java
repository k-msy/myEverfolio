package db;

import entity.Todoist_karma;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;

@Stateless
public class KarmaDb
{
  @PersistenceContext(unitName="myEverfolioPU")
  private EntityManager em;
  
  public Todoist_karma findObj(String id)
  {
    return (Todoist_karma)this.em.find(Todoist_karma.class, id);
  }
  
  public String diffData(HttpSession session, String json)
  {
    Todoist_karma karma = (Todoist_karma)this.em.find(Todoist_karma.class, session.getAttribute("user_id"));
    String beforeKarmaJson = karma.getKarma();
    
    return "";
  }
  
  public void mergeKarma(String userId, String json)
  {
    Todoist_karma karma = (Todoist_karma)this.em.find(Todoist_karma.class, userId);
    karma.setKarma(json);
    this.em.merge(karma);
  }
}
