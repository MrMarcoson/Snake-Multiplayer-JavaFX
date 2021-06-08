package sample;

import java.io.Serializable;
import java.util.LinkedList;

public class Snake implements Serializable
{
    static int block_size = 30;

    LinkedList<Snake_Segment> body;
    String direction;

    public Snake(int x, int y, String direction)
    {
        body = new LinkedList<>();
        body.add(new Snake_Segment(x, y));
        this.direction = direction;
    }

    //Zmiana lokalizacji segmentow weza
    public void move()
    {
        int dir_x = 0, dir_y = 0;

        if(direction.equals("UP"))
        {
            dir_x = 0;
            dir_y = -block_size;
        }

        else if(direction.equals("DOWN"))
        {
            dir_x = 0;
            dir_y = block_size;
        }

        else if(direction.equals("RIGHT"))
        {
            dir_x = block_size;
            dir_y = 0;
        }

        else if(direction.equals("LEFT"))
        {
            dir_x = -block_size;
            dir_y = 0;
        }

        else
        {
            return;
        }

        for(int i = body.size() - 1; i > 0; i--)
        {
            Snake_Segment S = new Snake_Segment(body.get(i - 1).x, body.get(i - 1).y);
            body.set(i, S);
        }

        Snake_Segment S = new Snake_Segment(body.getFirst().x + dir_x, body.getFirst().y + dir_y);
        body.set(0, S);
    }

    //Dodanie segmentu
    public void createSegment()
    {
        int dir_x = 0, dir_y = 0;

        if(direction.equals("UP"))
        {
            dir_x = 0;
            dir_y = block_size;
        }

        else if(direction.equals("DOWN"))
        {
            dir_x = 0;
            dir_y = -block_size;
        }

        else if(direction.equals("RIGHT"))
        {
            dir_x = -block_size;
            dir_y = 0;
        }

        else if(direction.equals("LEFT"))
        {
            dir_x = block_size;
            dir_y = 0;
        }

        else
        {
            return;
        }

        body.add(new Snake_Segment(body.getLast().x + dir_x, body.getLast().y + dir_y));
    }

    //sprawdzenie kolizji dla siebie i sciany
    public Boolean checkColision()
    {
        if(body.getFirst().x > 19 * block_size || body.getFirst().x < 0 || body.getFirst().y > 19 * block_size || body.getFirst().y < 0) return true;

        for(int i = 1; i < body.size(); i++)
        {
            if(body.getFirst().x == body.get(i).x && body.getFirst().y == body.get(i).y)
            {
                return true;
            }
        }

        return false;
    }

    //sprawdzenie kolizji dla innego gracza
    public Boolean checkColision(Snake enemy)
    {
        for(int i = 0; i < enemy.body.size(); i++)
        {
            if(body.getFirst().x == enemy.body.get(i).x && body.getFirst().y == enemy.body.get(i).y)
            {
                return true;
            }
        }

        return false;
    }
}
