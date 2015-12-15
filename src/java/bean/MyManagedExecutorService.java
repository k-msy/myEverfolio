/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.util.concurrent.Future;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;

/**
 *
 * @author bpg0129
 */
@Stateless
public class MyManagedExecutorService {
    @Resource(name="concurrent/DefaultManagedExecutorService")
    ManagedExecutorService managedExecsvc;
    
    public void execExecutorService(){
        MyRunnableTask task = new MyRunnableTask();
        managedExecsvc.submit(task);
        MyCallableTask singleTask = new MyCallableTask("Foo Bar");
        Future<String> singleFuture = managedExecsvc.submit(singleTask);
    }
}
