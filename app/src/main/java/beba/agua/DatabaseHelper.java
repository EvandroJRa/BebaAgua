package beba.agua;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "bebaagua.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_HISTORICO = "historico";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_QUANTIDADE = "quantidade";
    private static final String COLUMN_META_DIARIA = "metaDiaria";
    private LembretesListener lembretesListener;

    private static final String CREATE_TABLE_HISTORICO = "CREATE TABLE IF NOT EXISTS " + TABLE_HISTORICO + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DATA + " TEXT NOT NULL UNIQUE, " +
            COLUMN_QUANTIDADE + " REAL NOT NULL DEFAULT 0, " +
            COLUMN_META_DIARIA + " REAL NOT NULL DEFAULT 500)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public interface LembretesListener {
        void verificarEAtualizarLembretes();
    }
    public void setLembretesListener(LembretesListener listener) {
        this.lembretesListener = listener;
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
                        "SELECT _id, data, IFNULL(quantidade, 0), IFNULL(metaDiaria, 500) FROM historico_old");
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

        // 🔄 Garante que o novo dia foi iniciado antes de registrar consumo
        iniciarNovoDia();

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

            // 🔥 Chama o listener para verificar lembretes quando a meta for atingida
            if (lembretesListener != null && novoTotal >= metaDiaria) {
                lembretesListener.verificarEAtualizarLembretes();
            }

        } else {
            values.put(COLUMN_DATA, data);
            values.put(COLUMN_QUANTIDADE, quantidade);
            values.put(COLUMN_META_DIARIA, metaDiaria);
            db.insert(TABLE_HISTORICO, null, values);
            Log.d("DatabaseHelper", "📌 Novo registro criado: " + data + " | Meta: " + metaDiaria + "ml");
        }
    }

    // 🔥 Atualiza a meta diária no banco de dados
    public void atualizarMetaDiaria(String data, double novaMeta) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_META_DIARIA, novaMeta);

        int rowsAffected = db.update(TABLE_HISTORICO, values, COLUMN_DATA + " = ?", new String[]{data});

        if (rowsAffected > 0) {
            Log.d("DatabaseHelper", "✅ Meta diária ATUALIZADA para " + novaMeta + "ml na data " + data);
        } else {
            Log.w("DatabaseHelper", "⚠️ Nenhum registro atualizado! Criando nova entrada para " + data);
            values.put(COLUMN_DATA, data);
            values.put(COLUMN_QUANTIDADE, 0); // Inicializa com 0ml de consumo
            db.insert(TABLE_HISTORICO, null, values);
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

    public String obterDataAtual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    public void iniciarNovoDia() {
        String dataAtual = obterDataAtual();
        SQLiteDatabase db = this.getWritableDatabase();

        // Verifica se já existe um registro para a data atual
        if (!verificarRegistroExistente(dataAtual)) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DATA, dataAtual);
            values.put(COLUMN_QUANTIDADE, 0); // Reseta o consumo diário
            values.put(COLUMN_META_DIARIA, 0); // Define a meta padrão (ajuste conforme necessário)

            db.insert(TABLE_HISTORICO, null, values);
            Log.d("DatabaseHelper", "📌 Novo dia detectado! Meta reiniciada para " + dataAtual);
        } else {
            Log.d("DatabaseHelper", "🔄 Data já registrada no histórico. Nenhuma ação necessária.");
        }
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
