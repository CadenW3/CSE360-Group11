package guiRole1;

import database.Database;

/*******
 * <p> Title: ControllerRole1Home Class. </p>
 * * <p> Description: The Java/FX-based Role 1 Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * * This page is a stub for establish future roles for the application.
 * * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation * */

public class ControllerRole1Home {

	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	private static Database theDatabase = applicationMain.FoundationsMain.database;
	protected static boolean filterMyPosts = false;
	protected static boolean filterUnread = false;

	/**
	 * Default constructor is not used.
	 */
	public ControllerRole1Home() {
	}

	protected static void refreshDiscussionTree(javafx.scene.control.TreeView<String> treeView, String userName, javafx.scene.control.Button btnMyPosts, javafx.scene.control.Button btnUnread) {
		try {
			int myPostsCount = 0;
			int unreadCount = 0;

			javafx.scene.control.TreeItem<String> hiddenRoot = new javafx.scene.control.TreeItem<>("Hidden");
			javafx.scene.control.TreeItem<String> discussionsRoot = new javafx.scene.control.TreeItem<>("Discussions");
			javafx.scene.control.TreeItem<String> questionsRoot = new javafx.scene.control.TreeItem<>("Questions");
			discussionsRoot.setExpanded(true); questionsRoot.setExpanded(true);

			for (String threadData : theDatabase.getThreadList()) {
				String[] tParts = threadData.split("\\|");
				if (tParts.length < 4) continue;
				int id = Integer.parseInt(tParts[0].trim());
				
				boolean isMine = tParts[3].trim().equals(userName);
				boolean isRead = theDatabase.hasReadPost(userName, id, "Discussion");
				if (isMine) myPostsCount++; if (!isRead) unreadCount++;

				if (filterMyPosts && !isMine) continue;
				if (filterUnread && isRead) continue;

				String display = "[Thread-" + id + "] " + (!isRead ? "(UNREAD) " : "") + tParts[1].trim();
				javafx.scene.control.TreeItem<String> node = new javafx.scene.control.TreeItem<>(display);
				
				java.util.List<String> replies = theDatabase.getRepliesForThread(id);
				java.util.Map<Integer, javafx.scene.control.TreeItem<String>> rNodes = new java.util.HashMap<>();
				for (String r : replies) {
					String[] p = r.split("\\|");
					String prefix = (Integer.parseInt(p[1].trim()) == 0) ? "[Post-" : "[Reply-";
					rNodes.put(Integer.parseInt(p[0].trim()), new javafx.scene.control.TreeItem<>(prefix + p[0].trim() + "] " + p[2].trim() + " (" + p[3].trim() + ")"));
				}
				for (String r : replies) {
					String[] p = r.split("\\|");
					int rId = Integer.parseInt(p[0].trim()); int pId = Integer.parseInt(p[1].trim());
					if (pId == 0) node.getChildren().add(rNodes.get(rId));
					else if (rNodes.containsKey(pId)) rNodes.get(pId).getChildren().add(rNodes.get(rId));
				}
				discussionsRoot.getChildren().add(node);
			}

			for (String qData : theDatabase.getQuestionList()) {
				String[] qParts = qData.split("\\|");
				if (qParts.length < 4) continue;
				int id = Integer.parseInt(qParts[0].trim());
				
				boolean isMine = qParts[3].trim().equals(userName);
				boolean isRead = theDatabase.hasReadPost(userName, id, "Question");
				if (isMine) myPostsCount++; if (!isRead) unreadCount++;

				if (filterMyPosts && !isMine) continue;
				if (filterUnread && isRead) continue;

				String display = "[Question-" + id + "] " + (!isRead ? "(UNREAD) " : "") + qParts[1].trim();
				javafx.scene.control.TreeItem<String> node = new javafx.scene.control.TreeItem<>(display);
				
				java.util.List<String> replies = theDatabase.getRepliesForQuestion(id);
				java.util.Map<Integer, javafx.scene.control.TreeItem<String>> rNodes = new java.util.HashMap<>();
				for (String r : replies) {
					String[] p = r.split("\\|");
					String prefix = (Integer.parseInt(p[1].trim()) == 0) ? "[Post-" : "[Reply-";
					rNodes.put(Integer.parseInt(p[0].trim()), new javafx.scene.control.TreeItem<>(prefix + p[0].trim() + "] " + p[2].trim() + " (" + p[3].trim() + ")"));
				}
				for (String r : replies) {
					String[] p = r.split("\\|");
					int rId = Integer.parseInt(p[0].trim()); int pId = Integer.parseInt(p[1].trim());
					if (pId == 0) node.getChildren().add(rNodes.get(rId));
					else if (rNodes.containsKey(pId)) rNodes.get(pId).getChildren().add(rNodes.get(rId));
				}
				questionsRoot.getChildren().add(node);
			}

			hiddenRoot.getChildren().addAll(discussionsRoot, questionsRoot);
			treeView.setRoot(hiddenRoot);
			treeView.setShowRoot(false);

			if (btnMyPosts != null) btnMyPosts.setText("My Posts (" + myPostsCount + ")");
			if (btnUnread != null) btnUnread.setText("Unread (" + unreadCount + ")");
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void renderPostView(int id, String type, javafx.scene.layout.VBox container) {
		try {
			container.getChildren().clear(); 
			String data = (type.equals("Discussion")) ? theDatabase.getThread(id) : theDatabase.getQuestion(id);
			if (data == null) return;
			String[] tParts = data.split("\\|");
			
			String dateStr = theDatabase.getTimestampStr(id, type.equals("Discussion") ? "DiscussionThreads" : "Questions");

			javafx.scene.layout.VBox threadBox = new javafx.scene.layout.VBox(8);
			threadBox.setStyle("-fx-background-color: #f4f6f8; -fx-padding: 15; -fx-border-color: #d1d5db; -fx-border-width: 0 0 2 0;");
			
			javafx.scene.control.Label lblTitle = new javafx.scene.control.Label(tParts[1].trim());
			lblTitle.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 22));
			lblTitle.setTextFill(javafx.scene.paint.Color.web("#1f2937"));
			
			javafx.scene.control.Label lblCreator = new javafx.scene.control.Label(type + " by: " + tParts[3].trim() + "  •  " + dateStr);
			lblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontPosture.ITALIC, 14));
			lblCreator.setTextFill(javafx.scene.paint.Color.web("#6b7280"));
			
			javafx.scene.control.Label lblTopic = new javafx.scene.control.Label(tParts[2].trim());
			lblTopic.setFont(javafx.scene.text.Font.font("Arial", 16));
			lblTopic.setWrapText(true);
			
			threadBox.getChildren().addAll(lblTitle, lblCreator, lblTopic);
			container.getChildren().add(threadBox);
			
			java.util.List<String> replies = (type.equals("Discussion")) ? theDatabase.getRepliesForThread(id) : theDatabase.getRepliesForQuestion(id);
			for (String replyData : replies) {
				String[] rParts = replyData.split("\\|");
				int rId = Integer.parseInt(rParts[0].trim());
				int parentId = Integer.parseInt(rParts[1].trim());
				
				javafx.scene.layout.VBox replyBox = new javafx.scene.layout.VBox(5);
				double indent = (parentId == 0) ? 15 : 50; 
				replyBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 15 15 15 " + indent + "; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
				
				// --- STAFF GRADING BUTTON ---
				javafx.scene.control.Button btnGrade = new javafx.scene.control.Button("Grade Post");
				btnGrade.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");
				
				// id is the thread ID, rId is the specific reply ID
				btnGrade.setOnAction(e -> {
				    showGradingDialog(rId, id, type, container);
				});
				
				replyBox.getChildren().add(btnGrade);
				
				String typeLabel = (parentId == 0) ? "Post #" : "Reply #";
				String rDateStr = theDatabase.getTimestampStr(rId, type.equals("Discussion") ? "Replies" : "QuestionReplies");

				javafx.scene.control.Label rLblCreator = new javafx.scene.control.Label(rParts[3].trim() + " (" + typeLabel + rId + ")  •  " + rDateStr);
				rLblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
				
				javafx.scene.control.Label rLblContent = new javafx.scene.control.Label(rParts[2].trim());
				rLblContent.setFont(javafx.scene.text.Font.font("Arial", 15));
				rLblContent.setWrapText(true);
				
				replyBox.getChildren().addAll(rLblCreator, rLblContent);

				// --- GRADING BADGE (Visible to Staff) ---
				if (type.equals("Discussion")) {
					javafx.scene.layout.HBox gradeBox = new javafx.scene.layout.HBox();
					gradeBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
					gradeBox.setStyle("-fx-padding: 5 10; -fx-background-radius: 15;");
					javafx.scene.layout.VBox.setMargin(gradeBox, new javafx.geometry.Insets(10, 0, 0, 0));
					
					String gradeData = theDatabase.getGrade(rId);
					javafx.scene.control.Label lblGrade = new javafx.scene.control.Label();
					lblGrade.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 12));
					
					if (gradeData != null) {
						String[] pts = gradeData.split("\\|");
						int total = Integer.parseInt(pts[0]) + Integer.parseInt(pts[1]) + Integer.parseInt(pts[2]);
						lblGrade.setText("Grade: " + total + "/30");
						gradeBox.setStyle(gradeBox.getStyle() + "-fx-background-color: #d1fae5;"); // Green
						lblGrade.setTextFill(javafx.scene.paint.Color.web("#065f46"));
					} else {
						lblGrade.setText("Grade: N/A");
						gradeBox.setStyle(gradeBox.getStyle() + "-fx-background-color: #f3f4f6;"); // Gray
						lblGrade.setTextFill(javafx.scene.paint.Color.web("#374151"));
					}
					
					gradeBox.getChildren().add(lblGrade);
					replyBox.getChildren().add(gradeBox);
				}

				container.getChildren().add(replyBox);
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void showGradingDialog(int replyId, int threadId, String type, javafx.scene.layout.VBox container) {
	    try {
	        // Create the custom dialog
	        javafx.scene.control.Dialog<int[]> dialog = new javafx.scene.control.Dialog<>();
	        dialog.setTitle("Grade Student Post");
	        dialog.setHeaderText("Enter scores for the three criteria (0-10 each)");

	        // Set the button types
	        javafx.scene.control.ButtonType submitButtonType = new javafx.scene.control.ButtonType("Submit Grade", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
	        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, javafx.scene.control.ButtonType.CANCEL);

	        // Create the grid and inputs
	        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
	        grid.setHgap(10);
	        grid.setVgap(10);
	        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

	        // Assuming each criteria is worth up to 10 points (totaling 30)
	        javafx.scene.control.Spinner<Integer> wordSpinner = new javafx.scene.control.Spinner<>(0, 10, 0);
	        javafx.scene.control.Spinner<Integer> qualitySpinner = new javafx.scene.control.Spinner<>(0, 10, 0);
	        javafx.scene.control.Spinner<Integer> timeSpinner = new javafx.scene.control.Spinner<>(0, 10, 0);

	        grid.add(new javafx.scene.control.Label("Word Count Score:"), 0, 0);
	        grid.add(wordSpinner, 1, 0);
	        grid.add(new javafx.scene.control.Label("Quality Score:"), 0, 1);
	        grid.add(qualitySpinner, 1, 1);
	        grid.add(new javafx.scene.control.Label("Timeliness Score:"), 0, 2);
	        grid.add(timeSpinner, 1, 2);

	        dialog.getDialogPane().setContent(grid);

	        // Convert the result to an array of integers when the submit button is clicked
	        dialog.setResultConverter(dialogButton -> {
	            if (dialogButton == submitButtonType) {
	                return new int[] { wordSpinner.getValue(), qualitySpinner.getValue(), timeSpinner.getValue() };
	            }
	            return null;
	        });

	        // Show the dialog and capture the result
	        java.util.Optional<int[]> result = dialog.showAndWait();

	        // If the teacher submitted the grade, save it to the DB and refresh the view
	        result.ifPresent(scores -> {
	            theDatabase.saveGrade(replyId, scores[0], scores[1], scores[2]);
	            
	            // Re-render the thread so the teacher sees the updated grade
	            renderPostView(threadId, type, container);
	        });
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	protected static void executeReplyDB(int id, int parentReplyId, String type, String content, String userName, javafx.scene.control.TreeView<String> tree, javafx.scene.layout.VBox container, javafx.scene.control.Button b1, javafx.scene.control.Button b2) {
		try {
			if (type.equals("Discussion")) theDatabase.createReply(id, parentReplyId, content, userName);
			else theDatabase.createQuestionReply(id, parentReplyId, content, userName);
			refreshDiscussionTree(tree, userName, b1, b2);
			renderPostView(id, type, container);
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void createNewThread(String title, String topic, String userName, javafx.scene.control.TreeView<String> tree, javafx.scene.control.Button b1, javafx.scene.control.Button b2) {
		try {
			theDatabase.createThread(title, topic, userName);
			refreshDiscussionTree(tree, userName, b1, b2);
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void createNewQuestion(String title, String topic, String userName, javafx.scene.control.TreeView<String> tree, javafx.scene.control.Button b1, javafx.scene.control.Button b2) {
		try {
			theDatabase.createQuestion(title, topic, userName);
			refreshDiscussionTree(tree, userName, b1, b2);
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void renderDirectMessages(String currentUser, String studentUser, javafx.scene.layout.VBox container) {
		try {
			container.getChildren().clear();
			container.setStyle("-fx-background-color: #ffffff; -fx-padding: 15;"); 
			javafx.scene.layout.VBox headerBox = new javafx.scene.layout.VBox(5);
			headerBox.setStyle("-fx-padding: 0 0 15 0; -fx-border-color: #d1d5db; -fx-border-width: 0 0 2 0;");
			javafx.scene.control.Label lblTitle = new javafx.scene.control.Label("Messages with " + studentUser);
			lblTitle.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 20));
			lblTitle.setTextFill(javafx.scene.paint.Color.web("#b31b1b")); 
			headerBox.getChildren().add(lblTitle);
			container.getChildren().add(headerBox);
			
			for (String msgData : theDatabase.getDirectMessages(currentUser, studentUser)) {
				String[] parts = msgData.split("\\|");
				javafx.scene.layout.VBox msgBox = new javafx.scene.layout.VBox(5);
				msgBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 15; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
				javafx.scene.layout.VBox.setMargin(msgBox, new javafx.geometry.Insets(10, 0, 0, 0));
				javafx.scene.control.Label lblCreator = new javafx.scene.control.Label(parts[0].trim());
				lblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
				javafx.scene.control.Label lblContent = new javafx.scene.control.Label(parts[1].trim());
				lblContent.setFont(javafx.scene.text.Font.font("Arial", 15));
				lblContent.setWrapText(true);
				msgBox.getChildren().addAll(lblCreator, lblContent);
				container.getChildren().add(msgBox);
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void executeDirectMessageDB(String sender, String receiver, String content, javafx.scene.layout.VBox container) {
		try {
			theDatabase.createDirectMessage(sender, receiver, content);
			renderDirectMessages(sender, receiver, container);
		} catch (Exception e) { e.printStackTrace(); }
	}

	/**********
	 * <p> Method: performUpdate() </p>
	 * * <p> Description: This method directs the user to the User Update Page so the user can change
	 * the user account attributes. </p>
	 * */
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole1Home.theStage, ViewRole1Home.theUser);
	}	

	/**********
	 * <p> Method: performLogout() </p>
	 * * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 * */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole1Home.theStage);
	}
	
	/**********
	 * <p> Method: performQuit() </p>
	 * * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 * */	
	protected static void performQuit() {
		System.exit(0);
	}
}