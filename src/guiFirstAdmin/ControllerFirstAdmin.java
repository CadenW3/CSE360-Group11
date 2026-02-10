package guiFirstAdmin;

import java.sql.SQLException;
import userNameRecognizerTestbed.UserNameRecognizer;

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;

/*******
 * <p> Title: ControllerFirstAdmin Class. </p>
 * 
 * <p> Description: ControllerFirstAdmin class provides the controller actions based on the user's
 *  use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have not yet been implemented.  WHhen those buttons
 * are pressed, an alert pops up to tell the user that the function associated with the button has
 * not been implemented. Also, be aware that What has been implemented may not work the way the
 * final product requires and there maybe defects in this code.
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
 *  
 */

public class ControllerFirstAdmin {
	/*-********************************************************************************************

	The controller attributes for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	private static String adminUsername = "";
	private static String adminPassword1 = "";
	private static String adminPassword2 = "";		
	protected static Database theDatabase = applicationMain.FoundationsMain.database;		

	/*-********************************************************************************************

	The User Interface Actions for this page
	
	*/
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerFirstAdmin() {
	}

	/**********
	 * <p> Method: setAdminUsername() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the username field in the
	 * View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminUsername() {
		adminUsername = ViewFirstAdmin.text_AdminUsername.getText();
	}
	
	
	/**********
	 * <p> Method: setAdminPassword1() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the password 1 field in
	 * the View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminPassword1() {
		adminPassword1 = ViewFirstAdmin.text_AdminPassword1.getText();
		ViewFirstAdmin.label_PasswordsDoNotMatch.setText("");
	}
	
	
	/**********
	 * <p> Method: setAdminPassword2() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the password 2 field in
	 * the View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminPassword2() {
		adminPassword2 = ViewFirstAdmin.text_AdminPassword2.getText();		
		ViewFirstAdmin.label_PasswordsDoNotMatch.setText("");
	}
	
	
	/**********
	 * <p> Method: doSetupAdmin() </p>
	 * 
	 * <p> Description: This method is called when the user presses the button to set up the Admin
	 * account.  It start by trying to establish a new user and placing that user into the
	 * database.  If that is successful, we proceed to the UserUpdate page.</p>
	 * 
	 */
protected static void doSetupAdmin(Stage ps, int r) {
		
		// Validate the Admin username using the FSM recognizer before creating the account.
		String usernameError = UserNameRecognizer.checkForValidUserName(adminUsername);
		if (!usernameError.isEmpty()) {
		    ViewFirstAdmin.alertUsernamePasswordError.setTitle("Invalid Admin Username");
		    ViewFirstAdmin.alertUsernamePasswordError.setHeaderText(null);
		    ViewFirstAdmin.alertUsernamePasswordError.setContentText(usernameError);
		    ViewFirstAdmin.alertUsernamePasswordError.showAndWait();
		    return;
		}

		// new code start
		String passwordError = checkPassword(adminPassword1);
		if (!passwordError.isEmpty()) {
			// We reuse the existing alert box to show the password error
			ViewFirstAdmin.alertUsernamePasswordError.setTitle("Invalid Password");
			ViewFirstAdmin.alertUsernamePasswordError.setHeaderText(null);
			ViewFirstAdmin.alertUsernamePasswordError.setContentText(passwordError);
			ViewFirstAdmin.alertUsernamePasswordError.showAndWait();
			return;
		}
		// new code end
		
		// Make sure the two passwords are the same
		if (adminPassword1.compareTo(adminPassword2) == 0) {
        	// Create the user and proceed to the user home page
        	User user = new User(adminUsername, adminPassword1, "", "", "", "", "", true, false, 
        			false);
            try {
            	// Create a new User object with admin role and register in the database
            	theDatabase.register(user);
            	}
            catch (SQLException e) {
                System.err.println("*** ERROR *** Database error trying to register a user: " + 
                		e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
            
            // User was established in the database, so navigate to the User Update Page
        	guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewFirstAdmin.theStage, user);
		}
		else {
			// The two passwords are NOT the same, so clear the passwords, explain the passwords
			// must be the same, and clear the message as soon as the first character is typed.
			ViewFirstAdmin.text_AdminPassword1.setText("");
			ViewFirstAdmin.text_AdminPassword2.setText("");
			ViewFirstAdmin.label_PasswordsDoNotMatch.setText(
					"The two passwords must match. Please try again!");
		}
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
		System.out.println("Perform Quit");
		System.exit(0);
	}
	
	/**********
	 * <p> Method: checkPassword() </p>
	 * * <p> Description: Internal helper method to validate the password against 
	 * security requirements.</p>
	 * * @param password The password string to check
	 * @return An error message string, or an empty string if valid.
	 */
	private static String checkPassword(String password) {
		// Requirement 1: Length check (12 - 16 characters)
		if (password.length() < 12) {
			return "Password must be at least 12 characters";
		}
		if (password.length() > 16) {
			return "Password must be less than 16 characters";
		}

		// Count numbers and special characters
		int numberCount = 0;
		int specialCount = 0;
		
		for (char c : password.toCharArray()) {
			if (Character.isDigit(c)) {
				numberCount++;
			}
			// If it's not a letter and not a digit, it is a special character
			else if (!Character.isLetterOrDigit(c)) {
				specialCount++;
			}
		}

		// Requirement 2: At least 2 numbers
		if (numberCount < 2) {
			return "Weak Password. Please use at least 2 numbers";
		}

		// Requirement 3: At least 1 special character
		if (specialCount < 1) {
			return "Weak Password. Please use at least 1 special character";
		}

		// If no errors, return empty string
		return "";
	}
}

