/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.repository;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.BookCopy;
import com.library.domain.entities.BookTitle;
import com.library.domain.entities.Category;
import com.library.domain.entities.Librarian;
import com.library.domain.entities.LoanTransaction;
import com.library.domain.entities.Member;
import com.library.domain.enums.BorrowType;
import com.library.domain.enums.LoanStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rafianandra
 */
public class LoanTransactionRepositoryMySQLImpl implements ILoanTransactionRepository {

    // Implementasi metode save untuk menyimpan LoanTransaction ke database MySQL
    @Override
    public void save(LoanTransaction entity) {
        String sql = "INSERT INTO loans (member_id, book_copy_id, borrow_type, status, request_date, "
            + "scheduled_pickup_date, due_date, fine_amount, created_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, entity.getMember().getId());
            stmt.setInt(2, entity.getBookCopy().getId());
            stmt.setString(3, entity.getBorrowType().name());
            stmt.setString(4, entity.getStatus().name());

            if (entity.getRequestDate() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(entity.getRequestDate()));
            } else {
                stmt.setNull(5, java.sql.Types.TIMESTAMP);
            }

            if (entity.getScheduledPickupDate() != null) {
                stmt.setDate(6, java.sql.Date.valueOf(entity.getScheduledPickupDate()));
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }

            if (entity.getDueDate() != null) {
                stmt.setDate(7, java.sql.Date.valueOf(entity.getDueDate()));
            } else {
                stmt.setNull(7, java.sql.Types.DATE);
            }

            if (entity.getFineAmount() != null) {
                stmt.setDouble(8, entity.getFineAmount());
            } else {
                stmt.setDouble(8, 0.0);
            }

            if (entity.getCreatedBy() != null) {
                stmt.setString(9, entity.getCreatedBy());
            } else {
                stmt.setNull(9, java.sql.Types.VARCHAR);
            }

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Implementasi metode update untuk memperbarui status dan tanggal transaksi
    @Override
    public void update(LoanTransaction entity) {
        String sql = "UPDATE loans SET status = ?, scheduled_pickup_date = ?, due_date = ?, "
            + "return_date = ?, cancelled_at = ?, fine_amount = ?, fine_per_day_snapshot = ?, "
            + "fine_calculated_at = ?, approved_by = ?, fine_paid_at = ?, fine_processed_by = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getStatus().name());

            if (entity.getScheduledPickupDate() != null) {
                stmt.setDate(2, java.sql.Date.valueOf(entity.getScheduledPickupDate()));
            } else {
                stmt.setNull(2, java.sql.Types.DATE);
            }

            if (entity.getDueDate() != null) {
                stmt.setDate(3, java.sql.Date.valueOf(entity.getDueDate()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }

            if (entity.getReturnDate() != null) {
                stmt.setDate(4, java.sql.Date.valueOf(entity.getReturnDate()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }

            if (entity.getCancelledAt() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(entity.getCancelledAt()));
            } else {
                stmt.setNull(5, java.sql.Types.TIMESTAMP);
            }

            if (entity.getFineAmount() != null) {
                stmt.setDouble(6, entity.getFineAmount());
            } else {
                stmt.setNull(6, java.sql.Types.DOUBLE);
            }

            if (entity.getFinePerDaySnapshot() != null) {
                stmt.setDouble(7, entity.getFinePerDaySnapshot());
            } else {
                stmt.setNull(7, java.sql.Types.DOUBLE);
            }

            if (entity.getFineCalculatedAt() != null) {
                stmt.setTimestamp(8, Timestamp.valueOf(entity.getFineCalculatedAt()));
            } else {
                stmt.setNull(8, java.sql.Types.TIMESTAMP);
            }

            // Parameter 9 — approved_by (bug fix)
            if (entity.getApprovedBy() != null) {
                stmt.setInt(9, entity.getApprovedBy().getId());
            } else {
                stmt.setNull(9, java.sql.Types.INTEGER);
            }

            if (entity.getFinePaidAt() != null) {
                stmt.setTimestamp(10, Timestamp.valueOf(entity.getFinePaidAt()));
            } else {
                stmt.setNull(10, java.sql.Types.TIMESTAMP);
            }

            if (entity.getFineProcessedBy() != null) {
                stmt.setInt(11, entity.getFineProcessedBy().getId());
            } else {
                stmt.setNull(11, java.sql.Types.INTEGER);
            }

            // Parameter 12 — WHERE id = ?
            stmt.setInt(12, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Belum diimplementasikan.");
    }

    @Override
    public LoanTransaction findById(Integer id) {
        String sql = getBaseSelectQuery() + " WHERE l.id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLoanTransaction(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<LoanTransaction> findAll() {
        String sql = getBaseSelectQuery() + " ORDER BY l.request_date DESC";
        List<LoanTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToLoanTransaction(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<LoanTransaction> findByMemberId(Integer memberId) {
        List<LoanTransaction> list = new ArrayList<>();
        String sql = getBaseSelectQuery() + " WHERE l.member_id = ? ORDER BY l.request_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoanTransaction(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<LoanTransaction> findActiveLoansByMemberId(Integer memberId) {
        List<LoanTransaction> list = new ArrayList<>();
        String sql = getBaseSelectQuery() 
                + " WHERE l.member_id = ? AND l.status IN ('REQUESTED', 'WAITING_PICKUP', 'ACTIVE', 'OVERDUE')";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoanTransaction(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<LoanTransaction> findByStatus(LoanStatus status) {
        List<LoanTransaction> list = new ArrayList<>();
        String sql = getBaseSelectQuery() + " WHERE l.status = ? ORDER BY l.request_date ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoanTransaction(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<LoanTransaction> findByStatusAndScheduledPickupDate(LoanStatus status, LocalDate date) {
        List<LoanTransaction> list = new ArrayList<>();
        String sql = getBaseSelectQuery() + " WHERE l.status = ? AND l.scheduled_pickup_date = ? ORDER BY l.request_date ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoanTransaction(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<LoanTransaction> findOverdueActive(LocalDate today) {
        List<LoanTransaction> list = new ArrayList<>();
        String sql = getBaseSelectQuery() + " WHERE l.status = 'ACTIVE' AND l.due_date < ? ORDER BY l.due_date ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(today));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoanTransaction(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<LoanTransaction> findExpiredWaitingPickup(LocalDate cutoffDate) {
        List<LoanTransaction> list = new ArrayList<>();
        String sql = getBaseSelectQuery() + " WHERE l.status = 'WAITING_PICKUP' AND l.scheduled_pickup_date < ? ORDER BY l.scheduled_pickup_date ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(cutoffDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoanTransaction(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Helper untuk biar ga nulis sql terus ini cuma buat ambil semua datu buat bisa di rekonstruct ke LoanTransaction objk
    private String getBaseSelectQuery() {
        return "SELECT l.id AS loan_id, l.borrow_type, l.status AS loan_status, "
                + "l.request_date, l.scheduled_pickup_date, l.due_date, l.return_date, l.cancelled_at, "
                + "l.fine_amount, l.fine_per_day_snapshot, l.fine_calculated_at, "
                + "l.fine_paid_at, l.fine_processed_by, "
                + "m.id AS member_id, m.name AS member_name, "
                + "bc.id AS copy_id, bc.location, bc.status AS copy_status, "
            + "bt.id AS title_id, bt.title, bt.author, "
            + "c.id AS cat_id, c.name AS cat_name, "
            + "lb.id AS librarian_id, lb.name AS librarian_name, "
            + "fp.id AS fine_processed_by_id, fp.name AS fine_processed_by_name "
                + "FROM loans l "
                + "INNER JOIN users m ON l.member_id = m.id "
                + "INNER JOIN book_copies bc ON l.book_copy_id = bc.id "
                + "INNER JOIN book_titles bt ON bc.book_title_id = bt.id "
            + "LEFT JOIN categories c ON bt.category_id = c.id "
            + "LEFT JOIN users lb ON l.approved_by = lb.id "
            + "LEFT JOIN users fp ON l.fine_processed_by = fp.id";
    }

    // Implementasi pemetaan ResultSet ke dalam objek LoanTransaction
    private LoanTransaction mapResultSetToLoanTransaction(ResultSet rs) throws SQLException {
        LoanTransaction txn = new LoanTransaction();

        txn.setId(rs.getInt("loan_id"));
        txn.setBorrowType(BorrowType.valueOf(rs.getString("borrow_type")));
        txn.setStatus(LoanStatus.valueOf(rs.getString("loan_status")));

        if (rs.getTimestamp("request_date") != null) {
            txn.setRequestDate(rs.getTimestamp("request_date").toLocalDateTime());
        }
        if (rs.getDate("scheduled_pickup_date") != null) {
            txn.setScheduledPickupDate(rs.getDate("scheduled_pickup_date").toLocalDate());
        }
        if (rs.getDate("due_date") != null) {
            txn.setDueDate(rs.getDate("due_date").toLocalDate());
        }
        if (rs.getDate("return_date") != null) {
            txn.setReturnDate(rs.getDate("return_date").toLocalDate());
        }
        if (rs.getTimestamp("cancelled_at") != null) {
            txn.setCancelledAt(rs.getTimestamp("cancelled_at").toLocalDateTime());
        }

        txn.setFineAmount(rs.getDouble("fine_amount"));
        txn.setFinePerDaySnapshot(rs.getDouble("fine_per_day_snapshot"));

        if (rs.getTimestamp("fine_calculated_at") != null) {
            txn.setFineCalculatedAt(rs.getTimestamp("fine_calculated_at").toLocalDateTime());
        }

        if (rs.getTimestamp("fine_paid_at") != null) {
            txn.setFinePaidAt(rs.getTimestamp("fine_paid_at").toLocalDateTime());
        }

        // Mapping dilakukan secara dangkal karna harusnya tidak perlu semua data

        // Mapping Member
        Member member = new Member(rs.getInt("member_id"), rs.getString("member_name"));
        txn.setMember(member);

        // Mapping Category
        Category category = new Category(rs.getInt("cat_id"), rs.getString("cat_name"));

        // Mapping BookTitle
        BookTitle title = new BookTitle(rs.getInt("title_id"), rs.getString("title"), rs.getString("author"), category);
       
        // Mapping BookCopy
        BookCopy copy = new BookCopy(rs.getInt("copy_id"), rs.getString("location"), title);
        txn.setBookCopy(copy);

        // Mapping Librarian (approvedBy)
        int librarianId = rs.getInt("librarian_id");
        if (!rs.wasNull()) {
            Librarian librarian = new Librarian(rs.getString("librarian_name"), null, null, null, null);
            librarian.setId(librarianId);
            txn.setApprovedBy(librarian);
        }

        int fineProcessedById = rs.getInt("fine_processed_by_id");
        if (!rs.wasNull()) {
            Librarian fineProcessor = new Librarian(rs.getString("fine_processed_by_name"), null, null, null, null);
            fineProcessor.setId(fineProcessedById);
            txn.setFineProcessedBy(fineProcessor);
        }

        return txn;
    }
}