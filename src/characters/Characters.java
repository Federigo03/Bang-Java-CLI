package characters;

public class Characters implements ICharacters {
    private String name;
    private int startingLives;
    private String ability;

    public Characters(String name, int startingLives, String ablity) {
        setName(name);
        setStartingLives(startingLives);
        setAbility(ablity);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getStartingLives() {
        return this.startingLives;
    }

    public void setStartingLives(int startingLives) {
        this.startingLives = startingLives;
    }

    @Override
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
