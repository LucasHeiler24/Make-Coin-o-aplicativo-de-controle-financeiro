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
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import br.com.makecoin.view.TelaPrincipalCategorias.TelaPrincipalCategorias
import br.com.makecoin.view.TelaPrincipalCategorias.activity_tela_principal_categorias_receitas
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class activity_tela_das_categorias_despesas : AppCompatActivity() {
    private val TAG = "SuaAtividade"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_das_categorias_despesas)
        FirebaseApp.initializeApp(this)

        // Inicialize o Firestore
        val db = FirebaseFirestore.getInstance()

        usuarioCriaCategoria()

        val voltarParaTelaCategoriasReceitas = findViewById<ImageView>(R.id.Voltar_tela_inicial)
        voltarParaTelaCategoriasReceitas.setOnClickListener {
            abrirTelaPrincipalCategoriasDespesas()
        }

        val btnCriarCategoria = findViewById<Button>(R.id.btnCriarCategoria)
        btnCriarCategoria.setOnClickListener {
            val intent = Intent(this, activity_criar_categoria_despesa::class.java)
            startActivity(intent)
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
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaEducacao= findViewById<ImageView>(R.id.seta_educacao)
        setaEducacao.setOnClickListener {
            val categoriaSelecionada = "Educação" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriaeducacao // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#017985") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaVestuario = findViewById<ImageView>(R.id.seta_vestuario)
        setaVestuario.setOnClickListener {
            val categoriaSelecionada = "Vestuário" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriavestuario // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#EC4379") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaSupermercado = findViewById<ImageView>(R.id.seta_supermercado)
        setaSupermercado.setOnClickListener {
            val categoriaSelecionada = "Supermercado" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriasupermercado // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#CAC200") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaSaude = findViewById<ImageView>(R.id.seta_saude)
        setaSaude.setOnClickListener {
            val categoriaSelecionada = "Saúde" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriasaude // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#25BF25") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaLazer = findViewById<ImageView>(R.id.seta_lazer)
        setaLazer.setOnClickListener {
            val categoriaSelecionada = "Lazer" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categorialazer // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#A62C8F") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaTransporte = findViewById<ImageView>(R.id.seta_transporte)
        setaTransporte.setOnClickListener {
            val categoriaSelecionada = "Transporte" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriatransporte // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#E58736") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaViagens = findViewById<ImageView>(R.id.seta_viagens)
        setaViagens.setOnClickListener {
            val categoriaSelecionada = "Viagens" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriaviagens // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#29328A") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
        val setaAlimentacao = findViewById<ImageView>(R.id.seta_alimentacao)
        setaAlimentacao.setOnClickListener {
            val categoriaSelecionada = "Alimentação" // Ou obtenha a categoria selecionada de outra forma
            val iconeResId = R.drawable.categoriaalimentacao // Defina o ícone correspondente à categoria
            val corImagemRedonda = Color.parseColor("#D61117") // Defina a cor da imagem redonda correspondente à categoria
            abrirTelaPrincipalComCategoriaSelecionada(categoriaSelecionada, iconeResId, corImagemRedonda)
        }
    }
    private fun abrirTelaPrincipalCategoriasDespesas() {
        val intent = Intent(this, TelaPrincipalCategorias::class.java)
        startActivity(intent)
    }
    private fun abrirTelaPrincipalComCategoriaSelecionada(
        categoriaSelecionada: String,
        iconeResId: Int,
        corCirculoSelecionada: Int
    ) {
        val intent = Intent(this, TelaPrincipalCategorias::class.java)
        intent.putExtra("categoria_selecionada", categoriaSelecionada)
        intent.putExtra("icone_res_id", iconeResId)
        intent.putExtra("circulo_cor", corCirculoSelecionada) // Adicione a cor do círculo aqui
        startActivity(intent)
    }
    private fun abrirTelaPrincipalComCategoriaCriadaSelecionada(
        categoriaSelecionada: String,
        corCirculoSelecionada: Int
    ) {
        val intent = Intent(this, TelaPrincipalCategorias::class.java)
        intent.putExtra("categoria_selecionada", categoriaSelecionada)
        intent.putExtra("circulo_cor", corCirculoSelecionada) // Adicione a cor do círculo aqui
        startActivity(intent)
    }
    private fun usuarioCriaCategoria(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val despesasRef = FirebaseFirestore.getInstance().collection("categorias")
            .whereEqualTo("usuario_da_categoria", userId)

        despesasRef.get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Tentativa bem-sucedida de obter categorias.")

                for (document in result) {

                    val containerLayout = findViewById<LinearLayout>(R.id.layoutCategorias)
                    val inflater = LayoutInflater.from(this)

                    val nomeCategoria = document.getString("categoria_criada_pelo_usuario")
                    val corCategoria = document.getLong("cor_criada_pelo_usuario")

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