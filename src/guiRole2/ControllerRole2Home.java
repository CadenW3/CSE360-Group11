package guiRole2;

import database.Database;

/*******
 * <p> Title: ControllerRole2Home Class. </p>
 * * <p> Description: The Java/FX-based Role 2 Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * * This page is a stub for establish future roles for the application.
 * * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any 
 * of these methods to public.</p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation * */

public class ControllerRole2Home {

	private static Database theDatabase = applicationMain.FoundationsMain.database;
	protected static boolean filterMyPosts = false;
	protected static boolean filterUnread = false;
	
	protected static String currentFilterKeyword = "";
	protected static String currentFilterType = "";

	public ControllerRole2Home() {
	}
	
	protected static void refreshDiscussionTree(javafx.scene.control.TreeView<String> treeView, String userName, javafx.scene.control.Button btnMyPosts, javafx.scene.control.Button btnUnread) {
		try {
			theDatabase.getOrCreateGeneralThread(); 

			managers.PostCollection postManager = new managers.PostCollection(theDatabase);
			managers.ReplyCollection replyManager = new managers.ReplyCollection(theDatabase);
			postManager.loadAllPosts();

			int myPostsCount = 0;
			int unreadCount = 0;

			javafx.scene.control.TreeItem<String> hiddenRoot = new javafx.scene.control.TreeItem<>("Hidden");
			javafx.scene.control.TreeItem<String> discussionsRoot = new javafx.scene.control.TreeItem<>("Discussions");
			javafx.scene.control.TreeItem<String> questionsRoot = new javafx.scene.control.TreeItem<>("Questions");
			discussionsRoot.setExpanded(true); questionsRoot.setExpanded(true);

			for (entityClasses.Post post : postManager.getAllPosts()) {
				boolean isMine = post.getAuthor().equalsIgnoreCase(userName);
				boolean isRead = theDatabase.hasReadPost(userName, post.getId(), post.getType());
				if (isMine) myPostsCount++; 
				if (!isRead) unreadCount++;
			}

			//Disable basic filter to use advanced search
			postManager.filterPosts("", currentFilterType.isEmpty() ? null : currentFilterType, filterMyPosts, filterUnread, userName);

			for (entityClasses.Post post : postManager.getFilteredSubset()) {
				//Load replies for the post
				replyManager.loadRepliesForPost(post.getId(), post.getType());
				java.util.List<entityClasses.Reply> activeReplies = replyManager.getActiveSubset();

				boolean matchFound = false;
				//Determine if search is active
				boolean searchActive = (currentFilterKeyword != null && !currentFilterKeyword.trim().isEmpty());

				//Check if keyword is empty
				if (!searchActive) {
					matchFound = true;
				} else {
					// Convert to lowercase and remove accidental spaces at the very end
					String kw = currentFilterKeyword.toLowerCase().trim();
					
					//Check title and topic using indexOf to guarantee exact character sequence match
					if (post.getTitle().toLowerCase().indexOf(kw) != -1 || 
					    post.getTopic().toLowerCase().indexOf(kw) != -1) {
						matchFound = true;
					} else {
						//Check all replies for the exact character sequence
						for (entityClasses.Reply r : activeReplies) {
							if (r.getContent().toLowerCase().indexOf(kw) != -1) {
								matchFound = true;
								break;
							}
						}
					}
				}

				//Skip if no match
				if (!matchFound) continue;

				if (post.getTitle().equals("[Deleted]") && activeReplies.isEmpty()) {
					postManager.deletePost(post.getId(), post.getType());
					continue;
				}

				boolean isRead = theDatabase.hasReadPost(userName, post.getId(), post.getType());
				
				//Set prefix type
				String prefixType = post.getType().equals("Discussion") ? "Thread" : "Question";
				String display = "[" + prefixType + "-" + post.getId() + "] " + (!isRead ? "(UNREAD) " : "") + post.getTitle();
				
				javafx.scene.control.TreeItem<String> node = new javafx.scene.control.TreeItem<>(display);
				
				//Expand node if search is active
				if (searchActive) {
					node.setExpanded(true);
				}
				
				java.util.Map<Integer, javafx.scene.control.TreeItem<String>> rNodes = new java.util.HashMap<>();
				int postCounter = 1;
				java.util.Map<Integer, Integer> replyCounters = new java.util.HashMap<>();
				
				//Map replies
				for (entityClasses.Reply r : activeReplies) {
					String prefix;
					String localLabel;
					if (r.getParentId() == 0) {
						prefix = "[Post-" + r.getId() + "]";
						localLabel = "Post " + (postCounter++);
					} else {
						prefix = "[Reply-" + r.getId() + "]";
						int c = replyCounters.getOrDefault(r.getParentId(), 1);
						localLabel = "Reply " + c;
						replyCounters.put(r.getParentId(), c + 1);
					}
					
					javafx.scene.control.TreeItem<String> replyNode = new javafx.scene.control.TreeItem<>(prefix + " " + localLabel + ": " + r.getContent() + " (" + r.getAuthor() + ")");
					
					//Expand reply node if search is active
					if (searchActive) {
						replyNode.setExpanded(true);
					}
					
					rNodes.put(r.getId(), replyNode);
				}
				for (entityClasses.Reply r : activeReplies) {
					if (r.getParentId() == 0) node.getChildren().add(rNodes.get(r.getId()));
					else if (rNodes.containsKey(r.getParentId())) rNodes.get(r.getParentId()).getChildren().add(rNodes.get(r.getId()));
				}

				//Add root node
				if (post.getType().equals("Discussion")) {
					discussionsRoot.getChildren().add(node);
				} else {
					questionsRoot.getChildren().add(node);
				}
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
			
			managers.PostCollection postManager = new managers.PostCollection(theDatabase);
			managers.ReplyCollection replyManager = new managers.ReplyCollection(theDatabase);
			postManager.loadAllPosts();
			
			entityClasses.Post tempPost = null;
			for (entityClasses.Post p : postManager.getAllPosts()) {
				if (p.getId() == id && p.getType().equals(type)) {
					tempPost = p;
					break;
				}
			}
			if (tempPost == null) return;
			
			final entityClasses.Post post = tempPost;
			
			replyManager.loadRepliesForPost(id, type);
			java.util.List<entityClasses.Reply> replies = replyManager.getActiveSubset();
			
			String dateStr = theDatabase.getTimestampStr(id, type.equals("Discussion") ? "DiscussionThreads" : "Questions");
			String currentUser = ViewRole2Home.theUser.getUserName();

			javafx.scene.layout.VBox threadBox = new javafx.scene.layout.VBox(8);
			threadBox.setStyle("-fx-background-color: #f4f6f8; -fx-padding: 15; -fx-border-color: #d1d5db; -fx-border-width: 0 0 2 0;");
			
			javafx.scene.layout.HBox headerLayout = new javafx.scene.layout.HBox(10);
			headerLayout.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
			
			javafx.scene.control.Label lblTitle = new javafx.scene.control.Label(post.getTitle());
			lblTitle.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 22));
			lblTitle.setTextFill(javafx.scene.paint.Color.web("#1f2937"));
			
			javafx.scene.control.Label lblCreator = new javafx.scene.control.Label(type + " by: " + post.getAuthor() + "  •  " + dateStr);
			lblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontPosture.ITALIC, 14));
			lblCreator.setTextFill(javafx.scene.paint.Color.web("#6b7280"));
			
			headerLayout.getChildren().addAll(lblTitle, lblCreator);

			if (post.getAuthor().equalsIgnoreCase(currentUser) && !post.getTopic().equals("[This post was deleted.]")) {
				javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("Edit");
				btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5;");
				btnEdit.setOnAction(e -> showEditThreadDialog(id, type, post.getTitle(), post.getTopic(), container));
				headerLayout.getChildren().add(btnEdit);
			}
			
			javafx.scene.control.Label lblTopic = new javafx.scene.control.Label(post.getTopic());
			lblTopic.setFont(javafx.scene.text.Font.font("Arial", 16));
			lblTopic.setWrapText(true);
			lblTopic.prefWidthProperty().bind(container.widthProperty().subtract(30)); 
			
			threadBox.getChildren().addAll(headerLayout, lblTopic);
			container.getChildren().add(threadBox);
			
			int postCounter = 1;
			java.util.Map<Integer, Integer> replyCounters = new java.util.HashMap<>();
			
			for (entityClasses.Reply r : replies) {
				javafx.scene.layout.VBox replyBox = new javafx.scene.layout.VBox(5);
				double indent = (r.getParentId() == 0) ? 15 : 50; 
				replyBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 15 15 15 " + indent + "; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
				
				javafx.scene.layout.HBox rHeaderLayout = new javafx.scene.layout.HBox(10);
				rHeaderLayout.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				
				String typeLabel;
				if (r.getParentId() == 0) {
					typeLabel = "Post #" + (postCounter++);
				} else {
					int c = replyCounters.getOrDefault(r.getParentId(), 1);
					typeLabel = "Reply #" + c;
					replyCounters.put(r.getParentId(), c + 1);
				}
				
				String rDateStr = theDatabase.getTimestampStr(r.getId(), type.equals("Discussion") ? "Replies" : "QuestionReplies");

				javafx.scene.control.Label rLblCreator = new javafx.scene.control.Label(r.getAuthor() + " (" + typeLabel + ")  •  " + rDateStr);
				rLblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
				rHeaderLayout.getChildren().add(rLblCreator);
				
				boolean isMyReply = r.getAuthor().equalsIgnoreCase(currentUser);
				
				if (isMyReply && !r.getContent().equals("[This post was deleted.]")) {

					javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("Edit");
					btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 5; -fx-font-size: 11px;");
					btnEdit.setOnAction(e -> showEditReplyDialog(r.getId(), type, r.getContent(), id, container));
					rHeaderLayout.getChildren().add(btnEdit);

					javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("Delete");
					btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 5; -fx-font-size: 11px;");
					btnDelete.setOnAction(e -> {
						javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this reply?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
						alert.initOwner(ViewRole2Home.theStage);
						alert.showAndWait().ifPresent(response -> {
							if (response == javafx.scene.control.ButtonType.YES) {
								try {
									replyManager.deleteReply(r.getId(), type, id);
								} catch (Exception ex) { ex.printStackTrace(); }
								
								refreshDiscussionTree(ViewRole2Home.tree_Discussions, currentUser, ViewRole2Home.button_FilterMyPosts, ViewRole2Home.button_FilterUnread);
								renderPostView(id, type, container);
							}
						});
					});
					rHeaderLayout.getChildren().add(btnDelete);
				}

				javafx.scene.control.Label rLblContent = new javafx.scene.control.Label(r.getContent());
				rLblContent.setFont(javafx.scene.text.Font.font("Arial", 15));
				rLblContent.setWrapText(true);
				rLblContent.prefWidthProperty().bind(container.widthProperty().subtract(indent + 40));
				
				replyBox.getChildren().addAll(rHeaderLayout, rLblContent);

				if (type.equals("Discussion") && isMyReply) {
					javafx.scene.layout.HBox gradeBox = new javafx.scene.layout.HBox();
					gradeBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
					gradeBox.setStyle("-fx-padding: 5 10; -fx-background-radius: 15;");
					javafx.scene.layout.VBox.setMargin(gradeBox, new javafx.geometry.Insets(10, 0, 0, 0));
					
					String gradeData = theDatabase.getGrade(r.getId());
					javafx.scene.control.Label lblGrade = new javafx.scene.control.Label();
					lblGrade.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 12));
					
					if (gradeData != null) {
						String[] pts = gradeData.split("\\|");
						int total = Integer.parseInt(pts[0]) + Integer.parseInt(pts[1]) + Integer.parseInt(pts[2]);
						lblGrade.setText("Grade: " + total + "/30");
						gradeBox.setStyle(gradeBox.getStyle() + "-fx-background-color: #d1fae5;");
						lblGrade.setTextFill(javafx.scene.paint.Color.web("#065f46"));
					} else {
						lblGrade.setText("Grade: N/A");
						gradeBox.setStyle(gradeBox.getStyle() + "-fx-background-color: #f3f4f6;");
						lblGrade.setTextFill(javafx.scene.paint.Color.web("#374151"));
					}
					
					gradeBox.getChildren().add(lblGrade);
					replyBox.getChildren().add(gradeBox);
				}

				container.getChildren().add(replyBox);
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void showEditThreadDialog(int id, String type, String oldTitle, String oldTopic, javafx.scene.layout.VBox container) {
		javafx.scene.control.Dialog<String[]> dialog = new javafx.scene.control.Dialog<>();
		dialog.initOwner(ViewRole2Home.theStage);
		dialog.setTitle("Edit " + type);
		javafx.scene.control.ButtonType saveBtnType = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, javafx.scene.control.ButtonType.CANCEL);

		javafx.scene.layout.VBox vBox = new javafx.scene.layout.VBox(10);
		javafx.scene.control.TextField titleF = new javafx.scene.control.TextField(oldTitle);
		javafx.scene.control.TextArea topicF = new javafx.scene.control.TextArea(oldTopic);
		topicF.setPrefRowCount(3);
		vBox.getChildren().addAll(new javafx.scene.control.Label("Title:"), titleF, new javafx.scene.control.Label("Content:"), topicF);
		dialog.getDialogPane().setContent(vBox);

		dialog.setResultConverter(btn -> {
			if (btn == saveBtnType) return new String[]{titleF.getText(), topicF.getText()};
			return null;
		});

		dialog.showAndWait().ifPresent(res -> {
			try {
				managers.PostCollection postManager = new managers.PostCollection(theDatabase);
				postManager.updatePost(id, type, res[0], res[1]);
			} catch (Exception e) { e.printStackTrace(); }
			refreshDiscussionTree(ViewRole2Home.tree_Discussions, ViewRole2Home.theUser.getUserName(), ViewRole2Home.button_FilterMyPosts, ViewRole2Home.button_FilterUnread);
			renderPostView(id, type, container);
		});
	}

	protected static void showEditReplyDialog(int rId, String type, String oldContent, int threadId, javafx.scene.layout.VBox container) {
		javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(oldContent);
		dialog.initOwner(ViewRole2Home.theStage);
		dialog.setTitle("Edit Reply");
		dialog.setHeaderText("Update your reply:");
		dialog.showAndWait().ifPresent(newContent -> {
			managers.ReplyCollection replyManager = new managers.ReplyCollection(theDatabase);
			replyManager.updateReply(rId, type, newContent, threadId);
			refreshDiscussionTree(ViewRole2Home.tree_Discussions, ViewRole2Home.theUser.getUserName(), ViewRole2Home.button_FilterMyPosts, ViewRole2Home.button_FilterUnread);
			renderPostView(threadId, type, container);
		});
	}

	protected static void executeReplyDB(int id, int parentReplyId, String type, String content, String userName, javafx.scene.control.TreeView<String> tree, javafx.scene.layout.VBox container, javafx.scene.control.Button b1, javafx.scene.control.Button b2) {
		try {
			managers.ReplyCollection replyManager = new managers.ReplyCollection(theDatabase);
			replyManager.createReply(id, parentReplyId, type, content, userName);
			
            try { theDatabase.markPostAsRead(userName, id, type); } catch(Exception e){}
			refreshDiscussionTree(tree, userName, b1, b2);
			renderPostView(id, type, container);
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void createNewQuestion(String title, String topic, String userName, javafx.scene.control.TreeView<String> tree, javafx.scene.control.Button b1, javafx.scene.control.Button b2) {
		try {
			managers.PostCollection postManager = new managers.PostCollection(theDatabase);
			postManager.createPost("Question", title, topic, userName);
			
            postManager.loadAllPosts();
            int maxId = -1;
            for(entityClasses.Post p : postManager.getAllPosts()) {
                if(p.getType().equals("Question") && p.getAuthor().equals(userName) && p.getId() > maxId) {
                    maxId = p.getId();
                }
            }
            if(maxId != -1) {
                try { theDatabase.markPostAsRead(userName, maxId, "Question"); } catch(Exception e){}
            }

			refreshDiscussionTree(tree, userName, b1, b2);
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void renderDirectMessages(String currentUser, String staffUser, javafx.scene.layout.VBox container) {
		try {
			container.getChildren().clear();
			container.setStyle("-fx-background-color: #ffffff; -fx-padding: 15;"); 
			javafx.scene.layout.VBox headerBox = new javafx.scene.layout.VBox(5);
			headerBox.setStyle("-fx-padding: 0 0 15 0; -fx-border-color: #d1d5db; -fx-border-width: 0 0 2 0;");
			javafx.scene.control.Label lblTitle = new javafx.scene.control.Label("Messages from " + staffUser);
			lblTitle.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 20));
			lblTitle.setTextFill(javafx.scene.paint.Color.web("#b31b1b")); 
			headerBox.getChildren().add(lblTitle);
			container.getChildren().add(headerBox);
			
			for (String msgData : theDatabase.getDirectMessages(currentUser, staffUser)) {
				String[] parts = msgData.split("\\|");
				javafx.scene.layout.VBox msgBox = new javafx.scene.layout.VBox(5);
				msgBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 15; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
				javafx.scene.layout.VBox.setMargin(msgBox, new javafx.geometry.Insets(10, 0, 0, 0));
				javafx.scene.control.Label lblCreator = new javafx.scene.control.Label(parts[0].trim());
				lblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
				javafx.scene.control.Label lblContent = new javafx.scene.control.Label(parts[1].trim());
				lblContent.setFont(javafx.scene.text.Font.font("Arial", 15));
				lblContent.setWrapText(true);
				lblContent.prefWidthProperty().bind(container.widthProperty().subtract(60));
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

	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole2Home.theStage, ViewRole2Home.theUser);
	}	

	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole2Home.theStage);
	}
	
	protected static void performQuit() {
		System.exit(0);
	}
}