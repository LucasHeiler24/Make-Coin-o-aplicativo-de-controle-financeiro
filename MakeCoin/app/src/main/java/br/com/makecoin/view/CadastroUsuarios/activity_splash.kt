package br.com.makecoin.view.CadastroUsuarios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import br.com.makecoin.R

class activity_splash : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 3000 // Tempo em 3 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Aguarda o tempo especificado e redireciona para a próxima tela
        Handler().postDelayed({
            // Redireciona para a próxima tela telaPrincipal
            val intent = Intent(this@activity_splash, CadastroUsuarios::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_DELAY)
    }
}