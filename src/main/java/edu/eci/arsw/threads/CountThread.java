package edu.eci.arsw.threads;

import java.util.stream.IntStream;

public class CountThread extends Thread {
    private final int a;
    private final int b;
    public CountThread(int a, int b){
        this.a = a;
        this.b = b;
    }
    @Override
    public void run() {
        IntStream.range(a,b)
                .forEach(x-> System.out.println(Thread.currentThread().getName() + " - Count: " + x));
    }
}
