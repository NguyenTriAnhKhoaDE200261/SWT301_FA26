package fu.de200261;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import fu.de200261.*;

class AccountServiceTest {

    AccountService service;

    static final String USER = "alice_01";
    static final String EMAIL = "alice@example.com";
    static final String PASS = "Secret@123";
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
