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
}
