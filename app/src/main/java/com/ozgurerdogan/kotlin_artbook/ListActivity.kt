package com.ozgurerdogan.kotlin_artbook

import android.content.Intent
import android.database.sqlite.SQLiteStatement
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.ozgurerdogan.kotlin_artbook.databinding.ActivityListBinding
import java.lang.Exception
import java.util.ArrayList

class ListActivity : AppCompatActivity() {

    private lateinit var binding:ActivityListBinding
    private lateinit var artModel:ArtModel
    private lateinit var artArray:ArrayList<ArtModel>
    lateinit var rcAdapter:RcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artArray=ArrayList<ArtModel>()

        getDatabase()

        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        rcAdapter=RcAdapter(artArray)
        binding.recyclerView.adapter=rcAdapter
    }
    
    fun getDatabase(){

        try {
            var myDatabase=this.openOrCreateDatabase("Artbook", MODE_PRIVATE,null)
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS artbook (id INTEGER PRIMARY KEY, artName VARCHAR, artistName VARCHAR, date VARCHAR,image BLOB)")
            val cursor=myDatabase.rawQuery("SELECT * FROM artbook",null)

            val artnameIx=cursor.getColumnIndex("artName")
            val idIx=cursor.getColumnIndex("id")


            while (cursor.moveToNext()){
                val name:String=cursor.getString(artnameIx)
                val id:Int=cursor.getInt(idIx)
                println("name: $name , id: $id")
                artModel=ArtModel(id,name)
                artArray.add(artModel)

            }
            rcAdapter.notifyDataSetChanged()

        }catch (e: Exception){
            e.printStackTrace()
        }

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //val inflater:MenuInflater=menuInflater
        //inflater.inflate(R.menu.add_art_menu,menu)

        menuInflater.inflate(R.menu.add_art_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.add_art){
            val intent= Intent(applicationContext,AddArtActivity::class.java)
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }
}