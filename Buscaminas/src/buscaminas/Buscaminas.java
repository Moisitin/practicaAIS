package buscaminas;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
public class Buscaminas extends JFrame implements ActionListener, MouseListener{
    int nomines ;
    int contmines;
    int perm[][];
    String tmp, i;
    boolean found = false;
    int row;
    int column;
    int guesses[][];
    JButton b[][];
    int[][] mines;
    boolean allmines;
    int n ;
    int m ;
    int deltax[] = {-1, 0, 1, -1, 1, -1, 0, 1};
    int deltay[] = {-1, -1, -1, 0, 0, 1, 1, 1};
    double starttime;
    double endtime;
    JFrame frame;
    JMenuBar menuBar;
    JMenu options;
    JMenuItem reiniciar;
    ArrayList<Integer> tiempos ;
    ArrayList<String> nombres ;
    JLabel tiempo;
    JLabel minas;
    Timer timer;
    TimerTask t;
    int tiempom;
    
    public Buscaminas(int n, int m, int nomines,String i){
        tiempos = new ArrayList();
        nombres = new ArrayList();
        frame= new JFrame();
        
        this.n=n;
        this.m=m;
        this.nomines=nomines;
        this.i =i;
        contmines=nomines;
        tiempom=-1;
        frame.setLayout(new GridLayout(n,m));
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        
        options= new JMenu ("Options");
        menuBar.add(options);
        
        reiniciar= new JMenuItem("reset");
        options.add(reiniciar);
        reiniciar.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e){
                frame.dispose();
                new Buscaminas(n,m,nomines,i);
            }
        });
        
        minas= new JLabel();
        minas.setForeground(Color.blue);
        menuBar.add(minas);
        minas.setText("Minas: "+contmines+" ");
        
        tiempo= new JLabel();
        tiempo.setForeground(Color.red);   
        menuBar.add (tiempo);
        tiempo.setText(" Tiempo: "+tiempom);
        timer = new Timer();
        t= new TimerTask() {
            @Override
            public void run() {
                tiempom++;
                tiempo.setText(" Tiempo: "+tiempom);
            }
        };
         timer.schedule(t,0, 1000);
        
        
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        perm = new int[n][m];
        boolean allmines = false;
        guesses = new int [n+2][m+2];
        mines = new int[n+2][m+2];
        b = new JButton [n][m];
        
        for (int y = 0;y<m+2;y++){
            mines[0][y] = 3;
            mines[n+1][y] = 3;
            guesses[0][y] = 3;
            guesses[n+1][y] = 3;
        }
        for (int x = 0;x<n+2;x++){
            mines[x][0] = 3;
            mines[x][m+1] = 3;
            guesses[x][0] = 3;
            guesses[x][m+1] = 3;
        }
        do {
            int check = 0;
            for (int y = 1;y<m+1;y++){
                for (int x = 1;x<n+1;x++){
                    mines[x][y] = 0;
                    guesses[x][y] = 0;
                }
            }
            for (int x = 0;x<nomines;x++){
                mines [(int) (Math.random()*(n)+1)][(int) (Math.random()*(m)+1)] = 1;
            }
            for (int x = 0;x<n;x++){
                for (int y = 0;y<m;y++){
                if (mines[x+1][y+1] == 1){
                        check++;
                    }
                }
            }
            if (check == nomines){
                allmines = true;
            }
        }while (allmines == false);
        for (int y = 0;y<m;y++){
            for (int x = 0;x<n;x++){
                if ((mines[x+1][y+1] == 0) || (mines[x+1][y+1] == 1)){
                    perm[x][y] = perimcheck(x,y);
                }
                b[x][y] = new JButton("?");
                b[x][y].addActionListener(this);
                b[x][y].addMouseListener(this);
                frame.add(b[x][y]);
                b[x][y].setEnabled(true);
            }//end inner for
        }//end for
        frame.pack();
        frame.setVisible(true);
        for (int y = 0;y<m+2;y++){
            for (int x = 0;x<n+2;x++){
                System.out.print(mines[x][y]);
            }
        System.out.println("");}
        starttime = System.nanoTime();
    }//end constructor Mine()
 
    public void actionPerformed(ActionEvent e){
        found =  false;
        JButton current = (JButton)e.getSource();
        for (int y = 0;y<m;y++){
            for (int x = 0;x<n;x++){
                JButton t = b[x][y];
                if(t == current){
                    row=x;column=y; found =true;
                }
            }//end inner for
        }//end for
        if(!found) {
            System.out.println("didn't find the button, there was an error "); System.exit(-1);
        }
        Component temporaryLostComponent = null;
        if (b[row][column].getBackground() == Color.orange){
            return;
        }else if (mines[row+1][column+1] == 1){
                timer.cancel();
                JOptionPane.showMessageDialog(temporaryLostComponent, "You set off a Mine!!!!.");
                System.exit(0);
        } else {
            tmp = Integer.toString(perm[row][column]);
            if (perm[row][column] == 0){
                    tmp = " ";
            }
            b[row][column].setText(tmp);
            b[row][column].setEnabled(false);
            checkifend();
            if (perm[row][column] == 0){
                scan(row, column);
                checkifend();
            }
        }
    }
 
    public void checkifend(){
        int check= 0;
        for (int y = 0; y<m;y++){
            for (int x = 0;x<n;x++){
                if (b[x][y].isEnabled()){
                    check++;
                }
            }
        }
        
        if (check == nomines){
            timer.cancel();
            endtime = System.nanoTime();
            Component temporaryLostComponent = null;
            int tiempoFinal =(int)((endtime-starttime)/1000000000);
            if((i.equalsIgnoreCase("Personalizado"))){
                JOptionPane.showMessageDialog(temporaryLostComponent, "Congratulations you won!!! It took you "+tiempoFinal+" seconds!");    
            }else{
        
                if (i.equalsIgnoreCase("Principiante")){
                    String ruta = "Principiante.txt";

                    File archivo = new File (ruta);
                    if( archivo.exists()){
                    try{
                            
                        FileReader fr = new FileReader (ruta);
                        BufferedReader br = new BufferedReader(fr);
                        String linea;
                        while ((linea = br.readLine()) != null){
                            String str[] = linea.split (" ");
                            nombres.add(str[0]);
                            tiempos.add(Integer.parseInt(str[1]));      
                        }
                        fr.close();
                        br.close();

                        

                    }catch (IOException ex) {
                        Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                    }   

                    int tamTiempos = tiempos.size();
                    if (tiempos.get(tamTiempos-1)>tiempoFinal){
                        String nombre = JOptionPane.showInputDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                                +tiempoFinal+" seconds!\n"+
                        "INSERTE SU NOMBRE SI QUIERE GUARDAR EL TIEMPO\n"+"              Y PULSE ACEPTAR");

                        for(int i=0; i<tamTiempos-1; i++){
                            if ((tiempoFinal < tiempos.get(i))){
                                nombres.add(i, nombre);
                                tiempos.add(i, tiempoFinal);
                            }
                        }
                    
                        if (tamTiempos==11){
                        tiempos.remove(10);
                        nombres.remove(10);
                        }
                        
                        try{
                            FileWriter fw= new FileWriter(archivo);
                            BufferedWriter bw = new BufferedWriter(fw);
                            for(int i=0; i<10;i++){
                                bw.write(nombres.get(i)+" "+tiempos.get(i));
                            }
                            bw.flush();
                            bw.close();

                        }catch (IOException ex){
                            Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }else{
                        String nombre = JOptionPane.showInputDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                                +tiempoFinal+" seconds!\n"+
                        "INSERTE SU NOMBRE SI QUIERE GUARDAR EL TIEMPO\n"+"              Y PULSE ACEPTAR");
                        try{
                        FileWriter fw= new FileWriter(archivo);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(nombre+" "+tiempoFinal);
                         }catch (IOException ex){
                            Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    } else {
                        JOptionPane.showMessageDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                            +tiempoFinal+" seconds!"); 
                    }
                
                }else if (i.equalsIgnoreCase("Intermedio")){
                    String ruta = "Intermedio.txt";  
                    
                    File archivo = new File (ruta);
                    try{
                            
                        FileReader fr = new FileReader (ruta);
                        BufferedReader br = new BufferedReader(fr);
                        String linea;
                        while ((linea = br.readLine()) != null){
                            String str[] = linea.split (" ");
                            nombres.add(str[0]);
                            tiempos.add(Integer.parseInt(str[1]));      
                        }
                        fr.close();
                        br.close();

                        

                    }catch (IOException ex) {
                        Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                    }   

                    int tamTiempos = tiempos.size();
                    if (tiempos.get(tamTiempos-1)>tiempoFinal){
                        String nombre = JOptionPane.showInputDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                                +tiempoFinal+" seconds!\n"+
                        "INSERTE SU NOMBRE SI QUIERE GUARDAR EL TIEMPO\n"+"              Y PULSE ACEPTAR");

                        for(int i=0; i<tamTiempos-1; i++){
                            if ((tiempoFinal < tiempos.get(i))){
                                nombres.add(i, nombre);
                                tiempos.add(i, tiempoFinal);
                            }
                        }
                    
                        if (tamTiempos==11){
                        tiempos.remove(10);
                        nombres.remove(10);
                        }
                        
                        try{
                            FileWriter fw= new FileWriter(archivo);
                            BufferedWriter bw = new BufferedWriter(fw);
                            for(int i=0; i<10;i++){
                                bw.write(nombres.get(i)+" "+tiempos.get(i));
                            }
                            bw.flush();
                            bw.close();

                        }catch (IOException ex){
                            Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        JOptionPane.showMessageDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                            +tiempoFinal+" seconds!"); 
                    }
                
                }else if (i.equalsIgnoreCase("Avanzado")){
                    String ruta = "Avanzado.txt";  
                   
                    File archivo = new File (ruta);
                    try{
                            
                        FileReader fr = new FileReader (ruta);
                        BufferedReader br = new BufferedReader(fr);
                        String linea;
                        while ((linea = br.readLine()) != null){
                            String str[] = linea.split (" ");
                            nombres.add(str[0]);
                            tiempos.add(Integer.parseInt(str[1]));      
                        }
                        fr.close();
                        br.close();

                        

                    }catch (IOException ex) {
                        Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                    }   

                    int tamTiempos = tiempos.size();
                    if (tiempos.get(tamTiempos-1)>tiempoFinal){
                        String nombre = JOptionPane.showInputDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                                +tiempoFinal+" seconds!\n"+
                        "INSERTE SU NOMBRE SI QUIERE GUARDAR EL TIEMPO\n"+"              Y PULSE ACEPTAR");

                        for(int i=0; i<tamTiempos-1; i++){
                            if ((tiempoFinal < tiempos.get(i))){
                                nombres.add(i, nombre);
                                tiempos.add(i, tiempoFinal);
                            }
                        }
                    
                        if (tamTiempos==11){
                        tiempos.remove(10);
                        nombres.remove(10);
                        }
                        
                        try{
                            FileWriter fw= new FileWriter(archivo);
                            BufferedWriter bw = new BufferedWriter(fw);
                            for(int i=0; i<10;i++){
                                bw.write(nombres.get(i)+" "+tiempos.get(i));
                            }
                            bw.flush();
                            bw.close();

                        }catch (IOException ex){
                            Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        JOptionPane.showMessageDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                            +tiempoFinal+" seconds!"); 
                    }
                
                }
    
            }
        }
    }

    public void scan(int x, int y){
        for (int a = 0;a<8;a++) {
            if (mines[x+1+deltax[a]][y+1+deltay[a]] == 3){
 
            } else if ((perm[x+deltax[a]][y+deltay[a]] == 0) && (mines[x+1+deltax[a]][y+1+deltay[a]] == 0) && (guesses[x+deltax[a]+1][y+deltay[a]+1] == 0)){
                if (b[x+deltax[a]][y+deltay[a]].isEnabled()){
                    b[x+deltax[a]][y+deltay[a]].setText(" ");
                    b[x+deltax[a]][y+deltay[a]].setEnabled(false);
                    scan(x+deltax[a], y+deltay[a]);
                }
            } else if ((perm[x+deltax[a]][y+deltay[a]] != 0) && (mines[x+1+deltax[a]][y+1+deltay[a]] == 0)  && (guesses[x+deltax[a]+1][y+deltay[a]+1] == 0)){
                tmp = new Integer(perm[x+deltax[a]][y+deltay[a]]).toString();
                b[x+deltax[a]][y+deltay[a]].setText(Integer.toString(perm[x+deltax[a]][y+deltay[a]]));
                b[x+deltax[a]][y+deltay[a]].setEnabled(false);
            }
        }
    }
 
    public int perimcheck(int a, int y){
        int minecount = 0;
        for (int x = 0;x<8;x++){
            if (mines[a+deltax[x]+1][y+deltay[x]+1] == 1){
                minecount++;
            }
        }
        return minecount;
    }
 
    public void windowIconified(WindowEvent e){
 
    }
 
    public static void main(String[] args){
        PantallaInicio pi;
        pi= new PantallaInicio();
        pi.setVisible(true);
        //new Buscaminas();
    }
 
    public void mouseClicked(MouseEvent e) {
 
    }
 
    @Override
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }
 
    @Override
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }
 
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            found =  false;
            Object current = e.getSource();
            for (int y = 0;y<m;y++){
                    for (int x = 0;x<n;x++){
                            JButton t = b[x][y];
                            if(t == current){
                                    row=x;column=y; found =true;
                            }
                    }//end inner for
            }//end for
            if(!found) {
                System.out.println("didn't find the button, there was an error "); System.exit(-1);
            }
            if ((guesses[row+1][column+1] == 0) && (b[row][column].isEnabled())){
                b[row][column].setText("x");
                guesses[row+1][column+1] = 1;
                b[row][column].setBackground(Color.orange);
                contmines--;
                minas.setText("Minas: "+contmines+" ");
            } else if (guesses[row+1][column+1] == 1){
                b[row][column].setText("?");
                guesses[row+1][column+1] = 0;
                b[row][column].setBackground(null);
                contmines++;
                minas.setText("Minas: "+contmines+" ");
            }
        }
    }
 
    @Override
    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }
}//end class