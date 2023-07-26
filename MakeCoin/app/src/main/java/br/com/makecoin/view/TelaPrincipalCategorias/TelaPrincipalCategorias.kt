package br.com.makecoin.view.TelaPrincipalCategorias

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.com.makecoin.R
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TelaPrincipalCategorias : AppCompatActivity() {
    private var selectedDate: Calendar? = null
    private lateinit var registrarDataHojeDataEfetuacaoButton: Button
    private lateinit var registrarDataHojeDataVencimentoButton: Button
    private var formattedValue: Double? = null
    private val data = HashMap<String, Any>()
    private var formaPagamento: String = "" // Inicializa com uma string vazia
    private var isPagarAVistaSelected = false // Variável para rastrear o estado selecionado
    private var isPagarEmMensalVisible = false
    private lateinit var resultadoMensalTextView: TextView
    private var valorDespesaForMensalCalculation: Double = 0.0
    private var valorMensal: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_principal_categorias)

        val voltarTelaPrincipal = findViewById<ImageView>(R.id.Voltar_tela_inicial)
        voltarTelaPrincipal.setOnClickListener {
            abrirTelaPrincipal()
        }
        val abrirPagarDespesasRecorrentes = findViewById<Button>(R.id.pagar_recorrente)
        abrirPagarDespesasRecorrentes.setOnClickListener{
            showRecurrenceOptionsDialog()
        }
        // Botão "Outros" para abrir o DatePickerDialog
        val abrirCalendarioDataEfetuacaoButton = findViewById<Button>(R.id.abrir_calendario_data_efetuacao)
        abrirCalendarioDataEfetuacaoButton.setOnClickListener {
            openDatePickerDialog() // Chama o método para exibir o DatePickerDialog
        }

        // Botão "Hoje" para definir a data como hoje
        registrarDataHojeDataEfetuacaoButton = findViewById<Button>(R.id.registrar_hoje_data_efetuacao)
        registrarDataHojeDataEfetuacaoButton.setOnClickListener {
            selectedDate = Calendar.getInstance()
            updateDataEfetuacaoTextView(selectedDate)

            // Esconda os botões "Hoje" e "Outros" após definir a data manualmente
            findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.GONE
            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE

            // Exiba o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE

            // Exiba o TextView com a data selecionada
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
        }

        // Botão "Limpar Data" para limpar a data selecionada
        val limparDataButton = findViewById<Button>(R.id.limpar_data_button)
        limparDataButton.setOnClickListener {
            selectedDate = null

            // Exiba os botões "Hoje" e "Outros" novamente
            findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.VISIBLE
            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.VISIBLE

            // Esconda o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button).visibility = View.GONE

            // Esconda o TextView com a data selecionada
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE
        }
        // Defina a visibilidade do botão "Limpar Data" com base no estado da variável selectedDate
        if (selectedDate != null) {
            limparDataButton.visibility = View.VISIBLE
            updateDataEfetuacaoTextView(selectedDate)
        } else {
            limparDataButton.visibility = View.GONE
        }
        // Obtenha a referência para o EditText
        val valorDaDespesaEditText = findViewById<EditText>(R.id.valor_da_despesa)

        // Crie um SpannableString com o hint e defina a cor vermelha
        val hint = "R$ 0,00"
        val spannableString = SpannableString(hint)
        spannableString.setSpan(ForegroundColorSpan(Color.RED), 0, hint.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Defina o SpannableString como o hint do EditText
        valorDaDespesaEditText.hint = spannableString



        val registrarDespesaButton = findViewById<Button>(R.id.Registrar_despesa)
        registrarDespesaButton.setOnClickListener {
            // Obtenha os valores inseridos nos campos
            val valorDespesaText = valorDaDespesaEditText.text.toString()

            // Verifique se o valor da despesa é válido
            if (valorDespesaText.isEmpty()) {
                Toast.makeText(this, "Insira um valor para a despesa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Converta o valor da despesa de String para Double
            try {
                formattedValue = valorDespesaText.replace("R", "")
                    .replace("$", "")
                    .replace(" ", "")
                    .replace(",", ".")
                    .toDouble()

            } catch (e: NumberFormatException) {
                formattedValue = null
            }

            // Verifique se a conversão foi bem-sucedida e se o valor é maior que zero
            if (formattedValue == null || formattedValue!! <= 0) {
                Toast.makeText(this, "Insira um valor válido para a despesa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Format o valor da despesa com duas casas decimais
            val valorDespesaFormatado = formatCurrencyValue(formattedValue!!)


            // Obtenha o nome da despesa
            val nomeDespesa = findViewById<EditText>(R.id.Nome_da_despesa).text.toString()

            // Verifique se o nome da despesa foi preenchido
            if (nomeDespesa.isEmpty()) {
                Toast.makeText(this, "O campo 'Nome da despesa' é obrigatório", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtenha a descrição da despesa
            val descricaoDespesa = findViewById<EditText>(R.id.Descricao_da_despesa).text.toString()

            // Crie um mapa com os dados a serem salvos no Firestore
            val data = hashMapOf(
                "valor_despesa" to valorDespesaFormatado,
                "nome_despesa" to nomeDespesa
            )

            // Adicione a descrição ao mapa, se estiver preenchida
            if (descricaoDespesa.isNotEmpty()) {
                data["descricao"] = descricaoDespesa
            }

            // Adicione a data e hora ao mapa, se selecionada
            if (selectedDate != null) {
                val dataHoraFormatada = obterDataHoraFormatada(selectedDate!!)
                data["data_efetuacao"] = dataHoraFormatada
            }

            data["forma_pagamento"] = formaPagamento

            // Acesse a instância do Firestore e salve os dados na coleção "despesas"
            FirebaseFirestore.getInstance()
                .collection("despesas")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Despesa registrada com sucesso", Toast.LENGTH_SHORT).show()
                    abrirTelaPrincipal()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Falha ao registrar a despesa: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Botão "Outros" para abrir o DatePickerDialog para a data de vencimento
        val abrirCalendarioDataVencimentoButton = findViewById<Button>(R.id.abrir_calendario_data_vencimento)
        abrirCalendarioDataVencimentoButton.setOnClickListener {
            abrirDatePickerVencimento() // Chama o método para exibir o DatePickerDialog para a data de vencimento
        }

        // Botão "Hoje" para definir a data como hoje para a data de vencimento
        registrarDataHojeDataVencimentoButton = findViewById<Button>(R.id.registrar_data_hoje_data_vencimento)
        registrarDataHojeDataVencimentoButton.setOnClickListener {
            selectedDate = Calendar.getInstance()
            updateDataVencimentoTextView(selectedDate)

            // Esconda os botões "Hoje" e "Outros" após definir a data manualmente
            findViewById<Button>(R.id.registrar_data_hoje_data_vencimento).visibility = View.GONE
            findViewById<Button>(R.id.abrir_calendario_data_vencimento).visibility = View.GONE

            // Exiba o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button_vencimento).visibility = View.VISIBLE

            // Exiba o TextView com a data selecionada
            findViewById<TextView>(R.id.data_vencimento_textview).visibility = View.VISIBLE
        }

        // Botão "Limpar Data" para limpar a data selecionada para a data de vencimento
        val limparDataVencimentoButton = findViewById<Button>(R.id.limpar_data_button_vencimento)
        limparDataVencimentoButton.setOnClickListener {
            selectedDate = null

            // Exiba os botões "Hoje" e "Outros" novamente
            findViewById<Button>(R.id.registrar_data_hoje_data_vencimento).visibility = View.VISIBLE
            findViewById<Button>(R.id.abrir_calendario_data_vencimento).visibility = View.VISIBLE

            // Esconda o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button_vencimento).visibility = View.GONE

            // Esconda o TextView com a data selecionada
            findViewById<TextView>(R.id.data_vencimento_textview).visibility = View.GONE
        }

        // Defina a visibilidade do botão "Limpar Data" com base no estado da variável selectedDate
        if (selectedDate != null) {
            limparDataVencimentoButton.visibility = View.VISIBLE
            updateDataVencimentoTextView(selectedDate)
        } else {
            limparDataVencimentoButton.visibility = View.GONE
        }

        val pagarAVistaButton = findViewById<Button>(R.id.pagar_a_vista)

        // Configure o OnClickListener para o botão "À vista"
        pagarAVistaButton.setOnClickListener {
            registrarDespesaAVista()
        }
        // Botão "Pagar Mensal" para calcular o pagamento mensal
        val pagarEmMensalButton = findViewById<Button>(R.id.pagar_mensal)
        pagarEmMensalButton.setOnClickListener {
            pagamentoMensal()
        }
    }
    //aqui seria uma função para calcular o pagamento a vista
    private fun registrarDespesaAVista() {
        val pagarAVistaButton = findViewById<Button>(R.id.pagar_a_vista)
        val corBotaoPadrao = ContextCompat.getColor(this, R.color.colorButtonAVista)
        val corBotaoDesmarcado = ContextCompat.getColor(this, R.color.colorButtonPadrao)

        if (isPagarAVistaSelected) {
            // Deselecionar "à vista" e voltar à cor original
            isPagarAVistaSelected = false
            pagarAVistaButton.setBackgroundColor(corBotaoPadrao)
            showDataVencimentoSection()
            formaPagamento = ""
        } else {
            // Selecionar "à vista" e mudar a cor do botão
            isPagarAVistaSelected = true
            pagarAVistaButton.setBackgroundColor(corBotaoDesmarcado)
            hideDataVencimentoSection()
            formaPagamento = "À vista"
        }
        updateFormaPagamento()
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
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDate = selectedCalendar

            // Esconda os botões "Outros" e "Hoje" após selecionar a data manualmente
            findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.GONE
            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE

            // Exiba o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE

            // Atualize o campo de texto ou realize qualquer ação adicional que você deseje
            // após a seleção da data.
            updateDataEfetuacaoTextView(selectedDate)

            // Após selecionar a data, exiba o campo de texto com a data selecionada
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
        }, year, month, dayOfMonth)

        // Defina uma data mínima para que o usuário não possa selecionar datas anteriores à data atual.
        datePickerDialog.datePicker.minDate = currentDate.timeInMillis

        // Exiba o diálogo seletor de data.
        datePickerDialog.show()
    }


    override fun onBackPressed() {
        // Impede que o usuário volte deslizando para a tela anterior
    }
    fun limparDataEfetuacaoButton(view: View) {
        selectedDate = null
        findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.VISIBLE
        findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.VISIBLE
        findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE
    }
    private fun updateDataVencimentoTextView(selectedDate: Calendar?) {
        if (selectedDate != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            findViewById<TextView>(R.id.data_vencimento_textview).text = formattedDate

            // Mostrar o botão "Limpar Data" quando uma data estiver selecionada
            findViewById<Button>(R.id.limpar_data_button_vencimento).visibility = View.VISIBLE
        } else {
            // Esconder o botão "Limpar Data" quando não houver data selecionada
            findViewById<Button>(R.id.limpar_data_button_vencimento).visibility = View.GONE
        }
    }

    // Função para abrir o DatePickerDialog para a data de vencimento
    private fun abrirDatePickerVencimento() {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDate = selectedCalendar

            // Esconda os botões "Outros" e "Hoje" após selecionar a data manualmente
            findViewById<Button>(R.id.registrar_data_hoje_data_vencimento).visibility = View.GONE
            findViewById<Button>(R.id.abrir_calendario_data_vencimento).visibility = View.GONE

            // Exiba o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button_vencimento).visibility = View.VISIBLE

            // Atualize o campo de texto ou realize qualquer ação adicional que você deseje
            // após a seleção da data.
            updateDataVencimentoTextView(selectedDate)

            // Após selecionar a data, exiba o campo de texto com a data selecionada
            findViewById<TextView>(R.id.data_vencimento_textview).visibility = View.VISIBLE
        }, year, month, dayOfMonth)

        // Defina uma data mínima para que o usuário não possa selecionar datas anteriores à data atual.
        datePickerDialog.datePicker.minDate = currentDate.timeInMillis

        // Exiba o diálogo seletor de data.
        datePickerDialog.show()
    }
    private fun abrirTelaPrincipal() {
        val intent = Intent(this, TelaPrincipal::class.java)
        startActivity(intent)
    }
    fun limparDataVencimentoButton(view: View) {
        selectedDate = null
        findViewById<Button>(R.id.registrar_data_hoje_data_vencimento).visibility = View.VISIBLE
        findViewById<Button>(R.id.abrir_calendario_data_vencimento).visibility = View.VISIBLE
        findViewById<TextView>(R.id.data_vencimento_textview).visibility = View.GONE
    }
    private fun formatCurrencyValue(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }
    private fun hideDataVencimentoSection() {
        // Esconda os elementos relacionados à data de vencimento
        findViewById<TextView>(R.id.Data_de_vencimento).visibility = View.GONE
        findViewById<View>(R.id.linha_de_data_vencimento).visibility = View.GONE
        findViewById<View>(R.id.img_calendario_vencimento).visibility = View.GONE
        findViewById<Button>(R.id.registrar_data_hoje_data_vencimento).visibility = View.GONE
        findViewById<Button>(R.id.abrir_calendario_data_vencimento).visibility = View.GONE
        findViewById<TextView>(R.id.data_vencimento_textview).visibility = View.GONE
        findViewById<Button>(R.id.limpar_data_button_vencimento).visibility = View.GONE
        findViewById<Button>(R.id.pagar_mensal).visibility = View.GONE
        findViewById<Button>(R.id.pagar_recorrente).visibility = View.GONE

    }
    private fun showDataVencimentoSection() {
        // Mostrar os elementos relacionados à data de vencimento
        findViewById<TextView>(R.id.Data_de_vencimento).visibility = View.VISIBLE
        findViewById<View>(R.id.linha_de_data_vencimento).visibility = View.VISIBLE
        findViewById<View>(R.id.img_calendario_vencimento).visibility = View.VISIBLE
        findViewById<Button>(R.id.registrar_data_hoje_data_vencimento).visibility = View.VISIBLE
        findViewById<Button>(R.id.abrir_calendario_data_vencimento).visibility = View.VISIBLE
        findViewById<Button>(R.id.pagar_mensal).visibility = View.VISIBLE
        findViewById<Button>(R.id.pagar_recorrente).visibility = View.VISIBLE
    }
    private fun pagamentoMensal() {
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
            pagarEmMensalButton.setBackgroundColor(corBotaoPadrao)
            formaPagamento = ""
            showBotoesPagamentoSection()
            findViewById<TextView>(R.id.resultado_mensal_textview).visibility = View.GONE
        } else {
            // Se não estava selecionado, marque, mude a cor do botão e exiba o cálculo do pagamento mensal
            isPagarEmMensalVisible = true
            pagarEmMensalButton.setBackgroundColor(corBotaoDesmarcado)
            showCalculateMensalValueDialog(valueDespesa)
            hideBotoesPagamentoSection()
            formaPagamento = "Mensal"
        }
        updateFormaPagamento()
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

                        // Exiba o resultado do cálculo no TextView do diálogo
                        val resultadoMensalDialogTextView = dialogView.findViewById<TextView>(R.id.tv_result)
                        resultadoMensalDialogTextView.text = String.format(Locale.US, "Valor mensal: R$ %.2f", valorDespesaForMensalCalculation)
                        resultadoMensalDialogTextView.visibility = View.VISIBLE
                    }
                }
            }
        })

        // Configurar o OnClickListener para o botão "Confirmar"
        btnConfirm.setOnClickListener {
            // Exibir o resultado do cálculo no TextView da tela principal
            val resultadoMensalTextView = findViewById<TextView>(R.id.resultado_mensal_textview)
            resultadoMensalTextView.text = String.format(Locale.US, "Valor mensal: R$ %.2f", valorDespesaForMensalCalculation)
            resultadoMensalTextView.visibility = View.VISIBLE
            // Salvar o valor mensal no HashMap data
            data["valor_mensal"] = valorMensal

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
        val options = arrayOf("Diário", "Semanal", "Quinzenal", "Mensal", "Bimestral", "Semestral", "Anual")
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
            "Diário" -> valueDespesa * period
            "Semanal" -> valueDespesa * (period * 7)
            "Quinzenal" -> valueDespesa * (period * 15)
            "Mensal" -> valueDespesa * period
            "Bimestral" -> valueDespesa * (period * 2)
            "Semestral" -> valueDespesa * (period * 6)
            "Anual" -> valueDespesa * period
            else -> valueDespesa // Recorrência desconhecida, retorne o valor original
        }

        // Exiba o resultado do cálculo em algum lugar da interface do usuário, como um TextView.
        val resultadoRecorrenteTextView = findViewById<TextView>(R.id.resultado_recorrente_textview)
        resultadoRecorrenteTextView.text = String.format(Locale.US, "Valor recorrente: R$ %.2f", valorRecorrente)
        resultadoRecorrenteTextView.visibility = View.VISIBLE
        hideBotoesPagamentoSection()
        formaPagamento = "Recorrente"

        updateFormaPagamento()
    }
    private fun updateFormaPagamento() {
        data["forma_pagamento"] = formaPagamento
    }
}
