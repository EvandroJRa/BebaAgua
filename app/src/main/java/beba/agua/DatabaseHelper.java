package beba.agua;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bebaagua.db";
    private static final int DATABASE_VERSION = 4; // 🔥 Incrementado para aplicar a correção

    private static final String TABLE_HISTORICO = "historico";
    private static final String COLUMN_ID = "_id";  // 🔹 Agora o ID é "_id" para compatibilidade com CursorAdapter
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_QUANTIDADE = "quantidade";
    private static final String COLUMN_META_DIARIA = "metaDiaria";

    private static final String CREATE_TABLE_HISTORICO = "CREATE TABLE IF NOT EXISTS " + TABLE_HISTORICO + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +  // 🔹 Corrigido para "_id"
            COLUMN_DATA + " TEXT NOT NULL UNIQUE, " + // 🔹 UNIQUE evita múltiplos registros para o mesmo dia
            COLUMN_QUANTIDADE + " REAL NOT NULL, " +
            COLUMN_META_DIARIA + " REAL NOT NULL)";

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
            if (oldVersion < 4) {  // 🔹 Apenas recria a tabela se for necessário
                db.execSQL("ALTER TABLE " + TABLE_HISTORICO + " RENAME TO historico_old");
                db.execSQL(CREATE_TABLE_HISTORICO);
                db.execSQL("INSERT INTO " + TABLE_HISTORICO + " (_id, data, quantidade, metaDiaria) " +
                        "SELECT id, data, quantidade, metaDiaria FROM historico_old");
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
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HISTORICO + " WHERE " + COLUMN_DATA + " = ?", new String[]{data});
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    // 🔹 **Registra ou Atualiza o Consumo Diário**
    public void registrarConsumo(String data, double quantidade, double metaDiaria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (verificarRegistroExistente(data)) {
            values.put(COLUMN_QUANTIDADE, quantidade);
            db.update(TABLE_HISTORICO, values, COLUMN_DATA + " = ?", new String[]{data});
            Log.d("DatabaseHelper", "🔄 Consumo atualizado: " + quantidade + "ml para " + data);
        } else {
            values.put(COLUMN_DATA, data);
            values.put(COLUMN_QUANTIDADE, quantidade);
            values.put(COLUMN_META_DIARIA, metaDiaria);
            db.insert(TABLE_HISTORICO, null, values);
            Log.d("DatabaseHelper", "📌 Novo registro criado: " + data + " | Meta: " + metaDiaria + "ml");
        }
    }

    // 🔹 **Obtém o histórico de consumo ordenado por data (mais recente primeiro)**
    public Cursor obterHistorico() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT _id, data, quantidade, metaDiaria FROM historico ORDER BY data DESC", null);
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
        return meta;
    }
}
