package characters;

public class Characters {
    private String name;
    private short startingLifes;
    private String ability;

    public Characters(String name, short startingLifes, String ablity) {
        setName(name);
        setStartingLifes(startingLifes);
        setAbility(ablity);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getStartingLifes() {
        return this.startingLifes;
    }

    public void setStartingLifes(short startingLifes) {
        this.startingLifes = startingLifes;
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
