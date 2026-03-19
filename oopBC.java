
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class oopBC extends JFrame {

    static final String FILE = "wrath.txt";
    static BankAccount[] accounts = new BankAccount[100];
    static int count = 0;
    static long nextId = 66671001;
    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
    private int loggedIn = -1;

    static final Color BG = new Color(11, 13, 18), SURFACE = new Color(18, 21, 28),
            CARD = new Color(24, 28, 38), HOVER = new Color(30, 35, 48),
            BORDER = new Color(40, 46, 62), ACCENT = new Color(56, 128, 240),
            ACCH = new Color(38, 98, 200), GREEN = new Color(48, 190, 120),
            RED = new Color(230, 75, 75), GOLD = new Color(220, 170, 40),
            T1 = new Color(228, 232, 242), T2 = new Color(130, 142, 170), T3 = new Color(64, 74, 98);

    static final Font FH = new Font("Segoe UI", Font.BOLD, 15), FS = new Font("Segoe UI", Font.BOLD, 13),
            FB = new Font("Segoe UI", Font.PLAIN, 13), FM = new Font("Segoe UI", Font.PLAIN, 11),
            FL = new Font("Segoe UI", Font.BOLD, 10), FN = new Font("Segoe UI", Font.BOLD, 14),
            FG = new Font("Segoe UI", Font.BOLD, 26), FEM = new Font("Segoe UI Emoji", Font.PLAIN, 18);

    static final double LOAN_MIN = 500;
    static final double[] TIER_REQ = {10000, 5000, 2000, 500}, TIER_LIM = {50000, 15000, 5000, 1000};
    static final String[] TIER_NAME = {"\uD83D\uDC8E Platinum", "\uD83E\uDD47 Gold", "\uD83E\uDD48 Silver", "\uD83E\uDD49 Bronze"};

    public oopBC() {
        patchUI();
        setTitle("Philippine National Banc");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 760);
        setMinimumSize(new Dimension(460, 760));
        setMaximumSize(new Dimension(460, 760));
        setResizable(false);
        setLocationRelativeTo(null);
        loadData();
        showLogin();
        setVisible(true);
    }

    static void patchUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        Border nb = cb(BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(9, 12, 9, 12));
        String[] tf = {"TextField", "PasswordField"};
        for (String k : tf) {
            UIManager.put(k + ".background", CARD);
            UIManager.put(k + ".foreground", T1);
            UIManager.put(k + ".caretForeground", T1);
            UIManager.put(k + ".selectionBackground", ACCENT);
            UIManager.put(k + ".selectionForeground", Color.WHITE);
            UIManager.put(k + ".border", nb);
            UIManager.put(k + ".font", FB);
        }
        UIManager.put("Panel.background", SURFACE);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", T1);
        UIManager.put("Button.background", CARD);
        UIManager.put("Button.foreground", T1);
        UIManager.put("Button.border", cb(BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(6, 16, 6, 16)));
        UIManager.put("Button.font", FS);
        UIManager.put("Button.focus", CARD);
        UIManager.put("Label.foreground", T1);
        UIManager.put("Label.background", SURFACE);
        UIManager.put("Label.font", FB);
        UIManager.put("ScrollBar.background", BG);
        UIManager.put("ScrollBar.thumb", BORDER);
        UIManager.put("ScrollBar.thumbShadow", BORDER);
        UIManager.put("ScrollBar.thumbHighlight", BORDER);
        UIManager.put("ScrollBar.track", BG);
    }

    static Border cb(Border a, Border b) {
        return BorderFactory.createCompoundBorder(a, b);
    }

    static Border focusBorder() {
        return cb(BorderFactory.createLineBorder(ACCENT, 1), new EmptyBorder(9, 12, 9, 12));
    }

    static Border normBorder() {
        return cb(BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(9, 12, 9, 12));
    }

    void showLogin() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG);
        JPanel card = roundCard(360, 510, 18);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(36, 36, 36, 36));

        JPanel logo = flow(6);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        logo.add(lbl("\u2B22", new Font("Segoe UI", Font.BOLD, 15), ACCENT));
        logo.add(lbl("PNB", new Font("Segoe UI", Font.BOLD, 14), T1));
        card.add(logo);
        card.add(vsp(22));

        JLabel title = lbl("Sign In", new Font("Segoe UI", Font.BOLD, 22), T1);
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = lbl("Philippine National Banc", FM, T3);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        card.add(title);
        card.add(vsp(4));
        card.add(sub);
        card.add(vsp(26));

        JTextField idF = field();
        idF.setAlignmentX(CENTER_ALIGNMENT);
        JPasswordField pwF = pass();
        pwF.setAlignmentX(CENTER_ALIGNMENT);
        capRow(card, "ACCOUNT ID");
        card.add(idF);
        card.add(vsp(14));
        capRow(card, "PASSWORD");
        card.add(pwF);
        card.add(vsp(6));

        JPanel fp = flow(FlowLayout.RIGHT, 0);
        fp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        JLabel fpL = link("Forgot password?");
        fpL.addMouseListener(click(e -> showForgotPassword()));
        fp.add(fpL);
        card.add(fp);
        card.add(vsp(22));

        JButton si = btnPrimary("Sign In");
        si.setAlignmentX(CENTER_ALIGNMENT);
        si.addActionListener(e -> {
            String raw = idF.getText().trim();
            if (raw.isEmpty()) {
                err("Enter your Account ID.");
                return;
            }
            try {
                long id = Long.parseLong(raw);
                int i = findAcc(id);
                if (i < 0) {
                    err("Account not found.");
                    return;
                }
                if (!accounts[i].getPassword().equals(new String(pwF.getPassword()))) {
                    err("Incorrect password.");
                    return;
                }
                loggedIn = i;
                showDashboard();
            } catch (NumberFormatException ex) {
                err("Account ID must be numeric.");
            }
        });
        card.add(si);
        card.add(vsp(10));

        JButton ca = btnSecondary("Create New Account");
        ca.setAlignmentX(CENTER_ALIGNMENT);
        ca.addActionListener(e -> showCreateAccount());
        card.add(ca);
        card.add(vsp(20));
        card.add(divider("or"));
        card.add(vsp(14));

        JPanel g = flow(FlowLayout.CENTER, 0);
        g.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        JLabel gl = link("\u2192  Currency Exchange  (no login required)");
        gl.addMouseListener(click(e -> showCurrencyExchange()));
        g.add(gl);
        card.add(g);

        root.add(card, new GridBagConstraints());
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(BG);
        w.add(root, BorderLayout.CENTER);
        w.add(footer(), BorderLayout.SOUTH);
        swap(w);
    }

    void showCreateAccount() {
        JTextField nF = field();
        JPasswordField pF = pass(), cF = pass();
        JPanel p = dlg("Create Account", "Fill in the details below");
        row(p, "FULL NAME", nF);
        row(p, "PASSWORD", pF);
        row(p, "CONFIRM PASSWORD", cF);
        if (!ok(p, "Create Account")) {
            return;
        }
        String name = nF.getText().trim(), pass = new String(pF.getPassword()), conf = new String(cF.getPassword());
        if (name.isEmpty() || pass.isEmpty()) {
            err("All fields are required.");
            return;
        }
        if (!pass.equals(conf)) {
            err("Passwords do not match.");
            return;
        }
        if (pass.length() < 4) {
            err("Password must be at least 4 characters.");
            return;
        }
        accounts[count++] = new BankAccount(nextId++, name, pass);
        saveData();
        msg("Account Created\n\nYour Account ID:  " + (nextId - 1) + "\n\nKeep this ID \u2014 you need it to sign in.");
    }

    void showForgotPassword() {
        JTextField iF = field();
        JPasswordField nF = pass(), cF = pass();
        JPanel p = dlg("Reset Password", "Enter your Account ID and new password");
        row(p, "ACCOUNT ID", iF);
        row(p, "NEW PASSWORD", nF);
        row(p, "CONFIRM PASSWORD", cF);
        if (!ok(p, "Reset Password")) {
            return;
        }
        try {
            int i = findAcc(Long.parseLong(iF.getText().trim()));
            if (i < 0) {
                err("Account not found.");
                return;
            }
            String np = new String(nF.getPassword());
            if (np.length() < 4) {
                err("Password must be at least 4 characters.");
                return;
            }
            if (!np.equals(new String(cF.getPassword()))) {
                err("Passwords do not match.");
                return;
            }
            BankAccount u = new BankAccount(accounts[i].getId(), accounts[i].getName(), np);
            u.copyFrom(accounts[i]);
            accounts[i] = u;
            saveData();
            msg("Password updated. You can now sign in.");
        } catch (NumberFormatException ex) {
            err("Account ID must be numeric.");
        }
    }

    void showDashboard() {
        BankAccount a = accounts[loggedIn];
        a.applySavingsInterest();
        a.applyLoanInterest();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SURFACE);
        bar.setBorder(cb(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(12, 22, 12, 22)));
        JPanel bL = flow(0);
        bL.setOpaque(false);
        bL.add(lbl("PNB", FL, ACCENT));
        bL.add(lbl("   |   ", FB, BORDER));
        bL.add(lbl(a.getName(), FS, T1));
        JPanel bR = flow(FlowLayout.RIGHT, 8);
        bR.setOpaque(false);
        bR.add(lbl("ID " + a.getId(), FM, T3));
        JButton out = flatBtn("Sign Out");
        out.addActionListener(e -> {
            saveData();
            loggedIn = -1;
            showLogin();
        });
        bR.add(out);
        bar.add(bL, BorderLayout.WEST);
        bar.add(bR, BorderLayout.EAST);
        root.add(bar, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(18, 18, 28, 18));

        double net = a.getWallet() + a.getSavings() - a.getLoan();
        body.add(netCard(net));
        body.add(vsp(10));

        JPanel tiles = new JPanel(new GridLayout(1, 3, 8, 0));
        tiles.setOpaque(false);
        tiles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        tiles.setPreferredSize(new Dimension(1, 76));
        tiles.add(statTile("WALLET", a.getWallet(), ACCENT));
        tiles.add(statTile("SAVINGS", a.getSavings(), GREEN));
        tiles.add(statTile("LOAN", a.getLoan(), a.getLoan() > 0 ? RED : T3));
        body.add(tiles);
        body.add(vsp(22));

        JLabel qa = lbl("QUICK ACTIONS", FL, T3);
        qa.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(qa);
        body.add(vsp(10));

        JPanel grid = new JPanel(new GridLayout(2, 4, 8, 8));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        grid.setPreferredSize(new Dimension(1, 128));
        grid.add(tile("\uD83D\uDCB0", "Deposit", e -> quickDeposit()));
        grid.add(tile("\uD83C\uDFE7", "Withdraw", e -> quickWithdraw()));
        grid.add(tile("\uD83D\uDCC8", "Save", e -> showSavings()));
        grid.add(tile("\uD83E\uDD1D", "Borrow", e -> showLoan()));
        grid.add(tile("\uD83D\uDCCA", "Summary", e -> showSummary()));
        grid.add(tile("\uD83D\uDCB1", "Exchange", e -> showCurrencyExchange()));
        grid.add(tile("\uD83D\uDCE4", "Transfer", e -> showTransfer()));
        grid.add(tile("\uD83D\uDCDC", "History", e -> showHistory()));
        body.add(grid);

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(BG);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        root.add(sp, BorderLayout.CENTER);
        root.add(footer(), BorderLayout.SOUTH);
        swap(root);
    }

    JPanel netCard(double net) {
        JPanel nc = roundCard(-1, 100, 14);
        nc.setLayout(new BorderLayout());
        nc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        nc.setPreferredSize(new Dimension(1, 100));
        nc.setBorder(new EmptyBorder(16, 20, 16, 20));
        JPanel L = new JPanel();
        L.setOpaque(false);
        L.setLayout(new BoxLayout(L, BoxLayout.Y_AXIS));
        L.add(lbl("TOTAL NET ASSETS", FL, T3));
        L.add(vsp(4));
        L.add(lbl("\u20B1 " + fmtK(net), FG, T1));
        L.add(vsp(3));
        L.add(lbl("Updated " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, h:mm a")), FM, T3));
        boolean h = net >= 0;
        JLabel chip = new JLabel(h ? "  Healthy  " : "  In Debt  ");
        chip.setFont(FM);
        chip.setForeground(h ? GREEN : RED);
        chip.setBorder(BorderFactory.createLineBorder(h ? GREEN : RED, 1));
        chip.setHorizontalAlignment(SwingConstants.CENTER);
        nc.add(L, BorderLayout.WEST);
        nc.add(chip, BorderLayout.EAST);
        return nc;
    }

    JPanel statTile(String label, double val, Color vc) {
        JPanel t = roundCard(-1, 76, 10);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setBorder(new EmptyBorder(11, 12, 11, 12));
        t.add(lbl(label, FL, T3));
        t.add(vsp(5));
        t.add(lbl("\u20B1 " + fmtK(val), FN, vc));
        return t;
    }

    JPanel tile(String emoji, String label, ActionListener al) {
        JPanel t = roundCard(-1, -1, 10);
        t.setLayout(new GridBagLayout());
        t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        JLabel eL = new JLabel(emoji, SwingConstants.CENTER);
        eL.setFont(FEM);
        eL.setAlignmentX(CENTER_ALIGNMENT);
        JLabel tL = new JLabel(label, SwingConstants.CENTER);
        tL.setFont(FM);
        tL.setForeground(T2);
        tL.setAlignmentX(CENTER_ALIGNMENT);
        col.add(eL);
        col.add(vsp(4));
        col.add(tL);
        t.add(col, new GridBagConstraints());
        t.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                t.setBackground(HOVER);
                t.repaint();
            }

            public void mouseExited(MouseEvent e) {
                t.setBackground(CARD);
                t.repaint();
            }

            public void mouseClicked(MouseEvent e) {
                al.actionPerformed(new ActionEvent(t, 0, ""));
            }
        });
        return t;
    }

    void quickDeposit() {
        String s = askAmt("Deposit", "Amount to deposit into wallet");
        if (s == null) {
            return;
        }
        double a = parseAmt(s);
        if (a <= 0) {
            return;
        }
        accounts[loggedIn].depositWallet(a);
        accounts[loggedIn].addTx("\u2B06 \u20B1" + fmt(a) + " \u2014 Deposit");
        saveData();
        msg("Deposited \u20B1" + fmt(a) + ".");
        showDashboard();
    }

    void quickWithdraw() {
        String s = askAmt("Withdraw", "Amount to withdraw from wallet");
        if (s == null) {
            return;
        }
        double a = parseAmt(s);
        if (a <= 0) {
            return;
        }
        if (a > accounts[loggedIn].getWallet()) {
            err("Insufficient balance.");
            return;
        }
        accounts[loggedIn].withdrawWallet(a);
        accounts[loggedIn].addTx("\u2B07 \u20B1" + fmt(a) + " \u2014 Withdrawal");
        saveData();
        msg("Withdrew \u20B1" + fmt(a) + ".");
        showDashboard();
    }

    void showSavings() {
        accounts[loggedIn].applySavingsInterest();
        BankAccount a = accounts[loggedIn];
        JPanel p = dlg("Savings Account", "1% interest per day");
        p.add(infoGrid(new String[]{"Savings", "Wallet"},
                new String[]{"\u20B1 " + fmt(a.getSavings()), "\u20B1 " + fmt(a.getWallet())},
                new Color[]{GREEN, ACCENT}));
        p.add(vsp(14));
        int[] pick = {-1};
        JDialog[] ref = {null};
        JButton d = optBtn("Deposit to Savings", GREEN), w = optBtn("Withdraw from Savings", ACCENT), c = optBtn("Cancel", T3);
        d.addActionListener(e -> {
            pick[0] = 0;
            ref[0].dispose();
        });
        w.addActionListener(e -> {
            pick[0] = 1;
            ref[0].dispose();
        });
        c.addActionListener(e -> ref[0].dispose());
        p.add(d);
        p.add(vsp(8));
        p.add(w);
        p.add(vsp(8));
        p.add(c);
        ref[0] = mkDlg(p, "Savings");
        ref[0].setVisible(true);
        if (pick[0] == 0) {
            String s = askAmt("Deposit to Savings", "Move from wallet to savings");
            if (s == null) {
                return;
            }
            double amt = parseAmt(s);
            if (amt <= 0) {
                return;
            }
            if (amt > a.getWallet()) {
                err("Insufficient wallet balance.");
                return;
            }
            a.depositSavings(amt);
            a.addTx("\u2192 \u20B1" + fmt(amt) + " \u2014 To Savings");
            saveData();
            msg("\u20B1" + fmt(amt) + " moved to savings.");
            showDashboard();
        } else if (pick[0] == 1) {
            String s = askAmt("Withdraw from Savings", "Move back to wallet");
            if (s == null) {
                return;
            }
            double amt = parseAmt(s);
            if (amt <= 0) {
                return;
            }
            if (amt > a.getSavings()) {
                err("Insufficient savings.");
                return;
            }
            a.withdrawSavings(amt);
            a.addTx("\u2190 \u20B1" + fmt(amt) + " \u2014 From Savings");
            saveData();
            msg("\u20B1" + fmt(amt) + " moved to wallet.");
            showDashboard();
        }
    }

    String tierName(double b) {
        for (int i = 0; i < TIER_REQ.length; i++) {
            if (b >= TIER_REQ[i]) {
                return TIER_NAME[i];

            }
        }
        return "None";
    }

    double tierLim(double b) {
        for (int i = 0; i < TIER_REQ.length; i++) {
            if (b >= TIER_REQ[i]) {
                return TIER_LIM[i];

            }
        }
        return 0;
    }

    void showLoan() {
        accounts[loggedIn].applyLoanInterest();
        BankAccount a = accounts[loggedIn];
        double bal = a.getWallet() + a.getSavings();
        if (bal < LOAN_MIN) {
            JPanel p = dlg("Loan Unavailable", "Minimum \u20B1" + fmt(LOAN_MIN) + " total balance required");
            String[] ln = new String[4];
            String[] lv = new String[4];
            Color[] lc = new Color[4];
            for (int i = 0; i < 4; i++) {
                ln[i] = TIER_NAME[i];
                lv[i] = "\u20B1" + fmt(TIER_REQ[i]) + " \u2192 \u20B1" + fmt(TIER_LIM[i]);
                lc[i] = GOLD;
            }
            p.add(infoGrid(ln, lv, lc));
            mkDlg(p, "Loan Tiers").setVisible(true);
            return;
        }
        double lim = tierLim(bal);
        String tier = tierName(bal);
        JPanel p = dlg("Loan Management", tier + " \u00B7 Max \u20B1" + fmt(lim));
        p.add(infoGrid(new String[]{"Wallet", "Loan"},
                new String[]{"\u20B1 " + fmt(a.getWallet()), "\u20B1 " + fmt(a.getLoan())},
                new Color[]{ACCENT, a.getLoan() > 0 ? RED : T3}));
        p.add(vsp(6));
        JLabel wi = lbl("5% daily interest on outstanding loans", FM, RED);
        wi.setAlignmentX(CENTER_ALIGNMENT);
        p.add(wi);
        p.add(vsp(14));
        int[] pick = {-1};
        JDialog[] ref = {null};
        JButton b = optBtn("Borrow Money", GREEN), r = optBtn("Repay Loan", ACCENT), c = optBtn("Cancel", T3);
        b.addActionListener(e -> {
            pick[0] = 0;
            ref[0].dispose();
        });
        r.addActionListener(e -> {
            pick[0] = 1;
            ref[0].dispose();
        });
        c.addActionListener(e -> ref[0].dispose());
        p.add(b);
        p.add(vsp(8));
        p.add(r);
        p.add(vsp(8));
        p.add(c);
        ref[0] = mkDlg(p, "Loan");
        ref[0].setVisible(true);
        if (pick[0] == 0) {
            String s = askAmt("Borrow", "Max \u20B1" + fmt(lim) + " | 5% daily interest");
            if (s == null) {
                return;
            }
            double amt = parseAmt(s);
            if (amt <= 0) {
                return;
            }
            if (amt > lim) {
                err("Exceeds tier limit of \u20B1" + fmt(lim));
                return;
            }
            if (a.getLoan() + amt > lim) {
                err("Would exceed limit. Current: \u20B1" + fmt(a.getLoan()));
                return;
            }
            a.borrowLoan(amt);
            a.addTx("\uD83D\uDCB3 \u20B1" + fmt(amt) + " \u2014 Loan (" + tier + ")");
            saveData();
            msg("Loan of \u20B1" + fmt(amt) + " approved.");
            showDashboard();
        } else if (pick[0] == 1) {
            if (a.getLoan() <= 0) {
                msg("No outstanding loan.");
                return;
            }
            String s = askAmt("Repay Loan", "Payment deducted from wallet");
            if (s == null) {
                return;
            }
            double amt = parseAmt(s);
            if (amt <= 0) {
                return;
            }
            if (amt > a.getWallet()) {
                err("Insufficient wallet balance.");
                return;
            }
            a.payLoan(amt);
            a.addTx("\u2713 \u20B1" + fmt(amt) + " \u2014 Loan Payment");
            saveData();
            msg("Payment of \u20B1" + fmt(amt) + " applied.");
            showDashboard();
        }
    }

    void showSummary() {
        accounts[loggedIn].applySavingsInterest();
        accounts[loggedIn].applyLoanInterest();
        BankAccount a = accounts[loggedIn];
        double net = a.getWallet() + a.getSavings() - a.getLoan();
        JPanel p = dlg("Account Summary", a.getName() + " \u00B7 ID " + a.getId());
        p.add(sumRow("Wallet", "\u20B1 " + fmt(a.getWallet()), ACCENT));
        p.add(vsp(10));
        p.add(sumRow("Savings", "\u20B1 " + fmt(a.getSavings()), GREEN));
        p.add(vsp(10));
        p.add(sumRow("Loan", "\u20B1 " + fmt(a.getLoan()), a.getLoan() > 0 ? RED : T3));
        p.add(vsp(14));
        p.add(hr());
        p.add(vsp(12));
        p.add(sumRow("Net Assets", "\u20B1 " + fmt(net), net >= 0 ? GREEN : RED));
        mkDlg(p, "Summary").setVisible(true);
    }

    void showCurrencyExchange() {
        String[] opts = {"\u20B1 Peso \u2192 Dollar $", "$ Dollar \u2192 Peso \u20B1",
            "\u20AC Euro \u2192 Dollar $", "\u20AC Euro \u2192 Peso \u20B1",
            "$ Dollar \u2192 Euro \u20AC", "\u00A5 Yen \u2192 Peso \u20B1"};
        JPanel p = dlg("Currency Exchange", "$1=\u20B156 \u00B7 \u20AC1=\u20B160 \u00B7 \u20AC1=$1.08 \u00B7 \u00A51=\u20B10.38");
        int[] pick = {-1};
        JDialog[] ref = {null};
        for (int i = 0; i < opts.length; i++) {
            final int idx = i;
            JButton b = optBtn(opts[i], ACCENT);
            b.addActionListener(e -> {
                pick[0] = idx;
                ref[0].dispose();
            });
            p.add(b);
            p.add(vsp(7));
        }
        JButton cx = optBtn("Cancel", T3);
        cx.addActionListener(e -> ref[0].dispose());
        p.add(cx);
        ref[0] = mkDlg(p, "Exchange");
        ref[0].setVisible(true);
        if (pick[0] < 0) {
            return;
        }
        String s = askAmt("Currency Exchange", opts[pick[0]]);
        if (s == null) {
            return;
        }
        double a = parseAmt(s);
        if (a <= 0) {
            return;
        }
        String[] res = {"\u20B1" + fmt(a) + " = $" + fmt(a / 56), "$" + fmt(a) + " = \u20B1" + fmt(a * 56),
            "\u20AC" + fmt(a) + " = $" + fmt(a * 1.08), "\u20AC" + fmt(a) + " = \u20B1" + fmt(a * 60),
            "$" + fmt(a) + " = \u20AC" + fmt(a / 1.08), "\u00A5" + fmt(a) + " = \u20B1" + fmt(a * 0.38)};
        msg("Result\n\n" + res[pick[0]] + "\n\n(Approximate rates)");
    }

    void showTransfer() {
        BankAccount snd = accounts[loggedIn];
        JTextField toId = field(), toAmt = field(), toNote = field();
        JPanel p = dlg("Transfer Funds", "Available: \u20B1" + fmt(snd.getWallet()));
        row(p, "RECIPIENT ACCOUNT ID", toId);
        row(p, "AMOUNT (\u20B1)", toAmt);
        row(p, "NOTE (OPTIONAL)", toNote);
        if (!ok(p, "Transfer")) {
            return;
        }
        long rid;
        try {
            rid = Long.parseLong(toId.getText().trim());
        } catch (NumberFormatException ex) {
            err("Recipient ID must be numeric.");
            return;
        }
        if (rid == snd.getId()) {
            err("Cannot transfer to yourself.");
            return;
        }
        int ri = findAcc(rid);
        if (ri < 0) {
            err("Account #" + rid + " not found.");
            return;
        }
        double a = parseAmt(toAmt.getText().trim());
        if (a <= 0) {
            return;
        }
        if (a > snd.getWallet()) {
            err("Insufficient balance.");
            return;
        }
        BankAccount rcv = accounts[ri];
        String note = toNote.getText().trim().isEmpty() ? "No note" : toNote.getText().trim();
        JPanel cp = dlg("Confirm Transfer", "Review before proceeding");
        cp.add(sumRow("From", snd.getName() + " (#" + snd.getId() + ")", T1));
        cp.add(vsp(8));
        cp.add(sumRow("To", rcv.getName() + " (#" + rcv.getId() + ")", ACCENT));
        cp.add(vsp(8));
        cp.add(sumRow("Amount", "\u20B1 " + fmt(a), GREEN));
        cp.add(vsp(8));
        cp.add(sumRow("Note", note, T2));
        cp.add(vsp(14));
        cp.add(hr());
        cp.add(vsp(12));
        double after = snd.getWallet() - a;
        cp.add(sumRow("Balance After", "\u20B1 " + fmt(after), after >= 0 ? T1 : RED));
        if (!ok(cp, "Confirm Transfer")) {
            return;
        }
        snd.withdrawWallet(a);
        rcv.depositWallet(a);
        String nt = note.equals("No note") ? "" : " \u201C" + note + "\u201D";
        snd.addTx("\uD83D\uDCE4 \u20B1" + fmt(a) + " \u2014 To " + rcv.getName() + nt);
        rcv.addTx("\uD83D\uDCE5 \u20B1" + fmt(a) + " \u2014 From " + snd.getName() + nt);
        saveData();
        msg("Transfer complete.\n\u20B1" + fmt(a) + " sent to " + rcv.getName());
        showDashboard();
    }

    void showHistory() {
        BankAccount a = accounts[loggedIn];
        String[] txs = a.getLastTransactions();
        JPanel p = dlg("Transaction History", a.getName() + " \u00B7 Last " + txs.length + " entries");
        if (txs.length == 0) {
            JLabel el = lbl("No transactions yet.", FB, T3);
            el.setAlignmentX(CENTER_ALIGNMENT);
            p.add(el);
        } else {
            for (int i = 0; i < txs.length; i++) {
                String tx = txs[i];
                Color c = T2;
                if (tx.contains("Deposit") || tx.contains("\u2192") || tx.contains("From ")) {
                    c = GREEN;
                } else if (tx.contains("Withdrawal") || tx.contains("To ")) {
                    c = RED;
                } else if (tx.contains("Loan (")) {
                    c = GOLD;
                } else if (tx.contains("Payment")) {
                    c = ACCENT;
                }
                JPanel r = new JPanel(new BorderLayout());
                r.setBackground(i % 2 == 0 ? CARD : HOVER);
                r.setBorder(new EmptyBorder(10, 12, 10, 12));
                r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
                r.add(lbl(tx, FB, c));
                p.add(r);
                if (i < txs.length - 1) {
                    p.add(hr());
                }
            }
        }
        mkDlg(p, "History").setVisible(true);
    }

    JPanel roundCard(int w, int h, int r) {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, r, r);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBackground(CARD);
        if (w > 0 && h > 0) {
            p.setPreferredSize(new Dimension(w, h));
        }
        return p;
    }

    JTextField field() {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setPreferredSize(new Dimension(380, 42));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(focusBorder());
            }

            public void focusLost(FocusEvent e) {
                f.setBorder(normBorder());
            }
        });
        return f;
    }

    JPasswordField pass() {
        JPasswordField f = new JPasswordField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setPreferredSize(new Dimension(380, 42));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(focusBorder());
            }

            public void focusLost(FocusEvent e) {
                f.setBorder(normBorder());
            }
        });
        return f;
    }

    JButton btnPrimary(String t) {
        JButton b = new JButton(t);
        b.setFont(FS);
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(11, 16, 11, 16));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(ACCH);
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(ACCENT);
            }
        });
        return b;
    }

    JButton btnSecondary(String t) {
        JButton b = new JButton(t);
        b.setFont(FS);
        b.setForeground(T2);
        b.setBackground(CARD);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(cb(BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(10, 16, 10, 16)));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(HOVER);
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(CARD);
            }
        });
        return b;
    }

    JButton flatBtn(String t) {
        JButton b = new JButton(t);
        b.setFont(FM);
        b.setForeground(T2);
        b.setBackground(CARD);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(cb(BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(5, 12, 5, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    JButton optBtn(String t, Color ac) {
        JButton b = new JButton(t);
        b.setFont(FS);
        b.setForeground(ac);
        b.setBackground(CARD);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(cb(BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(9, 16, 9, 16)));
        b.setMaximumSize(new Dimension(320, 42));
        b.setPreferredSize(new Dimension(300, 42));
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(HOVER);
                b.setBorder(cb(BorderFactory.createLineBorder(ac, 1), new EmptyBorder(9, 16, 9, 16)));
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(CARD);
                b.setBorder(cb(BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(9, 16, 9, 16)));
            }
        });
        return b;
    }

    JLabel lbl(String t, Font f, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    JLabel link(String t) {
        JLabel l = lbl(t, FM, ACCENT);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                l.setForeground(T1);
            }

            public void mouseExited(MouseEvent e) {
                l.setForeground(ACCENT);
            }
        });
        return l;
    }

    Component vsp(int n) {
        return Box.createVerticalStrut(n);
    }

    JSeparator hr() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    JPanel flow(int gap) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, gap, 0));
        p.setOpaque(false);
        return p;
    }

    JPanel flow(int align, int gap) {
        JPanel p = new JPanel(new FlowLayout(align, gap, 0));
        p.setOpaque(false);
        return p;
    }

    Component divider(String txt) {
        JPanel r = new JPanel(new BorderLayout(10, 0));
        r.setOpaque(false);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        JSeparator l = new JSeparator(), rr = new JSeparator();
        l.setForeground(BORDER);
        rr.setForeground(BORDER);
        JLabel c = lbl(txt, FM, T3);
        c.setHorizontalAlignment(SwingConstants.CENTER);
        r.add(l, BorderLayout.WEST);
        r.add(c, BorderLayout.CENTER);
        r.add(rr, BorderLayout.EAST);
        return r;
    }

    JPanel footer() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(2, 0, 10, 0));
        p.add(lbl("\u00A9 2026 Philippine National Banc  \u00B7  256-bit Encrypted", FM, T3));
        return p;
    }

    JPanel dlg(String title, String sub) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(SURFACE);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));
        JLabel t = lbl(title, FH, T1);
        t.setAlignmentX(CENTER_ALIGNMENT);
        JLabel s = lbl(sub, FM, T3);
        s.setAlignmentX(CENTER_ALIGNMENT);
        p.add(t);
        p.add(vsp(3));
        p.add(s);
        p.add(vsp(16));
        return p;
    }

    void capRow(JPanel p, String cap) {
        JLabel l = lbl(cap, FL, T3);
        l.setAlignmentX(CENTER_ALIGNMENT);
        p.add(l);
        p.add(vsp(6));
    }

    void row(JPanel p, String cap, JComponent f) {
        JLabel l = lbl(cap, FL, T3);
        l.setAlignmentX(CENTER_ALIGNMENT);
        f.setAlignmentX(CENTER_ALIGNMENT);
        p.add(l);
        p.add(vsp(5));
        p.add(f);
        p.add(vsp(13));
    }

    JPanel infoGrid(String[] lbls, String[] vals, Color[] cols) {
        JPanel g = new JPanel(new GridLayout(lbls.length, 2, 10, 6));
        g.setBackground(CARD);
        g.setBorder(cb(BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(10, 14, 10, 14)));
        g.setMaximumSize(new Dimension(Integer.MAX_VALUE, lbls.length * 32));
        g.setAlignmentX(CENTER_ALIGNMENT);
        for (int i = 0; i < lbls.length; i++) {
            g.add(lbl(lbls[i], FB, T2));
            g.add(lbl(vals[i], FS, cols[i]));
        }
        return g;
    }

    JPanel sumRow(String l, String v, Color vc) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        r.add(lbl(l, FB, T2), BorderLayout.WEST);
        r.add(lbl(v, FS, vc), BorderLayout.EAST);
        return r;
    }

    JDialog mkDlg(JPanel c, String t) {
        JDialog d = new JOptionPane(c, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}).createDialog(this, t);
        d.getContentPane().setBackground(SURFACE);
        return d;
    }

    MouseAdapter click(ActionListener al) {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                al.actionPerformed(null);
            }
        };
    }

    String askAmt(String t, String l) {
        JTextField f = field();
        JPanel p = dlg(t, l);
        row(p, "AMOUNT (\u20B1)", f);
        return ok(p, t) ? f.getText().trim() : null;
    }

    boolean ok(JPanel p, String t) {
        return JOptionPane.showConfirmDialog(this, p, t, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    void err(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }

    void msg(String m) {
        JOptionPane.showMessageDialog(this, m, "", JOptionPane.PLAIN_MESSAGE);
    }

    double parseAmt(String s) {
        try {
            double v = Double.parseDouble(s);
            if (v <= 0) {
                err("Amount must be > 0.");
                return -1;
            }
            return v;
        } catch (NumberFormatException e) {
            err("Enter a valid number.");
            return -1;
        }
    }

    String fmt(double v) {
        return String.format("%,.2f", v);
    }

    String fmtK(double v) {
        if (Math.abs(v) >= 1_000_000) {
            return String.format("%.2fM", v / 1_000_000);
        }
        if (Math.abs(v) >= 1_000) {
            return String.format("%.1fK", v / 1_000);
        }
        return fmt(v);
    }

    int findAcc(long id) {
        for (int i = 0; i < count; i++) {
            if (accounts[i].getId() == id) {
                return i;

            }
        }
        return -1;
    }

    void swap(JPanel p) {
        setContentPane(p);
        revalidate();
        repaint();
    }

    static void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println(count);
            pw.println(nextId);
            for (int i = 0; i < count; i++) {
                pw.println(accounts[i].toFileString(DTF));
            }
        } catch (Exception e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    static void loadData() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            count = Integer.parseInt(br.readLine());
            nextId = Long.parseLong(br.readLine());
            for (int i = 0; i < count; i++) {
                accounts[i] = BankAccount.fromFileString(br.readLine(), DTF);
            }
        } catch (Exception e) {
            count = 0;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(oopBC::new);
    }
}

class BankAccount {

    private final long id;
    private final String name, password;
    private double wallet, savings, loanBalance;
    private LocalDateTime lastSavingsDate, loanStartDate;
    private final LinkedList<String> txHistory = new LinkedList<>();

    public BankAccount(long id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
        lastSavingsDate = LocalDateTime.now();
    }

    public void copyFrom(BankAccount o) {
        wallet = o.wallet;
        savings = o.savings;
        loanBalance = o.loanBalance;
        lastSavingsDate = o.lastSavingsDate;
        loanStartDate = o.loanStartDate;
        txHistory.addAll(o.txHistory);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public double getWallet() {
        return wallet;
    }

    public double getSavings() {
        return savings;
    }

    public double getLoan() {
        return loanBalance;
    }

    public void depositWallet(double a) {
        wallet += a;
    }

    public void withdrawWallet(double a) {
        if (wallet >= a) {
            wallet -= a;

        }
    }

    public void depositSavings(double a) {
        if (wallet >= a) {
            wallet -= a;
            savings += a;
        }
    }

    public void withdrawSavings(double a) {
        if (savings >= a) {
            savings -= a;
            wallet += a;
        }
    }

    public void borrowLoan(double a) {
        if (loanStartDate == null) {
            loanStartDate = LocalDateTime.now();
        }
        loanBalance += a;
        wallet += a;
    }

    public void payLoan(double a) {
        if (wallet >= a) {
            wallet -= a;
            loanBalance -= a;
            if (loanBalance <= 0) {
                loanBalance = 0;
                loanStartDate = null;
            }
        }
    }

    public void applySavingsInterest() {
        if (savings > 0 && lastSavingsDate != null) {
            long d = ChronoUnit.DAYS.between(lastSavingsDate, LocalDateTime.now());
            if (d > 0) {
                savings += savings * 0.01 * d;
                lastSavingsDate = LocalDateTime.now();
            }
        }
    }

    public void applyLoanInterest() {
        if (loanBalance > 0 && loanStartDate != null) {
            long d = ChronoUnit.DAYS.between(loanStartDate, LocalDateTime.now());
            if (d > 0) {
                loanBalance += loanBalance * 0.05 * d;
                loanStartDate = LocalDateTime.now();
            }
        }
    }

    public void addTx(String tx) {
        txHistory.addFirst(tx);
        if (txHistory.size() > 6) {
            txHistory.removeLast();

        }
    }

    public String[] getLastTransactions() {
        return txHistory.toArray(new String[0]);
    }

    public String toFileString(DateTimeFormatter dtf) {
        String txs = String.join("||", txHistory);
        return id + ", " + name + ", " + password + ", " + wallet + ", " + savings + ", " + loanBalance + ", "
                + lastSavingsDate.format(dtf) + ", " + (loanStartDate != null ? loanStartDate.format(dtf) : "no loan")
                + ", " + (txs.isEmpty() ? "no_tx" : txs);
    }

    public static BankAccount fromFileString(String line, DateTimeFormatter dtf) {
        String[] p = line.split(", ", 9);
        BankAccount a = new BankAccount(Long.parseLong(p[0]), p[1], p[2]);
        a.wallet = Double.parseDouble(p[3]);
        a.savings = Double.parseDouble(p[4]);
        a.loanBalance = Double.parseDouble(p[5]);
        a.lastSavingsDate = LocalDateTime.parse(p[6], dtf);
        a.loanStartDate = p[7].equals("no loan") ? null : LocalDateTime.parse(p[7], dtf);
        if (p.length > 8 && !p[8].equals("no_tx")) {
            for (String tx : p[8].split("\\|\\|")) {
                a.txHistory.add(tx);
            }
        }
        return a;
    }
}
