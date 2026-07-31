package edu.eci.arsw.threads;

import java.util.Scanner;

/**
 *
 * @author juanesgl
 * @author Valero25
 */

public class CountThreadsMain {
    
    private static final int NUM_THREADS = 3;
    
    /**
     * Método auxiliar para leer un entero con validación
     */
    private static int leerEntero(Scanner scanner, String mensaje) {
        Integer numero = null;
        while (numero == null) {
            System.out.print(mensaje);
            try {
                if (scanner.hasNextInt()) {
                    numero = scanner.nextInt();
                } else {
                    String entrada = scanner.next();
                    System.out.println("Error: '" + entrada + "' no es un numero entero valido.");
                    System.out.println("Por favor ingrese solo numeros enteros (ejemplo: 1, 2, 3, etc.)\n");
                }
            } catch (Exception e) {
                System.out.println("Error en la entrada. Por favor ingrese un numero entero.\n");
                scanner.next(); // Limpiar el buffer
            }
        }
        return numero;
    }
    
    public static void main(String args[]){
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== CONTADOR DE HILOS ===");
        System.out.println("Numero de hilos a utilizar: " + NUM_THREADS);
        
        // Solicitar rango total con validación
        int rangoInicio = 0, rangoFin = 0;
        boolean rangoValido = false;
        
        while (!rangoValido) {
            System.out.println();
            rangoInicio = leerEntero(scanner, "Ingrese el valor inicial del rango total: ");
            rangoFin = leerEntero(scanner, "Ingrese el valor final del rango total: ");
            
            if (rangoInicio >= rangoFin) {
                System.out.println("Error: El valor inicial debe ser menor al valor final.");
                System.out.println("Valores ingresados: inicio=" + rangoInicio + ", fin=" + rangoFin);
                System.out.println("Intente nuevamente.");
            } else {
                rangoValido = true;
            }
        }
        
        // División automática con residuo en el último hilo
        int rangoTotal = rangoFin - rangoInicio;
        int tamañoPorHilo = rangoTotal / NUM_THREADS;
        int residuo = rangoTotal % NUM_THREADS;
        
        System.out.println("\n--- Division automatica de rangos ---");
        System.out.println("Rango total: [" + rangoInicio + ", " + rangoFin + ")");
        System.out.println("Total de numeros: " + rangoTotal);
        System.out.println("Tamaño base por hilo: " + tamañoPorHilo);
        if (residuo > 0) {
            System.out.println("Residuo: " + residuo + " (se asignara al ultimo hilo)");
        }
        System.out.println();
        
        CountThread[] threads = new CountThread[NUM_THREADS];
        int inicioActual = rangoInicio;
        
        for (int i = 0; i < NUM_THREADS; i++) {
            int a = inicioActual;
            int b;
            
            // El último hilo recibe el residuo (si hay)
            if (i == NUM_THREADS - 1) {
                b = rangoFin; // El último hilo llega hasta el final
            } else {
                b = inicioActual + tamañoPorHilo;
            }
            
            threads[i] = new CountThread(a, b);
            System.out.println("Hilo " + (i + 1) + ": [" + a + ", " + b + ") - " + (b - a) + " numeros");
            
            inicioActual = b;
        }
        
        // Menú para elegir método de ejecución
        System.out.println("\n=== MENU DE EJECUCION ===");
        System.out.println("1. Ejecutar con start() (hilos en paralelo)");
        System.out.println("2. Ejecutar con run() (secuencial)");
        
        int opcion = 0;
        while (opcion != 1 && opcion != 2) {
            opcion = leerEntero(scanner, "Seleccione una opcion (1 o 2): ");
            if (opcion != 1 && opcion != 2) {
                System.out.println("Error: Opcion invalida. Debe seleccionar 1 o 2.\n");
            }
        }
        
        if (opcion == 1) {
            System.out.println("\nEjecutando hilos con start()...\n");
            for (CountThread thread : threads) {
                thread.start();
            }
        } else {
            System.out.println("\nEjecutando hilos con run()...\n");
            for (CountThread thread : threads) {
                thread.run();
            }
        }
        
        scanner.close();
    }

}
