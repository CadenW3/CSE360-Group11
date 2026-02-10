package guiNewAccount;

import java.sql.SQLException;
import javafx.scene.paint.Color; // Import for Colors
import userNameRecognizerTestbed.UserNameRecognizer;
import database.Database;
import entityClasses.User;

/*******
 * <p> Title: ControllerNewAccount Class. </p>
 * <p> Description: The Java/FX-based New Account Page. </p>
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * @author Lynn Robert Carter
 * @version 1.03 Updated Password Logic (Start with Letter)
 */
public class ControllerNewAccount {

	private static Database theDatabase;

	public ControllerNewAccount() {
	}

	public static void setDatabase(Database db) {
		theDatabase = db;
	}

	//Real-time Password checker
	public static void checkPasswordStrengthRealTime(String password) {
		String result = checkPassword(password);
		
		if (password.isEmpty()) {
			ViewNewAccount.label_PasswordStrength.setText("");
			return;
		}

		// LOGIC:
		// 1. Too Small -> Yellow
		if (password.length() < 12) {
			ViewNewAccount.label_PasswordStrength.setText("Too Short (<12 chars)");
			ViewNewAccount.label_PasswordStrength.setTextFill(Color.ORANGE); // Yellow/Orange
		}
		// 2. Too Big -> Red
		else if (password.length() > 16) {
			ViewNewAccount.label_PasswordStrength.setText("Too Long (>16 chars)");
			ViewNewAccount.label_PasswordStrength.setTextFill(Color.RED);
		}
		// 3. Length is Good, Check Complexity (Num, Special, Start with Letter)
		else if (!result.isEmpty()) {
			// Length is 12-16, but checkPassword returned an error -> Red
			ViewNewAccount.label_PasswordStrength.setText(result); // Show specific error
			ViewNewAccount.label_PasswordStrength.setTextFill(Color.RED);
		}
		// 4. Good -> Green
		else {
			ViewNewAccount.label_PasswordStrength.setText("Password Strength: Strong");
			ViewNewAccount.label_PasswordStrength.setTextFill(Color.GREEN);
		}
	}

	protected static void processUserStep(String username) {

		// 1. Validate the Username
		String errorMessage = UserNameRecognizer.checkForValidUserName(username);
		if (!errorMessage.isEmpty()) {
			ViewNewAccount.alertUsernamePasswordError.setTitle("Invalid Username");
			ViewNewAccount.alertUsernamePasswordError.setHeaderText("Username Error");
			ViewNewAccount.alertUsernamePasswordError.setContentText(errorMessage);
			ViewNewAccount.alertUsernamePasswordError.showAndWait();
			return;
		}

		// Get the passwords from the GUI
		String password1 = ViewNewAccount.text_Password1.getText();
		String password2 = ViewNewAccount.text_Password2.getText();

		// Check Password Requirements
		String passwordError = checkPassword(password1);
		if (!passwordError.isEmpty()) {
			ViewNewAccount.alertUsernamePasswordError.setTitle("Invalid Password");
			ViewNewAccount.alertUsernamePasswordError.setHeaderText("Password Weak or Invalid");
			ViewNewAccount.alertUsernamePasswordError.setContentText(passwordError);
			ViewNewAccount.alertUsernamePasswordError.showAndWait();
			return;
		}

		// 2. Check if the passwords match
		if (password1.compareTo(password2) == 0) {
			
			// fix start
			boolean isAdmin = false;
			boolean isRole1 = false;
			boolean isRole2 = false;
			
			String role = ViewNewAccount.theRole; // Get the role from the View
			
			if ("Admin".equalsIgnoreCase(role)) {
				isAdmin = true;
			} else if ("Role1".equalsIgnoreCase(role)) {
				isRole1 = true;
			} else if ("Role2".equalsIgnoreCase(role)) {
				isRole2 = true;
			} else {
				// Fallback: If the role is just "User" or unknown, assign Role1 so they can log in
				isRole1 = true; 
			}
			// fix end

			// Create the User object with the correct flags
			User user = new User(username, password1, "", "", "", "", "", isAdmin, isRole1, isRole2);

			try {
				theDatabase.register(user);
			} catch (SQLException e) {
				System.err.println("*** ERROR *** Database error: " + e.getMessage());
				e.printStackTrace();
				System.exit(0);
			}

			// Remove the invitation from the system
			theDatabase.removeInvitationAfterUse(ViewNewAccount.theInvitationCode);

			// Set the database so it has this user and the current user
			theDatabase.getUserAccountDetails(username);

			// Navigate to the User Update Page
			guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewNewAccount.theStage, user);
		} else {
			// The two passwords are NOT the same
			ViewNewAccount.text_Password1.setText("");
			ViewNewAccount.text_Password2.setText("");
			ViewNewAccount.label_PasswordStrength.setText(""); 
			ViewNewAccount.alertUsernamePasswordError.setTitle("Password Mismatch");
			ViewNewAccount.alertUsernamePasswordError.setHeaderText(null);
			ViewNewAccount.alertUsernamePasswordError.setContentText("The two passwords must match.");
			ViewNewAccount.alertUsernamePasswordError.showAndWait();
		}
	}

	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}

	// Helper method to validate password rules
	// Returns empty string if VALID, error message string if INVALID
	private static String checkPassword(String password) {
		if (password.length() < 12)
			return "Password must be at least 12 characters";
		if (password.length() > 16)
			return "Password must be less than 16 characters";

		// NEW CHECK: First character must be a letter
		if (!Character.isLetter(password.charAt(0))) {
			return "Password must start with a letter (a-z or A-Z).";
		}

		int numberCount = 0;
		int specialCount = 0;

		for (char c : password.toCharArray()) {
			if (Character.isDigit(c))
				numberCount++;
			else if (!Character.isLetterOrDigit(c))
				specialCount++;
		}

		if (numberCount < 2)
			return "Weak Password. Please use at least 2 numbers";
		if (specialCount < 1)
			return "Weak Password. Please use at least 1 special character";

		return "";
	}
}