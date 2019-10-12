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

        }
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
            client.myLobby.send_command("GAMESTART");
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

        for(int x = 0; x < client.myLobby.myBoard.size; x++){
            String info = "";
            for(int y = 0; y < client.myLobby.myBoard.size; y++){
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

    public static boolean cell_reveal(SMM.Handler client, String argument) throws Exception {
        permission_game_start(client.myLobby);
        permission_is_alive(client.myUser);
        
        String[] argument_array = null;
        try{ argument_array  = argument.split(" "); }
        catch (Exception e) { throw new Exception("bad request"); }
        if(argument_array.length >= 2){
            int x = 0, y = 0;
            try{
                x = Integer.parseInt(argument_array[1]);
                y = Integer.parseInt(argument_array[0]);
            } catch (Exception e) { throw new Exception("invalid arguments!"); }
            permission_first_cell(client.myLobby, client.myUser, x, y);
            Cell cell = client.myLobby.myBoard.get_cell(x,y);
            if( cell != null ){
                List<Cell> cellList = cell.reveal(client.myUser);
                cellList.forEach((cell_) -> {
                    client.myLobby.send_command("CELLREVEAL " + cell_.x + " " + cell_.y + " " + cell_.value);
                });
            }else { throw new Exception("Coords are out of bounds!!!"); }
        }else{ throw new Exception("Not enough arguments!!"); }
        return true;
    }

    public static boolean cell_flag(SMM.Handler client, String argument) throws Exception {
        permission_game_start(client.myLobby);
        permission_is_alive(client.myUser);

        String[] argument_array = null;
        try{ argument_array  = argument.split(" "); }
        catch (Exception e) { throw new Exception("bad request"); }
        if(argument_array.length >= 2){
            int x = 0, y = 0;
            try{
                x = Integer.parseInt(argument_array[1]);
                y = Integer.parseInt(argument_array[0]);
            } catch (Exception e) { throw new Exception("invalid arguments!"); }
            //check if inside the range
            permission_first_cell(client.myLobby, client.myUser, x, y);
            Cell cell = client.myLobby.myBoard.get_cell(x,y);
            if( cell != null ) {
                if ( cell.set_flag(client.myUser) ) {
                    client.myLobby.send_command("FLAG " + cell.x + " " + cell.y + " " + client.myLobby.myUsers.indexOf(client.myUser));
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
            client.out.println("USERINFO " + user.id + " " + user.name + " " + user.id_game);
        });
        return true;
    }
}
