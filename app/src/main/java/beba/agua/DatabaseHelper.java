package beba.agua;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bebaagua.db";
    private static final int DATABASE_VERSION = 4; // 🔥 Mantém versão para evitar recriação desnecessária

    private static final String TABLE_HISTORICO = "historico";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_QUANTIDADE = "quantidade";
    private static final String COLUMN_META_DIARIA = "metaDiaria";

    private static final String CREATE_TABLE_HISTORICO = "CREATE TABLE IF NOT EXISTS " + TABLE_HISTORICO + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DATA + " TEXT NOT NULL UNIQUE, " +
            COLUMN_QUANTIDADE + " REAL NOT NULL DEFAULT 0, " +
            COLUMN_META_DIARIA + " REAL NOT NULL DEFAULT 2000)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_HISTORICO);
            Log.d("DatabaseHelper", "✅ Tabela 'historico' criada com sucesso.");
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "❌ Erro ao criar tabela 'historico': " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 4) {
                db.execSQL("ALTER TABLE " + TABLE_HISTORICO + " RENAME TO historico_old");
                db.execSQL(CREATE_TABLE_HISTORICO);
                db.execSQL("INSERT INTO " + TABLE_HISTORICO + " (_id, data, quantidade, metaDiaria) " +
                        "SELECT _id, data, IFNULL(quantidade, 0), IFNULL(metaDiaria, 2000) FROM historico_old");
                db.execSQL("DROP TABLE historico_old");
                Log.d("DatabaseHelper", "🔄 Banco de dados atualizado para a versão " + newVersion);
            }
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "❌ Erro ao atualizar banco: " + e.getMessage());
        }
    }

    // 🔹 **Verifica se já existe um registro para o dia**
    public boolean verificarRegistroExistente(String data) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_QUANTIDADE + " FROM " + TABLE_HISTORICO + " WHERE " + COLUMN_DATA + " = ?", new String[]{data});
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    // 🔹 **Registra ou Atualiza o Consumo Diário - Agora SOMA ao invés de SOBRESCREVER**
    public void registrarConsumo(String data, double quantidade, double metaDiaria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (verificarRegistroExistente(data)) {
            Cursor cursor = db.rawQuery("SELECT " + COLUMN_QUANTIDADE + " FROM " + TABLE_HISTORICO + " WHERE " + COLUMN_DATA + " = ?", new String[]{data});
            double consumoAtual = 0;
            if (cursor.moveToFirst()) {
                consumoAtual = cursor.getDouble(0);
            }
            cursor.close();

            double novoTotal = consumoAtual + quantidade; // 🔥 SOMA ao invés de substituir
            values.put(COLUMN_QUANTIDADE, novoTotal);

            db.update(TABLE_HISTORICO, values, COLUMN_DATA + " = ?", new String[]{data});
            Log.d("DatabaseHelper", "🔄 Consumo atualizado: " + novoTotal + "ml para " + data);
        } else {
            values.put(COLUMN_DATA, data);
            values.put(COLUMN_QUANTIDADE, quantidade);
            values.put(COLUMN_META_DIARIA, metaDiaria);
            db.insert(TABLE_HISTORICO, null, values);
            Log.d("DatabaseHelper", "📌 Novo registro criado: " + data + " | Meta: " + metaDiaria + "ml");
        }
    }

    // 🔹 **Obtém o histórico de consumo ordenado por data**
    public Cursor obterHistorico() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT _id, data, IFNULL(quantidade, 0) AS quantidade, IFNULL(metaDiaria, 2000) AS metaDiaria FROM " + TABLE_HISTORICO + " ORDER BY data DESC", null);
    }

    // 🔹 **Obtém a soma total do consumo diário**
    public double obterConsumoDiario(String data) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_QUANTIDADE + ") FROM " + TABLE_HISTORICO + " WHERE " + COLUMN_DATA + " = ?", new String[]{data});

        double consumo = 0;
        if (cursor.moveToFirst()) {
            consumo = cursor.getDouble(0);
        }
        cursor.close();
        Log.d("DatabaseHelper", "📊 Consumo total do dia " + data + ": " + consumo + "ml");
        return consumo;
    }

    // 🔹 **Obtém a meta diária de um dia específico**
    public double obterMetaDiaria(String data) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_META_DIARIA + " FROM " + TABLE_HISTORICO + " WHERE " + COLUMN_DATA + " = ?", new String[]{data});

        double meta = 2000.0; // Valor padrão de 2000ml caso não encontre
        if (cursor.moveToFirst()) {
            meta = cursor.getDouble(0);
        }
        cursor.close();
        Log.d("DatabaseHelper", "📊 Meta diária de " + data + ": " + meta + "ml");
        return meta;
    }

    // 🔹 **Deleta um registro específico do histórico**
    public void deletarRegistro(String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_HISTORICO, COLUMN_DATA + " = ?", new String[]{data});
        if (result > 0) {
            Log.d("DatabaseHelper", "🗑️ Registro deletado com sucesso para o dia " + data);
        } else {
            Log.d("DatabaseHelper", "⚠️ Nenhum registro encontrado para deletar em " + data);
        }
    }

    // 🔹 **Deleta todo o histórico**
    public void limparHistorico() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_HISTORICO);
        Log.d("DatabaseHelper", "🗑️ Histórico de consumo apagado com sucesso.");
    }

    // 🔹 **Verifica se o banco de dados está vazio**
    public boolean isBancoVazio() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_HISTORICO, null);
        boolean isEmpty = true;
        if (cursor.moveToFirst()) {
            isEmpty = cursor.getInt(0) == 0;
        }
        cursor.close();
        return isEmpty;
    }

    public List<HistoricoModel> obterHistoricoPaginado(int offset, int limite) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<HistoricoModel> historicoList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT data, quantidade, metaDiaria FROM historico ORDER BY data DESC LIMIT ? OFFSET ?",
                new String[]{String.valueOf(limite), String.valueOf(offset)});

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);
                double quantidade = cursor.getDouble(1);
                double metaDiaria = cursor.getDouble(2);
                historicoList.add(new HistoricoModel(data, quantidade, metaDiaria));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return historicoList;
    }
    // -------------------------------------------------------------------------------//
    // dados ficticio, para teste
    public void inserirDadosFicticios() {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 1; i <= 50; i++) { // 🔥 Insere 50 registros de teste
            String data = "2025-01-" + (i < 10 ? "0" + i : i); // Formato: 2025-01-01, 2025-01-02...
            double quantidade = 150 + (i * 10); // Simula um consumo variável
            double metaDiaria = 2000.0; // Mantém a meta fixa

            ContentValues values = new ContentValues();
            values.put("data", data);
            values.put("quantidade", quantidade);
            values.put("metaDiaria", metaDiaria);

            db.insertWithOnConflict("historico", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        Log.d("DatabaseHelper", "📊 50 registros de teste foram adicionados ao histórico.");
    }

}
