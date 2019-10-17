package client.minesweepermultiplayer;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Tablero extends JPanel implements MouseListener {

    int numCol;
    int numFil;
    Casilla[][] Casilla;

    public Tablero(int numFil, int numCol) {
        this.numFil = numFil;
        this.numCol = numCol;
        this.setEnabled(false);
    }

    public void Llenar() {
        Casilla = new Casilla[numFil][numCol];
        this.setLayout(new GridLayout(numFil, numCol));
        for (int x = 0; x < numCol; x++) {
            for (int y = 0; y < numFil; y++) {
                Casilla[y][x] = new Casilla(x, y);
                Casilla[y][x].addMouseListener(this);
                Casilla[y][x].setPreferredSize(new Dimension(25, 25));
                this.add(Casilla[y][x]);
            }
        }
    }

    public void Habilitar(boolean b) {
        for (int x = 0; x < numCol; x++) {
            for (int y = 0; y < numFil; y++) {
                Casilla[y][x].setEnabled(b);
            }
        }
    }

    public void RevelarCasilla(int x, int y, int valor) {
        ImageIcon img = new ImageIcon(getClass().getResource("/imagenes/" + valor + ".jpg"));
        Casilla[y][x].setIcon(new ImageIcon(img.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_DEFAULT)));
        Casilla[y][x].setStatus(1);
    }

    public void PonerBandera(int x, int y, int id) {
        ImageIcon img = new ImageIcon(getClass().getResource("/imagenes/bandera_" + id + ".jpg"));
        Casilla[y][x].setIcon(new ImageIcon(img.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_DEFAULT)));
        Casilla[y][x].setStatus(2);
    }
    
    public void QuitarBandera(int x, int y){
        Casilla[y][x].setIcon(null);
        Casilla[y][x].setStatus(0);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Casilla boton = (Casilla) e.getSource();

        String coord = boton.x+ " " + boton.y;
        if (e.getButton() == MouseEvent.BUTTON1) {
            ClientMineSweeperMultiplayer.Salida.println("/cell_reveal " + coord);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (boton.status == 0) {
                ClientMineSweeperMultiplayer.Salida.println("/cell_set_flag " + coord);
            } else if (boton.status == 2) {
                ClientMineSweeperMultiplayer.Salida.println("/cell_remove_flag " + coord);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
