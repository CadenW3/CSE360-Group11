package guiNewAccount;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;

/*******
 * <p> Title: ViewNewAccount Class. </p>
 * * <p> Description: The ViewNewAccount Page is used to enable a potential user with an invitation
 * code to establish an account after they have specified an invitation code on the standard login
 * page. </p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * * @version 1.02		2025-08-19 Added Real-time Password Checker
 * */

public class ViewNewAccount {
	
	/*-********************************************************************************************

	Attributes
	
	*/
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;
	
	private static Label label_ApplicationTitle = new Label("Foundation Application Account Setup Page");
    protected static Label label_NewUserCreation = new Label(" User Account Creation.");
    protected static Label label_NewUserLine = new Label("Please enter a username and a password.");
    
    protected static TextField text_Username = new TextField();
    
    protected static PasswordField text_Password1 = new PasswordField();
    // NEW: Label for real-time password feedback
    protected static Label label_PasswordStrength = new Label(""); 
    
    protected static PasswordField text_Password2 = new PasswordField();
    protected static Button button_UserSetup = new Button("User Setup");
    protected static TextField text_Invitation = new TextField();

    protected static Alert alertInvitationCodeIsInvalid = new Alert(AlertType.INFORMATION);
	protected static Alert alertUsernamePasswordError = new Alert(AlertType.WARNING); // Changed to WARNING for better visuals

    protected static Button button_Quit = new Button("Quit");

	private static ViewNewAccount theView;	
	private static Database theDatabase = applicationMain.FoundationsMain.database;		

	protected static Stage theStage;			
	private static Pane theRootPane;			
	protected static User theUser;				
   
    protected static String theInvitationCode;	
    protected static String emailAddress;		
    protected static String theRole;			
	public static Scene theNewAccountScene = null;	
	

	/*-********************************************************************************************

	Constructors
	
	*/

	public static void displayNewAccount(Stage ps, String ic) {
		ControllerNewAccount.setDatabase(theDatabase);
		theStage = ps;				
		theInvitationCode = ic;		
		
		if (theView == null) theView = new ViewNewAccount();
		
		text_Username.setText("");	
		text_Password1.setText("");	
		text_Password2.setText("");
		label_PasswordStrength.setText("");
		
//		Had to block out code to create new users Cannot figure out invitation Code for now
		
		theRole = theDatabase.getRoleGivenAnInvitationCode(theInvitationCode);
		
		if (theRole.length() == 0) {
			alertInvitationCodeIsInvalid.showAndWait();	
			return;					
		}
		
		emailAddress = theDatabase.getEmailAddressUsingCode(theInvitationCode);
		
    	theRootPane.getChildren().clear();
    	// Added label_PasswordStrength to the pane
    	theRootPane.getChildren().addAll(label_NewUserCreation, label_NewUserLine, text_Username,
    			text_Password1, label_PasswordStrength, text_Password2, button_UserSetup, button_Quit);    	

		theStage.setTitle("CSE 360 Foundation Code: New User Account Setup");	
        theStage.setScene(theNewAccountScene);
		theStage.show();
	}
	
	private ViewNewAccount() {
		
		theRootPane = new Pane();
		theNewAccountScene = new Scene(theRootPane, width, height);

		setupLabelUI(label_ApplicationTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
    	setupLabelUI(label_NewUserCreation, "Arial", 32, width, Pos.CENTER, 0, 10);
    	setupLabelUI(label_NewUserLine, "Arial", 24, width, Pos.CENTER, 0, 70);
		
		setupTextUI(text_Username, "Arial", 18, 300, Pos.BASELINE_LEFT, 50, 160, true);
		text_Username.setPromptText("Enter the Username");
		
		// Password 1 Field
		setupTextUI(text_Password1, "Arial", 18, 300, Pos.BASELINE_LEFT, 50, 210, true);
		text_Password1.setPromptText("Enter the Password");
		
		// Password listener for real time
		text_Password1.textProperty().addListener((observable, oldValue, newValue) -> {
			ControllerNewAccount.checkPasswordStrengthRealTime(newValue);
		});
		
		// Password strength label
		setupLabelUI(label_PasswordStrength, "Arial", 14, 300, Pos.BASELINE_LEFT, 50, 242);
		
		setupTextUI(text_Password2, "Arial", 18, 300, Pos.BASELINE_LEFT, 50, 270, true); // Moved down slightly to make room
		text_Password2.setPromptText("Enter the Password Again");
		
		alertInvitationCodeIsInvalid.setTitle("Invalid Invitation Code");
		alertInvitationCodeIsInvalid.setHeaderText("The invitation code is not valid.");
		alertInvitationCodeIsInvalid.setContentText("Correct the code and try again.");

		alertUsernamePasswordError.setTitle("Password Error");
		alertUsernamePasswordError.setHeaderText("Invalid Password");
		alertUsernamePasswordError.setContentText("Correct the passwords and try again.");

        setupButtonUI(button_UserSetup, "Dialog", 18, 200, Pos.CENTER, 475, 210);
        // Updated Lambda to pass username
        button_UserSetup.setOnAction((event) -> { ControllerNewAccount.processUserStep(text_Username.getText()); });
		
        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((event) -> { ControllerNewAccount.performQuit(); });
	}
	
	/*-********************************************************************************************
	Helper methods
	 */
	
	private void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	private void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}

	private void setupTextUI(TextField t, String ff, double f, double w, Pos p, double x, double y, boolean e){
		t.setFont(Font.font(ff, f));
		t.setMinWidth(w);
		t.setMaxWidth(w);
		t.setAlignment(p);
		t.setLayoutX(x);
		t.setLayoutY(y);		
		t.setEditable(e);
	}	
}