package com.fbiego.dt78

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.fbiego.dt78.data.*
import kotlinx.android.synthetic.main.activity_contacts.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import com.fbiego.dt78.app.SettingsActivity as ST
import com.fbiego.dt78.app.ForegroundService as FG


class ContactsActivity : AppCompatActivity() {

    private val REQUEST_PICK_CONTACT = 1432

    companion object{
        var contacts = ArrayList<ContactData>()
        var sos = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(ST.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)


        val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
        sos = setPref.getInt(ST.PREF_SOS, 0)






    }

    override fun onResume() {
        super.onResume()

        val dbHandler = MyDBHandler(this, null, null, 1)
        contacts = dbHandler.getContacts()
        if (sos >= contacts.size){
            sos = 0
        }
        val myCustomList = ContactAdapter(this, contacts, sos)

        customListView.adapter = myCustomList
        customListView.setOnItemClickListener { _, _, i, _ ->
            sos = i
            val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
            val editor = setPref.edit()
            editor.putInt(ST.PREF_SOS, sos)
            editor.apply()
            customListView.adapter = ContactAdapter(this, contacts, sos)
        }
        customListView.setOnItemLongClickListener { _, _, i, _ ->
            contacts.removeAt(i)

            dbHandler.insertContact(contacts)
            if (sos >= contacts.size){
               sos = 0
            }
            customListView.adapter = ContactAdapter(this, contacts, sos)
            addContact.visibility = View.VISIBLE
            true
        }

        if (contacts.size >= 8){
            addContact.visibility = View.GONE
        } else {
            addContact.visibility = View.VISIBLE
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sync_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sync -> {

                if (!FG().sendData(byteArrayOf(0x00))){
                    Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
                }

                for (c in 0 until contacts.size){
                    if (sos == c){
                        val ss = byteArrayOfInts(0xAB, 0x00, 0x05, 0xFF, 0xA5, 0x80, sos, contacts.size )
                        FG().sendData(ss)
                    }
                    val nameBytes = nameConvert(contacts[c].name, c)
                    val numberBytes = numberConvert(contacts[c].number, c)
                    if (nameBytes.size > 20){
                        FG().sendData(nameBytes.sliceArray(0 until 20))
                        FG().sendData(nameBytes.sliceArray(20 until nameBytes.size))
                    } else {
                        FG().sendData(nameBytes)
                    }
                    FG().sendData(numberBytes)

                }

                return true
            }
            else  -> super.onOptionsItemSelected(item)
        }
    }

    fun choose(view: View){
        Timber.w("View ${view.id}")
        if (checkContactPermission()) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                startActivityForResult(intent, REQUEST_PICK_CONTACT)
        } else {
            requestContactPermission()
        }
    }

    private fun checkContactPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(
                this@ContactsActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun requestContactPermission(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_CONTACTS), MainActivity.PERMISSIONS_CONTACTS
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PICK_CONTACT) {
            var phoneNo: String?
            val name: String?
            val uri: Uri = data?.data!!
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor!!.moveToFirst()) {
                val phoneIndex: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameIndex: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                phoneNo = cursor.getString(phoneIndex)
                name = cursor.getString(nameIndex)
                Timber.d("$phoneIndex $phoneNo $nameIndex $name")

                phoneNo =
                    phoneNo.replace(("[^0-9\\+]").toRegex(), "")
                        .replace(("(.)(\\++)(.)").toRegex(), "$1$3")
                val new = ContactData(contacts.size+1, name, phoneNo)
                contacts.add(new)
                val dbHandler = MyDBHandler(this, null, null, 1)
                dbHandler.insertContact(contacts)

            }
            cursor.close()
        }
    }

    private fun nameConvert(name: String, pos: Int): ByteArray{
        val start = byteArrayOfInts(0xAB, 0x00)
        val type = byteArrayOfInts(0xFF, 0xA2)
        val msg = name.toByteArray()
        val msgByte = if (msg.size > 33){
            msg.slice(0 until 33)
        } else {
            msg.slice(msg.indices)
        }
        val lenD = (msgByte.size + 3).toByte()
        //val lenM = (msgByte.size).toByte()

        return if (msgByte.size <= 14){
            start + lenD + type + pos.toByte() +  msgByte
        } else {
            val msg0 = msgByte.slice(0 until 14)
            val msg1 = msgByte.slice(14 until msgByte.size)
            start + lenD + type + pos.toByte() + msg0 + 0x00 + msg1
        }

    }

    private fun numberConvert(number: String, pos: Int): ByteArray{
        val start = byteArrayOfInts(0xAB, 0x00)
        val type = byteArrayOfInts(0xFF, 0xA3)

        val lenN = (number.length).toByte()
        var no = if (number.length % 2 != 0 ){
            number + "0"
        } else {
            number
        }
        no = no.replace("+", "A")
        var phone : ByteArray = byteArrayOf()
        for (x in 0 until no.length/2) {
            val digit = no.substring(x * 2, (x + 1) * 2)
            phone += (digit.reversed().toInt(16)).toByte()

        }
        val lenD = (4 + phone.size).toByte()
        return start + lenD + type + pos.toByte() + lenN + phone

    }
}