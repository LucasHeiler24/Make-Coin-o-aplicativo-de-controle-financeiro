package br.com.makecoin.view.telaEditarRegistros

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import br.com.makecoin.R
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import br.com.makecoin.view.TelaPrincipalCategorias.TelaPrincipalCategorias
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class activity_tela_editar_as_despesas : AppCompatActivity() {

    val SEU_CODIGO_DE_REQUEST = 1 // Pode ser qualquer número inteiro que você escolher
    private lateinit var toggleSwitch: Switch
    private var selectedDateEfetuacao: Calendar? = null
    private var dataEfetuacaoInMillis: Long = 0
    private var novoIconeResId: Int = 0
    private var novaCorCirculo: Int = 0
    private var isPagarAVistaSelected = false
    private var formaPagamento: String = ""
    private var isPagarEmMensalVisible = false
    private var valorDespesaMensal: Double = 0.0
    private var valorDespesaForMensalCalculation: Double = 0.0
    private var numeroParcelas: Int = 0
    private var valorDespesaRecorrente: Double = 0.0
    private var isPagamentoRecorrenteSelecionado = false
    private lateinit var valorDaDespesaEditText: EditText
    private lateinit var descricaoDaDespesaEditText: EditText
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private var categoriaSelecionada: Boolean = false
    private lateinit var autoCompleteTextViewFavorecido: AutoCompleteTextView
    private var grupoDoId: String = ""
    private var dataEfetuacao: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_editar_as_despesas)
        FirebaseApp.initializeApp(this)

        // Obtenha o ID da receita da Intent
        val despesasId = intent.getStringExtra("despesa_id") ?: ""

        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Verifique se o usuário está logado
        if (userId.isNullOrEmpty()) {
            // Lidar com a situação em que o usuário não está logado
            return
        }
        val voltarTelaPrincipal = findViewById<ImageView>(R.id.Voltar_tela_inicial)
        voltarTelaPrincipal.setOnClickListener {
            abrirTelaPrincipal()
        }
        // Referência para o Firestore
        val firestore = FirebaseFirestore.getInstance()

        // Referência ao documento da receita usando o ID
        val despesaRef = firestore.collection("despesas").document(despesasId)

        despesaRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val data = documentSnapshot.data

                // Verifique se a receita pertence ao usuário logado
                val despesaUserId = data?.get("userId")?.toString() ?: ""

                if (despesaUserId == userId) {
                    val valorDaDespesa = data?.get("valor_despesa") as? Double ?: 0.0
                    findViewById<EditText>(R.id.valor_da_despesa).setText(valorDaDespesa.toString())

                    val categoriaTextView = findViewById<TextView>(R.id.categoria_salario)
                    categoriaTextView.text =
                        data?.get("categoria_escolhida_pelo_usuario")?.toString() ?: ""
                    DadosTemporariosIntermediarios.categoriaNome = categoriaTextView.toString()

                    val iconeCategoria = data?.get("iconeResId") as? Long
                    DadosTemporariosIntermediarios.iconeCategoria = iconeCategoria?.toInt() ?: 0

                    val corCategoria = data?.get("corCirculo") as? Long
                    DadosTemporariosIntermediarios.corCategoria = corCategoria?.toInt() ?: 0

                    findViewById<EditText>(R.id.auto_completa_texto).setText(
                        data?.get("nome_despesa")?.toString() ?: ""
                    )
                    findViewById<EditText>(R.id.Descricao_da_despesa).setText(
                        data?.get("descricao")?.toString() ?: ""
                    )
                    findViewById<EditText>(R.id.auto_completa_texto_favorecidos).setText(
                        data?.get(
                            "nome_favorecido"
                        )?.toString() ?: ""
                    )
                    // Obtenha o valor atual da forma de pagamento do Firestore durante a edição
                    val formaPagamentoFirestore = data?.get("forma_pagamento") as? String
                    formaPagamento = formaPagamentoFirestore ?: ""

                    val valorDoMensal = data?.get("valor_das_parcelas_calculado") as? Double ?: 0.0
                    findViewById<TextView>(R.id.resultado_mensal_textview).text = formatCurrencyValue(valorDoMensal)

                    val numeroDeParcelas = data?.get("numero_de_parcelas") as? Long
                    DadosTemporariosIntermediarios.parcelasMensais = numeroDeParcelas?.toInt() ?: 0

                    DadosTemporariosIntermediarios.calculoDoMensal = valorDoMensal

                    val valorDoRecorrente = data?.get("valor_do_calculo_recorrente") as? Double ?: 0.0
                    val grupoId = data?.get("grupo_id") as? String
                    grupoDoId = grupoId ?: ""
                    val recurrenceOption = data?.get("opcao_recorrente") as? String
                    val period = data?.get("tempo_recorrente") as? Long

                    // Armazenar temporariamente os dados recorrentes no objeto DadosTemporariosIntermediarios
                    DadosTemporariosIntermediarios.recurrenceOption = recurrenceOption.toString()
                    DadosTemporariosIntermediarios.period = period?.toInt() ?: 0
                    DadosTemporariosIntermediarios.valorRecorrente = valorDoRecorrente

                    val corBotaoDesmarcado = ContextCompat.getColor(this, R.color.colorButtonPadrao)

                    if (formaPagamento == "À vista") {
                        val limparDataButton = findViewById<Button>(R.id.limpar_data_button)
                        limparDataButton.visibility = View.VISIBLE

                        // Botão "Limpar Data" para limpar a data selecionada
                        limparDataButton.setOnClickListener {
                            selectedDateEfetuacao = null

                            toggleSwitch.isChecked = false

                            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.VISIBLE

                            // Esconda o botão "Limpar Data"
                            findViewById<Button>(R.id.limpar_data_button).visibility = View.GONE

                            // Esconda o TextView com a data selecionada
                            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE
                        }

                        // Botão "Outros" para abrir o DatePickerDialog
                        val abrirCalendarioDataEfetuacaoButton = findViewById<Button>(R.id.abrir_calendario_data_efetuacao)
                        abrirCalendarioDataEfetuacaoButton.setOnClickListener {
                            openDatePickerDialog() // Chama o método para exibir o DatePickerDialog
                        }

                        isPagarAVistaSelected = true
                        val pagarAVistaButton = findViewById<Button>(R.id.pagar_a_vista)
                        pagarAVistaButton.setBackgroundColor(corBotaoDesmarcado)
                        findViewById<Button>(R.id.pagar_mensal).visibility = View.GONE
                        findViewById<Button>(R.id.pagar_recorrente).visibility = View.GONE

                        pagarAVistaButton.setOnClickListener {
                            Toast.makeText(this, "A forma de pagamento não pode ser alterada!", Toast.LENGTH_LONG).show()
                            registrarDespesaAVista()
                        }
                    }
                    else if (formaPagamento == "Mensal") {
                        val editTextMensal = findViewById<EditText>(R.id.valor_da_despesa)

                        editTextMensal.setOnClickListener {
                            // Exibe o Toast informando que o campo não pode ser editado
                            Toast.makeText(this, "O valor não pode ser digitado", Toast.LENGTH_LONG).show()
                        }

                        editTextMensal.keyListener = null
                        val pagarMensalButton = findViewById<Button>(R.id.pagar_mensal)
                        pagarMensalButton.setBackgroundColor(corBotaoDesmarcado)
                        findViewById<Button>(R.id.pagar_a_vista).visibility = View.GONE
                        findViewById<Button>(R.id.pagar_recorrente).visibility = View.GONE
                        findViewById<TextView>(R.id.resultado_mensal_textview).visibility = View.VISIBLE
                        findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE

                        pagarMensalButton.setOnClickListener {
                            Toast.makeText(this, "A forma de pagamento não pode ser alterada!", Toast.LENGTH_LONG).show()
                            registrarDespesaMensal()
                        }



                    }
                    else if (formaPagamento == "Recorrente") {
                        val pagarRecorrenteButton = findViewById<Button>(R.id.pagar_recorrente)
                        pagarRecorrenteButton.setBackgroundColor(corBotaoDesmarcado)
                        findViewById<Button>(R.id.pagar_a_vista).visibility = View.GONE
                        findViewById<Button>(R.id.pagar_mensal).visibility = View.GONE
                        findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE

                        pagarRecorrenteButton.setOnClickListener {
                            Toast.makeText(this, "A forma de pagamento não pode ser alterada!", Toast.LENGTH_LONG).show()
                            registrarDespesaRecorrente()
                        }
                        val limparDataButton = findViewById<Button>(R.id.limpar_data_button)
                        limparDataButton.visibility = View.VISIBLE

                        // Botão "Limpar Data" para limpar a data selecionada
                        limparDataButton.setOnClickListener {
                            selectedDateEfetuacao = null

                            toggleSwitch.isChecked = false

                            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.VISIBLE

                            // Esconda o botão "Limpar Data"
                            findViewById<Button>(R.id.limpar_data_button).visibility = View.GONE

                            // Esconda o TextView com a data selecionada
                            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE
                        }

                        // Botão "Outros" para abrir o DatePickerDialog
                        val abrirCalendarioDataEfetuacaoButton = findViewById<Button>(R.id.abrir_calendario_data_efetuacao)
                        abrirCalendarioDataEfetuacaoButton.setOnClickListener {
                            calendarioRecorrente() // Chama o método para exibir o DatePickerDialog
                        }
                    }

                    val dataEfetuacaoTimestamp = data?.get("data_efetuacao") as? com.google.firebase.Timestamp
                    if (dataEfetuacaoTimestamp != null) {
                        dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                        val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dataFormatada = formatoData.format(dataEfetuacao)
                        findViewById<TextView>(R.id.data_efetuacao_textview).text = dataFormatada

                        // Criar um objeto Calendar e definir a data
                        val calendar = Calendar.getInstance()
                        calendar.time = dataEfetuacao

                        // Armazenar a data temporariamente na classe de dados temporários
                        DadosTemporariosIntermediarios.dataEfetuacaoTemporaria = calendar

                        // Armazena a data original recuperada do Firestore
                        selectedDateEfetuacao = Calendar.getInstance()
                        selectedDateEfetuacao?.time = dataEfetuacao
                    }

                    toggleSwitch = findViewById(R.id.toggleSwitch)
                    val valorJaPago = data?.get("valor_ja_pago") as? String // Supondo que o valor do Firebase seja uma String

                    if (valorJaPago == "Pago") {
                        toggleSwitch.isChecked = true // Define o switch como ativado se for "Pago"
                    } else {
                        toggleSwitch.isChecked = false // Define o switch como desativado caso contrário
                    }

                    val iconeResId = data?.get("iconeResId") as? Long ?: 0
                    val corCirculo = data?.get("corCirculo") as? Long ?: 0

                    val categoriaImageView = findViewById<ImageView>(R.id.categoria_de_salario)
                    val circuloImageView = findViewById<ImageView>(R.id.Circulo_cor)

                    categoriaImageView.setImageResource(iconeResId.toInt())
                    circuloImageView.setColorFilter(corCirculo.toInt())
                } else {
                    // Lidar com a situação em que a receita não pertence ao usuário logado
                }
            }
        }
        // Obtenha as referências dos campos de entrada
        valorDaDespesaEditText = findViewById(R.id.valor_da_despesa)
        val textInputLayout: TextInputLayout = findViewById(R.id.Nome_da_despesa)
        autoCompleteTextView = textInputLayout.findViewById(R.id.auto_completa_texto)
        val textInputLayout2: TextInputLayout = findViewById(R.id.Nome_do_favorecido)
        autoCompleteTextViewFavorecido = textInputLayout2.findViewById(R.id.auto_completa_texto_favorecidos)
        descricaoDaDespesaEditText = findViewById(R.id.Descricao_da_despesa)

        val irParaCategoriasDasReceitas = findViewById<ImageView>(R.id.Ir_para_tela_escolher_categorias_despesas)
        irParaCategoriasDasReceitas.setOnClickListener {
            // Salvar os valores no SharedPreferences
            abrirTelaDasCategoriasDasReceitas(despesasId)
        }
        // Obtenha a referência para o EditText
        val valorDaReceitaEditText = findViewById<EditText>(R.id.valor_da_despesa)
        valorDaReceitaEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        // Aplicando o estilo EditTextHintGreen ao EditText
        valorDaReceitaEditText.setTextAppearance(R.style.HintStyle)

        // Defina a cor verde também para o hint do EditText usando um SpannableString
        val hint = "R$ 0,00"
        val spannableString = SpannableString(hint)
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red)),
            0,
            hint.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        valorDaReceitaEditText.hint = spannableString

        val deletarDespesaTextView = findViewById<TextView>(R.id.deletar_despesa_textview)
        deletarDespesaTextView.setOnClickListener {
            // Verifique se a forma de pagamento é "Mensal"
            if (formaPagamento == "Mensal") {
                // Se for "Mensal", exiba um diálogo de confirmação
                exibirDialogConfirmacaoMensal(grupoDoId)
            }
            else if(formaPagamento == "Recorrente"){
                exibirDialogConfirmacaoMensal(grupoDoId)
            }
            else{
                exibirDialogConfirmacao()
            }
        }
        val editarReceitaButton = findViewById<Button>(R.id.Salvar_despesa)
        editarReceitaButton.setOnClickListener {
            // Chame o método para atualizar os dados do registro no Firestore
            if(formaPagamento == "Mensal" || formaPagamento == "Recorrente") {
                exibirDialogoOpcoesEdicao(despesasId)
            }
            else {
                atualizarRegistroNoFirestore(despesasId)
            }
        }
        carregarNomesDespesasDoUsuario()
        carregarNomesFavorecidosDoUsuario()
    }
    class DadosTemporariosIntermediarios {
        companion object {
            var recurrenceOption: String = ""
            var period: Int = 0
            var valueDespesa: Double = 0.0
            var valorRecorrente: Double = 0.0
            var calculoDoMensal: Double = 0.0
            var parcelasMensais: Int = 0
            var categoriaNome: String = ""
            var iconeCategoria: Int = 0
            var corCategoria: Int = 0
            var dataEfetuacaoTemporaria: Calendar? = null
        }
    }
    private fun exibirDialogoOpcoesEdicao(despesasId: String) {
        val opcoes = arrayOf(
            "Alterar somente esta despesa",
            "Alterar esta e próximas parcelas",
            "Alterar todas as parcelas"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Escolha a opção de edição:")
        builder.setItems(opcoes) { dialog, which ->
            when (which) {
                0 -> {
                    // Lógica para "Alterar Somente Esta Despesa"
                    atualizarRegistroNoFirestore(despesasId)
                }
                1 -> {
                    // Lógica para "Alterar Esta e Próximas Parcelas"
                    atualizarParaEstaEProximas(despesasId)
                }
                2 -> {
                    // Lógica para "Alterar Todas as Parcelas"
                    atualizarRegistroNoFirestoreTodasParcelas(despesasId)
                }
            }
        }
        builder.show()
    }
    private fun atualizarRegistroNoFirestoreTodasParcelas(despesasId: String){
        val firestore = FirebaseFirestore.getInstance()
        val selectedTimestamp = selectedDateEfetuacao?.let {
            dataEfetuacaoInMillis = it.timeInMillis // Atualize o valor da variável
            Timestamp(it.time)
        }
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

        val defaultTimestamp = Timestamp(Date(0)) // Substitua a data e hora desejadas

        // Crie um mapa com os dados que serão atualizados apenas para o documento atual
        val atualizacaoMapDocumentoAtual = hashMapOf<String, Any>(
            "data_efetuacao" to (selectedTimestamp ?: defaultTimestamp)
        )

        // Atualize o documento no Firestore apenas para o documento atual (atualizacaoMapDocumentoAtual)
        firestore.collection("despesas").document(despesasId)
            .update(atualizacaoMapDocumentoAtual)
            .addOnSuccessListener {
                // Registro da despesa atual atualizado com sucesso.
                Toast.makeText(this, "Despesa editada com sucesso!", Toast.LENGTH_SHORT).show()

                val recebido = toggleSwitch.isChecked

                // Certifique-se de que os valores são do tipo certo
                val valorDaReceita = findViewById<EditText>(R.id.valor_da_despesa).text.toString().toDouble()
                val categoriaEscolhida = findViewById<TextView>(R.id.categoria_salario).text.toString()
                val nomeDaReceita = findViewById<EditText>(R.id.auto_completa_texto).text.toString()
                val descricaoDaReceita = findViewById<EditText>(R.id.Descricao_da_despesa).text.toString()
                val nomeFavorecido = findViewById<EditText>(R.id.auto_completa_texto_favorecidos).text.toString()
                val numeroDeParcelas = DadosTemporariosIntermediarios.parcelasMensais
                val valorDoCaculo =  DadosTemporariosIntermediarios.calculoDoMensal
                val recurrenceOption = DadosTemporariosIntermediarios.recurrenceOption
                val period = DadosTemporariosIntermediarios.period
                val valorRecorrente = DadosTemporariosIntermediarios.valorRecorrente

                val iconeCategoria = DadosTemporariosIntermediarios.iconeCategoria
                val corCategoria = DadosTemporariosIntermediarios.corCategoria


                // Defina o valor "Recebido" ou "Não Recebido" com base no toggleSwitch
                val statusRecebimento = if (recebido) "Pago" else "Não pago"
                val defaultTimestamp = Timestamp(Date(0)) // Substitua a data e hora desejadas

                // Crie um mapa com os dados que serão atualizados
                val atualizacaoMap = hashMapOf<String, Any>(
                    "valor_despesa" to valorDaReceita,
                    "categoria_escolhida_pelo_usuario" to categoriaEscolhida,
                    "nome_despesa" to nomeDaReceita,
                    "descricao" to descricaoDaReceita,
                    "nome_favorecido" to nomeFavorecido,
                    "valor_ja_pago" to statusRecebimento,
                    "valor_das_parcelas_calculado" to valorDoCaculo,
                    "numero_de_parcelas" to numeroDeParcelas,
                    "valor_do_calculo_recorrente" to valorRecorrente,
                    "tempo_recorrente" to period,
                    "opcao_recorrente" to recurrenceOption,
                    "forma_pagamento" to formaPagamento,
                    "iconeResId" to iconeCategoria, // Adicione esta linha para o ícone
                    "corCirculo" to corCategoria // Adicione esta linha para a cor
                )
                // Agora atualize os documentos para as próximas parcelas (atualizacaoMap)
                firestore.collection("despesas")
                    .whereEqualTo("grupo_id", grupoDoId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val documentId = document.id

                            // Atualize cada documento
                            firestore.collection("despesas").document(documentId)
                                .update(atualizacaoMap)
                                .addOnSuccessListener {
                                    // Registro da próxima despesa atualizado com sucesso.
                                    // Você pode adicionar aqui um feedback ao usuário se desejar.
                                }
                                .addOnFailureListener {
                                    // Falha na atualização
                                    Log.e(ContentValues.TAG, "Erro ao atualizar registro: $it")
                                    // Você pode exibir uma mensagem de erro ao usuário
                                }
                        }

                        // Informe ao usuário que os registros foram atualizados
                        Toast.makeText(this, "Despesas editadas em lote com sucesso!", Toast.LENGTH_SHORT).show()

                        // Redirecione para a tela principal ou outra tela desejada
                        abrirTelaPrincipal()
                    }
                    .addOnFailureListener { e ->
                        // Lide com a falha na consulta
                        Toast.makeText(this, "Falha ao consultar despesas: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                // Falha na atualização do documento atual
                Log.e(ContentValues.TAG, "Erro ao atualizar registro: $it")
                // Você pode exibir uma mensagem de erro ao usuário
            }
    }

    private fun atualizarParaEstaEProximas(despesasId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val selectedTimestamp = selectedDateEfetuacao?.let {
            dataEfetuacaoInMillis = it.timeInMillis // Atualize o valor da variável
            Timestamp(it.time)
        }
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

        val defaultTimestamp = Timestamp(Date(0)) // Substitua a data e hora desejadas

        // Crie um mapa com os dados que serão atualizados apenas para o documento atual
        val atualizacaoMapDocumentoAtual = hashMapOf<String, Any>(
            "data_efetuacao" to (selectedTimestamp ?: defaultTimestamp)
        )
        Log.d("DEBUG", "Atualizando com atualizacaoMapDocumentoAtual: $atualizacaoMapDocumentoAtual")

        // Atualize o documento no Firestore apenas para o documento atual (atualizacaoMapDocumentoAtual)
        firestore.collection("despesas").document(despesasId)
            .update(atualizacaoMapDocumentoAtual)
            .addOnSuccessListener {
                Log.d("DEBUG", "Atualização com atualizacaoMapDocumentoAtual bem-sucedida")

                // Registro da despesa atual atualizado com sucesso.
                Toast.makeText(this, "Despesa editada com sucesso!", Toast.LENGTH_SHORT).show()
                            val recebido = toggleSwitch.isChecked

                            // Certifique-se de que os valores são do tipo certo
                            val valorDaReceita = findViewById<EditText>(R.id.valor_da_despesa).text.toString().toDouble()
                            val categoriaEscolhida = findViewById<TextView>(R.id.categoria_salario).text.toString()
                            val nomeDaReceita = findViewById<EditText>(R.id.auto_completa_texto).text.toString()
                            val descricaoDaReceita = findViewById<EditText>(R.id.Descricao_da_despesa).text.toString()
                            val nomeFavorecido = findViewById<EditText>(R.id.auto_completa_texto_favorecidos).text.toString()
                            val numeroDeParcelas = DadosTemporariosIntermediarios.parcelasMensais
                            val valorDoCaculo =  DadosTemporariosIntermediarios.calculoDoMensal
                            val recurrenceOption = DadosTemporariosIntermediarios.recurrenceOption
                            val period = DadosTemporariosIntermediarios.period
                            val valorRecorrente = DadosTemporariosIntermediarios.valorRecorrente

                            val iconeCategoria = DadosTemporariosIntermediarios.iconeCategoria
                            val corCategoria = DadosTemporariosIntermediarios.corCategoria


                            // Defina o valor "Recebido" ou "Não Recebido" com base no toggleSwitch
                            val statusRecebimento = if (recebido) "Pago" else "Não pago"
                            val defaultTimestamp = Timestamp(Date(0)) // Substitua a data e hora desejadas

                            // Crie um mapa com os dados que serão atualizados
                            val atualizacaoMap = hashMapOf<String, Any>(
                                "valor_despesa" to valorDaReceita,
                                "categoria_escolhida_pelo_usuario" to categoriaEscolhida,
                                "nome_despesa" to nomeDaReceita,
                                "descricao" to descricaoDaReceita,
                                "nome_favorecido" to nomeFavorecido,
                                "valor_ja_pago" to statusRecebimento,
                                "valor_das_parcelas_calculado" to valorDoCaculo,
                                "numero_de_parcelas" to numeroDeParcelas,
                                "valor_do_calculo_recorrente" to valorRecorrente,
                                "tempo_recorrente" to period,
                                "opcao_recorrente" to recurrenceOption,
                                "forma_pagamento" to formaPagamento,
                                "iconeResId" to iconeCategoria, // Adicione esta linha para o ícone
                                "corCirculo" to corCategoria, // Adicione esta linha para a cor7
                            )
                atualizarDespesasPorGrupoEData(grupoDoId, atualizacaoMap)
            }
            .addOnFailureListener {
                // Falha na atualização do documento atual
                Log.e(ContentValues.TAG, "Erro ao atualizar registro: $it")
                // Você pode exibir uma mensagem de erro ao usuário
            }
    }
    private fun atualizarDespesasPorGrupoEData(
        grupoId: String,
        atualizacaoMap: Map<String, Any>
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDateEfetuacao?.time?.time ?: System.currentTimeMillis()

        val dataVerificar = Timestamp(calendar.time)

        firestore.collection("despesas")
            .whereEqualTo("grupo_id", grupoId)
            .whereGreaterThanOrEqualTo("data_efetuacao", dataVerificar)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val documentId = document.id

                    firestore.collection("despesas").document(documentId)
                        .update(atualizacaoMap)
                        .addOnSuccessListener {
                            Log.d("DEBUG", "Atualização bem-sucedida para o documento $documentId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ERROR", "Falha na atualização para o documento $documentId: $e")
                        }
                }

                // Informe ao usuário que os registros foram atualizados
                Toast.makeText(this, "Despesas editadas em lote com sucesso!", Toast.LENGTH_SHORT).show()

                // Redirecione para a tela principal ou outra tela desejada
                abrirTelaPrincipal()
            }
            .addOnFailureListener { e ->
                // Lide com a falha na consulta
                Toast.makeText(this, "Falha ao consultar despesas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun calendarioRecorrente() {
        // Recupere o valor de data do Firebase Firestore como um Calendar?
        val dataEfetuacaoCalendar = DadosTemporariosIntermediarios.dataEfetuacaoTemporaria

        // Use o objeto Calendar para definir a data padrão no DatePickerDialog
        val calendar = Calendar.getInstance()

        if (dataEfetuacaoCalendar != null) {
            val year = dataEfetuacaoCalendar.get(Calendar.YEAR)
            val month = dataEfetuacaoCalendar.get(Calendar.MONTH)
            val dayOfMonth = dataEfetuacaoCalendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, dayOfMonth)
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            // Verifique se a data selecionada está dentro do mês do Firestore
            val firestoreMonth = calendar.get(Calendar.MONTH)
            val firestoreYear = calendar.get(Calendar.YEAR)

            if (year == firestoreYear && month == firestoreMonth) {
                // A data está dentro do mês do Firestore, permita a seleção
                // Aqui você pode atualizar a data da despesa ou realizar qualquer ação necessária
                if (selectedCalendar.before(selectedCalendar) || selectedCalendar == selectedCalendar) {
                    toggleSwitch.isChecked = true // Ativa o toggle
                } else {
                    toggleSwitch.isChecked = false // Deixa o toggle desativado
                }
                findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE

                // Exiba o botão "Limpar Data"
                findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE

                selectedDateEfetuacao = selectedCalendar
                updateDataEfetuacaoTextView(selectedDateEfetuacao)
                dataEfetuacaoInMillis = selectedDateEfetuacao?.timeInMillis ?: 0

                // Após selecionar a data, exiba o campo de texto com a data selecionada
                findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
            } else {
                // A data não está dentro do mês do Firestore, exiba uma mensagem de erro
                Toast.makeText(this, "Selecione uma data dentro do mês atual", Toast.LENGTH_LONG).show()
            }
        }, year, month, dayOfMonth)

        // Exiba o diálogo seletor de data.
        datePickerDialog.show()
    }
    private fun exibirDialogConfirmacao() {
        val despesasId = intent.getStringExtra("despesa_id") ?: ""

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmacao, null)

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = alertDialogBuilder.show()

        val btnSim = dialogView.findViewById<Button>(R.id.btn_sim)
        val btnNao = dialogView.findViewById<Button>(R.id.btn_nao)

        btnSim.setOnClickListener {
            // Executar ação de exclusão
            deletarDespesa(despesasId)
            alertDialog.dismiss()
        }

        btnNao.setOnClickListener {
            alertDialog.dismiss()
        }
    }
    // Função para deletar a despesa
    private fun deletarDespesa(despesaId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Referência ao documento da despesa que será deletada
        val despesaRef = firestore.collection("despesas").document(despesaId)

        despesaRef.delete()
            .addOnSuccessListener {
                // Despesa deletada com sucesso
                Toast.makeText(this, "Despesa deletada com sucesso!", Toast.LENGTH_SHORT).show()
                abrirTelaPrincipal()
            }
            .addOnFailureListener { e ->
                // Falha ao deletar a despesa
                Log.e(TAG, "Erro ao deletar despesa: ${e.message}")
                Toast.makeText(this, "Erro ao deletar despesa", Toast.LENGTH_SHORT).show()
            }
    }
    private fun exibirDialogConfirmacaoMensal(grupoDoId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmação")
            .setMessage("Atenção! Ao deletar essa despesa, todos os registros feitos serão deletadas. Deseja continuar?")
            .setPositiveButton("Sim") { _, _ ->
                // Se o usuário escolher "Sim", confirme a exclusão
                confirmarExclusaoDespesaMensal(grupoDoId)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun confirmarExclusaoDespesaMensal(grupoDoId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Realize uma consulta para obter todas as despesas com o mesmo grupo_id
        val query = firestore.collection("despesas").whereEqualTo("grupo_id", grupoDoId)

        query.get().addOnSuccessListener { documents ->
            for (document in documents) {
                // Para cada despesa no grupo, exclua o documento
                val despesaId = document.id
                val despesaRef = firestore.collection("despesas").document(despesaId)
                despesaRef.delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Despesa $despesaId deletada com sucesso")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Erro ao deletar despesa $despesaId: ${e.message}")
                    }
            }

            // Após excluir todas as despesas do grupo, abra a tela principal
            abrirTelaPrincipal()
        }
    }
    private fun abrirTelaDasCategoriasDasReceitas(despesasId: String) {
        val intent = Intent(this, activity_tela_editar_despesa_escolher_categorias::class.java)
        intent.putExtra("despesa_id", despesasId) // Passe o ID da receita para a próxima tela
        startActivityForResult(intent, SEU_CODIGO_DE_REQUEST)
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

            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDateEfetuacao = selectedCalendar

            // Verifica se a data selecionada é anterior à data atual
            val currentDate = Calendar.getInstance()
            if (selectedCalendar.before(currentDate)) {
                toggleSwitch.isChecked = true // Ativa o toggle
            } else if (selectedCalendar == currentDate) {
                toggleSwitch.isChecked = true // Ativa o toggle
            } else {
                toggleSwitch.isChecked = false // Deixa o toggle desativado
            }

            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE

            // Exiba o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE

            // Atualize o campo de texto ou realize qualquer ação adicional que você deseje
            // após a seleção da data.
            updateDataEfetuacaoTextView(selectedDateEfetuacao)
            dataEfetuacaoInMillis = selectedDateEfetuacao?.timeInMillis ?: 0

            // Após selecionar a data, exiba o campo de texto com a data selecionada
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
        }, year, month, dayOfMonth)

        // Exiba o diálogo seletor de data.
        datePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == SEU_CODIGO_DE_REQUEST) {
            val novaCategoria = data?.getStringExtra("categoria_selecionada")
            val novoIconeResId = data?.getIntExtra("icone_res_id", 0) ?: 0
            val novaCorCirculo = data?.getIntExtra("circulo_cor", 0) ?: 0

            if (novaCategoria != null && novoIconeResId != 0 && novaCorCirculo != 0) {
                val categoriaImageView = findViewById<ImageView>(R.id.categoria_de_salario)
                categoriaImageView.setImageResource(novoIconeResId)

                val circuloImageView = findViewById<ImageView>(R.id.Circulo_cor)
                circuloImageView.setColorFilter(novaCorCirculo)

                val textViewCategorias = findViewById<TextView>(R.id.categoria_salario)
                textViewCategorias.text = novaCategoria

                DadosTemporariosIntermediarios.categoriaNome = textViewCategorias.toString()
                DadosTemporariosIntermediarios.iconeCategoria = novoIconeResId
                DadosTemporariosIntermediarios.corCategoria = novaCorCirculo
            }
        }
    }
    private fun abrirTelaPrincipal() {
        val intent = Intent(this, TelaPrincipal::class.java)
        startActivity(intent)
    }
    private fun atualizarRegistroNoFirestore(despesasId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val selectedTimestamp = selectedDateEfetuacao?.let {
            dataEfetuacaoInMillis = it.timeInMillis // Atualize o valor da variável
            Timestamp(it.time)
        }
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
        val recebido = toggleSwitch.isChecked

        // Certifique-se de que os valores são do tipo certo
        val valorDaReceita = findViewById<EditText>(R.id.valor_da_despesa).text.toString().toDouble()
        val categoriaEscolhida = findViewById<TextView>(R.id.categoria_salario).text.toString()
        val nomeDaReceita = findViewById<EditText>(R.id.auto_completa_texto).text.toString()
        val descricaoDaReceita = findViewById<EditText>(R.id.Descricao_da_despesa).text.toString()
        val nomeFavorecido = findViewById<EditText>(R.id.auto_completa_texto_favorecidos).text.toString()
        val numeroDeParcelas = DadosTemporariosIntermediarios.parcelasMensais
        val valorDoCaculo =  DadosTemporariosIntermediarios.calculoDoMensal
        val recurrenceOption = DadosTemporariosIntermediarios.recurrenceOption
        val period = DadosTemporariosIntermediarios.period
        val valorRecorrente = DadosTemporariosIntermediarios.valorRecorrente

        val iconeCategoria = DadosTemporariosIntermediarios.iconeCategoria
        val corCategoria = DadosTemporariosIntermediarios.corCategoria


        // Defina o valor "Recebido" ou "Não Recebido" com base no toggleSwitch
        val statusRecebimento = if (recebido) "Pago" else "Não pago"
        val defaultTimestamp = Timestamp(Date(0)) // Substitua a data e hora desejadas

        // Crie um mapa com os dados que serão atualizados
        val atualizacaoMap = hashMapOf<String, Any>(
            "valor_despesa" to valorDaReceita,
            "categoria_escolhida_pelo_usuario" to categoriaEscolhida,
            "nome_despesa" to nomeDaReceita,
            "descricao" to descricaoDaReceita,
            "nome_favorecido" to nomeFavorecido,
            "data_efetuacao" to (selectedTimestamp ?: defaultTimestamp),
            "valor_ja_pago" to statusRecebimento,
            "valor_das_parcelas_calculado" to valorDoCaculo,
            "numero_de_parcelas" to numeroDeParcelas,
            "valor_do_calculo_recorrente" to valorRecorrente,
            "tempo_recorrente" to period,
            "opcao_recorrente" to recurrenceOption,
            "forma_pagamento" to formaPagamento,
            "iconeResId" to iconeCategoria, // Adicione esta linha para o ícone
            "corCirculo" to corCategoria // Adicione esta linha para a cor
        )

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

        // Atualize o documento no Firestore
        firestore.collection("despesas").document(despesasId)
            .update(atualizacaoMap)
            .addOnSuccessListener {
                // Registro da receita concluído com sucesso.
                Toast.makeText(this, "Despesa editada com sucesso!", Toast.LENGTH_SHORT).show()

                DadosTemporariosIntermediarios.recurrenceOption = ""
                DadosTemporariosIntermediarios.period = 0
                DadosTemporariosIntermediarios.valorRecorrente = 0.0
                DadosTemporariosIntermediarios.parcelasMensais = 0
                DadosTemporariosIntermediarios.calculoDoMensal = 0.0
                toggleSwitch.isChecked = false

                abrirTelaPrincipal()
            }
            .addOnFailureListener {
                // Falha na atualização
                Log.e(ContentValues.TAG, "Erro ao atualizar registro: $it")
                // Você pode exibir uma mensagem de erro ao usuário
            }
    }
    private fun registrarDespesaAVista() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val pagarAVistaButton = findViewById<Button>(R.id.pagar_a_vista)
        val corBotaoPadrao = ContextCompat.getColor(this, R.color.colorButtonAVista)
        val corBotaoDesmarcado = ContextCompat.getColor(this, R.color.colorButtonPadrao)

        if (isPagarAVistaSelected) {
            // Selecionar "à vista" e mudar a cor do botão
            isPagarAVistaSelected = true
            pagarAVistaButton.setBackgroundColor(corBotaoDesmarcado)
            findViewById<Button>(R.id.pagar_mensal).visibility = View.GONE
            findViewById<Button>(R.id.pagar_recorrente).visibility = View.GONE
            formaPagamento = "À vista"
        }
    }
    private fun registrarDespesaMensal(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val pagarMensalButton = findViewById<Button>(R.id.pagar_mensal)
        val corBotaoPadrao = ContextCompat.getColor(this, R.color.colorButtonAVista)
        val corBotaoDesmarcado = ContextCompat.getColor(this, R.color.colorButtonPadrao)

        if (isPagarEmMensalVisible) {
            // Selecionar "à vista" e mudar a cor do botão
            isPagarEmMensalVisible = true
            pagarMensalButton.setBackgroundColor(corBotaoDesmarcado)
            findViewById<Button>(R.id.pagar_a_vista).visibility = View.GONE
            findViewById<Button>(R.id.pagar_recorrente).visibility = View.GONE
            formaPagamento = "Mensal"
        }
    }
    private fun registrarDespesaRecorrente(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val pagarMensalButton = findViewById<Button>(R.id.pagar_recorrente)
        val corBotaoPadrao = ContextCompat.getColor(this, R.color.colorButtonAVista)
        val corBotaoDesmarcado = ContextCompat.getColor(this, R.color.colorButtonPadrao)

        if (isPagamentoRecorrenteSelecionado) {
            // Selecionar "à vista" e mudar a cor do botão
            isPagamentoRecorrenteSelecionado = true
            pagarMensalButton.setBackgroundColor(corBotaoDesmarcado)
            findViewById<Button>(R.id.pagar_a_vista).visibility = View.GONE
            findViewById<Button>(R.id.pagar_mensal).visibility = View.GONE
            formaPagamento = "Recorrente"
        }
    }

    private fun formatCurrencyValue(value: Double): String {
        val decimalFormat = DecimalFormat.getCurrencyInstance()
        return decimalFormat.format(value)
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

                                    val adapter = ArrayAdapter(this@activity_tela_editar_as_despesas, android.R.layout.simple_dropdown_item_1line, filtro)
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

                                    val adapter = ArrayAdapter(this@activity_tela_editar_as_despesas, android.R.layout.simple_dropdown_item_1line, filtro)
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