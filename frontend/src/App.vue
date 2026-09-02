<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { api, getToken, setToken } from './api'

const user=ref(null), route=ref('dashboard'), loading=ref(false), error=ref(''), toast=ref('')
const stats=reactive({cats:0,activeRescues:0,volunteerTasks:0})
const cats=ref([]),rescues=ref([]),volunteers=ref([]),adminMetrics=ref(null)
const authMode=ref('login'), loginForm=reactive({username:'',password:''}), registerForm=reactive({username:'',displayName:'',password:'',confirmPassword:''}), modal=ref(null), form=reactive({})
const catQuery=ref(''),catScope=ref('all')
const detailCat=ref(null),detailImage=ref('')
const selectedMapLocation=ref('')

const nav=[['dashboard','⌂','首页'],['map','⌖','猫咪地图'],['cats','◉','健康档案'],['rescue','✚','救助协同'],['volunteer','♡','志愿服务'],['admin','▦','后台管理']]
const titles={dashboard:['总览','今天也一起守护校园里的小生命'],map:['校园猫地图','查询活动区域、发现记录与身份档案'],cats:['猫咪档案','一猫一档，持续记录健康与成长'],rescue:['救助协同','让每一次异常上报都有回应'],volunteer:['志愿服务','可维护的值班与照护任务'],admin:['管理后台','用数据提升校园猫治理效率']}
const visibleNav=computed(()=>nav.filter(([key])=>key!=='admin'||user.value?.role==='ADMIN'))
const campusCats=computed(()=>cats.value.filter(c=>c.schoolStatus==='在校'))
const mapLocations=[
  {id:'building-11',label:'原11 · 和熙苑1号',x:16,y:13,keywords:['十一号楼','11号楼']},
  {id:'building-12',label:'原12 · 和合苑2号',x:84,y:13,keywords:['十二号楼','12号楼']},
  {id:'building-9',label:'原9 · 和合苑1号',x:77,y:17,keywords:['九号楼','9号楼']},
  {id:'building-5',label:'原5 · 和勉苑1号',x:29,y:20,keywords:['五号楼','5号楼']},
  {id:'building-3',label:'原3 · 和勉苑3号',x:36,y:21,keywords:['三号楼','3号楼']},
  {id:'building-4',label:'原4 · 和勉苑2号',x:24,y:24,keywords:['四号楼','4号楼']},
  {id:'building-2',label:'原2 · 和畅苑1号',x:39,y:25,keywords:['二号楼','2号楼']},
  {id:'building-1',label:'原1 · 和畅苑2号',x:42,y:28,keywords:['一号楼','1号楼']},
  {id:'building-10',label:'原10 · 和畅苑5号',x:55,y:31,keywords:['十号楼','10号楼']},
  {id:'building-7',label:'原7 · 和畅苑3号',x:31,y:36,keywords:['七号公寓','七号楼','7号楼']},
  {id:'building-6',label:'原6 · 和畅苑4号',x:23,y:39,keywords:['六号楼','6号楼']},
  {id:'tunnel',label:'教学区地下通道',x:35,y:52,keywords:['地下通道','桥洞']},
  {id:'library',label:'图书馆',x:58,y:43,keywords:['图书馆']},
  {id:'red-stairs',label:'教学区红砖楼梯',x:29,y:69,keywords:['红砖楼梯']},
  {id:'music-fountain',label:'教学区音乐喷泉',x:37,y:60,keywords:['音乐喷泉']},
  {id:'wendao',label:'闻道楼',x:16,y:66,keywords:['闻道楼']},
  {id:'parking',label:'停车场',x:59,y:31,keywords:['停车场']},
  {id:'mountain',label:'龙山及中区',x:44,y:34,keywords:['猫山','山上','以山为中心']},
  {id:'cainiao',label:'菜鸟驿站及食堂',x:29,y:29,keywords:['菜鸟','食堂中间']},
  {id:'bathhouse',label:'生活区澡堂',x:34,y:28,keywords:['澡堂']},
  {id:'supermarket',label:'生活区超市',x:36,y:24,keywords:['生活区超市']},
  {id:'sports-office',label:'操场社办',x:77,y:35,keywords:['操场社办']},
  {id:'liushui',label:'流水人家',x:93,y:35,keywords:['流水人家']},
  {id:'wide-range',label:'活动范围较广',x:64,y:49,keywords:['到处溜达','爱跑']},
]
function mapPointFor(cat){
  const area=String(cat.area||'')
  return mapLocations.find(point=>point.keywords.some(keyword=>area.includes(keyword)))||{id:'unknown',label:'活动位置待补充',x:71,y:52}
}
const mapGroups=computed(()=>{
  const groups=new Map()
  campusCats.value.forEach(cat=>{
    const point=mapPointFor(cat)
    if(!groups.has(point.id))groups.set(point.id,{...point,cats:[]})
    groups.get(point.id).cats.push(cat)
  })
  return [...groups.values()]
})
const selectedMapGroup=computed(()=>mapGroups.value.find(group=>group.id===selectedMapLocation.value)||null)
const pendingLocationCount=computed(()=>mapGroups.value.find(group=>group.id==='unknown')?.cats.length||0)
const catNameCollator=new Intl.Collator('zh-CN-u-co-pinyin',{sensitivity:'base',numeric:true})
const filteredCats=computed(()=>cats.value.filter(c=>{
  const inScope=catScope.value==='all'||(catScope.value==='campus'?c.schoolStatus==='在校':c.schoolStatus!=='在校')
  const query=catQuery.value.trim().toLowerCase()
  return inScope&&(!query||[c.name,c.code,c.area,c.appearance,c.notes].some(value=>String(value||'').toLowerCase().includes(query)))
}).sort((a,b)=>catNameCollator.compare(a.name||'',b.name||'')))

function notify(message){toast.value=message;setTimeout(()=>toast.value='',2400)}
function statusClass(s=''){return /治疗|紧急|异常|驳回|失踪|离世|去世|车祸/.test(s)?'danger':/等待|观察|待|缺人/.test(s)?'warn':/完成|正常|已安排|校园生活/.test(s)?'':'info'}
function catEmoji(c){return c.name==='煤球'?'🐈‍⬛':'🐈'}
function go(name){if(name==='admin'&&user.value?.role!=='ADMIN')return;route.value=name;if(name==='admin')loadAdmin()}
function switchAuth(mode){authMode.value=mode;error.value=''}
function catImages(cat){return cat?.imageUrls?.length?cat.imageUrls:(cat?.imageUrl?[cat.imageUrl]:[])}
function openCatDetail(cat){detailCat.value=cat;detailImage.value=catImages(cat)[0]||''}
function closeCatDetail(){detailCat.value=null;detailImage.value=''}
function selectMapGroup(group){selectedMapLocation.value=selectedMapLocation.value===group.id?'':group.id}

async function login(){loading.value=true;error.value='';try{const data=await api.login(loginForm.username,loginForm.password);setToken(data.token);user.value=data;await loadAll();notify(`欢迎，${data.name}`)}catch(e){error.value=e.message}finally{loading.value=false}}
async function register(){
  if(registerForm.password!==registerForm.confirmPassword){error.value='两次输入的密码不一致';return}
  loading.value=true;error.value=''
  try{const data=await api.register(registerForm.username,registerForm.displayName,registerForm.password);setToken(data.token);user.value=data;await loadAll();notify(`注册成功，欢迎你，${data.name}`)}catch(e){error.value=e.message}finally{loading.value=false}
}
async function logout(){try{await api.logout()}catch{}setToken(null);user.value=null;route.value='dashboard'}
async function restore(){if(!getToken())return;try{user.value=await api.me();await loadAll()}catch{setToken(null);user.value=null}}
async function loadAll(){loading.value=true;error.value='';try{const [dashboardResult,catResult,rescueResult,volunteerResult]=await Promise.allSettled([api.dashboard(),api.cats(),api.rescues(),api.volunteers()]);if(dashboardResult.status==='fulfilled')Object.assign(stats,dashboardResult.value);if(catResult.status==='fulfilled')cats.value=catResult.value;else error.value=`健康档案加载失败：${catResult.reason.message}`;if(rescueResult.status==='fulfilled')rescues.value=rescueResult.value;if(volunteerResult.status==='fulfilled')volunteers.value=volunteerResult.value;if(user.value.role==='ADMIN')await loadAdmin()}finally{loading.value=false}}
async function loadAdmin(){if(user.value?.role==='ADMIN')adminMetrics.value=await api.adminMetrics()}

function openModal(type,payload={}){modal.value=type;Object.keys(form).forEach(k=>delete form[k]);const defaults=type==='rescue'?{priority:'MEDIUM',status:'待接单',ownerName:'暂未指派'}:type==='volunteer'?{status:'待安排',ownerName:'待认领'}:{};Object.assign(form,defaults,payload)}
function closeModal(){modal.value=null}
async function submitModal(){loading.value=true;error.value='';try{
  const {id,createdAt,...payload}=form
  if(modal.value==='cat')await api.createCat(form)
  if(modal.value==='rescue')id?await api.updateRescue(id,payload):await api.createRescue(payload)
  if(modal.value==='volunteer')id?await api.updateVolunteer(id,payload):await api.createVolunteer(payload)
  closeModal();await loadAll();notify('信息已保存到 MySQL 数据库')
}catch(e){error.value=e.message}finally{loading.value=false}}
async function accept(id){try{await api.acceptRescue(id);await loadAll();notify('接单成功')}catch(e){notify(e.message)}}
async function removeRescue(id){if(!window.confirm('确定删除这条救助任务吗？'))return;try{await api.deleteRescue(id);await loadAll();notify('救助任务已删除')}catch(e){notify(e.message)}}
async function removeVolunteer(id){if(!window.confirm('确定删除这条志愿任务吗？'))return;try{await api.deleteVolunteer(id);await loadAll();notify('志愿任务已删除')}catch(e){notify(e.message)}}
window.addEventListener('auth-expired',()=>{user.value=null;error.value='登录已过期，请重新登录'})
onMounted(restore)
</script>

<template>
  <div v-if="!user" class="login-page">
    <section class="login-visual"><div class="login-brand"><span>🐾</span><strong>猫伴校园</strong></div><div class="login-message"><div class="eyebrow">CATMATE CAMPUS</div><h1>让守护从登录开始，<br>让每份记录持续发生。</h1><p>汇聚校园猫健康档案、救助信息与志愿协作，让每一次守护都有迹可循。</p></div><div class="login-caption">校园猫救助 · 健康档案 · 志愿协作</div></section>
    <section class="login-panel"><div class="login-card"><div class="login-logo">🐈</div><div class="auth-tabs"><button type="button" :class="{active:authMode==='login'}" @click="switchAuth('login')">登录</button><button type="button" :class="{active:authMode==='register'}" @click="switchAuth('register')">注册</button></div><form v-if="authMode==='login'" @submit.prevent="login"><h2>欢迎回来</h2><p>登录“猫伴校园”继续参与守护</p><div class="field"><label>账号</label><input v-model="loginForm.username" required autocomplete="username" placeholder="请输入账号"></div><div class="field"><label>密码</label><input v-model="loginForm.password" required autocomplete="current-password" type="password" placeholder="请输入密码"></div><div class="login-error">{{ error }}</div><button class="btn btn-primary login-submit" :disabled="loading">{{ loading?'登录中…':'登录系统' }}</button></form><form v-else @submit.prevent="register"><h2>创建账号</h2><p>注册后即可进入校园猫守护平台</p><div class="field"><label>账号</label><input v-model.trim="registerForm.username" required minlength="3" maxlength="20" pattern="[A-Za-z0-9_]+" autocomplete="username" placeholder="3-20位字母、数字或下划线"></div><div class="field"><label>昵称</label><input v-model.trim="registerForm.displayName" required maxlength="20" autocomplete="name" placeholder="请输入你的昵称"></div><div class="field"><label>密码</label><input v-model="registerForm.password" required minlength="6" maxlength="50" autocomplete="new-password" type="password" placeholder="至少6个字符"></div><div class="field"><label>确认密码</label><input v-model="registerForm.confirmPassword" required minlength="6" maxlength="50" autocomplete="new-password" type="password" placeholder="请再次输入密码"></div><div class="login-error">{{ error }}</div><button class="btn btn-primary login-submit" :disabled="loading">{{ loading?'注册中…':'注册并进入系统' }}</button><p class="register-note">新注册账号均为普通用户，管理员权限不可自行申请。</p></form></div></section>
  </div>

  <div v-else class="app-shell">
    <aside class="sidebar"><div class="brand"><div class="brand-mark">🐾</div><div class="brand-copy"><h1>猫伴校园</h1><p>CATMATE CAMPUS</p></div></div><nav class="nav"><button v-for="[key,icon,label] in visibleNav" :key="key" class="nav-button" :class="{active:route===key}" @click="go(key)"><span class="nav-icon">{{icon}}</span><span class="nav-label">{{label}}</span></button></nav><div class="sidebar-foot"><div class="account-summary"><span>{{user.role==='ADMIN'?'🛡️':'👤'}}</span><div><strong>{{user.name}}</strong><small>{{user.role==='ADMIN'?'管理者账号':'普通用户账号'}}</small></div></div><button class="logout-button" @click="logout">退出登录</button></div></aside>
    <main class="main"><header class="topbar"><div class="topbar-title"><h2>{{titles[route][0]}}</h2><p>{{titles[route][1]}}</p></div><div class="topbar-actions"><button class="icon-button" @click="notify('你有 3 条待处理消息')">🔔<span class="notification-dot"></span></button><button class="btn btn-primary" @click="openModal('rescue')">＋ 上报发现</button><button class="avatar" @click="logout" title="点击退出">{{user.role==='ADMIN'?'🛡️':'👤'}}</button></div></header>
      <div class="content">
        <template v-if="route==='dashboard'"><section class="hero"><div class="hero-copy"><div class="eyebrow">CATMATE CAMPUS · 实时数据</div><h3>让每一只校园猫，<br>都有连续可见的守护。</h3><p>从校园发现、健康建档到救助与志愿协作，每次操作都通过 REST API 保存到数据库。</p><div class="hero-actions"><button class="btn btn-primary" @click="openModal('rescue')">📍 上报发现</button><button class="btn btn-secondary" @click="go('volunteer')">♡ 志愿任务</button></div></div></section><section class="stats-grid"><div class="stat-card"><div class="stat-icon">🐈</div><div><div class="stat-value">{{stats.cats}}</div><div class="stat-label">猫咪档案</div></div></div><div class="stat-card"><div class="stat-icon">🩺</div><div><div class="stat-value">{{stats.activeRescues}}</div><div class="stat-label">进行中救助</div></div></div><div class="stat-card"><div class="stat-icon">♡</div><div><div class="stat-value">{{stats.volunteerTasks}}</div><div class="stat-label">志愿任务</div></div></div></section><section class="grid-2"><div class="card"><div class="section-head"><div><h3>待处理救助</h3><p>高优先级任务优先响应</p></div></div><div v-if="!rescues.length" class="empty">暂无救助任务</div><div v-for="r in rescues.slice(0,3)" :key="r.id" class="task"><span class="task-priority" :class="r.priority.toLowerCase()"></span><div><h4>{{r.title}}</h4><p>{{r.area}} · {{r.ownerName}}</p></div><span class="status" :class="statusClass(r.status)">{{r.status}}</span></div></div><div class="card"><h3 class="panel-title">数据持久化状态</h3><div class="notice">猫咪档案、救助任务、志愿任务和登录会话均由 MySQL 保存。</div><div class="metric-row"><span>档案完善度</span><strong>82%</strong></div><div class="progress"><span style="width:82%"></span></div><div class="metric-row"><span>救助响应率</span><strong>91%</strong></div><div class="progress"><span style="width:91%"></span></div></div></section></template>

        <template v-else-if="route==='map'"><div class="map-summary"><div><strong>在校猫咪 {{campusCats.length}} 只</strong><span>依据健康档案中的活动区域标注，同一地点自动合并</span></div><span v-if="pendingLocationCount" class="status warn">{{pendingLocationCount}} 只待补充位置</span></div><div class="map-layout"><div class="campus-map-scroll card"><div class="campus-map" role="img" aria-label="金石滩校区彩色地图及在校猫咪分布"><button v-for="group in mapGroups" :key="group.id" type="button" class="marker" :class="{active:selectedMapLocation===group.id,unknown:group.id==='unknown'}" :style="{left:group.x+'%',top:group.y+'%'}" :aria-label="`${group.label}，${group.cats.length}只猫`" @click="selectMapGroup(group)"><span class="marker-bubble"><span>🐾</span><b>{{group.cats.length}}</b></span><span class="marker-label">{{group.label}}</span></button></div></div><aside class="map-side card"><h3 class="panel-title">在校猫咪 · {{campusCats.length}} 只</h3><section v-if="selectedMapGroup" class="map-selection"><div class="map-selection-head"><div><small>当前地点</small><h4>{{selectedMapGroup.label}}</h4></div><button type="button" class="modal-close" aria-label="取消地点选择" @click="selectedMapLocation=''">✕</button></div><button v-for="c in selectedMapGroup.cats" :key="c.id" type="button" class="map-cat-chip" @click="openCatDetail(c)"><img v-if="c.imageUrl" :src="c.imageUrl" :alt="c.name"><span v-else>{{catEmoji(c)}}</span><span><strong>{{c.name}}</strong><small>{{c.health||'健康情况未记录'}}</small></span></button></section><p v-else class="map-guide">点击地图上的橙色标记，查看该地点的小猫和健康信息。</p><div class="map-list"><button v-for="c in campusCats" :key="c.id" type="button" class="list-item map-list-button" @click="openCatDetail(c)"><img v-if="c.imageUrl" class="list-photo" :src="c.imageUrl" :alt="c.name"><span v-else class="list-avatar">{{catEmoji(c)}}</span><span class="list-main"><h4>{{c.name}}</h4><p>{{c.area||'活动区域待补充'}}<br>{{c.health||'健康情况未记录'}}</p></span><span class="status" :class="statusClass(c.status)">{{c.status}}</span></button></div></aside></div></template>

        <template v-else-if="route==='cats'"><div class="section-head"><div><h3>全部校园猫档案</h3><p>已整合表格数据 {{cats.length}} 条，其中在校 {{campusCats.length}} 条 · 按姓名首字母排序</p></div><button class="btn btn-primary" @click="openModal('cat',{status:'待确认',mapX:50,mapY:50})">＋ 新建档案</button></div><div class="toolbar"><div class="search"><input v-model="catQuery" placeholder="搜索姓名、区域、特征或备注"></div><select v-model="catScope"><option value="all">全部状态</option><option value="campus">仅看在校</option><option value="former">非在校记录</option></select><span class="muted small">找到 {{filteredCats.length}} 条</span></div><div class="grid-3"><article v-for="c in filteredCats" :key="c.id" class="card cat-card" role="button" tabindex="0" :aria-label="`查看${c.name}的详细档案`" @click="openCatDetail(c)" @keydown.enter="openCatDetail(c)"><div class="cat-cover" :class="{photo:!!c.imageUrl}" :data-emoji="c.imageUrl?'':catEmoji(c)"><img v-if="c.imageUrl" class="cat-photo" :src="c.imageUrl" :alt="c.name" loading="lazy"></div><div class="cat-body"><div class="cat-card-badges"><span class="status" :class="statusClass(c.status)">{{c.status}}</span><span v-if="catImages(c).length" class="photo-count">📷 {{catImages(c).length}}</span></div><div class="cat-title"><h4>{{c.name}}</h4><span v-if="c.friendliness" class="friendliness">亲人度 {{c.friendliness}}/5</span></div><div class="cat-meta">{{c.sex||'未知'}} · {{c.enrollmentTime?c.enrollmentTime+' 入校':'入校时间未知'}} · {{c.area||'区域待补充'}}</div><div class="tag-row"><span v-if="c.health" class="tag">{{c.health}}</span><span v-if="c.personality" class="tag">{{c.personality}}</span></div><p v-if="c.appearance" class="cat-description">特征：{{c.appearance}}</p><span class="cat-card-more">查看完整档案与相册 →</span></div></article><div v-if="!filteredCats.length" class="empty card"><div class="empty-icon">🐾</div><h4>没有找到符合条件的猫咪</h4><p>试试更换关键词或状态筛选。</p></div></div></template>

        <template v-else-if="route==='rescue'"><div class="section-head"><div><h3>救助任务队列</h3><p>可新增、接单、编辑或删除，修改实时写入数据库</p></div><button class="btn btn-primary" @click="openModal('rescue',{priority:'MEDIUM',status:'待接单',ownerName:'暂未指派'})">＋ 发起救助</button></div><div class="card"><div v-if="!rescues.length" class="empty"><div class="empty-icon">✚</div><h4>暂无救助任务</h4><p>旧演示记录已清理，可以从右上角新建任务。</p></div><div v-for="r in rescues" :key="r.id" class="task"><span class="task-priority" :class="r.priority.toLowerCase()"></span><div><h4>{{r.title}}</h4><p>{{r.catName}} · {{r.area}} · {{r.ownerName}}</p></div><div class="task-actions"><button v-if="r.status==='待接单'" class="btn btn-primary btn-sm" @click="accept(r.id)">接单</button><button class="btn btn-secondary btn-sm" @click="openModal('rescue',r)">编辑</button><button class="btn btn-ghost btn-sm" @click="removeRescue(r.id)">删除</button><span class="status" :class="statusClass(r.status)">{{r.status}}</span></div></div></div></template>

        <template v-else-if="route==='volunteer'"><div class="section-head"><div><h3>志愿任务</h3><p>值班、投喂、清洁和物资协作统一维护</p></div><button class="btn btn-primary" @click="openModal('volunteer',{status:'待安排',ownerName:'待认领'})">＋ 新建志愿任务</button></div><div class="card"><div v-if="!volunteers.length" class="empty"><div class="empty-icon">♡</div><h4>暂无志愿任务</h4><p>原硬编码历史记录已清理，可以新建本周安排。</p></div><div v-for="v in volunteers" :key="v.id" class="list-item volunteer-item"><span class="list-avatar">🥣</span><span class="list-main"><h4>{{v.title}}</h4><p>{{v.scheduleText}} · {{v.ownerName||'待认领'}}</p><small v-if="v.notes">{{v.notes}}</small></span><div class="task-actions"><button class="btn btn-secondary btn-sm" @click="openModal('volunteer',v)">编辑</button><button class="btn btn-ghost btn-sm" @click="removeVolunteer(v.id)">删除</button><span class="status" :class="statusClass(v.status)">{{v.status}}</span></div></div></div></template>

        <template v-else-if="route==='admin' && user.role==='ADMIN'"><div v-if="adminMetrics" class="stats-grid"><div class="stat-card"><div class="stat-icon">🐾</div><div><div class="stat-value">{{adminMetrics.catCount}}</div><div class="stat-label">猫咪档案</div></div></div><div class="stat-card"><div class="stat-icon">⚡</div><div><div class="stat-value">{{adminMetrics.rescueCount}}</div><div class="stat-label">救助任务</div></div></div><div class="stat-card"><div class="stat-icon">♡</div><div><div class="stat-value">{{adminMetrics.volunteerCount}}</div><div class="stat-label">志愿任务</div></div></div><div class="stat-card"><div class="stat-icon">👥</div><div><div class="stat-value">{{adminMetrics.userCount}}</div><div class="stat-label">系统账号</div></div></div></div><div class="grid-2"><div class="card"><h3>治理指标</h3><div v-for="[n,v] in [['档案完善度',adminMetrics?.profileCompleteness],['健康记录覆盖率',adminMetrics?.healthCoverage],['救助闭环率',adminMetrics?.rescueClosureRate]]"><div class="metric-row"><span>{{n}}</span><strong>{{v}}%</strong></div><div class="progress"><span :style="{width:v+'%'}"></span></div></div></div><div class="card"><h3>权限说明</h3><div class="notice">本页面由后端 `/api/admin/**` 权限拦截器保护。普通用户即使手动调用接口，也会收到 403。</div></div></div></template>
      </div>
    </main>
  </div>

  <div v-if="detailCat" class="modal-backdrop" @click.self="closeCatDetail" @keydown.esc.window="closeCatDetail"><article class="cat-detail-modal" role="dialog" aria-modal="true" :aria-label="`${detailCat.name}的猫咪详情`"><header class="cat-detail-head"><div><span class="status" :class="statusClass(detailCat.status)">{{detailCat.status}}</span><h3>{{detailCat.name}}</h3><p>{{detailCat.code}}</p></div><button type="button" class="modal-close" aria-label="关闭猫咪详情" @click="closeCatDetail">✕</button></header><div class="cat-detail-body"><section class="cat-gallery"><div class="cat-gallery-main"><img v-if="detailImage" :src="detailImage" :alt="detailCat.name"><span v-else>{{catEmoji(detailCat)}}</span></div><div v-if="catImages(detailCat).length>1" class="cat-gallery-thumbs"><button v-for="(image,index) in catImages(detailCat)" :key="image" type="button" :class="{active:image===detailImage}" :aria-label="`查看第${index+1}张照片`" @click="detailImage=image"><img :src="image" :alt="`${detailCat.name}第${index+1}张照片`" loading="lazy"></button></div><p class="small muted">{{catImages(detailCat).length?`共 ${catImages(detailCat).length} 张照片`:'原始表格暂无照片'}}</p></section><section class="cat-detail-info"><div class="cat-detail-grid"><div><span>性别</span><strong>{{detailCat.sex||'未知'}}</strong></div><div><span>入校时间</span><strong>{{detailCat.enrollmentTime||'未记录'}}</strong></div><div><span>活动区域</span><strong>{{detailCat.area||'未记录'}}</strong></div><div><span>亲人度</span><strong>{{detailCat.friendliness?detailCat.friendliness+'/5':'未记录'}}</strong></div><div><span>在校状态</span><strong>{{detailCat.schoolStatus||'未记录'}}</strong></div><div><span>绝育情况</span><strong>{{detailCat.health||'未记录'}}</strong></div></div><div v-if="detailCat.personality" class="cat-detail-section"><h4>健康状态</h4><p>{{detailCat.personality}}</p></div><div v-if="detailCat.appearance" class="cat-detail-section"><h4>辨识特征</h4><p>{{detailCat.appearance}}</p></div><div v-if="detailCat.notes" class="cat-detail-section"><h4>完整备注</h4><p>{{detailCat.notes}}</p></div></section></div></article></div>

  <div v-if="modal" class="modal-backdrop" @click.self="closeModal"><form class="modal" @submit.prevent="submitModal"><div class="modal-head"><h3>{{modal==='cat'?'新建猫咪档案':modal==='rescue'?(form.id?'编辑救助任务':'发起救助任务'):(form.id?'编辑志愿任务':'新建志愿任务')}}</h3><button type="button" class="modal-close" @click="closeModal">✕</button></div><div class="modal-body form-grid">
    <template v-if="modal==='cat'"><div class="field"><label>猫咪名称</label><input v-model="form.name" required></div><div class="field"><label>其他昵称</label><input v-model="form.aliases"></div><div class="field"><label>性别</label><select v-model="form.sex"><option>未知</option><option>公</option><option>母</option></select></div><div class="field"><label>大致年龄</label><input v-model="form.ageText"></div><div class="field"><label>常见区域</label><input v-model="form.area" required></div><div class="field"><label>当前状态</label><select v-model="form.status"><option>待确认</option><option>校园生活中</option><option>观察中</option><option>等待领养</option></select></div><div class="field"><label>健康情况</label><input v-model="form.health"></div><div class="field"><label>性格</label><input v-model="form.personality"></div></template>
    <template v-if="modal==='rescue'"><div class="field"><label>关联猫咪</label><select v-model="form.catName" required><option v-for="c in cats" :key="c.id">{{c.name}}</option></select></div><div class="field"><label>紧急程度</label><select v-model="form.priority" required><option value="HIGH">高</option><option value="MEDIUM">中</option><option value="LOW">低</option></select></div><div class="field full"><label>发现位置</label><input v-model="form.area" required></div><div class="field full"><label>异常描述</label><textarea v-model="form.title" required></textarea></div><template v-if="form.id"><div class="field"><label>任务状态</label><select v-model="form.status" required><option>待接单</option><option>前往中</option><option>治疗中</option><option>已完成</option></select></div><div class="field"><label>负责人</label><input v-model="form.ownerName" required></div></template></template>
    <template v-if="modal==='volunteer'"><div class="field full"><label>志愿任务</label><input v-model="form.title" required placeholder="例如：图书馆饮水点清洁"></div><div class="field"><label>服务时间</label><input v-model="form.scheduleText" required placeholder="例如：周二 18:30"></div><div class="field"><label>负责人</label><input v-model="form.ownerName" placeholder="待认领"></div><div class="field"><label>任务状态</label><select v-model="form.status" required><option>待安排</option><option>待认领</option><option>进行中</option><option>已完成</option></select></div><div class="field full"><label>备注</label><textarea v-model="form.notes"></textarea></div></template>
    <div v-if="error" class="login-error full">{{error}}</div>
  </div><div class="modal-foot"><button type="button" class="btn btn-ghost" @click="closeModal">取消</button><button class="btn btn-primary" :disabled="loading">{{loading?'保存中…':'保存到数据库'}}</button></div></form></div>
  <div v-if="toast" class="toast app-toast">{{toast}}</div>
</template>
