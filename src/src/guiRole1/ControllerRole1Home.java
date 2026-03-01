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

	private static Database theDatabase = applicationMain.FoundationsMain.database;
	protected static boolean filterMyPosts = false;
	protected static boolean filterUnread = false;
	
	protected static String currentFilterKeyword = "";
	protected static String currentFilterType = "";

	public ControllerRole1Home() {
	}
	
	public static void refreshRequestsTree(javafx.scene.control.TreeView<String> treeView, String username) {
        javafx.scene.control.TreeItem<String> hiddenRoot = new javafx.scene.control.TreeItem<>("Hidden");
        javafx.scene.control.TreeItem<String> pendingRoot = new javafx.scene.control.TreeItem<>("Pending Requests");
        javafx.scene.control.TreeItem<String> closedRoot = new javafx.scene.control.TreeItem<>("Closed Requests");
        pendingRoot.setExpanded(true); closedRoot.setExpanded(true);

        java.util.List<String> openReqs = theDatabase.getAdminRequests("Pending", username);
        for (String r : openReqs) {
            String[] p = r.split("<SEP>");
            pendingRoot.getChildren().add(new javafx.scene.control.TreeItem<>("[Req-" + p[0] + "] Pending"));
        }

        java.util.List<String> closedReqs = theDatabase.getAdminRequests("Closed", username);
        for (String r : closedReqs) {
            String[] p = r.split("<SEP>");
            closedRoot.getChildren().add(new javafx.scene.control.TreeItem<>("[Req-" + p[0] + "] " + p[2]));
        }

        hiddenRoot.getChildren().addAll(pendingRoot, closedRoot);
        treeView.setRoot(hiddenRoot);
        treeView.setShowRoot(false);
    }
	
	public static void renderStaffRequestDetails(int reqId, javafx.scene.layout.VBox container, javafx.scene.control.TreeView<String> tree) {
		container.getChildren().clear();
		container.setStyle("-fx-padding: 15; -fx-background-color: white;");
		
		if (reqId == -1) {
			javafx.scene.control.Label title = new javafx.scene.control.Label("Request Admin Privileges");
			title.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
			javafx.scene.control.Label desc = new javafx.scene.control.Label("Write a message to the Admins explaining why you need temporary Admin privileges.");
			desc.setWrapText(true);
			
			javafx.scene.control.TextArea txtMsg = new javafx.scene.control.TextArea();
			txtMsg.setPromptText("Enter your reason here...");
			txtMsg.setPrefRowCount(4);
			
			javafx.scene.control.Button btnSubmit = new javafx.scene.control.Button("Submit Request");
			btnSubmit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
			btnSubmit.setOnAction(e -> {
				if (!txtMsg.getText().isEmpty()) {
					theDatabase.submitAdminRequest(ViewRole1Home.theUser.getUserName(), txtMsg.getText());
					refreshRequestsTree(tree, ViewRole1Home.theUser.getUserName());
					renderStaffRequestDetails(-1, container, tree);
				}
			});
			container.getChildren().addAll(title, desc, txtMsg, btnSubmit);
			return;
		}
		
		java.util.List<String> reqs = theDatabase.getAdminRequests(null, ViewRole1Home.theUser.getUserName());
		String[] reqParts = null;
		for (String r : reqs) {
			String[] p = r.split("<SEP>");
			if (Integer.parseInt(p[0]) == reqId) { reqParts = p; break; }
		}
		if (reqParts == null) return;
		
		String status = reqParts[2];
		String message = reqParts[4];
		String adminNotes = reqParts.length > 5 ? reqParts[5] : "";
		
		javafx.scene.control.Label title = new javafx.scene.control.Label("Request #" + reqId + " Details");
		title.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
		
		javafx.scene.control.Label lblStatus = new javafx.scene.control.Label("Status: " + status);
		lblStatus.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
		if (status.equals("Accepted")) lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
		else if (status.equals("Denied")) lblStatus.setTextFill(javafx.scene.paint.Color.RED);
		
		container.getChildren().addAll(title, lblStatus);
		
		if (status.equals("Pending")) {
			javafx.scene.layout.VBox msgBox = new javafx.scene.layout.VBox(5);
			msgBox.setStyle("-fx-background-color: #f3f4f6; -fx-padding: 10; -fx-background-radius: 5;");
			javafx.scene.control.Label lblMsg = new javafx.scene.control.Label(message);
			lblMsg.setWrapText(true);
			msgBox.getChildren().addAll(new javafx.scene.control.Label("Your Message:"), lblMsg);
			container.getChildren().add(msgBox);
		} else {
			if (!adminNotes.isEmpty()) {
				javafx.scene.layout.VBox noteBox = new javafx.scene.layout.VBox(5);
				noteBox.setStyle("-fx-background-color: #fef3c7; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #f59e0b; -fx-border-radius: 5;");
				javafx.scene.control.Label lblNotes = new javafx.scene.control.Label(adminNotes);
				lblNotes.setWrapText(true);
				noteBox.getChildren().addAll(new javafx.scene.control.Label("Admin Notes:"), lblNotes);
				container.getChildren().add(noteBox);
			}
			
			javafx.scene.control.Label lblEdit = new javafx.scene.control.Label("Update and Resubmit Request:");
			lblEdit.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
			javafx.scene.control.TextArea txtEdit = new javafx.scene.control.TextArea(message);
			txtEdit.setPrefRowCount(4);
			
			javafx.scene.control.Button btnResubmit = new javafx.scene.control.Button("Resubmit Request");
			btnResubmit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
			btnResubmit.setOnAction(e -> {
				if (!txtEdit.getText().isEmpty()) {
					theDatabase.resubmitAdminRequest(reqId, txtEdit.getText());
					refreshRequestsTree(tree, ViewRole1Home.theUser.getUserName());
					renderStaffRequestDetails(reqId, container, tree);
				}
			});
			container.getChildren().addAll(lblEdit, txtEdit, btnResubmit);
		}
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
				boolean isMine = post.getAuthor().equals(userName);
				boolean isRead = theDatabase.hasReadPost(userName, post.getId(), post.getType());
				if (isMine) myPostsCount++; 
				if (!isRead) unreadCount++;
			}

			postManager.filterPosts(currentFilterKeyword, currentFilterType.isEmpty() ? null : currentFilterType, filterMyPosts, filterUnread, userName);

			for (entityClasses.Post post : postManager.getFilteredSubset()) {
				replyManager.loadRepliesForPost(post.getId(), post.getType());
				java.util.List<entityClasses.Reply> activeReplies = replyManager.getActiveSubset();

				if (post.getTitle().equals("[Deleted]") && activeReplies.isEmpty()) {
					postManager.deletePost(post.getId(), post.getType());
					continue;
				}

				boolean isRead = theDatabase.hasReadPost(userName, post.getId(), post.getType());
				
				// Fix 1: Properly prefix the Thread/Question identifier for the View logic
				String prefixType = post.getType().equals("Discussion") ? "Thread" : "Question";
				String display = "[" + prefixType + "-" + post.getId() + "] " + (!isRead ? "(UNREAD) " : "") + post.getTitle();
				
				javafx.scene.control.TreeItem<String> node = new javafx.scene.control.TreeItem<>(display);
				
				java.util.Map<Integer, javafx.scene.control.TreeItem<String>> rNodes = new java.util.HashMap<>();
				int postCounter = 1;
				java.util.Map<Integer, Integer> replyCounters = new java.util.HashMap<>();
				
				for (entityClasses.Reply r : activeReplies) {
					String prefix; String localLabel;
					if (r.getParentId() == 0) {
						prefix = "[Post-" + r.getId() + "]";
						localLabel = "Post " + (postCounter++);
					} else {
						prefix = "[Reply-" + r.getId() + "]";
						int c = replyCounters.getOrDefault(r.getParentId(), 1);
						localLabel = "Reply " + c;
						replyCounters.put(r.getParentId(), c + 1);
					}
					rNodes.put(r.getId(), new javafx.scene.control.TreeItem<>(prefix + " " + localLabel + ": " + r.getContent() + " (" + r.getAuthor() + ")"));
				}
				for (entityClasses.Reply r : activeReplies) {
					if (r.getParentId() == 0) node.getChildren().add(rNodes.get(r.getId()));
					else if (rNodes.containsKey(r.getParentId())) rNodes.get(r.getParentId()).getChildren().add(rNodes.get(r.getId()));
				}

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
			String currentUser = ViewRole1Home.theUser.getUserName();
			java.util.List<String> students = theDatabase.getStudentUsers();

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

			boolean threadOwner = post.getAuthor().equals(currentUser);
			boolean threadIsStudent = students.contains(post.getAuthor());
			
			if ((threadOwner || threadIsStudent) && !post.getTopic().equals("[This post was deleted.]")) {
				javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("Edit");
				btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5;");
				btnEdit.setOnAction(e -> showEditThreadDialog(id, type, post.getTitle(), post.getTopic(), container));
				
				javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("Delete Thread");
				btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5;");
				btnDelete.setOnAction(e -> {
					javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION, "Delete this entire thread and all replies?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
					alert.showAndWait().ifPresent(res -> {
						if (res == javafx.scene.control.ButtonType.YES) {
							try {
								postManager.deletePost(id, type);
								container.getChildren().clear();
								refreshDiscussionTree(ViewRole1Home.tree_Discussions, currentUser, ViewRole1Home.button_FilterMyPosts, ViewRole1Home.button_FilterUnread);
							} catch (Exception ex) { ex.printStackTrace(); }
						}
					});
				});
				headerLayout.getChildren().addAll(btnEdit, btnDelete);
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
				
				String typeLabel = (r.getParentId() == 0) ? "Post #" + (postCounter++) : "Reply #" + (replyCounters.merge(r.getParentId(), 1, Integer::sum));
				String rDateStr = theDatabase.getTimestampStr(r.getId(), type.equals("Discussion") ? "Replies" : "QuestionReplies");

				javafx.scene.control.Label rLblCreator = new javafx.scene.control.Label(r.getAuthor() + " (" + typeLabel + ")  •  " + rDateStr);
				rLblCreator.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
				rHeaderLayout.getChildren().add(rLblCreator);
				
				javafx.scene.layout.HBox actionBox = new javafx.scene.layout.HBox(5);
				
				boolean replyOwner = r.getAuthor().equals(currentUser);
				boolean replyIsStudent = students.contains(r.getAuthor());
				
				if ((replyOwner || replyIsStudent) && !r.getContent().equals("[This post was deleted.]")) {
					javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("Edit");
					btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 11px;");
					btnEdit.setOnAction(e -> showEditReplyDialog(r.getId(), type, r.getContent(), id, container));
					
					javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("Delete");
					btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 11px;");
					btnDelete.setOnAction(e -> {
						javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION, "Delete this reply?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
						alert.showAndWait().ifPresent(res -> {
							if (res == javafx.scene.control.ButtonType.YES) {
								try {
									replyManager.deleteReply(r.getId(), type, id);
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
					btnGrade.setOnAction(e -> showGradingDialog(r.getId(), id, type, container));
					actionBox.getChildren().add(btnGrade);
				}
				
				if (!actionBox.getChildren().isEmpty()) rHeaderLayout.getChildren().add(actionBox);

				javafx.scene.control.Label rLblContent = new javafx.scene.control.Label(r.getContent());
				rLblContent.setFont(javafx.scene.text.Font.font("Arial", 15));
				rLblContent.setWrapText(true);
				rLblContent.prefWidthProperty().bind(container.widthProperty().subtract(indent + 40));
				
				replyBox.getChildren().addAll(rHeaderLayout, rLblContent);

				if (type.equals("Discussion")) {
					String gradeData = theDatabase.getGrade(r.getId());
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
				managers.PostCollection postManager = new managers.PostCollection(theDatabase);
				postManager.updatePost(id, type, res[0], res[1]);
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
			managers.ReplyCollection replyManager = new managers.ReplyCollection(theDatabase);
			replyManager.updateReply(rId, type, newContent, threadId);
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
			managers.ReplyCollection replyManager = new managers.ReplyCollection(theDatabase);
			replyManager.createReply(id, parentReplyId, type, content, userName);
			// Auto read after replying
            try { theDatabase.markPostAsRead(userName, id, type); } catch(Exception e){}
			refreshDiscussionTree(tree, userName, b1, b2);
			renderPostView(id, type, container);
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void createNewThread(String title, String topic, String userName, javafx.scene.control.TreeView<String> tree, javafx.scene.control.Button b1, javafx.scene.control.Button b2) {
		try {
			managers.PostCollection postManager = new managers.PostCollection(theDatabase);
			postManager.createPost("Discussion", title, topic, userName);
			
			// Auto mark created thread as read
            postManager.loadAllPosts();
            int maxId = -1;
            for(entityClasses.Post p : postManager.getAllPosts()) {
                if(p.getType().equals("Discussion") && p.getAuthor().equals(userName) && p.getId() > maxId) {
                    maxId = p.getId();
                }
            }
            if(maxId != -1) {
                try { theDatabase.markPostAsRead(userName, maxId, "Discussion"); } catch(Exception e){}
            }

			refreshDiscussionTree(tree, userName, b1, b2);
		} catch (Exception e) { e.printStackTrace(); }
	}

	protected static void createNewQuestion(String title, String topic, String userName, javafx.scene.control.TreeView<String> tree, javafx.scene.control.Button b1, javafx.scene.control.Button b2) {
		try {
			managers.PostCollection postManager = new managers.PostCollection(theDatabase);
			postManager.createPost("Question", title, topic, userName);
			
			// Auto mark created question as read
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

	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole1Home.theStage, ViewRole1Home.theUser);
	}	

	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole1Home.theStage);
	}
	
	protected static void performQuit() {
		System.exit(0);
	}
}