import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocialMediaApp1 extends JFrame {
    private List<JPanel> posts;
    private JPanel postPanel;
    private JButton addPostButton;
    private Font postFont = new Font("Arial", Font.PLAIN, 14);
    private Color postBackgroundColor = new Color(240, 240, 240);
    private Color buttonBackgroundColor = new Color(150, 200, 255);

    private Graph socialGraph;
    private Map<String, UserProfile> userProfiles;

    public SocialMediaApp1() {
        setTitle("Social Media App");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        posts = new ArrayList<>();
        postPanel = new JPanel();
        postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
        postPanel.setBackground(Color.WHITE);

        addPostButton = new JButton("Add Post");
        addPostButton.setBackground(buttonBackgroundColor);
        addPostButton.setFocusPainted(false);
        addPostButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String postContent = JOptionPane.showInputDialog(SocialMediaApp1.this, "Enter your post:");
                if (postContent != null && !postContent.isEmpty()) {
                    addNewPost(postContent);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(postPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane, BorderLayout.CENTER);
        add(addPostButton, BorderLayout.SOUTH);

        socialGraph = new Graph();
        userProfiles = new HashMap<>();

        setVisible(true);
    }

    private void addNewPost(String postContent) {
        JPanel post = new JPanel();
        post.setLayout(new BorderLayout());
        post.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        post.setBackground(postBackgroundColor);

        JLabel postLabel = new JLabel("<html><body style='width: 300px;'>" + postContent + "</body></html>");
        postLabel.setFont(postFont);

        JButton likeButton = new JButton("Like");
        likeButton.setBackground(buttonBackgroundColor);
        likeButton.setFocusPainted(false);
        JButton dislikeButton = new JButton("Dislike");
        dislikeButton.setBackground(buttonBackgroundColor);
        dislikeButton.setFocusPainted(false);
        JButton commentButton = new JButton("Comment");
        commentButton.setBackground(buttonBackgroundColor);
        commentButton.setFocusPainted(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(postBackgroundColor);
        buttonPanel.add(likeButton);
        buttonPanel.add(dislikeButton);
        buttonPanel.add(commentButton);

        post.add(postLabel, BorderLayout.NORTH);
        post.add(buttonPanel, BorderLayout.SOUTH);

        posts.add(post);
        postPanel.add(post);
        postPanel.revalidate();
        postPanel.repaint();

        String userName = "User";
        likeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(SocialMediaApp1.this, "Liked post!");
                trackUserInteractions(userName, "like", postContent);
            }
        });

        dislikeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(SocialMediaApp1.this, "Disliked post!");
                trackUserInteractions(userName, "dislike", postContent);
            }
        });

        commentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String comment = JOptionPane.showInputDialog(SocialMediaApp1.this, "Enter your comment:");
                if (comment != null && !comment.isEmpty()) {
                    JOptionPane.showMessageDialog(SocialMediaApp1.this, "Commented: " + comment);
                    trackUserInteractions(userName, "comment", postContent);
                }
            }
        });
    }

    private class Node {
        private String userName;

        public Node(String userName) {
            this.userName = userName;
        }

        public String getUserName() {
            return userName;
        }
    }

    private class Edge {
        private Node source;
        private Node destination;
        private String interactionType;

        public Edge(Node source, Node destination, String interactionType) {
            this.source = source;
            this.destination = destination;
            this.interactionType = interactionType;
        }

        public Node getSource() {
            return source;
        }

        public Node getDestination() {
            return destination;
        }

        public String getInteractionType() {
            return interactionType;
        }
    }

    private class Graph {
        private List<Node> nodes;
        private List<Edge> edges;

        public Graph() {
            nodes = new ArrayList<>();
            edges = new ArrayList<>();
        }

        public void addNode(Node node) {
            nodes.add(node);
        }

        public void addEdge(Node source, Node destination, String interactionType) {
            Edge edge = new Edge(source, destination, interactionType);
            edges.add(edge);
        }

        public List<Edge> getEdges() {
            return edges;
        }
    }

    // User profile class
    private class UserProfile {
        private String userName;
        private List<String> interests;
        private List<String> connections;

        public UserProfile(String userName) {
            this.userName = userName;
            interests = new ArrayList<>();
            connections = new ArrayList<>();
        }

        public void addInterest(String interest) {
            interests.add(interest);
        }

        public void addConnection(String connection) {
            connections.add(connection);
        }

        public String getUserName() {
            return userName;
        }

        public List<String> getInterests() {
            return interests;
        }

        public List<String> getConnections() {
            return connections;
        }
    }

    private void trackUserInteractions(String userName, String interactionType, String targetContent) {
        String targetUserName = "Post Creator"; // Assuming target is the post creator
        socialGraph.addEdge(new Node(userName), new Node(targetUserName), interactionType);

        UserProfile userProfile = userProfiles.getOrDefault(userName, new UserProfile(userName));
        userProfile.addConnection(targetUserName);
        userProfiles.put(userName, userProfile);

        if (interactionType.equals("like")) {
            UserProfile targetProfile = userProfiles.getOrDefault(targetUserName, new UserProfile(targetUserName));
            targetProfile.addInterest("Liked posts");
            userProfiles.put(targetUserName, targetProfile);
        }
        // Call the recommendation method after each interaction to update suggestions
        List<String> recommendations = recommendContent(userName);
        displayRecommendedContentUI(recommendations);
    }

    private List<String> recommendContent(String userName) {
        List<String> recommendations = new ArrayList<>();
        UserProfile userProfile = userProfiles.get(userName);
        if (userProfile != null) {
            // Gather interests of connected users
            for (String connection : userProfile.connections) {
                UserProfile connectedProfile = userProfiles.get(connection);
                if (connectedProfile != null) {
                    recommendations.addAll(connectedProfile.interests);
                }
            }
        }
        return recommendations;
    }

    private void displayRecommendedContentUI(List<String> recommendations) {
        // Create a dialog box to display recommended content
        StringBuilder contentMessage = new StringBuilder("<html><body>");
        contentMessage.append("<h3>Recommended Content</h3>");
        if (recommendations.isEmpty()) {
            contentMessage.append("<p>No recommendations available.</p>");
        } else {
            for (String recommendation : recommendations) {
                contentMessage.append("<p>").append(recommendation).append("</p>");
                // Add like, dislike, and comment buttons for each recommended content
                JButton likeButton = new JButton("Like");
                likeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // Track the feedback when the user likes the content
                        // For demonstration, we print the liked content
                        System.out.println("Liked Content: " + recommendation);
                    }
                });
                JButton dislikeButton = new JButton("Dislike");
                dislikeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // Track the feedback when the user dislikes the content
                        // For demonstration, we print the disliked content
                        System.out.println("Disliked Content: " + recommendation);
                    }
                });
                JButton commentButton = new JButton("Comment");
                commentButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String comment = JOptionPane.showInputDialog(SocialMediaApp1.this, "Enter your comment:");
                        if (comment != null && !comment.isEmpty()) {
                            JOptionPane.showMessageDialog(SocialMediaApp1.this, "Commented: " + comment);
                        }
                    }
                });
                JPanel buttonPanel = new JPanel();
                buttonPanel.add(likeButton);
                buttonPanel.add(dislikeButton);
                buttonPanel.add(commentButton);
                contentMessage.append(buttonPanel);
            }
        }
        contentMessage.append("</body></html>");

        JLabel contentLabel = new JLabel(contentMessage.toString());
        JScrollPane scrollPane = new JScrollPane(contentLabel);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Recommended Content", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SocialMediaApp1();
            }
        });
    }
}