package br.com.makecoin.view.telaEditarRegistros

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import br.com.makecoin.R
import br.com.makecoin.view.TelaPrincipalCategorias.TelaPrincipalCategorias
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class activity_tela_editar_despesa_escolher_categorias : AppCompatActivity() {
    private lateinit var despesasId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_editar_despesa_escolher_categorias)

        // Inicialize o Firestore
        val db = FirebaseFirestore.getInstance()
        despesasId = intent.getStringExtra("despesa_id") ?: ""

        val voltarParaTelaCategoriasReceitas = findViewById<ImageView>(R.id.Voltar_tela_inicial)
        voltarParaTelaCategoriasReceitas.setOnClickListener {
        }

        // Defina a coleção e o documento que você deseja recuperar
        val collectionName = "categorias"
        val documentId = "categorias_despesas"

        // Recupere o documento do Firestore
        db.collection(collectionName).document(documentId).get()
            .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        // O documento existe, agora você pode obter o nome da categoria
                        val nomeCategoria = document.getString("categoria1") // "nome" é o nome do campo onde está o nome da categoria
                        val nomeCategoria2 = document.getString("categoria2") // "nome" é o nome do campo onde está o nome da categoria
                        val nomeCategoria3 = document.getString("categoria3") // "nome" é o nome do campo onde está o nome da categoria
                        val nomeCategoria4 = document.getString("categoria4") // "nome" é o nome do campo onde está o nome da categoria
                        val nomeCategoria5 = document.getString("categoria5") // "nome" é o nome do campo onde está o nome da categoria
                        val nomeCategoria6 = document.getString("categoria6") // "nome" é o nome do campo onde está o nome da categoria
                        val nomeCategoria7 = document.getString("categoria7") // "nome" é o nome do campo onde está o nome da categoria
                        val nomeCategoria8 = document.getString("categoria8") // "nome" é o nome do campo onde está o nome da categoria
                        val nomeCategoria9 = document.getString("categoria9") // "nome" é o nome do campo onde está o nome da categoria

                        if (nomeCategoria != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoriaTextView = findViewById<TextView>(R.id.categoria_moradia)
                            categoriaTextView.text = nomeCategoria
                        }
                        if (nomeCategoria2 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria2")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria2TextView = findViewById<TextView>(R.id.categoria_educacao)
                            categoria2TextView.text = nomeCategoria2
                        }
                        if (nomeCategoria3 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria3")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria3TextView = findViewById<TextView>(R.id.categoria_vestuario)
                            categoria3TextView.text = nomeCategoria3
                        }
                        if (nomeCategoria4 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria4")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria4TextView = findViewById<TextView>(R.id.categoria_supermercado)
                            categoria4TextView.text = nomeCategoria4
                        }
                        if (nomeCategoria5 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria5")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria5TextView = findViewById<TextView>(R.id.categoria_saude)
                            categoria5TextView.text = nomeCategoria5
                        }
                        if (nomeCategoria6 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria6")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria6TextView = findViewById<TextView>(R.id.categoria_lazer)
                            categoria6TextView.text = nomeCategoria6
                        }
                        if (nomeCategoria7 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria7")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria7TextView = findViewById<TextView>(R.id.categoria_transporte)
                            categoria7TextView.text = nomeCategoria7
                        }
                        if (nomeCategoria8 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria8")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria8TextView = findViewById<TextView>(R.id.categoria_viagens)
                            categoria8TextView.text = nomeCategoria8
                        }
                        if (nomeCategoria9 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria9")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria9TextView = findViewById<TextView>(R.id.categoria_alimentacao)
                            categoria9TextView.text = nomeCategoria9
                        }
                    } else {
                        Log.d(TAG, "O documento não existe.")
                    }
                } else {
                    Log.d(TAG, "Falha ao obter o documento: ", task.exception)
                }
            }
        val setaMoradia = findViewById<ImageView>(R.id.seta_moradia)
        setaMoradia.setOnClickListener {
            val categoriaSelecionada = "Moradia" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriamoradia // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#24077E") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, despesasId)
        }
        val setaEducacao= findViewById<ImageView>(R.id.seta_educacao)
        setaEducacao.setOnClickListener {
            val categoriaSelecionada = "Educação" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriaeducacao // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#017985") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, despesasId)
        }
        val setaVestuario = findViewById<ImageView>(R.id.seta_vestuario)
        setaVestuario.setOnClickListener {
            val categoriaSelecionada = "Vestuário" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriavestuario // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#EC4379") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, despesasId)
        }
        val setaSupermercado = findViewById<ImageView>(R.id.seta_supermercado)
        setaSupermercado.setOnClickListener {
            val categoriaSelecionada = "Supermercado" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriasupermercado // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#CAC200") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, despesasId)
        }
        val setaSaude = findViewById<ImageView>(R.id.seta_saude)
        setaSaude.setOnClickListener {
            val categoriaSelecionada = "Saúde" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriasaude // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#25BF25") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, despesasId)
        }
        val setaLazer = findViewById<ImageView>(R.id.seta_lazer)
        setaLazer.setOnClickListener {
            val categoriaSelecionada = "Lazer" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categorialazer // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#A62C8F") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, despesasId)
        }
        val setaTransporte = findViewById<ImageView>(R.id.seta_transporte)
        setaTransporte.setOnClickListener {
            val categoriaSelecionada = "Transporte" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriatransporte // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#E58736") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, despesasId)
        }
        val setaViagens = findViewById<ImageView>(R.id.seta_viagens)
        setaViagens.setOnClickListener {
            val categoriaSelecionada = "Viagens" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriaviagens // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#29328A") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, despesasId)
        }
        val setaAlimentacao = findViewById<ImageView>(R.id.seta_alimentacao)
        setaAlimentacao.setOnClickListener {
            val categoriaSelecionada = "Alimentação" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriaalimentacao // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#D61117") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, despesasId)
        }
    }
    private fun abrirTelaPrincipalComCategoriaSelecionada(
        categoriaSelecionada: String,
        iconeResId: Int,
        corCirculoSelecionada: Int,
        despesasId: String
    ) {
        val intent = Intent()
        intent.putExtra("categoria_selecionada", categoriaSelecionada)
        intent.putExtra("icone_res_id", iconeResId)
        intent.putExtra("circulo_cor", corCirculoSelecionada) // Adicione a cor do círculo aqui
        intent.putExtra("despesa_id", despesasId) // Inclua o ID da receita na intenção
        setResult(RESULT_OK, intent)
        finish()
    }
}