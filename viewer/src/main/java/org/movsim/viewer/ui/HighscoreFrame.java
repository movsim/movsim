package org.movsim.viewer.ui;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeSet;

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

    final static Logger logger = LoggerFactory.getLogger(HighscoreFrame.class);

    public enum Quantity {
        totalSimulationTime("Time (s)"), totalTravelTime("Total Traveltime (s)"), totalTravelDistance(
                "Total Distance (km)"), totalFuelUsedLiters("Fuel (liters)");

        final String label;

        private Quantity(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

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
                TreeSet<HighscoreEntry> sortedResults = new TreeSet<HighscoreEntry>(new Comparator<HighscoreEntry>() {
                    @Override
                    public int compare(HighscoreEntry o1, HighscoreEntry o2) {
                        Double d1 = new Double(o1.getQuantity(Quantity.totalSimulationTime));
                        Double d2 = new Double(o2.getQuantity(Quantity.totalSimulationTime));
                        return d1.compareTo(d2); 
                    }
                });
                sortedResults.addAll(readHighscores(highscoreFilename));

                int rank = determineRanking(highscoreEntry, sortedResults);
                JOptionPane.showMessageDialog(null, getDialogMessage(highscoreEntry, sortedResults.size(), rank));

                if (rank <= MAX_RANK_FOR_HIGHSCORE) {
                    // TODO limit input to reasonable number of characters
                    String username = JOptionPane.showInputDialog(null, askingForName, "");
                    highscoreEntry.setPlayerName(username);
                }

                sortedResults.add(highscoreEntry);

                writeFile(highscoreFilename, sortedResults);

                displayHighscores(sortedResults);
            }

            private int determineRanking(HighscoreEntry resultEntry, TreeSet<HighscoreEntry> sortedResults) {
                int ranking = 1;
                for (HighscoreEntry entry : sortedResults) {
                    if (sortedResults.comparator().compare(resultEntry, entry) < 0) {
                        return ranking;
                    }
                    ++ranking;

                }
                return ranking;
            }

            private String getDialogMessage(HighscoreEntry entry, int highscoreSize, int rank) {
                return String.format(simulationFinished,
                        (int) highscoreEntry.getQuantity(Quantity.totalSimulationTime),
                        (int) highscoreEntry.getQuantity(Quantity.totalTravelTime),
                        (int) highscoreEntry.getQuantity(Quantity.totalTravelDistance),
                        highscoreEntry.getQuantity(Quantity.totalFuelUsedLiters), highscoreSize + 1, rank);
            }

            private void writeFile(String highscoreFilename, Iterable<HighscoreEntry> highscores) {
                PrintWriter hswriter = FileUtils.getWriter(highscoreFilename);
                for (HighscoreEntry entry : highscores) {
                    hswriter.println(entry.toString());
                }
                hswriter.close();
            }
        });
    }

    /**
     * Reads and validates the high score table
     * 
     * @return the high score table
     */
    private List<HighscoreEntry> readHighscores(String filename) {
        List<HighscoreEntry> highscores = new LinkedList<HighscoreEntry>();
        BufferedReader hsreader = FileUtils.getReader(filename);
        if(hsreader==null){
            // no file available
            return new LinkedList<HighscoreEntry>(); 
        }
        String line;
        try {
            while ((line = hsreader.readLine()) != null) {
                highscores.add(new HighscoreEntry(line));
            }
        } catch (IOException e) {
            logger.error("error reading file {} - starting new high score.", filename);
            return new LinkedList<HighscoreEntry>();
        }
        return highscores;
    }

    /**
     * Displays the high score table
     */
    public void displayHighscores(Collection<HighscoreEntry> highscores) {
        // TODO combine with Quantity.values
        
        String[] columnNames = getTableHeader();
        String[][] table = new String[MAX_RANK_FOR_HIGHSCORE][columnNames.length];
        int row = 0;
        for (HighscoreEntry entry : highscores) {
            if (row > MAX_RANK_FOR_HIGHSCORE) {
                break;
            }
            for (Quantity quantity : Quantity.values()) {
                table[row][0] = String.format("%d", row+1);
                table[row][1] = String.format("%s", entry.getPlayerName());
                table[row][2 + quantity.ordinal()] = String.format("%.1f", entry.getQuantity(quantity));
            }
            ++row;
        }

        JTable highscoreTable = new JTable(table, columnNames);
        highscoreTable.setEnabled(false);

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.add(new JScrollPane(highscoreTable));
        f.pack();
        f.setVisible(true);
    }

    private String[] getTableHeader() {
        String[] columnsHeaders = new String[Quantity.values().length+2];
        int col = 0;
        columnsHeaders[col++] = "Rank";
        columnsHeaders[col++] = "Name";
        for(Quantity quantity : Quantity.values()){
            columnsHeaders[col++] = quantity.getLabel();
        }
        return columnsHeaders;
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
        HighscoreEntry highscoreEntry = new HighscoreEntry();
        highscoreEntry.setQuantity(Quantity.totalSimulationTime, simulationTime);
        highscoreEntry.setQuantity(Quantity.totalTravelTime, roadNetwork.totalVehicleTravelTime());
        highscoreEntry.setQuantity(Quantity.totalTravelDistance, roadNetwork.totalVehicleTravelDistance()
                * Units.M_TO_KM);
        highscoreEntry.setQuantity(Quantity.totalFuelUsedLiters, roadNetwork.totalVehicleFuelUsedLiters());
        highscoreForGames(highscoreEntry);
    }

    public static void initialize(ResourceBundle resourceBundle, Simulator simulator, Properties properties) {
        new HighscoreFrame(resourceBundle, simulator, properties);
    }

    final class HighscoreEntry {
        private static final String CSV_SEPARATOR = ";";

        private double[] quantities = new double[Quantity.values().length];

        private String playerName = "";

        public HighscoreEntry() {
        }

        public HighscoreEntry(String line) {
            String[] entries = line.split(CSV_SEPARATOR);
            for (Quantity quantity : Quantity.values()) {
                quantities[quantity.ordinal()] = Double.parseDouble(entries[quantity.ordinal()]);
            }
            if (entries.length == Quantity.values().length + 1) {
                playerName = entries[entries.length - 1];
            }
        }

        public double getQuantity(Quantity quantity) {
            return quantities[quantity.ordinal()];
        }

        public void setQuantity(Quantity quantity, double value) {
            quantities[quantity.ordinal()] = value;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String username) {
            playerName = username;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (double d : quantities) {
                sb.append(String.format(Locale.US, "%.6f", d)).append(CSV_SEPARATOR);
            }
            sb.append(playerName).append(CSV_SEPARATOR);
            return sb.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((playerName == null) ? 0 : playerName.hashCode());
            result = prime * result + Arrays.hashCode(quantities);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            HighscoreEntry other = (HighscoreEntry) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (playerName == null) {
                if (other.playerName != null)
                    return false;
            } else if (!playerName.equals(other.playerName))
                return false;
            if (!Arrays.equals(quantities, other.quantities))
                return false;
            return true;
        }

        private HighscoreFrame getOuterType() {
            return HighscoreFrame.this;
        }

    }
}
