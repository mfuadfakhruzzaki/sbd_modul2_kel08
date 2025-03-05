package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.model.Mahasiswa;
import com.example.model.IRS;
import com.example.model.MataKuliah;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Controller
public class MahasiswaController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Existing methods...
    // Keep all the existing methods from the original MahasiswaController

    // Halaman utama dengan pencarian mahasiswa
    @SuppressWarnings("deprecation")
    @GetMapping("/")
    public String index(Model model, @RequestParam(required = false) String search) {
        String sql = "SELECT * FROM mahasiswa";

        // Jika parameter pencarian ada, filter berdasarkan NIM atau Nama
        if (search != null && !search.isEmpty()) {
            sql += " WHERE LOWER(nim) LIKE LOWER(?) OR LOWER(nama) LIKE LOWER(?)";
        }

        List<Mahasiswa> mahasiswa;
        try {
            // Jika search ada, lakukan pencarian berdasarkan NIM atau Nama
            if (search != null && !search.isEmpty()) {
                mahasiswa = jdbcTemplate.query(sql,
                        new Object[] { "%" + search.toLowerCase() + "%", "%" + search.toLowerCase() + "%" },
                        BeanPropertyRowMapper.newInstance(Mahasiswa.class));
            } else {
                mahasiswa = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Mahasiswa.class));
            }
        } catch (Exception e) {
            // Tangani error dengan memberikan list kosong
            System.out.println("Error Query: " + e.getMessage());
            mahasiswa = new ArrayList<>();
        }

        // Mengirimkan data mahasiswa dan nilai pencarian ke halaman
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("search", search); // Menambahkan parameter pencarian untuk tetap ditampilkan
        return "index"; // Mengembalikan halaman dengan daftar mahasiswa
    }

    // Menampilkan form tambah mahasiswa
    @GetMapping("/add")
    public String add(Model model) {
        return "add";
    }

    // Proses tambah mahasiswa
    @PostMapping("/add")
    public String add(Mahasiswa mahasiswa) {
        String sql = "INSERT INTO mahasiswa (nim, nama, angkatan, gender) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, mahasiswa.getNim(), mahasiswa.getNama(), mahasiswa.getAngkatan(),
                mahasiswa.getGender());
        return "redirect:/"; // Redirect ke halaman utama setelah penambahan data
    }

    // Menampilkan form untuk edit data mahasiswa
    @GetMapping("/edit/{nim}")
    public String edit(@PathVariable("nim") String nim, Model model) {
        String sql = "SELECT * FROM mahasiswa WHERE nim = ?";
        Mahasiswa mahasiswa = null;

        try {
            // Coba ambil mahasiswa berdasarkan NIM
            mahasiswa = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(Mahasiswa.class), nim);
        } catch (Exception e) {
            // Jika mahasiswa tidak ditemukan, tampilkan error
            model.addAttribute("error", "Mahasiswa dengan NIM " + nim + " tidak ditemukan.");
            return "error";
        }

        model.addAttribute("mahasiswa", mahasiswa);
        return "edit"; // Mengarahkan ke halaman edit
    }

    // Proses untuk mengupdate data mahasiswa
    @PostMapping("/edit")
    public String edit(Mahasiswa mahasiswa) {
        String sql = "UPDATE mahasiswa SET nama = ?, angkatan = ?, gender = ? WHERE nim = ?";
        jdbcTemplate.update(sql, mahasiswa.getNama(), mahasiswa.getAngkatan(), mahasiswa.getGender(),
                mahasiswa.getNim());
        return "redirect:/"; // Redirect ke halaman utama setelah update data
    }

    // Menghapus data mahasiswa
    @GetMapping("/delete/{nim}")
    public String delete(@PathVariable("nim") String nim) {
        String sqlCheck = "SELECT COUNT(*) FROM mahasiswa WHERE nim = ?";
        @SuppressWarnings("null")
        int count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, nim);

        // Cek apakah mahasiswa dengan NIM tersebut ada
        if (count == 0) {
            return "redirect:/?error=NIM tidak ditemukan";
        }

        // Hapus mahasiswa berdasarkan NIM
        String sql = "DELETE FROM mahasiswa WHERE nim = ?";
        jdbcTemplate.update(sql, nim);
        return "redirect:/"; // Redirect ke halaman utama setelah penghapusan
    }

    // New methods for detail functionality

    // This is the method in MahasiswaController that needs to be updated
    // Replace the detail method in your MahasiswaController with this updated
    // version

    @GetMapping("/detail/{nim}")
    public String detail(@PathVariable("nim") String nim, Model model) {
        try {
            // Get mahasiswa data
            String sqlMahasiswa = "SELECT * FROM mahasiswa WHERE nim = ?";
            Mahasiswa mahasiswa = jdbcTemplate.queryForObject(sqlMahasiswa,
                    BeanPropertyRowMapper.newInstance(Mahasiswa.class), nim);

            // Get IRS data with course information using JOIN
            // UPDATED QUERY: Removed the i.nilai column
            String sqlIrs = "SELECT i.id, i.nim, i.kode_mk as kodeMk, i.semester, " +
                    "m.nama_mk as namaMk, m.sks, m.dosen " +
                    "FROM irs i " +
                    "JOIN matakuliah m ON i.kode_mk = m.kode_mk " +
                    "WHERE i.nim = ? " +
                    "ORDER BY i.semester";

            List<IRS> irsList = jdbcTemplate.query(sqlIrs, (rs, rowNum) -> {
                IRS irs = new IRS();
                irs.setId(rs.getLong("id"));
                irs.setNim(rs.getString("nim"));
                irs.setKodeMk(rs.getString("kodeMk"));
                irs.setSemester(rs.getString("semester"));
                irs.setNamaMk(rs.getString("namaMk"));
                irs.setSks(rs.getInt("sks"));
                irs.setDosen(rs.getString("dosen"));
                return irs;
            }, nim);

            // Set data to model
            model.addAttribute("mahasiswa", mahasiswa);
            model.addAttribute("irsList", irsList);

            return "detail";
        } catch (Exception e) {
            // If student not found or any error occurs
            model.addAttribute("error", "Error retrieving data for student with NIM " + nim + ": " + e.getMessage());
            e.printStackTrace(); // Log the stack trace for debugging
            return "error";
        }
    }
}