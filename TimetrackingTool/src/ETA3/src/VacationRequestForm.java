import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VacationRequestForm extends JFrame {
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextArea additionalInfoArea;

    public VacationRequestForm(String employeeUsername) {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        JLabel startDateLabel = new JLabel("Start Date:");
        JLabel endDateLabel = new JLabel("End Date:");
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
        LocalDate startDate = LocalDate.parse(startDateField.getText());
        LocalDate endDate = LocalDate.parse(endDateField.getText());
        String additionalInfo = additionalInfoArea.getText();

        // You can implement logic here to store the vacation request data,
        // e.g., in a map or a database, for HR to later view and approve/reject.

        // For now, just display a message to indicate that the request is sent.
        JOptionPane.showMessageDialog(this, "Vacation request sent for " + employeeUsername);

        // Optionally, you can close the form after sending the request.
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VacationRequestForm form = new VacationRequestForm("employee123");
            form.setVisible(true);
        });
    }
}
