package grafo;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import grafo.controller.TraceController;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class EnsembleRun {



    private static String read(Process process){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            return result;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, CsvValidationException {

        long startingTime = System.currentTimeMillis();
        Locale.setDefault(Locale.US);
        System.out.println("Log evaluation start");
        LogUtilsRepeatingGraph log = new LogUtilsRepeatingGraph();
        File f = new File("input");

        // TODO: Rimuovere
        f.mkdir();



        log.setFileList(f.listFiles());
        int numberOfFiles = log.getFileList().length;
        if (numberOfFiles <= 2) {
            System.out.println("Not enough Input XES Files found");
            System.exit(99);
        }
        log.setTraceNum(new int[numberOfFiles]);
        log.setAvgTraceLen(new double[numberOfFiles]);

        if (args.length == 0) {
            Scanner tastiera = new Scanner(System.in);
            log.startMenu(tastiera);
        } else if (args.length == 4) {
            double gamma = Double.parseDouble(args[0]);
            double nodiRepeating = Double.parseDouble(args[1]);
            double archiRepeating = Double.parseDouble(args[2]);
            int primiTreSimboli = Integer.parseInt(args[3]);
            boolean treSimboli;
            treSimboli = primiTreSimboli != 0;

            log.setScoreChange(true);
            log.setGamma(gamma);
            log.setNodeSemiScore(nodiRepeating);
            log.setEdgeSemiScore(archiRepeating);
            log.setTreCifre(treSimboli);

        } else {
            double gamma = Double.parseDouble(args[0]);
            double nodiEqualScore = Double.parseDouble(args[1]);
            double nodiNotEqualScore = Double.parseDouble(args[2]);
            double nodiSemiEqualScore = Double.parseDouble(args[3]);
            double archiEqualScore = Double.parseDouble(args[4]);
            double archiNotEqualScore = Double.parseDouble(args[5]);
            double archiSemiScore = Double.parseDouble(args[6]);
            int primiTreSimboli = Integer.parseInt(args[7]);
            boolean treSimboli;
            treSimboli = primiTreSimboli != 0;

            log.setScoreChange(true);
            log.setGamma(gamma);
            log.setNodeEqualScore(nodiEqualScore);
            log.setNodeNotEqualScore(nodiNotEqualScore);
            log.setNodeSemiScore(nodiSemiEqualScore);
            log.setEdgeEqualScore(archiEqualScore);
            log.setEdgeNotEqualScore(archiNotEqualScore);
            log.setEdgeSemiScore(archiSemiScore);

            log.setTreCifre(treSimboli);
        }

        log.analyzeTraces();
        // Metodo aggiunto per vedere il dizionario di n-gram
        Stream.of(TraceController.dictionary).forEach(System.out::println);

        String[][] distanceMatrix = log.generateDistanceMatrix();
        log.convertToCSV(distanceMatrix);
        System.out.println("Evaluation Terminated - Execution Time:" + (System.currentTimeMillis() - startingTime));

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

        System.out.println("Script path: " + scriptPath);
        System.out.println("Current path: " + currentPath);

        if (cores > 1 && ((log.getFileList().length - 2) > (cores * 2))) {
            System.out.println("Clustering Algorithm start");

            ProcessBuilder[] builders = new ProcessBuilder[cores];
            Process[] processes = new Process[cores];

            int subpart = (int) Math.floor((double) log.getFileList().length / cores);

            int diff = log.getFileList().length - subpart * cores;

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
                System.out.println(read(processes[i]));
            }

            System.out.print("waiting for " + processes.length + " processes to end");
            for (int i = 0; i < cores; i++) {
                processes[i].waitFor();
                System.out.print(".");
            }
            System.out.println();
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
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, "" + 2, "" + log.getFileList().length + "", "" + currentPath + "\\output");
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
        log.generateNodeListReport("CUSTOM");
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
