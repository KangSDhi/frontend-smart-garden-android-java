package com.example.mygarden.dto;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class GardenResponse {

    @SerializedName("data")
    private Data data;

    public Data getData(){
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {
        @SerializedName("nama_node")
        private String namaNode;

        @SerializedName("kelembapan")
        private Float kelembapan;

        @SerializedName("tanggal")
        private String tanggalNode;

        public String getNamaNode() {
            return namaNode;
        }

        public void setNamaNode(String namaNode) {
            this.namaNode = namaNode;
        }

        public Float getKelembapan() {
            return kelembapan;
        }

        public void setKelembapan(Float kelembapan) {
            this.kelembapan = kelembapan;
        }

        public String getTanggalNode() {
            return tanggalNode;
        }

        public void setTanggalNode(String tanggalNode) {
            this.tanggalNode = tanggalNode;
        }
    }

    public static GardenResponse parseJson(String jsonString){
        Gson gson = new Gson();
        return gson.fromJson(jsonString, GardenResponse.class);
    }
}
