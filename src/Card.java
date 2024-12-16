import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class Card
{
    private String cardImagePath;       // Path to the card image
    private String draggingImagePath;   // Path to the dragging PNG
    private String plantGifPath;        // Path to the plant GIF (optional)
    private Class<? extends Plant> plantType; // Class type of the Plant

    private ImageView cardImageView;    // Card ImageView for dragging
    private ImageView draggingImageView; // Temporary image for dragging
    private ImageView hoverImageView;

    // Constructor used for locked cards
    public Card(String cardImagePath)
    {
        this.cardImagePath = cardImagePath;
        this.cardImageView = new ImageView(new Image(cardImagePath));
    }

    // Constructor used for unlocked cards
    public Card(String cardImagePath, String draggingImagePath, String plantGifPath, Class<? extends Plant> plantType)
    {
        this.cardImagePath = cardImagePath;
        this.draggingImagePath = draggingImagePath;
        this.plantGifPath = plantGifPath;
        this.plantType = plantType;

        this.cardImageView = new ImageView(new Image(cardImagePath));
        this.draggingImageView = new ImageView(new Image(draggingImagePath));
        // Hovering image is the same as the dragging image.
        this.hoverImageView = new ImageView(new Image(draggingImagePath));
    }

    public void cardImageViewSetProperties(int layoutX, int layoutY, int fitWidth, int fitHeight, boolean preserveRatio, boolean setVisible)
    {
        // Configure card properties
        cardImageView.setLayoutX(layoutX);
        cardImageView.setLayoutY(layoutY);

        cardImageView.setFitWidth(fitWidth);
        cardImageView.setFitHeight(fitHeight);

        cardImageView.setPreserveRatio(preserveRatio);
        cardImageView.setVisible(setVisible);
    }

    public void draggingImageViewSetProperties(int fitWidth, int fitHeight, boolean preserveRatio, boolean setVisible)
    {
        draggingImageView.setFitWidth(fitWidth);
        draggingImageView.setFitHeight(fitHeight);

        draggingImageView.setPreserveRatio(preserveRatio);
        draggingImageView.setVisible(setVisible);
    }

    // Create a semi-transparent hover image
    public void hoverImageViewSetProperties(int fitWidth, int fitHeight, boolean preserveRatio, boolean setVisible)
    {
        hoverImageView.setFitWidth(fitWidth);
        hoverImageView.setFitHeight(fitHeight);

        hoverImageView.setPreserveRatio(preserveRatio);
        hoverImageView.setVisible(setVisible);
        hoverImageView.setOpacity(0.5); // Semi-transparent
    }

    public void addToYard(AnchorPane root, GridPane yardGrid, Yard yard)
    {
        // Add card to the root pane
        root.getChildren().add(cardImageView);

        // Set mouse events for dragging & dropping once clicking on a card


        // Create a boolean array to track whether the card has been dropped
        boolean[] dropped = {false};

        // Once card is clicked
        cardImageView.setOnMousePressed(event -> {
            draggingImageView.setLayoutX(event.getSceneX() - 30);
            draggingImageView.setLayoutY(event.getSceneY() - 35);
            draggingImageView.setVisible(true);
            dropped[0] = false;
            root.getChildren().add(draggingImageView);


            hoverImageView.setVisible(false);    // Initially hidden
            if(!root.getChildren().contains(hoverImageView))
            {
                root.getChildren().add(hoverImageView);  // Ensure hoverImageView is added to the root
            }
            event.consume();
        });

        // Update dragging and hover behavior
        cardImageView.setOnMouseDragged(event -> {
            if (draggingImageView.isVisible()) {
                // Update dragging image position
                draggingImageView.setLayoutX(event.getSceneX() - 30);
                draggingImageView.setLayoutY(event.getSceneY() - 35);

                // Track the closest grid cell and update hover image position
                double closestDistance = Double.MAX_VALUE;
                Button closestButton = null;

                for (Node node : yardGrid.getChildren()) {
                    if (node instanceof Button button) {
                        // Get button bounds
                        Bounds buttonBounds = button.localToScene(button.getBoundsInLocal());

                        // Calculate distance to the center of the button
                        double centerX = buttonBounds.getMinX() + buttonBounds.getWidth() / 2;
                        double centerY = buttonBounds.getMinY() + buttonBounds.getHeight() / 2;
                        double distance = Math.hypot(centerX - event.getSceneX(), centerY - event.getSceneY());

                        // Check if this button is closer than the previously tracked one
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closestButton = button;
                        }
                    }
                }

                if (closestButton != null) {
                    // Get the row and column indices
                    int row = GridPane.getRowIndex(closestButton);
                    int col = GridPane.getColumnIndex(closestButton);

                    // Check if the cell is empty by verifying no plant exists at this cell
                    if (yard.isValidPosition(row, col)) {
                        // Get button bounds for positioning the hover image
                        Bounds buttonBounds = closestButton.localToScene(closestButton.getBoundsInLocal());

                        // Center the hover image on this button
                        hoverImageView.setLayoutX(buttonBounds.getMinX() + buttonBounds.getWidth() / 2 - hoverImageView.getFitWidth() / 2);
                        hoverImageView.setLayoutY(buttonBounds.getMinY() + buttonBounds.getHeight() / 2 - hoverImageView.getFitHeight() / 2);

                        // Ensure opacity is set for hover image
                        hoverImageView.setOpacity(0.5);  // Make sure opacity is consistent
                        hoverImageView.setVisible(true); // Show the hover image
                    } else {
                        // Hide hover image if the cell is occupied
                        hoverImageView.setVisible(false);
                    }
                } else {
                    // Hide hover image if no valid grid cell is nearby
                    hoverImageView.setVisible(false);
                }
            }
        });

        // Drop the plant when the mouse is released
        cardImageView.setOnMouseReleased(event -> {
            // Check if the drop is within any button
            for (Node node : yardGrid.getChildren())
            {
                if (node instanceof Button)
                {
                    Button button = (Button) node;

                    // Get button bounds on screen
                    Bounds buttonBounds = button.localToScene(button.getBoundsInLocal());

                    // Check if the drop point is within this button
                    if (buttonBounds.contains(event.getSceneX(), event.getSceneY()))
                    {
                        // Calculate the button's center position
                        double centerX = buttonBounds.getMinX() + buttonBounds.getWidth() / 2;
                        double centerY = buttonBounds.getMinY() + buttonBounds.getHeight() / 2;

                        // Hide & remove the draggingImageView
                        draggingImageView.setVisible(false);
                        root.getChildren().remove(draggingImageView);


                        if (plantType == null)
                        {
                            System.out.println("Shovel used at (" + GridPane.getRowIndex(button) + ", " + GridPane.getColumnIndex(button) + ")");


                            // Call a method to remove the plant and its image
                            yard.removePlant(root, GridPane.getRowIndex(button), GridPane.getColumnIndex(button));

                        }
                        else
                        {
                            // Instantiate the plant dynamically using reflection
                            try
                            {
                                // Create the plant instance
                                Plant plant = plantType.getDeclaredConstructor(int.class, int.class).newInstance((int) centerX, (int) centerY);

                                // Place the plant in the yard and display it
                                yard.placePlant(plant, root, GridPane.getRowIndex(button), GridPane.getColumnIndex(button));
                            } catch (Exception e) {
                                System.out.println("An exception occurred: " + e);
//                                System.exit(1);
                            }
                        }



                        // Hide and remove the hover image
                        hoverImageView.setVisible(false);
                        root.getChildren().remove(hoverImageView);



                        return; // Exit after placing the plant
                    }
                }
            }

            // If not dropped on a button, remove the dragging image and hide hover image
            draggingImageView.setVisible(false);
            root.getChildren().remove(draggingImageView);
            hoverImageView.setVisible(false); // Hide hover image if drop fails
            root.getChildren().remove(hoverImageView); // Remove hover image from scene

            event.consume();
        });
    }

    public ImageView getCardImageView()
    {
        return cardImageView;
    }

    public void setCardImageView(ImageView cardImageView)
    {
        this.cardImageView = cardImageView;
    }
}