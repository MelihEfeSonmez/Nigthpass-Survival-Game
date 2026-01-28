// Represents a single node in the AVL tree
public class Node {

    // DATA FIELDS
    private Card card;
    private Node left;
    private Node right;
    private int height;

    // CONSTRUCTORS
    public Node(Card card) {
        this.card = card;
        this.left = null;
        this.right = null;
        this.height = 1;
    }

    // GETTERS
    public Card getCard() {return card;}
    public Node getLeft() {return left;}
    public Node getRight() {return right;}
    public int getHeight() {return height;}

    // SETTERS
    public void setCard(Card card) {this.card = card;}
    public void setLeft(Node left) {this.left = left;}
    public void setRight(Node right) {this.right = right;}
    public void setHeight(int height) {this.height = height;}

}
