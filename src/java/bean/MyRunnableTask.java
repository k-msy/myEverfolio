/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import static com.sun.xml.ws.security.addressing.impl.policy.Constants.logger;
import java.util.logging.Level;

/**
 *
 * @author bpg0129
 */
public class MyRunnableTask implements Runnable {

    @Override
    public void run() {
        try{
            Thread.sleep(10000);
        }catch(InterruptedException ex){
            logger.log(Level.SEVERE, null, ex);
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
