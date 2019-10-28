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
        System.out.println("the chat is running now!");

        Actions.initialize();
        
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

            for(Command action : Actions.myCommands){
                if (action.com().equals(command)){
                    try{
                        action.exec(this, argument);
                    }catch (Exception e){
                        System.out.println("Exception " + e);
                        out.println("MESSAGE " + e.getMessage());
                    }
                    return true;
                }
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
                    System.out.println("ESPERANDO NOMBRE:");
                    myUser.name = in.nextLine();
                    System.out.println(myUser.name);
                    if(myUser.name==null || myUser.name.isEmpty()){continue;}
                    if(myUser.name.equalsIgnoreCase("quit")){return;}
                    out.println("NAMEACCEPTED " + myUser.name + " " + myUser.id);
                    out.println("SIZE " + myLobby.myBoard.size);
                    out.println("MINECOUNT " + myLobby.myBoard.minecount);
                    break;
                }

                //notify the lobby about this new player
                myLobby.send_message(myUser.name + " joined");
                myLobby.send_command("USERINFO " + myUser.info());

                if(myLobby.owner == myUser){
                    //send message to owner that we have to make some stuff
                    out.println("OWNER");
                    myLobby.send_message(myUser.name + " Is the owner of the lobby. Set the settings!!");
                }else{
                    out.println("NOTOWNER");
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
                    //send message to everyone except me
                    myLobby.send_command("USEREXIT " + myUser.id, myUser);
                    myLobby.remove_user(myUser);

                    if(myLobby.owner == myUser){
                        myLobby.owner = null;
                        //make the next if posible the owner
                        if(myLobby.myUsers.size() > 0){
                            myLobby.owner = myLobby.myUsers.get(0);
                            out.println("OWNER");
                            myLobby.send_message(myLobby.owner.name + " now is the new owner of the lobby!!!");
                        }
                    }
                    
                    //check if lobby still has players in
                    if(myLobby.myUsers.isEmpty()){
                        //destroy lobby since there is no more players in it
                        lobbyList.remove(myLobby);
                        //if timer is alive try to purgate it
                        myLobby.timer.cancel();
                        myLobby.timer.purge();
                    }else {
                        //send message to all other players if there are still players inside
                        myLobby.send_message(myUser.name + " has left");
                    }
                }
                try { socket.close(); } 
                catch (IOException e) { }
            }
        }
    }
}

