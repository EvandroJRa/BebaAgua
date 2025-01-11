package beba.agua;

public class HistoricoModel {
    private String data;
    private double quantidade;
    private double metaDiaria;

    public HistoricoModel(String data, double quantidade, double metaDiaria) {
        this.data = data;
        this.quantidade = quantidade;
        this.metaDiaria = metaDiaria;
    }

    public String getData() {
        return data;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public double getMetaDiaria() {
        return metaDiaria;
    }
}

