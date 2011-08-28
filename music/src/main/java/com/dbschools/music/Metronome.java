package com.dbschools.music;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.log4j.Logger;

/**
 * A metronome.
 * @author  Dave Briccetti
 */
public class Metronome extends JPanel {

    static Logger log = Logger.getLogger(Metronome.class);
    private final int velocity = 127;
    private Thread thread; // New thread each time the metronome is turned on
    private final Runnable runnable = createRunnable();
    private long timeBetweenBeats;
    private MidiChannel channel = null;
    private boolean keepPlaying;
    private int note;

    public static void main(String[] args) {
        JFrame f = new JFrame("DBSchools Metronome");
        final JPanel met = new Metronome();
        met.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        f.getContentPane().add(met, BorderLayout.CENTER);
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    /** Creates new form Metronome */
    public Metronome() {
        try {
            final Synthesizer synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            channel = synthesizer.getChannels()[9];
            
        } catch (MidiUnavailableException ex) {
            log.error(ex);
        }
        initComponents();
        setTempo(108);
        setNoteFromChoice();
        metronomeButton.requestFocus();
    }

    /**
     * Sets the tempo. May be called while the metronome is on.
     * @param beatsPerMinute the tempo, in beats per minute
     */
    public void setTempo(int beatsPerMinute) {
        processTempoChange(beatsPerMinute);
        tempoChooser.setValue(beatsPerMinute);
    }

    /**
     * Sets the MIDI note, in the percussion channel, to use for the 
     * metronome sound. See http://en.wikipedia.org/wiki/General_MIDI. 
     * @param note the MIDI note to use
     */
    public void setNote(int note) {
        this.note = note;
    }

    /**
     * Stops the metronome.
     */
    public void stop() {
        keepPlaying = false;
        if (thread != null) {
            thread.interrupt(); // Interrupt the sleep
        }
    }

    private Runnable createRunnable() {
        return new Runnable() {

            public void run() {
                final long startTime = System.currentTimeMillis();
                long wokeLateBy = 0;

                while (keepPlaying) {
                    // Someone could change note while we sleep. Make sure we 
                    // turn on and off the same note.
                    final int noteForThisBeat = note; 
                    
                    if (wokeLateBy > 10) {
                        log.debug("Woke late by " + wokeLateBy);
                    } else {
                        channel.noteOn(noteForThisBeat, velocity);
                    }
                    final long currentTimeBeforeSleep = System.currentTimeMillis();
                    final long currentLag = (currentTimeBeforeSleep - startTime) % timeBetweenBeats;
                    final long sleepTime = timeBetweenBeats - currentLag;
                    final long expectedWakeTime = currentTimeBeforeSleep + sleepTime;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        log.debug("Interrupted");
                    }
                    wokeLateBy = System.currentTimeMillis() - expectedWakeTime;
                    channel.noteOff(noteForThisBeat);
                }
                log.debug("Thread ending");
            }
        };
    }

    private void processTempoChange(int beatsPerMinute) {
        setMetronomeButtonText(beatsPerMinute);
        timeBetweenBeats = 1000 * 60 / beatsPerMinute;
        restartAtEndOfBeatIfRunning();
    }

    private void restartAtEndOfBeatIfRunning() {
        if (keepPlaying) {
            keepPlaying = false;
            try {
                thread.join();
            } catch (InterruptedException ex) {
                log.debug(ex);
            }
            startThread();
        }
    }

    private void setMetronomeButtonText(int beatsPerMinute) {
        metronomeButton.setText(Integer.toString(beatsPerMinute));
    }

    private void startThread() {
        if (channel != null) {
            keepPlaying = true;
            thread = new Thread(runnable, "Metronome");
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
    }
    
    void setNoteFromChoice() {
        setNote(((PercussionSound)soundChooser.getSelectedItem()).getMidiNote());
    }

    static private class PercussionSound {
        private final String name;
        private final int midiNote;

        public PercussionSound(String name, int midiNote) {
            this.name = name;
            this.midiNote = midiNote;
        }

        public int getMidiNote() {
            return midiNote;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
        
    }
    
    private PercussionSound[] getSounds() {
        return new PercussionSound[] {
            new PercussionSound("Claves", 75),
            new PercussionSound("Cow Bell", 56),
            new PercussionSound("High Bongo", 60),
            new PercussionSound("Low Bongo", 61),
            new PercussionSound("High Wood Block", 76),
            new PercussionSound("Low Wood Block", 77),
        };
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        metronomeButton = new JToggleButton();
        soundChooser = new JComboBox();
        tempoChooser = new JSlider();

        setBorder(BorderFactory.createTitledBorder("Metronome"));
        setLayout(new GridBagLayout());

        metronomeButton.setFont(new Font("Tahoma", 1, 14));
        metronomeButton.setText("Beat");
        metronomeButton.setToolTipText("Start and stop the metronome");
        metronomeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                metronomeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(metronomeButton, gridBagConstraints);

        soundChooser.setModel(new DefaultComboBoxModel(getSounds()));
        soundChooser.setToolTipText("Select the sound to use");
        soundChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                soundChooserActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(soundChooser, gridBagConstraints);

        tempoChooser.setMaximum(208);
        tempoChooser.setMinimum(40);
        tempoChooser.setValue(108);
        tempoChooser.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                tempoChooserStateChanged(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        add(tempoChooser, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void metronomeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metronomeButtonActionPerformed

    if (metronomeButton.isSelected()) {
        startThread();
    } else {
        stop();
    }

}//GEN-LAST:event_metronomeButtonActionPerformed

private void soundChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_soundChooserActionPerformed
    setNoteFromChoice();
}//GEN-LAST:event_soundChooserActionPerformed

private void tempoChooserStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tempoChooserStateChanged
    final int tempo = tempoChooser.getValue();
    if (((JSlider) evt.getSource()).getValueIsAdjusting()) {
        setMetronomeButtonText(tempo);
    } else {
        processTempoChange(tempo);
    }
}//GEN-LAST:event_tempoChooserStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    JToggleButton metronomeButton;
    JComboBox soundChooser;
    JSlider tempoChooser;
    // End of variables declaration//GEN-END:variables
}
