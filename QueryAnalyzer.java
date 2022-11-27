import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.regex.*;

public class QueryAnalyzer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Format: QueryAnalyzer.java <insert filename here>");
            return;
        }

        try {
            if (args[0] == null) {
                throw new FileNotFoundException();
            }

            File dir = new File(".");
            File myFile = new File(dir.getPath() + "/" + args[0]);
            if (myFile.exists() && myFile.isFile()) {
                Scanner scan = new Scanner(myFile);
                int numQueries = 0;
                int numMutables = 0;
                int numFaulty = 0;
                int codeLine = 0;
                ArrayList<String> faultyLines = new ArrayList<>();
                while (scan.hasNextLine()) {
                    codeLine++;
                    String line = scan.nextLine();
                    String[] tempTokens = line.split(" ");
                    if (tempTokens[0].equals("//") || tempTokens[0].equals("/*") || tempTokens[0].equals("/**") || tempTokens[0].equals("*")) {
                        continue;
                    }

                    boolean active = false;
                    boolean faulty = false;
                    int numPlus = 0;
                    for (int i = 0; i < tempTokens.length; i++) {
                        if (active) {
                            if (tempTokens[i].equals("+")) {
                                if (i != tempTokens.length -1 && !tempTokens[i+1].equals("\"")) {
                                    faulty = true;
                                    numPlus++;
                                    if (numPlus % 2 == 1) {
                                        numMutables++;
                                        numFaulty++;
                                    }
                                }
                            } else if (tempTokens[i].matches("(.*)\\?(.*)")) {
                                numMutables++;
                            }
                        } else if (tempTokens[i].toLowerCase().equals("\"select") || tempTokens[i].toLowerCase().equals("select")) {
                            numQueries++;
                            active = true;
                        }
                    }
                    if (faulty) {
                        if (numPlus - 2 <= 0) {
                            faultyLines.add(Integer.toString(codeLine));
                        } else {
                            faultyLines.add(Integer.toString(codeLine) + " (" + Integer.toString(numPlus / 2 + 1) + ")");
                        }
                    }
                }
                scan.close();

                System.out.println("Number of SQL Queries: " + numQueries);
                System.out.println("Number of Mutable Variables: " + numMutables);
                System.out.println("Number of Vulnerabilities: " + numFaulty);

                if (numFaulty == 0) {
                    System.out.println("Good job!");
                } else {
                    System.out.print("Vulnerabilities in line(s): ");
                    for(int i = 0; i < faultyLines.size() - 1; i++) {
                        System.out.print(faultyLines.get(i) + ", ");
                    }
                    System.out.println(faultyLines.get(faultyLines.size() - 1));
                }
            } else {
                throw new FileNotFoundException();
            }
        } catch(FileNotFoundException fnf) {
            System.err.println("FNF: " + fnf.getMessage());
        }
    }
}
