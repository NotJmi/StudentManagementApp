package StudentManagementSystem;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class ModernTheme {

    // Palette
    static final Color NAVY      = new Color(0x1E293B);
    static final Color NAVY_HOVER= new Color(0x334155);
    static final Color BLUE      = new Color(0x2563EB);
    static final Color BLUE_DARK = new Color(0x1D4ED8);
    static final Color GRAY_BTN  = new Color(0xE2E8F0);
    static final Color RED       = new Color(0xDC2626);
    static final Color BG        = Color.WHITE;
    static final Color TEXT      = new Color(0x0F172A);
    static final Color MUTED     = new Color(0x64748B);
    static final Color BORDER    = new Color(0xCBD5E1);

    static final Font BASE  = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font BOLD  = new Font("Segoe UI", Font.BOLD, 13);
    static final Font TITLE = new Font("Segoe UI", Font.BOLD, 22);

    // ---------- Buttons ----------
    static void primary(JButton b)   { style(b, BLUE, Color.WHITE, BLUE_DARK); }
    static void secondary(JButton b) { style(b, GRAY_BTN, TEXT, BORDER); }
    static void danger(JButton b)    { style(b, RED, Color.WHITE, RED.darker()); }
    static void sidebar(JButton b)   {
        style(b, NAVY, Color.WHITE, NAVY_HOVER);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
    }

    private static void style(JButton b, Color bg, Color fg, Color hover) {
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(BOLD);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        for (java.awt.event.MouseListener l : b.getMouseListeners()) {
            if (l instanceof HoverListener) b.removeMouseListener(l);
        }
        b.addMouseListener(new HoverListener(b, bg, hover));
    }

    private static class HoverListener extends java.awt.event.MouseAdapter {
        final JButton b; final Color normal, hover;
        HoverListener(JButton b, Color n, Color h) { this.b = b; normal = n; hover = h; }
        public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
        public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(normal); }
    }

    // ---------- Inputs ----------
    static void field(JTextField f) {
        f.setFont(BASE);
        f.setForeground(TEXT);
        f.setBackground(Color.WHITE);
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true), new EmptyBorder(6, 10, 6, 10)));
    }

    static void combo(JComboBox<?> c) {
        c.setFont(BASE);
        c.setBackground(Color.WHITE);
        c.setForeground(TEXT);
    }

    // ---------- Labels ----------
    static void title(JLabel l)    { l.setFont(TITLE); l.setForeground(TEXT); }
    static void subtitle(JLabel l) { l.setFont(BASE);  l.setForeground(MUTED); }
    static void label(JLabel l)    { l.setFont(BOLD);  l.setForeground(TEXT); }

    static void statCard(JLabel l, Color accent) {
        l.setOpaque(true);
        l.setBackground(new Color(0xF1F5F9));
        l.setForeground(TEXT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setBorder(new CompoundBorder(
            new MatteBorder(0, 5, 0, 0, accent),
            new EmptyBorder(14, 14, 14, 14)));
    }

    // ---------- Tables ----------
    static void table(JTable t) {
        t.setFont(BASE);
        t.setRowHeight(30);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xE2E8F0));
        t.setSelectionBackground(new Color(0xDBEAFE));
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tb, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(tb, v, s, f, r, c);
                setBackground(NAVY);
                setForeground(Color.WHITE);
                setFont(BOLD);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
        h.setPreferredSize(new Dimension(100, 34));
        h.setReorderingAllowed(false);

        // Alternating row colors
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tb, Object v, boolean sel, boolean f, int r, int c) {
                super.getTableCellRendererComponent(tb, v, sel, f, r, c);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                return this;
            }
        });
    }

    static void scroll(JScrollPane sp) {
        sp.setBorder(new LineBorder(BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
    }

    static void panel(JPanel p) { p.setBackground(BG); }
}