package cards;

public class Weapon extends PlayingCard {
    private int range;
    private boolean unlimitedBang;

    public Weapon(String name, char suit, int rank, int range, boolean unlimitedBang) {
        super(name, suit, rank);
        setRange(range);
        setUnlimitedBang(unlimitedBang);
    }

    public Weapon(PlayingCard card){
        super(card.getName(), card.getSuit(), card.getRank());
        if(this.getName() == "Volcanic"){
            setRange(1);
            setUnlimitedBang(true);
        }
        else{
            setUnlimitedBang(false);
            switch (card.getName()) {
                case "Schofield":
                    setRange(2);
                    break;
                case "Remington":
                    setRange(3);
                    break;
                case "Rev. Carabine":
                    setRange(4);
                    break;
                case "Winchester":
                    setRange(5);
            }
        }
    }
    
    public int getRange() {
        return this.range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public boolean isUnlimitedBang() {
        return this.unlimitedBang;
    }

    public void setUnlimitedBang(boolean unlimitedBang) {
        this.unlimitedBang = unlimitedBang;
    }

    @Override
    public String toString(){
        return super.toString() + " Range:(" + range + ")";
    }
}
