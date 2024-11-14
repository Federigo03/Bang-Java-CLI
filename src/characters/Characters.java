package characters;

public class Characters {
    private String name;
    private int startingLives;
    private String ability;

    public Characters(String name, int startingLives, String ablity) {
        setName(name);
        setStartingLives(startingLives);
        setAbility(ablity);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartingLives() {
        return this.startingLives;
    }

    public void setStartingLives(int startingLives) {
        this.startingLives = startingLives;
    }

    public String getAbility() {
        return this.ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }

    public String toString(){
        return getName() + ". Ability: " + getAbility();
    }
}
