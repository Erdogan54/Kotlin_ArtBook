package com.ozgurerdogan.kotlin_artbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.snackbar.Snackbar
import com.ozgurerdogan.kotlin_artbook.databinding.ActivityAddArtBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception


class AddArtActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAddArtBinding

    lateinit var artName:String
    lateinit var artistName:String
    lateinit var date: String
    lateinit var image:ByteArray
    var selectedImage:Bitmap?=null

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    lateinit var myDatabase:SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddArtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myDatabase=this.openOrCreateDatabase("Artbook", MODE_PRIVATE,null)
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS artbook (id INTEGER PRIMARY KEY, artName VARCHAR, artistName VARCHAR, date VARCHAR,image BLOB)")

        registerLauncher()

        val intent=intent
        val info=intent.getStringExtra("info")

        if (info.equals("old")){
            binding.saveBtn.visibility=View.INVISIBLE
            binding.imageView.isClickable=false
            binding.artNameTxt.isFocusable=false
            binding.artistNameTxt.isFocusable=false
            binding.dateTxt.isFocusable=false

            val model:ArtModel=intent.getSerializableExtra("model") as ArtModel
            val id=model.id

            try {

                val cursor=myDatabase.rawQuery("SELECT * FROM artbook WHERE id=?",arrayOf(id.toString()))

                val artnameIx=cursor.getColumnIndex("artName")
                val artistIx=cursor.getColumnIndex("artistName")
                val dateIx=cursor.getColumnIndex("date")
                val imageIx=cursor.getColumnIndex("image")
                //val idIx=cursor.getColumnIndex("id")

                while (cursor.moveToNext()){
                    val name=cursor.getString(artnameIx)
                    val artist=cursor.getString(artistIx)
                    val date=cursor.getString(dateIx)
                    binding.artNameTxt.setText(name)
                    binding.artistNameTxt.setText(artist)
                    binding.dateTxt.setText(date)


                    val imageByteArray=cursor.getBlob(imageIx)
                    val bitmap=BitmapFactory.decodeByteArray(imageByteArray,0,imageByteArray.size)
                    binding.imageView.setImageBitmap(bitmap)

                }


            }catch (e: Exception){
                e.printStackTrace()
            }

        }else{
            binding.artNameTxt.setText("")
            binding.artistNameTxt.setText("")
            binding.dateTxt.setText("")
            binding.imageView.setImageResource(R.drawable.select_image)
            binding.saveBtn.visibility=View.VISIBLE

            binding.imageView.isClickable=true
            binding.artNameTxt.isFocusable=true
            binding.artistNameTxt.isFocusable=true
            binding.dateTxt.isFocusable=true


        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun imageClick(view:View){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                    View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }


    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(this.contentResolver, imageData!!)
                            selectedImage = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedImage)
                        } else {
                            selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, imageData)
                            binding.imageView.setImageBitmap(selectedImage)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(this, "Permisson needed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun makeSmallerBitmap(image:Bitmap,maximumSize:Int):Bitmap{

        var width=image.width.toDouble()
        var height=image.height.toDouble()

        val bitmapRatio:Double=width/height

        if(bitmapRatio>1){
            //yatay resim

            height=height*(maximumSize.toDouble()/width)
            width=maximumSize.toDouble()

        }else{
            //dikey resim

            width=width*(maximumSize.toDouble()/height)
            height=maximumSize.toDouble()
        }

        return Bitmap.createScaledBitmap(image,width.toInt(),height.toInt(),true)
    }


    fun saveClick(view:View){

        artName=binding.artNameTxt.text.toString()
        artistName=binding.artistNameTxt.text.toString()
        date=binding.dateTxt.text.toString()

        if (selectedImage!=null && !artName.equals("")){

            val smallerBitmapImage=makeSmallerBitmap(selectedImage!!,421)
            val stream = ByteArrayOutputStream()
            smallerBitmapImage.compress(Bitmap.CompressFormat.PNG, 50, stream)
            image=stream.toByteArray()

            try {

                val insert="INSERT INTO artbook (artName,artistName,date,image) VALUES(?,?,?,?)"
                val sqliteStatement:SQLiteStatement=myDatabase.compileStatement(insert)

                sqliteStatement.bindString(1,artName)
                sqliteStatement.bindString(2,artistName)
                sqliteStatement.bindString(3,date)
                sqliteStatement.bindBlob(4,image)
                sqliteStatement.execute()

                val cursor=myDatabase.rawQuery("SELECT * FROM artbook",null)

                val artnameIx=cursor.getColumnIndex("artName")
                val artistnameIx=cursor.getColumnIndex("artistName")
                val dateIx=cursor.getColumnIndex("date")
                val imageIx=cursor.getColumnIndex("image")

                while (cursor.moveToNext()){
                    println(cursor.getString(artnameIx))
                    println(cursor.getString(artistnameIx))
                    println(cursor.getString(dateIx))
                    println(cursor.getBlob(imageIx))
                }

                val intent=Intent(this,ListActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                //finish()

            }catch (e:Exception){
                e.printStackTrace()
            }

        }else{
            Toast.makeText(this,"Please select image and enter art name",Toast.LENGTH_LONG).show()
        }

    }
}