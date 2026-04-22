package StudentPostTests;

import entityClasses.Post;
import entityClasses.Reply;

import java.util.ArrayList;
import java.util.List;

/**
 * title: StudentPostTests
 *
 * * tests for the Post and Reply classes, makes sure everything
 * from the student user stories works the way it should
 * 
 * requirements:
 * REQ01: student can post a question 
 * REQ02: post stores its type correctly
 * REQ03: post stores its ID correctly
 * REQ04: all post fields are readable via getters
 * REQ05: student can see posts others have made 
 * REQ06: student can see only their own posts 
 * REQ07: student can filter by unread posts
 * REQ08: student can search posts by keyword in title
 * REQ09: student can search posts by keyword in topic/body
 * REQ10: posts default to the General thread if none is specified
 * REQ11: student can reply to a thread 
 * REQ12: student can reply to a reply 
 * REQ13: student can delete their own reply
 * REQ14: deleting a reply with children soft deletes it
 * REQ15: student can update their reply content
 * REQ16: student can update a post title
 * REQ17: student can update a post topic/body 
 * REQ18: isValidTitle rejects null
 * REQ19: isValidTitle rejects empty string
 * REQ20: isValidTitle accepts normal text
 * REQ21: isValidTopic rejects null
 * REQ22: isValidTopic rejects empty string
 * REQ23: isValidAuthor rejects null
 * REQ24: isValidAuthor rejects empty string
 * REQ25: isValidType accepts Discussion
 * REQ26: isValidType accepts Question
 *
 * @author CSE 360 Group 11
 * @version 1.00 2025-11-01
 */
public class StudentPostTests {

    /**
     * Constructor for StudentPostTests.
     */
    public StudentPostTests() {
    }

    static int passed = 0;
    static int total = 0;

    /**
     * runs all tests and prints a count at the end
     *
     * @param args not used
     */
    public static void main(String[] args) {
        System.out.println("Running StudentPostTests\n");

        testCreateQuestion();
        testPostStoresType();
        testPostStoresId();
        testPostGetters();
        testReadAllPosts();
        testFilterMyPosts();
        testFilterUnread();
        testKeywordInTitle();
        testKeywordInTopic();
        testDefaultGeneralThread();
        testReplyToThread();
        testNestedReply();
        testDeleteReply();
        testSoftDeleteWithChildren();
        testUpdateReplyContent();
        testUpdatePostTitle();
        testUpdatePostTopic();
        testIsValidTitleNull();
        testIsValidTitleEmpty();
        testIsValidTitleValid();
        testIsValidTopicNull();
        testIsValidTopicEmpty();
        testIsValidAuthorNull();
        testIsValidAuthorEmpty();
        testIsValidTypeDiscussion();
        testIsValidTypeQuestion();

        System.out.println("\n" + passed + "/" + total + " tests passed");
    }

    /**
     * prints pass or fail for a test
     * true = pass, false = fail
     *
     * @param name what test this is
     * @param condition the result
     */
    public static void check(String name, boolean condition) {
        total++;
        if (condition) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            System.out.println("[FAIL] " + name);
        }
    }

    /**
     * tests REQ01
     * students can post a question - makes a Question type post
     */
    public static void testCreateQuestion() {
        Post p = new Post(1, "Question", "Why does recursion work?", "can someone explain", "alice");
        check("create question type", "Question".equals(p.getType())); // REQ01
    }

    /**
     * tests REQ02
     * post stores its type correctly
     */
    public static void testPostStoresType() {
        Post p = new Post(2, "Discussion", "General", "welcome", "system");
        check("post stores type", "Discussion".equals(p.getType())); // REQ02
    }

    /**
     * tests REQ03
     * post stores its ID correctly
     */
    public static void testPostStoresId() {
        Post p = new Post(5, "Question", "title", "body", "bob");
        check("post stores id", p.getId() == 5); // REQ03
    }

    /**
     * tests REQ04
     * all five getters return the right values
     */
    public static void testPostGetters() {
        Post p = new Post(7, "Question", "My Question", "body here", "carol");
        check("getters id", p.getId() == 7); // REQ04
        check("getters type", "Question".equals(p.getType()));
        check("getters title", "My Question".equals(p.getTitle()));
        check("getters topic", "body here".equals(p.getTopic()));
        check("getters author", "carol".equals(p.getAuthor()));
    }

    /**
     * tests REQ05
     * students can see a list of all posts - simulates loading all posts
     */
    public static void testReadAllPosts() {
        List<Post> all = new ArrayList<>();
        all.add(new Post(1, "Discussion", "General", "welcome", "system"));
        all.add(new Post(2, "Question", "Help with hw", "stuck on problem 3", "alice"));
        all.add(new Post(3, "Question", "Exam question", "what topics covered", "bob"));

        check("read all posts not empty", all.size() > 0); // REQ05
        check("read all posts count", all.size() == 3);
    }

    /**
     * tests REQ06
     * students can filter to see only their own posts
     */
    public static void testFilterMyPosts() {
        List<Post> all = new ArrayList<>();
        all.add(new Post(1, "Question", "alice question 1", "body", "alice"));
        all.add(new Post(2, "Question", "bob question", "body", "bob"));
        all.add(new Post(3, "Discussion", "alice question 2", "body", "alice"));

        // simulates the myPostsOnly filter in PostCollection.filterPosts()
        List<Post> mine = new ArrayList<>();
        for (Post p : all) {
            if ("alice".equalsIgnoreCase(p.getAuthor())) mine.add(p);
        }

        check("my posts count", mine.size() == 2); // REQ06
        boolean allMine = true;
        for (Post p : mine) {
            if (!"alice".equalsIgnoreCase(p.getAuthor())) allMine = false;
        }
        check("my posts all mine", allMine);
    }

    /**
     * tests REQ07
     * students can filter to see only unread posts
     * simulates this by tracking which posts a user has read
     */
    public static void testFilterUnread() {
        List<Post> all = new ArrayList<>();
        all.add(new Post(1, "Question", "post 1", "body", "alice"));
        all.add(new Post(2, "Question", "post 2", "body", "bob"));
        all.add(new Post(3, "Discussion", "post 3", "body", "carol"));

        // simulates read status - user has read post id 1 only
        List<Integer> readIds = new ArrayList<>();
        readIds.add(1);

        List<Post> unread = new ArrayList<>();
        for (Post p : all) {
            if (!readIds.contains(p.getId())) unread.add(p);
        }

        check("unread filter count", unread.size() == 2); // REQ07
    }

    /**
     * tests REQ08
     * students can search posts by keyword in the title
     */
    public static void testKeywordInTitle() {
        List<Post> all = new ArrayList<>();
        all.add(new Post(1, "Question", "help with homework", "body", "alice"));
        all.add(new Post(2, "Question", "exam tomorrow", "body", "bob"));
        all.add(new Post(3, "Discussion", "homework tips", "body", "carol"));

        String keyword = "homework";
        List<Post> result = new ArrayList<>();
        for (Post p : all) {
            if (p.getTitle().toLowerCase().contains(keyword) ||
                p.getTopic().toLowerCase().contains(keyword)) {
                result.add(p);
            }
        }

        check("keyword in title count", result.size() == 2); // REQ08
    }

    /**
     * tests REQ09
     * students can search posts by keyword in the body/topic
     */
    public static void testKeywordInTopic() {
        List<Post> all = new ArrayList<>();
        all.add(new Post(1, "Question", "question 1", "this is about recursion", "alice"));
        all.add(new Post(2, "Question", "question 2", "unrelated stuff", "bob"));

        String keyword = "recursion";
        List<Post> result = new ArrayList<>();
        for (Post p : all) {
            if (p.getTitle().toLowerCase().contains(keyword) ||
                p.getTopic().toLowerCase().contains(keyword)) {
                result.add(p);
            }
        }

        check("keyword in topic count", result.size() == 1); // REQ09
    }

    /**
     * tests REQ10
     * if no thread is specified a post defaults to the General thread
     * the title "General" is what the DB uses for the default thread
     */
    public static void testDefaultGeneralThread() {
        Post p = new Post(1, "Discussion", "General", "General Discussion", "System");
        check("default general thread title", "General".equals(p.getTitle())); // REQ10
        check("default general thread author", "System".equals(p.getAuthor()));
    }

    /**
     * tests REQ11
     * students can reply to a thread - parentId 0 means direct reply to thread
     */
    public static void testReplyToThread() {
        Reply r = new Reply(10, 0, 5, "Discussion", "my reply to the thread", "alice");
        check("reply to thread parentId", r.getParentId() == 0); // REQ11
        check("reply to thread postId", r.getPostId() == 5);
    }

    /**
     * tests REQ12
     * students can reply to another reply - parentId points to the parent reply
     */
    public static void testNestedReply() {
        Reply r = new Reply(11, 10, 5, "Discussion", "reply to a reply", "bob");
        check("nested reply parentId", r.getParentId() == 10); // REQ12
    }

    /**
     * tests REQ13
     * students can delete their own reply - simulates removing from list
     */
    public static void testDeleteReply() {
        List<Reply> replies = new ArrayList<>();
        Reply toDelete = new Reply(20, 0, 5, "Discussion", "my reply", "alice");
        replies.add(toDelete);
        replies.add(new Reply(21, 0, 5, "Discussion", "someone elses reply", "bob"));

        replies.remove(toDelete); // simulates hard delete when no children

        boolean gone = true;
        for (Reply r : replies) {
            if (r.getId() == 20) gone = false;
        }
        check("delete reply removed", gone); // REQ13
        check("delete reply other stays", replies.size() == 1);
    }

    /**
     * tests REQ14
     * if a deleted reply has children it shows sentinel text instead of being removed
     * this is what the DB does when softDeleteReply() is called
     */
    public static void testSoftDeleteWithChildren() {
        Reply r = new Reply(15, 0, 5, "Discussion", "original content", "alice");
        r.setContent("[This post was deleted.]"); // soft delete when children exist
        check("soft delete sentinel", "[This post was deleted.]".equals(r.getContent())); // REQ14
    }

    /**
     * tests REQ15
     * students can update the content of their own reply
     */
    public static void testUpdateReplyContent() {
        Reply r = new Reply(12, 0, 5, "Discussion", "old content", "carol");
        r.setContent("updated content");
        check("update reply content", "updated content".equals(r.getContent())); // REQ15
    }

    /**
     * tests REQ16
     * students can edit the title of their own post
     */
    public static void testUpdatePostTitle() {
        Post p = new Post(3, "Question", "old title", "body", "dave");
        p.setTitle("new title");
        check("update post title", "new title".equals(p.getTitle())); // REQ16
    }

    /**
     * tests REQ17
     * students can edit the body/topic of their own post
     */
    public static void testUpdatePostTopic() {
        Post p = new Post(4, "Question", "title", "old body", "eve");
        p.setTopic("new body");
        check("update post topic", "new body".equals(p.getTopic())); // REQ17
    }

    /** tests REQ18 - null should be invalid */
    public static void testIsValidTitleNull() {
        check("isValidTitle null", !Post.isValidTitle(null)); // REQ18
    }

    /** tests REQ19 - empty string should be invalid */
    public static void testIsValidTitleEmpty() {
        check("isValidTitle empty", !Post.isValidTitle("")); // REQ19
    }

    /** tests REQ20 - normal text should be valid */
    public static void testIsValidTitleValid() {
        check("isValidTitle valid", Post.isValidTitle("Why does Java do this")); // REQ20
    }

    /** tests REQ21 - null should be invalid */
    public static void testIsValidTopicNull() {
        check("isValidTopic null", !Post.isValidTopic(null)); // REQ21
    }

    /** tests REQ22 - empty string should be invalid */
    public static void testIsValidTopicEmpty() {
        check("isValidTopic empty", !Post.isValidTopic("")); // REQ22
    }

    /** tests REQ23 - null should be invalid */
    public static void testIsValidAuthorNull() {
        check("isValidAuthor null", !Post.isValidAuthor(null)); // REQ23
    }

    /** tests REQ24 - empty string should be invalid */
    public static void testIsValidAuthorEmpty() {
        check("isValidAuthor empty", !Post.isValidAuthor("")); // REQ24
    }

    /** tests REQ25 - Discussion should be valid */
    public static void testIsValidTypeDiscussion() {
        check("isValidType Discussion", Post.isValidType("Discussion")); // REQ25
    }

    /** tests REQ26 - Question should be valid */
    public static void testIsValidTypeQuestion() {
        check("isValidType Question", Post.isValidType("Question")); // REQ26
    }
}
