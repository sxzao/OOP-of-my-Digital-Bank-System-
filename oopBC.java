
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

// ════════════════════════════════════════════════════════════
//  Philippine National Banco — Premium UI v3 (fixed layout)
// ════════════════════════════════════════════════════════════
public class oopBC extends JFrame {

    static final String FILE_NAME = "BCkd.txt";
    static BankAccount[] accounts = new BankAccount[100];
    static int accountCount = 0;
    static long nextId = 66671001;
    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

    // ── Palette ──────────────────────────────────────────────
    static final Color C_BG = new Color(228, 235, 250);
    static final Color C_CARD = new Color(255, 255, 255);
    static final Color C_G1 = new Color(52, 115, 235);
    static final Color C_G2 = new Color(108, 70, 228);
    static final Color C_ACCENT = new Color(52, 115, 235);
    static final Color C_TEXT = new Color(18, 28, 52);
    static final Color C_SUB = new Color(90, 102, 128);
    static final Color C_HINT = new Color(155, 165, 185);
    static final Color C_IBRD = new Color(205, 215, 238);
    static final Color C_IBG = new Color(244, 247, 254);
    static final Color C_GREEN = new Color(22, 175, 110);
    static final Color C_RED = new Color(225, 58, 58);
    static final Color C_GOLD = new Color(240, 165, 20);

    // ── Fonts ────────────────────────────────────────────────
    static final Font F_HERO = new Font("Segoe UI", Font.BOLD, 24);
    static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    static final Font F_SUB = new Font("Segoe UI", Font.BOLD, 14);
    static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    static final Font F_BTN = new Font("Segoe UI", Font.BOLD, 13);
    static final Font F_LBL = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font F_NUM = new Font("Segoe UI", Font.BOLD, 15);
    static final Font F_MONO = new Font("Courier New", Font.BOLD, 11);

    private int loggedIn = -1;

    // ════════════════════════════════════════════════════════
    public oopBC() {
        setTitle("Philippine National Banco");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        int w = 540, h = 760;
        setSize(w, h);
        setMinimumSize(new Dimension(w, h));
        setMaximumSize(new Dimension(w, h));
        setResizable(false);
        setLocationRelativeTo(null);
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        loadData();
        showMain();
        setVisible(true);
    }

    // ════════════════════════════════════════════════════════
    //  SCREENS
    // ════════════════════════════════════════════════════════
    void showMain() {
        JPanel root = bg();
        root.setLayout(new BorderLayout());
        root.add(mkHeader("Philippine National Banco", "Banking Solutions Made Simple", null), BorderLayout.NORTH);

        JPanel body = bg();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(30, 35, 10, 35));

        body.add(cLabel("Get Started", F_HERO, C_TEXT));
        body.add(vg(5));
        body.add(cLabel("Your trusted digital banking partner", F_BODY, C_SUB));
        body.add(vg(22));

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pills.setOpaque(false);
        pills.setMaximumSize(new Dimension(460, 34));
        pills.add(pill("Secure", C_GREEN));
        pills.add(pill("Instant", C_ACCENT));
        pills.add(pill("Premium", C_GOLD));
        body.add(pills);
        body.add(vg(28));

        body.add(btnGrad("Create New Account", e -> showCreateAccount()));
        body.add(vg(11));
        body.add(btnGrad("Login to Account", e -> showLogin()));
        body.add(vg(11));
        body.add(btnOutline("Currency Exchange", e -> showCurrencyExchange()));
        body.add(vg(11));
        body.add(btnGhost("Exit Application", e -> {
            saveData();
            System.exit(0);
        }));
        body.add(Box.createVerticalGlue());

        root.add(body, BorderLayout.CENTER);
        root.add(mkFooter(), BorderLayout.SOUTH);
        swap(root);
    }

    void showDashboard() {
        BankAccount a = accounts[loggedIn];
        a.applySavingsInterest();
        a.applyLoanInterest();

        JPanel root = bg();
        root.setLayout(new BorderLayout());
        root.add(mkHeader("Welcome back", a.getName(), "Account ID: " + a.getId()), BorderLayout.NORTH);

        JPanel body = bg();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(22, 26, 16, 12)); // right reduced to match scrollbar width

        // ── Balance hero card ─────────────────────────────
        JPanel balCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, C_G1, getWidth(), getHeight(), C_G2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        balCard.setOpaque(false);
        balCard.setMaximumSize(new Dimension(490, 118));
        balCard.setPreferredSize(new Dimension(490, 118));
        balCard.setMinimumSize(new Dimension(490, 118));
        balCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        double total = a.getWallet() + a.getSavings() - a.getLoan();
        JPanel balLeft = new JPanel();
        balLeft.setOpaque(false);
        balLeft.setLayout(new BoxLayout(balLeft, BoxLayout.Y_AXIS));

        JLabel balCaption = new JLabel("Total Net Assets");
        balCaption.setFont(F_SMALL);
        balCaption.setForeground(new Color(195, 212, 255));

        JLabel balAmt = new JLabel("\u20b1 " + fmtK(total));
        balAmt.setFont(new Font("Segoe UI", Font.BOLD, 28));
        balAmt.setForeground(Color.WHITE);
        int balH = balAmt.getPreferredSize().height;
        Dimension balSize = new Dimension(260, balH);
        balAmt.setPreferredSize(balSize);
        balAmt.setMinimumSize(balSize);
        balAmt.setMaximumSize(balSize);

        JLabel balDate = new JLabel("Updated " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, h:mm a")));
        balDate.setFont(F_SMALL);
        balDate.setForeground(new Color(170, 195, 255));

        balLeft.add(balCaption);
        balLeft.add(Box.createVerticalStrut(6));
        balLeft.add(balAmt);
        balLeft.add(Box.createVerticalStrut(4));
        balLeft.add(balDate);

        JLabel statusChip = mkChip(total >= 0 ? "\u25b2 Healthy" : "\u25bc In Debt", total >= 0 ? C_GREEN : C_RED);
        balCard.add(balLeft, BorderLayout.WEST);
        balCard.add(statusChip, BorderLayout.EAST);
        body.add(balCard);
        body.add(vg(18));

        // ── 3 stat tiles ──────────────────────────────────
        JPanel tiles = new JPanel(new GridLayout(1, 3, 12, 0));
        tiles.setOpaque(false);
        tiles.setMaximumSize(new Dimension(490, 92));
        tiles.setPreferredSize(new Dimension(490, 92));
        tiles.setMinimumSize(new Dimension(490, 92));
        tiles.add(statTile("\ud83d\udcbc", "Wallet", "\u20b1" + fmt(a.getWallet()), C_ACCENT));
        tiles.add(statTile("\ud83d\udc37", "Savings", "\u20b1" + fmt(a.getSavings()), C_GREEN));
        tiles.add(statTile("\ud83d\udcb3", "Loan", "\u20b1" + fmt(a.getLoan()), a.getLoan() > 0 ? C_RED : C_HINT));
        body.add(tiles);
        body.add(vg(22));

        // ── Quick Actions (CENTERED) ──────────────────────
        JLabel qaLbl = new JLabel("Quick Actions", SwingConstants.CENTER);
        qaLbl.setFont(F_SUB);
        qaLbl.setForeground(C_TEXT);
        qaLbl.setAlignmentX(CENTER_ALIGNMENT);
        qaLbl.setMaximumSize(new Dimension(490, 22));
        body.add(qaLbl);
        body.add(vg(10));

        // 2 rows × 4 cols to include Transfer + History tiles
        JPanel grid = new JPanel(new GridLayout(2, 4, 10, 10));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(490, 126));
        grid.setPreferredSize(new Dimension(490, 126));
        grid.setMinimumSize(new Dimension(490, 126));
        grid.setAlignmentX(CENTER_ALIGNMENT);
        grid.add(actionTile("\ud83d\udcb0", "Deposit", e -> quickDeposit()));
        grid.add(actionTile("\ud83c\udfe7", "Withdraw", e -> quickWithdraw()));
        grid.add(actionTile("\ud83d\udcc8", "Save", e -> showSavings()));
        grid.add(actionTile("\ud83e\udd1d", "Borrow", e -> showLoan()));
        grid.add(actionTile("\ud83d\udcca", "Summary", e -> showSummary()));
        grid.add(actionTile("\ud83d\udcb1", "Exchange", e -> showCurrencyExchange()));
        grid.add(actionTile("\ud83d\udce4", "Transfer", e -> showTransfer()));
        grid.add(actionTile("\ud83d\udcdc", "History", e -> showFullHistory()));
        body.add(grid);
        body.add(vg(22));

        body.add(btnOutline("\ud83d\udeaa  Logout", e -> {
            saveData();
            loggedIn = -1;
            showMain();
        }));
        body.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBorder(null);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(14, 0)); // fixed 14px scrollbar
        root.add(sp, BorderLayout.CENTER);
        root.add(mkFooter(), BorderLayout.SOUTH);
        swap(root);
    }

    // ════════════════════════════════════════════════════════
    //  FEATURES
    // ════════════════════════════════════════════════════════
    void showCreateAccount() {
        JPanel p = dlgPanel("Create New Account", "Join Philippine National Banco today");
        JTextField nameF = iField("Full Name");
        JPasswordField passF = pField("Password (min 4 chars)");
        JPasswordField confF = pField("Confirm Password");
        addRow(p, "Full Name", nameF);
        addRow(p, "Password", passF);
        addRow(p, "Confirm Password", confF);

        if (dlgOk(p, "Create Account")) {
            String name = nameF.getText().trim();
            String pass = new String(passF.getPassword());
            String conf = new String(confF.getPassword());
            if (name.isEmpty() || pass.isEmpty()) {
                err("Please fill in all fields.");
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
            accounts[accountCount++] = new BankAccount(nextId++, name, pass);
            saveData();
            ok("\u2705  Account created!\n\nYour Account ID:  " + (nextId - 1) + "\n\nSave your ID \u2014 you\u2019ll need it to log in.");
        }
    }

    void showLogin() {
        JPanel p = dlgPanel("Login", "Access your account securely");
        JTextField idF = iField("Account ID");
        JPasswordField passF = pField("Password");
        addRow(p, "Account ID", idF);
        addRow(p, "Password", passF);

        if (dlgOk(p, "Login")) {
            try {
                long id = Long.parseLong(idF.getText().trim());
                int f = -1;
                for (int i = 0; i < accountCount; i++) {
                    if (accounts[i].getId() == id) {
                        f = i;
                        break;
                    }
                }
                if (f == -1) {
                    err("Account not found.");
                    return;
                }
                if (!accounts[f].getPassword().equals(new String(passF.getPassword()))) {
                    err("Incorrect password.");
                    return;
                }
                loggedIn = f;
                showDashboard();
            } catch (NumberFormatException ex) {
                err("Account ID must be a number.");
            }
        }
    }

    void quickDeposit() {
        String s = askAmt("Deposit to Wallet", "Add money to your wallet", "Amount (\u20b1)");
        if (s == null) {
            return;
        }
        double a = safeD(s);
        if (a <= 0) {
            return;
        }
        accounts[loggedIn].depositWallet(a);
        accounts[loggedIn].addTx("\u2b06  \u20b1" + fmt(a) + "  \u2014  Wallet Deposit");
        saveData();
        ok("Deposited \u20b1" + fmt(a) + " to wallet.");
        showDashboard();
    }

    void quickWithdraw() {
        String s = askAmt("Withdraw from Wallet", "Cash out from your wallet", "Amount (\u20b1)");
        if (s == null) {
            return;
        }
        double a = safeD(s);
        if (a <= 0) {
            return;
        }
        if (a > accounts[loggedIn].getWallet()) {
            err("Insufficient wallet balance.");
            return;
        }
        accounts[loggedIn].withdrawWallet(a);
        accounts[loggedIn].addTx("\u2b07  \u20b1" + fmt(a) + "  \u2014  Wallet Withdrawal");
        saveData();
        ok("Withdrew \u20b1" + fmt(a) + " from wallet.");
        showDashboard();
    }

    void showSavings() {
        accounts[loggedIn].applySavingsInterest();
        BankAccount a = accounts[loggedIn];
        String[] opts = {"Deposit to Savings", "Withdraw from Savings", "Cancel"};
        int ch = JOptionPane.showOptionDialog(this,
                "Savings Balance: \u20b1" + fmt(a.getSavings())
                + "\nWallet Balance:  \u20b1" + fmt(a.getWallet())
                + "\n\n\ud83d\udcc8 Earns 1% interest per day",
                "\ud83d\udc37 Savings Account", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[2]);
        if (ch == 0) {
            String s = askAmt("Save Money", "Transfer wallet \u2192 savings", "Amount (\u20b1)");
            if (s == null) {
                return;
            }
            double amt = safeD(s);
            if (amt <= 0) {
                return;
            }
            if (amt > a.getWallet()) {
                err("Insufficient wallet balance.");
                return;
            }
            a.depositSavings(amt);
            a.addTx("\u2192  \u20b1" + fmt(amt) + "  \u2014  Moved to Savings");
            saveData();
            ok("\u20b1" + fmt(amt) + " moved to savings.\nEarning 1% daily interest! \ud83d\udcc8");
            showDashboard();
        } else if (ch == 1) {
            String s = askAmt("Savings Withdrawal", "Transfer savings \u2192 wallet", "Amount (\u20b1)");
            if (s == null) {
                return;
            }
            double amt = safeD(s);
            if (amt <= 0) {
                return;
            }
            if (amt > a.getSavings()) {
                err("Insufficient savings balance.");
                return;
            }
            a.withdrawSavings(amt);
            a.addTx("\u2190  \u20b1" + fmt(amt) + "  \u2014  Savings Withdrawal");
            saveData();
            ok("\u20b1" + fmt(amt) + " transferred back to wallet.");
            showDashboard();
        }
    }

    void showLoan() {
        accounts[loggedIn].applyLoanInterest();
        BankAccount a = accounts[loggedIn];
        String[] opts = {"Borrow Money", "Repay Loan", "Cancel"};
        int ch = JOptionPane.showOptionDialog(this,
                "Outstanding Loan: \u20b1" + fmt(a.getLoan())
                + "\nWallet Balance:   \u20b1" + fmt(a.getWallet())
                + "\n\n\u26a0  5% interest accrues per day",
                "\ud83d\udcb3 Loan Management", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[2]);
        if (ch == 0) {
            String s = askAmt("Borrow a Loan", "Funds credited to wallet immediately", "Loan Amount (\u20b1)");
            if (s == null) {
                return;
            }
            double amt = safeD(s);
            if (amt <= 0) {
                return;
            }
            a.borrowLoan(amt);
            a.addTx("\ud83d\udcb3 \u20b1" + fmt(amt) + "  \u2014  Loan Disbursed");
            saveData();
            ok("\u20b1" + fmt(amt) + " loan approved!\n\n\u26a0 5% daily interest applies.\nRepay promptly to avoid high charges.");
            showDashboard();
        } else if (ch == 1) {
            if (a.getLoan() <= 0) {
                ok("\ud83c\udf89 You have no outstanding loan!");
                return;
            }
            String s = askAmt("Repay Loan", "Payment deducted from wallet", "Payment Amount (\u20b1)");
            if (s == null) {
                return;
            }
            double amt = safeD(s);
            if (amt <= 0) {
                return;
            }
            if (amt > a.getWallet()) {
                err("Insufficient wallet balance.");
                return;
            }
            a.payLoan(amt);
            a.addTx("\u2713  \u20b1" + fmt(amt) + "  \u2014  Loan Repayment");
            saveData();
            ok("\u20b1" + fmt(amt) + " loan payment applied!");
            showDashboard();
        }
    }

    void showSummary() {
        accounts[loggedIn].applySavingsInterest();
        accounts[loggedIn].applyLoanInterest();
        BankAccount a = accounts[loggedIn];
        double total = a.getWallet() + a.getSavings() - a.getLoan();

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 6, 10, 6));
        p.setPreferredSize(new Dimension(320, 260));

        p.add(cLabel("Account Summary", F_TITLE, C_TEXT));
        p.add(vg(3));
        p.add(cLabel(a.getName() + "  \u00b7  ID " + a.getId(), F_LBL, C_SUB));
        p.add(vg(20));
        p.add(sumRow("\ud83d\udcbc  Wallet", "\u20b1 " + fmt(a.getWallet()), C_ACCENT));
        p.add(vg(8));
        p.add(sumRow("\ud83d\udc37  Savings", "\u20b1 " + fmt(a.getSavings()), C_GREEN));
        p.add(vg(8));
        p.add(sumRow("\ud83d\udcb3  Loan Balance", "\u20b1 " + fmt(a.getLoan()), a.getLoan() > 0 ? C_RED : C_HINT));
        p.add(vg(16));
        JSeparator sep = new JSeparator();
        sep.setForeground(C_IBRD);
        sep.setMaximumSize(new Dimension(320, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        p.add(sep);
        p.add(vg(14));
        p.add(sumRow("\ud83d\udcca  Net Assets", "\u20b1 " + fmt(total), total >= 0 ? C_GREEN : C_RED));

        JOptionPane.showMessageDialog(this, p, "\ud83d\udcca Account Summary", JOptionPane.PLAIN_MESSAGE);
    }

    void showCurrencyExchange() {
        String[] opts = {
            "\u20b1 \u2192 $  Peso \u2192 Dollar", "$ \u2192 \u20b1  Dollar \u2192 Peso",
            "\u20ac \u2192 $  Euro \u2192 Dollar", "\u20ac \u2192 \u20b1  Euro \u2192 Peso",
            "$ \u2192 \u20ac  Dollar \u2192 Euro", "\u00a5 \u2192 \u20b1  Yen \u2192 Peso"
        };
        int ch = JOptionPane.showOptionDialog(this,
                "Rates: $1=\u20b156  |  \u20ac1=\u20b160  |  \u20ac1=$1.08  |  \u00a51=\u20b10.38",
                "\ud83d\udcb1 Currency Exchange",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (ch == -1) {
            return;
        }
        String s = askAmt("Currency Exchange", opts[ch], "Amount");
        if (s == null) {
            return;
        }
        double a = safeD(s);
        if (a <= 0) {
            return;
        }
        String result = switch (ch) {
            case 0 ->
                "\u20b1" + fmt(a) + " = $" + fmt(a / 56.0);
            case 1 ->
                "$" + fmt(a) + " = \u20b1" + fmt(a * 56.0);
            case 2 ->
                "\u20ac" + fmt(a) + " = $" + fmt(a * 1.08);
            case 3 ->
                "\u20ac" + fmt(a) + " = \u20b1" + fmt(a * 60.0);
            case 4 ->
                "$" + fmt(a) + " = \u20ac" + fmt(a / 1.08);
            case 5 ->
                "\u00a5" + fmt(a) + " = \u20b1" + fmt(a * 0.38);
            default ->
                "";
        };
        ok("\ud83d\udcb1 Result\n\n" + result + "\n\n(Approximate market rates)");
    }

    // ════════════════════════════════════════════════════════
    //  TRANSFER BETWEEN ACCOUNTS
    // ════════════════════════════════════════════════════════
    void showTransfer() {
        BankAccount sender = accounts[loggedIn];

        // ── Step 1: Input dialog ──────────────────────────
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(8, 4, 8, 4));

        JLabel title = new JLabel("Transfer Funds", SwingConstants.CENTER);
        title.setFont(F_TITLE);
        title.setForeground(C_TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Send money to another PNB account", SwingConstants.CENTER);
        sub.setFont(F_LBL);
        sub.setForeground(C_SUB);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(3));
        p.add(sub);
        p.add(Box.createVerticalStrut(14));

        // Wallet balance info bar
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        infoBar.setBackground(C_IBG);
        infoBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_IBRD, 1),
                new EmptyBorder(8, 14, 8, 14)));
        infoBar.setMaximumSize(new Dimension(340, 38));
        infoBar.setAlignmentX(CENTER_ALIGNMENT);
        JLabel balInfo = new JLabel("Your wallet:  \u20b1" + fmt(sender.getWallet()));
        balInfo.setFont(F_BTN);
        balInfo.setForeground(C_ACCENT);
        infoBar.add(balInfo);
        p.add(infoBar);
        p.add(Box.createVerticalStrut(14));

        JTextField recipientIdF = iField("Recipient Account ID");
        JTextField amountF = iField("Amount (\u20b1)");
        JTextField noteF = iField("Note (optional)");

        addRow(p, "Recipient Account ID", recipientIdF);
        addRow(p, "Amount (\u20b1)", amountF);
        addRow(p, "Note (optional)", noteF);

        int res = JOptionPane.showConfirmDialog(this, p, "\ud83d\udce4 Transfer Funds",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        // ── Validate recipient ────────────────────────────
        long recipientId;
        try {
            recipientId = Long.parseLong(recipientIdF.getText().trim());
        } catch (NumberFormatException ex) {
            err("Recipient ID must be a valid number.");
            return;
        }

        if (recipientId == sender.getId()) {
            err("You cannot transfer funds to yourself.");
            return;
        }

        int recipientIdx = -1;
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getId() == recipientId) {
                recipientIdx = i;
                break;
            }
        }
        if (recipientIdx == -1) {
            err("Recipient account ID " + recipientId + " was not found.");
            return;
        }

        // ── Validate amount ───────────────────────────────
        double amt = safeD(amountF.getText().trim());
        if (amt <= 0) {
            return;
        }
        if (amt > sender.getWallet()) {
            err("Insufficient wallet balance.\n\nWallet:    \u20b1" + fmt(sender.getWallet())
                    + "\nRequested: \u20b1" + fmt(amt));
            return;
        }

        // ── Step 2: Confirmation dialog ───────────────────
        BankAccount recipient = accounts[recipientIdx];
        String note = noteF.getText().trim().isEmpty() ? "No note" : noteF.getText().trim();

        JPanel confirm = new JPanel();
        confirm.setLayout(new BoxLayout(confirm, BoxLayout.Y_AXIS));
        confirm.setBackground(Color.WHITE);
        confirm.setBorder(new EmptyBorder(10, 6, 10, 6));
        confirm.setPreferredSize(new Dimension(340, 270));

        JLabel ct = new JLabel("Confirm Transfer", SwingConstants.CENTER);
        ct.setFont(F_TITLE);
        ct.setForeground(C_TEXT);
        ct.setAlignmentX(CENTER_ALIGNMENT);
        confirm.add(ct);
        confirm.add(Box.createVerticalStrut(16));
        confirm.add(sumRow("From", sender.getName() + " (#" + sender.getId() + ")", C_TEXT));
        confirm.add(Box.createVerticalStrut(8));
        confirm.add(sumRow("To", recipient.getName() + " (#" + recipient.getId() + ")", C_ACCENT));
        confirm.add(Box.createVerticalStrut(8));
        confirm.add(sumRow("Amount", "\u20b1 " + fmt(amt), C_GREEN));
        confirm.add(Box.createVerticalStrut(8));
        confirm.add(sumRow("Note", note, C_SUB));
        confirm.add(Box.createVerticalStrut(14));
        JSeparator csep = new JSeparator();
        csep.setForeground(C_IBRD);
        csep.setMaximumSize(new Dimension(340, 1));
        csep.setAlignmentX(LEFT_ALIGNMENT);
        confirm.add(csep);
        confirm.add(Box.createVerticalStrut(12));
        double afterBalance = sender.getWallet() - amt;
        confirm.add(sumRow("Balance after", "\u20b1 " + fmt(afterBalance), afterBalance >= 0 ? C_TEXT : C_RED));

        int conf = JOptionPane.showConfirmDialog(this, confirm, "Confirm Transfer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (conf != JOptionPane.OK_OPTION) {
            return;
        }

        // ── Step 3: Execute transfer ──────────────────────
        sender.withdrawWallet(amt);
        recipient.depositWallet(amt);

        String noteTag = note.equals("No note") ? "" : "  \u201c" + note + "\u201d";
        sender.addTx("\ud83d\udce4 \u20b1" + fmt(amt) + "  \u2014  Sent to " + recipient.getName() + noteTag);
        recipient.addTx("\ud83d\udce5 \u20b1" + fmt(amt) + "  \u2014  From " + sender.getName() + noteTag);

        saveData();
        ok("\u2705 Transfer Successful!\n\n"
                + "\u20b1" + fmt(amt) + " sent to " + recipient.getName()
                + "\nAccount #" + recipient.getId()
                + "\n\nYour new wallet balance:  \u20b1" + fmt(sender.getWallet()));
        showDashboard();
    }

    // ════════════════════════════════════════════════════════
    //  FULL TRANSACTION HISTORY
    // ════════════════════════════════════════════════════════
    void showFullHistory() {
        BankAccount a = accounts[loggedIn];
        String[] txs = a.getLastTransactions();

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 6, 10, 6));
        p.setPreferredSize(new Dimension(380, txs.length == 0 ? 110 : Math.min(60 + txs.length * 46, 340)));

        JLabel t = new JLabel("Transaction History", SwingConstants.CENTER);
        t.setFont(F_TITLE);
        t.setForeground(C_TEXT);
        t.setAlignmentX(CENTER_ALIGNMENT);
        JLabel s = new JLabel(a.getName() + "  \u00b7  Last " + txs.length + " transactions", SwingConstants.CENTER);
        s.setFont(F_LBL);
        s.setForeground(C_SUB);
        s.setAlignmentX(CENTER_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(s);
        p.add(Box.createVerticalStrut(16));

        if (txs.length == 0) {
            JLabel empty = new JLabel("No transactions yet.", SwingConstants.CENTER);
            empty.setFont(F_BODY);
            empty.setForeground(C_HINT);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            p.add(empty);
        } else {
            for (int i = 0; i < txs.length; i++) {
                String tx = txs[i];
                Color rowColor = C_TEXT;
                if (tx.contains("Deposit") || tx.contains("Savings") && tx.contains("\u2192") || tx.contains("From ")) {
                    rowColor = C_GREEN;
                } else if (tx.contains("Withdrawal") || tx.contains("Sent to")) {
                    rowColor = C_RED;
                } else if (tx.contains("Loan Disbursed")) {
                    rowColor = C_GOLD;
                } else if (tx.contains("Repayment")) {
                    rowColor = C_ACCENT;
                }

                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(i % 2 == 0 ? Color.WHITE : C_IBG);
                row.setMaximumSize(new Dimension(370, 38));
                row.setBorder(new EmptyBorder(8, 10, 8, 10));
                JLabel lbl = new JLabel(tx);
                lbl.setFont(F_BODY);
                lbl.setForeground(rowColor);
                row.add(lbl, BorderLayout.CENTER);
                p.add(row);
                if (i < txs.length - 1) {
                    JSeparator sep = new JSeparator();
                    sep.setForeground(C_IBRD);
                    sep.setMaximumSize(new Dimension(370, 1));
                    p.add(sep);
                }
            }
        }
        JOptionPane.showMessageDialog(this, p, "\ud83d\udcdc Transaction History", JOptionPane.PLAIN_MESSAGE);
    }

    // ════════════════════════════════════════════════════════
    //  UI BUILDERS
    // ════════════════════════════════════════════════════════
    JPanel mkHeader(String l1, String l2, String l3) {
        JPanel outer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, C_G1, getWidth(), getHeight(), C_G2));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(getWidth() - 80, -30, 130, 130);
                g2.fillOval(getWidth() - 150, 40, 80, 80);
                g2.dispose();
            }
        };
        outer.setOpaque(true);
        int h = (l3 != null) ? 128 : 105;
        outer.setPreferredSize(new Dimension(540, h));
        outer.setMinimumSize(new Dimension(540, h));
        outer.setMaximumSize(new Dimension(540, h));
        outer.setLayout(new BorderLayout());
        outer.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("\u2b21  PNB");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logo.setForeground(new Color(190, 215, 255));

        JLabel lb1 = new JLabel(l1);
        lb1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lb1.setForeground(new Color(178, 202, 255));

        JLabel lb2 = new JLabel(l2);
        lb2.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lb2.setForeground(Color.WHITE);

        inner.add(logo);
        inner.add(Box.createVerticalStrut(8));
        inner.add(lb1);
        inner.add(Box.createVerticalStrut(2));
        inner.add(lb2);

        if (l3 != null) {
            inner.add(Box.createVerticalStrut(4));
            JLabel lb3 = new JLabel(l3);
            lb3.setFont(F_MONO);
            lb3.setForeground(new Color(155, 188, 255));
            inner.add(lb3);
        }

        outer.add(inner, BorderLayout.CENTER);
        return outer;
    }

    JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(C_CARD);
        p.setOpaque(true);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 242), 1),
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(190, 205, 238, 90))
        ));
        return p;
    }

    JPanel statTile(String icon, String label, String val, Color vc) {
        JPanel t = card();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setBorder(new EmptyBorder(13, 13, 11, 13));
        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel vl = new JLabel(val);
        vl.setFont(F_NUM);
        vl.setForeground(vc);
        Dimension vSize = new Dimension(120, vl.getPreferredSize().height);
        vl.setPreferredSize(vSize);
        vl.setMinimumSize(vSize);
        vl.setMaximumSize(vSize);
        JLabel lb = new JLabel(label);
        lb.setFont(F_SMALL);
        lb.setForeground(C_SUB);
        t.add(ic);
        t.add(Box.createVerticalStrut(5));
        t.add(vl);
        t.add(Box.createVerticalStrut(2));
        t.add(lb);
        return t;
    }

    JButton actionTile(String icon, String label, ActionListener al) {
        JButton btn = new JButton("<html><center>" + icon + "<br><small>" + label + "</small></center></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(228, 234, 252) : C_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(208, 218, 242));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        btn.setForeground(C_TEXT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    JButton btnGrad(String text, ActionListener al) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color a2 = getModel().isPressed() ? C_G2 : C_G1;
                Color b = getModel().isPressed() ? C_G1 : C_G2;
                g2.setPaint(new GradientPaint(0, 0, a2, getWidth(), 0, b));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        applyBtnStyle(btn);
        btn.addActionListener(al);
        return btn;
    }

    JButton btnOutline(String text, ActionListener al) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(228, 234, 252) : C_IBG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(C_IBRD);
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 28, 28);
                g2.setFont(getFont());
                g2.setColor(C_SUB);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        applyBtnStyle(btn);
        btn.addActionListener(al);
        return btn;
    }

    JButton btnGhost(String text, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(F_LBL);
        btn.setForeground(C_HINT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    void applyBtnStyle(JButton btn) {
        btn.setFont(F_BTN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(460, 46));
        btn.setPreferredSize(new Dimension(400, 46));
        btn.setMinimumSize(new Dimension(300, 46));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    JLabel pill(String text, Color col) {
        JLabel l = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 22));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 120));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(F_SMALL);
        l.setForeground(col);
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(4, 12, 4, 12));
        return l;
    }

    JLabel mkChip(String text, Color col) {
        JLabel l = new JLabel(text);
        l.setFont(F_SMALL);
        l.setOpaque(true);
        l.setBackground(new Color(0, 0, 0, 60));
        l.setForeground(Color.WHITE);
        l.setBorder(new EmptyBorder(5, 10, 5, 10));
        return l;
    }

    // Centered transaction row for the dashboard card
    JPanel txRowCentered(String tx) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(450, 26));
        JLabel lbl = new JLabel(tx, SwingConstants.CENTER);
        lbl.setFont(F_BODY);
        lbl.setForeground(C_TEXT);
        row.add(lbl);
        return row;
    }

    JPanel sumRow(String lbl, String val, Color vc) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(340, 28));
        row.setPreferredSize(new Dimension(340, 28));
        JLabel l = new JLabel(lbl);
        l.setFont(F_BODY);
        l.setForeground(C_SUB);
        JLabel v = new JLabel(val);
        v.setFont(F_BTN);
        v.setForeground(vc);
        Dimension vSize = new Dimension(160, v.getPreferredSize().height);
        v.setPreferredSize(vSize);
        v.setMinimumSize(vSize);
        v.setMaximumSize(vSize);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    JLabel cLabel(String t, Font f, Color c) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(f);
        l.setForeground(c);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    JPanel bg() {
        JPanel p = new JPanel();
        p.setBackground(C_BG);
        p.setOpaque(true);
        return p;
    }

    Box.Filler vg(int h) {
        return (Box.Filler) Box.createVerticalStrut(h);
    }

    JPanel mkFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(C_BG);
        p.setBorder(new EmptyBorder(2, 0, 8, 0));
        JLabel l = new JLabel("\u00a9 2026 Philippine National Banco  \u00b7  256-bit encrypted");
        l.setFont(F_SMALL);
        l.setForeground(C_HINT);
        p.add(l);
        return p;
    }

    // ── Dialog helpers ───────────────────────────────────────
    JPanel dlgPanel(String title, String sub) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(8, 4, 8, 4));
        JLabel t = new JLabel(title);
        t.setFont(F_TITLE);
        t.setForeground(C_TEXT);
        t.setAlignmentX(CENTER_ALIGNMENT);
        JLabel s = new JLabel(sub);
        s.setFont(F_LBL);
        s.setForeground(C_SUB);
        s.setAlignmentX(CENTER_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(3));
        p.add(s);
        p.add(Box.createVerticalStrut(18));
        return p;
    }

    JTextField iField(String ph) {
        JTextField f = new JTextField(20);
        f.setFont(F_BODY);
        f.setBackground(C_IBG);
        f.setForeground(C_TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_IBRD, 1), new EmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(340, 40));
        f.setPreferredSize(new Dimension(320, 40));
        f.setMinimumSize(new Dimension(280, 40));
        f.setHorizontalAlignment(SwingConstants.CENTER);
        return f;
    }

    JPasswordField pField(String ph) {
        JPasswordField f = new JPasswordField(20);
        f.setFont(F_BODY);
        f.setBackground(C_IBG);
        f.setForeground(C_TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_IBRD, 1), new EmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(340, 40));
        f.setPreferredSize(new Dimension(320, 40));
        f.setMinimumSize(new Dimension(280, 40));
        f.setHorizontalAlignment(SwingConstants.CENTER);
        return f;
    }

    void addRow(JPanel p, String lbl, JComponent field) {
        JLabel l = new JLabel(lbl);
        l.setFont(F_LBL);
        l.setForeground(C_SUB);
        l.setAlignmentX(CENTER_ALIGNMENT);
        field.setAlignmentX(CENTER_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(5));
        p.add(field);
        p.add(Box.createVerticalStrut(13));
    }

    String askAmt(String title, String msg, String lbl) {
        JPanel p = dlgPanel(title, msg);
        JTextField f = iField(lbl);
        addRow(p, lbl, f);
        return dlgOk(p, title) ? f.getText().trim() : null;
    }

    boolean dlgOk(JPanel p, String title) {
        return JOptionPane.showConfirmDialog(this, p, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    double safeD(String s) {
        try {
            double v = Double.parseDouble(s);
            if (v <= 0) {
                err("Amount must be greater than zero.");
                return -1;
            }
            return v;
        } catch (NumberFormatException e) {
            err("Please enter a valid number.");
            return -1;
        }
    }

    void err(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }

    void ok(String m) {
        JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    String fmt(double v) {
        return String.format("%,.2f", v);
    }

    String fmtK(double v) {
        if (Math.abs(v) >= 1_000_000) {
            return String.format("%.2fM", v / 1_000_000);
        }
        if (Math.abs(v) >= 1_000) {
            return String.format("%.2fK", v / 1_000);
        }
        return fmt(v);
    }

    void swap(JPanel p) {
        setContentPane(p);
        revalidate();
        repaint();
    }

    // ════════════════════════════════════════════════════════
    //  PERSISTENCE
    // ════════════════════════════════════════════════════════
    static void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            pw.println(accountCount);
            pw.println(nextId);
            for (int i = 0; i < accountCount; i++) {
                pw.println(accounts[i].toFileString(DTF));
            }
        } catch (Exception e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    static void loadData() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            accountCount = Integer.parseInt(br.readLine());
            nextId = Long.parseLong(br.readLine());
            for (int i = 0; i < accountCount; i++) {
                accounts[i] = BankAccount.fromFileString(br.readLine(), DTF);
            }
        } catch (Exception ignored) {
            accountCount = 0;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(oopBC::new);
    }
}

// ════════════════════════════════════════════════════════════
//  BankAccount
// ════════════════════════════════════════════════════════════
class BankAccount {

    private final long id;
    private final String name;
    private final String password;
    private double wallet, savings, loanBalance;
    private LocalDateTime lastSavingsInterest, loanStart;
    private final LinkedList<String> txHistory = new LinkedList<>();

    public BankAccount(long id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
        lastSavingsInterest = LocalDateTime.now();
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
        if (loanStart == null) {
            loanStart = LocalDateTime.now();
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
                loanStart = null;
            }
        }
    }

    public void applySavingsInterest() {
        if (savings > 0 && lastSavingsInterest != null) {
            long d = ChronoUnit.DAYS.between(lastSavingsInterest, LocalDateTime.now());
            if (d > 0) {
                savings += savings * 0.01 * d;
                lastSavingsInterest = LocalDateTime.now();
            }
        }
    }

    public void applyLoanInterest() {
        if (loanBalance > 0 && loanStart != null) {
            long d = ChronoUnit.DAYS.between(loanStart, LocalDateTime.now());
            if (d > 0) {
                loanBalance += loanBalance * 0.05 * d;
                loanStart = LocalDateTime.now();
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
                + lastSavingsInterest.format(dtf) + ", "
                + (loanStart != null ? loanStart.format(dtf) : "no loan") + ", "
                + (txs.isEmpty() ? "no_tx" : txs);
    }

    public static BankAccount fromFileString(String line, DateTimeFormatter dtf) {
        String[] p = line.split(", ", 9);
        BankAccount a = new BankAccount(Long.parseLong(p[0]), p[1], p[2]);
        a.wallet = Double.parseDouble(p[3]);
        a.savings = Double.parseDouble(p[4]);
        a.loanBalance = Double.parseDouble(p[5]);
        a.lastSavingsInterest = LocalDateTime.parse(p[6], dtf);
        a.loanStart = p[7].equals("no loan") ? null : LocalDateTime.parse(p[7], dtf);
        if (p.length > 8 && !p[8].equals("no_tx")) {
            for (String tx : p[8].split("\\|\\|")) {
                a.txHistory.add(tx);
            }
        }
        return a;
    }
}
