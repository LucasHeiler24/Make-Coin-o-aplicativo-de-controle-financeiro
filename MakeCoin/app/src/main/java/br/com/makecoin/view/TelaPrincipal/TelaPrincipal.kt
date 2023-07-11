package br.com.makecoin.view.TelaPrincipal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.com.makecoin.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class TelaPrincipal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_principal)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Defina os ícones para os itens do menu programaticamente
        bottomNavigationView.menu.findItem(R.id.navigation_item1).setIcon(R.drawable.navegarcasa)
        bottomNavigationView.menu.findItem(R.id.navigation_item2).setIcon(R.drawable.mais)
        bottomNavigationView.menu.findItem(R.id.navigation_item3).setIcon(R.drawable.estatistico)
    }
}