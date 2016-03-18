/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

/**
 *
 * @author bpg0129
 */
// こいつは、非同期に実行する具体的なアクションを記述するクラス、、だと思う
public class AsyncTask implements Runnable {

    @Override
    public void run() {
        System.out.println("First Async Task is start.");
        try {
            System.out.println("First Async Task is waiting.");
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.println("First Async Task is end.");
    }

}
