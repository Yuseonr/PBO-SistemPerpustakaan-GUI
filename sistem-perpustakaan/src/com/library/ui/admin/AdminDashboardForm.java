/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.library.ui.admin;

import com.library.domain.entities.BookCopy;
import com.library.domain.entities.BookTitle;
import com.library.domain.entities.Category;
import com.library.domain.entities.LoanTransaction;
import com.library.domain.entities.User;
import com.library.domain.enums.UserRole;
import com.library.repository.BookCopyRepositoryMySQLImpl;
import com.library.repository.BookTitleRepositoryMySQLImpl;
import com.library.repository.CategoryRepositoryMySQLImpl;
import com.library.repository.LoanTransactionRepositoryMySQLImpl;
import com.library.repository.UserRepositoryMySQLImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author delli
 */
public class AdminDashboardForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AdminDashboardForm.class.getName());
    private User loggedInUser;
    /**
     * Creates new form AdminDashboardForm
     */
    public AdminDashboardForm(User user) {
        this.loggedInUser = user;
        initComponents();
        loadDashboardData();
    }
    private void loadDashboardData() {
        try {
            // Inisialisasi Repository
            UserRepositoryMySQLImpl userRepo = new UserRepositoryMySQLImpl();
            BookTitleRepositoryMySQLImpl titleRepo = new BookTitleRepositoryMySQLImpl();
            BookCopyRepositoryMySQLImpl copyRepo = new BookCopyRepositoryMySQLImpl();
            CategoryRepositoryMySQLImpl catRepo = new CategoryRepositoryMySQLImpl();
            LoanTransactionRepositoryMySQLImpl loanRepo = new LoanTransactionRepositoryMySQLImpl();

            // --- 1. Hitung Pengguna (Member, Admin, Librarian) ---
            List<User> users = userRepo.findAll();
            long totalAdmin = users.stream().filter(u -> u.getRole() == UserRole.ADMIN).count();
            long totalLibrarian = users.stream().filter(u -> u.getRole() == UserRole.LIBRARIAN).count();
            long totalMember = users.stream().filter(u -> u.getRole() == UserRole.MEMBER).count();

            // Set ke Label Panel Atas
            jLabelNumTotalAdmin.setText(String.valueOf(totalAdmin));
            jLabelNumTotalLibrarian.setText(String.valueOf(totalLibrarian));
            jLabelNumTotalMember.setText(String.valueOf(totalMember));

            // --- 2. Hitung Entitas Perpustakaan ---
            List<BookTitle> titles = titleRepo.findAll();
            List<BookCopy> copies = copyRepo.findAll();
            List<Category> categories = catRepo.findAll();

            // Set ke Label Panel Bawah
            jLabelNumTotalJudul.setText(String.valueOf(titles.size()));
            jLabelNumTotalBuku.setText(String.valueOf(copies.size()));
            jLabelNumTotalKategori.setText(String.valueOf(categories.size()));

            // --- 3. Hitung Statistik Buku Paling Sering Dipinjam ---
            List<LoanTransaction> loans = loanRepo.findAll();
            Map<String, Integer> borrowCountMap = new HashMap<>(); 
            Map<String, String> authorMap = new HashMap<>(); 

            for (LoanTransaction loan : loans) {
                // Pastikan copy dan title tidak null
                if (loan.getBookCopy() != null && loan.getBookCopy().getBookTitle() != null) {
                    BookTitle title = loan.getBookCopy().getBookTitle();
                    String judul = title.getTitle();
                    
                    // Tambah hitungan setiap kali judul ini muncul di transaksi
                    borrowCountMap.put(judul, borrowCountMap.getOrDefault(judul, 0) + 1);
                    authorMap.putIfAbsent(judul, title.getAuthor()); // Simpan nama penulisnya
                }
            }

            // Urutkan Map berdasarkan nilai tertinggi (Descending / Sering Dipinjam)
            List<Map.Entry<String, Integer>> sortedStats = new java.util.ArrayList<>(borrowCountMap.entrySet());
            sortedStats.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            // --- 4. Masukkan ke jTableStatBuku ---
            DefaultTableModel model = (DefaultTableModel) jTableStatBuku.getModel();
            model.setRowCount(0);
            
            int no = 1;
            for (Map.Entry<String, Integer> entry : sortedStats) {
                String judul = entry.getKey();
                int totalDipinjam = entry.getValue();
                String penulis = authorMap.get(judul);
                
                model.addRow(new Object[]{no++, judul, penulis, totalDipinjam + " kali"});
            }

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat data dashboard Admin: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        Role = new javax.swing.JLabel();
        jButtonDashboard = new javax.swing.JButton();
        jButtonAkun = new javax.swing.JButton();
        jButtonKonfigurasi = new javax.swing.JButton();
        jButtonLogOut = new javax.swing.JButton();
        jLabelJudul = new javax.swing.JLabel();
        jPanelTotalMember = new javax.swing.JPanel();
        jLabelNumTotalMember = new javax.swing.JLabel();
        jLabelTotalMember = new javax.swing.JLabel();
        jPanelTotalAdmin = new javax.swing.JPanel();
        jLabelNumTotalAdmin = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        JPanelTotalLibrarian = new javax.swing.JPanel();
        jLabelNumTotalLibrarian = new javax.swing.JLabel();
        jLabelTotalLibrarian = new javax.swing.JLabel();
        jPanelTotalJudul = new javax.swing.JPanel();
        jLabelNumTotalJudul = new javax.swing.JLabel();
        jLabelTotalJudul = new javax.swing.JLabel();
        jPanelTotalBuku = new javax.swing.JPanel();
        jLabelNumTotalBuku = new javax.swing.JLabel();
        jLabelTotalBuku = new javax.swing.JLabel();
        jPanelTotalKategori = new javax.swing.JPanel();
        jLabelNumTotalKategori = new javax.swing.JLabel();
        jLabelTotalKategori = new javax.swing.JLabel();
        jLabelDesk = new javax.swing.JLabel();
        jScrollPaneStatBuku = new javax.swing.JScrollPane();
        jTableStatBuku = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1280, 720));

        jPanel1.setBackground(new java.awt.Color(125, 173, 186));
        jPanel1.setPreferredSize(new java.awt.Dimension(235, 720));

        Role.setFont(new java.awt.Font("Sylfaen", 1, 18)); // NOI18N
        Role.setText("ADMIN");

        jButtonDashboard.setBackground(new java.awt.Color(63, 108, 120));
        jButtonDashboard.setFont(new java.awt.Font("Sylfaen", 0, 24)); // NOI18N
        jButtonDashboard.setText("DASHBOARD");
        jButtonDashboard.setBorderPainted(false);
        jButtonDashboard.setPreferredSize(new java.awt.Dimension(192, 38));
        jButtonDashboard.addActionListener(this::jButtonDashboardActionPerformed);

        jButtonAkun.setBackground(new java.awt.Color(63, 108, 120));
        jButtonAkun.setFont(new java.awt.Font("Sylfaen", 0, 24)); // NOI18N
        jButtonAkun.setText("AKUN");
        jButtonAkun.setBorderPainted(false);
        jButtonAkun.setPreferredSize(new java.awt.Dimension(192, 38));
        jButtonAkun.addActionListener(this::jButtonAkunActionPerformed);

        jButtonKonfigurasi.setBackground(new java.awt.Color(63, 108, 120));
        jButtonKonfigurasi.setFont(new java.awt.Font("Sylfaen", 0, 24)); // NOI18N
        jButtonKonfigurasi.setText("KONFIGURASI");
        jButtonKonfigurasi.setBorderPainted(false);
        jButtonKonfigurasi.setPreferredSize(new java.awt.Dimension(192, 38));
        jButtonKonfigurasi.addActionListener(this::jButtonKonfigurasiActionPerformed);

        jButtonLogOut.setBackground(new java.awt.Color(63, 108, 120));
        jButtonLogOut.setFont(new java.awt.Font("Sylfaen", 0, 24)); // NOI18N
        jButtonLogOut.setText("log out");
        jButtonLogOut.setBorderPainted(false);
        jButtonLogOut.addActionListener(this::jButtonLogOutActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(76, 76, 76)
                        .addComponent(Role))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonKonfigurasi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jButtonAkun, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButtonDashboard, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(jButtonLogOut)))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(Role)
                .addGap(53, 53, 53)
                .addComponent(jButtonDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jButtonAkun, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(jButtonKonfigurasi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonLogOut)
                .addGap(38, 38, 38))
        );

        jLabelJudul.setFont(new java.awt.Font("Sylfaen", 1, 24)); // NOI18N
        jLabelJudul.setText("DASHBOARD");

        jPanelTotalMember.setBackground(new java.awt.Color(102, 255, 153));

        jLabelNumTotalMember.setFont(new java.awt.Font("Sylfaen", 1, 18)); // NOI18N
        jLabelNumTotalMember.setText("32");

        jLabelTotalMember.setFont(new java.awt.Font("Sylfaen", 0, 14)); // NOI18N
        jLabelTotalMember.setText("Total Member");

        javax.swing.GroupLayout jPanelTotalMemberLayout = new javax.swing.GroupLayout(jPanelTotalMember);
        jPanelTotalMember.setLayout(jPanelTotalMemberLayout);
        jPanelTotalMemberLayout.setHorizontalGroup(
            jPanelTotalMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalMemberLayout.createSequentialGroup()
                .addContainerGap(36, Short.MAX_VALUE)
                .addComponent(jLabelTotalMember)
                .addGap(33, 33, 33))
            .addGroup(jPanelTotalMemberLayout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(jLabelNumTotalMember, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelTotalMemberLayout.setVerticalGroup(
            jPanelTotalMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalMemberLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabelNumTotalMember)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTotalMember)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jPanelTotalAdmin.setBackground(new java.awt.Color(102, 255, 153));

        jLabelNumTotalAdmin.setFont(new java.awt.Font("Sylfaen", 1, 18)); // NOI18N
        jLabelNumTotalAdmin.setText("3");

        jLabel8.setFont(new java.awt.Font("Sylfaen", 0, 14)); // NOI18N
        jLabel8.setText("Total Admin");

        javax.swing.GroupLayout jPanelTotalAdminLayout = new javax.swing.GroupLayout(jPanelTotalAdmin);
        jPanelTotalAdmin.setLayout(jPanelTotalAdminLayout);
        jPanelTotalAdminLayout.setHorizontalGroup(
            jPanelTotalAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalAdminLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(jPanelTotalAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelNumTotalAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        jPanelTotalAdminLayout.setVerticalGroup(
            jPanelTotalAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalAdminLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabelNumTotalAdmin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        JPanelTotalLibrarian.setBackground(new java.awt.Color(51, 255, 153));

        jLabelNumTotalLibrarian.setFont(new java.awt.Font("Sylfaen", 1, 18)); // NOI18N
        jLabelNumTotalLibrarian.setText("3");

        jLabelTotalLibrarian.setFont(new java.awt.Font("Sylfaen", 0, 14)); // NOI18N
        jLabelTotalLibrarian.setText("Total Librarian");

        javax.swing.GroupLayout JPanelTotalLibrarianLayout = new javax.swing.GroupLayout(JPanelTotalLibrarian);
        JPanelTotalLibrarian.setLayout(JPanelTotalLibrarianLayout);
        JPanelTotalLibrarianLayout.setHorizontalGroup(
            JPanelTotalLibrarianLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPanelTotalLibrarianLayout.createSequentialGroup()
                .addContainerGap(46, Short.MAX_VALUE)
                .addGroup(JPanelTotalLibrarianLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JPanelTotalLibrarianLayout.createSequentialGroup()
                        .addComponent(jLabelNumTotalLibrarian, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JPanelTotalLibrarianLayout.createSequentialGroup()
                        .addComponent(jLabelTotalLibrarian)
                        .addGap(32, 32, 32))))
        );
        JPanelTotalLibrarianLayout.setVerticalGroup(
            JPanelTotalLibrarianLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPanelTotalLibrarianLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(jLabelNumTotalLibrarian)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTotalLibrarian, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelTotalJudul.setBackground(new java.awt.Color(153, 255, 255));

        jLabelNumTotalJudul.setFont(new java.awt.Font("Sylfaen", 1, 18)); // NOI18N
        jLabelNumTotalJudul.setText("4");

        jLabelTotalJudul.setFont(new java.awt.Font("Sylfaen", 0, 14)); // NOI18N
        jLabelTotalJudul.setText("Total Judul");

        javax.swing.GroupLayout jPanelTotalJudulLayout = new javax.swing.GroupLayout(jPanelTotalJudul);
        jPanelTotalJudul.setLayout(jPanelTotalJudulLayout);
        jPanelTotalJudulLayout.setHorizontalGroup(
            jPanelTotalJudulLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalJudulLayout.createSequentialGroup()
                .addGroup(jPanelTotalJudulLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelTotalJudulLayout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addComponent(jLabelNumTotalJudul, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelTotalJudulLayout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabelTotalJudul, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelTotalJudulLayout.setVerticalGroup(
            jPanelTotalJudulLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalJudulLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabelNumTotalJudul)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTotalJudul)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanelTotalBuku.setBackground(new java.awt.Color(102, 255, 255));

        jLabelNumTotalBuku.setFont(new java.awt.Font("Sylfaen", 1, 18)); // NOI18N
        jLabelNumTotalBuku.setText("5");

        jLabelTotalBuku.setFont(new java.awt.Font("Sylfaen", 0, 14)); // NOI18N
        jLabelTotalBuku.setText("Total Buku");

        javax.swing.GroupLayout jPanelTotalBukuLayout = new javax.swing.GroupLayout(jPanelTotalBuku);
        jPanelTotalBuku.setLayout(jPanelTotalBukuLayout);
        jPanelTotalBukuLayout.setHorizontalGroup(
            jPanelTotalBukuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalBukuLayout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(jLabelNumTotalBuku, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(54, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelTotalBukuLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelTotalBuku, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelTotalBukuLayout.setVerticalGroup(
            jPanelTotalBukuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalBukuLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabelNumTotalBuku)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTotalBuku)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanelTotalKategori.setBackground(new java.awt.Color(102, 255, 255));

        jLabelNumTotalKategori.setFont(new java.awt.Font("Sylfaen", 1, 18)); // NOI18N
        jLabelNumTotalKategori.setText("5 ");

        jLabelTotalKategori.setFont(new java.awt.Font("Sylfaen", 0, 14)); // NOI18N
        jLabelTotalKategori.setText("Total Kategori");

        javax.swing.GroupLayout jPanelTotalKategoriLayout = new javax.swing.GroupLayout(jPanelTotalKategori);
        jPanelTotalKategori.setLayout(jPanelTotalKategoriLayout);
        jPanelTotalKategoriLayout.setHorizontalGroup(
            jPanelTotalKategoriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalKategoriLayout.createSequentialGroup()
                .addContainerGap(40, Short.MAX_VALUE)
                .addGroup(jPanelTotalKategoriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelTotalKategoriLayout.createSequentialGroup()
                        .addComponent(jLabelNumTotalKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelTotalKategoriLayout.createSequentialGroup()
                        .addComponent(jLabelTotalKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17))))
        );
        jPanelTotalKategoriLayout.setVerticalGroup(
            jPanelTotalKategoriLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotalKategoriLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabelNumTotalKategori)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTotalKategori)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jLabelDesk.setFont(new java.awt.Font("Sylfaen", 0, 14)); // NOI18N
        jLabelDesk.setText("Buku yang sering di pinjam");

        jScrollPaneStatBuku.setPreferredSize(new java.awt.Dimension(900, 402));

        jTableStatBuku.setFont(new java.awt.Font("Sylfaen", 0, 12)); // NOI18N
        jTableStatBuku.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "No", "Judul Buku", "Penulis", "Total Dipinjam"
            }
        ));
        jScrollPaneStatBuku.setViewportView(jTableStatBuku);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(58, 58, 58)
                            .addComponent(jLabelDesk, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGap(46, 46, 46)
                            .addComponent(jScrollPaneStatBuku, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabelJudul, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jPanelTotalMember, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanelTotalJudul, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(176, 176, 176)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jPanelTotalBuku, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanelTotalKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jPanelTotalAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(JPanelTotalLibrarian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                .addGap(181, 181, 181))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 733, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabelJudul)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelTotalMember, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelTotalAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanelTotalJudul, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelTotalBuku, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(JPanelTotalLibrarian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanelTotalKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(28, 28, 28)
                .addComponent(jLabelDesk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPaneStatBuku, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDashboardActionPerformed
        new AdminDashboardForm(loggedInUser).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButtonDashboardActionPerformed

    private void jButtonAkunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAkunActionPerformed
        new ManajemenAkun(loggedInUser).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButtonAkunActionPerformed

    private void jButtonKonfigurasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonKonfigurasiActionPerformed
        new Konfigurasi(loggedInUser).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButtonKonfigurasiActionPerformed

    private void jButtonLogOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLogOutActionPerformed
        new com.library.ui.auth.FormLogin().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButtonLogOutActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new AdminDashboardForm(null).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel JPanelTotalLibrarian;
    private javax.swing.JLabel Role;
    private javax.swing.JButton jButtonAkun;
    private javax.swing.JButton jButtonDashboard;
    private javax.swing.JButton jButtonKonfigurasi;
    private javax.swing.JButton jButtonLogOut;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelDesk;
    private javax.swing.JLabel jLabelJudul;
    private javax.swing.JLabel jLabelNumTotalAdmin;
    private javax.swing.JLabel jLabelNumTotalBuku;
    private javax.swing.JLabel jLabelNumTotalJudul;
    private javax.swing.JLabel jLabelNumTotalKategori;
    private javax.swing.JLabel jLabelNumTotalLibrarian;
    private javax.swing.JLabel jLabelNumTotalMember;
    private javax.swing.JLabel jLabelTotalBuku;
    private javax.swing.JLabel jLabelTotalJudul;
    private javax.swing.JLabel jLabelTotalKategori;
    private javax.swing.JLabel jLabelTotalLibrarian;
    private javax.swing.JLabel jLabelTotalMember;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelTotalAdmin;
    private javax.swing.JPanel jPanelTotalBuku;
    private javax.swing.JPanel jPanelTotalJudul;
    private javax.swing.JPanel jPanelTotalKategori;
    private javax.swing.JPanel jPanelTotalMember;
    private javax.swing.JScrollPane jScrollPaneStatBuku;
    private javax.swing.JTable jTableStatBuku;
    // End of variables declaration//GEN-END:variables
}
