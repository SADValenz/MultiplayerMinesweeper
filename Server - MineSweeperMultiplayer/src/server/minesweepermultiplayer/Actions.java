package server.minesweepermultiplayer;

import java.util.ArrayList;
import java.util.List;
import static server.minesweepermultiplayer.SMM.lobbyList;

/**
 *
 * @author Josue Millan
 */
interface Command{
    void permissions(SMM.Handler client) throws Exception;
    void exec(SMM.Handler client, String args) throws Exception;
    String param();
    String com();
    String desc();
}

public class Actions {
    public static List<Command> myCommands = new ArrayList<>();

    public static void initialize(){
        //set up all the commands that are going to be used
        myCommands.add(new Game_start());
        myCommands.add(new Game_restart());
        myCommands.add(new Board_size());
        myCommands.add(new Board_set_size());
        myCommands.add(new Board_set_minecount());
        myCommands.add(new Cell_reveal());
        myCommands.add(new Cell_set_flag());
        myCommands.add(new Cell_remove_flag());
        myCommands.add(new Chat_global());
        myCommands.add(new Player_data());
        myCommands.add(new My_gameid());
        myCommands.add(new Mouse_pos());
        myCommands.add(new Help());

        System.out.println("Actions Setup!");
    }

    public static int arg_int(String str) throws Exception{
        try{ return Integer.parseInt(str); }
        catch (Exception e) { throw new Exception("invalid arguments!"); }
    }

    public static String[] arg_split(String str, String c) throws Exception{
        try{ return str.split(c); }
        catch (Exception e) { throw new Exception("bad request"); }
    }

    public static class Game_start implements Command {

        public void permissions(SMM.Handler client) throws Exception{
            Permissions.not_game_start(client.myLobby);
            Permissions.is_owner(client.myLobby, client.myUser);
        }

        public void exec(SMM.Handler client, String args) throws Exception{
            this.permissions(client);
        
            System.out.println("trying to start game in lobby<" + client.myLobby.id + ">!");
            if(client.myLobby.myUsers.size()>=client.myLobby.lobby_minimum){
                client.myLobby.game_start();
            }else{
                client.myLobby.send_message("Cannot start the game just yet!");
            }
        }

        public String param(){ return ""; }
        public String desc(){ return "This should start the game"; }
        public String com() { return "game_start"; }

    }
    public static class Game_restart implements Command {

        public void permissions(SMM.Handler client) throws Exception{
            Permissions.game_start(client.myLobby);
            Permissions.is_owner(client.myLobby, client.myUser);
            Permissions.debug();
        }

        public void exec(SMM.Handler client, String args) throws Exception{
            this.permissions(client);

            System.out.println("trying to restart game in lobby<" + client.myLobby.id + ">!");
            client.myLobby.myBoard.generate_new_board();
            client.myLobby.send_message("Game Restart!!!");
        }

        public String param(){ return ""; }
        public String desc(){ return "This should restart the game"; }
        public String com() { return "game_restart"; }

    }
    public static class Board_size implements Command {

        public void permissions(SMM.Handler client) throws Exception {
            Permissions.game_start(client.myLobby);
        }

        public void exec(SMM.Handler client, String args) throws Exception{
            this.permissions(client);
            client.out.println("SIZE " + client.myLobby.myBoard.size);
        }

        public String param(){ return ""; }
        public String desc(){ return "Retreive the size of the board"; }
        public String com() { return "board_size"; }
    }
    public static class Board_set_size implements Command {

        public void permissions(SMM.Handler client) throws Exception {
            Permissions.not_game_start(client.myLobby);
            Permissions.is_owner(client.myLobby, client.myUser);
        }

        public void exec(SMM.Handler client, String args) throws Exception {
            this.permissions(client);

            Board board = client.myLobby.myBoard;

            int value = 0;
            //check what kind of arguments are we recieving 
            if(args.startsWith("-")){//means an option
                //check what argument
            }else{  //parse
                value = arg_int(args);
            }

            if (value > Board.size_min && value < Board.size_max) {
                board.size = value;
                client.myLobby.send_command("SIZE " + value);

                if(board.minecount >= board.size*board.size){
                    board.minecount = board.size*board.size-1;
                    client.myLobby.send_command("MINECOUNT " + board.minecount);
                }
            }else {
                throw new Exception("size not in range: [" + Board.size_min + "," + Board.size_max + "]");
            }
        }

        public String param(){ return "int : -add : -sub"; }
        public String desc(){ return "Set the size of the board"; }
        public String com() { return "set_size"; }
    }
    public static class Board_set_minecount implements Command {

        public void permissions(SMM.Handler client) throws Exception {
            Permissions.not_game_start(client.myLobby);
            Permissions.is_owner(client.myLobby, client.myUser);
        }

        public void exec(SMM.Handler client, String args) throws Exception {
            this.permissions(client);

            Board board = client.myLobby.myBoard;

            int value = 0;
            //check what kind of arguments are we recieving 
            if(args.startsWith("-")){//means an option
                //check what argument
            }else{  //parse
                value = arg_int(args);
            }

            if (value > 0 && value < board.get_total_size()) {
                board.minecount = value;
                client.myLobby.send_command("MINECOUNT " + value);
            }else {
                throw new Exception("mine must be more than 1 and less than the board size!");
            }
        }

        public String param(){ return "int : -add : -sub"; }
        public String desc(){ return "Set the mine count of the board"; }
        public String com() { return "set_minecount"; }
    }
    public static class Cell_reveal implements Command {

        public void permissions(SMM.Handler client) throws Exception {
            Permissions.game_start(client.myLobby);
            Permissions.is_alive(client.myUser);
        }

        public void exec(SMM.Handler client, String args) throws Exception{
            permissions(client);

            Lobby myLobby = client.myLobby;
            Board myBoard = client.myLobby.myBoard;
            User myUser = client.myUser;

            String[] arg = arg_split(args," ");
            if(arg.length < 2){ throw new Exception("Not enough arguments!!"); }

            int x = arg_int(arg[0]), y = arg_int(arg[1]);

            Permissions.first_cell(myLobby, myUser, x, y);

            Cell cell = myBoard.get_cell(x,y);
            if( cell == null ){ throw new Exception("Coords are out of bounds!!!"); }

            List<Cell> reveals = cell.reveal(myUser);
            reveals.forEach((revealed) -> {
                myLobby.send_command("CELLREVEAL " + revealed.x + " " + revealed.y + " " + revealed.value + " " + myUser.id_game + " " + revealed.GAMEMAKER_DEPTH);
                if(revealed.value == -1){//if mine kill the player
                    myLobby.kill_user(myUser);
                }else{ myBoard.unlocked++; }//change this to do this inside of the cell

                //check for win
                if(myLobby.check_win()){ myLobby.game_end(); } 

                ///Check for soflock
                myLobby.check_softlock();
            });
        }

        public String param(){ return "int(x), int(y)"; }
        public String desc(){ return "Reveal a Cell with the given coords"; }
        public String com() { return "cell_reveal"; }

    }
    public static class Cell_set_flag implements Command {

        public void permissions(SMM.Handler client) throws Exception {
            Permissions.game_start(client.myLobby);
            Permissions.is_alive(client.myUser);
            Permissions.have_flags(client.myLobby.myBoard, client.myUser);
        }

        public void exec(SMM.Handler client, String args) throws Exception {
            this.permissions(client);

            String[] arg = arg_split(args, " ");
            if(arg.length < 2){ throw new Exception("Not enough arguments!!"); }

            int x = arg_int(arg[0]), y = arg_int(arg[1]);

            Permissions.first_cell(client.myLobby, client.myUser, x, y);

            Cell cell = client.myLobby.myBoard.get_cell(x, y);
            if( cell == null ) { throw new Exception(""); }

            if( cell.set_flag(client.myUser) ){
                client.myLobby.send_command("FLAG " + cell.x + " " + cell.y + " " + client.myUser.id_game);

                //check for win
                if(client.myLobby.check_win()){ client.myLobby.game_end(); } 
            }
        }

        public String param(){ return "int(x), int(y)"; }
        public String desc(){ return "flag a cell with the given coords"; }
        public String com() { return "cell_set_flag"; }
    }
    public static class Cell_remove_flag implements Command {

        public void permissions(SMM.Handler client) throws Exception {
            Permissions.game_start(client.myLobby);
            Permissions.is_alive(client.myUser);
        }

        public void exec(SMM.Handler client, String args) throws Exception { 
            this.permissions(client);

            String[] arg = arg_split(args, " ");
            if(arg.length < 2){ throw new Exception("Not enough arguments!!"); }

            int x = arg_int(arg[0]), y = arg_int(arg[1]);

            Permissions.first_cell(client.myLobby, client.myUser, x, y);
            Cell cell = client.myLobby.myBoard.get_cell(x,y);
            if( cell == null ) {throw new Exception("Coords are out of bounds!!!");}

            if ( cell.unset_flag(client.myUser) ) {
                client.myLobby.send_command("UNFLAG " + cell.x + " " + cell.y );
                if(client.myLobby.check_win()){ client.myLobby.game_end(); } //re send stuff just in case
            }

        }

        public String param(){ return "int(x), int(y)"; }
        public String desc(){ return "Remove the flag of a cell"; }
        public String com() { return "cell_remove_flag"; }
    }
    public static class Chat_global implements Command {

        public void permissions(SMM.Handler client) throws Exception { }

        public void exec(SMM.Handler client, String args) throws Exception { 
            lobbyList.forEach((lobby) -> {
                lobby.send_message("<Global><lobby:"+client.myLobby.id+"> "+ client.myUser.name+ ": " + args);
            });
        }

        public String param(){ return "string(message)"; }
        public String desc(){ return "Send a message to all active lobbies"; }
        public String com() { return "global"; }
    }
    public static class Player_data implements Command {

        public void permissions(SMM.Handler client) throws Exception { }

        public void exec(SMM.Handler client, String args) throws Exception {
            client.myLobby.myUsers.forEach((user) ->{
                client.out.println("USERINFO " + user.info());
            });
        }

        public String param(){ return ""; }
        public String desc(){ return "Get for all player's info"; }
        public String com() { return "ask_player_data"; }
    }
    public static class My_gameid implements Command {

        public void permissions(SMM.Handler client) throws Exception {
            Permissions.game_start(client.myLobby);
        }

        public void exec(SMM.Handler client, String args) throws Exception {
            this.permissions(client);

            client.out.println("MYGAMEID " + client.myUser.id_game);
        }

        public String param(){ return ""; }
        public String desc(){ return "Get your game id"; }
        public String com() { return "my_gameid"; }
    }
    public static class Mouse_pos implements Command {
        public void permissions(SMM.Handler client) throws Exception {
            Permissions.game_start(client.myLobby);
        }

        public void exec(SMM.Handler client, String args) throws Exception {
            this.permissions(client);

            String[] arg = arg_split(args, " ");

            int x = arg_int(arg[0]), y = arg_int(arg[1]);

            client.myLobby.send_command("MOUSEPOS " + x + " " + y + " " + client.myUser.id_game, client.myUser);
        }

        public String param(){ return "int(x), int(y)"; }
        public String desc(){ return "Send your mouse position to all other players!"; }
        public String com() { return "mouse_pos"; }
    }
    public static class Help implements Command {

        public void permissions(SMM.Handler client) throws Exception { }

        public void exec(SMM.Handler client, String str) throws Exception {
            //return a list of all actions
            Actions.myCommands.forEach((action) ->{
                client.myUser.out.println("MESSAGE " + action.com() + " : " + action.param());
                //client.myUser.out.println("MESSAGE     " + action.desc());
            });
        }

        public String param(){ return "Help"; }
        public String desc(){ return "Get all commands, or a description of an action"; }
        public String com() { return "help"; }
    }

    public static boolean my_flags(SMM.Handler client) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
