import java.awt.image.BufferedImage;

/**
 * Created by lawrencew on 11/25/2015.
 */
public class Card {
    private String suite="";
    private int value = 0;


    public Card(String suite, int value)
    {
        this.suite=suite;
        this.value=value;
      //  this.card=card;
    }
    public String getSuite() {
        return suite;
    }
    public int getValue() {

        if(value==1)
        {
            return 14;
        }
        return value;
    }

    public int getColor()
    {
        if(suite.equals("heart")||suite.equals("diamond"))
        {
            return 1;
        }
        return 0;
    }
    public String getType()
    {
        return (value+suite);
    }
}