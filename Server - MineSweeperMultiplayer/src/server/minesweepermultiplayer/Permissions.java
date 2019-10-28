package server.minesweepermultiplayer;

/**
 *
 * @author Josue Millan
 */
public class Permissions {

    public static void game_start(Lobby lobby) throws Exception {
        if(!lobby.started)
            throw new Exception("El juego no a empezado.");
    }

    public static void not_game_start(Lobby lobby) throws Exception {
        if(lobby.started)
            throw new Exception("El juego ya a empezado!");
    }

    public static void is_owner(Lobby lobby, User user) throws Exception {
        if(!lobby.owner.equals(user))
            throw new Exception("No eres el dueño del lobby.");
    }

    public static void is_alive(User user) throws Exception {
        if(!user.alive)
            throw new Exception("Estas muerto jaja xd.");
    }

    public static void debug() throws Exception {
        if(!SMM.DEBUG)
            throw new Exception("No en modo debug.");
    }

    public static void first_cell(Lobby lobby, User user, int x, int y) throws Exception {
        //Change how this one behaves!!
        
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
                    if(!(y>0 && y<lobby.myBoard.size && x==lobby.myBoard.size-1)){
                        throw new Exception("not your section... just yet!");
                    }
                break;
                case 3:
                    if(!(y>0 && y<lobby.myBoard.size && x==0)){
                        throw new Exception("not your section... just yet!");
                    }
                break;
                default:
                throw new Exception("fuera de rango que onda?");
            }
        }
    }

    public static void have_flags(Board board, User user) throws Exception {
        if(user.flags.size() >= board.bombs.size())
            throw new Exception("No tienes banderas!");
    }
}
