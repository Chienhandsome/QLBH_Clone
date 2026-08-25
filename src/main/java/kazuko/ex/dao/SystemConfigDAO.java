package kazuko.ex.dao;

import kazuko.ex.model.system.SystemConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SystemConfigDAO {
    private static final String CONFIG_FILE = "system_config.properties";
    private static SystemConfig cachedConfig = null;

    public synchronized SystemConfig loadConfig() {
        if (cachedConfig != null) {
            return cachedConfig;
        }

        SystemConfig config = new SystemConfig();
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            Properties props = new Properties();
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                props.load(reader);

                config.setTenCongTy(props.getProperty("company.name", config.getTenCongTy()));
                config.setDiaChi(props.getProperty("company.address", config.getDiaChi()));
                config.setLogoPath(props.getProperty("company.logo", config.getLogoPath()));
                config.setSoDienThoai(props.getProperty("company.phone", config.getSoDienThoai()));
                config.setEmail(props.getProperty("company.email", config.getEmail()));
                config.setLoiCamOn(props.getProperty("company.thanks", config.getLoiCamOn()));
                config.setFax(props.getProperty("company.fax", config.getFax()));
                config.setMauHoaDon(props.getProperty("print.invoiceTemplate", config.getMauHoaDon()));
                config.setLuaChonMauKhiIn(Boolean.parseBoolean(props.getProperty("print.selectTemplate", String.valueOf(config.isLuaChonMauKhiIn()))));
                config.setHienThiTruocKhiIn(Boolean.parseBoolean(props.getProperty("print.previewBeforePrint", String.valueOf(config.isHienThiTruocKhiIn()))));

                // Load Printers
                for (String key : config.getPrinterSettings().keySet()) {
                    String val = props.getProperty("printer." + key);
                    if (val != null) {
                        config.getPrinterSettings().put(key, val);
                    }
                }

                // Load Voucher Rules
                for (String key : config.getVoucherRules().keySet()) {
                    String val = props.getProperty("voucher." + key);
                    if (val != null) {
                        config.getVoucherRules().put(key, val);
                    }
                }
            } catch (Exception ignored) {}
        }

        cachedConfig = config;
        return cachedConfig;
    }

    public synchronized boolean saveConfig(SystemConfig config) {
        if (config == null) return false;
        cachedConfig = config;

        Properties props = new Properties();
        props.setProperty("company.name", config.getTenCongTy() != null ? config.getTenCongTy() : "");
        props.setProperty("company.address", config.getDiaChi() != null ? config.getDiaChi() : "");
        props.setProperty("company.logo", config.getLogoPath() != null ? config.getLogoPath() : "");
        props.setProperty("company.phone", config.getSoDienThoai() != null ? config.getSoDienThoai() : "");
        props.setProperty("company.email", config.getEmail() != null ? config.getEmail() : "");
        props.setProperty("company.thanks", config.getLoiCamOn() != null ? config.getLoiCamOn() : "");
        props.setProperty("company.fax", config.getFax() != null ? config.getFax() : "");
        props.setProperty("print.invoiceTemplate", config.getMauHoaDon() != null ? config.getMauHoaDon() : "");
        props.setProperty("print.selectTemplate", String.valueOf(config.isLuaChonMauKhiIn()));
        props.setProperty("print.previewBeforePrint", String.valueOf(config.isHienThiTruocKhiIn()));

        // Save Printers
        for (var entry : config.getPrinterSettings().entrySet()) {
            props.setProperty("printer." + entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
        }

        // Save Voucher Rules
        for (var entry : config.getVoucherRules().entrySet()) {
            props.setProperty("voucher." + entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            props.store(writer, "Cấu hình hệ thống QLBH");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
