/*
 * DBSchools
 * Copyright (C) 2005 David C. Briccetti
 * www.davebsoft.com
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.dbschools.music;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.dbschools.music.events.Event;
import com.dbschools.music.orm.User;

public class ClientSession implements Serializable, Comparable<ClientSession> {
    private static final long serialVersionUID = -4169836336490640853L;
	private final Integer sessionId;
	private final User user;
    private final String databaseName;
    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
    
	public ClientSession(User user, String databaseName) {
        this.user = user;
        this.databaseName = databaseName;
		sessionId = (int) (Math.random() * Integer.MAX_VALUE);
	}
	
	@Override
    public String toString() {
        return new ToStringBuilder(this).append("sessionId", sessionId).
                append("user", user).toString();
	}

	public void enqueueEvent(Event event) {
	    eventQueue.add(event);
	}
	
	public Event dequeueEvent() throws InterruptedException {
	    return eventQueue.take();
	}
	
	public int compareTo(ClientSession o) {
		return sessionId.compareTo(o.sessionId);
	}

	public Integer getSessionId() {
		return sessionId;
	}

	public final User getUser() {
        return user;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((sessionId == null) ? 0 : sessionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ClientSession other = (ClientSession) obj;
        if (sessionId == null) {
            if (other.sessionId != null)
                return false;
        } else if (!sessionId.equals(other.sessionId))
            return false;
        return true;
    }

}
