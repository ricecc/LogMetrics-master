package grafo;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.graphstream.graph.Graph;

/**
 * Class for LogUtilsRepeatingGraph multiple iterations
 *
 * @author luigi.bucchicchioAtgmail.com
 */
public class BatchAnalysisParallel {
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
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        if (!nested) {
            LogUtilsRepeatingGraph lurg = iteration(0.0, 0.0, 0.0, null);
            BatchAnalysisParallel parallelClass = new BatchAnalysisParallel();
            IterationThread it1 = parallelClass.new IterationThread(0.0, 1.0, 1.0, lurg);
            IterationThread it2 = parallelClass.new IterationThread(0.5, 0.0, 0.0, lurg);
            IterationThread it3 = parallelClass.new IterationThread(0.5, 1.0, 1.0, lurg);
            IterationThread it4 = parallelClass.new IterationThread(1.0, 0.0, 0.0, lurg);
            IterationThread it5 = parallelClass.new IterationThread(1.0, 1.0, 1.0, lurg);
            executor.submit(it1);
            executor.submit(it2);
            executor.submit(it3);
            executor.submit(it4);
            executor.submit(it5);
        } else {
            for (int i = 0; i < listOfFileList.size(); i++) {
                fileList = listOfFileList.get(i);
                LogUtilsRepeatingGraph lurg = iteration(0.0, 0.0, 0.0, null);

                BatchAnalysisParallel parallelClass = new BatchAnalysisParallel();
                IterationThread it1 = parallelClass.new IterationThread(0.0, 1.0, 1.0, lurg);
                IterationThread it2 = parallelClass.new IterationThread(0.5, 0.0, 0.0, lurg);
                IterationThread it3 = parallelClass.new IterationThread(0.5, 1.0, 1.0, lurg);
                IterationThread it4 = parallelClass.new IterationThread(1.0, 0.0, 0.0, lurg);
                IterationThread it5 = parallelClass.new IterationThread(1.0, 1.0, 1.0, lurg);
                executor.submit(it1);
                executor.submit(it2);
                executor.submit(it3);
                executor.submit(it4);
                executor.submit(it5);
            }
        }

        executor.shutdown();
        while (!executor.isTerminated())
            Thread.sleep(1000);

        //quando esce fuori lui, abbiamo fatto. Ci manca un "Arrivederci!" di cortesia
        long repTime = System.currentTimeMillis() - startingTime;
        long minutes = (repTime / 1000) / 60;
        long seconds = (repTime / 1000) % 60;
        JOptionPane.showMessageDialog(null, "finished in: " + minutes + " min and " + seconds + " sec");


    }


    private class IterationThread extends Thread {
        private double a;
        private double b;
        private double c;
        private List<Graph> graphList = new ArrayList<Graph>();
        private File[] fileList;

        private IterationThread(double a, double b, double c, LogUtilsRepeatingGraph previous) {
            this.a = a;
            this.b = b;
            this.c = c;
            Iterator<Graph> it = previous.getGraphList().iterator();
            while (it.hasNext()) {
                Graph g = it.next();
                this.graphList.add(g);
            }
            this.fileList = previous.getFileList().clone();
        }

        @Override
        public void run() {
            LogUtilsRepeatingGraph log = new LogUtilsRepeatingGraph();
            log.setFileList(this.fileList);
            log.setGraphList(this.graphList);
            log.setScoreChange(true);
            log.setGamma(a);
            log.setNodeEqualScore((double) 1.0);
            log.setNodeNotEqualScore((double) 0.0);
            //semiscore o RepeatingScore nodi/attivit�
            log.setNodeSemiScore(c);
            log.setEdgeEqualScore((double) 1.0);
            log.setEdgeNotEqualScore((double) 0.0);
            log.setEdgeSemiScore(b);
            String[][] distanceMatrix = log.generateDistanceMatrix();
            log.convertToCSV(distanceMatrix);
            System.out.println("a Distance Matrix is created");
            this.graphList = null;
            this.fileList = null;
        }
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
