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
	
	private static Database theDatabase;	// This points to the database
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
		String username = ViewUserLogin.text_Username.getText().trim();
		String password = ViewUserLogin.text_Password.getText().trim();
    	
    	String usernameError = userNameRecognizerTestbed.UserNameRecognizer.checkForValidUserName(username);
    	
    	if (!usernameError.isEmpty()) {
    	    ViewUserLogin.alertUsernamePasswordError.setContentText(usernameError);
    	    ViewUserLogin.alertUsernamePasswordError.showAndWait();
    	    return;
    	}

    	if (theDatabase == null) {
    		theDatabase = applicationMain.FoundationsMain.database;
    	}

     	if (theDatabase.getUserAccountDetails(username) == false) {
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
		
    	String actualPassword = theDatabase.getCurrentPassword();
    	
    	if (password.compareTo(actualPassword) != 0) {
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}

    	if (theDatabase.isAccountExpired(username)) {
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Your One-Time Password has expired or already been used.\nPlease request a new one.");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}

    	if (theDatabase.isUsingOTP(username)) {
    		boolean resetSuccess = forcePasswordReset(username);
    		if (resetSuccess) {
    			theDatabase.burnOTP(username);
    			ViewUserLogin.alertUsernamePasswordError.setTitle("Password Reset Successful");
    			ViewUserLogin.alertUsernamePasswordError.setHeaderText("Success");
    			ViewUserLogin.alertUsernamePasswordError.setContentText("Your password has been updated.\nPlease log in again with your new password.");
    			ViewUserLogin.alertUsernamePasswordError.showAndWait();
    			ViewUserLogin.text_Username.setText("");
    			ViewUserLogin.text_Password.setText("");
    		}
    		return;
    	}

    	theDatabase.burnOTP(username);
    	
    	User user = new User(username, password, theDatabase.getCurrentFirstName(), 
    	        theDatabase.getCurrentMiddleName(), theDatabase.getCurrentLastName(), 
    	        theDatabase.getCurrentPreferredFirstName(), theDatabase.getCurrentEmailAddress(), 
    	        theDatabase.getCurrentAdminRole(), 
    	        theDatabase.getCurrentNewRole1(), theDatabase.getCurrentNewRole2());

    	int numberOfRoles = theDatabase.getNumberOfRoles(user);		
    	
    	boolean isTempAdmin = false;
    	try {
    		isTempAdmin = theDatabase.isTempAdmin(username);
    		if (isTempAdmin && !user.getAdminRole()) {
    			numberOfRoles++;
    		}
    	} catch(Exception e) {}
    			
    	if (numberOfRoles == 1) {
    		if (user.getAdminRole() || isTempAdmin) {
    			guiAdminHome.ViewAdminHome.displayAdminHome(theStage, user);
   			} else if (user.getNewRole1()) {
   				guiRole1.ViewRole1Home.displayRole1Home(theStage, user);
    		} else if (user.getNewRole2()) {
   				guiRole2.ViewRole2Home.displayRole2Home(theStage, user);
   			} else {
   				System.out.println("***** UserLogin goToUserHome request has an invalid role");
    		}
    	} else if (numberOfRoles > 1) {
    		guiMultipleRoleDispatch.ViewMultipleRoleDispatch.displayMultipleRoleDispatch(theStage, user);
   		} else {
   			ViewUserLogin.alertUsernamePasswordError.setTitle("Login Error");
   			ViewUserLogin.alertUsernamePasswordError.setHeaderText("No Roles Assigned");
    		ViewUserLogin.alertUsernamePasswordError.setContentText("This account exists but has no valid roles assigned.\nPlease contact an Admin.");
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
	
	private static boolean forcePasswordReset(String username) {
		javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
		dialog.setTitle("Forced Password Reset");
		dialog.setHeaderText("You must reset your password to continue.");
		
		javafx.scene.control.ButtonType updateBtnType = new javafx.scene.control.ButtonType("Update Password", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(updateBtnType, javafx.scene.control.ButtonType.CANCEL);
		
		javafx.scene.control.PasswordField pwd1 = new javafx.scene.control.PasswordField();
		pwd1.setPromptText("New Password");
		javafx.scene.control.PasswordField pwd2 = new javafx.scene.control.PasswordField();
		pwd2.setPromptText("Confirm New Password");
		
		javafx.scene.control.Label strengthLabel = new javafx.scene.control.Label();
		
		pwd1.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.isEmpty()) {
				strengthLabel.setText("");
				return;
			}
			String result = checkPassword(newValue);
			if (newValue.length() < 12) {
				strengthLabel.setText("Too Short (<12 chars)");
				strengthLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
			} else if (newValue.length() > 16) {
				strengthLabel.setText("Too Long (>16 chars)");
				strengthLabel.setTextFill(javafx.scene.paint.Color.RED);
			} else if (!result.isEmpty()) {
				strengthLabel.setText(result);
				strengthLabel.setTextFill(javafx.scene.paint.Color.RED);
			} else {
				strengthLabel.setText("Password Strength: Strong");
				strengthLabel.setTextFill(javafx.scene.paint.Color.GREEN);
			}
		});
		
		javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10, 
				new javafx.scene.control.Label("Enter new password:"), pwd1, strengthLabel, 
				new javafx.scene.control.Label("Confirm new password:"), pwd2);
		dialog.getDialogPane().setContent(vbox);
		
		final javafx.scene.Node updateBtn = dialog.getDialogPane().lookupButton(updateBtnType);
		updateBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
			String pass1 = pwd1.getText();
			String pass2 = pwd2.getText();
			String error = checkPassword(pass1);
			
			if (!error.isEmpty()) {
				javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, error);
				alert.showAndWait();
				event.consume(); 
			} else if (!pass1.equals(pass2)) {
				javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Passwords do not match!");
				alert.showAndWait();
				event.consume(); 
			}
		});
		
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == updateBtnType) {
				return pwd1.getText();
			}
			return null;
		});
		
		java.util.Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			theDatabase.updatePassword(username, result.get());
			return true;
		}
		return false; 
	}

	private static String checkPassword(String password) {
		if (password.length() < 12) return "Password must be at least 12 characters";
		if (password.length() > 16) return "Password must be less than 16 characters";

		int numberCount = 0;
		int specialCount = 0;

		for (char c : password.toCharArray()) {
			if (Character.isDigit(c)) numberCount++;
			else if (!Character.isLetterOrDigit(c)) specialCount++;
		}
		
		if (numberCount < 2) return "Weak Password. Please use at least 2 numbers";
		if (specialCount < 1) return "Weak Password. Please use at least 1 special character";

		return "";
	}
}