package server.minesweepermultiplayer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Josue Millan
 */
public class Lobby {
    private static int globalId = 0;
    public int id;
    public User owner = null;
    public List<User> myUsers = new ArrayList<>();
    public int lobby_size = 4;
    public int lobby_minimum = 1;
    public Board myBoard = new Board();
    public boolean open = true;
    public boolean started = false;

    public Lobby() {
        this.id = globalId++; //select the correct id
    }

    public void send_command(String message){
        myUsers.forEach((user) ->{
            user.out.println(message);
        });
    }

    public void send_command(String message, User self){
        myUsers.forEach((user) ->{
            if(!user.equals(self)){
                user.out.println(message);
            }
        });
    }

    public void send_message(String message){
        send_command("MESSAGE " + message);
    }

    public void add_user_owner(User user) {
        owner = user;
        this.add_user(user);
    }

    public void add_user(User user) {
        System.out.println(user.name + " has join the lobby " + id);
        myUsers.add(user);
        check_lobby_slots();
    }

    public void remove_user(User user) {
        //find user in the list
        System.out.println(user.name + " has left the lobby " + id);
        myUsers.remove(user);
        check_lobby_slots();
    }

    public void check_lobby_slots() {
        //here we assign the new id values and send them to the clients
        if (started) return;//don't change state if the game have started
        open = this.lobby_size > myUsers.size();
        int i = 0;
        for(User user: myUsers){
            user.id_game = i++;
        }
    }

    public void game_end(){
        String args = "";
        for(User user : myUsers){
            args += user.id_game+":"+user.get_score()+":"+ user.name + " ";
        }

        send_command("GAMEEND " + args);
    }

    public boolean check_win() {
        //first check if the 
        boolean all_ded = true;
        for(User user: myUsers){
            if (user.alive) { all_ded = false; }
        }

        if(all_ded) { return true; }//the game end

        boolean run_out_flags = true;
        for(User user: myUsers){
            if (user.flags.size() < myBoard.minecount && user.alive) { run_out_flags = false; }
        }

        if(myBoard.unlocked == (myBoard.size*myBoard.size)-myBoard.minecount){//all non mines clicked
            //now check if the flags have run out
            if(run_out_flags){//if there is no more flags
                return true;//win
            }else{//if not check that all mine block are checked
                boolean not_visible_mines = true;
                for(Cell cell: myBoard.bombs) {
                    if(cell.visibility==0) { not_visible_mines = false; }
                }

                if(not_visible_mines) return true;
            }
        }else{
            return false;
        }

        return false;
    }
}
