package br.com.makecoin.view.telaEditarRegistros

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
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
import br.com.makecoin.R
import br.com.makecoin.view.TelaDasCategorias.activity_tela_das_categorias_receitas
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import br.com.makecoin.view.TelaPrincipalCategorias.activity_tela_principal_categorias_receitas
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class activity_tela_editar_os_registros_receitas : AppCompatActivity() {
    val SEU_CODIGO_DE_REQUEST = 1 // Pode ser qualquer número inteiro que você escolher
    private lateinit var toggleSwitch: Switch
    private var selectedDateEfetuacao: Calendar? = null
    private var dataEfetuacaoInMillis: Long = 0
    private var novoIconeResId: Int = 0
    private var novaCorCirculo: Int = 0
    private lateinit var autoCompleteTextViewReceita: AutoCompleteTextView
    private lateinit var autoCompleteTextViewFavorecidoReceita: AutoCompleteTextView
    private lateinit var valorDaReceitaEditText: EditText
    private lateinit var descricaoDaReceitaEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_editar_os_registros_receitas)

        // Obtenha o ID da receita da Intent
        val receitaId = intent.getStringExtra("receita_id") ?: ""

        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Verifique se o usuário está logado
        if (userId.isNullOrEmpty()) {
            // Lidar com a situação em que o usuário não está logado
            return
        }

        // Obtenha as referências dos campos de entrada
        valorDaReceitaEditText = findViewById(R.id.valor_da_receita)
        val textInputLayout: TextInputLayout = findViewById(R.id.Nome_da_receita)
        autoCompleteTextViewReceita = textInputLayout.findViewById(R.id.auto_completa_texto_receita)
        val textInputLayout2: TextInputLayout = findViewById(R.id.Nome_do_favorecido_receita)
        autoCompleteTextViewFavorecidoReceita = textInputLayout2.findViewById(R.id.auto_completa_texto_favorecido_receita)
        descricaoDaReceitaEditText = findViewById(R.id.Descricao_da_receita)

        // Referência para o Firestore
        val firestore = FirebaseFirestore.getInstance()

        // Referência ao documento da receita usando o ID
        val receitaRef = firestore.collection("receitas").document(receitaId)

        receitaRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val data = documentSnapshot.data

                // Verifique se a receita pertence ao usuário logado
                val receitaUserId = data?.get("userId")?.toString() ?: ""
                if (receitaUserId == userId) {
                    val valorDaReceita = data?.get("valor_da_receita") as? Double ?: 0.0
                    findViewById<EditText>(R.id.valor_da_receita).setText(valorDaReceita.toString())

                    val categoriaTextView = findViewById<TextView>(R.id.categoria_salario)
                    categoriaTextView.text =
                        data?.get("categoria_escolhida_pelo_usuario_receitas")?.toString() ?: ""

                    findViewById<EditText>(R.id.auto_completa_texto_receita).setText(
                        data?.get("nome_da_receita")?.toString() ?: ""
                    )
                    findViewById<EditText>(R.id.Descricao_da_receita).setText(
                        data?.get("descricao_da_receita")?.toString() ?: ""
                    )
                    findViewById<EditText>(R.id.auto_completa_texto_favorecido_receita).setText(
                        data?.get(
                            "nome_favorecido_receita"
                        )?.toString() ?: ""
                    )

                    val iconeCategoria = data?.get("iconeReceita") as? Long
                    DadosTemporariosIntermediarios.iconeCategoria = iconeCategoria?.toInt() ?: 0

                    val corCategoria = data?.get("corCirculoReceita") as? Long
                    DadosTemporariosIntermediarios.corCategoria = corCategoria?.toInt() ?: 0

                    val dataEfetuacaoTimestamp =
                        data?.get("data_efetuacao_receitas") as? com.google.firebase.Timestamp
                    if (dataEfetuacaoTimestamp != null) {
                        val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dataFormatada = formatoData.format(dataEfetuacao)
                        findViewById<TextView>(R.id.data_efetuacao_textview).text = dataFormatada

                        // Armazena a data original recuperada do Firestore
                        selectedDateEfetuacao = Calendar.getInstance()
                        selectedDateEfetuacao?.time = dataEfetuacao
                    }

                    // Corrija os valores do Switch e dos ImageViews
                    toggleSwitch = findViewById(R.id.toggleSwitch)
                    val recebidoOuNao = data?.get("recebido_ou_nao") as? String

                    if (recebidoOuNao == "Recebido") {
                        toggleSwitch.isChecked = true // Define o switch como ativado se for "Pago"
                    } else {
                        toggleSwitch.isChecked = false // Define o switch como desativado caso contrário
                    }


                    val iconeResId = data?.get("iconeReceita") as? Long ?: 0
                    val corCirculo = data?.get("corCirculoReceita") as? Long ?: 0

                    val categoriaImageView = findViewById<ImageView>(R.id.categoria_de_salario)
                    val circuloImageView = findViewById<ImageView>(R.id.Circulo_cor)

                    categoriaImageView.setImageResource(iconeResId.toInt())
                    circuloImageView.setColorFilter(corCirculo.toInt())
                } else {
                    // Lidar com a situação em que a receita não pertence ao usuário logado
                }
            }
        }
        val limparDataButton = findViewById<Button>(R.id.limpar_data_button)
        limparDataButton.visibility = View.VISIBLE

        // Botão "Outros" para abrir o DatePickerDialog
        val abrirCalendarioDataEfetuacaoButton =
            findViewById<Button>(R.id.abrir_calendario_data_efetuacao)
        abrirCalendarioDataEfetuacaoButton.setOnClickListener {
            openDatePickerDialog() // Chama o método para exibir o DatePickerDialog
        }

        // Botão "Limpar Data" para limpar a data selecionada
        limparDataButton.setOnClickListener {
            selectedDateEfetuacao = null

            toggleSwitch.isChecked = false

            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility =
                View.VISIBLE

            // Esconda o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button).visibility = View.GONE

            // Esconda o TextView com a data selecionada
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE
        }

        val irParaCategoriasDasReceitas =
            findViewById<ImageView>(R.id.Ir_para_tela_escolher_categorias_receitas)
        irParaCategoriasDasReceitas.setOnClickListener {
            // Salvar os valores no SharedPreferences
            abrirTelaDasCategoriasDasReceitas(receitaId)
        }
        val editarReceitaButton = findViewById<Button>(R.id.Editar_receita)
        editarReceitaButton.setOnClickListener {
            // Chame o método para atualizar os dados do registro no Firestore
            atualizarRegistroNoFirestore(receitaId)
        }
        // Obtenha a referência para o EditText
        val valorDaReceitaEditText = findViewById<EditText>(R.id.valor_da_receita)
        valorDaReceitaEditText.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        // Aplicando o estilo EditTextHintGreen ao EditText
        valorDaReceitaEditText.setTextAppearance(R.style.HintStyLE2)

        // Defina a cor verde também para o hint do EditText usando um SpannableString
        val hint = "0.00"
        val spannableString = SpannableString(hint)
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.green)),
            0,
            hint.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        valorDaReceitaEditText.hint = spannableString

        val deletarDespesaTextView = findViewById<TextView>(R.id.deletar_receita_textview)
        deletarDespesaTextView.setOnClickListener {
            exibirDialogConfirmacao()
        }
        val voltarTelaPrincipal = findViewById<ImageView>(R.id.Voltar_tela_inicial)
        voltarTelaPrincipal.setOnClickListener {
            abrirTelaPrincipal()
        }
        carregarNomesReceitasDoUsuario()
        carregarNomesFavorecidosReceitasDoUsuario()
    }
    private fun exibirDialogConfirmacao() {
        val receitaId = intent.getStringExtra("receita_id") ?: ""

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmacao_receita, null)

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = alertDialogBuilder.show()

        val btnSim = dialogView.findViewById<Button>(R.id.btn_sim)
        val btnNao = dialogView.findViewById<Button>(R.id.btn_nao)

        btnSim.setOnClickListener {
            // Executar ação de exclusão
            deletarReceita(receitaId)
            alertDialog.dismiss()
        }

        btnNao.setOnClickListener {
            alertDialog.dismiss()
        }
    }
    // Função para deletar a despesa
    private fun deletarReceita(receitaId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Referência ao documento da despesa que será deletada
        val receitaRef = firestore.collection("receitas").document(receitaId)

        receitaRef.delete()
            .addOnSuccessListener {
                // Despesa deletada com sucesso
                Toast.makeText(this, "Receita deletada com sucesso!", Toast.LENGTH_SHORT).show()
                abrirTelaPrincipal()
            }
            .addOnFailureListener { e ->
                // Falha ao deletar a despesa
                Log.e(TAG, "Erro ao deletar despesa: ${e.message}")
                Toast.makeText(this, "Erro ao deletar receita", Toast.LENGTH_SHORT).show()
            }
    }
    private fun abrirTelaDasCategoriasDasReceitas(receitaId: String) {
        val intent = Intent(this, activity_tela_editar_escolher_categorias::class.java)
        intent.putExtra("receita_id", receitaId) // Passe o ID da receita para a próxima tela
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
    class DadosTemporariosIntermediarios {
        companion object {
            var iconeCategoria: Int = 0
            var corCategoria: Int = 0
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

            if (novaCategoria != null && novaCorCirculo != 0) {
                val categoriaImageView = findViewById<ImageView>(R.id.categoria_de_salario)
                categoriaImageView.setImageResource(novoIconeResId)

                val circuloImageView = findViewById<ImageView>(R.id.Circulo_cor)
                circuloImageView.setColorFilter(novaCorCirculo)

                val textViewCategorias = findViewById<TextView>(R.id.categoria_salario)
                textViewCategorias.text = novaCategoria

                DadosTemporariosIntermediarios.iconeCategoria = novoIconeResId
                DadosTemporariosIntermediarios.corCategoria = novaCorCirculo
            }
        }
    }
    private fun abrirTelaPrincipal() {
        val intent = Intent(this, TelaPrincipal::class.java)
        startActivity(intent)
    }
    private fun atualizarRegistroNoFirestore(receitaId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val selectedTimestamp = selectedDateEfetuacao?.let {
            dataEfetuacaoInMillis = it.timeInMillis // Atualize o valor da variável
            Timestamp(it.time)
        }

        val recebido = toggleSwitch.isChecked

        // Certifique-se de que os valores são do tipo certo
        val valorDaReceita = findViewById<EditText>(R.id.valor_da_receita).text.toString()
        val categoriaEscolhida = findViewById<TextView>(R.id.categoria_salario).text.toString()
        val nomeDaReceita = findViewById<EditText>(R.id.auto_completa_texto_receita).text.toString()
        val descricaoDaReceita = findViewById<EditText>(R.id.auto_completa_texto_receita).text.toString()
        val nomeFavorecido = findViewById<EditText>(R.id.auto_completa_texto_favorecido_receita).text.toString()
        val iconeCategoria = DadosTemporariosIntermediarios.iconeCategoria
        val corCategoria = DadosTemporariosIntermediarios.corCategoria

        // Defina o valor "Recebido" ou "Não Recebido" com base no toggleSwitch
        val statusRecebimento = if (recebido) "Recebido" else "Não Recebido"
        val defaultTimestamp = Timestamp(Date(0)) // Substitua a data e hora desejadas

        // Verificar se o valor da receita foi inserido
        if (TextUtils.isEmpty(valorDaReceita)) {
            // Mostrar um Toast para informar ao usuário que ele precisa inserir o valor da receita
            Toast.makeText(this, "Por favor, insira o valor da receita.", Toast.LENGTH_SHORT).show()
            return // Encerra o método para evitar o registro sem o valor da receita
        }

        // Converter o valor da receita de String para Double
        val formattedValue: Double = try {
            valorDaReceita.replace("R", "")
                .replace("$", "")
                .replace(" ", "")
                .replace(",", ".")
                .toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Insira um valor válido para a receita", Toast.LENGTH_SHORT).show()
            return // Encerra o método se o valor não for válido
        }

        // Verificar se o valor é maior que zero
        if (formattedValue <= 0) {
            Toast.makeText(this, "Insira um valor válido para a receita", Toast.LENGTH_SHORT).show()
            return // Encerra o método se o valor não for válido
        }

        // Verificar se o nome da receita foi inserido
        if (TextUtils.isEmpty(nomeDaReceita)) {
            // Mostrar um Toast para informar ao usuário que ele precisa inserir o nome da receita
            Toast.makeText(this, "Por favor, insira o nome da receita.", Toast.LENGTH_SHORT).show()
            return // Encerra o método para evitar o registro sem o nome da receita
        }

        // Verificar se a data de efetuação foi selecionada
        if (selectedDateEfetuacao == null) {
            // Mostrar um Toast para informar ao usuário que ele precisa selecionar a data de efetuação
            Toast.makeText(this, "Por favor, selecione a data de efetuação.", Toast.LENGTH_SHORT).show()
            return // Encerra o método para evitar o registro sem a data de efetuação
        }


        // Crie um mapa com os dados que serão atualizados
        val atualizacaoMap = hashMapOf<String, Any>(
            "valor_da_receita" to formattedValue,
            "categoria_escolhida_pelo_usuario_receitas" to categoriaEscolhida,
            "nome_da_receita" to nomeDaReceita,
            "descricao_da_receita" to descricaoDaReceita,
            "nome_favorecido_receita" to nomeFavorecido,
            "data_efetuacao_receitas" to (selectedTimestamp ?: defaultTimestamp),
            "recebido_ou_nao" to statusRecebimento,
            "iconeReceita" to iconeCategoria, // Adicione esta linha para o ícone
            "corCirculoReceita" to corCategoria // Adicione esta linha para a cor
        )

        // Atualize o documento no Firestore
        firestore.collection("receitas").document(receitaId)
            .update(atualizacaoMap)
            .addOnSuccessListener {
                // Registro da receita concluído com sucesso.
                Toast.makeText(this, "Receita editada com sucesso!", Toast.LENGTH_SHORT).show()
                abrirTelaPrincipal()
            }
            .addOnFailureListener {
                // Falha na atualização
                Log.e(TAG, "Erro ao atualizar registro: $it")
                // Você pode exibir uma mensagem de erro ao usuário
            }
    }
    private fun carregarNomesReceitasDoUsuario() {
        Log.d("DespesasApp", "Iniciando carregarNomesDespesasDoUsuario")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário

        if (userId != null) {
            val despesasRef = FirebaseFirestore.getInstance()
                .collection("receitas")
                .whereEqualTo("userId", userId)

            despesasRef.get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("DespesasApp", "Sucesso ao obter dados do Firestore")

                    val nomesDespesasList = mutableListOf<String>()

                    for (document in querySnapshot) {
                        val nomeDespesa = document.getString("nome_da_receita")
                        nomeDespesa?.let {
                            nomesDespesasList.add(it)
                        }
                    }

                    val textInputLayout: TextInputLayout = findViewById(R.id.Nome_da_receita)
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

                                    val adapter = ArrayAdapter(this@activity_tela_editar_os_registros_receitas, android.R.layout.simple_dropdown_item_1line, filtro)
                                    autoCompleteTextView.setAdapter(adapter)
                                }

                                override fun afterTextChanged(s: Editable?) {}
                            })
                        }
                    } else {
                        Log.e("DespesasApp", "autoCompleteTextView não foi encontrado na layout")
                        // Lide com a situação quando o autoCompleteTextView não for encontrado
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DespesasApp", "Falha ao obter dados do Firestore: ${e.message}")
                    Toast.makeText(this, "Falha ao carregar os nomes das despesas do usuário: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun carregarNomesFavorecidosReceitasDoUsuario() {
        Log.d("DespesasApp", "Iniciando carregarNomesFavorecidosDoUsuario")

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val favorecidosRef = FirebaseFirestore.getInstance()
                .collection("receitas")
                .whereEqualTo("userId", userId)

            favorecidosRef.get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("DespesasApp", "Sucesso ao obter dados do Firestore para favorecidos")

                    val nomesFavorecidosList = mutableListOf<String>()
                    val nomesFavorecidosSet = mutableSetOf<String>() // Usar um conjunto para armazenar nomes únicos

                    for (document in querySnapshot) {
                        val nomeFavorecido = document.getString("nome_favorecido_receita")
                        nomeFavorecido?.let {
                            if (it.isNotBlank()) { // Verifique se o valor não está em branco
                                nomesFavorecidosSet.add(it)
                            }
                        }
                    }
                    nomesFavorecidosList.addAll(nomesFavorecidosSet.toList()) // Converter o conjunto de volta para uma lista

                    val textInputLayout3: TextInputLayout = findViewById(R.id.Nome_do_favorecido_receita)
                    val autoCompleteTextViewFavorecidoReceita = textInputLayout3.editText as? AutoCompleteTextView

                    if (autoCompleteTextViewFavorecidoReceita != null) {
                        autoCompleteTextViewFavorecidoReceita?.let {
                            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nomesFavorecidosList)
                            it.setAdapter(adapter)

                            it.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(d: CharSequence?, start: Int, count: Int, after: Int) {}

                                override fun onTextChanged(d: CharSequence?, start: Int, before: Int, count: Int) {
                                    Log.d("DespesasApp", "Texto alterado: $d")

                                    val filtro = nomesFavorecidosList.filter { it.contains(d.toString(), ignoreCase = true) }

                                    val adapter = ArrayAdapter(this@activity_tela_editar_os_registros_receitas, android.R.layout.simple_dropdown_item_1line, filtro)
                                    autoCompleteTextViewFavorecidoReceita.setAdapter(adapter)
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
