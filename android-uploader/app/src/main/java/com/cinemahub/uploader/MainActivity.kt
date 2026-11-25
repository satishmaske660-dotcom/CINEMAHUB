package com.cinemahub.uploader
import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity: ComponentActivity() {
 private val prefs by lazy { getSharedPreferences("cinemahub",MODE_PRIVATE) }
 private lateinit var root: LinearLayout
 private lateinit var status: TextView
 private var picked: Uri? = null
 private var pickedLabel: TextView? = null
 private val gold=0xffe8bd72.toInt()
 private val picker=registerForActivityResult(ActivityResultContracts.OpenDocument()){u->if(u!=null){try{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){};picked=u;pickedLabel?.text="✓ Video selected";status.text="Video selected. Ready to upload."}}
 override fun onCreate(b:Bundle?){super.onCreate(b);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),7);if(prefs.getString("token",null).isNullOrBlank())login()else uploader()}
 private fun page(title:String,sub:String){val sc=ScrollView(this).apply{setBackgroundColor(0xff09090d.toInt());isFillViewport=true};root=LinearLayout(this).apply{orientation=1;setPadding(20,24,20,30);setBackgroundColor(0xff09090d.toInt())};sc.addView(root);setContentView(sc);root.addView(TextView(this).apply{text="CINEMA HUB";textSize=18f;letterSpacing=.2f;setTextColor(gold)});root.addView(TextView(this).apply{text=title;textSize=29f;setTextColor(-1);setPadding(0,22,0,2)});root.addView(TextView(this).apply{text=sub;textSize=14f;setTextColor(0xffaaa8b4.toInt());setPadding(0,0,0,18)});status=TextView(this).apply{setTextColor(gold);textSize=13f}}
 private fun card()=LinearLayout(this).apply{orientation=1;setPadding(16,16,16,16);setBackgroundColor(0xff17171f.toInt());layoutParams=LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=14}}
 private fun field(h:String,v:String="")=EditText(this).apply{hint=h;setText(v);setTextColor(-1);setHintTextColor(0xffaaa8b4.toInt());setPadding(14,12,14,12);setBackgroundColor(0xff292933.toInt());layoutParams=LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=9}}
 private fun btn(t:String,f:()->Unit)=Button(this).apply{text=t;isAllCaps=false;setTextColor(0xff17120b.toInt());backgroundTintList=android.content.res.ColorStateList.valueOf(gold);setOnClickListener{f()};layoutParams=LinearLayout.LayoutParams(-1,-2).apply{topMargin=7}}
 private fun login(){
  page("Creator sign in","Secure owner access to upload and manage your videos.")
  val c=card();c.addView(TextView(this).apply{text="OWNER ACCOUNT";setTextColor(gold);textSize=11f})
  val email=field("Email address","satishmaske660@gmail.com");c.addView(email)
  val send=btn("Email me a sign-in code"){};c.addView(send)
  val otp=field("Enter 6-digit code");otp.inputType=android.text.InputType.TYPE_CLASS_NUMBER;otp.filters=arrayOf(android.text.InputFilter.LengthFilter(6));otp.visibility=View.GONE;c.addView(otp)
  val verify=btn("Verify code and sign in"){};verify.visibility=View.GONE;c.addView(verify);root.addView(c);root.addView(status)
  val handler=android.os.Handler(mainLooper)
  fun cooldown(sec:Int){val until=System.currentTimeMillis()+sec*1000L;prefs.edit().putLong("otp_cooldown_until",until).apply();send.isEnabled=false;fun tick(){val n=((until-System.currentTimeMillis()+999)/1000).toInt();if(n<=0){send.isEnabled=true;send.text="Email me a sign-in code";status.text="You can request another code now."}else{send.text="Try again in ${n}s";handler.postDelayed({tick()},1000)}};tick()}
  val old=prefs.getLong("otp_cooldown_until",0L);if(old>System.currentTimeMillis())cooldown(((old-System.currentTimeMillis()+999)/1000).toInt())
  send.setOnClickListener{val e=email.text.toString().trim().lowercase();if(e!="satishmaske660@gmail.com"){status.text="This email is not authorised.";return@setOnClickListener};send.isEnabled=false;send.text="Sending…";Thread{try{Api.call("POST","/auth/v1/otp","""{"email":"$e","create_user":false}""");runOnUiThread{otp.visibility=View.VISIBLE;verify.visibility=View.VISIBLE;status.text="Code sent to $e. Enter it above; check Spam/Junk too.";send.isEnabled=true;send.text="Send another code"}}catch(x:Exception){runOnUiThread{status.text="Sign-in error: "+x.message;if((x.message?:"").contains("429")||(x.message?:"").contains("rate limit",true))cooldown(60)else{send.isEnabled=true;send.text="Try again"}}}}.start()}
  verify.setOnClickListener{val e=email.text.toString().trim().lowercase();val code=otp.text.toString().trim();if(code.length!=6){status.text="Enter all 6 digits.";return@setOnClickListener};verify.isEnabled=false;verify.text="Verifying…";Thread{try{val r=org.json.JSONObject(Api.call("POST","/auth/v1/verify","""{"email":"$e","token":"$code","type":"email"}"""));prefs.edit().putString("token",r.getString("access_token")).putString("refresh_token",r.optString("refresh_token")).putLong("otp_cooldown_until",0).apply();runOnUiThread{uploader()}}catch(x:Exception){runOnUiThread{verify.isEnabled=true;verify.text="Verify code and sign in";status.text="Verification failed: "+x.message}}}.start()}
 }
 private fun uploader(){
  page("Creator Studio","Upload original movies, series episodes and bonus content.")
  val a=card();a.addView(TextView(this).apply{text="SIGNED IN · satishmaske660@gmail.com";setTextColor(gold)});root.addView(a)
  val c=card();c.addView(TextView(this).apply{text="ADD NEW VIDEO";setTextColor(gold);textSize=11f})
  val cat=Spinner(this);cat.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("Movies","Series","Content"));c.addView(cat)
  val title=field("Movie, series or video title");val desc=field("Description (optional)");c.addView(title);c.addView(desc)
  val st=Spinner(this);st.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("Short Film","Behind the Scenes","Trailer","Music Video","Bonus Video","Other"));c.addView(st)
  val season=field("Season number","1");val ep=field("Episode number","1");val epTitle=field("Episode name");c.addView(season);c.addView(ep);c.addView(epTitle)
  val choose=btn("Choose video from phone"){picker.launch(arrayOf("video/*"))};c.addView(choose)
  pickedLabel=TextView(this).apply{text="No video selected";setTextColor(0xffaaa8b4.toInt());setPadding(0,10,0,10)};c.addView(pickedLabel)
  val up=btn("Start background upload") {val u=picked;if(u==null){status.text="Choose a video first.";return@btn};if(title.text.isBlank()){status.text="Enter a title.";return@btn};val k=listOf("movie","series","content")[cat.selectedItemPosition];val i=Intent(this,UploadService::class.java).apply{action=UploadService.ACTION;putExtra("uri",u.toString());putExtra("title",title.text.toString());putExtra("description",desc.text.toString());putExtra("kind",k);putExtra("subtype",st.selectedItem.toString());putExtra("season",season.text.toString().toIntOrNull()?:1);putExtra("episode",ep.text.toString().toIntOrNull()?:1);putExtra("episodeTitle",epTitle.text.toString())};if(Build.VERSION.SDK_INT>=26)startForegroundService(i)else startService(i);status.text="Upload started. Track progress in Android notifications."};c.addView(up);root.addView(c)
  fun fields(){val s=cat.selectedItemPosition==1;val content=cat.selectedItemPosition==2;st.visibility=if(content)View.VISIBLE else View.GONE;season.visibility=if(s)View.VISIBLE else View.GONE;ep.visibility=if(s)View.VISIBLE else View.GONE;epTitle.visibility=if(s)View.VISIBLE else View.GONE}
  cat.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(p:AdapterView<*>?){};override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long)=fields()};fields()
  root.addView(status);root.addView(btn("Sign out"){prefs.edit().remove("token").remove("refresh_token").apply();login()})
 }
}