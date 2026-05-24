/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.app;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.Category;
import com.library.domain.entities.Librarian;
import com.library.domain.entities.Member;
import com.library.repository.CategoryRepositoryMySQLImpl;
import com.library.repository.ICategoryRepository;
import com.library.service.CategoryService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 *
 * @author rafianandra
 */
public class MainCategory {

    static final String RESET  = "\u001B[0m";
    static final String GREEN  = "\u001B[32m";
    static final String RED    = "\u001B[31m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE   = "\u001B[34m";
    static final String CYAN   = "\u001B[36m";

    static final int TEST_LIBRARIAN_ID = 7001;
    static final int TEST_MEMBER_ID    = 7002;

    static ICategoryRepository categoryRepo;
    static CategoryService     categoryService;

    static Librarian librarian;
    static Member    member;

    static int passed = 0;
    static int failed = 0;

    // ============================================================
    // MAIN
    // ============================================================
    public static void main(String[] args) {
        System.out.println(BLUE);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║       CATEGORY SERVICE — FULL TEST SUITE             ║");
        System.out.println("║       Strategi: Insert Fresh + Cleanup               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);

        try {
            setupDependencies();
            insertTestData();
            buildDomainObjects();

            runTest1_CreateCategory_HappyPath();
            runTest2_CreateCategory_EmptyName();
            runTest3_CreateCategory_NullName();
            runTest4_CreateCategory_DuplicateActive();
            runTest5_CreateCategory_DuplicateInactive();
            runTest6_CreateCategory_UnauthorizedMember();
            runTest7_UpdateCategory_HappyPath();
            runTest8_UpdateCategory_NameOnly_Overload();
            runTest9_UpdateCategory_EmptyName();
            runTest10_UpdateCategory_DuplicateNameOtherCategory();
            runTest11_UpdateCategory_SameNameSelf();
            runTest12_UpdateCategory_NotFound();
            runTest13_DeleteCategory_HappyPath();
            runTest14_DeleteCategory_NotFound();
            runTest15_DeleteCategory_Unauthorized();
            runTest16_ReactivateCategory_HappyPath();
            runTest17_ReactivateCategory_AlreadyActive();
            runTest18_ReactivateCategory_NotFound();
            runTest19_GetAllActiveCategories_ExcludesInactive();

        } catch (Exception e) {
            System.out.println(RED + "\n❌ FATAL ERROR DI TEST RUNNER: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            cleanupTestData();
            printSummary();
        }
    }

    // ============================================================
    // SETUP
    // ============================================================
    static void setupDependencies() {
        categoryRepo    = new CategoryRepositoryMySQLImpl();
        categoryService = new CategoryService(categoryRepo);
        System.out.println(CYAN + "[SETUP] Dependencies siap." + RESET);
    }

    static void insertTestData() throws SQLException {
        System.out.println(CYAN + "[SETUP] Inserting test data ke database..." + RESET);
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                "INSERT INTO users (id, name, email, password_hash, role, active, created_by) VALUES "
                + "(" + TEST_LIBRARIAN_ID + ", 'Test Librarian Cat', 'librarian_cat@lib.com', 'hash', 'LIBRARIAN', 1, 'TEST_SETUP'), "
                + "(" + TEST_MEMBER_ID    + ", 'Test Member Cat',    'member_cat@lib.com',    'hash', 'MEMBER',    1, 'TEST_SETUP')"
            );
        }
        System.out.println(CYAN + "[SETUP] Test data berhasil di-insert." + RESET);
    }

    static void buildDomainObjects() {
        librarian = new Librarian("Test Librarian Cat", "librarian_cat@lib.com", "hash", "EMP-CAT-01", "SHIFT-1");
        librarian.setId(TEST_LIBRARIAN_ID);

        member = new Member(TEST_MEMBER_ID, "Test Member Cat");

        System.out.println(CYAN + "[SETUP] Domain objects siap." + RESET);
    }

    // ============================================================
    // CLEANUP
    // ============================================================
    static void cleanupTestData() {
        System.out.println(CYAN + "\n[CLEANUP] Menghapus semua test data..." + RESET);
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM categories WHERE created_by = 'TEST_SETUP_CAT'");
            stmt.executeUpdate("DELETE FROM users WHERE id IN (" + TEST_LIBRARIAN_ID + "," + TEST_MEMBER_ID + ")");

            System.out.println(CYAN + "[CLEANUP] Selesai. Database bersih." + RESET);
        } catch (SQLException e) {
            System.out.println(RED + "[CLEANUP] ERROR: " + e.getMessage() + RESET);
        }
    }

    // ============================================================
    // HELPER
    // ============================================================
    static void assertPass(String label) {
        System.out.println(GREEN + "  ✔ LULUS: " + label + RESET);
        passed++;
    }

    static void assertFail(String label, String reason) {
        System.out.println(RED + "  ✖ GAGAL: " + label + " → " + reason + RESET);
        failed++;
    }

    static void printHeader(String title) {
        System.out.println(YELLOW + "\n" + title + RESET);
    }

    // Buat kategori langsung dan return object-nya
    static Category createCategory(String name, String description) {
        categoryService.createCategory(librarian, name, description);
        return categoryRepo.findByName(name);
    }

    // ============================================================
    // TEST 1 — createCategory: happy path
    // ============================================================
    static void runTest1_CreateCategory_HappyPath() {
        printHeader("[TEST 1] createCategory: Happy Path");
        try {
            categoryService.createCategory(librarian, "CAT-TEST-Fiksi", "Buku cerita fiksi");

            Category result = categoryRepo.findByName("CAT-TEST-Fiksi");
            if (result != null && result.getName().equals("CAT-TEST-Fiksi")) {
                assertPass("Kategori berhasil disimpan ke database");
            } else {
                assertFail("Kategori tersimpan", "findByName mengembalikan null");
            }

            if (result != null && result.isActive()) {
                assertPass("Kategori baru berstatus aktif secara default");
            } else {
                assertFail("Status aktif default", "isActive() = false");
            }

            if (result != null && librarian.getName().equals(result.getCreatedBy())) {
                assertPass("createdBy tersimpan sesuai nama actor");
            } else {
                assertFail("createdBy tersimpan", "actual: " + (result == null ? "null" : result.getCreatedBy()));
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 2 — createCategory: nama kosong
    // ============================================================
    static void runTest2_CreateCategory_EmptyName() {
        printHeader("[TEST 2] createCategory: Nama Kosong");
        try {
            categoryService.createCategory(librarian, "", "Deskripsi");
            assertFail("Nama kosong harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Nama kosong ditolak: " + e.getMessage());
        }

        // Spasi saja juga harus ditolak
        try {
            categoryService.createCategory(librarian, "   ", "Deskripsi");
            assertFail("Nama spasi saja harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Nama spasi saja ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // TEST 3 — createCategory: nama null
    // ============================================================
    static void runTest3_CreateCategory_NullName() {
        printHeader("[TEST 3] createCategory: Nama Null");
        try {
            categoryService.createCategory(librarian, null, "Deskripsi");
            assertFail("Nama null harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Nama null ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // TEST 4 — createCategory: nama duplikat kategori aktif
    // ============================================================
    static void runTest4_CreateCategory_DuplicateActive() {
        printHeader("[TEST 4] createCategory: Nama Duplikat (Kategori Aktif)");
        try {
            categoryService.createCategory(librarian, "CAT-TEST-Sains", "Buku sains");

            try {
                categoryService.createCategory(librarian, "CAT-TEST-Sains", "Sains duplikat");
                assertFail("Duplikat aktif harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("Duplikat aktif ditolak: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga di setup", e.getMessage());
        }
    }

    // ============================================================
    // TEST 5 — createCategory: nama duplikat kategori non-aktif
    // ============================================================
    static void runTest5_CreateCategory_DuplicateInactive() {
        printHeader("[TEST 5] createCategory: Nama Duplikat (Kategori Non-Aktif)");
        try {
            categoryService.createCategory(librarian, "CAT-TEST-Tidur", "Kategori yang akan di-delete");
            Category sleeping = categoryRepo.findByName("CAT-TEST-Tidur");
            if (sleeping == null) {
                assertFail("Setup kategori tidur gagal", "findByName null");
                return;
            }

            categoryService.deleteCategory(librarian, sleeping.getId());

            // Coba buat ulang dengan nama yang sama — harus ditolak dengan pesan reaktivasi
            try {
                categoryService.createCategory(librarian, "CAT-TEST-Tidur", "Coba buat lagi");
                assertFail("Duplikat non-aktif harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("Duplikat non-aktif ditolak dengan saran reaktivasi: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 6 — createCategory: actor tidak punya izin (Member)
    // ============================================================
    static void runTest6_CreateCategory_UnauthorizedMember() {
        printHeader("[TEST 6] createCategory: Member Tidak Punya Izin");
        try {
            categoryService.createCategory(member, "CAT-ILEGAL", "Seharusnya ditolak");
            assertFail("Member harus ditolak akses CRUD_CATEGORY", "sistem membiarkan");
        } catch (SecurityException e) {
            assertPass("Member ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // TEST 7 — updateCategory: happy path (nama + deskripsi)
    // ============================================================
    static void runTest7_UpdateCategory_HappyPath() {
        printHeader("[TEST 7] updateCategory: Happy Path (Nama + Deskripsi)");
        try {
            Category cat = createCategory("CAT-TEST-UpdateFull", "Deskripsi lama");
            if (cat == null) {
                assertFail("Setup createCategory gagal", "findByName null");
                return;
            }

            categoryService.updateCategory(librarian, cat.getId(), "CAT-TEST-UpdateFull-Baru", "Deskripsi baru");

            Category updated = categoryRepo.findById(cat.getId());
            if ("CAT-TEST-UpdateFull-Baru".equals(updated.getName())) {
                assertPass("Nama berhasil diupdate");
            } else {
                assertFail("Nama terupdate", "actual: " + updated.getName());
            }

            if ("Deskripsi baru".equals(updated.getDescription())) {
                assertPass("Deskripsi berhasil diupdate");
            } else {
                assertFail("Deskripsi terupdate", "actual: " + updated.getDescription());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 8 — updateCategory overload: hanya nama, deskripsi tidak berubah
    // ============================================================
    static void runTest8_UpdateCategory_NameOnly_Overload() {
        printHeader("[TEST 8] updateCategory (Overload): Hanya Nama, Deskripsi Tetap");
        try {
            Category cat = createCategory("CAT-TEST-OverloadLama", "Deskripsi yang harus tetap");
            if (cat == null) {
                assertFail("Setup createCategory gagal", "findByName null");
                return;
            }

            categoryService.updateCategory(librarian, cat.getId(), "CAT-TEST-OverloadBaru");

            Category updated = categoryRepo.findById(cat.getId());
            if ("CAT-TEST-OverloadBaru".equals(updated.getName())) {
                assertPass("Nama berhasil diupdate via overload");
            } else {
                assertFail("Nama terupdate via overload", "actual: " + updated.getName());
            }

            if ("Deskripsi yang harus tetap".equals(updated.getDescription())) {
                assertPass("Deskripsi tidak berubah saat pakai overload");
            } else {
                assertFail("Deskripsi tidak berubah", "actual: " + updated.getDescription());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 9 — updateCategory: nama baru kosong
    // ============================================================
    static void runTest9_UpdateCategory_EmptyName() {
        printHeader("[TEST 9] updateCategory: Nama Baru Kosong");
        try {
            Category cat = createCategory("CAT-TEST-EmptyUpdate", "Desc");
            if (cat == null) {
                assertFail("Setup createCategory gagal", "findByName null");
                return;
            }

            try {
                categoryService.updateCategory(librarian, cat.getId(), "", "Desc baru");
                assertFail("Nama kosong saat update harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("Nama kosong saat update ditolak: " + e.getMessage());
            }

            try {
                categoryService.updateCategory(librarian, cat.getId(), null, "Desc baru");
                assertFail("Nama null saat update harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("Nama null saat update ditolak: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga di setup", e.getMessage());
        }
    }

    // ============================================================
    // TEST 10 — updateCategory: nama baru konflik dengan kategori lain
    // ============================================================
    static void runTest10_UpdateCategory_DuplicateNameOtherCategory() {
        printHeader("[TEST 10] updateCategory: Nama Konflik Dengan Kategori Lain");
        try {
            Category catA = createCategory("CAT-TEST-ConflictA", "Desc A");
            Category catB = createCategory("CAT-TEST-ConflictB", "Desc B");

            if (catA == null || catB == null) {
                assertFail("Setup createCategory gagal", "salah satu null");
                return;
            }

            // Coba update catB dengan nama catA
            try {
                categoryService.updateCategory(librarian, catB.getId(), "CAT-TEST-ConflictA", "Desc");
                assertFail("Nama konflik dengan kategori lain harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("Nama konflik ditolak: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga di setup", e.getMessage());
        }
    }

    // ============================================================
    // TEST 11 — updateCategory: update dengan nama sendiri (harus boleh)
    // ============================================================
    static void runTest11_UpdateCategory_SameNameSelf() {
        printHeader("[TEST 11] updateCategory: Update Dengan Nama Sendiri (Harus Diizinkan)");
        try {
            Category cat = createCategory("CAT-TEST-SameName", "Deskripsi lama");
            if (cat == null) {
                assertFail("Setup createCategory gagal", "findByName null");
                return;
            }

            // Update dengan nama yang sama, hanya ubah deskripsi
            categoryService.updateCategory(librarian, cat.getId(), "CAT-TEST-SameName", "Deskripsi baru");

            Category updated = categoryRepo.findById(cat.getId());
            if ("Deskripsi baru".equals(updated.getDescription())) {
                assertPass("Update deskripsi dengan nama sendiri berhasil");
            } else {
                assertFail("Deskripsi terupdate", "actual: " + updated.getDescription());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 12 — updateCategory: ID tidak ditemukan
    // ============================================================
    static void runTest12_UpdateCategory_NotFound() {
        printHeader("[TEST 12] updateCategory: ID Tidak Ditemukan");
        try {
            categoryService.updateCategory(librarian, 999999, "Nama Baru", "Desc");
            assertFail("ID tidak ditemukan harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("ID tidak ditemukan ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // TEST 13 — deleteCategory: happy path (soft delete)
    // ============================================================
    static void runTest13_DeleteCategory_HappyPath() {
        printHeader("[TEST 13] deleteCategory: Happy Path (Soft Delete)");
        try {
            Category cat = createCategory("CAT-TEST-SoftDelete", "Akan di-soft delete");
            if (cat == null) {
                assertFail("Setup createCategory gagal", "findByName null");
                return;
            }

            categoryService.deleteCategory(librarian, cat.getId());

            // findById harus masih bisa nemu (soft delete, bukan hard delete)
            Category afterDelete = categoryRepo.findById(cat.getId());
            if (afterDelete != null && !afterDelete.isActive()) {
                assertPass("Soft delete berhasil, record masih ada tapi isActive = false");
            } else if (afterDelete == null) {
                assertFail("Soft delete", "record hilang dari DB (hard delete terjadi)");
            } else {
                assertFail("Soft delete", "isActive masih true");
            }

            // Kategori yang di-soft delete tidak boleh muncul di getAllActiveCategories
            List<Category> aktif = categoryService.getAllActiveCategories();
            boolean masihMuncul = aktif.stream().anyMatch(c -> c.getId().equals(cat.getId()));
            if (!masihMuncul) {
                assertPass("Kategori non-aktif tidak muncul di getAllActiveCategories");
            } else {
                assertFail("Kategori non-aktif tidak boleh muncul", "masih ada di list aktif");
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 14 — deleteCategory: ID tidak ditemukan
    // ============================================================
    static void runTest14_DeleteCategory_NotFound() {
        printHeader("[TEST 14] deleteCategory: ID Tidak Ditemukan");
        try {
            categoryService.deleteCategory(librarian, 999999);
            assertFail("ID tidak ditemukan harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("ID tidak ditemukan ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // TEST 15 — deleteCategory: actor tidak punya izin
    // ============================================================
    static void runTest15_DeleteCategory_Unauthorized() {
        printHeader("[TEST 15] deleteCategory: Member Tidak Punya Izin");
        try {
            Category cat = createCategory("CAT-TEST-DelUnauth", "Desc");
            if (cat == null) {
                assertFail("Setup createCategory gagal", "findByName null");
                return;
            }

            try {
                categoryService.deleteCategory(member, cat.getId());
                assertFail("Member harus ditolak delete kategori", "sistem membiarkan");
            } catch (SecurityException e) {
                assertPass("Member ditolak delete: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga di setup", e.getMessage());
        }
    }

    // ============================================================
    // TEST 16 — reactivateCategory: happy path
    // ============================================================
    static void runTest16_ReactivateCategory_HappyPath() {
        printHeader("[TEST 16] reactivateCategory: Happy Path");
        try {
            Category cat = createCategory("CAT-TEST-Reaktivasi", "Akan direaktivasi");
            if (cat == null) {
                assertFail("Setup createCategory gagal", "findByName null");
                return;
            }

            categoryService.deleteCategory(librarian, cat.getId());

            Category afterDelete = categoryRepo.findById(cat.getId());
            if (afterDelete == null || afterDelete.isActive()) {
                assertFail("Setup soft delete gagal", "isActive masih true atau null");
                return;
            }

            categoryService.reactivateCategory(librarian, cat.getId());

            Category afterReactivate = categoryRepo.findById(cat.getId());
            if (afterReactivate != null && afterReactivate.isActive()) {
                assertPass("Kategori berhasil diaktifkan kembali");
            } else {
                assertFail("Reaktivasi berhasil", "isActive masih false");
            }

            // Harus muncul kembali di getAllActiveCategories
            List<Category> aktif = categoryService.getAllActiveCategories();
            boolean muncul = aktif.stream().anyMatch(c -> c.getId().equals(cat.getId()));
            if (muncul) {
                assertPass("Kategori reaktivasi muncul kembali di getAllActiveCategories");
            } else {
                assertFail("Kategori muncul di list aktif", "tidak ditemukan");
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 17 — reactivateCategory: kategori yang sudah aktif
    // ============================================================
    static void runTest17_ReactivateCategory_AlreadyActive() {
        printHeader("[TEST 17] reactivateCategory: Kategori Yang Sudah Aktif");
        try {
            Category cat = createCategory("CAT-TEST-AlreadyActive", "Masih aktif");
            if (cat == null) {
                assertFail("Setup createCategory gagal", "findByName null");
                return;
            }

            try {
                categoryService.reactivateCategory(librarian, cat.getId());
                assertFail("Reaktivasi kategori aktif harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("Reaktivasi kategori aktif ditolak: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga di setup", e.getMessage());
        }
    }

    // ============================================================
    // TEST 18 — reactivateCategory: ID tidak ditemukan
    // ============================================================
    static void runTest18_ReactivateCategory_NotFound() {
        printHeader("[TEST 18] reactivateCategory: ID Tidak Ditemukan");
        try {
            categoryService.reactivateCategory(librarian, 999999);
            assertFail("ID tidak ditemukan harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("ID tidak ditemukan ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // TEST 19 — getAllActiveCategories: hanya return yang aktif
    // ============================================================
    static void runTest19_GetAllActiveCategories_ExcludesInactive() {
        printHeader("[TEST 19] getAllActiveCategories: Hanya Menampilkan Yang Aktif");
        try {
            Category aktifCat   = createCategory("CAT-TEST-AktifVisible",   "Harus muncul");
            Category inaktifCat = createCategory("CAT-TEST-InaktifHidden", "Tidak boleh muncul");

            if (aktifCat == null || inaktifCat == null) {
                assertFail("Setup createCategory gagal", "salah satu null");
                return;
            }

            categoryService.deleteCategory(librarian, inaktifCat.getId());

            List<Category> result = categoryService.getAllActiveCategories();

            boolean adaAktif = result.stream().anyMatch(c -> c.getId().equals(aktifCat.getId()));
            if (adaAktif) {
                assertPass("Kategori aktif muncul di list");
            } else {
                assertFail("Kategori aktif harus muncul", "tidak ditemukan");
            }

            boolean adaInaktif = result.stream().anyMatch(c -> c.getId().equals(inaktifCat.getId()));
            if (!adaInaktif) {
                assertPass("Kategori non-aktif tidak muncul di list");
            } else {
                assertFail("Kategori non-aktif tidak boleh muncul", "masih ada di list");
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // PRINT SUMMARY
    // ============================================================
    static void printSummary() {
        int total = passed + failed;
        System.out.println(BLUE);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.printf ("║  HASIL: %d/%d test lulus                              ║%n", passed, total);
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);
        if (failed == 0) {
            System.out.println(GREEN + "🎉 SEMUA TEST LULUS" + RESET);
        } else {
            System.out.println(RED + "⚠️  " + failed + " TEST GAGAL — cek output di atas" + RESET);
        }
    }
}