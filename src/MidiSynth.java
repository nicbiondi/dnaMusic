/*
 * @(#)MidiSynth.java	1.15	99/12/03
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import java.util.Vector;

import javax.sound.midi.Instrument;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * Illustrates general MIDI melody instruments and MIDI controllers.
 *
 * @version @(#)MidiSynth.java	1.15 99/12/03
 * @author Brian Lichtenwalter  
 */
public class MidiSynth extends JPanel implements ControlContext {

	
    final int PROGRAM = 192;
    final int NOTEON = 144;
    final int NOTEOFF = 128;
    final int SUSTAIN = 64;
    final int REVERB = 91;
    final int ON = 0, OFF = 1;
    final Color jfcBlue = new Color(204, 204, 255);
    final Color pink = new Color(255, 175, 175);
    Sequencer sequencer;
    Sequence sequence;
    Synthesizer synthesizer;
    Instrument instruments[];
    ChannelData channels[];
    ChannelData cc;    // current channel
    JCheckBox mouseOverCB = new JCheckBox("mouseOver", true);
    JSlider veloS, presS, bendS, revbS, shiftS, speedS;
    JCheckBox soloCB, monoCB, muteCB, sustCB; 
    Vector keys = new Vector();
    Vector whiteKeys = new Vector();
    JTable table;
    Piano piano;
    boolean record;
    Track track;
    long startTime;
    RecordFrame recordFrame;
    Controls controls;
    
    
    boolean stopDnaMusic=false;
    int keyShift;
    public int keySpeed=300;
    Key prevKey;
    double metrics[]= new double[21];
    int dnaIterator = 0;
    int lastNote=0;
    String dnaString="";//main buffer that holds the dna strand
    String dnaString2="";//main buffer that holds the second strand
    
    //text box stuff
    protected JTextField textField;
    protected JTextArea textArea;
    protected JTextArea textArea2;
    private final static String newline = "\n";
    
    public MidiSynth() {
    	
        setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        EmptyBorder eb = new EmptyBorder(5,5,5,5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb,bb);
        p.setBorder(new CompoundBorder(cb,eb));
        JPanel pp = new JPanel(new BorderLayout());
        pp.setBorder(new EmptyBorder(10,20,10,5));
        pp.add(piano = new Piano());
        p.add(pp);
        p.add(controls = new Controls());
        p.add(new InstrumentsTable());
        add(p);
        
        
        
    }


    public void open() {
        try {
            if (synthesizer == null) {
                if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
                    System.out.println("getSynthesizer() failed!");
                    return;
                }
            } 
            synthesizer.open();
            sequencer = MidiSystem.getSequencer();
            sequence = new Sequence(Sequence.PPQ, 10);
        } catch (Exception ex) { ex.printStackTrace(); return; }

        Soundbank sb = synthesizer.getDefaultSoundbank();
	if (sb != null) {
            instruments = synthesizer.getDefaultSoundbank().getInstruments();
            synthesizer.loadInstrument(instruments[0]);
        }
        MidiChannel midiChannels[] = synthesizer.getChannels();
        channels = new ChannelData[midiChannels.length];
        for (int i = 0; i < channels.length; i++) {
            channels[i] = new ChannelData(midiChannels[i], i);
        }
        cc = channels[0];

        ListSelectionModel lsm = table.getSelectionModel();
        lsm.setSelectionInterval(0,0);
        lsm = table.getColumnModel().getSelectionModel();
        lsm.setSelectionInterval(0,0);
    }


    public void close() {
        if (synthesizer != null) {
            synthesizer.close();
        }
        if (sequencer != null) {
            sequencer.close();
        }
        sequencer = null;
        synthesizer = null;
        instruments = null;
        channels = null;
        if (recordFrame != null) {
            recordFrame.dispose();
            recordFrame = null;
        }
    }




    /**
     * given 120 bpm:
     *   (120 bpm) / (60 seconds per minute) = 2 beats per second
     *   2 / 1000 beats per millisecond
     *   (2 * resolution) ticks per second
     *   (2 * resolution)/1000 ticks per millisecond, or 
     *      (resolution / 500) ticks per millisecond
     *   ticks = milliseconds * resolution / 500
     */
    public void createShortEvent(int type, int num) {
        ShortMessage message = new ShortMessage();
        try {
            long millis = System.currentTimeMillis() - startTime;
            long tick = millis * sequence.getResolution() / 500;
            message.setMessage(type+cc.num, num, cc.velocity); 
            MidiEvent event = new MidiEvent(message, tick);
            track.add(event);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public class HeartBeatTask extends TimerTask{
    	
    	public void run(){    		
    		playDNA();
    	}
    }
    /**
     * Black and white keys or notes on the piano.
     */
    class Key extends Rectangle {
        int noteState = OFF;
        int kNum;
        public Key(int x, int y, int width, int height, int num) {
            super(x, y, width, height);
            kNum = num;
        }
        public boolean isNoteOn() {
            return noteState == ON;
        }
        public void on() {
            setNoteState(ON);
            cc.channel.noteOn(kNum, cc.velocity);
            if (record) {
                createShortEvent(NOTEON, kNum);
            }
        }
        public void off() {
            setNoteState(OFF);
            cc.channel.noteOff(kNum, cc.velocity);
            if (record) {
                createShortEvent(NOTEOFF, kNum);
            }
        }
        public void setNoteState(int state) {
            noteState = state;
        }
    } // End class Key

    public String formatString(String dnaString)
    {
      //remove numbers if they exist
      dnaString=dnaString.replaceAll("0", "");
      dnaString=dnaString.replaceAll("1", "");
      dnaString=dnaString.replaceAll("2", "");
      dnaString=dnaString.replaceAll("3", "");
      dnaString=dnaString.replaceAll("4", "");
      dnaString=dnaString.replaceAll("5", "");
      dnaString=dnaString.replaceAll("6", "");
      dnaString=dnaString.replaceAll("7", "");
      dnaString=dnaString.replaceAll("8", "");
      dnaString=dnaString.replaceAll("9", "");
      //remove newlines
      dnaString=dnaString.replaceAll("\n", "");
      //remove spaces and make uppercase
      dnaString=dnaString.replaceAll(" ", "").toUpperCase();
      return dnaString;
    }
    //this function get's all the pertenant DNA variable set upe
    public void primeDNA()
    {
    	//metrics.
    	metrics= new double[21];;//reset metrics
    	stopDnaMusic=false;//have to set this to allow it to play a second time
    	dnaIterator=0;//reset main counter

    	//dnaString=synapse;
    	dnaString=textArea.getText();
    	
    	if(dnaString2.length()== 0)
    		dnaString2=textArea.getText();
    	else
    	dnaString2=textArea2.getText();
    	playDNA();
    }
    //itteration method for the bulk of the DNA music functions.
    public void playDNA()     {

    	//format the dna string 
    	dnaString=formatString(dnaString);
    	textArea.setText(dnaString);
    	textArea.setCaretPosition(0);
        textArea.select(dnaIterator,dnaIterator+3);
    	String tmp = "";
    	String tmp2 = "";
    	
    	//format the dna string 
    	dnaString2=formatString(dnaString2);
    	textArea2.setText(dnaString2);
    	textArea2.setCaretPosition(0);
        textArea2.select(dnaIterator,dnaIterator+3);
    	


    	int codonValue;
    	int codonValue2;
    	
    	//int readingFrame=0;  //choose 0,1 or 2 for the reading frame you want to use. (zero is default
    	int notes = 0; // counter, don't change it
    	int mcount = 0; //counter, don't change it

    	int base = 4;//using base 4 math
    	//int shift = 36; //this number represents the starting note for the song.  28 is dead center for middle c for codon mode. 60 starts on middle c.
    	int chord = 1; // how many notes do you want to play at the same time during baseline.

    	boolean chords = false; //this enables a baseline.
    	int measure = 16; // this tells when to hit the baseline    	
    	if(dnaIterator<dnaString.length()-3 && stopDnaMusic == false)
    	{    		
    		
    		tmp = dnaString.substring(dnaIterator, dnaIterator+3);    		
    		tmp=convertToDecimal(tmp); 	
    		
    		tmp2 = dnaString2.substring(dnaIterator, dnaIterator+3);
    		tmp2=convertToDecimal(tmp2); 
    		
    		//convert to base 10
    		codonValue = 0;
    		codonValue += Integer.parseInt(tmp.substring(0, 1))*base*base;
    		codonValue += Integer.parseInt(tmp.substring(1, 2))*base;
    		codonValue += Integer.parseInt(tmp.substring(2, 3));
    		
    		//convert to base 10
    		codonValue2 = 0;
    		codonValue2 += Integer.parseInt(tmp2.substring(0, 1))*base*base;
    		codonValue2 += Integer.parseInt(tmp2.substring(1, 2))*base;
    		codonValue2 += Integer.parseInt(tmp2.substring(2, 3));

    		int aminoAcidValue = mapToAminoAcid(codonValue,"simple");
    		int aminoAcidValue2 = mapToAminoAcid(codonValue2,"simple");
    		//int aminoAcidValue = mapToAminoAcid(codonValue,"codonValue");
    		//  if(aminoAcidValue <= 11)            	    
    		//  cc.channel.noteOn(aminoAcidValue+shift-12, cc.velocity);
    		//length of note waits during duplicates.
    		if(lastNote!=aminoAcidValue)
    		{ 
    			//Key currentKey = (Key) whiteKeys.get(aminoAcidValue+keyShift);
    			Key currentKey = (Key) whiteKeys.get(aminoAcidValue-1+keyShift);
    			currentKey.on();
    			metrics[aminoAcidValue-1]+=1;//sum up data for key metrics
    			
    			//turn last key off so that you can accurately see the notes hit on the keyboard;
				if (prevKey != null && prevKey != currentKey ) {
    			   //  prevKey.off();
    			} 
    			prevKey = currentKey;
    			//stopDnaMusic=true;
    			//continue;

    		}  
    		if(lastNote!=aminoAcidValue2)
    		{ 
    			//Key currentKey = (Key) whiteKeys.get(aminoAcidValue+keyShift);
    			Key currentKey = (Key) whiteKeys.get(aminoAcidValue2-1+keyShift);
    			currentKey.on();
    			metrics[aminoAcidValue2-1]+=1;//sum up data for key metrics
    			
    			//turn last key off so that you can accurately see the notes hit on the keyboard;
				if (prevKey != null && prevKey != currentKey ) {
    			   //  prevKey.off();
    			} 
    			prevKey = currentKey;
    			//stopDnaMusic=true;
    			//continue;

    		} 
    		
    		//else continue;
    		lastNote=aminoAcidValue;
    		if(chords)
    		{
    			if(notes<=chord)
    			{
    				notes++;
    				//get next key!!!!  continue;
    			}
    			//this allows for a baseline to play at a set measure
    			if(mcount>=measure)
    			{
    				mcount = 0;
    				notes = 0;//set off another chord to be played
    				//set the instrument to play for the measure
    				//  synthesizer.loadInstrument(instruments[16]);
    				cc.channel.programChange(16);
    				//if(lastNote!=aminoAcidValue){
    				cc.channel.noteOff(lastNote+keyShift-12, cc.velocity);
    				cc.channel.noteOn(aminoAcidValue+keyShift-12, cc.velocity);
        			Key currentKey = (Key) whiteKeys.get(aminoAcidValue+keyShift);
        			currentKey.on();
        			//prevMKey=currentKey;
        			
    				//}
    				//get next key!!!!!!!continue;

    			}
    			//synthesizer.loadInstrument(instruments[1]);
    			cc.channel.programChange(1);
    			mcount++;
    			
    		}        	
    		//if(dnaIterator<dnaString.length()-3&&stopDnaMusic==false)
    		//{
    		java.util.Timer t1 = new java.util.Timer();
    		HeartBeatTask tt = new HeartBeatTask();
        	
    		t1.schedule(tt, keySpeed); 		
    		//}
        	getParent().repaint();
        	dnaIterator+=3;
    	}  
        else {
        	
        	stopDnaMusic=true;
        	lastNote = 999; //fixes a bug that mutes a key on second playback
        	calculateMetrics();
		}
    }  
    
    public  String convertToDecimal(String tmp)
    {
      tmp=tmp.replaceAll("T","0");
	  tmp=tmp.replaceAll("U","0");
	  tmp=tmp.replaceAll("C","1");
	  tmp=tmp.replaceAll("A","2");
	  tmp=tmp.replaceAll("G","3");
	  
	  return tmp;
    }
    
    public void calculateMetrics()
    {
    	//count total
    	int total=0;
    	double tmp=0;
		for (int i = 0; i < metrics.length; i++) {
			total+= metrics[i];
		}
		for (int i = 0; i < metrics.length; i++) {
			if(metrics[i]==0)
				System.out.println(i + 1 + "--" + 0+"%");
			else
			{
				tmp=Math.round((metrics[i]/total)*100);
			  System.out.println(i + 1 + "--" + tmp+"%");
			}
		}
		
    }
    	   public int mapToAminoAcid(int codonValue, String type){

               /*
                * OPTIONS:
                * WhiteOnly  only play white keys.. only works if you shift to the C key. makes 3 perfect octives!
                * simpleCodonValue - sets notes side by side for 21 consecutive groupings that denote amino acids
                * empty - splits up the keys that represent amina acids by choosing the lowest value codon the amino acid can be represented by.
                * */
           	int simpleCodonValue=0;
           	int whiteOnly=0;
           	switch(codonValue)
           	{
           	     case 0:
                 case 1:
           		//Phenylalanine
           		codonValue=1;
           		simpleCodonValue=1;
           		whiteOnly = 1;
           		break;
                 case 2:
                 case 3:
                 case 16:
                 case 17:
                 case 18:
                 case 19:
           		//Leucine
           		codonValue=3;
           		simpleCodonValue=2;
           		whiteOnly = 3;
           		break;
                 case 4:
                 case 5:
                 case 6:
                 case 7:
                 case 44:
                 case 45:
           		//Serine
           		codonValue=5;
           		simpleCodonValue=3;
           		whiteOnly = 5;
           		break;
                 case 8:
                 case 9:
           		//Tyrosine
           		codonValue=9;
           		simpleCodonValue=4;
           		whiteOnly = 6;
           		break;
                 case 10:
                 case 11:
                 case 14:  
           		//Stop
           		codonValue=11;
           		simpleCodonValue=5;
           		whiteOnly = 8;
           		break;
                 case 12:
                 case 13:
           		//Cysteine
           		codonValue=13;
           		simpleCodonValue=6;
           		whiteOnly = 10;
           		break;
                 case 15:
                 //Tryptophan
           		codonValue=16;
           		simpleCodonValue=7;
           		whiteOnly = 12;
           		break;
                 case 20:
                 case 21:
                 case 22:
                 case 23:
           		//Proline
           		codonValue=21;
           		simpleCodonValue=8;
           		whiteOnly = 13;
           		break;
                 case 24:
                 case 25:
           		//Hystidine
           		codonValue=25;
           		simpleCodonValue=9;
           		whiteOnly = 15;
           		break;
                 case 26:
                 case 27:
           		//Glutamine
           		codonValue=27;
           		simpleCodonValue=10;
           		whiteOnly = 17;
           		break;
                 case 28:
                 case 29:
                 case 30:
                 case 31:
                 case 46:
                 case 47:
           		//Arginine
           		codonValue=29;
           		simpleCodonValue=11;
           		whiteOnly = 18;
           		break;
                 case 32:
                 case 33:
                 case 34:
           		//Isoleucine
           		codonValue=33;
           		simpleCodonValue=12;
           		whiteOnly = 20;
           		break;
                 case 35:
             		//Methionine
             		codonValue=36;
             		simpleCodonValue=13;
             		whiteOnly = 22;
             		break;
                 case 36:
                 case 37:
                 case 38:
                 case 39:
           		//Threonine
           		codonValue=37;
           		simpleCodonValue=14;
           		whiteOnly = 24;
           		break;
                 case 40:
                 case 41:
           		//Asparagine
           		codonValue=41;
           		simpleCodonValue=15;
           		whiteOnly = 25;
           		break;
                 case 42:
                 case 43:
           		//Lysine
           		codonValue=43;
           		simpleCodonValue=16;
           		whiteOnly = 27;
           		break;
                 case 48:
                 case 49:
                 case 50:
                 case 51:
           		//Valine
           		codonValue=49;
           		simpleCodonValue=17;
           		whiteOnly = 29;
           		break;
                 case 52:
                 case 53:
                 case 54:
                 case 55:
           		//Alanine
           		codonValue=53;
           		simpleCodonValue=18;
           		whiteOnly = 30;
           		break;
                 case 56:
                 case 57:
           		//Aspartic
           		codonValue=57;
           		simpleCodonValue=19;
           		whiteOnly = 32;
           		break;
                 case 58:
                 case 59:
           		//Glutamic
           		codonValue=59;
           		simpleCodonValue=20;
           		whiteOnly = 34;
           		break;
                 case 60:
                 case 61:
                 case 62:
                 case 63:
           		//Glycine
           		codonValue=61;
           		simpleCodonValue=21;
           		whiteOnly = 36;
           		break;           	
           	}
           	if(type.equals("simple"))
           	  return simpleCodonValue;
           	else if(type.equals("whiteOnly"))
           	  return whiteOnly;
           	else
           		return codonValue;
           }
    
    /**
     * Piano renders black & white keys and plays the notes for a MIDI 
     * channel.  
     */
    class Piano extends JPanel implements MouseListener {

        Vector blackKeys = new Vector();
        Key prevKey;
        final int kw = 16, kh = 80;


        public Piano() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(42*kw, kh+1));
            int transpose = 24;  
            int whiteIDs[] = { 0, 2, 4, 5, 7, 9, 11 }; 
          
            for (int i = 0, x = 0; i < 6; i++) {
                for (int j = 0; j < 7; j++, x += kw) {
                    int keyNum = i * 12 + whiteIDs[j] + transpose;
                    whiteKeys.add(new Key(x, 0, kw, kh, keyNum));
                }
            }
            for (int i = 0, x = 0; i < 6; i++, x += kw) {
                int keyNum = i * 12 + transpose;
                blackKeys.add(new Key((x += kw)-4, 0, kw/2, kh/2, keyNum+1));
                blackKeys.add(new Key((x += kw)-4, 0, kw/2, kh/2, keyNum+3));
                x += kw;
                blackKeys.add(new Key((x += kw)-4, 0, kw/2, kh/2, keyNum+6));
                blackKeys.add(new Key((x += kw)-4, 0, kw/2, kh/2, keyNum+8));
                blackKeys.add(new Key((x += kw)-4, 0, kw/2, kh/2, keyNum+10));
            }
            keys.addAll(blackKeys);
            keys.addAll(whiteKeys);

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    if (mouseOverCB.isSelected()) {
                        Key key = getKey(e.getPoint());
                        if (prevKey != null && prevKey != key) {
                            prevKey.off();
                        } 
                        if (key != null && prevKey != key) {
                            key.on();
                        }
                        prevKey = key;
                        repaint();
                    }
                }
            });
            addMouseListener(this);
        }

        public void mousePressed(MouseEvent e) {
            prevKey = getKey(e.getPoint());
            if (prevKey != null) {
                prevKey.on();
                repaint();
            }
        }
     
        public void mouseReleased(MouseEvent e) { 
            if (prevKey != null) {
              //  prevKey.off();
                repaint();
            }
        }
        public void mouseExited(MouseEvent e) { 
            if (prevKey != null) {
                prevKey.off();
                repaint();
                prevKey = null;
            }
        }
        public void mouseClicked(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }


        public Key getKey(Point point) {
            for (int i = 0; i < keys.size(); i++) {
                if (((Key) keys.get(i)).contains(point)) {
                    return (Key) keys.get(i);
                }
            }
            return null;
        }
        
        public Key getKey(int keyNum) {
            return (Key) keys.get(keyNum);
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Dimension d = getSize();

            g2.setBackground(getBackground());
            g2.clearRect(0, 0, d.width, d.height);

            g2.setColor(Color.white);
            g2.fillRect(0, 0, 42*kw, kh);

            for (int i = 0; i < whiteKeys.size(); i++) {
                Key key = (Key) whiteKeys.get(i);
                if (key.isNoteOn()) {
                    g2.setColor(record ? pink : jfcBlue);
                    g2.fill(key);
                }
                g2.setColor(Color.black);
                g2.draw(key);
            }
            for (int i = 0; i < blackKeys.size(); i++) {
                Key key = (Key) blackKeys.get(i);
                if (key.isNoteOn()) {
                    g2.setColor(record ? pink : jfcBlue);
                    g2.fill(key);
                    g2.setColor(Color.black);
                    g2.draw(key);
                } else {
                    g2.setColor(Color.black);
                    g2.fill(key);
                }
            }
        }
    } // End class Piano



    /**
     * Stores MidiChannel information.
     */
    class ChannelData {

        MidiChannel channel;
        boolean solo, mono, mute, sustain;
        int velocity, pressure, bend, reverb, shift, speed;
        int row, col, num;
 
        public ChannelData(MidiChannel channel, int num) {
            this.channel = channel;
            this.num = num;
            velocity = pressure = bend = reverb = 64;
            shift = 28;
            speed = 300;
        }

        public void setComponentStates() {
            table.setRowSelectionInterval(row, row);
            table.setColumnSelectionInterval(col, col);

            soloCB.setSelected(solo);
            monoCB.setSelected(mono);
            muteCB.setSelected(mute);
            //sustCB.setSelected(sustain);

            JSlider slider[] = { veloS, presS, bendS, revbS, shiftS, speedS };
            int v[] = { velocity, pressure, bend, reverb, shift, speed };
            for (int i = 0; i < slider.length; i++) {
                TitledBorder tb = (TitledBorder) slider[i].getBorder();
                String s = tb.getTitle();
                tb.setTitle(s.substring(0, s.indexOf('=')+1)+s.valueOf(v[i]));
                slider[i].repaint();
            }
        }
    } // End class ChannelData



    /**
     * Table for 128 general MIDI melody instuments.
     */
    public class InstrumentsTable extends JPanel {

        private String names[] = { 
           "Piano", "Chromatic Perc.", "Organ", "Guitar", 
           "Bass", "Strings", "Ensemble", "Brass", 
           "Reed", "Pipe", "Synth Lead", "Synth Pad",
           "Synth Effects", "Ethnic", "Percussive", "Sound Effects" };
        private int nRows = 8;
        private int nCols = names.length; // just show 128 instruments

        public InstrumentsTable() {
            setLayout(new BorderLayout());

            TableModel dataModel = new AbstractTableModel() {
                public int getColumnCount() { return nCols; }
                public int getRowCount() { return nRows;}
                public Object getValueAt(int r, int c) { 
                    if (instruments != null) {
                        return instruments[c*nRows+r].getName();
                    } else {
                        return Integer.toString(c*nRows+r);
                    }
                }
                public String getColumnName(int c) { 
                    return names[c];
                }
                public Class getColumnClass(int c) {
                    return getValueAt(0, c).getClass();
                }
                public boolean isCellEditable(int r, int c) {return false;}
                public void setValueAt(Object obj, int r, int c) {}
            };
    
            table = new JTable(dataModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Listener for row changes
            ListSelectionModel lsm = table.getSelectionModel();
            lsm.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    ListSelectionModel sm = (ListSelectionModel) e.getSource();
                    if (!sm.isSelectionEmpty()) {
                        cc.row = sm.getMinSelectionIndex();
                    }
                    programChange(cc.col*nRows+cc.row);
                }
            });

            // Listener for column changes
            lsm = table.getColumnModel().getSelectionModel();
            lsm.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    ListSelectionModel sm = (ListSelectionModel) e.getSource();
                    if (!sm.isSelectionEmpty()) {
                        cc.col = sm.getMinSelectionIndex();
                    }
                    programChange(cc.col*nRows+cc.row);
                }
            });

            table.setPreferredScrollableViewportSize(new Dimension(nCols*110, 200));
            table.setCellSelectionEnabled(true);
            table.setColumnSelectionAllowed(true);
            for (int i = 0; i < names.length; i++) {
                TableColumn column = table.getColumn(names[i]);
                column.setPreferredWidth(110);
            }
            table.setAutoResizeMode(table.AUTO_RESIZE_OFF);
        
            JScrollPane sp = new JScrollPane(table);
            sp.setVerticalScrollBarPolicy(sp.VERTICAL_SCROLLBAR_NEVER);
            sp.setHorizontalScrollBarPolicy(sp.HORIZONTAL_SCROLLBAR_ALWAYS);
            add(sp);
        }

        public Dimension getPreferredSize() {
            return new Dimension(800,170);
        }
        public Dimension getMaximumSize() {
            return new Dimension(800,170);
        }

        public void programChange(int program) {
            if (instruments != null) {
                synthesizer.loadInstrument(instruments[program]);
            }
            cc.channel.programChange(program);
            if (record) {
                createShortEvent(PROGRAM, program);
            }
        }
    }


    /**
     * A collection of MIDI controllers.
     */
    class Controls extends JPanel implements ActionListener, ChangeListener, ItemListener {
    	
        public JButton recordB;
        JMenu menu;
        int fileNum = 0;

        public Controls() {
        	super(new GridBagLayout());//text box
        	
        	
        	
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(5,10,5,10));

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

            veloS = createSlider("Velocity", p);
            presS = createSlider("Pressure", p);
            revbS = createSlider("Reverb", p);
            shiftS = createSlider("DNA Shift", p, 0,20,14);
            speedS = createSlider("DNA Speed", p, 10,1000,300);

			// create a slider with a 14-bit range of values for pitch-bend
            bendS = create14BitSlider("Bend", p);

            p.add(Box.createHorizontalStrut(10));
            add(p);

            p = new JPanel();
            p.setBorder(new EmptyBorder(10,0,10,0));
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

            JComboBox combo = new JComboBox();
            combo.setPreferredSize(new Dimension(120,25));
            combo.setMaximumSize(new Dimension(120,25));
            for (int i = 1; i <= 16; i++) {
                combo.addItem("Channel " + String.valueOf(i));
            } 
            combo.addItemListener(this);
            p.add(combo);
            p.add(Box.createHorizontalStrut(20));

            muteCB = createCheckBox("Mute", p);
            soloCB = createCheckBox("Solo", p);
            monoCB = createCheckBox("Mono", p);
            //sustCB = createCheckBox("Sustain", p);

            createButton("All Notes Off", p);
            p.add(Box.createHorizontalStrut(10));
            p.add(mouseOverCB);
            p.add(Box.createHorizontalStrut(10));
            recordB = createButton("Record...", p);
            p.add(Box.createHorizontalStrut(10));
            createButton("play DNA", p);
            p.add(Box.createHorizontalStrut(10));
            add(p);
            
            
          //text demo stuff
            

           // textField = new JTextField(20);
           // textField.addActionListener(this);

            textArea = new JTextArea(5, 20);
            textArea2 = new JTextArea(5, 20);
            textArea.setEditable(true);
            textArea2.setEditable(true);
            JScrollPane scrollPane = new JScrollPane(textArea);
            JScrollPane scrollPane2 = new JScrollPane(textArea2);

            textArea.setText("");
            //Add Components to this panel.
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;

            c.fill = GridBagConstraints.HORIZONTAL;
            //add(textField, c);

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            add(scrollPane, c);
            add(scrollPane2, c);
            
        }

        public JButton createButton(String name, JPanel p) {
            JButton b = new JButton(name);
            b.addActionListener(this);
            p.add(b);
            return b;
        }

        private JCheckBox createCheckBox(String name, JPanel p) {
            JCheckBox cb = new JCheckBox(name);
            cb.addItemListener(this);
            p.add(cb);
            return cb;
        }
        private JSlider createSlider(String name, JPanel p) {
        	return createSlider(name, p, 0, 127, 64);
        
        }
        private JSlider createSlider(String name, JPanel p, int left, int right,int center) {
            JSlider slider = new JSlider(JSlider.HORIZONTAL, left, right, center);
            slider.addChangeListener(this);
            TitledBorder tb = new TitledBorder(new EtchedBorder());
            tb.setTitle(name + " = "+center);
            slider.setBorder(tb);
            p.add(slider);
            p.add(Box.createHorizontalStrut(5));
            return slider;
        }

        private JSlider create14BitSlider(String name, JPanel p) {
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 16383, 8192);
            slider.addChangeListener(this);
            TitledBorder tb = new TitledBorder(new EtchedBorder());
            tb.setTitle(name + " = 8192");
            slider.setBorder(tb);
            p.add(slider);
            p.add(Box.createHorizontalStrut(5));
            return slider;
        }     
        

        public void stateChanged(ChangeEvent e) {
            JSlider slider = (JSlider) e.getSource();
            int value = slider.getValue();
            TitledBorder tb = (TitledBorder) slider.getBorder();
            String s = tb.getTitle();
            tb.setTitle(s.substring(0, s.indexOf('=')+1) + s.valueOf(value));
            if (s.startsWith("Velocity")) {
                cc.velocity = value;
            } else if (s.startsWith("Pressure")) {
                cc.channel.setChannelPressure(cc.pressure = value);
            } else if (s.startsWith("Bend")) {
                cc.channel.setPitchBend(cc.bend = value);
            } else if (s.startsWith("Reverb")) {
                cc.channel.controlChange(REVERB, cc.reverb = value);
            } else if (s.startsWith("DNA Sh")) {
                keyShift = value;
    			Key currentKey = (Key) whiteKeys.get(value);
    			currentKey.on();
    			if (prevKey != null && prevKey != currentKey ) 
   			     prevKey.off();
   			 
   			    prevKey=currentKey;
    			getParent().repaint();
    			//repaint();
            } else if (s.startsWith("DNA Sp")) {
            keySpeed = value;
			//repaint();
            }
            
            slider.repaint();
           
        }

        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() instanceof JComboBox) {
                JComboBox combo = (JComboBox) e.getSource();
                cc = channels[combo.getSelectedIndex()];
                cc.setComponentStates();
            } else {
                JCheckBox cb = (JCheckBox) e.getSource();
                String name = cb.getText();
                if (name.startsWith("Mute")) {
                    cc.channel.setMute(cc.mute = cb.isSelected());
                } else if (name.startsWith("Solo")) {
                    cc.channel.setSolo(cc.solo = cb.isSelected());
                } else if (name.startsWith("Mono")) {
                    cc.channel.setMono(cc.mono = cb.isSelected());
                } else if (name.startsWith("Sustain")) {
                    cc.sustain = cb.isSelected();
                    cc.channel.controlChange(SUSTAIN, cc.sustain ? 127 : 0);
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            if (button.getText().startsWith("All")) {
                for (int i = 0; i < channels.length; i++) {
                    channels[i].channel.allNotesOff();
                    stopDnaMusic=true;
                }
                for (int i = 0; i < keys.size(); i++) {
                    ((Key) keys.get(i)).setNoteState(OFF);
                }
            } else if (button.getText().startsWith("Record")) {
                if (recordFrame != null) {
                    recordFrame.toFront();
                } else {
                    recordFrame = new RecordFrame();
                }
            }else if (button.getText().startsWith("play DNA")) {            	
            	primeDNA();
    
            }
        }
    } // End class Controls



    /**
     * A frame that allows for midi capture & saving the captured data.
     */
    class RecordFrame extends JFrame implements ActionListener, MetaEventListener {

        public JButton recordB, playB, saveB;
        Vector tracks = new Vector();
        DefaultListModel listModel = new DefaultListModel();
        TableModel dataModel;
        JTable table;


        public RecordFrame() {
            super("Midi Capture");
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {recordFrame = null;}
            });

            sequencer.addMetaEventListener(this);
            try {
                sequence = new Sequence(Sequence.PPQ, 10);
            } catch (Exception ex) { ex.printStackTrace(); }

            JPanel p1 = new JPanel(new BorderLayout());

            JPanel p2 = new JPanel();
            p2.setBorder(new EmptyBorder(5,5,5,5));
            p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));

            recordB = createButton("Record", p2, true);
            playB = createButton("Play", p2, false);
            saveB = createButton("Save...", p2, false);
           

            getContentPane().add("North", p2);

            final String[] names = { "Channel #", "Instrument" };
    
            dataModel = new AbstractTableModel() {
                public int getColumnCount() { return names.length; }
                public int getRowCount() { return tracks.size();}
                public Object getValueAt(int row, int col) { 
                    if (col == 0) {
                        return ((TrackData) tracks.get(row)).chanNum;
                    } else if (col == 1) {
                        return ((TrackData) tracks.get(row)).name;
                    } 
                    return null;
                }
                public String getColumnName(int col) {return names[col]; }
                public Class getColumnClass(int c) {
                    return getValueAt(0, c).getClass();
                }
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
                public void setValueAt(Object val, int row, int col) { 
                    if (col == 0) {
                        ((TrackData) tracks.get(row)).chanNum = (Integer) val;
                    } else if (col == 1) {
                        ((TrackData) tracks.get(row)).name = (String) val;
                    } 
                }
            };
    
            table = new JTable(dataModel);
            TableColumn col = table.getColumn("Channel #");
            col.setMaxWidth(65);
            table.sizeColumnsToFit(0);
        
            JScrollPane scrollPane = new JScrollPane(table);
            EmptyBorder eb = new EmptyBorder(0,5,5,5);
            scrollPane.setBorder(new CompoundBorder(eb,new EtchedBorder()));

	    getContentPane().add("Center", scrollPane);
	    pack();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            int w = 210;
            int h = 160;
            setLocation(d.width/2 - w/2, d.height/2 - h/2);
            setSize(w, h);
	    setVisible(true);
        }


        public JButton createButton(String name, JPanel p, boolean state) {
            JButton b = new JButton(name);
            b.setFont(new Font("serif", Font.PLAIN, 10));
            b.setEnabled(state);
            b.addActionListener(this);
            p.add(b);
            return b;
        }


        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            if (button.equals(recordB)) {
                record = recordB.getText().startsWith("Record");
                if (record) {
                    track = sequence.createTrack();
                    startTime = System.currentTimeMillis();

                    // add a program change right at the beginning of 
                    // the track for the current instrument
                    createShortEvent(PROGRAM,cc.col*8+cc.row);

                    recordB.setText("Stop");
                    playB.setEnabled(false);
                    saveB.setEnabled(false);
                } else {
                    String name = null;
                    if (instruments != null) {
                        name = instruments[cc.col*8+cc.row].getName();
                    } else {
                        name = Integer.toString(cc.col*8+cc.row);
                    }
                    tracks.add(new TrackData(cc.num+1, name, track)); 
                    table.tableChanged(new TableModelEvent(dataModel));
                    recordB.setText("Record");
                    playB.setEnabled(true);
                    saveB.setEnabled(true);
                } 
            } else if (button.equals(playB)) {
                if (playB.getText().startsWith("Play")) {
                    try {
                        sequencer.open();
                        sequencer.setSequence(sequence);
                    } catch (Exception ex) { ex.printStackTrace(); }
                    sequencer.start();
                    playB.setText("Stop");
                    recordB.setEnabled(false);
                } else {
                    sequencer.stop();
                    playB.setText("Play");
                    recordB.setEnabled(true);
                } 
            } else if (button.equals(saveB)) {
                try {
                    File file = new File(System.getProperty("user.dir"));
                    JFileChooser fc = new JFileChooser(file);
                    fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        public boolean accept(File f) {
                            if (f.isDirectory()) {
                                return true;
                            }
                            return false;
                        }
                        public String getDescription() {
                            return "Save as .mid file.";
                        }
                    });
                    if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        saveMidiFile(fc.getSelectedFile());
                    }
                } catch (SecurityException ex) { 
                    JavaSound.showInfoDialog();
                    ex.printStackTrace();
                } catch (Exception ex) { 
                    ex.printStackTrace();
                }
            }
        }


        public void meta(MetaMessage message) {
            if (message.getType() == 47) {  // 47 is end of track
                playB.setText("Play");
                recordB.setEnabled(true);
            }
        }


        public void saveMidiFile(File file) {
            try {
                int[] fileTypes = MidiSystem.getMidiFileTypes(sequence);
                if (fileTypes.length == 0) {
                    System.out.println("Can't save sequence");
                } else {
                    if (MidiSystem.write(sequence, fileTypes[0], file) == -1) {
                        throw new IOException("Problems writing to file");
                    } 
                }
            } catch (SecurityException ex) { 
                JavaSound.showInfoDialog();
            } catch (Exception ex) { 
                ex.printStackTrace(); 
            }
        }


        class TrackData extends Object {
            Integer chanNum; String name; Track track;
            public TrackData(int chanNum, String name, Track track) {
                this.chanNum = new Integer(chanNum);
                this.name = name;
                this.track = track;
            }
        } // End class TrackData
    } // End class RecordFrame


    public static void main(String args[]) {
        final MidiSynth midiSynth = new MidiSynth();
        midiSynth.open();
        JFrame f = new JFrame("Midi Synthesizer");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        f.getContentPane().add("Center", midiSynth);
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 760;
        int h = 470;
        f.setLocation(screenSize.width/2 - w/2, screenSize.height/2 - h/2);
        f.setSize(w, h);
        f.setVisible(true);
        
        
        //text box stuff
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        //javax.swing.SwingUtilities.invokeLater(new Runnable() {
          //  public void run() {
               // createAndShowGUI();
           // }
        //});

        
        
    }
   /* private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TextDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add contents to the window.
        frame.add(new TextDemo());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }*/
    

   

    public void actionPerformed(ActionEvent evt) {
        String text = textField.getText();
        textArea.append(text + newline);
        textField.selectAll();

        //Make sure the new text is visible, even if there
        //was a selection in the text area.
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
} 

