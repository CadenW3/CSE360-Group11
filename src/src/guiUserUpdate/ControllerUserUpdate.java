package guiUserUpdate;

import entityClasses.User;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.util.Optional;
import database.Database;

public class ControllerUserUpdate {
	/*-********************************************************************************************

	The Controller for ViewUserUpdate 
	
	**********************************************************************************************/

	/**********
	 * <p> Title: ControllerUserUpdate Class</p>
	 * * <p> Description: This static class supports the actions initiated by the ViewUserUpdate
	 * class. In this case, there is just one method, no constructors, and no attributes.</p>
	 *
	 */

	/*-********************************************************************************************

	The User Interface Actions for this page
	
	**********************************************************************************************/

	
	/**********
	 * <p> Method: public goToUserHomePage(Stage theStage, User theUser) </p>
	 * * <p> Description: This method is called when the user has clicked on the button to
	 * proceed to the user's home page.
	 * * @param theStage specifies the JavaFX Stage for next next GUI page and it's methods
	 * * @param theUser specifies the user so we go to the right page and so the right information
	 */
	protected static void goToUserHomePage(Stage theStage, User theUser) {
		
		int theRole = applicationMain.FoundationsMain.activeHomePage;

		switch (theRole) {
		case 1:
			guiAdminHome.ViewAdminHome.displayAdminHome(theStage, theUser);
			break;
		case 2:
			guiRole1.ViewRole1Home.displayRole1Home(theStage, theUser);
			break;
		case 3:
			guiRole2.ViewRole2Home.displayRole2Home(theStage, theUser);
			break;
		default: 
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch.displayMultipleRoleDispatch(theStage, theUser);
			break;
		}
 	}
	
	protected static void performUpdateFirstName(User theUser) {
		TextInputDialog dialog = new TextInputDialog(theUser.getFirstName());
		dialog.setTitle("Update First Name");
		dialog.setHeaderText("Update First Name");
		dialog.setContentText("Please enter your First Name:");
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			System.out.println("First Name Updated to: " + result.get());
		}
	}

	protected static void performUpdateMiddleName(User theUser) {
		TextInputDialog dialog = new TextInputDialog(theUser.getMiddleName());
		dialog.setTitle("Update Middle Name");
		dialog.setHeaderText("Update Middle Name");
		dialog.setContentText("Please enter your Middle Name:");
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			System.out.println("Middle Name Updated to: " + result.get());
		}
	}

	protected static void performUpdateLastName(User theUser) {
		TextInputDialog dialog = new TextInputDialog(theUser.getLastName());
		dialog.setTitle("Update Last Name");
		dialog.setHeaderText("Update Last Name");
		dialog.setContentText("Please enter your Last Name:");
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			System.out.println("Last Name Updated to: " + result.get());
		}
	}

	protected static void performUpdatePreferredFirstName(User theUser) {
		TextInputDialog dialog = new TextInputDialog(theUser.getPreferredFirstName());
		dialog.setTitle("Update Preferred First Name");
		dialog.setHeaderText("Update Preferred First Name");
		dialog.setContentText("Please enter your Preferred First Name:");
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			System.out.println("Preferred Name Updated to: " + result.get());
		}
	}

	protected static void performUpdateEmailAddress(User theUser) {
		TextInputDialog dialog = new TextInputDialog(theUser.getEmailAddress());
		dialog.setTitle("Update Email Address");
		dialog.setHeaderText("Update Email Address");
		dialog.setContentText("Please enter your Email Address:");
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			System.out.println("Email Updated to: " + result.get());
		}
	}
	
	/**********
	 * <p> Method: performUpdatePassword(User theUser) </p>
	 * * <p> Description: Allows the user to change their password.
	 * This is critical for users who logged in with a One-Time Password (OTP).
	 * Changing the password here overwrites the OTP in the database, ensuring 
	 * the OTP can never be used again.</p>
	 * * @param theUser The current user object
	 */
	protected static String performUpdatePassword(User theUser) {
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Change Password");
		dialog.setHeaderText("Set New Password");
		dialog.setContentText("Enter new password:");
		
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			String newPass = result.get();
			if (newPass.isEmpty()) return null;
			
			String error = checkPassword(newPass);
			if (!error.isEmpty()) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Invalid Password");
				alert.setContentText(error);
				alert.showAndWait();
				return null;
			}
			
			TextInputDialog confirmDialog = new TextInputDialog("");
			confirmDialog.setTitle("Confirm Password");
			confirmDialog.setHeaderText("Confirm New Password");
			confirmDialog.setContentText("Re-enter new password:");
			
			Optional<String> confirmResult = confirmDialog.showAndWait();
			if (confirmResult.isPresent()) {
				String confirmPass = confirmResult.get();
				
				if (!newPass.equals(confirmPass)) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("Passwords do not match.");
					alert.showAndWait();
					return null;
				}
				
				try {
					Database db = applicationMain.FoundationsMain.database;
					db.resetPassword(theUser.getUserName(), newPass);
					
					Alert success = new Alert(AlertType.INFORMATION);
					success.setContentText("Password changed successfully.\nThe old password (or OTP) is no longer valid.");
					success.showAndWait();
					return newPass;
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	private static String checkPassword(String password) {
		if (password.length() < 12) return "Password must be at least 12 characters";
		if (password.length() > 16) return "Password must be less than 16 characters";
		if (!Character.isLetter(password.charAt(0))) return "Password must start with a letter (a-z or A-Z).";

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