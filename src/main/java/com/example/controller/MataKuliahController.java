package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.model.MataKuliah;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/matakuliah")
public class MataKuliahController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Tampilkan daftar matakuliah
    @GetMapping("")
    public String index(Model model) {
        try {
            // Map the database column names to the model property names
            String sql = "SELECT kode_mk as kodeMk, nama_mk as namaMk, sks, dosen FROM matakuliah ORDER BY kode_mk";
            List<MataKuliah> matakuliahList = jdbcTemplate.query(sql,
                    BeanPropertyRowMapper.newInstance(MataKuliah.class));

            model.addAttribute("matakuliahList", matakuliahList);
        } catch (Exception e) {
            System.out.println("Error Query: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            model.addAttribute("matakuliahList", new ArrayList<>());
        }

        return "matakuliah/index";
    }

    // Tampilkan form tambah matakuliah
    @GetMapping("/add")
    public String add() {
        return "matakuliah/add";
    }

    // Proses tambah matakuliah
    @PostMapping("/add")
    public String add(MataKuliah matakuliah) {
        try {
            String sql = "INSERT INTO matakuliah (kode_mk, nama_mk, sks, dosen) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    matakuliah.getKodeMk(),
                    matakuliah.getNamaMk(),
                    matakuliah.getSks(),
                    matakuliah.getDosen());
        } catch (Exception e) {
            System.out.println("Error Insert: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            return "redirect:/matakuliah?error=Failed to add course: " + e.getMessage();
        }

        return "redirect:/matakuliah";
    }

    // Tampilkan form edit matakuliah
    @GetMapping("/edit/{kodeMk}")
    public String edit(@PathVariable("kodeMk") String kodeMk, Model model) {
        try {
            String sql = "SELECT kode_mk as kodeMk, nama_mk as namaMk, sks, dosen FROM matakuliah WHERE kode_mk = ?";
            MataKuliah matakuliah = jdbcTemplate.queryForObject(sql,
                    BeanPropertyRowMapper.newInstance(MataKuliah.class), kodeMk);

            model.addAttribute("matakuliah", matakuliah);
        } catch (Exception e) {
            model.addAttribute("error", "Course with code " + kodeMk + " not found: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            return "error";
        }

        return "matakuliah/edit";
    }

    // Proses edit matakuliah
    @PostMapping("/edit")
    public String edit(MataKuliah matakuliah) {
        try {
            String sql = "UPDATE matakuliah SET nama_mk = ?, sks = ?, dosen = ? WHERE kode_mk = ?";
            jdbcTemplate.update(sql,
                    matakuliah.getNamaMk(),
                    matakuliah.getSks(),
                    matakuliah.getDosen(),
                    matakuliah.getKodeMk());
        } catch (Exception e) {
            System.out.println("Error Update: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            return "redirect:/matakuliah?error=Failed to update course: " + e.getMessage();
        }

        return "redirect:/matakuliah";
    }

    // Hapus matakuliah
    @GetMapping("/delete/{kodeMk}")
    public String delete(@PathVariable("kodeMk") String kodeMk) {
        try {
            // Check if mata kuliah exists
            String checkSql = "SELECT COUNT(*) FROM matakuliah WHERE kode_mk = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, kodeMk);

            if (count == null || count == 0) {
                return "redirect:/matakuliah?error=Course not found";
            }

            // Check if mata kuliah is used in IRS
            String checkIrsSql = "SELECT COUNT(*) FROM irs WHERE kode_mk = ?";
            Integer irsCount = jdbcTemplate.queryForObject(checkIrsSql, Integer.class, kodeMk);

            if (irsCount != null && irsCount > 0) {
                return "redirect:/matakuliah?error=Cannot delete course. It is being used in student IRS";
            }

            // Delete mata kuliah
            String sql = "DELETE FROM matakuliah WHERE kode_mk = ?";
            jdbcTemplate.update(sql, kodeMk);

        } catch (Exception e) {
            System.out.println("Error Delete: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            return "redirect:/matakuliah?error=Failed to delete course: " + e.getMessage();
        }

        return "redirect:/matakuliah";
    }
}