package cards;

public class Weapon extends PlayingCard {
    private short range;
    private boolean unlimitedBang;

    public Weapon(String name, char suit, short rank, short range, boolean unlimitedBang) {
        super(name, suit, rank);
        setRange(range);
        setUnlimitedBang(unlimitedBang);
    }

    public Weapon(PlayingCard card){
        super(card.getName(), card.getSuit(), card.getRank());
        if(this.getName() == "Volcanic"){
            setRange((short)1);
            setUnlimitedBang(true);
        }
        else{
            setUnlimitedBang(true);
            switch (card.getName()) {
                case "Schofield":
                    setRange((short) 2);
                    break;
                case "Remington":
                    setRange((short) 3);
                    break;
                case "Rev. Carabine":
                    setRange((short) 4);
                    break;
                case "Winchester":
                    setRange((short) 5);
            }
        }
    }
    
    public short getRange() {
        return this.range;
    }

    public void setRange(short range) {
        this.range = range;
    }

    public boolean isUnlimitedBang() {
        return this.unlimitedBang;
    }

    public boolean getUnlimitedBang() {
        return this.unlimitedBang;
    }

    public void setUnlimitedBang(boolean unlimitedBang) {
        this.unlimitedBang = unlimitedBang;
    }

    @Override
    public String toString(){
        return "Distance: " + range + ")" + super.toString();
    }
}
