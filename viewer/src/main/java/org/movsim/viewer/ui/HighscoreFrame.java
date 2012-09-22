package org.movsim.viewer.ui;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;

import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.SimulationRun;
import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.utilities.FileUtils;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HighscoreFrame implements SimulationRun.CompletionCallback, SimulationRunnable.UpdateStatusCallback {

    private static final String CSV_SEPARATOR = ";";
    final static Logger logger = LoggerFactory.getLogger(HighscoreFrame.class);
    private final Simulator simulator;
    private final String simulationFinished;
    private final String askingForName;
    
    private final int MAX_RANK_FOR_HIGHSCORE;

    public HighscoreFrame(ResourceBundle resourceBundle, Simulator simulator, Properties properties) {
        this.simulator = simulator;
        this.simulationFinished = (String) resourceBundle.getObject("SimulationFinished");
        this.askingForName = (String) resourceBundle.getObject("AskingForName");
        
        this.MAX_RANK_FOR_HIGHSCORE = Integer.parseInt(properties.getProperty("maxRankForHighscorePrompt"));

        simulator.getSimulationRunnable().setCompletionCallback(this);
        simulator.getSimulationRunnable().addUpdateStatusCallback(this);
    }

    /**
     * @param simulationTime
     * @param totalFuelUsedLiters
     */
    private void highscoreForGames(final HighscoreEntry highscoreEntry) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {

                String highscoreFilename = ProjectMetaData.getInstance().getProjectName() + "_highscore.txt";
                LinkedList<String> highscores = readHighscores(highscoreFilename);
                int rank = 1;
                String username = null;
                if (highscores.size() > 0) {
                    String scoreString = highscores.get(rank - 1);
                    double score;
                    while ((score = Double.parseDouble(scoreString.substring(0, scoreString.indexOf(CSV_SEPARATOR)))) <= highscoreEntry.getTotalSimulationTime()) {
                        if (++rank > highscores.size())
                            break;
                        scoreString = highscores.get(rank - 1);
                    }
                }
                
                JOptionPane.showMessageDialog(null, getDialogMessage(highscoreEntry, highscores.size(), rank) );
                
                if (rank <= MAX_RANK_FOR_HIGHSCORE) {
                    // TODO limit input to reasonable number of characters
                    username = JOptionPane.showInputDialog(null, askingForName, "");
                }
                highscores.add(rank - 1, (int) highscoreEntry.getTotalSimulationTime() + CSV_SEPARATOR + highscoreEntry.getTotalFuelUsedLiters() + CSV_SEPARATOR + username);

                writeFile(highscoreFilename, highscores);
                
                displayHighscores(highscoreFilename);
            }

            private String getDialogMessage(HighscoreEntry entry, int highscoreSize, int rank){
                return String.format(simulationFinished, (int)entry.getTotalSimulationTime(), (int)entry.getTotalTravelTime(), (int)entry.getTotalTravelDistance(), entry.getTotalFuelUsedLiters(), entry.getTotalElectricEnergyUsed(), highscoreSize + 1, rank);
                
            }
            
            private void writeFile(String highscoreFilename, LinkedList<String> highscores) {
                PrintWriter hswriter = FileUtils.getWriter(highscoreFilename);
                for (int i = 0; i < highscores.size();) {
                    hswriter.println(highscores.get(i++));
                }
                hswriter.flush();
                hswriter.close();
            }
        });
    }

    /**
     * Reads and validates the high score table
     * 
     * @return the high score table
     */
    private LinkedList<String> readHighscores(String filename) {
        LinkedList<String> highscores = new LinkedList<String>();
        try {
            BufferedReader hsreader = FileUtils.getReader(filename);
            String entry;
            int bettertime = 0;
            while ((entry = hsreader.readLine()) != null) {
                String[] entries = entry.split(CSV_SEPARATOR, 3);
                int worsetime = Integer.parseInt(entries[0]);
                double fuel = Double.parseDouble(entries[1]);
                if ((worsetime < bettertime) || (fuel < 0)) {
                    logger.error("high score file {} contains corrupt data", filename);
                    throw new Exception();
                }
                highscores.add(entry);
                bettertime = worsetime;
            }
        } catch (final Exception e) {
            logger.error("error reading file {} - starting new high score", filename);
            return new LinkedList<String>();
        }
        return highscores;
    }

    /**
     * Displays the high score table
     */
    public void displayHighscores(String filename) {
        List<String> highscores = readHighscores(filename);
        String[] columnNames = { "Rank", "Name", "Time (seconds)", "Total Traveltime (s)", "Fuel (liters)" };
        String[][] rowData = new String[MAX_RANK_FOR_HIGHSCORE][columnNames.length];
        for (int i = 0; (i < highscores.size()) && (i < MAX_RANK_FOR_HIGHSCORE); i++) {
            String[] entries = highscores.get(i).split(CSV_SEPARATOR, 3);
            rowData[i][0] = Integer.toString(i + 1);
            rowData[i][1] = entries[2];
            rowData[i][2] = String.format("%d", Integer.parseInt(entries[0]));
            // TODO arne
            rowData[i][3] = String.format("%.2f", Double.parseDouble(entries[1]));
            rowData[i][4] = String.format("%.2f", Double.parseDouble(entries[1]));
        }
        
        JTable highscoreTable = new JTable(rowData, columnNames);
        highscoreTable.setEnabled(false);
        
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.add(new JScrollPane(highscoreTable));
        f.pack();
        f.setVisible(true);
    }

    @Override
    public void updateStatus(double simulationTime) {
        if (simulator.isFinished()) {
            // hack to simulationComplete
            simulator.getSimulationRunnable().setDuration(simulationTime);
        }
    }
    
    @Override
    public void simulationComplete(double simulationTime) {
        RoadNetwork roadNetwork = simulator.getRoadNetwork();
        final double totalVehicleTravelTime = roadNetwork.totalVehicleTravelTime();
        final double totalVehicleTravelDistance = roadNetwork.totalVehicleTravelDistance() * Units.M_TO_KM;
        final double totalVehicleFuelUsedLiters = roadNetwork.totalVehicleFuelUsedLiters();
        final double totalVehicleElectricEnergyUsed = roadNetwork.totalVehicleElectricEnergyUsed() / (1000 * 3600);
        
        highscoreForGames(new HighscoreEntry(simulationTime, totalVehicleTravelTime, totalVehicleTravelDistance, totalVehicleFuelUsedLiters, totalVehicleElectricEnergyUsed));
    }

    public static void initialize(ResourceBundle resourceBundle, Simulator simulator, Properties properties) {
        new HighscoreFrame(resourceBundle, simulator, properties);
    }
    
    
   public final class HighscoreEntry{
        private final double totalSimulationTime;
        private final double totalTravelTime;
        private final double totalTravelDistance;
        private final double totalFuelUsedLiters;
        private final double totalElectricEnergyUsed;
        public HighscoreEntry(double totalSimulationTime, double totalTravelTime, double totalTravelDistance, double totalFuelUsedLiters, double totalElectricEnergyUsed) {
            this.totalSimulationTime = totalSimulationTime;
            this.totalTravelTime = totalTravelTime;
            this.totalTravelDistance = totalTravelDistance;
            this.totalFuelUsedLiters = totalFuelUsedLiters;
            this.totalElectricEnergyUsed = totalElectricEnergyUsed;
        }
        public double getTotalSimulationTime() {
            return totalSimulationTime;
        }
        public double getTotalTravelTime() {
            return totalTravelTime;
        }
        public double getTotalTravelDistance() {
            return totalTravelDistance;
        }
        public double getTotalFuelUsedLiters() {
            return totalFuelUsedLiters;
        }
        public double getTotalElectricEnergyUsed() {
            return totalElectricEnergyUsed;
        }
        
        
    }
}
