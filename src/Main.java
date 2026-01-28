/**
 * Nightpass - Algorithmic Survival Card Game
 * * This class serves as the main entry point for the application. It handles
 * file I/O operations, parses battle commands, and orchestrates the game loop
 * using a custom-built AVL Tree for O(log n) optimization.
 * * TECHNICAL HIGHLIGHTS:
 * =====================
 * - Implements a strict "No HashMap" constraint to demonstrate algorithmic efficiency.
 * - Utilizes a self-balancing Binary Search Tree (AVL) for dynamic data management.
 * - Processes ~550k commands under strict time constraints (<6s).
 * * USAGE:
 * ======
 * Compile: javac *.java
 * Run:     java Main <input_file_path> <output_file_path>
 * * AUTOMATED TESTING:
 * ==================
 * The project includes a Python test runner for benchmarking:
 * python test_runner.py --benchmark --verbose
 * @author Melih Efe Sönmez
 */

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.math.*;

public class Main {
    public static void main(String[] args) {

        // Check command line arguments
        if (args.length != 2) {
            System.out.println("Usage: java Main <input_file> <output_file>");
            System.out.println("Example: java Main ../testcase_inputs/test.txt ../output/test.txt");
            return;
        }

        String inFile = args[0];
        String outFile = args[1];

        // Initialize file reader
        Scanner reader = null;
        try {
            reader = new Scanner(new File(inFile));
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found: " + inFile);
            e.printStackTrace();
            return;
        }

        // Initialize file writer
        FileWriter writer = null;
        try {
            writer = new FileWriter(outFile);
        } catch (IOException e) {
            System.out.println("Writing error: " + outFile);
            e.printStackTrace();
            if (reader != null)
                reader.close();
            return;
        }

        // Helper Variables
        int surScore = 0; // The Survivor score
        int strScore = 0; // The Stranger score

        int entryOrder = 0; // Entry order of a card to the deck
        int deckSize = 0; // Size of the deck

        AVLTree attackTree = new AVLTree(false); // Sorted by attack
        AVLTree healthTree = new AVLTree(true);  // Sorted by health

        // Process commands line by line
        try {
            while (reader.hasNext()) {
                String line = reader.nextLine();
                Scanner scanner = new Scanner(line);
                String command = scanner.next();
                String out = "";

                switch (command) {
                    case "draw_card": {
                        String name = "";
                        int att = 0;
                        int hp = 0;
                        if (scanner.hasNext())
                            name = scanner.next();
                        if (scanner.hasNext())
                            att = scanner.nextInt();
                        if (scanner.hasNext())
                            hp = scanner.nextInt();

                        Card card = new Card(name, att, hp,entryOrder);
                        entryOrder += 1;

                        // Inserts card to both trees
                        attackTree.insert(card);
                        healthTree.insert(card);
                        deckSize++; // Increases deck size

                        out = drawCard(card);
                        break;
                    }
                    case "battle": {
                        int att = 0;
                        int hp = 0;
                        int heal = 0;
                        if (scanner.hasNext())
                            att = scanner.nextInt();
                        if (scanner.hasNext())
                            hp = scanner.nextInt();
                        if (scanner.hasNext())
                            heal = scanner.nextInt();

                        // Stores the priority number and card
                        int usedPriority = -1;
                        Card chosen = null;

                        // Tries P1 on attack tree
                        chosen = attackTree.findPriority1(att, hp);
                        if (chosen != null) {
                            usedPriority = 1;
                        } else {
                            // Tries P2 on health tree
                            chosen = healthTree.findPriority2(att, hp);
                            if (chosen != null) {
                                usedPriority = 2;
                            } else {
                                // Tries P3 on attack tree
                                chosen = attackTree.findPriority3(att, hp);
                                if (chosen != null) {
                                    usedPriority = 3;
                                } else {
                                    // Tries P4 on attack tree
                                    chosen = attackTree.findPriority4();
                                    if (chosen != null) {
                                        usedPriority = 4;
                                    }
                                }
                            }
                        }

                        int revivedNum = 0; // Alwas 0 in Type-1

                        if (chosen == null) {
                            strScore += 2;
                            // No cards to play
                            out = "No cards to play, " + revivedNum + " cards revived";
                        } else {
                            // Remove from both trees before applying stats changing
                            attackTree.delete(chosen);
                            healthTree.delete(chosen);
                            deckSize--; // Decreases deck size

                            // Survivor hits Stranger
                            int strNewH = hp - chosen.getAttackCur();
                            // Stranger hits Survivor
                            int surNewH = chosen.getHealthCur() - att;

                            // Scoring
                            if (strNewH <= 0) {
                                surScore += 2;
                            }
                            if (surNewH <= 0) {
                                strScore += 2;
                            }
                            if (strNewH > 0 && strNewH < hp) {
                                surScore += 1;
                            }
                            if (surNewH > 0 && surNewH < chosen.getHealthCur()) {
                                strScore += 1;
                            }

                            // Update my card's health
                            chosen.setHealthCur(Math.max(0, surNewH));

                            if (chosen.isInDeck()) {
                                // Recomputes current attack and new entry order
                                chosen.updateAttackAfterDamage();
                                chosen.setEntryOrder(entryOrder++);

                                // Inserts chosen card to both trees
                                attackTree.insert(chosen);
                                healthTree.insert(chosen);
                                deckSize++; // Increases deck size

                                out = battle(chosen, usedPriority, revivedNum, true);
                            } else {
                                out = battle(chosen, usedPriority, revivedNum, false);
                            }

                        }

                        break;
                    }
                    case "find_winning": {
                        out = findWinning(surScore, strScore);
                        break;
                    }
                    case "deck_count": {
                        out = deckCount(deckSize);
                        break;
                    }
                    case "steal_card": {
                        int att = 0;
                        int hp = 0;
                        if (scanner.hasNext())
                            att = scanner.nextInt();
                        if (scanner.hasNext())
                            hp = scanner.nextInt();

                        Card stolen = attackTree.findStealTarget(att, hp); // Finds the stolen card

                        if (stolen == null) {
                            out = "No card to steal";
                        } else {
                            // Remove from both trees
                            attackTree.delete(stolen);
                            healthTree.delete(stolen);
                            deckSize--; // Decreases tree size

                            out = stealCard(stolen);
                        }
                        break;
                    }
                    default: {
                        System.out.println("Invalid command: " + command);
                        scanner.close();
                        writer.close();
                        reader.close();
                        return;
                    }
                }

                scanner.close();

                try {
                    writer.write(out);
                    writer.write("\n"); // uncomment if each output needs to be in a new line
                } catch (IOException e2) {
                    System.out.println("Writing error");
                    e2.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println("Error processing commands: " + e.getMessage());
            e.printStackTrace();
        }

        // Clean up resources
        try {
            writer.close();
        } catch (IOException e2) {
            System.out.println("Writing error");
            e2.printStackTrace();
        }

        if (reader != null) {
            reader.close();
        }

        System.out.println("end");
        return;
    }

    // COMMANDS
    // Battle Commands Output methods
    private static String drawCard(Card card) {return "Added " + card.getName() + " to the deck";}
    private static String battle(Card chosen, int usedPriority, int revivedNum, boolean isSurAlive) {
        if (isSurAlive) {
            return "Found with priority " + usedPriority + ", Survivor plays " + chosen.getName() + ", the played card returned to deck, " + revivedNum + " cards revived";
        } else {
            return "Found with priority " + usedPriority + ", Survivor plays " + chosen.getName() + ", the played card is discarded, " + revivedNum + " cards revived";
        }
    }
    private static String stealCard(Card card) {return "The Stranger stole the card: " + card.getName();}

    // Queries Commands output methods
    private static String deckCount(int deckSize){return "Number of cards in the deck: " + deckSize;}
    private static String findWinning(int surScore, int strScore){
        if (surScore >= strScore){
            return "The Survivor, Score: " + surScore;
        } else {
            return "The Stranger, Score: " + strScore;
        }
    }

}
