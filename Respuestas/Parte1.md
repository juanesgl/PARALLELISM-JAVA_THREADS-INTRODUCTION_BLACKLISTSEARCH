## Parte 1 - Contador de threads

---

##### Clase CountThread 

```java 
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
```

##### Clase CountThreadsMain 

```java 
public class CountThreadsMain {

    public static void main(String a[]){
        CountThread t1 = new CountThread(0,99);
        CountThread t2 = new CountThread(99,199);
        CountThread t3 = new CountThread(200,299);

        t1.start();
        t2.start();
        t3.start();
        /*
        t1.run();
        t2.run();
        t3.run();
        */
    }
} 
```
---

### Salida con ```start()```
![Start1](../img/parte1/start1.png)

![Start2](../img/parte1/start2.png)

### Salida con ```run()```
![Run1](../img/parte1/run1.png)

![Run2](../img/parte1/run2.png)

### ¿Por qué cambia la salida? 

- En términos generales, lo que sucede es que al ejecutar ```start()``` se invocan hilos independientes, 
la salida se entrelaza, ya que el scheduler distribuye el tiempo de CPU de cada hilo. Mientras que al ejecutar ```run()``` 
lo que hace es correr el loop dentro del hilo principal, lo que hace que se imprima de manera secuencial (t1,t2,t3).

