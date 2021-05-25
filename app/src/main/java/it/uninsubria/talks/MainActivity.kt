package it.uninsubria.talks
import android.content.Context
import android.widget.ListView


import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private var mUserReference: DatabaseReference? = FirebaseDatabase.getInstance().getReference("users")
    private val TAG = "Main Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       // Log.i(TAG, "Passo alla schermata <Login>")
        // startActivity(Intent(this, Login::class.java))
        val listView = findViewById<ListView> (R.id.main_listview)
       // val color = Color.parseColor( "#FF0000")
       // listView.setBackgroundColor(color)
        listView.adapter = MyCustomAdapter(this)

    }
private class MyCustomAdapter(context: Context): BaseAdapter() {
    private val mContext: Context
    private val names = arrayListOf<String>(
        " Luca Centore","Marc Orlando", "johnny","booooh ","ciaoooo", " Luca Centore","Marc Orlando", "johnny","booooh ","ciaoooo"," Luca Centore","Marc Orlando", "johnny","booooh ","ciaoooo", " Luca Centore","Marc Orlando", "johnny","booooh ","ciaoooo"
    )
    init{
        mContext = context
    }
    override fun getCount(): Int {
        return 20
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getItem(position: Int): Any {
        return "test string"
    }
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val layoutInflater = LayoutInflater.from(mContext)
        val rowMain = layoutInflater.inflate(R.layout.row_main, viewGroup,false)
        val nameTextView = rowMain.findViewById<TextView>(R.id.name_textView)
        nameTextView.text= names.get(position)
      val positionTextView = rowMain.findViewById<TextView>(R.id.position_textview)
        positionTextView.text = "row number: $position"
        return rowMain
    //   val textView = TextView(mContext)
       // textView.text="here is my row for my list view"
      //  return  textView
    }



}

}