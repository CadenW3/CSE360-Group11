package guiAdminHome;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;

import database.Database;
import entityClasses.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;

/*******
 * <p> Title: ControllerAdminHome Class. </p>
 * * <p> Description: Controls the behavior of the Admin Home Page.
 * Implements logic for listing users, deleting users, and generating 
 * One-Time Passwords (OTP) via pop-up dialogs.</p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * @version 1.10		2025-02-10 Implemented Admin Features
 */
public class ControllerAdminHome {
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	public ControllerAdminHome() {
	}

	// ----------------------------------------------------------------------
	// ONE-TIME PASSWORD FEATURE
	// ----------------------------------------------------------------------
	protected static void setOnetimePassword() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("One-Time Password");
		dialog.setHeaderText("Generate OTP for User");
		dialog.setContentText("Enter the Username:");

		Optional<String> result = dialog.showAndWait();
		
		if (result.isPresent()) {
			String targetUser = result.get();
			if (targetUser.isEmpty()) return;

			// Generate 12-char password to meet requirements (Start with letter)
			String otp = generateRandomPassword(12);
			
			try {
				if (!theDatabase.usernameExists(targetUser)) {
					showAlert("Error", "User not found: " + targetUser);
					return;
				}
				
				theDatabase.resetPassword(targetUser, otp);
				
				showAlert("Success", "One-Time Password set for " + targetUser + ".\n\n" +
						"New Password: " + otp + "\n\n" +
						"Please share this with the user. They can use it to login immediately.");
				
			} catch (SQLException e) {
				showAlert("Database Error", e.getMessage());
			}
		}
	}
	
	private static String generateRandomPassword(int length) {
		if (length < 12) length = 12; 
		
		String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lower = "abcdefghijklmnopqrstuvwxyz";
		String nums = "0123456789";
		String special = "!@#$%^&*()_+-=[]?";
		String all = upper + lower + nums + special;
		String letters = upper + lower;
		
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		
		// 1. Ensure First character is a letter
		char firstChar = letters.charAt(random.nextInt(letters.length()));

		// 2. Ensure at least 2 numbers
		for(int i = 0; i < 2; i++) {
			sb.append(nums.charAt(random.nextInt(nums.length())));
		}
		
		// 3. Ensure at least 1 special char
		sb.append(special.charAt(random.nextInt(special.length())));
		
		// 4. Fill the rest with random characters
		for (int i = 0; i < length - 4; i++) {
			sb.append(all.charAt(random.nextInt(all.length())));
		}
		
		// 5. Shuffle the NON-FIRST characters
		char[] restOfChars = sb.toString().toCharArray();
		for (int i = 0; i < restOfChars.length; i++) {
			int randomIndex = random.nextInt(restOfChars.length);
			char temp = restOfChars[i];
			restOfChars[i] = restOfChars[randomIndex];
			restOfChars[randomIndex] = temp;
		}
		
		return firstChar + new String(restOfChars);
	}

	// ----------------------------------------------------------------------
	// DELETE USER FEATURE
	// ----------------------------------------------------------------------
	protected static void deleteUser() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Delete User");
		dialog.setHeaderText("WARNING: Delete User Account");
		dialog.setContentText("Enter the Username to DELETE:");

		Optional<String> result = dialog.showAndWait();
		
		if (result.isPresent()) {
			String targetUser = result.get();
			if (targetUser.isEmpty()) return;
			
			if (targetUser.equals(ViewAdminHome.theUser.getUserName())) {
				showAlert("Error", "You cannot delete your own admin account while logged in.");
				return;
			}

			try {
				if (!theDatabase.usernameExists(targetUser)) {
					showAlert("Error", "User not found.");
					return;
				}
				
				theDatabase.deleteUser(targetUser);
				
				// Update the counter label immediately
				ViewAdminHome.label_NumberOfUsers.setText("Number of users: " + 
						theDatabase.getNumberOfUsers());
				
				showAlert("Success", "User '" + targetUser + "' has been deleted.");
				
			} catch (SQLException e) {
				showAlert("Database Error", e.getMessage());
			}
		}
	}

	// ----------------------------------------------------------------------
	// LIST USERS FEATURE
	// ----------------------------------------------------------------------
	protected static void listUsers() {
		try {
			String report = theDatabase.getListOfUsers();
			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("System User List");
			alert.setHeaderText("Registered Users");
			alert.setContentText(report);
			alert.getDialogPane().setMinWidth(400);
			alert.showAndWait();
			
		} catch (SQLException e) {
			showAlert("Database Error", e.getMessage());
		}
	}
	
	// ----------------------------------------------------------------------
	// OTHER REQUIRED METHODS
	// ----------------------------------------------------------------------
	protected static void performInvitation() {
		String email = ViewAdminHome.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(email)) return;
		
		// FIX: Read the role from the ComboBox instead of using a hardcoded string
		String role = ViewAdminHome.combobox_SelectRole.getValue();
		
		// If no role is selected, default to Role1 to prevent errors
		if (role == null || role.isEmpty()) {
			role = "Role1";
		}
		
		// Use the Database method to generate the code and insert it with the selected role
		String inviteCode = theDatabase.generateInvitationCode(email, role);
		
		// Update the Invitation Counter on the screen
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
		
		// Show the code to the Admin in a popup
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Invitation Sent");
		alert.setHeaderText("Invitation Code Generated");
		alert.setContentText("The invitation has been logged.\n" +
				"Please send the following code to " + email + ":\n\n" + 
				inviteCode);
		alert.showAndWait();
	}
	
	protected static void manageInvitations() {
		try {
			// 1. Show the list of invitations (similar to listUsers)
			String report = theDatabase.getInvitationListReport();
			
			Alert listAlert = new Alert(AlertType.INFORMATION);
			listAlert.setTitle("Manage Invitations");
			listAlert.setHeaderText("Active Invitation Codes");
			listAlert.setContentText(report);
			listAlert.getDialogPane().setMinWidth(450);
			listAlert.showAndWait();
			
			// 2. Allow deletion of an invitation
			TextInputDialog deleteDialog = new TextInputDialog();
			deleteDialog.setTitle("Delete Invitation");
			deleteDialog.setHeaderText("Remove Invitation from System");
			deleteDialog.setContentText("Enter the Invitation Code to DELETE (or leave blank to cancel):");

			Optional<String> result = deleteDialog.showAndWait();
			
			if (result.isPresent()) {
				String targetCode = result.get().trim();
				if (targetCode.isEmpty()) return;

				theDatabase.deleteInvitation(targetCode);
				
				// 3. Update the counter label in ViewAdminHome
				ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
						theDatabase.getNumberOfInvitations());
				
				showAlert("Success", "Invitation '" + targetCode + "' has been deleted.");
			}
			
		} catch (SQLException e) {
			showAlert("Database Error", e.getMessage());
		}
	}
	
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	protected static boolean invalidEmailAddress(String emailAddress) {
		if (emailAddress.length() == 0) {
			ViewAdminHome.alertEmailError.setContentText("Correct the email address and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		return false;
	}
	
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	protected static void performQuit() {
		System.exit(0);
	}
	
	private static void showAlert(String title, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}