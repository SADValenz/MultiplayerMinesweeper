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

    public static List<Lobby> get_list() {
        return lobbyList;
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
                    case "field_reveal":    return Actions.field_reveal(this, argument);
                    case "field_flag":      return Actions.field_flag(this, argument);
                    case "global":          return Actions.chat_global(this, argument);
                    case "lobby_info":      return Actions.lobby_info(this);
                    case "lobby_list":      return Actions.lobby_list(this);
                    case "user_list":       return Actions.user_list(this);
                }
            }catch (Exception e){
                out.println("MESSAGE " + e.getMessage());
                return true;
            }
            return false;
        }

        public void action_send_message_lobby(String message) {
            //send a message to the current lobby players
            myLobby.myUsers.forEach((user) -> {
                user.out.println("MESSAGE " + message);
            });
        }

        public void action_send_message_global(String message) {
            //search all active lobbies and send the message to them
            lobbyList.forEach((lobby) -> {//iterate all lobbies
                lobby.myUsers.forEach((user) -> {//iterate all users
                    user.out.println("MESSAGE <Global><lobby:"+myLobby.id+"> " + message);
                });
            });
        }

        public Handler(Socket socket){
            this.socket = socket;
        }

        public void run(){
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                //creates the user to use
                myUser = new User("",out);

                //trying to find a lobby
                lobby_matchmaking();

                //collect data for our user data
                while(true){
                    out.println("SUBMITNAME");
                    myUser.name = in.nextLine();
                    if(myUser.name==null || myUser.name.isEmpty()){continue;}
                    if(myUser.name.equalsIgnoreCase("quit")){return;}
                    //confirm name stuff
                    out.println("NAMEACCEPTED " + myUser.name);
                    break;
                }

                //notify the lobby about this new player
                action_send_message_lobby(myUser.name + " joined");

                if(myLobby.owner == myUser){
                    //send message to owner that we have to make some stuff
                    out.println("LOBBYSETTINGS");
                    action_send_message_lobby(myUser.name + " you are the owner of the lobby. Set the settings!!");
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

                    action_send_message_lobby(myUser.name + ": " + input);
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
                            action_send_message_lobby(myUser.name + " now is the new owner of the lobby!!!");
                        }
                    }
                    
                    //check if lobby still has players in
                    if(myLobby.myUsers.isEmpty()){
                        //destroy lobby since there is no more players in it
                        lobbyList.remove(myLobby);
                    }else {
                        //send message to all other players if there are still players inside
                        action_send_message_lobby(myUser.name + " has left");
                    }
                }
                try { socket.close(); } 
                catch (Exception e) { }
            }
        }
    }
}

