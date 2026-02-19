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
 * * <p> Description: The Java/FX-based Role2 Home Page.  The page is a stub for some role needed for
 * the application.  The widgets on this page are likely the minimum number and kind for other role
 * pages that may be needed.</p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * * @version 1.00		2025-04-20 Initial version
 * */

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
	protected static Button button_FilterMyPosts = new Button("My Posts (0)");
	protected static Button button_FilterUnread = new Button("Unread (0)");
	
	protected static javafx.scene.control.TreeView<String> tree_Discussions = new javafx.scene.control.TreeView<>();
	protected static javafx.scene.control.TextArea text_SelectedDetails = new javafx.scene.control.TextArea();
	protected static javafx.scene.control.TextField input_ReplyBox = new javafx.scene.control.TextField();
	protected static Button button_SubmitReply = new Button("Post Reply");
	protected static javafx.scene.control.TextField input_Filter = new javafx.scene.control.TextField();
	
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
	 * * <p> Description: This method is the single entry point from outside this package to cause
	 * the Role2 Home page to be displayed.
	 * * It first sets up every shared attributes so we don't have to pass parameters.
	 * * It then checks to see if the page has been setup.  If not, it instantiates the class, 
	 * initializes all the static aspects of the GIUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 * * After the instantiation, the code then populates the elements that change based on the user
	 * and the system's current state.  It then sets the Scene onto the stage, and makes it visible
	 * to the user.
	 * * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * * @param user specifies the User for this GUI and it's methods
	 * */
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
	 * * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object. </p>
	 * * This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayRole2Home method.</p>
	 * */
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
		
		// --- GUI Area 2: ED DISCUSSION LAYOUT ---
		
		setupButtonUI(button_FilterMyPosts, "Dialog", 12, 100, Pos.CENTER, 20, 105);
		setupButtonUI(button_FilterUnread, "Dialog", 12, 100, Pos.CENTER, 140, 105);

		// Search Filter Input Box
		input_Filter.setLayoutX(250);
		input_Filter.setLayoutY(105);
		input_Filter.setPrefWidth(120);
		input_Filter.setVisible(false);
		input_Filter.textProperty().addListener((obs, oldVal, newVal) -> {
			ControllerRole2Home.currentFilterKeyword = newVal;
			ControllerRole2Home.refreshDiscussionTree(tree_Discussions, theUser.getUserName(), button_FilterMyPosts, button_FilterUnread);
		});

		// Left Side: Discussions Tree
		tree_Discussions.setLayoutX(20);
		tree_Discussions.setLayoutY(135);
		tree_Discussions.setPrefSize(350, 275); 
		ControllerRole2Home.refreshDiscussionTree(tree_Discussions, theUser.getUserName(), button_FilterMyPosts, button_FilterUnread);

		// Left Side: Staff Drop-Down Menu
		setupLabelUI(label_StaffSelect, "Arial", 16, 150, Pos.BASELINE_LEFT, 20, 420);
		
		dropdown_Staff.setLayoutX(140);
		dropdown_Staff.setLayoutY(415);
		dropdown_Staff.setPrefWidth(230);
		dropdown_Staff.getItems().add("<Select Staff Member>");
		try {
			dropdown_Staff.getItems().addAll(theDatabase.getStaffUsers());
		} catch (Exception e) {}
		dropdown_Staff.getSelectionModel().select(0);

		// Right Side: Professional Content viewer
		box_ThreadDetails.setStyle("-fx-background-color: white;");
		box_ThreadDetails.setPrefWidth(370); 
		
		scroll_ThreadDetails.setContent(box_ThreadDetails);
		scroll_ThreadDetails.setFitToWidth(true); 
		scroll_ThreadDetails.setLayoutX(390);
		scroll_ThreadDetails.setLayoutY(135);
		scroll_ThreadDetails.setPrefSize(390, 275);
		scroll_ThreadDetails.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-background-insets: 0;");

		// Right Side: Reply box
		input_ReplyBox.setLayoutX(390);
		input_ReplyBox.setLayoutY(420);
		input_ReplyBox.setPrefSize(300, 30);
		input_ReplyBox.setPromptText("Type a reply here...");

		setupButtonUI(button_SubmitReply, "Dialog", 14, 80, Pos.CENTER, 700, 420);

		// INITIAL STATE
		input_ReplyBox.setVisible(false);
		button_SubmitReply.setVisible(false);

		// Selection Listener 1: Consolidated listener for ANY click in the tree
		tree_Discussions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				javafx.application.Platform.runLater(() -> {
					dropdown_Staff.getSelectionModel().select(0); 
				});
				
				String val = newVal.getValue();
				
				// Fix: Visually clear (UNREAD) without rebuilding the tree and crashing
				if (val.contains("(UNREAD) ")) {
					newVal.setValue(val.replace("(UNREAD) ", ""));
					val = newVal.getValue(); // Keep string logic accurate below
				}

				// Check if they selected a root category to filter
				if (val.equals("Discussions") || val.equals("Questions")) {
					input_Filter.setVisible(true);
					input_Filter.setPromptText("Filter " + val);
					ControllerRole2Home.currentFilterType = val.equals("Discussions") ? "Discussion" : "Question";
				}
				
				input_ReplyBox.setVisible(true);
				button_SubmitReply.setVisible(true);
				input_ReplyBox.setPromptText("Type a reply here...");
				
				javafx.scene.control.TreeItem<String> current = newVal;
				while (current != null && !current.getValue().startsWith("[Thread-") && !current.getValue().startsWith("[Question-")) {
					current = current.getParent();
				}
				if (current != null) {
					String type = current.getValue().startsWith("[Thread-") ? "Discussion" : "Question";
					int id = Integer.parseInt(current.getValue().substring(current.getValue().indexOf("-") + 1, current.getValue().indexOf("]")));
					ControllerRole2Home.renderPostView(id, type, box_ThreadDetails);
				}
			}
		});

		// Selection Listener 2: Dropdown for Staff Messaging
		dropdown_Staff.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null && !newVal.equals("<Select Staff Member>")) {
				javafx.application.Platform.runLater(() -> {
					tree_Discussions.getSelectionModel().clearSelection(); 
				});
				
				input_ReplyBox.setVisible(true);
				button_SubmitReply.setVisible(true);
				input_ReplyBox.setPromptText("Message " + newVal + "...");
				ControllerRole2Home.renderDirectMessages(theUser.getUserName(), newVal, box_ThreadDetails);
			}
		});

		// Submit Button Logic (Consolidated single event)
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
			
			// 2. Otherwise, we are replying to a Thread/Question
			javafx.scene.control.TreeItem<String> selectedThread = tree_Discussions.getSelectionModel().getSelectedItem();
			if (selectedThread != null) {
				String val = selectedThread.getValue();
				int id = 0, parentReplyId = 0;
				
				javafx.scene.control.TreeItem<String> current = selectedThread;
				while (current != null && !current.getValue().startsWith("[Thread-") && !current.getValue().startsWith("[Question-")) {
					current = current.getParent();
				}
				if (current != null) {
					id = Integer.parseInt(current.getValue().substring(current.getValue().indexOf("-") + 1, current.getValue().indexOf("]")));
				}
				
				if (val.startsWith("[Reply-")) parentReplyId = Integer.parseInt(val.substring(7, val.indexOf("]")));
				else if (val.startsWith("[Post-")) parentReplyId = Integer.parseInt(val.substring(6, val.indexOf("]")));
				
				if (id > 0) {
					String type = current.getValue().startsWith("[Thread-") ? "Discussion" : "Question";
					ControllerRole2Home.executeReplyDB(id, parentReplyId, type, input_ReplyBox.getText(), theUser.getUserName(), tree_Discussions, box_ThreadDetails, button_FilterMyPosts, button_FilterUnread);
					input_ReplyBox.clear();
				}
			}
		});
		
		// GUI Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction((_) -> {ControllerRole2Home.performLogout(); });
        
        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((_) -> {ControllerRole2Home.performQuit(); });

		// This is the end of the GUI initialization code
		
		// Place all of the widget items into the Root Pane's list of children
		theRootPane.getChildren().clear(); // Prevent duplicate rendering 
        theRootPane.getChildren().addAll(
    			label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
    			button_FilterMyPosts, button_FilterUnread, input_Filter,
    			tree_Discussions, label_StaffSelect, dropdown_Staff, 
    			scroll_ThreadDetails, input_ReplyBox, button_SubmitReply,
    	        line_Separator4, button_Logout, button_Quit);
	}
	
	
	/*-********************************************************************************************

	Helper methods to reduce code length

	 */
	
	/**********
	 * Private local method to initialize the standard fields for a label
	 * * @param l		The Label object to be initialized
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
	 * * @param b		The Button object to be initialized
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