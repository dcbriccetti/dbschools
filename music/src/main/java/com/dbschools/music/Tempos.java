package com.dbschools.music;

import org.apache.commons.lang.ObjectUtils;

import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Tempo;

/**
 * Tempos provides static utility methods related to tempos.
 *
 * @author David C. Briccetti
 */
public final class Tempos {

    /**
     * Returns the tempo for the given instrument. If a tempo specific to the
     * specified instrument is not found, returns the default tempo if useDefault
     * is true, otherwise returns null.
     * @param tempos
     * @param instrument
     * @param useDefault
     * @return the tempo for the given instrument
     */
    public static Tempo tempoForInstrument(final Iterable<Tempo> tempos,
            final Instrument instrument, boolean useDefault) {
        Tempo defaultTempo = null;
        
        for (Tempo tempo : tempos) {
            final Instrument anInstrument = tempo.getMusicInstrument();
            if (anInstrument == null) {
                defaultTempo = tempo;
            }
            if (ObjectUtils.equals(instrument, anInstrument)) {
                return tempo;
            }
        }
        return useDefault ? defaultTempo : null;
    }
}
