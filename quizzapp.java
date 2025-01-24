import java.io.*;
import java.util.*;

class TriviaQuestion {
    String prompt;
    String[] choices;
    int correctChoice;
    String topic;

    public TriviaQuestion(String prompt, String[] choices, int correctChoice, String topic) {
        this.prompt = prompt;
        this.choices = choices;
        this.correctChoice = correctChoice;
        this.topic = topic;
    }
}

class PlayerProfile {
    String playerName;
    int gamesPlayed;
    int bestScore;
    double avgScore;
    Set<String> badges;

    public PlayerProfile(String playerName) {
        this.playerName = playerName;
        this.gamesPlayed = 0;
        this.bestScore = 0;
        this.avgScore = 0.0;
        this.badges = new HashSet<>();
    }

    public void updateProfile(int score, double accuracy) {
        gamesPlayed++;
        if (score > bestScore) {
            bestScore = score;
        }
        avgScore = ((avgScore * (gamesPlayed - 1)) + accuracy) / gamesPlayed;
        if (score >= 5) badges.add("Trivia Genius");
        if (accuracy == 100.0) badges.add("Flawless Victory");
    }

    @Override
    public String toString() {
        return "Player Name: " + playerName + "\n" +
               "Games Played: " + gamesPlayed + "\n" +
               "Best Score: " + bestScore + "\n" +
               "Average Accuracy: " + String.format("%.2f", avgScore) + "%\n" +
               "Badges: " + badges.toString();
    }
}

public class quizzapp {
    private static List<TriviaQuestion> questions = new ArrayList<>();
    private static Map<String, PlayerProfile> profiles = new HashMap<>();
    private static PlayerProfile currentPlayer;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        loadQuestions();
        loadProfiles();

        System.out.println("------------------------------------------------------------");
        System.out.println("          Welcome to the Ultimate Trivia Challenge!");
        System.out.println("     Test your knowledge across exciting topics and win!");
        System.out.println("------------------------------------------------------------");

        currentPlayer = loginPlayer(scanner);

        boolean continuePlaying = true;
        while (continuePlaying) {
            System.out.println("\n------------------------------------------------------------");
            System.out.println("Pick a topic:");
            System.out.println("1. Geography\n2. Technology\n3. Sports\n4. Exit");
            System.out.println("------------------------------------------------------------");
            int topicChoice = getValidInput(scanner, 1, 4);

            if (topicChoice == 4) break;

            startQuiz(scanner, topicChoice);

            System.out.println("\n------------------------------------------------------------");
            System.out.println("Your Profile:");
            System.out.println(currentPlayer);
            System.out.println("------------------------------------------------------------");

            System.out.println("Do you want to play another quiz? (yes/no)");
            String response = scanner.nextLine().trim();
            if (!response.equalsIgnoreCase("yes")) continuePlaying = false;
        }

        saveProfiles();
        System.out.println("\n------------------------------------------------------------");
        System.out.println("      Thank you for playing! Come back soon!");
        System.out.println("------------------------------------------------------------");
    }

    private static void startQuiz(Scanner scanner, int topicChoice) {
        String topic = switch (topicChoice) {
            case 1 -> "Geography";
            case 2 -> "Technology";
            case 3 -> "Sports";
            default -> "General Knowledge";
        };

        System.out.println("\n------------------------------------------------------------");
        System.out.println("Starting quiz on topic: " + topic);
        System.out.println("------------------------------------------------------------");

        int score = 0;
        int totalQuestions = 0;
        int correctAnswers = 0;

        for (TriviaQuestion question : questions) {
            if (!question.topic.equals(topic)) continue;

            System.out.println("\n" + question.prompt);
            for (int i = 0; i < question.choices.length; i++) {
                System.out.println((i + 1) + ". " + question.choices[i]);
            }

            System.out.println("------------------------------------------------------------");
            System.out.println("Enter your answer (1-4):");
            System.out.println("------------------------------------------------------------");

            int answer = getValidInput(scanner, 1, 4);

            totalQuestions++;
            if (answer == question.correctChoice) {
                System.out.println("Correct!");
                score++;
                correctAnswers++;
            } else {
                System.out.println("Wrong! The correct answer was: " + question.correctChoice);
            }
        }

        System.out.println("\n------------------------------------------------------------");
        System.out.println("Quiz Over!");
        System.out.println("Your Score: " + score);
        double accuracy = totalQuestions > 0 ? (correctAnswers * 100.0 / totalQuestions) : 0.0;
        System.out.println("Accuracy: " + String.format("%.2f", accuracy) + "%");
        System.out.println("------------------------------------------------------------");

        currentPlayer.updateProfile(score, accuracy);
    }

    private static void loadQuestions() {
        questions.add(new TriviaQuestion("Which is the largest desert in the world?", new String[]{"Sahara", "Arctic", "Antarctic", "Gobi"}, 3, "Geography"));
        questions.add(new TriviaQuestion("Who is known as the father of computers?", new String[]{"Alan Turing", "Charles Babbage", "John Von Neumann", "Bill Gates"}, 2, "Technology"));
        questions.add(new TriviaQuestion("Which country won the FIFA World Cup in 2018?", new String[]{"Germany", "Brazil", "France", "Argentina"}, 3, "Sports"));
        questions.add(new TriviaQuestion("What is the capital of Australia?", new String[]{"Sydney", "Melbourne", "Canberra", "Perth"}, 3, "Geography"));
    }

    private static void loadProfiles() {
        File file = new File("playerProfiles.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                PlayerProfile profile = new PlayerProfile(parts[0]);
                profile.gamesPlayed = Integer.parseInt(parts[1]);
                profile.bestScore = Integer.parseInt(parts[2]);
                profile.avgScore = Double.parseDouble(parts[3]);
                profiles.put(parts[0], profile);
            }
        } catch (IOException e) {
            System.err.println("Error loading player profiles: " + e.getMessage());
        }
    }

    private static void saveProfiles() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("playerProfiles.txt"))) {
            for (PlayerProfile profile : profiles.values()) {
                bw.write(profile.playerName + "," + profile.gamesPlayed + "," + profile.bestScore + "," + profile.avgScore + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving player profiles: " + e.getMessage());
        }
    }

    private static PlayerProfile loginPlayer(Scanner scanner) {
        System.out.println("\n------------------------------------------------------------");
        System.out.println("Enter your name:");
        System.out.println("------------------------------------------------------------");
        String playerName = scanner.nextLine().trim();
        if (!profiles.containsKey(playerName)) {
            PlayerProfile newProfile = new PlayerProfile(playerName);
            profiles.put(playerName, newProfile);
            System.out.println("New player profile created.");
            return newProfile;
        } else {
            System.out.println("Welcome back, " + playerName + "!");
            return profiles.get(playerName);
        }
    }

    private static int getValidInput(Scanner scanner, int min, int max) {
        while (true) {
            if (scanner.hasNextInt()) {
                int input = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (input >= min && input <= max) {
                    return input;
                }
            } else {
                scanner.nextLine(); // Clear invalid input
            }
            System.out.println("Invalid input. Please try again:");
        }
    }
}
