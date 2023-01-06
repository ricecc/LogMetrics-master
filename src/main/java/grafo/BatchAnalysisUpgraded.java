package grafo;


import org.graphstream.graph.Graph;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class for LogUtilsRepeatingGraph multiple iterations
 *
 * @author luigi.bucchicchioAtgmail.com
 */
public class BatchAnalysisUpgraded {
    public static List<Graph> graphList;
    public static List<List<Graph>> listOfGraphList;

    public static boolean nested = false;
    public static File[] folderList;
    public static List<File[]> listOfFileList;

    public static File[] fileList;

    /**
     * Analisi di XES files in cartella o in cartella di cartelle.
     * Ritorna un CSV rappresentante una Distance Matrix per ogni variazione di parametro in Batch. (gamma e repeating nodi/archi)
     * Vengono effettuati multipli richiami ad "iteration()" impostando la lista di lista se "nested"
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     * @author luigi.bucchicchioAtgmail.com
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        System.console();
        long startingTime = System.currentTimeMillis();

        //Cartella o cartella di cartelle?

        int n = JOptionPane.showConfirmDialog(null, "Nested Folder?", "Folder Option", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION)
            nested = true;
        else nested = false;

        if (!nested)
            startup();
        else {
            nestedFolderStartup();
        }

        if (!nested) {
            LogUtilsRepeatingGraph lurg = iteration(0.0, 0.0, 0.0, null);
            lurg = iteration(0.0, 1.0, 1.0, lurg);
            lurg = iteration(0.5, 0.0, 0.0, lurg);
            lurg = iteration(0.5, 1.0, 1.0, lurg);
            lurg = iteration(1.0, 0.0, 0.0, lurg);
            lurg = iteration(1.0, 1.0, 1.0, lurg);

        } else {
            for (int i = 0; i < listOfFileList.size(); i++) {
                fileList = listOfFileList.get(i);
                LogUtilsRepeatingGraph lurg = iteration(0.0, 0.0, 0.0, null);
                lurg = iteration(0.0, 1.0, 1.0, lurg);
                lurg = iteration(0.5, 0.0, 0.0, lurg);
                lurg = iteration(0.5, 1.0, 1.0, lurg);
                lurg = iteration(1.0, 0.0, 0.0, lurg);
                lurg = iteration(1.0, 1.0, 1.0, lurg);
            }
        }

//		
//		
//		// Qui il RepeatingIncr
//		double repeatingIncr=(double) 0.5;
//		double repeatingEdge=0.0;
//		double repeatingNode=0.0;
//
//		//solo nodi/attivit�
//		double gamma=1.0;
//		
//		// scenari Repeating 0 ; 0.5 ; 1
//		for(repeatingNode= (double)0.0;repeatingNode<=(double) 1.0;repeatingNode=repeatingNode+repeatingIncr) {
//			if(!nested)
//			iteration(gamma,repeatingEdge,repeatingNode);
//			else {
//				for(int i=0; i<listOfFileList.size();i++) {
//					fileList = listOfFileList.get(i);
//					iteration(gamma,repeatingEdge,repeatingNode);
//				}
//			}
//		}
//
//		//reset
//		repeatingNode=(double) 0.0;
//
//		//solo archi/transizioni
//		gamma=0.0;
//
//		// scenari Repeating 0 ; 0.5 ; 1
//		for(repeatingEdge=(double) 0.0;repeatingEdge<=(double) 1.0;repeatingEdge=repeatingEdge+repeatingIncr) {
//			if(!nested)
//			iteration(gamma, repeatingEdge, repeatingNode);
//			else {
//			    for(int i=0;i<listOfFileList.size();i++) {
//			    	fileList = listOfFileList.get(i);
//			    	iteration(gamma, repeatingEdge, repeatingNode);
//			    }
//			}
//		}
//
//		//reset
//		repeatingEdge=(double) 0.0;
//		
//		//50-50
//		gamma=0.5;
//
//		// scenari combo: (0 ; 0.5; 1) Nodi/Attivit� <-X-> (0 ; 0.5; 1) Transizioni/Archi
//		for(repeatingNode = (double)0.0;repeatingNode<=(double)1.0;repeatingNode=repeatingNode+repeatingIncr) {
//			for(repeatingEdge=(double)0.0;repeatingEdge<=(double)1.0; repeatingEdge=repeatingEdge+repeatingIncr) {
//				if(!nested)
//				iteration(gamma, repeatingEdge, repeatingNode);
//				else {
//					for(int i=0;i<listOfFileList.size();i++) {
//						fileList = listOfFileList.get(i);
//						iteration(gamma, repeatingEdge, repeatingNode);
//					}
//				}
//			}
//		}

        //quando esce fuori lui, abbiamo fatto. Ci manca un "Arrivederci!" di cortesia
        long repTime = System.currentTimeMillis() - startingTime;
        long minutes = (repTime / 1000) / 60;
        long seconds = (repTime / 1000) % 60;
        JOptionPane.showMessageDialog(null, "finished in: " + minutes + " min and " + seconds + " sec");


    }


    /**
     * Segue circa il MAIN di logUtilsRepeatingGraph, rappresentando una singola iterazione per configurazione di parametri.
     * Da notare i passi base dell'algoritmo:
     * listaFile(XES), settaggio parametri, analyzeTraces(), generateDistanceMatrix(), convertToCSV()
     *
     * @param a Valore di Gamma tra 0.0 e 1.0, rappresentando il peso tra Archi/Transizioni(0 max) e Nodi/Attivit�(1 max)
     * @param b Punteggio tra un Nodo/Attivit� Repeating e lo stesso nodo NotRepeating (es. 1.0 Uguali; 0.0 Diversi;)
     * @param c Punteggio tra un Arco/Transizone Repeating e lo stesso arco NotRepeating (es. 1.0 Uguali; 0.0 Diversi)
     * @author luigi.bucchicchioAtgmail.com
     */
    private static LogUtilsRepeatingGraph iteration(double a, double b, double c, LogUtilsRepeatingGraph previous) {
        if (previous == null) {
            LogUtilsRepeatingGraph log = new LogUtilsRepeatingGraph();
            log.setFileList(fileList);
            int x = fileList.length;
            log.setTraceNum(new int[x]);
            log.setAvgTraceLen(new double[x]);
            log.setScoreChange(true);
            //gamma
            log.setGamma(a);
            log.setNodeEqualScore((double) 1.0);
            log.setNodeNotEqualScore((double) 0.0);
            //semiscore o RepeatingScore nodi/attivit�
            log.setNodeSemiScore(c);
            log.setEdgeEqualScore((double) 1.0);
            log.setEdgeNotEqualScore((double) 0.0);
            //semiscore o RepeatingScore archi/transizioni
            log.setEdgeSemiScore(b);
            log.analyzeTraces();
            String[][] distanceMatrix = log.generateDistanceMatrix();
            log.convertToCSV(distanceMatrix);
            System.out.println("a Distance Matrix is created");
            return log;
        } else {
            LogUtilsRepeatingGraph log = new LogUtilsRepeatingGraph();
            log.setScoreChange(true);
            log.setGamma(a);
            log.setNodeEqualScore((double) 1.0);
            log.setNodeNotEqualScore((double) 0.0);
            //semiscore o RepeatingScore nodi/attivit�
            log.setNodeSemiScore(c);
            log.setEdgeEqualScore((double) 1.0);
            log.setEdgeNotEqualScore((double) 0.0);
            log.setEdgeSemiScore(b);
            log.setGraphList(previous.getGraphList());
            log.setFileList(previous.getFileList());
            String[][] distanceMatrix = log.generateDistanceMatrix();
            log.convertToCSV(distanceMatrix);
            System.out.println("a Distance Matrix is created");
            previous = null;
            return log;
        }
    }

    /**
     * crea lista file (aspettandosi una lista di file XES)
     *
     * @author luigi.bucchicchioAtgmail.com
     */
    private static void startup() {

        Locale.setDefault(Locale.US);
        System.out.println("Log evaluation - ");
        JFileChooser chooser = new JFileChooser(".");
        System.out.println("\u2705 " + "Please select the folder containing the XES Files" + " \u2705");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this folder: " + chooser.getSelectedFile().getName());
        }
        File folder = new File(chooser.getSelectedFile().getAbsolutePath());
        fileList = folder.listFiles();

    }

    /**
     * crea lista di liste file (aspettandosi una cartella con cartelle di file XES)
     *
     * @author luigi.bucchicchioAtgmail.com
     */
    private static void nestedFolderStartup() {

        Locale.setDefault(Locale.US);
        System.out.println("Log evaluation - ");
        JFileChooser chooser = new JFileChooser(".");
        System.out.println("\u2705 " + "Please select the main folder containing folder of XES files" + " \u2705");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this folder: " + chooser.getSelectedFile().getName());
        }
        File mainFolder = new File(chooser.getSelectedFile().getAbsolutePath());
        folderList = mainFolder.listFiles();
        listOfFileList = new ArrayList<File[]>(folderList.length);
        for (int i = 0; i < folderList.length; i++) {
            File folder = folderList[i];
            if (folder.isDirectory())
                listOfFileList.add(folder.listFiles());
        }

    }

}
