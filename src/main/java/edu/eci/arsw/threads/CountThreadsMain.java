package edu.eci.arsw.threads;

import java.util.Scanner;

/**
 *
 * @author juanesgl
 * @author Valero25
 */

public class CountThreadsMain {
    
    public static void main(String args[]){
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Ingrese el numero de hilos a crear:");
        int numThreads = scanner.nextInt();
        
        CountThread[] threads = new CountThread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            System.out.println("Hilo " + (i + 1) + " - Ingrese el valor inicial (a):");
            int a = scanner.nextInt();
            System.out.println("Hilo " + (i + 1) + " - Ingrese el valor final (b):");
            int b = scanner.nextInt();
            
            threads[i] = new CountThread(a, b);
        }
        
        System.out.println("\n=== MENU DE EJECUCION ===");
        System.out.println("1. Ejecutar con start() (hilos en paralelo)");
        System.out.println("2. Ejecutar con run() (secuencial)");
        System.out.print("Seleccione una opcion: ");
        int opcion = scanner.nextInt();
        
        if (opcion == 1) {
            System.out.println("\nEjecutando hilos con start()...\n");
            for (CountThread thread : threads) {
                thread.start();
            }
        } else if (opcion == 2) {
            System.out.println("\nEjecutando hilos con run()...\n");
            for (CountThread thread : threads) {
                thread.run();
            }
        } else {
            System.out.println("\nOpcion invalida. Saliendo...");
        }
        
        scanner.close();
    }

}
