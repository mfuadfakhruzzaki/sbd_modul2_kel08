package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.model.IRS;
import com.example.model.Mahasiswa;
import com.example.model.MataKuliah;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/irs")
public class IRSController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Tampilkan daftar IRS
    @GetMapping("")
    public String index(Model model) {
        List<IRS> irsList = new ArrayList<>();

        try {
            // Use a simpler query with explicit aliases
            String sql = "SELECT i.id, i.nim, m.nama AS nama, " +
                    "i.kode_mk AS kodeMk, mk.nama_mk AS namaMk, " +
                    "mk.sks AS sks, mk.dosen AS dosen, " +
                    "i.semester, i.nilai " +
                    "FROM irs i " +
                    "JOIN mahasiswa m ON i.nim = m.nim " +
                    "JOIN matakuliah mk ON i.kode_mk = mk.kode_mk " +
                    "ORDER BY i.nim, i.semester";

            irsList = jdbcTemplate.query(sql, (rs, rowNum) -> {
                IRS irs = new IRS();
                irs.setId(rs.getLong("id"));
                irs.setNim(rs.getString("nim"));
                irs.setNama(rs.getString("nama"));
                irs.setKodeMk(rs.getString("kodeMk"));
                irs.setNamaMk(rs.getString("namaMk"));
                irs.setSks(rs.getInt("sks"));
                irs.setDosen(rs.getString("dosen"));
                irs.setSemester(rs.getString("semester"));
                irs.setNilai(rs.getString("nilai"));
                return irs;
            });

        } catch (DataAccessException e) {
            System.err.println("Error retrieving IRS data: " + e.getMessage());
            e.printStackTrace();
        }

        model.addAttribute("irsList", irsList);
        return "irs/index";
    }

    // Tampilkan form tambah IRS
    @GetMapping("/add")
    public String add(Model model) {
        try {
            // Get list of students
            String sqlMahasiswa = "SELECT * FROM mahasiswa ORDER BY nim";
            List<Mahasiswa> mahasiswaList = jdbcTemplate.query(sqlMahasiswa,
                    BeanPropertyRowMapper.newInstance(Mahasiswa.class));

            // Get list of courses with explicitly mapped properties
            String sqlMatakuliah = "SELECT kode_mk AS kodeMk, nama_mk AS namaMk, sks, dosen FROM matakuliah ORDER BY kode_mk";
            List<MataKuliah> matakuliahList = jdbcTemplate.query(sqlMatakuliah,
                    BeanPropertyRowMapper.newInstance(MataKuliah.class));

            model.addAttribute("mahasiswaList", mahasiswaList);
            model.addAttribute("matakuliahList", matakuliahList);
        } catch (Exception e) {
            System.err.println("Error loading form data: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load form data: " + e.getMessage());
            return "error";
        }

        return "irs/add";
    }

    // Proses tambah IRS
    @PostMapping("/add")
    public String add(IRS irs) {
        try {
            // Check if the entry already exists
            String checkSql = "SELECT COUNT(*) FROM irs WHERE nim = ? AND kode_mk = ? AND semester = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class,
                    irs.getNim(), irs.getKodeMk(), irs.getSemester());

            if (count != null && count > 0) {
                return "redirect:/irs?error=Entry already exists for this student, course, and semester";
            }

            // Insert new IRS entry
            String sql = "INSERT INTO irs (id, nim, kode_mk, semester, nilai) VALUES (irs_seq.NEXTVAL, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, irs.getNim(), irs.getKodeMk(), irs.getSemester(), irs.getNilai());
        } catch (Exception e) {
            System.err.println("Error inserting IRS data: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/irs?error=Failed to add IRS entry: " + e.getMessage();
        }

        return "redirect:/irs";
    }

    // Tampilkan form edit IRS
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        try {
            // Get IRS entry
            String sqlIrs = "SELECT id, nim, kode_mk AS kodeMk, semester, nilai FROM irs WHERE id = ?";
            IRS irs = jdbcTemplate.queryForObject(sqlIrs, (rs, rowNum) -> {
                IRS item = new IRS();
                item.setId(rs.getLong("id"));
                item.setNim(rs.getString("nim"));
                item.setKodeMk(rs.getString("kodeMk"));
                item.setSemester(rs.getString("semester"));
                item.setNilai(rs.getString("nilai"));
                return item;
            }, id);

            // Get list of students
            String sqlMahasiswa = "SELECT * FROM mahasiswa ORDER BY nim";
            List<Mahasiswa> mahasiswaList = jdbcTemplate.query(sqlMahasiswa,
                    BeanPropertyRowMapper.newInstance(Mahasiswa.class));

            // Get list of courses with explicitly mapped properties
            String sqlMatakuliah = "SELECT kode_mk AS kodeMk, nama_mk AS namaMk, sks, dosen FROM matakuliah ORDER BY kode_mk";
            List<MataKuliah> matakuliahList = jdbcTemplate.query(sqlMatakuliah,
                    BeanPropertyRowMapper.newInstance(MataKuliah.class));

            model.addAttribute("irs", irs);
            model.addAttribute("mahasiswaList", mahasiswaList);
            model.addAttribute("matakuliahList", matakuliahList);
        } catch (Exception e) {
            System.err.println("Error retrieving IRS data for edit: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "IRS entry with ID " + id + " not found: " + e.getMessage());
            return "error";
        }

        return "irs/edit";
    }

    // Proses edit IRS
    @PostMapping("/edit")
    public String edit(IRS irs) {
        try {
            String sql = "UPDATE irs SET nim = ?, kode_mk = ?, semester = ?, nilai = ? WHERE id = ?";
            jdbcTemplate.update(sql, irs.getNim(), irs.getKodeMk(), irs.getSemester(), irs.getNilai(), irs.getId());
        } catch (Exception e) {
            System.err.println("Error updating IRS data: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/irs?error=Failed to update IRS entry: " + e.getMessage();
        }

        return "redirect:/irs";
    }

    // Hapus IRS
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        try {
            String sql = "DELETE FROM irs WHERE id = ?";
            jdbcTemplate.update(sql, id);
        } catch (Exception e) {
            System.err.println("Error deleting IRS data: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/irs?error=Failed to delete IRS entry: " + e.getMessage();
        }

        return "redirect:/irs";
    }
}