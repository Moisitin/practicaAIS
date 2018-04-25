package buscaminas;

/**
 * Práctica 2 Ampiación Ingeniería del Software
 * Doble Grado GII + ADE       Curso: 2017 - 2018
 * @author Paula Sestafe
 * @author Moises Garcia
 * @version 2.7
 * @since Buscaminas 1.0
*/

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 * Clase pública en la que instanciaremos todos los métodos y variables que utilizará nuestro código
 * @since Buscaminas 1.0
*/

public class Buscaminas extends JFrame implements ActionListener, MouseListener{
    /*
    Hemos creado un contador de minas que tiene el mismo valor que nomines
    esto lo hemos hecho porque la otra variable no se podía modificar para 
    mostrar el numero de minas que quedaban ya que nos indica cúando se ha ganado la partida
    */
    int nomines ;
    int contmines; 
    
    int perm[][];
    String tmp, i;
    boolean found = false;
    int row;
    int column;
    int guesses[][];
    JButton b[][]; //Matriz principal de botones del juego
    int[][] mines; //Matriz de minas 
    boolean allmines; //A True si todas las minas son marcadas
    int n ;
    int m ;
    
    //Posiciones alrededor de una casilla para conocer lo que esta alrededor
    
    int deltax[] = {-1, 0, 1, -1, 1, -1, 0, 1};
    int deltay[] = {-1, -1, -1, 0, 0, 1, 1, 1};
    
    //Inicio y final del juego
    
    double starttime;
    double endtime;
    
    //Parte dedicada a declarar los atributos usados en la GUI
    
    JFrame frame; //Frame con todos los componentes que muestra Buscaminas
    JMenuBar menuBar; //Barra superior
    JMenu options;
    JMenuItem reiniciar;    
    JMenuItem guardarPartida;
    JMenuItem cargarPartida;
    
    //Acceso a los tiempos de las distintas categorías
    
    JMenu tiemposM;
    JMenuItem tiemposP; 
    JMenuItem tiemposI; 
    JMenuItem tiemposE;
    
    //Frame para mostrar el listado de mejores de puntuaciones de cada categoría
    
    JFrame framep;
    JFrame framei;
    JFrame framee;
    
    JLabel tiempo;
    JLabel minas;

    
    //Arrays con los nombres y los tiempos de las mejores partidas
    
    ArrayList<Integer> tiempos ;
    ArrayList<String> nombres ;
    
    /*
    El Timer y el TimerTask se utilizan para 
    poder mostrar el tiempo que lleva ejecutandose la partida que se está jugando
    */
    
    Timer timer;
    TimerTask t;
    int tiempom;
   
    /**
     * Constructor principal del programa
     * @param n numero de filas
     * @param m numero de columnas
     * @param nomines numero de minas
     * @param i String del nivel de dificultad
     * @since Buscaminas 1.0
    */
    public Buscaminas(int n, int m, int nomines,String i){      
        
        
        //Inicialización de variables
        
        this.n=n;
        this.m=m;
        this.nomines=nomines;
        this.i =i;
        contmines=nomines;
        tiempom=-1;        
        tiempos = new ArrayList();
        nombres = new ArrayList();
        
        
        //Inicialización del Frame con todas sus características y sus componentes
        
        frame= new JFrame();
        frame.setLayout(new GridLayout(n,m));
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        
        options= new JMenu ("Options");
        menuBar.add(options);
        
        
        //Opción de reinicio
        
        reiniciar= new JMenuItem("Reset");
        options.add(reiniciar);
        
        /**
         * Método para reiniciar el programa
         * @return el Frame del juego
         * @since Buscaminas 1.4
        */
        
        reiniciar.addActionListener(new ActionListener() { 
            public void actionPerformed (ActionEvent e){

                frame.dispose();                  //Elimina el Frame actual
                new Buscaminas(n,m,nomines,i);    //Llama al contructor para iniciar una partida igual
            }
        });
        
        /*
        Creamos un label donde se van mostrando las minas que quedan por marcar
        posteriormente indicaremos que más instrucciones hemos añadido para que 
        esto tenga un buen funcionamiento
        */
        minas= new JLabel();
        minas.setForeground(Color.blue);
        menuBar.add(minas);
        minas.setText("Mines: "+contmines+" ");
        
        /*
        Creamos un label donde se va mostrando el tiempo de ejecución de la partida
        para ello se han añadido otras instrucciones más adelante del código que
        señalaremos
        */
        tiempo= new JLabel();
        tiempo.setForeground(Color.red);   
        menuBar.add (tiempo);
        tiempo.setText(" Tiempo: "+tiempom);
        timer = new Timer();
        t= new TimerTask() {
            @Override
            public void run() {
                tiempom++;
                tiempo.setText(" Time: "+tiempom);
            }
        };
        timer.schedule(t,0, 1000); //con esto se logra que el tiempo se muestre segundo a segundo 
        
        
        //Se van inicializando las distintas matrices necesarias para el juego
        
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
        
        
        //Se colocan las minas y se comprueban que están todas
        
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
        
        
        //Se inicializa la matriz de button y se añade al Frame
         
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
        
        //Se da forma al Frame y se visualiza
         
        
        frame.pack();       
        frame.setVisible(true);
        
        for (int y = 0;y<m+2;y++){
            for (int x = 0;x<n+2;x++){
                System.out.print(mines[x][y]);
            }
        System.out.println("");}
        starttime = System.nanoTime();

        // Añadimos en el menú Options la opción de que el usuario guarde una partida
        
        guardarPartida = new JMenuItem("Save Game");
        options.add(guardarPartida);
        
        /**
         * Método para guardar la partida
         * @return fichero con la partida guardada
         * @throws puede lanzar la excepción de IOException
         * @since Buscaminas 1.8
         */
        
        guardarPartida.addActionListener(new ActionListener() {  
        public void actionPerformed (ActionEvent e){    
            try{
                
                //Volcamos las características de nuestra partida en el fichero
                File archivo;
                String ruta ="PartidaGuardada.txt";
                archivo = new File (ruta);
                archivo.createNewFile();
                FileOutputStream fos= new FileOutputStream(archivo);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                
                oos.writeInt(n);
                oos.writeInt(m);
                oos.writeInt(nomines);
                oos.writeInt(contmines);
                oos.writeObject(i);
                oos.writeObject(tmp);
                oos.writeBoolean(found);
                oos.writeInt(row);
                oos.writeInt(column);
                oos.writeInt(tiempom);
                oos.writeObject(perm);
                oos.writeObject(guesses);
                oos.writeObject(mines);
                
                //La matriz b debemos pasarla como String
                for(int i = 0;i < n;i++){
                    for (int j = 0; j < m; j++){
                        oos.writeObject(b[i][j].getText());                
                    }
                }
 
                oos.close();
            } catch (IOException ex) {
                Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        });
        
        //Añadimos en el menú Options la opción de que el usuario cargue una partida
        cargarPartida = new JMenuItem("Load Game");
        options.add(cargarPartida);
        
        /**
         * Método para cargar la partida
         * @return partida guardada anteriormente
         * @throws puede lanzar la excepción de IOException
         * @since Buscaminas 1.9
        */
        
        cargarPartida.addActionListener(new ActionListener() {   
        public void actionPerformed (ActionEvent e){     
            frame.dispose();
            try{    
                /*Leemos el archivo y vamos volcando las características de la anterior partida
                en nuestra partida
                */
                File archivo;
                String ruta ="PartidaGuardada.txt";
                archivo = new File (ruta);
                archivo.createNewFile();
                FileInputStream fis = new FileInputStream(archivo);
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                Integer n = (Integer)ois.readInt();
                Integer m = (Integer)ois.readInt();
                Integer nomines = (Integer)ois.readInt();
                Integer contmines = (Integer)ois.readInt();
                String i = (String)ois.readObject();
                String tmp = (String)ois.readObject();
                Boolean found = (Boolean)ois.readBoolean();
                Integer row = (Integer)ois.readInt();
                Integer column = (Integer)ois.readInt();
                Integer tiempom = (Integer)ois.readInt();
                int[][] perm = (int[][])ois.readObject();
                int[][] guesses = (int[][])ois.readObject();
                int[][] mines = (int[][])ois.readObject();
                
                //Debemos recuperar la matriz b a partir de los String
                
               JButton[][] b = new JButton[n][m];
               for(int k = 0;k < n;k++){
                    for (int j = 0; j < m; j++){
                        b[k][j] = new JButton((String)ois.readObject());
                    }
                }
               
               //Llamada a un nuevo constructor con todas las características modificables
                new Buscaminas(n, m, nomines, contmines, i, tmp, found, row, column, tiempom,
                    perm, guesses, mines, b);
                
                ois.close();
                
                
            } catch (IOException ex) {
                Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        });

        /*
        Añadimos más componentes al menú para hacer legubles los ficheros de las puntuaciones
        desde la propia aplicación
        */
        tiemposM = new JMenu ("Show time");
        options.add(tiemposM);
        tiemposP = new JMenuItem ("Beginner");
        tiemposM.add(tiemposP);
        tiemposI = new JMenuItem ("Intermediate");
        tiemposM.add(tiemposI);
        tiemposE = new JMenuItem ("Expert");
        tiemposM.add(tiemposE);
        
        /**
         * Métodos`para mostrar los mejores tiempos por pantalla categoría principiante
         * @return un Frame con los mejores tiempos principiante
         * @throws puede lanzar la excepción de IOException
         * @since Buscaminas 1.5
        */
        
        tiemposP.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    //Cerramos la pantalla anterior
                    frame.dispose();
                    
                    //Creamos una nueva ventana con las siguientes características:
                    framep= new JFrame ("Tiempos Principiante");
                    framep.setVisible(true);
                    framep.setSize(500,500);
                    framep.setLayout(new GridLayout(10,1));//10lineas y 1 columna
                    framep.setDefaultCloseOperation(EXIT_ON_CLOSE);
                
                    /*
                    Leemos el archivo e insertamos línea a línea a un JLabel para
                    que aparezca en la pantalla
                    */
                    File archivo= new File("Principiante.txt");
                    archivo.createNewFile();
                    FileReader fr = new FileReader (archivo);
                    BufferedReader br = new BufferedReader(fr);
                    String linea;
                    while ((linea = br.readLine()) != null){
                        JLabel text = new JLabel(linea);
                        framep.add(text);  
                        
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        /**
         * Métodos`para mostrar los mejores tiempos por pantalla categoría intermedia
         * @return un Frame con los mejores tiempos intermedios
         * @throws puede lanzar la excepción de IOException
         * @since Buscaminas 1.6
        */
        
        tiemposI.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try{    
                    frame.dispose();
                    framei= new JFrame ("Tiempos Intermedio");
                    framei.setVisible(true);
                    framei.setSize(500,500);
                  
                    framei.setLayout(new GridLayout(10,1));
                    framei.setDefaultCloseOperation(EXIT_ON_CLOSE);
                          
                    File archivo= new File("Intermedio.txt");
                    archivo.createNewFile();
                    FileReader fr = new FileReader (archivo);
                    BufferedReader br = new BufferedReader(fr);
                    String linea;
                    while ((linea = br.readLine()) != null){
                        JLabel text = new JLabel(linea);
                        framei.add(text);  
                        
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        /**
         * Métodos`para mostrar los mejores tiempos por pantalla categoría experta
         * @return un Frame con los mejores tiempos expertos
         * @throws puede lanzar la excepción de IOException
         * @since Buscaminas 1.7
        */
        
        tiemposE.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try{    
                    frame.dispose();
                    framee= new JFrame ("Tiempos Experto");
                    framee.setVisible(true);
                    framee.setSize(500,500);
                  
                    framee.setLayout(new GridLayout(10,1));
                    framee.setDefaultCloseOperation(EXIT_ON_CLOSE);
                
                    File archivo= new File("Experto.txt");
                    archivo.createNewFile();
                    FileReader fr = new FileReader (archivo);
                    BufferedReader br = new BufferedReader(fr);
                    String linea;
                    while ((linea = br.readLine()) != null){
                        JLabel text = new JLabel(linea);
                        framee.add(text);  
                        
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
    }
    //Fin del constructor principal
    
    /**
     * Constructor creado para las partidas guardadas. Se deben de pasar todas las características 
     * de una partida para poder volver a retomarla
     * @param n numero de filas
     * @param m numero de columnas
     * @param nomines numero de minas totales
     * @param contadormines numero de minas marcadas
     * @param i String de categoría
     * @param tmp String del contenido de los button
     * @param found boolean para los button
     * @param row chequeo numero de filas
     * @param column chequeo numero de filas
     * @param contiempo contador de tiempo
     * @param perm2 matriz con el perímetro del tablero
     * @param guesses2 matriz de contenidos 
     * @param mines2 matriz de minas
     * @param b2 matriz de button
     * @since Buscaminas 2.0
    */
    
    public Buscaminas(int n, int m, int nomines, int contadormines, String i, String tmp, 
            boolean found, int row, int column, int contiempo, int[][] perm2, int[][] guesses2, 
            int[][] mines2, JButton[][] b2) {
        
        //Se inicializan todas las variables
        this.n=n;
        this.m=m;
        this.nomines=nomines;
        this.contmines=contadormines;
        this.i=i;
        this.tmp=tmp;
        this.found=found;
        this.row=row;
        this.column=column;
        this.tiempom=contiempo-1;
        this.perm=perm2;
        this.guesses=guesses2;
        this.mines=mines2;
        this.b=b2;
        
        tiempos = new ArrayList();
        nombres = new ArrayList();

        contmines=nomines;

        //Inicialización del Frame con todas sus características y sus componentes
        frame= new JFrame();
        frame.setLayout(new GridLayout(n,m));
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        
        options= new JMenu ("Options");
        menuBar.add(options);
        
        //Opción de reinicio
        reiniciar= new JMenuItem("Reset");
        options.add(reiniciar);
        
        /**
         * Método para reiniciar el programa
         * @return el Frame del juego
         * @since Buscaminas 2.1
        */
        
        reiniciar.addActionListener(new ActionListener() { 
            public void actionPerformed (ActionEvent e){
                frame.dispose();                  //Elimina el Frame actual
                new Buscaminas(n,m,nomines,i);    //Llama al contructor para iniciar una partida igual
            }
        });
        
        /*
        Creamos un label donde se van mostrando las minas que quedan por marcar
        posteriormente indicaremos que más instrucciones hemos añadido para que 
        esto tenga un buen funcionamiento
        */
        minas= new JLabel();
        minas.setForeground(Color.blue);
        menuBar.add(minas);
        minas.setText("Mines: "+contmines+" ");
        
        /*
        Creamos un label donde se va mostrando el tiempo de ejecución de la partida
        para ello se han añadido otras instrucciones más adelante del código que
        señalaremos
        */
        tiempo= new JLabel();
        tiempo.setForeground(Color.red);   
        menuBar.add (tiempo);
        tiempo.setText(" Time: "+tiempom);
        timer = new Timer();
        t= new TimerTask() {
            @Override
            public void run() {
                tiempom++;
                tiempo.setText(" Time: "+tiempom);
            }
        };
        timer.schedule(t,0, 1000); //con esto se logra que el tiempo se muestre segundo a segundo 

        //Le damos formato a la matriz de button para que sea igual que la incial
        for (int y = 0;y<m;y++){
            for (int x = 0;x<n;x++){
                if (b[x][y].getText().equals("?")){
                    b[x][y].setEnabled(true);
                } else if ((b[x][y].getText().equals("x"))){
                    b[x][y].setBackground(Color.orange);
                    b[x][y].setEnabled(true);
                } else {
                    b[x][y].setEnabled(false);
                }
                
                //Se dan funcionalidades a la matriz y se añade al Frame
                b[x][y].addActionListener(this);
                b[x][y].addMouseListener(this);
                frame.add(b[x][y]);             
            }
        }
        
        //Ajustamos el Frame y lo hacemos visible
        frame.pack();       
        frame.setVisible(true);
        
        for (int y = 0;y<m+2;y++){
            for (int x = 0;x<n+2;x++){
                System.out.print(mines[x][y]);
            }
        System.out.println("");}
        starttime = System.nanoTime();

        
        //Añadimos en el menú Options la opción de que el usuario guarde una partida
        guardarPartida = new JMenuItem("Save Game");
        options.add(guardarPartida);
        
        /**
         * Método para guardar la partida
         * @return fichero con la partida guardada
         * @throws puede lanzar la excepción de IOException
         * @since Buscaminas 2.2
         */
        
        guardarPartida.addActionListener(new ActionListener() {  
        public void actionPerformed (ActionEvent e){    
            try{
                
                //Volcamos las características de nuestra partida en el fichero
                File archivo;
                String ruta ="PartidaGuardada.txt";
                archivo = new File (ruta);
                archivo.createNewFile();
                FileOutputStream fos= new FileOutputStream(archivo);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                
                oos.writeInt(n);
                oos.writeInt(m);
                oos.writeInt(nomines);
                oos.writeInt(contmines);
                oos.writeObject(i);
                oos.writeObject(tmp);
                oos.writeBoolean(found);
                oos.writeInt(row);
                oos.writeInt(column);
                oos.writeInt(tiempom);
                oos.writeObject(perm);
                oos.writeObject(guesses);
                oos.writeObject(mines);
                
                //La matriz b debemos pasarla como String
                for(int i = 0;i < n;i++){
                    for (int j = 0; j < m; j++){
                        oos.writeObject(b[i][j].getText());                
                    }
                }
 
                oos.close();
            } catch (IOException ex) {
                Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        });
        
        //Añadimos en el menú Options la opción de que el usuario cargue una partida
        cargarPartida = new JMenuItem("Load Game");
        options.add(cargarPartida);
        
        /**
         * Método para cargar la partida
         * @return partida guardada anteriormente
         * @throws puede lanzar la excepción de IOException
         * @since Buscaminas 2.3
        */
        
        cargarPartida.addActionListener(new ActionListener() {   
        public void actionPerformed (ActionEvent e){     
            frame.dispose();
            try{    
                /*Leemos el archivo y vamos volcando las características de la anterior partida
                en nuestra partida
                */
                File archivo;
                String ruta ="PartidaGuardada.txt";
                archivo = new File (ruta);
                archivo.createNewFile();
                FileInputStream fis = new FileInputStream(archivo);
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                Integer n = (Integer)ois.readInt();
                Integer m = (Integer)ois.readInt();
                Integer nomines = (Integer)ois.readInt();
                Integer contmines = (Integer)ois.readInt();
                String i = (String)ois.readObject();
                String tmp = (String)ois.readObject();
                Boolean found = (Boolean)ois.readBoolean();
                Integer row = (Integer)ois.readInt();
                Integer column = (Integer)ois.readInt();
                Integer tiempom = (Integer)ois.readInt();
                int[][] perm = (int[][])ois.readObject();
                int[][] guesses = (int[][])ois.readObject();
                int[][] mines = (int[][])ois.readObject();
                
                //Debemos recuperar la matriz b a partir de los String
                
               JButton[][] b = new JButton[n][m];
               for(int k = 0;k < n;k++){
                    for (int j = 0; j < m; j++){
                        b[k][j] = new JButton((String)ois.readObject());
                    }
                }
               
               //Llamada a un nuevo constructor con todas las características modificables
                new Buscaminas(n, m, nomines, contmines, i, tmp, found, row, column, tiempom,
                    perm, guesses, mines, b);
                
                ois.close();
                
                
            } catch (IOException ex) {
                Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        });

        /*
        Añadimos más componentes al menú para hacer legubles los ficheros de las puntuaciones
        desde la propia aplicación
        */
        tiemposM = new JMenu ("Show time");
        options.add(tiemposM);
        tiemposP = new JMenuItem ("Beginner");
        tiemposM.add(tiemposP);
        tiemposI = new JMenuItem ("Intermediate");
        tiemposM.add(tiemposI);
        tiemposE = new JMenuItem ("Expert");
        tiemposM.add(tiemposE);
        
        /**
         * Métodos`para mostrar los mejores tiempos por pantalla categoría principiante
         * @return un Frame con los mejores tiempos principiante
         * @throws puede lanzar la excepción de IOException
         * @since Buscaminas 2.4
        */
        
        tiemposP.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    //Cerramos la pantalla anterior
                    frame.dispose();
                    
                    //Creamos una nueva ventana con las siguientes características:
                    framep= new JFrame ("Tiempos Principiante");
                    framep.setVisible(true);
                    framep.setSize(500,500);
                    framep.setLayout(new GridLayout(10,1));//10lineas y 1 columna
                    framep.setDefaultCloseOperation(EXIT_ON_CLOSE);
                
                    /*
                    Leemos el archivo e insertamos línea a línea a un JLabel para
                    que aparezca en la pantalla
                    */
                    File archivo= new File("Principiante.txt");
                    archivo.createNewFile();
                    FileReader fr = new FileReader (archivo);
                    BufferedReader br = new BufferedReader(fr);
                    String linea;
                    while ((linea = br.readLine()) != null){
                        JLabel text = new JLabel(linea);
                        framep.add(text);  
                        
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        /**
         * Métodos`para mostrar los mejores tiempos por pantalla categoría intermedio
         * @return un Frame con los mejores tiempos intermedios
         * @throws puede lanzar la excepción de IOException
         * @since Buscaminas 2.5
        */
        
        tiemposI.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try{    
                    frame.dispose();
                    framei= new JFrame ("Tiempos Intermedio");
                    framei.setVisible(true);
                    framei.setSize(500,500);
                  
                    framei.setLayout(new GridLayout(10,1));
                    framei.setDefaultCloseOperation(EXIT_ON_CLOSE);
                          
                    File archivo= new File("Intermedio.txt");
                    archivo.createNewFile();
                    FileReader fr = new FileReader (archivo);
                    BufferedReader br = new BufferedReader(fr);
                    String linea;
                    while ((linea = br.readLine()) != null){
                        JLabel text = new JLabel(linea);
                        framei.add(text);  
                        
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        /**
         * Métodos`para mostrar los mejores tiempos por pantalla categoría experto
         * @return un Frame con los mejores tiempos experto
         * @throws puede lanzar la excepción de IOException
         * @since Buscaminas 2.6
        */
        
        tiemposE.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try{    
                    frame.dispose();
                    framee= new JFrame ("Tiempos Experto");
                    framee.setVisible(true);
                    framee.setSize(500,500);
                  
                    framee.setLayout(new GridLayout(10,1));
                    framee.setDefaultCloseOperation(EXIT_ON_CLOSE);
                
                    File archivo= new File("Experto.txt");
                    archivo.createNewFile();
                    FileReader fr = new FileReader (archivo);
                    BufferedReader br = new BufferedReader(fr);
                    String linea;
                    while ((linea = br.readLine()) != null){
                        JLabel text = new JLabel(linea);
                        framee.add(text);  
                        
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
     
    }
    
    //Fin del segundo constructor
        
        /**
         * Método para chequear la matriz de buttons
         * @since Buscaminas 1.0
        */
       
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
        
        //Caso en el que haya problema con alguno de los button
        if(!found) {
            System.out.println("didn't find the button, there was an error "); System.exit(-1);
        }
        Component temporaryLostComponent = null;
        
        //Actuación del programa dependiendo del button que pulsemos
        if (b[row][column].getBackground() == Color.orange){ //Ya marcada
            return;
        }else if (mines[row+1][column+1] == 1){ //Pulsemos una mina, se para el tiempo
                timer.cancel();            
                JOptionPane.showMessageDialog(temporaryLostComponent, "You set off a Mine!!!!.");
                System.exit(0);
        } else { //Caso que no haya mina
            tmp = Integer.toString(perm[row][column]);
            if (perm[row][column] == 0){
                    tmp = " ";
            }
            b[row][column].setText(tmp);
            b[row][column].setEnabled(false);
            try {
                checkifend();
            } catch (IOException ex) {
                Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (perm[row][column] == 0){
                scan(row, column);
                try {
                    checkifend();
                } catch (IOException ex) {
                    Logger.getLogger(Buscaminas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    /**
     * Método que nos permite comprobar las minas marcadas
     * @throws IOException excepción lanzada por si hay un error en la lectura
     * @since Buscaminas 1.0     
     */
    public void checkifend() throws IOException{
        int check= 0;
        for (int y = 0; y<m;y++){
            for (int x = 0;x<n;x++){
                if (b[x][y].isEnabled()){
                    check++;
                }
            }
        }
        
        //Caso en el que todas las minas hayan sido marcadas, acaba el juego
        if (check == nomines){
            timer.cancel();        
            endtime = System.nanoTime();
            Component temporaryLostComponent = null;
            int tiempoFinal =(int)((endtime-starttime)/1000000000);
            
            /*
            A continuación, cuando se ha ganado la partida si la categoría es Personalizado
            se muestra un mensaje con el tiempo que se ha tardado en conseguir ganar
            */
            if((i.equalsIgnoreCase("Personalizado"))){
                JOptionPane.showMessageDialog(temporaryLostComponent, "Congratulations you won!!! It took you "+tiempoFinal+" seconds!");    
            }else{
                /*
                En el caso contrario, será Principiante, Intermedio o Avanzado por lo que
                secrean los ficheros que puedan permitir incluir el tiempo, lo unico que varía es la ruta
                */
                File archivo;
                if (i.equalsIgnoreCase("Principiante")){
                    String ruta ="Principiante.txt";
                    archivo = new File (ruta);
                    archivo.createNewFile();
                }else if (i.equalsIgnoreCase("Intermedio")){
                    String ruta = "Intermedio.txt";  
                    archivo = new File (ruta);
                    archivo.createNewFile();
                }else {
                    String ruta = "Avanzado.txt";  
                    archivo = new File (ruta);
                    archivo.createNewFile();
                }
                    
                    
                /*
                Leemos en primer lugar el fichero ya existente para poder comprobar los tiempos
                */
                FileReader fr = new FileReader (archivo);
                BufferedReader br = new BufferedReader(fr);
                String linea;
                while ((linea = br.readLine()) != null){
                    String str[] = linea.split (" ");
                    nombres.add(str[0]);
                    tiempos.add(Integer.parseInt(str[1]));      
                        
                }
                fr.close();
                br.close();
                
                //A continuación, nos podemos encontrar con 4 casos:
                
                int tamTiempos = tiempos.size();
                
                /*
                1- El fichero esté vacio y no haya que comprobar si el tiempo se debe insertar
                en el fichero ya que, se insertará si o si
                */
                if(tamTiempos==0){
                    String nombre = JOptionPane.showInputDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                        +tiempoFinal+" seconds!\n"+
                        "INSERT YOUR NAME IF YOU WANT TO SAVE YOUR SCORE\n"+"              IF NOT, PRESS CANCEL");
                    //No se permite meter nombres con espacio, el ususario debe introducirlo de nuevo
                    while(nombre.contains(" ")){
                         nombre = JOptionPane.showInputDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                        +tiempoFinal+" seconds!\n"+
                        "INSERT YOUR NAME IF YOU WANT TO SAVE YOUR SCORE\n"+"              IF NOT, PRESS CANCEL--> SPACES ARE FORBIDDEN");
                        if(nombre==null){
                            break;
                        }
                    }
                    
                    //Se procede a escribir el nombre y la puntuación en el fichero
                    if(nombre !=null){
                        FileWriter fw= new FileWriter(archivo);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.newLine();
                        bw.write(nombre+" "+tiempoFinal+System.getProperty("line.separator"));
                        bw.flush();
                        bw.close();
                    }
                    
                /*
                2-El fichero contenga tiempos pero no los suficientes para llegar a 10, con lo que
                solo comprobamos en qué posición debe ser insertado el nuevo tiempo  
                 */ 
                }else if(tamTiempos <10){
                    String nombre = JOptionPane.showInputDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                        +tiempoFinal+" seconds!\n"+
                        "INSERT YOUR NAME IF YOU WANT TO SAVE YOUR SCORE\n"+"              IF NOT, PRESS CANCEL");
                    while(nombre.contains(" ")){
                         nombre = JOptionPane.showInputDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                        +tiempoFinal+" seconds!\n"+
                        "INSERT YOUR NAME IF YOU WANT TO SAVE YOUR SCORE\n"+"              IF NOT, PRESS CANCEL--> SPACES ARE FORBIDDEN");
                        if(nombre==null){
                            break;
                        }
                    }
                    
                    //Comprobamos el tiempo respecto a los otros
                    if(nombre!=null){
                        int posicion=-1;
                        for(int i=0; i<tamTiempos; i++){
                            if ((tiempoFinal < tiempos.get(i))){
                                posicion=i;
                                break;
                            } 
                        }   
                        
                        //Si es es el peor tiempo
                        if(posicion==-1){
                            nombres.add(nombre);
                            tiempos.add(tiempoFinal);
                        
                        //Si está en una posicion intermedia o primero
                        }else{
                            nombres.add(posicion, nombre);
                            tiempos.add(posicion, tiempoFinal);
                        }
                        
                        //Se procede a escribir el nombre y el tiempo en el fichero
                        FileWriter fw= new FileWriter(archivo);
                        BufferedWriter bw = new BufferedWriter(fw);
                        for(int i=0; i<tiempos.size();i++){
                            bw.write(nombres.get(i)+" "+tiempos.get(i)+System.getProperty("line.separator"));
                        }
                        bw.flush();
                        bw.close();
                    }
                /*
                3-En este caso, el fichero ya tiene 10 puntuaciones por lo que hay que encontrar si la puntuacion
                última es mayor que la nueva puntuación y así saber que hay que introducirla
                */                
                }else if (tamTiempos==10){
                    if (tiempos.get(9)>tiempoFinal){
                        String nombre = JOptionPane.showInputDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                            +tiempoFinal+" seconds!\n"+
                            "INSERT YOUR NAME IF YOU WANT TO SAVE YOUR SCORE\n"+"              IF NOT, PRESS CANCEL");
                        while(nombre.contains(" ")){
                         nombre = JOptionPane.showInputDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                        +tiempoFinal+" seconds!\n"+
                        "INSERT YOUR NAME IF YOU WANT TO SAVE YOUR SCORE\n"+"              IF NOT, PRESS CANCEL--> SPACES ARE FORBIDDEN");
                        if(nombre==null){
                            break;
                        }
                        }
                        if (nombre!=null){
                            //Se averigua en qué posición hay que insertarla
                            for(int i=0; i<tamTiempos; i++){ 
                                if ((tiempoFinal < tiempos.get(i))){
                                    nombres.add(i, nombre);
                                    tiempos.add(i, tiempoFinal);
                                    break;
                                }
                            }
                            
                            /*
                            Se elimina la ultima posición que será en la que se encontrará el mayor valor
                            para controlar que solo haya 10 tiempos guardados en cada fichero
                            */
                            tiempos.remove(10);
                            nombres.remove(10);
                            
                            //Se procede a escribir el nombre y el tiempo en el fichero
                            FileWriter fw= new FileWriter(archivo);
                            BufferedWriter bw = new BufferedWriter(fw);
                            for(int i=0; i<tiempos.size();i++){
                                bw.write(nombres.get(i)+" "+tiempos.get(i)+System.getProperty("line.separator"));
                            }
                            bw.flush();
                            bw.close();
                        }
                    }
                }else{     
                    
                //4-En cualquier otro caso no se deja guardar el tiempo y se muestra el mensaje de enhorabuena
                    JOptionPane.showMessageDialog(temporaryLostComponent, "Congratulations you won!!! It took you "
                            +tiempoFinal+" seconds!"); 
                }
            }
        }
    }
    /**
     * Metodo que permite conocer lo que hay alrededor del button señalado
     * @param x fila actual
     * @param y columna actual
     * @since Buscaminas 1.0
     */
    
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
 
    /**
     * Método para comprobar las minas de alrededor
     * @param a fila actual
     * @param y columna actual
     * @return minas alredador
     * @since Buscaminas 1.0
     */
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
    
    /**
     * Método principal
     * @param args lectura de parámetros
     */
    public static void main(String[] args){

        PantallaInicio pi;
        pi= new PantallaInicio();
        pi.setVisible(true);

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
 
    /**
     * Método para marcar y desmarcar las minas
     * @param e acción del button
     * @since Buscaminas 1.0
     */
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
                contmines--;    //cuando clickamos el botón izquierdo el numero de minas a mostrar tiene que decrecer
                minas.setText("Mines: "+contmines+" ");
            } else if (guesses[row+1][column+1] == 1){
                b[row][column].setText("?");
                guesses[row+1][column+1] = 0;
                b[row][column].setBackground(null);
                contmines++;    //cuando clickamos el botón izquierdo por segunda vez el numero de minas a mostrar tiene que crecer
                minas.setText("Mines: "+contmines+" ");
            }
        }
    }
 
    @Override
    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }
}//end class