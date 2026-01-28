// Represents a card with attack and health attributes.
public class Card {

    // DATA FIELDS
    private String name;

    private int attackInit;
    private int attackBase;
    private int attackCur;

    private int healthInit;
    private int healthBase;
    private int healthCur;

    private int entryOrder;

    // CONSTRUCTORS
    // Default constructor
    public Card(){
        this.name = null;

        this.attackInit = 0;
        this.attackBase = 0;
        this.attackCur = 0;

        this.healthInit = 0;
        this.healthBase = 0;
        this.healthCur = 0;

        this.entryOrder = 0;
    }

    public Card(String name, int attackInit, int healthInit, int entryOrder) {
        this.name = name;

        this.attackInit = attackInit;
        this.attackBase = attackInit;
        this.attackCur = attackInit;

        this.healthInit = healthInit;
        this.healthBase = healthInit;
        this.healthCur = healthInit;

        this.entryOrder = entryOrder;
    }

    // GETTERS
    public String getName() {return name;}

    public int getAttackInit() {return attackInit;}
    public int getAttackBase() {return attackBase;}
    public int getAttackCur() {return attackCur;}

    public int getHealthInit() {return healthInit;}
    public int getHealthBase() {return healthBase;}
    public int getHealthCur() {return healthCur;}

    public int getEntryOrder() {return entryOrder;}

    // SETTERS
    public void setName(String name) {this.name = name;}

    public void setAttackInit(int attackInit) {this.attackInit = attackInit;}
    public void setAttackBase(int attackBase) {this.attackBase = attackBase;}
    public void setAttackCur(int attackCur) {this.attackCur = attackCur;}

    public void setHealthInit(int healthInit) {this.healthInit = healthInit;}
    public void setHealthBase(int healthBase) {this.healthBase = healthBase;}
    public void setHealthCur(int healthCur) {this.healthCur = healthCur;}

    public void setEntryOrder(int entryOrder) {this.entryOrder = entryOrder;}

    // METHODS
    // Returns true if the card is still active
    public boolean isInDeck() {return healthCur > 0 && healthCur <= healthBase;}

    // Recalculates the current attack value based on the remaining health
    public void updateAttackAfterDamage() {
        if (healthCur > 0) {
            // Formula application
            attackCur = Math.max(1, (attackBase * healthCur) / healthBase);
        }
    }

}
