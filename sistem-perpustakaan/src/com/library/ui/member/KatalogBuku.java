package com.library.ui.member;

import com.library.ui.member.BookCard;
import com.library.domain.entities.BookTitle;
import com.library.domain.entities.Category;
import com.library.domain.entities.User;
import com.library.repository.IBookTitleRepository;
import com.library.repository.IBookCopyRepository;
import com.library.repository.ICategoryRepository;
import com.library.repository.BookTitleRepositoryMySQLImpl;
import com.library.repository.BookCopyRepositoryMySQLImpl;
import com.library.repository.CategoryRepositoryMySQLImpl;
import com.library.service.BookService;
import com.library.service.CategoryService;
import com.library.ui.auth.FormLogin;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author vert
 */
public class KatalogBuku extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(KatalogBuku.class.getName());
    private final BookService bookService;
    private final CategoryService categoryService;
    private List<BookTitle> allBooks; // cache semua buku
    private List<Category> allCategories; // cache semua kategori
    private User loggedInUser;

    /**
     * Creates new form KatalogBuku
     */
    public KatalogBuku(User user) {
        this.loggedInUser = user;
        // 1. Inisialisasi dependency
        IBookTitleRepository bookTitleRepo = new BookTitleRepositoryMySQLImpl();
        IBookCopyRepository bookCopyRepo = new BookCopyRepositoryMySQLImpl();
        ICategoryRepository categoryRepo = new CategoryRepositoryMySQLImpl();
        this.bookService = new BookService(bookTitleRepo, bookCopyRepo);
        this.categoryService = new CategoryService(categoryRepo);
        // 2. Inisialisasi komponen GUI
        initComponents();
        // 3. Load data dari database
        loadCategories();
        loadBooks();
        // 4. Pasang listener search (real-time saat mengetik)
        jTextFieldCariBuku.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterBooks();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterBooks();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterBooks();
            }
        });
    }

    private void loadCategories() {
        allCategories = categoryService.getAllActiveCategories();

        jComboBoxFilterKategori.removeAllItems();
        jComboBoxFilterKategori.addItem("Semua Kategori");
        for (Category cat : allCategories) {
            jComboBoxFilterKategori.addItem(cat.getName());
        }
    }

    private void loadBooks() {
        allBooks = bookService.searchCatalog(null); // ambil semua
        displayBooks(allBooks);
    }

    private void displayBooks(List<BookTitle> books) {
        jPanelBookList.removeAll();

        for (BookTitle book : books) {
            int stock = bookService.getAvailableStock(book.getId());
            BookCard card = new BookCard(book, stock);

            // Tambah margin antar card
            card.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Pasang listener ke tombol Detail
            card.setDetailButtonListener(e -> {
                DeskripsiBuku deskripsi = new DeskripsiBuku(book, stock, loggedInUser);
                deskripsi.setVisible(true);
                this.dispose();
            });

            jPanelBookList.add(card);
        }

        jPanelBookList.revalidate();
        jPanelBookList.repaint();
    }

    private void filterBooks() {
        if (allBooks == null) return;
        String keyword = jTextFieldCariBuku.getText().trim().toLowerCase();
        String selectedCategory = (String) jComboBoxFilterKategori.getSelectedItem();

        List<BookTitle> filtered = allBooks;

        // Filter berdasarkan kategori
        if (selectedCategory != null && !"Semua Kategori".equals(selectedCategory)) {
            filtered = filtered.stream()
                    .filter(b -> b.getCategory().getName().equals(selectedCategory))
                    .collect(Collectors.toList());
        }

        // Filter berdasarkan keyword search
        if (!keyword.isEmpty()) {
            filtered = filtered.stream()
                    .filter(b -> b.matchesKeyword(keyword))
                    .collect(Collectors.toList());
        }
        displayBooks(filtered);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPaneBooklist = new javax.swing.JScrollPane();
        jPanelBookList = new javax.swing.JPanel();
        jLabeljUDUL = new javax.swing.JLabel();
        jTextFieldCariBuku = new javax.swing.JTextField();
        jComboBoxFilterKategori = new javax.swing.JComboBox<>();
        jLabelCari = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButtonKatalog = new javax.swing.JButton();
        jButtonRiwayat = new javax.swing.JButton();
        jButtonLogOut = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1280, 720));
        setResizable(false);

        jScrollPaneBooklist.setPreferredSize(new java.awt.Dimension(1000, 570));

        jPanelBookList.setBackground(new java.awt.Color(255, 255, 255));
        jPanelBookList.setLayout(new javax.swing.BoxLayout(jPanelBookList, javax.swing.BoxLayout.Y_AXIS));
        jScrollPaneBooklist.setViewportView(jPanelBookList);

        jLabeljUDUL.setFont(new java.awt.Font("Sylfaen", 1, 30)); // NOI18N
        jLabeljUDUL.setText("KATALOG BUKU");

        jTextFieldCariBuku.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
        jTextFieldCariBuku.setPreferredSize(new java.awt.Dimension(300, 32));

        jComboBoxFilterKategori.setFont(new java.awt.Font("Sylfaen", 0, 14)); // NOI18N
        jComboBoxFilterKategori.setPreferredSize(new java.awt.Dimension(100, 32));
        jComboBoxFilterKategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFilterKategoriActionPerformed(evt);
            }
        });

        jLabelCari.setFont(new java.awt.Font("Sylfaen", 0, 14)); // NOI18N
        jLabelCari.setText("Cari buku");

        jPanel1.setBackground(new java.awt.Color(124, 173, 186));
        jPanel1.setPreferredSize(new java.awt.Dimension(235, 720));

        jButtonKatalog.setBackground(new java.awt.Color(63, 108, 120));
        jButtonKatalog.setFont(new java.awt.Font("Sylfaen", 0, 24)); // NOI18N
        jButtonKatalog.setText("KATALOG");
        jButtonKatalog.setBorderPainted(false);
        jButtonKatalog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonKatalogActionPerformed(evt);
            }
        });

        jButtonRiwayat.setBackground(new java.awt.Color(63, 108, 120));
        jButtonRiwayat.setFont(new java.awt.Font("Sylfaen", 0, 24)); // NOI18N
        jButtonRiwayat.setText("RIWAYAT");
        jButtonRiwayat.setBorderPainted(false);
        jButtonRiwayat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRiwayatActionPerformed(evt);
            }
        });

        jButtonLogOut.setBackground(new java.awt.Color(63, 108, 120));
        jButtonLogOut.setFont(new java.awt.Font("Sylfaen", 0, 24)); // NOI18N
        jButtonLogOut.setText("log out");
        jButtonLogOut.setBorderPainted(false);
        jButtonLogOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLogOutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonRiwayat)
                    .addComponent(jButtonKatalog)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonLogOut)
                        .addGap(29, 29, 29)))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(138, 138, 138)
                .addComponent(jButtonKatalog)
                .addGap(27, 27, 27)
                .addComponent(jButtonRiwayat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 415, Short.MAX_VALUE)
                .addComponent(jButtonLogOut)
                .addGap(26, 26, 26))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldCariBuku, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelCari))
                        .addGap(351, 351, 351)
                        .addComponent(jComboBoxFilterKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPaneBooklist, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabeljUDUL)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabeljUDUL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelCari)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldCariBuku, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxFilterKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneBooklist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxFilterKategoriActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jComboBoxFilterKategoriActionPerformed
        filterBooks();
    }// GEN-LAST:event_jComboBoxFilterKategoriActionPerformed

    private void jButtonKatalogActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonKatalogActionPerformed
        loadBooks();
        jTextFieldCariBuku.setText("");
        jComboBoxFilterKategori.setSelectedIndex(0);
    }// GEN-LAST:event_jButtonKatalogActionPerformed

    private void jButtonRiwayatActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonRiwayatActionPerformed
        new RiwayatPinjaman(loggedInUser).setVisible(true);
        this.dispose();
    }// GEN-LAST:event_jButtonRiwayatActionPerformed

    private void jButtonLogOutActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonLogOutActionPerformed
        new FormLogin().setVisible(true);
        this.dispose();
    }// GEN-LAST:event_jButtonLogOutActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
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
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new KatalogBuku(null).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonKatalog;
    private javax.swing.JButton jButtonLogOut;
    private javax.swing.JButton jButtonRiwayat;
    private javax.swing.JComboBox<String> jComboBoxFilterKategori;
    private javax.swing.JLabel jLabelCari;
    private javax.swing.JLabel jLabeljUDUL;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelBookList;
    private javax.swing.JScrollPane jScrollPaneBooklist;
    private javax.swing.JTextField jTextFieldCariBuku;
    // End of variables declaration//GEN-END:variables
}
