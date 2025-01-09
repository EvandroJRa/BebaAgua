package beba.agua;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bebaagua.db";
    private static final int DATABASE_VERSION = 3; // 🔥 Incrementado para forçar atualização

    // 🗂️ Nome da tabela e colunas
    private static final String TABLE_HISTORICO = "historico";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_QUANTIDADE = "quantidade";
    private static final String COLUMN_META_DIARIA = "metaDiaria";

    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_HISTORICO = "CREATE TABLE IF NOT EXISTS " + TABLE_HISTORICO + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATA + " TEXT NOT NULL, " +
                COLUMN_QUANTIDADE + " REAL NOT NULL, " +
                COLUMN_META_DIARIA + " REAL NOT NULL)";
        db.execSQL(CREATE_TABLE_HISTORICO);
        Log.d(TAG, "✅ Tabela '" + TABLE_HISTORICO + "' criada com sucesso.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "⚠️ Atualizando banco de dados da versão " + oldVersion + " para " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORICO);
        onCreate(db);
    }

    // 🔍 **Verifica se já existe um registro para o dia**
    public boolean verificarRegistroExistente(String data) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_HISTORICO + " WHERE " + COLUMN_DATA + " = ?",
                new String[]{data}
        );
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        Log.d(TAG, "🔍 Registro para " + data + ": " + (existe ? "EXISTE" : "NÃO ENCONTRADO"));
        return existe;
    }

    // 💾 **Insere ou Atualiza o Consumo Diário**
    public void registrarConsumo(String data, double quantidade, double metaDiaria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (verificarRegistroExistente(data)) {
            // 🔄 Atualizar quantidade somando ao valor existente
            Cursor cursor = db.rawQuery(
                    "SELECT " + COLUMN_QUANTIDADE + " FROM " + TABLE_HISTORICO + " WHERE " + COLUMN_DATA + " = ?",
                    new String[]{data}
            );

            if (cursor.moveToFirst()) {
                double quantidadeAtual = cursor.getDouble(0);
                quantidade += quantidadeAtual; // Soma ao consumo existente
            }
            cursor.close();

            values.put(COLUMN_QUANTIDADE, quantidade);
            db.update(TABLE_HISTORICO, values, COLUMN_DATA + " = ?", new String[]{data});
            Log.d(TAG, "🔄 Consumo atualizado: " + quantidade + "ml para " + data);
        } else {
            // 📌 Criar novo registro
            values.put(COLUMN_DATA, data);
            values.put(COLUMN_QUANTIDADE, quantidade);
            values.put(COLUMN_META_DIARIA, metaDiaria);
            db.insert(TABLE_HISTORICO, null, values);
            Log.d(TAG, "📌 Novo registro criado: " + data + " | Meta: " + metaDiaria + "ml");
        }
    }

    // 📜 **Obtém o histórico de consumo**
    public Cursor obterHistorico() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_ID + " AS _id, " + COLUMN_DATA + ", " + COLUMN_QUANTIDADE + ", " + COLUMN_META_DIARIA +
                        " FROM " + TABLE_HISTORICO + " ORDER BY " + COLUMN_DATA + " DESC",
                null
        );

        Log.d(TAG, "📜 Histórico carregado.");
        return cursor;
    }
}
