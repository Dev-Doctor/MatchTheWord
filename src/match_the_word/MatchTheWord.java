package match_the_word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchTheWord {

    private String secret_word;
    private boolean playState;
    private String word;
    Server server;
    ArrayList<String> array_word;

    public MatchTheWord() {
        this.array_word = new ArrayList<>();
        this.word = "";
        this.playState = false;
    }
    
    private void Start() throws FileNotFoundException, IOException {
        playState = true;
        FileReader file_handler;
        file_handler = new FileReader("parole.txt");

        BufferedReader buffer_reader;
        buffer_reader = new BufferedReader(file_handler);

        String str;

        while (true) {
            str = buffer_reader.readLine();
            if (str == null) {
                break;
            }
            array_word.add(str);
        }

        int rnd = (int) (0 + (Math.random() * 650000));
        
        word = array_word.get(rnd);
        secret_word = HideWord(word);
        System.out.println(word);
    }
    
    public void Check(String message, ClientHandler client_handler) throws IOException {
        if (!playState) {
            if (message.equalsIgnoreCase(Constants.PREFIX + "start")) {
                Start();
                client_handler.ForwardMessageToAllClients(secret_word);
                return;
            }
            return;
        }

        if (message.equalsIgnoreCase(Constants.PREFIX + "jolly")) {
            client_handler.ForwardMessageToAllClients("Word: " + word + "\nGame Ended, write '!start' if you want to start another game");
            playState = false;
            return;
        }

        client_handler.counter++;
        
        if (message.equalsIgnoreCase(word)) {
            client_handler.ForwardMessageToAllClients(client_handler.name + " got the word in " + client_handler.counter + " tries! Write '!Start' if you want to play again or write '!exit' to leave");
            playState = false;
            CheckPoints(client_handler.name, client_handler.counter);
            client_handler.ForwardMessageToAllClients(VisualizeScoreboard());
            return;
        }

        for (int i = 0; i < word.length(); i++) {
            if (i >= message.length()) {
                continue;
            }

            if (word.charAt(i) == message.charAt(i)) {
                secret_word = secret_word.substring(0, i) + word.charAt(i) + secret_word.substring(i + 1);
            }
        }
        client_handler.ForwardMessageToAllClients(client_handler.name + "=> " + message);
        client_handler.ForwardMessageToAllClients("Word => " + secret_word);
    }
    
    private void CheckPoints(String name, int tries) {
        List<List<String>> scoreboard = new ArrayList<>();
        
        try ( Scanner scanner = new Scanner(new File("scoreboard.csv"));) {
            while (scanner.hasNextLine()) {
                scoreboard.add(getRecordFromLine(scanner.nextLine()));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MatchTheWord.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<String> record = new ArrayList<>();
        
        boolean found = false;
        
        for (int i = 0; i < scoreboard.size(); i++) {
            record = scoreboard.get(i);
            if (!record.get(0).equalsIgnoreCase(name)) {
                continue;
            }
            found = true;
            if (tries < Integer.parseInt(record.get(1))) {
                record.set(1, tries + "");
                UpdateScoreboard(scoreboard);
                return;
            }
        }
        if (found) {
            return;
        }
        
        record = new ArrayList<>();
        record.add(name);
        record.add(tries + "");
        
        scoreboard.add(record);
        UpdateScoreboard(scoreboard);
    }

    private List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<String>();
        try ( Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(";");
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }
    
    public boolean isPlaying() {
        return playState;
    }

    private String HideWord(String not_secret_word) {
        String secret_word = "";
        for (int i = 0; i < not_secret_word.length(); i++) {
            secret_word += '?';
        }
        return secret_word;
    }
    
    private String VisualizeScoreboard() {
        List<List<String>> scoreboard = new ArrayList<>();
        try ( Scanner scanner = new Scanner(new File("scoreboard.csv"));) {
            while (scanner.hasNextLine()) {
                scoreboard.add(getRecordFromLine(scanner.nextLine()));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MatchTheWord.class.getName()).log(Level.SEVERE, null, ex);
        }

        String temp = "Scoreboard:\n";
        for (int i = 0; i < scoreboard.size(); i++) {
            temp += i + 1 + ")" + scoreboard.get(i).get(0) + " " + scoreboard.get(i).get(1) + "\n";
        }
        return temp;
    }

    private void UpdateScoreboard(List<List<String>> scoreboard) {
        boolean ordinate = false;
        int i = 0;
        
        while (i < scoreboard.size() && !ordinate) {
            ordinate = true;
            for (int j = 0; j < scoreboard.size() - j; j++) {
                if (Integer.parseInt(scoreboard.get(j).get(1)) > Integer.parseInt(scoreboard.get(j + 1).get(1))) {
                    ordinate = false;
                    List<String> temporary = new ArrayList<String>();
                    temporary = scoreboard.get(j);
                    scoreboard.set(j, scoreboard.get(j + 1));
                    scoreboard.set(j + 1, temporary);
                }
            }
            i++;
        }
        writeToFile(ConvertToCSV(scoreboard));
    }

    private String ConvertToCSV(List<List<String>> scoreboard) {
        String csv = "";
        for (int i = 0; i < scoreboard.size(); i++) {
            csv += scoreboard.get(i).get(0) + ";" + scoreboard.get(i).get(1) + "\n";
        }
        return csv;
    }
    
    private void writeToFile(String str) {
        try {
            FileWriter file_writer = new FileWriter("scoreboard.csv");
            file_writer.write(str);
            file_writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
