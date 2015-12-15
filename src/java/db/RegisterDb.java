/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import entity.Users;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author bpg0129
 */
@Stateless
public class RegisterDb {
    @PersistenceContext
    private EntityManager em;
    
    public void create(Users users){
        em.persist(users);
    }
    
}
