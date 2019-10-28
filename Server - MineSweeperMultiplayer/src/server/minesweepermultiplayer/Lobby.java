package server.minesweepermultiplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

    Timer timer = new Timer();

    class MineSwaper extends TimerTask {
        public void run(){
            System.out.println("Trying to put a new bomb");
            //check if it is possible to put bombs
            int total = myBoard.get_total_size();
            total -= myBoard.unlocked;

            if(total>myBoard.bombs.size()){
                send_message("Trying to send new bomb");
                System.out.println(">new bomb!");
                
                Cell bomb = myBoard.generate_new_bomb();

                System.out.println(">trying!");
                myBoard.bombs.add(bomb);
                send_command("CELLREVEAL " + bomb.x + " " + bomb.y + " " + bomb.value + " " + (bomb.owner != null ? bomb.owner.id_game : -1) + " " + 4);

                send_message("New mine in: " + bomb.x + ", " + bomb.y);
                System.out.println(">have bomb now!");

                //success now update neighbors
                List<Cell> neighbors = myBoard.get_neighbors_cells(bomb);

                neighbors.forEach((neighbor) -> {
                    if(neighbor.value != -1){
                        neighbor.value++;
                        if(neighbor.visibility == 1){
                            send_command("CELLREVEAL " + neighbor.x + " " + neighbor.y + " " + neighbor.value + " " + (bomb.owner != null ? bomb.owner.id_game : -1) + " " + 3);
                        }
                    }
                });

                System.out.println("Sended");

                //new Mine count:
                System.out.println("Mine count: " + myBoard.bombs.size());
            }else{
                System.out.println("Not enough space!!");
            }

        }
    }

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

    public void kill_user(User user) {
        user.alive = false;
        user.out.println("URDEAD");
        send_command("USERINFO " + user.info());
        send_message(user.name + " Has Die!!!");
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

    public void check_softlock() {
        myUsers.forEach((user) ->{
            if(user.first_cell){
                if( !myBoard.check_line(user.id_game) ){//if is free then unlock him
                    user.first_cell = false;
                }
            }
        });
    }

    public void game_start(){
        started = true;
        open = false;
        myBoard.generate_new_board();
        send_message("Game Started!!!");
        send_command("GAMESTART " + myBoard.size + " " + myBoard.minecount);

        timer.schedule(new MineSwaper(), 5000, 5000);
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

        //lilst of conditions
        boolean all_ded = true;
        boolean run_out_of_flags = true;
        boolean not_hidden_mines = true;
        boolean all_non_mines_clicked = myBoard.unlocked == myBoard.get_total_size()-myBoard.bombs.size();

        for(User user: myUsers){
            if (user.alive) { all_ded = false; }
            if (user.flags.size() < myBoard.bombs.size() && user.alive) { run_out_of_flags = false; }
        }

        for(Cell cell: myBoard.bombs){
            if (cell.visibility==0) { not_hidden_mines = false; }
        }

        //send all game conditions
        send_command("INFO all_ded " + all_ded);
        send_command("INFO run_out_of_flags " + run_out_of_flags);
        send_command("INFO not_hidden_mines " + not_hidden_mines);
        send_command("INFO all_non_mines_clicked " + all_non_mines_clicked);

        if(all_ded) return true;
        if(run_out_of_flags && all_non_mines_clicked) return true;
        if(all_non_mines_clicked && not_hidden_mines) return true;

        return false;
    }
}
