package grafo;

//import java.awt.Dimension;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;

import com.opencsv.CSVWriter;
import grafo.controller.TraceController;
import grafo.io.IO_Handler;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import javax.swing.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Classe che permette di analizzare e confrontare files di Log (XES). <br>
 * Vengono creati dei Grafi riconducibili ad una lista di Nodi/attivit� e una lista di Archi/Transizioni <br>
 * Nodi e Archi possono essere Repeating o Not Repeating (ripetendosi pi� di una volta per traccia o meno) <br>
 * Ogni grafo rappresenta un Log, riassumendo l'analisi di tutte le tracce. <br>
 * I grafi generati vengono cos� comparati e come output viene data una DistanceMatrix in CSV. <br>
 * <br>
 * per funzionare, dopo aver creato un Oggetto di tipo LogUtilsRepeatingGraph, va eseguito il main <br>
 * oppure vanno eseguiti i seguenti passaggi: <br>
 * <br>
 * 1) selectFolder() o in alternativa setFileList() <br>
 * <br>
 * setTraceNum() (opzionale) <br>
 * setAvgTraceLen() (opzionale) <br>
 * <br>
 * 2) startMenu() o in alternativa importare setScoreChange(true) oppure setScoreChange(false). Nel primo caso impostare anche: <br>
 * setGamma() <br>
 * setNodeEqualsScore() <br>
 * setNodeNotEqualScore() <br>
 * setNodeSemiScore() <br>
 * setEdgeEqualsScore() <br>
 * setEdgeNotEqualsScore() <br>
 * setEdgeSemiScore() <br>
 * <br>
 * 3) analyzeTraces() <br>
 * 4) generateDistanceMatrix() <br>
 * 5) convertToCSV() <br>
 *
 * @author luigi.bucchicchioAtgmail.com
 */
public class LogUtilsRepeatingGraph {

    private File[] fileList;
    private int[] traceNum;
    private double[] avgTraceLen;
    private List<Graph> graphList = new ArrayList<Graph>();
    private String[][] graphDissimilarity;
    private String outputFileName = new String("");

    //scores
    private boolean scoreChange = false;
    private double edgeEqualScore = (double) 1.0;
    private double edgeSemiScore = (double) 0.0;
    private double edgeNotEqualScore = (double) 0.0;
    private double nodeEqualScore = (double) 1.0;
    private double nodeSemiScore = (double) 0.0;
    private double nodeNotEqualScore = (double) 0.0;
    private double gamma = (double) 0.0;

    //
    private boolean isTreCifre = false;

    static double startingTime;

//	static int slider=0;
//	
//	static int slider1=0;
//	static int slider2=0;
//	static int slider3=0;
//	static int slider4=0;
//	static int slider5=0;
//	static int slider6=0;
//	

    /**
     * XES parser
     *
     * @param filePath
     * @return XLog structured file
     * @throws Exception
     */
    public XLog parseXES(String filePath) throws Exception {
        XesXmlParser parser = new XesXmlParser();
        return parser.parse(new File(filePath)).get(0);
    }

    /**
     * L'algoritmo analizza le tracce di ogni singolo XES, costruendosi una lista di Grafi che rappesentano ognuno un Log. Click per dettagli  <br>
     * <br>
     * le tracce vengono lette in due diversi modi:<br>
     * come TraceLine, in una singola stringa: es. "ABBBABBABABA" "a1a2a3a4a5a1a2a3" "activity1activity2activity3"<br>
     * come Sequenza di attivit� in una lista: es. "[ A,B,B,B,.....] [a1,a2,a3...] [activity1,activity2....]<br>
     * sebbene si utilizzi prettamente il secondo metodo di lettura, alcuni algortimi sulle stringhe possono richiedere la TraceLine<br>
     * <br>
     * Info aggiuntive come il log di provenienza e l'id della traccia vengono salvate anche se non vengono usate<br>
     * Tutte le info vengono salvate mediante una classe di supporto, Trace, che confluisce in una traceList che viene passata all'analyzer<br>
     * l'analyzer eseguir� LogAnalyze() creando un Graph, ottenibile con getGraph() e aggiunto in una lista di Grafi.<br>
     *
     * @author luigi.bucchicchioAtgmail.com
     */
    public void analyzeTraces() {
        System.out.println("Starting to analyze traces");
        startingTime = System.currentTimeMillis();

        int gram = Integer.parseInt(IO_Handler.requireInput("Please give me the n for grams: "));

        for (int i = 0; i < fileList.length; i++) {
            File file = fileList[i];

            List<Trace> traceList = new ArrayList<Trace>();

            try {
                XLog xlog = parseXES(file.getAbsolutePath());
                //da cippus lives
                //XLog xlog=parseXES("/home/cippus/Downloads/log (5).xes");

                for (XTrace xTrace : xlog) {
                    ArrayList<String> activitySequence = new ArrayList<String>();
                    StringBuffer traceLine = new StringBuffer("");
                    // activitySequence = [t11, t45, t63, t12, t113, t9]
                    // traceLine = t11t45t63t12t113t9

                    for (XEvent xevent : xTrace) {
                        String activity = xevent.getAttributes().get("concept:name").toString();
                        if (isTreCifre)
                            activity = activity.substring(0, 3);
                        traceLine.append(activity);
                        activitySequence.add(activity);
                    }

                    Trace genericTrace = new Trace();
                    genericTrace.setTraceLine(traceLine.toString());
                    genericTrace.setActivitySequence(activitySequence);
                    genericTrace.setLogId(fileList[i].getName());
                    genericTrace.setTraceId(xTrace.getAttributes().get("concept:name").toString());
                    traceList.add(genericTrace);
                    genericTrace.setGrams(TraceController.generateGrams(gram, genericTrace.getActivitySequence()));
                    //analyzer.setTrace(traceLine.toString());
                }

                GraphLogAnalyzer analyzer = new GraphLogAnalyzer();
                analyzer.setTraceSet(traceList);
                analyzer.LogAnalyze();
                graphList.add(analyzer.getGraph());


                // Useful Info, can be used in combo with "consoleOutToFile()" (see main comment)

//				System.out.println("--------------------------- Node list of log graph:"+fileList[i].getName()+""
//						+ "---------------------------");
//				analyzer.printNodeSet();
//				System.out.println("--------------------------- Edge list of log graph:"+fileList[i].getName()+""
//						+ "---------------------------");
//				analyzer.printEdgeSet();
//				System.out.println("---------------------------"
//						+ "---------------------------\n");

                // GraphImage has Heavy RAM usage-> (use with nested folder + batch).equals("YOUR PC GOT NUKED") returned "true".

//				analyzer.GraphImage("Log "+fileList[i].getName()+" graph");

            } catch (Exception e) {
                e.printStackTrace();
            }
            int numeroTracce = traceList.size();
            int tot = 0;
            for (Trace trace : traceList) {
                tot = tot + trace.getTraceLength();
            }
            ;


            getAvgTraceLen()[i] = (double) tot / numeroTracce;
            getTraceNum()[i] = numeroTracce;

        }
        System.out.println("Traces analyzed");
    }

    /**
     * In output, il CSV con il nome formattato come segue:<br>
     * DistanceGraph_nomeCartella_numeroFilesLogs_gamma_parametriNodiOAttivit�_parametriArchiOTransizioni.csv
     *
     * @param data La distanceMatrix
     * @author luigi.bucchicchioAtgmail.com
     */
    public void convertToCSV(String[][] data) {
        int nLog = fileList.length;
        String directoryName = fileList[0].getParentFile().getName();

        String Sgamma = String.valueOf(gamma);
        Sgamma = Sgamma.replace(".0", "");
        Sgamma = Sgamma.replace(".", "");
        String s1 = String.valueOf(nodeEqualScore);
        s1 = s1.replace(".0", "");
        s1 = s1.replace(".", "");
        String s2 = String.valueOf(nodeNotEqualScore);
        s2 = s2.replace(".0", "");
        s2 = s2.replace(".", "");
        String s3 = String.valueOf(nodeSemiScore);
        s3 = s3.replace(".0", "");
        s3 = s3.replace(".", "");
        String s4 = String.valueOf(edgeEqualScore);
        s4 = s4.replace(".0", "");
        s4 = s4.replace(".", "");
        String s5 = String.valueOf(edgeNotEqualScore);
        s5 = s5.replace(".0", "");
        s5 = s5.replace(".", "");
        String s6 = String.valueOf(edgeSemiScore);
        s6 = s6.replace(".0", "");
        s6 = s6.replace(".", "");

        // TUTT' STUBBURDELL' PE' RINOMINA' L'OUTPUT

        if (getOutputFileName().equals(""))
            setOutputFileName("DistanceGraph_" + directoryName + "_" + nLog + "Logs_gamma" + Sgamma + "_" + s1 + s2 + s3 + "_" + s4 + s5 + s6 + ".csv");

        try {
            //cartellina
            File f = new File("output");
            f.mkdir();
            File csvFile = new File(f.getAbsolutePath() + "\\" + getOutputFileName());
            //scriba, pls
            CSVWriter writer = new CSVWriter(new FileWriter(csvFile));

            for (String[] array : data) {
                writer.writeNext(array);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Selettore di cartella
     *
     * @author luigi.bucchicchioAtgmail.com
     */
    public void selectFolder() {
        JFileChooser chooser = new JFileChooser(".");

        System.out.println("\u2705 " + "Please select the folder containing the XES Files" + " \u2705");

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this folder: " + chooser.getSelectedFile().getName());
        }

        // Ce manca l'else con scritto "TI SI SBAGLIATO, FRA?"

        File folder = new File(chooser.getSelectedFile().getAbsolutePath());

        this.fileList = folder.listFiles();
        int x = fileList.length;
        setTraceNum(new int[x]);
        setAvgTraceLen(new double[x]);

    }

    /**
     * Da invocare dopo analyzeTraces(), utilizza la lista di Grafi (ognuno rappresentante un Log) per generare la distance Matrix. Click per dettagli <br>
     * Il metodo utilizza la classe GraphComparator per generare la differenza tra due Grafi (due Log).<br>
     * Il punteggio risultante � la Similarit� in percentuale, poi convertita in Dissimilarit� o Distanza.<br>
     * Viene eseguito a due a due sulla lista di Grafi.<br>
     *
     * @return la Dissimilarity/Distance Matrix
     * @throws RuntimeException
     * @author luigi.bucchicchioAtgmail.com
     */
    public String[][] generateDistanceMatrix() throws RuntimeException {

        System.out.println("Starting the generation of the Distance Matrix");

        if (graphList.size() == 0) {
            throw new RuntimeException("invalid procedure: No Graph Found");
        }

        graphDissimilarity = new String[graphList.size()][graphList.size()];

        for (int i = 0; i < graphList.size(); i++) {
            Graph graph1 = graphList.get(i);
            // j=0
            for (int j = 0; j < graphList.size(); j++) {
                Graph graph2 = graphList.get(j);
                if (j == i)
                    graphDissimilarity[i][j] = "0.0";
                else {
                    if (graphDissimilarity[i][j] == null) {
                        GraphComparator comp = new GraphComparator();

                        if (scoreChange) {
                            comp.setEdgeEqualScore(edgeEqualScore);
                            comp.setEdgeNotEqualScore(edgeNotEqualScore);
                            comp.setEdgeSemiScore(edgeSemiScore);
                            comp.setNodeEqualScore(nodeEqualScore);
                            comp.setNodeNotEqualScore(nodeNotEqualScore);
                            comp.setNodeSemiScore(nodeSemiScore);
                        }

                        comp.setGraph1(graph1);
                        comp.setGraph2(graph2);
                        comp.setLogUtilsGamma(gamma);
                        Double metrics = (comp.getMetrics(gamma)) * 100;
                        // trasformazione da SIMILARITY a DISSIMILARITY/DISTANCE

                        metrics = 100 - metrics;

                        DecimalFormat df = new DecimalFormat("#.00");
                        graphDissimilarity[i][j] = String.valueOf(df.format(metrics));
                        graphDissimilarity[j][i] = String.valueOf(df.format(metrics));
                    }
                }
            }
        }

        // Adding the Header with LOG files names

        String[][] distanceMatrix = new String[graphDissimilarity.length + 1][graphDissimilarity.length + 1];

        for (int i = 0; i < distanceMatrix.length; i++) {
            for (int j = 0; j < distanceMatrix.length; j++) {
                if (j == i) {
                    distanceMatrix[i][j] = "0.0";
                } else {
                    if (distanceMatrix[i][j] == null) {
                        if (i == 0) {
                            File f = fileList[j - 1];
                            String filename = f.getName();
                            int extension = filename.lastIndexOf('.');
                            String nameOnly = filename.substring(0, extension);
                            distanceMatrix[i][j] = nameOnly;
                            distanceMatrix[j][i] = nameOnly;
                        } else {
                            distanceMatrix[i][j] = graphDissimilarity[i - 1][j - 1];
                            distanceMatrix[j][i] = graphDissimilarity[j - 1][i - 1];
                        }
                    }
                }
            }
        }
        distanceMatrix[0][0] = " ";
        System.out.println("Ended generation of Distance Matrix");
        return distanceMatrix;
    }

    /**
     * il metodo chiede se si vogliono cambiare i parametri o tenere quelli di default, considerando che nel confronto esistono: Click per dettagli <br>
     * Nodi/Attivit� o Archi/Transizioni NotRepeating (che appaiono una volta sola per singola traccia) A <br>
     * Nodi/Attivit� o Archi/Transizioni Repeating (cha appaiono pi� di una volta per singola traccia) A_R <br>
     * i parametri sono: <br>
     * Gamma, ovvero il peso tra archi/transizioni e Nodi/Attivit�, che va da 0.0 (solo archi) a 1.0 (solo nodi) <br>
     * Equal score, ovvero il punteggio che si vuole dare tra Nodi e Archi Uguali tra loro (es. A con A = 1.0; A_R con A_R = 1.0) <br>
     * Not Equal score, ovvero il punteggio che si vuole dare tra Nodi e Archi Disuguali(presente/mancante) tra loro (es A con null = 0.0) <br>
     * Repeating score, ovvero il putneggio che si vuole dare quando, nel confronto, uno � Repeating e l'altro � NotRepeating (es. A con A_R = 0.5) <br>
     *
     * @param tastiera Lo Scanner, generalmente con System.in
     * @author luigi.bucchicchioAtgmail.com
     */
    public void startMenu(Scanner tastiera) {

        String input = null;
        double a = (double) 0.0;

        do {
            System.out.println("Change the gamma value (default " + gamma + ")? <<y>> or <<n>>");
            input = tastiera.nextLine();
        } while ((!input.equals("y")) && (!input.equals("n")));

        if (input.equals("y")) {
            System.out.println("\u2705 " + "Please insert the value of Gamma in a range between 0.0 and 1.0" + " \u2705");
            a = Double.valueOf(tastiera.nextLine());
        }

        input = null;
        do {
            System.out.println("Change the Score settings? <<y>> or <<n>>");
            input = tastiera.nextLine();
        } while ((!input.equals("y")) && (!input.equals("n")));

        if (input.equals("y")) {
            this.scoreChange = true;

            double newScore = (double) 1.0;
            System.out.println("\u2705 " + "Insert the Node_Equal score (default " + nodeEqualScore + ")" + " \u2705");
            newScore = Double.valueOf(tastiera.nextLine());
            this.nodeEqualScore = newScore;

            newScore = (double) 0.0;
            System.out.println("\u2705 " + "Insert the Node_NOT_Equal score (default " + nodeNotEqualScore + ")" + " \u2705");
            newScore = Double.valueOf(tastiera.nextLine());
            this.nodeNotEqualScore = newScore;

            newScore = (double) 0.0;
            System.out.println("\u2705 " + "Insert the Node_Semi_Equal score (default " + nodeSemiScore + ")" + " \u2705");
            newScore = Double.valueOf(tastiera.nextLine());
            this.nodeSemiScore = newScore;

            newScore = (double) 1.0;
            System.out.println("\u2705 " + "Insert the Edge_Equal score (default " + edgeEqualScore + ")" + " \u2705");
            newScore = Double.valueOf(tastiera.nextLine());
            this.edgeEqualScore = newScore;

            newScore = (double) 0.0;
            System.out.println("\u2705 " + "Insert the Edge_NOT_Equal score (default " + edgeNotEqualScore + ")" + " \u2705");
            newScore = Double.valueOf(tastiera.nextLine());
            this.edgeNotEqualScore = newScore;

            newScore = (double) 0.0;
            System.out.println("\u2705 " + "Insert the Edge_Semi_Equal score (default " + edgeSemiScore + ")" + " \u2705");
            newScore = Double.valueOf(tastiera.nextLine());
            this.edgeSemiScore = newScore;
        }
        this.gamma = a;

    }

//private class Change implements ChangeListener {
//	private int sliderNumber=0;
//	
//	private Change(int x) {
//		this.sliderNumber=x;
//	}
//	
//	private Change() {
//	}
//	
//	public void stateChanged(ChangeEvent e) {
//	    JSlider source = (JSlider)e.getSource();
//	    if (!source.getValueIsAdjusting()) {
//	        int x = (int)source.getValue();
//	        if(sliderNumber==0)
//	        LogUtilsRepeatingGraph.slider=x;
//	        else if (sliderNumber==1)
//	        LogUtilsRepeatingGraph.slider1=x;
//	        else if (sliderNumber==2)
//		        LogUtilsRepeatingGraph.slider2=x;
//	        else if (sliderNumber==3)
//		        LogUtilsRepeatingGraph.slider3=x;
//	        else if (sliderNumber==4)
//		        LogUtilsRepeatingGraph.slider4=x;
//	        else if (sliderNumber==5)
//		        LogUtilsRepeatingGraph.slider5=x;
//	        else if (sliderNumber==6)
//		        LogUtilsRepeatingGraph.slider6=x;
//	    }
//	}
//}
//
//private class ButtonListener implements ActionListener {
//	
//	JPanel panel;
//	JFrame frame;
//	
//	private ButtonListener(JPanel panel,JFrame frame){
//		this.panel=panel;
//		this.frame=frame;
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent e) {
//		panel = new JPanel();
//		panel.setVisible(false);
//		frame.setVisible(false);
//	}
//}

//public void visualStartMenu() throws InterruptedException {
//
//	JPanel panel = new JPanel();
//	JFrame frame = new JFrame();
//	panel.setPreferredSize(new Dimension(500,500));
//	panel.setVisible(false);
//	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	frame.add(panel);
//	frame.pack();
//	frame.setLocationRelativeTo(null);
//	frame.setVisible(false);
//
//	String[] options = {"Yes","No"};
//	int n = JOptionPane.showOptionDialog(frame,
//			"Would you like to change the gamma value?",
//			"Gamma Value",
//			JOptionPane.YES_NO_OPTION,
//			JOptionPane.QUESTION_MESSAGE,
//			null,     //do not use a custom Icon
//			options,  //the titles of buttons
//			options[0]); //default button title
//
//	if(n==JOptionPane.YES_OPTION) {
//		panel.setVisible(true);
//		frame.setVisible(true);
//
//		final int GAMMA_MIN = 0;
//		final int GAMMA_MAX = 100;
//		final int GAMMA_INIT = 0;
//		JSlider gammaSlider = new JSlider(JSlider.HORIZONTAL,
//				GAMMA_MIN, GAMMA_MAX, GAMMA_INIT);
//		gammaSlider.addChangeListener(new Change());
//		//Turn on labels at major tick marks.
//		gammaSlider.setMajorTickSpacing(10);
//		gammaSlider.setMinorTickSpacing(10);
//		gammaSlider.setPaintTicks(true);
//		gammaSlider.setPaintLabels(true);
//		gammaSlider.setPreferredSize(new Dimension(200,150));
//		panel.add(gammaSlider);
//		JButton button = new JButton("OK");
//		button.setVisible(true);
//		panel.add(button);
//		frame.pack();
//		button.addActionListener(new ButtonListener(panel,frame));
//
//		while(frame.isVisible()) {
//			Thread.sleep(1000);
//		}
//
//		this.gamma = slider/100;
//	}
//
//	frame.dispose();
//	panel = new JPanel();
//	frame = new JFrame();
//	panel.setPreferredSize(new Dimension(500,800));
//	panel.setVisible(false);
//	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	frame.add(panel);
//	frame.pack();
//	frame.setLocationRelativeTo(null);
//	frame.setVisible(false);
//
//	n = JOptionPane.showOptionDialog(frame,
//			"Would you like to change the scores?",
//			"Change scores",
//			JOptionPane.YES_NO_OPTION,
//			JOptionPane.QUESTION_MESSAGE,
//			null,     //do not use a custom Icon
//			options,  //the titles of buttons
//			options[0]); //default button title
//
//	if(n==JOptionPane.YES_OPTION) {
//		this.scoreChange=true;
//
//		panel.setPreferredSize(new Dimension(500,800));
//		panel.setVisible(true);
//		frame.setVisible(true);
//
//		final int SCORE_MIN = 0;
//		final int SCORE_MAX = 100;
//		final int SCORE_INIT = 0;
//		JSlider n_eqSlider = new JSlider(JSlider.HORIZONTAL,
//				SCORE_MIN, SCORE_MAX, SCORE_INIT);
//		n_eqSlider.addChangeListener(new Change(1));
//		//Turn on labels at major tick marks.
//		n_eqSlider.setMajorTickSpacing(10);
//		n_eqSlider.setMinorTickSpacing(10);
//		n_eqSlider.setPaintTicks(true);
//		n_eqSlider.setPaintLabels(true);
//		n_eqSlider.setPreferredSize(new Dimension(200,50));
//		JSlider n_neqSlider = new JSlider(JSlider.HORIZONTAL,
//				SCORE_MIN, SCORE_MAX, SCORE_INIT);
//		n_neqSlider.addChangeListener(new Change(2));
//		//Turn on labels at major tick marks.
//		n_neqSlider.setMajorTickSpacing(10);
//		n_neqSlider.setMinorTickSpacing(10);
//		n_neqSlider.setPaintTicks(true);
//		n_neqSlider.setPaintLabels(true);
//		n_neqSlider.setPreferredSize(new Dimension(200,50));
//		JSlider n_seqSlider = new JSlider(JSlider.HORIZONTAL,
//				SCORE_MIN, SCORE_MAX, SCORE_INIT);
//		n_seqSlider.addChangeListener(new Change(3));
//		//Turn on labels at major tick marks.
//		n_seqSlider.setMajorTickSpacing(10);
//		n_seqSlider.setMinorTickSpacing(10);
//		n_seqSlider.setPaintTicks(true);
//		n_seqSlider.setPaintLabels(true);
//		n_seqSlider.setPreferredSize(new Dimension(200,50));
//		JSlider e_eqSlider = new JSlider(JSlider.HORIZONTAL,
//				SCORE_MIN, SCORE_MAX, SCORE_INIT);
//		e_eqSlider.addChangeListener(new Change(4));
//		//Turn on labels at major tick marks.
//		e_eqSlider.setMajorTickSpacing(10);
//		e_eqSlider.setMinorTickSpacing(10);
//		e_eqSlider.setPaintTicks(true);
//		e_eqSlider.setPaintLabels(true);
//		e_eqSlider.setPreferredSize(new Dimension(200,50));
//		JSlider e_neqSlider = new JSlider(JSlider.HORIZONTAL,
//				SCORE_MIN, SCORE_MAX, SCORE_INIT);
//		e_neqSlider.addChangeListener(new Change(5));
//		//Turn on labels at major tick marks.
//		e_neqSlider.setMajorTickSpacing(10);
//		e_neqSlider.setMinorTickSpacing(10);
//		e_neqSlider.setPaintTicks(true);
//		e_neqSlider.setPaintLabels(true);
//		e_neqSlider.setPreferredSize(new Dimension(200,50));
//		JSlider e_seqSlider = new JSlider(JSlider.HORIZONTAL,
//				SCORE_MIN, SCORE_MAX, SCORE_INIT);
//		e_seqSlider.addChangeListener(new Change(6));
//		//Turn on labels at major tick marks.
//		e_seqSlider.setMajorTickSpacing(10);
//		e_seqSlider.setMinorTickSpacing(10);
//		e_seqSlider.setPaintTicks(true);
//		e_seqSlider.setPaintLabels(true);
//		e_seqSlider.setPreferredSize(new Dimension(200,50));
//
//		JButton button = new JButton("OK");
//		button.addActionListener(new ButtonListener(panel,frame));
//
//
//		panel.add(n_eqSlider);
//		panel.add(n_neqSlider);
//		panel.add(n_seqSlider);
//		panel.add(e_eqSlider);
//		panel.add(e_neqSlider);
//		panel.add(e_seqSlider);
//		panel.add(button);
//		panel.setVisible(true);
//		frame.pack();
//		button.setVisible(true);
//
//		while(frame.isVisible()) {
//			Thread.sleep(1000);
//		}
//
//		this.nodeEqualScore = slider1/100;
//		this.nodeNotEqualScore = slider2/100;
//		this.nodeSemiScore= slider3/100;
//		this.edgeEqualScore = slider4/100;
//		this.edgeNotEqualScore = slider5/100;
//		this.edgeSemiScore = slider6/100;
//	}
//
//	frame.dispose();
//
//}

    /**
     * L'algoritmo esegue: Clicca per dettagli <br>
     * selectFolder() per prendere la cartella degli XES <br>
     * startMenu() per impostare i parametri <br>
     * analyzeTraces() per analizzare le tracce e salvare i rispettivi Grafi (ovvero Set di Nodi/Attivit� e Set di Archi/Transizioni) per ogni Log. <br>
     * generateDistanceMatrix() per generare una distanceMatrix con l'aiuto di GraphComparator <br>
     * convertToCSV() per l'output <br>
     *
     * @param args
     * @throws InterruptedException
     * @author luigi.bucchicchioAtgmail.com
     */
    public static void main(String[] args) throws InterruptedException {

        Locale.setDefault(Locale.US);
        System.out.println("Log evaluation - ");
        LogUtilsRepeatingGraph log = new LogUtilsRepeatingGraph();
        log.selectFolder();
        Scanner tastiera = new Scanner(System.in);
        log.startMenu(tastiera);
//		log.visualStartMenu();

        //log.consoleOutToFile();

        log.analyzeTraces();

        String[][] distanceMatrix = log.generateDistanceMatrix();

        log.convertToCSV(distanceMatrix);

        System.out.println("Execution Time:" + String.valueOf(System.currentTimeMillis() - startingTime));

        System.out.println("Use the file " + log.getOutputFileName() + " in output directory to make clusters\n");
//	

        log.generateNodeListReport("CUSTOM");

    }

    /**
     * Metodo per impostare L'output della console su file .TXT
     *
     * @author luigi.bucchicchioAtgmail.com
     */
    public void consoleOutToFile() {
        PrintStream fileOut;
        try {
            fileOut = new PrintStream("./ConsoleOutput.txt");
            System.setOut(fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stringa del nome del file di output
     *
     * @return output file's name
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * Settaggio del nome del file di output
     *
     * @param outputFileName
     */
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    /**
     * array dei file XES
     *
     * @return the array of XES files
     * @author luigi.bucchicchioAtgmail.com
     */
    public File[] getFileList() {
        return fileList;
    }

    /**
     * settaggio array di file XES
     *
     * @param fileList the list of XES files, expecting a File[]
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setFileList(File[] fileList) {
        this.fileList = fileList;
    }

    /**
     * settaggio valore booleano per punteggio custom
     *
     * @return true is the score is custom, false to defaults
     * @author luigi.bucchicchioAtgmail.com
     */
    public boolean isScoreChange() {
        return scoreChange;
    }

    /**
     * valore booleano per punteggio custom
     *
     * @param scoreChange set True if the score is Custom, false to defaults
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setScoreChange(boolean scoreChange) {
        this.scoreChange = scoreChange;
    }

    /**
     * punteggio equals tra archi
     *
     * @return the score used comparing two edges both repeating or both notRepeating (A_>B with A_>B) def. 1.0
     * @author luigi.bucchicchioAtgmail.com
     */
    public double getEdgeEqualScore() {
        return edgeEqualScore;
    }

    /**
     * settaggio punteggio equals tra archi
     *
     * @param edgeEqualScore the score used comparing two edges both repeating or both notRepeating (A_>B with A_>B) def. 1.0
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setEdgeEqualScore(double edgeEqualScore) {
        this.edgeEqualScore = edgeEqualScore;
    }

    /**
     * punteggio semiEquals tra archi
     *
     * @return the score used comparing two edges, one repeating and one notRepeating (A_>B,R with A_>B) def. 0.5
     * @author luigi.bucchicchioAtgmail.com
     */
    public double getEdgeSemiScore() {
        return edgeSemiScore;
    }

    /**
     * settaggio punteggio semiEquals tra archi
     *
     * @param edgeSemiScore the score used comparing two edges, one repeating and one notRepeating (A_>B,R with A_>B) def. 0.5
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setEdgeSemiScore(double edgeSemiScore) {
        this.edgeSemiScore = edgeSemiScore;
    }

    /**
     * punteggio disuguali tra archi
     *
     * @return the score used comparing two edges, one existing with one not existing (A_>B with null) def 0.0
     * @author luigi.bucchicchioAtgmail.com
     */
    public double getEdgeNotEqualScore() {
        return edgeNotEqualScore;
    }

    /**
     * settaggio punteggio disuguali tra archi
     *
     * @param edgeNotEqualScore the score used comparing two edges, one existing with one not existing (A_>B with null) def 0.0
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setEdgeNotEqualScore(double edgeNotEqualScore) {
        this.edgeNotEqualScore = edgeNotEqualScore;
    }

    /**
     * punteggio equals tra nodi
     *
     * @return the nodeEqualScore() the score used comparing two nodes, both reapeating or both notRepeating (es. A with A) def. 1.0
     * @author luigi.bucchicchioAtgmail.com
     */
    public double getNodeEqualScore() {
        return nodeEqualScore;
    }

    /**
     * settaggio punteggio equals tra nodi
     *
     * @param nodeEqualScore the score used comparing two nodes, both reapeating or both notRepeating (es. A with A) def. 1.0
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setNodeEqualScore(double nodeEqualScore) {
        this.nodeEqualScore = nodeEqualScore;
    }

    /**
     * punteggio semiEquals tra nodi
     *
     * @return the node SemiScore the score used comparing two nodes, one repeating and one notRepeating (es. A with A_R) def. 0.5
     * @author luigi.bucchicchioAtgmail.com
     */
    public double getNodeSemiScore() {
        return nodeSemiScore;
    }

    /**
     * settaggio punteggio semiEquals tra nodi
     *
     * @param nodeSemiScore the score used comparing two nodes, one repeating and one notRepeating (es. A with A_R) def. 0.5
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setNodeSemiScore(double nodeSemiScore) {
        this.nodeSemiScore = nodeSemiScore;
    }

    /**
     * punteggio disuguali tra nodi
     *
     * @return the node notEqualsScore the score used comparing two nodes, one existing and one not existing (es. A with null) def. 0.0
     * @author luigi.bucchicchioAtgmail.com
     */
    public double getNodeNotEqualScore() {
        return nodeNotEqualScore;
    }

    /**
     * settaggio punteggio diseuguali tra nodi
     *
     * @param nodeNotEqualScore the score used comparing two nodes, one existing and one not existing (es. A with null) def. 0.0
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setNodeNotEqualScore(double nodeNotEqualScore) {
        this.nodeNotEqualScore = nodeNotEqualScore;
    }

    /**
     * parametro gamma
     *
     * @return the gamma value, should be between 0.0 and 1.0
     * @author luigi.bucchicchioAtgmail.com
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * settaggio parametro gamma
     *
     * @param gamma between 0.0 and 1.0, representing the weigth between the Edges/Transitions (0.0 edges only) and the Nodes/Activities (1.0 nodes only)
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getStartingTime() {
        return startingTime;
    }

    public static void setStartingTime(double startingTime) {
        LogUtilsRepeatingGraph.startingTime = startingTime;
    }

    /**
     * Lista dei grafi (rappresentano uno XES ognuno)
     *
     * @return a List of Graphs, each generated from a XES file.
     * @author luigi.bucchicchioAtgmail.com
     */
    public List<Graph> getGraphList() {
        return graphList;
    }

    /**
     * Set della Lista dei grafi (se gi� computata)
     *
     * @param graphList, List of type Graph
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setGraphList(List<Graph> graphList) {
        this.graphList = graphList;
    }

    /**
     * DissimilarityMatrix (versione non formattata e non completa della distance matrix)
     *
     * @return the Dissimilarity Matrix, in String[][] format.
     * @author luigi.bucchicchioAtgmail.com
     */
    public String[][] getGraphDissimilarity() {
        return graphDissimilarity;
    }

    /**
     * array del numero di tracce dei file XES
     * <p>
     * TODO: It doesn't work, it only contains an array with the length of the total traces
     *          in each cell there is the same value
     *
     * @return the integer array representing the number of traces for each xes file
     * @author luigi.bucchicchioAtgmail.com
     */
    public int[] getTraceNum() {
        return traceNum;
    }

    /**
     * array della dimensione media delle tracce dei file XES
     *
     * @return the double array representing the average trace length for each xes file
     * @author luigi.bucchicchioAtgmail.com
     */
    public double[] getAvgTraceLen() {
        return avgTraceLen;
    }

    /**
     * Set Average Trace Length for each XES file.
     *
     * @param avgTraceLen in the setup, Expecting (new double[x]) where x is the number of XES files. Require an already defined double[x] otherwise
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setAvgTraceLen(double[] avgTraceLen) {
        this.avgTraceLen = avgTraceLen;
    }

    /**
     * Set Number of Traces for each XES file.
     *
     * @param traceNum in the setup, Expecting (new int[x]) where x is the number of XES files. Require an already defined int[x] otherwise
     * @author luigi.bucchicchioAtgmail.com
     */
    public void setTraceNum(int[] traceNum) {
        this.traceNum = traceNum;
    }

    public void generateNodeListReport(String etichettaDataset) {

        List<String[]> rows = new ArrayList<String[]>();

        List<Graph> graphsList = getGraphList();
        Iterator<Graph> graphIterator = graphsList.iterator();
        int index = 0;
        while (graphIterator.hasNext()) {
            Graph g = graphIterator.next();
            Iterator<Node> nodeList = g.nodes().iterator();
            while (nodeList.hasNext()) {
                Node n = nodeList.next();
                String id = n.getId();
                String repeatingString = (String) n.getAttribute("ui.label");
                boolean isRepeating = false;
                if (repeatingString.length() > 1) {
                    if (repeatingString.charAt(1) == '_')
                        isRepeating = true;
                }
                String logName = getFileList()[index].getName();
                String[] row = new String[6];
                row[0] = id;
                row[1] = logName;
                row[2] = etichettaDataset;
                row[3] = "null";
                if (isRepeating)
                    row[4] = String.valueOf(1);
                else
                    row[4] = String.valueOf(0);
                row[5] = "null";
                rows.add(row);
            }
            index++;
        }

        try {
            //cartellina
            File f = new File("output");
            f.mkdir();
            File csvFile = new File(f.getAbsolutePath() + "\\" + "select_all_from_attivita.csv");
            //scriba, pls
            CSVWriter writer = new CSVWriter(new FileWriter(csvFile));

            for (String[] array : rows) {

                writer.writeNext(array);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isTreCifre() {
        return isTreCifre;
    }

    public void setTreCifre(boolean isTreCifre) {
        this.isTreCifre = isTreCifre;
    }


}