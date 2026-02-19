package guiRole1;


/*******
 * <p> Title: ControllerRole1Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 1 Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page is a stub for establish future roles for the application.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 */

public class ControllerRole1Home {

	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	/**
	 * Default constructor is not used.
	 */
	public ControllerRole1Home() {
	}

	/**********
	 * <p> Method: performUpdate() </p>
	 * 
	 * <p> Description: This method directs the user to the User Update Page so the user can change
	 * the user account attributes. </p>
	 * 
	 */
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole1Home.theStage, ViewRole1Home.theUser);
	}	

	/**********
	 * <p> Method: performLogout() </p>
	 * 
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 * 
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole1Home.theStage);
	}
	
	/**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 * 
	 */	
	protected static void performQuit() {
		System.exit(0);
	}
	
	private static database.Database theDatabase = applicationMain.FoundationsMain.database;

	// Refreshes the ListView with the latest threads from the DB
	protected static void refreshThreads(javafx.scene.control.ListView<String> listView) {
		java.util.List<String> threads = theDatabase.getThreadList();
		listView.setItems(javafx.collections.FXCollections.observableArrayList(threads));
	}

	protected static void createThread(String title, String topic, javafx.scene.control.ListView<String> listView) {
		if (title.isEmpty() || topic.isEmpty()) {
			showAlert("Error", "Title and Topic cannot be empty.");
			return;
		}
		try {
			theDatabase.createThread(title, topic, ViewRole1Home.theUser.getUserName());
			refreshThreads(listView);
		} catch (Exception e) {
			showAlert("Database Error", e.getMessage());
		}
	}

	protected static void updateThread(String selection, String newTitle, String newTopic, javafx.scene.control.ListView<String> listView) {
		if (selection == null || newTitle.isEmpty() || newTopic.isEmpty()) {
			showAlert("Error", "Please select a thread and ensure Title/Topic are filled.");
			return;
		}
		try {
			int id = Integer.parseInt(selection.split("\\|")[0].trim());
			theDatabase.updateThread(id, newTitle, newTopic);
			refreshThreads(listView);
		} catch (Exception e) {
			showAlert("Database Error", e.getMessage());
		}
	}

	protected static void deleteThread(String selection, javafx.scene.control.ListView<String> listView) {
		if (selection == null) {
			showAlert("Error", "Please select a thread to delete.");
			return;
		}
		try {
			int id = Integer.parseInt(selection.split("\\|")[0].trim());
			theDatabase.deleteThread(id);
			refreshThreads(listView);
		} catch (Exception e) {
			showAlert("Database Error", e.getMessage());
		}
	}

	private static void showAlert(String title, String content) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
