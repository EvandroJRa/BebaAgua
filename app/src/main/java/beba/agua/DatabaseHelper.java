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
    private static final int DATABASE_VERSION = 5;

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
            Log.e("DatabaseHelper", "❌ Erro ao criar tabela: " + e.getMessage());
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
                Log.d("DatabaseHelper", "🔄 Banco atualizado para a versão " + newVersion);
            }
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "❌ Erro ao atualizar banco: " + e.getMessage());
        }
    }

    // Verifica se um registro para a data já existe
    public boolean verificarRegistroExistente(String data) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_HISTORICO + " WHERE " + COLUMN_DATA + " = ?", new String[]{data});
        boolean existe = false;
        if (cursor.moveToFirst()) {
            existe = cursor.getInt(0) > 0;
        }
        cursor.close();
        return existe;
    }

    // Registra ou Atualiza o Consumo Diário (agora somando corretamente)
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

            double novoTotal = consumoAtual + quantidade;
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

    // Obtém o histórico de consumo ordenado por data
    public Cursor obterHistorico() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT _id, data, IFNULL(quantidade, 0) AS quantidade, IFNULL(metaDiaria, 2000) AS metaDiaria FROM " + TABLE_HISTORICO + " ORDER BY data DESC, _id DESC", null);
    }

    // Obtém a soma total do consumo diário
    public double obterConsumoDiario(String data) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_QUANTIDADE + ") FROM " + TABLE_HISTORICO + " WHERE " + COLUMN_DATA + " = ?", new String[]{data});
        double consumo = 0;
        if (cursor.moveToFirst()) {
            consumo = cursor.getDouble(0);
        }
        cursor.close();
        return consumo;
    }

    // Obtém a meta diária de um dia específico
    public double obterMetaDiaria(String data) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_META_DIARIA + " FROM " + TABLE_HISTORICO + " WHERE " + COLUMN_DATA + " = ?", new String[]{data});
        double meta = 2000.0;
        if (cursor.moveToFirst()) {
            meta = cursor.getDouble(0);
        }
        cursor.close();
        return meta;
    }

    // Deleta um registro específico do histórico
    public void deletarRegistro(String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORICO, COLUMN_DATA + " = ?", new String[]{data});
    }

    // Deleta todo o histórico
    public void limparHistorico() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_HISTORICO);
        Log.d("DatabaseHelper", "🗑️ Histórico apagado.");
    }

    // Verifica se o banco de dados está vazio
    public boolean isBancoVazio() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_HISTORICO, null);
        boolean isEmpty = cursor.moveToFirst() && cursor.getInt(0) == 0;
        cursor.close();
        return isEmpty;
    }

    // Obtém histórico paginado (para Scroll Infinito)
    public List<HistoricoModel> obterHistoricoPaginado(int offset, int limite) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<HistoricoModel> historicoList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT data, quantidade, metaDiaria FROM historico ORDER BY data DESC LIMIT ? OFFSET ?", new String[]{String.valueOf(limite), String.valueOf(offset)});
        while (cursor.moveToNext()) {
            historicoList.add(new HistoricoModel(cursor.getString(0), cursor.getDouble(1), cursor.getDouble(2)));
        }
        cursor.close();
        return historicoList;
    }
    // -------------------------------------------------------------------------------//
//    // dados ficticio, para teste
//    public void inserirDadosFicticios() {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        for (int i = 1; i <= 100; i++) { // 🔥 Insere 100 registros de teste
//            String data = "2025-01-" + (i < 10 ? "0" + i : i); // Formato: 2025-01-01, 2025-01-02...
//            double quantidade = 100 + (i * 15); // Simula um consumo variável (ex: 100ml, 115ml...)
//            double metaDiaria = 2000.0; // Mantém a meta fixa
//
//            ContentValues values = new ContentValues();
//            values.put("data", data);
//            values.put("quantidade", quantidade);
//            values.put("metaDiaria", metaDiaria);
//
//            db.insertWithOnConflict("historico", null, values, SQLiteDatabase.CONFLICT_IGNORE);
//        }
//        Log.d("DatabaseHelper", "📊 100 registros de teste foram adicionados ao histórico.");
//    }


}
