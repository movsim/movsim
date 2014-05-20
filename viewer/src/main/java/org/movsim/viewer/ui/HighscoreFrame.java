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

import org.apache.commons.lang3.StringUtils;
import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.SimulationRun;
import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.utilities.FileUtils;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

// TODO this class needs a throughout refactoring ...
// FIXME: ramp_metering.xprj throws assertion error:
// Exception in thread "main" java.lang.AssertionError
// at org.movsim.simulator.SimulationRun.setCompletionCallback(SimulationRun.java:180)
// at org.movsim.viewer.ui.HighscoreFrame.<init>(HighscoreFrame.java:65)
// at org.movsim.viewer.ui.HighscoreFrame.initialize(HighscoreFrame.java:70)
// at org.movsim.viewer.ui.AppFrame.<init>(AppFrame.java:103)
// at org.movsim.viewer.App.main(App.java:88)
public class HighscoreFrame implements SimulationRun.CompletionCallback, SimulationRunnable.UpdateStatusCallback {

    private static final Logger LOG = LoggerFactory.getLogger(HighscoreFrame.class);

    enum Quantity {
        TOTAL_SIMULATION_TIME("Time (s)"), TOTAL_TRAVEL_TIME("Total Traveltime (s)"), TOTAL_TRAVEL_DISTANCE(
                "Total Distance (km)"), TOTAL_FUEL_USED_LITERS("Fuel (liters)");

        private final String label;

        Quantity(String label) {
            Preconditions.checkArgument(StringUtils.isNotBlank(label));
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final Simulator simulator;
    private final String simulationFinished;
    private final String askingForName;
    private final int maxRankForHighscore;

    public HighscoreFrame(ResourceBundle resourceBundle, Simulator simulator, Properties properties) {
        this.simulator = Preconditions.checkNotNull(simulator);
        this.simulationFinished = (String) resourceBundle.getObject("SimulationFinished");
        this.askingForName = (String) resourceBundle.getObject("AskingForName");
        this.maxRankForHighscore = Integer.parseInt(properties.getProperty("maxRankForHighscorePrompt"));

        simulator.getSimulationRunnable().setCompletionCallback(this);
        simulator.getSimulationRunnable().addUpdateStatusCallback(this);
    }

    private void highscoreForGames(final HighscoreEntry highscoreEntry) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                String highscoreFilename = ProjectMetaData.getInstance().getProjectName() + "_highscore.txt";
                TreeSet<HighscoreEntry> sortedResults = new TreeSet<>(new Comparator<HighscoreEntry>() {
                    @Override
                    public int compare(HighscoreEntry o1, HighscoreEntry o2) {
                        Double d1 = new Double(o1.getQuantity(Quantity.TOTAL_SIMULATION_TIME));
                        Double d2 = new Double(o2.getQuantity(Quantity.TOTAL_SIMULATION_TIME));
                        return d1.compareTo(d2);
                    }
                });
                sortedResults.addAll(readHighscores(highscoreFilename));

                int rank = determineRanking(highscoreEntry, sortedResults);
                JOptionPane.showMessageDialog(null, getDialogMessage(highscoreEntry, sortedResults.size(), rank));

                if (rank <= maxRankForHighscore) {
                    int maxChars = 20;
                    String username = JOptionPane.showInputDialog(null, askingForName, "");
                    if (username.length() > maxChars) {
                        username = username.substring(0, maxChars);
                    }
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
                        (int) highscoreEntry.getQuantity(Quantity.TOTAL_SIMULATION_TIME),
                        (int) highscoreEntry.getQuantity(Quantity.TOTAL_TRAVEL_TIME),
                        (int) highscoreEntry.getQuantity(Quantity.TOTAL_TRAVEL_DISTANCE),
                        highscoreEntry.getQuantity(Quantity.TOTAL_FUEL_USED_LITERS), highscoreSize + 1, rank);
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
        BufferedReader hsReader = FileUtils.getReader(filename);
        if (hsReader == null) {
            // no file available
            return new LinkedList<>();
        }
        List<HighscoreEntry> highscores = new LinkedList<>();
        String line;
        try {
            while ((line = hsReader.readLine()) != null) {
                highscores.add(new HighscoreEntry(line));
            }
        } catch (IOException e) {
            LOG.error("error reading file {} - starting new high score.", filename);
        }
        return highscores;
    }

    /**
     * Displays the high score table
     */
    public void displayHighscores(Collection<HighscoreEntry> highscores) {
        // TODO combine with Quantity.values

        String[] columnNames = getTableHeader();
        String[][] table = new String[maxRankForHighscore][columnNames.length];
        int row = 0;
        for (HighscoreEntry entry : highscores) {
            if (row > maxRankForHighscore) {
                break;
            }
            table[row][0] = String.format("%d", row + 1);
            table[row][1] = String.format("%s", entry.getPlayerName());
            for (Quantity quantity : Quantity.values()) {
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

    private static String[] getTableHeader() {
        String[] columnsHeaders = new String[Quantity.values().length + 2];
        int col = 0;
        columnsHeaders[col++] = "Rank";
        columnsHeaders[col++] = "Name";
        for (Quantity quantity : Quantity.values()) {
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
        highscoreEntry.setQuantity(Quantity.TOTAL_SIMULATION_TIME, simulationTime);
        highscoreEntry.setQuantity(Quantity.TOTAL_TRAVEL_TIME, roadNetwork.totalVehicleTravelTime());
        highscoreEntry.setQuantity(Quantity.TOTAL_TRAVEL_DISTANCE, roadNetwork.totalVehicleTravelDistance()
                * Units.M_TO_KM);
        highscoreEntry.setQuantity(Quantity.TOTAL_FUEL_USED_LITERS, roadNetwork.totalVehicleFuelUsedLiters());
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
