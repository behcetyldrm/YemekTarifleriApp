package com.behcetemreyildirim.yemektarifleriapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.Navigation
import com.behcetemreyildirim.yemektarifleriapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //menu oluşuturulur

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.yemek_ekle, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //menuden item seçilirse ne yapıulacağı yazılır

        if (item.itemId == R.id.yemek_ekle_item){
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("menudengeldim", 0)
            Navigation.findNavController(this, R.id.fragmentContainerView).navigate(action)
        }

        return super.onOptionsItemSelected(item)
    }
}