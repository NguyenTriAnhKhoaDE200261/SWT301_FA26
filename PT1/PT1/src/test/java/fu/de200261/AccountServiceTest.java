package fu.de200261;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    AccountService service;

    static final String USER = "alice_01";
    static final String EMAIL = "alice@example.com";
    static final String PASS = "Secret@123";
    static final String WRONG_PASS = "Wrong@123";
    static final LocalDate DOB = LocalDate.of(2000, 1, 1);
    static final String PHONE = "0323456789";

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    @Nested
    class Register {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("fu.de200261.AccountServiceTest#invalidRegisterInputs")
        void register_InvalidInput_ReturnsExpectedCode(String desc, String u, String e, String p, String c,
                                                       LocalDate dob, String phone, ResultCode expected) {
            assertEquals(expected, service.register(u, e, p, c, dob, phone));
            assertTrue(service.findByUsername(u).isEmpty());
        }

        @ParameterizedTest(name = "[{index}] today - {0} năm + {1} ngày -> {2}")
        @CsvSource({
                "18, 0, SUCCESS",
                "18, 1, UNDERAGE",
                "0, 1, INVALID_INPUT"
        })
        void register_AgeBoundary(int yearsAgo, int plusDays, ResultCode expected) {
            LocalDate dob = LocalDate.now().minusYears(yearsAgo).plusDays(plusDays);
            assertEquals(expected, service.register(USER, EMAIL, PASS, PASS, dob, null));
        }
    }

    @Nested
    class Login {

        @BeforeEach
        void registerUser() {
            service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);
        }

        @Test
        void login_Success_ResetsFailedAttempts() {
            assertEquals(ResultCode.SUCCESS, service.login(USER, PASS));
            assertEquals(0, service.findByUsername(USER).get().getFailedAttempts());
        }

        @Test
        void login_NonExistentUser_ReturnsInvalidCredentials() {
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login("not_exist", PASS));
        }

        @ParameterizedTest(name = "[{index}] {0} lần sai -> {1}, locked={2}")
        @CsvSource({
                "3, INVALID_CREDENTIALS, false",
                "4, ACCOUNT_LOCKED, true",
                "5, ACCOUNT_LOCKED, true"
        })
        void login_FailedAttemptsBoundary(int failures, ResultCode expectedResult, boolean expectedLocked) {
            for (int i = 0; i < failures; i++) {
                service.login(USER, WRONG_PASS);
            }
            // Kiểm tra bằng mật khẩu SAI ở lần gọi này để xem mã trả về tương ứng (INVALID_CREDENTIALS hoặc ACCOUNT_LOCKED)
            ResultCode result = service.login(USER, WRONG_PASS);
            assertEquals(expectedResult, result);
            assertEquals(expectedLocked, service.isLocked(USER));
        }

        @Test
        void login_AfterAdminUnlock_CounterRestartsAndCanLogin() {
            for (int i = 0; i < 5; i++) {
                service.login(USER, WRONG_PASS);
            }
            assertEquals(ResultCode.SUCCESS, service.unlockAccount(USER));
            assertFalse(service.isLocked(USER));

            // Sai 1 lần sau khi mở khóa
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, WRONG_PASS));
            assertEquals(1, service.findByUsername(USER).get().getFailedAttempts());

            // Đăng nhập đúng thành công
            assertEquals(ResultCode.SUCCESS, service.login(USER, PASS));
        }
    }

    static Stream<Arguments> invalidRegisterInputs() {
        return Stream.of(
                Arguments.of("username sai định dạng", "1alice", EMAIL, PASS, PASS, DOB, PHONE, ResultCode.INVALID_USERNAME),
                Arguments.of("email sai định dạng", USER, "bad-email", PASS, PASS, DOB, PHONE, ResultCode.INVALID_EMAIL),
                Arguments.of("mật khẩu yếu", USER, EMAIL, "weak", "weak", DOB, PHONE, ResultCode.WEAK_PASSWORD),
                Arguments.of("xác nhận mật khẩu không khớp", USER, EMAIL, PASS, "Wrong@123", DOB, PHONE, ResultCode.PASSWORD_MISMATCH),
                Arguments.of("username sai + email sai", "1alice", "bad-email", PASS, PASS, DOB, PHONE, ResultCode.INVALID_USERNAME)
        );
    }
}