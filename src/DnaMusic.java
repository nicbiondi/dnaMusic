import javax.sound.midi.MidiChannel;

public class DnaMusic
{
	
  	//String dnaString = "ATCGAUUGACGUUGACUAGCGUAGCUAACUCGAGAUCGC";
  	/*
  	A 102 base pair sequence of the nicotinic acetylcholine receptor deltasubunit
  	gene confers regulation by muscle electrical activity
  	*/
  	private String Nic ="CACCCTCTCTGAGGTGTGGCAGTCTGTTCCCCCACCTGAAGGTGTCAGTTCGGGGGAAAAGCTCACCTCATAAATAGAATCACACATGTATAAAAGAAGCTATGTGTCACCACCCTCTGCCCCTTAGGCTTCTTTCCAAAAACCCCAG" +
  			"TCTGGGAATTAAAGGTGTTGTGATGGTTTGCACATGCTTGGCCCAGGGAGTGGCACTATTAGAAAGTGTGTCACTGTGGTGGTGGGCTTGGAGACCCTCCTCCTAGCTGCCTAAGGATGCTCCAGGTGAAGATGTAGAACTCAGCTCCTCCTGCACCATGCCGGCCTAGATGCTGTCATGCTTCTGAACCTGTAAGCCAGCCCCAATTAAATGTTGTCCTTTATAAAACTTACATTGGTCATAGACCTGACTAAGGCAGGTGTGCACTGCCACTCCTGGGTTAAGTGTAATATTTTTAAAGACAGGTACAGCACCAGCAAGATGGTGCAATAAGTAACAGTTGCCTGCTGTGACAAGCCTGGTAGAGAGAGAGAAAAACAACTGTCACACACACACACACACACACACACACACACCAATATAAATGATAAAATCGTCGAACAATAGACTGTTTGATTTCCACAGAACCACCTACAAACAGAACACATATATACACACTCCGTACACACATGTACACACTCCATACACATATACCACATGCAGCAGCTGCTAACATGTCTTTCACTTCAGTCCTTTAAGGACTGAAGTGGCAGTAACTGACCACAGCTACCAGCATGCCACACAGACACACAGAGACACATACACACACACCATTTGTCCCCTACCAGGCCCCACCCTACCAACTAGCAGTGGCAGTCCCTGCCTGGGATCTTTGCATTCTGCTGGCAAACCCCACCCTCTCATCACCAGCCTTCAAGTATC AGATTGCGTTTCCGGCCTCTGCCAGCACCTGTCCCCTTGCTTGCCTCATTCCACAGCCAACAGGCTGAAGGGGAGACATAGGGGATGGCAGGGCCTGTGCCCACACTGGGGCTGCTGGCTGCCCTTGTTG";
  	
  	//Salmonella typhimurium LeuO (leuO) gene, complete cds
  	private String Sal ="gcttttagtg gtactggata tgccgtttaa tgtcaactct attttctgtc gccattgcat"+
    "tttgttgacg atatccggat acgtacttta gctaattgtg ctgggctgcg cgtccaataa"+
    "aggataaaag aacgagtgga cattcatact tttcattgtt tgaaatattt aggcattttt"+
    "gtttgcaaat tggttagggg aatggcctgt taaaggtatg ctaaaggttt ggtttatctt"+
    "tttgatttgt tttatatttt aacaaataac cttgtttgct taatggataa ggataaagtt"+
    "attgataaat taatgttaac tttttagcta ttaataatat aaacatttaa ttgaaactta"+
    "aatgatcatt cacttggtgt atgattgtgt attcgccata gttatggtta tattgcttgc"+
    "gtaatttaaa cattatgaat cgcaatggtg tgactcagga cacattacgc ttgcgctggc"+
    "attctatttg tctctgtcag cgtctttatg ttttccgaat tttaacgctt tccctttttc"+
    "ttattttata tgcatggtaa atcatatttt caggaattat ttctgccttc agccagaaaa"+
    "agggagttaa gcgtgacagt ggagttaaat atgccagagg tcaaaaccga aaagccgcat"+
    "cttttagata tgggcaaacc acacgttcgc atggttgatt tgaacctatt gaccgtgttc"+
    "gatgcggtaa tgcaagagca gaatattacg cgcgccgccc acacgctggg aatgtcgcag"+
    "cctgcggtca gtaacgccgt agcgcgtctg gtggttatgt ttaatgacgt actttttgtt"+
    "cgatatggac gaattcagcc gactgcccgt gcatttcagt tatttggttc agtccgtcag"+
    "gcgttgcaat tggtgcaaaa tgaattgccg ggatcggggt ttgagccgac cagcagcgaa"+
    "cgtgtattca atctttgcgt gtgcagtccg ctggataata tcctgacgtc acagatttat"+
    "aatcgtgtag aaaaaattgc gccaaatatt catgtcgttt ttaaagcgtc gttgaatcag"+
    "aatactgagc atcagttacg ctatcaggaa accgagttcg ttattagtta tgaagaattc"+
    "cgtcgtcctg agtttaccag cgtaccgcta tttaaagatg aaatggtttt agtcgccagc"+
    "cgaaaacacc cgcgtattag cggcccgcta ctggaaggcg atgtttataa tgaacaacat"+
    "gcggttgttt ccctcgatcg ttatgcgtca tttagtcagc cgtggtatga cacgccggat"+
    "aaacagtcga gcgtggctta tcagggcatg gcgcttatca gcgttctgaa cgtggtttcg"+
    "cagacgcatt tggtcgctat tgccccgcgc tggctggcgg aagagtttgc ggaatcgctg"+
    "gatctgcaaa tattgccgtt gcctttaaaa ctgaatagcc ggacatgcta cctttcctgg"+
    "catgaagcgg ctgggcgtga taaagggcat caatggatgg aagatttatt agtctctgtt"+
    "tgtaagcgat aaaaccgggc agaataaatc agaaacaaat tctggtttat tctgcttttt"+
    "agcgtatatc aaaaatttta tgctgaatta acggtgagcg attttttcga cgctgtgaaa"+
    "aatagatgaa ttgctgtaag tctgcattta taagcgatta tttattacca ggtgtgattc"+
    "agacggagta tttctccatt tcttctcttc ttgcctcctg attattctaa ccttccccat"+
    "ctttacgtca ttccacccaa ttgccaggcg tgatagcgag cggttaaggt gtccgtc";
  	
  	//Uncultured bacterium clone God-47 16S ribosomal RNA gene, partial sequence
     private String god = "tagggaatct tcggcaatgg gcgaaagcct gaccgagcaa cgccgcgtga atgaagaagg" +
     "ccttcgggtt gtaaaattct gttataaggg aagaaaggtg ataggaggaa atgactatca" +
     "attgacggta ccttatgaga aagccacggc taactacgtg ccagcagccg cggtaatacg" +
     "taggtggcaa gcgttatccg gaattattgg gcgtaaagag cgcgcaggtg gttaattaag" +
     "tctgatgtga aagcccacgg cttaaccgtg gagggtcatt ggaaactggt tgacttgagt" +
     "gcagaagagg gaagtggaat tccatgtgta gcggtgaaat gcgtagagat atggaggaac" +
     "accagtggcg aaggcggctt cctggtctgc aactgacact gaggcgcgaa agcgtgggga" +
     "gcaaacag";
	
	
	
  public void processDNA(String dnaString, int speed)
  {
  
  	dnaString=Sal; //set current base-pair string to use
  	//format the dna string 
  	dnaString=dnaString.replaceAll(" ", "").toUpperCase();
  	
  	String tmp = "";
  	int codonValue;
  	int base = 4;
  	int notes = 1; // how many notes do you want to play at the same time.
  	int shift = 28; //this number represents the starting note for the song.  28 is dead center for middle c.
  	int chord = 0; // this is a counter, don't change it.
  	for(int i=0;i<dnaString.length();i+=3)
  	{
  	  chord++;
  	  tmp = dnaString.substring(i, i+3);
  	  //parse codon into it's numerical value
  	  tmp=tmp.replaceAll("T","0");
  	  tmp=tmp.replaceAll("U","0");
  	  tmp=tmp.replaceAll("C","1");
  	  tmp=tmp.replaceAll("A","2");
  	  tmp=tmp.replaceAll("G","3");
  	  
  	  //convert to base 10
  	  codonValue = 0;
  	  codonValue += Integer.parseInt(tmp.substring(0, 1))*base*base;
  	  codonValue += Integer.parseInt(tmp.substring(1, 2))*base;
  	  codonValue += Integer.parseInt(tmp.substring(2, 3));
  	 // cc.channel.noteOn(codonValue+shift, cc.velocity);           
  	  
  	//  channel.noteOn((int)(Math.random()*63+shift), speed);
  	  
  	  //createShortEvent(NOTEON, codonValue);
  	 // Key key = (Key)keys.get(codonValue);
  	 // key.on();
  	 //    repaint();
  	  try {
  		  if(chord==notes)
  		  {
  			chord = 0;
			    Thread.sleep(50+speed*10);
  		  }
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
  	}
  	 
  	 	

	  
  }
}