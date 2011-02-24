
import javax.sound.midi.*;
import java.io.*;

/** Plays a midi file provided on command line */
public class MidiPlayer {
    public static void main(String args[]) {
        // Argument check
    	String arg = "midifile.mid";
        if(arg.length() == 0) {
            helpAndExit();
        }
        String file = arg;
        if(!file.endsWith(".mid")) {
            helpAndExit();
        }
        File midiFile = new File(file);
        if(!midiFile.exists() || midiFile.isDirectory() || !midiFile.canRead()) {
            helpAndExit();
        }
        // Play once
        try {
            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.setSequence(MidiSystem.getSequence(midiFile));
            sequencer.open();
            sequencer.start();
            while(true) {
                if(sequencer.isRunning()) {
                    try {
                        Thread.sleep(1000); // Check every second
                    } catch(InterruptedException ignore) {
                        break;
                    }
                } else {
                    break;
                }
            }
            // Close the MidiDevice & free resources
            sequencer.stop();
            sequencer.close();
        } catch(MidiUnavailableException mue) {
            System.out.println("Midi device unavailable!");
        } catch(InvalidMidiDataException imde) {
            System.out.println("Invalid Midi data!");
        } catch(IOException ioe) {
            System.out.println("I/O Error!");
        } 

    }  

    /** Provides help message and exits the program */
    private static void helpAndExit() {
        System.out.println("Usage: java MidiPlayer midifile.mid,");
        System.exit(1);
    }
}
