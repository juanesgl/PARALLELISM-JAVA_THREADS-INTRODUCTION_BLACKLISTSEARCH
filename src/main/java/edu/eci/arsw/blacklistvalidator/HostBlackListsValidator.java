/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress){
        return checkHost(ipaddress, 1);
    }
    
    /**
     * Check the given host's IP address in all the available black lists using N threads,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * 
     * @param ipaddress suspicious host's IP address.
     * @param N number of threads to use for the search
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int N){
        
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        
        int totalServers = skds.getRegisteredServersCount();
        
        // División de rangos entre N hilos
        int serversPerThread = totalServers / N;
        int residue = totalServers % N;
        
        BlackListThread[] threads = new BlackListThread[N];
        int currentStart = 0;
        
        // Crear y configurar los N hilos
        for (int i = 0; i < N; i++) {
            int start = currentStart;
            int end;
            
            // El último hilo recibe el residuo
            if (i == N - 1) {
                end = totalServers;
            } else {
                end = currentStart + serversPerThread;
            }
            
            threads[i] = new BlackListThread(start, end, ipaddress, skds);
            currentStart = end;
        }
        
        // Iniciar todos los hilos
        for (BlackListThread thread : threads) {
            thread.start();
        }
        
        // Esperar a que todos los hilos terminen usando join()
        try {
            for (BlackListThread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Recolectar resultados de todos los hilos
        int ocurrencesCount = 0;
        int checkedListsCount = 0;
        
        for (BlackListThread thread : threads) {
            ocurrencesCount += thread.getOccurrencesCount();
            blackListOcurrences.addAll(thread.getBlackListOccurrences());
            checkedListsCount += thread.getCheckedListsCount();
        }
        
        // Reportar según el número de ocurrencias
        if (ocurrencesCount >= BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }                
        
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }
    
    
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
    
    
}
