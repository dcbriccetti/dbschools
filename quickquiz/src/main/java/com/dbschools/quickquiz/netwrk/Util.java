package com.dbschools.quickquiz.netwrk;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;

import com.dbschools.gui.ErrorHandler;
import com.dbschools.gui.SwingWorker;

/**
 * Utility routines related to networking.
 * @author David C. Briccetti
 */
public class Util {

    /**
     * Removes any map entries no longer in the members list.
     * @param view
     * @param map
     * @return
     */
    public static int removeMapEntriesForLeftSources(View view, Map<Address, ?> map) {
        Vector<Address> members = view.getMembers();
        Iterator<Address> takersIt = map.keySet().iterator();
        int numRemoved = 0;
        while (takersIt.hasNext()) {
            Address address = takersIt.next();
            if (! members.contains(address)) {
                takersIt.remove();
                ++numRemoved;
            }
        }
        return numRemoved;
    }

}
