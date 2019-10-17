package client.minesweepermultiplayer;
import javax.swing.JButton;

public class Casilla extends JButton {

    int status;
    int x;
    int y;
    //status 0:Sin revelar, 1:Revelada, 2:Bandera
    public Casilla(int x, int y) {
        super();
        status = 0;
        this.x=x;
        this.y=y;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }   
}
