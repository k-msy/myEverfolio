/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import oauth.Twitter;

/**
 *
 * @author bpg0129
 */
@Stateless
public class AsyncExecute {

    @Resource(name="concurrent/DefaultManagedExecutorService")
    private ManagedExecutorService executeService;

    public void start() {
        //AsyncTask task = new AsyncTask();
        System.out.println("Before execute.");
        executeService.execute(new Twitter());
        System.out.println("After execute.");
    }
}
