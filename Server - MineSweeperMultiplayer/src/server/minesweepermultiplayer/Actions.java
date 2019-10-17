package server.minesweepermultiplayer;

import java.util.List;
import static server.minesweepermultiplayer.SMM.lobbyList;

/**
 *
 * @author Josue Millan
 */
public class Actions {
    //Permissions functions

    private static void permission_game_start(Lobby lobby) throws Exception {
        if(!lobby.started)
            throw new Exception("El juego no a empezado.");
    }

    private static void permission_not_game_start(Lobby lobby) throws Exception {
        if(lobby.started)
            throw new Exception("El juego ya a empezado!");
    }

    private static void permission_is_owner(Lobby lobby, User user) throws Exception {
        if(!lobby.owner.equals(user))
            throw new Exception("No eres el dueño del lobby.");
    }

    private static void permission_is_alive(User user) throws Exception {
        if(!user.alive)
            throw new Exception("Estas muerto jaja xd.");
    }

    private static void permission_debug() throws Exception {
        if(!SMM.DEBUG)
            throw new Exception("No en modo debug.");
    }

    private static void permission_first_cell(Lobby lobby, User user, int x, int y) throws Exception {
        if(user.first_cell){
            //check what id player is
            System.out.println("Estamos en permission flag");
            System.out.println("Caso " + user.id_game);
            System.out.println("y: " + y + ", x: " + x + ", size: " + lobby.myBoard.size);
            switch(user.id_game){
                case 0: 
                    if(!(y==0 && x>=0 && x<lobby.myBoard.size)){
                        throw new Exception("not your section... just yet!");
                    }
                break;
                case 1: 
                    if(!(y==lobby.myBoard.size-1 && x>=0 && x<lobby.myBoard.size)){
                        throw new Exception("not your section... just yet!");
                    }
                break;
                case 2: 
                    if(!(y>0 && y<lobby.myBoard.size && x==0)){
                        throw new Exception("not your section... just yet!");
                    }
                break;
                case 3:
                    if(!(y>0 && y<lobby.myBoard.size && x==lobby.myBoard.size-1)){
                        throw new Exception("not your section... just yet!");
                    }
                break;
                default:
                throw new Exception("fuera de rango que onda?");
            }
        }
    }

    private static void permission_have_flags(Board board, User user) throws Exception {
        if(user.flags.size() >= board.minecount)
            throw new Exception("No tienes banderas!");
    }

    public static boolean game_start(SMM.Handler client) throws Exception {
        permission_not_game_start(client.myLobby);
        permission_is_owner(client.myLobby, client.myUser);
        //send arguments here like size and minecount

        System.out.println("trying to start game in lobby<" + client.myLobby.id + ">!");
        if(client.myLobby.myUsers.size()>=client.myLobby.lobby_minimum){
            client.myLobby.started = true;
            client.myLobby.open = false;

            client.myLobby.myBoard.generate_new_board();
            client.myLobby.send_message("Game Started!!!");
            client.myLobby.send_command("GAMESTART " + client.myLobby.myBoard.size + " " + client.myLobby.myBoard.minecount);
        }else{
            client.myLobby.send_message("Cannot start the game just yet!");
        }
        return true;
    }

    public static boolean game_restart(SMM.Handler client) throws Exception {
        permission_game_start(client.myLobby);
        permission_is_owner(client.myLobby, client.myUser);
        permission_debug();

        System.out.println("trying to restart game in lobby<" + client.myLobby.id + ">!");
        client.myLobby.myBoard.generate_new_board();
        client.myLobby.send_message("Game Restart!!!");
        return true;
    }

    public static boolean board_get(SMM.Handler client) throws Exception {
        permission_game_start(client.myLobby);

        for(int y = 0; y < client.myLobby.myBoard.size; y++){
            String info = "";
            for(int x = 0; x < client.myLobby.myBoard.size; x++){
                Cell cell = client.myLobby.myBoard.grid[x][y];
                if(cell.visibility == 1){ info += (cell.value == -1 ? "☼" : cell.value) + " "; } 
                else { 
                    info += (cell.visibility == 0 ? "█" : "↑") + " ";
                }
            }
            client.out.println("MESSAGE " + info);
        }
        return true;
    }

    public static boolean board_get_dev(SMM.Handler client) throws Exception {
        permission_game_start(client.myLobby);
        permission_debug();

        for(int x = 0; x < client.myLobby.myBoard.size; x++){
            String info = "";
            for(int y = 0; y < client.myLobby.myBoard.size; y++){
                Cell cell = client.myLobby.myBoard.grid[x][y];
                info += (cell.value == -1 ? "☼" : cell.value) + " ";
            }
            client.out.println("MESSAGE " + info);
        }
        return true;
    }

    public static boolean board_size(SMM.Handler client) throws Exception {
        permission_game_start(client.myLobby);

        client.out.println("SIZE " + client.myLobby.myBoard.size);
        return true;
    }

    public static boolean cell_reveal(SMM.Handler client, String argument) throws Exception {
        permission_game_start(client.myLobby);
        permission_is_alive(client.myUser);
        
        String[] argument_array = null;
        try{ argument_array  = argument.split(" "); }
        catch (Exception e) { throw new Exception("bad request"); }
        if(argument_array.length >= 2){
            int x = 0, y = 0;
            try{
                x = Integer.parseInt(argument_array[0]);
                y = Integer.parseInt(argument_array[1]);
            } catch (Exception e) { throw new Exception("invalid arguments!"); }
            //lets check the x and y
            permission_first_cell(client.myLobby, client.myUser, x, y);
            Cell cell = client.myLobby.myBoard.get_cell(x,y);
            if( cell != null ){
                List<Cell> cellList = cell.reveal(client.myUser);
                cellList.forEach((cell_) -> {
                    client.myLobby.send_command("CELLREVEAL " + cell_.x + " " + cell_.y + " " + cell_.value + " " + client.myUser.id_game + " " + cell_.GAMEMAKER_DEPTH);
                    if(cell_.value == -1){
                        client.myUser.alive = false;
                        client.myLobby.send_message(client.myUser.name + " Has Die!!!");
                        client.out.println("URDEAD");
                        client.myLobby.send_command("USERINFO " + client.myUser.info());
                    }else{
                        //add to the counter
                        client.myLobby.myBoard.unlocked++;
                        //check for win
                    }
                    if(client.myLobby.check_win()){ client.myLobby.send_command("GAMEEND"); }   
                    ///Check for soflock
                    client.myLobby.myUsers.forEach((user) ->{
                        if(user.first_cell){
                            if( !client.myLobby.myBoard.check_line(user.id_game) ){//if is free then unlock him
                                user.first_cell = false;
                            }
                        }
                    });
                    
                });
            }else { throw new Exception("Coords are out of bounds!!!"); }
        }else{ throw new Exception("Not enough arguments!!"); }
        return true;
    }

    public static boolean cell_set_flag(SMM.Handler client, String argument) throws Exception {
        permission_game_start(client.myLobby);
        permission_is_alive(client.myUser);
        permission_have_flags(client.myLobby.myBoard, client.myUser);

        String[] argument_array = null;
        try{ argument_array  = argument.split(" "); }
        catch (Exception e) { throw new Exception("bad request"); }
        if(argument_array.length >= 2){
            int x = 0, y = 0;
            try{
                x = Integer.parseInt(argument_array[0]);
                y = Integer.parseInt(argument_array[1]);
            } catch (Exception e) { throw new Exception("invalid arguments!"); }
            //check if inside the range
            permission_first_cell(client.myLobby, client.myUser, x, y);
            Cell cell = client.myLobby.myBoard.get_cell(x,y);
            if( cell != null ) {
                if ( cell.set_flag(client.myUser) ) {
                    client.myLobby.send_command("FLAG " + cell.x + " " + cell.y + " " + client.myUser.id_game);
                    if(client.myLobby.check_win()){ client.myLobby.send_command("GAMEEND"); }
                }
            }else { throw new Exception("Coords are out of bounds!!!"); }
        }else{ throw new Exception("Not enough arguments!!"); }
        return true;
    }

    public static boolean cell_remove_flag(SMM.Handler client, String argument) throws Exception {
        permission_game_start(client.myLobby);
        permission_is_alive(client.myUser);

        String[] argument_array = null;
        try{ argument_array  = argument.split(" "); }
        catch (Exception e) { throw new Exception("bad request"); }
        if(argument_array.length >= 2){
            int x = 0, y = 0;
            try{
                x = Integer.parseInt(argument_array[0]);
                y = Integer.parseInt(argument_array[1]);
            } catch (Exception e) { throw new Exception("invalid arguments!"); }
            //check if inside the range
            permission_first_cell(client.myLobby, client.myUser, x, y);
            Cell cell = client.myLobby.myBoard.get_cell(x,y);
            if( cell != null ) {
                if ( cell.unset_flag(client.myUser) ) {
                    client.myLobby.send_command("UNFLAG " + cell.x + " " + cell.y );
                }
            }else { throw new Exception("Coords are out of bounds!!!"); }
        }else{ throw new Exception("Not enough arguments!!"); }
        return true;
    }

    public static boolean chat_global(SMM.Handler client, String argument) {
        //check if there is a message later

        lobbyList.forEach((lobby) ->{
            lobby.send_message("<Global><lobby:"+client.myLobby.id+"> "+ client.myUser.name+ ": " + argument);
        });
        return true;
    }

    public static boolean lobby_info(SMM.Handler client) throws Exception {
        permission_debug();

        client.out.println("MESSAGE ##########################");
        client.out.println("MESSAGE Lobby Settings: ");
        client.out.println("MESSAGE   >Id = " + client.myLobby.id);
        client.out.println("MESSAGE   >User count = " + client.myLobby.myUsers.size());
        client.out.println("MESSAGE   >Owner = " + (client.myLobby.owner.equals(client.myUser) ? "Is your self!" : "(" + client.myLobby.owner.name) + ")");
        client.out.println("MESSAGE Lobby Status:");
        client.out.println("MESSAGE   >Open = " + client.myLobby.open);
        client.out.println("MESSAGE   >Game in progress = " + client.myLobby.started);
        client.out.println("MESSAGE ##########################");
        return true;
    }

    public static boolean lobby_list(SMM.Handler client) throws Exception{
        permission_debug();

        client.out.println("MESSAGE ##########################");
        client.out.println("MESSAGE Lobby List: ");
        SMM.lobbyList.forEach((lobby) -> {
            client.out.println("MESSAGE " + lobby.id + " Owner: (" + lobby.owner.name + ") open: " + lobby.open);
        });
        client.out.println("MESSAGE ##########################");
        return true;
    }

    public static boolean user_list(SMM.Handler client) throws Exception {
        permission_debug();

        client.out.println("MESSAGE ##########################");
        client.out.println("MESSAGE User list: ");
        client.myLobby.myUsers.forEach((user) -> {
            client.out.println("MESSAGE (" + user.name + ")" + (user.equals(client.myLobby.owner) ? " is the owner!":""));
        });
        client.out.println("MESSAGE ##########################");
        return true;
    }

    public static boolean ask_player_data(SMM.Handler client) throws Exception {
        client.myLobby.myUsers.forEach((user) ->{
            client.out.println("USERINFO " + user.info());
        });
        return true;
    }

    public static boolean my_gameid(SMM.Handler client) throws Exception {
        permission_game_start(client.myLobby);

        client.out.println("MYGAMEID " + client.myUser.id_game);
        return true;
    }

    public static boolean set_size(SMM.Handler client, String argument) throws Exception{
        permission_not_game_start(client.myLobby);
        permission_is_owner(client.myLobby, client.myUser);

        String[] argument_array = null;
        int size = 0;
        try{ argument_array  = argument.split(" "); }
        catch (Exception e) { throw new Exception("bad request"); }
        try{size = Integer.parseInt(argument_array[0]); }
        catch (Exception e) { throw new Exception("invalid arguments!"); }
        //check to be inside the aceptable ranges
        if(size > 2 && size < 18){
            client.myLobby.myBoard.size = size;
            client.myLobby.send_command("SIZE " + size);
            //correct number of mines in this case
            if(client.myLobby.myBoard.minecount >= client.myLobby.myBoard.size*client.myLobby.myBoard.size){
                client.myLobby.myBoard.minecount = client.myLobby.myBoard.size*client.myLobby.myBoard.size-1;
                client.myLobby.send_command("MINECOUNT " + client.myLobby.myBoard.minecount);
            }
        }else{
            throw new Exception("size not in range: (2 > n < 18)");
        }
        return true;
    }

    public static boolean set_minecount(SMM.Handler client, String argument) throws Exception{
        permission_not_game_start(client.myLobby);
        permission_is_owner(client.myLobby, client.myUser);

        String[] argument_array = null;
        int mine = 0;
        try{ argument_array  = argument.split(" "); }
        catch (Exception e) { throw new Exception("bad request"); }
        try{ mine = Integer.parseInt(argument_array[0]); }
        catch (Exception e) { throw new Exception("invalid arguments!"); }

        if(mine > 0 && mine < client.myLobby.myBoard.size*client.myLobby.myBoard.size){
            client.myLobby.myBoard.minecount = mine;
            client.myLobby.send_command("MINECOUNT " + mine);
        }else{
            throw new Exception("mine must be more than 1 and less than the board size!");
        }
        return true;
    }

    public static boolean mouse_pos(SMM.Handler client, String argument) throws Exception{
        permission_game_start(client.myLobby);

        String[] argument_array = null;
        try{ argument_array  = argument.split(" "); }
        catch (Exception e) { throw new Exception("bad request"); }

        try{ 
            client.myLobby.send_command("MOUSEPOS " + argument_array[0] + " " + argument_array[1] + " " + client.myUser.id_game, client.myUser);
        }
        catch (Exception e) { throw new Exception("invalid arguments!"); }
        return true;
    }

    static boolean my_flags(SMM.Handler client) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
