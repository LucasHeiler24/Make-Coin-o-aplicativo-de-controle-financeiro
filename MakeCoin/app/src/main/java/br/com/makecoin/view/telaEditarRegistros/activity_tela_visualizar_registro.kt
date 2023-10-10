package br.com.makecoin.view.telaEditarRegistros

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import br.com.makecoin.R
import br.com.makecoin.view.LoginUsuarios.CadastroDosUsuarios
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class activity_tela_visualizar_registro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_visualizar_registro)

        // Recuperar os extras
        val nomeReceita = intent.getStringExtra("nome_da_receita")
        val idReceita = intent.getStringExtra("receita_id")
        val categoriaReceita = intent.getStringExtra("categoria_da_receita")
        val descricaoReceita = intent.getStringExtra("descricao_da_receita")
        val favorecidoReceita = intent.getStringExtra("nome_favorecido_receita")
        val valorReceita = intent.getDoubleExtra("valor_da_receita", 0.0)
        val dataEfetuacaoReceita = intent.getLongExtra("data_efetuacao_receita", 0)
        val iconeResId = intent.getIntExtra("iconeReceita", 0)
        val corImagemRedonda = intent.getIntExtra("corImagemReceita", 0)
        val corCirculo = intent.getIntExtra("corCirculoReceita", 0)

        // Recuperar os extras passados da atividade anterior
        val ganhojaRecebido = intent.getStringExtra("recebido_ou_nao")

        // Acessar o switch do layout
        val toggleSwitch = findViewById<Switch>(R.id.toggleSwitch)

        // Verificar se é "Recebido" e ajustar o estado do switch
        if (ganhojaRecebido == "Recebido") {
            toggleSwitch.isChecked = true
        }
        // Acessar os elementos do layout XML e preencher os campos
        val nomeRegistroTextView = findViewById<TextView>(R.id.Nome_do_registro)
        val categoriaTextView = findViewById<TextView>(R.id.Nome_da_categoria)
        val favorecidoTextView = findViewById<TextView>(R.id.Nome_do_favorecido_registro)
        val valorTextView = findViewById<TextView>(R.id.Valor_do_registro)
        val dataTextView = findViewById<TextView>(R.id.data_do_registro)
        val descricaoTextView = findViewById<TextView>(R.id.Descricao_do_registro)

        // Preencher os campos com os valores recuperados
        nomeRegistroTextView.text = nomeReceita
        categoriaTextView.text = categoriaReceita
        favorecidoTextView.text = favorecidoReceita
        valorTextView.text = formatCurrencyValue(valorReceita)// Converte o valor em String
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
        val irParaTelaEditarReceita = findViewById<Button>(R.id.Ir_para_editar_receita)
        irParaTelaEditarReceita.setOnClickListener {
            abrirTelaParaEditarReceita()
        }
    }
    // Função para abrir a tela de cadastro
    private fun abrirTelaPrincipal() {
        val intent = Intent(this, TelaPrincipal::class.java)
        startActivity(intent)
    }
    private fun abrirTelaParaEditarReceita() {
        val idReceita = intent.getStringExtra("receita_id")
        val intent = Intent(this, activity_tela_editar_os_registros_receitas::class.java)
        intent.putExtra("receita_id", idReceita)
        startActivity(intent)
    }
    private fun formatCurrencyValue(value: Double): String {
        return String.format("R$ %.2f", value)
    }
    override fun onBackPressed() {
        // Impede que o usuário volte deslizando para a tela anterior
    }
}