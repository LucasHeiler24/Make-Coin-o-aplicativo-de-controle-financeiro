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
import br.com.makecoin.view.TelaPrincipalCategorias.activity_tela_principal_categorias_receitas
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class activity_tela_editar_escolher_categorias : AppCompatActivity() {
    // Declare a propriedade do ID da receita
    private lateinit var receitaId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_editar_escolher_categorias)
        // Inicialize o Firestore
        val db = FirebaseFirestore.getInstance()
        receitaId = intent.getStringExtra("receita_id") ?: ""

        val voltarParaTelaCategoriasReceitas = findViewById<ImageView>(R.id.VoltarTelaAnterior)
        voltarParaTelaCategoriasReceitas.setOnClickListener {
            abrirTelaEditarPrinicpalCategoriasReceitas(receitaId)
        }
        // Defina a coleção e o documento que você deseja recuperar
        val collectionName = "categorias"
        val documentId = "categoria_receitas"

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

                        if (nomeCategoria != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoriaTextView = findViewById<TextView>(R.id.categoria_salario)
                            categoriaTextView.text = nomeCategoria
                        }
                        if (nomeCategoria2 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria2")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria2TextView = findViewById<TextView>(R.id.categoria_investimentos)
                            categoria2TextView.text = nomeCategoria2
                        }
                        if (nomeCategoria3 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria3")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria3TextView = findViewById<TextView>(R.id.categoria_imobiliaria)
                            categoria3TextView.text = nomeCategoria3
                        }
                        if (nomeCategoria4 != null) {
                            // Faça o que você precisa com o nome da categoria
                            Log.d(TAG, "Nome da categoria: $nomeCategoria4")
                            // Por exemplo, defina o nome da categoria em um TextView:
                            val categoria4TextView = findViewById<TextView>(R.id.categoria_premios)
                            categoria4TextView.text = nomeCategoria4
                        }
                    } else {
                        Log.d(TAG, "O documento não existe.")
                    }
                } else {
                    Log.d(TAG, "Falha ao obter o documento: ", task.exception)
                }
            }

        val setaSalario = findViewById<ImageView>(R.id.seta_salario)
        setaSalario.setOnClickListener {
            val categoriaSelecionada = "Salário" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriasalario // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#17C245") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId,  corImagemRedonda, receitaId)
        }
        val setaInvestimentos = findViewById<ImageView>(R.id.seta_investimentos)
        setaInvestimentos.setOnClickListener {
            val categoriaSelecionada = "Investimentos" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriainvestimentos // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#A52AD5") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, receitaId)
        }
        val setaImmobiliaria = findViewById<ImageView>(R.id.seta_imobiliaria)
        setaImmobiliaria.setOnClickListener {
            val categoriaSelecionada = "Imobiliária" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriaimobiliaria // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#D52A2A") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, receitaId)
        }
        val setaPremios = findViewById<ImageView>(R.id.seta_premios)
        setaPremios.setOnClickListener {
            val categoriaSelecionada = "Prêmios" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriapremios // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#0097B1") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda, receitaId)
        }
    }

    private fun abrirTelaEditarPrinicpalCategoriasReceitas(receitaId: String) {
        val intent = Intent(this, activity_tela_editar_os_registros_receitas::class.java)
        intent.putExtra("receita_id", receitaId) // Inclua o ID da receita na intenção
        startActivity(intent)
    }

    private fun abrirTelaPrincipalComCategoriaSelecionada(
        categoriaSelecionada: String,
        iconeResId: Int,
        corCirculoSelecionada: Int,
        receitaId: String
        ) {
        val intent = Intent()
        intent.putExtra("categoria_selecionada", categoriaSelecionada)
        intent.putExtra("icone_res_id", iconeResId)
        intent.putExtra("circulo_cor", corCirculoSelecionada) // Adicione a cor do círculo aqui
        intent.putExtra("receita_id", receitaId) // Inclua o ID da receita na intenção
        setResult(RESULT_OK, intent)
        finish()
    }

}