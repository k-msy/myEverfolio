package entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "TODOIST_KARMA")
@XmlRootElement
@NamedQueries({
    @javax.persistence.NamedQuery(name = "Todoist_karma.findAll", query = "SELECT t FROM Todoist_karma t"),
    @javax.persistence.NamedQuery(name = "Todoist_karma.findByUserid", query = "SELECT t FROM Todoist_karma t WHERE t.userid = :userid"),
    @javax.persistence.NamedQuery(name = "Todoist_karma.findByKarma", query = "SELECT t FROM Todoist_karma t WHERE t.karma = :karma")})
public class Todoist_karma
        implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "USERID")
    private String userid;
    @Size(max = 30000)
    @Column(name = "KARMA")
    private String karma;

    public Todoist_karma() {
    }

    public Todoist_karma(String userid, String karma) {
        this.userid = userid;
        this.karma = karma;
    }

    public String getUserid() {
        return this.userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getKarma() {
        return this.karma;
    }

    public void setKarma(String karma) {
        this.karma = karma;
    }

    public int hashCode() {
        int hash = 0;
        hash += (this.userid != null ? this.userid.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Todoist_karma)) {
            return false;
        }
        Todoist_karma other = (Todoist_karma) object;
        if (((this.userid == null) && (other.userid != null)) || ((this.userid != null) && (!this.userid.equals(other.userid)))) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "entity.Todoist_karma[ userid=" + this.userid + " ]";
    }
}
