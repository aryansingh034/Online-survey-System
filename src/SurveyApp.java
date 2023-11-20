import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SurveyApp extends JFrame {
    private final JTextField nameField;
    private final JTextField emailField;
    private final JTextField additionalField;
    private final JTextArea feedbackArea;

    private Connection connection;
    private PreparedStatement insertStatement;
    private PreparedStatement updateStatement;

    private JButton submitButton;
    private JButton updateButton;
    private JButton exitButton;

    public SurveyApp() {
        setTitle("Survey App");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize the database connection and prepared statements
        initializeDatabase();

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel textPanel = new JPanel();
        JLabel topLabel = new JLabel("Welcome to the Survey App");
        topLabel.setFont(new Font("Arial", Font.BOLD, 18));
        textPanel.add(topLabel);
        mainPanel.add(textPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);

        JLabel nameLabel = new JLabel("Name:");
        JLabel emailLabel = new JLabel("Email:");
        JLabel additionalLabel = new JLabel("Where do you invest your Money?");
        JLabel feedbackLabel = new JLabel("Share your Experience with us:");

        nameField = new JTextField(30);
        emailField = new JTextField(30);
        additionalField = new JTextField(30);
        feedbackArea = new JTextArea(10, 30);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setLineWrap(true);

        submitButton = new JButton("Submit");
        exitButton = new JButton("Exit");

        submitButton.addActionListener(e -> submitFeedback());
        exitButton.addActionListener(e -> {
            closeConnection();
            System.exit(0);
        });

        updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateFeedback());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(emailLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(additionalLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(additionalField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(feedbackLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(new JScrollPane(feedbackArea), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(submitButton, gbc);
        gbc.gridx = 2;
        panel.add(exitButton, gbc);
        gbc.gridy = 5;
        gbc.gridx = 1;
        panel.add(updateButton, gbc);


        mainPanel.add(panel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private void initializeDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/survey_db";
            String username = "root";
            String password = "Aryan@htc03";
            connection = DriverManager.getConnection(url, username, password);

            String insertQuery = "INSERT INTO feedback (name,email,additional_field,feedback) VALUES (?, ?, ?, ?)";
            String updateQuery = "UPDATE feedback SET additional_field = ?, feedback = ? WHERE name = ? AND email = ?";

            insertStatement = connection.prepareStatement(insertQuery);
            updateStatement = connection.prepareStatement(updateQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitFeedback() {
        String name = nameField.getText();
        String email = emailField.getText();
        String additionalValue = additionalField.getText();
        String feedback = feedbackArea.getText();

        if (name.isEmpty() || email.isEmpty() || additionalValue.isEmpty() || feedback.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            insertStatement.setString(1, name);
            insertStatement.setString(2, email);
            insertStatement.setString(3, additionalValue);
            insertStatement.setString(4, feedback);
            insertStatement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Thank you for your feedback!", "Success", JOptionPane.INFORMATION_MESSAGE);

            displaySurveyDetails(name, email, additionalValue, feedback);

            nameField.setText("");
            emailField.setText("");
            additionalField.setText("");
            feedbackArea.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to submit feedback.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFeedback() {
        String name = nameField.getText();
        String email = emailField.getText();
        String additionalValue = additionalField.getText();
        String feedback = feedbackArea.getText();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Email are required for updating.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            updateStatement.setString(1, additionalValue);
            updateStatement.setString(2, feedback);
            updateStatement.setString(3, name);
            updateStatement.setString(4, email);

            int updatedRows = updateStatement.executeUpdate();

            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "Feedback updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                displaySurveyDetails(name, email, additionalValue, feedback);
                nameField.setText("");
                emailField.setText("");
                additionalField.setText("");
                feedbackArea.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "No records updated. Please check the Name and Email.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update feedback.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displaySurveyDetails(String name, String email, String additionalValue, String feedback) {
        JFrame detailsFrame = new JFrame("Survey Details");
        detailsFrame.setSize(400, 300);
        detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        detailsFrame.setLocationRelativeTo(this);

        JPanel detailsPanel = new JPanel(new BorderLayout());

        JTextArea detailsTextArea = new JTextArea();
        detailsTextArea.setEditable(false);
        detailsTextArea.append("Name: " + name + "\n");
        detailsTextArea.append("Email: " + email + "\n");
        detailsTextArea.append("Where do you invest your Money? " + additionalValue + "\n");
        detailsTextArea.append("Experience: " + feedback);

        detailsPanel.add(new JScrollPane(detailsTextArea), BorderLayout.CENTER);

        detailsFrame.add(detailsPanel);
        detailsFrame.setVisible(true);
    }

    private void closeConnection() {
        try {
            if (insertStatement != null)
                insertStatement.close();
            if (updateStatement != null)
                updateStatement.close();
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SurveyApp::new);
    }
}
