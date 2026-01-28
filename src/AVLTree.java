// Implements a Adelson-Velsky and Landis Tree (AVL) to store the Survivor's cards
public class AVLTree {

    // DATA FIELDS
    private Node root; // Root of the tree
    private boolean sortByHealth; // true: health-based, false: attack-based

    // CONSTRUCTORS
    public AVLTree(boolean sortByHealth) {
        this.root = null;
        this.sortByHealth = sortByHealth;
    }

    // HELPER METHODS
    // Returns height of the node
    private int height(Node node) {
        if (node == null) {return 0;}
        return node.getHeight();
    }

    // Returns the height difference of right subtree and left subtree
    private int getHeightDif(Node node) {
        if (node == null) {return 0;}
        return height(node.getLeft()) - height(node.getRight());
    }

    // Updates the height of the node
    private void updateHeight(Node node) {
        if (node != null) {
            node.setHeight(1 + Math.max(height(node.getLeft()), height(node.getRight())));
        }
    }

    // METHODS
    // Rotations
    // Right rotation (left left)
    private Node rotateRight(Node node) {
        Node L = node.getLeft();
        Node LR = L.getRight();

        L.setRight(node);
        node.setLeft(LR);

        updateHeight(node);
        updateHeight(L);
        return L;
    }
    // Left rotation (right right)
    private Node rotateLeft(Node node) {
        Node R = node.getRight();
        Node RL = R.getLeft();

        R.setLeft(node);
        node.setRight(RL);

        updateHeight(node);
        updateHeight(R);
        return R;
    }

    // Compare cards based on tree type
    private int compareCards(Card c1, Card c2) {
        if (c1 == c2) return 0;  // Same object

        // Health tree: sort by health, then attack
        // Attack tree: sort by attack, then health
        if (sortByHealth) {
            if (c1.getHealthCur() != c2.getHealthCur()) {
                return c1.getHealthCur() - c2.getHealthCur();
            }
            if (c1.getAttackCur() != c2.getAttackCur()) {
                return c1.getAttackCur() - c2.getAttackCur();
            }
            return c1.getEntryOrder() - c2.getEntryOrder();
        } else {
            if (c1.getAttackCur() != c2.getAttackCur()) {
                return c1.getAttackCur() - c2.getAttackCur();
            }
            if (c1.getHealthCur() != c2.getHealthCur()) {
                return c1.getHealthCur() - c2.getHealthCur();
            }
            return c1.getEntryOrder() - c2.getEntryOrder();
        }
    }

    // Inserts card
    public void insert(Card card) {root = insertRec(root, card);}

    // Helps inserting card by recursion
    private Node insertRec(Node node, Card card) {
        if (node == null) {
            return new Node(card);
        }

        int cmp = compareCards(card, node.getCard()); // Comparison of cards
        if (cmp < 0) {
            node.setLeft(insertRec(node.getLeft(), card));
        } else {
            node.setRight(insertRec(node.getRight(), card));
        }

        // Updates card height and returns balance situation
        updateHeight(node);
        return balance(node);
    }

    // Deletes card
    public void delete(Card card) {root = deleteRec(root, card);}

    // Helps deleting card by recursion
    private Node deleteRec(Node node, Card card) {
        if (node == null) {
            return null;
        }

        if (node.getCard() == card) {
            // Found match
            if (node.getLeft() == null) {
                return node.getRight();
            } else if (node.getRight() == null) {
                return node.getLeft();
            }

            // Node has two children
            Node minNode = findMin(node.getRight());
            node.setCard(minNode.getCard());
            node.setRight(deleteRec(node.getRight(), minNode.getCard()));

            // Updates card height and returns balance situation
            updateHeight(node);
            return balance(node);
        }

        int cmp = compareCards(card, node.getCard()); // Comparison of cards

        // Continues to search for the card
        if (cmp < 0) {
            node.setLeft(deleteRec(node.getLeft(), card));
        } else {
            node.setRight(deleteRec(node.getRight(), card));
        }

        // Updates card height and returns balance situation
        updateHeight(node);
        return balance(node);
    }

    // Finds the minimum node
    private Node findMin(Node node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }

    // Balances the tree
    private Node balance(Node node) {
        if (node == null) {
            return null;
        }

        int balance = getHeightDif(node); // Decides which side is heavy

        // Left heavy
        if (balance > 1) {
            if (getHeightDif(node.getLeft()) < 0) {
                node.setLeft(rotateLeft(node.getLeft()));
            }
            return rotateRight(node);
        }

        // Right heavy
        if (balance < -1) {
            if (getHeightDif(node.getRight()) > 0) {
                node.setRight(rotateRight(node.getRight()));
            }
            return rotateLeft(node);
        }

        return node;
    }

    // Priority 1: Survive and Kill
    // Hcur > Astranger ∧ Acur ≥ Hstranger (minimal Acur -> minimal Hcur)
    public Card findPriority1(int strAttack, int strHealth) {
        Card best = null;
        best = findP1Rec(root, strAttack, strHealth, best);
        return best;
    }

    private Card findP1Rec(Node node, int strAttack, int strHealth, Card best) {
        if (node == null) return best;

        Card card = node.getCard();

        // Checks if this card satisfies priority 1
        if (card.getHealthCur() > strAttack && card.getAttackCur() >= strHealth) {
            if (best == null || isBetterP1(card, best)) {
                best = card;
            }
        }

        // Searchs both subtrees
        if (card.getAttackCur() < strHealth) {
            best = findP1Rec(node.getRight(), strAttack, strHealth, best);
        } else {
            best = findP1Rec(node.getLeft(), strAttack, strHealth, best);

            if (best == null || best.getAttackCur() >= card.getAttackCur()) {
                best = findP1Rec(node.getRight(), strAttack, strHealth, best);
            }

        }
        return best;
    }

    private boolean isBetterP1(Card c1, Card c2) {
        // Minimal attack
        if (c1.getAttackCur() != c2.getAttackCur()) {
            return c1.getAttackCur() < c2.getAttackCur();
        }
        // Minimal health
        if (c1.getHealthCur() != c2.getHealthCur()) {
            return c1.getHealthCur() < c2.getHealthCur();
        }
        // First entry
        return c1.getEntryOrder() < c2.getEntryOrder();
    }

    // Priority 2: Survive and Not Kill
    // Hcur > Astranger ∧ Acur < Hstranger (maximal Acur -> tied minimal Hcur)
    public Card findPriority2(int strAttack, int strHealth) {
        Card best = null;
        best = findP2Rec(root, strAttack, strHealth, best);
        return best;
    }

    private Card findP2Rec(Node node, int strAttack, int strHealth, Card best) {
        if (node == null) return best;

        Card card = node.getCard();

        // Checks if this card satisfies priority 2
        if (card.getHealthCur() > strAttack && card.getAttackCur() < strHealth) {
            if (best == null || isBetterP2(card, best)) {
                best = card;
            }
        }

        // Searches both subtrees
        if (card.getHealthCur() <= strAttack) {
            best = findP2Rec(node.getRight(), strAttack, strHealth, best);
        } else { // Searches both subtrees because we need to find max attack
            best = findP2Rec(node.getLeft(), strAttack, strHealth, best);
            best = findP2Rec(node.getRight(), strAttack, strHealth, best);
        }

        return best;
    }

    private boolean isBetterP2(Card c1, Card c2) {
        // Maximal attack
        if (c1.getAttackCur() != c2.getAttackCur()) {
            return c1.getAttackCur() > c2.getAttackCur();
        }
        // Minimal health
        if (c1.getHealthCur() != c2.getHealthCur()) {
            return c1.getHealthCur() < c2.getHealthCur();
        }
        // First entry
        return c1.getEntryOrder() < c2.getEntryOrder();
    }

    // Priority 3: Kill and Don’t Survive
    // Hcur ≤ Astranger ∧ Acur ≥ Hstranger (minimal Acur -> tied minimal Hcur)
    public Card findPriority3(int strAttack, int strHealth) {
        Card best = null;
        best = findP3Rec(root, strAttack, strHealth, best);
        return best;
    }

    private Card findP3Rec(Node node, int strAttack, int strHealth, Card best) {
        if (node == null) return best;

        Card card = node.getCard();

        // Checkss if this card satisfies priority 3
        if (card.getHealthCur() <= strAttack && card.getAttackCur() >= strHealth) {
            if (best == null || isBetterP3(card, best)) {
                best = card;
            }
        }

        // Searches both subtrees
        if (card.getAttackCur() < strHealth) {
            best = findP3Rec(node.getRight(), strAttack, strHealth, best);
        } else {
            // Needs to check all
            best = findP3Rec(node.getLeft(), strAttack, strHealth, best);
        }

        return best;
    }

    private boolean isBetterP3(Card c1, Card c2) {
        // Minimal attack
        if (c1.getAttackCur() != c2.getAttackCur()) {
            return c1.getAttackCur() < c2.getAttackCur();
        }
        // Minimal health
        if (c1.getHealthCur() != c2.getHealthCur()) {
            return c1.getHealthCur() < c2.getHealthCur();
        }
        // First entry
        return c1.getEntryOrder() < c2.getEntryOrder();
    }

    // Priority 4: Maximum Damage
    // (maximum Acur regardless of its Hcur -> tied minimal Hcur.)
    public Card findPriority4() {
        Card best = null;
        best = findP4Rec(root, best);
        return best;
    }

    private Card findP4Rec(Node node, Card best) {
        if (node == null) return best;

        Card card = node.getCard();

        if (best == null || isBetterP4(card, best)) {
            best = card;
        }

        // Searches both subtrees
        best = findP4Rec(node.getRight(), best);
        if (best == null || card.getAttackCur() == best.getAttackCur()) {
            best = findP4Rec(node.getLeft(), best);
        }

        return best;
    }

    private boolean isBetterP4(Card c1, Card c2) {
        // Maximal attack
        if (c1.getAttackCur() != c2.getAttackCur()) {
            return c1.getAttackCur() > c2.getAttackCur();
        }
        // Minimal health
        if (c1.getHealthCur() != c2.getHealthCur()) {
            return c1.getHealthCur() < c2.getHealthCur();
        }
        // First entry
        return c1.getEntryOrder() < c2.getEntryOrder();
    }

    // Find steal target: min attack > attLimit, min health > hpLimit
    public Card findStealTarget(int attLimit, int hpLimit) {
        Card best = null;
        best = findStealRec(root, attLimit, hpLimit, best);
        return best;
    }

    private Card findStealRec(Node node, int attLimit, int hpLimit, Card best) {
        if (node == null) return best;

        Card card = node.getCard();

        // Checks if this card can be stolen
        if (card.getAttackCur() > attLimit && card.getHealthCur() > hpLimit) {
            if (best == null || isBetterSteal(card, best)) {
                best = card;
            }
        }

        // Searches both subtrees
        if (card.getAttackCur() <= attLimit) {
            best = findStealRec(node.getRight(), attLimit, hpLimit, best);
        } else { // Needs to search both
            best = findStealRec(node.getLeft(), attLimit, hpLimit, best);
            best = findStealRec(node.getRight(), attLimit, hpLimit, best);
        }

        return best;
    }

    private boolean isBetterSteal(Card c1, Card c2) {
        // Minimal attack
        if (c1.getAttackCur() != c2.getAttackCur()) {
            return c1.getAttackCur() < c2.getAttackCur();
        }
        // Minimal health
        if (c1.getHealthCur() != c2.getHealthCur()) {
            return c1.getHealthCur() < c2.getHealthCur();
        }
        // First entry
        return c1.getEntryOrder() < c2.getEntryOrder();
    }

}
