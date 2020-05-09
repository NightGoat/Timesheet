package nightgoat.timesheet.presentation.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_note.*
import nightgoat.timesheet.R

class NoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val oldText = intent.getStringExtra("noteText")
        setContentView(R.layout.activity_note)
        note_btn_cancel.setOnClickListener {
            intent.putExtra("noteText", oldText)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        note_btn_save.setOnClickListener {
            val intent = Intent()
            intent.putExtra("noteText",note_edit.text.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        note_text_date.text = intent.getStringExtra("date")
        note_text_dayOfWeek.text = intent.getStringExtra("dayOfWeek")
        note_edit.setText(intent.getStringExtra("noteText"))
        if(note_edit.requestFocus()) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }
}
