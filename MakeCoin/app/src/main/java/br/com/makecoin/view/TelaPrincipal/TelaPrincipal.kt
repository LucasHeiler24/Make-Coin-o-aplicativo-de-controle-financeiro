package br.com.makecoin.view.TelaPrincipal

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import br.com.makecoin.R
import br.com.makecoin.view.TelaPrincipalCategorias.TelaPrincipalCategorias
import br.com.makecoin.view.TelaPrincipalGraficos.TelaPrincipalGraficos
import br.com.makecoin.view.telaConfiguracoesEditar.activity_tela_configuracoes_editar
import br.com.makecoin.view.telaDeTodosRegistros.activity_tela_de_todos_os_registros
import br.com.makecoin.view.telaEditarRegistros.activity_tela_visualizar_despesas
import br.com.makecoin.view.telaEditarRegistros.activity_tela_visualizar_registro
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalTime
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private var anoSelecionado = 0
    private var totalReceitas = 0.0
    private var totalDespesas = 0.0
    private var valorParcelaAtual: Double = 0.0
    private var documentosDespesas: List<DocumentSnapshot> = mutableListOf() // Declaração da variável no nível da classe
    private var documentosDespesasRecorrentes: List<DocumentSnapshot> = mutableListOf() // Declaração da variável no nível da classe
    private var documentosUltimosRegistros: List<DocumentSnapshot> = mutableListOf() // Declaração da variável no nível da classe
    private var documentosUltimosReceitas: List<DocumentSnapshot> = mutableListOf() // Declaração da variável no nível da classe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_principal)
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
        sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

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
        val telaRegistro = findViewById<ImageView>(R.id.ir_para_tela_registros)
        telaRegistro.setOnClickListener {
            abrirTelaRegistros()
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
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

            // Se estiver salvo, carregar o valor e atualizar a TextView
            mesSelecionado = sharedPreferences.getInt("mesSelecionado", 0)
        } else {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

            // Se não estiver salvo, definir o mês atual e salvar nas preferências
            mesSelecionado = mesAtual

            // Salvar o valor do mês nas preferências compartilhadas
            val editor = sharedPreferences.edit()
            editor.putInt("mesSelecionado", mesSelecionado)
            editor.apply()
        }

        // Verificar se o valor do ano foi salvo nas preferências compartilhadas
        if (sharedPreferences.contains("anoSelecionado")) {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)
            
            // Se estiver salvo, carregar o valor
            anoSelecionado = sharedPreferences.getInt("anoSelecionado", anoAtual)
        } else {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

            // Se não estiver salvo, definir o ano atual e salvar nas preferências
            anoSelecionado = anoAtual

            // Salvar o valor do ano nas preferências compartilhadas
            val editor = sharedPreferences.edit()
            editor.putInt("anoSelecionado", anoSelecionado)
            editor.apply()
        }

        // Atualizar a TextView do mês selecionado na tela principal
        atualizarMesSelecionado(anoSelecionado, mesSelecionado)

        val setaBaixoImageView = findViewById<ImageView>(R.id.setaBaixoImageView)
        setaBaixoImageView.setOnClickListener {
            mostrarDialogMiniCalendario()
        }
        carregarDespesas()
        carregarDespesasRecorrentes()
        carregarUltimosRegistros()

        // Obter os dados da intent
        val iconeResId = intent.getIntExtra("icone_res_id", 0)
        val corImagemRedonda = intent.getIntExtra("cor_imagem_redonda", 0)
        val corCirculo = intent.getIntExtra("cor_circulo", 0)

        exibirUltimosRegistros(documentosUltimosRegistros, mesSelecionado, anoSelecionado)
        exibirUltimosReceitas(documentosUltimosReceitas, mesSelecionado, anoSelecionado)

        atualizarValoresReceitasDespesas()
    }
    override fun onResume() {
        super.onResume()
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
        sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

        // Obter os valores salvos nas preferências compartilhadas
        mesSelecionado = sharedPreferences.getInt("mesSelecionado", 0)
        anoSelecionado = sharedPreferences.getInt("anoSelecionado", 0)

        // Atualizar a TextView do mês selecionado na tela principal
        atualizarMesSelecionado(anoSelecionado, mesSelecionado)

        // Chame a função que atualiza os valores de receitas e despesas com o mês selecionado
        atualizarValoresReceitasDespesas()
    }
    private fun exibirDespesas(
        despesas: List<DocumentSnapshot>,
        mesSelecionado: Int,
        anoSelecionado: Int
    ) {
        val containerLayout = findViewById<LinearLayout>(R.id.layoutDespesasContainer)
        val inflater = LayoutInflater.from(this)
        containerLayout.removeAllViews()

        for (despesa in despesas) {
            val valorJaPago = despesa.getString("valor_ja_pago")
            val numeroParcelas = despesa.getLong("numero_de_parcelas") ?: 1
            val dataEfetuacaoTimestamp =
                despesa.get("data_efetuacao") as com.google.firebase.Timestamp
            val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
            val parcelasPagas = despesa.getLong("parcela_paga") ?: 1
            val calendar = Calendar.getInstance()
            calendar.time = dataEfetuacao

            if (valorJaPago == "Pago" && parcelasPagas <= numeroParcelas) {
                val parcelaMes = calendar.get(Calendar.MONTH)
                val parcelaAno = calendar.get(Calendar.YEAR)

                val isUltimaParcela = parcelasPagas == numeroParcelas

                if (parcelaMes == mesSelecionado && parcelaAno == anoSelecionado) {
                    val itemDespesa =
                        inflater.inflate(R.layout.item_despesa, containerLayout, false)

                    // Configurar os elementos do itemDespesa, por exemplo:
                    val nomeDespesaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                    val descricaoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                    val favorecidoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                    val valorParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemValorParcela)
                    val proximaParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemProximaParcela)

                    nomeDespesaTextView.text = despesa.getString("nome_despesa")
                    descricaoTextView.text = despesa.getString("descricao")
                    favorecidoTextView.text = despesa.getString("nome_favorecido")
                    valorParcelaTextView.text =
                        formatCurrencyValue(despesa.getDouble("valor_das_parcelas_calculado")
                            ?: 0.0)

                    val mesAbreviado = obterAbreviacaoMes(parcelaMes)
                    val dia = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)

                    // Verificar se esta é a última parcela e ajustar o mês exibido
                    if (isUltimaParcela) {
                        proximaParcelaTextView.text = "$mesAbreviado/$dia (Última Parcela)"
                    } else {
                        proximaParcelaTextView.text = "$mesAbreviado/$dia"
                    }
                    containerLayout.addView(itemDespesa)

                    // Verificar se esta é a próxima parcela a ser paga
                    if (proximaParcelaTextView.text == "$mesSelecionado/01") {
                        proximaParcelaTextView.setTextColor(Color.RED)
                    }
                }
            }
            else{
                val parcelaMes = calendar.get(Calendar.MONTH)
                val parcelaAno = calendar.get(Calendar.YEAR)

                val isUltimaParcela = parcelasPagas == numeroParcelas

                if (parcelaMes == mesSelecionado && parcelaAno == anoSelecionado) {
                    val itemDespesa =
                        inflater.inflate(R.layout.item_despesa, containerLayout, false)

                    // Configurar os elementos do itemDespesa, por exemplo:
                    val nomeDespesaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                    val descricaoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                    val favorecidoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                    val valorParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemValorParcela)
                    val proximaParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemProximaParcela)

                    nomeDespesaTextView.text = despesa.getString("nome_despesa")
                    descricaoTextView.text = despesa.getString("descricao")
                    favorecidoTextView.text = despesa.getString("nome_favorecido")
                    valorParcelaTextView.text = "Valor pendente"

                    val mesAbreviado = obterAbreviacaoMes(parcelaMes)
                    val dia = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)

                    // Verificar se esta é a última parcela e ajustar o mês exibido
                    if (isUltimaParcela) {
                        proximaParcelaTextView.text = "$mesAbreviado/$dia (Última Parcela)"
                    } else {
                        proximaParcelaTextView.text = "$mesAbreviado/$dia"
                    }
                    containerLayout.addView(itemDespesa)

                    // Verificar se esta é a próxima parcela a ser paga
                    if (proximaParcelaTextView.text == "$mesSelecionado/01") {
                        proximaParcelaTextView.setTextColor(Color.RED)
                    }
                }
            }
        }
    }
    private fun obterAbreviacaoMes(mes: Int): String {
        val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, mes)
        return dateFormat.format(calendar.time)
    }
    private fun carregarDespesas() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val db = FirebaseFirestore.getInstance()

            val despesasRef = db.collection("despesas")
                .whereEqualTo("userId", uid)
                .whereEqualTo("forma_pagamento", "Mensal")

            despesasRef.get()
                .addOnSuccessListener { documents ->
                    val documentosDespesas = documents.documents // Obter a lista de documentos despesas

                    exibirDespesas(documentosDespesas, mesSelecionado, anoSelecionado)
                }
                .addOnFailureListener { exception ->
                    // Lidar com falha na obtenção das despesas
                    Toast.makeText(this, "Falha ao obter as despesas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun exibirDespesasRecorrentes(
        despesasRecorrentes: List<DocumentSnapshot>,
        mesSelecionado: Int,
        anoSelecionado: Int
    ) {
        val containerLayout = findViewById<LinearLayout>(R.id.layoutDespesasRecorrentesContainer)
        val inflater = LayoutInflater.from(this)
        containerLayout.removeAllViews()

        for (recorrenteDespesas in despesasRecorrentes) {
            val valorJaPago = recorrenteDespesas.getString("valor_ja_pago")
            val opcaoRecorrente = recorrenteDespesas.getString("opcao_recorrente")
            val tempoRecorrente = recorrenteDespesas.getLong("tempo_recorrente") ?: 1
            val recorrenteTempo = recorrenteDespesas.getLong("periodo_paga") ?: 1
            val dataEfetuacaoTimestamp = recorrenteDespesas.get("data_efetuacao") as com.google.firebase.Timestamp
            val dataEfetuacao = dataEfetuacaoTimestamp.toDate()

            val calendar = Calendar.getInstance()
            calendar.time = dataEfetuacao
            if (valorJaPago == "Pago") {
                    val parcelaMes = calendar.get(Calendar.MONTH)
                    val parcelaAno = calendar.get(Calendar.YEAR)

                    val isUltimaParcela = recorrenteTempo == tempoRecorrente

                    if (parcelaMes == mesSelecionado && parcelaAno == anoSelecionado) {
                        val itemDespesa = inflater.inflate(R.layout.item_despesa, containerLayout, false)

                        // Configurar os elementos do itemDespesa, por exemplo:
                        val nomeDespesaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                        val descricaoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                        val favorecidoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                        val valorParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemValorParcela)
                        val proximaParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemProximaParcela)

                        nomeDespesaTextView.text = recorrenteDespesas.getString("nome_despesa")
                        descricaoTextView.text = recorrenteDespesas.getString("descricao")
                        favorecidoTextView.text = recorrenteDespesas.getString("nome_favorecido")
                        valorParcelaTextView.text =
                            formatCurrencyValue(recorrenteDespesas.getDouble("valor_despesa") ?: 0.0)

                        val mesAbreviado = obterAbreviacaoMes(parcelaMes)
                        val dia = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)

                        // Verificar se esta é a última parcela e ajustar o mês exibido
                        if (isUltimaParcela) {
                            proximaParcelaTextView.text = "$mesAbreviado/$dia (Último período)"
                        } else {
                            proximaParcelaTextView.text = "$mesAbreviado/$dia"
                        }
                        containerLayout.addView(itemDespesa)

                        // Verificar se esta é a próxima parcela a ser paga
                        if (proximaParcelaTextView.text == "$mesSelecionado/01") {
                            proximaParcelaTextView.setTextColor(Color.RED)
                        }
                }
            }
            else{
                val parcelaMes = calendar.get(Calendar.MONTH)
                val parcelaAno = calendar.get(Calendar.YEAR)

                val isUltimaParcela = recorrenteTempo == tempoRecorrente

                if (parcelaMes == mesSelecionado && parcelaAno == anoSelecionado) {
                    val itemDespesa = inflater.inflate(R.layout.item_despesa, containerLayout, false)

                    // Configurar os elementos do itemDespesa, por exemplo:
                    val nomeDespesaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                    val descricaoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                    val favorecidoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                    val valorParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemValorParcela)
                    val proximaParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemProximaParcela)

                    nomeDespesaTextView.text = recorrenteDespesas.getString("nome_despesa")
                    descricaoTextView.text = recorrenteDespesas.getString("descricao")
                    favorecidoTextView.text = recorrenteDespesas.getString("nome_favorecido")
                    valorParcelaTextView.text = "Valor pendente"
                    val mesAbreviado = obterAbreviacaoMes(parcelaMes)
                    val dia = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)

                    // Verificar se esta é a última parcela e ajustar o mês exibido
                    if (isUltimaParcela) {
                        proximaParcelaTextView.text = "$mesAbreviado/$dia (Último período)"
                    } else {
                        proximaParcelaTextView.text = "$mesAbreviado/$dia"
                    }
                    containerLayout.addView(itemDespesa)

                    // Verificar se esta é a próxima parcela a ser paga
                    if (proximaParcelaTextView.text == "$mesSelecionado/01") {
                        proximaParcelaTextView.setTextColor(Color.RED)
                    }
                }
            }
        }
    }
    private fun carregarDespesasRecorrentes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val db = FirebaseFirestore.getInstance()

            val despesasRef = db.collection("despesas")
                .whereEqualTo("userId", uid)
                .whereEqualTo("forma_pagamento", "Recorrente")

            despesasRef.get()
                .addOnSuccessListener { documents ->
                    val documentosDespesasRecorrentes = documents.documents // Obter a lista de documentos despesas

                    exibirDespesasRecorrentes(documentosDespesasRecorrentes, mesSelecionado, anoSelecionado)
                }
                .addOnFailureListener { exception ->
                    // Lidar com falha na obtenção das despesas
                    Toast.makeText(this, "Falha ao obter as despesas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun exibirUltimosRegistros(
        documentosUltimosRegistros: List<DocumentSnapshot>,
        mesSelecionado: Int,
        anoSelecionado: Int,
    ) {
        val containerLayout = findViewById<LinearLayout>(R.id.layoutUltimosRegistros)
        val inflater = LayoutInflater.from(this)
        containerLayout.removeAllViews() // Remove todas as visualizações antigas

        for (despesa in documentosUltimosRegistros) {
            val valorJaPago = despesa.getString("valor_ja_pago")
            val numeroParcelas = despesa.getLong("numero_de_parcelas") ?: 1
            val idDespesa = despesa.getString("despesa_id")
            val opcaoRecorrente = despesa.getString("opcao_recorrente")
            val formaPagamento = despesa.getString("forma_pagamento")
            val dataEfetuacaoTimestamp = despesa.get("data_efetuacao") as com.google.firebase.Timestamp
            val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
            val parcelasPagas = despesa.getLong("parcela_paga") ?: 1

            val calendar = Calendar.getInstance()
            calendar.time = dataEfetuacao

            if (valorJaPago == "Pago") {
                if (formaPagamento == "Mensal") {
                        val itemDespesa = inflater.inflate(R.layout.ultimos_registros, containerLayout, false)

                        // Configurar os elementos do itemDespesa
                        val nomeDespesaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                        val nomeCategoriaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeCategoria)
                        val descricaoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                        val favorecidoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                        val valorParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.valorRegistro)
                        val proximaParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.dataRegistro)

                        nomeDespesaTextView.text = despesa.getString("nome_despesa")
                        nomeCategoriaTextView.text =
                            despesa.getString("categoria_escolhida_pelo_usuario")
                        descricaoTextView.text = despesa.getString("descricao")
                        favorecidoTextView.text = despesa.getString("nome_favorecido")
                        valorParcelaTextView.text = formatCurrencyValue(
                            despesa.getDouble("valor_das_parcelas_calculado") ?: 0.0
                        )

                        // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dataFormatada = formatoData.format(dataEfetuacao)
                        proximaParcelaTextView.text = dataFormatada

                        // Recuperar os IDs inteiros dos ícones e cores
                        val iconeResId = despesa.getLong("iconeResId")?.toInt() ?: 0
                        val corImagemRedonda = despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                        val corCirculo = despesa.getLong("corCirculo")?.toInt() ?: 0

                        // Configurar o ícone diretamente usando o ID
                        val categoriaImageView =
                            itemDespesa.findViewById<ImageView>(R.id.categoria_de_salario)
                        categoriaImageView.setImageResource(iconeResId)
                        categoriaImageView.setColorFilter(corImagemRedonda)

                        // Configurar a cor do círculo diretamente
                        val circuloImageView = itemDespesa.findViewById<ImageView>(R.id.Circulo_cor)
                        circuloImageView.setColorFilter(corCirculo)
                        itemDespesa.setOnClickListener {
                            // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                            // Por exemplo, para iniciar uma nova atividade:
                            val intent =
                                Intent(this, activity_tela_visualizar_despesas::class.java)
                            // Passar os campos da receita como extras
                            intent.putExtra("nome_despesa", despesa.getString("nome_despesa"))
                            intent.putExtra(
                                "categoria_escolhida_pelo_usuario",
                                despesa.getString("categoria_escolhida_pelo_usuario")
                            )
                            intent.putExtra("descricao", despesa.getString("descricao"))
                            intent.putExtra(
                                "nome_favorecido",
                                despesa.getString("nome_favorecido")
                            )
                            intent.putExtra("valor_ja_pago", despesa.getString("valor_ja_pago"))
                            intent.putExtra(
                                "valor_das_parcelas_calculado",
                                despesa.getDouble("valor_das_parcelas_calculado") ?: 0.0
                            )
                            intent.putExtra(
                                "data_efetuacao",
                                dataEfetuacao.time
                            ) // Enviar a data como um timestamp
                            intent.putExtra(
                                "iconeResId",
                                despesa.getLong("iconeResId")?.toInt() ?: 0
                            )
                            intent.putExtra(
                                "corImagemRedonda",
                                despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                            )
                            intent.putExtra(
                                "corCirculo",
                                despesa.getLong("corCirculo")?.toInt() ?: 0
                            )
                            intent.putExtra("despesa_id", idDespesa)
                            intent.putExtra("numero_de_parcelas", numeroParcelas)
                            intent.putExtra("parcela_paga", parcelasPagas)
                            startActivity(intent)
                        }

                        containerLayout.addView(itemDespesa)
                }
                else if(formaPagamento == "À vista" || formaPagamento == "Recorrente"){
                    val itemDespesa = inflater.inflate(R.layout.ultimos_registros, containerLayout, false)

                    // Configurar os elementos do itemDespesa
                    val nomeDespesaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                    val nomeCategoriaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemNomeCategoria)
                    val descricaoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                    val favorecidoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                    val valorParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.valorRegistro)
                    val proximaParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.dataRegistro)

                    nomeDespesaTextView.text = despesa.getString("nome_despesa")
                    nomeCategoriaTextView.text =
                        despesa.getString("categoria_escolhida_pelo_usuario")
                    descricaoTextView.text = despesa.getString("descricao")
                    favorecidoTextView.text = despesa.getString("nome_favorecido")
                    valorParcelaTextView.text = formatCurrencyValue(
                        despesa.getDouble("valor_despesa") ?: 0.0
                    )

                    // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                    val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dataFormatada = formatoData.format(dataEfetuacao)
                    proximaParcelaTextView.text = dataFormatada

                    // Recuperar os IDs inteiros dos ícones e cores
                    val iconeResId = despesa.getLong("iconeResId")?.toInt() ?: 0
                    val corImagemRedonda = despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                    val corCirculo = despesa.getLong("corCirculo")?.toInt() ?: 0

                    // Configurar o ícone diretamente usando o ID
                    val categoriaImageView =
                        itemDespesa.findViewById<ImageView>(R.id.categoria_de_salario)
                    categoriaImageView.setImageResource(iconeResId)
                    categoriaImageView.setColorFilter(corImagemRedonda)

                    // Configurar a cor do círculo diretamente
                    val circuloImageView = itemDespesa.findViewById<ImageView>(R.id.Circulo_cor)
                    circuloImageView.setColorFilter(corCirculo)
                    itemDespesa.setOnClickListener {
                        // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                        // Por exemplo, para iniciar uma nova atividade:
                        val intent =
                            Intent(this, activity_tela_visualizar_despesas::class.java)
                        // Passar os campos da receita como extras
                        intent.putExtra("nome_despesa", despesa.getString("nome_despesa"))
                        intent.putExtra("nome_despesa", despesa.getString("nome_despesa"))
                        intent.putExtra(
                            "categoria_escolhida_pelo_usuario",
                            despesa.getString("categoria_escolhida_pelo_usuario")
                        )
                        intent.putExtra("descricao", despesa.getString("descricao"))
                        intent.putExtra(
                            "nome_favorecido",
                            despesa.getString("nome_favorecido")
                        )
                        intent.putExtra("valor_ja_pago", despesa.getString("valor_ja_pago"))
                        intent.putExtra(
                            "valor_das_parcelas_calculado",
                            despesa.getDouble("valor_das_parcelas_calculado") ?: 0.0
                        )
                        intent.putExtra(
                            "data_efetuacao",
                            dataEfetuacao.time
                        ) // Enviar a data como um timestamp
                        intent.putExtra(
                            "iconeResId",
                            despesa.getLong("iconeResId")?.toInt() ?: 0
                        )
                        intent.putExtra(
                            "corImagemRedonda",
                            despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                        )
                        intent.putExtra(
                            "corCirculo",
                            despesa.getLong("corCirculo")?.toInt() ?: 0
                        )
                        intent.putExtra("despesa_id", idDespesa)
                        intent.putExtra("numero_de_parcelas", numeroParcelas)
                        intent.putExtra("parcela_paga", parcelasPagas)
                        intent.putExtra("opcao_recorrente", opcaoRecorrente)
                        startActivity(intent)
                    }

                    containerLayout.addView(itemDespesa)
                }
            }
            else{
                if (formaPagamento == "Mensal") {
                    val itemDespesa = inflater.inflate(R.layout.ultimos_registros, containerLayout, false)

                    // Configurar os elementos do itemDespesa
                    val nomeDespesaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                    val nomeCategoriaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemNomeCategoria)
                    val descricaoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                    val favorecidoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                    val valorParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.valorRegistro)
                    val proximaParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.dataRegistro)

                    nomeDespesaTextView.text = despesa.getString("nome_despesa")
                    nomeCategoriaTextView.text =
                        despesa.getString("categoria_escolhida_pelo_usuario")
                    descricaoTextView.text = despesa.getString("descricao")
                    favorecidoTextView.text = despesa.getString("nome_favorecido")
                    valorParcelaTextView.text = "Valor pendente"

                    // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                    val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dataFormatada = formatoData.format(dataEfetuacao)
                    proximaParcelaTextView.text = dataFormatada

                    // Recuperar os IDs inteiros dos ícones e cores
                    val iconeResId = despesa.getLong("iconeResId")?.toInt() ?: 0
                    val corImagemRedonda = despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                    val corCirculo = despesa.getLong("corCirculo")?.toInt() ?: 0

                    // Configurar o ícone diretamente usando o ID
                    val categoriaImageView =
                        itemDespesa.findViewById<ImageView>(R.id.categoria_de_salario)
                    categoriaImageView.setImageResource(iconeResId)
                    categoriaImageView.setColorFilter(corImagemRedonda)

                    // Configurar a cor do círculo diretamente
                    val circuloImageView = itemDespesa.findViewById<ImageView>(R.id.Circulo_cor)
                    circuloImageView.setColorFilter(corCirculo)
                    itemDespesa.setOnClickListener {
                        // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                        // Por exemplo, para iniciar uma nova atividade:
                        val intent =
                            Intent(this, activity_tela_visualizar_despesas::class.java)
                        // Passar os campos da receita como extras
                        intent.putExtra("nome_despesa", despesa.getString("nome_despesa"))
                        intent.putExtra(
                            "categoria_escolhida_pelo_usuario",
                            despesa.getString("categoria_escolhida_pelo_usuario")
                        )
                        intent.putExtra("descricao", despesa.getString("descricao"))
                        intent.putExtra(
                            "nome_favorecido",
                            despesa.getString("nome_favorecido")
                        )
                        intent.putExtra("valor_ja_pago", despesa.getString("valor_ja_pago"))
                        intent.putExtra(
                            "valor_das_parcelas_calculado",
                            despesa.getDouble("valor_das_parcelas_calculado") ?: 0.0
                        )
                        intent.putExtra(
                            "data_efetuacao",
                            dataEfetuacao.time
                        ) // Enviar a data como um timestamp
                        intent.putExtra(
                            "iconeResId",
                            despesa.getLong("iconeResId")?.toInt() ?: 0
                        )
                        intent.putExtra(
                            "corImagemRedonda",
                            despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                        )
                        intent.putExtra(
                            "corCirculo",
                            despesa.getLong("corCirculo")?.toInt() ?: 0
                        )
                        intent.putExtra("despesa_id", idDespesa)
                        intent.putExtra("numero_de_parcelas", numeroParcelas)
                        intent.putExtra("parcela_paga", parcelasPagas)
                        startActivity(intent)
                    }

                    containerLayout.addView(itemDespesa)
                }
                else if(formaPagamento == "À vista" || formaPagamento == "Recorrente"){
                    val itemDespesa = inflater.inflate(R.layout.ultimos_registros, containerLayout, false)

                    // Configurar os elementos do itemDespesa
                    val nomeDespesaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                    val nomeCategoriaTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemNomeCategoria)
                    val descricaoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                    val favorecidoTextView =
                        itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                    val valorParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.valorRegistro)
                    val proximaParcelaTextView =
                        itemDespesa.findViewById<TextView>(R.id.dataRegistro)

                    nomeDespesaTextView.text = despesa.getString("nome_despesa")
                    nomeCategoriaTextView.text =
                        despesa.getString("categoria_escolhida_pelo_usuario")
                    descricaoTextView.text = despesa.getString("descricao")
                    favorecidoTextView.text = despesa.getString("nome_favorecido")
                    valorParcelaTextView.text = "Valor pendente"

                    // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                    val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dataFormatada = formatoData.format(dataEfetuacao)
                    proximaParcelaTextView.text = dataFormatada

                    // Recuperar os IDs inteiros dos ícones e cores
                    val iconeResId = despesa.getLong("iconeResId")?.toInt() ?: 0
                    val corImagemRedonda = despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                    val corCirculo = despesa.getLong("corCirculo")?.toInt() ?: 0

                    // Configurar o ícone diretamente usando o ID
                    val categoriaImageView =
                        itemDespesa.findViewById<ImageView>(R.id.categoria_de_salario)
                    categoriaImageView.setImageResource(iconeResId)
                    categoriaImageView.setColorFilter(corImagemRedonda)

                    // Configurar a cor do círculo diretamente
                    val circuloImageView = itemDespesa.findViewById<ImageView>(R.id.Circulo_cor)
                    circuloImageView.setColorFilter(corCirculo)
                    itemDespesa.setOnClickListener {
                        // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                        // Por exemplo, para iniciar uma nova atividade:
                        val intent =
                            Intent(this, activity_tela_visualizar_despesas::class.java)
                        // Passar os campos da receita como extras
                        intent.putExtra("nome_despesa", despesa.getString("nome_despesa"))
                        intent.putExtra(
                            "categoria_escolhida_pelo_usuario",
                            despesa.getString("categoria_escolhida_pelo_usuario")
                        )
                        intent.putExtra("descricao", despesa.getString("descricao"))
                        intent.putExtra(
                            "nome_favorecido",
                            despesa.getString("nome_favorecido")
                        )
                        intent.putExtra("valor_ja_pago", despesa.getString("valor_ja_pago"))
                        intent.putExtra(
                            "valor_das_parcelas_calculado",
                            despesa.getDouble("valor_das_parcelas_calculado") ?: 0.0
                        )
                        intent.putExtra(
                            "data_efetuacao",
                            dataEfetuacao.time
                        ) // Enviar a data como um timestamp
                        intent.putExtra(
                            "iconeResId",
                            despesa.getLong("iconeResId")?.toInt() ?: 0
                        )
                        intent.putExtra(
                            "corImagemRedonda",
                            despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                        )
                        intent.putExtra(
                            "corCirculo",
                            despesa.getLong("corCirculo")?.toInt() ?: 0
                        )
                        intent.putExtra("despesa_id", idDespesa)
                        intent.putExtra("numero_de_parcelas", numeroParcelas)
                        intent.putExtra("parcela_paga", parcelasPagas)
                        intent.putExtra("opcao_recorrente", opcaoRecorrente)
                        startActivity(intent)
                    }

                    containerLayout.addView(itemDespesa)
                }
            }
        }
    }

    private fun exibirUltimosReceitas(
        documentosUltimosReceitas: List<DocumentSnapshot>,
        mesSelecionado: Int,
        anoSelecionado: Int,
    ) {
        val containerLayout = findViewById<LinearLayout>(R.id.layoutUltimosRegistrosReceitas)
        val inflater = LayoutInflater.from(this)
        containerLayout.removeAllViews() // Remove todas as visualizações antigas

        for (receita in documentosUltimosReceitas) {

            val ganhojaRecebido = receita.getString("recebido_ou_nao")
            val idReceita = receita.getString("receita_id")

            val dataEfetuacaoTimestamp2 = receita.get("data_efetuacao_receitas") as com.google.firebase.Timestamp
            val dataEfetuacao2 = dataEfetuacaoTimestamp2.toDate()

            val calendar2 = Calendar.getInstance()
            calendar2.time = dataEfetuacao2

            if (ganhojaRecebido == "Recebido") {

                val itemReceita = inflater.inflate(R.layout.ultimos_registros, containerLayout, false)

                // Configurar os elementos do itemDespesa
                val nomeDespesaTextView = itemReceita.findViewById<TextView>(R.id.itemNomeDespesa)
                val nomeCategoriaTextView =
                    itemReceita.findViewById<TextView>(R.id.itemNomeCategoria)
                val descricaoTextView = itemReceita.findViewById<TextView>(R.id.itemDescricao)
                val favorecidoTextView = itemReceita.findViewById<TextView>(R.id.itemFavorecido)
                val valorParcelaTextView = itemReceita.findViewById<TextView>(R.id.valorRegistro)
                val proximaParcelaTextView = itemReceita.findViewById<TextView>(R.id.dataRegistro)

                nomeDespesaTextView.text = receita.getString("nome_da_receita")
                nomeCategoriaTextView.text = receita.getString("categoria_escolhida_pelo_usuario_receitas")
                descricaoTextView.text = receita.getString("descricao_da_receita")
                favorecidoTextView.text = receita.getString("nome_favorecido_receita")
                valorParcelaTextView.text = formatCurrencyValue(receita.getDouble("valor_da_receita") ?: 0.0)
                valorParcelaTextView.setTextColor(ContextCompat.getColor(this, R.color.green)) // Certifique-se de ter uma cor chamada "verde" definida em seus recursos

                // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dataFormatada = formatoData.format(dataEfetuacao2)
                proximaParcelaTextView.text = dataFormatada

                // Recuperar os IDs inteiros dos ícones e cores
                val iconeResId = receita.getLong("iconeReceita")?.toInt() ?: 0
                val corImagemRedonda = receita.getLong("corImagemReceita")?.toInt() ?: 0
                val corCirculo = receita.getLong("corCirculoReceita")?.toInt() ?: 0

                // Configurar o ícone diretamente usando o ID
                val categoriaImageView = itemReceita.findViewById<ImageView>(R.id.categoria_de_salario)
                categoriaImageView.setImageResource(iconeResId)
                categoriaImageView.setColorFilter(corImagemRedonda)

                // Configurar a cor do círculo diretamente
                val circuloImageView = itemReceita.findViewById<ImageView>(R.id.Circulo_cor)
                circuloImageView.setColorFilter(corCirculo)

                itemReceita.setOnClickListener {
                    // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                    // Por exemplo, para iniciar uma nova atividade:
                    val intent = Intent(this, activity_tela_visualizar_registro::class.java)
                    // Passar os campos da receita como extras
                    intent.putExtra("nome_da_receita", receita.getString("nome_da_receita"))
                    intent.putExtra("receita_id", idReceita)
                    intent.putExtra("categoria_da_receita", receita.getString("categoria_escolhida_pelo_usuario_receitas"))
                    intent.putExtra("descricao_da_receita", receita.getString("descricao_da_receita"))
                    intent.putExtra("nome_favorecido_receita", receita.getString("nome_favorecido_receita"))
                    intent.putExtra("recebido_ou_nao", receita.getString("recebido_ou_nao"))
                    intent.putExtra("valor_da_receita", receita.getDouble("valor_da_receita") ?: 0.0)
                    intent.putExtra("data_efetuacao_receita", dataEfetuacao2.time) // Enviar a data como um timestamp
                    intent.putExtra("iconeReceita", receita.getLong("iconeReceita")?.toInt() ?: 0)
                    intent.putExtra("corImagemReceita", receita.getLong("corImagemReceita")?.toInt() ?: 0)
                    intent.putExtra("corCirculoReceita", receita.getLong("corCirculoReceita") ?.toInt() ?: 0)
                    startActivity(intent)
                }
                containerLayout.addView(itemReceita)
            }
            else{
                val itemReceita = inflater.inflate(R.layout.ultimos_registros, containerLayout, false)
                // Configurar os elementos do itemDespesa
                val nomeDespesaTextView = itemReceita.findViewById<TextView>(R.id.itemNomeDespesa)
                val nomeCategoriaTextView =
                    itemReceita.findViewById<TextView>(R.id.itemNomeCategoria)
                val descricaoTextView = itemReceita.findViewById<TextView>(R.id.itemDescricao)
                val favorecidoTextView = itemReceita.findViewById<TextView>(R.id.itemFavorecido)
                val valorParcelaTextView = itemReceita.findViewById<TextView>(R.id.valorRegistro)
                val proximaParcelaTextView = itemReceita.findViewById<TextView>(R.id.dataRegistro)

                nomeDespesaTextView.text = receita.getString("nome_da_receita")
                nomeCategoriaTextView.text = receita.getString("categoria_escolhida_pelo_usuario_receitas")
                descricaoTextView.text = receita.getString("descricao_da_receita")
                favorecidoTextView.text = receita.getString("nome_favorecido_receita")
                valorParcelaTextView.text = "Valor pendente"
                valorParcelaTextView.setTextColor(ContextCompat.getColor(this, R.color.green)) // Certifique-se de ter uma cor chamada "verde" definida em seus recursos

                // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dataFormatada = formatoData.format(dataEfetuacao2)
                proximaParcelaTextView.text = dataFormatada

                // Recuperar os IDs inteiros dos ícones e cores
                val iconeResId = receita.getLong("iconeReceita")?.toInt() ?: 0
                val corImagemRedonda = receita.getLong("corImagemReceita")?.toInt() ?: 0
                val corCirculo = receita.getLong("corCirculoReceita")?.toInt() ?: 0

                // Configurar o ícone diretamente usando o ID
                val categoriaImageView = itemReceita.findViewById<ImageView>(R.id.categoria_de_salario)
                categoriaImageView.setImageResource(iconeResId)
                categoriaImageView.setColorFilter(corImagemRedonda)

                // Configurar a cor do círculo diretamente
                val circuloImageView = itemReceita.findViewById<ImageView>(R.id.Circulo_cor)
                circuloImageView.setColorFilter(corCirculo)

                itemReceita.setOnClickListener {
                    // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                    // Por exemplo, para iniciar uma nova atividade:
                    val intent = Intent(this, activity_tela_visualizar_registro::class.java)
                    // Passar os campos da receita como extras
                    intent.putExtra("nome_da_receita", receita.getString("nome_da_receita"))
                    intent.putExtra("receita_id", idReceita)
                    intent.putExtra("categoria_da_receita", receita.getString("categoria_escolhida_pelo_usuario_receitas"))
                    intent.putExtra("descricao_da_receita", receita.getString("descricao_da_receita"))
                    intent.putExtra("nome_favorecido_receita", receita.getString("nome_favorecido_receita"))
                    intent.putExtra("recebido_ou_nao", receita.getString("recebido_ou_nao"))
                    intent.putExtra("valor_da_receita", receita.getDouble("valor_da_receita") ?: 0.0)
                    intent.putExtra("data_efetuacao_receita", dataEfetuacao2.time) // Enviar a data como um timestamp
                    intent.putExtra("iconeReceita", receita.getLong("iconeReceita")?.toInt() ?: 0)
                    intent.putExtra("corImagemReceita", receita.getLong("corImagemReceita")?.toInt() ?: 0)
                    intent.putExtra("corCirculoReceita", receita.getLong("corCirculoReceita") ?.toInt() ?: 0)
                    startActivity(intent)
                }
                containerLayout.addView(itemReceita)
            }
        }
    }

    private fun carregarUltimosRegistros() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val db = FirebaseFirestore.getInstance()

            val despesasRef = db.collection("despesas")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp_registro", Query.Direction.DESCENDING)


            val receitasRef = db.collection("receitas")
                .whereEqualTo("userId", uid)
                .orderBy("data_efetuacao_receitas", Query.Direction.DESCENDING) // Ordenar por data de forma decrescente

            receitasRef.get()
                .addOnSuccessListener { documents ->
                    val documentosUltimosReceitas = documents.documents.take(3)

                    exibirUltimosReceitas(documentosUltimosReceitas, mesSelecionado, anoSelecionado)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Falha ao obter as receitas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

            despesasRef.get()
                .addOnSuccessListener { documents ->
                    val documentosUltimosRegistros = documents.documents.take(3) // Obter a lista de documentos despesas

                    exibirUltimosRegistros(documentosUltimosRegistros, mesSelecionado, anoSelecionado)
                }
                .addOnFailureListener { exception ->
                    // Lidar com falha na obtenção das despesas
                    Toast.makeText(this, "Falha ao obter as despesas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun atualizarValoresReceitasDespesas() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val db = FirebaseFirestore.getInstance()

            // Referência à coleção "receitas" do usuário atual
            val receitasRef = db.collection("receitas")
                .whereEqualTo("userId", uid)

            // Referência à coleção "despesas" do usuário atual
            val despesasRef = db.collection("despesas")
                .whereEqualTo("userId", uid)

            // Calcular o total das receitas
            receitasRef.get()
                .addOnSuccessListener { documents ->
                    var totalReceitas = 0.0

                    for (document in documents) {
                        val dataEfetuacaoTimestamp = document.get("data_efetuacao_receitas")
                        val valorReceita = document.get("valor_da_receita")
                        val ganhoJaRecebido = document.getString("recebido_ou_nao")

                        if (ganhoJaRecebido == "Recebido") {
                            if (valorReceita is Number && dataEfetuacaoTimestamp is com.google.firebase.Timestamp) {
                                val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                                val calendar = Calendar.getInstance()
                                calendar.time = dataEfetuacao

                                // Verificar se a receita está no mês e ano selecionados
                                if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(Calendar.YEAR) == anoSelecionado) {
                                    totalReceitas += valorReceita.toDouble()
                                }
                            }
                        }
                    }
                    // Exibir o total de receitas no TextView
                    val valorReceitasTextView = findViewById<TextView>(R.id.valor_receitas)
                    valorReceitasTextView.text = formatCurrencyValue(totalReceitas)

                    // Calcular o total das despesas
                    despesasRef.get()
                        .addOnSuccessListener { documents ->
                            var totalDespesas = 0.0

                            for (document in documents) {
                                val valorDespesa = document.getDouble("valor_despesa") ?: 0.0
                                val dataEfetuacaoTimestamp = document.get("data_efetuacao")

                                val formaPagamento = document.getString("forma_pagamento")
                                val opcaoRecorrente = document.getString("opcao_recorrente")
                                val valorMensalDespesa = document.getDouble("valor_das_parcelas_calculado") ?: 0.0
                                val numeroParcelas = document.getLong("numero_de_parcelas") ?: 1 // Supondo que 1 é o valor padrão caso não esteja presente
                                val tempoRecorrente = document.getLong("tempo_recorrente") ?: 1
                                val valorJaPago = document.getString("valor_ja_pago")

                                if (valorJaPago == "Pago") {
                                    if (dataEfetuacaoTimestamp is com.google.firebase.Timestamp) {
                                        val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                                        val calendar = Calendar.getInstance()
                                        calendar.time = dataEfetuacao

                                        // Verificar se a despesa está no mês e ano selecionados
                                        if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(Calendar.YEAR) == anoSelecionado) {
                                            if (formaPagamento == "À vista" || formaPagamento == "Recorrente") {
                                                totalDespesas += valorDespesa
                                            } else if (formaPagamento == "Mensal") {
                                                // Se for pagamento mensal, calcular apenas para o mês atual
                                                totalDespesas += valorMensalDespesa
                                            }
                                        }
                                    }
                                }
                            }
                            // Exibir o total de despesas no TextView
                            val valorDespesasTextView = findViewById<TextView>(R.id.valor_despesas)
                            valorDespesasTextView.text = formatCurrencyValue(totalDespesas)

                            // Calcular o saldo e atualizar o TextView valorAtual
                            val valorAtualTextView = findViewById<TextView>(R.id.valorAtual)
                            valorAtualTextView.text = formatCurrencyValue(totalReceitas - totalDespesas)
                        }
                        .addOnFailureListener { exception ->
                            // Lidar com falha na obtenção das despesas
                            Toast.makeText(this, "Falha ao obter as despesas: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    // Lidar com falha na obtenção das receitas
                    Toast.makeText(this, "Falha ao obter as receitas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Função para formatar um valor monetário como uma string no formato R$ 0,00
    private fun formatCurrencyValue(value: Double): String {
        return String.format("R$ %.2f", value)
    }
    private fun calcularSaldoAtual(totalReceitas: Double, totalDespesas: Double): Double {
        return totalReceitas - totalDespesas
    }
    private fun atualizarMesSelecionado(ano: Int, mes: Int) {
        val mesAtual = mesesAbreviados[mes]
        val texto = "$mesAtual $ano"
        mesSelecionadoTextView.text = texto
    }
    private fun atualizarMesSelecionadoNoDialog(mes: Int, ano: Int) {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
        sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

        // Atualiza o mês selecionado com o valor escolhido no diálogo
        mesSelecionado = mes
        anoSelecionado = ano

        // Obter o ano atual
        val calendar = Calendar.getInstance()
        val anoAtual = calendar.get(Calendar.YEAR)

        // Atualizar a TextView do mês selecionado na tela principal
        atualizarMesSelecionado(anoSelecionado, mesSelecionado)

        // Salvar o valor do mês nas preferências compartilhadas
        val editor = sharedPreferences.edit()
        editor.putInt("mesSelecionado", mesSelecionado)
        editor.putInt("anoSelecionado", anoSelecionado)
        editor.apply()

        // Chame a função que atualiza os valores de receitas e despesas com o novo mês selecionado
        atualizarValoresReceitasDespesas()
    }

    private fun mostrarDialogMiniCalendario() {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
        sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

        val dialogView = layoutInflater.inflate(R.layout.dialog_mini_calendario, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Selecione o ano e mês")

        val anoPicker = dialogView.findViewById<NumberPicker>(R.id.anoPicker)
        val mesPicker = dialogView.findViewById<NumberPicker>(R.id.mesPicker)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        // Configurar o NumberPicker para o ano
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val anosExibidos = 71 // Quantidade total de anos exibidos no NumberPicker (de -5 a +5)
        anoPicker.minValue = anoAtual - (anosExibidos / 2) // Exibir a metade dos anos antes do ano atual
        anoPicker.maxValue = anoAtual + (anosExibidos / 2) // Exibir a metade dos anos após o ano atual
        anoPicker.value = anoSelecionado // Selecionar o ano que estava previamente selecionado

        // Configurar o NumberPicker para os meses
        mesPicker.minValue = 0 // Janeiro (índice 0)
        mesPicker.maxValue = mesesAbreviados.size - 1 // Dezembro (índice 11)
        mesPicker.displayedValues = mesesAbreviados // Mostrar os meses abreviados
        mesPicker.value = mesSelecionado // Selecionar o mês atual

        val dialog = dialogBuilder.create()

        btnConfirmar.setOnClickListener {
            val anoSelecionado = anoPicker.value
            val mesSelecionado = mesPicker.value
            val categoria = intent.getStringExtra("categoria_selecionada")


            val iconeResId = intent.getIntExtra("icone_res_id", 0)
            val corImagemRedonda = intent.getIntExtra("cor_imagem_redonda", 0)
            val corCirculo = intent.getIntExtra("circulo_cor", 0)

            // Faça o que desejar com o anoSelecionado e mesSelecionado
            // Por exemplo, atualize o mês selecionado na tela principal

            atualizarMesSelecionado(anoSelecionado, mesSelecionado)

            atualizarMesSelecionadoNoDialog(mesSelecionado, anoSelecionado)

            exibirDespesas(documentosDespesas, mesSelecionado, anoSelecionado)

            atualizarValoresReceitasDespesas()

            carregarDespesas()

            exibirDespesasRecorrentes(documentosDespesasRecorrentes, mesSelecionado, anoSelecionado)

            carregarDespesasRecorrentes()

            exibirUltimosRegistros(documentosUltimosRegistros, mesSelecionado, anoSelecionado)
            exibirUltimosReceitas(documentosUltimosReceitas, mesSelecionado, anoSelecionado)

            carregarUltimosRegistros()


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

    private fun abrirTelaRegistros(){
        val intent = Intent(this, activity_tela_de_todos_os_registros::class.java)
        startActivity(intent)
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
