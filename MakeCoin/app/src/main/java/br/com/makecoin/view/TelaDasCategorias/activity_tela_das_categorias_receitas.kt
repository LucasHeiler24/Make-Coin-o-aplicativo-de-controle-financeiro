package br.com.makecoin.view.TelaDasCategorias

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import br.com.makecoin.R
import br.com.makecoin.view.Criar_categoria_despesa.activity_criar_categoria_despesa
import br.com.makecoin.view.Criar_categoria_despesa.activity_criar_categoria_receita
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import br.com.makecoin.view.TelaPrincipalCategorias.TelaPrincipalCategorias
import br.com.makecoin.view.TelaPrincipalCategorias.activity_tela_principal_categorias_receitas
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class activity_tela_das_categorias_receitas : AppCompatActivity() {
    private val TAG = "SuaAtividade2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_das_categorias_receitas)
        FirebaseApp.initializeApp(this)

        // Inicialize o Firestore
        val db = FirebaseFirestore.getInstance()
        usuarioCriaCategoria()

        val voltarParaTelaCategoriasReceitas = findViewById<ImageView>(R.id.VoltarTelaAnterior)
        voltarParaTelaCategoriasReceitas.setOnClickListener {
            abrirTelaPrinicpalCategoriasReceitas()
        }

        val btnCriarCategoria = findViewById<Button>(R.id.btnCriarCategoria)
        btnCriarCategoria.setOnClickListener {
            val intent = Intent(this, activity_criar_categoria_receita::class.java)
            startActivity(intent)
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
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaInvestimentos = findViewById<ImageView>(R.id.seta_investimentos)
        setaInvestimentos.setOnClickListener {
            val categoriaSelecionada = "Investimentos" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriainvestimentos // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#A52AD5") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaImmobiliaria = findViewById<ImageView>(R.id.seta_imobiliaria)
        setaImmobiliaria.setOnClickListener {
            val categoriaSelecionada = "Imobiliária" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriaimobiliaria // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#D52A2A") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaPremios = findViewById<ImageView>(R.id.seta_premios)
        setaPremios.setOnClickListener {
            val categoriaSelecionada = "Prêmios" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriapremios // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#0097B1") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
    }

    private fun abrirTelaPrinicpalCategoriasReceitas() {
        val intent = Intent(this, activity_tela_principal_categorias_receitas::class.java)
        startActivity(intent)
    }

    private fun abrirTelaPrincipalComCategoriaSelecionada(
        categoriaSelecionada: String,
        iconeResId: Int,
        corCirculoSelecionada: Int
    ) {
        val intent = Intent(this, activity_tela_principal_categorias_receitas::class.java)
        intent.putExtra("categoria_selecionada", categoriaSelecionada)
        intent.putExtra("icone_res_id", iconeResId)
        intent.putExtra("circulo_cor", corCirculoSelecionada) // Adicione a cor do círculo aqui
        startActivity(intent)
    }
    private fun abrirTelaPrincipalComCategoriaCriadaSelecionada(
        categoriaSelecionada: String,
        corCirculoSelecionada: Int
    ) {
        val intent = Intent(this, activity_tela_principal_categorias_receitas::class.java)
        intent.putExtra("categoria_selecionada", categoriaSelecionada)
        intent.putExtra("circulo_cor", corCirculoSelecionada) // Adicione a cor do círculo aqui
        startActivity(intent)
    }
    private fun usuarioCriaCategoria(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val despesasRef = FirebaseFirestore.getInstance().collection("categorias")
            .whereEqualTo("usuario_da_categoria_receita", userId)

        despesasRef.get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Tentativa bem-sucedida de obter categorias.")

                for (document in result) {

                    val containerLayout = findViewById<LinearLayout>(R.id.layoutCategorias)
                    val inflater = LayoutInflater.from(this)

                    val nomeCategoria = document.getString("categoria_criada_pelo_usuario_receita")
                    val corCategoria = document.getLong("cor_criada_pelo_usuario_receita")

                    if (nomeCategoria != null && corCategoria != null) {

                        // Inflate the item_categoria layout for each category
                        val itemCategoria = inflater.inflate(R.layout.item_categoria, containerLayout, false)

                        // Populate the inflated layout with data
                        val categoriaTextView = itemCategoria.findViewById<TextView>(R.id.nomeCategoria)
                        val corView = itemCategoria.findViewById<ImageView>(R.id.circuloCor)
                        val viewCategoria = itemCategoria.findViewById<View>(R.id.viewCategoria)

                        categoriaTextView.text = nomeCategoria

                        // Aplica a cor selecionada como tintura na imagem
                        val drawable = ContextCompat.getDrawable(this, R.drawable.circle_background)

                        drawable?.let {
                            it.mutate()  // Necessário para evitar que a tintura afete outras instâncias da mesma imagem
                            DrawableCompat.setTint(it, corCategoria.toInt())
                            corView.setImageDrawable(it)
                        }

                        val setaCategoria = itemCategoria.findViewById<ImageView>(R.id.categoriaCriadaUsuario)
                        setaCategoria.setOnClickListener {
                            abrirTelaPrincipalComCategoriaCriadaSelecionada(
                                nomeCategoria.toString(),
                                corCategoria.toInt()
                            )
                        }
                        containerLayout.addView(itemCategoria)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
}
