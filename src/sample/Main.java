package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main extends Application implements Serializable{

    public static int block_size = 30;
    GraphicsContext graphics_context;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;
    private boolean isServer;
    Socket socket;
    private Snake player1;
    private Snake player2;
    private Snake_Segment apple;
    private Game_Info game_info;
    private boolean game_over = false;
    private Label label;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("Snake");
        label = new Label("");

        //Deklaracja gracza 1
        player1 = new Snake(0, 0, "RIGHT");
        player2 = new Snake(19 * block_size, 19 * block_size, "LEFT");

        //Deklaracja jablka
        apple = new Snake_Segment(0, 0);
        apple.randomPos();

        //Deklaracja info o grze
        game_info = new Game_Info(player1, player2, apple);

        //Narysowanie mapy
        Canvas canvas = new Canvas();
        canvas.setHeight(20 * block_size);
        canvas.setWidth(20 * block_size);
        graphics_context = canvas.getGraphicsContext2D();
        drawMap();

        //Polaczenie
        try
        {
            //Jesli serwer jest to socket sie otworzy
            socket = new Socket(InetAddress.getLocalHost(), 9001);
            System.out.println("Polaczylem sie z serwerem");
            isServer = false;
        }
        catch (Exception e)
        {
            //Jesli serwer nie istnieje to go otwieram
            ServerSocket server = new ServerSocket(9001);
            System.out.println("Czekam na gracza...");
            socket = server.accept();
            System.out.println("Gracz sie dolaczyl");
            isServer = true;
        }

        //Deklaracja przesylania informacji
        OutputStream outputStream = socket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);

        InputStream inputStream = socket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);

        //Wyslanie na soccet info o grze
        if(isServer)
        {
            try {
                objectOutputStream.writeObject(game_info);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Odebranie info o grze i przypisanie
        else
        {
            try {
                game_info = (Game_Info) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            player1 = game_info.player1;
            player2 = game_info.player2;
            apple = game_info.apple;
        }

        //Petla gry
        new AnimationTimer()
        {
            int frame = 1;

            public void handle(long currentNanoTime)
            {
                if(frame == 30)
                {
                    //Serwer odbiera kierunek p2 i oblicza cala mape
                    if(isServer)
                    {
                        try {
                            player2.direction = (String) objectInputStream.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        //Aktualizacja polozenia dla kazdego segmentu gracza
                        player1.move();
                        player2.move();

                        //Detekcja kolizji miedzy jablkiem a graczem1
                        if(player1.body.getFirst().x == apple.x && player1.body.getFirst().y == apple.y)
                        {
                            player1.createSegment();
                            apple.randomPos();
                        }

                        //Detekcja kolizji miedzy jablkiem a graczem2
                        if(player2.body.getFirst().x == apple.x && player2.body.getFirst().y == apple.y)
                        {
                            player2.createSegment();
                            apple.randomPos();
                        }

                        //sprawdzenie czy gracz koliduje z czyms
                        boolean player1_colliding;
                        player1_colliding = player1.checkColision();
                        if(!player1_colliding) player1_colliding = player1.checkColision(player2);

                        boolean player2_colliding;
                        player2_colliding = player2.checkColision();
                        if(!player2_colliding) player2_colliding = player2.checkColision(player1);

                        //Jesli jest kolizja to przeslimy kierunek jako informacje o tym kto wygral
                        if(player1_colliding && player2_colliding)
                        {
                            game_over = true;
                            player1.direction = "DRAW";
                            player2.direction = "DRAW";
                        }

                        else if(player1_colliding)
                        {
                            game_over = true;
                            player1.direction = "P2";
                            player2.direction = "P2";
                        }

                        else if(player2_colliding)
                        {
                            game_over = true;
                            player1.direction = "P1";
                            player2.direction = "P1";
                        }

                        game_info.player1 = player1;
                        game_info.player2 = player2;
                        game_info.apple = apple;

                        //Wyslanie informacji o grze do klienta
                        try {
                            objectOutputStream.reset();
                            objectOutputStream.writeObject(game_info);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    else
                    {
                        //Wyslanie kierunku weza
                        try {
                            objectOutputStream.reset();
                            objectOutputStream.writeObject(player2.direction);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //Pobranie wszystkich informacji o grze
                        try {
                            game_info = (Game_Info) objectInputStream.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        player1 = game_info.player1;
                        player2 = game_info.player2;
                        apple = game_info.apple;
                    }

                    if(player1.direction.equals("DRAW") || player2.direction.equals("DRAW"))
                    {
                        label.setText("REMIS!");
                        this.stop();
                    }

                    else if(player1.direction.equals("P1") || player2.direction.equals("P1"))
                    {
                        label.setText("GRACZ ZIELONY WYGRYWA!");
                        this.stop();
                    }

                    else if(player1.direction.equals("P2") || player2.direction.equals("P2"))
                    {
                        label.setText("GRACZ NIEBIESKI WYGRYWA!");
                        this.stop();
                    }

                    //Wyczyszczenie mapy
                    drawMap();

                    //Rysowanie jablka
                    drawApple();

                    //Rysowanie wezy
                    drawSnake(player1);
                    drawSnake(player2);
                }

                frame++;
                if(frame == 31) frame = 1;
            }
        }.start();

        Group group = new Group(canvas);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(group);
        stackPane.getChildren().add(label);
        Scene scene = new Scene(stackPane, block_size * 20, block_size * 20);

        //Movement
        scene.setOnKeyPressed(
                keyEvent -> {
                    if(!game_over)
                    {
                        if(keyEvent.getCode().toString().equals("UP"))
                        {
                            if(isServer) player1.direction = "UP";
                            else player2.direction = "UP";
                        }

                        if(keyEvent.getCode().toString().equals("DOWN"))
                        {
                            if(isServer) player1.direction = "DOWN";
                            else player2.direction = "DOWN";
                        }

                        if(keyEvent.getCode().toString().equals("RIGHT"))
                        {
                            if(isServer) player1.direction = "RIGHT";
                            else player2.direction = "RIGHT";
                        }

                        if(keyEvent.getCode().toString().equals("LEFT"))
                        {
                            if(isServer) player1.direction = "LEFT";
                            else player2.direction = "LEFT";
                        }
                    }
                }
        );

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void drawMap()
    {
        int r = 0;
        for(int i = 0; i < 20 * block_size; i += block_size)
        {
            for(int j = 0; j < 20 * block_size; j += block_size)
            {
                if(r % 2 == 0) graphics_context.setFill(Color.WHITESMOKE);
                else graphics_context.setFill(Color.GHOSTWHITE);
                graphics_context.fillRect(j, i, block_size, block_size);
                r++;
            }
            r++;
        }
    }

    public void drawSnake(Snake player)
    {
        for(int i = 0; i < player.body.size(); i++)
        {
            if(player == player1) graphics_context.setFill(Color.GREEN);
            else graphics_context.setFill(Color.BLUE);
            graphics_context.fillRect(player.body.get(i).x, player.body.get(i).y, block_size, block_size);
        }
    }

    public void drawApple()
    {
        graphics_context.setFill(Color.RED);
        graphics_context.fillOval(apple.x, apple.y, block_size, block_size);
    }

    public static void main(String[] args) {
        launch(args);
    }

    //Metoda gdy gracz wylaczy gre
    @Override
    public void stop(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
