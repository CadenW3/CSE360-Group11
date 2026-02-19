package guiRole2;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import database.Database;
/*******
 * <p> Title: ControllerRole2Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 2 Home Page.  This class provides the controller
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

public class ControllerRole2Home {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	/**
	 * Default constructor is not used.
	 */
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	
	
	public ControllerRole2Home() {
		
	}

	/**********
	 * <p> Method: performUpdate() </p>
	 * 
	 * <p> Description: This method directs the user to the User Update Page so the user can change
	 * the user account attributes. </p>
	 * 
	 */
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole2Home.theStage, ViewRole2Home.theUser);
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
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole2Home.theStage);
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
	
	// Builds the Ed Discussion style folder structure + Staff Directory
		protected static void refreshDiscussionTree(javafx.scene.control.TreeView<String> treeView) {
			// Create an invisible absolute root so we can have multiple visible "roots"
			javafx.scene.control.TreeItem<String> hiddenRoot = new javafx.scene.control.TreeItem<>("Hidden");

			// --- 1. Discussions Dropdown ---
			javafx.scene.control.TreeItem<String> discussionsRoot = new javafx.scene.control.TreeItem<>("Discussions");
			discussionsRoot.setExpanded(true);

			java.util.List<String> threads = theDatabase.getThreadList();
			for (String threadData : threads) {
				String[] tParts = threadData.split("\\|");
				if (tParts.length < 4) continue;
				
				int threadId = Integer.parseInt(tParts[0].trim());
				String threadDisplay = "[Thread-" + threadId + "] " + tParts[1].trim() + " (" + tParts[3].trim() + ")";
				javafx.scene.control.TreeItem<String> threadNode = new javafx.scene.control.TreeItem<>(threadDisplay);
				
				java.util.List<String> replies = theDatabase.getRepliesForThread(threadId);
				java.util.Map<Integer, javafx.scene.control.TreeItem<String>> replyNodes = new java.util.HashMap<>();
				
				for (String replyData : replies) {
					String[] rParts = replyData.split("\\|");
					int replyId = Integer.parseInt(rParts[0].trim());
					replyNodes.put(replyId, new javafx.scene.control.TreeItem<>("[Reply-" + replyId + "] " + rParts[2].trim() + " (" + rParts[3].trim() + ")"));
				}
				
				for (String replyData : replies) {
					String[] rParts = replyData.split("\\|");
					int replyId = Integer.parseInt(rParts[0].trim());
					int parentId = Integer.parseInt(rParts[1].trim());
					
					if (parentId == 0) threadNode.getChildren().add(replyNodes.get(replyId));
					else if (replyNodes.containsKey(parentId)) replyNodes.get(parentId).getChildren().add(replyNodes.get(replyId));
				}
				discussionsRoot.getChildren().add(threadNode);
			}

			// --- 2. Staff Messaging Dropdown ---
			javafx.scene.control.TreeItem<String> staffRoot = new javafx.scene.control.TreeItem<>("Staff");
			staffRoot.setExpanded(true);
			
			java.util.List<String> staffMembers = theDatabase.getStaffUsers();
			for (String staff : staffMembers) {
				staffRoot.getChildren().add(new javafx.scene.control.TreeItem<>("[Staff] " + staff));
			}

			// Add both to the hidden root
			hiddenRoot.getChildren().addAll(discussionsRoot, staffRoot);
			treeView.setRoot(hiddenRoot);
			treeView.setShowRoot(false); // This hides the top "Hidden" root, making Discussions and Staff look like the main roots
		}

		// Updated to refresh the Feed Box instantly
		protected static void executeReplyDB(int threadId, int parentReplyId, String content, String userName, javafx.scene.control.TreeView<String> tree, javafx.scene.layout.VBox container) {
			try {
				theDatabase.createReply(threadId, parentReplyId, content, userName);
				refreshDiscussionTree(tree);
				renderThreadView(threadId, container); // INSTANT REFRESH
			} catch (Exception e) { e.printStackTrace(); }
		}

		// Renders the Direct Message chat interface
		

	protected static void submitReply(String selectedItem, String content, TreeView<String> treeView) {
		if (selectedItem == null || content.isEmpty()) return;
		
		try {
			int threadId = 0;
			int parentReplyId = 0;
			
			if (selectedItem.startsWith("[Thread-")) {
				threadId = Integer.parseInt(selectedItem.substring(8, selectedItem.indexOf("]")));
			} else if (selectedItem.startsWith("[Reply-")) {
				parentReplyId = Integer.parseInt(selectedItem.substring(7, selectedItem.indexOf("]")));
				// We need the threadId of the parent to associate it properly, but for simplicity, 
				// if replying to a reply, we extract thread ID from the tree hierarchy in the View.
				// (The View passes the threadId directly below)
			} else {
				return; // Root node selected
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
	

	// Renders the Ed Discussion-style feed on the right side
		protected static void renderThreadView(int threadId, javafx.scene.layout.VBox container) {
			container.getChildren().clear(); // Clear previous view
			
			String threadData = theDatabase.getThread(threadId);
			if (threadData == null) return;
			
			String[] tParts = threadData.split("\\|");
			String title = tParts[1].trim();
			String topic = tParts[2].trim();
			String creator = tParts[3].trim();
			
			// --- 1. Build Original Thread UI ---
			javafx.scene.layout.VBox threadBox = new javafx.scene.layout.VBox(8);
			// Light blue-ish/gray background for the main post, like Ed Discussions
			threadBox.setStyle("-fx-background-color: #f4f6f8; -fx-padding: 15; -fx-border-color: #d1d5db; -fx-border-width: 0 0 2 0;");
			
			javafx.scene.control.Label lblTitle = new javafx.scene.control.Label(title);
			lblTitle.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 22));
			lblTitle.setTextFill(javafx.scene.paint.Color.web("#1f2937"));
			
			javafx.scene.control.Label lblCreator = new javafx.scene.control.Label("Posted by: " + creator);
			lblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontPosture.ITALIC, 14));
			lblCreator.setTextFill(javafx.scene.paint.Color.web("#6b7280"));
			
			javafx.scene.control.Label lblTopic = new javafx.scene.control.Label(topic);
			lblTopic.setFont(javafx.scene.text.Font.font("Arial", 16));
			lblTopic.setWrapText(true);
			
			threadBox.getChildren().addAll(lblTitle, lblCreator, lblTopic);
			container.getChildren().add(threadBox);
			
			// --- 2. Build Replies UI ---
			java.util.List<String> replies = theDatabase.getRepliesForThread(threadId);
			
			for (String replyData : replies) {
				String[] rParts = replyData.split("\\|");
				int replyId = Integer.parseInt(rParts[0].trim());
				int parentId = Integer.parseInt(rParts[1].trim());
				String content = rParts[2].trim();
				String rCreator = rParts[3].trim();
				
				javafx.scene.layout.VBox replyBox = new javafx.scene.layout.VBox(5);
				
				// Indent nested replies heavily, keep direct replies flush
				double indent = (parentId == 0) ? 15 : 50; 
				
				// White background with bottom border for clean separation
				replyBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 15 15 15 " + indent + "; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
				
				javafx.scene.control.Label rLblCreator = new javafx.scene.control.Label(rCreator + " (Reply #" + replyId + ")");
				rLblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
				rLblCreator.setTextFill(javafx.scene.paint.Color.web("#374151"));
				
				javafx.scene.control.Label rLblContent = new javafx.scene.control.Label(content);
				rLblContent.setFont(javafx.scene.text.Font.font("Arial", 15));
				rLblContent.setWrapText(true);
				
				replyBox.getChildren().addAll(rLblCreator, rLblContent);
				container.getChildren().add(replyBox);
			}
		}
		
		// Renders the Direct Message chat interface (WhatsApp Style)
		protected static void renderDirectMessages(String currentUser, String staffUser, javafx.scene.layout.VBox container) {
			container.getChildren().clear();
			// WhatsApp Web background color
			container.setStyle("-fx-background-color: #efeae2; -fx-padding: 15;"); 
			
			// Chat Header
			javafx.scene.layout.VBox headerBox = new javafx.scene.layout.VBox(5);
			headerBox.setStyle("-fx-background-color: #f0f2f5; -fx-padding: 10; -fx-border-color: #d1d5db; -fx-border-width: 0 0 1 0;");
			headerBox.setAlignment(javafx.geometry.Pos.CENTER);
			
			javafx.scene.control.Label lblTitle = new javafx.scene.control.Label("Chat with " + staffUser);
			lblTitle.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
			headerBox.getChildren().add(lblTitle);
			container.getChildren().add(headerBox);
			
			// Load Messages
			java.util.List<String> messages = theDatabase.getDirectMessages(currentUser, staffUser);
			
			for (String msgData : messages) {
				String[] parts = msgData.split("\\|");
				String sender = parts[0].trim();
				String content = parts[1].trim();
				
				javafx.scene.layout.HBox row = new javafx.scene.layout.HBox();
				row.setPadding(new javafx.geometry.Insets(5, 0, 5, 0));
				
				javafx.scene.layout.VBox bubble = new javafx.scene.layout.VBox();
				bubble.setMaxWidth(260); // Keeps bubbles from stretching all the way across
				
				javafx.scene.control.Label lblContent = new javafx.scene.control.Label(content);
				lblContent.setFont(javafx.scene.text.Font.font("Arial", 14));
				lblContent.setWrapText(true);
				
				if (sender.equals(currentUser)) {
					// My message: Align Right, Green Bubble (WhatsApp style)
					row.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
					bubble.setStyle("-fx-background-color: #d9fdd3; -fx-padding: 10; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 1, 1);");
					lblContent.setTextFill(javafx.scene.paint.Color.BLACK);
				} else {
					// Their message: Align Left, White Bubble
					row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
					bubble.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 1, 1);");
					lblContent.setTextFill(javafx.scene.paint.Color.BLACK);
				}
				
				bubble.getChildren().add(lblContent);
				row.getChildren().add(bubble);
				container.getChildren().add(row);
			}
		}

		// Executes a Direct Message and instantly refreshes the chat
		protected static void executeDirectMessageDB(String sender, String receiver, String content, javafx.scene.layout.VBox container) {
			try {
				theDatabase.createDirectMessage(sender, receiver, content);
				renderDirectMessages(sender, receiver, container); // INSTANT REFRESH
			} catch (Exception e) { e.printStackTrace(); }
		}
}
