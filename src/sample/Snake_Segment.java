package sample;

import java.io.Serializable;

public class Snake_Segment implements Serializable
{
    static int block_size = 30;
    public int x, y;

    public Snake_Segment(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    //Losowa pozycja dla jablka
    public void randomPos()
    {
        this.x = (int)(Math.random() * ((19) + 1)) * block_size;
        this.y = (int)(Math.random() * ((19) + 1)) * block_size;
    }
}
