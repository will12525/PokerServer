import java.util.ArrayList;
import java.util.List;

/**
 * Created by william on 12/4/2015.
 */
public class DetermineRank {
    public DetermineRank()
    {

    }
    /*
     x   c22 royal flush
     x   c21 straight flush
     x   c20 four of a kind
     x   c19 full house
     x   c18 flush
     x   c17 straight
     x   c16 three of a kind
     x   c15 two pair
     x   c14 pair
     x   c13 high card

     */
    public String decodeRank(String rank)
    {
        int type = Integer.parseInt(rank.substring(0,2));
        if(type==22)
        {
            return "Royal Flush";
        }
        if(type==21)
        {
            return "Straight Flush ending with a "+cardType(Integer.parseInt(rank.substring(2,4)));
        }
        if(type==20)
        {
            return "Four of a kind with rank "+cardType(Integer.parseInt(rank.substring(2,4)));
        }
        if(type==19)
        {
            return "Full house with ranks "+cardType(Integer.parseInt(rank.substring(2,4)))+" and "+cardType(Integer.parseInt(rank.substring(4,6)));
        }
        if(type==18)
        {
            return "Flush";
        }
        if(type==17)
        {
            return "Straight ending with a "+cardType(Integer.parseInt(rank.substring(2,4)));
        }
        if(type==16)
        {
            return "Three of a kind with rank "+cardType(Integer.parseInt(rank.substring(2,4)));
        }
        if(type==15)
        {
            return "Two pair with ranks "+cardType(Integer.parseInt(rank.substring(2,4)))+" and "+cardType(Integer.parseInt(rank.substring(4,6)));
        }
        if(type==14)
        {
            return "A pair of "+cardType(Integer.parseInt(rank.substring(2,4)))+"'s";
        }
        if(type==13)
        {
            return "High card of rank "+cardType(Integer.parseInt(rank.substring(2,4)));
        }

        return "";
    }
    public String cardType(int card)
    {
        switch(card)
        {
            case 1: return "Ace";
            case 2: return "Two";
            case 3: return "Three";
            case 4: return "Four";
            case 5: return "Five";
            case 6: return "Six";
            case 7: return "Seven";
            case 8: return "Eight";
            case 9: return "Nine";
            case 10: return "Ten";
            case 11: return "Jack";
            case 12: return "Queen";
            case 13: return "King";
            default: return "Something went wrong";
        }

    }
    public String determineRank(List<Card> hand)
    {
        List<Card> sorted = sort(hand);
        /*for(int x=0;x<sorted.size();x++)
        {
            System.out.println(sorted.get(x).getType());
        }*/

        String handValue = royalFlush(sorted);
        if(!handValue.equals(""))
        {
            return handValue;
        }
        handValue = straightFlush(sorted);
        if(!handValue.equals("")) {
            if (handValue.substring(0, 2).equals("21")) {
                return handValue;
            }
        }
        handValue = someOfAKind(sorted,4);
        if(!handValue.equals(""))
        {
            return handValue;
        }
        handValue = fullHouse(sorted);
        if(!handValue.equals(""))
        {
            return handValue;
        }
        handValue = flush(sorted);
        if(!handValue.equals(""))
        {
            return handValue;
        }
        handValue = straightFlush(sorted);
        if(!handValue.equals("")) {
            if (handValue.substring(0, 2).equals("17")) {
                return handValue;
            }
        }
        handValue = someOfAKind(sorted,3);
        if(!handValue.equals(""))
        {
            return handValue;
        }
        handValue = twoPair(sorted);
        if(!handValue.equals(""))
        {
            return handValue;
        }
        handValue = pair(sorted);

        if(!handValue.equals(""))
        {
            return handValue;
        }
        return "13"+convertNum(sorted.get(sorted.size()-1).getValue());

    }

    private String pair(List<Card> hand)
    {
        int sequence=1;
        int lastNum=hand.get(hand.size()-1).getValue();
        for(int x=hand.size()-2;x>=0;x--) {
            int value = hand.get(x).getValue();
            if (value == lastNum) {
                sequence++;
            } else {
                lastNum = value;
                sequence = 1;
            }
            if(sequence==2)
            {
                return "14"+convertNum(lastNum);
            }
        }
        return "";
    }
    private String twoPair(List<Card> hand)
    {
        List<Card> falseHand = new ArrayList<>();
        falseHand.addAll(hand);
        String firstPair = pair(falseHand);
        if (firstPair.equals(""))
        {
            return "";
        }
        else
        {
            List<Card> removeCards = new ArrayList<>();
            for(int x=0;x<falseHand.size();x++)
            {
                Card card = falseHand.get(x);
                if(convertNum(card.getValue()).equals(firstPair.substring(2)))
                {
                    removeCards.add(card);
                }
            }

            falseHand.removeAll(removeCards);

            String aPair = pair(falseHand);
            if(aPair.equals(""))
            {
                return "";
            }
            else
            {
                return "15"+firstPair.substring(2)+aPair.substring(2);
            }
        }
    }
    //four of a kind and 3 of a kind
    private String someOfAKind(List<Card>hand,int amount)
    {
        int sequence=1;
        int lastNum=hand.get(hand.size()-1).getValue();
        for(int x=hand.size()-2;x>=0;x--)
        {
            int value = hand.get(x).getValue();
            if(value==lastNum)
            {
                sequence++;
            }
            else
            {
                lastNum=value;
                sequence=1;
            }
            if(sequence>=amount) {
                if (amount == 4) {
                    return "20"+convertNum(lastNum);
                }
                if(amount==3)
                {
                    return "16"+convertNum(lastNum);
                }
            }
        }
        return "";
    }
    private String flush(List<Card>hand)
    {

        if(!compareSuit(hand).equals(""))
        {
            return "18";
        }
        return "";
    }
    private String fullHouse(List<Card>hand)
    {
        List<Card> falseHand = new ArrayList<>();
        falseHand.addAll(hand);
        String threeOfAKind = someOfAKind(falseHand,3);
        if(threeOfAKind.equals(""))
        {
            return "";
        }
        else
        {
            List<Card> removeCards = new ArrayList<>();
            for(int x=0;x<falseHand.size();x++)
            {
                Card card = falseHand.get(x);
                if(convertNum(card.getValue()).equals(threeOfAKind.substring(2)))
                {
                    removeCards.add(card);
                }
            }
            falseHand.removeAll(removeCards);

            String aPair = pair(falseHand);
            if(aPair.equals(""))
            {
                return "";
            }
            else
            {
                return "19"+threeOfAKind.substring(2)+aPair.substring(2);
            }
        }
    }

    private String straightFlush(List<Card> hand)
    {
        //returns a straight or straight flush
        int sequence=1;
        int lastNum=hand.get(hand.size()-1).getValue();
        int fallBack=0;
        //is the high card
        String flush = compareSuit(hand);
        for (int x=hand.size()-2;x>=0;x--)
        {
            int value= hand.get(x).getValue();
            if(value==lastNum-1)
            {
                lastNum=value;
                sequence++;
            }
            else if(value==lastNum)
            {
                fallBack = x;
            }
            else
            {
                lastNum=value;
                sequence=1;
            }
            if(sequence>=5)
            {
                if(flush.equals(""))
                {
                    return 17+convertNum(lastNum);
                }
                else
                {
                    return 21+flush;
                }
            }
            if(x==0)
            {
                if(fallBack==0)
                {
                    return "";
                }
                x=fallBack;
                fallBack=0;
                sequence=1;
            }
        }

        return "";
    }
    private String royalFlush(List<Card> hand)
    {
        boolean ace = false;
        boolean king = false;
        boolean queen = false;
        boolean jack = false;
        boolean ten = false;
        List<Card> possibleStraight = new ArrayList<>();

        for(int x=0;x<hand.size();x++)
        {
            Card card = hand.get(x);
            if(card.getValue()==14)
            {
                ace=true;
                possibleStraight.add(card);
            }
            if(card.getValue()==10)
            {
                ten=true;
                possibleStraight.add(card);
            }
            if(card.getValue()==11)
            {
                jack=true;
                possibleStraight.add(card);
            }
            if(card.getValue()==12)
            {
                queen=true;
                possibleStraight.add(card);
            }
            if (card.getValue()==13)
            {
                king=true;
                possibleStraight.add(card);
            }
        }
        if(ace&&king&&queen&&jack&&ten)
        {
            String possStraight = compareSuit(possibleStraight);
            if(!possStraight.equals(""))
            {
                return "22";
            }
        }
        return "";
    }


    private String compareSuit(List<Card> hand)
    {
        int heart=0;
        int diamond=0;
        int club = 0;
        int spade=0;
        int highClub=0;
        int highDiamond=0;
        int highSpade=0;
        int highHeart=0;
        for(int x=0;x<hand.size();x++)
        {
            Card card = hand.get(x);
            if(card.getSuite().equals("heart"))
            {
                heart++;
                highHeart=card.getValue();
            }
            if(card.getSuite().equals("diamond"))
            {
                diamond++;
                highDiamond=card.getValue();
            }
            if(card.getSuite().equals("club"))
            {
                club++;
                highClub=card.getValue();
            }
            if(card.getSuite().equals("spade"))
            {
                spade++;
                highSpade=card.getValue();
            }
        }
        if(heart>=5)
        {
            return convertNum(highHeart);
        }
        if(diamond>=5)
        {
            return convertNum(highDiamond);
        }
        if(spade>=5)
        {
            return convertNum(highSpade);
        }
        if(club>=5)
        {
            return convertNum(highClub);
        }
        return "";
    }
    private String convertNum(int num)
    {
        if(num==14)
        {
            num=1;
        }
        if(num<10)
        {
            return "0"+num;
        }

        return ""+num;
    }
    //find the lowest value then work up from there
    private List<Card> sort(List<Card> hand)
    {

        boolean firstRun=true;
        List<Card> sorted = new ArrayList<>();
        Card lastCard=null;
        if(hand.size()==1)
        {
            sorted.add(hand.get(0));
            return sorted;
        }
        for(int x=0;x<hand.size();x++)
        {
            if(firstRun) {
                Card card1 = hand.get(x);
                Card card2 = hand.get(x + 1);
                if (card1.getValue() > card2.getValue()) {
                    lastCard = card2;
                }
                else
                {
                    lastCard=card1;
                }
                firstRun=false;
            }
            else
            {
                Card card1 = hand.get(x);
                if(card1.getValue()<lastCard.getValue())
                {
                    lastCard=card1;
                }
            }
        }

        sorted.add(lastCard);
        hand.remove(lastCard);
        if(hand.size()>0) {
            sorted.addAll(sort(hand));
        }
        return sorted;
    }
}
