import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class VacationRequestForm extends JFrame {
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextArea additionalInfoArea;

    public VacationRequestForm(String employeeUsername) {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        JLabel startDateLabel = new JLabel("Start Date (yyyy-mm-dd):");
        JLabel endDateLabel = new JLabel("End Date (yyyy-mm-dd):");
        JLabel additionalInfoLabel = new JLabel("Additional Information:");

        startDateField = new JTextField();
        endDateField = new JTextField();
        additionalInfoArea = new JTextArea();

        JButton sendButton = new JButton("Send");

        panel.add(startDateLabel);
        panel.add(startDateField);
        panel.add(endDateLabel);
        panel.add(endDateField);
        panel.add(additionalInfoLabel);
        panel.add(new JScrollPane(additionalInfoArea));
        panel.add(sendButton);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendVacationRequest(employeeUsername);
            }
        });

        setTitle("Vacation Request Form");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 200);
        add(panel);
    }

    private void sendVacationRequest(String employeeUsername) {
        try {
            LocalDate endDate = LocalDate.parse(endDateField.getText());
            LocalDate startDate = LocalDate.parse(startDateField.getText());
            String additionalInfo = additionalInfoArea.getText();

            saveRequestToFile(employeeUsername, startDate, endDate, additionalInfo);
            JOptionPane.showMessageDialog(this, "Vacation request sent for " + employeeUsername);
    
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date. Please enter a date in the format yyyy-mm-dd");
            startDateField.setText("");
            endDateField.setText("");
            return;
        }
    }
    private void saveRequestToFile(String username, LocalDate startDate, LocalDate endDate, String additionalInfo) {
        String filename = "vacation_requests.txt"; // Name der Datei, in der die Anfragen gespeichert werden

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write("Username: " + username + "\n");
            writer.write("Start Date: " + startDate + "\n");
            writer.write("End Date: " + endDate + "\n");
            writer.write("Additional Information: " + additionalInfo + "\n");
            writer.write("------------------------------------\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VacationRequestForm form = new VacationRequestForm("employee123");
            form.setVisible(true);
        });
    }
}
