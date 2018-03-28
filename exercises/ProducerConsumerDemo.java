import java.util.concurrent.locks.*;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Thread;

public class Testlock {

    public static ReentrantLock lock = new ReentrantLock();
    public static Condition notFull = lock.newCondition();
    public static Condition notEmpty = lock.newCondition();

    public static int count = 0;
    public static int BUFFER_SIZE = 5;

    private static int threadsKilled = 0;

    public static void main(String[] args) {

        ArrayList<Thread> threadList = new ArrayList<>();
        
        try {

            for (int i = 0; i < 1000; i++) {
                ProducerThread pt = new ProducerThread();
                threadList.add(pt);
                pt.start();

                ConsumerThread ct = new ConsumerThread();
                threadList.add(ct);
                ct.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



        for (Thread t : threadList) {
            try {
                t.join();
                threadsKilled++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nTotal count: " + count);
        System.out.println("Threads completed: " + threadsKilled);
    }
}

class ProducerThread extends Thread {

    public void run() {
        increment();
    }

    public void increment() {

        Random rand = new Random();
        int randInt = rand.nextInt(500);

        Testlock.lock.lock();
        try {
            while (Testlock.count == Testlock.BUFFER_SIZE) Testlock.notFull.await(); // set this thread to wait
            Testlock.count++;
            System.out.println("producer made a count, remaining: " + Testlock.count);
            Testlock.notEmpty.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Testlock.lock.unlock();
        }
    }
}

class ConsumerThread extends Thread {

    public void run() {
        decrement();
    }

    public void decrement() {

        Random rand = new Random();
        int randInt = rand.nextInt(500);

        Testlock.lock.lock();
        try {
            while (Testlock.count <= 0) Testlock.notEmpty.await();
            Testlock.count--;
            System.out.println("consumer ate a count, remaining: " + Testlock.count);
            Testlock.notFull.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Testlock.lock.unlock();
        }
    }
}