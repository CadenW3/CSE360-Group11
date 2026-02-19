package guiRole1;

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
 * <p> Title: GUIReviewerHomePage Class. </p>
 * * <p> Description: The Java/FX-based Role1 Home Page.  The page is a stub for some role needed for
 * the application.  The widgets on this page are likely the minimum number and kind for other role
 * pages that may be needed.</p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * * @version 1.00		2025-08-20 Initial version
 * */

public class ViewRole1Home {
	
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
	private static ViewRole1Home theView;		// Used to determine if instantiation of the class
												// is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us	
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	

	private static Scene theViewRole1HomeScene;	// The shared Scene each invocation populates
	protected static final int theRole = 2;		// Admin: 1; Role1: 2; Role2: 3
	
	// UI Elements for Filtering
	protected static Button button_FilterMyPosts = new Button("My Posts (0)");
	protected static Button button_FilterUnread = new Button("Unread (0)");

	// GUI Area 2: Discussion Thread Management UI Elements
	protected static javafx.scene.control.TreeView<String> tree_Discussions = new javafx.scene.control.TreeView<>();
	protected static javafx.scene.control.ScrollPane scroll_ThreadDetails = new javafx.scene.control.ScrollPane();
	protected static javafx.scene.layout.VBox box_ThreadDetails = new javafx.scene.layout.VBox();
	
	protected static Label label_StudentSelect = new Label("Message Student:");
	protected static javafx.scene.control.ComboBox<String> dropdown_Students = new javafx.scene.control.ComboBox<>();
		
	protected static javafx.scene.control.TextField input_ReplyBox = new javafx.scene.control.TextField();
	protected static Button button_SubmitReply = new Button("Post / Send");

	// Create Thread/Question Elements
	protected static javafx.scene.control.TextField input_NewTitle = new javafx.scene.control.TextField();
	protected static javafx.scene.control.TextField input_NewTopic = new javafx.scene.control.TextField();
	protected static Button button_CreateThread = new Button("New Thread");
	protected static Button button_CreateQuestion = new Button("New Question");

	/*-*******************************************************************************************

	Constructors
	
	 */


	/**********
	 * <p> Method: displayRole1Home(Stage ps, User user) </p>
	 * * <p> Description: This method is the single entry point from outside this package to cause
	 * the Role1 Home page to be displayed.
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
	public static void displayRole1Home(Stage ps, User user) {
		
		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewRole1Home();		// Instantiate singleton if needed
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;
		
		label_UserDetails.setText("User: " + theUser.getUserName());
				
		// Reset filters on load
		ControllerRole1Home.filterMyPosts = false;
		ControllerRole1Home.filterUnread = false;
		button_FilterMyPosts.setStyle("");
		button_FilterUnread.setStyle("");
		ControllerRole1Home.refreshDiscussionTree(tree_Discussions, theUser.getUserName(), button_FilterMyPosts, button_FilterUnread);

		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("CSE 360 Foundations: Staff Home Page");
		theStage.setScene(theViewRole1HomeScene);
		theStage.show();
	}
	
	/**********
	 * <p> Method: ViewRole1Home() </p>
	 * * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object.</p>
	 * * This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayRole2Home method.</p>
	 * */
	private ViewRole1Home() {

		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theViewRole1HomeScene = new Scene(theRootPane, width, height);	// Create the scene
		
		// Set the title for the window
		
		// Populate the window with the title and other common widgets and set their static state
		
		// GUI Area 1
		label_PageTitle.setText("Staff Home Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		
		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((_) -> {ControllerRole1Home.performUpdate(); });
		
		// GUI Area 2
		// --- GUI Area 2: STAFF DISCUSSIONS ---

		// Filters
		setupButtonUI(button_FilterMyPosts, "Dialog", 12, 100, Pos.CENTER, 20, 105);
		setupButtonUI(button_FilterUnread, "Dialog", 12, 100, Pos.CENTER, 140, 105);

		button_FilterMyPosts.setOnAction((_) -> {
			ControllerRole1Home.filterMyPosts = !ControllerRole1Home.filterMyPosts;
			button_FilterMyPosts.setStyle(ControllerRole1Home.filterMyPosts ? "-fx-background-color: lightgreen;" : "");
			ControllerRole1Home.refreshDiscussionTree(tree_Discussions, theUser.getUserName(), button_FilterMyPosts, button_FilterUnread);
		});

		button_FilterUnread.setOnAction((_) -> {
			ControllerRole1Home.filterUnread = !ControllerRole1Home.filterUnread;
			button_FilterUnread.setStyle(ControllerRole1Home.filterUnread ? "-fx-background-color: lightblue;" : "");
			ControllerRole1Home.refreshDiscussionTree(tree_Discussions, theUser.getUserName(), button_FilterMyPosts, button_FilterUnread);
		});

		tree_Discussions.setLayoutX(20);
		tree_Discussions.setLayoutY(135);
		tree_Discussions.setPrefSize(350, 275); 

		setupLabelUI(label_StudentSelect, "Arial", 16, 150, Pos.BASELINE_LEFT, 20, 420);
		dropdown_Students.setLayoutX(150);
		dropdown_Students.setLayoutY(415);
		dropdown_Students.setPrefWidth(220);
		dropdown_Students.getItems().add("<Select a Student>");
		try {
			dropdown_Students.getItems().addAll(theDatabase.getStudentUsers());
		} catch (Exception e) { e.printStackTrace(); }
		dropdown_Students.getSelectionModel().select(0);

		// Thread Creation Box
		input_NewTitle.setLayoutX(20);
		input_NewTitle.setLayoutY(460);
		input_NewTitle.setPrefWidth(120);
		input_NewTitle.setPromptText("Title...");

		input_NewTopic.setLayoutX(150);
		input_NewTopic.setLayoutY(460);
		input_NewTopic.setPrefWidth(120);
		input_NewTopic.setPromptText("Topic...");
		
		setupButtonUI(button_CreateThread, "Dialog", 14, 90, Pos.CENTER, 280, 460);
		setupButtonUI(button_CreateQuestion, "Dialog", 14, 90, Pos.CENTER, 280, 495);

		// Right Side Feed
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
		input_ReplyBox.setPromptText("Type a reply here...");

		setupButtonUI(button_SubmitReply, "Dialog", 14, 80, Pos.CENTER, 700, 420);

		// INITIAL STATE: Hide the reply box so it doesn't show at launch
		input_ReplyBox.setVisible(false);
		button_SubmitReply.setVisible(false);

		// Listeners
				tree_Discussions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
					if (newVal != null) {
						javafx.application.Platform.runLater(() -> {
							dropdown_Students.getSelectionModel().select(0);
						});
						
						input_ReplyBox.setVisible(true);
						button_SubmitReply.setVisible(true);
						input_ReplyBox.setPromptText("Type a reply here...");
						
						javafx.scene.control.TreeItem<String> current = newVal;
						while (current != null && !current.getValue().startsWith("[Thread-") && !current.getValue().startsWith("[Question-")) {
							current = current.getParent();
						}
						if (current != null) {
							String val = current.getValue();
							String type = val.startsWith("[Thread-") ? "Discussion" : "Question";
							int id = Integer.parseInt(val.substring(val.indexOf("-") + 1, val.indexOf("]")));
							
							try {
								theDatabase.markPostAsRead(theUser.getUserName(), id, type);
							} catch (Exception e) { e.printStackTrace(); }

							// FIX: Remove the (UNREAD) tag visually without rebuilding the entire tree!
							if (newVal.getValue().contains("(UNREAD) ")) {
							    newVal.setValue(newVal.getValue().replace("(UNREAD) ", ""));
							}

							// We completely removed the ControllerRole1Home.refreshDiscussionTree(...) call from here.
							ControllerRole1Home.renderPostView(id, type, box_ThreadDetails);
						}
					}
				});

		dropdown_Students.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null && !newVal.equals("<Select a Student>")) {
				javafx.application.Platform.runLater(() -> {
					tree_Discussions.getSelectionModel().clearSelection();
				});
				
				input_ReplyBox.setVisible(true);
				button_SubmitReply.setVisible(true);
				input_ReplyBox.setPromptText("Message " + newVal + "...");
				ControllerRole1Home.renderDirectMessages(theUser.getUserName(), newVal, box_ThreadDetails);
			}
		});

		// Buttons
		button_CreateThread.setOnAction((_) -> {
			if (!input_NewTitle.getText().isEmpty() && !input_NewTopic.getText().isEmpty()) {
				ControllerRole1Home.createNewThread(input_NewTitle.getText(), input_NewTopic.getText(), theUser.getUserName(), tree_Discussions, button_FilterMyPosts, button_FilterUnread);
				input_NewTitle.clear();
				input_NewTopic.clear();
			}
		});

		button_CreateQuestion.setOnAction((_) -> {
			if (!input_NewTitle.getText().isEmpty() && !input_NewTopic.getText().isEmpty()) {
				ControllerRole1Home.createNewQuestion(input_NewTitle.getText(), input_NewTopic.getText(), theUser.getUserName(), tree_Discussions, button_FilterMyPosts, button_FilterUnread);
				input_NewTitle.clear();
				input_NewTopic.clear();
			}
		});

		button_SubmitReply.setOnAction((_) -> {
			String messageText = input_ReplyBox.getText();
			if (messageText.isEmpty()) return;

			String selectedStudent = dropdown_Students.getSelectionModel().getSelectedItem();
			if (selectedStudent != null && !selectedStudent.equals("<Select a Student>")) {
				ControllerRole1Home.executeDirectMessageDB(theUser.getUserName(), selectedStudent, messageText, box_ThreadDetails);
				input_ReplyBox.clear();
				return;
			}
			
			javafx.scene.control.TreeItem<String> selectedThread = tree_Discussions.getSelectionModel().getSelectedItem();
			if (selectedThread != null) {
				int id = 0, parentReplyId = 0;
				String val = selectedThread.getValue();
				javafx.scene.control.TreeItem<String> current = selectedThread;
				while (current != null && !current.getValue().startsWith("[Thread-") && !current.getValue().startsWith("[Question-")) { current = current.getParent(); }
				if (current != null) id = Integer.parseInt(current.getValue().substring(current.getValue().indexOf("-") + 1, current.getValue().indexOf("]")));
				
				if (val.startsWith("[Post-")) parentReplyId = Integer.parseInt(val.substring(6, val.indexOf("]")));
				else if (val.startsWith("[Reply-")) parentReplyId = Integer.parseInt(val.substring(7, val.indexOf("]")));
				
				if (id > 0) {
					String type = current.getValue().startsWith("[Thread-") ? "Discussion" : "Question";
					ControllerRole1Home.executeReplyDB(id, parentReplyId, type, messageText, theUser.getUserName(), tree_Discussions, box_ThreadDetails, button_FilterMyPosts, button_FilterUnread);
					input_ReplyBox.clear();
				}
			}
		});
		
		
		// GUI Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction((_) -> {ControllerRole1Home.performLogout(); });
        
        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((_) -> {ControllerRole1Home.performQuit(); });

		// This is the end of the GUI initialization code
		
		// Place all of the widget items into the Root Pane's list of children
		theRootPane.getChildren().clear(); // Fixes duplicate children crash
         theRootPane.getChildren().addAll(
 			label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
 			button_FilterMyPosts, button_FilterUnread, tree_Discussions, label_StudentSelect, dropdown_Students,
 			input_NewTitle, input_NewTopic, button_CreateThread, button_CreateQuestion,
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