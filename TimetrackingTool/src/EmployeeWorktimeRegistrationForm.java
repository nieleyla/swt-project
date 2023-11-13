
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.border.Border;
import java.time.LocalTime;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.event.TableModelEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;



public class EmployeeWorktimeRegistrationForm extends JFrame {
    private JTable table;

    public EmployeeWorktimeRegistrationForm() {
        // Create a panel to hold the table
        JPanel panel = new JPanel(new BorderLayout());

        // Determine the current month and year
        LocalDate today = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        String monthYear = today.format(monthFormatter);

        // Create a table with 10 columns
        String[] columnNames = {"Day", "Day Name", "Begin", "End", "Break", "Hours Target", "Hours As-Is", "Plus/Minus", "Absence", "Comment"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2; // Allow editing columns 2 and onwards
            }
        };

        for (int day = 1; day <= today.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(today.getYear(), today.getMonthValue(), day);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
            model.addRow(new Object[]{day, dayName, "", "", "", "", "", "", "", ""});
        }

        table = new JTable(model);

        // Create a custom cell editor for the "Break," "Hours Target," and "Hours As-Is" columns
        TableCellEditor editor = new DefaultCellEditor(new JTextField());
        table.getColumnModel().getColumn(4).setCellEditor(editor);
        table.getColumnModel().getColumn(5).setCellEditor(editor);
        table.getColumnModel().getColumn(6).setCellEditor(editor);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                if (column == 4) {
                    int row = table.getSelectedRow();
                    model.setValueAt("00:45", row, 4); // Set "00:45" in the "Break" column
                } else if (column == 5) {
                    int row = table.getSelectedRow();
                    model.setValueAt("08:00", row, 5); // Set "08:00" in the "Hours Target" column
                } else if (column == 6) {
                    int row = table.getSelectedRow();
                    String beginValue = (String) model.getValueAt(row, 2);
                    String endValue = (String) model.getValueAt(row, 3);
                    String breakValue = (String) model.getValueAt(row, 4);

                    if (!beginValue.isEmpty() && !endValue.isEmpty()) {
                        LocalTime beginTime = LocalTime.parse(beginValue);
                        LocalTime endTime = LocalTime.parse(endValue);
                        Duration workDuration = Duration.between(beginTime, endTime);
                        long workMinutes = workDuration.toMinutes();

                        if (!breakValue.isEmpty()) {
                            LocalTime breakTime = LocalTime.parse(breakValue);
                            workMinutes -= breakTime.getHour() * 60 + breakTime.getMinute();
                        }

                        long hours = workMinutes / 60;
                        long remainingMinutes = workMinutes % 60;
                        String hoursAsIs = String.format("%02d:%02d", hours, remainingMinutes);
                        model.setValueAt(hoursAsIs, row, 6);
                    }

                } else if (column == 7) {
                    // Calculate "Plus/Minus" when "Plus/Minus" column is clicked
                    int row=table.getSelectedRow();
                    // Calculate "Plus/Minus" when "Plus/Minus" column is clicked
                    String hoursAsIsValue = (String) model.getValueAt(row, 6);
                    String hoursTargetValue = (String) model.getValueAt(row, 5);

                    if (!hoursAsIsValue.isEmpty() && !hoursTargetValue.isEmpty()) {
                        LocalTime hoursAsIsTime = LocalTime.parse(hoursAsIsValue);
                        LocalTime hoursTargetTime = LocalTime.parse(hoursTargetValue);

                        Duration plusMinusDuration = Duration.between(hoursTargetTime, hoursAsIsTime);

                        if (plusMinusDuration.isNegative()) {
                            // If the duration is negative, set minus sign "-"
                            String plusMinusValue = "-" + formatDuration(plusMinusDuration.negated());
                            model.setValueAt(plusMinusValue, row, 7);
                        } else {
                            String plusMinusValue = formatDuration(plusMinusDuration);
                            model.setValueAt(plusMinusValue, row, 7);
                        }
                    } else {
                        model.setValueAt("", row, 7); // Clear "Plus/Minus" if data is missing
                    }
                }else if (column == 8) {
                    int row=table.getSelectedRow();
                    model.setValueAt("No", row, 8);
                }
            }
        });

        // Add a DocumentListener to calculate "Hours As-Is" dynamically
        int hoursAsIsColumnIndex = 6;
        JTextField hoursAsIsField = (JTextField) editor.getTableCellEditorComponent(table, "", false, 0, hoursAsIsColumnIndex);

        hoursAsIsField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calculateHoursAsIs();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calculateHoursAsIs();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calculateHoursAsIs();
            }

            private void calculateHoursAsIs() {
                SwingUtilities.invokeLater(() -> {
                    int row = table.getSelectedRow();
                    String beginTime = (String) model.getValueAt(row, 2);
                    String endTime = (String) model.getValueAt(row, 3);
                    String breakTime = (String) model.getValueAt(row, 4);

                    // Calculate "Hours As-Is"
                    Duration workDuration = calculateWorkDuration(beginTime, endTime, breakTime);
                    String hoursAsIs = formatDuration(workDuration);
                    model.setValueAt(hoursAsIs, row, hoursAsIsColumnIndex);
                });
            }
        });

        // Add the table to the panel
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Set up the frame
        setTitle(monthYear + " Worktime Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 400);
        add(panel);
    }

    private Duration calculateWorkDuration(String beginTime, String endTime, String breakTime) {
        LocalTime begin = LocalTime.parse(beginTime);
        LocalTime end = LocalTime.parse(endTime);
        LocalTime breakDuration = LocalTime.parse(breakTime);
        return Duration.between(begin, end).minusHours(breakDuration.getHour()).minusMinutes(breakDuration.getMinute());
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        return String.format("%02d:%02d", hours, minutes);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EmployeeWorktimeRegistrationForm form = new EmployeeWorktimeRegistrationForm();
            form.setVisible(true);
        });
    }
}