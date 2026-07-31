package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;

/**
 * Thread para búsqueda paralela en listas negras
 * 
 * @author juanesgl
 * @author Valero25
 */
public class BlackListThread extends Thread {
    
    private int startIndex;
    private int endIndex;
    private String ipAddress;
    private HostBlacklistsDataSourceFacade skds;
    
    private LinkedList<Integer> blackListOccurrences;
    private int occurrencesCount;
    private int checkedListsCount;
    
    /**
     * Constructor del hilo de búsqueda en listas negras
     * 
     * @param startIndex Índice inicial del rango de búsqueda (inclusivo)
     * @param endIndex Índice final del rango de búsqueda (exclusivo)
     * @param ipAddress Dirección IP a buscar
     * @param skds Instancia del facade para acceder a las listas negras
     */
    public BlackListThread(int startIndex, int endIndex, String ipAddress, HostBlacklistsDataSourceFacade skds) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.ipAddress = ipAddress;
        this.skds = skds;
        this.blackListOccurrences = new LinkedList<>();
        this.occurrencesCount = 0;
        this.checkedListsCount = 0;
    }
    
    @Override
    public void run() {
        // Buscar en el rango asignado [startIndex, endIndex)
        for (int i = startIndex; i < endIndex; i++) {
            checkedListsCount++;
            
            if (skds.isInBlackListServer(i, ipAddress)) {
                blackListOccurrences.add(i);
                occurrencesCount++;
            }
        }
    }
    
    /**
     * Obtiene el número de ocurrencias encontradas por este hilo
     * 
     * @return Número de ocurrencias encontradas
     */
    public int getOccurrencesCount() {
        return occurrencesCount;
    }
    
    /**
     * Obtiene la lista de índices de listas negras donde se encontró la IP
     * 
     * @return Lista de índices de listas negras
     */
    public List<Integer> getBlackListOccurrences() {
        return blackListOccurrences;
    }
    
    /**
     * Obtiene el número de listas revisadas por este hilo
     * 
     * @return Número de listas revisadas
     */
    public int getCheckedListsCount() {
        return checkedListsCount;
    }
}
