package listenable;

import com.github.wang007.listenable.executor.ListenableExecutor;
import com.github.wang007.listenable.executor.ListenableExecutorService;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * created by wang007 on 2019/11/30
 */
public class Main {

    public static void main(String[] args) {

        Executor executor0 = new Executor() {

            private AtomicInteger num = new AtomicInteger();

            @Override
            public void execute(Runnable command) {
                new Thread(command, "thread0-num-" + num.getAndIncrement()).start();
            }
        };

        Executor executor1 = new Executor() {

            private AtomicInteger num = new AtomicInteger();

            @Override
            public void execute(Runnable command) {
                new Thread(command, "thread1-num-" + num.getAndIncrement()).start();
            }
        };

        ListenableExecutor executor = ListenableExecutor.create(executor0);

        executor.submit(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("thread name -> " + Thread.currentThread().getName());
            System.out.println("1. submit");
            System.out.println();

        }).flatMap(v -> {
            System.out.println("thread name -> " + Thread.currentThread().getName());
            System.out.println("2. flatMap");
            System.out.println();
            return "result2";

        }, executor1).flatMap(str -> {

            System.out.println("thread name -> " + Thread.currentThread().getName());
            System.out.println("3. flatMap result -> " +  str);
            System.out.println();
            return "result3";

        }).map(str -> {
            System.out.println("thread name -> " + Thread.currentThread().getName());
            System.out.println("4. map result -> " +  str);
            System.out.println();
            return "result4";

        }).addOnSucceeded(str -> {
            System.out.println("thread name -> " + Thread.currentThread().getName());
            System.out.println("finally -> " + str);
        });


    }

}
