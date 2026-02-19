package guiUserLogin;

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;

/*******
 * <p> Title: ControllerUserLogin Class. </p>
 * * <p> Description: The Java/FX-based User Login Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * * This controller determines if the log in is valid.  If so set up the link to the database, 
 * determines how many roles this user is authorized to play, and the calls one the of the array of
 * role home pages if there is only one role.  If there are more than one role, it setup up and
 * calls the multiple roles dispatch page for the user to determine which role the user wants to
 * play.
 * * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation * */

public class ControllerUserLogin {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be used to gather data to be passed to the Controller.

	*/
	
	// FIX: Directly link to the main database to prevent NullPointerException
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	private static Stage theStage;			// This is the stage that is used to display the scene
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerUserLogin() {
	}

	/**********
	 * <p> Method: setDatabase(Database db) </p>
	 * * <p> Description: This method is used to pass the database object to this controller so it
	 * can be used to access the database.</p>
	 * * @param db	The Database object
	 */
	public static void setDatabase(Database db) {
		theDatabase = db;
	}

	/**********
	 * <p> Method: doLogin(Stage ts) </p>
	 * * <p> Description: This method is called when the user has filled in the username and password
	 * fields and clicked on the "Login" button.  It validates the input, checks the database, and
	 * then dispatches the user to the appropriate home page.</p>
	 * * @param ts	The Stage to be used for the next page
	 */
	protected static void doLogin(Stage ts) {
		theStage = ts;
		String username = ViewUserLogin.text_Username.getText();
		String password = ViewUserLogin.text_Password.getText();
    	boolean loginResult = false;
    	
    	// Validate the username format before checking the database.
    	String usernameError = userNameRecognizerTestbed.UserNameRecognizer.checkForValidUserName(username);
    	
    	if (!usernameError.isEmpty()) {
    	    ViewUserLogin.alertUsernamePasswordError.setContentText(usernameError);
    	    ViewUserLogin.alertUsernamePasswordError.showAndWait();
    	    return;
    	}

    	// Double check database connection is alive
    	if (theDatabase == null) {
    		theDatabase = applicationMain.FoundationsMain.database;
    	}

		// Fetch the user and verify the username
     	if (theDatabase.getUserAccountDetails(username) == false) {
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
		
		// Check to see that the login password matches the account password
    	String actualPassword = theDatabase.getCurrentPassword();
    	
    	if (password.compareTo(actualPassword) != 0) {
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
		
		// Establish this user's details
    	User user = new User(username, password, theDatabase.getCurrentFirstName(), 
    			theDatabase.getCurrentMiddleName(), theDatabase.getCurrentLastName(), 
    			theDatabase.getCurrentPreferredFirstName(), theDatabase.getCurrentEmailAddress(), 
    			theDatabase.getCurrentAdminRole(), 
    			theDatabase.getCurrentNewRole1(), theDatabase.getCurrentNewRole2());
    	
    	// Force Admin to go directly to Admin Home (Bypassing Role Selection if needed)
    	if (user.getAdminRole()) {
			loginResult = theDatabase.loginAdmin(user);
			if (loginResult) {
				guiAdminHome.ViewAdminHome.displayAdminHome(theStage, user);
				return;
			}
    	}
    	
    	// See which home page dispatch to use for non-admin or multi-role logic
		int numberOfRoles = theDatabase.getNumberOfRoles(user);		
		
		if (numberOfRoles == 1) {
			// Single Account Home Page
			if (user.getNewRole1()) {
				loginResult = theDatabase.loginRole1(user);
				if (loginResult) {
					guiRole1.ViewRole1Home.displayRole1Home(theStage, user);
				}
			} else if (user.getNewRole2()) {
				loginResult = theDatabase.loginRole2(user);
				if (loginResult) {
					guiRole2.ViewRole2Home.displayRole2Home(theStage, user);
				}
			} else {
				System.out.println("***** UserLogin request has an invalid role");
			}
		} else if (numberOfRoles > 1) {
			// Multiple Account Home Page
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch.
				displayMultipleRoleDispatch(theStage, user);
		} else {
			// Handle case where user exists but has 0 roles assigned
			ViewUserLogin.alertUsernamePasswordError.setContentText("No valid roles assigned to this user.");
			ViewUserLogin.alertUsernamePasswordError.showAndWait();
		}
	}
	
		
	/**********
	 * <p> Method: setup() </p>
	 * * <p> Description: This method is called to reset the page and then populate it with new
	 * content for the new user.</p>
	 * */
	protected static void doSetupAccount(Stage theStage, String invitationCode) {
		guiNewAccount.ViewNewAccount.displayNewAccount(theStage, invitationCode);
	}

	
	/**********
	 * <p> Method: public performQuit() </p>
	 * * <p> Description: This method is called when the user has clicked on the Quit button.  Doing
	 * this terminates the execution of the application.  All important data must be stored in the
	 * database, so this is just a clean way to exit.</p>
	 * */
	protected static void performQuit() {
		System.exit(0);
	}
}