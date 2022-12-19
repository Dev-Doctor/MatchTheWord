/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package match_the_word;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {
    static List<ClientHandler> clients;
    ServerSocket server_socket;
    static int numOfUsers = 0;
    Socket socket;
    MatchTheWord game;
    
    public Server(){
        clients = new ArrayList<>();
        try{
            server_socket = new ServerSocket(Constants.PORT);
        }catch(IOException ex){
            log("Server => " + ex.getMessage());
        }
        game = new MatchTheWord();
    }
    
    public static void main(String[] args){
        Server server = new Server();
        server.watiConnection();
    }
    
    private void watiConnection(){
        log("Server Running...");
        
        while(true){
            try{
                socket = server_socket.accept();
            }catch(IOException ex){
                log("waitConnection => " + ex.getMessage());
            }
            
            log("Client accepted => " + socket.getInetAddress());
            numOfUsers++;
            
            ClientHandler handler = new ClientHandler(socket, "User_" + numOfUsers, game);
            
            Thread thread = new Thread(handler);
            addClient(handler);
            thread.start();
        }
    }
    
    public static List<ClientHandler> getClients(){
        return clients;
    }

    private void addClient(ClientHandler client){
        clients.add(client);
    }
    private void log(String message) {
        System.out.println(message);
    }
    
}
