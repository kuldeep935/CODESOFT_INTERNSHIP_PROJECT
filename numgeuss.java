import java.io.*;
import java.util.*;

public class numgeuss{
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();
    private static final String PROFILE_FILE = "profiles.txt";
    private static Map<String, PlayerProfile> profiles = new HashMap<>();

    public static void main(String[] args) {
        loadProfiles();
        System.out.println("-------------------------------------------------");
        System.out.println("Welcome to the Professional Number Guessing Game!");
        System.out.println("-------------------------------------------------");

        String username = loginOrRegister();

        boolean playAgain = true;
        while (playAgain) {
            System.out.println("\nSelect Difficulty Level:");
            System.out.println("1. Easy (1-50, Unlimited Attempts)");
            System.out.println("2. Medium (1-100, 10 Attempts)");
            System.out.println("3. Hard (1-200, 5 Attempts)");
            System.out.print("Enter your choice (1/2/3): ");
            int difficulty = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            int maxRange = switch (difficulty) {
                case 1 -> 50;
                case 2 -> 100;
                case 3 -> 200;
                default -> 100;
            };

            int maxAttempts = switch (difficulty) {
                case 1 -> Integer.MAX_VALUE;
                case 2 -> 10;
                case 3 -> 5;
                default -> 10;
            };

            System.out.print("Enter the number of rounds to play: ");
            int totalRounds = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            playGame(username, maxRange, maxAttempts, totalRounds);

            System.out.print("\nDo you want to play again? (yes/no): ");
            playAgain = scanner.nextLine().trim().equalsIgnoreCase("yes");
        }

        saveProfiles();
        System.out.println("Thanks for playing! Goodbye!");
    }

    private static String loginOrRegister() {
        System.out.print("\nDo you already have a profile? (yes/no): ");
        boolean hasProfile = scanner.nextLine().trim().equalsIgnoreCase("yes");

        if (hasProfile) {
            System.out.print("Enter your username: ");
            String username = scanner.nextLine().trim();
            if (profiles.containsKey(username)) {
                System.out.println("Welcome back, " + username + "!");
                return username;
            } else {
                System.out.println("No profile found for that username. Creating a new profile.");
                return createProfile();
            }
        } else {
            return createProfile();
        }
    }

    private static String createProfile() {
        System.out.print("Enter a username to register: ");
        String username = scanner.nextLine().trim();
        profiles.put(username, new PlayerProfile(username));
        System.out.println("Profile created successfully! Welcome, " + username + "!");
        return username;
    }

    private static void playGame(String username, int maxRange, int maxAttempts, int totalRounds) {
        PlayerProfile profile = profiles.get(username);
        int totalScore = 0;

        for (int round = 1; round <= totalRounds; round++) {
            System.out.println("\n--- Round " + round + " ---");
            int numberToGuess = random.nextInt(maxRange) + 1;
            boolean guessedCorrectly = false;
            int attempts = 0;

            System.out.println("Guess the number between 1 and " + maxRange + ".");
            System.out.println("You have " + (maxAttempts == Integer.MAX_VALUE ? "unlimited" : maxAttempts) + " attempts.");

            while (attempts < maxAttempts && !guessedCorrectly) {
                System.out.print("Enter your guess: ");
                int guess = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                attempts++;

                if (guess == numberToGuess) {
                    System.out.println("Correct! You guessed the number in " + attempts + " attempts.");
                    guessedCorrectly = true;
                    int score = (maxRange / attempts) + (maxAttempts - attempts) * 10;
                    totalScore += score;
                    profile.addWin();
                } else if (guess < numberToGuess) {
                    System.out.println("Too low! Try again.");
                } else {
                    System.out.println("Too high! Try again.");
                }

                // Provide hints
                if (!guessedCorrectly && Math.abs(guess - numberToGuess) <= 10) {
                    System.out.println("Hint: You're within 10 of the correct number!");
                }
                if (!guessedCorrectly && attempts == maxAttempts / 2) {
                    System.out.println("Hint: The number is " + (numberToGuess % 2 == 0 ? "even." : "odd."));
                }
            }

            if (!guessedCorrectly) {
                System.out.println("You ran out of attempts! The correct number was " + numberToGuess + ".");
                profile.addLoss();
            }
        }

        System.out.println("\nGame Over! Your total score: " + totalScore);
        profile.addGame(totalRounds, totalScore);
    }

    @SuppressWarnings("unchecked") // Safe deserialization
    private static void loadProfiles() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PROFILE_FILE))) {
            profiles = (Map<String, PlayerProfile>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing profiles found. Starting fresh.");
        }
    }

    private static void saveProfiles() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PROFILE_FILE))) {
            oos.writeObject(profiles);
        } catch (IOException e) {
            System.out.println("Error saving profiles: " + e.getMessage());
        }
    }
}

class PlayerProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String username;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private int totalScore;

    public PlayerProfile(String username) {
        this.username = username;
    }

    public void addGame(int rounds, int score) {
        gamesPlayed += rounds;
        totalScore += score;
    }

    public void addWin() {
        wins++;
    }

    public void addLoss() {
        losses++;
    }

    @Override
    public String toString() {
        return "Player: " + username +
               "\nGames Played: " + gamesPlayed +
               "\nWins: " + wins +
               "\nLosses: " + losses +
               "\nTotal Score: " + totalScore;
    }
}
