package com.bankofcli.ui.swing;


import com.bankofcli.exception.BankException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

// Generic Swing helpers: buttons, dialogs, background task execution.
public class UiSupport {
    private static final Logger logger = LoggerFactory.getLogger(UiSupport.class);
    private final JFrame frame;
    private final JPanel menu;
    private boolean busy;

    public UiSupport(JFrame frame, JPanel menu) { this.frame = frame; this.menu = menu; }

    public void button(String label, Runnable action) {
        JButton b = new JButton(label);
        b.setBackground(new Color(10, 45, 90));
        b.setForeground(new Color(255, 210, 70));
        b.setFont(new Font("Arial", Font.BOLD, 17));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(255, 210, 70), 2));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(220, 50));
        b.setPreferredSize(new Dimension(220, 50));
        b.addActionListener(e -> {
            if (busy) return;
            try { action.run(); } catch (NumberFormatException ex) { error("Enter a valid number."); }
        });
        menu.add(b);
        menu.add(Box.createVerticalStrut(15));
        menu.revalidate();
        menu.repaint();
    }

    public boolean confirm(String title, Object[] fields) {
        return JOptionPane.showConfirmDialog(frame, fields, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public String secret(JPasswordField field) {
        char[] chars = field.getPassword();
        try { return new String(chars); }
        finally { Arrays.fill(chars, '\0'); field.setText(""); }
    }

    public void display(String title, String text) {
        JTextArea area = new JTextArea(text, 12, 40);
        area.setEditable(false);
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(frame, new JScrollPane(area), title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void error(String message) {
        JOptionPane.showMessageDialog(frame, message, "Unable to complete request", JOptionPane.ERROR_MESSAGE);
    }

    public <T> void run(Supplier<T> task, Consumer<T> success) {
        busy = true;
        for (Component c : menu.getComponents()) c.setEnabled(false);
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T, Void>() {
            protected T doInBackground() { return task.get(); }
            protected void done() {
                try { success.accept(get()); }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Request interrupted", e);
                    error("Request interrupted.");
                }
                catch (ExecutionException e) {
                    Throwable c = e.getCause();
                    if ((c instanceof BankException || c instanceof IllegalArgumentException)
                            && c.getCause() == null) {
                        logger.warn("Request rejected: {}", c.getMessage());
                    } else {
                        logger.error("Request failed", c);
                    }
                    error(c instanceof BankException || c instanceof IllegalArgumentException
                            ? c.getMessage() : "Request failed. Check your database connection and logs.");
                } finally {
                    busy = false;
                    for (Component c : menu.getComponents()) c.setEnabled(true);
                    frame.setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
}
