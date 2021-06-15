package br.net.johansson.filipe.mypetlocator;

public class Pet {

    private String pid;
    private String nome;
    private double lat, lng;

    public Pet() {

    }

    public Pet(String pid, String nome, double lat, double lng) {
        this.pid = pid;
        this.nome = nome;
        this.lat = lat;
        this.lng = lng;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
