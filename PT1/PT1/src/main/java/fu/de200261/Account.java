package fu.de200261;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Account {
    private String username;
    private String email;
    private LocalDate dateOfBirth;
    private String phone;
    private String salt;
    private AccountStatus status;
    private int failedAttempts;
    private boolean locked;
    private List<String> passwordHistory;

    public Account(String username, String email, LocalDate dateOfBirth, String phone, String salt, String initialPasswordHash) {
        this.username = username.toLowerCase(Locale.ROOT);
        this.email = email.toLowerCase(Locale.ROOT);
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.salt = salt;
        this.status = AccountStatus.ACTIVE;
        this.failedAttempts = 0;
        this.locked = false;
        this.passwordHistory = new ArrayList<>();
        this.passwordHistory.add(initialPasswordHash);
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }
    public String getSalt() { return salt; }
    public AccountStatus getStatus() { return status; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return locked; }
    public List<String> getPasswordHistory() { return List.copyOf(passwordHistory); }

    public String getCurrentPasswordHash() {
        if (passwordHistory.isEmpty()) return null;
        return passwordHistory.get(passwordHistory.size() - 1);
    }

    // Các hàm thay đổi trạng thái để package-private theo yêu cầu đề bài
    void incrementFailedAttempts() { this.failedAttempts++; }
    void resetFailedAttempts() { this.failedAttempts = 0; }
    void lock() { this.locked = true; }
    void unlock() {
        this.locked = false;
        this.failedAttempts = 0;
    }
    void setStatus(AccountStatus status) { this.status = status; }
}