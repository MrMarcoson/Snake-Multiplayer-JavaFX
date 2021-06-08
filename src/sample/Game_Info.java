package sample;

import java.io.Serializable;

public class Game_Info implements Serializable
{
    public Snake player1;
    public Snake player2;
    public Snake_Segment apple;

    public Game_Info(Snake player1, Snake player2, Snake_Segment apple)
    {
        this.player1 = player1;
        this.player2 = player2;
        this.apple = apple;
    }
}
