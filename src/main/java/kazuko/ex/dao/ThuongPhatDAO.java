package kazuko.ex.dao;

import kazuko.ex.model.nhansu.ThuongPhat;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ThuongPhatDAO {
    private static final List<ThuongPhat> MEMORY_LIST = new CopyOnWriteArrayList<>();

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!MEMORY_LIST.isEmpty()) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            ThuongPhat tp1 = new ThuongPhat();
            tp1.setId("tp-001");
            tp1.setSoPhieu("TP26/00001");
            tp1.setNgay(sdf.parse("31/08/2026 09:00"));
            tp1.setTimeCreated(sdf.parse("31/08/2026 09:00"));
            tp1.setUserCreatedId("Administrator");
            tp1.setNhanVienId("nv-1");
            tp1.setTenNhanVien("Hoàng Giang");
            tp1.setTienThuong(10000.0);
            tp1.setTienPhat(100.0);
            tp1.setLyDoId("ldtp-1");
            tp1.setTenLyDo("Doanh số");
            tp1.setGhiChu("note");
            tp1.setStatus(1);
            MEMORY_LIST.add(tp1);
        } catch (Exception ignored) {}
    }

    public List<ThuongPhat> getAll() {
        return MEMORY_LIST.stream()
                .filter(tp -> tp.getStatus() == 1)
                .sorted(Comparator.comparing(ThuongPhat::getNgay, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<ThuongPhat> getTrashItems() {
        return MEMORY_LIST.stream()
                .filter(tp -> tp.getStatus() == -1)
                .sorted(Comparator.comparing(ThuongPhat::getNgay, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public ThuongPhat findById(String id) {
        if (id == null) return null;
        return MEMORY_LIST.stream()
                .filter(tp -> id.equals(tp.getId()))
                .findFirst()
                .orElse(null);
    }

    public synchronized String generateNextSoPhieu(Date date) {
        Calendar cal = Calendar.getInstance();
        if (date != null) cal.setTime(date);
        int year2Digits = cal.get(Calendar.YEAR) % 100;
        String prefix = String.format("TP%02d/", year2Digits);

        int maxSeq = 0;
        for (ThuongPhat tp : MEMORY_LIST) {
            if (tp.getSoPhieu() != null && tp.getSoPhieu().startsWith(prefix)) {
                try {
                    String seqStr = tp.getSoPhieu().substring(prefix.length());
                    int seq = Integer.parseInt(seqStr);
                    if (seq > maxSeq) maxSeq = seq;
                } catch (Exception ignored) {}
            }
        }
        return String.format("%s%05d", prefix, maxSeq + 1);
    }

    public boolean insert(ThuongPhat tp) {
        if (tp == null) return false;
        if (tp.getId() == null || tp.getId().trim().isEmpty()) {
            tp.setId("tp-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (tp.getSoPhieu() == null || tp.getSoPhieu().trim().isEmpty()) {
            tp.setSoPhieu(generateNextSoPhieu(tp.getNgay()));
        }
        if (tp.getTimeCreated() == null) {
            tp.setTimeCreated(new Date());
        }
        tp.setStatus(1);
        MEMORY_LIST.add(0, tp);
        return true;
    }

    public boolean update(ThuongPhat tp) {
        if (tp == null || tp.getId() == null) return false;
        for (int i = 0; i < MEMORY_LIST.size(); i++) {
            if (tp.getId().equals(MEMORY_LIST.get(i).getId())) {
                MEMORY_LIST.set(i, tp);
                return true;
            }
        }
        return false;
    }

    public boolean delete(String id) {
        return moveToTrash(id);
    }

    public boolean moveToTrash(String id) {
        ThuongPhat tp = findById(id);
        if (tp != null) {
            tp.setStatus(-1);
            return true;
        }
        return false;
    }

    public boolean restore(String id) {
        ThuongPhat tp = findById(id);
        if (tp != null) {
            tp.setStatus(1);
            return true;
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        return MEMORY_LIST.removeIf(tp -> id != null && id.equals(tp.getId()));
    }
}
