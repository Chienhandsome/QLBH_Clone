package kazuko.ex.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LicenseServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void newInstallationStartsWithTrialAndStableMachineId() {
        LicenseService service = new LicenseService(tempDir.resolve("license.properties"));

        LicenseService.LicenseStatus first = service.getStatus();
        LicenseService.LicenseStatus second = service.getStatus();

        assertFalse(first.registered());
        assertEquals(LicenseService.TRIAL_DAYS, first.remainingTrialDays());
        assertEquals(first.machineId(), second.machineId());
        assertTrue(first.machineId().matches("\\d{5}(-\\d{5}){4}"));
    }

    @Test
    void rejectsEmptyAndIncorrectRegistrationDetails() {
        LicenseService service = new LicenseService(tempDir.resolve("license.properties"));

        assertFalse(service.register("", "Công ty", "key").success());
        assertFalse(service.register("Nguyễn Văn A", "", "key").success());
        assertFalse(service.register("Nguyễn Văn A", "Công ty", "key-khong-dung").success());
        assertFalse(service.getStatus().registered());
    }

    @Test
    void validRegistrationIsPersistedAndReloaded() {
        Path file = tempDir.resolve("license.properties");
        LicenseService service = new LicenseService(file);
        String machineId = service.getMachineId();
        String key = LicenseService.expectedKeyForMachine(machineId);

        LicenseService.RegistrationResult result = service.register("Nguyễn Văn A", "Công ty ABC", key);
        LicenseService.LicenseStatus reloaded = new LicenseService(file).getStatus();

        assertTrue(result.success());
        assertTrue(reloaded.registered());
        assertEquals("Nguyễn Văn A", reloaded.owner());
        assertEquals("Công ty ABC", reloaded.organization());
        assertEquals(key, reloaded.registrationKey());
    }
}
