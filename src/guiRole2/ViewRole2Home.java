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
	
	// Mode Toggle Buttons
	protected static Button button_DiscussionsMode = new Button("Discussions");
	protected static Button button_MessagesMode = new Button("Messages");
	
	// GUI Area 2: Ed Discussion Style Layout
	protected static Button button_FilterMyPosts = new Button("My Posts (0)");
	protected static Button button_FilterUnread = new Button("Unread (0)");
	protected static javafx.scene.control.TextField input_Filter = new javafx.scene.control.TextField();

	protected static javafx.scene.control.TreeView<String> tree_Discussions = new javafx.scene.control.TreeView<>();
	protected static javafx.scene.control.TreeView<String> tree_Messages = new javafx.scene.control.TreeView<>();
	
	protected static javafx.scene.control.TextArea text_SelectedDetails = new javafx.scene.control.TextArea();
	protected static javafx.scene.control.TextField input_ReplyBox = new javafx.scene.control.TextField();
	protected static Button button_SubmitReply = new Button("Post Reply");
	
	// Right Side UI Elements
	protected static javafx.scene.control.ScrollPane scroll_ThreadDetails = new javafx.scene.control.ScrollPane();
	protected static javafx.scene.layout.VBox box_ThreadDetails = new javafx.scene.layout.VBox();
	
	protected static javafx.scene.control.TextField input_NewTitle = new javafx.scene.control.TextField();
	protected static javafx.scene.control.TextField input_NewTopic = new javafx.scene.control.TextField();
	protected static Button button_CreateQuestion = new Button("New Question");

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

		// SECURE CACHE CLEAR: Always blank out the details box upon entry so no grades leak from Staff view
		box_ThreadDetails.getChildren().clear();

		ControllerRole2Home.filterMyPosts = false;
		ControllerRole2Home.filterUnread = false;
		button_FilterMyPosts.setStyle("");
		button_FilterUnread.setStyle("");
		ControllerRole2Home.refreshDiscussionTree(tree_Discussions, theUser.getUserName(), button_FilterMyPosts, button_FilterUnread);

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

		// Mode Buttons Setup
		setupButtonUI(button_DiscussionsMode, "Dialog", 14, 140, Pos.CENTER, 20, 105);
		setupButtonUI(button_MessagesMode, "Dialog", 14, 140, Pos.CENTER, 170, 105);
		
		setupButtonUI(button_FilterMyPosts, "Dialog", 12, 100, Pos.CENTER, 20, 145);
		setupButtonUI(button_FilterUnread, "Dialog", 12, 100, Pos.CENTER, 130, 145);

		input_Filter.setLayoutX(240);
		input_Filter.setLayoutY(145);
		input_Filter.setPrefWidth(130);
		input_Filter.setVisible(false);
		input_Filter.textProperty().addListener((obs, oldVal, newVal) -> {
			ControllerRole2Home.currentFilterKeyword = newVal;
			ControllerRole2Home.refreshDiscussionTree(tree_Discussions, theUser.getUserName(), button_FilterMyPosts, button_FilterUnread);
		});

		button_FilterMyPosts.setOnAction((_) -> {
			ControllerRole2Home.filterMyPosts = !ControllerRole2Home.filterMyPosts;
			button_FilterMyPosts.setStyle(ControllerRole2Home.filterMyPosts ? "-fx-background-color: lightgreen;" : "");
			ControllerRole2Home.refreshDiscussionTree(tree_Discussions, theUser.getUserName(), button_FilterMyPosts, button_FilterUnread);
		});

		button_FilterUnread.setOnAction((_) -> {
			ControllerRole2Home.filterUnread = !ControllerRole2Home.filterUnread;
			button_FilterUnread.setStyle(ControllerRole2Home.filterUnread ? "-fx-background-color: lightblue;" : "");
			ControllerRole2Home.refreshDiscussionTree(tree_Discussions, theUser.getUserName(), button_FilterMyPosts, button_FilterUnread);
		});

		tree_Discussions.setLayoutX(20);
		tree_Discussions.setLayoutY(180);
		tree_Discussions.setPrefSize(350, 270); 

		tree_Messages.setLayoutX(20);
		tree_Messages.setLayoutY(145); 
		tree_Messages.setPrefSize(350, 305); 
		tree_Messages.setVisible(false);

		input_NewTitle.setLayoutX(20); input_NewTitle.setLayoutY(460); input_NewTitle.setPrefWidth(120); input_NewTitle.setPromptText("Title...");
		input_NewTopic.setLayoutX(150); input_NewTopic.setLayoutY(460); input_NewTopic.setPrefWidth(120); input_NewTopic.setPromptText("Topic...");
		setupButtonUI(button_CreateQuestion, "Dialog", 14, 90, Pos.CENTER, 280, 460);

		box_ThreadDetails.setStyle("-fx-background-color: white;");
		box_ThreadDetails.setPrefWidth(370); 
		scroll_ThreadDetails.setContent(box_ThreadDetails);
		scroll_ThreadDetails.setFitToWidth(true); 
		scroll_ThreadDetails.setLayoutX(390);
		scroll_ThreadDetails.setLayoutY(135);
		scroll_ThreadDetails.setPrefSize(390, 275);
		scroll_ThreadDetails.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-background-insets: 0;");

		input_ReplyBox.setLayoutX(390);
		input_ReplyBox.setLayoutY(420);
		input_ReplyBox.setPrefSize(300, 30);
		input_ReplyBox.setPromptText("Type a reply (Defaults to General)...");

		setupButtonUI(button_SubmitReply, "Dialog", 14, 80, Pos.CENTER, 700, 420);

		input_ReplyBox.setVisible(true);
		button_SubmitReply.setVisible(true);

		// Mode Switching Actions
		button_DiscussionsMode.setOnAction((_) -> {
		    tree_Messages.setVisible(false);
		    tree_Discussions.setVisible(true);
		    button_FilterMyPosts.setVisible(true);
		    button_FilterUnread.setVisible(true);
		    input_NewTitle.setVisible(true);
		    input_NewTopic.setVisible(true);
		    button_CreateQuestion.setVisible(true);
		    
		    String val = "";
		    if(tree_Discussions.getSelectionModel().getSelectedItem() != null) val = tree_Discussions.getSelectionModel().getSelectedItem().getValue();
		    if (val.equals("Discussions") || val.equals("Questions")) input_Filter.setVisible(true);
		    
		    box_ThreadDetails.getChildren().clear();
		    input_ReplyBox.setVisible(true);
		    button_SubmitReply.setVisible(true);
		    input_ReplyBox.setPromptText("Type a reply (Defaults to General)...");
		});

		button_MessagesMode.setOnAction((_) -> {
		    tree_Discussions.setVisible(false);
		    button_FilterMyPosts.setVisible(false);
		    button_FilterUnread.setVisible(false);
		    input_Filter.setVisible(false);
		    input_NewTitle.setVisible(false);
		    input_NewTopic.setVisible(false);
		    button_CreateQuestion.setVisible(false);
		    
		    javafx.scene.control.TreeItem<String> rootMessages = new javafx.scene.control.TreeItem<>("Hidden");
		    javafx.scene.control.TreeItem<String> staffNode = new javafx.scene.control.TreeItem<>("Staff");
		    
		    try {
		        java.util.List<String> staff = theDatabase.getStaffUsers();
		        staff.remove(theUser.getUserName());
		        for (String s : staff) staffNode.getChildren().add(new javafx.scene.control.TreeItem<>(s));
		    } catch (Exception e) {}
		    
		    rootMessages.getChildren().add(staffNode);
		    tree_Messages.setRoot(rootMessages);
		    tree_Messages.setShowRoot(false);
		    
		    tree_Messages.setVisible(true);
		    box_ThreadDetails.getChildren().clear();
		    input_ReplyBox.setVisible(false);
		    button_SubmitReply.setVisible(false);
		});

		// Listeners
		tree_Messages.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
		    if (newVal != null && newVal.isLeaf()) {
		        String target = newVal.getValue();
		        input_ReplyBox.setVisible(true);
		        button_SubmitReply.setVisible(true);
		        input_ReplyBox.setPromptText("Message " + target + "...");
		        ControllerRole2Home.renderDirectMessages(theUser.getUserName(), target, box_ThreadDetails);
		    } else {
		        input_ReplyBox.setVisible(false);
		        button_SubmitReply.setVisible(false);
		    }
		});

		tree_Discussions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				String val = newVal.getValue();
				
				if (val.contains("(UNREAD) ")) {
					newVal.setValue(val.replace("(UNREAD) ", ""));
					val = newVal.getValue(); 
				}

				if (val.equals("Discussions") || val.equals("Questions")) {
					input_Filter.setVisible(true);
					input_Filter.setPromptText("Filter " + val);
					ControllerRole2Home.currentFilterType = val.equals("Discussions") ? "Discussion" : "Question";
					box_ThreadDetails.getChildren().clear(); // SECURE: Ensure it clears if clicking a root node
				} else {
					input_Filter.setVisible(false);
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
				} else {
					box_ThreadDetails.getChildren().clear(); // SECURE: Fallback clear
				}
			}
		});

		// Buttons

		button_CreateQuestion.setOnAction((_) -> {
			String title = input_NewTitle.getText().isEmpty() ? "General" : input_NewTitle.getText();
			if (!input_NewTopic.getText().isEmpty()) {
				ControllerRole2Home.createNewQuestion(title, input_NewTopic.getText(), theUser.getUserName(), tree_Discussions, button_FilterMyPosts, button_FilterUnread);
				input_NewTitle.clear();
				input_NewTopic.clear();
			}
		});

		button_SubmitReply.setOnAction((_) -> {
			String messageText = input_ReplyBox.getText();
			if (messageText.isEmpty()) return;

			if (tree_Messages.isVisible()) {
				javafx.scene.control.TreeItem<String> target = tree_Messages.getSelectionModel().getSelectedItem();
				if (target != null && target.isLeaf()) {
					ControllerRole2Home.executeDirectMessageDB(theUser.getUserName(), target.getValue(), messageText, box_ThreadDetails);
					input_ReplyBox.clear();
				}
				return;
			}
			
			javafx.scene.control.TreeItem<String> selectedThread = tree_Discussions.getSelectionModel().getSelectedItem();
			int id = 0, parentReplyId = 0;
			String type = "Discussion";
			
			if (selectedThread != null && !selectedThread.getValue().equals("Discussions") && !selectedThread.getValue().equals("Questions")) {
				String val = selectedThread.getValue();
				javafx.scene.control.TreeItem<String> current = selectedThread;
				while (current != null && !current.getValue().startsWith("[Thread-") && !current.getValue().startsWith("[Question-")) { current = current.getParent(); }
				if (current != null) id = Integer.parseInt(current.getValue().substring(current.getValue().indexOf("-") + 1, current.getValue().indexOf("]")));
				
				if (val.startsWith("[Post-")) parentReplyId = Integer.parseInt(val.substring(6, val.indexOf("]")));
				else if (val.startsWith("[Reply-")) parentReplyId = Integer.parseInt(val.substring(7, val.indexOf("]")));
				
				if (current != null) type = current.getValue().startsWith("[Thread-") ? "Discussion" : "Question";
			} else {
				id = theDatabase.getOrCreateGeneralThread();
			}
			
			if (id > 0) {
				ControllerRole2Home.executeReplyDB(id, parentReplyId, type, messageText, theUser.getUserName(), tree_Discussions, box_ThreadDetails, button_FilterMyPosts, button_FilterUnread);
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
		theRootPane.getChildren().clear(); // Prevent duplicate rendering 
        theRootPane.getChildren().addAll(
    			label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
    			button_DiscussionsMode, button_MessagesMode, button_FilterMyPosts, button_FilterUnread, input_Filter,
    			tree_Discussions, tree_Messages, 
    			input_NewTitle, input_NewTopic, button_CreateQuestion,
    			scroll_ThreadDetails, input_ReplyBox, button_SubmitReply,
    	        line_Separator4, button_Logout, button_Quit);

		// --- DYNAMIC UI SCALING ---
		theRootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
			double w = newVal.doubleValue();
			line_Separator1.setEndX(w - 20);
			line_Separator4.setEndX(w - 20);
			scroll_ThreadDetails.setPrefWidth(w - 410);
			box_ThreadDetails.setPrefWidth(w - 430);
			button_SubmitReply.setLayoutX(w - 100);
			input_ReplyBox.setPrefWidth(w - 500);
			button_UpdateThisUser.setLayoutX(w - 190);
			button_Quit.setLayoutX(w - 280); 
		});

		theRootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
			double h = newVal.doubleValue();
			line_Separator4.setStartY(h - 75);
			line_Separator4.setEndY(h - 75);
			button_Logout.setLayoutY(h - 60);
			button_Quit.setLayoutY(h - 60);
			
			tree_Discussions.setPrefHeight(h - 330);
			tree_Messages.setPrefHeight(h - 295);
			scroll_ThreadDetails.setPrefHeight(h - 325);
			
			input_ReplyBox.setLayoutY(h - 180);
			button_SubmitReply.setLayoutY(h - 180);
			
			input_NewTitle.setLayoutY(h - 140);
			input_NewTopic.setLayoutY(h - 140);
			button_CreateQuestion.setLayoutY(h - 140);
		});
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