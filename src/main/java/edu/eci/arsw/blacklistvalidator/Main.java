/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import java.util.List;

/**
 *
 * @author hcadavid
 */
public class Main {
    
    public static void main(String a[]){
        HostBlackListsValidator hblv = new HostBlackListsValidator();
        
        // Dirección IP a verificar
        String ipAddress = "200.24.34.55";
        
        // Número de hilos a usar (puedes cambiar este valor para experimentar)
        int numberOfThreads = 1;
        
        // Si se proporciona argumento, usar ese número de hilos
        if (a.length > 0) {
            try {
                numberOfThreads = Integer.parseInt(a[0]);
            } catch (NumberFormatException e) {
                System.err.println("Argumento invalido. Usando 1 hilo por defecto.");
            }
        }
        
        System.out.println("================================================");
        System.out.println("Checking IP: " + ipAddress);
        System.out.println("Number of threads: " + numberOfThreads);
        System.out.println("================================================");
        
        // Medir tiempo de ejecución
        long startTime = System.currentTimeMillis();
        
        List<Integer> blackListOcurrences = hblv.checkHost(ipAddress, numberOfThreads);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        System.out.println("The host was found in the following blacklists: " + blackListOcurrences);
        System.out.println("Execution time: " + executionTime + " ms");
        System.out.println("================================================");
    }
    
}
