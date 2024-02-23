package solitaire;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;

public class Solitaire extends JFrame {


    public static GamePanel getGamePanel() {
		return gamePanel;
	}

	public static void setGamePanel(GamePanel gamePanel) {
		Solitaire.gamePanel = gamePanel;
	}

	public static int getPanelWidth() {
		return boxWidth;
	}

	public static int getPanelHeight() {
		return boxHeight;
	}
	static protected GamePanel gamePanel;
    public static final int boxWidth = 700, boxHeight = 500;

    public Solitaire() {
        initializeFrame();
        initializeGamePanel();
        add(gamePanel);
        pack();
    }

    private void initializeFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(boxWidth, boxHeight));
    }

    private void initializeGamePanel() {
        gamePanel = new GamePanel();

        JButton restartButton = new JButton("Restart");
        restartButton.setBounds(boxWidth - 70, 10, 60, 50);
        restartButton.setFont(new Font(restartButton.getFont().getName(), Font.PLAIN, 10));
        restartButton.addActionListener(new RestartButtonListener());

        JButton undoButton = new JButton("Undo");
        undoButton.setBounds(boxWidth - 70, 70, 60, 50);
        undoButton.setFont(new Font(undoButton.getFont().getName(), Font.PLAIN, 10));
        undoButton.addActionListener(new UndoButtonListener());

        gamePanel.add(restartButton);
        gamePanel.add(undoButton);
    }

    private void restartGame() {
        dispose();
        SwingUtilities.invokeLater(() -> new Solitaire().setVisible(true));
    }

    public static void main(String[] args) {
        new Solitaire().setVisible(true);
    }

    static class RestartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int option = JOptionPane.showConfirmDialog(gamePanel, "Are you sure you want to restart?", "Restart", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                new Solitaire().restartGame();
            }
        }
    }

    static class UndoButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            gamePanel.undoLastMove();
        }
    }

    class Card {
        private Image image;
        private int val;
        private String suit;
        private boolean up;
        private Colour colour;
        private static final String DIRECTORY = "cards/";
        private static final String EXTENSION = ".gif";
        private static final String CARD_BACK_FILENAME = "back001";
        private static final String CARD_OUTLINE_FILENAME = "bottom01";
        private static final String FP_BASE_FILENAME = "fpBase0";

        public Card(int val, Suit suit) {
            initializeCard(val, suit);
            loadImage();
        }

        private void initializeCard(int val, Suit suit) {
            this.val = val;
            this.suit = getSuitAbbreviation(suit);
            this.colour = getColour(suit);
            this.up = false;
        }

        private void loadImage() {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(DIRECTORY + getFileName()));
                this.image = icon.getImage();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        private String getFileName() {
            return String.format("%s%s%s", val < 10 ? "0" + val : val, suit, EXTENSION);
        }

        public Image getCardImage() {
            return image;
        }

        public boolean upwards() {
            return up;
        }

        public Colour getColour() {
            return colour;
        }

        public void showFace() {
            up = true;
        }

        public static Image getBase(int suit) {
            return new ImageIcon(Card.class.getResource(DIRECTORY + FP_BASE_FILENAME + suit + EXTENSION)).getImage();
        }

        public static Image getCardOutline() {
            return new ImageIcon(Card.class.getResource(DIRECTORY + CARD_OUTLINE_FILENAME + EXTENSION)).getImage();
        }

        public static Image getCardBack() {
            return new ImageIcon(Card.class.getResource(DIRECTORY + CARD_BACK_FILENAME + EXTENSION)).getImage();
        }

        public int getval() {
            return val;
        }

        public String getSuit() {
            return suit;
        }
        public static String getSuitAbbreviation(Suit suit) {
            return switch (suit) {
                case Clubs -> "c";
                case Diamonds -> "d";
                case Spades -> "s";
                case Hearts -> "h";
            };
        }

        private Colour getColour(Suit suit) {
            return (suit == Suit.Clubs || suit == Suit.Spades) ? Colour.Black : Colour.Red;
        }

        @Override
        public String toString() {
            return val + " of " + suit;
        }
    }
 
    enum Colour {
    	
    	Red, Black, Neither

    }
    class Deck extends Pile {

    	   public Deck(int x, int y) {
    	        super(x, y);
    	        super.setSize(70, 100);
    	        initializeDeck();
    	    }

    	    private void initializeDeck() {
    	        for (Suit suit : Suit.values()) {
    	            for (int j = 1; j <= 13; ++j) {
    	                push(new Card(j, suit));
    	            }
    	        }
    	        Collections.shuffle(cards);
    	    }

    	    public boolean isEmpty() {
    	        return cards.isEmpty();
    	    }

    	    public void reshuffle() {
    	        if (isEmpty()) {
    	            moveCardsFromWasteToDeck();
    	            Collections.shuffle(cards);
    	        } else {
    	            dealCardToWaste();
    	        }
    	    }

    	    private void moveCardsFromWasteToDeck() {
    	        Waste wastePile = GamePanel.getWastePile();
    	        while (!wastePile.isEmpty()) {
    	            push(wastePile.pop());
    	        }
    	    }

    	    private void dealCardToWaste() {
    	        Waste wastePile = GamePanel.getWastePile();
    	        if (isEmpty()) {
    	            moveCardsFromWasteToDeck();
    	        }
    	        wastePile.push(pop());
    	        wastePile.topCard().showFace();
    	    }

    	    @Override
    	    protected void paintComponent(Graphics g) {
    	        super.paintComponent(g);
    	        Graphics2D graphic = (Graphics2D) g;
    	        graphic.setStroke(new BasicStroke(5));
    	        graphic.setColor(new Color(0, 128, 0));
    	        graphic.drawRect(0, 0, 70, this.getHeight());

    	        if (!isEmpty()) {
    	            g.drawImage(Card.getCardBack(), 0, 0, 70, this.getHeight(), this);
    	        }
    	    }

    }
    class Foundation extends Pile {

        private int suit;

        public Foundation(int x, int y, int suit) {
            super(x, y);
            setSize(70, 100);
            this.suit = suit;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isEmpty()) {
                drawBase(g);
            } else {
                drawTopCard(g);
            }
        }

        private void drawBase(Graphics g) {
            g.drawImage(Card.getBase(suit), 0, 0, getWidth(), getHeight(), this);
        }

        private void drawTopCard(Graphics g) {
            g.drawImage(topCard().getCardImage(), 0, 0, getWidth(), getHeight(), this);
        }

        public void moveFromWaste(Waste source, Card card) {
            if (accepts(card)) {
                push(source.pop());
                source = null;
            }
        }

        public void moveTo(Tableau destination, Card card) {
            if (destination.accepts(card)) {
                destination.push(pop());
            }
        }

        public boolean accepts(Card card) {
            if (!isEmpty()) {
                return topCard().getval() == card.getval() - 1 && topCard().getSuit().equals(card.getSuit());
            }
            return card.getval() == 1 && intToSuit(card.getSuit()); // Ace
        }

        private boolean intToSuit(String pSuit) {
            switch (pSuit) {
                case "s":
                    return suit == 1;
                case "h":
                    return suit == 2;
                case "c":
                    return suit == 3;
                case "d":
                    return suit == 4;
                default:
                    return false;
            }
        }
    }
    class GameMoveListener extends MouseInputAdapter {

        private Deck deck = GamePanel.getDeck();
        private Waste waste = null;
        private Tableau pressedTaubleau = null;
        private Foundation pressedFoundation = null;
        private Card pressedCard = null;
        static Stack<Move> moveStack = new Stack<>();

     
        @Override
        public void mousePressed(MouseEvent e) {
            Component pressedComponent = e.getComponent().getComponentAt(e.getPoint());
            handleComponentAction(pressedComponent, e);
            e.getComponent().repaint();
        }

        private void handleComponentAction(Component component, MouseEvent e) {
            switch (component.getClass().getSimpleName()) {
                case "Foundation":
                    pressedFoundation = (Foundation) component;
                    pressedTaubleau = null;
                    waste = null;
                    pressedCard = pressedFoundation.topCard();
                    break;
                case "Tableau":
                    pressedTaubleau = (Tableau) component;
                    waste = null;
                    pressedCard = pressedTaubleau.getClickedCard(e.getY() - 150);
                    for (Foundation foundation : GamePanel.getFoundationPiles()) {
                        if (pressedTaubleau.moveTo(foundation, pressedCard)) {
                            pressedTaubleau = null;
                            break;
                        }
                    }
                    break;
                case "Deck":
                    Deck clickedDeck = (Deck) component;
                    if (clickedDeck.isEmpty()) {
                        clickedDeck.reshuffle();
                    } else {
                        Waste waste = GamePanel.getWastePile();
                        waste.push(clickedDeck.pop());
                        waste.topCard().showFace();
                    }
                    break;
                case "Waste":
                    pressedTaubleau = null;
                    waste = GamePanel.getWastePile();
                    pressedCard = waste.topCard();
                    if (pressedCard != null) {
                        for (Foundation foundation : GamePanel.getFoundationPiles()) {
                            foundation.moveFromWaste(waste, pressedCard);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

       
        @Override
        public void mouseReleased(MouseEvent e) {
            Component releasedComponent = e.getComponent().getComponentAt(e.getPoint());
            handleReleasedComponentAction(releasedComponent);
            e.getComponent().repaint();
        }

        private void handleReleasedComponentAction(Component component) {
            switch (component.getClass().getSimpleName()) {
                case "Tableau":
                    if (waste != null) {
                        Tableau destination = (Tableau) component;
                        if (!waste.isEmpty()) {
                            destination.moveFromWaste(waste, pressedCard);
                        }
                        waste.repaint();
                    } else if (pressedTaubleau != null) {
                        Tableau source = pressedTaubleau;
                        Tableau destination = (Tableau) component;
                        source.moveTo(destination, pressedCard);
                        source.repaint();
                        moveStack.push(new Move(destination, source, pressedCard));
                    } else if (pressedFoundation != null) {
                        Foundation source = pressedFoundation;
                        Tableau destination = (Tableau) component;
                        source.moveTo(destination, pressedCard);
                        source.repaint();
                        destination.repaint();
                        moveStack.push(new Move(destination, source, pressedCard));
                    }
                    break;
                default:
                    break;
            }
        }

        public static void undoMove() {
            if (!moveStack.isEmpty()) {
                Move lastMove = moveStack.pop();
                lastMove.undo();
            }
        }

        class Move {
            private Pile sourcePile;
            private Pile destinationPile;
            private Card card;

            public Move(Pile sourcePile, Pile destinationPile, Card card) {
                this.sourcePile = sourcePile;
                this.destinationPile = destinationPile;
                this.card = card;
            }

            public void undo() {
                // Move the card back to its source pile
            	sourcePile.pop(); // Remove the card from the destination pile
            	destinationPile.push(card); // Add the card back to the source pile

                // update the GUI to reflect the undone move
                destinationPile.repaint();
                sourcePile.repaint();
            
            
            }
        
        }
   
    }class GamePanel extends JPanel {
    	 private static final int tmp = 80;
    	    private static final Point DeckPosition = new Point(500, 20);
    	    private static final Point TableauPosition = new Point(20, 150);
    	    private static final int TABLEAU_OFFSET = 80;
    	    private static Deck deck;
    	    private static Waste waste;
    	    private static Foundation[] foundationPiles;
    	    private static Tableau[] tableau;
    	    private GameMoveListener gameMoveListener;

    	    public GamePanel() {
    	        setLayout(null);
    	        initializePiles();
    	        gameMoveListener = new GameMoveListener(); // Initialize the gameMoveListener
    	        addMouseListener(gameMoveListener);
    	        addMouseMotionListener(gameMoveListener);
    	    }

    	    private void initializePiles() {
    	        deck = createDeck();
    	        waste = createWaste();
    	        foundationPiles = createFoundationPiles();
    	        tableau = createTableau();
    	        
    	        add(deck);
    	        add(waste);
    	        for (Foundation pile : foundationPiles) {
    	            add(pile);
    	        }
    	        for (Tableau pile : tableau) {
    	            add(pile);
    	        }
    	    }

    	    private Deck createDeck() {
    	        return new Deck(DeckPosition.x, DeckPosition.y);
    	    }

    	    private Waste createWaste() {
    	        return new Waste(DeckPosition.x - tmp, DeckPosition.y);
    	    }

    	    private Foundation[] createFoundationPiles() {
    	        Foundation[] piles = new Foundation[4];
    	        for (int i = 0; i < piles.length; ++i) {
    	            piles[i] = new Foundation(20 + tmp * i, 20, i + 1);
    	            add(piles[i]);
    	        }
    	        return piles;
    	    }

    	    private Tableau[] createTableau() {
    	        Tableau[] piles = new Tableau[7];
    	        for (int i= 0; i < piles.length; ++i) {
    	            piles[i] = new Tableau(TableauPosition.x + TABLEAU_OFFSET * i, TableauPosition.y, i + 1);
    	            add(piles[i]);
    	        }
    	        return piles;
    	    }


    	    public static Foundation[] getFoundationPiles() {
    	        return foundationPiles;
    	    }

    	    public static Waste getWastePile() {
    	        return waste;
    	    }

    	    public static Deck getDeck() {
    	        return deck;
    	    }

    	    @Override
    	    protected void paintComponent(Graphics g) {
    	        super.paintComponent(g);
    	        g.setColor(new Color(0, 128, 0)); // Green color
    	        g.fillRect(0, 0, getWidth(), getHeight());
    	    }

    	    public void undoLastMove() {
    	        if (gameMoveListener != null) {
    	            gameMoveListener.undoMove();
    	        }
    	        repaint();
    	    }
    }
    abstract class Pile extends JPanel {
    	
    	protected int x, y;
    	protected Stack<Card> cards;

    	public Pile(int x, int y) {
    		super.setLocation(x, y);
    		cards = new Stack<>();
    		
    	}
  
    	public Card topCard() {
            return cards.isEmpty() ? null : cards.peek();

    	}
    	
    	public Card pop() {
            return cards.isEmpty() ? null : cards.pop();
    	}
    	
    	public void push(Card someCard) {
    		this.cards.push(someCard);
    	}
    	
    	public boolean isEmpty() {
    		return this.cards.isEmpty();
    	}
    
    	

    }
    enum Suit {
    	
    	Spades, Hearts, Clubs, Diamonds

    }
    class Tableau extends Pile {


        public Tableau(int x, int y, int initSize) {
            super(x, y);
            setInitialSize(initSize);
        }

        private void setInitialSize(int initSize) {
            setSize(70, 450);
            setOpaque(false);
            for (int i = 0; i < initSize; ++i) {
                push(GamePanel.getDeck().pop());
            }

            if (initSize > 0) {
                topCard().showFace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D graphic = (Graphics2D) g;
          
            drawGradient(graphic);
            drawCards(g);
        }

     

        private void drawGradient(Graphics2D graphic) {
            graphic.setPaint(new GradientPaint(36, 0, new Color(98, 0, 36, 80), 36, 60, new Color(0, 0, 0, 0)));
            graphic.fillRect(0, 0, getWidth(), getHeight());
        }

        private void drawCards(Graphics g) {
            int cardYPos = 0;
            if (!isEmpty()) {
                for (Card c : cards) {
                    if (c.upwards()) {
                        g.drawImage(c.getCardImage(), 0, cardYPos, 70, 100, this);
                        cardYPos += 20;
                    } else {
                        g.drawImage(Card.getCardBack(), 0, cardYPos, 70, 100, this);
                        cardYPos += 20;
                    }
                }
            }
        }

        public void moveFromWaste(Waste source, Card card) {
            if (accepts(card)) {
                push(source.pop());
            }
            source = null;
        }

        public boolean accepts(Card card) {
            if (!isEmpty()) {
                return topCard().getval() == card.getval() + 1 && !topCard().getColour().equals(card.getColour());
            }
            return card.getval() == 13;
        }

        public boolean moveTo(Foundation destination, Card card) {
            if (destination.accepts(card)) {
                destination.push(pop());
                if (!isEmpty()) {
                    topCard().showFace();
                }
                return true;
            }
            return false;
        }

        public void moveTo(Tableau destination, Card card) {
            if (!isEmpty() && (isEmpty() || destination.accepts(card))) {
                Deque<Card> cardsToMove = new ArrayDeque<>();
                while (!isEmpty() && topCard() != card) {
                    cardsToMove.push(pop());
                }
                if (!isEmpty()) {
                    cardsToMove.push(pop());
                    while (!cardsToMove.isEmpty()) {
                        destination.push(cardsToMove.pop());
                    }
                    if (!isEmpty()) {
                        topCard().showFace();
                    }
                }
            }
        }
        
        public Card getClickedCard(int y) {
            int index = y / 20;
            if (!isEmpty() && index < cards.size()) {
                Card returnMe = cards.get(index);
                if (returnMe.upwards()) {
                    return returnMe;
                }
            }
            return !isEmpty() ? cards.peek() : null;
        }
    	

    }
    class Waste extends Pile{


        public Waste(int x, int y) {
            super(x, y);
            super.setSize(70, 100);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawCard(g);
        }

        private void drawCard(Graphics g) {
            if (isEmpty()) {
                g.drawImage(Card.getCardOutline(), 0, 0, 70, getHeight(), this);
            } else {
                g.drawImage(topCard().getCardImage(), 0, 0, 70, getHeight(), this);
            }
        }
    }
}