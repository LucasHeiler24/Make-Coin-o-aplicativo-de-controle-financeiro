package br.com.makecoin.view.TelaPrincipal

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import br.com.makecoin.R
import br.com.makecoin.view.TelaPrincipalCategorias.TelaPrincipalCategorias
import br.com.makecoin.view.TelaPrincipalGraficos.TelaPrincipalGraficos
import br.com.makecoin.view.telaConfiguracoesEditar.activity_tela_configuracoes_editar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalTime
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import java.util.Calendar

class TelaPrincipal : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var nomeCompletoTextView: TextView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var profileImageView: ImageView
    private val PROFILE_IMAGE_REQUEST_CODE = 1
    private lateinit var mesSelecionadoTextView: TextView // Adicionar esta linha
    private val mesesAbreviados = arrayOf(
        "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
        "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    )
    private var mesSelecionado = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_principal)

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Defina os ícones para os itens do menu programaticamente
        bottomNavigationView.menu.findItem(R.id.navigation_item1).setIcon(R.drawable.navegarcasa)
        bottomNavigationView.menu.findItem(R.id.navigation_item2).setIcon(R.drawable.mais)
        bottomNavigationView.menu.findItem(R.id.navigation_item3).setIcon(R.drawable.estatistico)

        nomeCompletoTextView = findViewById(R.id.saudacaoNomeTextView)

        val menuHamburguer = findViewById<ImageView>(R.id.menuHamburguer)
        menuHamburguer.setOnClickListener {
            abrirTelaConfiguracoes()
        }

        profileImageView = findViewById(R.id.profileImageView)

        buscarNomeUsuario()
        carregarImagemPerfil()
        bottomNavigationView.setSelectedItemId(R.id.navigation_item1)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.navigation_item1-> {
                    // Abrir a Tela 1
                    val intent1 = Intent(this, TelaPrincipal::class.java)
                    startActivity(intent1)
                    true
                }
                R.id.navigation_item2 -> {
                    // Abrir a Tela 2
                    val intent2 = Intent(this, TelaPrincipalCategorias::class.java)
                    startActivity(intent2)
                    true
                }
                R.id.navigation_item3 -> {
                    // Abrir a Tela 3
                    val intent3 = Intent(this, TelaPrincipalGraficos::class.java)
                    startActivity(intent3)
                    true
                }
                else -> false
            }
        }
        mesSelecionadoTextView = findViewById(R.id.mesSelecionadoTextView)

        // Obter o mês e o ano atuais
        val calendar = Calendar.getInstance()
        val anoAtual = calendar.get(Calendar.YEAR)
        val mesAtual = calendar.get(Calendar.MONTH)

        // Verificar se o valor do mês foi salvo nas preferências compartilhadas
        if (sharedPreferences.contains("mesSelecionado")) {
            // Se estiver salvo, carregar o valor e atualizar a TextView
            mesSelecionado = sharedPreferences.getInt("mesSelecionado", 0)
            atualizarMesSelecionado(anoAtual, mesSelecionado)
        } else {
            // Se não estiver salvo, definir o mês atual na TextView e salvar nas preferências
            mesSelecionado = mesAtual
            atualizarMesSelecionado(anoAtual, mesAtual)

            // Salvar o valor do mês nas preferências compartilhadas
            val editor = sharedPreferences.edit()
            editor.putInt("mesSelecionado", mesSelecionado)
            editor.apply()
        }
        val setaBaixoImageView = findViewById<ImageView>(R.id.setaBaixoImageView)
        setaBaixoImageView.setOnClickListener {
            mostrarDialogMiniCalendario()
        }
    }
    private fun atualizarMesSelecionado(ano: Int, mes: Int) {
        val mesAtual = mesesAbreviados[mes]
        val texto = "$mesAtual $ano"
        mesSelecionadoTextView.text = texto
    }
    private fun mostrarDialogMiniCalendario() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_mini_calendario, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Selecione o ano e mês")

        val anoPicker = dialogView.findViewById<NumberPicker>(R.id.anoPicker)
        val mesPicker = dialogView.findViewById<NumberPicker>(R.id.mesPicker)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        // Configurar o NumberPicker para o ano
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        anoPicker.minValue = anoAtual - 5 // Mostrar os últimos 5 anos
        anoPicker.maxValue = anoAtual + 5 // Mostrar os próximos 5 anos
        anoPicker.value = anoAtual // Selecionar o ano atual

        // Configurar o NumberPicker para os meses
        mesPicker.minValue = 0 // Janeiro (índice 0)
        mesPicker.maxValue = mesesAbreviados.size - 1 // Dezembro (índice 11)
        mesPicker.displayedValues = mesesAbreviados // Mostrar os meses abreviados
        mesPicker.value = mesSelecionado // Selecionar o mês atual

        val dialog = dialogBuilder.create()

        btnConfirmar.setOnClickListener {
            val anoSelecionado = anoPicker.value
            val mesSelecionado = mesPicker.value

            // Faça o que desejar com o anoSelecionado e mesSelecionado
            // Por exemplo, atualize o mês selecionado na tela principal

            atualizarMesSelecionado(anoSelecionado, mesSelecionado)
            dialog.dismiss() // Fechar o dialog após confirmar
        }

        dialog.show()
    }
    override fun onBackPressed() {
        // Impede que o usuário volte deslizando para a tela anterior
    }
    private fun buscarNomeUsuario() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { id ->
            db.collection("usuario").document(id).get()
                .addOnSuccessListener { documentSnapshot ->
                    val nome = documentSnapshot.getString("nome")
                    nome?.let { nomeUsuario ->
                        val saudacaoNome = getSaudacao(nomeUsuario)
                        nomeCompletoTextView.text = saudacaoNome
                    }
                }
        }
    }

    private fun getSaudacao(nome: String): String {
        val primeiroNome = extrairPrimeiroNome(nome)
        val tempoAgora = LocalTime.now()

        return when {
            tempoAgora.isAfter(LocalTime.of(0, 0)) && tempoAgora.isBefore(LocalTime.of(12, 0)) -> "Bom dia $primeiroNome!"
            tempoAgora.isAfter(LocalTime.of(12, 0)) && tempoAgora.isBefore(LocalTime.of(18, 0)) -> "Boa tarde $primeiroNome!"
            else -> "Boa noite $primeiroNome!"
        }
    }

    private fun extrairPrimeiroNome(nomeCompleto: String): String {
        val nomeParts = nomeCompleto.split(" ")
        return nomeParts.firstOrNull() ?: ""
    }

    private fun abrirTelaConfiguracoes() {
        val intent = Intent(this, activity_tela_configuracoes_editar::class.java)
        startActivityForResult(intent, PROFILE_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PROFILE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val novoNome = data?.getStringExtra("novoNome")
            novoNome?.let {
                val saudacaoNome = getSaudacao(it)
                nomeCompletoTextView.text = saudacaoNome
            }
            val novaUrlImagem = data?.getStringExtra("novaUrlImagem")
            novaUrlImagem?.let { imageUrl ->
                // Carrega a nova imagem de perfil usando a biblioteca Glide e adiciona circleCrop()
                Glide.with(this)
                    .load(imageUrl)
                    .transform(CircleCrop()) // Exibe a imagem de forma redonda
                    .skipMemoryCache(true) // Desabilita o cache de memória
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(profileImageView)
            }
        }
    }

    private fun carregarImagemPerfil() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { id ->
            db.collection("usuario").document(id).get()
                .addOnSuccessListener { documentSnapshot ->
                    val urlImagem = documentSnapshot.getString("urlImagem")
                    urlImagem?.let { imageUrl ->
                        // Carrega a imagem de perfil usando a biblioteca Glide e adiciona circleCrop()
                        Glide.with(this)
                            .load(imageUrl)
                            .transform(CircleCrop()) // Exibe a imagem de forma redonda
                            .skipMemoryCache(true) // Desabilita o cache de memória
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(profileImageView)
                    }
                }
        }
    }
}
