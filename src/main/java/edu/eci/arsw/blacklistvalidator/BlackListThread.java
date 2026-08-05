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
        for (int i = startIndex; i < endIndex; i++) {
            checkedListsCount++;
            
            if (skds.isInBlackListServer(i, ipAddress)) {
                blackListOccurrences.add(i);
                occurrencesCount++;
            }
        }
    }

    public int getOccurrencesCount() {
        return occurrencesCount;
    }

    public List<Integer> getBlackListOccurrences() {
        return blackListOccurrences;
    }

    public int getCheckedListsCount() {
        return checkedListsCount;
    }
}
