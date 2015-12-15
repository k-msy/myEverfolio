/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.util.concurrent.Callable;

/**
 *
 * @author bpg0129
 */
public class MyCallableTask implements Callable<String> {

    MyCallableTask(String foo_Bar) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String call() throws Exception {
        return "Hello World";
    }
    
}
