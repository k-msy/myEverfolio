/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author bpg0129
 */
@Entity
@Table(name="FOLIO_GROUP")
public class UserGroup implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    private String userId;
    private String grpRole;
    
    public UserGroup(){
        
    }
    
    public UserGroup(String userId, String grpRole){
        this.userId = userId;
        this.grpRole = grpRole;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGrpRole() {
        return grpRole;
    }

    public void setGrpRole(String grpRole) {
        this.grpRole = grpRole;
    }
    


    
    
}
