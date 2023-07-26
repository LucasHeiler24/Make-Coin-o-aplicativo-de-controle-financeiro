package br.com.makecoin.view.TelaPrincipalGraficos

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import br.com.makecoin.R
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import br.com.makecoin.view.TelaPrincipalCategorias.TelaPrincipalCategorias
import com.google.android.material.bottomnavigation.BottomNavigationView

class TelaPrincipalGraficos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_principal_graficos)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setSelectedItemId(R.id.navigation_item3)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.navigation_item1 -> {
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
    }
    override fun onBackPressed() {
        // Impede que o usuário volte deslizando para a tela anterior
    }
}
