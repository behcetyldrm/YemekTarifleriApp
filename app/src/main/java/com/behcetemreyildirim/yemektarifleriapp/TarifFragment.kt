package com.behcetemreyildirim.yemektarifleriapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.behcetemreyildirim.yemektarifleriapp.databinding.FragmentTarifBinding
import java.io.ByteArrayOutputStream

class TarifFragment : Fragment() {
    private lateinit var binding: FragmentTarifBinding

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTarifBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {  //onClick kullanmak için id den setOnclicklistener çağırmamız gerekir
            kaydet(it)
        }
        binding.imageView.setOnClickListener{
            gorselSec(it)
        }

        arguments?.let {

            var gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi

            if (gelenBilgi.equals("menudengeldim")){
                //yeni yemek ekleme

                binding.yemekIsmiText.setText("")
                binding.yemekMalzemeText.setText("")
                binding.button.visibility = View.VISIBLE

                val gorselArkaPlan = BitmapFactory.decodeResource(context?.resources, R.drawable.gorselsec)
                binding.imageView.setImageBitmap(gorselArkaPlan)
            }
            else{
                //daha önce oluşturulan yemeği göster
                binding.button.visibility = View.INVISIBLE //buton görnümez olur

                val secilenId = TarifFragmentArgs.fromBundle(it).id

                context?.let {

                    try {

                        val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE, null)
                        val cursor = database.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secilenId.toString()))

                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex = cursor.getColumnIndex("yemekmalzeme")
                        val yemekGorselIndex = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()){

                            binding.yemekIsmiText.setText(cursor.getString(yemekIsmiIndex))
                            binding.yemekMalzemeText.setText(cursor.getString(yemekMalzemeIndex))

                            val byteDizisi = cursor.getBlob(yemekGorselIndex)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                            binding.imageView.setImageBitmap(bitmap)
                        }
                        cursor.close()

                    }catch (e : Exception){
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    fun kaydet(view: View){
        //SQLite'a kaydetme
        val yemekIsmi = binding.yemekIsmiText.text.toString()
        val yemekMalzeme = binding.yemekMalzemeText.text.toString()

        if (secilenBitmap != null){

            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)

            val outputStream = ByteArrayOutputStream() //görseli byte dizisine çevirmede kullanılır
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {

                context?.let {
                    var database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE, null)

                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemekmalzeme VARCHAR, gorsel BLOB)")

                    val sqlString = "INSERT INTO yemekler (yemekismi, yemekmalzeme, gorsel) VALUES (?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1, yemekIsmi)
                    statement.bindString(2,yemekMalzeme)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()
                }

            }catch (e: Exception){
                e.printStackTrace()
            }

            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    fun gorselSec(view: View){

        activity?.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //contextCompat -> android sürümleri arasındaki uyumsuzluğu giderir. kontrol edilecek izini seçtik. izin verildiye eşit değilse çalışır

                //izin verilmedi, izin istenecek
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
                //izin isteme yapısı. istenecek izni belirttik.

            }
            else{
                //izin verilmiş, direkt galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //intent ile seçim yapılacağını belirttik. seçim yolu ise galerinin yolu

                startActivityForResult(galeriIntent,2) //görsel sonucu döndüreceği için forResult seçtik
            }
        }

    }

    override fun onRequestPermissionsResult( //İzin alma işleminin sonuçları yazılır.
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode == 1){

            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //grantResults -> izin kodlarını tutar. size > 0 kontrolu ile izin verilirse en az 1 elemanı olacağı için kotrol yapılır
                // 0. eleman galeriye git izni olduğu için onun izin verildiye eşit olduğu kontrol edilir ve doğruysa galeriye gidilir

                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            secilenGorsel = data.data

            try {

                context?.let {
                    if (secilenGorsel != null){
                        if (Build.VERSION.SDK_INT >= 28){ //createSource api 28'in altında çalışmaz
                            val source = ImageDecoder.createSource(it.contentResolver, secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                        else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    }

                }

            }catch (e : Exception){
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmapOlustur(secilenBitmap : Bitmap, maxBoyut : Int) : Bitmap {

        var width = secilenBitmap.width //seçilen resmin boyutlarını alır
        var height = secilenBitmap.height

        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if (bitmapOrani > 1){
            //görsel yatay

            width = maxBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        }
        else {
            //gorsel dikey

            height = maxBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }

        return Bitmap.createScaledBitmap(secilenBitmap,width,height,true)
    }

}