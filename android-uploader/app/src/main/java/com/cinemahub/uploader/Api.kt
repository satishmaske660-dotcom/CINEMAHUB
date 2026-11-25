package com.cinemahub.uploader
import java.net.HttpURLConnection
import java.net.URL
object Api {
 const val ID="quotsxqjjupshauthrtf"
 const val KEY="sb_publishable_J2Jz4NLPOan97alVyQq1Gg_QlEsAXjg"
 const val BASE="https://"+ID+".supabase.co"
 fun call(method:String,path:String,body:String?=null,token:String?=null,extra:Map<String,String> = emptyMap()):String {
  val c=URL(BASE+path).openConnection() as HttpURLConnection
  c.requestMethod=method;c.connectTimeout=30000;c.readTimeout=60000
  c.setRequestProperty("apikey",KEY);c.setRequestProperty("Authorization","Bearer "+(token?:KEY));c.setRequestProperty("Content-Type","application/json")
  extra.forEach{(k,v)->c.setRequestProperty(k,v)}
  if(body!=null){c.doOutput=true;c.outputStream.use{it.write(body.toByteArray())}}
  val code=c.responseCode;val stream=if(code in 200..299)c.inputStream else c.errorStream
  val result=stream?.bufferedReader()?.use{it.readText()}?:"";c.disconnect()
  if(code !in 200..299)throw IllegalStateException("HTTP "+code+": "+result)
  return result
 }
}
