/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package match_the_word;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    
    final Socket SOCKET;
    final Scanner SCAN;
    
    private DataInputStream input;
    private DataOutputStream output;
    
    String name = "";
    int counter = 0;
    MatchTheWord game;
    boolean isLosggedIn;

    public ClientHandler(Socket socket, String name, MatchTheWord match_the_word) {
        this.SOCKET = socket;
        SCAN = new Scanner(System.in);
        this.name = name;
        isLosggedIn = true;
        counter = 0;

        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

        } catch (IOException ex) {
            Log("ClientHander => " + ex.getMessage());
        }
        this.game = match_the_word;
    }

    @Override
    public void run() {
        String received;
        ClientHandler.this.WriteMessage(output, "Your name is => " + name);

        String temporary = "";
        List<ClientHandler> clients = new ArrayList<>(Server.getClients());
        int size = clients.size();
        if (size > 1) {
            for (int i = 0; i < size - 1; i++) {
                temporary += clients.get(i).name;
            }
            ClientHandler.this.WriteMessage(output, "Online Clients:\n" + temporary);
        }

        while (true) {
            received = ReadMessage();
            if (received.equalsIgnoreCase(Constants.LOGOUT)) {
                this.isLosggedIn = false;
                closeSocket();
                closeStreams();
                break;
            }
            try {
                game.Check(received, this);
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        closeStreams();
    }
    
    public void ForwardMessageToAllClients(String message){        
        for(ClientHandler another_client : Server.getClients()){
            if(another_client.isLosggedIn){
                ClientHandler.this.WriteMessage(another_client.output, message);
            }
        }
    }

    private String ReadMessage() {
        String line = "";
        try {
            line = input.readUTF();
        } catch (IOException ex) {
            Log("read => " + ex.getMessage());
        }
        return line;
    }

    public void WriteMessage(String message) {
        ClientHandler.this.WriteMessage(output, message);
    }

    private void WriteMessage(DataOutputStream output, String message) {
        try {
            output.writeUTF(message);
        } catch (IOException ex) {
            Log("write => " + ex.getMessage());
        }
    }
    
    private void Log(String msg) {
        System.out.println(msg);
    }
    
    private void closeSocket() {
        try {
            SOCKET.close();
        } catch (IOException ex) {
            Log("closeSocket => " + ex.getMessage());
        }
    }
    
    private void closeStreams() {
        try {
            this.input.close();
            this.output.close();
        } catch (IOException ex) {
            Log("closeStreams => " + ex.getMessage());
        }
    }
}
