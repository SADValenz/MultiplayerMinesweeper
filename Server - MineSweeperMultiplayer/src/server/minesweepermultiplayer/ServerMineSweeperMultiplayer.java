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
public class ServerMineSweeperMultiplayer {
    private static List<Lobby> lobbyList = new ArrayList<>(); 
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
        private User myUser = null;
        private Lobby myLobby = null;
        private Socket socket = null;
        private Scanner in = null;
        private PrintWriter out = null;
        private boolean keep_running = true;

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
        private boolean action_game_script(String command,String argument) {
            switch(command){
                case "getboard":
                    for(int x = 0; x < myLobby.size; x++){
                        String info = "";
                        for(int y = 0; y < myLobby.size; y++){
                            Lobby.Field field = myLobby.grid[x][y];
                            if(field.visibility == 1){ info += (field.value == -1 ? "☼" : field.value) + " "; } 
                            else { 
                                info += (field.visibility == 0 ? "█" : "↑") + " ";
                            }
                        }
                        out.println("MESSAGE " + info);
                    }
                return true;
                case "getboard_dev":
                    for(int x = 0; x < myLobby.size; x++){
                        String info = "";
                        for(int y = 0; y < myLobby.size; y++){
                            Lobby.Field field = myLobby.grid[x][y];
                            info += (field.value == -1 ? "☼" : field.value) + " ";
                        }
                        out.println("MESSAGE " + info);
                    }
                return true;
                case "field_reveal":

                return true;
            }
            return false;
        }

        private boolean action_owner_script(String command, String argument) {
            //if the game is not setup just yet then
            //send information about he need to send the information
            if(!myLobby.started) {
                //set up the lobby settings
                //and start the game when there is enough players
                //wait for data to be retrieved
                switch(command){
                    case "startgame":
                        System.out.println("trying to start game in lobby<" + myLobby.id + ">!");
                        if(myLobby.myUsers.size()>=myLobby.lobby_minumum){
                            myLobby.started = true;
                            myLobby.open = false;
                            //for now user default settings
                            myLobby.set_grid_size(8);
                            myLobby.minecount = 6;

                            myLobby.generate_new_board();
                            action_send_message_lobby("Game Started!!!");
                        }else{
                            action_send_message_lobby("Cannot start the game just yet!");
                        }
                    return true;
                }
            }else {
                //do normal owner actions if the game have started()

            }
            //always actions

            return false;
        }

        private boolean action_chat_script(String command, String argument) {
            switch(command){
                case "global": 
                    System.out.println("sending global message by: (" + myUser.name + ") message: " + argument);
                    action_send_message_global(myUser.name + ": " + argument);
                return true;
                case "lobbyinfo": 
                    System.out.println("requesting lobby info by: " + myUser.name);
                    out.println("MESSAGE ##########################");
                    out.println("MESSAGE Lobby Settings: ");
                    out.println("MESSAGE   >Id = " + myLobby.id);
                    out.println("MESSAGE   >User count = " + myLobby.myUsers.size());
                    out.println("MESSAGE   >Owner = " + (myLobby.owner.equals(myUser) ? "Is your self!" : "(" + myLobby.owner.name) + ")");
                    out.println("MESSAGE Lobby Status:");
                    out.println("MESSAGE   >Open = " + myLobby.open);
                    out.println("MESSAGE   >Game in progress = " + myLobby.started);
                    out.println("MESSAGE ##########################");
                return true;
                case "lobbylist":
                    System.out.println("requesting lobby list by: " + myUser.name);
                    out.println("MESSAGE ##########################");
                    out.println("MESSAGE Lobby List: ");
                    lobbyList.forEach((lobby) -> {
                        out.println("MESSAGE " + lobby.id + " Owner: (" + lobby.owner.name + ") open: " + lobby.open);
                    });
                    out.println("MESSAGE ##########################");
                return true;
                case "userlist":
                    System.out.println("requesting user list by: " + myUser.name);
                    out.println("MESSAGE ##########################");
                    out.println("MESSAGE User list: ");
                    myLobby.myUsers.forEach((user) -> {
                        out.println("MESSAGE (" + user.name + ")" + (user.equals(myLobby.owner) ? " is the owner!":""));
                    });
                    out.println("MESSAGE ##########################");
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
                myUser = new User("",in,out);

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
                    action_send_message_lobby(myUser.name + " you are the owner of the lobby. Setup it!!");
                }

                //recieved actions here
                while(keep_running){
                    String input = in.nextLine();
                    
                    //state of the game...
                    //pre game
                    if(input.toLowerCase().startsWith("/")) {
                        int inp = input.indexOf(' ');
                        String command = inp > 1 ? input.toLowerCase().substring(1, inp) : input.toLowerCase().substring(1, input.length());
                        String argument = inp > 1 ? input.substring(inp+1, input.length()) : "";
                       
                        //if any of this actions succed
                        //then continue with the next input
                        if(myLobby.started) {//accept game commands
                            if (action_game_script(command, argument)) {continue;}
                        }
                        
                        if(myLobby.owner.equals(myUser)) {//user owner commands
                            if (action_owner_script(command, argument)) {continue;}
                        }

                        //look if it is a chat command
                        if (action_chat_script(command, argument)) {continue;}
                    }

                    //if is not any of those then this means that we recieved a normal message
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
