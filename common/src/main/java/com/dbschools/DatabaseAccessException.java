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

package com.dbschools;

/**
 * Signals a data retrieval error.
 *  
 * @author Dave Briccetti
 */
public final class DatabaseAccessException extends Exception {

    /**
     * 
     */
    public DatabaseAccessException() {
        super();
    }

    /**
     * @param message
     */
    public DatabaseAccessException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public DatabaseAccessException(Throwable cause) {
        super(cause);
    }

}
