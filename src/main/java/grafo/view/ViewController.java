package grafo.view;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import grafo.LogUtilsRepeatingGraph;
import grafo.controller.TraceController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

//import static grafo.EnsembleRun.prepareForHeatMap;

public class ViewController implements Initializable {



    @FXML
    private Label _xesFiles;
    @FXML
    private TextField _gammaID;
    @FXML
    private ChoiceBox<String> _changeScoreID = new ChoiceBox<>();

    @FXML
    private TextField _nodeEqualScoreID;
    @FXML
    private TextField _nodeNotEqualScoreID;
    @FXML
    private TextField _nodeSemiEqualScoreID;
    @FXML
    private TextField _edgeEqualScoreID;
    @FXML
    private TextField _edgeNotEqualScoreID;
    @FXML
    private TextField _edgeSemiEqualScoreID;

    @FXML
    private TextField _nGramID;

    private File _xesDirectory = new File("");
    private boolean validInputs = false;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        _changeScoreID.getItems().addAll("No", "Yes");
        _changeScoreID.setValue("No");
    }

    public void loadDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select XES Files Directory");
        directoryChooser.setInitialDirectory(new java.io.File("."));
        _xesDirectory = directoryChooser.showDialog(null);
        _xesFiles.setText(_xesDirectory.listFiles().length == 0 ? "No files found" : _xesDirectory.getAbsolutePath());
    }

    public void changeScore() {
        if (_changeScoreID.getValue().equals("Yes")) {
            _nodeEqualScoreID.setDisable(false);
            _nodeNotEqualScoreID.setDisable(false);
            _nodeSemiEqualScoreID.setDisable(false);
            _edgeEqualScoreID.setDisable(false);
            _edgeNotEqualScoreID.setDisable(false);
            _edgeSemiEqualScoreID.setDisable(false);
            _nGramID.setDisable(false);
        } else {
            _nodeEqualScoreID.setDisable(true);
            _nodeNotEqualScoreID.setDisable(true);
            _nodeSemiEqualScoreID.setDisable(true);
            _edgeEqualScoreID.setDisable(true);
            _edgeNotEqualScoreID.setDisable(true);
            _edgeSemiEqualScoreID.setDisable(true);
            _nGramID.setDisable(true);
        }
    }

    public void runMining() throws IOException, InterruptedException, CsvValidationException {
        validInputs = checkInputValues();
        if(validInputs){
            startMining();
        } else {
            System.out.println("Invalid inputs");
        }
    }

    /**
     * Questo metodo permette di controllare la validità di tutti i valori inseriti: gamma, score, ngram.
     *
     * @return true se tutti i valori in input sono validi.
     */
    private boolean checkInputValues(){
        if (_changeScoreID.getValue().equals("Yes")) {
            return  validateValue(_gammaID) &
                    validateValue(_nodeEqualScoreID) &
                    validateValue(_nodeNotEqualScoreID) &
                    validateValue(_nodeSemiEqualScoreID) &
                    validateValue(_edgeEqualScoreID) &
                    validateValue(_edgeNotEqualScoreID) &
                    validateValue(_edgeSemiEqualScoreID);
        }
        else {
            return validateValue(_gammaID);
        }
    }
/*
    private boolean checkInputValues(){
        List<TextField> inputValues = new LinkedList<TextField>();
        inputValues.add(_gammaID);
        inputValues.add(_nodeEqualScoreID);
        inputValues.add(_nodeNotEqualScoreID);
        inputValues.add(_nodeSemiEqualScoreID);
        inputValues.add(_edgeEqualScoreID);
        inputValues.add(_edgeNotEqualScoreID);
        inputValues.add(_edgeSemiEqualScoreID);
        List<Boolean> check= inputValues.stream().map(this::validateValue).toList();
        return check.stream().allMatch(b -> b);
    }
*/
    /**
     * Questo metodo permette di controllare la validità di un campo inserito in input.
     * Un campo è valido se è un numero reale compreso tra 0 e 1.
     *
     * @param textField il campo da validare
     * @return true se il campo inserito è un numero reale compreso tra 0 e 1.
     */
    private boolean validateValue(TextField textField) {
        if (textField.getText().matches("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$")) {
            if (Double.parseDouble(textField.getText()) < 0 || Double.parseDouble(textField.getText()) > 1) {
                textField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
                return false;
            } else {
                textField.setStyle("-fx-border-color: transparent ; -fx-border-width: 0px ;");
                return true;
            }
        } else {
            textField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            return false;
        }
    }


    /**
     * Questo metodo permette di avviare il process mining.
     * Si può dire che è la copia del metodo main di EnsembleRun
     */
    private void startMining() throws IOException, InterruptedException, CsvValidationException {
        long startingTime = System.currentTimeMillis();
        Locale.setDefault(Locale.US);
        System.out.println("Log evaluation start");
        LogUtilsRepeatingGraph logUtils = new LogUtilsRepeatingGraph();


        logUtils.setFileList(_xesDirectory.listFiles());

        int size = _xesDirectory.listFiles().length;

        if (size <= 2) {
            System.out.println("Not enough Input XES Files found");
            System.exit(99);
        }

        logUtils.setTraceNum(new int[size]);
        logUtils.setAvgTraceLen(new double[size]);
        logUtils.setScoreChange(true);

        double gamma = Double.valueOf(_gammaID.getText());

        if (_changeScoreID.getValue().equals("Yes")){
            double nodeEqualScoreID = Double.valueOf(_nodeEqualScoreID.getText());
            double nodeNotEqualScoreID = Double.valueOf(_nodeNotEqualScoreID.getText());
            double nodeSemiEqualScoreID = Double.valueOf(_nodeSemiEqualScoreID.getText());
            double edgeEqualScoreID =  Double.valueOf(_edgeEqualScoreID.getText());
            double edgeNotEqualScoreID = Double.valueOf(_edgeNotEqualScoreID.getText());
            double edgeSemiEqualScoreID = Double.valueOf(_edgeSemiEqualScoreID.getText());
            int nGramID = Integer.valueOf(_nGramID.getText());


            logUtils.setGamma(gamma);
            logUtils.setNodeEqualScore(nodeEqualScoreID);
            logUtils.setNodeNotEqualScore(nodeNotEqualScoreID);
            logUtils.setNodeSemiScore(nodeSemiEqualScoreID);
            logUtils.setEdgeEqualScore(edgeEqualScoreID);
            logUtils.setEdgeNotEqualScore(edgeNotEqualScoreID);
            logUtils.setEdgeSemiScore(edgeSemiEqualScoreID);
            logUtils.setnGram(nGramID);
        } else {
            logUtils.setGamma(gamma);
        }


        logUtils.analyzeTraces();
        // Metodo aggiunto per vedere il dizionario di n-gram
        Stream.of(TraceController.dictionary).forEach(System.out::println);
        String[][] distanceMatrix = logUtils.generateDistanceMatrix();


        logUtils.convertToCSV(distanceMatrix);
        System.out.println("Evaluation Terminated - Execution Time:" + (System.currentTimeMillis() - startingTime));

        // Multi-threading
        int cores = Runtime.getRuntime().availableProcessors();

        System.out.println("System cores: " + cores);
        File script = new File(
                Optional
                        .ofNullable(System.getenv("CLUSTERING_SCRIPT_PATH"))
                        .orElse("main.py")
        );
        String scriptPath = script.getAbsolutePath();
        scriptPath = scriptPath.replace('\\', '/');
        File currentDirectory = new File("");
        String currentPath = currentDirectory.getAbsolutePath();
        currentPath = currentPath.replace('\\', '/');



        if (cores > 1 && ((logUtils.getFileList().length - 2) > (cores * 2))) {
            System.out.println("Clustering Algorithm start");

            ProcessBuilder[] builders = new ProcessBuilder[cores];
            Process[] processes = new Process[cores];

            int subpart = (int) Math.floor((double) logUtils.getFileList().length / cores);

            int diff = logUtils.getFileList().length - subpart * cores;

            int last = 0;

            //			if(log.getFileList().length<=(cores*4)) {

            for (int i = 0; i < cores; i++) {
                ProcessBuilder pb;
                if (i == 0) {
                    pb = new ProcessBuilder("python", scriptPath, "" + 2, "" + (last + subpart + diff) + "", "" + currentPath + "\\output");
                    last = last + subpart + diff;
                } else {
                    pb = new ProcessBuilder("python", scriptPath, "" + last, "" + (last + subpart) + "", "" + currentPath + "\\output");
                    last = last + subpart;
                }
                pb.redirectErrorStream(true);
                builders[i] = pb;
            }

            for (int i = 0; i < cores; i++) {
                processes[i] = builders[i].start();
            }

            System.out.print("waiting for " + processes.length + " processes to end");
            for (int i = 0; i < cores; i++) {
                processes[i].waitFor();
                System.out.print(".");
            }
            System.out.println("\nClustering Algorithm terminated - total execution time: " + (System.currentTimeMillis() - startingTime));
            System.out.println("Incoming Results on output directory...");

            File dir = new File("");
            String dirPath = dir.getAbsolutePath();
            dir = new File(dirPath);



            List<File> fileList = new ArrayList<>();
            Collections.addAll(fileList, dir.listFiles());


            List<File> outputList = new ArrayList<>();
            for (File nextFile : fileList) {
                if (nextFile.getName().contains("clustering") || nextFile.getName().contains("smallOut"))
                    outputList.add(nextFile);
            }



            Iterator<File> outputFileIterator = outputList.iterator();
            double max = 0.0;
            File winner = null;
            CSVReader reader;
            while (outputFileIterator.hasNext()) {
                File nextOutputFile = outputFileIterator.next();
                if (nextOutputFile.getName().contains("smallOut")) {
                    reader = new CSVReader(new FileReader(nextOutputFile));
                    String[] row = reader.readNext();
                    double score = Double.parseDouble(row[1]);
                    if (score > max) {
                        max = score;
                        winner = nextOutputFile;
                    }
                    reader.close();
                }
            }

            File[] winners = new File[2];
            int winnersIndex = 0;
            String winnerName = winner.getName();
            for (File file : outputList) {
                int winnerNameIndex = winnerName.indexOf("smallOut");
                String winnerNameNumber = winnerName.substring(0, winnerNameIndex);
                if (!file.getName().contains(winnerNameNumber)) {
                    file.deleteOnExit();
                } else {
                    winners[winnersIndex] = file;
                    winnersIndex++;
                }
            }

            String parentDir0 = winners[0].getParent();
            parentDir0 = parentDir0 + "\\output";
            String winner0name = winners[0].getName();
            winners[0].renameTo(new File(parentDir0 + "\\" + winner0name));

            String parentDir1 = winners[1].getParent();
            parentDir1 = parentDir1 + "\\output";
            String winner1name = winners[1].getName();
            winners[1].renameTo(new File(parentDir1 + "\\" + winner1name));

            System.out.println("Done");

        } else {
            System.out.println("Clustering Algorithm start");
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, "" + 2, "" + logUtils.getFileList().length + "", "" + currentPath + "\\output");
            Process p = pb.start();
            p.waitFor();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = bfr.readLine()) != null) {
                System.out.println(line);
            }
            bfr.close();
            System.out.println("Clustering Algorithm terminated - total execution time: " + (System.currentTimeMillis() - startingTime));
            System.out.println("Incoming Results on output directory...");
            Thread.sleep(1000);


            File dir = new File("");
            String dirPath = dir.getAbsolutePath();
            dir = new File(dirPath);

            List<File> fileList = new ArrayList<>();
            Collections.addAll(fileList, dir.listFiles());

            List<File> outputList = new ArrayList<>();
            for (File nextFile : fileList) {
                if (nextFile.getName().contains("clustering") || nextFile.getName().contains("smallOut"))
                    outputList.add(nextFile);
            }

            if (outputList.size() == 2) {
                String parentDir0 = outputList.get(0).getParent();
                parentDir0 = parentDir0 + "\\output";
                String winner0name = outputList.get(0).getName();
                outputList.get(0).renameTo(new File(parentDir0 + "\\" + winner0name));

                String parentDir1 = outputList.get(1).getParent();
                parentDir1 = parentDir1 + "\\output";
                String winner1name = outputList.get(1).getName();
                outputList.get(1).renameTo(new File(parentDir1 + "\\" + winner1name));
            }

            System.out.println("Done");
        }
        logUtils.generateNodeListReport("CUSTOM");
        prepareForHeatMap();
    }

    public static void prepareForHeatMap() throws IOException {
        File dir = new File("");
        String dirPath = dir.getAbsolutePath();
        dir = new File(dirPath);
        File outputDirectory = new File(dir + "\\output");
        if (outputDirectory.isDirectory()) {
            File[] fileList = outputDirectory.listFiles();
            for (File one : fileList) {
                if (one.getName().contains("clustering")) {
                    File newClusteringFile = new File(outputDirectory + "\\preparedLabelsForHeatmap.csv");
                    FileWriter fw = new FileWriter(newClusteringFile);
                    BufferedWriter bw = new BufferedWriter(fw);
                    Scanner s = new Scanner(one);
                    String line;
                    while (s.hasNextLine()) {
                        line = s.nextLine();
                        if (!line.contains(".")) {
                            bw.newLine();
                            line = line.replace("['", "");
                            line = line.replace("]", "");
                            line = line.replace("[", "");
                            line = line.replace("' ", ",");
                            bw.write(line);
                        } else if (line.contains("DistanceGraph")) {
                            bw.write("NomeLog,ClusterId");
                        }
                        // Skip
                    }
                    s.close();
                    bw.close();
                }
            }
        }
    }
}
