package com.example.model;

public class Mahasiswa {
    @Size(min = 10, max = 20, message = "NIM harus antara 10 hingga 20 karakter")
    private String nim;

    private String nama;
    private String angkatan;
    private String gender;

    // Getter dan Setter
    public String getNim() {
        return nim;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getAngkatan() {
        return angkatan;
    }

    public void setAngkatan(String angkatan) {
        this.angkatan = angkatan;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
