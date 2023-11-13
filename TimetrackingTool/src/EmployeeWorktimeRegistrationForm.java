import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.time.LocalTime;
import javax.swing.table.TableCellEditor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

public class EmployeeWorktimeRegistrationForm extends JFrame {
    private JTable table;
    private LocalDate today;
    private DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    List<String> zeitenList = new ArrayList<>();

    public EmployeeWorktimeRegistrationForm() {

        JPanel panel = new JPanel(new BorderLayout());

        today = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        String monthYear = today.format(monthFormatter);

        String[] columnNames = { "Day", "Day Name", "Begin", "End", "Break", "Hours Target", "Hours As-Is",
                "Plus/Minus", "Absence", "Comment" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2;
            }
        };

        for (int day = 1; day <= today.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(today.getYear(), today.getMonthValue(), day);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
            model.addRow(new Object[] { day, dayName, "", "", "", "", "", "", "", "" });
        }

        table = new JTable(model);

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
                    model.setValueAt("00:45", row, 4);
                } else if (column == 5) {
                    int row = table.getSelectedRow();
                    model.setValueAt("08:00", row, 5);
                } else if (column == 6) {
                    int row = table.getSelectedRow();
                    String beginValue = (String) model.getValueAt(row, 2);
                    String endValue = (String) model.getValueAt(row, 3);
                    String breakValue = (String) model.getValueAt(row, 4);

                    if (!beginValue.isEmpty() && !endValue.isEmpty()) {
                        LocalTime beginTime = LocalTime.parse(beginValue);
                        LocalTime endTime = LocalTime.parse(endValue);
                        Duration workDuration = calculateWorkDuration(beginTime, endTime, breakValue);
                        String hoursAsIs = formatDuration(workDuration);
                        model.setValueAt(hoursAsIs, row, 6);
                    }
                } else if (column == 7) {

                    int row = table.getSelectedRow();
                    String hoursAsIsValue = (String) model.getValueAt(row, 6);
                    String hoursTargetValue = (String) model.getValueAt(row, 5);

                    if (!hoursAsIsValue.isEmpty() && !hoursTargetValue.isEmpty()) {
                        LocalTime hoursAsIsTime = LocalTime.parse(hoursAsIsValue);
                        LocalTime hoursTargetTime = LocalTime.parse(hoursTargetValue);

                        Duration plusMinusDuration = Duration.between(hoursTargetTime, hoursAsIsTime);

                        if (plusMinusDuration.isNegative()) {

                            String plusMinusValue = "-" + formatDuration(plusMinusDuration.negated());
                            model.setValueAt(plusMinusValue, row, 7);
                        } else {
                            String plusMinusValue = formatDuration(plusMinusDuration);
                            model.setValueAt(plusMinusValue, row, 7);
                        }
                    } else {
                        model.setValueAt("", row, 7);
                    }
                } else if (column == 8) {
                    int row = table.getSelectedRow();
                    model.setValueAt("No", row, 8);

                }

            }
        });

        JButton prevMonthButton = new JButton("Prev Month");
        JButton nextMonthButton = new JButton("Next Month");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load Data");
        JButton calculateSumButton = new JButton("Berechnen");

        calculateSumButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openSumWindow();
            }
        });

        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayDataFromFile();
                JOptionPane.showMessageDialog(null, "Data loaded from worktime_data.csv");
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDataToFile();
                JOptionPane.showMessageDialog(null, "Data saved to worktime_data.csv");

            }
        });

        prevMonthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                today = today.minusMonths(1);
                updateTable();

            }
        });

        nextMonthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                today = today.plusMonths(1);
                updateTable();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevMonthButton);
        buttonPanel.add(nextMonthButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(calculateSumButton);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        setTitle(monthYear + " Worktime Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 400);
        add(panel);

        updateTable();
    }

    private String formatLocalTime(LocalTime localTime) {
        return localTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void openSumWindow() {

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        JFrame sumFrame = new JFrame("Summen");
        sumFrame.setSize(300, 200);

        JLabel hoursTargetLabel = new JLabel("Summe Hours Target: ");
        JLabel hoursAsIsLabel = new JLabel("Summe Hours As Is: ");
        JLabel plusMinusLabel = new JLabel("Summe Plus/Minus: ");

        int totalHoursTarget = calculateTotalHoursTarget();
        int totalHoursAsIs = calculateTotalHoursAsIs();
        String totalPlusMinus = calculateTotalPlusMinus();

        JLabel hoursTargetSumLabel = new JLabel(
                String.format("%02d:%02d", totalHoursTarget / 60, totalHoursTarget % 60));
        JLabel hoursAsIsSumLabel = new JLabel(String.format("%02d:%02d", totalHoursAsIs / 60, totalHoursAsIs % 60));
        // JLabel plusMinusSumLabel = new JLabel(String.format("%02d:%02d",
        // totalPlusMinus / 60, totalPlusMinus % 60));
        JLabel plusMinusSumLabel = new JLabel(totalPlusMinus);

        JPanel sumPanel = new JPanel(new GridLayout(3, 2));
        sumPanel.add(hoursTargetLabel);
        sumPanel.add(hoursTargetSumLabel);
        sumPanel.add(hoursAsIsLabel);
        sumPanel.add(hoursAsIsSumLabel);
        sumPanel.add(plusMinusLabel);
        sumPanel.add(plusMinusSumLabel);

        sumFrame.add(sumPanel);

        sumFrame.setVisible(true);
    }

    private int calculateTotalHoursTarget() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        int totalHoursTarget = 0;

        for (int row = 0; row < model.getRowCount(); row++) {
            String hoursTargetValue = (String) model.getValueAt(row, 5);
            if (!hoursTargetValue.isEmpty()) {
                String[] parts = hoursTargetValue.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                totalHoursTarget += hours * 60 + minutes;
            }
        }
        return totalHoursTarget;
    }

    private int calculateTotalHoursAsIs() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int totalHoursAsIs = 0;

        for (int row = 0; row < model.getRowCount(); row++) {
            String hoursAsIsValue = (String) model.getValueAt(row, 6);
            if (!hoursAsIsValue.isEmpty()) {
                String[] parts = hoursAsIsValue.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                totalHoursAsIs += hours * 60 + minutes;
            }
        }
        return totalHoursAsIs;
    }

    private String formatTime(int hours, int minutes) {
        String sign = (hours < 0) ? "-" : "";
        hours = Math.abs(hours);

        return String.format("%s%02d:%02d", sign, hours, minutes);
    }

    private String calculateTotalPlusMinus() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        int totalPlusMinusHours = 0;
        int totalPlusMinusMinutes = 0;

        for (int row = 0; row < model.getRowCount(); row++) {
            String plusMinusValue = (String) model.getValueAt(row, 7);
            if (!plusMinusValue.isEmpty()) {
                String[] parts = plusMinusValue.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);

                // Check if the value is negative
                boolean isNegative = plusMinusValue.startsWith("-");
                if (isNegative) {
                    totalPlusMinusHours -= hours;
                    totalPlusMinusMinutes -= minutes;
                } else {
                    totalPlusMinusHours += hours;
                    totalPlusMinusMinutes += minutes;
                }
            }
        }

        // Adjust totalPlusMinus if minutes exceed 60
        totalPlusMinusHours += totalPlusMinusMinutes / 60;
        totalPlusMinusMinutes = Math.abs(totalPlusMinusMinutes) % 60;

        return formatTime(totalPlusMinusHours, totalPlusMinusMinutes);
    }

    public void loadDataFromFile() {
        try {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0); // L schen Sie alle vorhandenen Daten in der Tabelle

            BufferedReader reader = new BufferedReader(new FileReader("worktime_data.csv"));
            String line;

            if ((line = reader.readLine()) != null) {
                String[] headers = line.split(",");
                model.setColumnIdentifiers(headers);
            }

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                model.addRow(data);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayDataFromFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("worktime_data.csv"));
            DefaultTableModel model = new DefaultTableModel();

            String headerLine = reader.readLine();
            if (headerLine != null) {
                String[] headers = headerLine.split(",");
                model.setColumnIdentifiers(headers);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",");

                if (rowData.length > 1) {
                    model.addRow(rowData);
                }
            }

            reader.close();

            JTable table = new JTable(model);

            JFrame frame = new JFrame("Loaded Data");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new JScrollPane(table));
            frame.pack();
            frame.setVisible(true);

            // Add button that allows applying the displayed data to the current table
            JPanel buttonPanel = new JPanel();
            JButton applyButton = new JButton("Apply");

            buttonPanel.add(applyButton);
            frame.add(buttonPanel, BorderLayout.NORTH);

            applyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultTableModel currentModel = (DefaultTableModel) EmployeeWorktimeRegistrationForm.this.table
                            .getModel();
                    currentModel.setRowCount(0);

                    for (int row = 0; row < model.getRowCount(); row++) {
                        Object[] rowData = new Object[model.getColumnCount()];
                        for (int column = 0; column < model.getColumnCount(); column++) {
                            rowData[column] = model.getValueAt(row, column);
                        }
                        currentModel.addRow(rowData);
                    }

                    frame.dispose();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDataToFile() {
        try {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int rowCount = model.getRowCount();

            FileWriter writer = new FileWriter("worktime_data.csv");

            // Schreiben Sie die Spalten berschriften in die CSV-Datei
            for (int i = 0; i < model.getColumnCount(); i++) {
                writer.append(model.getColumnName(i));
                if (i < model.getColumnCount() - 1) {
                    writer.append(",");
                } else {
                    writer.append("\n");
                }
            }

            // Schreiben Sie die Zeilendaten in die CSV-Datei
            for (int row = 0; row < rowCount; row++) {
                for (int column = 0; column < model.getColumnCount(); column++) {
                    writer.append(model.getValueAt(row, column).toString());
                    if (column < model.getColumnCount() - 1) {
                        writer.append(",");
                    } else {
                        writer.append("\n");
                    }
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Duration calculateWorkDuration(LocalTime beginTime, LocalTime endTime, String breakValue) {
        Duration workDuration = Duration.between(beginTime, endTime);

        if (!breakValue.isEmpty()) {
            LocalTime breakTime = LocalTime.parse(breakValue);
            workDuration = workDuration.minus(Duration.ofHours(breakTime.getHour()).plusMinutes(breakTime.getMinute()));
        }

        return workDuration;
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        return String.format("%02d:%02d", hours, minutes);
    }

    private String formatDuration(int hours) {
        return String.format("%02d:00", hours);
    }

    private void updateTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (int day = 1; day <= today.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(today.getYear(), today.getMonthValue(), day);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
            model.addRow(new Object[] { day, dayName, "", "", "", "", "", "", "", "" });
        }
        // model.addRow(new Object[]{"", "", "", "", "", "", "", "", ""});

        setTitle(today.format(monthFormatter) + " Worktime Registration"); // Aktualisiere den Fenstertitel

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EmployeeWorktimeRegistrationForm form = new EmployeeWorktimeRegistrationForm();
            form.setVisible(true);
        });
    }
}
