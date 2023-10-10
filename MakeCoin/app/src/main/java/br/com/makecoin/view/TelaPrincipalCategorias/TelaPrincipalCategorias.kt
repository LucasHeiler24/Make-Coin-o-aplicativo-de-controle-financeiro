package br.com.makecoin.view.TelaPrincipalCategorias

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.com.makecoin.R
import br.com.makecoin.view.TelaDasCategorias.activity_tela_das_categorias_despesas
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import com.google.firebase.Timestamp
import java.util.*

class TelaPrincipalCategorias : AppCompatActivity() {
    private var selectedDateEfetuacao: Calendar? = null
    private var selectedDateVencimento: Calendar? = null
    private lateinit var registrarDataHojeDataEfetuacaoButton: Button
    private lateinit var registrarDataHojeDataVencimentoButton: Button
    private var formattedValue: Double? = null
    private val data = HashMap<String, Any>()
    private var formaPagamento: String = "" // Inicializa com uma string vazia
    private var isPagarAVistaSelected = false // Variável para rastrear o estado selecionado
    private var isPagarEmMensalVisible = false
    private var valorDespesaForMensalCalculation: Double = 0.0
    private var valorMensal: Double = 0.0
    private var valorDespesaMensal: Double = 0.0
    private var valorDespesaRecorrente: Double = 0.0
    private var numeroParcelas: Int = 0
    private lateinit var toggleSwitch: Switch
    private var formattedValueFixa: Double = 0.0
    private var categoriaSelecionada: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var valorDaDespesaEditText: EditText
    private lateinit var descricaoDaDespesaEditText: EditText
    private var dataEfetuacaoInMillis: Long = 0
    private var dataVencimentoInMillis: Long = 0
    private var isPagamentoMensalSelecionado = false
    private var valorDespesaArmazenarRecorrente: Double = 0.0
    private var isPagamentoRecorrenteSelecionado: Boolean = false
    private var valorArmazenadoMensal: Double = 0.0
    private var valorArmazenadoCertoMensal: Double = 0.0
    private var recurrenceOption: String = ""
    private var recurrencePeriod: Int = 0
    private var calculatedRecurringExpense: Double = 0.0
    private val nomesDespesasList = mutableListOf<String>()
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private var PagoouNaoPago: String = ""
    private lateinit var autoCompleteTextViewFavorecido: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_principal_categorias)
        FirebaseApp.initializeApp(this)
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)


        // Obtenha as referências dos campos de entrada
        valorDaDespesaEditText = findViewById(R.id.valor_da_despesa)
        val textInputLayout: TextInputLayout = findViewById(R.id.Nome_da_despesa)
        autoCompleteTextView = textInputLayout.findViewById(R.id.auto_completa_texto)
        val textInputLayout2: TextInputLayout = findViewById(R.id.Nome_do_favorecido)
        autoCompleteTextViewFavorecido = textInputLayout2.findViewById(R.id.auto_completa_texto_favorecidos)
        descricaoDaDespesaEditText = findViewById(R.id.Descricao_da_despesa)

        bottomNavigationView.itemBackgroundResource = R.drawable.bottom_navigation_item_background
        val voltarTelaPrincipal = findViewById<ImageView>(R.id.Voltar_tela_inicial)
        voltarTelaPrincipal.setOnClickListener {
            abrirTelaPrincipal()
        }
        val moverParaCategoriasDespesas = findViewById<ImageView>(R.id.Ir_para_tela_escolher_categorias_despesas)
        moverParaCategoriasDespesas.setOnClickListener {
            salvarValoresAoSelecionarCategoria()
            abrirTelaDasCategoriasDespesas()
        }
        val abrirPagarDespesasRecorrentes = findViewById<Button>(R.id.pagar_recorrente)
        abrirPagarDespesasRecorrentes.setOnClickListener{
            if (isPagarAVistaSelected) {
                // Botão "À vista" está selecionado, mostre um Toast
                Toast.makeText(this, "Desmarque o botão À vista primeiro", Toast.LENGTH_SHORT).show()
            } else {
                showRecurrenceOptionsDialog()
            }
        }
        toggleSwitch = findViewById(R.id.toggleSwitch)
        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                PagoouNaoPago = "Pago"
            } else {
                PagoouNaoPago = "Não pago"
            }
            // Salvar o estado do toggleSwitch no SharedPreferences
            sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("valor_ja_pago", isChecked)
            editor.apply()
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.menu_receitas-> {
                    // Abrir a Tela 1
                    val intent1 = Intent(this, activity_tela_principal_categorias_receitas::class.java)
                    startActivity(intent1)
                    true
                }
                else -> false
            }
        }

        // Botão "Outros" para abrir o DatePickerDialog
        val abrirCalendarioDataEfetuacaoButton = findViewById<Button>(R.id.abrir_calendario_data_efetuacao)
        abrirCalendarioDataEfetuacaoButton.setOnClickListener {
            openDatePickerDialog() // Chama o método para exibir o DatePickerDialog
        }

        // Botão "Hoje" para definir a data como hoje
        registrarDataHojeDataEfetuacaoButton = findViewById<Button>(R.id.registrar_hoje_data_efetuacao)
        registrarDataHojeDataEfetuacaoButton.setOnClickListener {

            selectedDateEfetuacao = Calendar.getInstance()
            updateDataEfetuacaoTextView(selectedDateEfetuacao)
            dataEfetuacaoInMillis = selectedDateEfetuacao?.timeInMillis ?: 0

            toggleSwitch.isChecked = true

            // Esconda os botões "Hoje" e "Outros" após definir a data manualmente
            findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.GONE
            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE

            // Exiba o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE

            // Exiba o TextView com a data selecionada
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE

            val editor = sharedPreferences.edit()
            editor.putLong("data_efetuacao", dataEfetuacaoInMillis)
            editor.apply()
        }

        // Botão "Limpar Data" para limpar a data selecionada
        val limparDataButton = findViewById<Button>(R.id.limpar_data_button)
        limparDataButton.setOnClickListener {
            selectedDateEfetuacao = null

            toggleSwitch.isChecked = false

            // Exiba os botões "Hoje" e "Outros" novamente
            findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.VISIBLE
            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.VISIBLE

            // Esconda o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button).visibility = View.GONE

            // Esconda o TextView com a data selecionada
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE

            // Salve a data selecionada no SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putLong("data_efetuacao", dataEfetuacaoInMillis)
            editor.apply()
        }
        // Defina a visibilidade do botão "Limpar Data" com base no estado da variável selectedDate
        if (selectedDateEfetuacao != null) {
            limparDataButton.visibility = View.VISIBLE
            updateDataEfetuacaoTextView(selectedDateEfetuacao)
        } else {
            limparDataButton.visibility = View.GONE
        }
        // Obtenha a referência para o EditText
        val valorDaDespesaEditText = findViewById<EditText>(R.id.valor_da_despesa)

        // Crie um SpannableString com o hint e defina a cor vermelha
        val hint = "R$ 0,00"
        val spannableString = SpannableString(hint)
        spannableString.setSpan(ForegroundColorSpan(Color.WHITE), 0, hint.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        // Defina o SpannableString como o hint do EditText
        valorDaDespesaEditText.hint = spannableString

        val registrarDespesaButton = findViewById<Button>(R.id.Registrar_despesa)
        registrarDespesaButton.setOnClickListener {
            salvarDespesaNoFirestore()
        }
        val pagarAVistaButton = findViewById<Button>(R.id.pagar_a_vista)
        // Configure o OnClickListener para o botão "À vista"
        pagarAVistaButton.setOnClickListener {
            registrarDespesaAVista()
        }
        // Botão "Pagar Mensal" para calcular o pagamento mensal
        val pagarEmMensalButton = findViewById<Button>(R.id.pagar_mensal)
        pagarEmMensalButton.setOnClickListener {
            if (isPagarAVistaSelected) {
                // Botão "À vista" está selecionado, mostre um Toast
                Toast.makeText(this, "Desmarque o botão À vista primeiro", Toast.LENGTH_SHORT).show()
            }else {
                pagamentoMensal()
            }
        }
        // Obter os dados da categoria selecionada da Activity anterior
        val categoria = intent.getStringExtra("categoria_selecionada")


        val iconeResId = intent.getIntExtra("icone_res_id", 0)
        val corImagemRedonda = intent.getIntExtra("cor_imagem_redonda", 0)
        val corCirculo = intent.getIntExtra("circulo_cor", 0)

        // Agora você tem as informações da categoria selecionada
        // Faça o que precisar com essas informações, por exemplo:
        // - Defina o ícone e a cor da imagem redonda na tela de registro de receitas
        val categoriaImageView = findViewById<ImageView>(R.id.categoria_de_salario)
        categoriaImageView.setImageResource(iconeResId)

        // Defina a cor da imagem redonda
        categoriaImageView.setColorFilter(corImagemRedonda)

        // - Defina a cor do círculo
        val circuloImageView = findViewById<ImageView>(R.id.Circulo_cor)
        circuloImageView.setColorFilter(corCirculo)
        DadosCategoriasTemporarios.categoriaSelecionada = categoria


        // Verifique se há uma categoria selecionada pela Activity anterior
        if (!categoria.isNullOrEmpty()) {
            categoriaSelecionada = true
            val categoriaSelecionada = findViewById<TextView>(R.id.categoria_salario)
            categoriaSelecionada.text = categoria
        }

        valorDaDespesaEditText.requestFocus()

        // Crie um Handler para postar a exibição do teclado depois de um pequeno atraso
        Handler().postDelayed({
            // Abra o teclado
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(valorDaDespesaEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)  // Aguarde 200 milissegundos antes de exibir o teclado

        carregarNomesDespesasDoUsuario()
        carregarNomesFavorecidosDoUsuario()
        exibirDataSalvaNoSharedPreferences()
    }
    object DadosCategoriasTemporarios {
        var categoriaSelecionada: String? = ""
    }
    override fun onPause() {
        super.onPause()
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        // Salvar o estado atual do toggleSwitch no SharedPreferences
        val isChecked = toggleSwitch.isChecked
        val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("valor_ja_pago", isChecked)
        editor.apply()
    }
    override fun onResume() {
        super.onResume()
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
        val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)
        val isPago = sharedPreferences.getBoolean("valor_ja_pago", false)
        toggleSwitch.isChecked = isPago

        if (selectedDateEfetuacao != null) {
            findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.GONE
            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE
            findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
            updateDataEfetuacaoTextView(selectedDateEfetuacao)

            // Verificar se o botão "à vista" está selecionado e salvar a data efetuação no SharedPreferences
            if (isPagarAVistaSelected) {
                val editor = sharedPreferences.edit()
                editor.putLong("data_efetuacao", selectedDateEfetuacao?.timeInMillis ?: 0)
                editor.apply()
            }
        }
        exibirDataSalvaNoSharedPreferences()

        // Verificar o estado anterior do botão "à vista" (caso tenha sido selecionado anteriormente)
        isPagarAVistaSelected = sharedPreferences.getBoolean("is_pagar_a_vista_selected", false)
        val pagarAVistaButton = findViewById<Button>(R.id.pagar_a_vista)
        val corBotaoPadrao = ContextCompat.getColor(this, R.color.colorButtonAVista)
        val corBotaoDesmarcado = ContextCompat.getColor(this, R.color.colorButtonPadrao)

        if (isPagarAVistaSelected) {
            // O botão "à vista" estava selecionado anteriormente
            pagarAVistaButton.setBackgroundColor(corBotaoDesmarcado)
            if (selectedDateEfetuacao != null){
                findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE
                findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
            }
            else{
                findViewById<Button>(R.id.limpar_data_button).visibility = View.GONE
                findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE
            }
            formaPagamento = "À vista"
            findViewById<Button>(R.id.pagar_mensal).visibility = View.GONE
            findViewById<Button>(R.id.pagar_recorrente).visibility = View.GONE
        } else {
            // O botão "à vista" não estava selecionado anteriormente
            pagarAVistaButton.setBackgroundColor(corBotaoPadrao)
            formaPagamento = ""
        }

        // Recuperar o valor mensal do SharedPreferences
        valorMensal = sharedPreferences.getFloat("valor_armazenado_certo_mensal", 0.0F).toDouble()
        // Exibir o valor mensal no TextView na tela principal, se houver um valor salvo
        if (valorMensal > 0) {
            val mensalTextView = findViewById<TextView>(R.id.resultado_mensal_textview)
            mensalTextView.text = formatCurrencyValue(valorMensal)
            mensalTextView.visibility = View.VISIBLE

            // Esconde os botões de pagamento
            hideBotoesPagamento()
            findViewById<Button>(R.id.limpar_pagamento_button).visibility = View.VISIBLE
            if (selectedDateEfetuacao != null){
                findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.GONE
                findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE
            }
            else{
                findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.VISIBLE
                findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.VISIBLE
            }

            formaPagamento = "Mensal"
        } else {
            // Se não houver valor salvo, mostra os botões de pagamento
            showBotoesPagamento()
            findViewById<Button>(R.id.limpar_pagamento_button).visibility = View.GONE
        }

        // Recuperar o valor recorrente e a informação sobre a forma de pagamento recorrente selecionada do SharedPreferences
        val valorRecorrenteSalvo = sharedPreferences.getFloat("valor_despesa_armazenar_recorrente", 0.0F)
        isPagamentoRecorrenteSelecionado = sharedPreferences.getBoolean("is_pagamento_recorrente_selecionado", false)

        // Exibir o valor recorrente no TextView se a forma de pagamento recorrente estiver selecionada
        if (isPagamentoRecorrenteSelecionado) {
            val resultadoRecorrenteTextView = findViewById<TextView>(R.id.resultado_recorrente_textview)
            resultadoRecorrenteTextView.text = String.format(Locale.US, "R$ %.2f", valorRecorrenteSalvo.toDouble())
            resultadoRecorrenteTextView.visibility = View.VISIBLE
            val mensalTextView = findViewById<TextView>(R.id.resultado_mensal_textview)
            mensalTextView.text = formatCurrencyValue(valorMensal)
            mensalTextView.visibility = View.GONE
            // Exibir o botão de limpar
            findViewById<Button>(R.id.limpar_pagamento_button).visibility = View.VISIBLE
            if (selectedDateEfetuacao != null){
                findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.GONE
                findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE
            }
            else{
                findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.VISIBLE
                findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.VISIBLE
            }
            // Esconder os botões de seleção de forma de pagamento
            hideBotoesPagamentoSection()
            formaPagamento = "Recorrente"
        } else {
            // Caso contrário, se não estiver selecionado, volte à exibição padrão
            findViewById<TextView>(R.id.resultado_recorrente_textview).visibility = View.GONE

        }

        updateFormaPagamento()

    }

    // Função para salvar os dados da despesa no Firestore com base no número de parcelas
    private fun salvarDespesaNoFirestore() {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
        val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

        Log.d("MyApp", "Iniciando salvarDespesaNoFirestore")

        // Obter os valores inseridos nos campos
        val valorDespesaText = valorDaDespesaEditText.text.toString()

        // Verificar se o valor da despesa é válido
        if (valorDespesaText.isEmpty()) {
            Toast.makeText(this, "Insira um valor para a despesa", Toast.LENGTH_SHORT).show()
            return
        }

        // Converter o valor da despesa de String para Double
        val formattedValue = try {
            valorDespesaText.replace("R", "")
                .replace("$", "")
                .replace(" ", "")
                .replace(",", ".")
                .toDouble()
        } catch (e: NumberFormatException) {
            null
        }

        // Verificar se a conversão foi bem-sucedida e se o valor é maior que zero
        if (formattedValue == null || formattedValue <= 0) {
            Toast.makeText(this, "Insira um valor válido para a despesa", Toast.LENGTH_SHORT).show()
            return
        }

        // Obter o nome da despesa
        val nomeDespesa = autoCompleteTextView.text.toString()

        // Verificar se o nome da despesa foi preenchido
        if (nomeDespesa.isEmpty()) {
            Toast.makeText(this, "O campo 'Nome da despesa' é obrigatório", Toast.LENGTH_SHORT).show()
            return
        }

        // Obter a descrição da despesa
        val descricaoDespesa = descricaoDaDespesaEditText.text.toString()
        val nomeFavorecido = autoCompleteTextViewFavorecido.text.toString()

        if (formaPagamento == "Mensal") {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDateEfetuacao?.time?.time ?: System.currentTimeMillis()

            val firestore = FirebaseFirestore.getInstance()

            // Obter os valores temporários armazenados em DadosRecorrentesTemporarios
            val recurrenceOption = DadosRecorrentesTemporarios.recurrenceOption
            val period = DadosRecorrentesTemporarios.period
            val valorRecorrente = DadosRecorrentesTemporarios.valorRecorrente
            val numeroDeParcelas = DadosMensaisTemporarios.parcelasMensais
            val valorDoCalculo = DadosMensaisTemporarios.calculoDoMensal
            val categoriaEscolhida = DadosCategoriasTemporarios.categoriaSelecionada
            val iconeResId = intent.getIntExtra("icone_res_id", 0)
            val corImagemRedonda = intent.getIntExtra("cor_imagem_redonda", 0)
            val corCirculo = intent.getIntExtra("circulo_cor", 0)
            val grupoId = UUID.randomUUID().toString() // Gera um grupo_id único


            for (i in 1..numeroDeParcelas) {
                val novaDespesaRef = firestore.collection("despesas")
                    .document() // Cria uma referência com ID automático
                val novaDespesaId = novaDespesaRef.id // ID exclusivo gerado automaticamente

                // Converta a data para um objeto Timestamp
                val timestamp = Timestamp(calendar.time)

                val data = hashMapOf(
                    "userId" to userId, // Incluicao do UID do usuário no mapa de dados
                    "despesa_id" to novaDespesaId,
                    "valor_despesa" to formattedValue,
                    "nome_despesa" to nomeDespesa,
                    "opcao_recorrente" to recurrenceOption,
                    "tempo_recorrente" to period,
                    "valor_do_calculo_recorrente" to valorRecorrente,
                    "numero_de_parcelas" to numeroDeParcelas,
                    "valor_das_parcelas_calculado" to valorDoCalculo,
                    "categoria_escolhida_pelo_usuario" to categoriaEscolhida,
                    "nome_favorecido" to nomeFavorecido,
                    "iconeResId" to iconeResId,
                    "corImagemRedonda" to corImagemRedonda,
                    "corCirculo" to corCirculo,
                    "data_efetuacao" to timestamp,
                    "parcela_paga" to i,
                    "timestamp_registro" to System.currentTimeMillis(),
                    "grupo_id" to grupoId
                )

                // Adicionar a descrição ao mapa, se estiver preenchida
                if (descricaoDespesa.isNotEmpty()) {
                    data["descricao"] = descricaoDespesa
                }

                // Verificar se a data de efetuação foi selecionada
                if (selectedDateEfetuacao == null) {
                    Toast.makeText(
                        this,
                        "Por favor, selecione a data de efetuação.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return // Encerra o método para evitar o registro sem a data de efetuação
                }

                // Verificar se a forma de pagamento foi selecionada
                if (formaPagamento.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Por favor, selecione uma opção de pagamento.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return // Encerra o método para evitar o registro sem opção de pagamento selecionada
                }

                // Verificar se a categoria foi selecionada
                if (!categoriaSelecionada) {
                    Toast.makeText(this, "Por favor, selecione a categoria.", Toast.LENGTH_SHORT)
                        .show()
                    return // Encerra o método para evitar o registro sem a categoria
                }

                // Verificar se é uma despesa fixa e adicionar o valor da despesa fixa ao mapa de dados
                data["valor_ja_pago"] = PagoouNaoPago

                // Adicionar a forma de pagamento ao mapa de dados
                data["forma_pagamento"] = formaPagamento

                novaDespesaRef.set(data)
                    .addOnSuccessListener {
                        Log.d("MyApp", "Parcela $i registrada com sucesso")

                        // Verifique se todas as parcelas foram registradas e, em seguida, faça algo (se necessário)
                        if (i == numeroDeParcelas) {
                            // Todas as parcelas foram registradas, você pode adicionar lógica aqui, se necessário
                            Toast.makeText(
                                this,
                                "Despesa registrada com sucesso",
                                Toast.LENGTH_SHORT
                            ).show()

                            DadosRecorrentesTemporarios.recurrenceOption = ""
                            DadosRecorrentesTemporarios.period = 0
                            DadosRecorrentesTemporarios.valorRecorrente = 0.0
                            DadosMensaisTemporarios.parcelasMensais = 0
                            DadosMensaisTemporarios.calculoDoMensal = 0.0
                            DadosCategoriasTemporarios.categoriaSelecionada = ""
                            toggleSwitch.isChecked = false

                            // Limpar os valores armazenados no SharedPreferences após o registro bem-sucedido
                            val editor = sharedPreferences.edit()
                            editor.clear()
                            editor.apply()

                            // Atualize os valores de receitas, despesas e saldo na TelaPrincipal
                            val intent = Intent(this, TelaPrincipal::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener {
                        // Trate falhas na criação da parcela, se necessário
                        Log.e("MyApp", "Erro ao registrar parcela $i: $it")
                    }
                calendar.add(Calendar.MONTH, 1)
            }
        }
        else if(formaPagamento == "Recorrente"){
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDateEfetuacao?.time?.time ?: System.currentTimeMillis()
            val firestore = FirebaseFirestore.getInstance()

            // Obter os valores temporários armazenados em DadosRecorrentesTemporarios
            val recurrenceOption = DadosRecorrentesTemporarios.recurrenceOption
            val period = DadosRecorrentesTemporarios.period
            val valorRecorrente = DadosRecorrentesTemporarios.valorRecorrente
            val numeroDeParcelas = DadosMensaisTemporarios.parcelasMensais
            val valorDoCalculo = DadosMensaisTemporarios.calculoDoMensal
            val categoriaEscolhida = DadosCategoriasTemporarios.categoriaSelecionada
            val iconeResId = intent.getIntExtra("icone_res_id", 0)
            val corImagemRedonda = intent.getIntExtra("cor_imagem_redonda", 0)
            val corCirculo = intent.getIntExtra("circulo_cor", 0)
            val grupoId = UUID.randomUUID().toString() // Gera um grupo_id único

            if(recurrenceOption == "Mensal") {
                for (i in 1..period) {
                    val novaDespesaRef = firestore.collection("despesas").document() // Cria uma referência com ID automático
                    val novaDespesaId = novaDespesaRef.id // ID exclusivo gerado automaticamente

                    // Converta a data para um objeto Timestamp
                    val timestamp = Timestamp(calendar.time)

                    val data = hashMapOf(
                        "userId" to userId, // Incluicao do UID do usuário no mapa de dados
                        "despesa_id" to novaDespesaId,
                        "valor_despesa" to formattedValue,
                        "nome_despesa" to nomeDespesa,
                        "opcao_recorrente" to recurrenceOption,
                        "tempo_recorrente" to period,
                        "valor_do_calculo_recorrente" to valorRecorrente,
                        "numero_de_parcelas" to numeroDeParcelas,
                        "valor_das_parcelas_calculado" to valorDoCalculo,
                        "categoria_escolhida_pelo_usuario" to categoriaEscolhida,
                        "nome_favorecido" to nomeFavorecido,
                        "iconeResId" to iconeResId,
                        "corImagemRedonda" to corImagemRedonda,
                        "corCirculo" to corCirculo,
                        "data_efetuacao" to timestamp,
                        "periodo_paga" to i,
                        "timestamp_registro" to System.currentTimeMillis(),
                        "grupo_id" to grupoId
                    )

                    // Adicionar a descrição ao mapa, se estiver preenchida
                    if (descricaoDespesa.isNotEmpty()) {
                        data["descricao"] = descricaoDespesa
                    }

                    // Verificar se a data de efetuação foi selecionada
                    if (selectedDateEfetuacao == null) {
                        Toast.makeText(
                            this,
                            "Por favor, selecione a data de efetuação.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return // Encerra o método para evitar o registro sem a data de efetuação
                    }

                    // Verificar se a forma de pagamento foi selecionada
                    if (formaPagamento.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Por favor, selecione uma opção de pagamento.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return // Encerra o método para evitar o registro sem opção de pagamento selecionada
                    }

                    // Verificar se a categoria foi selecionada
                    if (!categoriaSelecionada) {
                        Toast.makeText(
                            this,
                            "Por favor, selecione a categoria.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return // Encerra o método para evitar o registro sem a categoria
                    }

                    // Verificar se é uma despesa fixa e adicionar o valor da despesa fixa ao mapa de dados
                    data["valor_ja_pago"] = PagoouNaoPago

                    // Adicionar a forma de pagamento ao mapa de dados
                    data["forma_pagamento"] = formaPagamento

                    novaDespesaRef.set(data)
                        .addOnSuccessListener {
                            Log.d("MyApp", "Parcela $i registrada com sucesso")

                            // Verifique se todas as parcelas foram registradas e, em seguida, faça algo (se necessário)
                            if (i == period) {
                                // Todas as parcelas foram registradas, você pode adicionar lógica aqui, se necessário
                                Toast.makeText(
                                    this,
                                    "Despesa registrada com sucesso",
                                    Toast.LENGTH_SHORT
                                ).show()

                                DadosRecorrentesTemporarios.recurrenceOption = ""
                                DadosRecorrentesTemporarios.period = 0
                                DadosRecorrentesTemporarios.valorRecorrente = 0.0
                                DadosMensaisTemporarios.parcelasMensais = 0
                                DadosMensaisTemporarios.calculoDoMensal = 0.0
                                DadosCategoriasTemporarios.categoriaSelecionada = ""
                                toggleSwitch.isChecked = false

                                // Limpar os valores armazenados no SharedPreferences após o registro bem-sucedido
                                val editor = sharedPreferences.edit()
                                editor.clear()
                                editor.apply()

                                // Atualize os valores de receitas, despesas e saldo na TelaPrincipal
                                val intent = Intent(this, TelaPrincipal::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener {
                            // Trate falhas na criação da parcela, se necessário
                            Log.e("MyApp", "Erro ao registrar parcela $i: $it")
                        }
                    calendar.add(Calendar.MONTH, 1)
                }
            }
            else{
                for (i in 1..period) {
                    // Converta a data para um objeto Timestamp
                    val timestamp = Timestamp(calendar.time)

                    val novaDespesaRef = firestore.collection("despesas")
                        .document() // Cria uma referência com ID automático
                    val novaDespesaId = novaDespesaRef.id // ID exclusivo gerado automaticamente

                    val data = hashMapOf(
                        "userId" to userId, // Incluicao do UID do usuário no mapa de dados
                        "despesa_id" to novaDespesaId,
                        "valor_despesa" to formattedValue,
                        "nome_despesa" to nomeDespesa,
                        "opcao_recorrente" to recurrenceOption,
                        "tempo_recorrente" to period,
                        "valor_do_calculo_recorrente" to valorRecorrente,
                        "numero_de_parcelas" to numeroDeParcelas,
                        "valor_das_parcelas_calculado" to valorDoCalculo,
                        "categoria_escolhida_pelo_usuario" to categoriaEscolhida,
                        "nome_favorecido" to nomeFavorecido,
                        "iconeResId" to iconeResId,
                        "corImagemRedonda" to corImagemRedonda,
                        "corCirculo" to corCirculo,
                        "data_efetuacao" to timestamp,
                        "periodo_paga" to i,
                        "timestamp_registro" to System.currentTimeMillis(),
                        "grupo_id" to grupoId
                    )

                    // Adicionar a descrição ao mapa, se estiver preenchida
                    if (descricaoDespesa.isNotEmpty()) {
                        data["descricao"] = descricaoDespesa
                    }

                    // Verificar se a data de efetuação foi selecionada
                    if (selectedDateEfetuacao == null) {
                        Toast.makeText(
                            this,
                            "Por favor, selecione a data de efetuação.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return // Encerra o método para evitar o registro sem a data de efetuação
                    }

                    // Verificar se a forma de pagamento foi selecionada
                    if (formaPagamento.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Por favor, selecione uma opção de pagamento.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return // Encerra o método para evitar o registro sem opção de pagamento selecionada
                    }

                    // Verificar se a categoria foi selecionada
                    if (!categoriaSelecionada) {
                        Toast.makeText(
                            this,
                            "Por favor, selecione a categoria.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return // Encerra o método para evitar o registro sem a categoria
                    }

                    // Verificar se é uma despesa fixa e adicionar o valor da despesa fixa ao mapa de dados
                    data["valor_ja_pago"] = PagoouNaoPago

                    // Adicionar a forma de pagamento ao mapa de dados
                    data["forma_pagamento"] = formaPagamento

                    novaDespesaRef.set(data)
                        .addOnSuccessListener {
                            Log.d("MyApp", "Parcela $i registrada com sucesso")

                            // Verifique se todas as parcelas foram registradas e, em seguida, faça algo (se necessário)
                            if (i == period) {
                                // Todas as parcelas foram registradas, você pode adicionar lógica aqui, se necessário
                                Toast.makeText(
                                    this,
                                    "Despesa registrada com sucesso",
                                    Toast.LENGTH_SHORT
                                ).show()

                                DadosRecorrentesTemporarios.recurrenceOption = ""
                                DadosRecorrentesTemporarios.period = 0
                                DadosRecorrentesTemporarios.valorRecorrente = 0.0
                                DadosMensaisTemporarios.parcelasMensais = 0
                                DadosMensaisTemporarios.calculoDoMensal = 0.0
                                DadosCategoriasTemporarios.categoriaSelecionada = ""
                                toggleSwitch.isChecked = false

                                // Limpar os valores armazenados no SharedPreferences após o registro bem-sucedido
                                val editor = sharedPreferences.edit()
                                editor.clear()
                                editor.apply()

                                // Atualize os valores de receitas, despesas e saldo na TelaPrincipal
                                val intent = Intent(this, TelaPrincipal::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener {
                            // Trate falhas na criação da parcela, se necessário
                            Log.e("MyApp", "Erro ao registrar parcela $i: $it")
                        }
                    calendar.add(Calendar.YEAR, 1)
                }
            }
        }
        else{
            // Obter os valores temporários armazenados em DadosRecorrentesTemporarios
            val recurrenceOption = DadosRecorrentesTemporarios.recurrenceOption
            val period = DadosRecorrentesTemporarios.period
            val valorRecorrente = DadosRecorrentesTemporarios.valorRecorrente
            val numeroDeParcelas = DadosMensaisTemporarios.parcelasMensais
            val valorDoCalculo = DadosMensaisTemporarios.calculoDoMensal
            val categoriaEscolhida = DadosCategoriasTemporarios.categoriaSelecionada
            val iconeResId = intent.getIntExtra("icone_res_id", 0)
            val corImagemRedonda = intent.getIntExtra("cor_imagem_redonda", 0)
            val corCirculo = intent.getIntExtra("circulo_cor", 0)

            // Exemplo de como enviar os dados para o Firestore (banco de dados):
            val firestore = FirebaseFirestore.getInstance()

            val novaDespesaRef = firestore.collection("despesas")
                    .document() // Cria uma referência com ID automático
                val novaDespesaId = novaDespesaRef.id // ID exclusivo gerado automaticamente

                val data = hashMapOf(
                    "userId" to userId, // Incluicao do UID do usuário no mapa de dados
                    "despesa_id" to novaDespesaId,
                    "valor_despesa" to formattedValue,
                    "nome_despesa" to nomeDespesa,
                    "opcao_recorrente" to recurrenceOption,
                    "tempo_recorrente" to period,
                    "valor_do_calculo_recorrente" to valorRecorrente,
                    "numero_de_parcelas" to numeroDeParcelas,
                    "valor_das_parcelas_calculado" to valorDoCalculo,
                    "categoria_escolhida_pelo_usuario" to categoriaEscolhida,
                    "nome_favorecido" to nomeFavorecido,
                    "iconeResId" to iconeResId,
                    "corImagemRedonda" to corImagemRedonda,
                    "corCirculo" to corCirculo,
                    "timestamp_registro" to System.currentTimeMillis()
                )

                val efetuacaoTimestamp = selectedDateEfetuacao?.time

                // Adicionar a descrição ao mapa, se estiver preenchida
                if (descricaoDespesa.isNotEmpty()) {
                    data["descricao"] = descricaoDespesa
                }

                // Adicionar a data e hora ao mapa, se selecionada
                if (selectedDateEfetuacao != null) {
                    data["data_efetuacao"] = efetuacaoTimestamp
                }

                // Verificar se a data de efetuação foi selecionada
                if (selectedDateEfetuacao == null) {
                    Toast.makeText(
                        this,
                        "Por favor, selecione a data de efetuação.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return // Encerra o método para evitar o registro sem a data de efetuação
                }

                // Verificar se a forma de pagamento foi selecionada
                if (formaPagamento.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Por favor, selecione uma opção de pagamento.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return // Encerra o método para evitar o registro sem opção de pagamento selecionada
                }

                // Verificar se a categoria foi selecionada
                if (!categoriaSelecionada) {
                    Toast.makeText(this, "Por favor, selecione a categoria.", Toast.LENGTH_SHORT)
                        .show()
                    return // Encerra o método para evitar o registro sem a categoria
                }

                // Verificar se é uma despesa fixa e adicionar o valor da despesa fixa ao mapa de dados
                data["valor_ja_pago"] = PagoouNaoPago

                // Adicionar a forma de pagamento ao mapa de dados
                data["forma_pagamento"] = formaPagamento

                novaDespesaRef.set(data)
                    .addOnSuccessListener {

                            // Todas as parcelas foram registradas, você pode adicionar lógica aqui, se necessário
                            Toast.makeText(
                                this,
                                "Despesa registrada com sucesso",
                                Toast.LENGTH_SHORT
                            ).show()

                            DadosRecorrentesTemporarios.recurrenceOption = ""
                            DadosRecorrentesTemporarios.period = 0
                            DadosRecorrentesTemporarios.valorRecorrente = 0.0
                            DadosMensaisTemporarios.parcelasMensais = 0
                            DadosMensaisTemporarios.calculoDoMensal = 0.0
                            DadosCategoriasTemporarios.categoriaSelecionada = ""
                            toggleSwitch.isChecked = false

                            // Limpar os valores armazenados no SharedPreferences após o registro bem-sucedido
                            val editor = sharedPreferences.edit()
                            editor.clear()
                            editor.apply()

                            // Atualize os valores de receitas, despesas e saldo na TelaPrincipal
                            val intent = Intent(this, TelaPrincipal::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                    }
                    .addOnFailureListener {
                        // Trate falhas na criação da parcela, se necessário
                    }
            }
    }

    private fun hideBotoesPagamento() {
        findViewById<Button>(R.id.pagar_a_vista).visibility = View.GONE
        findViewById<Button>(R.id.pagar_mensal).visibility = View.GONE
        findViewById<Button>(R.id.pagar_recorrente).visibility = View.GONE
    }

    private fun showBotoesPagamento() {
        findViewById<Button>(R.id.pagar_a_vista).visibility = View.VISIBLE
        findViewById<Button>(R.id.pagar_mensal).visibility = View.VISIBLE
        findViewById<Button>(R.id.pagar_recorrente).visibility = View.VISIBLE
    }
    private fun salvarValoresAoSelecionarCategoria() {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
        val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

        // Obter os valores inseridos pelo usuário
        val valorDaDespesa = valorDaDespesaEditText.text.toString()
        val nomeDaDespesa = autoCompleteTextView.text.toString()
        val descricaoDaDespesa = descricaoDaDespesaEditText.text.toString()
        val nomeDoFavorecido =  autoCompleteTextViewFavorecido.text.toString()

        // Salvar os valores no SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("valor_da_receita", valorDaDespesa)
        editor.putString("nome_da_receita", nomeDaDespesa)
        editor.putString("descricao_da_receita", descricaoDaDespesa) // Adicione a descrição ao SharedPreferences
        editor.putLong("data_efetuacao", dataEfetuacaoInMillis)
        editor.putString("nome_do_favorecido", nomeDoFavorecido)
        editor.apply()
    }
    private fun exibirDataSalvaNoSharedPreferences() {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
        val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

        dataEfetuacaoInMillis = sharedPreferences.getLong("data_efetuacao", 0)

        // Atualize a variável selectedDateEfetuacao com a data recuperada (se existir)
        if (dataEfetuacaoInMillis > 0) {
            selectedDateEfetuacao = Calendar.getInstance()
            selectedDateEfetuacao!!.timeInMillis = dataEfetuacaoInMillis

            // Exiba o botão "Limpar Data" e atualize o TextView com a data salva
            findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
            updateDataEfetuacaoTextView(selectedDateEfetuacao)
        }

        // Exibir os valores salvos nos campos de entrada
        valorDaDespesaEditText.setText(sharedPreferences.getString("valor_da_receita", ""))
        autoCompleteTextView.setText(sharedPreferences.getString("nome_da_receita", ""))
        descricaoDaDespesaEditText.setText(sharedPreferences.getString("descricao_da_receita", ""))
        autoCompleteTextViewFavorecido.setText(sharedPreferences.getString("nome_do_favorecido", ""))
        updateDataEfetuacaoTextView(selectedDateEfetuacao)
    }

    //aqui seria uma função para calcular o pagamento a vista
    private fun registrarDespesaAVista() {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
        val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

        val pagarAVistaButton = findViewById<Button>(R.id.pagar_a_vista)
        val corBotaoPadrao = ContextCompat.getColor(this, R.color.colorButtonAVista)
        val corBotaoDesmarcado = ContextCompat.getColor(this, R.color.colorButtonPadrao)

        if (isPagarAVistaSelected) {
            // Deselecionar "à vista" e voltar à cor original
            isPagarAVistaSelected = false
            pagarAVistaButton.setBackgroundColor(corBotaoPadrao)
            findViewById<Button>(R.id.pagar_mensal).visibility = View.VISIBLE
            findViewById<Button>(R.id.pagar_recorrente).visibility = View.VISIBLE
            formaPagamento = ""
        } else {
            // Selecionar "à vista" e mudar a cor do botão
            isPagarAVistaSelected = true
            pagarAVistaButton.setBackgroundColor(corBotaoDesmarcado)
            findViewById<Button>(R.id.pagar_mensal).visibility = View.GONE
            findViewById<Button>(R.id.pagar_recorrente).visibility = View.GONE
            formaPagamento = "À vista"
        }
        updateFormaPagamento()

        // Salvar o estado do botão "à vista" no SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_pagar_a_vista_selected", isPagarAVistaSelected)
        editor.apply()
    }

    private fun obterDataHoraFormatada(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun updateDataEfetuacaoTextView(selectedDate: Calendar?) {
        if (selectedDate != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            findViewById<TextView>(R.id.data_efetuacao_textview).text = formattedDate
        }
    }

    private fun openDatePickerDialog() {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileName = "Prefs_$userId"

            // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
            val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            // Verifica se a data selecionada é anterior à data atual
            val currentDate = Calendar.getInstance()
            if (selectedCalendar.before(currentDate)) {
                toggleSwitch.isChecked = true // Ativa o toggle
            } else if(selectedCalendar == currentDate){
                toggleSwitch.isChecked = true // Ativa o toggle
            }
            else {
                toggleSwitch.isChecked = false // Deixa o toggle desativado
            }

            // Restante do seu código para atualizar a interface e salvar a data
            selectedDateEfetuacao = selectedCalendar
            findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.GONE
            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE
            findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE
            updateDataEfetuacaoTextView(selectedDateEfetuacao)
            dataEfetuacaoInMillis = selectedDateEfetuacao?.timeInMillis ?: 0

            val editor = sharedPreferences.edit()
            editor.putLong("data_efetuacao", dataEfetuacaoInMillis)
            editor.apply()

            if (selectedDateEfetuacao != null) {
                val dataHoraFormatada = obterDataHoraFormatada(selectedDateEfetuacao!!)
                data["data_efetuacao"] = dataHoraFormatada
            }

            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE

        }, year, month, dayOfMonth)

        datePickerDialog.show()
    }
    override fun onBackPressed() {
        // Impede que o usuário volte deslizando para a tela anterior
    }
    fun limparDataEfetuacaoButton(view: View) {
        selectedDateVencimento = null
        findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.VISIBLE
        findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.VISIBLE
        findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE
    }
    private fun abrirTelaPrincipal() {
        val intent = Intent(this, TelaPrincipal::class.java)
        startActivity(intent)
    }
    private fun abrirTelaDasCategoriasDespesas() {
        val intent = Intent(this, activity_tela_das_categorias_despesas::class.java)
        startActivity(intent)
    }
    private fun pagamentoMensal() {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
        val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

        // Obtenha o valor inserido no campo "valor_da_despesa"
        val valorDespesaEditText = findViewById<EditText>(R.id.valor_da_despesa)
        val valorDespesaText = valorDespesaEditText.text.toString()

        // Verifique se o campo "valor_da_despesa" está vazio
        if (valorDespesaText.isEmpty()) {
            Toast.makeText(this, "Insira um valor para a despesa antes de calcular o pagamento mensal", Toast.LENGTH_SHORT).show()
            return
        }

        // Converta o valor da despesa de String para Double
        val valueDespesa = try {
            valorDespesaText.replace("R", "")
                .replace("$", "")
                .replace(" ", "")
                .replace(",", ".")
                .toDouble()
        } catch (e: NumberFormatException) {
            null
        }

        // Verifique se a conversão foi bem-sucedida e se o valor é maior que zero
        if (valueDespesa == null || valueDespesa <= 0) {
            Toast.makeText(this, "Insira um valor válido para a despesa antes de calcular o pagamento mensal", Toast.LENGTH_SHORT).show()
            return
        }

        // Se o valor for válido, verifique o estado do botão "pagar_mensal"
        val pagarEmMensalButton = findViewById<Button>(R.id.pagar_mensal)
        val corBotaoPadrao = ContextCompat.getColor(this, R.color.colorButtonAVista)
        val corBotaoDesmarcado = ContextCompat.getColor(this, R.color.colorButtonPadrao)

        if (isPagarEmMensalVisible) {
            // Se já estava selecionado, desmarque e esconda o cálculo do pagamento mensal
            isPagarEmMensalVisible = false
            isPagamentoMensalSelecionado = false
            pagarEmMensalButton.setBackgroundColor(corBotaoPadrao)
            formaPagamento = ""
            showBotoesPagamentoSection()
            findViewById<TextView>(R.id.resultado_mensal_textview).visibility = View.GONE
        } else {
            // Se não estava selecionado, marque, mude a cor do botão e exiba o cálculo do pagamento mensal
            isPagarEmMensalVisible = true
            isPagamentoMensalSelecionado = true
            pagarEmMensalButton.setBackgroundColor(corBotaoDesmarcado)
            showCalculateMensalValueDialog(valueDespesa)
            hideBotoesPagamentoSection()
            valorDespesaMensal = valueDespesa
            formaPagamento = "Mensal"
            findViewById<Button>(R.id.limpar_pagamento_button).visibility = View.VISIBLE
        }
        updateFormaPagamento()
    }
    private fun formatCurrencyValue(value: Double): String {
        val decimalFormat = DecimalFormat.getCurrencyInstance()
        return decimalFormat.format(value)
    }
    object DadosMensaisTemporarios {
        var parcelasMensais: Int = 0
        var calculoDoMensal: Double = 0.0
    }
    // Função para exibir o diálogo de cálculo mensal
    private fun showCalculateMensalValueDialog(valueDespesa: Double) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_calculate_mensal_value, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val etMonths = dialogView.findViewById<EditText>(R.id.et_months)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)
        val corBotaoPadrao = ContextCompat.getColor(this, R.color.colorButtonAVista)
        val pagarEmMensalButton = findViewById<Button>(R.id.pagar_mensal)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss()
                isPagarEmMensalVisible = false
                showBotoesPagamentoSection() // Exibir os botões de pagamento novamente
                pagarEmMensalButton.setBackgroundColor(corBotaoPadrao)
                findViewById<Button>(R.id.limpar_pagamento_button).visibility = View.GONE
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        // TextWatcher para atualizar automaticamente o resultado ao alterar o número de meses
        etMonths.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val monthsText = s.toString()
                if (monthsText.isNotEmpty()) {
                    val months = monthsText.toIntOrNull()
                    if (months != null && months > 0) {
                        // Atualize o valor da despesa para cálculos futuros
                        valorDespesaForMensalCalculation = valueDespesa / months
                        numeroParcelas = months

                        DadosMensaisTemporarios.calculoDoMensal = valorDespesaForMensalCalculation
                        DadosMensaisTemporarios.parcelasMensais = numeroParcelas
                    }
                }
            }
        })
        // Configurar o OnClickListener para o botão "Confirmar"
        btnConfirm.setOnClickListener {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileName = "Prefs_$userId"

            // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
            val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

            // Exiba o valor mensal no TextView na tela principal
            val mensalTextView = findViewById<TextView>(R.id.resultado_mensal_textview)
            valorArmazenadoCertoMensal = valorDespesaForMensalCalculation
            mensalTextView.text = formatCurrencyValue(valorDespesaForMensalCalculation)
            mensalTextView.visibility = View.VISIBLE

            // Salvar o valor mensal no SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putFloat("valor_armazenado_certo_mensal", valorArmazenadoCertoMensal.toFloat())
            editor.apply()

            alertDialog.dismiss() // Fechar o diálogo após a confirmação
        }
    }
    private fun hideBotoesPagamentoSection() {
        // Esconda os elementos relacionados à data de vencimento
        findViewById<Button>(R.id.pagar_a_vista).visibility = View.GONE
        findViewById<Button>(R.id.pagar_mensal).visibility = View.GONE
        findViewById<Button>(R.id.pagar_recorrente).visibility = View.GONE
    }
    private fun showBotoesPagamentoSection() {

        findViewById<Button>(R.id.pagar_a_vista).visibility = View.VISIBLE
        findViewById<Button>(R.id.pagar_mensal).visibility = View.VISIBLE
        findViewById<Button>(R.id.pagar_recorrente).visibility = View.VISIBLE
    }
    private fun showRecurrenceOptionsDialog() {
        val valorDespesaEditText = findViewById<EditText>(R.id.valor_da_despesa)
        val valorDespesaText = valorDespesaEditText.text.toString()

        // Verifique se o campo "valor_da_despesa" está vazio ou contém um valor inválido
        if (valorDespesaText.isEmpty()) {
            Toast.makeText(this, "Insira um valor para a despesa antes de selecionar a recorrência.", Toast.LENGTH_SHORT).show()
            return
        }

        // Converta o valor da despesa de String para Double
        val valueDespesa = try {
            valorDespesaText.replace("R", "")
                .replace("$", "")
                .replace(" ", "")
                .replace(",", ".")
                .toDouble()
        } catch (e: NumberFormatException) {
            null
        }

        // Verifique se a conversão foi bem-sucedida e se o valor é maior que zero
        if (valueDespesa == null || valueDespesa <= 0) {
            Toast.makeText(this, "Insira um valor válido para a despesa antes de selecionar a recorrência.", Toast.LENGTH_SHORT).show()
            return
        }

        // Se o valor for válido, abra o diálogo de opções de recorrência
        val options = arrayOf("Mensal", "Anual")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione a recorrência")
        builder.setAdapter(adapter) { _, which ->
            val selectedRecurrence = options[which]
            showRecurrencePeriodDialog(selectedRecurrence)
        }
        builder.show()
    }

    private fun showRecurrencePeriodDialog(selectedRecurrence: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_recurrence_period, null)
        val etPeriod = dialogView.findViewById<EditText>(R.id.et_period)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Calcular") { dialog, _ ->
                val period = etPeriod.text.toString().toIntOrNull()
                if (period != null && period > 0) {
                    calculateRecurringExpense(selectedRecurrence, period)
                } else {
                    Toast.makeText(this, "Insira um período de recorrência válido.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
    object DadosRecorrentesTemporarios {
        var recurrenceOption: String = ""
        var period: Int = 0
        var valueDespesa: Double = 0.0
        var valorRecorrente: Double = 0.0
    }

    private fun calculateRecurringExpense(recurrenceOption: String, period: Int) {
        // Verificar o tipo de recorrência selecionado pelo usuário e realizar o cálculo apropriado.
        val valorDespesaEditText = findViewById<EditText>(R.id.valor_da_despesa)
        val valorDespesaText = valorDespesaEditText.text.toString()

        // Verifique se o campo "valor_da_despesa" está vazio
        if (valorDespesaText.isEmpty()) {
            Toast.makeText(this, "Insira um valor para a despesa antes de calcular o pagamento recorrente", Toast.LENGTH_SHORT).show()
            return
        }

        // Converta o valor da despesa de String para Double
        val valueDespesa = try {
            valorDespesaText.replace("R", "")
                .replace("$", "")
                .replace(" ", "")
                .replace(",", ".")
                .toDouble()
        } catch (e: NumberFormatException) {
            null
        }

        // Verifique se a conversão foi bem-sucedida e se o valor é maior que zero
        if (valueDespesa == null || valueDespesa <= 0) {
            Toast.makeText(this, "Insira um valor válido para a despesa antes de calcular o pagamento recorrente", Toast.LENGTH_SHORT).show()
            return
        }

        // Calcular o valor recorrente com base na recorrência selecionada e no período informado
        val valorRecorrente = when (recurrenceOption) {
            "Mensal" -> valueDespesa * period
            "Anual" -> valueDespesa * period
            else -> valueDespesa // Recorrência desconhecida, retorne o valor original
        }
        valorDespesaRecorrente = valorRecorrente

        // Armazenar temporariamente os dados recorrentes no objeto DadosRecorrentesTemporarios
        DadosRecorrentesTemporarios.recurrenceOption = recurrenceOption
        DadosRecorrentesTemporarios.period = period
        DadosRecorrentesTemporarios.valueDespesa = valueDespesa
        DadosRecorrentesTemporarios.valorRecorrente = valorRecorrente

        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
        val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

        // Salvar o valor recorrente no SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putFloat("valor_despesa_armazenar_recorrente", valorRecorrente.toFloat())
        editor.putBoolean("is_pagamento_recorrente_selecionado", true)
        editor.apply()

        // Exiba o resultado do cálculo em algum lugar da interface do usuário, como um TextView.
        val resultadoRecorrenteTextView = findViewById<TextView>(R.id.resultado_recorrente_textview)
        resultadoRecorrenteTextView.text = String.format(Locale.US, "R$ %.2f", valorRecorrente)
        resultadoRecorrenteTextView.visibility = View.VISIBLE
        hideBotoesPagamentoSection()
        formaPagamento = "Recorrente"

        // Exibir o botão de limpar
        findViewById<Button>(R.id.limpar_pagamento_button).visibility = View.VISIBLE

        // Atualizar a variável isPagarEmMensalVisible para indicar que o botão "pagar_mensal" não está selecionado
        isPagarEmMensalVisible = false

        // Alterar a cor de fundo do botão "pagar_mensal" de volta para a cor padrão (caso seja necessário)
        val corBotaoPadrao = ContextCompat.getColor(this, R.color.colorButtonAVista)
        val pagarEmMensalButton = findViewById<Button>(R.id.pagar_mensal)
        pagarEmMensalButton.setBackgroundColor(corBotaoPadrao)

        updateFormaPagamento()
    }
    private fun updateFormaPagamento() {
        data["forma_pagamento"] = formaPagamento
    }
    fun limparPagamento(view: View) {
        // Limpar os valores dos pagamentos mensal e recorrente
        valorDespesaMensal = 0.0
        valorDespesaRecorrente = 0.0
        numeroParcelas = 0

        // Ocultar o TextView do resultado
        findViewById<TextView>(R.id.resultado_mensal_textview).visibility = View.GONE
        findViewById<TextView>(R.id.resultado_recorrente_textview).visibility = View.GONE

        // Esconder o botão de limpar
        findViewById<Button>(R.id.limpar_pagamento_button).visibility = View.GONE

        // Mostrar os botões de seleção de forma de pagamento
        showBotoesPagamentoSection()

        // Atualizar a variável isPagarEmMensalVisible para indicar que o botão "pagar_mensal" não está selecionado
        isPagarEmMensalVisible = false

        // Alterar a cor de fundo do botão "pagar_mensal" de volta para a cor padrão (caso seja necessário)
        val corBotaoPadrao = ContextCompat.getColor(this, R.color.colorButtonAVista)
        val pagarEmMensalButton = findViewById<Button>(R.id.pagar_mensal)
        pagarEmMensalButton.setBackgroundColor(corBotaoPadrao)

        updateFormaPagamento()
    }

    private fun carregarNomesDespesasDoUsuario() {
        Log.d("DespesasApp", "Iniciando carregarNomesDespesasDoUsuario")

        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
        val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

        if (userId != null) {
            val despesasRef = FirebaseFirestore.getInstance()
                .collection("despesas")
                .whereEqualTo("userId", userId)

            despesasRef.get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("DespesasApp", "Sucesso ao obter dados do Firestore")

                    val nomesDespesasList = mutableListOf<String>()
                    val nomesDespesasSet = mutableSetOf<String>() // Usar um conjunto para armazenar nomes únicos

                    for (document in querySnapshot) {
                        val nomeDespesa = document.getString("nome_despesa")
                        nomeDespesa?.let {
                            nomesDespesasSet.add(it) // Adicionar ao conjunto para garantir nomes únicos
                        }
                    }

                    nomesDespesasList.addAll(nomesDespesasSet.toList()) // Converter o conjunto de volta para uma lista

                    val textInputLayout: TextInputLayout = findViewById(R.id.Nome_da_despesa)
                    val autoCompleteTextView = textInputLayout.editText as? AutoCompleteTextView

                    if (autoCompleteTextView != null) {
                        autoCompleteTextView?.let {
                            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nomesDespesasList)
                            it.setAdapter(adapter)

                            it.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    Log.d("DespesasApp", "Texto alterado: $s")

                                    val filtro = nomesDespesasList.filter { it.contains(s.toString(), ignoreCase = true) }

                                    val adapter = ArrayAdapter(this@TelaPrincipalCategorias, android.R.layout.simple_dropdown_item_1line, filtro)
                                    autoCompleteTextView.setAdapter(adapter)

                                    val editor = sharedPreferences.edit()
                                    editor.putString("nome_da_despesa", s.toString())
                                    editor.apply()
                                }

                                override fun afterTextChanged(s: Editable?) {}
                            })
                        }
                    } else {
                        Log.e("DespesasApp", "autoCompleteTextView não foi encontrado no layout")
                        // Lide com a situação quando o autoCompleteTextView não for encontrado
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DespesasApp", "Falha ao obter dados do Firestore: ${e.message}")
                    Toast.makeText(this, "Falha ao carregar os nomes das despesas do usuário: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun carregarNomesFavorecidosDoUsuario() {
        Log.d("DespesasApp", "Iniciando carregarNomesFavorecidosDoUsuario")

        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName = "Prefs_$userId"

        // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
        val sharedPreferences = getSharedPreferences(prefsFileName, MODE_PRIVATE)

        if (userId != null) {
            val favorecidosRef = FirebaseFirestore.getInstance()
                .collection("despesas")
                .whereEqualTo("userId", userId)

            favorecidosRef.get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("DespesasApp", "Sucesso ao obter dados do Firestore para favorecidos")

                    val nomesFavorecidosList = mutableListOf<String>()
                    val nomesFavorecidosSet = mutableSetOf<String>() // Usar um conjunto para armazenar nomes únicos

                    for (document in querySnapshot) {
                        val nomeFavorecido = document.getString("nome_favorecido")
                        nomeFavorecido?.let {
                            nomesFavorecidosSet.add(it) // Adicionar ao conjunto para garantir nomes únicos
                        }
                    }

                    nomesFavorecidosList.addAll(nomesFavorecidosSet.toList()) // Converter o conjunto de volta para uma lista

                    val textInputLayout2: TextInputLayout = findViewById(R.id.Nome_do_favorecido)
                    val autoCompleteTextViewFavorecido = textInputLayout2.editText as? AutoCompleteTextView

                    if (autoCompleteTextViewFavorecido != null) {
                        autoCompleteTextViewFavorecido?.let {
                            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nomesFavorecidosList)
                            it.setAdapter(adapter)

                            it.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(d: CharSequence?, start: Int, count: Int, after: Int) {}

                                override fun onTextChanged(d: CharSequence?, start: Int, before: Int, count: Int) {
                                    Log.d("DespesasApp", "Texto alterado: $d")

                                    val filtro = nomesFavorecidosList.filter { it.contains(d.toString(), ignoreCase = true) }

                                    val adapter = ArrayAdapter(this@TelaPrincipalCategorias, android.R.layout.simple_dropdown_item_1line, filtro)
                                    autoCompleteTextViewFavorecido.setAdapter(adapter)

                                    val editor = sharedPreferences.edit()
                                    editor.putString("nome_do_favorecido", d.toString())
                                    editor.apply()
                                }

                                override fun afterTextChanged(s: Editable?) {}
                            })
                        }
                    } else {
                        Log.e("DespesasApp", "autoCompleteTextView não foi encontrado no layout")
                        // Lide com a situação quando o autoCompleteTextView não for encontrado
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DespesasApp", "Falha ao obter dados do Firestore para favorecidos: ${e.message}")
                    Toast.makeText(this, "Falha ao carregar os nomes dos favorecidos do usuário: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
