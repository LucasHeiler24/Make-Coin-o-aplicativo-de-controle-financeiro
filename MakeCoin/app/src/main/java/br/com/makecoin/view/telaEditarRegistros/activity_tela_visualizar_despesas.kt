package br.com.makecoin.view.telaEditarRegistros

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import br.com.makecoin.R
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class activity_tela_visualizar_despesas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_visualizar_despesas)

        // Recuperar os extras
        val nomeReceita = intent.getStringExtra("nome_despesa")
        val idDespesa = intent.getStringExtra("despesa_id")
        Log.d("Id", "Valor: $idDespesa")
        val numeroParcelas = intent.getLongExtra("numero_de_parcelas", 0)
        Log.d("NumeroParcelas", "Valor: $numeroParcelas")
        val valorMensal = intent.getDoubleExtra("valor_das_parcelas_calculado", 0.0)
        val categoriaReceita = intent.getStringExtra("categoria_escolhida_pelo_usuario")
        val descricaoReceita = intent.getStringExtra("descricao")
        val favorecidoReceita = intent.getStringExtra("nome_favorecido")
        val opcaoRecorrente = intent.getStringExtra("opcao_recorrente")
        val valorReceita = intent.getDoubleExtra("valor_despesa", 0.0)
        val dataEfetuacaoReceita = intent.getLongExtra("data_efetuacao", 0)
        val iconeResId = intent.getIntExtra("iconeResId", 0)
        val corImagemRedonda = intent.getIntExtra("corImagemRedonda", 0)
        val corCirculo = intent.getIntExtra("corCirculo", 0)
        val parcelasPagas = intent.getLongExtra("parcela_paga", 0)
        // Recuperar os extras passados da atividade anterior
        val ganhojaRecebido = intent.getStringExtra("valor_ja_pago")

        // Acessar o switch do layout
        val toggleSwitch = findViewById<Switch>(R.id.toggleSwitch)

        // Verificar se é "Recebido" e ajustar o estado do switch
        if (ganhojaRecebido == "Pago") {
            toggleSwitch.isChecked = true
        }
        // Acessar os elementos do layout XML e preencher os campos
        val nomeRegistroTextView = findViewById<TextView>(R.id.Nome_do_registro)
        val categoriaTextView = findViewById<TextView>(R.id.Nome_da_categoria)
        val favorecidoTextView = findViewById<TextView>(R.id.Nome_do_favorecido_registro)
        val valorTextView = findViewById<TextView>(R.id.Valor_do_registro)
        val dataTextView = findViewById<TextView>(R.id.data_do_registro)
        val descricaoTextView = findViewById<TextView>(R.id.Descricao_do_registro)
        val valorMensalTextView = findViewById<TextView>(R.id.Valor_do_registro_mensal)
        val opcaoRecorTextView = findViewById<TextView>(R.id.opcao_recorrente)
        val tempoParcelasTextView = findViewById<TextView>(R.id.tempo_parcelas)

        // Preencher os campos com os valores recuperados
        nomeRegistroTextView.text = nomeReceita
        categoriaTextView.text = categoriaReceita
        favorecidoTextView.text = favorecidoReceita
        if (valorMensal != 0.0) {
            valorMensalTextView.text = formatCurrencyValue(valorMensal)
            valorTextView.visibility = View.GONE // Esconder o TextView do valorReceita
        } else if (valorReceita != 0.0) {
            valorTextView.text = formatCurrencyValue(valorReceita)
            valorMensalTextView.visibility = View.GONE // Esconder o TextView do valorMensal
        } else {

        }
        if (opcaoRecorrente != null && opcaoRecorrente.isNotBlank()) {
            opcaoRecorTextView.text = opcaoRecorrente
            tempoParcelasTextView.visibility = View.GONE
        } else if (numeroParcelas > 0) {
            if(toggleSwitch.isChecked) {
                val textoParcelas = "$parcelasPagas | $numeroParcelas"
                tempoParcelasTextView.text = textoParcelas
                opcaoRecorTextView.visibility = View.GONE
            }else{
                val textoParcelas = "${parcelasPagas - 1} | $numeroParcelas"
                tempoParcelasTextView.text = textoParcelas
                opcaoRecorTextView.visibility = View.GONE
            }
        } else {
            tempoParcelasTextView.visibility = View.GONE
            opcaoRecorTextView.visibility = View.GONE
        }
        // Converta o timestamp de dataEfetuacaoReceita em uma string de data formatada
        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dataFormatada = formatoData.format(Date(dataEfetuacaoReceita))
        dataTextView.text = dataFormatada
        descricaoTextView.text = descricaoReceita

        // Acessar os ImageViews
        val circuloImageView = findViewById<ImageView>(R.id.Circulo_cor)
        val categoriaImageView = findViewById<ImageView>(R.id.categoria_de_salario)

        circuloImageView.setColorFilter(corCirculo) // cor

        categoriaImageView.setImageResource(iconeResId) // ícone

        val voltarParaTelaPrincipal = findViewById<ImageView>(R.id.Ir_para_tela_principal)
        voltarParaTelaPrincipal.setOnClickListener {
            abrirTelaPrincipal()
        }
        val irParaTelaEditarDespesa = findViewById<Button>(R.id.ir_para_editar_despesas)
        irParaTelaEditarDespesa.setOnClickListener {
            abrirTelaParaEditarReceita()
        }
    }
    // Função para abrir a tela de cadastro
    private fun abrirTelaPrincipal() {
        val intent = Intent(this, TelaPrincipal::class.java)
        startActivity(intent)
    }

    private fun formatCurrencyValue(value: Double): String {
        return String.format("R$ %.2f", value)
    }
    override fun onBackPressed() {
        // Impede que o usuário volte deslizando para a tela anterior
    }
    private fun abrirTelaParaEditarReceita() {
        val idDespesa = intent.getStringExtra("despesa_id")
        val intent = Intent(this, activity_tela_editar_as_despesas::class.java)
        intent.putExtra("despesa_id", idDespesa)
        startActivity(intent)
    }
}