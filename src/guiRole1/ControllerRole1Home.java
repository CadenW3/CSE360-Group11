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
	
	protected static String currentFilterKeyword = "";
	protected static String currentFilterType = "";

	/**
	 * Default constructor is not used.
	 */
	public ControllerRole1Home() {
	}

	// Helper method: Recursively cleans up any soft-deleted posts that no longer have active replies under them
	private static java.util.List<String> cleanupOrphanedDeletes(java.util.List<String> replies, String type) {
		boolean changed = true;
		while (changed) {
			changed = false;
			java.util.List<String> toKeep = new java.util.ArrayList<>();
			for (String replyData : replies) {
				String[] rParts = replyData.split("\\|");
				int rId = Integer.parseInt(rParts[0].trim());
				String content = rParts[2].trim();
				
				boolean hasChildren = false;
				for (String checkR : replies) {
					int parentId = Integer.parseInt(checkR.split("\\|")[1].trim());
					if (parentId == rId) { hasChildren = true; break; }
				}
				
				if (content.equals("[This post was deleted.]") && !hasChildren) {
					try {
						if (type.equals("Discussion")) {
							theDatabase.deleteReply(rId);
						} else {
							theDatabase.deleteQuestionReply(rId);
						}
					} catch (Exception e) { e.printStackTrace(); }
					changed = true; 
				} else {
					toKeep.add(replyData);
				}
			}
			replies = toKeep;
		}
		return replies;
	}

	protected static void refreshDiscussionTree(javafx.scene.control.TreeView<String> treeView, String userName, javafx.scene.control.Button btnMyPosts, javafx.scene.control.Button btnUnread) {
		try {
			int myPostsCount = 0;
			int unreadCount = 0;
			
			theDatabase.getOrCreateGeneralThread();

			javafx.scene.control.TreeItem<String> hiddenRoot = new javafx.scene.control.TreeItem<>("Hidden");
			javafx.scene.control.TreeItem<String> discussionsRoot = new javafx.scene.control.TreeItem<>("Discussions");
			javafx.scene.control.TreeItem<String> questionsRoot = new javafx.scene.control.TreeItem<>("Questions");
			discussionsRoot.setExpanded(true); questionsRoot.setExpanded(true);

			for (String threadData : theDatabase.getThreadList()) {
				String[] tParts = threadData.split("\\|");
				if (tParts.length < 4) continue;
				int id = Integer.parseInt(tParts[0].trim());
				
				java.util.List<String> replies = cleanupOrphanedDeletes(theDatabase.getRepliesForThread(id), "Discussion");
				if (tParts[1].trim().equals("[Deleted]") && replies.isEmpty()) {
					try { theDatabase.deleteThread(id); } catch(Exception e) {}
					continue;
				}
				
				if (currentFilterType.equals("Discussion") && !currentFilterKeyword.isEmpty()) {
					if (!tParts[1].toLowerCase().contains(currentFilterKeyword.toLowerCase()) && 
						!tParts[2].toLowerCase().contains(currentFilterKeyword.toLowerCase())) { continue; }
				}
				
				boolean isMine = tParts[3].trim().equals(userName);
				boolean isRead = theDatabase.hasReadPost(userName, id, "Discussion");
				if (isMine) myPostsCount++; if (!isRead) unreadCount++;

				if (filterMyPosts && !isMine) continue;
				if (filterUnread && isRead) continue;

				String display = "[Thread-" + id + "] " + (!isRead ? "(UNREAD) " : "") + tParts[1].trim();
				javafx.scene.control.TreeItem<String> node = new javafx.scene.control.TreeItem<>(display);
				
				java.util.Map<Integer, javafx.scene.control.TreeItem<String>> rNodes = new java.util.HashMap<>();
				int postCounter = 1;
				java.util.Map<Integer, Integer> replyCounters = new java.util.HashMap<>();
				for (String r : replies) {
					String[] p = r.split("\\|");
					int rId = Integer.parseInt(p[0].trim());
					int pId = Integer.parseInt(p[1].trim());
					
					String prefix; String localLabel;
					if (pId == 0) {
						prefix = "[Post-" + rId + "]";
						localLabel = "Post " + (postCounter++);
					} else {
						prefix = "[Reply-" + rId + "]";
						int c = replyCounters.getOrDefault(pId, 1);
						localLabel = "Reply " + c;
						replyCounters.put(pId, c + 1);
					}
					rNodes.put(rId, new javafx.scene.control.TreeItem<>(prefix + " " + localLabel + ": " + p[2].trim() + " (" + p[3].trim() + ")"));
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
				
				java.util.List<String> replies = cleanupOrphanedDeletes(theDatabase.getRepliesForQuestion(id), "Question");
				if (qParts[1].trim().equals("[Deleted]") && replies.isEmpty()) {
					try { theDatabase.deleteQuestion(id); } catch(Exception e) {}
					continue;
				}
				
				if (currentFilterType.equals("Question") && !currentFilterKeyword.isEmpty()) {
					if (!qParts[1].toLowerCase().contains(currentFilterKeyword.toLowerCase()) && 
						!qParts[2].toLowerCase().contains(currentFilterKeyword.toLowerCase())) { continue; }
				}
				
				boolean isMine = qParts[3].trim().equals(userName);
				boolean isRead = theDatabase.hasReadPost(userName, id, "Question");
				if (isMine) myPostsCount++; if (!isRead) unreadCount++;

				if (filterMyPosts && !isMine) continue;
				if (filterUnread && isRead) continue;

				String display = "[Question-" + id + "] " + (!isRead ? "(UNREAD) " : "") + qParts[1].trim();
				javafx.scene.control.TreeItem<String> node = new javafx.scene.control.TreeItem<>(display);
				
				java.util.Map<Integer, javafx.scene.control.TreeItem<String>> rNodes = new java.util.HashMap<>();
				int postCounter = 1;
				java.util.Map<Integer, Integer> replyCounters = new java.util.HashMap<>();
				for (String r : replies) {
					String[] p = r.split("\\|");
					int rId = Integer.parseInt(p[0].trim());
					int pId = Integer.parseInt(p[1].trim());
					
					String prefix; String localLabel;
					if (pId == 0) {
						prefix = "[Post-" + rId + "]";
						localLabel = "Post " + (postCounter++);
					} else {
						prefix = "[Reply-" + rId + "]";
						int c = replyCounters.getOrDefault(pId, 1);
						localLabel = "Reply " + c;
						replyCounters.put(pId, c + 1);
					}
					rNodes.put(rId, new javafx.scene.control.TreeItem<>(prefix + " " + localLabel + ": " + p[2].trim() + " (" + p[3].trim() + ")"));
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
			String currentUser = ViewRole1Home.theUser.getUserName();
			java.util.List<String> students = theDatabase.getStudentUsers();
			
			java.util.List<String> replies = (type.equals("Discussion")) ? theDatabase.getRepliesForThread(id) : theDatabase.getRepliesForQuestion(id);
			replies = cleanupOrphanedDeletes(replies, type);

			javafx.scene.layout.VBox threadBox = new javafx.scene.layout.VBox(8);
			threadBox.setStyle("-fx-background-color: #f4f6f8; -fx-padding: 15; -fx-border-color: #d1d5db; -fx-border-width: 0 0 2 0;");
			
			javafx.scene.layout.HBox headerLayout = new javafx.scene.layout.HBox(10);
			headerLayout.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
			
			javafx.scene.control.Label lblTitle = new javafx.scene.control.Label(tParts[1].trim());
			lblTitle.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 22));
			lblTitle.setTextFill(javafx.scene.paint.Color.web("#1f2937"));
			
			javafx.scene.control.Label lblCreator = new javafx.scene.control.Label(type + " by: " + tParts[3].trim() + "  •  " + dateStr);
			lblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontPosture.ITALIC, 14));
			lblCreator.setTextFill(javafx.scene.paint.Color.web("#6b7280"));
			
			headerLayout.getChildren().addAll(lblTitle, lblCreator);

			boolean threadOwner = tParts[3].trim().equals(currentUser);
			boolean threadIsStudent = students.contains(tParts[3].trim());
			
			if ((threadOwner || threadIsStudent) && !tParts[2].trim().equals("[This post was deleted.]")) {
				javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("Edit");
				btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5;");
				btnEdit.setOnAction(e -> showEditThreadDialog(id, type, tParts[1].trim(), tParts[2].trim(), container));
				
				javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("Delete Thread");
				btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5;");
				btnDelete.setOnAction(e -> {
					javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION, "Delete this entire thread and all replies?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
					alert.showAndWait().ifPresent(res -> {
						if (res == javafx.scene.control.ButtonType.YES) {
							try {
								if (type.equals("Discussion")) {
									theDatabase.deleteThread(id);
									theDatabase.deleteAllRepliesForThread(id);
								} else {
									theDatabase.deleteQuestion(id);
									theDatabase.deleteAllRepliesForQuestion(id);
								}
								container.getChildren().clear();
								refreshDiscussionTree(ViewRole1Home.tree_Discussions, currentUser, ViewRole1Home.button_FilterMyPosts, ViewRole1Home.button_FilterUnread);
							} catch (Exception ex) { ex.printStackTrace(); }
						}
					});
				});
				headerLayout.getChildren().addAll(btnEdit, btnDelete);
			}
			
			javafx.scene.control.Label lblTopic = new javafx.scene.control.Label(tParts[2].trim());
			lblTopic.setFont(javafx.scene.text.Font.font("Arial", 16));
			lblTopic.setWrapText(true);
			lblTopic.prefWidthProperty().bind(container.widthProperty().subtract(30)); 
			
			threadBox.getChildren().addAll(headerLayout, lblTopic);
			container.getChildren().add(threadBox);
			
			int postCounter = 1;
			java.util.Map<Integer, Integer> replyCounters = new java.util.HashMap<>();
			
			for (String replyData : replies) {
				String[] rParts = replyData.split("\\|");
				int rId = Integer.parseInt(rParts[0].trim());
				int parentId = Integer.parseInt(rParts[1].trim());
				
				javafx.scene.layout.VBox replyBox = new javafx.scene.layout.VBox(5);
				double indent = (parentId == 0) ? 15 : 50; 
				replyBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 15 15 15 " + indent + "; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
				
				javafx.scene.layout.HBox rHeaderLayout = new javafx.scene.layout.HBox(10);
				rHeaderLayout.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				
				String typeLabel = (parentId == 0) ? "Post #" + (postCounter++) : "Reply #" + (replyCounters.merge(parentId, 1, Integer::sum));
				String rDateStr = theDatabase.getTimestampStr(rId, type.equals("Discussion") ? "Replies" : "QuestionReplies");

				javafx.scene.control.Label rLblCreator = new javafx.scene.control.Label(rParts[3].trim() + " (" + typeLabel + ")  •  " + rDateStr);
				rLblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
				rHeaderLayout.getChildren().add(rLblCreator);
				
				javafx.scene.layout.HBox actionBox = new javafx.scene.layout.HBox(5);
				
				boolean replyOwner = rParts[3].trim().equals(currentUser);
				boolean replyIsStudent = students.contains(rParts[3].trim());
				
				if ((replyOwner || replyIsStudent) && !rParts[2].trim().equals("[This post was deleted.]")) {
					boolean tempHasChildren = false;
					for (String checkR : replies) {
						if (Integer.parseInt(checkR.split("\\|")[1].trim()) == rId) { tempHasChildren = true; break; }
					}
					final boolean hasChildren = tempHasChildren;
					
					javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("Edit");
					btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 11px;");
					btnEdit.setOnAction(e -> showEditReplyDialog(rId, type, rParts[2].trim(), id, container));
					
					javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("Delete");
					btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 11px;");
					btnDelete.setOnAction(e -> {
						javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION, "Delete this reply?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
						alert.showAndWait().ifPresent(res -> {
							if (res == javafx.scene.control.ButtonType.YES) {
								try {
									if (hasChildren) {
										if (type.equals("Discussion")) { theDatabase.softDeleteReply(rId); } 
										else { theDatabase.softDeleteQuestionReply(rId); }
									} else {
										if (type.equals("Discussion")) { theDatabase.deleteReply(rId); } 
										else { theDatabase.deleteQuestionReply(rId); }
									}
								} catch (Exception ex) { ex.printStackTrace(); }
								refreshDiscussionTree(ViewRole1Home.tree_Discussions, currentUser, ViewRole1Home.button_FilterMyPosts, ViewRole1Home.button_FilterUnread);
								renderPostView(id, type, container);
							}
						});
					});
					actionBox.getChildren().addAll(btnEdit, btnDelete);
				}

				if (type.equals("Discussion") && replyIsStudent) {
					javafx.scene.control.Button btnGrade = new javafx.scene.control.Button("Grade");
					btnGrade.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 11px;");
					btnGrade.setOnAction(e -> showGradingDialog(rId, id, type, container));
					actionBox.getChildren().add(btnGrade);
				}
				
				if (!actionBox.getChildren().isEmpty()) rHeaderLayout.getChildren().add(actionBox);

				javafx.scene.control.Label rLblContent = new javafx.scene.control.Label(rParts[2].trim());
				rLblContent.setFont(javafx.scene.text.Font.font("Arial", 15));
				rLblContent.setWrapText(true);
				rLblContent.prefWidthProperty().bind(container.widthProperty().subtract(indent + 40));
				
				replyBox.getChildren().addAll(rHeaderLayout, rLblContent);

				if (type.equals("Discussion")) {
					String gradeData = theDatabase.getGrade(rId);
					if (gradeData != null) {
						javafx.scene.layout.HBox gradeBox = new javafx.scene.layout.HBox();
						gradeBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
						gradeBox.setStyle("-fx-padding: 5 10; -fx-background-radius: 15; -fx-background-color: #d1fae5;");
						javafx.scene.layout.VBox.setMargin(gradeBox, new javafx.geometry.Insets(10, 0, 0, 0));
						String[] pts = gradeData.split("\\|");
						int total = Integer.parseInt(pts[0]) + Integer.parseInt(pts[1]) + Integer.parseInt(pts[2]);
						javafx.scene.control.Label lblGrade = new javafx.scene.control.Label("Grade Given: " + total + "/30");
						lblGrade.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 12));
						lblGrade.setTextFill(javafx.scene.paint.Color.web("#065f46"));
						gradeBox.getChildren().add(lblGrade);
						replyBox.getChildren().add(gradeBox);
					}
				}

				container.getChildren().add(replyBox);
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void showEditThreadDialog(int id, String type, String oldTitle, String oldTopic, javafx.scene.layout.VBox container) {
		javafx.scene.control.Dialog<String[]> dialog = new javafx.scene.control.Dialog<>();
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
				if (type.equals("Discussion")) {
					theDatabase.updateThread(id, res[0], res[1]);
				} else {
					theDatabase.updateQuestion(id, res[0], res[1]);
				}
			} catch (Exception e) { e.printStackTrace(); }
			refreshDiscussionTree(ViewRole1Home.tree_Discussions, ViewRole1Home.theUser.getUserName(), ViewRole1Home.button_FilterMyPosts, ViewRole1Home.button_FilterUnread);
			renderPostView(id, type, container);
		});
	}

	protected static void showEditReplyDialog(int rId, String type, String oldContent, int threadId, javafx.scene.layout.VBox container) {
		javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(oldContent);
		dialog.setTitle("Edit Reply");
		dialog.setHeaderText("Update your reply:");
		dialog.showAndWait().ifPresent(newContent -> {
			if (type.equals("Discussion")) {
				theDatabase.updateReply(rId, newContent);
			} else {
				theDatabase.updateQuestionReply(rId, newContent);
			}
			refreshDiscussionTree(ViewRole1Home.tree_Discussions, ViewRole1Home.theUser.getUserName(), ViewRole1Home.button_FilterMyPosts, ViewRole1Home.button_FilterUnread);
			renderPostView(threadId, type, container);
		});
	}

	protected static void showGradingDialog(int replyId, int threadId, String type, javafx.scene.layout.VBox container) {
	    try {
	        javafx.scene.control.Dialog<int[]> dialog = new javafx.scene.control.Dialog<>();
	        dialog.setTitle("Grade Student Post");
	        dialog.setHeaderText("Enter scores for the three criteria (0-10 each)");

	        javafx.scene.control.ButtonType submitButtonType = new javafx.scene.control.ButtonType("Submit Grade", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
	        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, javafx.scene.control.ButtonType.CANCEL);

	        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
	        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

	        javafx.scene.control.Spinner<Integer> wordSpinner = new javafx.scene.control.Spinner<>(0, 10, 0);
	        javafx.scene.control.Spinner<Integer> qualitySpinner = new javafx.scene.control.Spinner<>(0, 10, 0);
	        javafx.scene.control.Spinner<Integer> timeSpinner = new javafx.scene.control.Spinner<>(0, 10, 0);

	        grid.add(new javafx.scene.control.Label("Word Count Score:"), 0, 0); grid.add(wordSpinner, 1, 0);
	        grid.add(new javafx.scene.control.Label("Quality Score:"), 0, 1); grid.add(qualitySpinner, 1, 1);
	        grid.add(new javafx.scene.control.Label("Timeliness Score:"), 0, 2); grid.add(timeSpinner, 1, 2);

	        dialog.getDialogPane().setContent(grid);
	        dialog.setResultConverter(btn -> {
	            if (btn == submitButtonType) return new int[] { wordSpinner.getValue(), qualitySpinner.getValue(), timeSpinner.getValue() };
	            return null;
	        });

	        dialog.showAndWait().ifPresent(scores -> {
	            theDatabase.saveGrade(replyId, scores[0], scores[1], scores[2]);
	            renderPostView(threadId, type, container);
	        });
	    } catch (Exception e) { e.printStackTrace(); }
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

	protected static void renderDirectMessages(String currentUser, String targetUser, javafx.scene.layout.VBox container) {
		try {
			container.getChildren().clear();
			container.setStyle("-fx-background-color: #ffffff; -fx-padding: 15;"); 
			javafx.scene.layout.VBox headerBox = new javafx.scene.layout.VBox(5);
			headerBox.setStyle("-fx-padding: 0 0 15 0; -fx-border-color: #d1d5db; -fx-border-width: 0 0 2 0;");
			javafx.scene.control.Label lblTitle = new javafx.scene.control.Label("Messages with " + targetUser);
			lblTitle.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 20));
			lblTitle.setTextFill(javafx.scene.paint.Color.web("#b31b1b")); 
			headerBox.getChildren().add(lblTitle);
			container.getChildren().add(headerBox);
			
			for (String msgData : theDatabase.getDirectMessages(currentUser, targetUser)) {
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