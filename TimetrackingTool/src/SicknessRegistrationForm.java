

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class SicknessRegistrationForm extends JFrame {
	private JTextField sicknessStartDateField;
    private JTextField sicknessEndDateField;
    private JTextArea sicknessInfoArea;

    public SicknessRegistrationForm(String employeeUsername) {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        JLabel sicknessStartDateLabel = new JLabel("Sickness Start Date:");
        JLabel sicknessEndDateLabel = new JLabel("Sickness End Date:");
        JLabel sicknessInfoLabel = new JLabel("Additional Information:");

        sicknessStartDateField = new JTextField();
        sicknessEndDateField = new JTextField();
        sicknessInfoArea = new JTextArea();

        JButton sendButton = new JButton("Send");

        panel.add(sicknessStartDateLabel);
        panel.add(sicknessStartDateField);
        panel.add(sicknessEndDateLabel);
        panel.add(sicknessEndDateField);
        panel.add(sicknessInfoLabel);
        panel.add(new JScrollPane(sicknessInfoArea));
        panel.add(sendButton);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendSicknessRequest(employeeUsername);
            }
        });

        setTitle("Sickness Registration Form");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 200);
        add(panel);
    }

    private void sendSicknessRequest(String employeeUsername) {
        LocalDate sicknessStartDate = LocalDate.parse(sicknessStartDateField.getText());
        LocalDate sicknessEndDate = LocalDate.parse(sicknessEndDateField.getText());
        String additionalInfo = sicknessInfoArea.getText();

        saveSicknessToFile(employeeUsername, sicknessStartDate, sicknessEndDate, additionalInfo);

        JOptionPane.showMessageDialog(this, "Sickness request sent for " + employeeUsername);
        dispose();
    }

    private void saveSicknessToFile(String username, LocalDate startDate, LocalDate endDate, String additionalInfo) {
        String filename = "sickness_records.txt"; 

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write("Username: " + username + "\n");
            writer.write("Sickness Start Date: " + startDate + "\n");
            writer.write("Sickness End Date: " + endDate + "\n");
            writer.write("Additional Information: " + additionalInfo + "\n");
            writer.write("------------------------------------\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SicknessRegistrationForm form = new SicknessRegistrationForm("employee123");
            form.setVisible(true);
        });
    }
}