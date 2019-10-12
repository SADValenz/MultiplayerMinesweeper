package server.minesweepermultiplayer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 *
 * @author Josue Millan
 */
public class SMM {
    public static List<Lobby> lobbyList = new ArrayList<>();
    public static boolean DEBUG = true;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        System.out.println("the chat is running now!");
        //for now just test table generation 
        
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try(ServerSocket listener = new ServerSocket(59001)){
            while(true){
                pool.execute(new Handler(listener.accept()));
            }
        }
    }
    
    public static class Handler implements Runnable {
        public User myUser = null;
        public Lobby myLobby = null;
        public Socket socket = null;
        public Scanner in = null;
        public PrintWriter out = null;
        public boolean keep_running = true;

        //other useful stuff
        public void lobby_matchmaking() {
            //search for a open lobby
            for(Lobby l : lobbyList){
                if(l.open){
                    myLobby = l;
                    break;
                }
            }

            if(myLobby!=null){
                myLobby.add_user(myUser);
            }else{//create a new one
                myLobby = new Lobby();
                lobbyList.add(myLobby);
                myLobby.add_user_owner(myUser);
            }
        }

        //actions handler
        private boolean action_script(String command, String argument) {
            try {
                switch(command){
                    case "game_start":      return Actions.game_start(this);
                    case "restart":         return Actions.game_restart(this);
                    case "board_get":       return Actions.board_get(this);
                    case "board_get_dev":   return Actions.board_get_dev(this);
                    case "cell_reveal":     return Actions.cell_reveal(this, argument);
                    case "cell_flag":       return Actions.cell_flag(this, argument);
                    case "global":          return Actions.chat_global(this, argument);
                    case "lobby_info":      return Actions.lobby_info(this);
                    case "lobby_list":      return Actions.lobby_list(this);
                    case "user_list":       return Actions.user_list(this);
                }
            }catch (Exception e){
                System.out.println("Exception " + e);
                out.println("MESSAGE " + e.getMessage());
                return true;
            }
            return false;
        }
        
        public Handler(Socket socket){
            this.socket = socket;
        }
        
        public String read_line(){
            while(in.hasNextLine()){
                return in.nextLine();
            }
            return "";
        }

        public void print_line(String string) {
            out.println(string);
            out.flush();
        }
        
        public void run(){
            try {
                System.out.println("Running the socket");
                in = new Scanner(socket.getInputStream(), "utf-8");
                out = new PrintWriter(socket.getOutputStream(), true);

                //creates the user to use
                myUser = new User("",out);

                //trying to find a lobby
                lobby_matchmaking();

                //collect data for our user data
                while(true){
                    out.println("SUBMITNAME");
                    
                    myUser.name = in.nextLine();
                    byte[] bytes = myUser.name.getBytes();
                    for(byte bit : bytes){
                        System.out.println(Integer.toHexString(bit));
                    }
                    System.out.println("Recieved: (" + myUser.name + ")");
                    if(myUser.name==null || myUser.name.isEmpty()){continue;}
                    if(myUser.name.equalsIgnoreCase("quit")){return;}
                    out.println("NAMEACCEPTED " + myUser.name);
                    System.out.println("printed NAMEACCEPTED");
                    break;
                }

                //notify the lobby about this new player
                myLobby.send_message(myUser.name + " joined");

                if(myLobby.owner == myUser){
                    //send message to owner that we have to make some stuff
                    out.println("LOBBYSETTINGS");
                    myLobby.send_message(myUser.name + " you are the owner of the lobby. Set the settings!!");
                }

                //recieved actions here
                while(keep_running){
                    String input = in.nextLine();
                    
                    if(input.toLowerCase().startsWith("/")) {
                        int inp = input.indexOf(' ');
                        String command = inp > 1 ? input.toLowerCase().substring(1, inp) : input.toLowerCase().substring(1, input.length());
                        String argument = inp > 1 ? input.substring(inp+1, input.length()) : "";
                       
                        if (action_script(command, argument)) {continue;}
                    }

                    myLobby.send_message(myUser.name + ": " + input);
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if( out != null || myUser != null || myLobby != null ){
                    //if we are owner remove our self from the title and 
                    //remove from the lobby
                    myLobby.remove_user(myUser);

                    if(myLobby.owner == myUser){
                        myLobby.owner = null;
                        //make the next if posible the owner
                        if(myLobby.myUsers.size() > 0){
                            myLobby.owner = myLobby.myUsers.get(0);
                            myLobby.send_message(myUser.name + " now is the new owner of the lobby!!!");
                        }
                    }
                    
                    //check if lobby still has players in
                    if(myLobby.myUsers.isEmpty()){
                        //destroy lobby since there is no more players in it
                        lobbyList.remove(myLobby);
                    }else {
                        //send message to all other players if there are still players inside
                        myLobby.send_message(myUser.name + " has left");
                    }
                }
                try { socket.close(); } 
                catch (Exception e) { }
            }
        }
    }
}

