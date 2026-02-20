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
	
	// --- NEW ADMIN REQUEST FEATURE METHODS ---
	public static void refreshRequestsTree(javafx.scene.control.TreeView<String> treeView) {
        javafx.scene.control.TreeItem<String> hiddenRoot = new javafx.scene.control.TreeItem<>("Hidden");
        javafx.scene.control.TreeItem<String> openRoot = new javafx.scene.control.TreeItem<>("Open Requests");
        javafx.scene.control.TreeItem<String> closedRoot = new javafx.scene.control.TreeItem<>("Closed Requests");
        openRoot.setExpanded(true); closedRoot.setExpanded(true);

        java.util.List<String> openReqs = theDatabase.getAdminRequests("Pending", null);
        for (String r : openReqs) {
            String[] p = r.split("<SEP>");
            openRoot.getChildren().add(new javafx.scene.control.TreeItem<>("[Req-" + p[0] + "] " + p[1]));
        }

        java.util.List<String> closedReqs = theDatabase.getAdminRequests("Closed", null);
        for (String r : closedReqs) {
            String[] p = r.split("<SEP>");
            closedRoot.getChildren().add(new javafx.scene.control.TreeItem<>("[Req-" + p[0] + "] " + p[1] + " (" + p[2] + ")"));
        }

        hiddenRoot.getChildren().addAll(openRoot, closedRoot);
        treeView.setRoot(hiddenRoot);
        treeView.setShowRoot(false);
    }
	
	public static void renderAdminRequestDetails(int reqId, javafx.scene.layout.VBox container, javafx.scene.control.TreeView<String> tree) {
		container.getChildren().clear();
		container.setStyle("-fx-padding: 15; -fx-background-color: white;");
		
		java.util.List<String> reqs = theDatabase.getAdminRequests(null, null);
		String[] reqParts = null;
		for (String r : reqs) {
			String[] p = r.split("<SEP>");
			if (Integer.parseInt(p[0]) == reqId) { reqParts = p; break; }
		}
		if (reqParts == null) return;
		
		String username = reqParts[1];
		String status = reqParts[2];
		boolean wasDenied = reqParts[3].equals("1");
		String message = reqParts[4];
		String adminNotes = reqParts.length > 5 ? reqParts[5] : "";
		
		javafx.scene.control.Label title = new javafx.scene.control.Label("Admin Privileges Request #" + reqId);
		title.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
		
		javafx.scene.control.Label lblUser = new javafx.scene.control.Label("From: " + username);
		lblUser.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
		
		javafx.scene.control.Label lblWarning = new javafx.scene.control.Label();
		if (wasDenied) {
			lblWarning.setText("⚠️ WARNING: This user was previously denied a request.");
			lblWarning.setTextFill(javafx.scene.paint.Color.RED);
		}
		
		javafx.scene.layout.VBox msgBox = new javafx.scene.layout.VBox(5);
		msgBox.setStyle("-fx-background-color: #f3f4f6; -fx-padding: 10; -fx-background-radius: 5;");
		javafx.scene.control.Label lblMsg = new javafx.scene.control.Label(message);
		lblMsg.setWrapText(true);
		msgBox.getChildren().addAll(new javafx.scene.control.Label("User's Request Message:"), lblMsg);
		
		javafx.scene.control.Label lblNotes = new javafx.scene.control.Label("Admin Notes (Only visible to you & Staff):");
		javafx.scene.control.TextArea txtNotes = new javafx.scene.control.TextArea(adminNotes);
		txtNotes.setPrefRowCount(4);
		txtNotes.setWrapText(true);
		
		container.getChildren().addAll(title, lblUser);
		if (wasDenied) container.getChildren().add(lblWarning);
		container.getChildren().addAll(msgBox, lblNotes, txtNotes);
		
		if (status.equals("Pending")) {
			javafx.scene.layout.HBox btnBox = new javafx.scene.layout.HBox(10);
			javafx.scene.control.Button btnAccept = new javafx.scene.control.Button("Accept (Grants 24h Admin)");
			btnAccept.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
			btnAccept.setOnAction(e -> {
				theDatabase.acceptAdminRequest(reqId, username, txtNotes.getText());
				refreshRequestsTree(tree);
				renderAdminRequestDetails(reqId, container, tree);
			});
			
			javafx.scene.control.Button btnDeny = new javafx.scene.control.Button("Deny Request");
			btnDeny.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
			btnDeny.setOnAction(e -> {
				theDatabase.denyAdminRequest(reqId, txtNotes.getText());
				refreshRequestsTree(tree);
				renderAdminRequestDetails(reqId, container, tree);
			});
			
			btnBox.getChildren().addAll(btnAccept, btnDeny);
			container.getChildren().add(btnBox);
		} else {
			javafx.scene.control.Label lblStatus = new javafx.scene.control.Label("Status: " + status);
			lblStatus.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
			if (status.equals("Accepted")) lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
			else lblStatus.setTextFill(javafx.scene.paint.Color.RED);
			
			javafx.scene.control.Button btnUpdateNotes = new javafx.scene.control.Button("Save Updated Notes");
			btnUpdateNotes.setOnAction(e -> {
				theDatabase.updateAdminRequestNotes(reqId, txtNotes.getText());
				refreshRequestsTree(tree);
			});
			
			container.getChildren().addAll(lblStatus, btnUpdateNotes);
		}
	}
	// --- END NEW ADMIN REQUEST FEATURE METHODS ---

	// ----------------------------------------------------------------------
		// ONE-TIME PASSWORD FEATURE
		// ----------------------------------------------------------------------
		protected static void setOnetimePassword() {
			// Create a custom dialog window
			javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
			dialog.setTitle("One-Time Password");
			dialog.setHeaderText("Select a User to Generate OTP");

			// Set the button types (Generate and Cancel)
			javafx.scene.control.ButtonType generateButtonType = new javafx.scene.control.ButtonType("Generate OTP", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, javafx.scene.control.ButtonType.CANCEL);

			// Create a ListView to hold and display the users
			javafx.scene.control.ListView<String> listView = new javafx.scene.control.ListView<>();
			
			// Fetch the user list and filter out the placeholder
			java.util.List<String> rawUsers = theDatabase.getUserList();
			java.util.List<String> displayUsers = new java.util.ArrayList<>();
			if (rawUsers != null) {
				for (String u : rawUsers) {
					if (!u.equals("<Select a User>")) {
						displayUsers.add(u);
					}
				}
			}
			
			listView.setItems(javafx.collections.FXCollections.observableArrayList(displayUsers));
			
			// Set the layout size
			javafx.scene.layout.VBox selectionVbox = new javafx.scene.layout.VBox(10, listView);
			selectionVbox.setPrefWidth(300);
			selectionVbox.setPrefHeight(250);
			dialog.getDialogPane().setContent(selectionVbox);

			// Disable the Generate button initially so it cannot be clicked until a user is selected
			javafx.scene.Node generateButton = dialog.getDialogPane().lookupButton(generateButtonType);
			generateButton.setDisable(true);

			listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
				generateButton.setDisable(newValue == null);
			});

			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == generateButtonType) {
					return listView.getSelectionModel().getSelectedItem();
				}
				return null;
			});

			Optional<String> result = dialog.showAndWait();
			
			if (result.isPresent()) {
				String targetUser = result.get();
				
				// Generate 12-char password to meet requirements (Start with letter)
				String otp = generateRandomPassword(12);
				
				try {
					// Actually save the newly generated OTP to the database!
					// Without this line, the system never overwrites the old password.
					theDatabase.resetPassword(targetUser, otp);
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("OTP Generated Successfully");
					alert.setHeaderText("One-Time Password set for " + targetUser);
					
					// CHANGED: Use a dedicated TextField just for the password so it copies perfectly without hidden spaces
					javafx.scene.control.TextField passwordField = new javafx.scene.control.TextField(otp);
					passwordField.setEditable(false);
					passwordField.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial';");
					
					javafx.scene.control.Label topLabel = new javafx.scene.control.Label("Please share this with the user. They can use it to login immediately.\n\nNew Password:");
					javafx.scene.control.Label timerLabel = new javafx.scene.control.Label("\nTime Remaining: 24 Hours : 00 Minutes\n(Updates automatically every minute)");
					
					// Stack them vertically
					javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(5, topLabel, passwordField, timerLabel);
					
					long expiryTimeMillis = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
					
					javafx.animation.Timeline timeline = new javafx.animation.Timeline(
						new javafx.animation.KeyFrame(javafx.util.Duration.minutes(1), e -> {
							long millisLeft = expiryTimeMillis - System.currentTimeMillis();
							if (millisLeft <= 0) {
								timerLabel.setText("\nStatus: EXPIRED");
							} else {
								long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(millisLeft);
								long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millisLeft) % 60;
								timerLabel.setText(String.format("\nTime Remaining: %02d Hours : %02d Minutes\n(Updates automatically every minute)", hours, minutes));
							}
						})
					);
					timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
					timeline.play();
					
					alert.getDialogPane().setContent(vbox);
					alert.setOnHidden(e -> timeline.stop());
					alert.showAndWait();
					
				} catch (Exception e) {
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
			// Create a custom dialog window
			javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
			dialog.setTitle("Delete User");
			dialog.setHeaderText("Select a User to Delete");

			// Set the button types (Delete and Cancel)
			javafx.scene.control.ButtonType deleteButtonType = new javafx.scene.control.ButtonType("Delete", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, javafx.scene.control.ButtonType.CANCEL);

			// Create a ListView to hold and display the users
			javafx.scene.control.ListView<String> listView = new javafx.scene.control.ListView<>();
			
			// Fetch the user list and filter out the "<Select a User>" placeholder AND the currently logged-in Admin
			java.util.List<String> rawUsers = theDatabase.getUserList();
			java.util.List<String> displayUsers = new java.util.ArrayList<>();
			if (rawUsers != null) {
				for (String u : rawUsers) {
					if (!u.equals("<Select a User>") && !u.equalsIgnoreCase(ViewAdminHome.theUser.getUserName())) {
						displayUsers.add(u);
					}
				}
			}
			
			listView.setItems(javafx.collections.FXCollections.observableArrayList(displayUsers));
			
			// Set the layout size
			javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10, listView);
			vbox.setPrefWidth(300);
			vbox.setPrefHeight(250);
			dialog.getDialogPane().setContent(vbox);

			// Disable the Delete button initially so it cannot be clicked until a user is selected
			javafx.scene.Node deleteButton = dialog.getDialogPane().lookupButton(deleteButtonType);
			deleteButton.setDisable(true);

			// Add a listener: Enable the Delete button ONLY when a user is clicked/selected
			listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
				deleteButton.setDisable(newValue == null);
			});

			// Return the selected username when the Delete button is clicked
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == deleteButtonType) {
					return listView.getSelectionModel().getSelectedItem();
				}
				return null;
			});

			// Show the dialog and capture the result
			Optional<String> result = dialog.showAndWait();
			
			if (result.isPresent()) {
				String targetUser = result.get();
				
				// Show the "Are you sure?" confirmation popup
				Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
				confirmAlert.setTitle("Confirm Deletion");
				confirmAlert.setHeaderText("Are you sure?");
				confirmAlert.setContentText("Are you sure you want to permanently delete user '" + targetUser + "'?");
				
				// Custom Yes / No buttons
				javafx.scene.control.ButtonType btnYes = new javafx.scene.control.ButtonType("Yes", javafx.scene.control.ButtonBar.ButtonData.YES);
				javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("No", javafx.scene.control.ButtonBar.ButtonData.NO);
				confirmAlert.getButtonTypes().setAll(btnYes, btnNo);
				
				Optional<javafx.scene.control.ButtonType> confirmResult = confirmAlert.showAndWait();
				
				// Only delete if they explicitly clicked "Yes"
				if (confirmResult.isPresent() && confirmResult.get() == btnYes) {
					try {
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
		}
		protected static void listUsers() {
			try {
				// FIX: Convert the List<String> into a single formatted String with line breaks!
				java.util.List<String> userList = theDatabase.getListOfUsers();
				String report = String.join("\n", userList);
						
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("System User List");
				alert.setHeaderText("Registered Users");
						
				// Use a TextArea to ensure the content is fully copyable and formatted cleanly
				javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(report);
				textArea.setEditable(false);
				textArea.setWrapText(false);
				textArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 13px;"); // Monospace aligns columns perfectly
				textArea.setPrefWidth(550);
				textArea.setPrefHeight(300);
						
				alert.getDialogPane().setContent(textArea);
				alert.showAndWait();
						
			} catch (Exception e) {
			showAlert("Database Error", e.getMessage());
			}
	}
	

		protected static void performInvitation() {
			String email = ViewAdminHome.text_InvitationEmailAddress.getText();
			if (invalidEmailAddress(email)) return;
			
			String role = ViewAdminHome.combobox_SelectRole.getValue();
			
			// CHANGED: Default to "Staff" if no role is selected
			if (role == null || role.isEmpty()) {
				role = "Staff";
			}
		
		//Use the Database method to generate the code and insert it with the selected role
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
		javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
		dialog.setTitle("Manage Invitations");
		dialog.setHeaderText("Select an Invitation to Delete");

		javafx.scene.control.ButtonType deleteButtonType = new javafx.scene.control.ButtonType("Delete", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, javafx.scene.control.ButtonType.CANCEL);

		// Use a ListView to make them clickable/selectable
		javafx.scene.control.ListView<String> listView = new javafx.scene.control.ListView<>();
		
		java.util.List<String> invList = theDatabase.getInvitationList();
		listView.setItems(javafx.collections.FXCollections.observableArrayList(invList));
		listView.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 13px;");

		// Header formatted with monospaced font to match the list data spacing exactly
		javafx.scene.control.Label listHeader = new javafx.scene.control.Label(String.format("%-10s | %-30s | %s", "Code", "Email Address", "Role"));
		listHeader.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 13px; -fx-font-weight: bold;");

		// Custom Key Event listener so Ctrl+C / Cmd+C automatically isolates and copies just the 6-character code
		listView.setOnKeyPressed(event -> {
			if (new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.C, javafx.scene.input.KeyCombination.SHORTCUT_DOWN).match(event)) {
				String selection = listView.getSelectionModel().getSelectedItem();
				if (selection != null) {
					String targetCode = selection.split("\\|")[0].trim();
					javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
					content.putString(targetCode);
					javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
				}
			}
		});
		
		// Use the styled listHeader in the VBox to ensure perfect alignment with the listView
		javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(5, listHeader, listView);
		vbox.setPrefWidth(550);
		vbox.setPrefHeight(250);
		dialog.getDialogPane().setContent(vbox);

		// Disable Delete button until an item is clicked
		javafx.scene.Node deleteButton = dialog.getDialogPane().lookupButton(deleteButtonType);
		deleteButton.setDisable(true);

		listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			deleteButton.setDisable(newValue == null);
		});

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == deleteButtonType) {
				return listView.getSelectionModel().getSelectedItem();
			}
			return null;
		});

		Optional<String> result = dialog.showAndWait();
		
		if (result.isPresent()) {
			// Extract just the code (the first 6 characters) from the formatted string
			String selection = result.get();
			String targetCode = selection.split("\\|")[0].trim();
			
			// Show the "Are you sure?" confirmation popup
			Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
			confirmAlert.setTitle("Confirm Deletion");
			confirmAlert.setHeaderText("Are you sure?");
			confirmAlert.setContentText("Are you sure you want to delete invitation code '" + targetCode + "'?");
			
			javafx.scene.control.ButtonType btnYes = new javafx.scene.control.ButtonType("Yes", javafx.scene.control.ButtonBar.ButtonData.YES);
			javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("No", javafx.scene.control.ButtonBar.ButtonData.NO);
			confirmAlert.getButtonTypes().setAll(btnYes, btnNo);
			
			Optional<javafx.scene.control.ButtonType> confirmResult = confirmAlert.showAndWait();
			
			if (confirmResult.isPresent() && confirmResult.get() == btnYes) {
				try {
					theDatabase.deleteInvitation(targetCode);
					ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
							theDatabase.getNumberOfInvitations());
					showAlert("Success", "Invitation '" + targetCode + "' has been deleted.");
				} catch (SQLException e) {
					showAlert("Database Error", e.getMessage());
				}
			}
		}
	}
	
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	protected static boolean invalidEmailAddress(String emailAddress) {
		// First check if it is completely empty
		if (emailAddress == null || emailAddress.trim().isEmpty()) {
			ViewAdminHome.alertEmailError.setHeaderText("Email Required");
			ViewAdminHome.alertEmailError.setContentText("Email address cannot be empty. Please correct and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		
		// requires at least one character, an '@', at least one character, a '.', and at least one character
		String emailRegex = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
		if (!emailAddress.matches(emailRegex)) {
			ViewAdminHome.alertEmailError.setHeaderText("Invalid Email Format");
			ViewAdminHome.alertEmailError.setContentText("Please enter a valid email address (e.g., example@email.com).");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		
		return false; // Email is valid
	}
	
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	protected static void performQuit() {
		// Safely release the SQLite lock before quitting
		if (applicationMain.FoundationsMain.database != null) {
			applicationMain.FoundationsMain.database.closeConnection();
		}
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