package guiRole2;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
//import database.Database;
import entityClasses.User;


/*******
 * <p> Title: ViewRole2Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role2 Home Page.  The page is a stub for some role needed for
 * the application.  The widgets on this page are likely the minimum number and kind for other role
 * pages that may be needed.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-04-20 Initial version
 *  
 */

public class ViewRole2Home {
	
	/*-*******************************************************************************************

	Attributes
	
	 */
	
	// These are the application values required by the user interface
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;


	// These are the widget attributes for the GUI. There are 3 areas for this GUI.
	
	// GUI Area 1: It informs the user about the purpose of this page, whose account is being used,
	// and a button to allow this user to update the account settings
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Button button_UpdateThisUser = new Button("Account Update");
		
	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI ARea 2: This is a stub, so there are no widgets here.  For an actual role page, this are
	// would contain the widgets needed for the user to play the assigned role.
	
	
	
	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator4 = new Line(20, 525, width-20,525);
	
	// GUI Area 3: This is last of the GUI areas.  It is used for quitting the application and for
	// logging out.
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// This is the end of the GUI objects for the page.
	
	// These attributes are used to configure the page and populate it with this user's information
	private static ViewRole2Home theView;		// Used to determine if instantiation of the class
												// is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us	
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	
	private static Scene theRole2HomeScene;		// The shared Scene each invocation populates
	protected static final int theRole = 3;		// Admin: 1; Role1: 2; Role2: 3
	
	// GUI Area 2: Ed Discussion Style Layout
	protected static javafx.scene.control.TreeView<String> tree_Discussions = new javafx.scene.control.TreeView<>();
	protected static javafx.scene.control.TextArea text_SelectedDetails = new javafx.scene.control.TextArea();
	protected static javafx.scene.control.TextField input_ReplyBox = new javafx.scene.control.TextField();
	protected static Button button_SubmitReply = new Button("Post Reply");
		
	
	// Right Side UI Elements
	protected static javafx.scene.control.ScrollPane scroll_ThreadDetails = new javafx.scene.control.ScrollPane();
	protected static javafx.scene.layout.VBox box_ThreadDetails = new javafx.scene.layout.VBox();
	
	// Drop-down for Direct Messages
	protected static Label label_StaffSelect = new Label("Message Staff:");
	protected static javafx.scene.control.ComboBox<String> dropdown_Staff = new javafx.scene.control.ComboBox<>();

	/*-*******************************************************************************************

	Constructors
	
	 */

	/**********
	 * <p> Method: displayRole2Home(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Role2 Home page to be displayed.
	 * 
	 * It first sets up every shared attributes so we don't have to pass parameters.
	 * 
	 * It then checks to see if the page has been setup.  If not, it instantiates the class, 
	 * initializes all the static aspects of the GIUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 * 
	 * After the instantiation, the code then populates the elements that change based on the user
	 * and the system's current state.  It then sets the Scene onto the stage, and makes it visible
	 * to the user.
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 */
	public static void displayRole2Home(Stage ps, User user) {
		
		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewRole2Home();		// Instantiate singleton if needed
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;
		
		label_UserDetails.setText("User: " + theUser.getUserName());// Set the username

		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("CSE 360 Foundations: Student Home Page");
		theStage.setScene(theRole2HomeScene);						// Set this page onto the stage
		theStage.show();											// Display it to the user
	}
	
	/**********
	 * <p> Method: ViewRole2Home() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object. </p>
	 * 
	 * This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayRole2Home method.</p>
	 * 
	 */
	private ViewRole2Home() {
		
		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theRole2HomeScene = new Scene(theRootPane, width, height);	// Create the scene
		
		// Set the title for the window
		
		// Populate the window with the title and other common widgets and set their static state
		
		// GUI Area 1
		label_PageTitle.setText("Student Home Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		
		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((_) -> {ControllerRole2Home.performUpdate(); });
		
		// GUI Area 2
		
		// --- GUI Area 2: ED DISCUSSION LAYOUT ---
		
				// Left Side: File-manager style drop down tree
		// Left Side: Discussions Tree
				tree_Discussions.setLayoutX(20);
				tree_Discussions.setLayoutY(110);
				tree_Discussions.setPrefSize(350, 330); // Made slightly shorter to fit dropdown
				ControllerRole2Home.refreshDiscussionTree(tree_Discussions);

				// Left Side: Staff Drop-Down Menu
				setupLabelUI(label_StaffSelect, "Arial", 16, 150, Pos.BASELINE_LEFT, 20, 455);
				
				dropdown_Staff.setLayoutX(140);
				dropdown_Staff.setLayoutY(450);
				dropdown_Staff.setPrefWidth(230);
				dropdown_Staff.getItems().add("<Select Staff Member>");
				dropdown_Staff.getItems().addAll(theDatabase.getStaffUsers());
				dropdown_Staff.getSelectionModel().select(0);

				// Selection Listener 1: When user clicks a Discussion Thread
				tree_Discussions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
					if (newVal != null) {
						dropdown_Staff.getSelectionModel().select(0); // Clear Staff selection
						input_ReplyBox.setPromptText("Type a reply here...");
						
						javafx.scene.control.TreeItem<String> current = newVal;
						while (current != null && !current.getValue().startsWith("[Thread-")) {
							current = current.getParent();
						}
						if (current != null) {
							int threadId = Integer.parseInt(current.getValue().substring(8, current.getValue().indexOf("]")));
							ControllerRole2Home.renderThreadView(threadId, box_ThreadDetails);
						}
					}
				});

				// Selection Listener 2: When user selects a Staff member from the Drop-Down
				dropdown_Staff.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
					if (newVal != null && !newVal.equals("<Select Staff Member>")) {
						tree_Discussions.getSelectionModel().clearSelection(); // Clear Thread selection
						input_ReplyBox.setPromptText("Message " + newVal + "...");
						
						// Render WhatsApp View
						ControllerRole2Home.renderDirectMessages(theUser.getUserName(), newVal, box_ThreadDetails);
					}
				});

				// Submit Button Logic
				button_SubmitReply.setOnAction((_) -> {
					String messageText = input_ReplyBox.getText();
					if (messageText.isEmpty()) return;

					String selectedStaff = dropdown_Staff.getSelectionModel().getSelectedItem();
					
					// 1. Check if we are sending a Direct Message
					if (selectedStaff != null && !selectedStaff.equals("<Select Staff Member>")) {
						ControllerRole2Home.executeDirectMessageDB(theUser.getUserName(), selectedStaff, messageText, box_ThreadDetails);
						input_ReplyBox.clear();
						return;
					}
					
					// 2. Otherwise, we are replying to a Thread
					javafx.scene.control.TreeItem<String> selectedThread = tree_Discussions.getSelectionModel().getSelectedItem();
					if (selectedThread != null) {
						String val = selectedThread.getValue();
						int threadId = 0, parentReplyId = 0;
						
						javafx.scene.control.TreeItem<String> current = selectedThread;
						while (current != null && !current.getValue().startsWith("[Thread-")) {
							current = current.getParent();
						}
						if (current != null) threadId = Integer.parseInt(current.getValue().substring(8, current.getValue().indexOf("]")));
						if (val.startsWith("[Reply-")) parentReplyId = Integer.parseInt(val.substring(7, val.indexOf("]")));
						
						if (threadId > 0) {
							ControllerRole2Home.executeReplyDB(threadId, parentReplyId, messageText, theUser.getUserName(), tree_Discussions, box_ThreadDetails);
							input_ReplyBox.clear();
						}
					}
				});

				// Right Side: Professional Content viewer
				box_ThreadDetails.setStyle("-fx-background-color: white;");
				box_ThreadDetails.setPrefWidth(370); // Slightly smaller than ScrollPane to prevent horizontal scroll
				
				scroll_ThreadDetails.setContent(box_ThreadDetails);
				scroll_ThreadDetails.setFitToWidth(true); // Expands VBox to fit the pane
				scroll_ThreadDetails.setLayoutX(390);
				scroll_ThreadDetails.setLayoutY(110);
				scroll_ThreadDetails.setPrefSize(390, 200);
				scroll_ThreadDetails.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-background-insets: 0;");

				// Right Side: Reply box
				input_ReplyBox.setLayoutX(390);
				input_ReplyBox.setLayoutY(320);
				input_ReplyBox.setPrefSize(300, 30);
				input_ReplyBox.setPromptText("Type a reply here...");

				setupButtonUI(button_SubmitReply, "Dialog", 14, 80, Pos.CENTER, 700, 320);

				// Selection Listener: When they click ANY thread or reply on the left
				tree_Discussions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
					if (newVal != null) {
						// 1. Find the ROOT thread ID by walking up the tree structure
						javafx.scene.control.TreeItem<String> current = newVal;
						while (current != null && !current.getValue().startsWith("[Thread-")) {
							current = current.getParent();
						}
						
						// 2. Render that entire thread on the right side
						if (current != null) {
							int threadId = Integer.parseInt(current.getValue().substring(8, current.getValue().indexOf("]")));
							ControllerRole2Home.renderThreadView(threadId, box_ThreadDetails);
						}
					}
				});

				// Selection Listener: Determine if clicking a Thread or a Staff member
				tree_Discussions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
					if (newVal != null) {
						String val = newVal.getValue();
						
						if (val.startsWith("[Staff] ")) {
							// Load Direct Messages
							String staffName = val.substring(8).trim();
							ControllerRole2Home.renderDirectMessages(theUser.getUserName(), staffName, box_ThreadDetails);
							input_ReplyBox.setPromptText("Message " + staffName + "...");
						} else {
							// Find ROOT thread ID and load Thread
							input_ReplyBox.setPromptText("Type a reply here...");
							javafx.scene.control.TreeItem<String> current = newVal;
							while (current != null && !current.getValue().startsWith("[Thread-")) {
								current = current.getParent();
							}
							if (current != null) {
								int threadId = Integer.parseInt(current.getValue().substring(8, current.getValue().indexOf("]")));
								ControllerRole2Home.renderThreadView(threadId, box_ThreadDetails);
							}
						}
					}
				});

				// Submit Button: Automatically routes to Thread Reply OR Direct Message
				button_SubmitReply.setOnAction((_) -> {
					javafx.scene.control.TreeItem<String> selected = tree_Discussions.getSelectionModel().getSelectedItem();
					if (selected == null || input_ReplyBox.getText().isEmpty()) return;
					String val = selected.getValue();
					
					if (val.startsWith("[Staff] ")) {
						// Send Direct Message
						String staffName = val.substring(8).trim();
						ControllerRole2Home.executeDirectMessageDB(theUser.getUserName(), staffName, input_ReplyBox.getText(), box_ThreadDetails);
						input_ReplyBox.clear();
						return;
					}
					
					// Send Thread Reply
					int threadId = 0;
					int parentReplyId = 0;
					
					javafx.scene.control.TreeItem<String> current = selected;
					while (current != null && !current.getValue().startsWith("[Thread-")) {
						current = current.getParent();
					}
					if (current != null) {
						threadId = Integer.parseInt(current.getValue().substring(8, current.getValue().indexOf("]")));
					}

					if (val.startsWith("[Reply-")) {
						parentReplyId = Integer.parseInt(val.substring(7, val.indexOf("]")));
					}
					
					if (threadId > 0) {
						// We pass box_ThreadDetails so the feed updates instantly!
						ControllerRole2Home.executeReplyDB(threadId, parentReplyId, input_ReplyBox.getText(), theUser.getUserName(), tree_Discussions, box_ThreadDetails);
						input_ReplyBox.clear();
					}
				});
		
		
		// GUI Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction((_) -> {ControllerRole2Home.performLogout(); });
        
        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((_) -> {ControllerRole2Home.performQuit(); });

		// This is the end of the GUI initialization code
		
		// Place all of the widget items into the Root Pane's list of children
        theRootPane.getChildren().addAll(
    			label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
    			tree_Discussions, label_StaffSelect, dropdown_Staff, 
    			scroll_ThreadDetails, input_ReplyBox, button_SubmitReply,
    	        line_Separator4, button_Logout, button_Quit);
	}
	
	
	/*-********************************************************************************************

	Helper methods to reduce code length

	 */
	
	/**********
	 * Private local method to initialize the standard fields for a label
	 * 
	 * @param l		The Label object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, 
			double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	
	/**********
	 * Private local method to initialize the standard fields for a button
	 * 
	 * @param b		The Button object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, 
			double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}
}
