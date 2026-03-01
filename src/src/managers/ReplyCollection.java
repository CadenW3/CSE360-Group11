package managers;

import database.Database;
import entityClasses.Reply;
import java.util.ArrayList;
import java.util.List;

public class ReplyCollection {
    private Database db;
    private List<Reply> allReplies;
    private List<Reply> activeSubset;

    public ReplyCollection(Database db) {
        this.db = db;
        this.allReplies = new ArrayList<>();
        this.activeSubset = new ArrayList<>();
    }

    // --- READ / LOAD ---
    public void loadRepliesForPost(int postId, String type) {
        allReplies.clear();
        List<String> rawReplies = type.equals("Discussion") ? 
                                  db.getRepliesForThread(postId) : 
                                  db.getRepliesForQuestion(postId);

        for (String data : rawReplies) {
            String[] parts = data.split("\\|");
            if (parts.length >= 4) {
                allReplies.add(new Reply(
                    Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), 
                    postId, type, parts[2].trim(), parts[3].trim()
                ));
            }
        }
        cleanupOrphanedDeletes(); // Automatically process the active subset
    }

    // --- FILTER SUBSET (Clean Orphaned Deletes) ---
    private void cleanupOrphanedDeletes() {
        boolean changed = true;
        List<Reply> currentList = new ArrayList<>(allReplies);

        while (changed) {
            changed = false;
            List<Reply> toKeep = new ArrayList<>();

            for (Reply reply : currentList) {
                boolean hasChildren = false;
                for (Reply checkR : currentList) {
                    if (checkR.getParentId() == reply.getId()) {
                        hasChildren = true;
                        break;
                    }
                }

                if (reply.getContent().equals("[This post was deleted.]") && !hasChildren) {
                    try {
                        if (reply.getType().equals("Discussion")) db.deleteReply(reply.getId());
                        else db.deleteQuestionReply(reply.getId());
                    } catch (Exception e) { e.printStackTrace(); }
                    changed = true;
                } else {
                    toKeep.add(reply);
                }
            }
            currentList = toKeep;
        }
        this.activeSubset = currentList;
    }

    public List<Reply> getActiveSubset() { return activeSubset; }

    // --- CREATE ---
    public void createReply(int postId, int parentId, String type, String content, String author) {
        try {
            if (type.equals("Discussion")) db.createReply(postId, parentId, content, author);
            else db.createQuestionReply(postId, parentId, content, author);
            loadRepliesForPost(postId, type);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- UPDATE ---
    public void updateReply(int replyId, String type, String newContent, int postId) {
        try {
            if (type.equals("Discussion")) db.updateReply(replyId, newContent);
            else db.updateQuestionReply(replyId, newContent);
            loadRepliesForPost(postId, type);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- DELETE (Soft or Hard) ---
    public void deleteReply(int replyId, String type, int postId) {
        try {
            boolean hasChildren = false;
            for (Reply r : activeSubset) {
                if (r.getParentId() == replyId) { hasChildren = true; break; }
            }

            if (hasChildren) {
                if (type.equals("Discussion")) db.softDeleteReply(replyId);
                else db.softDeleteQuestionReply(replyId);
            } else {
                if (type.equals("Discussion")) db.deleteReply(replyId);
                else db.deleteQuestionReply(replyId);
            }
            loadRepliesForPost(postId, type);
        } catch (Exception e) { e.printStackTrace(); }
    }
}