const URL='https://quotsxqjjupshauthrtf.supabase.co',KEY='sb_publishable_J2Jz4NLPOan97alVyQq1Gg_QlEsAXjg';
const $=id=>document.getElementById(id);
const say=(id,s)=>{const el=$(id);if(el)el.textContent=s;};
const show=(id,v)=>{const el=$(id);if(el)el.classList.toggle('hidden',!v);};
let db=null,user=null;
const esc=s=>String(s||'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
function boot(){
 const button=$('sendLink');
 if(!window.supabase?.createClient){say('loginStatus','Login service did not load. Check your internet connection, refresh this page, and try again.');if(button)button.disabled=true;return;}
 db=window.supabase.createClient(URL,KEY);
 async function refresh(){
  const {data:t,error}=await db.from('titles').select('*').order('created_at',{ascending:false});if(error)throw error;
  const series=(t||[]).filter(x=>x.kind==='series');
  $('seriesSelect').innerHTML='<option value="">Select a series</option>'+series.map(x=>'<option value="'+x.id+'">'+esc(x.title)+'</option>').join('');
  const {data:s,error:se}=await db.from('seasons').select('id,title_id,season_number,name').order('season_number');if(se)throw se;
  $('seasonSelect').innerHTML='<option value="">Select a season</option>'+(s||[]).map(x=>'<option value="'+x.id+'">'+esc((series.find(y=>y.id===x.title_id)||{}).title||'Series')+' — '+esc(x.name||('Season '+x.season_number))+'</option>').join('');
  $('library').innerHTML=(t||[]).length?t.map(x=>'<div class="item"><div><b>'+esc(x.title)+'</b><div class="tag">'+x.kind.toUpperCase()+' · '+(x.is_published?'PUBLISHED':'DRAFT')+'</div></div><div><button class="btn secondary" data-toggle="'+x.id+'" data-state="'+x.is_published+'">'+(x.is_published?'Unpublish':'Publish')+'</button> <button class="btn danger" data-delete="'+x.id+'">Delete</button></div></div>').join(''):'No titles yet. Add your first original above.';
 }
 async function session(s){
  user=s?.user||null;show('dashboard',!!user);show('loginCard',!user);
  if(user){$('who').textContent=user.email||'Creator';try{await refresh();say('mainStatus','Connected to your private CinemaHub library.');}catch(e){say('mainStatus','Library error: '+e.message);}}
 }
 button.onclick=async()=>{
  const email=$('email').value.trim().toLowerCase();
  if(email!=='deltasatish9@gmail.com'){say('loginStatus','This email is not authorised.');return;}
  button.disabled=true;say('loginStatus','Sending secure sign-in link…');
  try{
   const redirect=location.origin+location.pathname;
   const {error}=await db.auth.signInWithOtp({email,options:{emailRedirectTo:redirect}});
   if(error)throw error;
   say('loginStatus','Sign-in link sent. Open the email from Supabase and tap the link. Check Spam too.');
  }catch(e){say('loginStatus','Sign-in failed: '+(e.message||String(e)));}
  finally{button.disabled=false;}
 };
 $('signOut').onclick=()=>db.auth.signOut();
 async function upload(file){if(!file)throw Error('Choose a video file.');const name=file.name.normalize('NFKD').replace(/[^a-zA-Z0-9._-]+/g,'-').slice(-90);const path=user.id+'/'+Date.now()+'-'+name;const {error}=await db.storage.from('cinemahub-videos').upload(path,file,{contentType:file.type||'video/mp4'});if(error)throw error;return path;}
 $('addTitle').onclick=async()=>{const title=$('title').value.trim(),kind=$('kind').value;if(!title)return say('titleStatus','Enter a title.');const b=$('addTitle');b.disabled=true;try{let movie_video_path=null;if(kind==='movie'&&$('movieFile').files[0]){say('titleStatus','Uploading video… keep this page open.');movie_video_path=await upload($('movieFile').files[0]);}const {error}=await db.from('titles').insert({kind,title,synopsis:$('synopsis').value.trim(),poster_url:$('poster').value.trim()||null,movie_video_path,is_published:$('publishTitle').checked,is_premium:true});if(error)throw error;say('titleStatus','Saved successfully.');$('title').value='';$('synopsis').value='';$('poster').value='';$('movieFile').value='';await refresh();}catch(e){say('titleStatus','Error: '+e.message);}finally{b.disabled=false;}};
 $('addSeason').onclick=async()=>{const b=$('addSeason');b.disabled=true;try{const {error}=await db.from('seasons').insert({title_id:$('seriesSelect').value,season_number:+$('seasonNo').value,name:$('seasonName').value.trim(),is_published:$('publishSeason').checked});if(error)throw error;say('seasonStatus','Season saved.');await refresh();}catch(e){say('seasonStatus','Error: '+e.message);}finally{b.disabled=false;}};
 $('addEpisode').onclick=async()=>{const b=$('addEpisode');b.disabled=true;try{const f=$('episodeFile').files[0],title=$('episodeTitle').value.trim();if(!$('seasonSelect').value||!f||!title)throw Error('Choose a season, episode title and video.');say('episodeStatus','Uploading… keep this page open.');const video_path=await upload(f);const {error}=await db.from('episodes').insert({season_id:$('seasonSelect').value,episode_number:+$('episodeNo').value,title,synopsis:$('episodeSynopsis').value.trim(),video_path,is_published:$('publishEpisode').checked});if(error)throw error;say('episodeStatus','Episode uploaded and saved.');$('episodeFile').value='';$('episodeTitle').value='';await refresh();}catch(e){say('episodeStatus','Error: '+e.message);}finally{b.disabled=false;}};
 $('library').onclick=async e=>{const t=e.target.closest('[data-toggle]'),d=e.target.closest('[data-delete]');if(t){const {error}=await db.from('titles').update({is_published:t.dataset.state!=='true'}).eq('id',t.dataset.toggle);if(error)alert(error.message);else await refresh();}if(d&&confirm('Delete this title and its seasons and episodes? Stored video files may need separate cleanup.')){const {error}=await db.from('titles').delete().eq('id',d.dataset.delete);if(error)alert(error.message);else await refresh();}};
 $('kind').onchange=()=>{$('movieFile').disabled=$('kind').value!=='movie';};
 db.auth.onAuthStateChange((_e,s)=>{session(s).catch(e=>say('mainStatus',e.message));});
 db.auth.getSession().then(({data,error})=>{if(error)throw error;return session(data.session);}).catch(e=>say('loginStatus','Could not check sign-in session: '+e.message));
}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',boot);else boot();
