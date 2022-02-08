import org.apfloat.Apfloat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * The code in this file is inspired by the simple cookie clicker game in the link
 * https://codereview.stackexchange.com/questions/184892/cookie-clicker-in-java/184940.
 * */

public class PlanetClicker extends JFrame implements Serializable {
    // Non-graphical variable (for saved game data)
    GameData gameData;

    // Graphical variables
    int numberOfColumns = 8;
    JPanel container;
    JLabel energyLabel;
    JLabel gemLabel;
    JButton gainResourcesButton;
    JLabel clickerLabel;
    JButton increaseClickerButton;
    JButton saveButton;

    public PlanetClicker() {
        try {
            gameData = loadGameData();
        }
        catch (Exception ex) {
            gameData = new GameData();
        }
        assert gameData != null;
        container = new JPanel();
        JScrollPane scrollPane = new JScrollPane(container);
        add(scrollPane);
        container.setLayout(new GridLayout(numberOfColumns, 1));
        energyLabel = new JLabel("Energy = " + gameData.getEnergy().toString());
        gemLabel = new JLabel("Gems = " + gameData.getGems().toString());
        gainResourcesButton = new JButton("Gain Resources");

        // Producing resources by hand
        gainResourcesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameData.setEnergy(gameData.getEnergy().add(gameData.getClicker()[0]));

                // Increase gems with 10% chance
                if (Math.random() <= 0.1) {
                    gameData.setGems(gameData.getGems().add(gameData.getClicker()[1]));
                }
            }
        });

        // Improve clicking production rate
        clickerLabel = new JLabel("Produce " + gameData.getClicker()[0] + " energy, " + gameData.getClicker()[1] + " gems with 10% chance!");
        increaseClickerButton = new JButton("Improve clicker (costs " + gameData.getClickerEnergyCost() + " energy)");
        increaseClickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                increaseClicker();
            }

            public void increaseClicker() {
                if (gameData.getEnergy().compareTo(gameData.getClickerEnergyCost()) >= 0) {
                    gameData.setEnergy(gameData.getEnergy().subtract(gameData.getClickerEnergyCost()));

                    // Increase the clicker production rate and price
                    gameData.setClicker(new Apfloat[]{gameData.getClicker()[0].multiply(new Apfloat("5")),
                            gameData.getClicker()[1].multiply(new Apfloat("5"))});
                    gameData.setClickerEnergyCost(gameData.getClickerEnergyCost().multiply(new Apfloat("10")));
                    JOptionPane.showMessageDialog(null, "You have improved your clicker!");
                }
                else {
                    JOptionPane.showMessageDialog(null, "You have insufficient energy!");
                }
            }
        });

        // Updating current player's progress
        java.util.Timer updateProgress = new java.util.Timer();
        updateProgress.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                energyLabel.setText("Energy = " + gameData.getEnergy().toString());
                gemLabel.setText("Gems = " + gameData.getGems().toString());
                clickerLabel.setText("Produce " + gameData.getClicker()[0] + " energy, " + gameData.getClicker()[1] + " gems with 10% chance!");
                increaseClickerButton.setText("Improve clicker (costs " + gameData.getClickerEnergyCost() + " energy)");
            }
        }, 0, 25);

        // Unlocking more planets
        java.util.Timer unlockMorePlanets = new java.util.Timer();
        unlockMorePlanets.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Planet planet : gameData.getPlanets()) {
                    planet.unlock();
                }
            }
        }, 0, 2000);

        // Producing resources with planets
        java.util.Timer produceResources = new java.util.Timer();
        produceResources.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Apfloat totalEnergyGain = new Apfloat("0");
                Apfloat totalGemsGain = new Apfloat("0");

                for (Planet planet : gameData.getPlanets()) {
                    if (planet.isUnlocked) {
                        totalEnergyGain = totalEnergyGain.add(planet.energyPerSecond);
                        totalGemsGain = totalGemsGain.add(planet.gemsPerSecond);
                    }
                }

                gameData.setEnergy(gameData.getEnergy().add(totalEnergyGain));
                gameData.setGems(gameData.getGems().add(totalGemsGain));
            }
        }, 0, 1000);

        // Save game data
        saveButton = new JButton("SAVE");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveGameData();
                } catch (IOException ignored) {

                }
            }
        });

        container.add(energyLabel);
        container.add(gemLabel);
        container.add(gainResourcesButton);
        container.add(new JLabel(""));
        container.add(clickerLabel);
        container.add(increaseClickerButton);
        container.add(new JLabel(""));
        container.add(saveButton);
    }

    public void saveGameData() throws IOException {
        ObjectOutputStream save = new ObjectOutputStream(new FileOutputStream("Planet Clicker.dat"));
        save.writeObject(gameData);
        save.close();
    }

    public GameData loadGameData() throws IOException, ClassNotFoundException {
        ObjectInputStream restore = new ObjectInputStream(new FileInputStream("Planet Clicker.dat"));
        return (GameData) restore.readObject();
    }

    class Planet implements Serializable {
        // Non-graphical variables
        private final String name;
        private int level;
        private Apfloat energyPerSecond;
        private Apfloat gemsPerSecond;
        private Apfloat energyCost;
        private Apfloat gemsCost;
        private boolean isUnlocked;

        // Graphical variables
        JLabel label;
        JButton button;

        public Planet(String name, Apfloat energyPerSecond, Apfloat gemsPerSecond,
                      Apfloat energyCost, Apfloat gemsCost) {
            // Non-graphical variables
            this.name = name;
            this.level = 0;
            this.energyPerSecond = energyPerSecond;
            this.gemsPerSecond = gemsPerSecond;
            this.energyCost = energyCost;
            this.gemsCost = gemsCost;
            isUnlocked = false;

            // Graphical variables
            label = new JLabel();
            button = new JButton();
            button.addActionListener(e -> improve());
        }

        public void unlock() {
            if (gameData.getEnergy().compareTo(energyCost) >= 0 && gameData.getGems().compareTo(gemsCost) > 0 && !isUnlocked) {
                numberOfColumns += 3;
                container.setLayout(new GridLayout(numberOfColumns, 1));
                container.add(new JLabel(""));
                container.add(label);
                container.add(button);
                isUnlocked = true;
                display();
            }
        }

        public void improve() {
            if (gameData.getEnergy().compareTo(energyCost) >= 0 && gameData.getGems().compareTo(gemsCost) >= 0 &&
                    isUnlocked) {
                gameData.setEnergy(gameData.getEnergy().subtract(energyCost));
                gameData.setGems(gameData.getGems().subtract(gemsCost));
                level++;
                energyCost = energyCost.multiply(new Apfloat("10"));
                if (gemsCost.equals(new Apfloat("0"))) {
                    gemsCost = new Apfloat("1");
                }
                else {
                    gemsCost = gemsCost.multiply(new Apfloat("10"));
                }
                energyPerSecond = energyPerSecond.multiply(new Apfloat("5"));
                if (gemsPerSecond.equals(new Apfloat("0"))) {
                    gemsPerSecond = new Apfloat("0.1");
                }
                else {
                    gemsPerSecond = gemsPerSecond.multiply(new Apfloat("5"));
                }
                JOptionPane.showMessageDialog(null, "You have improved " + name + " planet!");
            }
            else {
                JOptionPane.showMessageDialog(null, "You have insufficient energy or gems!");
            }

            display();
        }

        public void display() {
            label.setText(name + "(level " + level + "), Energy per second: " + energyPerSecond + ", Gems per second: " + gemsPerSecond);
            button.setText("Improve (costs: " + energyCost + " energy, " + gemsCost + " gems)");
        }
    }

    class GameData implements Serializable {
        // Class attributes
        private Apfloat energy;
        private Apfloat gems;
        private Apfloat[] clicker;
        private Apfloat clickerEnergyCost;
        private ArrayList<Planet> planets;

        public GameData() {
            energy = new Apfloat("0");
            gems = new Apfloat("0");
            clicker = new Apfloat[]{new Apfloat("1"), new Apfloat("0.1")}; // 0 = energy clicker, 1 = gem clicker
            clickerEnergyCost = new Apfloat("20");
            planets = new ArrayList<>();
            // Adding a list of planets in the game.
            planets.add(new Planet("Cocheiphus", new Apfloat("5"), new Apfloat("0"),
                    new Apfloat("500"), new Apfloat("0")));
            planets.add(new Planet("Lelviuyama", new Apfloat("50"), new Apfloat("1"),
                    new Apfloat("50000"), new Apfloat("5")));
            planets.add(new Planet("Ankagua", new Apfloat("5000"), new Apfloat("10"),
                    new Apfloat("5e7"), new Apfloat("50")));
            planets.add(new Planet("Pisillon", new Apfloat("5e6"), new Apfloat("1000"),
                    new Apfloat("5e11"), new Apfloat("5000")));
            planets.add(new Planet("Yiothea", new Apfloat("5e10"), new Apfloat("1e6"),
                    new Apfloat("5e16"), new Apfloat("5e6")));
            planets.add(new Planet("Tecarro", new Apfloat("5e15"), new Apfloat("1e10"),
                    new Apfloat("5e22"), new Apfloat("5e10")));
            planets.add(new Planet("Phituzuno", new Apfloat("5e21"), new Apfloat("1e15"),
                    new Apfloat("5e29"), new Apfloat("5e15")));
            planets.add(new Planet("Niatis", new Apfloat("5e28"), new Apfloat("1e21"),
                    new Apfloat("5e37"), new Apfloat("5e21")));
            planets.add(new Planet("Delreiwei", new Apfloat("5e36"), new Apfloat("1e28"),
                    new Apfloat("5e46"), new Apfloat("5e28")));
            planets.add(new Planet("Ubatera", new Apfloat("5e45"), new Apfloat("1e36"),
                    new Apfloat("5e56"), new Apfloat("5e36")));
            planets.add(new Planet("Lanrarvis", new Apfloat("5e55"), new Apfloat("1e45"),
                    new Apfloat("5e67"), new Apfloat("5e45")));
            planets.add(new Planet("Ephillon", new Apfloat("5e66"), new Apfloat("1e55"),
                    new Apfloat("5e79"), new Apfloat("5e55")));
            planets.add(new Planet("Zavis", new Apfloat("5e78"), new Apfloat("1e66"),
                    new Apfloat("5e92"), new Apfloat("5e66")));
            planets.add(new Planet("Remia", new Apfloat("5e91"), new Apfloat("1e78"),
                    new Apfloat("5e106"), new Apfloat("5e78")));
            planets.add(new Planet("Bruagawa", new Apfloat("5e105"), new Apfloat("1e91"),
                    new Apfloat("5e121"), new Apfloat("5e91")));
            planets.add(new Planet("Bramoter", new Apfloat("5e120"), new Apfloat("1e105"),
                    new Apfloat("5e137"), new Apfloat("5e105")));
            planets.add(new Planet("Lengamia", new Apfloat("5e136"), new Apfloat("1e120"),
                    new Apfloat("5e154"), new Apfloat("5e120")));
            planets.add(new Planet("Chulmeithea", new Apfloat("5e153"), new Apfloat("1e136"),
                    new Apfloat("5e172"), new Apfloat("5e136")));
            planets.add(new Planet("Kadrone", new Apfloat("5e171"), new Apfloat("1e153"),
                    new Apfloat("5e191"), new Apfloat("5e153")));
            planets.add(new Planet("Linkurn", new Apfloat("5e190"), new Apfloat("1e171"),
                    new Apfloat("5e211"), new Apfloat("5e171")));
        }

        public Apfloat getEnergy() {
            return energy;
        }

        public void setEnergy(Apfloat energy) {
            this.energy = energy;
        }

        public Apfloat getGems() {
            return gems;
        }

        public void setGems(Apfloat gems) {
            this.gems = gems;
        }

        public Apfloat[] getClicker() {
            return clicker;
        }

        public void setClicker(Apfloat[] clicker) {
            this.clicker = clicker;
        }

        public Apfloat getClickerEnergyCost() {
            return clickerEnergyCost;
        }

        public void setClickerEnergyCost(Apfloat clickerEnergyCost) {
            this.clickerEnergyCost = clickerEnergyCost;
        }

        public ArrayList<PlanetClicker.Planet> getPlanets() {
            return planets;
        }

        public void setPlanets(ArrayList<PlanetClicker.Planet> planets) {
            this.planets = planets;
        }
    }

    public static void main(String[] args) {
        PlanetClicker planetClicker = new PlanetClicker();
        planetClicker.setTitle("Planet Clicker");
        planetClicker.setSize(800, 400);
        planetClicker.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        planetClicker.setVisible(true);
    }
}


